package net.yura.domination.mobile.flashgui;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.domination.mapstore.MapChooser;
import net.yura.domination.mobile.RiskMiniIO;
import net.yura.grasshopper.SimpleBug;
import net.yura.mobile.gui.DesktopPane;
import net.yura.mobile.gui.Midlet;
import net.yura.mobile.gui.components.OptionPane;
import net.yura.mobile.gui.plaf.SynthLookAndFeel;
import net.yura.swingme.core.CoreUtil;

public class DominationMidlet extends Midlet {

    @Override
    public void initialize(DesktopPane rootpane) {

        // IO depends on this, so we need to do this first
        RiskUtil.streamOpener = new RiskMiniIO();

        try {

            SimpleBug.initLogFile( RiskUtil.GAME_NAME , Risk.RISK_VERSION+" MiniFlashGUI" , TranslationBundle.getBundle().getLocale().toString() );

            CoreUtil.setupLogging();

            Logger.getLogger("").addHandler( new Handler() {

                @Override
                public void publish(LogRecord record) {
                    if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
                        OptionPane.showMessageDialog(null, record.getMessage()+" "+record.getThrown(), "WARN", OptionPane.WARNING_MESSAGE);
                    }
                }

                @Override public void flush() { }
                @Override public void close() { }
            } );
            
            // if we want to see DEBUG, default is INFO
            //java.util.logging.Logger.getLogger("").setLevel(java.util.logging.Level.ALL);

        }
        catch (Throwable th) {
            System.out.println("Grasshopper not loaded");
            th.printStackTrace();
        }

        SynthLookAndFeel synth = new SynthLookAndFeel();
        try {
            synth.load( getClass().getResourceAsStream("/domFlash.xml") );
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        rootpane.setLookAndFeel( synth );

        MapChooser.loadThemeExtension(); // this has theme elements used inside AND outside of the MapChooser



// temp hack untill i find a way for undo to work
Risk.skipUndo = true;


        Risk risk = new Risk("luca.map","risk.cards");
        MiniFlashRiskAdapter adapter = new MiniFlashRiskAdapter(risk);

        adapter.openMainMenu();


        //risk.parser("newgame");
        //risk.parser("newplayer ai hard blue bob");
        //risk.parser("newplayer ai hard red fred");
        //risk.parser("newplayer ai hard green greg");
        //risk.parser("startgame domination increasing");

    }

}