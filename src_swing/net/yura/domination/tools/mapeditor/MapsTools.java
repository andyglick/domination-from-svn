/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.yura.domination.tools.mapeditor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.net.URI;
import java.util.Vector;
import net.yura.domination.engine.RiskUIUtil;
import net.yura.domination.mapstore.Map;
import net.yura.domination.mapstore.gen.XMLMapAccess;

/**
 *
 * @author Administrator
 */
public class MapsTools {
    
    public static final String MAPS_XML_FILE = "maps.xml";

    public static Vector loadMaps() {

        try {

            File mapsDir = new File(new URI(RiskUIUtil.mapsdir.toString()));
            File xml = new File(mapsDir,MAPS_XML_FILE);

            if (xml.exists()) {

                XMLMapAccess access = new XMLMapAccess();
                
                // load big XML file
                Vector maps = (Vector)access.load( new FileReader(xml) ); // TODO should we need to say its UTF-8 ???
        
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
        
            File mapsDir = new File(new URI(RiskUIUtil.mapsdir.toString()));
            File xml = new File(mapsDir,MAPS_XML_FILE);

            XMLMapAccess access = new XMLMapAccess();
            
            access.save( new FileOutputStream(xml) , maps);
            
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }

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
}
