package net.yura.domination.engine;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;
import net.yura.domination.lobby.mini.MiniLobbyClient;
import net.yura.domination.lobby.mini.MiniLobbyRisk;
import net.yura.lobby.model.Game;
import net.yura.lobby.model.GameType;
import net.yura.me4se.ME4SEPanel;
import net.yura.mobile.gui.ActionListener;

/**
 * @author Yura Mamyrin
 */
public class MiniLobbySwingWrapper implements ActionListener {

    JDialog dialog;
    ME4SEPanel wrapper;
    MiniLobbyClient mlc;

    public MiniLobbySwingWrapper(Risk myrisk) {

        wrapper = new ME4SEPanel(); // this sets the theme to NimbusLookAndFeel

        wrapper.getApplicationManager().applet = RiskUIUtil.applet;

        MiniLobbyRisk mlgame = new MiniLobbyRisk(myrisk) {

                private net.yura.domination.lobby.client.GameSetupPanel gsp;
                public void openGameSetup(GameType gameType) {

                        // TODO how do i get the mini lobby main Swing window
                        //EmptyMidlet midlet = (EmptyMidlet)Midlet.getMidlet();
                        //ME4SEPanel panel = midlet.getParent();
                        //Container container = javax.microedition.midlet.ApplicationManager.getInstance().awtContainer;
                        //(java.awt.Window)javax.swing.SwingUtilities.getAncestorOfClass(java.awt.Window.class, container)

                        if (gsp==null) {
                            gsp = new net.yura.domination.lobby.client.GameSetupPanel();
                        }

                        Game result = gsp.showDialog( dialog , gameType.getOptions(), lobby.whoAmI() );

                        if (result!=null) {
                            lobby.createNewGame(result);
                        }

                }
                
        };

        mlc = new MiniLobbyClient( mlgame );

        wrapper.add( mlc.getRoot() );
    }

    // called when the map chooser is closed
    public void actionPerformed(String arg0) {
        dialog.setVisible(false);
    }

    public void show(Frame parent) {

        dialog = new JDialog(parent, "TODO title" , true);
        dialog.setDefaultCloseOperation( JDialog.DO_NOTHING_ON_CLOSE );

        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                actionPerformed(null); // same as pressing cancel
            }
        });

        dialog.getContentPane().add(wrapper);
        //dialog.pack();
        dialog.setSize(320,480); // same as Jesus Piece
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);


        dialog.dispose();
        
        wrapper.destroy();

    }

}
