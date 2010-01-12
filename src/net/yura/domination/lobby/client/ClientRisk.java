package net.yura.domination.lobby.client;



import org.lobby.client.*;
import risk.ui.FlashGUI.FlashRiskAdapter;
import risk.ui.FlashGUI.GameFrame;

import risk.engine.*;

import risk.engine.core.*;

import java.awt.Frame;
import java.awt.Dimension;
import java.util.Map;
import java.util.HashMap;
import javax.swing.JDialog;
import java.net.URL;

import java.applet.Applet;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.BorderFactory;
import javax.swing.JProgressBar;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;

import java.util.Vector;





public class ClientRisk extends Risk {

	ClientGameRisk lgml;

	public void setupMapsDir(Applet a) {

		// TODO this should not be needed as there should
		// be some OTHER way to check for sandbox
		// as signed applets may be used, and unsigned webstart may be

	}

	public ClientRisk(ClientGameRisk b) {

		super(null);

		lgml = b;

	}



	public void createGame(String a ,RiskGame b) {

		inbox.clear();

		Object g = game;

		myAddress = a;
		game = b;

		if (g==null) { controller.startGame(unlimitedLocalMode); }

	}

	public void resignPlayer() {


		// need to stop asking this player for input
		Vector players = game.getPlayers();

		for (int c=0;c<players.size();c++) {

			risk.engine.core.Player player = (risk.engine.core.Player)players.elementAt(c);

			if (player.getAddress().equals(myAddress)) {

				player.setAddress("resignedplayer");

			}

		}

		closeBattle();

	}


	public void addToInbox(String message) {

		inbox.add(message);

		synchronized (this) {

			notify();
		}

	}




	// must catch all messages from the gui
	public void parser(String messagefromgui) {

		// myAddress needs to be set to the alias of the player logged in (no spaces)
		// the alias or "Risk.myAddress" can be added later

		// catch all closegames from any part of the GUI
		if ("closegame".equals(messagefromgui)) {

			lgml.leaveGame();

			// closethegame and kill the thread!
			//stopflag = true;

			inbox.add(myAddress+" "+ messagefromgui); // same as "bob closegame"
			synchronized(this) { notify(); }

		}
		else {

			lgml.sendGameMessage(messagefromgui);

		}

	}




	// pass things from the inbot to the GameParser

	// this thread will die when the game is closed and stopflag is set to true
	public void run() {

		String message;

		while (true) { // !stopflag

			if( game==null || inbox.isEmpty() ) { //  stopflag ||

				synchronized(this) {

					try { this.wait(); }
					catch(InterruptedException e){}
				}

				// if u get a message and game is null, dont process it
				if (game==null) { continue; }

			}

			message = (String)inbox.remove(0);

			super.GameParser(message);

		}


		//System.out.println("LOBBY-GAME-RISK THREAD DIE");

	}


	// catch anything the game sends here and do nothing with it
	public void GameParser(String mem) { }


}
