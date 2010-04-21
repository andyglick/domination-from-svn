// Yura Mamyrin, Group D

package net.yura.domination.ui.swinggui;

import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JFrame;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskUIUtil;
import net.yura.domination.engine.guishared.AboutDialog;

/**
 * <p> Swing GUI Main Frame </p>
 * @author Yura Mamyrin
 */

public class SwingGUIFrame {

	/**
	 * This runs the program
	 * @param argv
	 */
	public static void main(String[] argv) {

		RiskUIUtil.parseArgs(argv);

		SwingGUIPanel sg = new SwingGUIPanel( new Risk() );

		JFrame gui = new JFrame();

		gui.setContentPane( sg );

		gui.setTitle( SwingGUIPanel.product );
		gui.setIconImage(Toolkit.getDefaultToolkit().getImage( AboutDialog.class.getResource("icon.gif") ));

		gui.pack();

		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = gui.getSize();
		frameSize.height = ((frameSize.height > screenSize.height) ? screenSize.height : frameSize.height);
		frameSize.width = ((frameSize.width > screenSize.width) ? screenSize.width : frameSize.width);
		gui.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);

		try {

			gui.setMinimumSize( gui.getPreferredSize() );

		}
		catch(NoSuchMethodError ex) {

			// must me java 1.4
			gui.setResizable(false);

		}

		gui.setVisible(true);

		sg.setupLobbyButton();

	}

}
