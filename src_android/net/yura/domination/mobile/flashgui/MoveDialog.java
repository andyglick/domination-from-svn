package net.yura.domination.mobile.flashgui;

import android.graphics.ColorMatrix;
import com.nokia.mid.ui.DirectGraphics;
import com.nokia.mid.ui.DirectUtils;
import java.util.List;
import javax.microedition.lcdui.Image;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.mobile.PicturePanel;
import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.ChangeListener;
import net.yura.mobile.gui.Font;
import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.KeyEvent;
import net.yura.mobile.gui.components.Button;
import net.yura.mobile.gui.components.Component;
import net.yura.mobile.gui.components.Frame;
import net.yura.mobile.gui.components.Panel;
import net.yura.mobile.gui.components.Slider;
import net.yura.mobile.gui.layout.Layout;
import net.yura.mobile.gui.layout.XULLoader;
import net.yura.mobile.gui.plaf.Style;
import net.yura.mobile.util.Properties;

/**
 * @author Yura
 */
public class MoveDialog extends Frame implements ActionListener,ChangeListener {

    PicturePanel pp;
    Font font;
    
    Properties resb = GameActivity.resb;
    
    Risk myrisk;
    Slider slider;
    Button cancelMove;
    Button moveb;
    
    int c1num,c2num;
    
    public MoveDialog(Risk risk,PicturePanel p) {
        myrisk = risk;
        pp = p;

        slider = new Slider();
        slider.addChangeListener(this);

        cancelMove = new Button(resb.getProperty("move.cancel"));
        cancelMove.setActionCommand("cancel");

        final Button moveall = new Button(resb.getProperty("move.moveall"));
        moveall.setActionCommand("all");
        moveb = new Button(resb.getProperty("move.move"));
        moveb.setActionCommand("move");

        final Panel moveControl = new Panel();

        moveControl.add(moveall);
        moveControl.add(moveb);
        moveControl.add(cancelMove);

        cancelMove.addActionListener(this);
        moveall.addActionListener(this);
        moveb.addActionListener(this);

        Panel contentPane = getContentPane();
        
        contentPane.setLayout( new DialogLayout( getImageAreaHeight() ) );
        contentPane.add(slider);
        contentPane.add(moveControl);
        setMaximum(true);


        setName("TransparentDialog");
        setForeground(0xFF000000);
        setBackground(0xAA000000);
    }
    
    private int getImageAreaHeight() {
        return font.getHeight() * 5;
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
        int max = src-1;

        slider.setVisible( min!=max );
        moveb.setVisible( min!=max );
        
        slider.setMinimum(min);
        slider.setMaximum( max );
        slider.setValue(min);
/* TODO
        int spacig = Math.round( (src-1)/10f );

        if (spacig==0) {
                slider.setMajorTickSpacing(1);
        }
        else {
                slider.setMajorTickSpacing( spacig );
                slider.setMinorTickSpacing(1);
        }
*/
        revalidate();

    }


    @Override
    public void paintComponent(Graphics2D g) {

        Image c1img = pp.getCountryImage(c1num);
        Image c2img = pp.getCountryImage(c2num);
        int csrc = myrisk.hasArmiesInt( c1num );
        int cdes = myrisk.hasArmiesInt( c2num );
        int color = myrisk.getCurrentPlayerColor();
        int move = (Integer)slider.getValue();

        g.setFont(font);

        int imageAreaHeight = getImageAreaHeight();
        int heightOfComponents = ((DialogLayout)getContentPane().getLayout()).getHeightOfComponents(getContentPane());
        // this is the MIDDLE of the images area
        int xOffset = getContentPane().getWidth() / 2;
        int yOffset = (getContentPane().getHeight()-heightOfComponents)/2 + imageAreaHeight/2 + getContentPane().getY();

        paintMove(g,xOffset,yOffset,c1img,c2img,color,color,csrc-move,cdes+move,move);
    }

    public static int distanceFromCenter = XULLoader.adjustSizeToDensity(80);
    
    public static void paintMove(Graphics2D g,
            int xMiddle,int yMiddle,
            Image c1img,Image c2img,
            int color1, int color2,
            int noa1, int noa2, int move
            ) {
        
        int fh = g.getFont().getHeight();
        

        g.getGraphics().setColorMarix( getMarix(color1) );
        g.drawImage(c1img, xMiddle-distanceFromCenter-(c1img.getWidth()/2), yMiddle-(c1img.getHeight()/2));
        
        if (color1!=color2) g.getGraphics().setColorMarix( getMarix(color2) );
        g.drawImage(c2img, xMiddle+distanceFromCenter-(c2img.getWidth()/2), yMiddle-(c2img.getHeight()/2));

        g.getGraphics().setColorMarix(null);

        //g.setColor( 0xFF000000 );
        //tl = new TextLayout( country1.getName() , font, frc); // Display
        //tl.draw( g, (float) (130-(tl.getBounds().getWidth()/2)), (float)40 );
        //tl = new TextLayout( country2.getName() , font, frc); // Display
        //tl.draw( g, (float) (350-(tl.getBounds().getWidth()/2)), (float)40 );

        g.setColor( color1 );
        g.fillOval( xMiddle-distanceFromCenter -(fh/2) , yMiddle-(fh/2) , fh, fh);
        
        g.setColor( color2 );
        g.fillOval( xMiddle+distanceFromCenter -(fh/2) , yMiddle-(fh/2) , fh, fh );

        int xOffset = XULLoader.adjustSizeToDensity(52);
        int yOffset = XULLoader.adjustSizeToDensity(15);
        int xCoords[] = {xMiddle-xOffset, xMiddle, xMiddle, xMiddle+xOffset, xMiddle, xMiddle, xMiddle-xOffset};
        int yCoords[] = {yMiddle-yOffset,  yMiddle-yOffset,  yMiddle-yOffset*2, yMiddle, yMiddle+yOffset*2,  yMiddle+yOffset, yMiddle+yOffset};
        DirectGraphics g2 = DirectUtils.getDirectGraphics(g.getGraphics());
        g2.fillPolygon(xCoords, 0, yCoords, 0, xCoords.length, PicturePanel.colorWithAlpha(color1, 150) );

        

        int textY = yMiddle-(fh/2);
        
        g.setColor( RiskUtil.getTextColorFor(color1) );
        if (noa1 < 10) {
                g.drawString( String.valueOf( noa1 ) , xMiddle-distanceFromCenter -4, textY );
        }
        else if (noa1 < 100) {
                g.drawString( String.valueOf( noa1 ) , xMiddle-distanceFromCenter -7, textY );
        }
        else {
                g.drawString( String.valueOf( noa1 ) , xMiddle-distanceFromCenter -10, textY );
        }

        g.setColor( RiskUtil.getTextColorFor(color2) );
        if (noa2 < 10) {
                g.drawString( String.valueOf( noa2 ) , xMiddle+distanceFromCenter -4, textY );
        }
        else if (noa2 < 100) {
                g.drawString( String.valueOf( noa2 ) , xMiddle+distanceFromCenter -7, textY );
        }
        else {
                g.drawString( String.valueOf( noa2 ) , xMiddle+distanceFromCenter -10, textY );
        }

        if (move > 0) {
            g.drawString( Integer.toString(move) , xMiddle -7, textY );
        }

    }

    static ColorMatrix getMarix(int color) {
        ColorMatrix m = PicturePanel.RescaleOp( 0.5f, -1.0f);
        m.preConcat(PicturePanel.gray);
        m.postConcat( PicturePanel.getMatrix( PicturePanel.colorWithAlpha(color, 100) ) );
        return m;
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
    
    // events from slider
    public void changeEvent(Component source, int num) {
        repaint();
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

    @Override
    public void updateUI() {
        super.updateUI();
        font = theme.getFont(Style.ALL);
    }

    
    public static class DialogLayout implements Layout {
        
        int imageAreaHeight;
        int gap;

        public DialogLayout(int imageAreaHeight) {
            this.imageAreaHeight = imageAreaHeight;
            gap = XULLoader.adjustSizeToDensity(5);
        }
        
        
        public void layoutPanel(Panel panel) {

            int heightOfComponents = getHeightOfComponents(panel);

            int yOffset = (panel.getHeight()-heightOfComponents)/2 + imageAreaHeight + gap;
            int width = panel.getWidth();
            for (Component c: (List<Component>)panel.getComponents() ) {
                if (c.isVisible()) {
                    int w = c.getWidthWithBorder();
                    if (w < heightOfComponents) {
                        w = heightOfComponents; // strech the slider to at least heightOfComponents
                    }
                    int h = c.getHeightWithBorder();
                    c.setBoundsWithBorder(
                            (width-w)/2, 
                            yOffset, 
                            w, 
                            h);
                    yOffset = yOffset + h + gap;
                }
            }

        }

        public int getPreferredHeight(Panel panel) {
            return 10; //  dont care
        }

        public int getPreferredWidth(Panel panel) {
            return 10; //  dont care
        }

        public int getHeightOfComponents(Panel panel) {
            int heightOfComponents = imageAreaHeight + gap*panel.getComponentCount();
            List<Component> comps = panel.getComponents();
            for (Component c:comps) {
                if (c.isVisible()) {
                    heightOfComponents = heightOfComponents + c.getHeightWithBorder();
                }
            }
            return heightOfComponents;
        }
            
    }
    
}
