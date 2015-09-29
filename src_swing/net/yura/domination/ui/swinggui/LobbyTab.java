package net.yura.domination.ui.swinggui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskUIUtil;
import net.yura.domination.engine.SwingMEWrapper;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.lobby.mini.MiniLobbyClient;
import net.yura.lobby.model.Game;
import net.yura.me4se.ME4SEPanel;
import net.yura.mobile.gui.components.Label;

/**
 * @author Yura Mamyrin
 */
public class LobbyTab extends ME4SEPanel implements SwingGUITab,ActionListener {

    ResourceBundle resb = TranslationBundle.getBundle();
    
    MiniLobbyClient mlc;
    Risk risk;

    JToolBar toolbar;
    JButton start,stop,open;
    
    public LobbyTab(Risk myrisk) {
        getApplicationManager().applet = RiskUIUtil.applet;
        risk = myrisk;

        toolbar = new JToolBar();
        toolbar.setRollover(true);
	toolbar.setFloatable(false);

        start = new JButton("Start Lobby");
	start.setActionCommand("start");
	start.addActionListener(this);
	toolbar.add(start);

        stop = new JButton("Stop Lobby");
	stop.setActionCommand("stop");
	stop.addActionListener(this);
	toolbar.add(stop);

        open = new JButton("Open Game");
	open.setActionCommand("open");
	open.addActionListener(this);
        open.setEnabled(false);
	toolbar.add(open);

        updateButton();
        
        add( new Label("click start in the toolbar") );
    }

    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        if ("start".equals(action)) {
            createLobby();
            updateButton();
        }
        else if ("stop".equals(action)) {
            closeLobby();
            updateButton();
        }
        else if ("open".equals(action)) {
            String input = JOptionPane.showInputDialog("game id:");
            if (input != null) {
                try {
                    int id = Integer.parseInt(input);
                    Game game = mlc.findGame(id);
                    if (game != null) {
                        mlc.playGame(game);
                    }
                    else {
                        JOptionPane.showMessageDialog(this, "game " + id + " not found");
                    }
                }
                catch(NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, input + " not a number");
                }
            }
        }
        else {
            throw new RuntimeException("unknown action " + action);
        }
    }
    
    private void updateButton() {
        start.setEnabled( mlc==null );
        stop.setEnabled( mlc!=null );
    }

    public JToolBar getToolBar() {
        return toolbar;
    }
    public JMenu getMenu() {
        return null;
    }
    public String getName() {
        return resb.getString("swing.tab.online");
    }



    void createLobby() {
        mlc = SwingMEWrapper.makeMiniLobbyClient(risk, SwingUtilities.getWindowAncestor(this) );
        mlc.removeBackButton();
        add( mlc.getRoot() );
        open.setEnabled(true);
    }
    
    void closeLobby() {
        mlc.destroy();
        mlc = null;
        add( new Label("no lobby") ); // can throw when DesktopPane is null (open and close MapStore)
        open.setEnabled(false);
    }

}
