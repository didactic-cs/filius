package filius.gui.modes.simulation.desktop;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import filius.auxiliary.Information;
import filius.gui.modes.simulation.windows.GUIAppMainWindow;
import filius.software.Application;

@SuppressWarnings("serial")
public class JStartMenu extends JPopupMenu {
	
	private ActionListener menuItemClick;
	
	public JStartMenu() {		
		
		initMenuListener();
		initMenu();
	}

	private void initMenu() {

//		ImageIcon folderIcon = new ImageIcon(getClass().getResource("/gfx/desktop/explorer_closedDirectory.png"));		
//		
//		// Fill the start menu with the apps
//		for (Entry<String, Application> entry : hostOS.getInstalledApps().entrySet()) {
//
//			HashMap<String, String> ai = Information.getInstance().getAppInfo(entry.getKey());
//			if (ai == null) continue;
//
//			String category = ai.get(Information.APP_CATEGORY);
//			String appName = ai.get(Information.APP_NAME);
//			String appIconFilename = minify(ai.get(Information.APP_ICON_FILENAME));
//
//			JMenuItem menuItem = new JMenuItem(appName);
//			menuItem.setIcon(new ImageIcon(getClass().getResource(appIconFilename)));
//			menuItem.addActionListener(startMenuItemClick);
//
//			if (category.equals("-")) add(menuItem);
//		}     
//
//		// Fill the start menu with the apps
//		for (Entry<String, Application> entry : hostOS.getInstalledApps().entrySet()) {
//
//			HashMap<String, String> ai = Information.getInstance().getAppInfo(entry.getKey());
//            if (ai == null) continue;
//            	
//            String category = ai.get(Information.APP_CATEGORY);
//            String appName = ai.get(Information.APP_NAME);
//            String appIconFilename = minify(ai.get(Information.APP_ICON_FILENAME));
//            
//            JMenuItem menuItem = new JMenuItem(appName);
//            menuItem.setIcon(new ImageIcon(getClass().getResource(appIconFilename)));
//			menuItem.addActionListener(startMenuItemClick);
//	        			
//            if (!category.equals("-")) {       
//            	// Look for a submenu matching the given category name
//            	JMenu me = null;
//            	for (int i = getComponentCount() - 1; i >= 0; i--) {
//            		if (((JMenuItem) getComponent(i)).getText().equals(category)) {
//            			me = (JMenu) getComponent(i);
//            			break;
//            		}
//            	}
//            	if (me == null) me = new JMenu(category);
//            	me.setIcon(folderIcon);
//            	me.add(menuItem);
//            	add(me);
//            }
//        }
    }
	
	private void initMenuListener() {

//		menuItemClick = new ActionListener() {
//
//			public void actionPerformed(ActionEvent e) {
//				String appName = ((JMenuItem) e.getSource()).getText();
//
//				GUIAppMainWindow appWindow = getRunningAppFromName(appName);				
//				if (appWindow == null) return;      		
//
//				appWindow.start(null, null);
//				appWindow.updateUI();
//				appWindow.show();
//
//				addAppButtonToTaskbar(appWindow.getAppName(), appWindow.getFrameIcon());
//			}    				
//		};
	}
}