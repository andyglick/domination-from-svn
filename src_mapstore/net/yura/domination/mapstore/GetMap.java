package net.yura.domination.mapstore;

import java.util.Hashtable;
import java.util.Vector;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.mobile.io.ServiceLink.Task;

/**
 * @author Yura Mamyrin
 */
public class GetMap implements MapServerListener {

    MapServerClient client;
    Risk myrisk;
    String filename;
    Exception problem;
    
    public static void getMap(String filename, Risk risk,Exception ex) {
        GetMap get = new GetMap();
        get.filename = filename;
        get.myrisk = risk;
        get.problem = ex;
        get.client = new MapServerClient(get, MapChooser.SERVER_URL);
        get.client.start();
        get.client.makeRequestXML( MapChooser.MAP_PAGE,"mapfile",filename );
    }

    public void onError(String string) {
        myrisk.getMapError(string);
        client.kill();
    }

    public void gotResultXML(String url, Task task) {
        Hashtable map = (Hashtable)task.getObject();
        Vector maps = (Vector)map.get("maps");
        if (maps.size()==1) {
            Map themap = (Map)maps.elementAt(0);
            client.downloadMap( MapChooser.getURL(MapChooser.getContext(url), themap.mapUrl ) );
        }
        else if (maps.size()==0) {
            
            
        }
        else { // this should never happen
            String error = "wrong number of maps: "+maps.size()+" for map: "+filename;
            System.err.println(error);
            onError(error);
        }
    }

    public void downloadFinished(String mapUID) {
        try {
            myrisk.setMap(mapUID);
            client.kill();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            onError( TranslationBundle.getBundle().getString( "core.choosemap.error.unable")+" "+ex );
        }
    }
    
}
