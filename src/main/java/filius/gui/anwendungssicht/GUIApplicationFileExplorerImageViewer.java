package filius.gui.anwendungssicht;

import java.awt.Dimension;

import javax.swing.JInternalFrame;
import javax.swing.tree.DefaultMutableTreeNode;

@SuppressWarnings("serial")
public class GUIApplicationFileExplorerImageViewer extends JInternalFrame {
	
	private static GUIApplicationFileExplorerImageViewer singleton = null;	
	private GUIDesktopPanel desktop = null;  //JBackgroundDesktopPane
	
	public GUIApplicationFileExplorerImageViewer() {
        super();
        this.setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);

        this.setPreferredSize(new Dimension(400, 300));
        this.setClosable(true);
        this.setMaximizable(true);
        this.setIconifiable(false);
        this.setResizable(true);
        
        this.setTitle("Preview");
    }

    /*
	 * returns the instance of the singleton
	 */
	public static GUIApplicationFileExplorerImageViewer gI() {
		if (singleton == null) {
			singleton = new GUIApplicationFileExplorerImageViewer();
		}
		return singleton;
	}
	
	public void preview(JInternalFrame launcher, DefaultMutableTreeNode node) {
//		if (desktop == null) {
//			desktop = (GUIDesktopPanel) launcher.getParent().getParent();
//			desktop.add(this);
//		}
//		this.show();
	}

}
