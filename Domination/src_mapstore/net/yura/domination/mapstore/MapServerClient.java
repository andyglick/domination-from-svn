package net.yura.domination.mapstore;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import net.yura.domination.mapstore.gen.XMLMapAccess;
import net.yura.mobile.io.HTTPClient;
import net.yura.mobile.io.ServiceLink.Task;
import net.yura.mobile.io.UTF8InputStreamReader;
import net.yura.mobile.logging.Logger;

/**
 * @author Yura Mamyrin
 */
public class MapServerClient extends HTTPClient {

    MapChooser chooser;

    public MapServerClient(MapChooser aThis) {
        chooser = aThis;
    }

    protected void onError(Request request, int responseCode, Hashtable headers, Exception ex) {
        Logger.warn(ex);
    }

    protected void onResult(Request request, int responseCode, Hashtable headers, InputStream is, long length) throws IOException {
        XMLMapAccess access = new XMLMapAccess();
        Task task = (Task)access.load( new UTF8InputStreamReader(is) );

        chooser.gotResult(task);
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
                request.params = params;
            }

            // TODO, should be using RiskIO to do this get
            // as otherwise it will not work with lobby
            makeRequest(request);

    }
}
