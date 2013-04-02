package net.yura.domination.android;

import java.io.File;

import android.os.Bundle;
import net.yura.android.AndroidMeActivity;
import net.yura.android.AndroidMeApp;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.mobile.flashgui.DominationMain;
import net.yura.mobile.logging.Logger;

public class GameActivity extends AndroidMeActivity {
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Logger.info("[GameActivity] onSaveInstanceState");
        // if the system wants to kill our activity we need to save the game if we have one
        if ( shouldSaveGame() ) {
            // in game thread, we do not want to do it there
            //getRisk().parser("savegame "+getAutoSaveFileURL());

            // in current thread
            try {
                Logger.info("[GameActivity] SAVING TO AUTOSAVE");
                RiskUtil.saveFile(DominationMain.getAutoSaveFileURL() , getRisk().getGame() );
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
        return risk.getGame()!=null && risk.getLocalGame();
    }

    private Risk getRisk() {
        DominationMain dmain = (DominationMain)AndroidMeApp.getMIDlet();
        return dmain.risk;
    }

}
