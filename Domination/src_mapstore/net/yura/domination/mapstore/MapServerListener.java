package net.yura.domination.mapstore;

import net.yura.mobile.io.ServiceLink.Task;

/**
 * @author Yura Mamyrin
 */
public interface MapServerListener {

    public void onError(String string);
    public void gotResultXML(String url, Task task);
    public void downloadFinished(String mapUID);
    public void gotImg(String url,byte[] data);
    
}
