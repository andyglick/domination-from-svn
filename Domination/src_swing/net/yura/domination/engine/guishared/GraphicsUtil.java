package net.yura.domination.engine.guishared;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.ImageObserver;

public class GraphicsUtil {

    public static final double density = getDisplayDensity();
    public static final double scale = getScale();

    public static int scale(int i) {
        return (int) (i * density / scale);
    }
    
    public static void setBounds(Component comp, int x, int y, int w, int h) {
        comp.setBounds(scale(x), scale(y), scale(w), scale(h));
    }

    public static Dimension newDimension(int width, int height) {
        return new Dimension(scale(width), scale(height));
    }

    public static Insets newInsets(int top, int left, int bottom, int right) {
        return new Insets(scale(top), scale(left), scale(bottom), scale(right));
    }

    public static void drawImage(Graphics g, Image img, int x, int y, ImageObserver observer) {
        g.drawImage(img,
                scale(x),
                scale(y),
                scale(img.getWidth(observer)),
                scale(img.getHeight(observer)),
                observer);
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

    private static double getDisplayDensity() {
        try {
            return ((Double)Class.forName("javax.microedition.midlet.ApplicationManager")
                    .getMethod("getDisplayDensity").invoke(null)).doubleValue();
        }
        catch (Throwable th) { }
        return 1;
    }
    
    private static double getScale() {
        try {
            return ((Double)Class.forName("javax.microedition.midlet.ApplicationManager")
                    .getMethod("getScale").invoke(null)).doubleValue();
        }
        catch (Throwable th) { }
        return 1;
    }
}
