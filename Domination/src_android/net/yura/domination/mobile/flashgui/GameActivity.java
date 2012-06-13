package net.yura.domination.mobile.flashgui;

import javax.microedition.lcdui.Graphics;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.engine.guishared.MapMouseListener;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.domination.mobile.MiniUtil;
import net.yura.domination.mobile.MouseListener;
import net.yura.domination.mobile.PicturePanel;
import net.yura.domination.mobile.RiskMiniIO;
import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.Icon;
import net.yura.mobile.gui.KeyEvent;
import net.yura.mobile.gui.components.Button;
import net.yura.mobile.gui.components.CheckBox;
import net.yura.mobile.gui.components.Frame;
import net.yura.mobile.gui.components.Label;
import net.yura.mobile.gui.components.Menu;
import net.yura.mobile.gui.components.OptionPane;
import net.yura.mobile.gui.components.Panel;
import net.yura.mobile.gui.components.ScrollPane;
import net.yura.mobile.gui.components.TextField;
import net.yura.mobile.gui.components.Window;
import net.yura.mobile.gui.layout.BorderLayout;
import net.yura.mobile.util.Properties;
import net.yura.swingme.core.CoreUtil;

/**
 * @author Yura
 */
public class GameActivity extends Frame implements ActionListener {
 
    public static Properties resb = CoreUtil.wrap(TranslationBundle.getBundle());
    
    Risk myrisk;
    PicturePanel pp;
    MapViewChooser mapViewControl;
    Button gobutton,closebutton,savebutton,undobutton;
    String status,note;
    int gameState;

    private CheckBox AutoEndGo,AutoDefend;
    private Button cardsbutton,missionbutton;

    public GameActivity(Risk risk) {
        myrisk = risk;
        setMaximum(true);

        pp = new PicturePanel(myrisk);

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
        
        // MWMWMWMWMWMWM MENU MWMWMWMWMWMWMW
        
        savebutton = new Button( resb.getProperty("game.menu.save") );
        savebutton.addActionListener(this);
        savebutton.setActionCommand("save");
        
        Button graphbutton = new Button( resb.getProperty("game.button.statistics") );
        graphbutton.addActionListener(this);
        graphbutton.setActionCommand("graph");
        
        undobutton = new Button( resb.getProperty("game.button.undo") );
        undobutton.addActionListener(this);
        undobutton.setActionCommand("undo");

        AutoEndGo = new CheckBox( resb.getProperty("game.menu.autoendgo") );
        AutoEndGo.setActionCommand("autoendgo");
        AutoEndGo.addActionListener(this);

        AutoDefend = new CheckBox( resb.getProperty("game.menu.autodefend") );
        AutoDefend.setActionCommand("autodefend");
        AutoDefend.addActionListener(this);

        Button helpbutton = new Button( resb.getProperty("game.menu.manual") );
        helpbutton.addActionListener(this);
        helpbutton.setActionCommand("help");
        
        
        Menu menu = new Menu();
        menu.setMnemonic(KeyEvent.KEY_SOFTKEY1);
        menu.setActionCommand("menu");
        menu.addActionListener(this);
        menu.setName("ActionbarMenuButton");
        menu.setIcon( new Icon("/menu.png") );
        menu.add( savebutton );
        menu.add( graphbutton );
        menu.add( undobutton );
        menu.add( AutoEndGo );
        menu.add( AutoDefend );
        menu.add( helpbutton );
        
        // MWMWMWMWMWMWM END MENU MWMWMWMWMWMWMW

        gobutton = new Button();
        gobutton.addActionListener(this);
        gobutton.setActionCommand("go");

        cardsbutton = new Button();
        cardsbutton.setToolTipText(resb.getProperty("game.button.cards"));
        
        missionbutton = new Button();
        missionbutton.setToolTipText(resb.getProperty("game.button.mission"));
        
        Panel gamecontrol = new Panel( new BorderLayout() );
        gamecontrol.setName("TransPanel");
        
        mapViewControl = new MapViewChooser(pp);
        gamecontrol.add(mapViewControl);
        
        closebutton = new Button();
        closebutton.setMnemonic( KeyEvent.KEY_END );
        closebutton.setActionCommand("close");
        closebutton.addActionListener(this);
        gamecontrol.add(closebutton,Graphics.LEFT);
        
        gamecontrol.add(menu,Graphics.RIGHT);
        
        Panel gamepanel2 = new Panel();
        gamepanel2.setName("TransPanel");
        gamepanel2.add( cardsbutton );
        gamepanel2.add( missionbutton );
        gamepanel2.add( gobutton );
        
        
        
        Panel mainWindow = new Panel( new BorderLayout() );
        ScrollPane sp = new ScrollPane(pp) {
            // a little hack as we set setClip to false
            @Override
            public void repaint() {
                Window w = getWindow();
                if (w!=null) {
                    w.repaint();
                }
            }
        };


        //sp.setMode( ScrollPane.MODE_FLOATING_SCROLLBARS );
        sp.setClip(false);
        mainWindow.add( sp );
        mainWindow.add(gamecontrol,Graphics.TOP);
        mainWindow.add(gamepanel2,Graphics.BOTTOM);

        Panel contentPane = new Panel( new BorderLayout() );
        contentPane.add( mainWindow );
        setContentPane(contentPane);
        
        
        setUndecorated(true);
        //setName("GameFrame");
        setBackground(0xFF666666);
    }
    
    boolean localGame;
    /**
     * @see net.yura.domination.ui.flashgui.GameFrame#setup(boolean) 
     */
    public void startGame(boolean localGame) {
        this.localGame = localGame;

        closebutton.setText( getLeaveCloseText() );
        
        // ============================================ setup UI

        try {
            pp.load();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        mapViewControl.resetMapView();

        // ============================================ show

        setVisible(true);

    }
    
    public void actionPerformed(String actionCommand) {
        if ("go".equals(actionCommand)) {
            goOn();
        }
        else if ("menu".equals(actionCommand)) {
            
            	if (myrisk.getGame().getCurrentPlayer()!=null) {

			AutoEndGo.setSelected( myrisk.getAutoEndGo() );
			AutoDefend.setSelected( myrisk.getAutoDefend() );

		}
            
        }
        else if ("save".equals(actionCommand)) {
            
            final TextField saveText = new TextField();
            saveText.setText( MiniUtil.getSaveGameName(myrisk.getGame()) );
            
            OptionPane.showOptionDialog(new ActionListener() {
                public void actionPerformed(String actionCommand) {
                    if ("ok".equals(actionCommand)) {
                        String name = MiniUtil.getSaveGameDirURL() + saveText.getText() +".save";
                        go("savegame " + name );
                    }
                }
            }, saveText, resb.getProperty("game.menu.save") , OptionPane.OK_CANCEL_OPTION, OptionPane.QUESTION_MESSAGE, null, null, null);

        }
        else if ("graph".equals(actionCommand)) {
            
            
        }
        else if ("undo".equals(actionCommand)) {
            pp.setC1(PicturePanel.NO_COUNTRY);
            pp.setC2(PicturePanel.NO_COUNTRY);
            go("undo");
        }
        else if ("autoendgo".equals(actionCommand)) {
            go("autoendgo "+(AutoEndGo.isSelected()?"on":"off"));
        }
        else if ("autodefend".equals(actionCommand)) {
            go("autodefend "+(AutoDefend.isSelected()?"on":"off"));
        }
        else if ("help".equals(actionCommand)) {
            MiniUtil.openHelp();
        }
        else if ("close".equals(actionCommand)) {

                OptionPane.showOptionDialog(new ActionListener() {
                public void actionPerformed(String actionCommand) {
                    if ("ok".equals(actionCommand)) {
                        go("closegame");
                    }
                }
            }, resb.getProperty("game.areyousurequit"), getLeaveCloseText() , OptionPane.OK_CANCEL_OPTION, OptionPane.QUESTION_MESSAGE, null, null, null);

        }
        else {
            throw new IllegalArgumentException("unknown command "+actionCommand);
        }
    }

    String getLeaveCloseText() {
        return resb.getProperty( localGame ? "game.menu.close" : "game.menu.leave");
    }
    
    void setGameStatus(String state) {
        status = state;
    }
    

    public void needInput(int s) {
            gameState=s;
            String goButtonText=null;
            switch (gameState) {
                    case RiskGame.STATE_TRADE_CARDS: {
                            // after wiping out someone if you go into trade mode
                            pp.setC1(255);
                            pp.setC2(255);
                            goButtonText = resb.getProperty("game.button.go.endtrade");
                            break;
                    }
                    case RiskGame.STATE_PLACE_ARMIES: {
//                            if (setupDone==false) {
                                    goButtonText = resb.getProperty("game.button.go.autoplace");
//                            }
                            break;
                    }
                    case RiskGame.STATE_ATTACKING: {
                            pp.setC1(255);
                            pp.setC2(255);
                            note = resb.getProperty("game.note.selectattacker");
                            goButtonText = resb.getProperty("game.button.go.endattack");
                            break;
                    }
                    case RiskGame.STATE_FORTIFYING: {
                            note = resb.getProperty("game.note.selectsource");
                            goButtonText = resb.getProperty("game.button.go.nomove");
                            break;
                    }
                    case RiskGame.STATE_END_TURN: {
                            goButtonText = resb.getProperty("game.button.go.endgo");
                            break;
                    }
                    case RiskGame.STATE_GAME_OVER: {
//                            if (localGame) {
                                    goButtonText = resb.getProperty("game.button.go.closegame");
//                            }
//                            else {
//                                    goButtonText = resBundle.getProperty("game.button.go.leavegame");
//                            }
                            break;

                    }
                    case RiskGame.STATE_SELECT_CAPITAL: {
                            note = resb.getProperty("game.note.happyok");
                            goButtonText = resb.getProperty("game.button.go.ok");
                            break;
                    }
                    // for gameState 4 look in FlashRiskAdapter.java
                    // for gameState 10 look in FlashRiskAdapter.java
                    default: break;
            }

            if (gobutton!=null) {
                if (goButtonText!=null) {
                        gobutton.setFocusable(true);
                        gobutton.setText(goButtonText);
                }
                else {
                        gobutton.setFocusable(false);
                        gobutton.setText("");
                }
            }

            if (gameState!=RiskGame.STATE_DEFEND_YOURSELF) {
                    cardsbutton.setFocusable(true);
                    missionbutton.setFocusable(true);

                    if (localGame) {
                        undobutton.setFocusable(true);
                        savebutton.setFocusable(true);
                    }

                    AutoEndGo.setFocusable(true);
                    //AutoEndGo.setBackground( Color.white );
                    AutoEndGo.setSelected( myrisk.getAutoEndGo() );

                    AutoDefend.setFocusable(true);
                    //AutoDefend.setBackground( Color.white );
                    AutoDefend.setSelected( myrisk.getAutoDefend() );
            }

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

    private void go(String input) {

        blockInput();

        myrisk.parser(input);

    }
    
    private void blockInput() {

            pp.setHighLight(255);

            if (gameState!=RiskGame.STATE_PLACE_ARMIES || !myrisk.getGame().getSetup() ) { noInput(); }

    }

    public void noInput() {

            cardsbutton.setFocusable(false);
            missionbutton.setFocusable(false);
            undobutton.setFocusable(false);
            savebutton.setFocusable(false);
            AutoEndGo.setFocusable(false);
            AutoDefend.setFocusable(false);

            gobutton.setText("");
            gobutton.setFocusable(false);

            note="";
            gameState=0;

    }

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
                note=resb.getProperty("game.note.selectattacker");
            }
            else if (countries.length == 1) {
                note=resb.getProperty("game.note.selectdefender");
            }
            else {
                go("attack " + countries[0] + " " + countries[1]);
                note=resb.getProperty("game.note.selectattacker");
            }

        }
        else if (gameState == RiskGame.STATE_FORTIFYING) {
            if (countries.length==0) {
                note=resb.getProperty("game.note.selectsource");
            }
            else if (countries.length==1) {
                note=resb.getProperty("game.note.selectdestination");
            }
            else {
                note="";

                MoveDialog move = new MoveDialog(myrisk) {
                    @Override
                    public void setVisible(boolean b) { // catch closing of the dialog
                        super.setVisible(b);
                        if (!b) {
                            // clean up
                            pp.setC1(255);
                            pp.setC2(255);
                            note=resb.getProperty("game.note.selectsource");                            
                        }
                    }
                };
                
                move.setupMove(1,countries[0] , countries[1], true);
                move.setVisible(true);

            }
        }
        else if (gameState == RiskGame.STATE_SELECT_CAPITAL) {
            // do nothing ??
        }

    }

    public void mapRedrawRepaint(boolean redrawNeeded, boolean repaintNeeded) {
        if (pp!=null) {
            if(redrawNeeded) {
                pp.repaintCountries( mapViewControl.getMapView() );
            }
            if (repaintNeeded) {
                pp.repaint();
            }
        }
    }

}
