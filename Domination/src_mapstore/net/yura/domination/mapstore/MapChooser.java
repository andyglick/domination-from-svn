package net.yura.domination.mapstore;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.ButtonGroup;
import net.yura.mobile.gui.Icon;
import net.yura.mobile.gui.components.Button;
import net.yura.mobile.gui.components.Component;
import net.yura.mobile.gui.components.List;
import net.yura.mobile.gui.components.Panel;
import net.yura.mobile.gui.components.RadioButton;
import net.yura.mobile.gui.components.TextComponent;
import net.yura.mobile.gui.layout.XULLoader;
import net.yura.mobile.io.HTTPClient.Request;
import net.yura.mobile.io.ServiceLink.Task;
import net.yura.mobile.util.Properties;
import net.yura.swingme.core.CoreUtil;

/**
 * @author Yura Mamyrin
 */
public class MapChooser implements ActionListener {

    public Properties resBundle = CoreUtil.wrap(TranslationBundle.getBundle());

    XULLoader loader;
    ActionListener al;

    public MapChooser(ActionListener al) {
        this.al = al;
        
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
            Icon oni = on.getSubimage(c*w, 0, w, off.getIconHeight());
            Icon offi = off.getSubimage(c*w, 0, w, off.getIconHeight());

            b.setIcon(offi);
            b.setSelectedIcon(oni);
            b.setRolloverIcon(offi);
            b.setRolloverSelectedIcon(oni);

            b.setText("");
            b.setHorizontalAlignment(Graphics.HCENTER);
            b.setMargin(0);
        }

        //Frame mapframe = new Frame( resBundle.getProperty("newgame.choosemap") );

        //System.out.println("map: "+mapframe);

    }

    public void actionPerformed(String actionCommand) {
        if ("local".equals(actionCommand)) {
            mainCatList(actionCommand);

            // get list of maps
            File file = new File("maps");
            String [] maps = file.list();


        }
        else if ("catagories".equals(actionCommand)) {
            mainCatList(actionCommand);

            MapServerClient client = new MapServerClient(this);
            client.start();

            client.makeRequest( "categories.dot",null );

        }
        else if ("top25".equals(actionCommand)) {
            mainCatList(actionCommand);

            String mincat = ((ButtonGroup)loader.getGroups().get("Top25View")).getSelection().getActionCommand();
            actionPerformed(mincat);
        }
        else if ("search".equals(actionCommand)) {
            mainCatList(actionCommand);

            actionPerformed("doMapSearch");
        }
        else if ("update".equals(actionCommand)) {
            mainCatList(actionCommand);

        }
        else if ("TOP_NEW".equals(actionCommand)) {
             
        }
        else if ("TOP_RATINGS".equals(actionCommand)) {
            
        }
        else if ("TOP_DOWNLOADS".equals(actionCommand)) {
            
        }
        else if ("listSelect".equals(actionCommand)) {
            List list = (List)loader.find("ResultList");
            Object value = list.getSelectedValue();
            if (value instanceof Category) {


            }
            else {
                // download the map

                // set return value to this map

                al.actionPerformed(null);
            }
        }
        else if ("cancel".equals(actionCommand)) {
            al.actionPerformed(null);
        }
        else if ("defaultMap".equals(actionCommand)) {


        }
        else if ("doMapSearch".equals(actionCommand)) {
            String text = ((TextComponent)loader.find("mapSearchBox")).getText();

            if (text == null || "".equals(text)) {
                // clear list
            }

        }
        else {
            System.out.println("Unknown command "+actionCommand);
        }
    }

    public void mainCatList(String actionCommand) {
        Enumeration group = ((ButtonGroup)loader.getGroups().get("MapView")).getElements();
        while (group.hasMoreElements()) {
            Button button = (Button)group.nextElement();
            String action = button.getActionCommand();
            Component panel = loader.find(action+"Bar");
            panel.setVisible( action.equals(actionCommand) );
        }

        List list = (List)loader.find("ResultList");
        list.setListData( new Vector(0) ); // todo, use a constant?

        getRoot().revalidate();
        getRoot().repaint();
    }

    public Panel getRoot() {
        return ((Panel)loader.getRoot());
    }

    public String getSelectedMap() {
        return "todo";
    }

    void gotResult(Task task) {
        List list = (List)loader.find("ResultList");

        Object param = task.getObject();
        if (param instanceof Vector) {
            list.setListData( (Vector)param );
        }

        getRoot().revalidate();
        getRoot().repaint();
    }

}
