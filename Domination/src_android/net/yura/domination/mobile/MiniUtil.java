package net.yura.domination.mobile;

import java.util.ResourceBundle;
import net.yura.mobile.util.Properties;

public class MiniUtil {






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

    public static void showAbout() {
        // TODO Auto-generated method stub

    }

}
