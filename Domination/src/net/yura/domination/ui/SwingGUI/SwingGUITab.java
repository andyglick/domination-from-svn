package risk.ui.SwingGUI;

import javax.swing.JToolBar;
import javax.swing.JMenu;

public interface SwingGUITab {

    public JToolBar getToolBar();
    public JMenu getMenu();
    public String getName();
}
