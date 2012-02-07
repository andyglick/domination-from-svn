package net.yura.domination.mapstore;

import java.util.Hashtable;
import java.util.Vector;
import net.yura.mobile.io.ServiceLink.Task;

/**
 * @author Yura
 */
public class MapUpdateService implements MapServerListener {

    static MapUpdateService updateService;
    
    Vector maps;
    int results;
    
    Vector mapsToUpdate = new Vector();
    MapServerClient client;
    
    public static void init(Vector mapList) {
        
        if (updateService==null) {
            updateService = new MapUpdateService(mapList);
        }
        
    }
    public static MapUpdateService getInstance() {
        return updateService;
    }
    
    public MapUpdateService(Vector mapsUIDs) {
        maps = new Vector();
        client = new MapServerClient(this);
        client.start();
        for (int c=0;c<mapsUIDs.size();c++) {
            String uid = (String)mapsUIDs.elementAt(c);
            Map map = MapChooser.createMap(uid);
            maps.add( map );
            client.makeRequestXML( MapChooser.MAP_PAGE,"mapfile",uid );
        }
    }

    public void gotResultXML(String url, Task task) {

        Hashtable map = (Hashtable)task.getObject();
        Vector gotMaps = (Vector)map.get("maps");
        if (gotMaps.size()==1) {
            Map themap = (Map)gotMaps.elementAt(0);
            String ver = themap.getVersion();
            if (ver!=null && !"".equals(ver) && !"1".equals(ver)) {
                String mapUID = MapChooser.getFileUID( themap.getMapUrl() );
                for (int c=0;c<maps.size();c++) {
                    Map localMap = (Map)maps.elementAt(c);
                    if (mapUID.equals( localMap.getMapUrl() )) { // we found the map
                        if (!ver.equals( localMap.getVersion() )) { // versions do not match
                            mapsToUpdate.add(themap);
                            //client.downloadMap( MapChooser.getURL(MapChooser.getContext(url), themap.mapUrl ) ); // download 
                        }
                        break;
                    }
                }
            }
        }
        // else do nothing
        
        response();
    }

    void response() {
        results++;
        if (results==maps.size()) {
            client.kill();
            client=null;
            maps = null;
        }
    }
    
    public void downloadFinished(String mapUID) {
    
        for (int c=0;c<mapsToUpdate.size();c++) {
            Map map = (Map)mapsToUpdate.elementAt(c);
            String amapUID = MapChooser.getFileUID( map.getMapUrl() );
            if (mapUID.equals(amapUID)) {
                mapsToUpdate.removeElementAt(c);
                return;
            }
        }
    
    }
    public void onError(String string) {
        response();
    }
    public void gotImg(String url, byte[] data) { }

}
