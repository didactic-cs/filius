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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import filius.Main;
import filius.gui.netzwerksicht.GUICableItem;
import filius.gui.netzwerksicht.GUINodeItem;
import filius.gui.netzwerksicht.JCablePanel;
import filius.gui.netzwerksicht.JNodeLabel;
import filius.hardware.Cable;
import filius.hardware.NetworkInterface;
import filius.hardware.Port;
import filius.hardware.knoten.Computer;
import filius.hardware.knoten.InternetNode;
import filius.hardware.knoten.Modem;
import filius.hardware.knoten.Node;
import filius.hardware.knoten.Notebook;
import filius.hardware.knoten.Router;
import filius.hardware.knoten.Switch;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.ProjectManager;
import filius.software.system.SwitchFirmware;

public class GUIEvents implements I18n {
	
	private static GUIEvents guiEvents;   
	private GUIContainer container;
	
	// Clicked items
	private enum MouseTarget {nothing, cable, node, marquee};
	private MouseTarget mouseTarget = MouseTarget.nothing;

	// Marquee
    private int tempMarqueeX1, tempMarqueeY1;
    private int tempMarqueeX2, tempMarqueeY2;    
    private int marqueeStartDragX, marqueeStartDragY;
    private List<GUINodeItem> marqueeNodeItemList;
    
    // Selected node or cable    
    private GUICableItem selectedCableItem;
    private GUINodeItem selectedNodeItem = null;  
    private int dragRelativeX, dragRelativeY;
    
    // Selected node or cable freeze
    private GUINodeItem frozenSelectedItem; 
    private GUICableItem frozenSelectedCable;       
    private boolean selectionIsFrozen = false;
    
    // Connection
    private GUICableItem newCableItem;
    private JCablePanel newCablePanel;   
    

    private GUIEvents() {
        marqueeNodeItemList = new LinkedList<GUINodeItem>();
        container = GUIContainer.getInstance();
    }

    public static GUIEvents getInstance() {
    	
        if (guiEvents == null) {
            guiEvents = new GUIEvents();
        }
        return guiEvents;
    }
    
    //-----------------------------------------------------------------------------------
    //  Mouse events (for the DESIGN mode)
    //-----------------------------------------------------------------------------------   
    
    public void mousePressed(MouseEvent e) {
        
        //ProjectManager.getInstance().setModified();    // <<<  To be fixed: the configuration panels must notify their changes themselves!
        
        // Determine what the user clicked on: the marquee, a single node 
    	// outside of the marquee, a cable, or an empty area
        getMouseTarget(e.getX(), e.getY());
        
        // Left button        
        if (e.getButton() == MouseEvent.BUTTON1) {

        	// A node is selected
        	if (mouseTarget == MouseTarget.node) {

        		// If the connecting tool is currently selected, we try to connect nodes
        		if (container.getConnectingTool().isVisible()) {

        			// Hide the configuration panel 
        			container.getConfigPanel().minimize();
        			
        			if (!selectedNodeItem.getNode().hasFreePort()) {
        				// All ports are already connected        	
        				GUIErrorHandler.getGUIErrorHandler().DisplayError(messages.getString("guievents_msg1"));
        				return;
        			}             			
        
        			if (newCableItem == null) {  
        				// A new cable is required     				
        				newCableItem = new GUICableItem();   
        				newCablePanel = newCableItem.getCablePanel();         
        			} else {
        				// The first extremity of the cable is already plugged
        				GUINodeItem nodeItem = newCablePanel.getNodeItem1();
            			if (nodeItem != null) {        	
            				if (selectedNodeItem.getNode().isConnectedTo(nodeItem.getNode())) {
            					// nodeItems are already connected to each other        					
            					GUIErrorHandler.getGUIErrorHandler().DisplayError(messages.getString("guievents_msg12"));
            					return;
            				}
            			}
        			}    
        			
        			// Do connect the cable to the node
        			plugCable(e.getX(), e.getY());       

        			// 
        		} else {
        			// Update property panel
        			container.setConfigPanel(selectedNodeItem);
        			if (e.getClickCount() == 2) container.getConfigPanel().maximize();

        			// Show selection frame
        			JNodeLabel selectedNodeLabel = selectedNodeItem.getNodeLabel();
        			selectedNodeLabel.setSelected(true);

        			// Coordinates of the mouse relative to the top left corner of the nodeLabel
        			dragRelativeX = e.getX() - selectedNodeLabel.getX();                           
        			dragRelativeY = e.getY() - selectedNodeLabel.getY();    
        		}
        			
        	} else if (mouseTarget == MouseTarget.cable) {
        		
        		// A cable was clicked on
        		container.setConfigPanel(selectedCableItem);
        		if (e.getClickCount() == 2) container.getConfigPanel().maximize();
        		
        	} else if (mouseTarget == MouseTarget.nothing) {

        		// The click was done on an empty area 
        		container.getTempMarquee().setVisible(false);   
        		unselectCable();
        		container.getConfigPanel().minimize();
        		container.setConfigPanel(null);        		
        	}
        }       
        
        // Right button
        else if (e.getButton() == MouseEvent.BUTTON3) {
        	
        	// Release the connecting tool if selected
        	if (container.getConnectingTool().isVisible()) {
                resetAndHideCablePreview();
                return;
            }  
        	
        	if (mouseTarget == MouseTarget.marquee) {      
            	// Show the context menu            
                designModeContextMenu(e.getX(), e.getY());   
                
        	} else if (mouseTarget == MouseTarget.node) {      
            	// Update the configuration panel and show the context menu
            	container.setConfigPanel(selectedNodeItem);              
                designModeContextMenu(e.getX(), e.getY());           
                 
            } else if (mouseTarget == MouseTarget.cable) {   
            	// Update the configuration panel and show the context menu
            	container.setConfigPanel(selectedCableItem);            	          	
            	cableContextMenu(selectedCableItem, e.getX(), e.getY());               
            } 
        }
    }
    
    public void mouseDragged(MouseEvent e) {    
    	
    	// Only drag with left mouse button
        if (!SwingUtilities.isLeftMouseButton(e)) return; 
    	
        // Do not allow dragging while cable connector is visible (i.e. during cable assignment)
        if (container.getConnectingTool().isVisible()) return;          
        
        boolean dragPreviewVisible = container.getDragNode().isVisible();

        if (!container.isMarqueeVisible()) {
            if (selectedNodeItem != null && !dragPreviewVisible) {
            	
            	JNodeLabel selectedNodeLabel = selectedNodeItem.getNodeLabel();
            	
            	// Drag a single nodeLabel
            	int newX = e.getX() - dragRelativeX;
            	if (newX < 0) {
            		newX = 0;
            	} else {
            		int maxX = container.getWidth() - selectedNodeLabel.getWidth();
            		if (newX >= maxX) {
            			newX = maxX - 1;
            	    }            		
            	}            	
            	int newY = e.getY() - dragRelativeY;
            	if (newY < 0) {
            		newY = 0;
            	} else {
            		int maxY = container.getHeight() - selectedNodeLabel.getHeight();
            		if (newY >= maxY) {
            			newY = maxY - 1;
            	    }            		
            	}
            	
                selectedNodeLabel.setLocation(newX, newY);
                container.updateCables();                
                ProjectManager.getInstance().setModified();
               
            } else {
            	// Marquee definition 
                JMarqueePanel tempMarquee = container.getTempMarquee();
                
                // Start marquee definition
                if (!tempMarquee.isVisible()) {
                    tempMarqueeX1 = e.getX();
                    tempMarqueeY1 = e.getY();
                    tempMarqueeX2 = tempMarqueeX1;
                    tempMarqueeY2 = tempMarqueeY1;

                    tempMarquee.setBounds(tempMarqueeX1, tempMarqueeY1, 0, 0);
                    tempMarquee.setVisible(true);
                    
                // Extend marquee definition    
                } else {
                    tempMarqueeX2 = e.getX();
                    tempMarqueeY2 = e.getY();

                    if (tempMarqueeX1 < tempMarqueeX2) {
                    	if (tempMarqueeY1 < tempMarqueeY2) {
                    		tempMarquee.setBounds(tempMarqueeX1, tempMarqueeY1, tempMarqueeX2 - tempMarqueeX1, tempMarqueeY2 - tempMarqueeY1);
                    	} else {
                    		tempMarquee.setBounds(tempMarqueeX1, tempMarqueeY2, tempMarqueeX2 - tempMarqueeX1, tempMarqueeY1 - tempMarqueeY2);
                    	}

                    } else {
                    	if (tempMarqueeY1 < tempMarqueeY2) {
                    		tempMarquee.setBounds(tempMarqueeX2, tempMarqueeY1, tempMarqueeX1 - tempMarqueeX2, tempMarqueeY2 - tempMarqueeY1);
                    	} else {
                    		tempMarquee.setBounds(tempMarqueeX2, tempMarqueeY2, tempMarqueeX1 - tempMarqueeX2, tempMarqueeY1 - tempMarqueeY2);
                    	}
                    }
                }
            }
        }
        
        else if (!dragPreviewVisible) {
            // Drag all selected items in marquee
            if ((mouseTarget == MouseTarget.marquee) &&  
               (0 <= e.getX()) && (e.getX() <= container.getWidth()) && 
               (0 <= e.getY()) && (e.getY() <= container.getHeight())) {

                int Xshift = marqueeStartDragX - e.getX();
                marqueeStartDragX = e.getX();
                int Yshift = marqueeStartDragY - e.getY();
                marqueeStartDragY = e.getY();

                container.moveMarquee(-Xshift, -Yshift, marqueeNodeItemList);
                ProjectManager.getInstance().setModified();
                
            } else {
                Main.debug.println("Out of Boundaries!");
            }
        }
    }
    
    public void mouseReleased() {        
        
        JMarqueePanel tempMarquee = container.getTempMarquee();

        // Transform the temporary marquee into a definitive marquee
        // Build the list of nodeItems inside the temporary marquee 
        if (tempMarquee.isVisible()) {
            int minX = Integer.MAX_VALUE; 
            int minY = Integer.MAX_VALUE;
            int maxX = 0; 
            int maxY = 0;  
            
            tempMarquee.setVisible(false);
            
            marqueeNodeItemList = new LinkedList<GUINodeItem>();
            List<GUINodeItem> nodeItems = container.getNodeItems();
            for (GUINodeItem nodeItem : nodeItems) {
                int nodeX = nodeItem.getNodeLabel().getX();                
                int nodeY = nodeItem.getNodeLabel().getY();
                int nodeW = nodeItem.getNodeLabel().getWidth();
                int nodeH = nodeItem.getNodeLabel().getHeight();

                int nodeCenterX = nodeX + nodeW / 2;
                int nodeCenterY = nodeY + nodeH / 2;

                // Add the node to the list if its center is in the temporary marquee
                if ((tempMarquee.getX() <= nodeCenterX) && (nodeCenterX <= tempMarquee.getX() + tempMarquee.getWidth()) &&
                    (tempMarquee.getY() <= nodeCenterY) && (nodeCenterY <= tempMarquee.getY() + tempMarquee.getHeight())) {
                	
                	marqueeNodeItemList.add(nodeItem);
                	
                	// Compute the dimension of the definitive marquee
                    minX = Math.min(nodeX, minX);
                    maxX = Math.max(nodeX + nodeW, maxX);
                    minY = Math.min(nodeY, minY);
                    maxY = Math.max(nodeY + nodeH, maxY);
                }
            }
            
            // If the marqueeList is not empty, create the definitive marquee
            // except when there is only one nodeItem in the list, in which case
            // the nodeItem is selected directly
            if (!marqueeNodeItemList.isEmpty()) {
            	if (marqueeNodeItemList.size() > 1) {
            		JMarqueePanel marquee = container.getMarquee();
            		marquee.setBounds(minX, minY, maxX - minX, maxY - minY);
            		marquee.setVisible(true);
            	} else {
            		// if there is only one element in the marqueeList, just select it directly
            		selectedNodeItem = marqueeNodeItemList.get(0);            		
            		JNodeLabel nodeLabel = selectedNodeItem.getNodeLabel();                   
            		nodeLabel.setSelected(true);      
            		marqueeNodeItemList = null;
            		nodeLabel.updateUI();
            	}            	
            }
        }
    }
    
    //-----------------------------------------------------------------------------------
    //  Selection and marquee
    //-----------------------------------------------------------------------------------      

    /**
     * <b>getMouseTarget</b> determines what the mouse is over.
     * 
     * @param posX
     * @param posY
     */
    private void getMouseTarget(int posX, int posY) {
    	
    	// The mouse was pressed on the marquee?
    	if (container.getMarquee().isVisible() && container.getMarquee().inBounds(posX, posY)) {    		
        	marqueeStartDragX = posX;
        	marqueeStartDragY = posY;
        	
        	mouseTarget = MouseTarget.marquee;
        	return;
           
        } else {         
        	// The marquee, if any, is lost
        	container.getMarquee().setVisible(false);
    		
    		// The mouse was pressed on a node?
    		for (GUINodeItem nodeItem : container.getNodeItems()) {
    			
    			JNodeLabel nodeLabel = nodeItem.getNodeLabel();
    			if (nodeLabel.inBounds(posX, posY)) {
    				if (nodeItem == selectedNodeItem) {
    					mouseTarget = MouseTarget.node;
    					return;
    				}
    				
    				unselectNode();
    				unselectCable(); 
    				
    				selectedNodeItem = nodeItem;
    				nodeLabel.setSelected(true);
        			nodeLabel.updateUI();
        			mouseTarget = MouseTarget.node;
    				return;
    			}
    		}  
    		// The selected node, if any, is deselected
    		unselectNode();
    		
    		// The mouse was pressed on a cable?
    		for (GUICableItem cableItem : container.getCableList()) {

    			JCablePanel cablePanel = cableItem.getCablePanel();
    			if (cablePanel.clicked(posX, posY)) {
    				if (cableItem == selectedCableItem) {
    					mouseTarget = MouseTarget.cable;
    					return;
    				}

    				unselectCable();   

    				cablePanel.setSelected(true);
    				selectedCableItem = cableItem;
    				mouseTarget = MouseTarget.cable;
    				return;
    			}
    		}
    		// The selected cable, if any, is deselected
    		unselectCable();   
  		 
    		// The mouse was pressed on an empty area
    		mouseTarget = MouseTarget.nothing;
        }
    }    
    
    // Unselect the selected cable if any
    protected void unselectCable() {     
    	
    	if (selectedCableItem != null) {
    		selectedCableItem.getCablePanel().setSelected(false);
    		selectedCableItem = null;
    	}
    } 
    
    // Remove the selected cable if any
    protected void removeSelectedCable() {     
    	
    	if (selectedCableItem != null) {
    		container.removeCableItem(selectedCableItem);
    		selectedCableItem = null;
    		container.getConfigPanel().minimize();
    		container.setConfigPanel(null);
    		ProjectManager.getInstance().setModified();  
    	}
    }
    
    // Remove the selected node if any
    protected void removeSelectedNode() {     
    	
    	if (selectedNodeItem != null) {
    		container.removeItem(selectedNodeItem);
    		selectedNodeItem = null;
    		container.getConfigPanel().minimize();
    		container.setConfigPanel(null);
    		ProjectManager.getInstance().setModified();  
    	}
    }
    
    public void removeSelectedItem() {
        
    	if (selectedNodeItem != null) {
    		removeSelectedNode();
    	} else if (selectedCableItem != null) {
    		removeSelectedCable();
    	}        
    } 
    
    // 
    public GUINodeItem getSelectedItem() {
        return selectedNodeItem;
    }
    
    // Called when creating a new node
    public void setSelectedItem(GUINodeItem item) {
        selectedNodeItem = item;
    }
    
    // Unselect the selected node if any
    protected void unselectNode() {     
    	
    	if (selectedNodeItem != null) {
    		selectedNodeItem.getNodeLabel().setSelected(false);
    		selectedNodeItem.getNodeLabel().updateUI();
    		selectedNodeItem = null;
    	}
    }
    
    // Keep track of which element is selected
    // when leaving the design mode
    public void freezeSelectedElement() {
    	
    	if (! selectionIsFrozen) {
    		// Freeze both, even though only one is really selected
    		frozenSelectedItem = selectedNodeItem;
        	frozenSelectedCable = selectedCableItem; 
        	
        	selectionIsFrozen = true;
    	}    	
    }
    
    // Restore the selected element when
    // returning to the design mode
    public void unfreezeSelectedElement() {
    	
    	if (selectionIsFrozen) {
    		if (frozenSelectedItem != null) {
    			selectedNodeItem = frozenSelectedItem;
    			selectedNodeItem.getNodeLabel().setSelected(true);    			
    		} else if (frozenSelectedCable != null) {
    			selectedCableItem = frozenSelectedCable;
    			selectedCableItem.getCablePanel().setSelected(true);
    		}        	
        	selectionIsFrozen = false;
    	}  
    }
    
    //-----------------------------------------------------------------------------------
    //  Connecting a cable
    //-----------------------------------------------------------------------------------

    public void plugCable(int X, int Y) {
    	
        if (newCablePanel.getNodeItem1() == null) {
        	
        	// Plug the first extremity of the cable
        	plugCableIntoFirstNode(X, Y);

        } else if (newCablePanel.getNodeItem1() != selectedNodeItem) {

        	// Plug the second extremity of the cable
        	plugCableIntoSecondNode(X, Y);

        	ProjectManager.getInstance().setModified();  
        }
    }

    private void plugCableIntoFirstNode(int X, int Y) {
    	
    	// Plug the new cable into the selected (first) node
    	newCablePanel.setNodeItem1(selectedNodeItem);           	       	            
        
    	// Attach the other end to the handle node
        newCablePanel.setNodeItem2(container.getHandleEndNodeItem()); 
                
        // Add the cable to the design panel
        container.getDesignPanel().add(newCablePanel);           
        newCablePanel.setVisible(true);  
        
        // Set it at the cable being drawn
        container.setCurrentDesignCable(newCablePanel, X, Y);   
        
        // Select connecting tool icon 2
        container.setConnectingToolIcon2();
    }

    private void plugCableIntoSecondNode(int X, int Y) {
       
        Port port1 = null;
        Port port2 = null;

        // Attach the other end to the selected (second) node
        newCablePanel.setNodeItem2(selectedNodeItem);
        
        newCablePanel.updateBounds();        
        container.getCableList().add(newCableItem);
        container.getDesignPanel().updateUI();
        
        // Get the end ports
        
        Node node = newCablePanel.getNodeItem1().getNode();
        if (node instanceof Modem) {
            Modem vrOut = (Modem) node;
            port1 = vrOut.getPort();
            
        } else if (node instanceof Router) {
            Router r = (Router) node;
            port1 = r.getFreePort();
            
        } else if (node instanceof Switch) {
            Switch sw = (Switch) node;
            port1 = ((SwitchFirmware) sw.getSystemSoftware()).getNode().getFreePort();
            
        } else if (node instanceof InternetNode) {
        	NetworkInterface nic1 = (NetworkInterface) ((InternetNode) node).getNICList().get(0);
            port1 = nic1.getPort();
        }

        node = newCablePanel.getNodeItem2().getNode();
        if (node instanceof Modem) {
            Modem vrOut = (Modem) node;
            port2 = vrOut.getPort();
            
        } else if (node instanceof Router) {
            Router r = (Router) node;
            port2 = r.getFreePort();
            
        } else if (node instanceof Switch) {
            Switch sw = (Switch) node;
            port2 = ((SwitchFirmware) sw.getSystemSoftware()).getNode().getFreePort();
            
        } else if (node instanceof InternetNode) {
        	NetworkInterface nic2 = (NetworkInterface) ((InternetNode) node).getNICList().get(0);
            port2 = nic2.getPort();
        }

        // Create the cable between the ports
        newCableItem.setCable(new Cable(port1, port2));
        
        // Reset the newCable for another connection
        resetAndShowCablePreview(X, Y);
    }

    private void resetCableTool() {    	
    	
        newCableItem = null;
        newCablePanel = null;
        container.setConnectingToolIcon1();
        container.setCurrentDesignCable(null, 0, 0); 
    }
    
    public void resetAndShowCablePreview(int currentPosX, int currentPosY) {
    	
        resetCableTool();

        container.getConnectingTool().setLocation(currentPosX, currentPosY);
        container.getConnectingTool().setVisible(true);
        
        container.getMarquee().setVisible(false);
    }    
    
    public void resetAndHideCablePreview() {    	
    	
    	if (newCablePanel != null) {
    		// Don't forget to remove the no longer used cable from the design panel
    		container.getDesignPanel().remove(newCablePanel);  
    		container.getDesignPanel().updateUI();
    	}    	
        resetCableTool();
        
        container.getConnectingTool().setVisible(false);
    }
    
    //-----------------------------------------------------------------------------------
    //  Removing items
    //-----------------------------------------------------------------------------------
    
    /**
     * Remove all nodes under the marquee
     */
    public void removeMarqueeNodes() {
    	
    	for (GUINodeItem nodeItem : marqueeNodeItemList) container.removeItem(nodeItem);    
    	marqueeNodeItemList = null;
    	container.getMarquee().setVisible(false);
    	ProjectManager.getInstance().setModified();  
    }
    
    //-----------------------------------------------------------------------------------
    //  Context menus
    //-----------------------------------------------------------------------------------

    /**
     * @author Johannes Bade & Thomas Gerding
     * 
     *         Bei rechter Maustaste auf ein Item (bei Laufendem Entwurfsmodus) wird ein Kontextmenü angezeigt, in dem
     *         z.B. das Item gelöscht, kopiert oder ausgeschnitten werden kann.
     * 
     * @param nodeLabel
     *            Item auf dem das Kontextmenü erscheint
     * @param e
     *            MouseEvent (Für Position d. Kontextmenü u.a.)
     */
    private void designModeContextMenu(int posX, int posY) {
    	
    	if (container.isMarqueeVisible()) {
    		
    		final JMenuItem pmDeleteNodes = new JMenuItem(messages.getString("guievents_msg7"));
            pmDeleteNodes.setActionCommand("delete");

            JPopupMenu popmen = new JPopupMenu();
            ActionListener al = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                	removeMarqueeNodes(); 
                }
            };

            pmDeleteNodes.addActionListener(al);
            popmen.add(pmDeleteNodes);

            container.getDesignPanel().add(popmen);
            popmen.setVisible(true);
            popmen.show(container.getDesignPanel(), posX, posY);
    		
    	} else if (selectedNodeItem != null) {
    		
        	String removeCableString;
            if (selectedNodeItem.getNode() instanceof Computer || selectedNodeItem.getNode() instanceof Notebook) {
                removeCableString = messages.getString("guievents_msg5");
            } else {
                removeCableString = messages.getString("guievents_msg6");
            }

            final JMenuItem pmShowConfig = new JMenuItem(messages.getString("guievents_msg11"));
            pmShowConfig.setActionCommand("showconfig");
            
            final JMenuItem pmDeleteCable = new JMenuItem(removeCableString);
            pmDeleteCable.setActionCommand("deletecable");
            
            final JMenuItem pmDeleteNode = new JMenuItem(messages.getString("guievents_msg7"));
            pmDeleteNode.setActionCommand("deletenode");

            JPopupMenu popmen = new JPopupMenu();
            ActionListener al = new ActionListener() {
                public void actionPerformed(ActionEvent e) {

                    if (e.getActionCommand() == pmDeleteNode.getActionCommand()) {
                        removeSelectedNode();
                        
                    } else if (e.getActionCommand() == pmDeleteCable.getActionCommand()) {
                    	container.removeCablesConnectedTo(selectedNodeItem);
                        
                    } else if (e.getActionCommand() == pmShowConfig.getActionCommand()) {
                    	container.setConfigPanel(selectedNodeItem);
                    	container.getConfigPanel().maximize();
                    }
                }
            };

            pmDeleteNode.addActionListener(al);
            pmDeleteCable.addActionListener(al);
            pmShowConfig.addActionListener(al);

            popmen.add(pmShowConfig);
            popmen.add(pmDeleteCable);
            popmen.add(pmDeleteNode);

            container.getDesignPanel().add(popmen);
            popmen.setVisible(true);
            popmen.show(container.getDesignPanel(), posX, posY);
        }
    }

    /**
     * Context menu in case of clicking on single cable item
	 * Used to delete a single cable
     */
    private void cableContextMenu(final GUICableItem cableItem, int posX, int posY) {
    	
        final JMenuItem pmRemoveCable = new JMenuItem(messages.getString("guievents_msg5"));
        pmRemoveCable.setActionCommand("removecable");

        JPopupMenu popmen = new JPopupMenu();
        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand() == pmRemoveCable.getActionCommand()) {
                	removeSelectedCable();                    
                }
            }
        };

        pmRemoveCable.addActionListener(al);
        popmen.add(pmRemoveCable);

        container.getDesignPanel().add(popmen);
        popmen.setVisible(true);
        popmen.show(container.getDesignPanel(), posX, posY);
    }
    
    /**
     * Context menu in case of clicking on the workarea in action mode
	 * Used to hide all windows
     */
    public void actionModeContextMenu(int posX, int posY) {
    	
    	final JMenuItem pmMoveToBottom = new JMenuItem(messages.getString("guiactionmenu_msg1"));
    	//pmMoveToBottom.setActionCommand("movetobottom");

    	final JMenuItem pmCloseAllFrames = new JMenuItem(messages.getString("guiactionmenu_msg2"));
    	//pmCloseAllFrames.setActionCommand("removecable");

        JPopupMenu popmen = new JPopupMenu();
        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand() == pmMoveToBottom.getActionCommand()) {
                	JFrameList.getInstance().putMasterInTheBackground();                  
                }
                else if (e.getActionCommand() == pmCloseAllFrames.getActionCommand()) {
                	JFrameList.getInstance().closeAll();                   
                }
            }
        };
        
        pmMoveToBottom.addActionListener(al);
        popmen.add(pmMoveToBottom);

        pmCloseAllFrames.addActionListener(al);
        popmen.add(pmCloseAllFrames);

        container.getDesignPanel().add(popmen);
        popmen.setVisible(true);
        popmen.show(container.getDesignPanel(), posX, posY);
    }
    
    /**
     * @author Johannes Bade & Thomas Gerding
     * 
     *         Bei rechter Maustaste auf ein Item (bei Laufendem Aktionsmodus) wird ein Kontextmenü angezeigt, in dem
     *         z.B. der Desktop angezeigt werden kann.
     * 
     * @param templabel
     *            Item auf dem das Kontextmenü erscheint
     * @param e
     *            MouseEvent (Für Position d. Kontextmenü u.a.)
     */
    public void actionModeNodeContextMenu(final GUINodeItem nodeItem, int posX, int posY) {
    	
        if (nodeItem != null) {
        	
        	if (nodeItem.getNode() instanceof Switch) {

            	Switch sw = (Switch) nodeItem.getNode();
            	
                JPopupMenu menu = new JPopupMenu();

                ActionListener al = new ActionListener() {
                	
                    public void actionPerformed(ActionEvent e) {
                    	
                        if (e.getActionCommand().equals("satanzeigen")) {
                        	container.showDesktop(nodeItem);
                        	SatViewerControl.getInstance().addOrShowViewer(sw);
                        }
                    }
                };       
                                            
                JMenuItem pmSatAnzeigen = new JMenuItem(messages.getString("guievents_msg13"));
                pmSatAnzeigen.setActionCommand("satanzeigen");
                pmSatAnzeigen.addActionListener(al);
                menu.add(pmSatAnzeigen);
 
                nodeItem.getNodeLabel().add(menu);                
                menu.setVisible(true);                
                menu.show(nodeItem.getNodeLabel(), posX, posY);
            }
        	else if (nodeItem.getNode() instanceof InternetNode) {

            	InternetNode node = (InternetNode) nodeItem.getNode();
            	
                JPopupMenu menu = new JPopupMenu();

                ActionListener al = new ActionListener() {
                	
                    public void actionPerformed(ActionEvent e) {
                    	
                        if (e.getActionCommand().equals("desktopanzeigen")) {
                        	container.showDesktop(nodeItem);
                        }

                        if (e.getActionCommand().startsWith("datenaustausch")) {
                            String macAddress = e.getActionCommand().substring(15);
                            container.displayInPacketsAnalyzerDialog(nodeItem, macAddress);
                        }
                    }
                };

                JMenuItem pmVROUTKonf = new JMenuItem(messages.getString("guievents_msg2"));
                pmVROUTKonf.setActionCommand("vroutkonf");
                pmVROUTKonf.addActionListener(al);
                
                if (node instanceof Computer || node instanceof Notebook) {                               
                	JMenuItem pmDesktopAnzeigen = new JMenuItem(messages.getString("guievents_msg3"));
                    pmDesktopAnzeigen.setActionCommand("desktopanzeigen");
                    pmDesktopAnzeigen.addActionListener(al);
                    menu.add(pmDesktopAnzeigen);
                }
                
                for (NetworkInterface nic : node.getNICList()) {
                	if (nic.getPort().isConnected()) {
                		// Display only the connected nic
                		JMenuItem pmDatenAustauschAnzeigen = new JMenuItem(messages.getString("guievents_msg4") + " (" + nic.getIp() + ")");
                		pmDatenAustauschAnzeigen.setActionCommand("datenaustausch-" + nic.getMac());
                		pmDatenAustauschAnzeigen.addActionListener(al);
                		menu.add(pmDatenAustauschAnzeigen);
                	}                    
                }

                nodeItem.getNodeLabel().add(menu);                
                menu.setVisible(true);                
                menu.show(nodeItem.getNodeLabel(), posX, posY);
            }
        }
    }
}
