package net.yura.domination.lobby.client;

import java.awt.Frame;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.OutputStream;
import javax.swing.border.EmptyBorder;
import java.util.HashMap;
import javax.swing.JDialog;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.BorderFactory;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Locale;
import java.awt.Insets;
import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskIO;
import net.yura.domination.engine.RiskUIUtil;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.domination.ui.flashgui.FlashRiskAdapter;
import net.yura.domination.ui.flashgui.GameFrame;
import org.lobby.client.GameSetup;
import org.lobby.client.LobbyClientGUI;
import org.lobby.client.ResBundle;
import org.lobby.client.TurnBasedAdapter;

public class ClientGameRisk extends TurnBasedAdapter {

	private final static String product;
	private final static String version = "0.2";

	static {
                final String RISK_PATH = RiskUtil.GAME_NAME + "/";
                final String MAP_PATH = "maps/";

		product = RiskUtil.GAME_NAME + " Lobby Client";

                RiskUtil.streamOpener = new RiskIO() {
                    public InputStream openStream(String name) throws IOException {
                            return LobbyClientGUI.openStream(RISK_PATH+name);
                    }
                    public InputStream openMapStream(String name) throws IOException {
                            return openStream(MAP_PATH+name);
                    }
                    public ResourceBundle getResourceBundle(Class a,String n,Locale l) {
                            return ResBundle.getBundle(a,n,l);
                    }
                    public void openURL(URL url) throws Exception {
                            LobbyClientGUI.openURL(url);
                    }
                    public void openDocs(String doc) throws Exception {
                            openURL( new URL( LobbyClientGUI.getCodeBase(), RISK_PATH+doc) );
                    }
                    public void saveGameFile(String name, Object obj) throws Exception {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                    public InputStream loadGameFile(String file) throws Exception {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                    public void getMap(String filename, Risk risk,Exception ex) {
                        RiskUtil.printStackTrace(ex);
                        risk.getMapError(ex.toString());
                    }
                    public OutputStream saveMapFile(String fileName) throws Exception {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                    public void renameMapFile(String oldName, String newName) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
            };
	}

	public ClientGameRisk() {

	}


	//##################################################################################
	// game setup
	//##################################################################################

	private String newGameOptions;

	//private String mapsurl;
	private RiskMap[] maps;

	private JDialog dialog;
	private GameSetupPanel gsp;

	private HashMap MapMap;


	public GameSetup newGameDialog(Frame parent, String serveroptions,String myname) { // String serveroptions is a list of maps


		if (dialog == null) {

			dialog = new JDialog(parent,"Game Options",true);

			gsp = new GameSetupPanel(dialog,myname+"'s "+RiskUtil.GAME_NAME+" Game");

			// @todo:
			// do noting on close
			// when close then send event to gsp

			dialog.setContentPane(gsp);
			dialog.setResizable(false);
			dialog.pack();

		}


		if (serveroptions!=null && !serveroptions.equals(newGameOptions) ) {

			newGameOptions = serveroptions;

			String[] split = newGameOptions.split(",");

			maps = new RiskMap[split.length];

			for (int c=0;c<split.length;c++) {

				maps[c] = getRiskMap(split[c]);

			}

			gsp.setMaps(maps);

			final javax.swing.JList list = gsp.getList();

			new Thread() {

				public void run() {


					for (int c=0;c<maps.length;c++) {

						maps[c].loadInfo();

						if (c==0) {
							gsp.setSelected(c);
						}

						list.repaint();
					}


				}

			}.start();


		}



		gsp.reset();

		dialog.setVisible(true);

		String op = gsp.getOptions();

		if (op!=null) { return new GameSetup( gsp.getGameName(), op, gsp.getNumberOfHumanPlayers() ); }

		return null;

	}

	public ImageIcon getIcon(String options) {

		String choosemap = options.split("\\n")[3];

		RiskMap iconedmap = getRiskMap( choosemap.substring( 10 ,choosemap.length() ) );
		iconedmap.loadInfo();

		return iconedmap.getSmallIcon();

	}

	public RiskMap getRiskMap(String name) {

		//Risk.setupMapsDir(applet);

		if (MapMap==null) { MapMap = new HashMap(); }

		RiskMap themap = (RiskMap)MapMap.get(name);

		if (themap==null) {

			themap = new RiskMap(name);

			MapMap.put(name,themap);

		}

		return themap;

	}

	//##################################################################################
	// in game client stuff
	//##################################################################################


	private ClientRisk myrisk;

	private JLabel nameLabel;

	private GameFrame frame;


	public void startNewGame(String name) {

		if (frame==null) {


			myrisk = new ClientRisk(this);

			makeNewGameFrame();

		}

		nameLabel.setText(name);

	}

	private void makeNewGameFrame() {

		ResourceBundle resb = TranslationBundle.getBundle();

		//setReplay(false);

		final ImageIcon borderimage = new ImageIcon( ClientGameRisk.class.getResource("back.jpg") );



		final Box sidepanel = new Box(javax.swing.BoxLayout.Y_AXIS);/* {

			public void paintComponent(java.awt.Graphics g) {

				java.awt.Image img = borderimage.getImage();

				int w = img.getWidth(this);
				int h = img.getHeight(this);

				for (int i = 0; i < getWidth(); i += w) for (int j = 0; j < getHeight(); j += h) {

					g.drawImage(img, i, j, this);

				}
			}

		};*/




		JButton aboutButton = new JButton( resb.getString( "mainmenu.about") );

		aboutButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				RiskUIUtil.openAbout(frame,product, version);
			}
		});


		JPanel panel1 = new JPanel();
		JPanel panel2 = new JPanel();
		JPanel panel3 = new JPanel( new GridLayout(1,2,5,5) );
		panel3.setBorder( new EmptyBorder(5,5,5,5) );

		Insets insets = new Insets( startButton.getMargin().top ,0, startButton.getMargin().bottom ,0);
		startButton.setMargin(insets);
		aboutButton.setMargin(insets);

		nameLabel = new JLabel();

		panel1.add( nameLabel );
		panel2.add( timer );
		panel3.add( startButton );
		panel3.add( aboutButton );

		sidepanel.add( panel1 );
		sidepanel.add( panel2 );
		sidepanel.add( playerListArea );
		sidepanel.add( panel3 );
		sidepanel.add( chatBoxArea );


		//panel2.setBorder( new EmptyBorder(20, 0, 20, 0) );
		panel2.setBorder( BorderFactory.createMatteBorder(20, 0, 20, 0, borderimage ) );
		playerListArea.setBorder( BorderFactory.createMatteBorder(0, 0, 20, 0, borderimage ) );
		chatBoxArea.setBorder( BorderFactory.createMatteBorder(20, 0, 0, 0, borderimage ) );
		sidepanel.setBorder( BorderFactory.createMatteBorder(20, 20, 20, 20, borderimage ) );

		Dimension topsize = new Dimension(160,120);

		playerListArea.setPreferredSize( topsize );
		playerListArea.setMaximumSize( topsize );
		playerListArea.setMinimumSize( topsize );

		sidepanel.setPreferredSize( new Dimension(200,600) );



		FlashRiskAdapter riskadapter = new FlashRiskAdapter(myrisk) {

			public void addPlayer(int type, String name, java.awt.Color color, String ip) {}
			public void sendDebug(String a) {  } // System.out.println("\tRISK "+ a);

			public void noInput() {

				if (gameFrame!=null) {
					gameFrame.noInput();
				}
			}

			public void startGame(boolean s) {

				try {
					pp.load();
				}
				catch (Exception e) {
					RiskUtil.printStackTrace(e);
				}

				gameFrame.setup(s);

				gameFrame.setVisible(true);
				gameFrame.requestFocus();

			}

			public void closeGame() {

				gameFrame.setVisible(false);

			}

			//public void needInput(int s) {
			//	super.needInput(s);
			//}

		};




		frame = riskadapter.getGameFrame();

		frame.getContentPane().add(sidepanel, java.awt.BorderLayout.EAST );
		frame.pack();

		try {

			frame.setMinimumSize( frame.getPreferredSize() );

		}
		catch(NoSuchMethodError ex) {

			// must me java 1.4
			// dont need to do anything here as it would have already been set to resizable false
		}

		// amoung other things, the newplayer command needs to be passed with option that matches this
		// computers Risk.myAddress, or the Risk game would not know when to ask for input



	}

	// this NEEDS to call leaveGame();
	public void closegame() {

		myrisk.parser("closegame");

	}

	public void resignPlayer() {

		myrisk.resignPlayer();
	}

	public void blockInput() {

		frame.blockInput();

	}

	public void gameString(String message) {

		System.out.println("\tGOT: "+message);

		myrisk.addToInbox(message);

	}


	public void gameObject(Object object) {


		Object[] objects = (Object[])object;

		myrisk.createGame( (String)objects[0] , (RiskGame)objects[1] );

		// not needed as "gameFrame.setup(s);" calls this anyway
		//frame.setGameStatus(null);

	}

	public void renamePlayer(String oldser,String newuser) {

	    myrisk.renamePlayer(oldser,newuser);

	}

/*
	public static void main(String[] argv) throws Exception { // TESTING METHOD!!!!


		// server sends
		// GameId = 1
		// GameName = Risk // maybe NOT
		// JarFile = Risk.jar // or a full URL
		// LobbyGame = risk.lobby.LobbyGameRisk
		// gameOptions = list of maps that can be used

		// create a new LobbyGame

		String name = "risk.lobby.LobbyGameRisk";
		Class myclass = Class.forName( name );
		LobbyGame lobbygame = (LobbyGame)myclass.newInstance();
		lobbygame.addLobbyGameMoveListener( new LobbyGameMoveListener() );

		//selected gametype Risk
		// clicked new game
		// need to make a new game string
		String newGameOptionString = lobbygame.newGameDialog( null , 

			"http://jrisk.sourceforge.net/applet/maps/\n"+
			"http://jrisk.sourceforge.net/images/maps/\n"+
			"board.map aa.jpg Risk Board\n" +
			"board.map board.jpg Risk Board\n" +
		//	"board.map chutes.jpg Risk Board\n" +
		//	"board.map conquest.jpg Risk Board\n" +
		//	"board.map cow.jpg Risk Board\n" +
		//	"board.map europass.jpg Risk Board\n" +
		//	"board.map fortress.jpg Risk Board\n" +
		//	"board.map france.jpg Risk Board\n" +
		//	"board.map geoscape.jpg Risk Board\n" +
			"risk2.map risk2.jpg Risk II"


		); // getParent

		System.out.println("GOT START GAME OPTIONS: "+newGameOptionString);



		// send it to the server

		// sever informs lobbs that new game is made

		// people can join

		// player who made it is autojoined and the game is removed from the list if everyone leaves

		// lobby sends...


		// game starts when it fills up
//		lobbygame.joinGame(game_id_from_server , newGameOptionString + playerinfo);

		// Risk object is onyl now created
		// options are passed to it
		// game starts



		// spectator comes in

	}
*/
}
