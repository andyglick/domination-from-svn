// Yura Mamyrin

package net.yura.domination.ui.swinggui;


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import net.yura.domination.engine.RiskUIUtil;
import net.yura.domination.engine.RiskUtil;

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

				RiskUIUtil.sendText( from.getText(), text.getText(),"Suggestion" );

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
