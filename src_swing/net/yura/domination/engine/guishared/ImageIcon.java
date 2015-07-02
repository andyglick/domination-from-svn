package net.yura.domination.engine.guishared;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.net.URL;

public class ImageIcon extends javax.swing.ImageIcon {
    
    public ImageIcon (String filename) {
        super(filename);
    }

    public ImageIcon(URL location) {
        super(location);
    }

    public ImageIcon(Image image) {
        super(image);
    }

    @Override
    public int getIconWidth() {
        return GraphicsUtil.scale(super.getIconWidth());
    }
    
    @Override
    public int getIconHeight() {
        return GraphicsUtil.scale(super.getIconHeight());
    }

    @Override
    public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
        g.drawImage(getImage(), x, y, getIconWidth(), getIconHeight(), c);
    }
}
