// Yura Mamyrin, Group D

package risk.ui.FlashGUI;

import risk.engine.*;

import java.awt.event.WindowEvent;
import javax.swing.JDialog;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.event.MouseEvent;
import java.awt.Graphics;
import javax.swing.JPanel;
import java.awt.Toolkit;
import java.awt.Dimension;
import javax.swing.JLayeredPane;
import java.awt.event.MouseListener;
import javax.swing.JTextField;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.awt.font.TextLayout;
import java.awt.font.FontRenderContext;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Color;

import java.text.AttributedString;
import java.awt.font.LineBreakMeasurer;
import java.text.AttributedCharacterIterator;
import java.awt.font.TextAttribute;
import java.awt.RenderingHints;

/**
 * <p> Mission Dialog for FlashGUI </p>
 * @author Yura Mamyrin
 */

public class MissionDialog extends JDialog implements MouseListener {

    private BufferedImage mission;
    private String text;

    /**
     * Creates a mission dialog
     * @param parent Frame
     * @param modal boolean
     * @param r Risk parser
     */
    public MissionDialog(Frame parent, boolean modal, Risk r) {

        super(parent, modal);

	text=r.getCurrentMission();

	mission = RiskUtil.getUIImage(this.getClass(),"mission.jpg");

        initGUI();

	setResizable(false);

        pack();

    }

    /** This method is called from within the constructor to initialize the form. */

    /**
     * Initialises the GUI
     */
    private void initGUI() {

        // set title
        setTitle("");

	Dimension d = new Dimension(150, 230);

	missionPanel missionpanel = new missionPanel();
	missionpanel.setPreferredSize(d);
	missionpanel.setMinimumSize(d);
	missionpanel.setMaximumSize(d);
	missionpanel.addMouseListener(this);

	getContentPane().add(missionpanel);

        addWindowListener(
            new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent evt) {
                    exitForm();
                }
            }
	);

    }

    /** Exit the Application */

    /**
     * Closes the GUI
     * @param evt Close button was pressed
     */
    private void exitForm() {

	setVisible(false);
	dispose();

    }

    class missionPanel extends JPanel {

    /**
     * Paints graphic
     * @param g Graphics
     */
	public void paintComponent(Graphics g) {

	    g.drawImage(mission, 0, 0, this);

	    Graphics2D g2 = (Graphics2D)g;

	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	    int cardWidth = 150;

	    Font font = new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 11);

	    AttributedString as = new AttributedString(text);
	    as.addAttribute(TextAttribute.FONT, font);

	    AttributedCharacterIterator aci = as.getIterator();
	    FontRenderContext frc = g2.getFontRenderContext();

            LineBreakMeasurer lbm = new LineBreakMeasurer(aci, frc);

	    g2.setColor( Color.blue );
	    TextLayout tl = new TextLayout(aci, frc);

            float y = 70;

	    lbm.setPosition( 0 );

	    while (lbm.getPosition() < text.length()) {
		tl = lbm.nextLayout(cardWidth - 50);
		tl.draw(g2, (float)(cardWidth/2-tl.getBounds().getWidth()/2), y += tl.getAscent());
		y += tl.getDescent() + tl.getLeading();
	    }


	}

    }

	//**********************************************************************
	//                     MouseListener Interface
	//**********************************************************************

	public void mouseClicked(MouseEvent e) {

	    exitForm();

	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}
}
