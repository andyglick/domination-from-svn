package net.yura.domination.mobile.flashgui;

import net.yura.domination.engine.Risk;
import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.components.List;
import net.yura.mobile.gui.components.Component;
import net.yura.mobile.gui.cellrenderer.DefaultListCellRenderer;
import net.yura.domination.engine.core.Player;

import java.util.Collection;

/**
 * @author Yura
 */
public class PlayerList extends List {

    private Risk risk;

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
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(String actionCommand) {
                DominationMain.openURL("native://net.yura.domination.android.ColorPickerActivity", new DominationMain.ActivityResultListener() {
                    public void onActivityResult(Object data) {
                        Player player = (Player)getSelectedValue();
                        int color = (Integer)data;
                        if (player.getColor() != color) {
                            Player playerWithColor = getPlayerByColor(color);
                            if (playerWithColor == null) {
                                risk.parser("delplayer "+player);
                                risk.parser("newplayer "+risk.getType(player.getType())+" "+color+" "+player.getName());
                            }
                            else {
                                risk.parser("delplayer "+player);
                                risk.parser("delplayer "+playerWithColor);
                                risk.parser("newplayer "+risk.getType(playerWithColor.getType())+" "+player.getColor()+" "+playerWithColor.getName());
                                risk.parser("newplayer "+risk.getType(player.getType())+" "+color+" "+player.getName());
                            }
                        }
                    }
                    @Override
                    public void onCanceled() {
                        // dont care
                    }
                });
            }
        });
    }

    private Player getPlayerByColor(int color) {
        for (Player player : (Collection<Player>)risk.getGame().getPlayers()) {
            if (player.getColor() == color) {
                return player;
            }
        }
        return null;
    }

    public void setGame(Risk risk) {
        this.risk = risk;
    }

    public int getSize() {
        return risk.getGame().getNoPlayers();
    }

    public Object getElementAt(int index) {
        return risk.getGame().getPlayers().get(index);
    }

}
