package net.yura.domination.mobile.flashgui;

import javax.microedition.lcdui.Graphics;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.engine.guishared.MapMouseListener;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.domination.mobile.MouseListener;
import net.yura.domination.mobile.PicturePanel;
import net.yura.domination.mobile.RiskMiniIO;
import net.yura.domination.mobile.simplegui.GamePanel;
import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.components.Button;
import net.yura.mobile.gui.components.Frame;
import net.yura.mobile.gui.components.Label;
import net.yura.mobile.gui.components.OptionPane;
import net.yura.mobile.gui.components.Panel;
import net.yura.mobile.gui.components.ScrollPane;
import net.yura.mobile.gui.components.Slider;
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
    GamePanel gamecontrol;
    Button gobutton;
    Label status;
    int gameState;
    String note;

    public GameActivity(Risk risk) {
        myrisk = risk;
        setMaximum(true);
    }
    
    public void startGame() {

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
        gobutton.addActionListener(this);
        gobutton.setActionCommand("go");

        Button saveButton = new Button("Save");
        saveButton.addActionListener(this);
        saveButton.setActionCommand("save");
        
        Panel gamepanel2 = new Panel();
        // stats
        // cards
        gamepanel2.add( saveButton );
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
        ScrollPane sp = new ScrollPane(pp) {
            // a little hack as we set setClip to false
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
        contentPane.add(status,Graphics.BOTTOM);
        setContentPane(contentPane);

        setUndecorated(true);
        //setName("GameFrame");
        setBackground(0xFF666666);

        setVisible(true);

    }
    
    public void actionPerformed(String actionCommand) {
        if ("go".equals(actionCommand)) {
            goOn();
        }
        else if ("save".equals(actionCommand)) {
            
            final TextField saveText = new TextField();
            saveText.setText( RiskMiniIO.getSaveGameName(myrisk.getGame()) );
            
            OptionPane.showOptionDialog(new ActionListener() {
                public void actionPerformed(String actionCommand) {
                    if ("ok".equals(actionCommand)) {
                        String name = RiskMiniIO.getSaveGameDirURL() + saveText.getText() +".save";
                        if (name!=null) {
                            go("savegame " + name );
                        }                        
                    }
                }
            }, saveText, resb.getProperty("game.menu.save") , OptionPane.OK_CANCEL_OPTION, OptionPane.QUESTION_MESSAGE, null, null, null);

        }
        else {
            throw new IllegalArgumentException("unknown command "+actionCommand);
        }
    }
 
    void setGameStatus(String state) {
        status.setText(state);
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

    private void go(String input) {

        blockInput();

        myrisk.parser(input);

    }
    
    private void blockInput() {

            pp.setHighLight(255);

            if (gameState!=RiskGame.STATE_PLACE_ARMIES || !myrisk.getGame().getSetup() ) { noInput(); }

    }

    public void noInput() {

            //cardsbutton.setEnabled(false);
            //missionbutton.setEnabled(false);
            //undobutton.setEnabled(false);
            //savebutton.setEnabled(false);
            //AutoEndGo.setEnabled(false);
            //AutoDefend.setEnabled(false);

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
                pp.repaintCountries( gamecontrol.getMapView() );
            }
            if (repaintNeeded) {
                pp.repaint();
            }
        }
    }

}
