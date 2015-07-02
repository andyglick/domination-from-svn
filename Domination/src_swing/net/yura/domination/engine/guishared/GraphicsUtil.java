package net.yura.domination.engine.guishared;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;

/**
 * code "borrowed" from com.intellij.util.ui.JBUI
 */
public class GraphicsUtil {

    public static boolean IS_HIDPI;

    static {
        IS_HIDPI = System.getProperty("os.name").toLowerCase().startsWith("windows") && Toolkit.getDefaultToolkit().getScreenResolution() > 144;
    }

    public static int scale(int i) {
        return IS_HIDPI ? 2 * i : i;
    }
    
    public static void setBounds(Component comp, int x, int y, int w, int h) {
        comp.setBounds(scale(x), scale(y), scale(w), scale(h));
    }

    public static void drawImage(Graphics g, Image img,
            int dx1, int dy1, int dx2, int dy2,
            int sx1, int sy1, int sx2, int sy2,
            ImageObserver observer) {
        g.drawImage(img,
                scale(dx1),
                scale(dy1),
                scale(dx2),
                scale(dy2),
                sx1, sy1, sx2, sy2, observer);
    }

    public static void drawImage(Graphics g, Image img, int x, int y, ImageObserver observer) {
        g.drawImage(img,
                scale(x),
                scale(y),
                scale(img.getWidth(observer)),
                scale(img.getHeight(observer)),
                observer);
    }
}
