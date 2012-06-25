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
        
        contentPane.setLayout(new Layout() {
            public void layoutPanel(Panel panel) {
                
                int imageAreaHeight = font.getHeight() * 4;
                
                int heightOfComponents = getHeightOfComponents(imageAreaHeight);
                
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
        });
        contentPane.add(slider);
        contentPane.add(moveControl);
        setMaximum(true);


        setName("TransparentDialog");
        setForeground(0xFF000000);
        setBackground(0xAA000000);
    }
    
    int gap = XULLoader.adjustSizeToDensity(5);
    int getHeightOfComponents(int imageAreaHeight) {
        Panel panel = getContentPane();
        
        int heightOfComponents = imageAreaHeight + gap*panel.getComponentCount();
        List<Component> comps = panel.getComponents();
        for (Component c:comps) {
            if (c.isVisible()) {
                heightOfComponents = heightOfComponents + c.getHeightWithBorder();
            }
        }
        return heightOfComponents;
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
        int move = (Integer)slider.getValue();
        int color = myrisk.getCurrentPlayerColor();

        g.setFont(font);
        int fh = font.getHeight();

        
        int imageAreaHeight = font.getHeight() * 4;
        int heightOfComponents = getHeightOfComponents(imageAreaHeight);
        // this is the MIDDLE of the images area
        int xOffset = getContentPane().getWidth() / 2;
        int yOffset = (getContentPane().getHeight()-heightOfComponents)/2 + imageAreaHeight/2 + getContentPane().getY();

        
        ColorMatrix m = PicturePanel.RescaleOp( 0.5f, -1.0f);
        m.preConcat(PicturePanel.gray);
        m.postConcat( PicturePanel.getMatrix( PicturePanel.colorWithAlpha(color, 100) ) );
        g.getGraphics().setColorMarix(m);

        g.drawImage(c1img, xOffset-110-(c1img.getWidth()/2), yOffset-(c1img.getHeight()/2));
        g.drawImage(c2img, xOffset+110-(c2img.getWidth()/2), yOffset-(c2img.getHeight()/2));

        g.getGraphics().setColorMarix(null);

        g.setColor( 0xFF000000 );

        //tl = new TextLayout( country1.getName() , font, frc); // Display
        //tl.draw( g, (float) (130-(tl.getBounds().getWidth()/2)), (float)40 );

        //tl = new TextLayout( country2.getName() , font, frc); // Display
        //tl.draw( g, (float) (350-(tl.getBounds().getWidth()/2)), (float)40 );

        g.setColor( color );

        g.fillOval( xOffset-110 -(fh/2) , yOffset-(fh/2) , fh, fh);
        g.fillOval( xOffset+110 -(fh/2) , yOffset-(fh/2) , fh, fh );

        int xCoords[] = {xOffset-70, xOffset, xOffset, xOffset+70, xOffset, xOffset, xOffset-70};
        int yCoords[] = {yOffset-20,  yOffset-20,  yOffset-40, yOffset, yOffset+40,  yOffset+20, yOffset+20};
        DirectGraphics g2 = DirectUtils.getDirectGraphics(g.getGraphics());
        g2.fillPolygon(xCoords, 0, yCoords, 0, xCoords.length, PicturePanel.colorWithAlpha(color, 150) );

        g.setColor( RiskUtil.getTextColorFor(color) );

        int noa;

        noa = csrc-move;

        int textY = yOffset-(fh/2);
        
        if (noa < 10) {
                g.drawString( String.valueOf( noa ) , xOffset-110 -4, textY );
        }
        else if (noa < 100) {
                g.drawString( String.valueOf( noa ) , xOffset-110 -7, textY );
        }
        else {
                g.drawString( String.valueOf( noa ) , xOffset-110 -10, textY );
        }

        noa = cdes+move;

        if (noa < 10) {
                g.drawString( String.valueOf( noa ) , xOffset+110 -4, textY );
        }
        else if (noa < 100) {
                g.drawString( String.valueOf( noa ) , xOffset+110 -7, textY );
        }
        else {
                g.drawString( String.valueOf( noa ) , xOffset+110 -10, textY );
        }

        g.drawString( Integer.toString(move) , xOffset -7, textY );

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

}
