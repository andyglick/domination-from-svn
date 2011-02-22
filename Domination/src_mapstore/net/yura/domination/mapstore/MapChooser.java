package net.yura.domination.mapstore;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import net.yura.abba.Events;
import net.yura.abba.persistence.AbbaRepository;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.domination.mapstore.gen.BinMapAccess;
import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.ButtonGroup;
import net.yura.mobile.gui.Icon;
import net.yura.mobile.gui.border.LineBorder;
import net.yura.mobile.gui.components.Button;
import net.yura.mobile.gui.components.Component;
import net.yura.mobile.gui.components.List;
import net.yura.mobile.gui.components.Panel;
import net.yura.mobile.gui.components.RadioButton;
import net.yura.mobile.gui.components.TextComponent;
import net.yura.mobile.gui.layout.XULLoader;
import net.yura.mobile.io.NativeUtil;
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

    AbbaRepository repo;
    MapServerClient client;

    Vector mapfiles;

    public MapChooser(ActionListener al,Vector mapfiles) {
        this.al = al;
        this.mapfiles = mapfiles;

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

            b.setToolTipText( b.getText() );

            b.setText("");
            b.setHorizontalAlignment(Graphics.HCENTER);
            b.setMargin(0);

        }

        List list = (List)loader.find("ResultList");
        list.setDoubleClick(true);
        list.setCellRenderer( new MapRenderer() );

        client = new MapServerClient(this);
        client.start();

        repo = new AbbaRepository(256 * 1024, 2 * 1024 * 1024, new BinMapAccess(), client);
        Events.CLIENT_RESOURCE.subscribe(repo);
        Events.SERVER_GET_RESOURCE.resubscribe(client,repo);

        activateGroup("MapView");

    }

    public void destroy() {

        Events.CLIENT_RESOURCE.unsubscribe(repo);
        Events.SERVER_GET_RESOURCE.unsubscribe(repo);

        repo.destroy();
        repo=null;
    }

    public void actionPerformed(String actionCommand) {
        if ("local".equals(actionCommand)) {
            mainCatList(actionCommand);


            Vector riskmaps = new Vector( mapfiles.size() );
            for (int c=0;c<mapfiles.size();c++) {
                String file = (String)mapfiles.elementAt(c);

                Hashtable info = RiskUtil.loadInfo(file, false);

                Map map = new Map();
                map.setMapUrl( file );
                map.setName( (String)info.get("name") );
                map.setPreviewUrl( "preview/"+(String)info.get("prv") );
                map.setDescription( (String)info.get("comment") );
                
                riskmaps.add( map );
            }

            List list = (List)loader.find("ResultList");
            list.setListData( riskmaps );

        }
        else if ("catagories".equals(actionCommand)) {
            mainCatList(actionCommand);

            client.makeRequest( "categories.dot" );

        }
        else if ("top25".equals(actionCommand)) {
            mainCatList(actionCommand);

            activateGroup("Top25View");
        }
        else if ("search".equals(actionCommand)) {
            mainCatList(actionCommand);

            actionPerformed("doMapSearch");
        }
        else if ("update".equals(actionCommand)) {
            mainCatList(actionCommand);

            // TODO
        }
        else if ("TOP_NEW".equals(actionCommand)) {
            clearList();
            client.makeRequest( "maps.dot","category","TOP_NEW" );
        }
        else if ("TOP_RATINGS".equals(actionCommand)) {
            clearList();
            client.makeRequest( "maps.dot","category","TOP_RATINGS" );
        }
        else if ("TOP_DOWNLOADS".equals(actionCommand)) {
            clearList();
            client.makeRequest( "maps.dot","category","TOP_DOWNLOADS" );
        }
        else if ("listSelect".equals(actionCommand)) {
            List list = (List)loader.find("ResultList");
            Object value = list.getSelectedValue();
            if (value instanceof Category) {
                Category cat = (Category)value;
                clearList();
                client.makeRequest( "maps.dot","category",cat.getId() );
            }
            else {
                Map map = (Map)value;

                // download the map

                // set return value to this map

                selectedMap = map.getMapUrl();

                al.actionPerformed(null);
            }
        }
        else if ("cancel".equals(actionCommand)) {
            al.actionPerformed(null);
        }
        else if ("defaultMap".equals(actionCommand)) {

            // TODO ???
        }
        else if ("doMapSearch".equals(actionCommand)) {
            String text = ((TextComponent)loader.find("mapSearchBox")).getText();
            clearList();
            if (text != null && !"".equals(text)) {
                client.makeRequest( "maps.dot","search", text );
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

        clearList();

    }

    void clearList() {
        List list = (List)loader.find("ResultList");
        list.setListData( new Vector(0) ); // todo, use a constant?

        getRoot().revalidate();
        getRoot().repaint();
    }

    public Panel getRoot() {
        return ((Panel)loader.getRoot());
    }

    private String selectedMap;
    public String getSelectedMap() {
        return selectedMap;
    }

    void gotResult(Task task) {
        String method = task.getMethod();
        System.out.println("got "+task);

        List list = (List)loader.find("ResultList");

        Object param = task.getObject();
        if ("categories".equals(method)) {
            if (param instanceof Vector) {
                list.setListData( (Vector)param );
            }
        }
        else if ("maps".equals(method)) {
            if (param instanceof Hashtable) {
                Hashtable map = (Hashtable)param;

                map.get("search");
                map.get("author");
                map.get("category");

                map.get("offset");
                map.get("total");

                list.setListData( (Vector)map.get("maps") );
            }
        }

        getRoot().revalidate();
        getRoot().repaint();
    }

    private void activateGroup(String string) {

        String mincat = ((ButtonGroup)loader.getGroups().get(string)).getSelection().getActionCommand();
        actionPerformed(mincat);

    }

}
