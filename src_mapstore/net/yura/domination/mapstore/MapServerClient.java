package net.yura.domination.mapstore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Vector;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.mapstore.gen.XMLMapAccess;
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

    protected void onError(Request request, int responseCode, Hashtable headers, Exception ex) {

        // print error to console
        Logger.warn( "error: "+responseCode+" "+ex+" "+request+"\n"+headers );
        if (ex!=null) { Logger.warn(ex); }

        // show error dialog to the user
        chooser.onError("error: "+responseCode+(ex!=null?" "+ex:"") );
    }

    protected void onResult(Request request, int responseCode, Hashtable headers, InputStream is, long length) throws IOException {

        if (request.id == XML_REQUEST_ID) {
            XMLMapAccess access = new XMLMapAccess();
            Task task = (Task)access.load( new UTF8InputStreamReader(is) );

//System.out.println("Got XML "+task);

            chooser.gotResultXML(request.url,task);

        }
        else if (request.id == IMG_REQUEST_ID) {
            
            chooser.gotImg(request.url, SystemUtil.getData(is, (int)length) );
        }
        else if (request.id == MAP_REQUEST_ID) {

            gotResultMap(request.url,is);

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

System.out.println("Make Request: "+request);

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
