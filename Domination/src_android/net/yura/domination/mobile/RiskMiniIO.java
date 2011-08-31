/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.yura.domination.mobile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import net.yura.domination.engine.RiskIO;
import net.yura.mobile.gui.Midlet;
import net.yura.mobile.io.FileUtil;

/**
 * @author Administrator
 */
public class RiskMiniIO implements RiskIO {

    public InputStream openStream(String name) throws IOException {
        // TODO, this is useless as should load a file outside the jar
        return getClass().getResourceAsStream("/"+name);
    }

    public InputStream openMapStream(String name) throws IOException {
        return FileUtil.getInputStreamFromFileConnector("file:///android_asset/maps/"+name);
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public OutputStream saveMapFile(String fileName) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
