package net.yura.domination.mapstore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import javax.microedition.lcdui.Image;
import net.yura.abba.Events;
import net.yura.abba.eventex.Event;
import net.yura.abba.eventex.EventListener;
import net.yura.abba.persistence.ClientResource;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.mapstore.gen.XMLMapAccess;
import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.components.OptionPane;
import net.yura.mobile.io.HTTPClient;
import net.yura.mobile.io.ServiceLink.Task;
import net.yura.mobile.io.UTF8InputStreamReader;
import net.yura.mobile.logging.Logger;
import net.yura.mobile.util.SystemUtil;
import net.yura.mobile.util.Url;

/**
 * @author Yura Mamyrin
 */
public class MapServerClient extends HTTPClient implements EventListener {

    public static final Object XML_REQUEST_ID = new Object();
    public static final Object MAP_REQUEST_ID = new Object();

    MapChooser chooser;
    String xmlServerURL;

    public MapServerClient(MapChooser aThis,String url) {
        chooser = aThis;
        xmlServerURL = url;

        Events.SERVER_GET_RESOURCE.subscribe(this);
    }

    public void kill() {
        super.kill();

        Events.SERVER_GET_RESOURCE.unsubscribe(this);
    }


    protected void onError(Request request, int responseCode, Hashtable headers, Exception ex) {

        // TODO make this better

        OptionPane.showMessageDialog(null, "error: "+responseCode+" "+ex+" "+request+"\n\n"+headers, "Error!", OptionPane.ERROR_MESSAGE);

        if (ex!=null) {
            Logger.warn(ex);
        }
    }

    protected void onResult(Request request, int responseCode, Hashtable headers, InputStream is, long length) throws IOException {

        if (request.id == XML_REQUEST_ID) {
            XMLMapAccess access = new XMLMapAccess();
            Task task = (Task)access.load( new UTF8InputStreamReader(is) );

System.out.println("Got XML "+task);

            chooser.gotResultXML(request.url,task);

        }
        else if (request.id == MAP_REQUEST_ID) {

            chooser.gotResultMap(request.url,is);

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
                    Image prv = Image.createImage(150, 94);
                    new Graphics2D(prv.getGraphics()).drawScaledImage(img, 0, 0, prv.getWidth(), prv.getHeight() );
                    img = null; // drop the large image as soon as we can

                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    net.yura.mobile.util.ImageUtil.saveImage(prv, bytes);
                    prv = null; // drop the small image as soon as we can

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
}
