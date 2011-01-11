package net.yura.domination.mobile.flashgui;

import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.mobile.RiskMiniIO;
import net.yura.mobile.gui.DesktopPane;
import net.yura.mobile.gui.Midlet;
import net.yura.mobile.gui.plaf.LookAndFeel;
import net.yura.mobile.gui.plaf.SynthLookAndFeel;

public class DominationMidlet extends Midlet {

    @Override
    public void initialize(DesktopPane rootpane) {



        LookAndFeel lookandfeel=null;
        try {
                if (Midlet.getPlatform() == Midlet.PLATFORM_ANDROID) {
                        lookandfeel = (SynthLookAndFeel)Class.forName("net.yura.android.plaf.AndroidLookAndFeel").newInstance();
                }
                //else if (Midlet.getPlatform() == Midlet.PLATFORM_BLACKBERRY) {
                //      lookandfeel = (LookAndFeel)Class.forName("net.yura.blackberry.plaf.BlackBerryLookAndFeel").newInstance();
                //}
        }
        catch (Throwable ex) {
                ex.printStackTrace();
        }
        if (lookandfeel==null) {
                lookandfeel = new net.yura.mobile.gui.plaf.nimbus.NimbusLookAndFeel(16);
        }
        rootpane.setLookAndFeel( lookandfeel );




        //rootpane.setLookAndFeel( new net.yura.mobile.gui.plaf.MetalLookAndFeel() );
/*
        SynthLookAndFeel synth = new SynthLookAndFeel();
        try {
            synth.load( getClass().getResourceAsStream("/domFlash.xml") );
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        rootpane.setLookAndFeel( synth );
*/

        RiskUtil.streamOpener = new RiskMiniIO();

        Risk risk = new Risk("luca.map","risk.cards");

        MiniFlashGUI gui = new MiniFlashGUI(risk);

        gui.setMaximum(true);
        gui.setVisible(true);

        //risk.parser("newgame");
        //risk.parser("newplayer ai hard blue bob");
        //risk.parser("newplayer ai hard red fred");
        //risk.parser("newplayer ai hard green greg");
        //risk.parser("startgame domination increasing");

    }

}