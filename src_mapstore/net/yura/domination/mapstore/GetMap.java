package net.yura.domination.mapstore;

import java.util.Hashtable;
import java.util.Vector;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskUtil;
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
        get.client = new MapServerClient(get);
        get.client.start();
        get.client.makeRequestXML( MapChooser.MAP_PAGE,"mapfile",filename );
    }

    public void onError(String exception) {
        myrisk.getMapError(exception);
        client.kill();
    }

    public void gotResultXML(String url, Task task) {
        Hashtable map = (Hashtable)task.getObject();
        Vector maps = (Vector)map.get("maps");
        if (maps.size()==1) {
            Map themap = (Map)maps.elementAt(0);
            client.downloadMap( MapChooser.getURL(MapChooser.getContext(url), themap.mapUrl ) );
        }
        else {
            System.err.println( "wrong number of maps on server: "+maps.size()+" for map: "+filename );

            RiskUtil.printStackTrace(problem);
            onError(problem.toString());
        }
    }

    public void downloadFinished(String mapUID) {
        try {
            myrisk.setMap(mapUID);
            client.kill();
        }
        catch (Exception ex) {
            RiskUtil.printStackTrace(ex);
            onError(ex.toString());
        }
    }

    public void gotImg(String url, byte[] data) {
        throw new UnsupportedOperationException("Not supported");
    }
    
}
