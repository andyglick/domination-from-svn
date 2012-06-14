package net.yura.domination.mobile.flashgui;

import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.core.Card;
import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.mobile.PicturePanel;
import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.DesktopPane;
import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.KeyEvent;
import net.yura.mobile.gui.components.Button;
import net.yura.mobile.gui.components.Component;
import net.yura.mobile.gui.components.Frame;
import net.yura.mobile.gui.components.Label;
import net.yura.mobile.gui.components.Panel;
import net.yura.mobile.gui.components.ScrollPane;
import net.yura.mobile.gui.layout.BoxLayout;
import net.yura.mobile.gui.layout.XULLoader;
import net.yura.mobile.util.Properties;

/**
 * @author Yura
 */
public class CardsDialog extends Frame implements ActionListener {

	private Risk myrisk;
	private PicturePanel pp;


        //private Vector cards;
	private Panel myCardsPanel;
	private Panel TradePanel;


	private Image Infantry;
	private Image Cavalry;
	private Image Artillery;
	private Image Wildcard;

	private Button tradeButton;
	private boolean canTrade;

	private Properties resb = GameActivity.resb;

	/**
	 * Creates a new CardsDialog
	 * @param parent decides the parent of the frame
	 * @param modal
	 * @param r the risk main program
	 */

	public CardsDialog(Risk r, PicturePanel p) {
		myrisk = r;
		pp=p;

                Image cards;
                try {
                    cards = Image.createImage("/cards.png");
                }
                catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
                        
                int w = cards.getWidth()/4;
                int h = cards.getHeight();

		
		Cavalry = Image.createImage(cards, 0, 0, w, h, 0);
		Infantry = Image.createImage(cards, w, 0, w, h, 0);
		Artillery = Image.createImage(cards, w*2, 0, w, h, 0);
                Wildcard = Image.createImage(cards, w*3, 0, w, h, 0);

                
                XULLoader loader;
                try {
                    loader = XULLoader.load( getClass().getResourceAsStream("/cards.xml") , this, resb);
                }
                catch(Exception ex) {
                    throw new RuntimeException(ex);
                }
                
                setContentPane( (Panel)loader.getRoot() );
                
		setTitle(resb.getProperty("cards.title"));
		setMaximum(true);

                
                // labels
                ((Label)loader.find("NumArmies")).setText( getNumArmies() );


		myCardsPanel = (Panel) ((ScrollPane)loader.find("myCardsPanel")).getView();
		myCardsPanel.setLayout( new BoxLayout( Graphics.HCENTER ) );



		TradePanel = (Panel) ((ScrollPane)loader.find("TradePanel")).getView();
		TradePanel.setLayout( new BoxLayout( Graphics.HCENTER ) );

		tradeButton = (Button)loader.find("tradeButton");



	}

    public void actionPerformed(String actionCommand) {
        if ("done".equals(actionCommand)) {
            setVisible(false);
        }
        else if ("trade".equals(actionCommand)) {
            
            Vector<CardPanel> cards2 = TradePanel.getComponents();

            if (cards2.size()==3) {

                myrisk.parser("trade "+((CardPanel)cards2.elementAt(0)).getCardName() + " " + ((CardPanel)cards2.elementAt(1)).getCardName() + " " + ((CardPanel)cards2.elementAt(2)).getCardName() );

                TradePanel.removeAll();

                TradePanel.validate();

                tradeButton.setFocusable(false);

                repaint();

            }
            
        }
        else {
            throw new RuntimeException("unknown command "+actionCommand);
        }
    }
        
        

	public void setup(boolean ct) {
		canTrade=ct;

		myCardsPanel.removeAll();

		TradePanel.removeAll();

		Vector cards = myrisk.getCurrentCards();
		for (int c=0; c < cards.size(); c++) {
			Panel cp = new CardPanel( (Card)cards.elementAt(c) );
			myCardsPanel.add(cp);
		}

                tradeButton.setFocusable(false);

	}

	public String getNumArmies() {
            // return resb.getString("cards.nexttrade").replaceAll( "\\{0\\}", "" + resb.getString("cards.fixed"));
            if(myrisk.getGame().getCardMode()==RiskGame.CARD_FIXED_SET) {
		 return resb.getString("cards.fixed");
            }
            else if(myrisk.getGame().getCardMode()==RiskGame.CARD_ITALIANLIKE_SET) {
		 return resb.getString("cards.italianlike");
            }
            else {
		 return resb.getString("cards.nexttrade").replaceAll( "\\{0\\}", "" + myrisk.getNewCardState());
            }
	}

	class CardPanel extends Panel {

		private Card card;

		/**
		 * Constructor of for the panel
		 * @param c The card
		 */
		public CardPanel (Card c) {
			card=c;


			int cardWidth=100;
			int cardHeight=170;


			this.setPreferredSize( cardWidth, cardHeight );


		}

		/**
		 * Paints the panel
		 * @param g The graphics
		 */
                @Override
		public void paintComponent(Graphics2D g2) {


                        g2.setColor( 0xAA000000 );
			g2.fillRoundRect(0, 0, getWidth(), getHeight() ,5,5);
                        g2.setColor( 0xFF000000 );
                        g2.drawRoundRect(5, 5, getWidth()-10, getHeight()-10 ,5,5);
                        
			if (!(card.getName().equals(Card.WILDCARD))) {

				String text = ((Country)card.getCountry()).getName(); // Display

                                int a = card.getCountry().getColor();

				Image i = pp.getCountryImage(a, false);
                                
                                if ( myrisk.isOwnedCurrentPlayerInt( a ) ) {
                                    // TODO apply color filter to make it the color of the player
                                }
                                
				g2.drawImage( i , 25+ (25-(i.getWidth()/2)) ,35+ (25-(i.getHeight()/2)) );

				if (card.getName().equals("Infantry")) {
					g2.drawImage( Infantry ,25 ,90  );
				}
				else if (card.getName().equals("Cavalry")) {
					g2.drawImage( Cavalry ,25 ,90  );
				}
				else if (card.getName().equals("Cannon")) {
					g2.drawImage( Artillery ,15 ,105  );
				}

			}
			else {
				g2.drawImage( Wildcard ,25 ,10 );
			}

		}

		/**
		 * Gets the card name
		 * @return String The card name
		 */
		public String getCardName() {
			if (card.getName().equals( Card.WILDCARD )) {
                            return card.getName();
                        }
                        else {
                            return ((Country)card.getCountry()).getColor()+"";
			}
		}

		//**********************************************************************
		//                     MouseListener Interface
		//**********************************************************************

		/**
		 * Works out what has been clicked
		 * @param e A mouse event
		 */
                
            public void processMouseEvent(int type, int x, int y, KeyEvent keys) {

                if (type == DesktopPane.RELEASED) {

                            Vector<CardPanel> trades = TradePanel.getComponents();
                    
                            if ( this.getParent() == myCardsPanel ) {
                                    if (TradePanel.getComponentCount() < 3) {
                                        myCardsPanel.remove(this); TradePanel.add(this);
                                    }
                                    if (TradePanel.getComponentCount() == 3 && canTrade && myrisk.canTrade( ((CardPanel)trades.elementAt(0)).getCardName() , ((CardPanel)trades.elementAt(1)).getCardName(), ((CardPanel)trades.elementAt(2)).getCardName() ) ) {
                                        tradeButton.setFocusable(true);
                                    }
                            }
                            else if ( this.getParent() == TradePanel ) {
                                    TradePanel.remove(this);
                                    myCardsPanel.add(this);
                                    tradeButton.setFocusable(false);
                            }

                            myCardsPanel.getParent().revalidate();
                            TradePanel.revalidate();

                            myCardsPanel.getParent().repaint();
                            TradePanel.repaint();

                }
            }
        }
    
}
