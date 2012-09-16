package net.yura.domination.mobile.flashgui;

import java.io.InputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.microedition.lcdui.Image;
import net.yura.domination.ImageManager;
import net.yura.domination.engine.OnlineRisk;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.domination.lobby.mini.MiniLobbyClient;
import net.yura.domination.lobby.mini.MiniLobbyGame;
import net.yura.domination.mapstore.MapChooser;
import net.yura.lobby.model.Game;
import net.yura.lobby.model.GameType;
import net.yura.mobile.gui.Icon;
import net.yura.mobile.util.Properties;
import net.yura.swingme.core.CoreUtil;

/**
 *
 * @author Yura Mamyrin
 */
public class MiniLobbyRisk implements MiniLobbyGame {

    static final Logger logger = Logger.getLogger( MiniLobbyRisk.class.getName() );
    
    private Risk myrisk;
    private MiniLobbyClient lobby;
    public MiniLobbyRisk(Risk risk) {
        myrisk = risk;
    }

    public void addLobbyGameMoveListener(MiniLobbyClient lgl) {
        lobby = lgl;
    }
    
    public Properties getProperties() {
        return CoreUtil.wrap(TranslationBundle.getBundle());
    }

    public boolean isMyGameType(GameType gametype) {
        return "Domination".equals( gametype.getName() );
    }
    
    
    
    public Icon getIconForGame(Game game) {
        Icon aicon = ImageManager.get( game );
        if (aicon==null) {
            aicon = ImageManager.newIcon(game,50,50);
            loadImg( game );
        }
        return aicon;
    }
    
    
    void loadImg(Game game) {
        String mapName = getMapName(game.getOptions());
        
        java.util.List localFiles = net.yura.domination.mobile.MiniUtil.getFileList("map");
        if (localFiles.contains(mapName)) {
            Map info = RiskUtil.loadInfo(mapName, false);
            
            String prv = (String)info.get("prv");
            
            InputStream in=null;
            if (prv!=null) {
                in = MapChooser.getLocalePreviewImg("preview/"+prv);
            }
            if (in==null) {
                in = MapChooser.getLocalePreviewImg( (String)info.get("pic") );
            }

            Image img=null;
            if (in!=null) {
                try {
                    img = MapChooser.createImage(in);
                }
                catch (Exception ex) {
                    logger.log(Level.WARNING, "odd!", ex);
                }
            }

            if (img!=null) {
                ImageManager.gotImg(game, img);
                //list.repaint();
            }
        }
    }
    
    
    
        
    /**
     * option string looks like this:
     * 
     *   0
     *   2
     *   2
     *   choosemap luca.map
     *   startgame domination increasing
     */
    static String getMapName(String options) {
        String[] lines = options.split( RiskUtil.quote("\n") );
        String choosemap = lines[3];
        return choosemap.substring( "choosemap ".length() );
    }
    
    
    public void openGameSetup(GameType gameType) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
    public void objectForGame(Object object) {
        Map map = (Map)object;
        String command = (String)map.get("command");

        if ("game".equals(command)) {
            String address = (String)map.get("playerId");
            RiskGame game = (RiskGame)map.get("game");
            OnlineRisk lrisk = new OnlineRisk() {
                public void sendUserCommand(final String messagefromgui) {
                    lobby.sendGameMessage(messagefromgui);
                }
                public void sendGameCommand(String mtemp) {
                    logger.info( "ignore GameCommand "+mtemp );
                }
                public void close() {
                    lobby.closeGame();
                }
            };
            myrisk.createGame( address , game, lrisk );
        }
        else if ("rename".equals(command)) {
            String myName = lobby.whoAmI();
            String oldName = (String)map.get("oldName");
            String newName = (String)map.get("newName");
            myrisk.renamePlayer(oldName, newName);
            if (myName.equals(newName)) {
                myrisk.joinAs(newName);
            }
        }
        else {
            throw new RuntimeException("unknown command "+command);
        }
    }

    public void stringForGame(String message) {
        myrisk.parserFromNetwork(message);
    }
    
}
