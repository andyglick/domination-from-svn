package net.yura.domination.engine;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.domination.mapstore.MapChooser;
import net.yura.mobile.gui.ActionListener;
import net.yura.tools.translation.ME4SEPanel;

/**
 * @author Yura Mamyrin
 */
public class MapChooserSwingWrapper implements ActionListener {

    JDialog dialog;
    ME4SEPanel wrapper;
    MapChooser chooser;

    public MapChooserSwingWrapper() {

        wrapper = new ME4SEPanel();

        chooser = new MapChooser(this);

        wrapper.add( chooser.getRoot() );
    }

    // called when the map chooser is closed
    public void actionPerformed(String arg0) {
        dialog.setVisible(false);
    }

    public String getNewMap(Frame parent) {

        dialog = new JDialog(parent, TranslationBundle.getBundle().getString("newgame.choosemap") , true);
        dialog.setDefaultCloseOperation( JDialog.DO_NOTHING_ON_CLOSE );

        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                actionPerformed(null); // same as pressing cancel
            }
        });

        dialog.add(wrapper);
        //dialog.pack();
        dialog.setSize(320,480); // same as Jesus Piece
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        // come here when the dialog is closed

        dialog.dispose();

        return chooser.getSelectedMap();

    }

}
