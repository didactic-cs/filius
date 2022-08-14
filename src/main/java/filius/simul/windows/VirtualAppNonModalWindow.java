package filius.gui.simulationmode;

import java.awt.Dimension;

import javax.swing.JInternalFrame;

@SuppressWarnings("serial")
public class GUIApplicationSecondaryWindow extends JInternalFrame {
	
	public GUIApplicationSecondaryWindow(GUIApplicationWindow mainWindow, String title, int width, int height) {
		
		super();
		
		setSize(width, height);
		setMinimumSize(new Dimension(320, 240));
		setResizable(true);
		centerWindow();
		
		setClosable(true);
		setMaximizable(true);
		setIconifiable(false);				

		setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);

		setTitle(title);
		
		setFrameIcon(mainWindow.getIcon());
		
		mainWindow.getDesktop().addFrame(this);
		
		initComponents();
	}
	
	private void centerWindow() {

    	// 640x458: dimension of the desktop (without the taskbar)
    	setLocation((640 - getWidth())/2, (480 - 22 - getHeight())/2);
    }
	
	private void initComponents() {		
	}

}
