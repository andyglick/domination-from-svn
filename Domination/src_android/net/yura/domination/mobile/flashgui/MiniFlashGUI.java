package net.yura.domination.mobile.flashgui;

import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.domination.mobile.MiniUtil;
import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.ButtonGroup;
import net.yura.mobile.gui.components.Button;
import net.yura.mobile.gui.components.Frame;
import net.yura.mobile.gui.components.OptionPane;
import net.yura.mobile.gui.components.Panel;
import net.yura.mobile.gui.components.ScrollPane;
import net.yura.mobile.gui.layout.XULLoader;
import net.yura.mobile.util.Properties;

public class MiniFlashGUI extends Frame {

    private Properties resBundle = MiniUtil.wrap(TranslationBundle.getBundle());
    private Risk myrisk;

    ActionListener al = new ActionListener() {

        @Override
        public void actionPerformed(String actionCommand) {
            if ("new game".equals(actionCommand)) {
                myrisk.parser("newgame");
            }
            else if ("load game".equals(actionCommand)) {
                System.out.println("ac "+actionCommand);
            }
            else if ("manual".equals(actionCommand)) {
                try {
                    RiskUtil.openDocs("help/index_commands.htm");
                }
                catch(Exception e) {
                    OptionPane.showMessageDialog(null,"Unable to open manual: "+e.getMessage(),"Error", OptionPane.ERROR_MESSAGE);
                }
            }
            else if ("about".equals(actionCommand)) {
                MiniUtil.showAbout();
            }
            else if ("quit".equals(actionCommand)) {
                System.exit(0);
            }
            else if ("join game".equals(actionCommand)) {
                OptionPane.showMessageDialog(null,"not done yet","Error", OptionPane.ERROR_MESSAGE);
            }
            else if ("start server".equals(actionCommand)) {
                OptionPane.showMessageDialog(null,"not done yet","Error", OptionPane.ERROR_MESSAGE);
            }
            else if ("closegame".equals(actionCommand)) {
                myrisk.parser("closegame");
            }
            else if ("startgame".equals(actionCommand)) {

                ButtonGroup GameType = (ButtonGroup)newgame.getGroups().get("GameType");
                ButtonGroup CardType = (ButtonGroup)newgame.getGroups().get("CardType");

                Button autoplaceall = (Button)newgame.find("autoplaceall");
                Button recycle = (Button)newgame.find("recycle");

                myrisk.parser("startgame "+
                        GameType.getSelection().getActionCommand()+" "+
                        CardType.getSelection().getActionCommand()+
                        ((autoplaceall!=null&&autoplaceall.isSelected()?" "+autoplaceall.getActionCommand():""))+
                        ((recycle!=null&&recycle.isSelected()?" "+recycle.getActionCommand():""))
                        );

            }
            else {
                System.out.println("Unknown command: "+actionCommand);
            }
        }
    };


    public MiniFlashGUI(Risk risk) {
        myrisk = risk;

        MiniFlashRiskAdapter adapter = new MiniFlashRiskAdapter(this);

        risk.addRiskListener( adapter );

        openMainMneu();
    }

    private XULLoader getPanel(String xmlfile) {

        XULLoader loader;
        try {
            loader = XULLoader.load( getClass().getResourceAsStream(xmlfile) , al, resBundle);
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
        return loader;

    }

    public void openMainMneu() {

        setTitle( resBundle.getProperty("mainmenu.title") );

        XULLoader loader = getPanel("/mainmenu.xml");

        setContentPane( new ScrollPane( loader.getRoot() ) );

        revalidate();
        repaint();
    }

    XULLoader newgame;

    public void openNewGame(boolean localgame) {

        if (localgame) {
            setTitle(resBundle.getProperty("newgame.title.local"));
            //resetplayers.setVisible(true);
        }
        else {
            setTitle(resBundle.getProperty("newgame.title.network"));
            //resetplayers.setVisible(false);
        }

        newgame = getPanel("/newgame.xml");

        setContentPane( new ScrollPane( newgame.getRoot() ) );

        revalidate();
        repaint();
    }


}
