package net.yura.domination.mobile.flashgui;

import java.util.Arrays;
import java.util.List;
import javax.microedition.lcdui.Graphics;
import net.yura.domination.mobile.PicturePanel;
import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.ButtonGroup;
import net.yura.mobile.gui.components.Button;
import net.yura.mobile.gui.components.ComboBox;
import net.yura.mobile.gui.components.Panel;
import net.yura.mobile.gui.layout.BoxLayout;
import net.yura.mobile.gui.layout.FlowLayout;
import net.yura.mobile.util.Option;
import net.yura.mobile.util.Properties;

/**
 * @author Yura
 */
public class MapViewChooser extends Panel implements ActionListener {

    Properties resb = GameActivity.resb;
    Option[] options;
    PicturePanel pp;
    
    public MapViewChooser(PicturePanel pp) {
        this.pp = pp;
        
        Button test = new Button("test");
        test.workoutPreferredSize();

        setPreferredSize(10, test.getHeightWithBorder()); // some small size, but we will strech

        options = new Option[6];
        options[0] = new Option( String.valueOf( PicturePanel.VIEW_CONTINENTS ) , resb.getProperty("game.tabs.continents") );
        options[1] = new Option( String.valueOf( PicturePanel.VIEW_OWNERSHIP ) , resb.getProperty("game.tabs.ownership") );
        options[2] = new Option( String.valueOf( PicturePanel.VIEW_BORDER_THREAT ) , resb.getProperty("game.tabs.borderthreat") );
        options[3] = new Option( String.valueOf( PicturePanel.VIEW_CARD_OWNERSHIP ) , resb.getProperty("game.tabs.cardownership") );
        options[4] = new Option( String.valueOf( PicturePanel.VIEW_TROOP_STRENGTH ) , resb.getProperty("game.tabs.troopstrength") );
        options[5] = new Option( String.valueOf( PicturePanel.VIEW_CONNECTED_EMPIRE ) , resb.getProperty("game.tabs.connectedempire") );        
        
    }
    
    @Override
    public void setSize(int width, int height) {

        Option currentOption = getMapViewOption();

        int buttonsWidth = 0;
        Button[] buttons = new Button[options.length];
        for (int c=0;c<buttons.length;c++) {
            buttons[c] = new Button( options[c].getValue() );
            buttons[c].setActionCommand( options[c].getKey() );
            
            if (c==0) {
                buttons[c].setName("SegmentedControlLeft");
            }
            else if (c== (buttons.length-1) ) {
                buttons[c].setName("SegmentedControlRight");
            }
            else {
                buttons[c].setName("SegmentedControlMiddle");
            }
            
            buttons[c].workoutPreferredSize();
            buttonsWidth = buttonsWidth + buttons[c].getWidthWithBorder();
            if (currentOption == options[c]) {
                buttons[c].setSelected(true);
            }
        }

        removeAll();
        if (buttonsWidth <= width) {
            setLayout( new FlowLayout(Graphics.HCENTER,0) );
            ButtonGroup group = new ButtonGroup();
            for (Button b: buttons) {
                group.add(b);
                b.addActionListener(this);
                add(b);
            }
        }
        else {
            ComboBox combo = new ComboBox( new java.util.Vector( Arrays.asList( options ) ) );
            combo.setSelectedItem(currentOption);
            combo.workoutPreferredSize();
            combo.addActionListener(this);
            setLayout( new BoxLayout(Graphics.HCENTER) );
            add(combo);
        }
        
        super.setSize(width, height);
    }

    public void actionPerformed(String actionCommand) {
        pp.repaintCountries( getMapView() );
        pp.repaint();
    }
    
    Option getMapViewOption() {
        
        List components = getComponents();
        if (components.isEmpty()) {
            return options[0]; // default
        }
        else if (components.get(0) instanceof ComboBox) {
            return (Option) ((ComboBox)components.get(0)).getSelectedItem();
        }
        else {
            for (Button b: (List<Button>)components) {
                if (b.isSelected()) {
                    String id = b.getActionCommand();
                    for (int c=0;c<options.length;c++) {
                        if (id.equals( options[c].getKey() ) ) {
                            return options[c];
                        }
                    }
                }
            }
            throw new RuntimeException("no button selected");
        }
        
    }

    public int getMapView() {
        return Integer.parseInt( getMapViewOption().getKey() );
    }
    
    public void resetMapView() {
        List components = getComponents();
        if (components.isEmpty()) {
            // do nothing
        }
        else if (components.get(0) instanceof ComboBox) {
            ((ComboBox)components.get(0)).setSelectedIndex(0);
        }
        else {
            ((Button)components.get(0)).setSelected(true);
        }
    }
    
}
