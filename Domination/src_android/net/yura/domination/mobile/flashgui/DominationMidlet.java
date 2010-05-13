package net.yura.domination.mobile.flashgui;

import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.mobile.RiskMiniIO;
import net.yura.mobile.gui.DesktopPane;
import net.yura.mobile.gui.Midlet;
import net.yura.mobile.gui.components.Button;
import net.yura.mobile.gui.components.Frame;
import net.yura.mobile.gui.plaf.MetalLookAndFeel;

public class DominationMidlet extends Midlet {

    @Override
    public void initialize(DesktopPane rootpane) {
        rootpane.setLookAndFeel( new MetalLookAndFeel() );

        RiskUtil.streamOpener = new RiskMiniIO();

        Risk risk = new Risk("luca.map","risk.cards");

        MiniFlashRiskAdapter adapter = new MiniFlashRiskAdapter();

        risk.addRiskListener( adapter );

        MiniFlashGUI gui = new MiniFlashGUI();

        gui.setMaximum(true);
        gui.setVisible(true);

        //risk.parser("newgame");
        //risk.parser("newplayer ai hard blue bob");
        //risk.parser("newplayer ai hard red fred");
        //risk.parser("newplayer ai hard green greg");
        //risk.parser("startgame domination increasing");

    }

    @Override
    public DesktopPane makeNewRootPane() {
        return new DesktopPane(this, -1, null);
    }

}