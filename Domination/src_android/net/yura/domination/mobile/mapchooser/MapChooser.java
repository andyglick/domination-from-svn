package net.yura.domination.mobile.mapchooser;

import net.yura.mobile.gui.ButtonGroup;
import net.yura.mobile.gui.Icon;
import net.yura.mobile.gui.components.RadioButton;

public class MapChooser {

    public MapChooser() {
        Icon on,off;

        try {
            on = new Icon("/bar_on.png");
            off = new Icon("/bar_off.png");
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        RadioButton[] buttons = new RadioButton[6];

        ButtonGroup group = new ButtonGroup();
        int w = off.getIconWidth() / buttons.length;
        for (int c=0;c<buttons.length;c++) {
            buttons[c] = new RadioButton();
            group.add(buttons[c]);
            buttons[c].setIcon( off.getSubimage(c*w, 0, w, off.getIconHeight()) );
            buttons[c].setSelectedIcon( on.getSubimage(c*w, 0, w, off.getIconHeight()) );
        }

    }

}
