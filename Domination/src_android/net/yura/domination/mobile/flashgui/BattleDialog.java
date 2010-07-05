package net.yura.domination.mobile.flashgui;

import and.awt.Color;
import com.nokia.mid.ui.DirectGraphics;
import com.nokia.mid.ui.DirectUtils;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;
import net.yura.domination.engine.RiskUtil;
import net.yura.mobile.gui.Font;
import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.components.Frame;

/**
 * @author Yura Mamyrin
 */
public class BattleDialog extends Frame {

    MiniFlashGUI mainFrame;

    public BattleDialog(MiniFlashGUI a) {
        mainFrame = a;

        setTitle( mainFrame.resBundle.getProperty("battle.title") );

        try {
            Image red_img = Image.createImage("/red_dice.png");
            Sprite red_dice = new Sprite(red_img, red_img.getWidth()/3, red_img.getHeight()/3 ); // 29x29

            Image blue_img = Image.createImage("/blue_dice.png");
            Sprite blue_dice = new Sprite(blue_img, blue_img.getWidth()/3, blue_img.getHeight()/3 ); // 29x29

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
    boolean ani;
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

    public void paintComponent(Graphics2D g) {

        // just in case in the middle of the draw the att and def get set to null
        int[] atti=att;
        int[] defi=def;


        // we are not drawing the countires, we will use the ones already on the map behind thid dialog
        //g.drawImage(c1img, 130-(c1img.getWidth()/2), 100-(c1img.getHeight()/2), this);
        //g.drawImage(c2img, 350-(c2img.getWidth()/2), 100-(c2img.getHeight()/2), this);

        // not supported
        //g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Font font = g.getFont();
        g.setColor( getCurrentForeground() );

        //tl = new TextLayout( country1.getName(), font, frc); // Display
        //tl.draw( g, (float) (130-(tl.getBounds().getWidth()/2)), 40f );

        //tl = new TextLayout( country2.getName(), font, frc); // Display
        //tl.draw( g, (float) (350-(tl.getBounds().getWidth()/2)), 40f );

        g.drawString("TODO", 5, 5);

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

        // #####################################################
        // ################## drawing DICE!!!!! ################

        // this is the max defend dice allowed for this battle
        int deadDice = myrisk.hasArmiesInt(c2num);
        if (deadDice > myrisk.getGame().getMaxDefendDice()) {
            deadDice = myrisk.getGame().getMaxDefendDice();
        }

        // selecting the number of attacking dice
        if (max != 0 && canRetreat) {

                g.drawImage( Battle.getSubimage(481, 0, 21, 21) , 120, 180, this );

                if (nod > 1) {
                    g.drawImage( Battle.getSubimage(481, 0, 21, 21) , 120, 211, this );
                }
                else if (max > 1) {
                    g.drawImage( Battle.getSubimage(502, 0, 21, 21) , 120, 211, this );
                }

                if (nod > 2) {
                    g.drawImage( Battle.getSubimage(481, 0, 21, 21) , 120, 242, this );
                }
                else if (max > 2) {
                    g.drawImage( Battle.getSubimage(502, 0, 21, 21) , 120, 242, this );
                }

                // draw the dead dice

                g.drawImage( Battle.getSubimage(502, 21, 21, 21) , 339, 180, this );

                if (deadDice > 1) {
                    g.drawImage( Battle.getSubimage(502, 21, 21, 21) , 339, 211, this );
                }

                if (deadDice > 2) {
                    g.drawImage( Battle.getSubimage(502, 21, 21, 21) , 339, 242, this );
                }

        }
        // selecting the number of dice to defend
        else if (max != 0 ) {

                g.drawImage( Battle.getSubimage(481, 21, 21, 21) , 339, 180, this );

                if (nod > 1) {
                    g.drawImage( Battle.getSubimage(481, 21, 21, 21) , 339, 211, this );
                }
                else if (max > 1) {
                    g.drawImage( Battle.getSubimage(502, 21, 21, 21) , 339, 211, this );
                }


                if (nod > 2) {
                    g.drawImage( Battle.getSubimage(481, 21, 21, 21) , 339, 242, this );
                }
                else if (max > 2) {
                    g.drawImage( Battle.getSubimage(502, 21, 21, 21) , 339, 242, this );
                }

        }
        // battle open and waiting for the attacker to select there number of dice
        else if (max == 0 && nodd == 0 && atti == null && defi == null ) {

                // draw the dead dice
                g.drawImage( Battle.getSubimage(502, 21, 21, 21) , 339, 180, this );

                if (deadDice > 1) {
                    g.drawImage( Battle.getSubimage(502, 21, 21, 21) , 339, 211, this );
                }

                if (deadDice > 2) {
                    g.drawImage( Battle.getSubimage(502, 21, 21, 21) , 339, 242, this );
                }

                if (noda == 0) {

                        // draw dead dice for attacker
                        int AdeadDice = myrisk.hasArmiesInt(c1num)-1;
                        // we assume that the attacker can attack with max of 3 dice

                        g.drawImage( Battle.getSubimage(502, 0, 21, 21) , 120, 180, this );

                        if (AdeadDice > 1) {
                                g.drawImage( Battle.getSubimage(502, 0, 21, 21) , 120, 211, this );
                        }
                        if (AdeadDice > 2) {
                                g.drawImage( Battle.getSubimage(502, 0, 21, 21) , 120, 242, this );
                        }

                }

        }

        // #####################################################
        // ##################### END DICE ######################

        drawDiceAnimated(g);

        if (atti != null && defi != null ) {


                if (defi[0] >= atti[0]) {

                        g.setColor( Color.blue );

                        int xCoords[] = {339, 339, 140};
                        int yCoords[] = {180, 200, 190};

                        g.fillPolygon(xCoords, yCoords, xCoords.length);

                }
                else {

                        g.setColor( Color.red );

                        int xCoords[] = {140, 140, 339};
                        int yCoords[] = {180, 200, 190};

                        g.fillPolygon(xCoords, yCoords, xCoords.length);

                }

                if (atti.length > 1 && defi.length > 1) {

                        if (defi[1] >= atti[1]) {

                                g.setColor( Color.blue );

                                int xCoords[] = {339, 339, 140};
                                int yCoords[] = {211, 231, 221};

                                g.fillPolygon(xCoords, yCoords, xCoords.length);

                        }
                        else {

                                g.setColor( Color.red );

                                int xCoords[] = {140, 140, 339};
                                int yCoords[] = {211, 231, 221};

                                g.fillPolygon(xCoords, yCoords, xCoords.length);

                        }
                }

                if (atti.length > 2 && defi.length > 2) {

                    if (defi[2] >= atti[2]) {

                            g.setColor( Color.blue );

                            int xCoords[] = {339, 339, 140};
                            int yCoords[] = {242, 262, 252};

                            g.fillPolygon(xCoords, yCoords, xCoords.length);

                    }
                    else {

                            g.setColor( Color.red );

                            int xCoords[] = {140, 140, 339};
                            int yCoords[] = {242, 262, 252};

                            g.fillPolygon(xCoords, yCoords, xCoords.length);

                    }
                }


                // draw attacker dice
                g.drawImage( getDice(true, atti[0] ), 120, 180, this );

                if (atti.length > 1) {
                        g.drawImage( getDice(true, atti[1] ), 120, 211, this );
                }
                if (atti.length > 2) {
                        g.drawImage( getDice(true, atti[2] ), 120, 242, this );
                }

                // draw defender dice
                g.drawImage( getDice(false, defi[0] ), 339, 180, this );

                if (defi.length > 1) {
                        g.drawImage( getDice(false, defi[1] ), 339, 211, this );
                }

                if (defi.length > 2) {
                    g.drawImage( getDice(false, defi[2] ), 339, 242, this );
                }

        }
*/
    }

}

