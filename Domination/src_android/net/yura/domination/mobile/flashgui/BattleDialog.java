package net.yura.domination.mobile.flashgui;

import com.nokia.mid.ui.DirectGraphics;
import com.nokia.mid.ui.DirectUtils;
import java.util.Random;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.core.RiskGame;
import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.Font;
import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.components.Button;
import net.yura.mobile.gui.components.Frame;
import net.yura.mobile.util.Properties;

/**
 * @author Yura Mamyrin
 */
public class BattleDialog extends Frame {

    Properties resb = GameActivity.resb;
    
    Risk myrisk;
    Sprite red_dice,blue_dice;
    Random r = new Random();
    Button rollButton;

    public BattleDialog(Risk a) {
        myrisk = a;

        try {
            Image red_img = Image.createImage("/red_dice.png");
            red_dice = new Sprite(red_img, red_img.getWidth()/3, red_img.getHeight()/3 ); // 29x29

            Image blue_img = Image.createImage("/blue_dice.png");
            blue_dice = new Sprite(blue_img, blue_img.getWidth()/3, blue_img.getHeight()/3 ); // 29x29
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        setName("TransparentDialog");
        setForeground(0xFF000000);

        rollButton = new Button(resb.getProperty("battle.roll"));
        getContentPane().add(rollButton,Graphics.TOP);

        ActionListener al = new ActionListener() {
            public void actionPerformed(String actionCommand) {
                go("roll "+nod);
            }
        };
        rollButton.addActionListener(al);

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
//                retreat.setVisible(true);
                setTitle(resb.getProperty("battle.select.attack"));
        }
        else {
                setTitle(resb.getProperty("battle.select.defend"));
        }

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
//        retreat.setVisible(false);
        canRetreat=false;
        max=0;
        setTitle(resb.getProperty("battle.title"));
    }

    private static final int DICE_NORMAL = 0;
    private static final int DICE_DARK = 1;
    private static final int COLOR_BLUE = 0xFF0000FF;
    private static final int COLOR_RED = 0xFFFF0000;

    public void paintComponent(Graphics2D g) {

        // just in case in the middle of the draw the att and def get set to null
        int[] atti=att;
        int[] defi=def;


        // we are not drawing the countires, we will use the ones already on the map behind thid dialog
        //g.drawSprite(c1img, 130-(c1img.getWidth()/2), 100-(c1img.getHeight()/2), this);
        //g.drawSprite(c2img, 350-(c2img.getWidth()/2), 100-(c2img.getHeight()/2), this);

        // not supported
        //g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Font font = g.getFont();
        g.setColor( getForeground() );

        //tl = new TextLayout( country1.getName(), font, frc); // Display
        //tl.draw( g, (float) (130-(tl.getBounds().getWidth()/2)), 40f );

        //tl = new TextLayout( country2.getName(), font, frc); // Display
        //tl.draw( g, (float) (350-(tl.getBounds().getWidth()/2)), 40f );

        g.drawString("TODO", 50, 50);

        //tl = new TextLayout( resb.getString("battle.select.dice") , font, frc);
        //tl.draw( g, (float) (240-(tl.getBounds().getWidth()/2)), 320f );
/*
        Ellipse2D ellipse;


        g.setColor( color1 );

        ellipse = new Ellipse2D.Double();
        ellipse.setFrame( 120 , 90 , 20, 20);
        g.fill(ellipse);

        g.setColor( color2 );

        ellipse = new Ellipse2D.Double();
        ellipse.setFrame( 340 , 90 , 20, 20);
        g.fill(ellipse);


        g.setColor( new Color(color1.getRed(), color1.getGreen(), color1.getBlue(), 150) );


        g.fillPolygon( arrow );

        int noa;

        g.setColor( RiskUIUtil.getTextColorFor(color1) );

        noa = myrisk.hasArmiesInt(c1num);

        if (noa < 10) {
                g.drawString( String.valueOf( noa ) , 126, 105 );
        }
        else if (noa < 100) {
                g.drawString( String.valueOf( noa ) , 123, 105 );
        }
        else {
                g.drawString( String.valueOf( noa ) , 120, 105 );
        }

        g.setColor( RiskUIUtil.getTextColorFor(color2) );

        noa = myrisk.hasArmiesInt(c2num);

        if (noa < 10) {
                g.drawString( String.valueOf( noa ) , 346, 105 );
        }
        else if (noa < 100) {
                g.drawString( String.valueOf( noa ) , 343, 105 );
        }
        else {
                g.drawString( String.valueOf( noa ) , 340, 105 );
        }
*/
        // #####################################################
        // ################## drawing DICE!!!!! ################

        // this is the max defend dice allowed for this battle
        int deadDice = myrisk.hasArmiesInt(c2num);
        if (deadDice > myrisk.getGame().getMaxDefendDice()) {
            deadDice = myrisk.getGame().getMaxDefendDice();
        }

        int ax = 116;
        int dx = 335;

        int y1 = 176;
        int y2 = 207;
        int y3 = 238;

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

        if (atti != null && defi != null ) {


                if (defi[0] >= atti[0]) {

                        int xCoords[] = {339, 339, 140};
                        int yCoords[] = {180, 200, 190};

                        g2.fillPolygon(xCoords, 0,yCoords,0, xCoords.length, COLOR_BLUE);

                }
                else {

                        int xCoords[] = {140, 140, 339};
                        int yCoords[] = {180, 200, 190};

                        g2.fillPolygon(xCoords, 0, yCoords,0, xCoords.length, COLOR_RED);

                }

                if (atti.length > 1 && defi.length > 1) {

                        if (defi[1] >= atti[1]) {

                                int xCoords[] = {339, 339, 140};
                                int yCoords[] = {211, 231, 221};

                                g2.fillPolygon(xCoords, 0,yCoords, 0,xCoords.length,COLOR_BLUE);

                        }
                        else {
                                int xCoords[] = {140, 140, 339};
                                int yCoords[] = {211, 231, 221};

                                g2.fillPolygon(xCoords, 0,yCoords, 0,xCoords.length,COLOR_RED);

                        }
                }

                if (atti.length > 2 && defi.length > 2) {

                    if (defi[2] >= atti[2]) {


                            int xCoords[] = {339, 339, 140};
                            int yCoords[] = {242, 262, 252};

                            g2.fillPolygon(xCoords, 0,yCoords, 0,xCoords.length,COLOR_BLUE);

                    }
                    else {


                            int xCoords[] = {140, 140, 339};
                            int yCoords[] = {242, 262, 252};

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

		int size=3;
                int offset=4;

		g.setColor( 0xC8FFFFFF );

		if (result==0) {

			g.fillOval(offset+9, offset+9, size, size);

		}
		else if (result==1) {

			g.fillOval(offset+3, offset+3, size, size);
			g.fillOval(offset+15, offset+15, size, size);

		}
		else if (result==2) {

			g.fillOval(offset+3, offset+3, size, size);
			g.fillOval(offset+9, offset+9, size, size);
			g.fillOval(offset+15, offset+15, size, size);

		}
		else if (result==3) {

			g.fillOval(offset+3, offset+3, size, size);
			g.fillOval(offset+15, offset+3, size, size);
			g.fillOval(offset+15, offset+15, size, size);
			g.fillOval(offset+3, offset+15, size, size);

		}
		else if (result==4) {

			g.fillOval(offset+3, offset+3, size, size);
			g.fillOval(offset+15, offset+3, size, size);
			g.fillOval(offset+15, offset+15, size, size);
			g.fillOval(offset+3, offset+15, size, size);
			g.fillOval(offset+9, offset+9, size, size);

		}
		else if (result==5) {

			g.fillOval(offset+3, offset+3, size, size);
			g.fillOval(offset+15, offset+3, size, size);
			g.fillOval(offset+15, offset+15, size, size);
			g.fillOval(offset+3, offset+15, size, size);
			g.fillOval(offset+9, offset+3, size, size);
			g.fillOval(offset+9, offset+15, size, size);

		}

		g.translate(-dx, -dy);
    }

}

