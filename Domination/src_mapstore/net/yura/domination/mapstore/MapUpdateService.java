package net.yura.domination.mapstore;

import java.util.Hashtable;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import net.yura.mobile.gui.Font;
import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.border.MatteBorder;
import net.yura.mobile.io.ServiceLink.Task;

/**
 * @author Yura
 */
public class MapUpdateService extends Observable implements MapServerListener {

    static MapUpdateService updateService;
    
    Vector maps;
    int results;
    
    Vector mapsToUpdate = new Vector();
    MapServerClient client;

    private MapUpdateService() { }
    public static MapUpdateService getInstance() {
        if (updateService==null) {
            updateService = new MapUpdateService();
        }
        return updateService;
    }

    void notifyListeners() {
        setChanged();
        notifyObservers( Integer.valueOf( mapsToUpdate.size() ) );
    }

    public synchronized void addObserver(Observer o) {
        super.addObserver(o);
        o.update(this, Integer.valueOf( mapsToUpdate.size() ) );
    }

    public void init(Vector mapsUIDs) {
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
                            notifyListeners();
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
                notifyListeners();
                return;
            }
        }
    
    }
    public void onError(String string) {
        response();
    }
    public void gotImg(String url, byte[] data) { }

    
    

    /**
     * TODO not sure where this method should be, but prob not here!!
     */
    public static void paintBadge(Graphics2D g,String badge,MatteBorder border) {
        
        if (!"0".equals(badge)) {
        
            Font font = g.getFont();
            
            int tw = font.getWidth(badge.length()==1?" "+badge:badge); // make sure its not too thin
            int th = font.getHeight();

            int l,r,t,b;
            if (border==null) {
                l=r=t=b=3;
            }
            else {
                l=border.getLeft();
                r=border.getRight();
                t=border.getTop();
                b=border.getBottom();
            }

            int x,y,w,h;

            w = l+r+tw;
            h = t+b+th;
            x = - w;
            y = 0;

            int[] clip = g.getClip();
            g.setClip(x, y, w, h);

            if (border==null) {
                g.setColor(0xFFFF0000);
                g.fillOval(x, y, w, h);
            }
            else {
                g.translate( x+border.getLeft() , y+border.getTop());
                border.paintBorder(null, g, w-border.getLeft()-border.getRight(), h-border.getTop()-border.getBottom());
                g.translate( -x-border.getLeft() , -y-border.getTop());
            }

            g.setColor(0xFFFFFFFF);
            g.drawString(badge, 1+ x + (w-font.getWidth(badge))/2, 1+ y + (h-font.getHeight())/2);
            
            g.setClip(clip);
        }

    }
}
