// Yura Mamyrin

package risk.ui.SwingGUI;

import risk.engine.*;
import risk.engine.core.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;
import javax.swing.colorchooser.*;
import javax.swing.filechooser.*;
import java.awt.image.BufferedImage;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.net.*;
import javax.imageio.ImageIO;

/**
 * @author Yura Mamyrin
 */

public class BugsPanel extends JPanel implements ActionListener, SwingGUITab {

	private JToolBar toolbar;
	private JTextArea text;
	private JTextField from;

	public BugsPanel() {

		setName( "Report Bugs" );

		setOpaque(false);

		toolbar = new JToolBar();

		toolbar.setRollover(true);
		toolbar.setFloatable(false);

		JButton send = new JButton("SEND MESSAGE");
		send.setActionCommand("send");
		send.addActionListener(this);
		toolbar.add(send);

		text = new JTextArea();
		from = new JTextField();

		setLayout( new BorderLayout() );

		JPanel top = new JPanel();
		top.setLayout( new BorderLayout() );
		top.setOpaque(false);

		top.add( new JLabel("type your bug/suggestion to yura and hit send at the top"), BorderLayout.NORTH );
		top.add( new JLabel("your Email:") , BorderLayout.WEST );
		top.add( from );

		add( top, BorderLayout.NORTH );
		add( new JScrollPane(text) );

	}

	public void actionPerformed(ActionEvent a) {

		if (a.getActionCommand().equals("send")) {

			try {

				RiskUtil.sendText( from.getText(), text.getText(),"Suggestion" );

				JOptionPane.showMessageDialog(this, "SENT!");


			}
			catch(Exception ex) {

				JOptionPane.showMessageDialog(this, "ERROR: "+ex.getMessage() );

			}
		}

	}

	public JToolBar getToolBar() {

		return toolbar;

	}
	public JMenu getMenu() {

		return null;

	}

}
