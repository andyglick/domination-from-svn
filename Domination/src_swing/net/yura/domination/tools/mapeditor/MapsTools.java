package net.yura.domination.tools.mapeditor;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import net.yura.domination.engine.RiskUIUtil;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.mapstore.Map;
import net.yura.domination.mapstore.MapChooser;
import net.yura.domination.mapstore.gen.XMLMapAccess;
import net.yura.mobile.io.ServiceLink.Task;
import net.yura.mobile.io.UTF8InputStreamReader;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

/**
 * @author Yura
 */
public class MapsTools {
    
    public static final String MAPS_XML_FILE = "maps.xml";

    public static Vector loadMaps() {

        try {

            File mapsDir = RiskUIUtil.getSaveMapDir();
            File xml = new File(mapsDir,MAPS_XML_FILE);

            if (xml.exists()) {

                XMLMapAccess access = new XMLMapAccess();
                
                Task task = (Task)access.load( new FileReader(xml) ); // TODO should we need to say its UTF-8 ???
                
                // load big XML file
                Vector maps = (Vector)task.getObject();
        
                return maps;
            }
            else {
                // only make a blank empty vector if there is no current file
                return new Vector();
            }
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        
        
    }
    
    public static void saveMaps(Vector maps) {
        
        try {
            
            Task task = new Task("categories", maps);
        
            File mapsDir = RiskUIUtil.getSaveMapDir();
            File xml = new File(mapsDir,MAPS_XML_FILE);

            XMLMapAccess access = new XMLMapAccess();
            
            access.save( new FileOutputStream(xml) , task);
            
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }
    
    public static void saveMapsHTML(Vector maps) {
        
        
        
        String a = "<div class='thumbnail'><a href=\"maps/haiti.zip\">"
                + "<img src=\"images/maps/haiti.jpg\" border=\"1\" width=\"150\" height=\"94\"><br>"
                + "29. Haiti Map</a><br> by Louis-Pierre Charbonneau </div>";

    }

    public static Map findMap(Vector maps,String fileName) {

        // find entry fot this map
        for (int c=0;c<maps.size();c++) {

            Map map = (Map)maps.elementAt(c);

            if (fileName.equals( map.getMapUrl() )) {

                // yay we found the correct map
                return map;
            }
        }
    
        return null;
    }

    public static String getSafeMapID(String file) {

        if (file.toLowerCase().endsWith(".map")) {
            file = file.substring(0, file.length()-4);
        }
        
        int i;
        while ((i=file.indexOf(' '))>=0) {
            file = file.substring(0, i)+
            ((i<(file.length()-1) )?Character.toUpperCase(file.charAt(i+1))+file.substring(i+2):"");
        }
        
        return file;
    }
    
    public static final String PREVIEW = "preview";
    
    public static String makePreview(String mapUID, BufferedImage prvimg,File previewDir, String format) throws Exception {

            // we do NOT have a preview, we need to generate one
            String prv = getSafeMapID(mapUID)+"."+format;

            boolean done = ImageIO.write( prvimg , format , new File(previewDir,prv) );

            if (!done) throw new Exception("not done "+format);

            return PREVIEW+"/"+prv;

    }
    
    
    static String publish(Map map, String[] myCategoriesIds) {
        
            try {     

                File mapsDir = RiskUIUtil.getSaveMapDir();
                
                File zipFile = new File(mapsDir, getSafeMapID(map.getMapUrl())+".zip" );
                if (!zipFile.exists()) {
                    
                    Hashtable info = RiskUtil.loadInfo( map.getMapUrl() , false);
                    
                    String[] files = new String[ info.get("prv")==null?4:5 ];
                    files[0] = map.getMapUrl();
                    files[1] = (String)info.get("crd");
                    files[2] = (String)info.get("pic");
                    files[3] = (String)info.get("map");
                    if (info.get("prv")!=null) {
                        files[4] = PREVIEW+"/"+(String)info.get("prv");
                    }
                    
                    makeZipFile(zipFile, mapsDir, files);
                    
                }
                
                // create the multipart request and add the parts to it
                MultipartEntity requestContent = new MultipartEntity();
                
                requestContent.addPart("first_name", new StringBody( map.getAuthorName() ));
                requestContent.addPart("email", new StringBody( map.getAuthorId() ));
                
                requestContent.addPart("name", new StringBody( map.getName() ));
                requestContent.addPart("description", new StringBody( map.getDescription() ));
                
                for (int c=0;c<myCategoriesIds.length;c++) {
                    requestContent.addPart("categories", new StringBody( myCategoriesIds[c] ) );
                }
                
                requestContent.addPart("mapZipFile", new FileBody(zipFile));

                return doPost( "http://maps.yura.net/upload-unauthorised", requestContent );

            }
            catch (Exception ex) {
                throw new RuntimeException(ex);
            }
    }
    
    public static void makeZipFile(File zipFile,File root, String[] files) {
        
        
        byte[] buf = new byte[1024];

        try {
            // Create the ZIP file
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));

            // Compress the files
            for (int i=0; i<files.length; i++) {
                FileInputStream in = new FileInputStream( new File(root, files[i] ));

                // Add ZIP entry to output stream.
                out.putNextEntry(new ZipEntry( files[i] ));

                // Transfer bytes from the file to the ZIP file
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                // Complete the entry
                out.closeEntry();
                in.close();
            }

            // Complete the ZIP file
            out.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        
    }
    
    
        public static String doPost(String url, MultipartEntity requestContent) throws IOException {

        	StringBuffer buffer = new StringBuffer();

            	URLConnection conn = new URL(url).openConnection();
        	conn.setDoOutput(true);

                org.apache.http.Header contentType = requestContent.getContentType();
                conn.setRequestProperty(contentType.getName(), contentType.getValue());

                // this seems to be null and does not seem to be needed
                //org.apache.http.Header contentEncoding = requestContent.getContentEncoding();
                //conn.setRequestProperty(contentEncoding.getName(), contentEncoding.getValue());

                conn.setRequestProperty("Content-Length", String.valueOf(requestContent.getContentLength()) );

                OutputStream out = conn.getOutputStream();
                requestContent.writeTo( out );
        	out.close();

        	// Get the response
        	BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        	String line;
        	while ((line = rd.readLine()) != null) {
			buffer.append(line);
                        buffer.append("\n");
        	}
        	rd.close();
		return buffer.toString();

    }

    public static Vector getCategories() {
        try {
            URLConnection conn = new URL( MapChooser.CATEGORIES_PAGE ).openConnection();
            XMLMapAccess access = new XMLMapAccess();
            InputStream in = conn.getInputStream();
            net.yura.mobile.io.ServiceLink.Task result = (net.yura.mobile.io.ServiceLink.Task)access.load( new UTF8InputStreamReader( in ) );
            in.close();
            return (Vector)result.getObject();
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
