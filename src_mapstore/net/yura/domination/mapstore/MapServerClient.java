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

    protected void onError(Request request, int responseCode, Hashtable headers, Exception ex) {
        Logger.warn(ex);
    }

    protected void onResult(Request request, int responseCode, Hashtable headers, InputStream is, long length) throws IOException {
        XMLMapAccess access = new XMLMapAccess();
        Task task = (Task)access.load( new UTF8InputStreamReader(is) );


        System.out.println("GOT TASK "+task);
    }

}
