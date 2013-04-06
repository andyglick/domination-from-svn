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

            Thread thread = new Thread(null,null,"Domination-onSaveInstanceState", 100000000) {
                public void run() {
                    // in current thread
                    try {
                        Logger.info("[GameActivity] SAVING TO AUTOSAVE");
                        // we want to save to auto.save.part and then rename it to auto.save
                        // in case the save fails for some reason so we dont end up with half a game in the file
                        RiskUtil.saveFile(DominationMain.getAutoSaveFileURL()+".part" , getRisk().getGame() );
                        File autoSaveFile = DominationMain.getAutoSaveFile();
                        RiskUtil.rename(new File(autoSaveFile.getParent(),autoSaveFile.getName()+".part"), autoSaveFile);
                    }
                    catch (Throwable ex) {
                        ex.printStackTrace();
                    }
                };
            };
            thread.start();
            try {
                thread.join();
            }
            catch(InterruptedException in) {
                Thread.currentThread().interrupt();
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
        return dmain.risk;
    }

}
