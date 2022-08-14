package filius.gui.modes.simulation.desktop;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

@SuppressWarnings("serial")
public class JStartMenuItem extends JMenuItem {
	
	public JStartMenuItem(ApplicationManager appManager, String appClass, String appName, String appIcon) {
		
		super(appName);
		setIcon(new ImageIcon(getClass().getResource(appIcon)));
		
		addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) { appManager.launchApp(appClass); }    				
		});
	}	
}