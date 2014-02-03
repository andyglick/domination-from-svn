package net.yura.domination.android;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.InvitationBuffer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.OnInvitationsLoadedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeReliableMessageSentListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.example.games.basegameutils.GameHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;
import net.yura.android.AndroidMeActivity;
import net.yura.android.AndroidMeApp;
import net.yura.android.AndroidPreferences;
import net.yura.android.LoadingDialog;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.domination.mobile.flashgui.DominationMain;
import net.yura.domination.mobile.flashgui.MiniFlashRiskAdapter;
import net.yura.lobby.client.ProtoAccess;
import net.yura.lobby.model.Message;

public class GameActivity extends AndroidMeActivity implements GameHelper.GameHelperListener,DominationMain.GooglePlayGameServices {

    private static final Logger logger = Logger.getLogger(GameActivity.class.getName());

    private static final int GOOGLE_PLAY_GAME_MIN_OTHER_PLAYERS = 1;

    private static final int RC_REQUEST_ACHIEVEMENTS = 1;
    private static final int RC_SELECT_PLAYERS = 2;
    private static final int RC_CREATOR_WAITING_ROOM = 3;
    private static final int RC_JOINER_WAITING_ROOM = 4;

    protected GameHelper mHelper;
    private ProtoAccess encodeDecoder = new ProtoAccess(null);

    private Room gameRoom;
    private net.yura.lobby.model.Game lobbyGame;

    private String pendingAchievement;
    private boolean pendingShowAchievements;
    private net.yura.lobby.model.Game pendingStartGameGooglePlay;
    private boolean pendingSendLobbyUsername;

    @Override
    protected void onSingleCreate() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        DominationMain.appPreferences = new AndroidPreferences(preferences);

        super.onSingleCreate();

        mHelper = new GameHelper(this);
        mHelper.enableDebugLog(true, "DominationPlay");
        mHelper.setup(this, GameHelper.CLIENT_GAMES);
    }

    @Override
    public void onMidletStarted() {
        DominationMain dmain = (DominationMain)AndroidMeApp.getMIDlet();
        dmain.setGooglePlayGameServices(GameActivity.this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mHelper.onStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHelper.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_REQUEST_ACHIEVEMENTS) {
            // Nothing to do.
            return;
        }
        if (requestCode == RC_SELECT_PLAYERS) {
            handlePlayersSelected(resultCode, data);
            return;
        }
        if (requestCode == RC_CREATOR_WAITING_ROOM) {
            handleReturnFromWaitingRoom(resultCode, true /* isCreator */);
            return;
        }
        if (requestCode == RC_JOINER_WAITING_ROOM) {
            handleReturnFromWaitingRoom(resultCode, false /* not creator */);
            return;
        }
        mHelper.onActivityResult(requestCode, resultCode, data);
    }

    private void closeLoadingDialog() {
        Intent intent = new Intent(this, LoadingDialog.class);
        intent.putExtra(LoadingDialog.PARAM_COMMAND, "hide");
        startActivity(intent);
    }

    private void openLoadingDialog(String messageName) {
        Intent intent = new Intent(this, LoadingDialog.class);
        intent.putExtra(LoadingDialog.PARAM_MESSAGE, TranslationBundle.getBundle().getString(messageName));
        intent.putExtra(LoadingDialog.PARAM_CANCELLABLE, true);
        startActivity(intent);
    }

    private void handlePlayersSelected(int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            logger.info("Player selection failed. "+resultCode);
            return;
        }
        ArrayList<String> invitees = data.getStringArrayListExtra(GamesClient.EXTRA_PLAYERS);
        logger.info("Players selected. Creating room.");
        openLoadingDialog("mainmenu.googlePlayGame.waitRoom");
        mHelper.getGamesClient().createRoom(RoomConfig.builder(
                new BaseRoomUpdateListener() {
                    @Override
                    public void onRoomCreated(int statusCode, Room room) {
                        super.onRoomCreated(statusCode, room);
                        closeLoadingDialog();
                        if (statusCode != GamesClient.STATUS_OK) {
                            String error = "onRoomCreated failed. "+statusCode+" "+getErrorString(statusCode);
                            logger.warning(error);
                            toast(error);
                            return;
                        }
                        gameRoom = room;
                        logger.info("Starting waiting room activity.");
                        startActivityForResult(mHelper.getGamesClient().getRealTimeWaitingRoomIntent(room, 1), RC_CREATOR_WAITING_ROOM);
                    }
                })
                .setRoomStatusUpdateListener(new BaseRoomStatusUpdateListener(){
                    @Override
                    public void onRoomUpdated(Room room) {
                        super.onRoomUpdated(room);
                        gameRoom = room;
                    }
                })
                .setMessageReceivedListener(new RealTimeMessageReceivedListener() {
                    @Override
                    public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {
                        onMessageReceived(realTimeMessage);
                    }
                })
                .addPlayersToInvite(invitees).build());
        logger.info("Room created, waiting for it to be ready");
    }

    private void onMessageReceived(RealTimeMessage realTimeMessage) {
        byte[] data = realTimeMessage.getMessageData();
        try {
            Message message = (Message)encodeDecoder.load(new ByteArrayInputStream(data), data.length);
            onMessageReceived(message);
        }
        catch (IOException ex) {
            logger.log(Level.WARNING, "can not decode", ex);
        }
    }

    private void onMessageReceived(Message message) {
        logger.info("Room message received: " + message);
        if (ProtoAccess.REQUEST_JOIN_GAME.equals(message.getCommand())) {
            String name = (String)message.getParam();
            lobbyGame.getPlayers().add(new net.yura.lobby.model.Player(name, 0));

            int joined = getParticipantStatusCount(Participant.STATUS_JOINED);
            logger.info("new player joined: "+name+" "+lobbyGame.getNumOfPlayers()+"/"+joined+"/"+gameRoom.getParticipantIds().size());
            if (lobbyGame.getNumOfPlayers() == joined) {
                // TODO can we be not inside lobby???
                // TODO can we be not logged in?
                // in case we decided to start with less then the max number of human players
                // we need to update the max number to the current number, so no one else can join
                lobbyGame.setMaxPlayers(lobbyGame.getNumOfPlayers());
                getUi().lobby.createNewGame(lobbyGame);
            }
        }
        else if (ProtoAccess.COMMAND_GAME_STARTED.equals(message.getCommand())) {
            int gameId = (Integer)message.getParam();
            getUi().lobby.playGame(gameId);
        }
        else {
            logger.warning("unknown command "+message);
        }
    }

    private int getParticipantStatusCount(int status) {
        int count=0;
        for (String id: gameRoom.getParticipantIds()) {
            if (gameRoom.getParticipantStatus(id) == status) {
                count++;
            }
        }
        return count;
    }

    private void handleReturnFromWaitingRoom(int resultCode, boolean isCreator) {
        logger.info("Returning from waiting room. isCreator="+isCreator);
        if (resultCode != Activity.RESULT_OK) {
            logger.info("Room was cancelled, result code = " + resultCode);
            return;
        }
        logger.info("Room ready.");
        openLoadingDialog("mainmenu.googlePlayGame.waitGame");
        if (!isCreator) {
            MiniFlashRiskAdapter ui = getUi();
            if (ui.lobby != null) {
                if (ui.lobby.whoAmI() != null) {
                    sendLobbyUsername(ui.lobby.whoAmI());
                }
                else {
                    pendingSendLobbyUsername = true;
                    logger.warning("lobby open but we do not have a username");
                }
            }
            else {
                pendingSendLobbyUsername = true;
                ui.openLobby();
            }
        }
    }

    public void setLobbyUsername(String username) {
        if (pendingSendLobbyUsername) {
            pendingSendLobbyUsername = false;
            sendLobbyUsername(username);
        }
    }

    private void sendLobbyUsername(String username) {
        logger.info("Sending ID to creator.");

        Message message = new Message();
        message.setCommand(ProtoAccess.REQUEST_JOIN_GAME);
        message.setParam(username);

        sendMessage(message, gameRoom.getCreatorId());
    }

    @Override
    public void gameStarted(int id) {
        logger.info("lobby gameStarted "+id+" "+gameRoom);
        if (gameRoom != null) {
            Message message = new Message();
            message.setCommand(ProtoAccess.COMMAND_GAME_STARTED);
            message.setParam(id);

            String me = gameRoom.getCreatorId();
            List<String> participants = gameRoom.getParticipantIds();
            for (String participant : participants) {
                // Play Games throws a error if i tell it to send a message to myself
                if (participant.equals(me)) {
                    onMessageReceived(message);
                }
                else {
                    sendMessage(message, participant);
                }
            }
        }
    }

    void sendMessage(Message message, String recipientParticipantId) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream( encodeDecoder.computeAnonymousObjectSize(message) );
        try {
            encodeDecoder.save(bytes, message);
        }
        catch (IOException ex) {
            throw new RuntimeException("can not encode", ex);
        }
        byte[] data = bytes.toByteArray();

        mHelper.getGamesClient().sendReliableRealTimeMessage(new RealTimeReliableMessageSentListener() {
            @Override
            public void onRealTimeMessageSent(int statusCode, int tokenId, String recipientId) {
                logger.info(String.format("Message %d sent (%d) to %s", tokenId, statusCode, recipientId));
            }
        }, data, gameRoom.getRoomId(), recipientParticipantId);
    }

    @Override
    public void beginUserInitiatedSignIn() {
        mHelper.beginUserInitiatedSignIn();
    }

    @Override
    public void signOut() {
        mHelper.signOut();
        mHelper.getGamesClient().unregisterInvitationListener();
    }

    @Override
    public void onSignInSucceeded() {
        logger.info("onSignInSucceeded()");
	MiniFlashRiskAdapter ui = getUi();
	if (ui!=null) {
	    ui.playGamesStateChanged();
	}
        if (pendingAchievement!=null) {
            unlockAchievement(pendingAchievement);
            pendingAchievement=null;
        }
        if (pendingShowAchievements) {
            pendingShowAchievements = false;
            showAchievements();
        }
        if (pendingStartGameGooglePlay != null) {
            startGameGooglePlay(pendingStartGameGooglePlay);
            pendingStartGameGooglePlay = null;
        }
        // TODO: find why this is always null when there *is* a pending invitation
        logger.info("invitationId: " + mHelper.getInvitationId());
        if (mHelper.getInvitationId() != null) {
            acceptInvitation(mHelper.getInvitationId());
        }
        else {
            // there is a bug in GooglePlayGameServicesFroyo that mHelper.getInvitationId() returns null
            // even when there is a invitation, so we use this method instead to get it.
            mHelper.getGamesClient().loadInvitations(new OnInvitationsLoadedListener() {
                @Override
                public void onInvitationsLoaded(int statusCode, InvitationBuffer buffer) {
                    logger.info("onInvitationsLoaded: "+statusCode+" "+buffer.getCount()+" "+buffer);
                    for (Invitation invitation : buffer) {
                        logger.info("onInvitationsLoaded invitation: "+getCurrentPlayerState(invitation)+" "+invitation);
                        // TODO this is not good enough, as maybe we have already accepted the invitation.
                        createAcceptDialog(invitation).show();
                    }
                    // LOL who closes a buffer!
                    buffer.close();
                }
            });
        }
        mHelper.getGamesClient().registerInvitationListener(new OnInvitationReceivedListener() {
            @Override
            public void onInvitationReceived(final Invitation invitation) {
                logger.info("Invitation received from: " + invitation.getInviter());
                createAcceptDialog(invitation).show();
            }
        });
    }

    private int getCurrentPlayerState(Invitation invitation) {
        List<Participant> participants = invitation.getParticipants();
        for (Participant participant : participants) {
            Player player = participant.getPlayer();
            if (player.getPlayerId().equals(mHelper.getGamesClient().getCurrentPlayerId())) {
                return participant.getStatus();
            }
        }
        throw new RuntimeException("me not found");
    }

    private AlertDialog createAcceptDialog(Invitation invitation) {
        ResourceBundle resb = TranslationBundle.getBundle();
        String title = resb.getString("mainmenu.googlePlayGame.acceptGame");
        String message = resb.getString("mainmenu.googlePlayGame.invited")
                .replaceAll("\\{0\\}", invitation.getInviter().getDisplayName());
        String accept = resb.getString("mainmenu.googlePlayGame.accept");
        String reject = resb.getString("mainmenu.googlePlayGame.reject");
        final String invitationId = invitation.getInvitationId();
        return new AlertDialog.Builder(GameActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(accept, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        acceptInvitation(invitationId);
                    }
                })
                .setNegativeButton(reject, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mHelper.getGamesClient().declineRoomInvitation(invitationId);
                    }
                })
                .create();
    }

    private void acceptInvitation(String invitationId) {
        openLoadingDialog("mainmenu.googlePlayGame.waitRoom");
        mHelper.getGamesClient().joinRoom(RoomConfig.builder(
                new BaseRoomUpdateListener() {
                    @Override
                    public void onJoinedRoom(int statusCode, Room room) {
                        super.onJoinedRoom(statusCode, room);
                        closeLoadingDialog();
                        if (statusCode != GamesClient.STATUS_OK) {
                            String error = "onJoinedRoom failed. "+statusCode+" "+getErrorString(statusCode);
                            logger.warning(error);
                            toast(error);
                            return;
                        }
                        gameRoom = room;
                        logger.info("Starting waiting room activity as joiner.");
                        startActivityForResult(mHelper.getGamesClient().getRealTimeWaitingRoomIntent(room, 1), RC_JOINER_WAITING_ROOM);
                    }
                })
                .setInvitationIdToAccept(invitationId)
                .setRoomStatusUpdateListener(new BaseRoomStatusUpdateListener())
                .setMessageReceivedListener(new RealTimeMessageReceivedListener() {
                    @Override
                    public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {
                        onMessageReceived(realTimeMessage);
                    }
                })
                .build());
    }

    static String getErrorString(int statusCode) {
        switch(statusCode) {
            case GamesClient.STATUS_OK: return "OK"; // 0
            case GamesClient.STATUS_INTERNAL_ERROR: return "INTERNAL_ERROR"; // 1
            case GamesClient.STATUS_CLIENT_RECONNECT_REQUIRED: return "CLIENT_RECONNECT_REQUIRED"; // 2
            case GamesClient.STATUS_NETWORK_ERROR_STALE_DATA: return "NETWORK_ERROR_STALE_DATA"; // 3
            case GamesClient.STATUS_NETWORK_ERROR_NO_DATA: return "NETWORK_ERROR_NO_DATA"; // 4
            case GamesClient.STATUS_NETWORK_ERROR_OPERATION_DEFERRED: return "NETWORK_ERROR_OPERATION_DEFERRED"; // 5
            case GamesClient.STATUS_NETWORK_ERROR_OPERATION_FAILED: return "NETWORK_ERROR_OPERATION_FAILED"; // 6
            case GamesClient.STATUS_LICENSE_CHECK_FAILED: return "LICENSE_CHECK_FAILED"; // 7
            case 8: return "APP_MISCONFIGURED";

            case GamesClient.STATUS_ACHIEVEMENT_UNLOCK_FAILURE: return "ACHIEVEMENT_UNLOCK_FAILURE"; // 3000
            case GamesClient.STATUS_ACHIEVEMENT_UNKNOWN: return "ACHIEVEMENT_UNKNOWN"; // 3001
            case GamesClient.STATUS_ACHIEVEMENT_NOT_INCREMENTAL: return "ACHIEVEMENT_NOT_INCREMENTAL"; // 3002
            case GamesClient.STATUS_ACHIEVEMENT_UNLOCKED: return "ACHIEVEMENT_UNLOCKED"; // 3003

            case GamesClient.STATUS_MULTIPLAYER_ERROR_CREATION_NOT_ALLOWED: return "MULTIPLAYER_ERROR_CREATION_NOT_ALLOWED"; // 6000
            case GamesClient.STATUS_MULTIPLAYER_ERROR_NOT_TRUSTED_TESTER: return "MULTIPLAYER_ERROR_NOT_TRUSTED_TESTER"; // 6001
            case 6002: return "MULTIPLAYER_ERROR_INVALID_MULTIPLAYER_TYPE";
            case 6003: return "MULTIPLAYER_DISABLED";
            case 6004: return "MULTIPLAYER_ERROR_INVALID_OPERATION";

            case 6500: return "MATCH_ERROR_INVALID_PARTICIPANT_STATE";
            case 6501: return "MATCH_ERROR_INACTIVE_MATCH";
            case 6502: return "MATCH_ERROR_INVALID_MATCH_STATE";
            case 6503: return "MATCH_ERROR_OUT_OF_DATE_VERSION";
            case 6504: return "MATCH_ERROR_INVALID_MATCH_RESULTS";
            case 6505: return "MATCH_ERROR_ALREADY_REMATCHED";
            case 6506: return "MATCH_NOT_FOUND";
            case 6507: return "MATCH_ERROR_LOCALLY_MODIFIED";

            case GamesClient.STATUS_REAL_TIME_CONNECTION_FAILED: return "REAL_TIME_CONNECTION_FAILED"; // 7000
            case GamesClient.STATUS_REAL_TIME_MESSAGE_SEND_FAILED: return "REAL_TIME_MESSAGE_SEND_FAILED"; // 7001
            case GamesClient.STATUS_INVALID_REAL_TIME_ROOM_ID: return "INVALID_REAL_TIME_ROOM_ID"; // 7002
            case GamesClient.STATUS_PARTICIPANT_NOT_CONNECTED: return "PARTICIPANT_NOT_CONNECTED"; // 7003
            case GamesClient.STATUS_REAL_TIME_ROOM_NOT_JOINED: return "REAL_TIME_ROOM_NOT_JOINED"; // 7004
            case GamesClient.STATUS_REAL_TIME_INACTIVE_ROOM: return "REAL_TIME_INACTIVE_ROOM"; // 7005
            case GamesClient.STATUS_REAL_TIME_MESSAGE_FAILED: return "REAL_TIME_MESSAGE_FAILED"; // -1
            case 7007: return "OPERATION_IN_FLIGHT";
            default: return "unknown statusCode "+statusCode;
        }
    }

    void toast(String text) {
    	Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSignInFailed() {
	MiniFlashRiskAdapter ui = getUi();
	if (ui!=null) {
	    ui.playGamesStateChanged();
	}
        // user must have cancelled signing in, so they must not want to see achievements.
	pendingShowAchievements = false;
    }

    @Override
    public boolean isSignedIn() {
        return mHelper.isSignedIn();
    }

    @Override
    public void unlockAchievement(String id) {
        if (isSignedIn()) {
            mHelper.getGamesClient().unlockAchievement(id);
        }
        else {
            pendingAchievement = id;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ResourceBundle resb = TranslationBundle.getBundle();
                    new AlertDialog.Builder(GameActivity.this)
                    .setTitle(resb.getString("achievement.achievementUnlocked"))
                    .setMessage(resb.getString("achievement.signInToSave"))
                    .setPositiveButton(resb.getString("achievement.signInToSave.ok"), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            beginUserInitiatedSignIn();
                        }
                     })
                    .setNegativeButton(resb.getString("achievement.signInToSave.cancel"), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                     })
                     .show();
                }
            });
        }
    }

    @Override
    public void showAchievements() {
        if (isSignedIn()) {
            startActivityForResult(mHelper.getGamesClient().getAchievementsIntent(), RC_REQUEST_ACHIEVEMENTS);
        }
        else {
            pendingShowAchievements = true;
            beginUserInitiatedSignIn();
        }
    }

    @Override
    public void startGameGooglePlay(net.yura.lobby.model.Game game) {
        logger.info("startGameGooglePlay");
        if (isSignedIn()) {
            logger.info("starting player selection");

            lobbyGame = game;
            if (lobbyGame.getNumOfPlayers() != 1) {
                throw new RuntimeException("should only have creator "+game.getPlayers());
            }

            startActivityForResult(mHelper.getGamesClient().getSelectPlayersIntent(
                    GOOGLE_PLAY_GAME_MIN_OTHER_PLAYERS, game.getMaxPlayers() - 1),
                    RC_SELECT_PLAYERS);
        }
        else {
            logger.info("redirecting to sign in");
            pendingStartGameGooglePlay = game;
            beginUserInitiatedSignIn();
        }
    }

  @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        logger.info("[GameActivity] onSaveInstanceState");
        // if the system wants to kill our activity we need to save the game if we have one
        if ( shouldSaveGame() ) {
            logger.info("[GameActivity] SAVING TO AUTOSAVE");
            // in game thread, we do not want to do it there as we will not know when its finished
            //getRisk().parser("savegame "+getAutoSaveFileURL());

            try {
                final Risk risk = getRisk();
                final File autoSaveFile = DominationMain.getAutoSaveFile();
                final File tempSaveFile = new File(autoSaveFile.getParent(),autoSaveFile.getName()+".part");

                risk.parserAndWait("savegame "+DominationMain.getAutoSaveFileURL()+".part");
                RiskUtil.rename(tempSaveFile, autoSaveFile);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        logger.info("[GameActivity] onPause");
        // if everything is shut down and there is no current game
        // make sure we clean up so no game is loaded on next start
        if ( !shouldSaveGame() ) {
            File file = DominationMain.getAutoSaveFile();
            if (file.exists()) {
                logger.info("[GameActivity] DELETING AUTOSAVE");
                file.delete();
            }
        }
    }

    private boolean shouldSaveGame() {
        Risk risk = getRisk();
        return risk!=null && risk.getGame()!=null && risk.getLocalGame();
    }

    private Risk getRisk() {
        DominationMain dmain = (DominationMain)AndroidMeApp.getMIDlet();
        return dmain==null?null:dmain.risk;
    }

    private MiniFlashRiskAdapter getUi() {
        DominationMain dmain = (DominationMain)AndroidMeApp.getMIDlet();
        return dmain==null?null:dmain.adapter;
    }

}
