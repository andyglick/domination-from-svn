package net.yura.domination.engine;

import java.io.InputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.net.URL;

public interface RiskIO {


	InputStream openStream(String name) throws IOException;
	InputStream openMapStream(String name) throws IOException;
	ResourceBundle getResourceBundle(Class a,String n,Locale l);
	void openURL(URL url) throws Exception;
	void openDocs(String doc) throws Exception;

        // only here coz of android, as are not used for lobby
        void saveGameFile(String name,Object obj) throws Exception;
        InputStream loadGameFile(String file) throws Exception;
}
