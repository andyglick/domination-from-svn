package net.yura.domination.mobile.flashgui;

import android.graphics.ColorMatrix;
import com.nokia.mid.ui.DirectGraphics;
import com.nokia.mid.ui.DirectUtils;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.mobile.PicturePanel;
import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.KeyEvent;
import net.yura.mobile.gui.components.Button;
import net.yura.mobile.gui.components.Frame;
import net.yura.mobile.gui.components.Panel;
import net.yura.mobile.gui.components.Slider;
import net.yura.mobile.util.Properties;

/**
 * @author Yura
 */
public class MoveDialog extends Frame implements ActionListener {

    PicturePanel pp;
    
    Properties resb = GameActivity.resb;
    
    Risk myrisk;
    Slider slider;
    Button cancelMove;
    
    int c1num,c2num;
    
    public MoveDialog(Risk risk,PicturePanel p) {
        myrisk = risk;
        pp = p;

        slider = new Slider();
        

        cancelMove = new Button(resb.getProperty("move.cancel"));
        cancelMove.setActionCommand("cancel");

        Button moveall = new Button(resb.getProperty("move.moveall"));
        moveall.setActionCommand("all");
        Button moveb = new Button(resb.getProperty("move.move"));
        moveb.setActionCommand("move");

        Panel moveControl = new Panel();
        moveControl.add(slider);
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
                cancelMove.setMnemonic( KeyEvent.KEY_SOFTKEY2 );
        }
        else {
                setTitle(resb.getProperty("move.title.captured"));
                cancelMove.setVisible(false);
                cancelMove.setMnemonic( 0 );
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


                @Override
		public void paintComponent(Graphics2D g) {

                    Image c1img = pp.getCountryImage(c1num);
                    Image c2img = pp.getCountryImage(c2num);
                    int csrc = myrisk.hasArmiesInt( c1num );
                    int cdes = myrisk.hasArmiesInt( c2num );
                    int move = (Integer)slider.getValue();
                    int color = myrisk.getCurrentPlayerColor();
                    
                    
                        ColorMatrix m = PicturePanel.RescaleOp( 0.5f, -1.0f);
                        m.preConcat(PicturePanel.gray);
                        m.postConcat( PicturePanel.getMatrix( PicturePanel.colorWithAlpha(color, 100) ) );
                        g.getGraphics().setColorMarix(m);
                    
			g.drawImage(c1img, 130-(c1img.getWidth()/2), 100-(c1img.getHeight()/2));
			g.drawImage(c2img, 350-(c2img.getWidth()/2), 100-(c2img.getHeight()/2));
                        
                        g.getGraphics().setColorMarix(null);

			g.setColor( 0xFF000000 );

			//tl = new TextLayout( country1.getName() , font, frc); // Display
			//tl.draw( g, (float) (130-(tl.getBounds().getWidth()/2)), (float)40 );

			//tl = new TextLayout( country2.getName() , font, frc); // Display
			//tl.draw( g, (float) (350-(tl.getBounds().getWidth()/2)), (float)40 );

			g.setColor( color );

			g.fillOval(120 , 90 , 20, 20);
			g.fillOval( 340 , 90 , 20, 20 );


                        int x=110;
                        int y=40;
                        int xCoords[] = {x+60, x+130, x+130, x+200, x+130, x+130, x+60};
                        int yCoords[] = {y+40,  y+40,  y+20,  y+60, y+100,  y+80, y+80};
                        DirectGraphics g2 = DirectUtils.getDirectGraphics(g.getGraphics());
                        g2.fillPolygon(xCoords, 0, yCoords, 0, xCoords.length, PicturePanel.colorWithAlpha(color, 150) );

			g.setColor( RiskUtil.getTextColorFor(color) );

			int noa;

			noa = csrc-move;

			if (noa < 10) {
				g.drawString( String.valueOf( noa ) , 126, 105 );
			}
			else if (noa < 100) {
				g.drawString( String.valueOf( noa ) , 123, 105 );
			}
			else {
				g.drawString( String.valueOf( noa ) , 120, 105 );
			}

			noa = cdes+move;

			if (noa < 10) {
				g.drawString( String.valueOf( noa ) , 346, 105 );
			}
			else if (noa < 100) {
				g.drawString( String.valueOf( noa ) , 343, 105 );
			}
			else {
				g.drawString( String.valueOf( noa ) , 340, 105 );
			}

			g.drawString( Integer.toString(move) , 240, 104);

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
