package net.yura.domination.mobile.flashgui;

import java.util.List;
import java.util.Locale;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.mobile.MiniUtil;
import net.yura.lobby.mini.MiniLobbyClient;
import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.Midlet;
import net.yura.mobile.gui.components.Button;
import net.yura.mobile.gui.components.FileChooser;
import net.yura.mobile.gui.components.Frame;
import net.yura.mobile.gui.components.OptionPane;
import net.yura.mobile.gui.components.ScrollPane;
import net.yura.mobile.gui.components.Window;
import net.yura.mobile.gui.layout.XULLoader;
import net.yura.mobile.util.Properties;
import net.yura.mobile.util.Url;

/**
 * @author Yura Mamyrin
 */
public class MainMenu extends Frame implements ActionListener {

    // shares res
    Properties resb = GameActivity.resb;
    public Risk myrisk;
    MiniFlashRiskAdapter controller;

    // main menu res
    FileChooser chooser;

    public MainMenu(Risk risk,MiniFlashRiskAdapter controller) {
        myrisk = risk;
        this.controller = controller;

        //setTitle( resb.getProperty("mainmenu.title") );
        setUndecorated(true);

        setMaximum(true);

        setBorder(GameActivity.marble);
        setBackground( 0x00FFFFFF );
    }

    XULLoader mainMenu;
    public void openMainMenu() {

	mainMenu = GameActivity.getPanel("/mainmenu.xml",this);

        //Component onlineButton = loader.find("OnlineButton");
        //if (onlineButton!=null) {
        //    onlineButton.setVisible( Locale.getDefault().equals(new Locale("en","GB")) );
        //}

        setContentPane( new ScrollPane( mainMenu.getRoot() ) );
        revalidate();

        setVisible(true);
        moveToBack();
    }

    public void setPlayGamesSingedIn(boolean in) {
	if (mainMenu!=null) {
            Button showAchievements = (Button)mainMenu.find("showAchievements");
	    Button signIn = (Button)mainMenu.find("signIn");
	    Button signOut = (Button)mainMenu.find("signOut");
            if (showAchievements != null) { showAchievements.setVisible(true); }
            if (signIn != null) { signIn.setVisible(!in); }
	    if (signOut != null) { signOut.setVisible(in); }
	    mainMenu.getRoot().revalidate();
	    mainMenu.getRoot().repaint();
	}
    }

    private void moveToBack() {
        // we want to always be at the bottom of the stack
        // so move anything bellow us to be above us
        List windows = getDesktopPane().getAllFrames();
        for (int c=1;c<windows.size();c++) {
            getDesktopPane().setSelectedFrame((Window)windows.get(0));
        }
    }

    @Override
    public void actionPerformed(String actionCommand) {
            if ("new game".equals(actionCommand)) {
                myrisk.parser("newgame");
            }
            else if ("load game".equals(actionCommand)) {

                chooser = new FileChooser();
                chooser.setCurrentDirectory( MiniUtil.getSaveGameDirURL() );
                chooser.showDialog(this, "doLoad", resb.getProperty("mainmenu.loadgame.loadbutton") , resb.getProperty("mainmenu.loadgame.loadbutton") );
            }
            else if ("doLoad".equals(actionCommand)) {

                String file = chooser.getSelectedFile();
                chooser = null;

                if (file.endsWith( GameActivity.SAVE_EXTENSION )) {
                    myrisk.parser("loadgame " + file );
                }
                // else ignore file
            }
            else if (FileChooser.NO_FILE_SELECTED.equals(actionCommand)) {
                chooser = null;
            }
            else if ("manual".equals(actionCommand)) {

                //WebView webView = new WebView( AndroidMeActivity.DEFAULT_ACTIVITY );
                //webView.loadUrl("file:///android_asset/help/index.htm");
                //AndroidMeActivity.DEFAULT_ACTIVITY.setContentView(webView);

                MiniUtil.openHelp();
            }
            else if ("about".equals(actionCommand)) {
                MiniUtil.showAbout();
            }
            else if ("quit".equals(actionCommand)) {
                // HACK: if the user hits quit 2 times in a row,
                // the 2nd event may throw a nullpointer as desktopPane is set to null after the 1st
                if (net.yura.mobile.gui.DesktopPane.getDesktopPane() != null) {
                    Midlet.exit();
                }
            }
            else if ("donate".equals(actionCommand)) {
                try {
                    RiskUtil.donate();
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            else if ("online".equals(actionCommand)) {
        	controller.openLobby();
            }
            else if ("join game".equals(actionCommand)) {
                OptionPane.showMessageDialog(null,"not done yet","Error", OptionPane.ERROR_MESSAGE);
            }
            else if ("start server".equals(actionCommand)) {
                OptionPane.showMessageDialog(null,"not done yet","Error", OptionPane.ERROR_MESSAGE);
            }
            else if ("feedback".equals(actionCommand)) {
                String url = "mailto:yura@yura.net" +
                        "?subject=" + Url.encode(
                                RiskUtil.GAME_NAME+" "+Risk.RISK_VERSION+" "+
                                DominationMain.product+" "+DominationMain.version+" "+
                                Locale.getDefault()+" Feedback").replace("+", "%20") // platforms do not seem to support + char here
                        +"&body=" + Url.encode(
                                "\n\n\nDevice: "+System.getProperty("http.agent")+
                                "\nID: "+MiniLobbyClient.getMyUUID()).replace("+", "%20");
                Midlet.openURL(url);
            }
            else if ("signIn".equals(actionCommand)) {
        	DominationMain.getGooglePlayGameServices().beginUserInitiatedSignIn();
            }
            else if ("signOut".equals(actionCommand)) {
        	DominationMain.getGooglePlayGameServices().signOut();
        	setPlayGamesSingedIn(false);
            }
            else if ("showAchievements".equals(actionCommand)) {
        	DominationMain.getGooglePlayGameServices().showAchievements();
            }
            else {
                System.err.println("Unknown command: "+actionCommand);
            }
    }

}
