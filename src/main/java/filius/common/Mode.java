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
package filius.gui.common;

import javax.swing.JFrame;
import javax.swing.ToolTipManager;

import filius.gui.dialogs.JHelpFrame;
import filius.gui.modes.design.GUICableItem;
import filius.gui.modes.design.GUINodeItem;
import filius.gui.modes.design.config.JConfigRouter;
import filius.system.net.layer1.link.SwitchSpanningTree;
import filius.system.software.SystemSoftware;

public class GUIModeSelector {
	
	private static GUIModeSelector instance = null;
	private int currentMode = GUIContainer.DESIGN_MODE;
    private GUIContainer container;
    private SwitchSpanningTree switchSpanningTree = new SwitchSpanningTree();
    

    public GUIModeSelector() {
        
    	container = GUIContainer.getInstance();
    }
    
    public static GUIModeSelector getInstance() {
    	
        if (instance == null) {
        	instance = new GUIModeSelector();
        }
        return instance;
    }
    
    private void stopSimulation() {
        for (GUINodeItem nodeItem : container.getNodeItems()) {
            SystemSoftware system;
            system = nodeItem.getNode().getSystemSoftware();
            try {
                system.stop();
            } catch (Exception e) {}
        }
        ((JFrame) container.getPacketsViewerDialog()).setVisible(false);
    }
	
	// set/reset cable highlight, i.e., make all cables normal coloured for
    // simulation and possibly highlight in development view
    private void resetCableHighlighting(int mode) {
        
        if (mode == GUIContainer.SIMULATION_MODE) {
        	// change to simulation view: unhighlight all cables
            for (GUICableItem cableItem : container.getCableList()) {
                if (cableItem.getCable() != null) cableItem.getCable().setActive(false);
            }
            GUIEvents.getInstance().unselectCable();
        } else {
        	// change to development view: possibly highlight a cable 
            // (only for 'Router' configuration)
        	for (GUICableItem cableItem : container.getCableList()) {
                if (cableItem.getCable() != null) cableItem.getCable().setBlocked(false);
            }
            if (container.getConfigPanel() instanceof JConfigRouter) {
                ((JConfigRouter) container.getConfigPanel()).highlightCable();
            }
            if (mode == GUIContainer.DOC_MODE) GUIEvents.getInstance().unselectCable();
        }
    }
    
    public synchronized void select(int mode) {

        if (mode == GUIContainer.DESIGN_MODE) {
        	
        	
        	stopSimulation();
        	
        	container.setCurrentMode(GUIContainer.DESIGN_MODE);
        	
            resetCableHighlighting(mode); // unhighlight cables   
            
            JHelpFrame.getInstance().loadModeMainPage(GUIContainer.DESIGN_MODE);
            
            GUIEvents.getInstance().unfreezeSelectedElement();   
            
            ToolTipManager.sharedInstance().setDismissDelay(3000);
            
        } else if (mode == GUIContainer.DOC_MODE) {
        	
            
            stopSimulation();
            
            container.setCurrentMode(GUIContainer.DOC_MODE);
        	
            resetCableHighlighting(mode); // unhighlight cables

            GUIEvents.getInstance().freezeSelectedElement();

            JHelpFrame.getInstance().loadModeMainPage(GUIContainer.DOC_MODE);
            
            ToolTipManager.sharedInstance().setDismissDelay(3000);
            
        } else if (mode == GUIContainer.SIMULATION_MODE && currentMode != GUIContainer.SIMULATION_MODE) {
        	
        	container.setCurrentMode(GUIContainer.SIMULATION_MODE);
        	
            resetCableHighlighting(mode); // unhighlight cables

            GUIEvents.getInstance().freezeSelectedElement(); 
            
            JHelpFrame.getInstance().loadModeMainPage(GUIContainer.SIMULATION_MODE);
            
            // Tooltips remain visible longer
            ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
                        
            switchSpanningTree.apply(container.getNodeItems());

            for (GUINodeItem nodeItem : container.getNodeItems()) {
                SystemSoftware system;
                system = nodeItem.getNode().getSystemSoftware();
                system.start();
            }
        }
        currentMode = mode;
    }   
    
    public int getCurrentMode() {
    	
    	return currentMode;
    }
}
