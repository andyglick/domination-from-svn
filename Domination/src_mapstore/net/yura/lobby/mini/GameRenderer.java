package net.yura.lobby.mini;

import net.yura.domination.mapstore.MapChooser;
import net.yura.lobby.model.Game;
import net.yura.mobile.gui.Font;
import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.Icon;
import net.yura.mobile.gui.cellrenderer.DefaultListCellRenderer;
import net.yura.mobile.gui.components.Component;

public class GameRenderer extends DefaultListCellRenderer {

    MiniLobbyClient lobby;
    ScaledIcon sicon;
    Game game;
    
    public GameRenderer(MiniLobbyClient l) {
        lobby = l;
        sicon = new ScaledIcon( MapChooser.adjustSizeToDensityFromMdpi(75),MapChooser.adjustSizeToDensityFromMdpi(47) );
    }

    public Component getListCellRendererComponent(Component list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        game = (Game)value;
        
        sicon.setIcon( lobby.game.getIconForGame(game) );
        setIcon(sicon);

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
            g.drawString(action, getWidth() - font.getWidth(action) - padding, (getHeight()-font.getHeight())/2);
        }
        
    }
    
    public static class ScaledIcon extends Icon {
        Icon icon;
        public ScaledIcon(int w,int h) {
            width = w;
            height = h;
        }
        public void setIcon(Icon i) {
            icon = i;
        }
        public void paintIcon(Component c, Graphics2D g, int x, int y) {
            g.translate(x, y);
            double sx=width/(double)icon.getIconWidth();
            double sy=height/(double)icon.getIconHeight();
            g.getGraphics().scale(sx, sy);
            icon.paintIcon(c, g, 0, 0);
            g.getGraphics().scale(1/sx,1/sy);
            g.translate(-x, -y);
        }
    }

}
