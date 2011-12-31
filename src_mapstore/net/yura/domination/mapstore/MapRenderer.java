package net.yura.domination.mapstore;

import java.util.Hashtable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.mobile.gui.Animation;
import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.Icon;
import net.yura.mobile.gui.cellrenderer.DefaultListCellRenderer;
import net.yura.mobile.gui.components.Component;
import net.yura.mobile.gui.components.ProgressBar;
import net.yura.mobile.gui.plaf.Style;

/**
 * @author Yura Mamyrin
 */
public class MapRenderer extends DefaultListCellRenderer {

    String line2;

    //net.yura.abba.persistence.ImageManager manager = new net.yura.abba.persistence.ImageManager();
    Hashtable images = new Hashtable();

    private ProgressBar bar = new ProgressBar();
    private Component list;
    
    private String context;
    
    Image play,download;
    Icon loading;
    
    MapChooser chooser;
    Map map;
    
    public MapRenderer(MapChooser chooser) {
        
        this.chooser = chooser;
        
        try {
            Image img = Image.createImage("/strip.png");
            Sprite spin1 = new Sprite( img , img.getHeight(), img.getHeight() );
            bar.setSprite(spin1);
            bar.workoutPreferredSize();
            //add(bar); // YURA do we need this???
            
            play = Image.createImage("/play.png");
            download = Image.createImage("/download.png");
            
            loading = new Icon("/icon_loading.png");
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public void animate() {
        bar.animate();

        if (list!=null) {
            list.repaint();
        }
    }
    
    
    public void setContext(String c) {
        context = c;
    }
    
    public String getContext() {
        return context;
    }

    public Component getListCellRendererComponent(Component list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        
        setVerticalTextPosition( Graphics.TOP );
        
        this.list = list;
        
        line2 = null; // reset everything
        map = null;
        String iconUrl=null;

        if (value instanceof Category) {
            Category category = (Category)value;

            setText( category.getName() );
            setVerticalTextPosition( Graphics.VCENTER );
            
            iconUrl = category.getIconURL();
        }
        else if (value instanceof Map) {
            map = (Map)value;

            setText( map.getName() );
            
            String author = map.getAuthorName();
            if (author!=null && !"".equals(author)) {
                line2 = TranslationBundle.getBundle().getString("mapchooser.by").replaceAll("\\{0\\}", author);
            }
            String description = map.getDescription();
            if (description!=null && !"".equals(description)) {
                line2 = (line2==null?"":line2+"\n")+description;
            }

            iconUrl = map.getPreviewUrl();
        }
        // else just do nothing


        if (iconUrl!=null) {

            iconUrl = MapChooser.getURL(context, iconUrl);

            //key = "http://www.imagegenerator.net/clippy/image.php?question="+map.getName();

            Icon aicon = (Icon)images.get( iconUrl );
            if (aicon==null) {
                //icon = new net.yura.abba.ui.components.IconCache(key, null, 0, 0, 0, 0, manager);
                //icon = net.yura.abba.ui.AbbaIcon.getIcon(iconUrl, 0, 0, 0, 0);
                
                aicon = new LazyIcon();
                images.put(iconUrl, aicon);
                
                chooser.loadImg( iconUrl );
            }

            setIcon( aicon );
        }
        else {
            System.out.println("[MapRenderer] No PreviewUrl for map or category: "+value);
        }

        
        return c;
    }
    
    public void gotImg(String url,Image img) {
        LazyIcon aicon = (LazyIcon)images.get( url );
        aicon.setImage(img);
    }
    
    public static class LazyIcon extends Icon {

        Image img;
        
        void setImage(Image img) {
            this.img = img;
            height = this.img.getHeight();
            width = this.img.getWidth();
        }

        public void paintIcon(Component c, Graphics2D g, int x, int y) {
            if (img!=null) {
                g.drawImage(img, x, y);
            }
        }

        public Image getImage() {
            return img;
        }

    }

    public void paintComponent(Graphics2D g) {
        
        Icon icon = getIcon();
        if (icon==null || icon.getImage()==null) {
            setIcon(loading);
        }

        super.paintComponent(g);
        
        if (line2!=null) {
            Icon i = getIcon();

            int state = getCurrentState();

            // if NOT focused or selected
            if ( (state&Style.FOCUSED)==0 && (state&Style.SELECTED)==0 ) {
                int color = theme.getForeground(Style.DISABLED);
                g.setColor( color );
            }

            g.drawString(line2, padding + (i!=null?i.getIconWidth()+gap:0), padding + getFont().getHeight() + gap);
        }

        if (map!=null) {
        
            int gap = 5;

            String mapUID = MapChooser.getFileUID( map.getMapUrl() );
            
            if ( chooser.client.isDownloading( mapUID ) ) { // we need to check for this first as we may have it and also be updating it

                // position spinner in top right corner
                int x = getWidth()-bar.getWidth()-gap;
                int y = gap;

                g.translate(x, y);

                bar.paintComponent(g);

                g.translate(-x, -y);


                // its ok to register more than once
                Animation.registerAnimated(this);
            }
            else if ( chooser.mapfiles.contains( mapUID ) ) {

                g.drawImage(play, getWidth()-play.getWidth()-gap, gap);

            }
            else {
                
                g.drawImage(download, getWidth()-download.getWidth()-gap, gap);
                
            }
        }
        
        setIcon(icon);
    }

    public void workoutMinimumSize() {
        super.workoutMinimumSize();

        // TODO could be done better!
        if (line2!=null && getIcon()==null ) {
            height = height + getFont().getHeight()+gap;
        }
    }

}
