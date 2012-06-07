package net.yura.domination.mobile.flashgui;

import javax.microedition.lcdui.Graphics;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.core.RiskGame;
import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.components.Button;
import net.yura.mobile.gui.components.Frame;
import net.yura.mobile.gui.components.Panel;
import net.yura.mobile.gui.components.Slider;
import net.yura.mobile.util.Properties;

/**
 * @author Yura
 */
public class MoveDialog extends Frame implements ActionListener {

    Properties resb = GameActivity.resb;
    
    Risk myrisk;
    Slider slider;
    Button cancelMove;
    
    int c1num,c2num;
    
    public MoveDialog(Risk risk) {
        myrisk = risk;


        slider = new Slider();
        add(slider);
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

        cancelMove.addActionListener(this);
        moveall.addActionListener(this);
        moveb.addActionListener(this);

        getContentPane().add(moveControl,Graphics.BOTTOM);
        setMaximum(true);


        setName("TransparentDialog");
        setForeground(0xFF000000);
        setBackground(0xAA000000);
    }
    
    public void setupMove(int min, int c1num, int c2num, boolean tacmove) {

        this.c1num = c1num;
        this.c2num = c2num;

        if (tacmove) {
                setTitle(resb.getProperty("move.title.tactical"));
                cancelMove.setVisible(true);
        }
        else {
                setTitle(resb.getProperty("move.title.captured"));
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
    
    public void actionPerformed(String actionCommand) {

        boolean tacmove = myrisk.getGame().getState()==RiskGame.STATE_FORTIFYING;

        if (actionCommand.equals("cancel")) {
            setVisible(false);
        }
        else if (actionCommand.equals("all")) {
            int src = myrisk.hasArmiesInt( c1num );
            if (tacmove) {
                    go("movearmies " +c1num+ " " +c2num+ " " + (src-1) );
            }
            else {
                    go("move " + (src-1) );
            }
        }
        else if (actionCommand.equals("move")) {
            int move = ((Integer)slider.getValue());
            if (tacmove) {
                    go("movearmies " +c1num+ " " +c2num+ " " + move );
            }
            else {
                    go("move " + move);
            }
        }
    }
    
    private void go(String input) {

        blockInput();

        myrisk.parser(input);

    }
    
    private void blockInput() {
        
            int gameState = myrisk.getGame().getState();
        
            if (gameState==RiskGame.STATE_BATTLE_WON || gameState==RiskGame.STATE_FORTIFYING) {

                    // this hides the dailog
                    setVisible(false);
            }
    }
    
    
}
