package net.yura.domination.engine;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.prefs.Preferences;
import net.yura.domination.engine.core.Player;
import net.yura.domination.engine.core.RiskGame;

public class RiskUtil {

	public static final String RISK_VERSION_URL;
	public static final String RISK_LOBBY_URL;
	public static final String RISK_POST_URL;
	public static final String GAME_NAME;
//	private static final String DEFAULT_MAP;

	public static final String SAVES_DIR = "saves/";

	public static RiskIO streamOpener;

	private final static Properties settings;
	static {

		settings = new Properties();

		try {
			settings.load(RiskUtil.class.getResourceAsStream("settings.ini"));
		}
		catch (Exception ex) {
			throw new RuntimeException("can not find settings.ini file!",ex);
		}

		RISK_VERSION_URL = settings.getProperty("VERSION_URL");
		RISK_LOBBY_URL = settings.getProperty("LOBBY_URL");
		RISK_POST_URL = settings.getProperty("POST_URL");
		GAME_NAME = settings.getProperty("name");
		//DEFAULT_MAP = settings.getProperty("defaultmap");
		Risk.RISK_VERSION = settings.getProperty("version");

		String dmap = settings.getProperty("defaultmap");
		String dcards = settings.getProperty("defaultcards");

		RiskGame.setDefaultMapAndCards( dmap , dcards );

	}
	public static String getGameName() {
		return GAME_NAME;
	}

	public static InputStream openMapStream(String a) throws IOException {
            return streamOpener.openMapStream(a);
	}

	public static InputStream openStream(String a) throws IOException {
            return streamOpener.openStream(a);
	}

	public static ResourceBundle getResourceBundle(Class c,String n,Locale l) {
            return streamOpener.getResourceBundle(c,n,l);
	}

	public static void openURL(URL url) throws Exception {
            streamOpener.openURL(url);
	}

	public static void openDocs(String docs) throws Exception {
            streamOpener.openDocs(docs);
	}
        public static void saveFile(String file, RiskGame aThis) throws Exception {
            streamOpener.saveGameFile(file, aThis);
        }
        public static InputStream getLoadFileInputStream(String file) throws Exception {
            return streamOpener.loadGameFile(file);
        }


        // instead of using java.awt.Color
        public static final int BLACK = 0xFF000000;
        public static final int WHITE = 0xFFFFFFFF;
        public final static int LIGHT_GRAY = 0xFFC0C0C0;
        public final static int GRAY = 0xFF808080;
        public final static int DARK_GRAY = 0xFF404040;
        public final static int RED = 0xFFFF0000;
        public final static int PINK = 0xFFFFAFAF;
        public final static int ORANGE = 0xFFFFC800;
        public final static int YELLOW = 0xFFFFFF00;
        public final static int GREEN = 0xFF00FF00;
        public final static int MAGENTA = 0xFFFF00FF;
        public final static int CYAN = 0xFF00FFFF;
        public final static int BLUE = 0xFF0000FF;

	static HashMap intToString = new HashMap();
	static HashMap stringToInt = new HashMap();
	static {
            add(BLACK,"black");
            add(BLUE,"blue");
            add(CYAN,"cyan");
            add(DARK_GRAY,"darkgray");
            add(GRAY,"gray");
            add(GREEN,"green");
            add(LIGHT_GRAY,"lightgray");
            add(MAGENTA,"magenta");
            add(ORANGE,"orange");
            add(PINK,"pink");
            add(RED,"red");
            add(WHITE,"white");
            add(YELLOW,"yellow");
	}

	static void add(int color,String name) {
	    Integer c = new Integer(color);
	    intToString.put(c, name);
	    stringToInt.put(name, c);
	}

	public static String getStringForColor(int c) {
	    String result = (String)intToString.get(new Integer(c));
	    if (result != null) {
	        return result;
            }
            return getHexForColor(c);
	}


	/**
         * get a color as a int from a string, alpha is set to 255
         * if we can not get the color then we return 0
	 */
	public static int getColor(String nm) {

		Integer color = (Integer)stringToInt.get(nm);
                if (color!=null) {
                    return color.intValue();
                }

		try {

                    Integer result = Integer.decode(nm);

                    // the 0xff000000 | means the alpha is 255
                    return 0xff000000 | result.intValue();
		}
		catch(Exception ex) {

			//System.out.print("Error: unable to find color "+s+".\n"); // testing
			return 0;
		}
	}

	public static int getTextColorFor(int c) {

/*
if ( c.getRed() < 100 && c.getBlue() < 100 && c.getGreen() < 100 ) {
return Color.white;
}
else {
return Color.black;
}



int r = c.getRed();
int g = c.getGreen();
int b = c.getBlue();

if (r > 240 || g > 240) {
return Color.black;
}
else {
return Color.white;
}
*/


		int r = getRed(c);
		int g = getGreen(c);
		// int b = c.getBlue();


		if ((r > 240 || g > 240) || (r > 150 && g > 150)) {
			return BLACK;
		}
		else {
			return WHITE;
		}

	}

        /**
         * Returns the red component in the range 0-255 in the default sRGB
         * space.
         * @return the red component.
         * @see #getRGB
         */
        public static int getRed(int rgb) {
            return (rgb >> 16) & 0xFF;
        }

        /**
         * Returns the green component in the range 0-255 in the default sRGB
         * space.
         * @return the green component.
         * @see #getRGB
         */
        public static int getGreen(int rgb) {
            return (rgb >> 8) & 0xFF;
        }

        /**
         * Returns the blue component in the range 0-255 in the default sRGB
         * space.
         * @return the blue component.
         * @see #getRGB
         */
        public static int getBlue(int rgb) {
            return (rgb >> 0) & 0xFF;
        }

        /**
         * Returns the alpha component in the range 0-255.
         * @return the alpha component.
         * @see #getRGB
         */
        public static int getAlpha(int rgb) {
            return (rgb >> 24) & 0xff;
        }

        public static String getHexForColor(int c) {
                return "#" + Integer.toHexString((  c & 0xffffff) | 0x1000000).substring(1);
        }

	public static void donate() throws Exception {
		openURL(new URL("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=yura%40yura%2enet&item_name="+GAME_NAME+"%20Donation&no_shipping=0&no_note=1&tax=0&currency_code=GBP&lc=GB&bn=PP%2dDonationsBF&charset=UTF%2d8"));
	}

        public static void loadPlayers(Risk risk) {

            Preferences prefs=null;
            try {
                 prefs = Preferences.userNodeForPackage( RiskUtil.class );
            }
            catch(Throwable th) { } // security

            for (int cc=1;cc<=RiskGame.MAX_PLAYERS;cc++) {
                String nameKey = "default.player"+cc+".name";
                String colorKey = "default.player"+cc+".color";
                String typeKey = "default.player"+cc+".type";

                String name = risk.getRiskConfig(nameKey);
                String color = risk.getRiskConfig(colorKey);
                String type = risk.getRiskConfig(typeKey);

                if (prefs!=null) {
                    name = prefs.get(nameKey, name);
                    color = prefs.get(colorKey, color);
                    type = prefs.get(typeKey, type);
                }

                if (!"".equals(name)&&!"".equals(color)&&!"".equals(type)) {
                    risk.parser("newplayer " + type+" "+ color+" "+ name );
                }
            }

        }

        public static void savePlayers(Risk risk) {

            Preferences prefs=null;
            try {
                 prefs = Preferences.userNodeForPackage( RiskUtil.class );
            }
            catch(Throwable th) { } // security

            if (prefs!=null) {

                Vector players = risk.getGame().getPlayers();

                for (int cc=1;cc<=RiskGame.MAX_PLAYERS;cc++) {
                    String nameKey = "default.player"+cc+".name";
                    String colorKey = "default.player"+cc+".color";
                    String typeKey = "default.player"+cc+".type";

                    Player player = (cc<=players.size())?(Player)players.elementAt(cc-1):null;

                    String name = "";
                    String color = "";
                    String type = "";

                    if (player!=null) {
                        name = player.getName();
                        color = getStringForColor( player.getColor() );
                        type = Risk.getType( player.getType() );
                    }
                    prefs.put(nameKey, name);
                    prefs.put(colorKey, color);
                    prefs.put(typeKey, type);
                }
            }
        }
}
