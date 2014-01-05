package net.yura.domination.android;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
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

import net.yura.android.AndroidMeActivity;
import net.yura.android.AndroidMeApp;
import net.yura.android.AndroidPreferences;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.domination.mobile.flashgui.DominationMain;
import net.yura.domination.mobile.flashgui.MiniFlashRiskAdapter;
import net.yura.lobby.client.ProtoAccess;
import net.yura.lobby.model.Message;
import net.yura.lobby.model.Player;

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

    private void handlePlayersSelected(int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            logger.info("Player selection failed. "+resultCode);
            return;
        }
        ArrayList<String> invitees = data.getStringArrayListExtra(GamesClient.EXTRA_PLAYERS);
        logger.info("Players selected. Creating room.");
        mHelper.getGamesClient().createRoom(RoomConfig.builder(
                new BaseRoomUpdateListener() {
                    @Override
                    public void onRoomCreated(int statusCode, Room room) {
                        super.onRoomCreated(statusCode, room);
                        if (statusCode != GamesClient.STATUS_OK) {
                            logger.warning("onRoomCreated failed. "+statusCode);
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
                        byte[] data = realTimeMessage.getMessageData();
                        try {
                            Message message = (Message)encodeDecoder.load(new ByteArrayInputStream(data), data.length);
                            onMessageReceived(message);
                        }
                        catch (IOException ex) {
                            logger.log(Level.WARNING, "can not decode", ex);
                        }
                    }
                })
                .addPlayersToInvite(invitees).build());
        logger.info("Room created, waiting for it to be ready");
    }

    private void onMessageReceived(Message message) {
        logger.info("Room message received: " + message);
        if (ProtoAccess.REQUEST_JOIN_GAME.equals(message.getCommand())) {
            String name = (String)message.getParam();
            lobbyGame.getPlayers().add(new Player(name, 0));

            int joined = getParticipantStatusCount(Participant.STATUS_JOINED);
            logger.info("new player joined: "+name+" "+lobbyGame.getNumOfPlayers()+"/"+joined+"/"+gameRoom.getParticipantIds().size());
            if (lobbyGame.getNumOfPlayers() == joined) {
                // TODO can the user start with less then the max number?
                // TODO can we be not inside lobby???
                // TODO can we be not logged in?
                // in case we decided to start with less then the max number of human players
                // we need to update the max number to the current number, so no one else can join
                lobbyGame.setMaxPlayers(lobbyGame.getNumOfPlayers());
                getUi().lobby.createNewGame(lobbyGame);
            }
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
        logger.info("Returning from waiting room.");
        if (resultCode != Activity.RESULT_OK) {
            logger.info("Room was cancelled, result code = " + resultCode);
            return;
        }
        logger.info("Room ready.");
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
        }, data, gameRoom.getRoomId(), gameRoom.getCreatorId());
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
        mHelper.getGamesClient().registerInvitationListener(new OnInvitationReceivedListener() {
            @Override
            public void onInvitationReceived(final Invitation invitation) {
                logger.info("Invitation received from: " + invitation.getInviter());
                createAcceptDialog(invitation).show();
            }
        });
    }

    private AlertDialog createAcceptDialog(final Invitation invitation) {
        ResourceBundle resb = TranslationBundle.getBundle();
        String title = resb.getString("mainmenu.googlePlayGame.acceptGame");
        String message = resb.getString("mainmenu.googlePlayGame.invited")
                .replaceAll("\\{0\\}", invitation.getInviter().getDisplayName());
        String accept = resb.getString("mainmenu.googlePlayGame.accept");
        String reject = resb.getString("mainmenu.googlePlayGame.reject");
        return new AlertDialog.Builder(GameActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(accept, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        acceptInvitation(invitation.getInvitationId());
                    }
                })
                .setNegativeButton(reject, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mHelper.getGamesClient().declineRoomInvitation(invitation.getInvitationId());
                    }
                })
                .create();
    }

    private void acceptInvitation(String invitationId) {
        mHelper.getGamesClient().joinRoom(RoomConfig.builder(
                new BaseRoomUpdateListener() {
                    @Override
                    public void onJoinedRoom(int statusCode, Room room) {
                        super.onJoinedRoom(statusCode, room);
                        if (statusCode != GamesClient.STATUS_OK) {
                            logger.warning("onJoinedRoom failed. "+statusCode);
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
                        // this is the message from the owner of the game to the person joining.
                        // it is mandatory to listen for this, but this will never actually happen.
                        logger.info("onRealTimeMessageReceived: " + realTimeMessage);
                    }
                })
                .build());
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
