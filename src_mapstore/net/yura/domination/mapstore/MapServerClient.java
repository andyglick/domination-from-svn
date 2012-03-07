package net.yura.domination.mapstore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Hashtable;
import java.util.Vector;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.mapstore.gen.XMLMapAccess;
import net.yura.mobile.gui.Midlet;
import net.yura.mobile.io.FileUtil;
import net.yura.mobile.io.HTTPClient;
import net.yura.mobile.io.ServiceLink.Task;
import net.yura.mobile.io.UTF8InputStreamReader;
import net.yura.mobile.logging.Logger;
import net.yura.mobile.util.SystemUtil;

/**
 * @author Yura Mamyrin
 */
public class MapServerClient extends HTTPClient {

    public static final Object XML_REQUEST_ID = new Object();
    public static final Object MAP_REQUEST_ID = new Object();
    public static final Object IMG_REQUEST_ID = new Object();

    MapServerListener chooser;

    public MapServerClient(MapServerListener aThis) {
        chooser = aThis;
    }

    public void kill() {
        chooser = null;
        if (downloads.isEmpty()) {
            super.kill();
        }
    }
    
    protected void onError(Request request, int responseCode, Hashtable headers, Exception ex) {

        MapServerListener ch = this.chooser;
        
        if (request.id == MAP_REQUEST_ID && getMapDownload(request.url).ignoreErrorInDownload(request.url)) {
            return;
        }

        // print error to console
        Logger.warn( "error: "+responseCode+" "+ex+" "+request+"\n"+headers );
        if (ex!=null) { Logger.warn(ex); } else { Logger.dumpStack(); }

        // show error dialog to the user
        if (ch!=null) {
            ch.onError("error: "+responseCode+(ex!=null?" "+ex:"") );
        }
    }

    protected void onResult(Request request, int responseCode, Hashtable headers, InputStream is, long length) throws IOException {

        MapServerListener ch = this.chooser;

        if (request.id == XML_REQUEST_ID) {
            XMLMapAccess access = new XMLMapAccess();
            Task task = (Task)access.load( new UTF8InputStreamReader(is) );

            // HACK!!! there is a massive bug in Android where if you dont do a extra read after reading all the data
            // HACK!!! your next http request will fail! http://code.google.com/p/android/issues/detail?id=7786
            // HACK!!! this bug is found on Android 1.6, it seems to be fixed on Android 2.3.3
            if (Midlet.getPlatform()==Midlet.PLATFORM_ANDROID) {
                is.read();
            }

//System.out.println("Got XML "+task);

            if (ch!=null) {
                ch.gotResultXML(request.url,task);
            }
        }
        else if (request.id == IMG_REQUEST_ID) {
            
            if (ch!=null) {
                ch.gotImg(request.url, SystemUtil.getData(is, (int)length) );
            }
        }
        else if (request.id == MAP_REQUEST_ID) {

            getMapDownload(request.url).gotRes(request.url, is );

        }
        else {
            System.err.println("[MapServerClient] unknown id "+request.id);
        }
    }

    void makeRequestXML(String string,String a,String b) {
        Hashtable params = null;
        if (a!=null&&b!=null) {
            params = new Hashtable();
            params.put(a, b);
        }
        makeRequest( string , params, XML_REQUEST_ID);
    }

    void makeRequest(String url,Hashtable params,Object type) {

            Request request = new Request();
            request.url = url;
            request.params = params;
            request.id = type;

Logger.info("Make Request: "+request);

            // TODO, should be using RiskIO to do this get
            // as otherwise it will not work with lobby
            makeRequest(request);

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

    MapDownload getMapDownload(final String url) {
        for (int c=0;c<downloads.size();c++) {
            MapDownload download = (MapDownload)downloads.elementAt(c);
            if ( download.hasUrl(url) ) {
                return download;
            }
        }
        throw new IllegalArgumentException("no download for url "+url);
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
        boolean error = false;
        
        MapDownload(String url) {
            
            mapUID = MapChooser.getFileUID(url);
            
            mapContext = url.substring(0, url.length() - mapUID.length() );
            
            downloadFile( mapUID );
        }
        
        public String toString() {
            return mapUID;
        }
        
        final void downloadFile(String fileName) {
            // this does not support spaces in file names
            //String url = mapContext + fileName;
            
            String url = getURL(mapContext, fileName);

            urls.addElement(url);

            makeRequest( url, null, MapServerClient.MAP_REQUEST_ID );
        }
        
        boolean hasUrl(String url) {
            return urls.contains(url);
        }

        private void gotResponse(String url) {
            urls.removeElement(url);
            if (urls.isEmpty()) {
                downloads.removeElement(this);

                MapUpdateService.getInstance().downloadFinished(mapUID);
                
                MapServerListener ch = chooser;
                
                if (ch!=null) {
                    if (!error) {
                        ch.downloadFinished(mapUID);
                    }
                }
                else {
                    kill();
                }
            }
        }
        
        private void gotRes(String url, InputStream is) {
            // this does not support spaces in file names
            //String fileName = url.substring(mapContext.length());
            
            String fileName = getPath(mapContext, url);
            
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
            }
            catch (Exception ex) {
                RiskUtil.printStackTrace(ex);
                // TODO what to do here?
                // TODO what if disk is full??
            }
            finally {
                FileUtil.close(is);
                FileUtil.close(out);
                
                gotResponse(url);
            }

        }

        private boolean ignoreErrorInDownload(String url) {
            //String fileName = url.substring(mapContext.length());

            String fileName = getPath(mapContext, url);
            
            boolean fileExists = MapChooser.fileExists(fileName);
            
            if (!fileExists) {
                error = true;
            }
            
            gotResponse(url);

            // we got a error, but we already have this file, so ignore the error
            return fileExists;
            
        }
    }
    
    
    
    
    public static String getURL(String context, String path) {
        // as we downloading a file, we need to have the correct encoding! (Hello World -> Hello%20World)
        try {
            return new URI(context).resolve( new URI(null, null, path, null) ).toASCIIString();
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String getPath(String context, String url) {
        // we need to convert this url back into a normal path (Hello%20world -> Hello World)
        try {
            return new URI(context).relativize( new URI(url) ).getPath();
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
}
