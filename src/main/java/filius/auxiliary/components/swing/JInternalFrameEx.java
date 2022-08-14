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
package filius.gui;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.beans.PropertyVetoException;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

/**
 * <b>JExtendedInternalFrame</b> provides a way to make a JInternalFrame
 * look modal relative to another one, which is seen as its parent.<br>
 * It is used by the class FiliusFileNodeChooser.<br>
 * {@link filius.gui.simulationmode.FiliusFileNodeChooser}
 */
@SuppressWarnings("serial")
public class JExtendedInternalFrame extends JInternalFrame {
	
	private JInternalFrame modalParent = null;
	private boolean parentIsClosable;
	private boolean parentIsMaximizable;
	private boolean parentIsIconifiable;
	private boolean parentIsResizable;
	private Component prevGlassPane;
	private JDesktopPane glassPane = null;
	private InternalFrameAdapter ifa = null;

	
	public void setModalParent(JInternalFrame parent) {
		
		if (parent != null) {
			
			modalParent = parent;
			
			// Retrieve the icon from the parent frame
	    	setFrameIcon(modalParent.getFrameIcon());
			
			// The modal parent must no longer be activable
			
			// Put a glasspane in front of the modal parent
			glassPane = new JDesktopPane();
			glassPane.setOpaque(false);
			glassPane.addMouseListener(new MouseAdapter(){});
			prevGlassPane = modalParent.getGlassPane();
			modalParent.setGlassPane(glassPane);
			glassPane.setVisible(true);
			
			// Remove the titlebar icons
			parentIsClosable = modalParent.isClosable();			
			modalParent.setClosable(false);			
			parentIsMaximizable = modalParent.isMaximizable();
			modalParent.setMaximizable(false);
			parentIsIconifiable = modalParent.isIconifiable();
			modalParent.setIconifiable(false);
			parentIsResizable = modalParent.isResizable();
			modalParent.setResizable(false);
			
            // Prevent the modal parent from being displayed on top of this frame 
            ifa = new InternalFrameAdapter() {
				
				public void internalFrameActivated(InternalFrameEvent e) {
					try {
						// Move this frame on top
			    		setSelected(true);
			    		requestFocusInWindow();
			    	} catch (PropertyVetoException ve) {}	
				}
			};			
			modalParent.addInternalFrameListener(ifa);
			
		} else {
			// The modal parent becomes activable again	
			
			modalParent.removeInternalFrameListener(ifa);
		
			modalParent.setClosable(parentIsClosable);			
			modalParent.setMaximizable(parentIsMaximizable);
			modalParent.setIconifiable(parentIsIconifiable);
			modalParent.setResizable(parentIsResizable);
			
			modalParent.setGlassPane(prevGlassPane);
			glassPane = null;		
			
			try {
				// Give the focus back to the parent
				modalParent.setSelected(true);
				modalParent.requestFocusInWindow();
	    	} catch (PropertyVetoException ve) {}	
			
			modalParent = null;
		}		
	}
}
