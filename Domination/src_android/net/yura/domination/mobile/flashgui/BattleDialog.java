package net.yura.domination.mobile.flashgui;

import com.nokia.mid.ui.DirectGraphics;
import com.nokia.mid.ui.DirectUtils;
import java.util.Random;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;
import net.yura.domination.engine.Risk;
import net.yura.mobile.gui.Font;
import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.components.Frame;
import net.yura.mobile.util.Properties;

/**
 * @author Yura Mamyrin
 */
public class BattleDialog extends Frame {

    MiniFlashGUI mainFrame;
    Sprite red_dice,blue_dice;
    Properties resBundle;
    Random r = new Random();

    public BattleDialog(MiniFlashGUI a) {
        mainFrame = a;
        resBundle = mainFrame.resBundle;

        setTitle( resBundle.getProperty("battle.title") );

        try {
            Image red_img = Image.createImage("/red_dice.png");
            red_dice = new Sprite(red_img, red_img.getWidth()/3, red_img.getHeight()/3 ); // 29x29

            Image blue_img = Image.createImage("/blue_dice.png");
            blue_dice = new Sprite(blue_img, blue_img.getWidth()/3, blue_img.getHeight()/3 ); // 29x29

            //ProgressBar redbar = new ProgressBar(red_dice);
            //ProgressBar bluebar = new ProgressBar(blue_dice);

        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        setName("TransparentDialog");
    }

    int[] att,def; // these are the dice results
    int noda,nodd; // these are the number of spinning dice
    int c1num,c2num;
    boolean ani,canRetreat;
    int nod,max;
    //@Override
    public void animate() throws InterruptedException {

        while(ani) {
            repaint();
            wait(200);
        }

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

//        button.setEnabled(true);
        max=n;
        nod=max;
        canRetreat=c;

        att=null;
        def=null;

        if (canRetreat) {
//                retreat.setVisible(true);
//                setTitle(resb.getString("battle.select.attack"));
        }
        else {
//                setTitle(resb.getString("battle.select.defend"));
        }

        repaint();

    }

    void setup(int c1num, int c2num) {
        this.c1num = c1num;
        this.c2num = c2num;
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
        g.setColor( getCurrentForeground() );

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

        Risk myrisk = mainFrame.myrisk;

        // this is the max defend dice allowed for this battle
        int deadDice = myrisk.hasArmiesInt(c2num);
        if (deadDice > myrisk.getGame().getMaxDefendDice()) {
            deadDice = myrisk.getGame().getMaxDefendDice();
        }

        // selecting the number of attacking dice
        if (max != 0 && canRetreat) {

                g.drawSprite(red_dice, DICE_NORMAL, 120, 180);

                if (nod > 1) {
                    g.drawSprite( red_dice , DICE_NORMAL , 120, 211);
                }
                else if (max > 1) {
                    g.drawSprite( red_dice , DICE_DARK , 120, 211 );
                }

                if (nod > 2) {
                    g.drawSprite( red_dice, DICE_NORMAL , 120, 242 );
                }
                else if (max > 2) {
                    g.drawSprite( red_dice , DICE_DARK , 120, 242 );
                }

                // draw the dead dice

                g.drawSprite( blue_dice , DICE_DARK , 339, 180 );

                if (deadDice > 1) {
                    g.drawSprite( blue_dice , DICE_DARK , 339, 211 );
                }

                if (deadDice > 2) {
                    g.drawSprite( blue_dice , DICE_DARK , 339, 242 );
                }

        }
        // selecting the number of dice to defend
        else if (max != 0 ) {

                g.drawSprite( blue_dice , DICE_NORMAL , 339, 180 );

                if (nod > 1) {
                    g.drawSprite( blue_dice , DICE_NORMAL , 339, 211 );
                }
                else if (max > 1) {
                    g.drawSprite( blue_dice , DICE_DARK , 339, 211 );
                }


                if (nod > 2) {
                    g.drawSprite( blue_dice , DICE_NORMAL , 339, 242 );
                }
                else if (max > 2) {
                    g.drawSprite( blue_dice , DICE_DARK , 339, 242 );
                }

        }
        // battle open and waiting for the attacker to select there number of dice
        else if (max == 0 && nodd == 0 && atti == null && defi == null ) {

                // draw the dead dice
                g.drawSprite( blue_dice , DICE_DARK , 339, 180 );

                if (deadDice > 1) {
                    g.drawSprite( blue_dice , DICE_DARK , 339, 211 );
                }

                if (deadDice > 2) {
                    g.drawSprite( blue_dice , DICE_DARK , 339, 242 );
                }

                if (noda == 0) {

                        // draw dead dice for attacker
                        int AdeadDice = myrisk.hasArmiesInt(c1num)-1;
                        // we assume that the attacker can attack with max of 3 dice

                        g.drawSprite( red_dice , DICE_DARK , 120, 180 );

                        if (AdeadDice > 1) {
                                g.drawSprite( red_dice , DICE_DARK , 120, 211 );
                        }
                        if (AdeadDice > 2) {
                                g.drawSprite( red_dice , DICE_DARK , 120, 242 );
                        }

                }

        }

        // #####################################################
        // ##################### END DICE ######################

        final int SPINS_OFFSET = 3;

        if (noda != 0) {

                g.drawSprite( red_dice, SPINS_OFFSET+r.nextInt( 6 ) , 116, 176);

                if (noda > 1) {
                        g.drawSprite( red_dice, SPINS_OFFSET+r.nextInt( 6 ) , 116, 207);
                }
                if (noda > 2) {
                        g.drawSprite( red_dice, SPINS_OFFSET+r.nextInt( 6 ) , 116, 238);
                }

                //g.drawString("ROLLING ATTACKER " + noda +"    " + Math.random() , 50, 100);

        }

        if (nodd != 0) {

                g.drawSprite( blue_dice, SPINS_OFFSET+r.nextInt( 6 ) , 335, 176);

                if (nodd > 1) {
                        g.drawSprite( blue_dice, SPINS_OFFSET+r.nextInt( 6 ) , 335, 207);
                }
                if (nodd > 2) {
                    g.drawSprite( blue_dice, SPINS_OFFSET+r.nextInt( 6 ) , 335, 238);
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
                drawDice(true, atti[0] , 120, 180, g );

                if (atti.length > 1) {
                        drawDice(true, atti[1] , 120, 211, g );
                }
                if (atti.length > 2) {
                        drawDice(true, atti[2] , 120, 242, g );
                }

                // draw defender dice
                drawDice(false, defi[0] , 339, 180, g );

                if (defi.length > 1) {
                        drawDice(false, defi[1] , 339, 211, g );
                }

                if (defi.length > 2) {
                    drawDice(false, defi[2] , 339, 242, g );
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

		g.setColor( 0xC8FFFFFF );

		if (result==0) {

			g.fillOval(9, 9, size, size);

		}
		else if (result==1) {

			g.fillOval(3, 3, size, size);
			g.fillOval(15, 15, size, size);

		}
		else if (result==2) {

			g.fillOval(3, 3, size, size);
			g.fillOval(9, 9, size, size);
			g.fillOval(15, 15, size, size);

		}
		else if (result==3) {

			g.fillOval(3, 3, size, size);
			g.fillOval(15, 3, size, size);
			g.fillOval(15, 15, size, size);
			g.fillOval(3, 15, size, size);

		}
		else if (result==4) {

			g.fillOval(3, 3, size, size);
			g.fillOval(15, 3, size, size);
			g.fillOval(15, 15, size, size);
			g.fillOval(3, 15, size, size);
			g.fillOval(9, 9, size, size);

		}
		else if (result==5) {

			g.fillOval(3, 3, size, size);
			g.fillOval(15, 3, size, size);
			g.fillOval(15, 15, size, size);
			g.fillOval(3, 15, size, size);
			g.fillOval(9, 3, size, size);
			g.fillOval(9, 15, size, size);

		}

		g.translate(-dx, -dy);
    }

}

