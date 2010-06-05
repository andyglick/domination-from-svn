package net.yura.domination.mobile.flashgui;

import java.util.Vector;

import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.core.Player;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.domination.mobile.MiniUtil;
import net.yura.domination.mobile.mapchooser.MapChooser;
import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.ButtonGroup;
import net.yura.mobile.gui.ChangeListener;
import net.yura.mobile.gui.components.Button;
import net.yura.mobile.gui.components.Component;
import net.yura.mobile.gui.components.Frame;
import net.yura.mobile.gui.components.Label;
import net.yura.mobile.gui.components.OptionPane;
import net.yura.mobile.gui.components.ScrollPane;
import net.yura.mobile.gui.components.Spinner;
import net.yura.mobile.gui.layout.XULLoader;
import net.yura.mobile.util.Properties;

public class MiniFlashGUI extends Frame implements ChangeListener {

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

                //myrisk.getGame().getPlayers();

                myrisk.parser("startgame "+
                        GameType.getSelection().getActionCommand()+" "+
                        CardType.getSelection().getActionCommand()+
                        ((autoplaceall!=null&&autoplaceall.isSelected()?" autoplaceall":""))+
                        ((recycle!=null&&recycle.isSelected()?" recycle":""))
                        );

            }
            else if ("choosemap".equals(actionCommand)) {

                new MapChooser();
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

        addChangeListener("easyAI");
        addChangeListener("hardAI");
        addChangeListener("crapAI");
        addChangeListener("human");

        setContentPane( new ScrollPane( newgame.getRoot() ) );

        if (localgame) {
            RiskUtil.loadPlayers( myrisk );
        }

        revalidate();
        repaint();
    }

    private void addChangeListener(String name) {
        Component comp = newgame.find(name);
        if (comp!=null && comp instanceof Spinner) {
            ((Spinner)comp).addChangeListener(this);
        }
    }

    public void updatePlayers() {

        Vector players = myrisk.getGame().getPlayers();

        int easyAIcount=0;
        int hardAIcount=0;
        int crapAIcount=0;
        int humancount=0;

        for (int c=0;c<players.size();c++) {
            int type = ((Player)players.elementAt(c)).getType();
            switch(type) {
                case Player.PLAYER_AI_CRAP: crapAIcount++; break;
                case Player.PLAYER_AI_HARD: hardAIcount++; break;
                case Player.PLAYER_AI_EASY: easyAIcount++; break;
                case Player.PLAYER_HUMAN: humancount++; break;
            }
        }

        Component easyAI = newgame.find("easyAI");
        Component hardAI = newgame.find("hardAI");
        Component crapAI = newgame.find("crapAI");
        Component human = newgame.find("human");

        easyAI.setValue( new Integer(easyAIcount) );
        hardAI.setValue( new Integer(hardAIcount) );
        crapAI.setValue( new Integer(crapAIcount) );
        human.setValue( new Integer(humancount) );
    }

    public void changeEvent(Component source, int num) {

        Component easyAI = newgame.find("easyAI");
        Component hardAI = newgame.find("hardAI");
        Component crapAI = newgame.find("crapAI");
        Component human = newgame.find("human");

        int type = -1;
        if (source == easyAI) {
            type = Player.PLAYER_AI_EASY;
        }
        else if (source == hardAI) {
            type = Player.PLAYER_AI_HARD;
        }
        else if (source == crapAI) {
            type = Player.PLAYER_AI_CRAP;
        }
        else if (source == human) {
            type = Player.PLAYER_HUMAN;
        }

        if (type!=-1) {
            Vector players = myrisk.getGame().getPlayers();
            int count=0;
            for (int c=0;c<players.size();c++) {
                int ptype = ((Player)players.elementAt(c)).getType();
                if (ptype == type) {
                    count++;
                }
            }
            int newval = ((Integer)((Spinner)source).getValue()).intValue();

            if (newval<count) {
                for (int c=players.size()-1;c>=0;c--) {
                    Player p = (Player)players.elementAt(c);
                    if (p.getType() == type) {
                        myrisk.parser("delplayer " + p.getName());
                        break;
                    }
                }
            }
            else if (newval>count) {




            }

        }

    }

}
