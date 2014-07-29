package net.yura.domination.mobile.flashgui;

import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.Midlet;
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
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(String actionCommand) {
                DominationMain.openURL("native://net.yura.domination.android.ColorPickerActivity", new DominationMain.ActivityResultListener() {
                    public void onActivityResult(Object data) {
                        GameActivity.toast("color="+data);
                    }
                    @Override
                    public void onCanceled() {
                        GameActivity.toast("canceled");
                    }
                });
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
