package net.yura.domination.mobile;

import java.io.File;
import java.util.Enumeration;
import java.util.Vector;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.core.RiskGame;
import net.yura.mobile.gui.components.OptionPane;
import net.yura.mobile.io.FileUtil;

public class MiniUtil {

    public static void showAbout() {

    }

    public static Vector getFileList(String string) {
        Vector result = new Vector();
        
        Enumeration en = FileUtil.getDirectoryFiles(RiskMiniIO.mapsdir);
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
    
    private static File savesDir;
    public static File getSaveGameDir() {

        if (savesDir!=null) {
            return savesDir;
        }

        File userHome = new File( System.getProperty("user.home") );
        File userMaps = new File(userHome, "saves");
        if (!userMaps.isDirectory() && !userMaps.mkdirs()) { // if it does not exist and i cant make it
            throw new RuntimeException("can not create dir "+userMaps);
        }

        savesDir = userMaps;
        return userMaps;
    }

    public static String getSaveGameDirURL() {
        return FileUtil.ROOT_PREX + getSaveGameDir().toString() +"/";
    }

    public static String getSaveGameName(RiskGame game) {
        String file = game.getMapFile();
        if (file.endsWith(".map")) {
            file = file.substring(0, file.length() - 4);
        }
        return file;
    }
    
    public static void openHelp() {
        try {
            RiskUtil.openDocs("help/index.htm");
        }
        catch(Exception e) {
            OptionPane.showMessageDialog(null,"Unable to open manual: "+e.getMessage(),"Error", OptionPane.ERROR_MESSAGE);
        }
    }
    
}
