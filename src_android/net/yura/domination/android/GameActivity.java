package net.yura.domination.android;

import java.io.File;

import com.google.example.games.basegameutils.GameHelper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import net.yura.android.AndroidMeActivity;
import net.yura.android.AndroidMeApp;
import net.yura.android.AndroidPreferences;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.mobile.flashgui.DominationMain;
import net.yura.domination.mobile.flashgui.MiniFlashRiskAdapter;
import net.yura.mobile.logging.Logger;

public class GameActivity extends AndroidMeActivity implements GameHelper.GameHelperListener,DominationMain.GooglePlayGameServices {

    protected GameHelper mHelper;

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
    public void onActivityResult(int request, int response, Intent data) {
        super.onActivityResult(request, response, data);
        mHelper.onActivityResult(request, response, data);
    }

    @Override
    public void beginUserInitiatedSignIn() {
        mHelper.beginUserInitiatedSignIn();
    }

    @Override
    public void signOut() {
        mHelper.signOut();
    }

    @Override
    public void onSignInSucceeded() {
	MiniFlashRiskAdapter ui = getUi();
	if (ui!=null) {
	    ui.playGamesStateChanged();
	}
    }

    @Override
    public void onSignInFailed() {
	MiniFlashRiskAdapter ui = getUi();
	if (ui!=null) {
	    ui.playGamesStateChanged();
	}
    }

    @Override
    public boolean isSignedIn() {
        return mHelper.isSignedIn();
    }

    @Override
    public void unlockAchievement(String id) {
        if (mHelper.getGamesClient().isConnected()) {
            mHelper.getGamesClient().unlockAchievement(id);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Logger.info("[GameActivity] onSaveInstanceState");
        // if the system wants to kill our activity we need to save the game if we have one
        if ( shouldSaveGame() ) {
            Logger.info("[GameActivity] SAVING TO AUTOSAVE");
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
        Logger.info("[GameActivity] onPause");
        // if everything is shut down and there is no current game
        // make sure we clean up so no game is loaded on next start
        if ( !shouldSaveGame() ) {
            File file = DominationMain.getAutoSaveFile();
            if (file.exists()) {
                Logger.info("[GameActivity] DELETING AUTOSAVE");
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
