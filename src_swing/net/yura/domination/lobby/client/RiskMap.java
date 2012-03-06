package net.yura.domination.lobby.client;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.util.Hashtable;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.translation.MapTranslator;

public class RiskMap {

	private boolean loaded;
	private ImageIcon icon;
	private ImageIcon iconSmall;
	private ImageIcon iconBig;
	private String displayName;
	private String fileName;
	private String[] missions;

	public ImageIcon getIcon() {

		return icon;

	}

	public ImageIcon getBigIcon() {

		return iconBig;

	}

	public ImageIcon getSmallIcon() {

		return iconSmall;

	}

	public String toString() {

		return displayName;

	}

	public String getFileName() {

		return fileName;

	}


	public String[] getMissions() {

		return missions;

	}

	public RiskMap(String a) {

		fileName = a;
		displayName = a;
		missions = new String[0];

	}

        public void loadInfo() {

            if (!loaded) {
                loaded=true;
                Hashtable mapinfo = RiskUtil.loadInfo(fileName,false);

                displayName = (String)mapinfo.get("name");

                String cardsFile = (String)mapinfo.get("crd");
                if (cardsFile!=null) {
                    Hashtable cardsinfo = RiskUtil.loadInfo(cardsFile,true);
                    missions = (String[])cardsinfo.get("missions");
                }

                String prvImage = (String)mapinfo.get("prv");
                if (prvImage!=null) {
                    try {
                        BufferedImage mapimageO = ImageIO.read( RiskUtil.openMapStream("preview/"+prvImage) );

                        icon = new ImageIcon(mapimageO.getScaledInstance(50,31,Image.SCALE_SMOOTH));
                        iconSmall = new ImageIcon(mapimageO.getScaledInstance(32,20,Image.SCALE_SMOOTH));
                        iconBig = new ImageIcon(mapimageO.getScaledInstance(203,127, Image.SCALE_SMOOTH));
                    }
                    catch (Exception ex) {
                        RiskUtil.printStackTrace(ex);
                    }
                }
            }
        }

}
