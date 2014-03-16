package net.yura.domination.android;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.yura.android.LoadingDialog;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.lobby.client.ProtoAccess;
import net.yura.lobby.model.Game;
import net.yura.lobby.model.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
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

public class RealTimeMultiplayer implements GameHelper.GameHelperListener {

    private static final int RC_SELECT_PLAYERS = 2;
    private static final int RC_CREATOR_WAITING_ROOM = 3;
    private static final int RC_JOINER_WAITING_ROOM = 4;

    private static final int GOOGLE_PLAY_GAME_MIN_OTHER_PLAYERS = 1;

    private static final Logger logger = Logger.getLogger(RealTimeMultiplayer.class.getName());

    private ProtoAccess encodeDecoder = new ProtoAccess(null);
    private Activity activity;
    private GameHelper mHelper;
    private Lobby lobby;

    private Room gameRoom;
    private Game lobbyGame;

    interface Lobby {
        void createNewGame(Game game);
        void playGame(int gameId);
        void getUsername();
    }

    public RealTimeMultiplayer(GameHelper helper, Activity activity, Lobby lobby) {
        mHelper = helper;
        this.activity = activity;
        this.lobby = lobby;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_SELECT_PLAYERS:
                handlePlayersSelected(resultCode, data);
                break;
            case RC_CREATOR_WAITING_ROOM:
                handleReturnFromWaitingRoom(resultCode, true /* isCreator */);
                break;
            case RC_JOINER_WAITING_ROOM:
                handleReturnFromWaitingRoom(resultCode, false /* not creator */);
                break;
        }
    }

    @Override
    public void onSignInSucceeded() {
        logger.info("invitationId: " + mHelper.getInvitationId());
        if (mHelper.getInvitationId() != null) {
            acceptInvitation(mHelper.getInvitationId());
        }
        else {
            // there is a bug in GooglePlayGameServicesFroyo that mHelper.getInvitationId() returns null
            // even when there is a invitation, so we use this method instead to get it.
            // OR we may go into the app directly instead of clicking on the notification.
            mHelper.getGamesClient().loadInvitations(new OnInvitationsLoadedListener() {
                @Override
                public void onInvitationsLoaded(int statusCode, InvitationBuffer buffer) {
                    logger.info("onInvitationsLoaded: "+statusCode+" "+buffer.getCount()+" "+buffer);
                    for (Invitation invitation : buffer) {
                        logger.info("onInvitationsLoaded invitation: "+getCurrentPlayerState(invitation)+" "+invitation);
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

    @Override
    public void onSignInFailed() {
        // dont care
    }

    private int getCurrentPlayerState(Invitation invitation) {
        return getMe(invitation.getParticipants()).getStatus();
    }

    private Participant getMe(List<Participant> participants) {
        String myId = mHelper.getGamesClient().getCurrentPlayerId();
        for (Participant participant : participants) {
            Player player = participant.getPlayer();
            if (player != null && myId.equals(player.getPlayerId())) {
                return participant;
            }
        }
        throw new RuntimeException("me not found");
    }

    public void startGameGooglePlay(Game game) {
        logger.info("starting player selection");

        lobbyGame = game;
        if (lobbyGame.getNumOfPlayers() != 1) {
            throw new RuntimeException("should only have creator "+game.getPlayers());
        }

        activity.startActivityForResult(mHelper.getGamesClient().getSelectPlayersIntent(
                GOOGLE_PLAY_GAME_MIN_OTHER_PLAYERS, game.getMaxPlayers() - 1),
                RC_SELECT_PLAYERS);
    }

    private void handlePlayersSelected(int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            logger.info("Player selection failed. "+resultCode);
            return;
        }

        openLoadingDialog("mainmenu.googlePlayGame.waitRoom");

        ArrayList<String> invitees = data.getStringArrayListExtra(GamesClient.EXTRA_PLAYERS);
        // get auto-match criteria
        int minAutoMatchPlayers = data.getIntExtra(GamesClient.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
        int maxAutoMatchPlayers = data.getIntExtra(GamesClient.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);

        logger.info("Players selected. Creating room. "+invitees+" "+minAutoMatchPlayers+" "+maxAutoMatchPlayers);

        RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(
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
                    activity.startActivityForResult(mHelper.getGamesClient().getRealTimeWaitingRoomIntent(room, 1), RC_CREATOR_WAITING_ROOM);
                }
            })
            .setRoomStatusUpdateListener(new BaseRoomStatusUpdateListener(){
                @Override
                public void onRoomUpdated(Room room) {
                    gameRoom = room;
                }
            })
            .setMessageReceivedListener(new RealTimeMessageReceivedListener() {
                @Override
                public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {
                    onMessageReceived(realTimeMessage);
                }
            })
            .addPlayersToInvite(invitees);

        if (minAutoMatchPlayers > 0) {
            Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            roomConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        }
        
        // The variant has to be positive, or else it will throw an Exception.
        roomConfigBuilder.setVariant(lobbyGame.getOptions().hashCode() & 0x7FFFFFFF);

        mHelper.getGamesClient().createRoom(roomConfigBuilder.build());
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
                lobby.createNewGame(lobbyGame);
            }
        }
        else if (ProtoAccess.COMMAND_GAME_STARTED.equals(message.getCommand())) {
            int gameId = (Integer)message.getParam();
            lobby.playGame(gameId);
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
            lobby.getUsername();
        }
    }

    public void sendLobbyUsername(String username) {
        logger.info("Sending ID to creator.");

        Message message = new Message();
        message.setCommand(ProtoAccess.REQUEST_JOIN_GAME);
        message.setParam(username);

        sendMessage(message, gameRoom.getCreatorId());
    }

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

    private AlertDialog createAcceptDialog(Invitation invitation) {
        ResourceBundle resb = TranslationBundle.getBundle();
        String title = resb.getString("mainmenu.googlePlayGame.acceptGame");
        String message = resb.getString("mainmenu.googlePlayGame.invited")
                .replaceAll("\\{0\\}", invitation.getInviter().getDisplayName());
        String accept = resb.getString("mainmenu.googlePlayGame.accept");
        String reject = resb.getString("mainmenu.googlePlayGame.reject");
        final String invitationId = invitation.getInvitationId();
        return new AlertDialog.Builder(activity)
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
                        activity.startActivityForResult(mHelper.getGamesClient().getRealTimeWaitingRoomIntent(room, 1), RC_JOINER_WAITING_ROOM);
                    }
                })
                .setRoomStatusUpdateListener(new BaseRoomStatusUpdateListener() {
                    @Override
                    public void onRoomUpdated(Room room) {
                        gameRoom = room;
                    }
                })
                .setMessageReceivedListener(new RealTimeMessageReceivedListener() {
                    @Override
                    public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {
                        onMessageReceived(realTimeMessage);
                    }
                })
                .setInvitationIdToAccept(invitationId)
                .build());
    }

    private void closeLoadingDialog() {
        Intent intent = new Intent(activity, LoadingDialog.class);
        intent.putExtra(LoadingDialog.PARAM_COMMAND, "hide");
        activity.startActivity(intent);
    }

    private void openLoadingDialog(String messageName) {
        Intent intent = new Intent(activity, LoadingDialog.class);
        intent.putExtra(LoadingDialog.PARAM_MESSAGE, TranslationBundle.getBundle().getString(messageName));
        intent.putExtra(LoadingDialog.PARAM_CANCELLABLE, true);
        activity.startActivity(intent);
    }

    void toast(String text) {
        Toast.makeText(activity, text, Toast.LENGTH_LONG).show();
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
}
