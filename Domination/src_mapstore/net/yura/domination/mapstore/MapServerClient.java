package net.yura.domination.mapstore;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import net.yura.abba.Events;
import net.yura.abba.eventex.Event;
import net.yura.abba.eventex.EventListener;
import net.yura.abba.persistence.ClientResource;
import net.yura.domination.mapstore.gen.XMLMapAccess;
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

            ClientResource cr = new ClientResource();

            cr.setResourceId( request.url ); //  same as uid
            cr.setData( getData(is, (int)length) );

            Events.CLIENT_RESOURCE.publish(request.url, cr, this);
        }
        else {

            XMLMapAccess access = new XMLMapAccess();
            Task task = (Task)access.load( new UTF8InputStreamReader(is) );

            chooser.gotResult(task);
        }
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

            Request request = new Request();
            request.url = (String)arg1;
            request.id = arg0;

            makeRequest( request );
        }
        else {
            System.out.println("AAAAAAAAA unknown event "+arg0);
        }
    }

    public boolean isUiEvent(Event arg0) {
        return false;
    }
}
