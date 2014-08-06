package net.yura.domination.mobile.flashgui;

import net.yura.domination.engine.Risk;
import net.yura.domination.mobile.PicturePanel;
import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.DesktopPane;
import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.components.List;
import net.yura.mobile.gui.components.Component;
import net.yura.mobile.gui.cellrenderer.DefaultListCellRenderer;
import net.yura.domination.engine.core.Player;
import net.yura.mobile.gui.layout.XULLoader;
import java.util.Collection;
import javax.microedition.lcdui.Image;

/**
 * @author Yura
 */
public class PlayerList extends List {

    private Risk risk;

    public PlayerList() {
        setLayoutOrientation(HORIZONTAL);
        int size = XULLoader.adjustSizeToDensity(75);
        DesktopPane dp = DesktopPane.getDesktopPane();
        // (10 is padding of 5 from the xml layout * 2)
        int screen = ((dp.getWidth() > dp.getHeight() ? dp.getHeight() : dp.getWidth()) - XULLoader.adjustSizeToDensity(10)) / 6;
        size = size > screen ? screen : size;
        final int iconSize = size - XULLoader.adjustSizeToDensity(4);
        setFixedCellWidth(size);
        setFixedCellHeight(size);
        setCellRenderer(new DefaultListCellRenderer() {
            {
                setName("ListRendererCollapsed");
            }
            @Override
            public void setValue(Object obj) {
                Player player = (Player) obj;
                setBackground(player.getColor());
            }

            @Override
            public void paintComponent(Graphics2D g) {
                super.paintComponent(g);

                int color = getBackground();
                Image img = PicturePanel.getIconForColor(color);

                if (img != null) {
                    int h = iconSize;
                    int w = (int)(img.getWidth()*(h/(double)img.getHeight()));
                    g.drawScaledImage(img, (getWidth()-w)/2, (getHeight()-h)/2, w, h);
                }
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
                                player.setColor(color);
                                PlayerList.this.repaint();
                                //risk.parser("delplayer "+player);
                                //risk.parser("newplayer "+risk.getType(player.getType())+" "+color+" "+player.getName());
                            }
                            else {
                                playerWithColor.setColor(player.getColor());
                                player.setColor(color);
                                PlayerList.this.repaint();
                                //risk.parser("delplayer "+player);
                                //risk.parser("delplayer "+playerWithColor);
                                //risk.parser("newplayer "+risk.getType(playerWithColor.getType())+" "+player.getColor()+" "+playerWithColor.getName());
                                //risk.parser("newplayer "+risk.getType(player.getType())+" "+color+" "+player.getName());
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
