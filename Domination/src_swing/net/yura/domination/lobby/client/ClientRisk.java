package net.yura.domination.lobby.client;

import java.util.logging.Logger;
import net.yura.domination.engine.OnlineRisk;
import net.yura.domination.engine.Risk;

/**
 * @author Yura Mamyrin
 */
public class ClientRisk implements OnlineRisk {

    Logger logger = Logger.getLogger( ClientRisk.class.getName() );
    ClientGameRisk lgml;
    Risk myrisk;
    
    public ClientRisk(ClientGameRisk b, Risk risk) {
	lgml = b;
        myrisk = risk;
    }
    
    public void sendUserCommand(final String messagefromgui) {
        lgml.sendGameMessage(messagefromgui);
    }
    
    public void sendGameCommand(String mtemp) {
        logger.info( "ignore GameCommand "+mtemp );
    }

    public void close() {
        lgml.leaveGame();
    }
}
