package net.yura.domination.mapstore;

import java.util.Enumeration;
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

    public MapChooser(ActionListener al) {

        
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


        }
        else if ("catagories".equals(actionCommand)) {
            mainCatList(actionCommand);

            MapServerClient client = new MapServerClient(this);
            client.start();

            Request request = new Request();
            request.url = "http://maps.domination.yura.net/xml/categories.dot";

            // TODO, should be using RiskIO to do this get
            // as otherwise it will not work with lobby
            client.makeRequest(request);

        }
        else if ("top25".equals(actionCommand)) {
            mainCatList(actionCommand);

        }
        else if ("search".equals(actionCommand)) {
            mainCatList(actionCommand);

        }
        else if ("update".equals(actionCommand)) {
            mainCatList(actionCommand);

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
        List panel = (List)loader.find("ResultList");

        Object param = task.getObject();
        if (param instanceof Vector) {
            panel.setListData( (Vector)param );
        }

        getRoot().revalidate();
        getRoot().repaint();
    }

}
