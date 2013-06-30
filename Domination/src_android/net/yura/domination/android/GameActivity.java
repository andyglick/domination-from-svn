package net.yura.domination.android;

import java.io.File;
import java.io.FileOutputStream;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import net.yura.android.AndroidMeActivity;
import net.yura.android.AndroidMeApp;
import net.yura.android.AndroidPreferences;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.core.Player;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.mobile.flashgui.DominationMain;
import net.yura.mobile.logging.Logger;

public class GameActivity extends AndroidMeActivity {

    @Override
    protected void onSingleCreate() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        DominationMain.appPreferences = new AndroidPreferences(preferences);

        super.onSingleCreate();
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

            final File autoSaveFile = DominationMain.getAutoSaveFile();
            final File tempSaveFile = new File(autoSaveFile.getParent(),autoSaveFile.getName()+".part");
            final Risk risk = getRisk();
            final RiskGame game = risk.getGame();

            // we can not save the current state if the AI is playing as we will get a inconsistent state inside the game
            if (game.getCurrentPlayer()==null || game.getCurrentPlayer().getType()==Player.PLAYER_HUMAN) {
                // we need to make a new as the main android thread does not have a big enough stack
                Thread thread = new Thread(null,null,"Domination-onSaveInstanceState", 100000000) {
                    @Override
                    public void run() {
                        // in current thread
                        try {
                            // we want to save to auto.save.part and then rename it to auto.save
                            // in case the save fails for some reason so we dont end up with half a game in the file
                            RiskUtil.saveFile(DominationMain.getAutoSaveFileURL()+".part" , game );
                            RiskUtil.rename(tempSaveFile, autoSaveFile);
                        }
                        catch (Throwable ex) {
                            ex.printStackTrace();
                        }
                    };
                };
                thread.start();
                tryAndWaitForThreadToFinish(thread);
            }
            else {
                try {
                    byte[] data = risk.getLastSavedState();
                    if (data.length==0) {
                        throw new IllegalStateException("data can not have zero length");
                    }
                    FileOutputStream out = new FileOutputStream(tempSaveFile);
                    out.write(data);
                    out.close();
                    RiskUtil.rename(tempSaveFile, autoSaveFile);
                }
                catch (Throwable ex) {
                    ex.printStackTrace();
                }
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

    private static void tryAndWaitForThreadToFinish(Thread thread) {
        try {
            thread.join();
        }
        catch(InterruptedException in) {
            Thread.currentThread().interrupt();
        }
    }
}
