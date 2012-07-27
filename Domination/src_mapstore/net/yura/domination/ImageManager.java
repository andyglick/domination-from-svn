package net.yura.domination;

import java.util.WeakHashMap;
import javax.microedition.lcdui.Image;
import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.Icon;
import net.yura.mobile.gui.components.Component;

public class ImageManager {

    public static final WeakHashMap images = new WeakHashMap();
    
    public static void put(Object key,LazyIcon icon) {
        images.put(key, icon);
    }
    public static LazyIcon get(Object key) {
        LazyIcon icon = (LazyIcon)images.get(key);
        // if we found it, it may be using a different key, just to make sure, put it back with this key
        if (icon!=null) {
            put(key, icon);
        }
        return icon;
    }
    public static LazyIcon newIcon(Object key,int w,int h) {
        LazyIcon icon = new LazyIcon( w,h );
        put(key, icon);
        return icon;
    }

    public static void gotImg(Object key, Image img) {
        LazyIcon icon = get( key );
        if (icon!=null) {
            icon.setImage(img);
        }
    }

    public static class LazyIcon extends Icon {

        Image img;
        public LazyIcon(int w,int h) {
            width = w;
            height = h;
        }
        
        public void setImage(Image img) {
            this.img = img;
        }

        public void paintIcon(Component c, Graphics2D g, int x, int y) {
            if (img!=null) {
                g.drawScaledImage(img, x, y, width, height);
            }
        }

        public Image getImage() {
            return img;
        }

    }
}
