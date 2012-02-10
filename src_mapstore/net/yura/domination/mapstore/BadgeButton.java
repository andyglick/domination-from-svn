package net.yura.domination.mapstore;

import java.util.Observable;
import java.util.Observer;
import javax.microedition.lcdui.Image;
import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.border.MatteBorder;
import net.yura.mobile.gui.components.Component;
import net.yura.mobile.gui.components.RadioButton;

/**
 * @author Yura
 */
public class BadgeButton extends RadioButton implements Observer {

    MatteBorder border;
    String badge="0"; // default is 0, and it will not draw anything when set to 0
    
    public BadgeButton() {
        
        try {
            border = MatteBorder.load9png( Image.createImage("/ic_notification_overlay.9.png") );
        }
        catch (Exception ex) { }

        MapUpdateService.getInstance().addObserver(this);

    }

    public void paint(Graphics2D g) {
        super.paint(g);
        
        int w = getWidth();
        g.translate(w, 0);
        MapUpdateService.paintBadge(g,badge,border );
        g.translate(-w, 0);
    }

    public void update(Observable o, Object arg) {
        badge = String.valueOf(arg);
        Component parent = getParent();
        if (parent!=null) {
            parent.repaint();
        }
    }
}
