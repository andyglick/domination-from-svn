package net.yura.domination.lobby.client;

import risk.engine.*;
import risk.ui.FlashGUI.*;

import javax.swing.JFrame;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import javax.swing.JPanel;
import javax.swing.JLayeredPane;

import javax.swing.JFileChooser;

import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.ButtonGroup;
import javax.swing.JTextField;
import javax.swing.JRadioButton;
import javax.swing.ImageIcon;
import java.awt.event.*;
import javax.swing.JCheckBox;
import javax.swing.JToggleButton;
import javax.swing.AbstractButton;

import javax.swing.JOptionPane; // just needed for testing
import java.awt.*;
import java.util.ResourceBundle;

import javax.swing.text.PlainDocument;
import javax.swing.text.Document;
import javax.swing.text.AttributeSet;
import javax.swing.JTextArea;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.JScrollPane;
import javax.swing.JDialog;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import javax.swing.JSpinner;
import javax.swing.Box;
import javax.swing.BorderFactory;

import javax.swing.SpinnerNumberModel;

/**
 * <p> New Game Frame for FlashGUI </p>
 * @author Yura Mamyrin
 */

public class GameSetupPanel extends JPanel implements ActionListener {

	private BufferedImage newgame;
	private BufferedImage game2;
	private BufferedImage card;

	private JLabel mapPic;
	private JPanel mapsMissions;

	private JButton start;
	//private JButton help;
	private JButton cancel;

	private JRadioButton domination;
	private JRadioButton capital;
	private JRadioButton mission;

	private JRadioButton fixed;
	private JRadioButton increasing;

	private JCheckBox AutoPlaceAll;
	private JCheckBox recycle;

	private ResourceBundle resb;

	private String options;
	private JList list;

	private JDialog dialog;
	private RiskMap riskmap;

	private JSpinner human;
	private JSpinner aipassive;
	private JSpinner aieasy;
	private JSpinner aihard;

	private JTextField gamename;

	/**
	 * The NewGameFrame Constructor
	 * @param r The Risk Parser used for playing the game
	 * @param t States whether this game is local
	 */
	public GameSetupPanel(JDialog da,String myname) {

		dialog = da;

		setLayout(null);

		Dimension d = new Dimension(700, 600);

		setPreferredSize(d);
		setMinimumSize(d);
		setMaximumSize(d);


		try {
			newgame = ImageIO.read( NewGameFrame.class.getResource("newgame.jpg") );
			game2 = ImageIO.read( getClass().getResource("game2.jpg") );

			card = game2.getSubimage(0,247,23,35);

		}
		catch (IOException ex) { throw new RuntimeException(ex); }










		mapPic = new JLabel();
		mapPic.setBounds(51, 51, 203 , 127 );
		add(mapPic);


		mapsMissions = new JPanel();
		mapsMissions.setOpaque(false);
		mapsMissions.setLayout(new javax.swing.BoxLayout(mapsMissions, javax.swing.BoxLayout.Y_AXIS));

		final JScrollPane sp2 = new JScrollPane(mapsMissions);
		sp2.setBounds(340, 51, 309 , 210 ); // this will allow 6 players, 30 pixels per player
		sp2.setBorder(null);

		sp2.setOpaque(false);
		sp2.getViewport().setOpaque(false);

		add( sp2 );










		list = new JList();
		list.setCellRenderer(new RiskMapListCellRenderer());
		//list.setVisibleRowCount(10);
		list.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setBounds(54, 192, 200 , 260 );
		scrollPane.setBorder(null);
		list.setOpaque(false);
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);

		add(scrollPane);






		list.addListSelectionListener( new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {

				if (e.getValueIsAdjusting()) {return;}

				RiskMap it = ((RiskMap)list.getSelectedValue());

				if (it!=null) {

					riskmap = it;
					mapPic.setIcon( riskmap.getBigIcon() );

					mapsMissions.removeAll();

					String[] missions = riskmap.getMissions();

					for (int c=0;c<missions.length;c++) {

						mapsMissions.add( makeNewMission(missions[c]) );

						mapsMissions.add( Box.createVerticalStrut(3) );

					}

					if (missions.length == 0) {

						mapsMissions.add( new JLabel(" No missions for this map.") );

						if (mission.isSelected()) { domination.setSelected(true); AutoPlaceAll.setEnabled(true); }

					}

					mission.setEnabled( missions.length != 0 );

					mapsMissions.revalidate();
					// @TODO: scroll to the top

				}

			}


		});





		human = new JSpinner( new SpinnerNumberModel(2,1,6,1) );
		aipassive = new JSpinner( new SpinnerNumberModel(0,0,6,1) );
		aieasy = new JSpinner( new SpinnerNumberModel(2,0,6,1) );
		aihard = new JSpinner( new SpinnerNumberModel(2,0,6,1) );

		JPanel playernum = new JPanel();
		playernum.setBounds(300,310,400,60);
		playernum.setOpaque(false);
		add(playernum);

		playernum.add(new JLabel("human"));
		playernum.add(human);
		playernum.add(new JLabel("crap ai"));
		playernum.add(aipassive);
		playernum.add(new JLabel("easy ai"));
		playernum.add(aieasy);
		playernum.add(new JLabel("hard ai"));
		playernum.add(aihard);

		ButtonGroup GameTypeButtonGroup = new ButtonGroup();
		ButtonGroup CardTypeButtonGroup = new ButtonGroup();



// ALL PROBLEMS COME FROM THIS!!!!
// when this was at the start of the init loads of class problems started
resb = risk.engine.translation.TranslationBundle.getBundle();


		domination = new JRadioButton(resb.getString("newgame.mode.domination"), true);
		NewGameFrame.sortOutButton( domination );
		domination.setBounds(380, 370, 90 , 25 );
		domination.addActionListener(this);

		capital = new JRadioButton(resb.getString("newgame.mode.capital"));
		NewGameFrame.sortOutButton( capital );
		capital.setBounds(380, 390, 90 , 25 );
		capital.addActionListener(this);

		mission = new JRadioButton(resb.getString("newgame.mode.mission"));
		NewGameFrame.sortOutButton( mission );
		mission.setBounds(380, 410, 90 , 25 );
		mission.addActionListener(this);


		AutoPlaceAll = new JCheckBox(resb.getString("newgame.autoplace"));
		NewGameFrame.sortOutButton( AutoPlaceAll );
		AutoPlaceAll.setBounds(380, 440, 120 , 25 );



		increasing = new JRadioButton(resb.getString("newgame.cardmode.increasing"),true);
		NewGameFrame.sortOutButton( increasing );
		increasing.setBounds(500,370,90,25);

		fixed = new JRadioButton(resb.getString("newgame.cardmode.fixed"));
		NewGameFrame.sortOutButton( fixed );
		fixed.setBounds(500,390,90,25);

		recycle = new JCheckBox(resb.getString("newgame.recycle"));
		NewGameFrame.sortOutButton( recycle );
		recycle.setBounds(500, 440, 120 , 25 );


		GameTypeButtonGroup.add ( domination );
		GameTypeButtonGroup.add ( capital );
		GameTypeButtonGroup.add ( mission );

		CardTypeButtonGroup.add ( fixed );
		CardTypeButtonGroup.add ( increasing );


		add(domination);
		add(capital);
		add(mission);

		add(fixed);
		add(increasing);



		add(AutoPlaceAll);
		add(recycle);

		int w=115;
		int h=31;

		cancel = new JButton(resb.getString("newgame.cancel"));
		NewGameFrame.sortOutButton( cancel , newgame.getSubimage(41, 528, w, h) , newgame.getSubimage(700, 233, w, h) , newgame.getSubimage(700, 264, w, h) );
		cancel.addActionListener( this );
		cancel.setBounds(41, 528, 115 , 31 );

		//help = new JButton();
		//NewGameFrame.sortOutButton( help , newgame.getSubimage(335, 528, 30 , 30) , newgame.getSubimage(794, 171, 30 , 30) , newgame.getSubimage(794, 202, 30 , 30) );
		//help.addActionListener( this );
		//help.setBounds(335, 529, 30 , 30 ); // should be 528

		gamename = new JTextField(myname);
		gamename.setBounds(310, 530, 150 , 25 ); // should be 528
		add(gamename);

		start = new JButton(resb.getString("newgame.startgame"));
		NewGameFrame.sortOutButton( start , newgame.getSubimage(544, 528, w, h) , newgame.getSubimage(700, 295, w, h) , newgame.getSubimage(700, 326, w, h) );
		start.addActionListener( this );
		start.setBounds(544, 528, 115 , 31 );

		add(cancel);
		//add(help);
		add(start);

		list.setFixedCellHeight(33);

	}

	public void setMaps(final RiskMap[] maps) {

		list.setListData(maps);

	}

	public JList getList() {

		return list;
	}

	public void setSelected(int a) {

		list.setSelectedIndex(a);

	}

	public JPanel makeNewMission(String a) {


			JPanel mission = new JPanel() {

				public void paintComponent(Graphics g) {

					g.setColor( new Color(255,255,255,100) );
					g.fillRect(0,0,getWidth(),getHeight());

				}
			};

			//Dimension size = new Dimension(290, 35);

			//mission.setPreferredSize(size);
			//mission.setMinimumSize(size);
			//mission.setMaximumSize(size);
			mission.setOpaque(false);
			mission.setLayout( new BorderLayout() );

			JLabel cl = new JLabel( new ImageIcon(card) );
			cl.setBorder( BorderFactory.createEmptyBorder(1,3,1,3) );

			mission.add( cl , BorderLayout.WEST );

			JTextArea text = new JTextArea(a);
			text.setLineWrap(true);
			text.setWrapStyleWord(true);
			text.setEditable(false);

			text.setOpaque(false);

			//JScrollPane sp = new JScrollPane(text);
			//sp.setBorder(null);

			//sp.setOpaque(false);
			//sp.getViewport().setOpaque(false);

			//mission.add( sp );
			mission.add( text );

			//mission.setBackground( new Color(255,255,255,100) );

			return mission;


	}

	public String getOptions() {

		return options;

	}

	public String getGameName() {

		return gamename.getText();

	}

	public int getNumberOfHumanPlayers() {

		return ((Integer)human.getValue()).intValue();

	}

	public void paintComponent(Graphics g) {

			((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

//			  destination		source
			g.drawImage(newgame,0,0,this);


			g.drawImage(game2.getSubimage(0,0,223,155), 41 ,185 ,this);
			g.drawImage(game2.getSubimage(25,155,169,127), 391 ,325 ,this );

			g.setColor( Color.black );

			g.drawString( resb.getString("newgame.label.map"), 55, 40);
			g.drawString( "Missions:", 350, 40);

			g.drawString( "Number of Players", 440, 300);

			g.drawString( "Game Name:", 240, 545);

			g.drawString( resb.getString("newgame.label.gametype"), 400, 365);
			g.drawString( resb.getString("newgame.label.cardsoptions"), 515, 365);


	}

	public void reset() {

		options=null;
	}

	/**
	 * Actionlistener applies the correct command to the button pressed
	 * @param e The ActionEvent Object
	 */
	public void actionPerformed(ActionEvent e) {

		if (e.getSource()==start) {

			int a = ((Integer)human.getValue()).intValue();
			int b = ((Integer)aipassive.getValue()).intValue();
			int c = ((Integer)aieasy.getValue()).intValue();
			int d = ((Integer)aihard.getValue()).intValue();

			int sum = a+b+c+d;

			if (sum >=2 && sum <= risk.engine.core.RiskGame.MAX_PLAYERS) {

				String players = 
				//a +"\n" +
				b +"\n" +
				c +"\n" +
				d +"\n";


				String type="";

				if (domination.isSelected()) type = "domination";
				else if (capital.isSelected()) type = "capital";
				else if (mission.isSelected()) type = "mission";

				if (increasing.isSelected()) type += " increasing";
				else if (fixed.isSelected()) type += " fixed";

				if ( AutoPlaceAll.isSelected() ) type += " autoplaceall";
				if ( recycle.isSelected() ) type += " recycle";

				options = players+ "choosemap "+riskmap.getFileName() +"\nstartgame " + type;

				dialog.setVisible(false);

			}
			else {

				JOptionPane.showMessageDialog(this, resb.getString("newgame.error.numberofplayers") , resb.getString("newgame.error.title"), JOptionPane.ERROR_MESSAGE );

			}

		}/*
		else if (e.getSource()==help) {

			try {
				Risk.openDocs( resb.getString("helpfiles.flash") );
			}
			catch(Exception er) {
				JOptionPane.showMessageDialog(this,"Unable to open manual: "+er.getMessage(),"Error", JOptionPane.ERROR_MESSAGE);
			}

		}*/
		else if (e.getSource()==cancel) {

			dialog.setVisible(false);

		}
		else if (e.getSource()==mission) {

			AutoPlaceAll.setEnabled(false);

		}
		else if (e.getSource()==domination) {

			AutoPlaceAll.setEnabled(true);

		}
		else if (e.getSource()==capital) {

			AutoPlaceAll.setEnabled(true);

		}
	}



    class RiskMapListCellRenderer extends DefaultListCellRenderer {
       public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus)
        {
            Component retValue = super.getListCellRendererComponent(
		list, value, index, isSelected, cellHasFocus
 	    );



		if (isSelected) { setBackground( new Color(255,255,255,100) ); }
		else { setBackground( new Color(0,0,0,0) ); }

            setIcon( ((RiskMap)value).getIcon() );
	    return retValue;
        }

    }



}
