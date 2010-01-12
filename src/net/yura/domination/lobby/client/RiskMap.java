package net.yura.domination.lobby.client;

import org.lobby.client.LobbyClientGUI;

import javax.swing.ImageIcon;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Image;

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

	public void loadInfo(String f) {

		if (!loaded) {

		    loaded=true;

		    for (int c=0;true;c++) {

			try {

				BufferedReader bufferin=new BufferedReader(new InputStreamReader( LobbyClientGUI.openStream(f+fileName) ));
				BufferedReader bufferin2=null;

				StringTokenizer st=null;

				String input = bufferin.readLine();
				String mode = null;

				Vector misss = new Vector();

				while(input != null) {

					if (input.equals("") || input.charAt(0)==';') {
						// do nothing
						//System.out.print("Nothing\n"); // testing
					}
					else {

						if (input.charAt(0)=='[' && input.charAt( input.length()-1 )==']') {
							mode="newsection";
						}
						else { st = new StringTokenizer(input); }

						if ("files".equals(mode)) {

							String fm = st.nextToken();

							if ( fm.equals("crd") ) {



								String name = st.nextToken();

								risk.engine.translation.MapTranslator.setCards( name );

								bufferin2=new BufferedReader(new InputStreamReader(LobbyClientGUI.openStream(f+name)));

							}
							else if ( fm.equals("prv") ) {

								BufferedImage mapimageO = ImageIO.read( LobbyClientGUI.openStream(f+"preview/"+st.nextToken()) );

								icon = new ImageIcon(mapimageO.getScaledInstance(50,31,Image.SCALE_SMOOTH));
								iconSmall = new ImageIcon(mapimageO.getScaledInstance(32,20,Image.SCALE_SMOOTH));
								iconBig = new ImageIcon(mapimageO.getScaledInstance(203,127, Image.SCALE_SMOOTH));

							}

						}
						else if ("continents".equals(mode)) {

							if (bufferin2==null) { throw new Exception("no cards found"); }

							bufferin.close();

							bufferin = bufferin2;

						}
						else if ("missions".equals(mode)) {


							String description=risk.engine.translation.MapTranslator.getTranslatedMissionName(st.nextToken()+"-"+st.nextToken()+"-"+st.nextToken()+"-"+st.nextToken()+"-"+st.nextToken()+"-"+st.nextToken());

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

							if (input.startsWith("name ")) {

								displayName = input.substring(5,input.length());

							}	

						}

					}

					input = bufferin.readLine(); // get next line

				}

				bufferin.close();

				missions = (String[])misss.toArray(new String[misss.size()]);

				break;
	    		}
	    		catch(Exception ex) {

				System.out.println("Error trying to load: "+fileName);

				ex.printStackTrace();

				if (c < 5) { // retry

					try { Thread.sleep(1000); } catch(Exception ex2) { }
				}
				else { // give up

					break;
				}


			}
		    }

		}

	}



}
