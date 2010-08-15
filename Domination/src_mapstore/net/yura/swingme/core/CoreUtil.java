package net.yura.swingme.core;

import java.util.ResourceBundle;
import net.yura.mobile.util.Properties;

/**
 * @author Yura Mamyrin
 */
public class CoreUtil {

    public static Properties wrap(final ResourceBundle res) {
        return new Properties() {
            public String getProperty(String key) {
                return res.getString(key);
            }
            public String getProperty(String key, String defaultValue) {
                try {
                    return res.getString(key);
                }
                catch (Exception ex) {
                    return defaultValue;
                }
            }
        };
    }

}
