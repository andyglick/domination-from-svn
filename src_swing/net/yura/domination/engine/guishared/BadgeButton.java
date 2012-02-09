package net.yura.domination.engine.guishared;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Observable;
import java.util.Observer;
import javax.microedition.lcdui.Image;
import javax.swing.JButton;
import net.yura.domination.mapstore.MapUpdateService;
import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.border.MatteBorder;

/**
 * @author Yura
 */
public class BadgeButton extends JButton implements Observer {

    MatteBorder border;
    String badge="0"; // default is 0, and it will not draw anything when set to 0
    
    public BadgeButton(String string) {
        super(string);
        
        try {
            border = MatteBorder.load9png( Image.createImage("/ic_notification_overlay.9.png") );
        }
        catch (Exception ex) { }

        MapUpdateService.getInstance().addObserver(this);

    }

    public void paint(Graphics g) {
        super.paint(g);
        
        if (!"0".equals(badge)) {
        
            int tw = g.getFontMetrics().stringWidth(badge.length()==1?" "+badge:badge); // make sure its not too thin
            int th = g.getFontMetrics().getHeight();

            int overlap = 5;

            int l,r,t,b;
            if (border==null) {
                l=r=t=b=3;
            }
            else {
                l=border.getLeft();
                r=border.getRight();
                t=border.getTop();
                b=border.getBottom();
            }

            int x,y,w,h;

            w = l+r+tw;
            h = t+b+th;
            x = (getWidth()+overlap) - w;
            y = -overlap;

            g.setClip(x, y, w, h);

            if (border==null) {
                g.setColor(Color.RED);
                g.fillOval(x, y, w, h);
            }
            else {
                g.translate( x+border.getLeft() , y+border.getTop());
                border.paintBorder(null, new Graphics2D( new javax.microedition.lcdui.Graphics(g) ), w-border.getLeft()-border.getRight(), h-border.getTop()-border.getBottom());
                g.translate( -x-border.getLeft() , -y-border.getTop());
            }

            g.setColor(Color.WHITE);
            g.drawString(badge, 1+ x + (w-g.getFontMetrics().stringWidth(badge))/2, -1+ y + h - (h-g.getFontMetrics().getAscent())/2);
        }
    }

    public void update(Observable o, Object arg) {
        badge = String.valueOf(arg);
        getParent().repaint();
    }
    
}
