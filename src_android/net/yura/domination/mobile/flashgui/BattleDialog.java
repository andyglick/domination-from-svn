package net.yura.domination.mobile.flashgui;

import com.nokia.mid.ui.DirectGraphics;
import com.nokia.mid.ui.DirectUtils;
import java.util.Random;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.mobile.PicturePanel;
import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.Font;
import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.KeyEvent;
import net.yura.mobile.gui.Midlet;
import net.yura.mobile.gui.components.Button;
import net.yura.mobile.gui.components.Frame;
import net.yura.mobile.gui.components.Panel;
import net.yura.mobile.gui.layout.XULLoader;
import net.yura.mobile.util.Properties;

/**
 * @author Yura Mamyrin
 */
public class BattleDialog extends Frame implements ActionListener {

    Properties resb = GameActivity.resb;
    
    Risk myrisk;
    Sprite red_dice,blue_dice;
    Random r = new Random();
    Button rollButton,retreat;
    PicturePanel pp;

    public BattleDialog(Risk a,PicturePanel p) {
        myrisk = a;
        pp=p;

        red_dice = getDice("/red_dice.png");
        blue_dice = getDice("/blue_dice.png");

        setName("TransparentDialog");
        setForeground(0xFF000000);
        setBackground(0xAA000000);

        rollButton = new Button(resb.getProperty("battle.roll"));
        retreat = new Button( resb.getProperty("battle.retreat") );
        
        rollButton.addActionListener(this);
        rollButton.setActionCommand("fight");

        retreat.addActionListener(this);
        retreat.setActionCommand("retreat");
        
        Panel controls = new Panel();
        controls.add(rollButton);
        controls.add(retreat);
        
        Panel contentPane = getContentPane();
        contentPane.setLayout( new MoveDialog.DialogLayout( getImageAreaHeight() ) );
        contentPane.add(controls);

        setMaximum(true);
    }
    
    private int getImageAreaHeight() {
        
        // mdpi
        // 90 for countries
        // 90 for dice
        // full height 180
        // 180 * 0.75 = 135
        
        return XULLoader.adjustSizeToDensity(135);
    }
    
    private Sprite getDice(String name) {
        Image img = Midlet.createImage(name);
        
        int w = img.getWidth()/3;
        int h = img.getHeight()/3;
        
        if ( img.getWidth() % w != 0 || img.getHeight() % h != 0) {
            img = Image.createImage(img, 0, 0, w*3, h*3, 0);
        }
        
        return new Sprite(img, w, h); // 29x29
    }

    public void actionPerformed(String actionCommand) {
        if ("fight".equals( actionCommand )) {
            go("roll "+nod);
        }
        else if ("retreat".equals( actionCommand )) {
            go("retreat");
        }
    }
    
    int[] att,def; // these are the dice results
    int noda,nodd; // these are the number of spinning dice
    int c1num,c2num;
    boolean ani,canRetreat;
    int nod,max;
    //@Override
    public void run() throws InterruptedException {

        while(ani) {
            repaint();
            wait(200);
        }

    }


	/**
	 * Sets number of attacking dice
	 * @param n number of dice
	 */
	public void setNODAttacker(int n) {

		att=null;
		def=null;

		noda = n;

		repaint();

                ani = true;
		getDesktopPane().animateComponent(this);

	}

	/**
	 * Sets number of defending dice
	 * @param n number of dice
	 */
	public void setNODDefender(int n) {

		nodd = n;

	}

    public void showDiceResults(int[] atti, int[] defi) {

            ani = false;

            noda=0;
            nodd=0;

            att=atti;
            def=defi;

            repaint();

    }

    void needInput(int n, boolean c) {

        rollButton.setFocusable(true);
        max=n;
        nod=max;
        canRetreat=c;

        att=null;
        def=null;

        if (canRetreat) {
                retreat.setVisible(true);
                retreat.setMnemonic( KeyEvent.KEY_SOFTKEY2 );
                setTitle(resb.getProperty("battle.select.attack"));
        }
        else {
                setTitle(resb.getProperty("battle.select.defend"));
        }

        revalidate();
        repaint();

    }

    void setup(int c1num, int c2num) {
        this.c1num = c1num;
        this.c2num = c2num;
        reset();
    }

    private void blockInput() {
        
        int gameState = myrisk.getGame().getState();
        
        if (gameState==RiskGame.STATE_ROLLING || gameState==RiskGame.STATE_DEFEND_YOURSELF) {

                //this does not close it, just resets its params
                reset();
        }
    }
    
    private void go(String input) {
        
        blockInput();
        
        myrisk.parser(input);
        
    }
    
    public void reset() {
        rollButton.setFocusable(false);
        retreat.setVisible(false);
        retreat.setMnemonic( 0 );
        canRetreat=false;
        max=0;
        setTitle(resb.getProperty("battle.title"));
        
        revalidate();
        repaint();
    }

    private static final int DICE_NORMAL = 0;
    private static final int DICE_DARK = 1;
    private static final int COLOR_BLUE = 0xFF0000FF;
    private static final int COLOR_RED = 0xFFFF0000;

    @Override
    public void paintComponent(Graphics2D g) {

        Image c1img = pp.getCountryImage(c1num);
        Image c2img = pp.getCountryImage(c2num);
        int csrc = myrisk.hasArmiesInt( c1num );
        int cdes = myrisk.hasArmiesInt( c2num );
        int color1 = myrisk.getCurrentPlayerColor();
        int color2 = myrisk.getColorOfOwner( c2num );

        int imageAreaHeight = getImageAreaHeight();
        int heightOfComponents = ((MoveDialog.DialogLayout)getContentPane().getLayout()).getHeightOfComponents(getContentPane());
        // this is the MIDDLE of the images area
        int xOffset = getContentPane().getWidth() / 2;
        int yOffset = (getContentPane().getHeight()-heightOfComponents)/2 + imageAreaHeight/4 + getContentPane().getY();

        //g.setColor(0xFFFF0000);
        //g.drawRect( (getContentPane().getWidth()-imageAreaHeight)/2 , (getContentPane().getHeight()-heightOfComponents)/2 + getContentPane().getY(), imageAreaHeight, imageAreaHeight);
        
        MoveDialog.paintMove(g,xOffset,yOffset,c1img,c2img,color1,color2,csrc,cdes,0);

        // #####################################################
        // ################## drawing DICE!!!!! ################

        int y1 = yOffset + imageAreaHeight/4; // top of dice
        
        // just in case in the middle of the draw the att and def get set to null
        int[] atti=att;
        int[] defi=def;
        
        // this is the max defend dice allowed for this battle
        int deadDice = myrisk.hasArmiesInt(c2num);
        if (deadDice > myrisk.getGame().getMaxDefendDice()) {
            deadDice = myrisk.getGame().getMaxDefendDice();
        }

        int w = getWidth();
        int diceWidth = red_dice.getWidth();
        
        int ax = w/2 - MoveDialog.distanceFromCenter - diceWidth/2;
        int dx = w/2 + MoveDialog.distanceFromCenter - diceWidth/2;

        int y2 = y1 + red_dice.getHeight() + XULLoader.adjustSizeToDensity(1);
        int y3 = y2 + red_dice.getHeight() + XULLoader.adjustSizeToDensity(1);

        // selecting the number of attacking dice
        if (max != 0 && canRetreat) {

                g.drawSprite(red_dice, DICE_NORMAL, ax, y1);

                if (nod > 1) {
                    g.drawSprite( red_dice , DICE_NORMAL , ax, y2);
                }
                else if (max > 1) {
                    g.drawSprite( red_dice , DICE_DARK , ax, y2 );
                }

                if (nod > 2) {
                    g.drawSprite( red_dice, DICE_NORMAL , ax, y3 );
                }
                else if (max > 2) {
                    g.drawSprite( red_dice , DICE_DARK , ax, y3 );
                }

                // draw the dead dice

                g.drawSprite( blue_dice , DICE_DARK , dx, y1 );

                if (deadDice > 1) {
                    g.drawSprite( blue_dice , DICE_DARK , dx, y2 );
                }

                if (deadDice > 2) {
                    g.drawSprite( blue_dice , DICE_DARK , dx, y3 );
                }

        }
        // selecting the number of dice to defend
        else if (max != 0 ) {

                g.drawSprite( blue_dice , DICE_NORMAL , dx, y1 );

                if (nod > 1) {
                    g.drawSprite( blue_dice , DICE_NORMAL , dx, y2 );
                }
                else if (max > 1) {
                    g.drawSprite( blue_dice , DICE_DARK , dx, y2 );
                }


                if (nod > 2) {
                    g.drawSprite( blue_dice , DICE_NORMAL , dx, y3 );
                }
                else if (max > 2) {
                    g.drawSprite( blue_dice , DICE_DARK , dx, y3 );
                }

        }
        // battle open and waiting for the attacker to select there number of dice
        else if (max == 0 && nodd == 0 && atti == null && defi == null ) {

                // draw the dead dice
                g.drawSprite( blue_dice , DICE_DARK , dx, y1 );

                if (deadDice > 1) {
                    g.drawSprite( blue_dice , DICE_DARK , dx, y2 );
                }

                if (deadDice > 2) {
                    g.drawSprite( blue_dice , DICE_DARK , dx, y3 );
                }

                if (noda == 0) {

                        // draw dead dice for attacker
                        int AdeadDice = myrisk.hasArmiesInt(c1num)-1;
                        // we assume that the attacker can attack with max of 3 dice

                        g.drawSprite( red_dice , DICE_DARK , ax, y1 );

                        if (AdeadDice > 1) {
                                g.drawSprite( red_dice , DICE_DARK , ax, y2 );
                        }
                        if (AdeadDice > 2) {
                                g.drawSprite( red_dice , DICE_DARK , ax, y3 );
                        }

                }

        }

        // #####################################################
        // ##################### END DICE ######################

        final int SPINS_OFFSET = 3;

        if (noda != 0) {

                g.drawSprite( red_dice, SPINS_OFFSET+r.nextInt( 6 ) , ax, y1);

                if (noda > 1) {
                        g.drawSprite( red_dice, SPINS_OFFSET+r.nextInt( 6 ) , ax, y2);
                }
                if (noda > 2) {
                        g.drawSprite( red_dice, SPINS_OFFSET+r.nextInt( 6 ) , ax, y3);
                }

                //g.drawString("ROLLING ATTACKER " + noda +"    " + Math.random() , 50, 100);

        }

        if (nodd != 0) {

                g.drawSprite( blue_dice, SPINS_OFFSET+r.nextInt( 6 ) , dx, y1);

                if (nodd > 1) {
                        g.drawSprite( blue_dice, SPINS_OFFSET+r.nextInt( 6 ) , dx, y2);
                }
                if (nodd > 2) {
                    g.drawSprite( blue_dice, SPINS_OFFSET+r.nextInt( 6 ) , dx, y3);
                }

                //g.drawString("ROLLING DEFENDER " + nodd +"    " + Math.random(), 300, 100);

        }

        DirectGraphics g2 = DirectUtils.getDirectGraphics(g.getGraphics());
        
        int offset = (int)( red_dice.getWidth() / (29D/4D) +0.5 );
        int bottom = red_dice.getHeight() - offset   -1; // TODO not sure why -1??
        int halfDice = red_dice.getHeight()/2;
        
        if (atti != null && defi != null ) {
                {
                        int yCoords[] = {y1+offset, y1+bottom, y1+halfDice};
                        if (defi[0] >= atti[0]) {
                                int xCoords[] = {dx+offset, dx+offset, ax+bottom};
                                g2.fillPolygon(xCoords, 0,yCoords,0, xCoords.length, COLOR_BLUE);
                        }
                        else {
                                int xCoords[] = {ax+bottom, ax+bottom, dx+offset};
                                g2.fillPolygon(xCoords, 0, yCoords,0, xCoords.length, COLOR_RED);
                        }
                }
                if (atti.length > 1 && defi.length > 1) {
                        int yCoords[] = {y2+offset, y2+bottom, y2+halfDice};
                        if (defi[1] >= atti[1]) {
                                int xCoords[] = {dx+offset, dx+offset, ax+bottom};
                                g2.fillPolygon(xCoords, 0,yCoords, 0,xCoords.length,COLOR_BLUE);
                        }
                        else {
                                int xCoords[] = {ax+bottom, ax+bottom, dx+offset};
                                g2.fillPolygon(xCoords, 0,yCoords, 0,xCoords.length,COLOR_RED);
                        }
                }
                if (atti.length > 2 && defi.length > 2) {
                    int yCoords[] = {y3+offset, y3+bottom, y3+halfDice};
                    if (defi[2] >= atti[2]) {
                            int xCoords[] = {dx+offset, dx+offset, ax+bottom};
                            g2.fillPolygon(xCoords, 0,yCoords, 0,xCoords.length,COLOR_BLUE);
                    }
                    else {
                            int xCoords[] = {ax+bottom, ax+bottom, dx+offset};
                            g2.fillPolygon(xCoords,0, yCoords, 0,xCoords.length,COLOR_RED);
                    }
                }

                // draw attacker dice
                drawDice(true, atti[0] , ax, y1, g );

                if (atti.length > 1) {
                        drawDice(true, atti[1] , ax, y2, g );
                }
                if (atti.length > 2) {
                        drawDice(true, atti[2] , ax, y3, g );
                }

                // draw defender dice
                drawDice(false, defi[0] , dx, y1, g );

                if (defi.length > 1) {
                        drawDice(false, defi[1] , dx, y2, g );
                }

                if (defi.length > 2) {
                    drawDice(false, defi[2] , dx, y3, g );
                }
        }

    }

    public void drawDice(boolean isAttacker, int result,int dx,int dy,Graphics2D g) {
                g.translate(dx, dy);

		if (isAttacker) {
			g.drawSprite(red_dice, DICE_NORMAL , 0, 0 );
		}
		else {
			g.drawSprite(blue_dice, DICE_NORMAL , 0, 0 );
		}

                int w = red_dice.getWidth();
                
		int size= (int)(w / (29D/3D) +0.5);
                int close = (int)(w / (29D/7D) +0.5);
                int middle = (w-size)/2;
                int far = w-close-size;

                g.setColor( 0xC8FFFFFF );
                
		if (result==0) {
			g.fillOval(middle, middle, size, size);
		}
		else if (result==1) {
			g.fillOval(close, close, size, size);
			g.fillOval(far, far, size, size);
		}
		else if (result==2) {
			g.fillOval(close, close, size, size);
			g.fillOval(middle, middle, size, size);
			g.fillOval(far, far, size, size);
		}
		else if (result==3) {
			g.fillOval(close, close, size, size);
			g.fillOval(far, close, size, size);
			g.fillOval(far, far, size, size);
			g.fillOval(close, far, size, size);
		}
		else if (result==4) {
			g.fillOval(close, close, size, size);
			g.fillOval(far, close, size, size);
			g.fillOval(far, far, size, size);
			g.fillOval(close, far, size, size);
			g.fillOval(middle, middle, size, size);
		}
		else if (result==5) {
			g.fillOval(close, close, size, size);
			g.fillOval(far, close, size, size);
			g.fillOval(far, far, size, size);
			g.fillOval(close, far, size, size);
			g.fillOval(middle, close, size, size);
			g.fillOval(middle, far, size, size);
		}

		g.translate(-dx, -dy);
    }

}

