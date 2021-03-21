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

import java.awt.Dimension;
import java.util.List;

import javax.swing.ImageIcon;

import filius.gui.GUIContainer;
import filius.gui.GUIEvents;
import filius.gui.GUIMainMenu;
import filius.hardware.knoten.Computer;
import filius.hardware.knoten.InternetNode;
import filius.hardware.knoten.Modem;
import filius.hardware.knoten.Node;
import filius.hardware.knoten.Notebook;
import filius.hardware.knoten.Router;
import filius.hardware.knoten.Switch;

@SuppressWarnings("serial")
public class GUIDesignPanel extends GUIMainArea {

    public GUIDesignPanel(int width, int height) {
    	setOpaque(false);
        setLayout(null);
        setPreferredSize(new Dimension(width, height));        
        setBounds(0, 0, width, height);
    }

    public void updateViewport(List<GUINodeItem> nodeItems, List<GUICableItem> cableItems) {
    	
        removeAll();

        for (GUINodeItem nodeItem : nodeItems) {
        	
            Node node = nodeItem.getNode();
            JNodeLabel nodeLabel = nodeItem.getNodeLabel();                

            // In design mode, one node may be selected
            nodeLabel.setSelected(GUIContainer.getInstance().getCurrentMode() == GUIMainMenu.DESIGN_MODE &&
            		              nodeItem == GUIEvents.getInstance().getSelectedItem());
            
            // When the text is changed, the location is recomputed so that the icon does not move 
            // (which otherwise happens when the text is wider than the icon)
            nodeLabel.setText2(node.getDisplayName());
            
            nodeLabel.setType(node.getHardwareType());
            if (node instanceof InternetNode) {
                nodeLabel.updateTooltip();
            }
            
            if (node instanceof Switch) {
            	String iconPath;
                if (((Switch) node).isCloud()) iconPath = GUIDesignSidebar.SWITCH_CLOUD;
                else                           iconPath = GUIDesignSidebar.SWITCH;
                nodeLabel.setIcon(new ImageIcon(getClass().getResource("/" + iconPath)));
                
            } else if (node instanceof Router) {
                nodeLabel.setIcon(new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.ROUTER)));
                
            } else if (node instanceof Computer) {
                nodeLabel.setIcon(new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.RECHNER)));
                
            } else if (node instanceof Notebook) {
                nodeLabel.setIcon(new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.NOTEBOOK)));
                
            } else if (node instanceof Modem) {
                nodeLabel.setIcon(new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.MODEM)));
            }

            nodeLabel.setBounds(nodeItem.getNodeLabel().getBounds());
            add(nodeLabel);
        }

        for (GUICableItem item : cableItems) {
        	
            add(item.getCablePanel());
        }
    }
}
