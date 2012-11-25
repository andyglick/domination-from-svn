package net.yura.domination.lobby.server;

import java.util.List;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.core.Player;
import net.yura.domination.engine.core.RiskGame;

public class ServerRisk extends Risk {

	private ServerGameRisk sgr;
	private boolean paused;
	private boolean killflag;
	private boolean waiting;

	public ServerRisk(ServerGameRisk a) {
		super();
		sgr = a;
	}

	public void makeNewGame() {
		try {
			game = new RiskGame();
		}
		catch (Exception ex) {
			throw new RuntimeException("unable to make game!",ex);
		}
		paused = true;
		// a new game, clear anything from the inbox
		inbox.clear();

	}

	public String getRiskconfig(String a) {
		return (String)riskconfig.get(a);
	}

	public synchronized void setPaued(boolean a) {
		paused = a;
		if (!a) {
			notify();
		}
	}

	public synchronized void addSetupCommandToInbox(String a) {
		addSetupCommandToInbox(myAddress,a);
	}

	public synchronized void addSetupCommandToInbox(String ad, String a) {
		inbox.add(ad+" "+a);
		waiting = false;
		notify();
	}

	public synchronized void addPlayerCommandToInbox(String a, String b) {
		inbox.add(a+" "+b);
		notify();
	}

	public void resignPlayer(String name,String Addr) {
		if (game!=null) { // if its a actual player of the game that has left

			//int count=0;

			// get all the players and make all with the ip of the leaver become nutral
			List<Player> leavers = game.getPlayers();

			for (int c=0; c< leavers.size() ; c++) {

				Player player = leavers.get(c);

				// AI will never have players addr for lobby game
				if ( player.getAddress().equals(Addr) ) {
                                        player.rename( name );
					player.setType( Player.PLAYER_AI_CRAP );
					player.setAddress( myAddress );
				}

				//if (player.getType() == Player.PLAYER_HUMAN) {

				//	count++;

				//}

			}

			//@todo this is wrong, just bot games wont work and otehr things wont
			//if (count==0) {
			// close everything (but this is wrong)
			//}

		}

	}

	public String playerJoins(String name, String Addr) {
            if (game!=null) {
                List<Player> players = game.getPlayers();
                for (int c=0; c< players.size() ; c++) {
                    Player player = players.get(c);
                    if ( player.getType() == Player.PLAYER_AI_CRAP ) {
                        String oldName = player.getName();
                        player.rename(name);
                        player.setType( Player.PLAYER_HUMAN );
                        player.setAddress( Addr );
                        return oldName;
                    }
                }
            }
            throw new RuntimeException("no AI CRAP found in game");
	}
        
	public synchronized void setKillFlag() {
		killflag = true;
		inbox.add(myAddress+" closegame");
		notify();
	}

	public boolean getWaiting() {
		return waiting;
	}

	@Override
	// must catch all messages from the ais (and humans too now)
	public void parser(String m) { //  synchronized 
		//System.out.print("\tGOT: "+m+"\n");
		// address must match for ai to know when to take its turn
		// game.getCurrentPlayer().getAddress()
		//inbox.add( myAddress +" "+m);
		//this.notify();
		addPlayerCommandToInbox(myAddress,m);
	}

	@Override
	// pass things from the inbot to the GameParser
	public void run() {
		String message;

		while (!killflag) {

			synchronized(this) {

				// dont go on if this is in catch all and dont run mode!!!!!
				while ( inbox.isEmpty() || (paused && game.getState()!=RiskGame.STATE_NEW_GAME ) ) {

					waiting=true;

					try { this.wait(); }
					catch(InterruptedException e){}
				}

				message = (String)inbox.remove(0);
			}

			inGameParser(message);
		}

		System.out.println("SERVER-GAME-RISK THREAD DIE");
	}

	@Override
	// catch all things and send it to the clients
	// game kicks off and messages are sent to here
	public void inGameParser(String mem) {
		//if not all players hit start and the game is started, just store the commands, do not run them
		// stick risk into paused mode
		if ( paused && game.getState()!=RiskGame.STATE_NEW_GAME ) {
			inbox.add(mem);
			return;
		}

		if (paused) {
			System.out.println("\tRISKSETUP "+mem);
		}
		else {
			System.out.println("\tRISKSEND "+mem);
			// send out to all clients
			sgr.sendStringToAllClient(mem);
		}

		super.inGameParser(mem);
	}

	@Override
	public void getInput() {
		super.getInput();
		if (!paused) { sgr.getInputFromSomeone(); }
	}

	@Override
	public String whoWon() {
		sgr.gameFinished( game.getCurrentPlayer().getName() );
		return super.whoWon();
	}

        String getAddress() {
            return myAddress;
        }
}
