package net.yura.lobby.mini;

import net.yura.lobby.model.Game;
import net.yura.mobile.gui.Font;
import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.cellrenderer.DefaultListCellRenderer;
import net.yura.mobile.gui.components.Component;

public class GameRenderer extends DefaultListCellRenderer {

    MiniLobbyClient lobby;
    
    Game game;
    
    public GameRenderer(MiniLobbyClient l) {
        lobby = l;
    }
    
    public Component getListCellRendererComponent(Component list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        game = (Game)value;
        
        setIcon( lobby.game.getIconForGame(game) );

        return c;
    }

    public void paintComponent(Graphics2D g) {
        super.paintComponent(g);

        g.setColor( getForeground() );

        Font font = g.getFont();
        String action;

        switch (game.getState( lobby.whoAmI() )) {
            case Game.STATE_CAN_JOIN: action = "Join"; break;
            case Game.STATE_CAN_LEAVE: action = "Leave"; break;
            case Game.STATE_CAN_PLAY: action = "Play"; break;
            case Game.STATE_CAN_WATCH: action = "Watch"; break;
            default: action = null; break;
        }

        if (action!=null) {
            g.drawString(action, getWidth() - font.getWidth(action), (getHeight()-font.getHeight())/2);
        }
        
    }
    
    
}
