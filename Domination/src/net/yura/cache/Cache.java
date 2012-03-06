package net.yura.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.logging.Level; // as this class is only used on j2se and android, we should use proper logging
import java.util.logging.Logger;

/**
 * @author Yura Mamyrin
 */
public class Cache {
    
    public static final boolean DEBUG = false;
    File cacheDir;
    
    public Cache() {
        
        
        String tmpDir = System.getProperty("java.io.tmpdir");
        
        String appName = "net.yura.domination"; // TODO fix
        
        cacheDir = new File(new File(tmpDir),appName+".cache");
        
        if (!cacheDir.isDirectory() && !cacheDir.mkdirs()) {
            throw new RuntimeException("can not make cache dir: "+cacheDir);
        }

        if (DEBUG) {
            System.out.println("[yura.net Cache] starting "+cacheDir);
        }
        
    }
    
    private File getFileName(String uid) {
        try {
            String fileName = URLEncoder.encode(uid, "UTF-8");
            return new File(cacheDir, fileName);
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        
    }
    
    public void put(String key, byte[] value) {
        File file = getFileName(key);
        if (file.exists()) {
            System.err.println("[yura.net Cache] already has file: "+key);
        }
        else {
            try {
                
                if (DEBUG) {
                    System.out.println("[yura.net Cache] saving to cache: "+key);
                }
                
                FileOutputStream out = new FileOutputStream(file);
                out.write(value);
                out.close();
            }
            catch (Exception ex) {
                if (file.exists()) {
                    file.delete();
                }
                Logger.getLogger(Cache.class.getName()).log(Level.WARNING, "failed to save data to file: "+key , ex);
            }
        }
        
    }
    
    public InputStream get(String key) {
        File file = getFileName(key);
        if (file.exists()) {
            try {
                if (DEBUG) {
                    System.out.println("[yura.net Cache] getting from cache: "+key);
                }
                
                file.setLastModified(System.currentTimeMillis());
                return new FileInputStream(file);
            }
            catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        else {
            if (DEBUG) {
                System.out.println("[yura.net Cache] key not found: "+key);
            }
        }
        return null;
    }

    public boolean containsKey(String key) {
        File file = getFileName(key);
        return file.exists();
    }

}
