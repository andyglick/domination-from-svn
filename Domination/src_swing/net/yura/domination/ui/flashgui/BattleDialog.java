// Yura Mamyrin, Group D

package net.yura.domination.ui.flashgui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.Random;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskUIUtil;
import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.translation.TranslationBundle;

/**
 * <p> Battle Dialog for FlashGUI </p>
 * @author Yura Mamyrin
 */
public class BattleDialog extends JDialog implements MouseListener {

	private GameFrame gui;
	private Risk myrisk;

	private BufferedImage c1img;
	private BufferedImage c2img;

	private int c1num;
	private int c2num;

	private BufferedImage Battle;
	private BufferedImage Back;

	private JButton button;
	private JButton retreat;

	private Country country1;
	private Country country2;

	private Color color1;
	private Color color2;

	private boolean canRetreat;

	private javax.swing.Timer timer;
	private int[] att;
	private int[] def;

	private int max; // indicates input is needed if > 0

	private int noda,nodd;
        private boolean spinA,spinD;

	private BufferedImage[] attackerSpins;
	private BufferedImage[] defenderSpins;

	private java.util.ResourceBundle resb;
	private JPanel battle;

	/**
	 * Creates a new BattleDialog
	 * @param parent decides the parent of the frame
	 * @param modal
	 * @param r the risk main program
	 */

	public BattleDialog(GameFrame parent, boolean modal, Risk r) {
		super(parent, modal);
		gui = parent;
		myrisk = r;

		setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

		Battle = RiskUIUtil.getUIImage(this.getClass(),"battle.jpg");

		Back = Battle.getSubimage(0, 0, 480, 350);

		int w=29;
		int h=29;

		attackerSpins = new BufferedImage[6];

		attackerSpins[0] = Battle.getSubimage(481, 43, w, h);
		attackerSpins[1] = Battle.getSubimage(481, 73, w, h);
		attackerSpins[2] = Battle.getSubimage(481, 103, w, h);
		attackerSpins[3] = Battle.getSubimage(541, 43, w, h);
		attackerSpins[4] = Battle.getSubimage(541, 73, w, h);
		attackerSpins[5] = Battle.getSubimage(541, 103, w, h);

		defenderSpins = new BufferedImage[6];

		defenderSpins[0] = Battle.getSubimage(511, 43, w, h);
		defenderSpins[1] = Battle.getSubimage(511, 73, w, h);
		defenderSpins[2] = Battle.getSubimage(511, 103, w, h);
		defenderSpins[3] = Battle.getSubimage(571, 43, w, h);
		defenderSpins[4] = Battle.getSubimage(571, 73, w, h);
		defenderSpins[5] = Battle.getSubimage(571, 103, w, h);

		initGUI();
		pack();
	}

	/** This method is called from within the constructor to initialize the dialog. */
	private void initGUI() {
		resb = TranslationBundle.getBundle();

		setResizable(false);

		battle = new BattlePanel();
		battle.setLayout(null);
		battle.addMouseListener(this);

		Dimension bSize = new Dimension(480, 350);

		battle.setPreferredSize( bSize );
		battle.setMinimumSize( bSize );
		battle.setMaximumSize( bSize );

		int w=88;
		int h=31;

		button = GameFrame.makeRiskButton( Battle.getSubimage(196, 270, w, h), Battle.getSubimage(481, 270, w, h), Battle.getSubimage(481, 238, w, h), Battle.getSubimage(481, 302, w, h) );
		button.setText(resb.getString("battle.roll"));
		button.setBounds(196, 270, 88, 31);

		retreat = GameFrame.makeRiskButton( Battle.getSubimage(487, 138, w, h), Battle.getSubimage(481, 206, w, h), Battle.getSubimage(481, 174, w, h), Battle.getSubimage(487, 138, w, h) );
		retreat.setText(resb.getString("battle.retreat"));
		retreat.setBounds(342, 270, 88, 31);

		button.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						gui.go( "roll " + (canRetreat?noda:nodd) );
					}
				}
		);

		retreat.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						gui.go( "retreat" );
					}
				}
		);

		battle.add(retreat);
		battle.add(button);

		getContentPane().add(battle);

		timer = new javax.swing.Timer(10, spinDiceAction());


		addWindowListener(
			new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent evt) {
					if (canRetreat) {
						gui.go( "retreat" );
					}
				}
			}
		);
	}

	/**
	 * Spins the images of the dice
	 * @return Action
	 */
	public Action spinDiceAction() {
		return new AbstractAction("spin dice action") {
			public void actionPerformed (ActionEvent e) {
				// repaint here slows the game down a lot!
				//repaint();
				drawDiceAnimated( battle.getGraphics() );
			}
		};
	}

	/*
	 * @param a the number of attacking armies
	 * @param b the number of defending armies
	 * @param ai the image of attacker
	 * @param bi the image of defender
	 * @param country1	attacking country
	 * @param country2	defending country
	 * @param c1 color of the attacker
	 * @param c2 color of the defender
	 */
	public void setup(int a, int b, BufferedImage ai, BufferedImage bi, Country country1, Country country2, Color c1, Color c2) {
		c1num=a;
		c2num=b;

		c1img = ai;
		c2img = bi;

		this.country1 = country1;
		this.country2 = country2;

		color1=c1;
		color2=c2;

		att=null;
		def=null;

		noda=0;
		nodd=0;

                spinA = false;
                spinD = false;

                blockInput();
        }

        public void blockInput() {
                button.setEnabled(false);
                retreat.setVisible(false);
		canRetreat=false;
		max=0;
		setTitle(resb.getString("battle.title"));
	}

	/**
	 * Sets number of attacking dice
	 * @param n number of dice
	 */
	public void setNODAttacker(int n) {
		att=null;
		def=null;
		noda = n;
                spinA = true;
		battle.repaint();
		timer.start();
	}

	/**
	 * Sets number of defending dice
	 * @param n number of dice
	 */
	public void setNODDefender(int n) {
		nodd = n;
                spinD = true;
                battle.repaint();
	}

	/**
	 * Shows the dice results
	 * @param atti the attacking results
	 * @param defi the defending results
	 */
	public void showDiceResults(int[] atti, int[] defi) {
		if( timer.isRunning() ) {
			timer.stop();
		}
		att=atti;
		def=defi;
                spinA = false;
                spinD = false;
		battle.repaint();
	}

	/**
	 * Checks to see if input is needed
	 * @param n Maximum number of dice
	 * @param c If you can retreat
	 */
	public void needInput(int n, boolean c) {
		button.setEnabled(true);
		max=n;
		canRetreat=c;
		att=null;
		def=null;
		if (canRetreat) {
                        if (noda==0 || noda > max) {
                            noda = max;
                        }
			retreat.setVisible(true);
			setTitle(resb.getString("battle.select.attack"));
		}
		else {
                        if (nodd==0 || nodd > max) {
                            nodd = max;
                        }
			setTitle(resb.getString("battle.select.defend"));
		}
		battle.repaint();
	}

	private static Random r = new Random();

	class BattlePanel extends JPanel {

		/**
		 * Paints the frame
		 * @param g The graphics
		 */
		public void paintComponent(Graphics g) {
			//super.paintComponent(g);

			g.drawImage( Back ,0 ,0 ,this );

			if (canRetreat) {
				g.drawImage( Battle.getSubimage(481, 133, 98, 40) ,336 ,265 ,this );
			}

                        MoveDialog.draw(g,
                                c1img, c2img,
                                color1, color2, 
                                country1.getName(), country2.getName(),
                                myrisk.hasArmiesInt(c1num),myrisk.hasArmiesInt(c2num) );

                        Graphics2D g2 = (Graphics2D)g;
                        FontRenderContext frc = g2.getFontRenderContext();
			Font font = g2.getFont();
                        TextLayout tl = new TextLayout( resb.getString("battle.select.dice") , font, frc);
                        g2.setColor( Color.BLACK );
			tl.draw( g2, (float) (240-(tl.getBounds().getWidth()/2)), 320f );

                        drawDiceSelect(g);
			drawDiceAnimated(g);
			drawDiceResults(g);
		}
	}

        private void drawDiceSelect(Graphics g) {

            // this is the max defend dice allowed for this battle
            int deadDiceD = myrisk.hasArmiesInt(c2num);
            if (deadDiceD > myrisk.getGame().getMaxDefendDice()) {
                deadDiceD = myrisk.getGame().getMaxDefendDice();
            }

            // if we need input
            if (max != 0) {
                // selecting the number of attacking dice
                if (canRetreat) {
                    g.drawImage( Battle.getSubimage(481, 0, 21, 21) , 120, 180, this );

                    if (noda > 1) {
                        g.drawImage( Battle.getSubimage(481, 0, 21, 21) , 120, 211, this );
                    }
                    else if (max > 1) {
                        g.drawImage( Battle.getSubimage(502, 0, 21, 21) , 120, 211, this );
                    }

                    if (noda > 2) {
                        g.drawImage( Battle.getSubimage(481, 0, 21, 21) , 120, 242, this );
                    }
                    else if (max > 2) {
                        g.drawImage( Battle.getSubimage(502, 0, 21, 21) , 120, 242, this );
                    }

                    // draw the dead dice
                    g.drawImage( Battle.getSubimage(502, 21, 21, 21) , 339, 180, this );
                    if (deadDiceD > 1) {
                        g.drawImage( Battle.getSubimage(502, 21, 21, 21) , 339, 211, this );
                    }
                    if (deadDiceD > 2) {
                        g.drawImage( Battle.getSubimage(502, 21, 21, 21) , 339, 242, this );
                    }
                }
                // selecting the number of dice to defend
                else {
                    g.drawImage( Battle.getSubimage(481, 21, 21, 21) , 339, 180, this );

                    if (nodd > 1) {
                        g.drawImage( Battle.getSubimage(481, 21, 21, 21) , 339, 211, this );
                    }
                    else if (max > 1) {
                        g.drawImage( Battle.getSubimage(502, 21, 21, 21) , 339, 211, this );
                    }

                    if (nodd > 2) {
                        g.drawImage( Battle.getSubimage(481, 21, 21, 21) , 339, 242, this );
                    }
                    else if (max > 2) {
                        g.drawImage( Battle.getSubimage(502, 21, 21, 21) , 339, 242, this );
                    }
                }
            }
            // battle open and waiting for the attacker to select there number of dice
            else if (att == null && def == null && !spinD) {
                    BufferedImage deadD = Battle.getSubimage(502, 21, 21, 21);

                    // draw the dead dice
                    g.drawImage( deadD , 339, 180, this );
                    if (deadDiceD > 1) {
                        g.drawImage( deadD , 339, 211, this );
                    }
                    if (deadDiceD > 2) {
                        g.drawImage( deadD , 339, 242, this );
                    }

                    if (!spinA) {
                            BufferedImage deadA = Battle.getSubimage(502, 0, 21, 21);

                            // draw dead dice for attacker
                            int deadDiceA = myrisk.hasArmiesInt(c1num)-1;
                            // we assume that the attacker can attack with max of 3 dice

                            g.drawImage( deadA , 120, 180, this );
                            if (deadDiceA > 1) {
                                    g.drawImage( deadA , 120, 211, this );
                            }
                            if (deadDiceA > 2) {
                                    g.drawImage( deadA , 120, 242, this );
                            }
                    }
            }
        }

        public void drawDiceAnimated(Graphics g) {
            if (spinA) {
                    g.drawImage( attackerSpins[ r.nextInt( 6 ) ] , 116, 176, this);

                    if (noda > 1) {
                            g.drawImage( attackerSpins[ r.nextInt( 6 ) ] , 116, 207, this);
                    }
                    if (noda > 2) {
                            g.drawImage( attackerSpins[ r.nextInt( 6 ) ] , 116, 238, this);
                    }
                    //g.drawString("ROLLING ATTACKER " + noda +"    " + Math.random() , 50, 100);

                    if (spinD) {
                            g.drawImage( defenderSpins[ r.nextInt( 6 ) ] , 335, 176, this);

                            if (nodd > 1) {
                                    g.drawImage( defenderSpins[ r.nextInt( 6 ) ] , 335, 207, this);
                            }
                            if (nodd > 2) {
                                    g.drawImage( defenderSpins[ r.nextInt( 6 ) ] , 335, 238, this);
                            }
                            //g.drawString("ROLLING DEFENDER " + nodd +"    " + Math.random(), 300, 100);
                    }
            }
	}

        private void drawDiceResults(Graphics g2) {

            // just in case in the middle of the draw the att and def get set to null
            int[] atti=att;
            int[] defi=def;

            if (atti != null && defi != null ) {

                    if (defi[0] >= atti[0]) {
                            g2.setColor( Color.blue );

                            int xCoords[] = {339, 339, 140};
                            int yCoords[] = {180, 200, 190};

                            g2.fillPolygon(xCoords, yCoords, xCoords.length);
                    }
                    else {
                            g2.setColor( Color.red );

                            int xCoords[] = {140, 140, 339};
                            int yCoords[] = {180, 200, 190};

                            g2.fillPolygon(xCoords, yCoords, xCoords.length);
                    }

                    if (atti.length > 1 && defi.length > 1) {

                            if (defi[1] >= atti[1]) {

                                    g2.setColor( Color.blue );

                                    int xCoords[] = {339, 339, 140};
                                    int yCoords[] = {211, 231, 221};

                                    g2.fillPolygon(xCoords, yCoords, xCoords.length);
                            }
                            else {

                                    g2.setColor( Color.red );

                                    int xCoords[] = {140, 140, 339};
                                    int yCoords[] = {211, 231, 221};

                                    g2.fillPolygon(xCoords, yCoords, xCoords.length);
                            }
                    }

                    if (atti.length > 2 && defi.length > 2) {

                        if (defi[2] >= atti[2]) {
                                g2.setColor( Color.blue );

                                int xCoords[] = {339, 339, 140};
                                int yCoords[] = {242, 262, 252};

                                g2.fillPolygon(xCoords, yCoords, xCoords.length);
                        }
                        else {
                                g2.setColor( Color.red );

                                int xCoords[] = {140, 140, 339};
                                int yCoords[] = {242, 262, 252};

                                g2.fillPolygon(xCoords, yCoords, xCoords.length);
                        }
                    }


                    // draw attacker dice
                    drawDice(true, atti[0] , 120, 180, g2 );

                    if (atti.length > 1) {
                            drawDice(true, atti[1] , 120, 211, g2 );
                    }
                    if (atti.length > 2) {
                            drawDice(true, atti[2] , 120, 242, g2 );
                    }

                    // draw defender dice
                    drawDice(false, defi[0] , 339, 180, g2 );

                    if (defi.length > 1) {
                            drawDice(false, defi[1] , 339, 211, g2 );
                    }

                    if (defi.length > 2) {
                            drawDice(false, defi[2] , 339, 242, g2 );
                    }
            }
        }

	/**
	 * Gets the dice
	 * @param isAttacker if the dice is an attacker
	 * @param result Result of the dice
	 * @return BufferedImage The image of the die
	 */
	public void drawDice(boolean isAttacker, int result,final int dx,final int dy,Graphics g) {

		//BufferedImage die = new BufferedImage(21, 21, java.awt.image.BufferedImage.TYPE_INT_RGB );
                g.translate(dx, dy);
		//Graphics2D g = die.createGraphics();

		if (isAttacker) {
			g.drawImage( Battle.getSubimage(481, 0, 21, 21) , 0, 0, this );
		}
		else {
			g.drawImage( Battle.getSubimage(481, 21, 21, 21) , 0, 0, this );
		}

		int size=3;
		//g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor( new Color(255, 255, 255, 200) );

		if (result==0) {

			g.fillOval(9, 9, size, size);
		}
		else if (result==1) {

			g.fillOval(3, 3, size, size);
			g.fillOval(15, 15, size, size);
		}
		else if (result==2) {

			g.fillOval(3, 3, size, size);
			g.fillOval(9, 9, size, size);
			g.fillOval(15, 15, size, size);
		}
		else if (result==3) {

			g.fillOval(3, 3, size, size);
			g.fillOval(15, 3, size, size);
			g.fillOval(15, 15, size, size);
			g.fillOval(3, 15, size, size);
		}
		else if (result==4) {

			g.fillOval(3, 3, size, size);
			g.fillOval(15, 3, size, size);
			g.fillOval(15, 15, size, size);
			g.fillOval(3, 15, size, size);
			g.fillOval(9, 9, size, size);
		}
		else if (result==5) {

			g.fillOval(3, 3, size, size);
			g.fillOval(15, 3, size, size);
			g.fillOval(15, 15, size, size);
			g.fillOval(3, 15, size, size);
			g.fillOval(9, 3, size, size);
			g.fillOval(9, 15, size, size);
		}

		g.translate(-dx, -dy);
	}

	/**
	 * Checks where the mouse was clicked
	 * @param x x co-ordinate
	 * @param y y co-ordinate
	 * @return int type of button
	 */
	public int insideButton(int x, int y) {
                int ax=120;
                int dx=339;
		int W=21;
		int H=21;

		if (x >= ax && x < (ax + W) && y >= 180 && y < (180 + H)) {
			return 1;
		}
		else if (x >= ax && x < (ax + W) && y >= 211 && y < (211 + H)) {
			return 2;
		}
		else if (x >= ax && x < (ax + W) && y >= 242 && y < (242 + H)) {
			return 3;
		}
		else if (x >= dx && x < (dx + W) && y >= 180 && y < (180 + H)) {
			return 4;
		}
		else if (x >= dx && x < (dx + W) && y >= 211 && y < (211 + H)) {
			return 5;
		}
                else if (x >= dx && x < (dx + W) && y >= 232 && y < (232 + H)) {
                        return 6;
                }

		return 0;
	}

	/**
	 * Works out where the mouse was clicked
	 * @param e A mouse event
	 */
	public void mouseClicked(MouseEvent e) {
		int click=insideButton(e.getX(),e.getY());
		if (max != 0) {
                    if (canRetreat) {
			if (click == 1) { noda=1; }
			if (click == 2 && max > 1) { noda=2; }
			if (click == 3 && max > 2) { noda=3; }
                    }
                    else {
			if (click == 4) { nodd=1; }
			if (click == 5 && max > 1) { nodd=2; }
			if (click == 6 && max > 2) { nodd=3; }
                    }
                    battle.repaint();
		}
	}

	public void mouseEntered(MouseEvent e) { }

	public void mouseExited(MouseEvent e) { }

	public void mousePressed(MouseEvent e) { }

	public void mouseReleased(MouseEvent e) { }

}
