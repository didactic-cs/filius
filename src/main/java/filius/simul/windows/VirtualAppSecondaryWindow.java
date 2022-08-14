/*
 ** This file is part of Filius, a network construction and simulation software.
 ** 
 ** Originally created at the University of Siegen, Institute "Didactics of
 ** Informatics and E-Learning" by a students' project group:
 **     members (2006-2007): 
 **         André Asschoff, Johannes Bade, Carsten Dittich, Thomas Gerding,
 **         Nadja Haßler, Ernst Johannes Klebert, Michell Weyer
 **     supervisors:
 **         Stefan Freischlad (maintainer until 2009), Peer Stechert
 ** Project is maintained since 2010 by Christian Eibl <filius@c.fameibl.de>
 **         and Stefan Freischlad
 ** Filius is free software: you can redistribute it and/or modify
 ** it under the terms of the GNU General Public License as published by
 ** the Free Software Foundation, either version 2 of the License, or
 ** (at your option) version 3.
 ** 
 ** Filius is distributed in the hope that it will be useful,
 ** but WITHOUT ANY WARRANTY; without even the implied
 ** warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 ** PURPOSE. See the GNU General Public License for more details.
 ** 
 ** You should have received a copy of the GNU General Public License
 ** along with Filius.  If not, see <http://www.gnu.org/licenses/>.
 */
package filius.gui.simulationmode.windows;

import java.awt.Dimension;

import javax.swing.JInternalFrame;

import filius.gui.JExtendedInternalFrame;
import filius.gui.simulationmode.GUIDesktopPanel;

/**
 * This class should not be instantiated directly.<br>
 * Instead, use its descendants: GUIAppNonModalWindow and GUIAppModalWindow.<br>
 * <br> 
 * GUIAppMainWindow can own      GUIAppNonModalWindow and GUIAppModalWindow<br>
 * GUIAppNonModalWindow can own  GUIAppNonModalWindow and GUIAppModalWindow<br>
 * GUIAppModalWindow can own     GUIAppModalWindow
 
 */
@SuppressWarnings("serial")
public class GUIAppSecondaryWindow extends JExtendedInternalFrame {
	
	private JInternalFrame ownerWindow = null;
	private GUIAppMainWindow mainWindow = null;
	private GUIDesktopPanel desktop = null;
	
	
	public GUIAppSecondaryWindow(JInternalFrame ownerWindow, String title, int width, int height) {
		
		super();
	
		this.ownerWindow = ownerWindow;  // << Should never be null!
		getMainWindow();
		
		setSize(width, height);
		setMinimumSize(new Dimension(320, 240));
		setResizable(true);
		
		setMaximizable(true);
		setIconifiable(false);				

		setClosable(true);
		setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);

		setTitle(title);
		
		if (mainWindow != null) {
			setFrameIcon(mainWindow.getIcon());
			desktop = mainWindow.getDesktop();
			desktop.addFrame(this);
		}
		
		initComponents();
	}
	
	public JInternalFrame getOwnerWindow() {
		
		return ownerWindow;
	}
	
	public GUIAppMainWindow getMainWindow() {
		
		if (mainWindow == null) {
			if (ownerWindow == null)  return null;  // << Should never happen
			// The owner window can be:
			// 1. the main window
			if (ownerWindow instanceof GUIAppMainWindow)  mainWindow = (GUIAppMainWindow) ownerWindow;
			// 2. a non modal or modal secondary window
			else if (ownerWindow instanceof GUIAppSecondaryWindow)  mainWindow =  ((GUIAppSecondaryWindow) ownerWindow).getMainWindow();

			else  return null;   // << Should never happen either
		}
		
		return  mainWindow;
	}
	
	public void centerInDesktop() {
		
		if (desktop == null)  return;
		
		int taskbarHeight = 22;
		
    	setLocation((desktop.getWidth() - getWidth())/2, (desktop.getHeight() - taskbarHeight - getHeight())/2);
    }
	
	public void centerInOwner() {
		
		if (ownerWindow == null)  return;

    	// 640x458: dimension of the desktop (without the taskbar)
    	setLocation((ownerWindow.getWidth() - getWidth())/2, (ownerWindow.getHeight() - getHeight())/2);
    }
	
	// To be overridden
	protected void initComponents() {}
}
