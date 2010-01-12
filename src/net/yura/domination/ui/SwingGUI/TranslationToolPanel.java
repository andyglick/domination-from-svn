package net.yura.domination.ui.SwingGUI;

import java.io.File;
import java.io.FileFilter;
import net.yura.domination.engine.RiskUtil;
import net.yura.translation.MessageTool;
import net.yura.translation.Mtcomm;
import net.yura.translation.plugins.PropertiesComm;

public class TranslationToolPanel extends MessageTool implements SwingGUITab {



    public void riskLoad() {


                        if (!RiskUtil.checkForNoSandbox()) {
                                RiskUtil.showAppletWarning(null);
                                return;
                        }

			Mtcomm mycomm1 = new PropertiesComm();

                        load(mycomm1);
    }



    {






/*
	    javax.swing.filechooser.FileFilter fileFilter = new javax.swing.filechooser.FileFilter() {
		public boolean accept(File file) {
		    //return (file.isDirectory());
		    return ( file.isFile() );
		}
		public String getDescription() {
		    //return "All Directories";
		    return "Risk File";
		}
	    };

	    fc.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
	    fc.setFileFilter(fileFilter);
	    fc.setAcceptAllFileFilterUsed(false);
	    fc.setDialogTitle("Select Image Directory");
*/



	    FileFilter ff = new FileFilter() {

		public boolean accept(File f) {

			return (f.isDirectory() || ( f.getName().equals("Risk.properties") || f.getName().equals("DefaultMaps.properties") || f.getName().equals("DefaultCards.properties") ) );

		}
		public String getDescription() {

			return "Game Translation Files (Risk.properties,DefaultMaps...,DefaultCards...)";
		}
	    };



	    fc.addChoosableFileFilter( ff );



	    fc.addChoosableFileFilter( new FileFilter() {

		public boolean accept(File f) {

			return (f.isDirectory() || "game.ini".equals(f.getName()) || "settings.ini".equals(f.getName()) );

		}
		public String getDescription() {

			return "Game Settings File (game.ini,settings.ini)";
		}
	    } );



	    fc.addChoosableFileFilter( new risk.engine.guishared.RiskFileFilter( risk.engine.guishared.RiskFileFilter.RISK_PROPERTIES_FILES ) );


	    fc.setFileFilter( ff );


    }

}

