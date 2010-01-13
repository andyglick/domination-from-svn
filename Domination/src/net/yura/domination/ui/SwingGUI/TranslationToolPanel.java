package net.yura.domination.ui.SwingGUI;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import net.yura.domination.engine.RiskUtil;
import net.yura.translation.MessageTool;
import net.yura.translation.Mtcomm;
import net.yura.translation.plugins.PropertiesComm;

public class TranslationToolPanel extends MessageTool implements SwingGUITab {

    private Mtcomm mycomm1;

    public void load() {

        if (!RiskUtil.checkForNoSandbox()) {
                RiskUtil.showAppletWarning(null);
                return;
        }

        if (mycomm1==null) {
            mycomm1 = new PropertiesComm() {
                public void setupFilter(JFileChooser fc) {
                    super.setupFilter(fc);

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

                    fc.setFileFilter( ff );

                }
            };
        }
        load(mycomm1);
    }

}

