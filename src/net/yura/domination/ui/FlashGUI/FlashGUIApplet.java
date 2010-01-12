// Yura Mamyrin

package risk.ui.FlashGUI;

import risk.engine.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;
import javax.swing.colorchooser.*;
import javax.swing.filechooser.*;
import javax.accessibility.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import java.io.*;
import java.applet.*;
import java.net.*;

/**
 * @author Yura Mamyrin
 */

public class FlashGUIApplet extends JApplet {

	public void init() {

		//Risk.applet=this;
/*
		// set up system Look&Feel
		try {

			String os = System.getProperty("os.name");
			String jv = System.getProperty("java.version");

			if ( jv.startsWith("1.4.2") && os != null && os.startsWith("Linux")) {
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
			}
			else {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
*/
		String lang = getParameter("lang");
		if (lang !=null) {

			risk.engine.translation.TranslationBundle.parseArgs( new String[] {"--lang="+lang } );

		}

        	setContentPane( new MainMenu( new Risk(this), (Frame)SwingUtilities.getAncestorOfClass(Frame.class, this) ) );

	}

}
