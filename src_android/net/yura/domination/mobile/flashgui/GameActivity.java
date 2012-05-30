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
 
    public Properties resb = CoreUtil.wrap(TranslationBundle.getBundle());
    
    Risk myrisk;
    PicturePanel pp;
    GamePanel gamecontrol;
    Button gobutton;
    Label status;
    int gameState;

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
            saveText.setText("mygame");
            
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
                    case RiskGame.STATE_BATTLE_WON: {
                            move.setVisible(true);
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
        if (pp!=null) {
            if(redrawNeeded) {
                pp.repaintCountries( gamecontrol.getMapView() );
            }
            if (repaintNeeded) {
                pp.repaint();
            }
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
            cancelMove = new Button(resb.getProperty("move.cancel"));
            cancelMove.setActionCommand("cancel");
            Button moveall = new Button(resb.getProperty("move.moveall"));
            moveall.setActionCommand("all");
            Button moveb = new Button(resb.getProperty("move.move"));
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
                move.setTitle(resb.getProperty("move.title.tactical"));
                cancelMove.setVisible(true);
        }
        else {
                move.setTitle(resb.getProperty("move.title.captured"));
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
