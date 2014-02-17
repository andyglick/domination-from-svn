package net.yura.domination.android;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import com.google.example.games.basegameutils.GameHelper;
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
import net.yura.lobby.model.Game;

public class GameActivity extends AndroidMeActivity implements GameHelper.GameHelperListener,DominationMain.GooglePlayGameServices {

    private static final Logger logger = Logger.getLogger(GameActivity.class.getName());

    /**
     * this code needs to not clash with other codes such as the ones in
     * {@link GameHelper} 9001,9002
     * {@link RealTimeMultiplayer} 2,3,4
     */
    private static final int RC_REQUEST_ACHIEVEMENTS = 1;

    private GameHelper mHelper;
    private RealTimeMultiplayer realTimeMultiplayer;

    private String pendingAchievement;
    private boolean pendingShowAchievements;
    private net.yura.lobby.model.Game pendingStartGameGooglePlay;
    private boolean pendingSendLobbyUsername;

    @Override
    protected void onSingleCreate() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        DominationMain.appPreferences = new AndroidPreferences(preferences);
        super.onSingleCreate();
    }

    @Override
    public void onMidletStarted() {
        DominationMain dmain = (DominationMain)AndroidMeApp.getMIDlet();
        dmain.setGooglePlayGameServices(GameActivity.this);

        mHelper = new GameHelper(this);
        mHelper.enableDebugLog(true, "DominationPlay");

        realTimeMultiplayer = new RealTimeMultiplayer(mHelper, this, new RealTimeMultiplayer.Lobby() {
            @Override
            public void createNewGame(Game game) {
                getUi().lobby.createNewGame(game);
            }
            @Override
            public void playGame(int gameId) {
                getUi().lobby.playGame(gameId);
            }
            @Override
            public void getUsername() {
                MiniFlashRiskAdapter ui = getUi();
                if (ui.lobby != null) {
                    if (ui.lobby.whoAmI() != null) {
                        realTimeMultiplayer.sendLobbyUsername(ui.lobby.whoAmI());
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
        });

        GameHelperListener gameHelperListener = new GameHelperListener();
        gameHelperListener.addListener(this);
        gameHelperListener.addListener(realTimeMultiplayer);

        mHelper.setup(gameHelperListener, GameHelper.CLIENT_GAMES);
    }

    class GameHelperListener implements GameHelper.GameHelperListener {
        List<GameHelper.GameHelperListener> listeners = new ArrayList();
        public void addListener(GameHelper.GameHelperListener listener) {
            listeners.add(listener);
        }
        @Override
        public void onSignInFailed() {
            for (GameHelper.GameHelperListener listener : listeners) {
                listener.onSignInFailed();
            }
        }
        @Override
        public void onSignInSucceeded() {
            for (GameHelper.GameHelperListener listener : listeners) {
                listener.onSignInSucceeded();
            }
        }
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
        realTimeMultiplayer.onActivityResult(requestCode, resultCode, data);
        mHelper.onActivityResult(requestCode, resultCode, data);
    }

    // ----------------------------- GameHelper.GameHelperListener -----------------------------

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

    // ----------------------------- GooglePlayGameServices -----------------------------

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
    public boolean isSignedIn() {
        return mHelper.isSignedIn();
    }

    @Override
    public void startGameGooglePlay(net.yura.lobby.model.Game game) {
        logger.info("startGameGooglePlay");
        if (isSignedIn()) {
            realTimeMultiplayer.startGameGooglePlay(game);
        }
        else {
            logger.info("redirecting to sign in");
            pendingStartGameGooglePlay = game;
            beginUserInitiatedSignIn();
        }
    }

    @Override
    public void setLobbyUsername(String username) {
        if (pendingSendLobbyUsername) {
            pendingSendLobbyUsername = false;
            realTimeMultiplayer.sendLobbyUsername(username);
        }
    }

    @Override
    public void gameStarted(int id) {
        realTimeMultiplayer.gameStarted(id);
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

    // ----------------------------- GAME SAVE -----------------------------

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
