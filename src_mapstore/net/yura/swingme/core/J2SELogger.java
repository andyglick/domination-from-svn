package net.yura.swingme.core;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Yura Mamyrin
 */
public class J2SELogger extends net.yura.mobile.logging.Logger {

    protected synchronized void log(String message, int level) {
        Logger.getLogger(J2SELogger.class.getName()).log( getLevel(level),message);
    }

    protected synchronized void log(Throwable throwable, int level) {
        Logger.getLogger(J2SELogger.class.getName()).log( getLevel(level),null,throwable);
    }
    
    private static Level getLevel(int level) {
        switch (level) {
            case net.yura.mobile.logging.Logger.DEBUG: return Level.FINE;
            case net.yura.mobile.logging.Logger.INFO: return Level.INFO;
            case net.yura.mobile.logging.Logger.WARN: return Level.WARNING;
            case net.yura.mobile.logging.Logger.ERROR: return Level.SEVERE;
            case net.yura.mobile.logging.Logger.FATAL: return Level.SEVERE;
            default: throw new IllegalArgumentException("level: "+level);
        }
    }
    
}
