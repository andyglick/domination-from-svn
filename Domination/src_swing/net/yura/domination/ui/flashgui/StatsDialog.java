// Yura Mamyrin

package net.yura.domination.ui.flashgui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskUIUtil;
import net.yura.domination.engine.guishared.StatsPanel;
import net.yura.domination.engine.translation.TranslationBundle;

/**
 * <p> Move Dialog for FlashGUI </p>
 * @author Yura Mamyrin
 */

public class StatsDialog extends JDialog implements ActionListener {

	private BufferedImage Back;
	private Risk myrisk;
	private StatsPanel graph;
	private java.util.ResourceBundle resb;

	public StatsDialog(Frame parent, boolean modal, Risk r) {

		super(parent, modal);

		myrisk = r;

		Back = RiskUIUtil.getUIImage(this.getClass(),"graph.jpg");

		initGUI();

		setResizable(false);

		pack();

	}

	/** This method is called from within the constructor to initialize the form. */

	/**
	 * Initialises the GUI
	 */
	private void initGUI()
	{
		resb = TranslationBundle.getBundle();

		setTitle( resb.getString("swing.tab.statistics") );


		graphPanel thisgraph = new graphPanel();

		Dimension d = new Dimension(740, 600);
		thisgraph.setPreferredSize(d);
		thisgraph.setMinimumSize(d);
		thisgraph.setMaximumSize(d);

		thisgraph.setLayout(null);






		JButton statbutton;

		int x=49;
		int y=483;

		int w=107;
		int h=33;

		int s=1;

		thisgraph.addButton("countries",x,y,w,h,s);

		x=x+w;
		s++;

		thisgraph.addButton("armies",x,y,w,h,s);

		x=x+w;
		s++;

		thisgraph.addButton("kills",x,y,w,h,s);

		x=x+w;
		s++;

		thisgraph.addButton("casualties",x,y,w,h,s);

		x=x+w;
		s++;

		thisgraph.addButton("reinforcements",x,y,w,h,s);

		x=x+w;
		s++;

		thisgraph.addButton("continents",x,y,w,h,s);

		x=49;
		y=y+h;
		s++;

		thisgraph.addButton("empire",x,y,w,h,s);

		x=x+w;
		s++;

		thisgraph.addButton("attacks",x,y,w,h,s);

		x=x+w;
		s++;

		thisgraph.addButton("retreats",x,y,w,h,s);

		x=x+w;
		s++;

		thisgraph.addButton("victories",x,y,w,h,s);

		x=x+w;
		s++;

		thisgraph.addButton("defeats",x,y,w,h,s);

		x=x+w;
		s++;

		thisgraph.addButton("attacked",x,y,w,h,s);



		graph = new StatsPanel(myrisk);
		graph.setBounds(50,50,640,400);

		thisgraph.add(graph);

		getContentPane().add(thisgraph);

		addWindowListener(
				new java.awt.event.WindowAdapter() {
					public void windowClosing(java.awt.event.WindowEvent evt) {
						exitForm();
					}
				}
		);

	}

	public void actionPerformed(ActionEvent a) {

		graph.repaintStats( Integer.parseInt( a.getActionCommand() ) );
		graph.repaint();

	}

	/** Exit the Application */

	/**
	 * Closes the GUI
	 */
	private void exitForm() {

		((GameFrame)getParent()).displayGraph();

	}

	class graphPanel extends JPanel {

		/**
		 * Paints the graphic
		 * @param g Graphics
		 */
		public void paintComponent(Graphics g) {

			g.drawImage(Back.getSubimage(0, 0, 50, 400), 0, 50, this);
			g.drawImage(Back.getSubimage(50, 0, 50, 400), 690, 50, this);

			g.drawImage(Back.getSubimage(100, 0, 740, 50), 0, 0, this);
			g.drawImage(Back.getSubimage(100, 182, 740, 150), 0, 450, this);

		}

		public void addButton(String a, int x,int y,int w,int h,int s) {

			JButton statbutton = new JButton(resb.getString("swing.toolbar."+a));
			statbutton.setActionCommand(s+"");
			statbutton.addActionListener( StatsDialog.this );
			statbutton.setBounds(x, y, w , h );

			NewGameFrame.sortOutButton( statbutton, Back.getSubimage(x+100,y-433+165,w,h), Back.getSubimage(x+100,y-433,w,h), Back.getSubimage(x+100,y-433+66,w,h) );

			add(statbutton);
		}

	}

}
