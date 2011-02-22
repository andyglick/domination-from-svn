package net.yura.domination.mapstore;

import java.util.Hashtable;
import javax.microedition.lcdui.Graphics;
import net.yura.abba.persistence.ImageManager;
import net.yura.abba.ui.components.IconCache;
import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.Icon;
import net.yura.mobile.gui.cellrenderer.DefaultListCellRenderer;
import net.yura.mobile.gui.components.Component;
import net.yura.mobile.gui.plaf.Style;

/**
 * @author Yura Mamyrin
 */
public class MapRenderer extends DefaultListCellRenderer {

    String line2;

    ImageManager manager = new ImageManager();
    Hashtable images = new Hashtable();

    public MapRenderer() {
        setVerticalTextPosition( Graphics.TOP );
    }

    public Component getListCellRendererComponent(Component list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        line2 = null; // reset everything

        if (value instanceof Category) {
            Category category = (Category)value;

            setText( category.getName() );
        }
        else if (value instanceof Map) {
            Map map = (Map)value;

            setText( map.getName() );
            //line2 = map.getAuthorName();
            line2 = map.getDescription();

            String key = map.getPreviewUrl();

            //key = "http://www.imagegenerator.net/clippy/image.php?question="+map.getName();

            IconCache icon = (IconCache)images.get( key );
            if (icon==null) {
                icon = new IconCache(key, null, 0, 0, 0, 0, manager);
                images.put(key, icon);
            }

            setIcon( icon );
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

    }

    public void workoutMinimumSize() {
        super.workoutMinimumSize();

        // TODO could be done better!
        if (line2!=null && getIcon()==null ) {
            height = height + getFont().getHeight()+gap;
        }
    }

}
