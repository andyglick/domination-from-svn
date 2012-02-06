package net.yura.domination.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.prefs.Preferences;
import net.yura.domination.engine.core.Player;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.engine.translation.MapTranslator;

public class RiskUtil {

	public static final String RISK_VERSION_URL;
	public static final String RISK_LOBBY_URL;
//	public static final String RISK_POST_URL; // look in Grasshopper.jar now
	public static final String GAME_NAME;
//	private static final String DEFAULT_MAP;

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
//		RISK_POST_URL = settings.getProperty("POST_URL");
		GAME_NAME = settings.getProperty("name");
		//DEFAULT_MAP = settings.getProperty("defaultmap");
		Risk.RISK_VERSION = settings.getProperty("version");

		String dmap = settings.getProperty("defaultmap");
		String dcards = settings.getProperty("defaultcards");

		RiskGame.setDefaultMapAndCards( dmap , dcards );

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
		openURL(new URL("http://domination.sourceforge.net/donate.shtml"));
	}
        
	public static void donatePayPal() throws Exception {
		openURL(new URL("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=yura%40yura%2enet&item_name="+GAME_NAME+"%20Donation&no_shipping=0&no_note=1&tax=0&currency_code=GBP&lc=GB&bn=PP%2dDonationsBF&charset=UTF%2d8"));
	}

        public static void loadPlayers(Risk risk,Class uiclass) {

            Preferences prefs=null;
            try {
                 prefs = Preferences.userNodeForPackage( uiclass );
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

        public static void savePlayers(Risk risk,Class uiclass) {

            Preferences prefs=null;
            try {
                 prefs = Preferences.userNodeForPackage( uiclass );
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

                // on android this does not work, god knows why
                // whats the point of including a class if its
                // most simple and basic operation does not work?
                try {
                    prefs.flush();
                }
                catch(Exception ex) {
                    ex.printStackTrace();
                }

            }
        }

        public static BufferedReader readMap(InputStream in) throws IOException {

            PushbackInputStream pushback = new PushbackInputStream(in,3);

            int first = pushback.read();
            if (first == 0xEF) {
                int second = pushback.read();
                if (second == 0xBB) {
                    int third = pushback.read();
                    if (third == 0xBF) {
                        return new BufferedReader(new InputStreamReader( pushback, "UTF-8" ) );
                    }
                    pushback.unread(third);
                }
                pushback.unread(second);
            }
            pushback.unread(first);

            return new BufferedReader(new InputStreamReader( pushback, "ISO-8859-1" ) );
        }

        /**
         * gets the info for a map or cards file
         * in the case of map files it will get the "name" "crd" "prv" "pic" "map" and any "comment"
         * and for cards it will have a "missions" that will contain the String[] of all the missions
         */
	public static Hashtable loadInfo(String fileName,boolean cards) {

            Hashtable info = new Hashtable();

            for (int c=0;true;c++) {

                BufferedReader bufferin=null;

                try {

                        bufferin= RiskUtil.readMap(RiskUtil.openMapStream(fileName));
                        Vector misss=null;

                        if (cards) {
                            MapTranslator.setCards( fileName );
                            misss = new Vector();
                        }

                        String input = bufferin.readLine();
                        String mode = null;

                        while(input != null) {

                                if (input.equals("")) {
                                        // do nothing
                                        //System.out.print("Nothing\n"); // testing
                                }
                                else if (input.charAt(0)==';') {
                                    String comment = (String)info.get("comment");
                                    String com = input.substring(1).trim();
                                    if (comment==null) {
                                        comment = com;
                                    }
                                    else {
                                        comment = comment +"\n"+com;
                                    }
                                    info.put("comment", comment);
                                }
                                else {

                                        if (input.charAt(0)=='[' && input.charAt( input.length()-1 )==']') {
                                                mode="newsection";
                                        }

                                        if ("files".equals(mode)) {

                                                int space = input.indexOf(' ');
                                            
                                                String fm = input.substring(0,space);
                                                String val = input.substring(space+1);

                                                info.put( fm , val);

                                        }
                                        else if ("continents".equals(mode)) {

                                                break;

                                        }
                                        else if ("missions".equals(mode)) {

                                                StringTokenizer st = new StringTokenizer(input);
                                            
                                                String description=MapTranslator.getTranslatedMissionName(st.nextToken()+"-"+st.nextToken()+"-"+st.nextToken()+"-"+st.nextToken()+"-"+st.nextToken()+"-"+st.nextToken());

                                                if (description==null) {

                                                        StringBuffer d = new StringBuffer();

                                                        while (st.hasMoreElements()) {

                                                                d.append( st.nextToken() );
                                                                d.append( " " );
                                                        }

                                                        description = d.toString();

                                                }

                                                misss.add( description );

                                        }
                                        else if ("newsection".equals(mode)) {

                                                mode = input.substring(1, input.length()-1); // set mode to the name of the section

                                        }
                                        else if (mode == null) {
                                            if (input.indexOf(' ')>0) {
                                                info.put( input.substring(0,input.indexOf(' ')) , input.substring(input.indexOf(' ')+1) );
                                            }
                                        }

                                }

                                input = bufferin.readLine(); // get next line

                        }

                        if (cards) {
                            info.put("missions", (String[])misss.toArray(new String[misss.size()]) );
                            misss = null;
                        }

                        break;
                }
                catch(IOException ex) {
                        System.out.println("Error trying to load: "+fileName);
                        ex.printStackTrace();
                        if (c < 5) { // retry
                                try { Thread.sleep(1000); } catch(Exception ex2) { }
                        }
                        else { // give up
                                break;
                        }
                }
                finally {
                    if (bufferin!=null) {
                        try { bufferin.close(); } catch(Exception ex2) { }
                    }
                }
            }

            return info;

	}

        public static OutputStream getOutputStream(File dir,String fileName) throws Exception {
            File outFile = new File(dir,fileName);
            // as this could be dir=.../maps fileName=preview/file.jpg
            // we need to make sure the preview dir exists, and if it does not, we must make it
            File parent = outFile.getParentFile();
            if (!parent.isDirectory() && !parent.mkdirs()) { // if it does not exist and i cant make it
                throw new RuntimeException("can not create dir "+parent);
            }
            return new FileOutputStream( outFile );
        }

        
        
        
        
        
        
        
    public static String replaceAll(String string, String notregex, String replacement) {
        return string.replaceAll( quote(notregex) , quoteReplacement(replacement));
    }

    public static String quote(String s) {
        int slashEIndex = s.indexOf("\\E");
        if (slashEIndex == -1)
            return "\\Q" + s + "\\E";

        StringBuilder sb = new StringBuilder(s.length() * 2);
        sb.append("\\Q");
        slashEIndex = 0;
        int current = 0;
        while ((slashEIndex = s.indexOf("\\E", current)) != -1) {
            sb.append(s.substring(current, slashEIndex));
            current = slashEIndex + 2;
            sb.append("\\E\\\\E\\Q");
        }
        sb.append(s.substring(current, s.length()));
        sb.append("\\E");
        return sb.toString();
    }

    public static String quoteReplacement(String s) {
        if ((s.indexOf('\\') == -1) && (s.indexOf('$') == -1))
            return s;
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\') {
                sb.append('\\'); sb.append('\\');
            } else if (c == '$') {
                sb.append('\\'); sb.append('$');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

}
