package net.yura.domination.mobile;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import and.awt.Color;
import and.awt.Polygon;
import and.awt.geom.Ellipse2D;

import android.graphics.ColorMatrix;
import com.nokia.mid.ui.DirectGraphics;
import com.nokia.mid.ui.DirectUtils;

import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.core.Card;
import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.Player;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.engine.guishared.MapPanel;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.mobile.gui.Font;
import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.Icon;
import net.yura.mobile.gui.components.Panel;
import net.yura.mobile.gui.plaf.Style;

/**
 * <p> Picture Panel </p>
 * @author Yura Mamyrin
 */

public class PicturePanel extends Panel implements MapPanel {

        public final static int NO_COUNTRY = 255;

        public final static int PP_X = 677;
        public final static int PP_Y = 425;

        public final static int VIEW_CONTINENTS       = 0;
        public final static int VIEW_OWNERSHIP        = 1;
        public final static int VIEW_BORDER_THREAT    = 2;
        public final static int VIEW_CARD_OWNERSHIP   = 3;
        public final static int VIEW_TROOP_STRENGTH   = 4;
        public final static int VIEW_CONNECTED_EMPIRE = 5;

        private countryImage[] CountryImages;
        private Risk myrisk;
        private Image original;
        private Image img;
        private Image tempimg;
        private byte[][] map;
        private int c1,c2,cc;

        private Font font;
        private String strCountry;

        // TODO: do not have this class, need to think of something else
        private ColorMatrix HighLight;

        private double scale=1;

        /**
         * Creates an Picture Panel
         */
        public PicturePanel(Risk r) {

                myrisk=r;

                this.strCountry = TranslationBundle.getBundle().getString( "picturepanel.country");

                img = null;
                map = null;

                // YURA YURA YURA MAYBE CHANGE 1.0F SO THAT FLAT COLORS HIGHLIGHT TOO
                                         // 0-2  0-255
                float scale = 1.5f;
                float offset = 1.0f;
                HighLight = RescaleOp(scale, offset); // 1.5f, 1.0f, null

                setupSize(PicturePanel.PP_X , PicturePanel.PP_Y);

        }

        int[] oldintx,oldinty;

        public void pointerEvent(int[] type, int[] x, int[] y) {
            if (type.length == 2) { // always true. but just in case


                double point1touchx = scale * x[0];
                double point1touchy = scale * y[0];

                //int xd = x[1]-x[0];
                //int yd = y[1]-y[0];
                //double distance = Math.sqrt(xd*xd + yd*yd);



            }
        }

        private void setupSize(int x,int y) {

            if (map==null || map.length!=x || map[0].length!=y) {

                //System.out.println("MAKING NEW SIZE!!!!");

                //Dimension size = new Dimension(x,y);

                setPreferredSize(x,y);
                //setMinimumSize(size);
                //setMaximumSize(size);

                img = Image.createImage(x, y);
                tempimg = Image.createImage(x, y);

                map = new byte[x][y];
            }

        }

        /**
         * Adds the images related to the game to the picture panel
         */
        public void load() throws IOException {

                RiskGame game = myrisk.getGame();

                // clean up before we load new images
                original = null;
                CountryImages = null;

                Image m = Image.createImage(RiskUtil.openMapStream(game.getImageMap()) );
                Image O = Image.createImage(RiskUtil.openMapStream(game.getImagePic()) );

                memoryLoad(m,O);

        }

        public void memoryLoad(Image m, Image O) {

                RiskGame game = myrisk.getGame();

                original = O;

                cc=NO_COUNTRY;

                c1=NO_COUNTRY;
                c2=NO_COUNTRY;


                setupSize(m.getWidth(),m.getHeight());


                //System.out.print("loading: "+(game.getImagePic()).getAbsolutePath()+" "+(game.getImageMap()).getAbsolutePath() +" "+((Vector)game.getCountries()).size()+"\n");

                int noc = game.getCountries().length;



                { Graphics zg = img.getGraphics(); zg.drawImage(original, 0, 0, 0); }

                //int[] pix = new int[ m.getWidth() ];

                CountryImages = new countryImage[noc];

                for (int c=0; c < noc; c++) {
                        CountryImages[c] = new countryImage();
                }

                countryImage cci;

                int[] pixels = new int[m.getWidth()];

                // create a very big 2d array with all the data from the image map
                for(int y=0; y < m.getHeight(); y++) {

                        // load line by line to not use up too much mem
                        m.getRGB(pixels,0,m.getWidth(),0,y,m.getWidth(),1);

                        for(int x=0; x < m.getWidth(); x++) {

                                int num = pixels[ x ] & 0xff; // (m.getRGB(x,y))&0xff;

                                // if ( num > noc && num !=NO_COUNTRY ) System.out.print("map error: "+x+" "+y+"\n"); // testing map

                                map[x][y]= (byte) (num - 128); // as byte is signed we have to use this

                                if ( num != NO_COUNTRY ) {

                                        cci = CountryImages[num-1];

                                        if (x < cci.getX1() ) { cci.setX1(x); }
                                        if (x > cci.getX2() ) { cci.setX2(x); }

                                        if (y < cci.getY1() ) { cci.setY1(y); }
                                        if (y > cci.getY2() ) { cci.setY2(y); }
                                }

                        }
                }

                //ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
                //ColorConvertOp Gray = new ColorConvertOp( cs , null);

                Icon original1 = new Icon(original);

                // create the bufferd image for each country
                for (int c=0; c < CountryImages.length ; c++) {

                        cci = CountryImages[c];

                        int x1=cci.getX1();
//                      int x2=cci.getX2();
                        int y1=cci.getY1();
//                      int y2=cci.getY2();
                        int w=cci.getWidth();
                        int h=cci.getHeight();

                        // System.out.print( "Country: "+ (c+1) +" X1: "+ x1 +" Y1: "+y1 +" Width: "+ w +" Height: "+ h +"\n");

                        Icon source = original1.getSubimage(x1, y1, w, h);

                        ColorMatrix cm = new ColorMatrix();
                        cm.setSaturation(0);

                        Image gray = Image.createImage(w, h);

                        Image.filter(source.getImage(),gray,cm);
                        //Gray.filter(source , gray);
                        //{ Graphics zg = gray.getGraphics(); zg.drawImage(source, 0, 0, 0); }

                        cci.setSourceImage( source );
                        cci.setGrayImage(gray);

                        cci.setNormalImage( Image.createImage(w, h) );
                        cci.setHighLightImage( Image.createImage(w, h) );

                        cci.setTemp1( Image.createImage(w, h) );
                        cci.setTemp2( Image.createImage(w, h) );
                }



        }

        /**
         * Paints the components
         * @param g a Graphics object.
         */
        public void paintComponent(Graphics2D g) {

            super.paintComponent(g);

            try {

                if (img != null) {

                        //System.out.print("#################################################### Repainted\n");

                        //super.paintComponent(g);

                        Graphics g2 = g.getGraphics();

                        double s = getScale();
                        int x = getDrawImageX(s);
                        int y = getDrawImageY(s);

                        //System.out.println("scale: "+s);

                        g.translate(x,y);
                        g2.scale(s,s);

                        //g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                        g.drawImage(img,0,0);

                        if (c1 != NO_COUNTRY) {
                                g.drawImage( CountryImages[c1-1].getHighLightImage() ,CountryImages[c1-1].getX1() ,CountryImages[c1-1].getY1());
                        }

                        if (c2 != NO_COUNTRY) {
                                g.drawImage(CountryImages[c2-1].getHighLightImage() ,CountryImages[c2-1].getX1() ,CountryImages[c2-1].getY1());
                        }

                        if (cc != NO_COUNTRY) {
                                g.drawImage( CountryImages[cc-1].getHighLightImage() ,CountryImages[cc-1].getX1() ,CountryImages[cc-1].getY1());
                        }

                        drawArmies(g);

                        if (cc != NO_COUNTRY) {

                                // TODO, dont just get the current font, set my own font

                                String text = this.strCountry + " "+ myrisk.getCountryName( cc );
                                int w = font.getWidth(text);
                                int h = font.getHeight();

                                g.setColor( new Color(255,255,255, 150).getRGB() );
                                g.fillRect( 5 , 5, w+3, h+1 );

                                g.setColor( Color.black.getRGB() );
                                g.drawString(text, 6, 15);
                        }

                        g2.scale(1/s,1/s);
                        g.translate(-x,-y);
                }

            }
            catch(Exception e) { // an excpetion here really does not matter
                e.printStackTrace();
            }

        }

        public void updateUI() {
            super.updateUI();
            font = theme.getFont(Style.ALL);
        }

        private int getDrawImageX(double ratio) {

                return (int) (getWidth()-(map.length*ratio) )/2;

        }

        private int getDrawImageY(double ratio) {


                return (int) (getHeight()-(map[0].length*ratio) )/2;

        }

        private double getScale() {

                //return Math.min(getHeight()/(double)map[0].length,getWidth()/(double)map.length);
                return scale;
        }

        /**
         * Paints the army components
         * @param g2 a 2D Graphics object.
         */
        public void drawArmies(Graphics2D g2) {

                DirectGraphics g = DirectUtils.getDirectGraphics(g2.getGraphics());

                RiskGame game = myrisk.getGame();

                Country[] v = game.getCountries();
                Country t;

                int r=10;

                if (game.getState()==4 || game.getState()==5 || game.getState()==10) {

                        int a=game.getAttacker().getColor();
                        int b=game.getDefender().getColor();

                        g2.drawImage(CountryImages[a-1].getHighLightImage() ,CountryImages[a-1].getX1() ,CountryImages[a-1].getY1() );
                        g2.drawImage(CountryImages[b-1].getHighLightImage() ,CountryImages[b-1].getX1() ,CountryImages[b-1].getY1() );

                        Color ac = new Color( game.getAttacker().getOwner().getColor() );
                        int argb = new Color(ac.getRed(),ac.getGreen(), ac.getBlue(), 150).getRGB();
                        //g2.setStroke(new BasicStroke(3));

                        if ( Math.abs( game.getAttacker().getX() - game.getDefender().getX() ) > (map.length / 2) ) {

                                if ( ((Country)game.getAttacker()).getX() > (map.length / 2) ) { // ie the attacker is on the right

                                    Polygon pol1 = makeArrow( game.getAttacker().getX(), ((Country)game.getAttacker()).getY(), ((Country)game.getDefender()).getX()+map.length, ((Country)game.getDefender()).getY(), r );
                                    Polygon pol2 = makeArrow( game.getAttacker().getX()-map.length, ((Country)game.getAttacker()).getY(), ((Country)game.getDefender()).getX(), ((Country)game.getDefender()).getY(), r );

                                    g.fillPolygon(pol1.xpoints,0, pol1.ypoints, 0, pol1.npoints, argb );
                                    g.fillPolygon(pol2.xpoints,0, pol2.ypoints, 0, pol2.npoints, argb );

                                }
                                else { // the attacker is on the left

                                    Polygon pol1 = makeArrow( game.getAttacker().getX(), ((Country)game.getAttacker()).getY(), ((Country)game.getDefender()).getX()-map.length, ((Country)game.getDefender()).getY(), r );
                                    Polygon pol2 = makeArrow( game.getAttacker().getX()+map.length, ((Country)game.getAttacker()).getY(), ((Country)game.getDefender()).getX(), ((Country)game.getDefender()).getY(), r );

                                    g.fillPolygon(pol1.xpoints,0, pol1.ypoints, 0, pol1.npoints, argb );
                                    g.fillPolygon(pol2.xpoints,0, pol2.ypoints, 0, pol2.npoints, argb );
                                }

                        }
                        else {

                            Polygon pol1 = makeArrow( ((Country)game.getAttacker()).getX(), ((Country)game.getAttacker()).getY(), ((Country)game.getDefender()).getX(), ((Country)game.getDefender()).getY(), r );

                            g.fillPolygon(pol1.xpoints,0, pol1.ypoints, 0, pol1.npoints, argb );

                        }

                        //g2.setStroke(new BasicStroke(1));

                }

                for (int c=0; c< v.length ; c++) {

                        t = v[c];

                        if ( ((Player)t.getOwner()) != null ) {

                                g.setARGBColor( new Color( ((Player)t.getOwner()).getColor() ).getRGB() );

                                Ellipse2D ellipse = new Ellipse2D.Double();
                                ellipse.setFrame( t.getX()-r , t.getY()-r , (r*2), (r*2) );
                                g2.fillArc( (int)ellipse.getX(), (int)ellipse.getY(), (int)ellipse.getWidth(), (int)ellipse.getHeight(), 0, 360);

                                //g.fillOval( t.getX()-r , t.getY()-r, (r*2), (r*2) );

                                g.setARGBColor( new Color( RiskUtil.getTextColorFor( ((Player)t.getOwner()).getColor() ) ).getRGB() );

                                g2.setFont( font );
                                int h = t.getY() -(font.getHeight()/2 -1);
                                int noa=t.getArmies();
                                if (noa < 10) {
                                        g2.drawString( String.valueOf( noa ) , t.getX()-3, h );
                                }
                                else if (noa < 100) {
                                        g2.drawString( String.valueOf( noa ) , t.getX()-6, h );
                                }
                                else {
                                        g2.drawString( String.valueOf( noa ) , t.getX()-9, h );
                                }
                        }

                }

                if (game.getGameMode() == 2 && game.getSetup() && game.getState() !=9 ) {

                        //g2.setStroke(new BasicStroke(2));
                        Vector players = game.getPlayers();

                        for (int c=0; c< players.size() ; c++) {

                                if ( ((Player)players.elementAt(c)).getCapital() !=null ) {
                                        Country capital = ((Country)((Player)players.elementAt(c)).getCapital());

                                        g.setARGBColor( new Color( RiskUtil.getTextColorFor( ((Player)capital.getOwner()).getColor() ) ).getRGB() );

                                        Ellipse2D ellipse = new Ellipse2D.Double();
                                        ellipse.setFrame( capital.getX()-10 , capital.getY()-10 , 19, 19);
                                        g2.drawArc( (int)ellipse.getX(), (int)ellipse.getY(), (int)ellipse.getWidth(), (int)ellipse.getHeight(), 0, 360);

                                        g.setARGBColor( new Color( ((Player)players.elementAt(c)).getColor() ).getRGB() );

                                        Ellipse2D ellipse2 = new Ellipse2D.Double();
                                        ellipse2.setFrame( capital.getX()-12 , capital.getY()-12 , 23, 23);
                                        g2.drawArc( (int)ellipse2.getX(), (int)ellipse2.getY(), (int)ellipse2.getWidth(), (int)ellipse2.getHeight(), 0, 360);

                                }

                        }
                        //g2.setStroke(new BasicStroke(1));
                }

        }

        /**
         * Paints the arrows for the game, ie - when attacking
         * @param x1i x point of the attacker's co-ordinates.
         * @param y1i y point of the attacker's co-ordinates.
         * @param x2i x point of the defender's co-ordinates.
         * @param y2i y point of the defender's co-ordinates.
         * @param ri the radius of the circle
         */
        public Polygon makeArrow(int x1i, int y1i, int x2i, int y2i, int ri) {

                Polygon arrow;

                double x1 = x1i;
                double y1 = y1i;
                double x2 = x2i;
                double y2 = y2i;

                double xd = x2-x1;
                double yd = y1-y2;

                double r = ri;
                double l = Math.sqrt( Math.pow(xd, 2d) + Math.pow(yd, 2d) );

                double a = Math.acos( (r/l) );
                double b = Math.atan( (yd/xd) );
                double c = Math.atan( (xd/yd) );

                double x3 = r * Math.cos( a - b );
                double y3 = r * Math.sin( a - b );

                double x4 = r * Math.sin( a - c );
                double y4 = r * Math.cos( a - c );

                //System.out.print("x3="+x3+" y3="+y3+" x4="+x4+" y4="+y4+"\n");

/*

              3
             /|\
    2--       |       --3
    |\        |        /|
       \      |      /
         \    |    /
           \  -  /
  /         / | \         \
2----------|--+--|----------3
  \         \ | /         /
           /  -  \
         /    |    \
       /      |      \
    |/        |        \|
    4--       |       --1
             \|/
              1

*/

                if (x2 >= x1 && y2 <= y1) {

                        //System.out.print("3\n");

                        int xCoords[] = { (int)x1, (int)Math.round(x1+x3) , (int)x2 , (int)Math.round(x1-x4) };
                        int yCoords[] = { (int)y1, (int)Math.round(y1+y3) , (int)y2 , (int)Math.round(y1-y4) };
                        arrow = new Polygon(xCoords, yCoords, xCoords.length);

                }
                else if (x2 >= x1 && y2 >= y1) {

                        //System.out.print("1\n");

                        int xCoords[] = { (int)x1, (int)Math.round(x1+x3) , (int)x2 , (int)Math.round(x1+x4) };
                        int yCoords[] = { (int)y1, (int)Math.round(y1+y3) , (int)y2 , (int)Math.round(y1+y4) };
                        arrow = new Polygon(xCoords, yCoords, xCoords.length);

                }
                else if (x2 <= x1 && y2 <= y1) {

                        //System.out.print("2\n");

                        int xCoords[] = { (int)x1, (int)Math.round(x1-x3) , (int)x2 , (int)Math.round(x1-x4) };
                        int yCoords[] = { (int)y1, (int)Math.round(y1-y3) , (int)y2 , (int)Math.round(y1-y4) };
                        arrow = new Polygon(xCoords, yCoords, xCoords.length);

                }

                else  { // if (x2 < x1 && y2 > y1)

                        //System.out.print("4\n");

                        int xCoords[] = { (int)x1, (int)Math.round(x1-x3) , (int)x2 , (int)Math.round(x1+x4) };
                        int yCoords[] = { (int)y1, (int)Math.round(y1-y3) , (int)y2 , (int)Math.round(y1+y4) };
                        arrow = new Polygon(xCoords, yCoords, xCoords.length);

                }


                return arrow;

        }

        /**
         * Repaints the countries for each of the different views
         * @param view The name of each of the map views.
         */
        public synchronized void repaintCountries(int view) { // synchronized

                RiskGame game = myrisk.getGame();

                { Graphics zg = tempimg.getGraphics(); zg.drawImage(original ,0 ,0, 0 ); }

                Vector b=null;

                if (view == VIEW_CONNECTED_EMPIRE) {

                        Vector players = game.getPlayers();

                        b = new Vector();

                        for (int c=0; c<players.size(); c++) {
                                b.addAll( game.getConnectedEmpire( (Player)players.elementAt(c) ) );
                        }
                }

                for (int c=0; c < CountryImages.length ; c++) {

                    Color val=null;

                    if (view == VIEW_CONTINENTS) {

                                val = new Color(0,true);

                    }
                    else if (view == VIEW_OWNERSHIP) {


                                if ( ((Country)game.getCountryInt( c+1 )).getOwner() != null ) {
                                        val = new Color( ((Player)((Country)game.getCountryInt( c+1 )).getOwner()).getColor() );
                                }
                                else {
                                        val = Color.GRAY;
                                }

                                val = new Color(val.getRed(), val.getGreen(), val.getBlue(), 100);

                    }
                    else if (view == VIEW_BORDER_THREAT) {

                                Player player = ((Country)game.getCountryInt( c+1 )).getOwner();

                                if (player != game.getCurrentPlayer() ) {
                                        val = Color.gray;
                                }
                                else {
                                        Vector neighbours = ((Country)game.getCountryInt( c+1 )).getNeighbours();
                                        int threat=0; // max of about 6

                                        for (int j = 0; j < neighbours.size() ; j++) {

                                                if ( ((Country)neighbours.elementAt(j)).getOwner() != player ) {
                                                        threat++;
                                                }

                                        }

                                        threat=threat*40;

                                        if (threat > 255) { threat=255; }

                                        val = (new Color( threat, 0, 0));
                                }

                                val = new Color(val.getRed(), val.getGreen(), val.getBlue(), 200);


                    }
                    else if (view == VIEW_CARD_OWNERSHIP) {

                                if (  game.getCurrentPlayer()==null  || ((Country)game.getCountryInt(c+1)).getOwner() != (Player)game.getCurrentPlayer()) {
                                        val = Color.lightGray;
                                }
                                else {
                                        Vector cards = ((Player)game.getCurrentPlayer()).getCards();

                                        for (int j = 0; j < cards.size() ; j++) {

                                                if ( ((Card)cards.elementAt(j)).getCountry() == (Country)game.getCountryInt(c+1) ) {
                                                        val = Color.blue;
                                                }

                                        }

                                        if (val == null) val = Color.darkGray;
                                }

                                val = new Color(val.getRed(), val.getGreen(), val.getBlue(), 100);

                    }
                    else if (view == VIEW_TROOP_STRENGTH) {

                                if (((Country)game.getCountryInt(c+1)).getOwner() != (Player)game.getCurrentPlayer()) {
                                        val = Color.gray;
                                }
                                else {
                                        int armies = ((Country)game.getCountryInt(c+1)).getArmies();

                                        armies=armies*25;

                                        if (armies > 255) { armies=255; }

                                        val = (new Color( 0 , armies, 0));
                                }

                                val = new Color(val.getRed(), val.getGreen(), val.getBlue(), 200);

                    }
                    else if (view == VIEW_CONNECTED_EMPIRE) {

                                Country thecountry = game.getCountryInt( c+1 );

                                if ( thecountry.getOwner() == null ) {

                                        val = Color.LIGHT_GRAY;

                                }
                                else if ( b.contains( thecountry ) ) {

                                        val = new Color( ((Player)thecountry.getOwner()).getColor() );

                                }
                                else {
                                        val = Color.DARK_GRAY;
                                }

                                val = new Color(val.getRed(), val.getGreen(), val.getBlue(), 100);

/*

                                Country thecountry = ((Country)game.getCountryInt(c+1));

                                if ( b != null && b.contains( thecountry ) ) {
                                        val = ((Player)game.getCurrentPlayer()).getColor();
                                }
                                else if (((Country)game.getCountryInt(c+1)).getOwner() == (Player)game.getCurrentPlayer()) {
                                        val = Color.darkGray;
                                }
                                else {
                                        val = Color.lightGray;
                                }

                                val = new Color(val.getRed(), val.getGreen(), val.getBlue(), 100);
*/
                    }

                    countryImage ci = CountryImages[c];

                    int x1=ci.getX1();
                    int y1=ci.getY1();
                    Image normalB = ci.getNormalImage(); // new BufferedImage( w ,h, java.awt.image.BufferedImage.TYPE_INT_ARGB );

                    if ( ci.checkChange(val) ) {

                        int y2=ci.getY2();
                        int w=ci.getWidth();
                        int h=ci.getHeight();

                        Image normalA = ci.getTemp1(); // new BufferedImage( w ,h, java.awt.image.BufferedImage.TYPE_INT_RGB );
                        Image highlightA = ci.getTemp2(); // new BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_RGB );
                        Image highlightB = ci.getHighLightImage(); // new BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB );

                        Graphics tempg = normalA.getGraphics();

                        if (view == VIEW_CONTINENTS) {

                                //tempg.drawImage( ci.getSourceImage() ,0,0,0 );
                                ci.getSourceImage().paintIcon(this, new Graphics2D(tempg), 0, 0);

                        }
                        else {

                                tempg.drawImage( ci.getGrayImage(), 0, 0, 0);
                                DirectUtils.getDirectGraphics(tempg).setARGBColor( val.getRGB() );
                                tempg.fillRect(0,0,w,h);

                        }

                        Image.filter(normalA, highlightA, HighLight);
                        //HighLight.filter( normalA , highlightA );

                        if (view != VIEW_CONTINENTS) { Graphics zg = normalB.getGraphics(); zg.drawImage(normalA,0,0,0); }

                        { Graphics zg = highlightB.getGraphics(); zg.drawImage(highlightA,0,0,0); }

                        for(int y=y1; y <= y2; y++) {
                                for(int x=0; x < w; x++) {
                                        if (map[x+x1][y] + 128 != (c+1) ) {
                                                normalB.setRGB( x, (y-y1), 0); // clear the un-needed area!
                                                highlightB.setRGB( x, (y-y1), 0); // clear the un-needed area!
                                        }
                                }
                        }

                    }

                    if (view != VIEW_CONTINENTS) { Graphics zg = tempimg.getGraphics(); zg.drawImage(normalB ,x1 ,y1 ,0); }

                }

                Image newback = img;

                img = tempimg;

                tempimg = newback;
        }

        /**
         * Gets the unique identifier of a country from its position on the map
         * @param x x co-ordinate on the map
         * @param y y co-ordinate on the map
         */
        public int getCountryNumber(int x, int y) {

                double s = getScale();

                x = x - getDrawImageX(s);
                y = y - getDrawImageY(s);

                x = (int)(x / s);
                y = (int)(y / s);

                if (x<0 || y<0 || x>=map.length || y>=map[0].length) {
                        return NO_COUNTRY;
                }

                return map[x][y] + 128;
        }

        /**
         * Sets which country to hilight
         * @param a number of the country
         */
        public void setHighLight(int a) {
                cc=a;
        }

        /**
         * Returns which country is hilighted
         * @return int Returns which country is hilighted
         */
        public int getHighLight() {
                return cc;
        }

        /**
         * Sets the attacking country
         * @param a number of the country
         */
        public void setC1(int a) {
                c1=a;
        }

        /**
         * Sets the defensive country
         * @param a number of the country
         */
        public void setC2(int a) {
                c2=a;
        }

        /**
         * Returns the attacking country
         * @return int number of the country
         */
        public int getC1() {
                return c1;
        }

        public int getC2() {
                return c2;
        }

        // Subclass countryImage - holds all the image information

        class countryImage {

                private int x1;
                private int y1;
                private int x2;
                private int y2;
                private Icon SourceImage;
                private Image GrayImage;
                private Image normalImage;
                private Image HighLightImage;

                private Image temp1;
                private Image temp2;

                private Color color;

                public countryImage() {
                        x1=map.length;
                        y1=map[0].length;
                        x2=0;
                        y2=0;
                        SourceImage=null;
                        GrayImage=null;
                        HighLightImage=null;
                        normalImage=null;

                }

                public boolean checkChange(Color b) {

                        if (!b.equals(color) ) {

                                color = b;
                                return true;
                        }

                        return false;

                }

                public void setTemp1(Image a) {
                        temp1=a;
                }
                public void setTemp2(Image a) {
                        temp2=a;
                }
                public Image getTemp1() {
                        return temp1;
                }
                public Image getTemp2() {
                        return temp2;
                }

                /**
                 * Sets the source image
                 * @param a Image buffered
                 */
                public void setSourceImage(Icon a) {
                        SourceImage=a;
                }

                /**
                 * Sets the gray image
                 * @param a Image buffered
                 */
                public void setGrayImage(Image a) {
                        GrayImage=a;
                }

                /**
                 * Sets the hilighted image
                 * @param a Image buffered
                 */
                public void setHighLightImage(Image a) {
                        HighLightImage=a;
                }

                public void setNormalImage(Image a) {
                        normalImage=a;
                }

                /**
                 * Sets the top left corner of a country
                 * @param a coordinate
                 */
                public void setX1(int a) {
                        x1=a;
                }

                /**
                 * Sets the bottom left corner of a country
                 * @param a coordinate
                 */
                public void setY1(int a) {
                        y1=a;
                }

                /**
                 * Sets the top right corner of a country
                 * @param a coordinate
                 */
                public void setX2(int a) {
                        x2=a;
                }

                /**
                 * Sets the bottom right corner of a country
                 * @param a coordinate
                 */
                public void setY2(int a) {
                        y2=a;
                }

                /**
                 * Gets the source image
                 * @return BufferedImage Returns the source image
                 */
                public Icon getSourceImage() {
                        return SourceImage;
                }

                /**
                 * Gets the gray image
                 * @return BufferedImage Returns the gray image
                 */
                public Image getGrayImage() {
                        return GrayImage;
                }

                /**
                 * Gets the hilighted image
                 * @return BufferedImage Returns the hilighted image
                 */
                public Image getHighLightImage() {
                        return HighLightImage;
                }

                public Image getNormalImage() {
                        return normalImage;
                }

                /**
                 * Gets the top left corner of a country
                 * @return int coordinate
                 */
                public int getX1() {
                        return x1;
                }

                /**
                 * Gets the bottom left corner of a country
                 * @return int coordinate
                 */
                public int getY1() {
                        return y1;
                }

                /**
                 * Gets the top right corner of a country
                 * @return int coordinate
                 */
                public int getX2() {
                        return x2;
                }

                /**
                 * Gets the bottom right corner of a country
                 * @return int coordinate
                 */
                public int getY2() {
                        return y2;
                }

                /**
                 * Gets the width of a country
                 * @return int width of a country
                 */
                public int getWidth() {
                        return (x2-x1+1);
                }

                /**
                 * Gets the height of a country
                 * @return int height of a country
                 */
                public int getHeight() {
                        return (y2-y1+1);
                }


        }

        private ColorMatrix RescaleOp(float a,float b) {
            ColorMatrix cm = new ColorMatrix();
            cm.set(new float[] {
                    a,0,0,0,b,
                    0,a,0,0,b,
                    0,0,a,0,b,
                    0,0,0,1,0,
                });
            return cm;
        }

        /**
         * Gets the image of a country
         * @param num the index of a country
         * @param incolor whether the image of a country is in colour or greyscale
         * @return BufferedImage Image buffered of a country
         */
        public Image getCountryImage(int num, boolean incolor) {

                int i = num-1;

                countryImage ci = CountryImages[i];

                int x1=ci.getX1();
//              int x2=ci.getX2();
                int y1=ci.getY1();
                int y2=ci.getY2();
                int w=ci.getWidth();
                int h=ci.getHeight();

                Image pictureA = Image.createImage(w, h);

                ColorMatrix HighLight = RescaleOp( 0.5f, -1.0f);
                //HighLight.filter( ci.getGrayImage() , pictureA );
                Image.filter( ci.getGrayImage() ,pictureA, HighLight );

                Image pictureB = Image.createImage(w, h);

                Graphics g = pictureB.getGraphics();

                g.drawImage( pictureA ,0 ,0 ,0);

                if (incolor) {

                        Color ownerColor = new Color( ((Player) ((Country) ((RiskGame)myrisk.getGame()) .getCountryInt( num )) .getOwner()).getColor() );

                        DirectUtils.getDirectGraphics(g).setARGBColor( new Color(ownerColor.getRed(), ownerColor.getGreen(), ownerColor.getBlue(), 100).getRGB() );
                        g.fillRect(0,0,w,h);

                }

                for(int y=y1; y <= y2; y++) {
                        for(int x=0; x <= w-1; x++) {
                                if (map[x+x1][y] + 128 != (i+1) ) {
                                        pictureB.setRGB( x, (y-y1), 0); // clear the un-needed area!
                                }
                        }
                }

                return pictureB;

        }
/*
        public static Image getImage(RiskGame game) throws Exception {

                // attempt to get the preview as its smaller
                String imagename = game.getPreviewPic();

                if (imagename==null) {

                        return Toolkit.getDefaultToolkit().getImage( new URL(RiskUIUtil.mapsdir,game.getImagePic() ) ).getScaledInstance(203,127, java.awt.Image.SCALE_SMOOTH );

                }
                else {

                        Image s = Image.createImage(RiskUtil.openMapStream("preview/"+imagename) );
                        String name = game.getMapName();

                        Image tmpimg = Image.createImage(203,127);
                        Graphics g = tmpimg.getGraphics();


                        //g.drawImage(s.getScaledInstance(203,127, java.awt.Image.SCALE_SMOOTH ),0,0,null );

                        g.drawImage(s,0,0,203,127,0,0,s.getWidth(),s.getHeight(),null);

                        //AffineTransform at = AffineTransform.getScaleInstance((double)203/s.getWidth(),(double)127/s.getHeight());
                        //g.drawRenderedImage(s,at);


                        if (name!=null) {

                                g.setARGBColor( new Color(255,255,255, 150).getRGB() );
                                g.fillRect(0,0,203,20);
                                g.setARGBColor( Color.BLACK.getRGB() );
                                g.drawString(name,5,15,0);

                        }

                        return tmpimg;
                }
        }
*/
        public Image getImage() {
                return img;
        }

}

