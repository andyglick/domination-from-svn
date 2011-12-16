// Yura Mamyrin, Group D

package net.yura.domination.engine;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;
import net.yura.domination.engine.ai.AIPlayer;
import net.yura.domination.engine.core.Card;
import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.Mission;
import net.yura.domination.engine.core.Player;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.engine.translation.TranslationBundle;

/**
 * <p> Main Risk Class </p>
 * @author Yura Mamyrin
 */

public class Risk extends Thread {

	public static String RISK_VERSION;

	private StringTokenizer StringT;
	protected RiskController controller;
	protected RiskGame game;
	//private String message;

	private PrintWriter outChat = null;
	private BufferedReader inChat = null;
	private ChatArea chatter = null;
	private Socket chatSocket = null;
	private ChatDisplayThread myReader = null;

	private int port;

	protected String myAddress;

	// crashes on a mac too much
	//private SealedObject Undo;
	private ByteArrayOutputStream Undo = new ByteArrayOutputStream();

	protected boolean unlimitedLocalMode;
	private boolean autoplaceall;
	private boolean battle;
	private boolean replay;

	protected final Vector inbox;

	protected ResourceBundle resb;
	protected Properties riskconfig;


	public Risk(String b,String c) {
		this();

		RiskGame.setDefaultMapAndCards(b,c);
	}

        public static final String[] types = new String[] { "human","ai easy","ai easy","ai easy","ai hard","ai hard" };
        public static final String[] names = new String[] { RiskUtil.getGameName()+"Player","bob","fred","ted","yura","lala"};
        public static final String[] colors = new String[] { "green","blue","red","cyan","magenta","yellow"};

	public Risk() {
		super(RiskUtil.getGameName()+"-GAME-THREAD");

		resb = TranslationBundle.getBundle();

                try {
                    String newName = System.getProperty("user.name");

                    if (newName==null || "".equals(newName.trim())) {
                        throw new Exception("bad user name");
                    }
                    else {
                        for (int c=0;c<names.length;c++) {
                            if (names[c].equals(newName)) {
                                throw new Exception("name already in use");
                            }
                        }
                    }
                    names[0] = newName;
		}
                catch(Throwable th) { }

		riskconfig = new Properties();

		riskconfig.setProperty("default.port","4444");
		riskconfig.setProperty("default.host","localhost");
		riskconfig.setProperty("default.map", RiskGame.getDefaultMap() );
		riskconfig.setProperty("default.cards", RiskGame.getDefaultCards() );

                for (int c=0;c<names.length;c++) {
                    riskconfig.setProperty("default.player"+(c+1)+".type",types[c]);
                    riskconfig.setProperty("default.player"+(c+1)+".color",colors[c]);
                    riskconfig.setProperty("default.player"+(c+1)+".name", names[c] );
                }

		try {
                    riskconfig.load( RiskUtil.openStream("game.ini") );
		}
		catch (Exception ex) {
                    // can not find file, no problem
		}

		String randomString = "#"+String.valueOf( Math.round(Math.random()*Long.MAX_VALUE) );

		try {
			//if (RiskUtil.checkForNoSandbox()) {
                        try {
				myAddress = InetAddress.getLocalHost().getHostName() + randomString;
			}
			//else {
                        catch(Throwable th) {
				myAddress = "sandbox" + randomString;
			}
/*

			//InetAddress localAddr = InetAddress.getLocalHost();

			//myAddress = localAddr.getHostAddress();

			myAddress=null;
			Enumeration ifaces = NetworkInterface.getNetworkInterfaces();

			search:
			while (ifaces.hasMoreElements()) {
				NetworkInterface ni = (NetworkInterface)ifaces.nextElement();
				//System.out.println(ni.getName() + ":");

				Enumeration addrs = ni.getInetAddresses();

				while (addrs.hasMoreElements()) {
					InetAddress ia = (InetAddress)addrs.nextElement();
					//System.out.println(" " + ia.getHostAddress());


					String tmpAddr = ia.getHostAddress();
					if (!tmpAddr.equals("127.0.0.1")) {

						myAddress = tmpAddr;
						break search;

					}


				}
			}

			if (myAddress==null) {
				throw new Exception("no IP found");
			}
*/

		}
		catch (Exception e) { // if network has not been setup
			myAddress = "nonet" + randomString;
		}

		RiskGame.setDefaultMapAndCards( riskconfig.getProperty("default.map") , riskconfig.getProperty("default.cards") );
		port = Integer.parseInt( riskconfig.getProperty("default.port") );

		battle = false;
		replay = false;

		controller = new RiskController();

		inbox = new Vector();
		this.start();

	}

	/**
	 * This gets the next token of the string tokenizer
	 * @return String Returns the next token as a string
	 */
	public String GetNext() {
		return StringT.nextToken();
	}

	public String getRiskConfig(String a) {

		return riskconfig.getProperty(a);

	}


	public void addRiskListener(RiskListener o) {

		controller.addListener(o);

		setHelp();

	}

	public void deleteRiskListener(RiskListener o) {

		controller.deleteListener(o);

	}

	/**
	 * This parses the string, calls the relavant method and displays the correct error messages
	 * @param m The string needed for parsing
	 */
	public void parser(String m) {

		//System.out.print("GOT: "+m+"\n");

		synchronized(inbox) {

			inbox.add(m);
			inbox.notify();
		}

	}

	public void run() {

		String message;

		try {

		    while (true) {

			synchronized(inbox) {

				if( inbox.isEmpty() ) {
					try { inbox.wait(); }
					catch(InterruptedException e) {
						System.err.println("InterruptedException in "+getName());
					}
				}

				try {

					message = (String)inbox.remove(0);

				}
				catch (ArrayIndexOutOfBoundsException ex) {

					// this should never happen but it does
					continue;

				}

			}

			//System.out.print("PROCESSING: "+message+"\n");

			String input;
			String output;

			StringT = new StringTokenizer( message );

			if (StringT.hasMoreTokens() == false) {
				controller.sendMessage(">", false, false );
				getInput();
				continue; // used to be return; when this was not a thread
			}
			else {
				input=GetNext();
				output="";
			}

			// Show version
			if (message.equals("ver")) {
				controller.sendMessage(">" + message, false, false );
				controller.sendMessage(RiskUtil.getGameName()+" Game Engine [Version " + RISK_VERSION + "]", false, false );

				getInput();
			}
			// take no action
			else if (input.equals("rem")) {
				controller.sendMessage(">" + message, false, false );
				//controller.sendMessage("no action", false, false );
				getInput();
			}
			// out of game commands
			else if (game==null) { // if no game

				controller.sendMessage(">" + message, false, false );

				// NEW GAME
				if (input.equals("newgame")) {

					if (StringT.hasMoreTokens()==false) {

						// already checked
						//if (game == null) {

							try {

								// CREATE A GAME
								game = new RiskGame();

								// NO SERVER OR CLIENT IS STARTED

								unlimitedLocalMode = true;

								controller.newGame(true);

								setupPreview();

								controller.showCardsFile( game.getCardsFile() , true);

								output=resb.getString( "core.newgame.created");
							}
							catch (Exception e) {
e.printStackTrace();
								output=resb.getString( "core.newgame.error") + " " + e.toString();
							}
						//}
						//else {
						//	output=resb.getString( "core.newgame.alreadyloaded");
						//}
					}
					else { output=resb.getString( "core.error.syntax").replaceAll( "\\{0\\}", "newgame"); }

				}

				// LOAD GAME
				else if (input.equals("loadgame")) {

					if (StringT.countTokens() >= 1) {

						// this is not needed here as u can only get into this bit of code if game == null
						//if (game == null) {
							String filename = GetNext();

							while ( StringT.hasMoreElements() ) {
								filename = filename + " " + GetNext();
							}

							try {

								game = RiskGame.loadGame( filename );

								if (game == null) {
									throw new Exception("no game");
								}

								unlimitedLocalMode = true;

								if (game.getState()==RiskGame.STATE_NEW_GAME) { controller.newGame(true); }
								else { controller.startGame(unlimitedLocalMode); }

								output=resb.getString( "core.loadgame.loaded");

								Player player = game.getCurrentPlayer();

								if ( player != null ) {

									// the game is saved
									saveGameToUndoObject();

									output=output+ System.getProperty("line.separator") + resb.getString( "core.loadgame.currentplayer") + " " + player.getName();

								}

							}
							catch (Exception ex) {
                                                                ex.printStackTrace();
								output=resb.getString( "core.loadgame.error.load")+" "+ex;
								showMessageDialog(output);
							}
						//}
						//else {
						//	output=resb.getString( "core.newgame.alreadyloaded");
						//}

					}
					else { output=resb.getString( "core.error.syntax").replaceAll( "\\{0\\}", "loadgame filename"); }

				}

				else if (input.equals("join")) {

					if (StringT.countTokens() == 1) {

						// already checked
						//if (game == null) {

							// CREATE A CLIENT
							try {

								chatSocket = new Socket( GetNext() , port);

								// Create a PrintWriter object for socket output

								outChat = new PrintWriter(
										chatSocket.getOutputStream(), true);

								// Create a BufferedReader object for socket input

								inChat = new BufferedReader(
										new InputStreamReader(
												chatSocket.getInputStream()));

								myReader = new ChatDisplayThread(this, inChat);
								myReader.start();

								// CREATE A GAME
								game = new RiskGame();

								unlimitedLocalMode = false;

								controller.newGame(false);

								setupPreview();

								controller.showCardsFile( game.getCardsFile() , true);

								output=resb.getString( "core.join.created");


								//if ( chatSocket==null ) { controller.closeGame(); throw new ConnectException("conection refused"); }

								outChat.println( RiskGame.NETWORK_VERSION +" "+RiskGame.getDefaultMap() );

							}
							catch (UnknownHostException e) {
								game = null;
								output=resb.getString( "core.join.error.unknownhost");
							}
							catch (ConnectException e) {
								game = null;
								output=resb.getString( "core.join.error.connect");
							}
							catch (IllegalArgumentException e) {
								game = null;
								output=resb.getString( "core.join.error.nothostname");
							}
							catch (IOException e) {
								game = null;
								output=resb.getString( "core.join.error.002");
							}
							catch (java.security.AccessControlException e) {
								game = null;
								output="AccessControlException:\n"+resb.getString( "core.error.applet");
							}
							catch (Exception e) { // catch not being able to make a new game, so game is null
                                                                game=null; // just in case ;-)
								output=resb.getString( "core.join.error.create")+" "+e;
							}


							if (game==null) {
								showMessageDialog(output);
							}

						//}
						//else {
						//	output=resb.getString( "core.join.error.001");
						//}
					}
					else { output=resb.getString( "core.error.syntax").replaceAll( "\\{0\\}", "join server"); }

				}

				// NEW SERVER
				else if (input.equals("startserver")) {

					if (StringT.hasMoreTokens()==false) {

						if ( chatter == null ) {

							// CREATE A SERVER
							try {

								chatter = new ChatArea(controller,port);

								output=resb.getString( "core.startserver.started");
								controller.serverState(true);

							}
							catch(Exception e) {

								chatter = null;
								output=resb.getString( "core.startserver.error")+" "+e;
								showMessageDialog(output);

							}

						}
						else {
							output=resb.getString( "core.startserver.error");
						}
					}
					else { output=resb.getString( "core.error.syntax").replaceAll( "\\{0\\}", "startserver"); }

				}
				// KILL SERVER
				else if (input.equals("killserver")) {

					if (StringT.hasMoreTokens()==false) {

						if ( chatter != null ) {

							try {

								// shut down the server
								//if (chatter.serverSocket != null) {
								//	chatter.serverSocket.close();
								//	chatter=null;
								//}

								if (chatter != null) {
									chatter.closeSocket();
									chatter=null;
								}

								output=resb.getString( "core.killserver.killed");
								controller.serverState(false);


							}
							catch (Exception e) {
								output=resb.getString( "core.killserver.error")+" "+e.getMessage();
							}


						}
						else {
							output=resb.getString( "core.killserver.noserver");
						}
					}
					else { output=resb.getString( "core.error.syntax").replaceAll( "\\{0\\}", "killserver"); }

				}

				else { // if there is no game and the command was unknown
					output=resb.getString( "core.loadgame.nogame");
				}

				// if there was NO game

				controller.sendMessage(output, false, true );

				setHelp();

				getInput();

			}
			// IN GAME COMMANDS
			else {

				//replace all country names with there numbers

				// 5 commands have to be checked:

				// capital	x1
				// trade	x3
				// placearmies	x1
				// attack	x2
				// movearmies	x2

				// send the message to all the clients

				String mtemp = myAddress+" " + message;


				if ( chatSocket == null ) {

					//GameParser( "game " + message );
					// change for yura:lobby

					GameParser( mtemp );

				}
				else { // if this is a network game
					outChat.println( mtemp );
				}


			}

			//System.out.print("END PROCESSING\n");

		    }
		}
		catch(RuntimeException ex) {

			System.err.println("FAITAL ERROR IN RISK");

			ex.printStackTrace();
			run();
		}

	}

	private void saveGameToUndoObject() {


			if ( unlimitedLocalMode ) {

				// the game is saved
				try {
					//Undo = new SealedObject( game, nullCipher );


					Undo.reset();
					ObjectOutputStream out = new ObjectOutputStream(Undo);
					out.writeObject(game);
					out.flush();
					out.close();

				}
				catch (Throwable e) {
					System.out.print(resb.getString( "core.loadgame.error.undo") + "\n");
					e.printStackTrace();
				}
			}
	}

	/**
	 * This parses the string, calls the relavant method and displays the correct error messages
	 * @param mem The string needed for parsing
	 */
	public void GameParser(String message) {

		controller.sendDebug(message);

		game.addCommand(message);

		int oldState=game.getState();

		boolean needInput=true;

		String input;
		String output=null;

		StringT = new StringTokenizer( message );

		String Addr = GetNext();

		if (Addr.equals("ERROR")) { // server has sent us a error

			String Pname = GetNext();

			while ( StringT.hasMoreElements() ) {
				Pname = Pname +" "+ GetNext();
			}

			showMessageDialog(Pname);

		}
		else if (Addr.equals("DICE")) { // a server command

			int attSize = RiskGame.getNumber(GetNext());
			int defSize = RiskGame.getNumber(GetNext());

			output=resb.getString( "core.dice.rolling") + System.getProperty("line.separator") + resb.getString( "core.dice.results");

			int att[] = new int[ attSize ];
			output = output + " " + resb.getString( "core.dice.attacker");
			for (int c=0; c< attSize ; c++) {
				att[c] = RiskGame.getNumber(GetNext());
				output = output + " " + (att[c]+1);
			}

			int def[] = new int[ defSize ];
			output = output + " " + resb.getString( "core.dice.defender");
			for (int c=0; c< defSize ; c++) {
				def[c] = RiskGame.getNumber(GetNext());
				output = output + " " + (def[c]+1);
			}

			output = output + System.getProperty("line.separator");

			int result[] = game.battle( att, def );

			if ( result[0]==1 ) {
				output = output + resb.getString( "core.dice.result").
									replaceAll( "\\{0\\}", "" + result[2]).//defeated
									replaceAll( "\\{1\\}", "" + result[1]);//lost


				if (result[3]==0) {
					int n=((Country)game.getAttacker()).getArmies()-1;

					output=output + System.getProperty("line.separator") + resb.getString( "core.dice.notdefeated") + " ";

					if (n > 0) {
						if (n > 3) { n=3; }
						output=output + resb.getString( "core.dice.attackagain").replaceAll( "\\{0\\}", "" + n);

						Player attackingPlayer = ((Country)game.getAttacker()).getOwner();

						if ( showHumanPlayerThereInfo( attackingPlayer ) ) {

							controller.showDice(n, true);

						}


					}
					else {
						output=output + resb.getString( "core.dice.noattackagain");
					}


				}
				else {

					Player attackingPlayer = ((Country)game.getAttacker()).getOwner();

					if ( showHumanPlayerThereInfo( attackingPlayer ) ) {

						//controller.setSlider(result[4], result[5]);
						controller.setSlider(result[4], ((Country)game.getAttacker()).getColor(), ((Country)game.getDefender()).getColor() );

					}

					output=output + System.getProperty("line.separator") + resb.getString( "core.dice.defeated") + " ";

					if ( result[3]==2 ) {
						output=output + resb.getString( "core.dice.eliminated") + " ";
					}

					// if there is only one amount of troops u can move
					if ( result[4] == result[5] ) {

						int noa = game.moveAll();

						int ma = game.moveArmies( noa );

						//Moved {0} armies to captured country.
						output=output + resb.getString( "core.dice.armiesmoved").replaceAll( "\\{0\\}", "" + noa);

						if (ma==2) {

							output=output + whoWon();

						}

					}
					else {
						//How many armies do you wish to move? ({0} to {1})
						output=output + resb.getString( "core.dice.howmanyarmies")
								.replaceAll( "\\{0\\}", "" + result[4])
								.replaceAll( "\\{1\\}", "" + result[5]);
					}


				}

				if ( battle ) {

					controller.showDiceResults( att, def );

					try{ Thread.sleep(1000); }
					catch(InterruptedException e){}

				}

			}
			else { output=resb.getString( "core.dice.error.unabletoroll"); }

			// ==1 this fixes the automove bug, when u need to trade after rolling and automove
			if ( game.getState()!=RiskGame.STATE_ROLLING && game.getState()!=RiskGame.STATE_DEFEND_YOURSELF) {

				closeBattle();

			}




		}
		else if (Addr.equals("PLAYER")) { // a server command

			String Pname = GetNext();

			while ( StringT.hasMoreElements() ) {
				Pname = Pname +" "+ GetNext();
			}

			Player p = game.setCurrentPlayer( Pname );

			controller.sendMessage("Game started", false, false);

			// moved to startgame for lobby
			//if ( chatSocket != null ) {
			//	controller.startGame(false);
			//}
			//else {
			//	controller.startGame(true);
			//}

			output=resb.getString( "core.player.randomselected").replaceAll( "\\{0\\}", p.getName());

			if ( game.getGameMode()==RiskGame.MODE_SECRET_MISSION || autoplaceall==true ) {
				needInput=false;
			}
			else {

				saveGameToUndoObject();
			}

		}
		else if (Addr.equals("CARD")) { // a server command

			// if the player deserves a card
			if ( StringT.hasMoreTokens() ) {

				// get the cards
				Vector cards = game.getCards();
				String name = GetNext();
				Card card = game.findCard( name );

				((Player)game.getCurrentPlayer()).giveCard( card );

				// find the card and remove it
				cards.remove( card );
				cards.trimToSize();

				if ( showHumanPlayerThereInfo( game.getCurrentPlayer() ) ) {

					String cardName;

					if (name.equals(Card.WILDCARD)) {
						cardName = name;
					}
					else {
						cardName = card.getName() + " " + game.getCountryInt( Integer.parseInt(name) ).getName();
					}

					controller.sendMessage("You got a new card: \"" + cardName +"\"", false , false);
				}
			}

			Player newplayer = game.endGo();

			output = resb.getString( "core.player.newselected").replaceAll( "\\{0\\}", newplayer.getName());

			// this is not a bug! (Easter egg)
			if ( unlimitedLocalMode && game.getSetup() && newplayer.getName().equals("Theo")) { newplayer.addArmies( newplayer.getExtraArmies() ); }

			saveGameToUndoObject();


		}
		else if (Addr.equals("PLACE")) { // a server command

			Country c = game.getCountryInt( Integer.parseInt( GetNext() ) );
			game.placeArmy( c ,1);
			controller.sendMessage( resb.getString( "core.place.oneplacedin").replaceAll( "\\{0\\}", c.getName()) , false, false); // Display
			output=resb.getString( "core.place.autoplaceok");

		}
		else if (Addr.equals("PLACEALL")) { // a server command

			for (int c=0; c< game.getNoCountries() ; c++) {

				Country t = game.getCountryInt( Integer.parseInt( GetNext() ) );
				game.placeArmy( t ,1);
				controller.sendMessage( resb.getString("core.place.getcountry")
						.replaceAll( "\\{0\\}", ((Player)game.getCurrentPlayer()).getName())
						.replaceAll( "\\{1\\}", t.getName()) // Display
						, false, false);
				game.endGo();
			}

			// the game is saved
			saveGameToUndoObject();

			controller.sendMessage("Auto place all successful.", false, false);
			//New player selected: {0}.
			output=resb.getString( "core.player.newselected").replaceAll( "\\{0\\}", ((Player)game.getCurrentPlayer()).getName());

		}
		else if (Addr.equals("MISSION")) { // a server command

			Vector m = game.getMissions();
			Vector p = game.getPlayers();

			for (int c=0; c< p.size() ; c++) {

				int i = RiskGame.getNumber( GetNext() );
				((Player)p.elementAt(c)).setMission( (Mission)m.elementAt(i) );
				m.removeElementAt(i);

			}

			output=null;
			needInput=false;

		}
		else { // parse this normal cammand

			String echo = message.substring( Addr.length()+1 );

			if (game != null && game.getCurrentPlayer() != null && game.getState()!=RiskGame.STATE_GAME_OVER ) {

				if ( ((Player)game.getCurrentPlayer()).getType()==Player.PLAYER_HUMAN ) { // if the player is human
					controller.sendMessage( ((Player)game.getCurrentPlayer()).getName()+ "("+resb.getString("newgame.player.type.human")+")>"+echo, false, false );
				}
				else if ( ((Player)game.getCurrentPlayer()).getType()==Player.PLAYER_AI_CRAP ) { // if the player is AI
					controller.sendMessage( ((Player)game.getCurrentPlayer()).getName()+ "("+resb.getString("newgame.player.type.crapai")+")>"+echo, false, false );
				}
				else if ( ((Player)game.getCurrentPlayer()).getType()==Player.PLAYER_AI_EASY ) { // if the player is AI
					controller.sendMessage( ((Player)game.getCurrentPlayer()).getName()+ "("+resb.getString("newgame.player.type.easyai")+")>"+echo, false, false );
				}
				else if ( ((Player)game.getCurrentPlayer()).getType()==Player.PLAYER_AI_HARD ) { // if the player is AI
					controller.sendMessage( ((Player)game.getCurrentPlayer()).getName()+ "("+resb.getString("newgame.player.type.hardai")+")>"+echo, false, false );
				}

			}
			else {
				controller.sendMessage( "game>" + echo, false, false );
			}

			//if (StringT.hasMoreTokens()) {

			input=GetNext();
			output="";

			// CLOSE GAME
			if (input.equals("closegame")) {

				if (StringT.hasMoreTokens()==false) {

                                    boolean doclose=false;

                                    if ( chatSocket == null ) { // LOCAL GAME/ OR LOBBY CLIENT

                                            doclose = true;

                                    }
                                    else { // NETWORK GAME

                                            if ( myAddress.equals(Addr) ) { // only on the pc of who called it

                                                    doclose = true;

                                                    try {
                                                            outChat.close();
                                                            inChat.close();

                                                            chatSocket.shutdownInput();
                                                            chatSocket.shutdownOutput();

                                                            chatSocket.close();
                                                    }
                                                    catch (IOException except) { }

                                                    chatSocket = null;

                                            }
                                            else {


                                                    // @todo, do we set needinput to flase, so that ai wont go twice, if its there go
                                                    // but then if there needinput was ignored coz this command was in the inbox, no needinput will get called

                                                    // if it is NOT there go the set needinput to false
                                                    if (game.getCurrentPlayer()!=null && !(game.getCurrentPlayer().getAddress().equals(Addr) && game.getCurrentPlayer().getType() == Player.PLAYER_HUMAN)) {

                                                            needInput = false;

                                                    }
                                                    // if this command stopped the last needInput from being called, then this will be screwed
                                                    // at worst AI or human wont get a chance to put any input in, game stalled

                                                    output = "someone has gone: ";

                                                    // get all the players and make all with the ip of the leaver become nutral
                                                    Vector leavers = game.getPlayers();

                                                    String newPlayerAddress="";

                                                    // find the first player that is NOT playing on this computer
                                                    // this happens in the same way on each computer
                                                    for (int c=0; c< leavers.size() ; c++) {

                                                            if ( !((Player)leavers.elementAt(c)).getAddress().equals(Addr) ) {


                                                                    newPlayerAddress = ((Player)leavers.elementAt(c)).getAddress();
                                                                    break;
                                                            }

                                                    }


                                                    for (int c=0; c < leavers.size(); c++) {

                                                            Player patc = ((Player)leavers.elementAt(c));

                                                            if ( patc.getAddress().equals(Addr) ) {

                                                                    if ( patc.getType() == Player.PLAYER_HUMAN ) {

                                                                            output = output + patc.getName()+" ";

                                                                            if (game.getState() == RiskGame.STATE_NEW_GAME ) {

                                                                                    // should never return false
                                                                                    if ( game.delPlayer( patc.getName() ) ) {

                                                                                            c--;

                                                                                            controller.delPlayer( patc.getName() );

                                                                                            patc = null;
                                                                                    }

                                                                            }
                                                                            else {

                                                                                    patc.setType( Player.PLAYER_AI_CRAP );

                                                                            }
                                                                    }

                                                                    if (patc!=null) {

                                                                            if (newPlayerAddress!=null) {
                                                                                    patc.setAddress( newPlayerAddress );
                                                                            }
                                                                            else {

                                                                                    // this means there are only spectators left
                                                                                    // so nothing really needs to be done
                                                                                    // game will stop, but hay there r no more players
                                                                            }
                                                                    }

                                                            }

                                                    }

                                            }


                                    }

                                    if (doclose) {
                                        closeGame();
                                        output=resb.getString( "core.close.closed");
                                    }
				}
				else {
                                    output=resb.getString( "core.error.syntax").replaceAll( "\\{0\\}", "closegame");
                                }

			}

/* THIS DOES NOT WORK AT ALL, ONLY HERE FOR HISTORY

			// LEAVE GAME
			else if (input.equals("leave")) {

				if (StringT.hasMoreTokens()==false) {

					if (chatSocket != null) {

						if (game.getState() != RiskGame.STATE_NEW_GAME ) {

							// get all the players and make all with the ip of the leaver become nutral
							Vector leavers = game.getPlayers();

							for (int c=0; c< leavers.size() ; c++) {

								if ( ((Player)leavers.elementAt(c)).getAddress().equals(Addr) ) {

									((Player)leavers.elementAt(c)).setType(3); WRONG!
									((Player)leavers.elementAt(c)).setAddress("all"); WRONG!
								}

							}

						}

						try {

							// shut down the client
							if (chatSocket !=null && myAddress.equals(Addr) ) {
								game = null;
								outChat.close();
								inChat.close();
								chatSocket.close();
								chatSocket = null;
							}

						}
						catch (IOException except) {
							//System.out.println("IOException in main");
						}

						if (game == null) {

							// does not work from here
							closeBattle();

							controller.closeGame();
							output=resb.getString( "core.leave.left");
						}
						else {
							//{0} has left the game.
							if (game.getState()==RiskGame.STATE_NEW_GAME) {
								output = "someone has gone";
							}
							else {
								This will not work if the player quits when its not there go

								output = resb.getString("core.leave.otherhasleft").replaceAll("\\{0\\}", ((Player)game.getCurrentPlayer()).getName());
							}
						}

					}
					else {
						output=resb.getString( "core.leave.error.nonetwork");
					}

				}
				else { output=resb.getString( "core.error.syntax").replaceAll( "\\{0\\}", "leave"); }

			}
*/
			else if (game.getState()==RiskGame.STATE_NEW_GAME) {

				if (input.equals("choosemap")) {

					if (StringT.countTokens() >= 1) {
						String filename=GetNext();

						while ( StringT.hasMoreElements() ) {
							filename = filename + " " + GetNext();
						}

						try {
                                                    setMap(filename);
						}
						catch (Exception e) {
                                                    // crap, we wanted to use this map, but we would not load it
                                                    // maybe we can download it from the server and then use it
                                                    RiskUtil.streamOpener.getMap(filename,this);
						}

					}
					else  { output=resb.getString( "core.error.syntax").replaceAll( "\\{0\\}", "choosemap filename"); }

				}
				else if (input.equals("choosecards")) {

					if (StringT.countTokens() >= 1) {
						String filename=GetNext();
						while ( StringT.hasMoreElements() ) {
							filename = filename + " " + GetNext();
						}

						try {

							boolean yesmissions = game.setCardsfile(filename);

							controller.showCardsFile( game.getCardsFile() , yesmissions );
							//New cards file selected: "{0}"
							output=resb.getString( "core.choosecards.chosen").replaceAll( "\\{0\\}", filename);
						}
						catch (Exception e) {
							output=resb.getString( "core.choosecards.error.unable");
						}
					}
					else  { output=resb.getString( "core.error.syntax").replaceAll( "\\{0\\}", "choosecards filename"); }

				}
				else if (input.equals("newplayer")) {

					if (StringT.countTokens()>=3) {

						String type=GetNext();
						if (type.equals("ai")) {
							type = type+" "+GetNext();
						}

						String c=GetNext();

						String name="";
						while ( StringT.hasMoreElements() ) {
							name = name + GetNext();
							if ( StringT.hasMoreElements() ) { name = name + " "; }
						}
						name = name.replaceAll("\\$","").replaceAll("\\\\","").replaceAll(" +"," ");

						// YURA:LOBBY CHANGE
						// ( ( chatSocket != null && game.addPlayer(t, name, color, Addr) ) || ( chatSocket == null && game.addPlayer(t, name, color, "all") ) )
						// name.replaceAll(" ","")+"#"+String.valueOf( Math.round(Math.random()*Long.MAX_VALUE) )

						int t=getType(type);
						int color=RiskUtil.getColor( c );

						if ( color != 0 && t != -1 && !name.equals("") && (   (  unlimitedLocalMode && game.addPlayer(t, name, color, "LOCALGAME" ) ) || ( !unlimitedLocalMode && game.addPlayer(t, name, color, Addr)    )    ) ) {
							//New player created, name: {0} color: {1}
							output=resb.getString( "core.newplayer.created")
										.replaceAll( "\\{0\\}", name)
										.replaceAll( "\\{1\\}", c);

							controller.addPlayer(t, name, color, Addr);
							//System.out.print("New player has Address: "+Addr+"\n");

						}
						else { output=resb.getString( "core.newplayer.error.unable"); }



					}
					else  { output=resb.getString( "core.error.syntax").replaceAll( "\\{0\\}", "newplayer type (skill) color name"); }

				}
				else if (input.equals("delplayer")) {

					if (StringT.countTokens()>=1) {
						String name=GetNext();

						while ( StringT.hasMoreElements() ) {
							name = name +" "+ GetNext();
						}

						if ( game.delPlayer(name) ) {
							controller.delPlayer(name);
							output=resb.getString( "core.delplayer.deleted").replaceAll( "\\{0\\}", name);
						}
						else { output=resb.getString( "core.delplayer.error.unable"); }
					}
					else { output=resb.getString( "core.error.syntax").replaceAll( "\\{0\\}", "delplayer name"); }

				}
				else if (input.equals("info")) {

					if (StringT.hasMoreTokens()==false) {

						output=resb.getString( "core.info.title") + "\n";

						Vector players = game.getPlayers();

						for (int a=0; a< players.size() ; a++) {

							output = output + resb.getString( "core.info.player") + " " + ((Player)players.elementAt(a)).getName() +"\n";

						}

						output = output + resb.getString( "core.info.mapfile") + " "+ game.getMapFile() +"\n";
						output = output + resb.getString( "core.info.cardsfile") + " "+ game.getCardsFile() ;

					}
					else { output=resb.getString( "core.error.syntax").replaceAll( "\\{0\\}", "info"); }

				}
				else if (input.equals("autosetup")) {

					if (StringT.hasMoreTokens()==false) {

						if ( game.getPlayers().size() == 0) {

						    if (!replay) {

							for (int c=1;c<=RiskGame.MAX_PLAYERS;c++) {

								parser("newplayer " + riskconfig.getProperty("default.player"+c+".type")+" "+ riskconfig.getProperty("default.player"+c+".color")+" "+ riskconfig.getProperty("default.player"+c+".name") );

							}

							output = resb.getString( "core.info.autosetup");

						    }
						    else {

							output = "replay mode, nothing done";

						    }

						}
						else {

							output = resb.getString( "core.info.autosetup.error");

						}

					}
					else { output=resb.getString( "core.error.syntax").replaceAll( "\\{0\\}", "autosetup"); }

				}
				else if (input.equals("startgame")) {
					if (StringT.countTokens() >= 2 && StringT.countTokens() <= 4) {

						int n=((Vector)game.getPlayers()).size();

						int newgame_type = -1;
						int newgame_cardType = -1;
						boolean newgame_autoplaceall = false;
						boolean newgame_recycle = false;
                                                boolean threeDice = false;

						String crap = null;

						while (StringT.hasMoreTokens()) {
							String newOption = GetNext();
							if ( newOption.equals("domination") ) {
								newgame_type = RiskGame.MODE_DOMINATION;
							}
							else if ( newOption.equals("capital") ) {
								newgame_type = RiskGame.MODE_CAPITAL;
							}
							else if ( newOption.equals("mission") ) {
								newgame_type = RiskGame.MODE_SECRET_MISSION;
							}
							else if ( newOption.equals("increasing") ) {
								newgame_cardType = RiskGame.CARD_INCREASING_SET;
							}
							else if ( newOption.equals("fixed") ) {
								newgame_cardType = RiskGame.CARD_FIXED_SET;
							}
							else if ( newOption.equals("italianlike") ) {
								newgame_cardType = RiskGame.CARD_ITALIANLIKE_SET;
                                                                threeDice = true;
							}
							else if ( newOption.equals("autoplaceall") ) {
								newgame_autoplaceall = true;
							}
							else if ( newOption.equals("recycle") ) {
								newgame_recycle = true;
							}
							else {
								crap = newOption;
							}
						}

                                                if (crap==null) {
                                                    // checks all the options are correct to start a game
                                                    if ( newgame_type!=-1 && newgame_cardType!=-1 && n>=2 && n<=RiskGame.MAX_PLAYERS) {

                                                            autoplaceall = newgame_autoplaceall;

                                                            try {

                                                                    game.startGame(newgame_type,newgame_cardType,newgame_recycle,threeDice);

                                                            }
                                                            catch (Exception e) {

                                                                    e.printStackTrace();

                                                            }

                                                    }

                                                    // this checks if the game was able to start or not
                                                    if (game.getState() != RiskGame.STATE_NEW_GAME ) {

                                                        controller.noInput();

                                                        controller.startGame( unlimitedLocalMode );

                                                        if (!replay) {

                                                            // this cant be if(unlimitedLocalMode) as then it wont get called on the lobby server, as it IS restricted
                                                            if ( chatSocket == null ) {
                                                                    GameParser( "PLAYER " + game.getRandomPlayer() );
                                                            }
                                                            else if ( myAddress.equals(Addr) ) { // if this is a network game
                                                                    outChat.println( "PLAYER " + game.getRandomPlayer() ); // recursive call
                                                            }


                                                            // do that mission thing
                                                            if (game.getGameMode()== RiskGame.MODE_SECRET_MISSION ) {

                                                                    // do that mission thing

                                                                    if ( chatSocket == null || myAddress.equals(Addr) ) {

                                                                            // give me a array of random numbers
                                                                            Random r = new Random();
                                                                            int a = game.getNoMissions();
                                                                            int b = game.getNoPlayers();

                                                                            String outputa="MISSION";

                                                                            for (int c=0; c< b ; c++) {

                                                                                    outputa=outputa + " " + r.nextInt(a) ;
                                                                                    a--;

                                                                            }

                                                                            if (chatSocket == null) {
                                                                                    GameParser( outputa );
                                                                            }
                                                                            else if ( myAddress.equals(Addr) ) {
                                                                                    outChat.println( outputa );
                                                                            }

                                                                    }

                                                            }

                                                            // do that autoplace thing
                                                            if ( game.getGameMode()==RiskGame.MODE_SECRET_MISSION || autoplaceall==true ) {


                                                                    if ( chatSocket == null || myAddress.equals(Addr) ) {

                                                                            Vector a = game.shuffleCountries();

                                                                            String outputb="PLACEALL";

                                                                            for (int c=0; c< a.size() ; c++) {

                                                                                    outputb=outputb + " " + ((Country)a.elementAt(c)).getColor();
                                                                                    //outputb=outputb + " " + ((Country)a.elementAt(c)).getName() ;

                                                                            }

                                                                            if (chatSocket == null) {
                                                                                    GameParser( outputb );
                                                                            }
                                                                            else if ( myAddress.equals(Addr) ) {
                                                                                    outChat.println( outputb );
                                                                            }


                                                                    }

                                                            }

                                                        }

                                                        output=null;
                                                        needInput=false;

                                                    }
                                                    else {
                                                        output=resb.getString( "core.start.error.players");
                                                    }
                                                }
                                                else {
                                                    output="unknown option: "+crap;
                                                }
					}
					else {
                                            output=resb.getString( "core.error.syntax").replaceAll( "\\{0\\}", "startgame gametype cardtype (autoplaceall recycle)");
                                        }
				}

				// REPLAY A GAME
				else if (input.equals("play")) {

					if (StringT.countTokens() >= 1) {

						String filename = GetNext();

						while ( StringT.hasMoreElements() ) {
							filename = filename + " " + GetNext();
						}

						try {

							URL url;

							// TODO dont think this can ever work as an applet anyway
							//if (Risk.applet==null) {

								url = (new File(filename)).toURI().toURL();

							//}
							//else {
							//	url = new URL( net.yura.domination.engine.Risk.applet.getCodeBase() , filename );
							//}

							BufferedReader bufferin=new BufferedReader(new InputStreamReader(url.openStream()));


							//create thread with bufferin
							class Replay extends Thread {

							    private Risk risk;
							    private BufferedReader bufferin;

							    public Replay(Risk r, BufferedReader in) {

								risk=r;
								bufferin=in;

							    }

							    public void run() {

								try {

								    String input = bufferin.readLine();
								    while(input != null) {

									//System.out.print(input+"\n");

									risk.GameParser(input);
									input = bufferin.readLine();

								    }

								    bufferin.close();

								}
								catch(Exception error) {

								    error.printStackTrace();

								}

								//set replay off
								replay = false;
								getInput();
							    }

							}

							Thread replaythread = new Replay(this, bufferin);

							//set boolean that replay is on
							replay = true;

							replaythread.start();

							output="playing \""+filename+"\"";


						}
						catch(Exception error) {
							output="unable to play \""+filename+"\"";
						}



					}
					else { output=resb.getString( "core.error.syntax").replaceAll( "\\{0\\}", "play filename"); }

				}


				else { output=resb.getString( "core.error.incorrect").replaceAll( "\\{0\\}", "newplayer, delplayer, startgame, choosemap, choosecards, info, autosetup"); }

			}

			// UNDO
			else if (input.equals("undo")) {

				if ( StringT.hasMoreTokens()==false ) {

				    if ( unlimitedLocalMode ) {

					try {
						if (Undo!=null && Undo.size()!=0) {

							ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(Undo.toByteArray()));
							game = (RiskGame)in.readObject();
							//game = (RiskGame)Undo.getObject( nullCipher );

							output =resb.getString( "core.undo.undone");
						}
						else {
							output =resb.getString( "core.undo.error.unable");
						}
					}
					catch (Exception e) {
						System.out.print(resb.getString( "core.loadgame.error.undo") + "\n");
						e.printStackTrace();
					}
				    }
				    else {
					output = resb.getString( "core.undo.error.network");
				    }

				}
				else { output=resb.getString( "core.error.syntax").replaceAll( "\\{0\\}", "undo"); }


			}


			// REPLAY A GAME FROM THE GAME FILE
			else if (input.equals("replay")) {

				if ( StringT.hasMoreTokens()==false ) {

				    if ( unlimitedLocalMode ) {

					try {

						Vector replayCommands = game.getCommands();
						replayCommands.remove( replayCommands.size()-1 );

						saveGameToUndoObject();

						game = new RiskGame();

						replay = true;

						for (Enumeration e = replayCommands.elements() ; e.hasMoreElements() ;) {

							GameParser( (String)e.nextElement() );

							//try{ Thread.sleep(1000); }
							//catch(InterruptedException e){}

						}

						replay = false;

						output="replay of game finished";

					}
					catch (Exception e) {
						System.out.print(resb.getString( "core.loadgame.error.undo") + "\n");
						e.printStackTrace();
					}
				    }
				    else {
					output = resb.getString( "core.undo.error.network");
				    }

				}
				else { output=resb.getString( "core.error.syntax").replaceAll( "\\{0\\}", "replay"); }

			}

			// SAVE GAME
			else if (input.equals("savegame")) {

				if (StringT.countTokens() >= 1) {

				    if ( unlimitedLocalMode ) {

					Vector replayCommands = game.getCommands();
					replayCommands.remove( replayCommands.size()-1 );

					String filename = GetNext();

					while ( StringT.hasMoreElements() ) {
						filename = filename + " " + GetNext();
					}

					if (battle) {
						output=resb.getString( "core.save.error.unable"); // TODO better error message, can not save during battle
                                                showMessageDialog(output);
                                        }
                                        else {
                                            try {
                                                game.saveGame(filename);
                                                output=resb.getString( "core.save.saved");
                                            }
                                            catch (Exception ex) {
                                                ex.printStackTrace();
                                                output=resb.getString( "core.save.error.unable")+" "+ex;
                                                showMessageDialog(output);
                                            }
					}

				    }
				    else {
					output = resb.getString( "core.save.error.unable" );
				    }

				}
				else { output=resb.getString( "core.error.syntax").replaceAll( "\\{0\\}", "savegame filename"); }
			}

			else if (input.equals("showmission")) {
				if (StringT.hasMoreTokens()==false) {

					// only show to the right player!

					if ( showHumanPlayerThereInfo( game.getCurrentPlayer() ) ) {
						output = resb.getString( "core.showmission.mission") + " " + getCurrentMission();
					}
					else { output=resb.getString( "core.showmission.error"); }
				}
				else { output=resb.getString( "core.error.syntax").replaceAll( "\\{0\\}", "showmission"); }
			}
			else if (input.equals("showarmies")) {
				if (StringT.hasMoreTokens()==false) {

					if ( game.getState() != RiskGame.STATE_NEW_GAME) {

						Country[] v = game.getCountries();

						output=resb.getString( "core.showarmies.countries") + System.getProperty("line.separator");

						for (int c=0; c< v.length ; c++) {

							output = output + v[c].getColor() + " " + v[c].getName()+" - "; // Display

							if ( v[c].getOwner() != null ) {
								output = output + ((Player)v[c].getOwner()).getName() +" ("+v[c].getArmies() +")";
								if (game.getGameMode() == 2 && game.getSetup() && game.getState() !=RiskGame.STATE_SELECT_CAPITAL) {

									Vector players = game.getPlayers();

									for (int a=0; a< players.size() ; a++) {

										if ( ((Player)players.elementAt(a)).getCapital() != null && ((Player)players.elementAt(a)).getCapital() == v[c] ) {
											output = output + " " + resb.getString( "core.showarmies.captial")
														.replaceAll( "\\{0\\}", ((Player)players.elementAt(a)).getName());
										}

									}

								}

								output = output + System.getProperty("line.separator");

							}
							else {
								output = output + resb.getString( "core.showarmies.noowner") + System.getProperty("line.separator");
							}

						}

					}
					else { output=resb.getString( "core.showarmies.error.unable"); }
				}
				else { output=resb.getString( "core.error.syntax").replaceAll( "\\{0\\}", "showarmies"); }
			}

			else if (input.equals("showcards")) {
				if (StringT.hasMoreTokens()==false) {

					if ( showHumanPlayerThereInfo( game.getCurrentPlayer() ) ) {

						Vector c = ((Player)game.getCurrentPlayer()).getCards();

						if (c.size() == 0) {
							output=resb.getString( "core.showcards.nocards");
						}
						else {
							output=resb.getString( "core.showcards.youhave");

							for (int a=0; a< c.size() ; a++) {

								if ( ((Card)c.elementAt(a)).getName().equals(Card.WILDCARD) ) {
									output = output + " " + Card.WILDCARD; // resb.getString( "core.showcards.wildcard"); // dont use this as user needs to type it in
								}
								else {
									output = output + " \"" + ((Card)c.elementAt(a)).getName() +" "+ ((Country)((Card)c.elementAt(a)).getCountry()).getName() +" ("+((Country)((Card)c.elementAt(a)).getCountry()).getColor()+")\""; // Display
								}

							}
						}

						if(game.getCardMode()==RiskGame.CARD_FIXED_SET) {
							output = output+"\n"+ resb.getString("cards.fixed");

						}
                                                else if(game.getCardMode()==RiskGame.CARD_ITALIANLIKE_SET) {
							output = output+"\n"+ resb.getString("cards.italianlike");

						}
						else {
							output = output+"\n"+ resb.getString("cards.nexttrade").replaceAll( "\\{0\\}", "" + getNewCardState());
						}

					}
					else { output=resb.getString( "core.showcards.error.unable"); }
				}
				else { output=resb.getString( "core.error.syntax").replaceAll( "\\{0\\}", "showcards"); }
			}

			else if (input.equals("autoendgo")) {
				if (StringT.hasMoreTokens()==false) {
					String strSelected;

					if ( game.getCurrentPlayer().getAutoEndGo() ) {
						strSelected = "core.autoendgo.on";
					}
					else {
						strSelected = "core.autoendgo.off";
					}
					output = resb.getString( "core.autoendgo.setto").replaceAll( "\\{0\\}", resb.getString( strSelected));
				}
				else if (StringT.countTokens() == 1) {

					String option = GetNext();
					if (option.equals("on") ) {
						game.getCurrentPlayer().setAutoEndGo(true);
						output = resb.getString( "core.autoendgo.setto").replaceAll( "\\{0\\}", resb.getString( "core.autoendgo.on"));
					}
					else if (option.equals("off") ) {
						game.getCurrentPlayer().setAutoEndGo(false);
						output = resb.getString( "core.autoendgo.setto").replaceAll( "\\{0\\}", resb.getString( "core.autoendgo.off"));
					}
					else { output=resb.getString( "core.autoendgo.error.unknown").replaceAll( "\\{0\\}", option); }

				}
				else { output=resb.getString( "core.error.syntax").replaceAll( "\\{0\\}", "autoendgo on/off"); }
			}

			else if (input.equals("autodefend")) {
				if (StringT.hasMoreTokens()==false) {

					String strSelected;
					if ( game.getCurrentPlayer().getAutoDefend() ) {
						strSelected = "core.autodefend.on";
					}
					else {
						strSelected = "core.autodefend.on";
					}
					output = resb.getString( "core.autodefend.setto").replaceAll( "\\{0\\}", resb.getString( strSelected));
				}
				else if (StringT.countTokens() == 1) {

					String option = GetNext();
					if (option.equals("on") ) {
						game.getCurrentPlayer().setAutoDefend(true);
						output = resb.getString( "core.autodefend.setto").replaceAll( "\\{0\\}", resb.getString( "core.autodefend.on"));
					}
					else if (option.equals("off") ) {
						game.getCurrentPlayer().setAutoDefend(false);
						output = resb.getString( "core.autodefend.setto").replaceAll( "\\{0\\}", resb.getString( "core.autodefend.off"));
					}
					else { output=resb.getString( "core.autodefend.error.unknown").replaceAll( "\\{0\\}", option); }

				}
				else { output=resb.getString( "core.error.syntax").replaceAll( "\\{0\\}", "autodefend on/off"); }
			}
			else if (game.getState()==RiskGame.STATE_TRADE_CARDS) {

				if (input.equals("trade")) {
					if (StringT.countTokens()==3) {
						// trade Japan wildcard Egypt
						int noa=0;

						Card cards[] = game.getCards(GetNext(),GetNext(),GetNext());

						if (cards[0] != null && cards[1] != null && cards[2] != null) { // if the player DOES HAVE all the cards he chose
							noa = game.trade(cards[0], cards[1], cards[2]);
						}

						if ( noa != 0 ) { // if the trade WAS SUCCESSFUL
							output=resb.getString( "core.trade.traded").replaceAll( "\\{0\\}", "" + noa);
						}
						else { output=resb.getString( "core.trade.error.unable"); }
					}
					else { output=resb.getString( "core.error.syntax").replaceAll( "\\{0\\}", "trade card card card"); }
				}
				else if (input.equals("endtrade")) {
					if (StringT.hasMoreTokens()==false) {

						if ( game.endTrade() ) {
							output=resb.getString( "core.trade.endtrade");
						}
						else { output=resb.getString( "core.trade.end.error.unable"); }
					}
					else { output=resb.getString( "core.error.syntax").replaceAll( "\\{0\\}", "endtrade"); }

				}
				else { output=resb.getString( "core.error.incorrect").replaceAll( "\\{0\\}", "showcards, trade, endtrade"); }

			}
			else if (game.getState()==RiskGame.STATE_PLACE_ARMIES) {

				if (input.equals("placearmies")) {
					if (StringT.countTokens()==2) {
						String country=GetNext();
						int c=RiskGame.getNumber( country );
						int num=RiskGame.getNumber( GetNext() );
						Country t;

						if (c != -1) {
							t=game.getCountryInt(c);
						}
						else {
							//YURA:LANG t=game.getCountryByName(country);
							t=null;
						}

						if ( t != null && num!=-1 && !( game.getGameMode() == 1 && t.getOwner() == null) && !( game.getGameMode() == 3 && t.getOwner() == null) ) {

							int result = game.placeArmy(t, num);

							if (result!=0) {
								//{0} new army placed in: {1}
								output= resb.getString( "core.place.placed")
										.replaceAll( "\\{0\\}", "" + num)
										.replaceAll( "\\{1\\}", t.getName()); // Display

								if (result == 2) {

									output=output + whoWon();

								}

							}
							else { output=resb.getString( "core.place.error.unable"); }

						}
						else { output=resb.getString( "core.place.error.invalid"); }
					}
					else { output=resb.getString( "core.error.syntax").replaceAll( "\\{0\\}", "placearmies country number"); }
				}
				else if (input.equals("autoplace")) {
					if (StringT.hasMoreTokens()==false) {

						if ( game.NoEmptyCountries() == false ) {

						    if (!replay) {

							if ( chatSocket == null) {
								GameParser( "PLACE " + game.getEmptyCountry() );
							}
							else if ( myAddress.equals(Addr) ) { // if this is a network game
								outChat.println( "PLACE " + game.getEmptyCountry() ); // recursive call
							}


						    }

						    needInput=false;
						    output = null;

						}
						else {
							output = resb.getString( "core.autoplace.error.unable");
						}

					}
					else { output=resb.getString( "core.error.syntax").replaceAll( "\\{0\\}", "autoplace"); }
				}
				else { output=resb.getString( "core.error.incorrect").replaceAll( "\\{0\\}", "showarmies, placearmies, autoplace"); }

			}
			else if (game.getState()==RiskGame.STATE_ATTACKING) {

				if (input.equals("attack")) {
					if (StringT.countTokens()==2) {

						String arg1=GetNext();
						String arg2=GetNext();
						int a1=RiskGame.getNumber(arg1);
						int a2=RiskGame.getNumber(arg2);

						Country country1;
						Country country2;

						if (a1 != -1) {
							country1=game.getCountryInt(a1);
						}
						else {
							//YURA:LANG country1=game.getCountryByName(arg1);
							country1=null;
						}

						if (a2 != -1) {
							country2=game.getCountryInt(a2);
						}
						else {
							//YURA:LANG country2=game.getCountryByName(arg2);
							country2=null;
						}

						int a[]=game.attack(country1, country2);

						if ( a[0]==1 ) {
							//Attack {0} ({1}) with {2} ({3}). (You can use up to {4} dice to attack)
							output = resb.getString( "core.attack.attacking")
										.replaceAll( "\\{0\\}", country2.getName()) // Display
										.replaceAll( "\\{1\\}", "" + country2.getArmies())
										.replaceAll( "\\{2\\}", country1.getName()) // Display
										.replaceAll( "\\{3\\}", "" + country1.getArmies())
										.replaceAll( "\\{4\\}", "" + a[1]);

							Player attackingPlayer = ((Country)game.getAttacker()).getOwner();

							if ( showHumanPlayerThereInfo( attackingPlayer ) ) {

								controller.showDice(a[1], true);
							}

						}
						else { output=resb.getString( "core.attack.error.unable"); }
					}
					else { output=resb.getString( "core.error.syntax").replaceAll( "\\{0\\}", "attack country country"); }
				}
				else if (input.equals("endattack")) {
					if (StringT.hasMoreTokens()==false) {
						if ( game.endAttack() ) {
							output=resb.getString( "core.attack.end.ended");
						}
						else { output=resb.getString( "core.attack.end.error.unable"); }
					}
					else { output=resb.getString( "core.error.syntax").replaceAll( "\\{0\\}", "endattack"); }
				}
				else { output=resb.getString( "core.error.incorrect").replaceAll( "\\{0\\}", "attack, endattack"); }

			}
			else if (game.getState()==RiskGame.STATE_ROLLING) {

				if (input.equals("roll")) {

					if (StringT.countTokens()==1) {

						int dice=RiskGame.getNumber( GetNext() );

						if ( dice != -1 && game.rollA(dice) ) {

							if ( battle ) {

								controller.setNODAttacker(dice);

							}

							int n=((Country)game.getDefender()).getArmies();


							if (n > game.getMaxDefendDice() ) { n= game.getMaxDefendDice() ; }


							//Rolled attacking dice, {0} defend yourself! (you can use up to {1} dice to defend)
							output = resb.getString( "core.roll.rolled")
										.replaceAll( "\\{0\\}", ((Player)game.getCurrentPlayer()).getName())
										.replaceAll( "\\{1\\}", "" + n);

							Player defendingPlayer = ((Country)game.getDefender()).getOwner();

							if ( showHumanPlayerThereInfo(defendingPlayer) ) {
								controller.showDice(n, false);
							}

						}
						else { output=resb.getString( "core.roll.error.unable"); }
					}
					else { output=resb.getString( "core.error.syntax").replaceAll( "\\{0\\}", "roll number"); }
				}
				else if (input.equals("retreat")) {
					if (StringT.hasMoreTokens()==false) {

						if ( game.retreat() ) {
							output=resb.getString( "core.retreat.retreated");
						}
						else { output=resb.getString( "core.retreat.error.unable"); }
					}
					else { output=resb.getString( "core.error.syntax").replaceAll( "\\{0\\}", "retreat"); }
				}
				else { output=resb.getString( "core.error.incorrect").replaceAll( "\\{0\\}", "roll, retreat"); }

			}
			else if (game.getState()==RiskGame.STATE_BATTLE_WON) {

				if (input.equals("move")) {
					if (StringT.countTokens()==1) {

						String num = GetNext();
						int noa;

						if (num.equals("all")) {
							noa=game.moveAll();
						}
						else {
							noa=RiskGame.getNumber( num );
						}

						int mov=game.moveArmies(noa);

						if ( mov != 0 ) {
							//Moved {0} armies to captured country.
							output = resb.getString( "core.move.moved").replaceAll( "\\{0\\}", "" + noa);

							if (mov == 2) {

								output=output + whoWon();

							}

						}
						else { output=resb.getString( "core.move.error.unable"); }
					}
					else { output=resb.getString( "core.error.syntax").replaceAll( "\\{0\\}", "move number"); }
				}
				else { output=resb.getString( "core.error.incorrect").replaceAll( "\\{0\\}", "move"); }

			}
			else if (game.getState()==RiskGame.STATE_FORTIFYING) {

				if (input.equals("movearmies")) {
					if (StringT.countTokens()==3) {

						String arg1=GetNext();
						String arg2=GetNext();
						int a1=RiskGame.getNumber(arg1);
						int a2=RiskGame.getNumber(arg2);

						Country country1;
						Country country2;

						if (a1 != -1) {
							country1=game.getCountryInt(a1);
						}
						else {
							//YURA:LANG country1=game.getCountryByName(arg1);
							country1=null;
						}

						if (a2 != -1) {
							country2=game.getCountryInt(a2);
						}
						else {
							//YURA:LANG country2=game.getCountryByName(arg2);
							country2=null;
						}

						int noa=RiskGame.getNumber( GetNext() );

						if ( game.moveArmy(country1, country2, noa) ) {
							//Moved {0} armies from {1} to {2}.
							output = resb.getString( "core.tacmove.movedfromto")
									.replaceAll("\\{0\\}", "" + noa)
									.replaceAll("\\{1\\}", country1.getName()) // Display
									.replaceAll("\\{2\\}", country2.getName()); // Display
						}
						else { output=resb.getString( "core.tacmove.error.unable"); }
					}
					else { output=resb.getString( "core.error.syntax").replaceAll( "\\{0\\}", "movearmies country country number"); }
				}
				else if (input.equals("nomove")) {
					if (StringT.hasMoreTokens()==false) {
						if ( game.noMove() ) {
							output=resb.getString( "core.tacmove.no.nomoves");
						}
						else { output=resb.getString( "core.tacmove.no.error.unable"); }
					}
					else { output=resb.getString( "core.error.syntax").replaceAll( "\\{0\\}", "nomove"); }
				}
				else { output=resb.getString( "core.error.incorrect").replaceAll( "\\{0\\}", "movearmies, nomove"); }

			}
			else if (game.getState()==RiskGame.STATE_END_TURN) {

				if (input.equals("endgo")) {
					if (StringT.hasMoreTokens()==false) {

						needInput=false;
						output=null;

						controller.sendMessage(resb.getString( "core.endgo.ended"), false , false);
						DoEndGo();

					}
					else { output=resb.getString( "core.error.syntax").replaceAll( "\\{0\\}", "endgo"); }
				}
				else { output=resb.getString( "core.error.incorrect").replaceAll( "\\{0\\}", "emdgo"); }

			}
			else if (game.getState()==RiskGame.STATE_GAME_OVER) {

				if (input.equals("continue")) {
					if (StringT.hasMoreTokens()==false) {

						if ( game.continuePlay() ) {
							output=resb.getString( "core.continue.successful");
						}
						else {
							output=resb.getString( "core.continue.error.unable");
						}

					}
					else { output=resb.getString( "core.error.syntax").replaceAll( "\\{0\\}", "continue"); }
				}
				else {
					//The game is over. {0} won! (current possible commands are: continue)
					output = resb.getString( "core.gameover.won").replaceAll( "\\{0\\}", ((Player)game.getCurrentPlayer()).getName());
				}

			}
			else if (game.getState()==RiskGame.STATE_SELECT_CAPITAL) {

				if (input.equals("capital")) {
					if (StringT.countTokens()==1) {

						String strCountry = GetNext();
						int nCountryId = RiskGame.getNumber(strCountry);
						Country t;

						if (nCountryId != -1) {
							t = game.getCountryInt( nCountryId);
						} else {
							//YURA:LANG t = game.getCountryByName( strCountry);
							t=null;
						}

						if ( t != null && game.setCapital(t) ) {

							if ( showHumanPlayerThereInfo( game.getCurrentPlayer() ) ) {

								output=resb.getString( "core.capital.selected").replaceAll( "\\{0\\}", t.getName()); // Display
							}
							else {
								output=resb.getString( "core.capital.hasbeenselected");
							}

						}
						else { output=resb.getString( "core.capital.error.unable"); }

					}
					else { output=resb.getString( "core.error.syntax").replaceAll( "\\{0\\}", "capital country"); }
				}
				else { output=resb.getString( "core.error.incorrect").replaceAll( "\\{0\\}", "capital"); }


			}
			else if (game.getState()==RiskGame.STATE_DEFEND_YOURSELF) {

				if (input.equals("roll")) {

					if (StringT.countTokens()==1) {

						int dice=RiskGame.getNumber( GetNext() );
						if ( dice != -1 && game.rollD(dice) ) {

							if ( battle ) {

								controller.setNODDefender(dice);

								try{ Thread.sleep(500); }
								catch(InterruptedException e){}

							}
							// client does a roll, and this is not called
							if ( !replay && (chatSocket == null || myAddress.equals(Addr)) ) { // recursive call

								int[] attackerResults = game.rollDice( game.getAttackerDice() );
								int[] defenderResults = game.rollDice( game.getDefenderDice() );

								String serverRoll = "DICE ";

								serverRoll = serverRoll + attackerResults.length + " ";
								serverRoll = serverRoll + defenderResults.length + " ";

								for (int c=0; c< attackerResults.length ; c++) {
									serverRoll = serverRoll + attackerResults[c] + " ";
								}
								for (int c=0; c< defenderResults.length ; c++) {
									serverRoll = serverRoll + defenderResults[c] + " ";
								}

								if (chatSocket == null ) {
									GameParser( serverRoll );
								}
								else if ( myAddress.equals(Addr) ) {
									outChat.println( serverRoll );
								}



							}

							output=null;
							needInput=false;

						}
						else { output=resb.getString( "core.roll.error.unable"); }

					}
					else { output=resb.getString( "core.error.syntax").replaceAll( "\\{0\\}", "roll number"); }
				}
				else { output=resb.getString( "core.error.incorrect").replaceAll( "\\{0\\}", "roll"); }

			}
			else { output=resb.getString( "core.error.unknownstate"); }

			//} // this was the end of "if there is somthing to pass" but not needed any more



			if (game != null ) {

				//System.out.print("oldstate = " +oldState+" newstate = " +game.getState()+"\n");

				if (game.getState()==RiskGame.STATE_ROLLING && battle==false) {

					Player attackingPlayer = ((Country)game.getAttacker()).getOwner();
					Player defendingPlayer = ((Country)game.getDefender()).getOwner();

					if ( showHumanPlayerThereInfo(attackingPlayer) || showHumanPlayerThereInfo(defendingPlayer) ) {

						controller.openBattle( ((Country)game.getAttacker()).getColor() , ((Country)game.getDefender()).getColor() );
						battle=true;
					}

				}

				// if someone retreats
				else if ( game.getState()==RiskGame.STATE_ATTACKING ) {

					closeBattle();

				}

			}


		}// end of parse of normal command

		// give a output if there is one
		if (output!=null) {

			// give a output
			if (game==null) {
				controller.sendMessage(output, false, true );
				setHelp();
			}
			else if ( game.getState()==RiskGame.STATE_NEW_GAME ) {
				controller.sendMessage(output, false, true );
				setHelp();
			}
			else if ( game.getState()==RiskGame.STATE_GAME_OVER ) {
				controller.sendMessage(output, true, true );
				setHelp();
			}
			else if (game.getState()==RiskGame.STATE_END_TURN) {

				// if ( game.getCurrentPlayer().getAutoEndGo() && chatSocket == null ) {
				// @todo:?????
				if ( game.getCurrentPlayer().getAutoEndGo() ) {
					controller.sendMessage(output, false, false );
				}
				else {
					controller.sendMessage(output, true, true );
					setHelp();
				}
			}
			else {// if player type is human or neutral or ai
				controller.sendMessage(output, true, true );
				if ( inbox.isEmpty() ) setHelp();
			}

		}

		// check to see if the players go should be ended
		if ( game != null && game.getState()==RiskGame.STATE_END_TURN && game.getCurrentPlayer().getAutoEndGo() ) {

			needInput=false;
			DoEndGo();

		}

		// ask for input if u need one
		if (needInput && inbox.isEmpty() ) {
			getInput();
		}

	}

        // TODO is this thread safe???
        public void setMap(String filename) throws Exception {
            
            boolean yesmissions = game.setMapfile(filename);

            setupPreview();

            controller.showCardsFile( game.getCardsFile() , yesmissions );
            //New map file selected: "{0}" (cards have been reset to the default for this map)
            String output=resb.getString( "core.choosemap.mapselected").replaceAll( "\\{0\\}", filename);
            
            controller.sendMessage(output, false , true);
        }
        public void getMapError(String string) {
            controller.sendMessage(string, false , true);
            showMessageDialog(string);
        }
        
	private void setupPreview() {
            controller.showMapPic( game );
	}

	public static int getType(String type) {
		if (type.equals("human")) {
			return Player.PLAYER_HUMAN;
		}
		if (type.equals("ai easy")) {
			return Player.PLAYER_AI_EASY;
		}
		if (type.equals("ai hard")) {
			return Player.PLAYER_AI_HARD;
		}
		if (type.equals("ai crap")) {
			return Player.PLAYER_AI_CRAP;
		}
		return -1;
	}
        public static String getType(int type) {
            switch (type) {
                case Player.PLAYER_HUMAN: return "human";
                case Player.PLAYER_AI_EASY: return "ai easy";
                case Player.PLAYER_AI_HARD: return "ai hard";
                case Player.PLAYER_AI_CRAP: return "ai crap";
                default: throw new IllegalArgumentException();
            }
	}

	/**
	 * return true ONLY if info of this Player p should be disclosed to this computer
	 */
	private boolean showHumanPlayerThereInfo(Player p) {

		return (p != null) && ( p.getType()==Player.PLAYER_HUMAN ) && ( unlimitedLocalMode || myAddress.equals( p.getAddress() ) );

	}

	/**
	 * Method that deals with an end of a player's turn
	 */
	public void DoEndGo() {

	    if (!replay) {

		controller.noInput(); // definatly need to block input at the end of someones go

		//give them a card if they deserve one

		if ( chatSocket == null ) {
			GameParser( "CARD " + game.getDesrvedCard() );
		}
		else if ( (((Player)game.getCurrentPlayer()).getAddress().equals( myAddress ) ) ) {

			// || ((Player)game.getCurrentPlayer()).getAddress().equals( "all" )
			// if this is a network game  // recursive call
			// YURA:LOBBY this will never happen
			//if (((Player)game.getCurrentPlayer()).getAddress().equals( "all" )) {
			//	GameParser("CARD");
			//}
			//else {
			// that was here V
			//}

			outChat.println( "CARD " + game.getDesrvedCard() );


		}


	    }

	}

	public void setReplay(boolean a) {

		replay = a;

	}

	/**
	 * This deals with trying to find out what input is required for the parser
	 */
	public void getInput() {

		if (game==null) {
			controller.needInput( -1 );
		}
		// work out what to do next
		else if ( game!=null && game.getCurrentPlayer()!=null && game.getState()!=RiskGame.STATE_GAME_OVER ) {// if player type is human or neutral or ai

			if (game.getState()==RiskGame.STATE_TRADE_CARDS) {
				controller.sendMessage(resb.getString( "core.input.newarmies").replaceAll( "\\{0\\}", ((Player)game.getCurrentPlayer()).getExtraArmies() + "") , false, false);
				controller.armiesLeft( ((Player)game.getCurrentPlayer()).getExtraArmies() , game.NoEmptyCountries() );
			}
			else if (game.getState()==RiskGame.STATE_PLACE_ARMIES) {
				controller.sendMessage(resb.getString( "core.input.armiesleft").replaceAll( "\\{0\\}", ((Player)game.getCurrentPlayer()).getExtraArmies() + ""), false, false);
				controller.armiesLeft( ((Player)game.getCurrentPlayer()).getExtraArmies() , game.NoEmptyCountries() );
			}

			setHelp();

			if (!replay) {

				// yura:lobby taken out: || ((Player)game.getCurrentPlayer()).getAddress().equals("all")

				// IF local game, OR addres match get input
			    if ( unlimitedLocalMode || ((Player)game.getCurrentPlayer()).getAddress().equals(myAddress) ) {

				if ( game.getState() == RiskGame.STATE_DEFEND_YOURSELF && game.getCurrentPlayer().getAutoDefend() ) {

					parser( AIPlayer.getOutput(game,AIPlayer.aicrap) );

				}

				// || ((Player)game.getCurrentPlayer()).getType()==Player.PLAYER_NEUTRAL

				else if ( ((Player)game.getCurrentPlayer()).getType()==Player.PLAYER_HUMAN ) {

					controller.needInput( game.getState() );

				}
				else {

					AIPlayer.play(this);

				}

			    }
			    //else if ( game.getCurrentPlayer().getType()==Player.PLAYER_HUMAN ) {

				// this is here for the lobby
				//getHumanInput();

			    //}

			}


		}
		else {
			controller.needInput( game.getState() );
		}

	}

	//public void getHumanInput() { }

	public String whoWon() {

		String winner = System.getProperty("line.separator") +
					resb.getString("core.whowon.hehaswon").replaceAll( "\\{0\\}", ((Player)game.getCurrentPlayer()).getName());

		if ( game.getGameMode() == RiskGame.MODE_SECRET_MISSION ) {
			//There mission was: {0}
			winner=winner + System.getProperty("line.separator") +
					resb.getString( "core.whowon.mission").replaceAll( "\\{0\\}", ((Mission)((Player)game.getCurrentPlayer()).getMission()).getDiscription());
		}

		return winner;

	}



	/** Shows helpful tips in each game state */
	public void setHelp() {

		String help="";

		if ( game!=null && game.getCurrentPlayer() != null ) {

			String strId = null;

			switch ( ((Player)game.getCurrentPlayer()).getType() ) {

				case Player.PLAYER_HUMAN: strId = "core.help.move.human"; break;
				case Player.PLAYER_AI_CRAP: strId = "core.help.move.ai.crap"; break;
				case Player.PLAYER_AI_EASY: strId = "core.help.move.ai.easy"; break;
				case Player.PLAYER_AI_HARD: strId = "core.help.move.ai.hard"; break;

			}

			help = resb.getString( strId).replaceAll( "\\{0\\}", ((Player)game.getCurrentPlayer()).getName()) +" ";
		}

		if (game == null) {
			help = resb.getString( "core.help.newgame");
		}
		else if (game.getState()==RiskGame.STATE_NEW_GAME) {
			help = resb.getString( "core.help.createplayers");
		}
		else if (game.getState()==RiskGame.STATE_TRADE_CARDS) {
			help = help + resb.getString( "core.help.trade");
		}
		else if (game.getState()==RiskGame.STATE_PLACE_ARMIES) {

			if ( game.getSetup() ) { help = help + resb.getString( "core.help.placearmies"); }

			else if ( game.NoEmptyCountries() ) { help = help + resb.getString( "core.help.placearmy"); }

			else { help = help + resb.getString( "core.help.placearmyempty"); }

		}
		else if (game.getState()==RiskGame.STATE_ATTACKING) {
			help = help + resb.getString( "core.help.attack");
		}
		else if (game.getState()==RiskGame.STATE_ROLLING) {
			help = help + resb.getString( "core.help.rollorretreat");
		}
		else if (game.getState()==RiskGame.STATE_BATTLE_WON) {
			help = help + resb.getString( "core.help.youhavewon");
		}
		else if (game.getState()==RiskGame.STATE_FORTIFYING) {
			help = help + resb.getString( "core.help.fortifyposition");
		}
		else if (game.getState()==RiskGame.STATE_END_TURN) {
			help = help + resb.getString( "core.help.endgo");
		}
		else if (game.getState()==RiskGame.STATE_GAME_OVER) {
			//the game is over, {0} has won! close the game to create a new one
			help = resb.getString( "core.help.gameover").replaceAll( "\\{0\\}", ((Player)game.getCurrentPlayer()).getName());
		}
		else if (game.getState()==RiskGame.STATE_SELECT_CAPITAL) {
			help = help + resb.getString( "core.help.selectcapital");
		}
		else if (game.getState()==RiskGame.STATE_DEFEND_YOURSELF) {
			help = help + resb.getString( "core.help.defendyourself");
		}
		else {
			help = resb.getString( "core.help.error.unknownstate");
		}

		controller.setGameStatus( help );

	}

	public synchronized void kickedOff() {

		//System.out.print("Got kicked off the server!\n");

		try {

			game = null;
			outChat.close();
			inChat.close();
			chatSocket.close();
			chatSocket = null;

		}
		catch (IOException except) {
			//System.out.println("IOException in main");
		}

		// does not work from here
		closeBattle();

		controller.closeGame();
		controller.sendMessage(resb.getString( "core.kicked.error.disconnected"),false,false);

		setHelp();
		getInput();
	}

	protected void closeBattle() {

		if ( battle ) { controller.closeBattle(); battle=false; }

	}

	/**
	 * Shows the cards a Player has in his/her possession
	 * @return Vector Returns the cards in a vector
	 */
	public Vector getCurrentCards() {

		//return game.getCards(); // for testing cards
		return ((Player)game.getCurrentPlayer()).getCards();

	}



	/**
	 * Checks whether a Player has armies in a country
	 * @param name The index of the country
	 * @return int Returns the number of armies
	 */
	public int hasArmiesInt(int name) {

		return ((Country)game.getCountryInt(name)).getArmies();

	}



	/**
	 * Checks whether a Player can attack a country
	 * @param a The name of the country attacking
	 * @param d The name of the country defending
	 * @return boolean Returns true if the player owns the country, else returns false
	 *
	//  * @deprecated

	public boolean canAttack(String a, String d) {

		if ( ((Country)game.getCountry(a)).isNeighbours( (Country)game.getCountry(d) ) ) { return true; }
		else { return false; }

	}
	 */

	/**
	 * checks whether a Player can attach a country
	 * @param nCountryFrom	The name of the country attacking
	 * @param nCountryTo	The name of the country defending
	 * @return boolean Returns true if the player can attack the other one, false if not
	 */
	public boolean canAttack(int nCountryFrom, int nCountryTo)
	{
		if (game.getCountryInt( nCountryFrom).isNeighbours( game.getCountryInt( nCountryTo))) {
			return true;
		}
		return false;
	}//public boolean canAttack(int nCountryFrom, int nCountryTo)



	/**
	 * Checks whether a Player owns a country
	 * @param name The name of the country
	 * @return boolean Returns true if the player owns the country, else returns false
	 */
	public boolean isOwnedCurrentPlayerInt(int name) {

		// not thread safe, so this can cause problems, but this method is used in display to thats ok


		if ( (game!=null && game.getCurrentPlayer()!=null && game.getCountryInt( name )!=null) &&

			( ((Country)game.getCountryInt( name )).getOwner() == null || ((Country)game.getCountryInt( name )).getOwner() == game.getCurrentPlayer() )

		) { return true; }
		else { return false; }


	}

	/**
	 * Get the current mission of the game, depending on the game mode
	 * @return String Returns the current mission
	 */
	public String getCurrentMission() {

		if ( game.getGameMode() == RiskGame.MODE_DOMINATION ) {
			return resb.getString( "core.mission.conquerworld");
		}
		//else if ( game.getGameMode() == 1 ) {
		//	return resb.getString( "core.mission.eliminateenemy");
		//}
		else if ( game.getGameMode() == RiskGame.MODE_CAPITAL ) {
			return resb.getString( "core.mission.capturecapitals");
		}
		else if ( game.getGameMode() == RiskGame.MODE_SECRET_MISSION ) {
			return ((Mission)((Player)game.getCurrentPlayer()).getMission()).getDiscription();
		}
		else {
			return resb.getString( "core.mission.error.cantshow");
		}
	}

	/**
	 * Get the colours of the players in the game
	 * @return Color[] Return the colour of the game players
	 */
	public int[] getPlayerColors() {

		if ( game.getState() == RiskGame.STATE_DEFEND_YOURSELF ) {

			return new int[] { game.getDefender().getOwner().getColor() };
		}

		Vector Players = game.getPlayers();
		boolean setup = game.NoEmptyCountries();

		int num=0;
		int start=0;

		for (int c=0; c< Players.size() ; c++) {

			if ( ((Player)Players.elementAt(c)).getNoTerritoriesOwned() > 0 || setup==false ) { num++; }
			if ( ((Player)Players.elementAt(c)) == game.getCurrentPlayer() ) { start=c; }

		}

		int[] playerColors = new int[num];

		int current=0;

		for (int c=start; c< Players.size() ; c++) {

			if ( ((Player)Players.elementAt(c)).getNoTerritoriesOwned() > 0 || setup==false ) { playerColors[ current ] = ((Player)Players.elementAt(c)).getColor() ; current++; }
			if ( current==num ) { break; }
			if ( c==Players.size()-1 ) { c=-1; }


		}

		return playerColors;

	}

	/**
	 * Get the colour of the current player
	 * @return Color Return the colour of the current player in the game
	 */
	public int getCurrentPlayerColor() {

		if (game != null && game.getState() != RiskGame.STATE_NEW_GAME) {
			return ((Player)game.getCurrentPlayer()).getColor();
		}
		else {
			return 0;
		}
	}

	/**
	 * Get the colour of the current player
	 * @param n the Country number identifier
	 * @return Color Return the colour of a player that owns a country
	 */
	public int getColorOfOwner(int n) {

		return ((Player)((Country)game.getCountryInt(n)).getOwner()).getColor();

	}

	/**
	 * Checks whether a set of cards can be traded in for extra armies
	 * @param c1 the name of the first card
	 * @param c2 the name of the second card
	 * @param c3 the name of the third card
	 * @return boolean Return true if the card can be traded, else return false
	 */
	public boolean canTrade(String c1, String c2, String c3) {

		if (game.getState() == RiskGame.STATE_TRADE_CARDS ) {

			Card[] cards = game.getCards(c1,c2,c3);

			return game.checkTrade(cards[0], cards[1], cards[2]);
		}
		return false;
	}

	/**
	 * Get the state of the cards
	 * @return int Return the newCardState
	 */
	public int getNewCardState() {

		return game.getNewCardState();

	}

	/**
	 * Get the present game
	 * @return RiskGame Return the current game
	 */
	public RiskGame getGame() {
		return game;
	}

	/**
	 * Get the name of the country from the game
	 * @param c The (unique) country identifier
	 * @return String Return Country name if it is there, else return empty speech-marks otherwise
	 */
	public String getCountryName(int c) {

		Country t = game.getCountryInt(c);
		if (t==null) {
			return "";
		} else {
			return t.getName();
		}

	}

	/**
	 * returns the country display name
	 * @param nCountryId	The ID of the country
	 * @return	The country's display name

	public String getCountryDisplayName(int nCountryId)
	{
		Country country = game.getCountryInt( nCountryId);
		if (country == null) {
			return "";
		} else {
			return country.getName(); // Display
		}
	}//public String getCountryDisplayName(int nCountryId)
	 */


	/**
	 * Checks whether the current Player has autoEndGo on
	 * @return boolean Return autoEndGo
	 */
	public boolean getAutoEndGo() {

		if (game != null && game.getCurrentPlayer()!=null) {

			return game.getCurrentPlayer().getAutoEndGo();
		}
		else {
			// this should never happen, but can come up with bad timing problems
			return false;
		}
	}

	/**
	 * Checks whether the current Player has autoDefend on
	 * @return boolean Return autoDefend
	 */
	public boolean getAutoDefend() {

		if (game !=null && game.getCurrentPlayer()!=null) {

			return game.getCurrentPlayer().getAutoDefend();
		}
		else {
			// this should never happen, but can come up with bad timing problems
			return false;
		}

	}







	public void showMessageDialog(String a) {

		controller.showMessageDialog(a);

	}

	public void renamePlayer(String oldser,String newuser) {

		Vector players = game.getPlayers();

		for (int c=0;c<players.size(); c++) {

			Player player = (Player)players.elementAt(c);

			if ( player.getName().equals(oldser) ) {

				player.rename(newuser);

			}
		}

	}

        private void closeGame() {

            // does not work from here
            closeBattle();

            controller.closeGame();

            game = null;

        }


	public void newMemoryGame(RiskGame g) {

		if (game != null) {
                    closeGame();
                }

                try {
                        // make a copy

                        javax.crypto.NullCipher nullCipher = new javax.crypto.NullCipher();

                        // @TODO, this will crash on macs
                        game = (RiskGame) (new javax.crypto.SealedObject( g, nullCipher ).getObject( nullCipher ));

                        for (int c=1;c<=RiskGame.MAX_PLAYERS;c++) {

                                game.delPlayer("PLAYER"+c);

                        }
                }
                catch (Exception e) {
                        // should never happen
                        //e.printStackTrace();
                        throw new RuntimeException(e);
                }

                controller.newGame(true);

                controller.showCardsFile( "loaded from memory" , (game.getNoMissions()!=0) );

                // we dont do this here as it wont work
                //controller.showMapPic(game);

                unlimitedLocalMode = true;
	}
}
