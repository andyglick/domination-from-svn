package net.yura.domination.mobile.flashgui;

import java.util.Vector;

import javax.microedition.lcdui.Graphics;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.core.Player;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.engine.guishared.MapMouseListener;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.domination.mobile.MiniUtil;
import net.yura.domination.mobile.MouseListener;
import net.yura.domination.mobile.PicturePanel;
import net.yura.domination.mobile.mapchooser.MapChooser;
import net.yura.domination.mobile.simplegui.GamePanel;
import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.ButtonGroup;
import net.yura.mobile.gui.ChangeListener;
import net.yura.mobile.gui.components.Button;
import net.yura.mobile.gui.components.Component;
import net.yura.mobile.gui.components.FileChooser;
import net.yura.mobile.gui.components.Frame;
import net.yura.mobile.gui.components.Label;
import net.yura.mobile.gui.components.OptionPane;
import net.yura.mobile.gui.components.Panel;
import net.yura.mobile.gui.components.ScrollPane;
import net.yura.mobile.gui.components.Slider;
import net.yura.mobile.gui.components.Spinner;
import net.yura.mobile.gui.layout.BorderLayout;
import net.yura.mobile.gui.layout.GridBagConstraints;
import net.yura.mobile.gui.layout.XULLoader;
import net.yura.mobile.util.Properties;

public class MiniFlashGUI extends Frame implements ChangeListener {

    public Properties resBundle = MiniUtil.wrap(TranslationBundle.getBundle());
    public Risk myrisk;

    ActionListener al = new ActionListener() {

        FileChooser chooser;

        @Override
        public void actionPerformed(String actionCommand) {
            if ("new game".equals(actionCommand)) {
                myrisk.parser("newgame");
            }
            else if ("load game".equals(actionCommand)) {

                chooser = new FileChooser();
                chooser.showDialog(this, "doLoad", resBundle.getProperty("mainmenu.loadgame.loadbutton") , resBundle.getProperty("mainmenu.loadgame.loadbutton") );

            }
            else if ("doLoad".equals(actionCommand)) {

                String file = chooser.getSelectedFile();
                chooser = null;
                myrisk.parser("loadgame " + file );

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

                        OptionPane.showMessageDialog(null, resBundle.getProperty("newgame.error.numberofplayers") , resBundle.getProperty("newgame.error.title"), OptionPane.ERROR_MESSAGE );

                }

            }
            else if ("choosemap".equals(actionCommand)) {

                new MapChooser();
            }
            else if ("go".equals(actionCommand)) {
                goOn();
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

    void setGameStatus(String state) {
        if (status!=null) {
            status.setText(state);
        }
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
    private boolean localgame;
    private static final String[] compsNames = new String[]{"crapAI","easyAI","hardAI","human"};
    private static final int[] compTypes = new int[] {Player.PLAYER_AI_CRAP,Player.PLAYER_AI_EASY,Player.PLAYER_AI_HARD,Player.PLAYER_HUMAN};

    public void openNewGame(boolean localgame) {

        this.localgame = localgame;

        if (localgame) {
            setTitle(resBundle.getProperty("newgame.title.local"));
            //resetplayers.setVisible(true);
        }
        else {
            setTitle(resBundle.getProperty("newgame.title.network"));
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

    public void openDialog() {

        // going to create transparent dialog for battles and moving armies

        // we have to open on top as there is not enough space under the map


    }


    // ================================================ IN GAME

    PicturePanel pp;
    GamePanel gamecontrol;
    Button gobutton;
    Label status;

    public void startGame(boolean s) {

        // ============================================ create UI

        pp = new PicturePanel(myrisk);
        gamecontrol = new GamePanel(myrisk,pp);

        final MapMouseListener mml = new MapMouseListener(myrisk, pp);
        pp.addMouseListener(
            new MouseListener() {
                public void click(int x,int y) {
                    int[] countries = mml.mouseReleased(x, y, gameState);
                    if (countries!=null) {
                        mapClick(countries);
                    }
                }
            }
        );

        gobutton = new Button();
        gobutton.addActionListener(al);
        gobutton.setActionCommand("go");

        Panel gamepanel2 = new Panel();
        gamepanel2.add( new Button("Stats") );
        gamepanel2.add( new Button("Cards") );
        gamepanel2.add( gobutton );

        status = new Label();

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
        ScrollPane sp = new ScrollPane(pp);


        sp.setMode( ScrollPane.MODE_FLOATING_SCROLLBARS );
        sp.setClip(false);
        mainWindow.add( sp );
        mainWindow.add(gamecontrol,Graphics.TOP);
        mainWindow.add(gamepanel2,Graphics.BOTTOM);

        Panel contentPane = new Panel( new BorderLayout() );
        contentPane.add( mainWindow );
        contentPane.add(status,Graphics.BOTTOM);
        setContentPane(contentPane);

        setName("GameFrame");
        setBackground(0xFF666666);

        revalidate();
        repaint();
    }

    int gameState;

    public void needInput(int s) {
            gameState=s;
            String goButtonText=null;
            switch (gameState) {
                    case RiskGame.STATE_TRADE_CARDS: {
                            // after wiping out someone if you go into trade mode
                            pp.setC1(255);
                            pp.setC2(255);
                            goButtonText = resBundle.getProperty("game.button.go.endtrade");
                            break;
                    }
                    case RiskGame.STATE_PLACE_ARMIES: {
//                            if (setupDone==false) {
                                    goButtonText = resBundle.getProperty("game.button.go.autoplace");
//                            }
                            break;
                    }
                    case RiskGame.STATE_ATTACKING: {
                            pp.setC1(255);
                            pp.setC2(255);
                            note = resBundle.getProperty("game.note.selectattacker");
                            goButtonText = resBundle.getProperty("game.button.go.endattack");
                            break;
                    }
                    case RiskGame.STATE_FORTIFYING: {
                            note = resBundle.getProperty("game.note.selectsource");
                            goButtonText = resBundle.getProperty("game.button.go.nomove");
                            break;
                    }
                    case RiskGame.STATE_END_TURN: {
                            goButtonText = resBundle.getProperty("game.button.go.endgo");
                            break;
                    }
                    case RiskGame.STATE_GAME_OVER: {
//                            if (localGame) {
                                    goButtonText = resBundle.getProperty("game.button.go.closegame");
//                            }
//                            else {
//                                    goButtonText = resBundle.getProperty("game.button.go.leavegame");
//                            }
                            break;

                    }
                    case RiskGame.STATE_SELECT_CAPITAL: {
                            note = resBundle.getProperty("game.note.happyok");
                            goButtonText = resBundle.getProperty("game.button.go.ok");
                            break;
                    }
                    case RiskGame.STATE_BATTLE_WON: {
                            move.setVisible(true);
                            break;
                    }
                    // for gameState 4 look in FlashRiskAdapter.java
                    // for gameState 10 look in FlashRiskAdapter.java
                    default: break;
            }

            if (goButtonText!=null) {
                    gobutton.setFocusable(true);
                    gobutton.setText(goButtonText);
            }
            else {
                    gobutton.setFocusable(false);
                    gobutton.setText("");
            }
//
//            if (gameState!=RiskGame.STATE_DEFEND_YOURSELF) {
//                    cardsbutton.setEnabled(true);
//                    missionbutton.setEnabled(true);
//
//                    if (localGame) {
//                        undobutton.setEnabled(true);
//                        savebutton.setEnabled(true);
//                    }
//
//                    AutoEndGo.setEnabled(true);
//                    AutoEndGo.setBackground( Color.white );
//                    AutoEndGo.setSelected( myrisk.getAutoEndGo() );
//
//                    AutoDefend.setEnabled(true);
//                    AutoDefend.setBackground( Color.white );
//                    AutoDefend.setSelected( myrisk.getAutoDefend() );
//            }

            repaint(); // SwingGUI has this here, if here then not needed in set status
    }

    private void goOn() {
            if (gameState==RiskGame.STATE_TRADE_CARDS) {
                    go("endtrade");
            }
            else if (gameState==RiskGame.STATE_PLACE_ARMIES) {
                    go("autoplace");
            }
            else if (gameState==RiskGame.STATE_ATTACKING) {
                    pp.setC1(255);
                    go("endattack");
            }
            else if (gameState==RiskGame.STATE_FORTIFYING) {
                    pp.setC1(255);
                    go("nomove");
            }
            else if (gameState==RiskGame.STATE_END_TURN) {
                    go("endgo");
            }
            else if (gameState==RiskGame.STATE_GAME_OVER) {
                    go("continue"); // TODO check if we can first
                    //closeleave();
            }
            else if (gameState == RiskGame.STATE_SELECT_CAPITAL) {
                    int c1Id = pp.getC1();
                    pp.setC1(255);
                    go("capital " + c1Id);
            }
    }//private void goOn()

    public void go(String input) {

        //pp.setHighLight(PicturePanel.NO_COUNTRY);
        // Testing.append("Submitted: \""+input+"\"\n");

        //if (gameState!=2 || !myrisk.getGame().getSetup() ) { blockInput(); }

        myrisk.parser(input);

        // Console.setCaretPosition(Console.getDocument().getLength());

    }

    String note;
    public void mapClick(int[] countries) {

        if (gameState == RiskGame.STATE_PLACE_ARMIES) {
            if (countries.length==1) {
                //if ( e.getModifiers() == java.awt.event.InputEvent.BUTTON1_MASK ) {
                    go( "placearmies " + countries[0] + " 1" );
                //}
                //else {
                // TODO: make a method for adding 10 armies at a time
                //    go( "placearmies " + countries[0] + " 10" );
                //}
            }
        }
        else if (gameState == RiskGame.STATE_ATTACKING) {

            if (countries.length==0) {
                note=resBundle.getProperty("game.note.selectattacker");
            }
            else if (countries.length == 1) {
                note=resBundle.getProperty("game.note.selectdefender");
            }
            else {
                go("attack " + countries[0] + " " + countries[1]);
                note=resBundle.getProperty("game.note.selectattacker");
            }

        }
        else if (gameState == RiskGame.STATE_FORTIFYING) {
            if (countries.length==0) {
                note=resBundle.getProperty("game.note.selectsource");
            }
            else if (countries.length==1) {
                note=resBundle.getProperty("game.note.selectdestination");
            }
            else {
                note="";

                setupMove(1,countries[0] , countries[1], true);
                move.setVisible(true);
// TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO
                // TODO: clean up
                //pp.setC1(255);
                //pp.setC2(255);
                //note=resb.getString("game.note.selectsource");
            }
        }
        else if (gameState == RiskGame.STATE_SELECT_CAPITAL) {
            // do nothing ??
        }

    }

    public void mapRedrawRepaint(boolean redrawNeeded, boolean repaintNeeded) {
        if(redrawNeeded) {
            pp.repaintCountries( gamecontrol.getMapView() );
        }
        if (repaintNeeded) {
            pp.repaint();
        }
    }


    // #############################################################
    // moving!!!!
    // #############################################################

    Frame move;
    Slider slider;
    Button cancelMove;
    public void setupMove(int min, int c1num, int c2num, boolean tacmove) {
        if (move==null) {
            move = new Frame();
            slider = new Slider();
            move.add(slider);
            cancelMove = new Button(resBundle.getProperty("move.cancel"));
            cancelMove.setActionCommand("cancel");
            Button moveall = new Button(resBundle.getProperty("move.moveall"));
            moveall.setActionCommand("all");
            Button moveb = new Button(resBundle.getProperty("move.move"));
            moveb.setActionCommand("move");

            Panel moveControl = new Panel();
            moveControl.add(moveall);
            moveControl.add(moveb);
            moveControl.add(cancelMove);

            ActionListener moveAl = new ActionListener() {
                public void actionPerformed(String actionCommand) {

                    boolean tacmove = myrisk.getGame().getState()==RiskGame.STATE_FORTIFYING;
                    int c1num = pp.getC1();
                    int c2num = pp.getC2();
                    
                    if (actionCommand.equals("cancel")) {
                        move.setVisible(false);
                    }
                    else if (actionCommand.equals("all")) {
                        int src = myrisk.hasArmiesInt( c1num );
                        if (tacmove) {
                                go("movearmies " +myrisk.getGame().getCountryInt(c1num).getColor()+ " " +myrisk.getGame().getCountryInt(c2num).getColor()+ " " + (src-1) );
                        }
                        else {
                                go("move " + (src-1) );
                        }
                    }
                    else if (actionCommand.equals("move")) {
                        int move = ((Integer)slider.getValue());
                        if (tacmove) {
                                go("movearmies " +myrisk.getGame().getCountryInt(c1num).getColor()+ " " +myrisk.getGame().getCountryInt(c2num).getColor()+ " " + move );
                        }
                        else {
                                go("move " + move);
                        }
                    }
                }
            };

            cancelMove.addActionListener(moveAl);
            moveall.addActionListener(moveAl);
            moveb.addActionListener(moveAl);

            move.getContentPane().add(moveControl,Graphics.BOTTOM);
            move.setMaximum(true);
        }


        if (tacmove) {
                move.setTitle(resBundle.getProperty("move.title.tactical"));
                cancelMove.setVisible(true);
        }
        else {
                move.setTitle(resBundle.getProperty("move.title.captured"));
                cancelMove.setVisible(false);
        }

        int src = myrisk.hasArmiesInt( c1num );

        slider.setMinimum(min);
        slider.setMaximum( src-1 );
        slider.setValue(min);

        int spacig = Math.round( (src-1)/10f );
/* TODO
        if (spacig==0) {
                slider.setMajorTickSpacing(1);
        }
        else {
                slider.setMajorTickSpacing( spacig );
                slider.setMinorTickSpacing(1);
        }
*/

    }

}
