package net.yura.domination.mobile.flashgui;

import java.io.InputStream;
import java.util.Vector;
import javax.microedition.lcdui.Image;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.core.Player;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.mobile.MiniUtil;
import net.yura.domination.mapstore.MapChooser;
import net.yura.domination.mobile.RiskMiniIO;
import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.ButtonGroup;
import net.yura.mobile.gui.ChangeListener;
import net.yura.mobile.gui.Icon;
import net.yura.mobile.gui.components.Button;
import net.yura.mobile.gui.components.Component;
import net.yura.mobile.gui.components.FileChooser;
import net.yura.mobile.gui.components.Frame;
import net.yura.mobile.gui.components.Label;
import net.yura.mobile.gui.components.OptionPane;
import net.yura.mobile.gui.components.Panel;
import net.yura.mobile.gui.components.ScrollPane;
import net.yura.mobile.gui.components.Spinner;
import net.yura.mobile.gui.layout.GridBagConstraints;
import net.yura.mobile.gui.layout.XULLoader;
import net.yura.mobile.logging.Logger;
import net.yura.mobile.util.Properties;

public class MiniFlashGUI extends Frame implements ChangeListener,ActionListener {

    Properties resb = GameActivity.resb;
    public Risk myrisk;


        FileChooser chooser;

        @Override
        public void actionPerformed(String actionCommand) {
            if ("new game".equals(actionCommand)) {
                myrisk.parser("newgame");
            }
            else if ("load game".equals(actionCommand)) {

                chooser = new FileChooser();
                chooser.setCurrentDirectory( MiniUtil.getSaveGameDirURL() );
                chooser.showDialog(this, "doLoad", resb.getProperty("mainmenu.loadgame.loadbutton") , resb.getProperty("mainmenu.loadgame.loadbutton") );

            }
            else if ("doLoad".equals(actionCommand)) {

                String file = chooser.getSelectedFile();
                chooser = null;
                myrisk.parser("loadgame " + file );

            }
            else if ("manual".equals(actionCommand)) {
                MiniUtil.openHelp();
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

                Vector players = myrisk.getGame().getPlayers();

                if (players.size() >= 2 && players.size() <= RiskGame.MAX_PLAYERS ) {

                    if (localgame) {
                        RiskUtil.savePlayers(myrisk, getClass());
                    }

                    myrisk.parser("startgame "+
                            GameType.getSelection().getActionCommand()+" "+
                            CardType.getSelection().getActionCommand()+
                            ((autoplaceall!=null&&autoplaceall.isSelected()?" autoplaceall":""))+
                            ((recycle!=null&&recycle.isSelected()?" recycle":""))
                            );
                }
                else {

                        OptionPane.showMessageDialog(null, resb.getProperty("newgame.error.numberofplayers") , resb.getProperty("newgame.error.title"), OptionPane.ERROR_MESSAGE );

                }

            }
            else if ("choosemap".equals(actionCommand)) {


                //WebView webView = new WebView( AndroidMeActivity.DEFAULT_ACTIVITY );
                //webView.loadUrl("file:///android_asset/help/index.htm");
                //AndroidMeActivity.DEFAULT_ACTIVITY.setContentView(webView);



                MapListener al = new MapListener();

                MapChooser mapc = new MapChooser(al, MiniUtil.getFileList("map") );
                al.mapc = mapc;

                Frame mapFrame = new Frame( resb.getProperty("newgame.choosemap") );
                mapFrame.setContentPane( mapc.getRoot() );
                mapFrame.setMaximum(true);
                mapFrame.setVisible(true);

            }
            else {
                System.out.println("Unknown command: "+actionCommand);
            }
        }

    class MapListener implements ActionListener {
        MapChooser mapc;
        public void actionPerformed(String arg0) {
            
            String name = mapc.getSelectedMap();
            if (name != null) {
                    myrisk.parser("choosemap " + name );
            }
            
            mapc.getRoot().getWindow().setVisible(false);
            
            mapc.destroy();
        }
    };

    public MiniFlashGUI(Risk risk) {
        myrisk = risk;
        setMaximum(true);
        
        try {
            setBorder( new BackgroundBorder( Image.createImage("/marble.jpg") ) );
            setBackground( 0x00FFFFFF );
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        
    }

    private XULLoader getPanel(String xmlfile) {

        XULLoader loader;
        try {
            loader = XULLoader.load( getClass().getResourceAsStream(xmlfile) , this, resb);
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
        return loader;

    }

    public void openMainMenu() {

        setTitle( resb.getProperty("mainmenu.title") );

        XULLoader loader = getPanel("/mainmenu.xml");

        setContentPane( new ScrollPane( loader.getRoot() ) );

        revalidate();
        repaint();
    }

    // ================================================ GAME SETUP

    XULLoader newgame;
    private boolean localgame;
    private static final String[] compsNames = new String[]{"crapAI","easyAI","hardAI","human"};
    private static final int[] compTypes = new int[] {Player.PLAYER_AI_CRAP,Player.PLAYER_AI_EASY,Player.PLAYER_AI_HARD,Player.PLAYER_HUMAN};

    public void openNewGame(boolean localgame) {

        this.localgame = localgame;

        if (localgame) {
            setTitle(resb.getProperty("newgame.title.local"));
            //resetplayers.setVisible(true);
        }
        else {
            setTitle(resb.getProperty("newgame.title.network"));
            //resetplayers.setVisible(false);
        }

        newgame = getPanel("/newgame.xml");

        // kind of a hack
        Component startButton = newgame.find("startButton");
        Component cancelButton = newgame.find("cancelButton");
        if (startButton!=null && cancelButton!=null && !cancelButton.isVisible()) {
            ((GridBagConstraints)((Panel)startButton.getParent()).getConstraints().get(startButton)).colSpan = 2;
        }

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

    public void showMapPic(RiskGame p) {

        InputStream in=null;
        
        String prv = p.getPreviewPic();
        if (prv!=null) {
            in = MapChooser.getLocalePreviewImg("preview/"+prv);
        }
        if (in==null) {
            in = MapChooser.getLocalePreviewImg( p.getImagePic() );
        }

        Image img=null;
        if (in!=null) {
            try {
                img = Image.createImage(in);
            }
            catch (Exception ex) {
                Logger.warn(ex);
            }
        }
        
        Label label = (Label)newgame.find("MapImg");
        label.setIcon( img!=null ? new Icon(img) : null );
    }

    public void showCardsFile(String c, boolean hasMission) {
        //cardsFile.setText(c);
        //if ( !hasMission && mission.isSelected() ) { domination.setSelected(true); AutoPlaceAll.setEnabled(true); }
        //mission.setEnabled(m);
    }
}
