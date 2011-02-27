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
import net.yura.mobile.io.HTTPClient;
import net.yura.mobile.io.ServiceLink.Task;
import net.yura.mobile.io.UTF8InputStreamReader;
import net.yura.mobile.logging.Logger;

/**
 * @author Yura Mamyrin
 */
public class MapServerClient extends HTTPClient implements EventListener {

    MapChooser chooser;

    public MapServerClient(MapChooser aThis) {
        chooser = aThis;

        Events.SERVER_GET_RESOURCE.subscribe(this);
    }

    protected void onError(Request request, int responseCode, Hashtable headers, Exception ex) {
        Logger.warn(ex);
    }

    protected void onResult(Request request, int responseCode, Hashtable headers, InputStream is, long length) throws IOException {

        if (request.id == Events.SERVER_GET_RESOURCE) {

            publishClientResource(request.url, is, length, false);

        }
        else {

            XMLMapAccess access = new XMLMapAccess();
            Task task = (Task)access.load( new UTF8InputStreamReader(is) );

            chooser.gotResult(task);
        }
    }

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
                    Image.saveImage(prv, bytes);
                    prv = null; // drop the small image as soon as we can

                    data = bytes.toByteArray();
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    // not sure what to do here??
                }

            }
            else {
            	data = getData(is, (int)length);
            }

            cr.setData( data );
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        Events.CLIENT_RESOURCE.publish(id, cr, this);

    }

    void makeRequest(String string) {
            makeRequest(string,null,null);
    }
    void makeRequest(String string,String a,String b) {

            Request request = new Request();
            request.url = "http://maps.domination.yura.net/xml/"+string;

            if (a!=null&&b!=null) {
                Hashtable params = new Hashtable();
                params.put(a, b);
                //request.params = params;
            }

            // TODO, should be using RiskIO to do this get
            // as otherwise it will not work with lobby
            makeRequest(request);

    }

    public void eventReceived(Event arg0, Object arg1, Object arg2) {
        if (arg0 == Events.SERVER_GET_RESOURCE) {

            String url = (String)arg1;

            // if this is a local map
            if (url.indexOf(':')<0) {
                try {
                    InputStream in = RiskUtil.openMapStream(url);
                    publishClientResource(url, in, -1, !url.startsWith("preview/") );
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            else {
                Request request = new Request();
                request.url = url;
                request.id = arg0;
                makeRequest( request );
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
