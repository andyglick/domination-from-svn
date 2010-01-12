// Yura Mamyrin, Group D

package risk.ui.SwingGUI;

import risk.engine.*;
import risk.engine.guishared.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Vector;
import java.util.ResourceBundle;

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

		RiskUtil.parseArgs(argv);

		SwingGUIPanel sg = new SwingGUIPanel( new Risk(null) );

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
