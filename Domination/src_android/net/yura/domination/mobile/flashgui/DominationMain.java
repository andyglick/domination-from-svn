package net.yura.domination.mobile.flashgui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.domination.mapstore.MapChooser;
import net.yura.domination.mapstore.MapUpdateService;
import net.yura.domination.mobile.MiniUtil;
import net.yura.domination.mobile.RiskMiniIO;
import net.yura.grasshopper.SimpleBug;
import net.yura.mobile.gui.DesktopPane;
import net.yura.mobile.gui.Midlet;
import net.yura.mobile.gui.components.OptionPane;
import net.yura.mobile.gui.plaf.SynthLookAndFeel;
import net.yura.mobile.gui.plaf.nimbus.NimbusLookAndFeel;
import net.yura.swingme.core.CoreUtil;

public class DominationMain extends Midlet {

    public static final String product = "MiniFlashGUI";
    public static final String version = System.getProperty("versionCode");
    
    public DominationMain() {

        // IO depends on this, so we need to do this first
        RiskUtil.streamOpener = new RiskMiniIO();

        // get version from AndroidManifest.xml
        String versionName = System.getProperty("versionName");
        Risk.RISK_VERSION = versionName!=null ? versionName : "?me4se?";
        
        try {

            SimpleBug.initLogFile( RiskUtil.GAME_NAME , Risk.RISK_VERSION+" "+product+" "+version , TranslationBundle.getBundle().getLocale().toString() );

            CoreUtil.setupLogging();

            if ( "true".equals( System.getProperty("debug") ) ) {
                Logger.getLogger("").addHandler( new Handler() {
                    boolean open;
                    @Override
                    public void publish(LogRecord record) {
                        if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
                            if (!open) {
                                open = true;
                                try {
                                    OptionPane.showMessageDialog(null, record.getMessage()+" "+record.getThrown(), "WARN", OptionPane.WARNING_MESSAGE);
                                }
                                catch(Exception ex) { ex.printStackTrace(); }
                            }
                        }
                    }

                    @Override public void flush() { }
                    @Override public void close() { }
                } );
            }
            
            // if we want to see DEBUG, default is INFO
            //java.util.logging.Logger.getLogger("").setLevel(java.util.logging.Level.ALL);

        }
        catch (Throwable th) {
            System.out.println("Grasshopper not loaded");
            th.printStackTrace();
        }

    }

    @Override
    public void initialize(DesktopPane rootpane) {

        SynthLookAndFeel synth;
        
        try {
            synth = (SynthLookAndFeel)Class.forName("net.yura.android.plaf.AndroidLookAndFeel").newInstance();
        }
        catch (Exception ex) {
            synth = new NimbusLookAndFeel();
        }
        
        try {
            synth.load( Midlet.getResourceAsStream("/dom_synth.xml") );
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        rootpane.setLookAndFeel( synth );

        MapChooser.loadThemeExtension(); // this has theme elements used inside AND outside of the MapChooser




        Risk risk = new Risk();
        MiniFlashRiskAdapter adapter = new MiniFlashRiskAdapter(risk);

        adapter.openMainMenu();


        
        new Thread() {
            @Override
            public void run() {
                MapUpdateService.getInstance().init( MiniUtil.getFileList("map"), MapChooser.MAP_PAGE );
            }
        }.start();

        
        //risk.parser("newgame");
        //risk.parser("newplayer ai hard blue bob");
        //risk.parser("newplayer ai hard red fred");
        //risk.parser("newplayer ai hard green greg");
        //risk.parser("startgame domination increasing");

        
        try {
            //File saves = new File( net.yura.android.AndroidMeApp.getIntance().getFilesDir() ,"saves");
            //File sdsaves = new File("/sdcard/Domination-saves");
            //copyFolder(saves, sdsaves);
            //copyFolder(sdsaves, saves);
            //System.out.println("files"+ Arrays.asList( saves.list() ) );
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static void copyFolder(File src, File dest) throws IOException{
     
            if(src.isDirectory()){
     
                    //if directory not exists, create it
                    if(!dest.exists()){
                       dest.mkdir();
                       System.out.println("Directory copied from " 
                                  + src + "  to " + dest);
                    }
     
                    //list all the directory contents
                    String files[] = src.list();
     
                    for (String file : files) {
                       //construct the src and dest file structure
                       File srcFile = new File(src, file);
                       File destFile = new File(dest, file);
                       //recursive copy
                       copyFolder(srcFile,destFile);
                    }
     
            }else{
                    //if file, then copy it
                    //Use bytes stream to support all file types
                    InputStream in = new FileInputStream(src);
                    OutputStream out = new FileOutputStream(dest); 
     
                    byte[] buffer = new byte[1024];
     
                    int length;
                    //copy the file content in bytes 
                    while ((length = in.read(buffer)) > 0){
                       out.write(buffer, 0, length);
                    }
     
                    in.close();
                    out.close();
                    System.out.println("File copied from " + src + " to " + dest);
            }
    }
}
