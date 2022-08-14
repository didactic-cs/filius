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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import filius.auxiliary.I18n;
import filius.gui.components.SatViewerControl;
import filius.gui.modes.design.CableItem;
import filius.gui.modes.design.NodeItem;
import filius.hardware.NetworkInterface;
import filius.hardware.node.Host;
import filius.hardware.node.InternetNode;
import filius.hardware.node.Modem;
import filius.hardware.node.Node;
import filius.hardware.node.Switch;

public class GUIContextMenu implements I18n {
	
	private static GUIContextMenu guiContextMenu = null;
	private GUIContainer container;
	
	
    private GUIContextMenu() {
       
        container = GUIContainer.getInstance();
    }

    public static GUIContextMenu getInstance() {
    	
        if (guiContextMenu == null) {
        	guiContextMenu = new GUIContextMenu();
        }
        return guiContextMenu;
    }

    public void designModeContextMenu(int posX, int posY) {
    	
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

            container.getNodePanel().add(popmen);
            popmen.setVisible(true);
            popmen.show(container.getNodePanel(), posX, posY);
    		
    	} else if (selectedNodeItem != null) {
    		
    		Node node = selectedNodeItem.getNode();
    		boolean hostOrModem = (node instanceof Host || node instanceof Modem);
    		boolean nodeHasCable = node.isConnected();
        	String removeCableString = (hostOrModem ? messages.getString("guievents_msg5") : messages.getString("guievents_msg6"));
        	
        	final JMenuItem pmShowConfig = new JMenuItem(messages.getString("guievents_msg11"));
            final JMenuItem pmRemoveCables = new JMenuItem(removeCableString);
            pmRemoveCables.setEnabled(nodeHasCable);
            final JMenuItem pmDeleteNode = new JMenuItem(messages.getString("guievents_msg7"));

            JPopupMenu popmen = new JPopupMenu();
            ActionListener al = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                	
                    if (e.getSource() == pmShowConfig) {
                    	container.setConfigPanel(selectedNodeItem);
                    	container.getConfigPanel().maximize();
                    }
                    else if (e.getSource() == pmRemoveCables) {
                    	container.removeCablesConnectedTo(selectedNodeItem);
                    }
                    else if (e.getSource() == pmDeleteNode) {
                        removeSelectedNode();
                    }
                }
            };
            
            pmShowConfig.addActionListener(al);
            pmRemoveCables.addActionListener(al);
            pmDeleteNode.addActionListener(al);

            popmen.add(pmShowConfig);
            popmen.add(pmRemoveCables);
            popmen.add(pmDeleteNode);

            container.getNodePanel().add(popmen);
            popmen.setVisible(true);
            popmen.show(container.getNodePanel(), posX, posY);
        }
    }

    /**
     * Context menu for a cable item
	 * Used to delete a single cable
     */
    public void cableContextMenu(final CableItem cableItem, int posX, int posY) {
    	
        final JMenuItem pmRemoveCable = new JMenuItem(messages.getString("guievents_msg5"));
        pmRemoveCable.setActionCommand("removecable");

        JPopupMenu popmen = new JPopupMenu();
        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	removeSelectedCable();
            }
        };

        pmRemoveCable.addActionListener(al);
        popmen.add(pmRemoveCable);

        container.getNodePanel().add(popmen);
        popmen.setVisible(true);
        popmen.show(container.getNodePanel(), posX, posY);
    }
    
    /**
     * Context menu for the workarea in action mode
	 * Used to hide all windows
     */
    public void simulationModeContextMenu(int posX, int posY) {
    	
    	JPopupMenu popupMenu = new JPopupMenu();
    	
    	JMenuItem pmMoveToBottom = new JMenuItem(messages.getString("guiactionmenu_msg1"));
    	
    	pmMoveToBottom.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { 
            	
            	JFrameList.getInstance().putMasterInTheBackground();   
            }
        });
        popupMenu.add(pmMoveToBottom);    

    	JMenuItem pmCloseAllFrames = new JMenuItem(messages.getString("guiactionmenu_msg2"));
    	
    	pmCloseAllFrames.addActionListener(new ActionListener() {
    		
            public void actionPerformed(ActionEvent e) {
               	JFrameList.getInstance().closeAll();
            }
        });
        popupMenu.add(pmCloseAllFrames);
        
        popupMenu.show(container.getNodePanel(), posX, posY);
    }
    
    public void simulationModeNodeContextMenu(final NodeItem nodeItem, int posX, int posY) {
    	
        if (nodeItem != null) {
        	
        	Node node = nodeItem.getNode();
        	
        	if (node instanceof Switch) {

            	Switch sw = (Switch) node;
            	
                JPopupMenu menu = new JPopupMenu();

                ActionListener al = new ActionListener() {
                	
                    public void actionPerformed(ActionEvent e) {                    
                        SatViewerControl.getInstance().addOrShowViewer(sw);                     
                    }
                };       
                                            
                JMenuItem pmSatAnzeigen = new JMenuItem(messages.getString("guievents_msg13"));
                pmSatAnzeigen.addActionListener(al);
                menu.add(pmSatAnzeigen);
 
                nodeItem.getNodeLabel().add(menu);                
                menu.setVisible(true);                
                menu.show(nodeItem.getNodeLabel(), posX, posY);
            }
        	else if (node instanceof InternetNode) {

            	InternetNode iNode = (InternetNode) node;
            	
                JPopupMenu menu = new JPopupMenu();

                ActionListener al = new ActionListener() {
                	
                    public void actionPerformed(ActionEvent e) {
                    	
                        if (e.getActionCommand().equals("showDesktop")) {                     
                        	container.showDesktop(((Host) node).getOS());
                        } 
                        else if (e.getActionCommand().startsWith("showPacketsViewer")) {
                            String macAddress = e.getActionCommand().substring(18);
                            container.showPacketsViewerDialog(nodeItem, macAddress);
                        }
                    }
                };
                
                if (iNode instanceof Host) {                               
                	JMenuItem pmShowDesktop = new JMenuItem(messages.getString("guievents_msg3"));
                    pmShowDesktop.setActionCommand("showDesktop");
                    pmShowDesktop.addActionListener(al);
                    menu.add(pmShowDesktop);
                    
                    NetworkInterface nic = iNode.getNic(0); 
                    JMenuItem pmShowPacketsViewer = new JMenuItem(messages.getString("guievents_msg4") + " (" + nic.getIp() + ")");
                    pmShowPacketsViewer.setActionCommand("showPacketsViewer-" + nic.getMac());
                    pmShowPacketsViewer.addActionListener(al);
                    menu.add(pmShowPacketsViewer);
                    
                } else {
                	// Node is a Router
                	for (NetworkInterface nic : iNode.getNicList()) {
                		if (nic.getPort().isConnected()) {
                			// Display only the connected nic
                			JMenuItem pmShowPacketsViewer = new JMenuItem(messages.getString("guievents_msg4") + " (" + nic.getIp() + ")");
                			pmShowPacketsViewer.setActionCommand("showPacketsViewer-" + nic.getMac());
                			pmShowPacketsViewer.addActionListener(al);
                			menu.add(pmShowPacketsViewer);
                		}                    
                	}
                }

                nodeItem.getNodeLabel().add(menu);                
                menu.setVisible(true);                
                menu.show(nodeItem.getNodeLabel(), posX, posY);
            }
        }
    }
}
