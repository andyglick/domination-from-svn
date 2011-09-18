package net.yura.domination.mapstore;

import java.util.Hashtable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;
import net.yura.abba.persistence.ImageManager;
import net.yura.abba.ui.components.IconCache;
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

    ImageManager manager = new ImageManager();
    Hashtable images = new Hashtable();

    private ProgressBar bar = new ProgressBar();
    private Component list;
    
    private String context;
    
    Image play,download;
    
    MapChooser chooser;
    Map map;
    
    public MapRenderer(MapChooser chooser) {
        setVerticalTextPosition( Graphics.TOP );
        
        this.chooser = chooser;
        
        try {
            Image img = Image.createImage("/strip.png");
            Sprite spin1 = new Sprite( img , img.getHeight(), img.getHeight() );
            bar.setSprite(spin1);
            bar.workoutPreferredSize();
            
            play = Image.createImage("/play.png");
            download = Image.createImage("/download.png");
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
        
        this.list = list;
        
        line2 = null; // reset everything
        map = null;

        if (value instanceof Category) {
            Category category = (Category)value;

            setText( category.getName() );
        }
        else if (value instanceof Map) {
            map = (Map)value;

            setText( map.getName() );
            //line2 = map.getAuthorName();
            line2 = map.getDescription();

            String key = map.getPreviewUrl();

            if (key!=null) {
            
                key = MapChooser.getURL(context, key);
  
                //key = "http://www.imagegenerator.net/clippy/image.php?question="+map.getName();

                IconCache icon = (IconCache)images.get( key );
                if (icon==null) {
                    icon = new IconCache(key, null, 0, 0, 0, 0, manager);
                    images.put(key, icon);
                }

                setIcon( icon );
            }
            else {
                System.out.println("No PreviewUrl for map "+map);
            }
        }
        // else just do nothing

        return c;
    }

    public void paintComponent(Graphics2D g) {
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
            
            if ( chooser.isDownloading( mapUID ) ) { // we need to check for this first as we may have it and also be updating it

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
    }

    public void workoutMinimumSize() {
        super.workoutMinimumSize();

        // TODO could be done better!
        if (line2!=null && getIcon()==null ) {
            height = height + getFont().getHeight()+gap;
        }
    }

}
