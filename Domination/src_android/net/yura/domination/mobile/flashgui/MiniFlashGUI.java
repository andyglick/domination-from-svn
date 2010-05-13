package net.yura.domination.mobile.flashgui;

import java.util.ResourceBundle;

import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.components.Frame;
import net.yura.mobile.gui.components.Panel;
import net.yura.mobile.gui.layout.XULLoader;
import net.yura.mobile.util.Properties;

public class MiniFlashGUI extends Frame {

    private Properties resBundle = wrap(TranslationBundle.getBundle());

    public MiniFlashGUI() {

        ActionListener al = new ActionListener() {

            @Override
            public void actionPerformed(String actionCommand) {
                System.out.println("ac "+actionCommand);

            }
        };

        XULLoader loader;
        try {
            loader = XULLoader.load( getClass().getResourceAsStream("/mainmenu.xml") , al, resBundle);
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }

        setContentPane( (Panel)loader.getRoot() );

    }


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
