package net.yura.domination.mobile.flashgui;

import java.util.Vector;

import javax.microedition.lcdui.Graphics;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.core.Player;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.domination.mobile.MiniUtil;
import net.yura.domination.mobile.PicturePanel;
import net.yura.domination.mobile.mapchooser.MapChooser;
import net.yura.domination.mobile.simplegui.GamePanel;
import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.ButtonGroup;
import net.yura.mobile.gui.ChangeListener;
import net.yura.mobile.gui.components.Button;
import net.yura.mobile.gui.components.Component;
import net.yura.mobile.gui.components.Frame;
import net.yura.mobile.gui.components.OptionPane;
import net.yura.mobile.gui.components.Panel;
import net.yura.mobile.gui.components.ScrollPane;
import net.yura.mobile.gui.components.Spinner;
import net.yura.mobile.gui.layout.BorderLayout;
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

    // ================================================ GAME SETUP

    XULLoader newgame;
    private static final String[] compsNames = new String[]{"crapAI","easyAI","hardAI","human"};
    private static final int[] compTypes = new int[] {Player.PLAYER_AI_CRAP,Player.PLAYER_AI_EASY,Player.PLAYER_AI_HARD,Player.PLAYER_HUMAN};

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

        for (int c=0;c<compsNames.length;c++) {
            addChangeListener(compsNames[c]);
        }

        setContentPane( new ScrollPane( newgame.getRoot() ) );

        if (localgame) {
            RiskUtil.loadPlayers( myrisk ,getClass());
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
    private void removeChangeListener(String name) {
        Component comp = newgame.find(name);
        if (comp!=null && comp instanceof Spinner) {
            ((Spinner)comp).removeChangeListener(this);
        }
    }


    public void updatePlayers() {

        Vector players = myrisk.getGame().getPlayers();

        int[] count = new int[compTypes.length];

        for (int c=0;c<players.size();c++) {
            int type = ((Player)players.elementAt(c)).getType();
            for (int a=0;a<compTypes.length;a++) {
                if (type == compTypes[a]) {
                    count[a]++;
                    break;
                }
            }
        }

        for (int c=0;c<compsNames.length;c++) {
            Component comp = newgame.find(compsNames[c]);
            if (comp!=null) {
                // we want to remove the listener first as this update is not user generated
                removeChangeListener(compsNames[c]);
                comp.setValue( new Integer(count[c]) );
                addChangeListener(compsNames[c]);
            }
        }

    }

    public void changeEvent(Component source, int num) {
        System.out.println("changeEvent changeEvent changeEvent changeEvent changeEvent changeEvent changeEvent changeEvent");

        int type = -1;
        for (int c=0;c<compsNames.length;c++) {
            Component comp = newgame.find(compsNames[c]);
            if (source == comp) {
                type = compTypes[c];
                break;
            }
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
                if (players.size() == RiskGame.MAX_PLAYERS) {
                    for (int c=players.size()-1;c>=0;c--) {
                        Player p = (Player)players.elementAt(c);
                        if (p.getType()!=type) {
                            p.setType( type );
                            updatePlayers();
                            return;
                        }
                    }
                }
                else {
                    String newname=null;
                    String newcolor=null;
                    for (int c=0;c<Risk.names.length;c++) {
                        boolean badname=false;
                        boolean badcolor=false;
                        for (int a=0;a<players.size();a++) {
                            if (Risk.names[c].equals(((Player)players.elementAt(a)).getName())) {
                                badname = true;
                            }
                            if (RiskUtil.getColor(Risk.colors[c])==((Player)players.elementAt(a)).getColor()) {
                                badcolor = true;
                            }
                            if (badname&&badcolor) {
                                break;
                            }
                        }
                        if (newname==null && !badname) {
                            newname = Risk.names[c];
                        }
                        if (newcolor==null && !badcolor) {
                            newcolor = Risk.colors[c];
                        }
                        if (newname!=null && newcolor!=null) {
                            break;
                        }
                    }

                    if (newname!=null&&newcolor!=null) {
                        myrisk.parser("newplayer " + Risk.getType(type)+" "+ newcolor+" "+ newname );
                    }
                    else {
                        throw new RuntimeException("new name and color can not be found"); // this should never happen
                    }
                }
            }
        }
        else {
            throw new RuntimeException("type for this Component can not be found "+source); // should also never happen
        }

    }




    // ================================================ IN GAME


    public void startGame(boolean s) {

        if (s) {
            RiskUtil.savePlayers(myrisk, getClass());
        }

        // ============================================ create UI

        final PicturePanel pp = new PicturePanel(myrisk);
        GamePanel gamecontrol = new GamePanel(myrisk,pp);

        // ============================================ setup UI

        try {
            pp.load();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        gamecontrol.resetMapView();

        // ============================================ add to window

        Panel mainWindow = new Panel( new BorderLayout() );
        mainWindow.add(pp);
        mainWindow.add(gamecontrol,Graphics.TOP);

        setContentPane( mainWindow );

        revalidate();
        repaint();
    }


}
