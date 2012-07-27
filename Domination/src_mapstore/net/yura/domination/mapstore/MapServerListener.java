package net.yura.domination.mapstore;

import net.yura.mobile.io.ServiceLink.Task;

/**
 * @author Yura Mamyrin
 */
public interface MapServerListener {

    public void gotResultXML(String url, Task task);
    public void onXMLError(String string);

    public void downloadFinished(String mapUID);    
    public void gotImgFromServer(Object param,String url,byte[] data);
    public void onDownloadError(String string); // (file and image errors here)

}
