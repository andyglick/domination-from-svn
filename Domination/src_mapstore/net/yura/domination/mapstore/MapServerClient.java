package net.yura.domination.mapstore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.lcdui.Image;
import net.yura.abba.Events;
import net.yura.abba.eventex.Event;
import net.yura.abba.eventex.EventListener;
import net.yura.abba.persistence.ClientResource;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.mapstore.gen.XMLMapAccess;
import net.yura.mobile.io.FileUtil;
import net.yura.mobile.io.HTTPClient;
import net.yura.mobile.io.ServiceLink.Task;
import net.yura.mobile.io.UTF8InputStreamReader;
import net.yura.mobile.logging.Logger;
import net.yura.mobile.util.ImageUtil;
import net.yura.mobile.util.SystemUtil;
import net.yura.mobile.util.Url;

/**
 * @author Yura Mamyrin
 */
public class MapServerClient extends HTTPClient implements EventListener {

    public static final Object XML_REQUEST_ID = new Object();
    public static final Object MAP_REQUEST_ID = new Object();

    MapServerListener chooser;
    String xmlServerURL;

    public MapServerClient(MapServerListener aThis,String url) {
        chooser = aThis;
        xmlServerURL = url;

        Events.SERVER_GET_RESOURCE.subscribe(this);
    }

    public void kill() {
        super.kill();

        Events.SERVER_GET_RESOURCE.unsubscribe(this);
    }


    protected void onError(Request request, int responseCode, Hashtable headers, Exception ex) {

        Logger.warn( "error: "+responseCode+" "+ex+" "+request+"\n\n"+headers );
        if (ex!=null) {
            Logger.warn(ex);
        }

        chooser.onError("error: "+responseCode);
    }

    protected void onResult(Request request, int responseCode, Hashtable headers, InputStream is, long length) throws IOException {

        if (request.id == XML_REQUEST_ID) {
            XMLMapAccess access = new XMLMapAccess();
            Task task = (Task)access.load( new UTF8InputStreamReader(is) );

System.out.println("Got XML "+task);

            chooser.gotResultXML(request.url,task);

        }
        else if (request.id == MAP_REQUEST_ID) {

            gotResultMap(request.url,is);

        }
        else {

            publishClientResource( (String)request.id, is, length, false);
        }
    }

    /**
     * This is used for thumbnails of the maps previews
     * these images are cached on the client
     * the image is also re-encoded into a small image if needed
     */
    private void publishClientResource(String id,InputStream is,long length,boolean reEncode) {

        ClientResource cr = new ClientResource();

        cr.setResourceId( id ); //  same as uid
        try {
        	byte[] data=null;

            if (reEncode) {

            	System.out.println("#################################### am going to re-encode img: "+id);

                try {
                    Image img = Image.createImage(is);                    
                    img = ImageUtil.scaleImage(img, 150, 94);

                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    ImageUtil.saveImage(img, bytes);
                    img = null; // drop the small image as soon as we can

                    data = bytes.toByteArray();
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    // not sure what to do here??
                }

            }
            else {
            	data = SystemUtil.getData(is, (int)length);
            }

            cr.setData( data );
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        Events.CLIENT_RESOURCE.publish(id, cr, this);

    }

    void makeRequestXML(String string) {
            makeRequestXML(string,null,null);
    }
    void makeRequestXML(String string,String a,String b) {
        Hashtable params = null;
        if (a!=null&&b!=null) {
            params = new Hashtable();
            params.put(a, b);
        }
        makeRequest( xmlServerURL+string , params, XML_REQUEST_ID);
    }

    void makeRequest(String url,Hashtable params,Object type) {

            Request request = new Request();
            request.url = url;
            request.params = params;
            request.id = type;

System.out.println("Make Request: "+request);

            // TODO, should be using RiskIO to do this get
            // as otherwise it will not work with lobby
            makeRequest(request);

    }

    public void eventReceived(final Event arg0, final Object arg1, final Object arg2) {
        if (arg0 == Events.SERVER_GET_RESOURCE) {

            String url = (String)arg1;

            // if this is a local map
            if (url.indexOf(':')<0 && !url.startsWith("/")) {
                try {
                    InputStream in = RiskUtil.openMapStream(url);
                    publishClientResource(url, in, -1, !url.startsWith("preview/") );
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            else {

                if (url.startsWith("/")) {

                    Url surl = new Url(xmlServerURL);
                    surl.setPath(url.substring(1));
                    surl.setQuery("");

                    url = surl.toString();
                }

                makeRequest( url,null,arg1 );
            }
        }
        else {
            System.err.println("AAAAAAAAA unknown event "+arg0);
        }
    }

    //@Override
    public boolean isUiEvent(Event event, Object message) {
        // TODO Auto-generated method stub
        return false;
    }
    
    
    
    
    
    
    Vector downloads = new Vector();
    
    public void downloadMap(String fullMapUrl) {
        
        MapDownload download = new MapDownload(fullMapUrl);

        downloads.addElement(download); // TODO thread safe??
    }
    
    public boolean isDownloading(String mapUID) {
        for (int c=0;c<downloads.size();c++) {
            MapDownload download = (MapDownload)downloads.elementAt(c);
            if ( download.mapUID.equals(mapUID) ) {
                return true;
            }
        }
        return false;
    }

    void gotResultMap(final String url,java.io.InputStream is) {
        for (int c=0;c<downloads.size();c++) {
            MapDownload download = (MapDownload)downloads.elementAt(c);
            if ( download.hasUrl(url) ) {
                download.gotRes(url,is);
                return;
            }
        }
    }
    
    private static void saveFile(InputStream is,OutputStream out) throws IOException {

        int COPY_BLOCK_SIZE=1024;

        byte[] data = new byte[COPY_BLOCK_SIZE];
        int i = 0;
        while( ( i = is.read(data,0,COPY_BLOCK_SIZE ) ) != -1  ) {
            out.write(data,0,i);
        }

    }
    
    
    class MapDownload {

        String mapUID;
        String mapContext;
        Vector urls = new Vector();
        
        MapDownload(String url) {
            
            mapUID = MapChooser.getFileUID(url);
            
            mapContext = url.substring(0, url.length() - mapUID.length() );
            
            downloadFile( mapUID );
        }
        
        public String toString() {
            return mapUID;
        }
        
        final void downloadFile(String fileName) {
            
            String url = mapContext + fileName;
            
            urls.addElement(url);

            makeRequest( url, null, MapServerClient.MAP_REQUEST_ID );
        }
        
        boolean hasUrl(String url) {
            return urls.contains(url);
        }

        private void gotRes(String url, InputStream is) {
            
            urls.removeElement(url);
            
            String fileName = url.substring(mapContext.length());

            OutputStream out = null;
            try {
                out = RiskUtil.streamOpener.saveMapFile(fileName);

                saveFile(is, out);

                if (fileName.endsWith(".map")) {

                    Hashtable info = RiskUtil.loadInfo(fileName, false);

                    // {prv=ameroki.jpg, pic=ameroki_pic.png, name=Ameroki Map, crd=ameroki.cards, map=ameroki_map.gif, comment=map: ameroki.map blah... }

                    // files to download
                    String pic = (String)info.get("pic");
                    String crd = (String)info.get("crd");
                    String map = (String)info.get("map");
                    String prv = (String)info.get("prv");

                    downloadFile( pic );
                    downloadFile( crd );
                    downloadFile( map );
                    if (prv!=null) {
                        downloadFile( "preview/"+prv );
                    }

                }

                if (urls.isEmpty()) {
                    
                    downloads.removeElement(this);

                    chooser.downloadFinished(mapUID);
                }

            }
            catch (Exception ex) {
                ex.printStackTrace();
                // TODO what to do here?
                // TODO what if disk is full??
            }
            finally {
                FileUtil.close(is);
                FileUtil.close(out);
            }
            
            
        }
    }
    
}
