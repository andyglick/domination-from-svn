package net.yura.domination.mobile.flashgui;

import net.yura.mobile.gui.components.List;
import net.yura.mobile.gui.components.Component;
import net.yura.mobile.gui.cellrenderer.DefaultListCellRenderer;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.engine.core.Player;

/**
 * @author Yura
 */
public class PlayerList extends List {

    private RiskGame game;

    public PlayerList() {
        setLayoutOrientation(HORIZONTAL);
        setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(Component list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                Player player = (Player) value;
                setBackground(player.getColor());
                return component;
            }
        });
    }

    public void setGame(RiskGame game) {
        this.game = game;
    }

    public int getSize() {
        return game.getPlayers().size();
    }

    public Object getElementAt(int index) {
        return game.getPlayers().get(index);
    }

}
