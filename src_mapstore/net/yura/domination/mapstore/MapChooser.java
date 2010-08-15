package net.yura.domination.mapstore;

import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.Icon;
import net.yura.mobile.gui.components.Frame;
import net.yura.mobile.gui.components.Panel;
import net.yura.mobile.gui.components.RadioButton;
import net.yura.mobile.gui.layout.XULLoader;
import net.yura.mobile.util.Properties;
import net.yura.swingme.core.CoreUtil;

/**
 * @author Yura Mamyrin
 */
public class MapChooser implements ActionListener {

    public Properties resBundle = CoreUtil.wrap(TranslationBundle.getBundle());

    public MapChooser(ActionListener al) {

        XULLoader loader;
        try {
            loader = XULLoader.load( getClass().getResourceAsStream("/maps.xml") , this, resBundle);
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }

        Panel TabBar = (Panel)loader.find("TabBar");
        Vector buttons = TabBar.getComponents();



        Icon on,off;

        try {
            on = new Icon("/bar_on.png");
            off = new Icon("/bar_off.png");
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        int w = off.getIconWidth() / buttons.size();
        for (int c=0;c<buttons.size();c++) {
            RadioButton b = (RadioButton)buttons.elementAt(c);
            b.setIcon( off.getSubimage(c*w, 0, w, off.getIconHeight()) );
            b.setSelectedIcon( on.getSubimage(c*w, 0, w, off.getIconHeight()) );
            b.setText("");
            b.setHorizontalAlignment(Graphics.HCENTER);
            b.setMargin(0);
        }

        //Frame mapframe = new Frame( resBundle.getProperty("newgame.choosemap") );


        ((Frame)loader.getRoot()).setVisible(true);

        //System.out.println("map: "+mapframe);

    }

    public void actionPerformed(String actionCommand) {
        
    }

    public String getSelectedMap() {
        return "todo";
    }

}
