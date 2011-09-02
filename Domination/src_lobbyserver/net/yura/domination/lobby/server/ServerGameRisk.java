package net.yura.domination.lobby.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.lobby.server.TurnBasedGame;

import java.util.Iterator;
import java.util.Vector;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
//import net.yura.domination.engine.RiskUIUtil;
import java.util.Locale;
import java.util.ResourceBundle;
import net.yura.domination.engine.RiskIO;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.ai.AIPlayer;
import net.yura.domination.engine.core.Player;
import net.yura.domination.engine.core.RiskGame;

public class ServerGameRisk extends TurnBasedGame {

	private ServerRisk myrisk;

	private Map<String,String> playersMap;
	private Map<String,String> inverseMap;

        static {
            final URL mapsdir;
            try {
                mapsdir = new java.io.File( RiskUtil.getGameName() + "/maps").toURI().toURL();
            }
            catch(Exception ex) {
                throw new RuntimeException(ex);
            }

            RiskUtil.streamOpener = new RiskIO() {
                public InputStream openStream(String name) throws IOException {
                    return new File(name).toURI().toURL().openStream();
                }
                public InputStream openMapStream(String name) throws IOException {
                    return new URL(mapsdir,name).openStream();
                }
                public ResourceBundle getResourceBundle(Class c, String n, Locale l) {
                    // TODO this should be different for different clients connected to this server
                    return ResourceBundle.getBundle(c.getPackage().getName()+"."+n, l );
                }
                public void openURL(URL url) throws Exception {

                }
                public void openDocs(String doc) throws Exception {

                }
                public void saveGameFile(String name,Object obj) throws Exception {

                }
                public InputStream loadGameFile(String file) throws Exception {
                    return null;
                }
                public java.io.OutputStream saveMapFile(String fileName) throws Exception {
                    throw new UnsupportedOperationException("can not add maps to server");
                }
            };
        }

	public ServerGameRisk() {

		myrisk = new ServerRisk(this);

		// POP UP DEBUG WINDOW
		//Increment1Frame gui = new Increment1Frame( myrisk );
		//RiskGUI gui = new RiskGUI( myrisk );
        	//gui.setVisible(true);

		playersMap = new HashMap<String,String>();
		inverseMap = new HashMap<String,String>();

	}

	public void createNewGame(String startGameOptions, String[] players) {

		// sort them so if player bob was green last time, they r again
		Arrays.sort(players);

		playersMap.clear();
		inverseMap.clear();

		//System.out.println("\tNEW GAME STARTING FOR RISK: "+gameid);

		//myguid = gameid;

		myrisk.makeNewGame();


		String[] options = startGameOptions.split("\\n");

		int aicrap = Integer.parseInt(options[0]);
		int aieasy = Integer.parseInt(options[1]);
		int aihard = Integer.parseInt(options[2]);

		if ((players.length+aicrap+aieasy+aihard)>RiskGame.MAX_PLAYERS ) { throw new RuntimeException("player number missmatch for startgame"); }

		myrisk.addSetupCommandToInbox(options[3]); // set the map file to use

		List<String> colorString = new ArrayList<String>();
		colorString.add( myrisk.getRiskconfig("default.player1.color") );
		colorString.add( myrisk.getRiskconfig("default.player2.color") );
		colorString.add( myrisk.getRiskconfig("default.player3.color") );
		colorString.add( myrisk.getRiskconfig("default.player4.color") );
		colorString.add( myrisk.getRiskconfig("default.player5.color") );
		colorString.add( myrisk.getRiskconfig("default.player6.color") );
		Iterator<String> it = colorString.iterator();

		for (int c=0;c<players.length;c++) {

			it.hasNext();
			String color = it.next();

			String playerid = "player"+(c+1);

			playersMap.put(players[c], playerid);
			inverseMap.put(playerid, players[c]);

			myrisk.addSetupCommandToInbox(playerid,"newplayer human "+color + " " + players[c]);

		}

		for (int c=0;c<aicrap;c++) {


			it.hasNext();
			String color = it.next();

			myrisk.addSetupCommandToInbox("newplayer ai crap "+color + " CrapBot" + (c+1));

		}

		for (int c=0;c<aieasy;c++) {

			it.hasNext();
			String color = it.next();

			myrisk.addSetupCommandToInbox("newplayer ai easy "+color + " EasyBot" + (c+1));
		}

		for (int c=0;c<aihard;c++) {

			it.hasNext();
			String color = it.next();

			myrisk.addSetupCommandToInbox("newplayer ai hard "+color + " HardBot" + (c+1));
		}

		myrisk.addSetupCommandToInbox(options[4]); // start the game

		// only return when the game is setup
		while(!myrisk.getWaiting()) {

			try { Thread.sleep(100); }
			catch(InterruptedException e){}

		}

	}

	public void startGame() {

		myrisk.setPaued(false);

	}

	// this NEEDS to call gameFinished(winning player)
	public void stopGame() {

		myrisk.setPaued(true);

		RiskGame game = myrisk.getGame();

		if ( game.checkPlayerWon() ) {

			gameFinished( game.getCurrentPlayer().getName() );

		}
		else {

			String name="???";
			int best=-1;

			Vector players = game.getPlayers();

			for (int c=0;c<players.size();c++) {

				Player player = (Player)players.elementAt(c);

				// player.getType() == Player.PLAYER_HUMAN &&
				// if all resign then no humans left
				if ( player.getNoTerritoriesOwned()>best) {

					name = player.getName();
					best = player.getNoTerritoriesOwned();

				}

			}

			gameFinished(name);	

		}

	}

	public void destroyGame() {

		myrisk.setKillFlag();

	}


	public void clientHasJoined(String username) {

		String playerid = playersMap.get(username);

		System.out.println(username+" -> "+playerid);

		sendObjectToClient(new Object[] { playerid,myrisk.getGame() }, username );

	}

	// get message from the user
	public void stringFromPlayer(String username, String message) {

		//System.out.print("\tGOTFROMCLIENT "+username+":"+message+"\n");


		String address = playersMap.get(username);

		//if (game.getCurrentPlayer()!=null) { System.out.print( "\t"+game.getCurrentPlayer().getAddress()+" "+address ); }

		// game not started OR game IS started and it is there go
		if (message.trim().equals("closegame")) {

			System.out.println("\tCLOSEGAME NOT ALLOWED TO BE SENT TO CORE: "+username);

		}
		else if (myrisk.getGame().getCurrentPlayer()!=null && myrisk.getGame().getCurrentPlayer().getAddress().equals( address )) {

			// creates the players with the correct address
			myrisk.addPlayerCommandToInbox(address , message);

		}
		else {

			System.out.print("\tCHEATING!!!!: "+username+" "+message+"\n");

			//listoner.sendChatroomMessage(username+" is trying to cheat!");

		}

	}


	public void doBasicGo(String username) {

		String playerid = playersMap.get(username);

		// this check is already done
		//if (myrisk.getGame().getCurrentPlayer().getAddress().equals(playerid)) {

			myrisk.addPlayerCommandToInbox(playerid+"-doBasicGo", AIPlayer.getOutput(myrisk.getGame(),AIPlayer.aicrap) );

		//}
		// else something is going very wrong!!!!
		// such as cheating

	}

	public void playerResigns(String username) {

		String playerid = playersMap.get(username);

		//String currentAddress = myrisk.getGame().getCurrentPlayer().getAddress();

		if (playerid != null) {


			myrisk.resignPlayer(playerid);


		}

		// already handled by the turn based game
		// ----
		// if they have resigned on there own go then do a go for them
		//if (currentAddress.equals(playerid)) {
		//	myrisk.parser( AIPlayer.getOutput(myrisk.getGame(),AIPlayer.aicrap) );
		//}

	}

	public void renamePlayer(String oldser,String newuser) {

	    synchronized(playersMap) {

		String playerid = playersMap.get(oldser);

		if (playerid!=null) {

			playersMap.remove(oldser);

			playersMap.put(newuser,playerid);

			inverseMap.remove(playerid);

			inverseMap.put(playerid,newuser);

		}
	    }

	    myrisk.renamePlayer(oldser,newuser);

	}


	public void getInputFromSomeone() {

		// trying to find what was null
		System.out.println(inverseMap);
		System.out.println(myrisk);
		System.out.println(myrisk.getGame());
		System.out.println(myrisk.getGame().getCurrentPlayer());
		System.out.println(myrisk.getGame().getCurrentPlayer().getAddress());

		String username = inverseMap.get( myrisk.getGame().getCurrentPlayer().getAddress() );

		getInputFromClient(username);

	}



}
