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
package filius.common;

import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.event.MouseInputAdapter;

import filius.auxiliary.I18n;
import filius.design.hardware.Hardware;
import filius.design.nodes.JNode;

@SuppressWarnings("serial")
public class JNodesSidebar extends JSidebar implements I18n {
	
	private GUIContainer container;
	
	
	public JNodesSidebar(GUIContainer container) {
		
		super();		
		this.container = container;
		initListeners();
	}
		
	// The behaviour of the button of the connecting tool is different from that of the nodes
    private JLabel createCableTool() {
		
		JLabel label = new JLabel(new ImageIcon(getClass().getResource("/" + Hardware.ICONS[0])));
		label.setText(Hardware.NAMES[0]);
		label.setVerticalTextPosition(SwingConstants.BOTTOM);
		label.setHorizontalTextPosition(SwingConstants.CENTER);
		label.setAlignmentX(0.5f);
		label.setToolTipText(messages.getString("hardware_msg0"));  // Cable (C) / Kabel (K)		           

		label.addMouseListener(new MouseInputAdapter() {
			
			public void mousePressed(MouseEvent e) {
				GUIEvents.getInstance().resetAndShowConnectingTool();
			}
		});
		
		return label;
    }
	
	@Override
	protected void addItems() {
		
		// Add the cable tool on top
		addItem(createCableTool());		
		
		// Add the node buttons
		for (int type = Hardware.COMPUTER; type <= Hardware.MODEM; type++) {
			addItem(new JNode(type, false));
		}
	}
	
    public void initListeners() {    

        // Begin and end the creation of a new node by dragging a specific node from the left sidebar 
        addMouseListener(new MouseInputAdapter() {
        	
            public void mousePressed(MouseEvent e) {

                JNode button = findButtonAt(e.getX(), e.getY() + getVerticalScrollBar().getValue());
                JNode node = container.getNewNode();
                if (button != null) {
                	//container.updateNewNode(button.type, e.getX() + container.getXOffset() - getWidth(), e.getY() + container.getYOffset());
                	node.updateType(button.type);
                	int x = e.getX() + container.getXOffset() - getWidth();
                	int y = e.getY() + container.getYOffset();
                	node.setBounds(x, y, node.getWidth(), node.getHeight());
                	node.setVisible(true);
                    
                    // The exact location depends on the size of the newNode which is only               < This needs to be clarified!
                    // determined in the lines above. Hence the call to setLocation now. 
                	node.setLocation(node.getX() - node.getWidth()/2, node.getY() - node.getHeight()/2); 
                	
                    GUIEvents.getInstance().resetAndHideConnectingTool();                    
                }
            }

            public void mouseReleased(MouseEvent e) {
            	
            	JNode node = container.getNewNode();
            	
            	int nodeW = node.getWidth();
            	int x = e.getX() - getWidth() - nodeW / 2; 
            	int waW = container.getWorkAreaSP().getViewport().getSize().width;
            	
            	int nodeH = node.getHeight();
                int y = e.getY() - nodeH / 2;
                int waH = container.getWorkAreaSP().getViewport().getSize().height + 22; // < To be emproved: should not overflow the bottom of the whole workArea
                
                if (node.isVisible()  &&  x >= 0  &&  x + nodeW < waW  &&  y >= 0  &&  y + nodeH < waH) {
                	// Create a new node
                    container.addNode(node.type, x, y);
                }
                node.setVisible(false);
            }
        });

        // Update the position of the dragged node
        addMouseMotionListener(new MouseInputAdapter() {
        	
            public void mouseDragged(MouseEvent e) {
            	
            	JNode node = container.getNewNode();
                if (node.isVisible()) {
                	node.setLocation(e.getX() + container.getXOffset() - node.getWidth()/2 - getWidth(),
                                     e.getY() + container.getYOffset() - node.getHeight()/2); 
                }
            }
        });    
    }
}
