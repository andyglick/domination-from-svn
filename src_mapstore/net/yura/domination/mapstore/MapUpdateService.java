package net.yura.domination.mapstore;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import net.yura.mobile.gui.Font;
import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.border.Border;
import net.yura.mobile.io.ServiceLink.Task;
import net.yura.mobile.util.Url;

/**
 * @author Yura
 */
public class MapUpdateService extends Observable implements MapServerListener {

    static MapUpdateService updateService;
    
    Vector maps;
    //int results;
    
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
        
        String url = MapChooser.MAP_PAGE;
        
        for (int c=0;c<mapsUIDs.size();c++) {
            String uid = (String)mapsUIDs.elementAt(c);
            Map map = MapChooser.createMap(uid);
            maps.add( map );
            //client.makeRequestXML( MapChooser.MAP_PAGE,"mapfile",uid );
            
            url = url + (url.indexOf('?')<0?'?':'&') + Url.encode("mapfile")+"="+Url.encode(uid);
        }
        
        client.makeRequestXML( url,null,null );
    }

    public void gotResultXML(String url, Task task) {

        Hashtable map = (Hashtable)task.getObject();
        Vector gotMaps = (Vector)map.get("maps");

        //try { new net.yura.domination.mapstore.gen.XMLMapAccess().save(System.out, map); } catch (Exception ex) { ex.printStackTrace(); }
        
        for (int c=0;c<maps.size();c++) {
            Map localMap = (Map)maps.elementAt(c);
            List theMaps = new ArrayList(1);

            for (int i=0;i<gotMaps.size();i++) {
                Map themap = (Map)gotMaps.elementAt(i);
                String mapUID = MapChooser.getFileUID( themap.getMapUrl() );
                if (mapUID.equals( localMap.getMapUrl() )) { // we found the map
                    theMaps.add(themap);
                    // we do NOT break, just in case there is more then one
                }
            }
            
            if (theMaps.size()==1) {
                Map themap = (Map)theMaps.get(0);
                String ver = themap.getVersion();
                if (ver!=null && !"".equals(ver) && !"1".equals(ver) && !ver.equals( localMap.getVersion() ) ) { // versions do not match, and update is needed
                    mapsToUpdate.add(themap);
                    notifyListeners();
                    //client.downloadMap( MapChooser.getURL(MapChooser.getContext(url), themap.mapUrl ) ); // download 
                }
            }
            // else if 0 then we did not find it, or if more then 1 then some error has happened
            
        }
        
        response();
    }

    void response() {
        //results++;
        //if (results==maps.size()) {
            client.kill();
            client=null;
            maps = null;
        //}
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
    public static void paintBadge(Graphics2D g,String badge,Border border) {
        
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
