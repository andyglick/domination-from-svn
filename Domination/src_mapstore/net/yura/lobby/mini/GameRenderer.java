package net.yura.lobby.mini;

import javax.microedition.lcdui.Graphics;
import net.yura.domination.engine.ColorUtil;
import net.yura.domination.mapstore.MapChooser;
import net.yura.lobby.model.Game;
import net.yura.mobile.gui.Font;
import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.Icon;
import net.yura.mobile.gui.cellrenderer.DefaultListCellRenderer;
import net.yura.mobile.gui.components.Component;
import net.yura.mobile.gui.plaf.Style;

public class GameRenderer extends DefaultListCellRenderer {

    MiniLobbyClient lobby;
    ScaledIcon sicon;
    Game game;
    String line2,part2;
    
    public GameRenderer(MiniLobbyClient l) {
        lobby = l;
        setName("ListRendererCollapsed"); // get rid of any padding
        sicon = new ScaledIcon( MapChooser.adjustSizeToDensityFromMdpi(75),MapChooser.adjustSizeToDensityFromMdpi(47) );
    }

    public Component getListCellRendererComponent(Component list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        game = (Game)value;
        
        sicon.setIcon( lobby.game.getIconForGame(game) );
        setIcon(sicon);
        
        line2 = lobby.game.getGameDescription(game);

        setVerticalTextPosition( line2==null?Graphics.VCENTER:Graphics.TOP);

        part2 = game.getNumOfPlayers()+"/"+game.getMaxPlayers();
        
        return c;
    }

    public void paintComponent(Graphics2D g) {
        super.paintComponent(g);

        g.setColor( getForeground() );

        Font font = g.getFont();
        String action;
        int color;

        switch (game.getState( lobby.whoAmI() )) {
            case Game.STATE_CAN_JOIN: action = "Join"; color=ColorUtil.GREEN; break;
            case Game.STATE_CAN_LEAVE: action = "Leave"; color=ColorUtil.RED; break;
            case Game.STATE_CAN_PLAY: action = "Play"; color=ColorUtil.BLUE; break;
            case Game.STATE_CAN_WATCH: action = "Watch"; color=ColorUtil.WHITE; break;
            default: action = null; color=0; break;
        }

        int state = getCurrentState();
        // if NOT focused or selected
        if ( (state&Style.FOCUSED)==0 && (state&Style.SELECTED)==0 ) {
            g.setColor( theme.getForeground(Style.DISABLED) );
        }

        if (line2!=null) {
            Icon i = getIcon();
            g.drawString(line2, padding + (i!=null?i.getIconWidth()+gap:0), getHeight()-font.getHeight()-padding);
        }
        if (part2!=null) {
            g.drawString(part2, getWidth()-font.getWidth(part2)-padding, getHeight()-font.getHeight()-padding);
        }

        int actionx=getWidth();
        if (action!=null) {
            int w = font.getWidth(action);
            int h = font.getHeight();

            g.setColor(color);
            actionx = getWidth()-w-padding*3;
            g.fillRoundRect(actionx, padding, w+padding*2, h+padding*2, 5, 5);

            g.setColor( ColorUtil.getTextColorFor(color) );
            g.drawString(action, getWidth()-w-padding*2, padding*2);
        }
        if (lobby.whoAmI().equals( game.getWhosTurn() )) {
            int wh = font.getHeight();
            g.setColor(0xFFFF0000);
            g.fillOval(actionx-wh-padding, (getHeight()-wh)/2, wh, wh);
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
