package net.yura.domination.mobile;

import java.io.IOException;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
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
import net.yura.mobile.gui.DesktopPane;
import net.yura.mobile.gui.Font;
import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.KeyEvent;
import net.yura.mobile.gui.border.Border;
import net.yura.mobile.gui.components.ImageView;
import net.yura.mobile.gui.plaf.Style;

/**
 * <p> Picture Panel </p>
 * @author Yura Mamyrin
 */

public class PicturePanel extends ImageView implements MapPanel {

        public final static int NO_COUNTRY = 255;

        public final static int PP_X = 677;
        public final static int PP_Y = 425;

        public final static int VIEW_CONTINENTS       = 0;
        public final static int VIEW_OWNERSHIP        = 1;
        public final static int VIEW_BORDER_THREAT    = 2;
        public final static int VIEW_CARD_OWNERSHIP   = 3;
        public final static int VIEW_TROOP_STRENGTH   = 4;
        public final static int VIEW_CONNECTED_EMPIRE = 5;

        private Risk myrisk;

        private Image img;
        private Image tempimg;
        private byte[][] map;
        private countryImage[] CountryImages;
        private int c1,c2,cc;

        private Font font;
        private String strCountry;

        private static final ColorMatrix HighLight;
        public static final ColorMatrix gray;
        static {

                // YURA YURA YURA MAYBE CHANGE 1.0F SO THAT FLAT COLORS HIGHLIGHT TOO
                                // 0-2  0-255
                float scale = 1.5f;
                float offset = 1.0f;
                HighLight = RescaleOp(scale, offset); // 1.5f, 1.0f, null
            
                gray = new ColorMatrix();
                gray.setSaturation(0);
        }

        /**
         * Creates an Picture Panel
         */
        public PicturePanel(Risk r) {

                getDesktopPane().IPHONE_SCROLL = true;

                myrisk=r;

                this.strCountry = TranslationBundle.getBundle().getString( "picturepanel.country");

                img = null;
                map = null;
                
                setupSize(PicturePanel.PP_X , PicturePanel.PP_Y);

                setName("PicturePanel");
        }

        int[] oldintx,oldinty;

        MouseListener ml;
        int x=-1000,y=-1000;
        public void processMouseEvent(int type, int x, int y, KeyEvent keys) {
            if (type == DesktopPane.PRESSED) {
                this.x=x;
                this.y=y;
            }
            else {
                DesktopPane dp = DesktopPane.getDesktopPane();
                if (DesktopPane.isAccurate(this.x, x, dp.inaccuracy) && DesktopPane.isAccurate(this.y, y, dp.inaccuracy)) {
                    if (type == DesktopPane.RELEASED && ml!=null) {
                        ml.click(x,y);
                    }
                }
                else {
                    x=-1000;
                    y=-1000;
                }
            }
        }

        public void addMouseListener(MouseListener ml) {
            this.ml = ml;
        }

        private void setupSize(int x,int y) {

            if (map==null || map.length!=x || map[0].length!=y) {

                //System.out.println("MAKING NEW SIZE!!!!");

                //Dimension size = new Dimension(x,y);

                setPreferredSize(x,y);
                //setMinimumSize(size);
                //setMaximumSize(size);

                // clear out old values
                img = null;
                tempimg = null;
                map = null;
                
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
                //original = null;
                CountryImages = null;

                //System.out.print("loading: "+(game.getImagePic()).getAbsolutePath()+" "+(game.getImageMap()).getAbsolutePath() +" "+((Vector)game.getCountries()).size()+"\n");
                
                memoryLoad(
                        Image.createImage(RiskUtil.openMapStream(game.getImageMap()) ),
                        Image.createImage(RiskUtil.openMapStream(game.getImagePic()) )
                        );

        }

        public int getMapWidth() {
            return map.length;
        }
        public int getMapHeight() {
            return map[0].length;
        }
        
        public void memoryLoad(Image m, Image original) {

                // ImageView vars
                imgW = original.getWidth();
                imgH = original.getHeight();



                RiskGame game = myrisk.getGame();
                cc=NO_COUNTRY;
                c1=NO_COUNTRY;
                c2=NO_COUNTRY;
                int noc = game.getCountries().length;



                setupSize(m.getWidth(),m.getHeight()); // creates a 2D byte array and double paint buffer
                { Graphics zg = img.getGraphics(); zg.drawImage(original, 0, 0, 0); }



                CountryImages = new countryImage[noc];
                for (int c=0; c < noc; c++) {
                        CountryImages[c] = new countryImage();
                }

 

                int[] pixels = new int[m.getWidth()];

                countryImage cci;
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

                pixels = null;
                m=null;

                // create the bufferd image for each country
                for (int c=0; c < CountryImages.length ; c++) {

                        cci = CountryImages[c];

                        int x1=cci.getX1();
//                      int x2=cci.getX2();
                        int y1=cci.getY1();
                        int y2=cci.getY2();
                        int w=cci.getWidth();
                        int h=cci.getHeight();

                        Image cimg = Image.createImage(w, h);
                        Graphics g = cimg.getGraphics();
                        g.drawRegion(original, x1, y1, w, h, 0, 0, 0, 0);
                        cci.setSourceImage( cimg );
                        
                        for(int y=y1; y <= y2; y++) {
                                for(int x=0; x < w; x++) {
                                        if (map[x+x1][y] + 128 != (c+1) ) {
                                                cimg.setRGB( x, (y-y1), 0); // clear the un-needed area!
                                        }
                                }
                        }
                        
                }



        }

        protected void paintBorder(Graphics2D g) {

                Border b = getBorder();
                if (b != null) {
                    
                    double s = getScale();
                    int x = getImgX(s);
                    int y = getImgY(s);

                    int w = (int) (img.getWidth() * s);
                    int h = (int) (img.getHeight() * s);
                    
                    g.translate(x,y);
                    b.paintBorder(this, g,w,h);
                    g.translate(-x,-y);
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
                        int x = getImgX(s);
                        int y = getImgY(s);

                        //System.out.println("scale: "+s);

                        g.translate(x,y);
                        g2.scale(s,s);

                        //g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                        g.drawImage(img,0,0);

                        if (c1 != NO_COUNTRY) {
                                drawHighLightImage(g, CountryImages[c1-1]);
                        }

                        if (c2 != NO_COUNTRY) {
                                drawHighLightImage(g,CountryImages[c2-1]);
                        }

                        if (cc != NO_COUNTRY) {
                                drawHighLightImage(g,CountryImages[cc-1]);
                        }

                        drawArmies(g);

                        if (cc != NO_COUNTRY) {

                                // TODO, dont just get the current font, set my own font

                                String text = this.strCountry + " "+ myrisk.getCountryName( cc );
                                int w = font.getWidth(text);
                                int h = font.getHeight();

                                g.setColor( 0x96FFFFFF );
                                g.fillRect( 5 , 5, w+3, h+1 );

                                g.setColor( 0xFF000000 );
                                g.drawString(text, 6, 15);
                        }

                        g2.scale(1/s,1/s);
                        g.translate(-x,-y);
                }

            }
            catch(Exception e) { // an excpetion here really does not matter
                RiskUtil.printStackTrace(e);
            }

        }

        public void updateUI() {
            super.updateUI();
            font = theme.getFont(Style.ALL);
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

                        Country attacker = game.getAttacker();
                        Country defender = game.getDefender();
                    
                        int a=attacker.getColor();
                        int b=defender.getColor();

                        drawHighLightImage(g2,CountryImages[a-1]);
                        drawHighLightImage(g2,CountryImages[b-1]);

                        int ac = attacker.getOwner().getColor();

                        int argb = colorWithAlpha( ac, 150 );
                        //g2.setStroke(new BasicStroke(3));

                        if ( Math.abs( attacker.getX() - defender.getX() ) > (map.length / 2) ) {

                                if ( attacker.getX() > (map.length / 2) ) { // ie the attacker is on the right

                                    Polygon pol1 = makeArrow( attacker.getX(), attacker.getY(), defender.getX()+map.length, defender.getY(), r );
                                    Polygon pol2 = makeArrow( attacker.getX()-map.length, attacker.getY(), defender.getX(), defender.getY(), r );

                                    g.fillPolygon(pol1.xpoints,0, pol1.ypoints, 0, pol1.npoints, argb );
                                    g.fillPolygon(pol2.xpoints,0, pol2.ypoints, 0, pol2.npoints, argb );

                                }
                                else { // the attacker is on the left

                                    Polygon pol1 = makeArrow( attacker.getX(), attacker.getY(), defender.getX()-map.length, defender.getY(), r );
                                    Polygon pol2 = makeArrow( attacker.getX()+map.length, attacker.getY(), defender.getX(), defender.getY(), r );

                                    g.fillPolygon(pol1.xpoints,0, pol1.ypoints, 0, pol1.npoints, argb );
                                    g.fillPolygon(pol2.xpoints,0, pol2.ypoints, 0, pol2.npoints, argb );
                                }

                        }
                        else {

                            Polygon pol1 = makeArrow( attacker.getX(), attacker.getY(), defender.getX(), defender.getY(), r );

                            g.fillPolygon(pol1.xpoints,0, pol1.ypoints, 0, pol1.npoints, argb );

                        }

                        //g2.setStroke(new BasicStroke(1));

                }

                for (int c=0; c< v.length ; c++) {

                        t = v[c];

                        if ( ((Player)t.getOwner()) != null ) {

                                g.setARGBColor( ((Player)t.getOwner()).getColor() );

                                g2.fillArc( t.getX()-r , t.getY()-r , (r*2), (r*2) , 0, 360);

                                //g.fillOval( t.getX()-r , t.getY()-r, (r*2), (r*2) );

                                g.setARGBColor( RiskUtil.getTextColorFor( ((Player)t.getOwner()).getColor() ) );

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

                                        g.setARGBColor( RiskUtil.getTextColorFor( ((Player)capital.getOwner()).getColor() ) );

                                        g2.drawArc( capital.getX()-10 , capital.getY()-10 , 19, 19 , 0, 360);

                                        g.setARGBColor( ((Player)players.elementAt(c)).getColor() );

                                        g2.drawArc( capital.getX()-12 , capital.getY()-12 , 23, 23, 0, 360);

                                }

                        }
                        //g2.setStroke(new BasicStroke(1));
                }

        }

        
        private void drawHighLightImage(Graphics2D g, countryImage countryImage) {
            
            int val = countryImage.color;
            Graphics g2 = g.getGraphics();
            ColorMatrix m;
            
            if (val == 0) {
                m = HighLight;
            }
            else {
                m = getMatrix(val);
                m.preConcat(gray);
                m.postConcat( HighLight );
            }
            
            g2.setColorMarix(m);
            g.drawImage(countryImage.getSourceImage(), countryImage.getX1(), countryImage.getY1());
            g2.setColorMarix(null);
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

                Graphics zg = tempimg.getGraphics();
                zg.drawImage(img ,0 ,0, 0 );

                Vector b=null;

                if (view == VIEW_CONNECTED_EMPIRE) {

                        Vector players = game.getPlayers();

                        b = new Vector();

                        for (int c=0; c<players.size(); c++) {
                                b.addAll( game.getConnectedEmpire( (Player)players.elementAt(c) ) );
                        }
                }

                for (int c=0; c < CountryImages.length ; c++) {

                    int val=0;

                    if (view == VIEW_CONTINENTS) {

                                val = 0x00000000;

                    }
                    else if (view == VIEW_OWNERSHIP) {


                                if ( ((Country)game.getCountryInt( c+1 )).getOwner() != null ) {
                                        val = ((Player)((Country)game.getCountryInt( c+1 )).getOwner()).getColor();
                                }
                                else {
                                        val = GRAY;
                                }

                                val = colorWithAlpha(val, 100);

                    }
                    else if (view == VIEW_BORDER_THREAT) {

                                Player player = ((Country)game.getCountryInt( c+1 )).getOwner();

                                if (player != game.getCurrentPlayer() ) {
                                        val = GRAY;
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

                                        val = newColor( threat, 0, 0);
                                }

                                val = colorWithAlpha(val, 200);


                    }
                    else if (view == VIEW_CARD_OWNERSHIP) {

                                if (  game.getCurrentPlayer()==null  || ((Country)game.getCountryInt(c+1)).getOwner() != (Player)game.getCurrentPlayer()) {
                                        val = LIGHT_GRAY;
                                }
                                else {
                                        Vector cards = ((Player)game.getCurrentPlayer()).getCards();

                                        for (int j = 0; j < cards.size() ; j++) {

                                                if ( ((Card)cards.elementAt(j)).getCountry() == (Country)game.getCountryInt(c+1) ) {
                                                        val = BLUE;
                                                }

                                        }

                                        if (val == 0) val = DARK_GRAY;
                                }

                                val = colorWithAlpha(val, 100);

                    }
                    else if (view == VIEW_TROOP_STRENGTH) {

                                if (((Country)game.getCountryInt(c+1)).getOwner() != (Player)game.getCurrentPlayer()) {
                                        val = GRAY;
                                }
                                else {
                                        int armies = ((Country)game.getCountryInt(c+1)).getArmies();

                                        armies=armies*25;

                                        if (armies > 255) { armies=255; }

                                        val = newColor( 0 , armies, 0);
                                }

                                val = colorWithAlpha(val, 200);

                    }
                    else if (view == VIEW_CONNECTED_EMPIRE) {

                                Country thecountry = game.getCountryInt( c+1 );

                                if ( thecountry.getOwner() == null ) {

                                        val = LIGHT_GRAY;

                                }
                                else if ( b.contains( thecountry ) ) {

                                        val = ((Player)thecountry.getOwner()).getColor();

                                }
                                else {
                                        val = DARK_GRAY;
                                }

                                val = colorWithAlpha(val, 100);

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

                    if (ci.checkChange(val)) {
                        if (view != VIEW_CONTINENTS) {
                            ColorMatrix m = getMatrix(val);
                            m.preConcat(gray);
                            zg.setColorMarix(m);
                        }
                        zg.drawImage(ci.getSourceImage() ,x1 ,y1 ,0);
                    }
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

                x = x - getImgX(s);
                y = y - getImgY(s);

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

        public static ColorMatrix RescaleOp(float a,float b) {
            ColorMatrix cm = new ColorMatrix();
            cm.set(new float[] {
                    a,0,0,0,b,
                    0,a,0,0,b,
                    0,0,a,0,b,
                    0,0,0,1,0,
                });
            return cm;
        }
        
        public static ColorMatrix getMatrix(int color) {
            
            float r = RiskUtil.getRed(color);
            float g = RiskUtil.getGreen(color);
            float b = RiskUtil.getBlue(color);
            
            float alpha = ((float)RiskUtil.getAlpha(color))/255f;
            float alpha2 = 1 - alpha;
            
            ColorMatrix cm = new ColorMatrix();
            cm.set(new float[] {
                    alpha2,0,0,0,r*alpha,
                    0,alpha2,0,0,g*alpha,
                    0,0,alpha2,0,b*alpha,
                    0,0,0,1,0,
                });
            return cm;
        }

        /**
         * Gets the image of a country
         * @param num the color of a country
         * @return BufferedImage Image buffered of a country
         */
        public Image getCountryImage(int num) {
                countryImage ci = CountryImages[num-1];
                return ci.getSourceImage();
        }

        public final static int GRAY      = newColor(128, 128, 128);
        public final static int DARK_GRAY  = newColor(64, 64, 64);
        public final static int LIGHT_GRAY = newColor(192, 192, 192);
        public final static int BLUE  = newColor(0, 0, 255);

        public static int colorWithAlpha(int color, int alpha) {
            return ((alpha & 0xFF) << 24) | (color & 0xFFFFFF);
        }
        private static int newColor(int r,int g,int b) {
            return ((255 & 0xFF) << 24) |
            ((r & 0xFF) << 16) |
            ((g & 0xFF) << 8)  |
            ((b & 0xFF) << 0);
        }
        
        
        // Subclass countryImage - holds all the image information

        class countryImage {

                private int x1;
                private int y1;
                private int x2;
                private int y2;
                private Image SourceImage;

                private int  color;

                public countryImage() {
                        x1=map.length;
                        y1=map[0].length;
                }

                public boolean checkChange(int b) {

                        if (b != color) {

                                color = b;
                                return true;
                        }

                        return false;

                }

                /**
                 * Sets the source image
                 * @param a Image buffered
                 */
                public void setSourceImage(Image a) {
                        SourceImage=a;
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
                public Image getSourceImage() {
                        return SourceImage;
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
        
        
        
        class Polygon {

            public int[] xpoints;
            public int[] ypoints;
            public int npoints;

            public Polygon(int[] xCoords, int[] yCoords, int length) {
                this.xpoints = xCoords;
                this.ypoints = yCoords;
                this.npoints = length;
            }
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

}

