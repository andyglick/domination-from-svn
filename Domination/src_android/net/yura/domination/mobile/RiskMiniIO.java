package net.yura.domination.mobile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskIO;
import net.yura.mobile.gui.Midlet;
import net.yura.mobile.io.FileUtil;

/**
 * @author Yura Mamyrin
 */
public class RiskMiniIO implements RiskIO {

    public static String mapsdir = "file:///android_asset/maps/";
    
    public InputStream openStream(String name) throws IOException {
        // TODO, this is useless as should load a file outside the jar
        return getClass().getResourceAsStream("/"+name);
    }

    public InputStream openMapStream(String name) throws IOException {
        try {
            File userMaps = getSaveMapDir();
            File newFile = new File(userMaps, name);
            return new FileInputStream(newFile);
        }
        catch (Exception ex) {
            return FileUtil.getInputStreamFromFileConnector(mapsdir+name);
        }
    }

    public ResourceBundle getResourceBundle(Class c, String n, Locale l) {
        return ResourceBundle.getBundle(c.getPackage().getName()+"."+n, l );
    }

    public void openURL(URL url) throws Exception {
        Midlet.openURL(url.toString());
    }

    public void openDocs(String doc) throws Exception {
        Midlet.openURL(doc.toString());
    }

    public InputStream loadGameFile(String file) throws Exception {
        return FileUtil.getInputStreamFromFileConnector(file);
    }

    public void saveGameFile(String name, Object obj) throws Exception {
        FileOutputStream fileout = new FileOutputStream(name);
        ObjectOutputStream objectout = new ObjectOutputStream(fileout);
        objectout.writeObject(obj);
        objectout.close();
    }

    public OutputStream saveMapFile(String fileName) throws Exception {
        File userMaps = getSaveMapDir();
        File newFile = new File(userMaps, fileName);
        return new FileOutputStream(newFile);
    }

    public void getMap(String filename, Risk risk, Exception ex) {
        net.yura.domination.mapstore.GetMap.getMap(filename, risk, ex);
    }

    
    public static Vector getFileList(String string) {
        Vector result = new Vector();
        
        Enumeration en = FileUtil.getDirectoryFiles(mapsdir);
        while (en.hasMoreElements()) {
            String file = (String)en.nextElement();
            if (file.endsWith("."+string)) {
                result.addElement( file );
            }
        }


        String[] list = getSaveMapDir().list();
        for (int c=0;c<list.length;c++) {
            String file = list[c];
            if (file.endsWith("."+string)) {
                result.addElement( file );
            }
        }        
        
        return result;
    }
    
    
    private static File mapsDir;
    public static File getSaveMapDir() {

        if (mapsDir!=null) {
            return mapsDir;
        }

        File userHome = new File( System.getProperty("user.home") );
        File userMaps = new File(userHome, "maps");
        if (!userMaps.isDirectory() && !userMaps.mkdirs()) { // if it does not exist and i cant make it
            throw new RuntimeException("can not create dir "+userMaps);
        }

        mapsDir = userMaps;
        return userMaps;
    }
    
}
