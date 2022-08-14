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
package filius.gui.netzwerksicht;

import javax.swing.ImageIcon;
import filius.Main;
import filius.gui.GUIContainer;
import filius.gui.GUIEvents;
import filius.hardware.knoten.Computer;
import filius.hardware.knoten.Modem;
import filius.hardware.knoten.Node;
import filius.hardware.knoten.Notebook;
import filius.hardware.knoten.Router;
import filius.hardware.knoten.Switch;

public class GUINodeItem {

	private Node node = null; 
    private JNodeLabel nodeLabel = null;       
    
    /**
     * <b>GUINodeItem</b> creates a new nodeItem<br>
     * Used when a project is loaded or for the connecting tool
     * 
     */
    public GUINodeItem() {    	
    }
    
    /**
     * <b>GUINodeItem</b> creates a new nodeItem along with its associated JNodeLabel and Node
     * 
     * @param nodeType String
     * @param x int horizontal position of the associated JNodeLabel
     * @param y int vertical position of the associated JNodeLabel
     */
    public GUINodeItem(String nodeType, int x, int y) {
    	
    	String resourceName = "";    	
    	
    	// Create the Node
    	
    	if (nodeType.equals(Switch.TYPE)) {

    		node = new Switch();
    		resourceName = GUIDesignSidebar.SWITCH;

    	} else if (nodeType.equals(Computer.TYPE)) {

    		node = new Computer();
    		resourceName = GUIDesignSidebar.RECHNER;

    	} else if (nodeType.equals(Notebook.TYPE)) {

    		node = new Notebook();
    		resourceName = GUIDesignSidebar.NOTEBOOK;

    	} else if (nodeType.equals(Router.TYPE)) {

    		node = new Router();
    		resourceName = GUIDesignSidebar.ROUTER;
    		int portCount = GUIContainer.getInstance().getRouterPortCount();
    		((Router) node).createNICList(portCount);    

    	} else if (nodeType.equals(Modem.TYPE)) {

    		node = new Modem();
    		resourceName = GUIDesignSidebar.MODEM;    		

    	} else {
    		Main.debug.println("ERROR (" + this.hashCode() + "): " + "unbekannter Hardwaretyp " + nodeType +
    				           " konnte nicht erzeugt werden.");
    		return;
    	}    	
    	 
    	ImageIcon icon;
    	try {
    		icon = new ImageIcon(getClass().getResource("/" + resourceName));  
    	} catch (Exception e) {
    		node = null;
    		return;
    	}   	
    	
    	setNode(node);
    	
    	// Create the JNodeLabel
    	
    	nodeLabel = new JNodeLabel(nodeType, "", icon);  
    	// It is important here that setLocation be called before setText2
    	// (If not, depending on the length of its display name, the nodeLabel might be shifted to the right) 
        nodeLabel.setLocation(x, y);    
        nodeLabel.setText2(node.getDisplayName());        	
    	          
    	setNodeLabel(nodeLabel);
    	
    	// Select the just created node
    	nodeLabel.setSelected(true);
    	GUIEvents.getInstance().setSelectedItem(this);
    	
    	// In case that the config panel is open, update its display to this node
    	GUIContainer.getInstance().setConfigPanel(this);    
    }    
    
    public JNodeLabel getNodeLabel() {
        return nodeLabel;
    }

    public void setNodeLabel(JNodeLabel nodeLabel) {
    	
        this.nodeLabel = nodeLabel;    
        nodeLabel.setNodeItem(this);
    }

    public Node getNode() {
        return node;
    }    
    
    public void setNode(Node node) {    	
   	
    	this.node = node;
    	node.setNodeItem(this);    	
    }
}
