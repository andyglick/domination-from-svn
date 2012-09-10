package net.yura.domination.lobby.mini;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.microedition.lcdui.Image;
import net.yura.domination.ImageManager;
import net.yura.domination.engine.OnlineRisk;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.domination.mapstore.MapChooser;
import net.yura.lobby.client.Connection;
import net.yura.lobby.client.LobbyClient;
import net.yura.lobby.client.LobbyCom;
import net.yura.lobby.model.Game;
import net.yura.lobby.model.GameType;
import net.yura.lobby.model.Player;
import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.Icon;
import net.yura.mobile.gui.Midlet;
import net.yura.mobile.gui.components.List;
import net.yura.mobile.gui.components.OptionPane;
import net.yura.mobile.gui.components.Panel;
import net.yura.mobile.gui.layout.XULLoader;
import net.yura.mobile.util.Properties;
import net.yura.swingme.core.CoreUtil;

public class MiniLobbyClient implements LobbyClient,ActionListener {

    static final Logger logger = Logger.getLogger( MiniLobbyClient.class.getName() );

    public Properties resBundle = CoreUtil.wrap(TranslationBundle.getBundle());
    XULLoader loader;
    List list;

    Connection mycom;

    public MiniLobbyClient(Risk risk) {
        myrisk = risk;

        try {
            loader = XULLoader.load( Midlet.getResourceAsStream("/ms_lobby.xml") , this, resBundle);
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
        
        list = (List)loader.find("ResultList");
        list.setCellRenderer( new GameRenderer(this) );

        
        
        String uuid = getMyUUID();
        
        
        
        
        
        mycom = new LobbyCom(uuid);
        mycom.addEventListener(this);
        mycom.connect("heather", 1964);
        
    }
    
    public static String getMyUUID() {
        
        java.util.Properties prop = new java.util.Properties();
        
        File lobbySettingsFile = new File( System.getProperty("user.home"),".lobby" );
        
        try {
            prop.load( new FileInputStream(lobbySettingsFile) );
        }
        catch (Exception ex) { }
        
        String uuid = prop.getProperty("uuid");
        if (uuid!=null) {
            return uuid;
        }
        uuid = UUID.randomUUID().toString();
        prop.setProperty("uuid", uuid);
        
        try {
            prop.store(new FileOutputStream(lobbySettingsFile), "yura.net Lobby");
        }
        catch (Exception ex) { }
        
        return uuid;
    }
    
    public Panel getRoot() {
        return ((Panel)loader.getRoot());
    }
    
    public void actionPerformed(String actionCommand) {
        
        if ("listSelect".equals(actionCommand)) {
            List list = (List)loader.find("ResultList");
            Game game = (Game)list.getSelectedValue();
            if (game!=null) {
                int state = game.getState( whoAmI() );
                switch (state) {
                    case Game.STATE_CAN_JOIN:
                        mycom.joinGame(game.getGameId());
                        break;
                    case Game.STATE_CAN_LEAVE:
                        mycom.leaveGame( game.getGameId() );
                        break;
                    case Game.STATE_CAN_PLAY:
                    case Game.STATE_CAN_WATCH:
                        mycom.playGame(game);
                        break;
                }
            }
        }
        else if ("create".equals(actionCommand)) {
        
            // TODO
            
        }
        else if ("close".equals(actionCommand)) {
            mycom.disconnect();
            getRoot().getWindow().setVisible(false);
        }
        else {
            OptionPane.showMessageDialog(null,"unknown command: "+actionCommand, null, OptionPane.INFORMATION_MESSAGE);
        }
    }

    String gameName = "Domination"; // the ONLY game this client will support
    Risk myrisk;

    
    
    
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
        
        //java.util.List localFiles = net.yura.domination.mobile.MiniUtil.getFileList("map");
        //if (localFiles.contains(mapName)) {
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
                list.repaint();
            }
        //}
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
    
    
    
    
    
    
    
    // WMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMW
    // WMWMWMWMWMWMWMWMWMWMWMWMWMWM LobbyClient MWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMW
    // WMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMW
    

    public ClassLoader getClassLoader(GameType gameType) {
        return getClass().getClassLoader();
    }

    public void connected() {
        mycom.getGameTypes();
    }

    public void disconnected() { }

    public void connecting(String message) {
        logger.info(message);
    }

    public void error(String error) {
        logger.info(error);
    }

    
    
    
    String myusername;
    public void setUsername(String name, boolean guest) {
        myusername = name;
    }
    public String whoAmI() {
            return myusername;
    }
        
    GameType theGame;
    public void addGameType(java.util.List gametypes) {
        
        for (int c=0;c<gametypes.size();c++) {
            GameType gametype = (GameType)gametypes.get(c);
        
            if (gameName.equals( gametype.getName() ) ) {
                theGame = gametype;
                
                mycom.getGames( gametype );
            }
            else {
                logger.info("ignore GameType: "+gametype);
            }
        }
    }

    public void addOrUpdateGame(Game game) {
        int index = list.indexOf(game);
        if (index>=0) {
            list.setElementAt(game, index);
        }
        else {
            list.addElement(game);
        }
        list.repaint();
        
    }

    public void removeGame(String gameid) {
        for (int c=0;c<list.getSize();c++) {
            Game game = (Game)list.getElementAt(c);
            if ( gameid.equals(game.getGameId()) ) {
                list.removeElementAt(c);
                break;
            }
        }
        list.repaint();
    }

    public void messageForGame(String gameid, Object message) {
        
        if (message instanceof String) {
            stringForGame(gameid,(String)message);
        }
        else if (message instanceof byte[]) {
            try {
                ByteArrayInputStream in = new ByteArrayInputStream( (byte[])message );
                ObjectInputStream oin = new ObjectInputStream(in);
                Object object = oin.readObject();
                objectForGame(gameid,object);
            }
            catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        else {
            throw new RuntimeException("unknown object");
        }
    }

    private void stringForGame(String gameId,String message) {
        
        if (message.startsWith("LOBBY_NEEDINPUT ")) {
                // TODO
                String who = message.substring(16, message.length() );
                //playerlist.needInputFrom(who);
                if (whoAmI().equals(who)) {
                        //needinput = true;
                }
        }
        else if (message.equals("LOBBY_GAMEOVER")) {
                // TODO
                //paused=true;
        }
        else {
                myrisk.parserFromNetwork(message);
        }
    }
    
    private void objectForGame(final String gameId, Object object) {
        Map map = (Map)object;
        String command = (String)map.get("command");

        if ("game".equals(command)) {
            String address = (String)map.get("playerId");
            RiskGame game = (RiskGame)map.get("game");
            OnlineRisk lrisk = new OnlineRisk() {
                public void sendUserCommand(final String messagefromgui) {
                    mycom.sendGameMessage(gameId,messagefromgui);
                }
                public void sendGameCommand(String mtemp) {
                    logger.info( "ignore GameCommand "+mtemp );
                }
                public void close() {
                    mycom.closeGame(gameId);
                }
            };
            myrisk.createGame( address , game, lrisk );
        }
        else if ("rename".equals(command)) {
            String myName = whoAmI();
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


    // chat
    public void serverMessage(String message) { }
    public void privateMessage(String fromwho, String message) { }
    public void incomingChat(String roomid, String fromwho, String message) { }
    public void addPlayer(String roomid, Player player) { }
    public void removePlayer(String roomid, String player) { }
    public void renamePlayer(String oldname, String newname,int newtype) { }
    public void addMainRoom(String roomid) { }
    public void setUserInfo(String user,java.util.List info) { }
    public void newMainRoomJoined(String id) { }

}
