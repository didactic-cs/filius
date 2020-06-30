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

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import filius.Main;
import filius.gui.nachrichtensicht.ExchangeDialog;
import filius.gui.netzwerksicht.GUICableItem;
import filius.gui.netzwerksicht.GUINodeItem;
import filius.gui.netzwerksicht.GUINetworkPanel;
import filius.gui.netzwerksicht.JCablePanel;
import filius.gui.netzwerksicht.JKonfiguration;
import filius.gui.netzwerksicht.JNodeLabel;
import filius.hardware.Cable;
import filius.hardware.NetworkInterface;
import filius.hardware.Port;
import filius.hardware.knoten.Host;
import filius.hardware.knoten.InternetNode;
import filius.hardware.knoten.Node;
import filius.hardware.knoten.Modem;
import filius.hardware.knoten.Notebook;
import filius.hardware.knoten.Rechner;
import filius.hardware.knoten.Switch;
import filius.hardware.knoten.Vermittlungsrechner;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.SzenarioVerwaltung;
import filius.software.system.InternetKnotenBetriebssystem;
import filius.software.system.SwitchFirmware;

public class GUIEvents implements I18n {

    private int auswahlx, auswahly, auswahlx2, auswahly2, mausposx, mausposy;

    private int startPosX, startPosY;
    
    private int shiftX, shiftY;

    private GUICableItem neuesKabel;

    private static GUIEvents ref;   

    private boolean aufmarkierung = false;

    private List<GUINodeItem> markedlist;
    
    private JNodeLabel loeschLabel, selectedLabel = null;

    private GUINodeItem loeschItem, selectedItem, frozenSelectedItem, ziel2;       
    
    private GUICableItem selectedCable, frozenSelectedCable;    
    
    private boolean selectedIsFrozen = false;

    private JCablePanel kabelPanelVorschau;
    
    private GUIContainer container;
    

    private GUIEvents() {
        markedlist = new LinkedList<GUINodeItem>();
        container = GUIContainer.getInstance();
    }

    public static GUIEvents getGUIEvents() {
        if (ref == null) {
            ref = new GUIEvents();
        }

        return ref;
    }

    public void mausReleased() {

        List<GUINodeItem> itemlist = container.getKnotenItems();
        JMarkerPanel auswahl = container.getAuswahl();
        JMarkerPanel markierung = container.getMarkierung();

        SzenarioVerwaltung.getInstance().setzeGeaendert();

        if (auswahl.isVisible()) {
            int tx, ty, twidth, theight;
            int minx = 999999, miny = 999999, maxx = 0, maxy = 0;
            markedlist = new LinkedList<GUINodeItem>();
            for (GUINodeItem tempitem : itemlist) {
                tx = tempitem.getNodeLabel().getX();
                twidth = tempitem.getNodeLabel().getWidth();
                ty = tempitem.getNodeLabel().getY();
                theight = tempitem.getNodeLabel().getHeight();

                int itemPosX = tx + twidth / 2;
                int itemPosY = ty + theight / 2;

                if (itemPosX >= auswahl.getX() && itemPosX <= auswahl.getX() + auswahl.getWidth()
                        && itemPosY >= auswahl.getY() && itemPosY <= auswahl.getY() + auswahl.getHeight()) {
                    minx = Math.min(tx, minx);
                    maxx = Math.max(tx + twidth, maxx);
                    miny = Math.min(ty, miny);
                    maxy = Math.max(ty + theight, maxy);

                    markedlist.add(tempitem);
                }
            }
            if (!this.markedlist.isEmpty()) {
                markierung.setBounds(minx, miny, maxx - minx, maxy - miny);
                markierung.setVisible(true);
            }
            auswahl.setVisible(false);
        }
    }

    public void mausDragged(MouseEvent e) {    	  
    	
        // Do not allow dragging while cable connector is visible, i.e., during
        // cable assignment
        if (container.getKabelvorschau().isVisible()) return;   
        
        // Only drag with left mouse button
        if (!SwingUtilities.isLeftMouseButton(e)) return;    
        
        JMarkerPanel auswahl = container.getAuswahl();
        int neuX, neuY;

        JNodeLabel dragVorschau = container.getDragVorschau();

        SzenarioVerwaltung.getInstance().setzeGeaendert();

        // Einzelnes Item verschieben
        if (!container.isMarkerVisible()) {
            if (selectedLabel != null && !dragVorschau.isVisible()) {
            	neuX = e.getX() + shiftX;
            	if (neuX < 0) {
            		neuX = 0;
            	} else {
            		int maxX = container.getWidth() - selectedLabel.getWidth();
            		if (neuX > maxX) {
            			neuX = maxX - 1;
            	    }            		
            	}
            	
            	neuY = e.getY() + shiftY;
            	if (neuY < 0) {
            		neuY = 0;
            	} else {
            		int maxY = container.getHeight() - selectedLabel.getHeight();
            		if (neuY > maxY) {
            			neuY = maxY - 1;
            	    }            		
            	}
            	
                selectedLabel.setLocation(neuX, neuY);
                container.updateCables();
            } else {
                mausposx = e.getX();
                mausposy = e.getY();
                if (!auswahl.isVisible()) {
                    auswahlx = mausposx;
                    auswahly = mausposy;
                    auswahlx2 = auswahlx;
                    auswahly2 = auswahly;

                    auswahl.setBounds(auswahlx, auswahly, auswahlx2 - auswahlx, auswahly2 - auswahly);
                    auswahl.setVisible(true);
                } else {
                    auswahlx2 = mausposx;
                    auswahly2 = mausposy;

                    auswahl.setBounds(auswahlx, auswahly, auswahlx2 - auswahlx, auswahly2 - auswahly);

                    if (mausposx < auswahlx) {
                        auswahl.setBounds(auswahlx2, auswahly, auswahlx - auswahlx2, auswahly2 - auswahly);
                    }
                    if (mausposy < auswahly) {
                        auswahl.setBounds(auswahlx, auswahly2, auswahlx2 - auswahlx, auswahly - auswahly2);
                    }
                    if (mausposy < auswahly && mausposx < auswahlx) {
                        auswahl.setBounds(auswahlx2, auswahly2, auswahlx - auswahlx2, auswahly - auswahly2);
                    }
                }
            }
        }
        // Items im Auswahlrahmen verschieben
        else if (!dragVorschau.isVisible()) {
            /* Verschieben mehrerer ausgewaehlter Objekte */
            if (aufmarkierung && markedlist.size() > 0 && e.getX() >= 0 && e.getX() <= container.getWidth() && e.getY() >= 0
                    && e.getY() <= container.getHeight()) {

                int verschiebungx = startPosX - e.getX();
                startPosX = e.getX();
                int verschiebungy = startPosY - e.getY();
                startPosY = e.getY();

                container.moveMarker(-verschiebungx, -verschiebungy, markedlist);
            } else {
                Main.debug.println("Out of Boundaries!");
            }
        }
    }

    public void mausPressedDesignMode(MouseEvent e) {
        
        JMarkerPanel auswahl = container.getAuswahl();

        SzenarioVerwaltung.getInstance().setzeGeaendert();

        if (neuesKabel == null) {
            neuesKabel = new GUICableItem();
        }
        updateAktivesItem(e.getX(), e.getY());

        if (container.getMarkierung().inBounds(e.getX(), e.getY())) {
            if (container.getMarkierung().isVisible()) {
                aufmarkierung = true;
                startPosX = e.getX();
                startPosY = e.getY();
            }
        } else {
            aufmarkierung = false;
            container.getMarkierung().setVisible(false);
            auswahl.setBounds(0, 0, 0, 0);
        }

        // Right click
        if (e.getButton() == MouseEvent.BUTTON3) {
        	
        	// On a node
            if (selectedItem != null && selectedLabel != null) {

                if (!container.getKabelvorschau().isVisible()) {
                    kontextMenueEntwurfsmodus(selectedLabel, e.getX(), e.getY());
                } else {
                    resetAndHideCablePreview();
                }
                
             // Show selection frame
                selectedLabel.setSelected(true);
                
            // On a cable?    
            } else {
            	updateActiveCable(e.getX(), e.getY());
            	
                GUICableItem cableItem = findClickedCable(e);
                if ((kabelPanelVorschau == null || !kabelPanelVorschau.isVisible())
                        && container.getActiveSite() == GUIMainMenu.MODUS_ENTWURF
                        && cableItem != null) {
                    contextMenuCable(cableItem, e.getX(), e.getY());
                } else {
                    resetAndHideCablePreview();
                }                  
            }
        }
        // Left click
        else {
            if (e.getButton() == MouseEvent.BUTTON1) {
            	
                // A node is active
                if (selectedItem != null && selectedLabel != null) {
                	
                    // The connector tool is selected
                    if (container.getKabelvorschau().isVisible()) {
                    	
                        // hide property panel (JKonfiguration)
                    	container.getProperty().minimieren();

                        if (selectedItem.getNode() instanceof Node) {
                            Node tempKnoten = (Node) selectedItem.getNode();
                            boolean success = true;
                            if (selectedItem.getNode() instanceof Node) {
                                tempKnoten = (Node) selectedItem.getNode();
                                Port anschluss = tempKnoten.getFreePort();
                                if (anschluss == null) {
                                    success = false;
                                    GUIErrorHandler.getGUIErrorHandler()
                                            .DisplayError(messages.getString("guievents_msg1"));
                                }
                            }
                            if (success && neuesKabel.getCablePanel().getZiel1() != null) {
                                Node quellKnoten = neuesKabel.getCablePanel().getZiel1().getNode();
                                if (tempKnoten.isConnectedTo(quellKnoten)) {
                                    success = false;
                                    GUIErrorHandler.getGUIErrorHandler()
                                            .DisplayError(messages.getString("guievents_msg12"));
                                }
                            }
                            if (success) {
                                processCableConnection(e.getX(), e.getY());
                            }
                        }
                        
                      // 
                    } else {
                    	// Update property panel
                        container.setProperty(selectedItem);
                        
                        // Display property panel if double-clicked
                        if (e.getClickCount() == 2) container.getProperty().maximieren();
                                                                
                        // Show selection frame
                        selectedLabel.setSelected(true);
                        
                        // Die Verschiebung speichern für spätere Verwendung in mausDragged
                        shiftX = selectedLabel.getX() - e.getX();                           
                        shiftY = selectedLabel.getY() - e.getY();    
                    }
           	
                // No node is active	
                } else {
                	// Did the click occur on a cable?
                	updateActiveCable(e.getX(), e.getY());
                	if (selectedCable != null) {
                		
                		container.setProperty(selectedCable);
                		
                		if (e.getClickCount() == 2) container.getProperty().maximieren();
                		
                		return;                	
                	}
                	
                	// The click was done on an empty area
                    auswahl.setVisible(false);   
                    unselectActiveCable();
                    container.getProperty().minimieren();
                    container.setProperty(null);
                }
            }
        }
    }
    
    public void cancelMultipleSelection() {
        aufmarkierung = false;
        container.getMarkierung().setVisible(false);
        container.getAuswahl().setBounds(0, 0, 0, 0);
    }

    public void processCableConnection(int currentPosX, int currentPosY) {
        if (neuesKabel.getCablePanel().getZiel1() == null) {
            connectCableToFirstComponent(currentPosX, currentPosY);
        } else {
            if (neuesKabel.getCablePanel().getZiel2() == null && neuesKabel.getCablePanel().getZiel1() != selectedItem) {
                connectCableToSecondComponent(selectedItem);
            }
            int posX = currentPosX;
            int posY = currentPosY;
            resetAndShowCablePreview(posX, posY);
        }
    }

    private void connectCableToFirstComponent(int currentPosX, int currentPosY) {
        // Main.debug.println("\tmausPressed: IF-2.2.1.2.1");
        neuesKabel.getCablePanel().setZiel1(selectedItem);
        container.getKabelvorschau().setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/ziel2.png")));
        kabelPanelVorschau = new JCablePanel();
        container.getDesignpanel().add(kabelPanelVorschau);
        kabelPanelVorschau.setZiel1(selectedItem);
        container.setZiel2Label(new JNodeLabel());
        ziel2 = new GUINodeItem();
        ziel2.setNodeLabel(container.getZiel2Label());

        container.getZiel2Label().setBounds(currentPosX, currentPosY, 8, 8);
        kabelPanelVorschau.setZiel2(ziel2);
        kabelPanelVorschau.setVisible(true);
        container.setKabelPanelVorschau(kabelPanelVorschau);
    }

    private GUICableItem findClickedCable(MouseEvent e) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + ", clickedCable(" + e + ")");
        // Falls kein neues Objekt erstellt werden soll
        int xPos = e.getX() + container.getXOffset();
        int yPos = e.getY() + container.getYOffset();

        for (GUICableItem tempitem : container.getCableItems()) {
            // item clicked, i.e., mouse pointer within item bounds
            if (tempitem.getCablePanel().clicked(xPos, yPos)) {
                // mouse pointer really close to the drawn line, too
                return tempitem;
            }
        }
        return null;
    }

    private void updateAktivesItem(int posX, int posY) {
        // Falls kein neues Objekt erstellt werden soll
        selectedLabel = null;
        selectedItem = null;        
        
        if (!container.isMarkerVisible()) {
        	
        	// Unselect all nodes and determine which is the active one
            for (GUINodeItem tempItem : container.getKnotenItems()) {
                JNodeLabel tempLabel = tempItem.getNodeLabel();
                tempLabel.setSelected(false);
                tempLabel.revalidate();
                tempLabel.updateUI();

                if (tempLabel.inBounds(posX, posY)) {
                    selectedItem = tempItem;
                    selectedLabel = tempItem.getNodeLabel();
                }
            }  
            
            if (selectedItem != null) unselectActiveCable();
        }
    }
    
    // Remove the active cable if any
    protected void removeActiveCable() {     
    	
    	if (selectedCable != null) {
    		removeSingleCable(selectedCable);
    		selectedCable = null;
    		container.getProperty().minimieren();
    		container.setProperty(null);
    	}
    }
    
    // Unselect the active cable if any
    // Activation is used in design mode only to highlight a cable
    protected void unselectActiveCable() {     
    	
    	if (selectedCable != null) {
    		selectedCable.getCablePanel().setSelected(false);
    		selectedCable = null;
    	}
    }
    
    // Determine if a cable can be set active based on the coordinates
    private void updateActiveCable(int x, int y) {      
    	
    	for (GUICableItem cable : container.getCableItems()) {

    		JCablePanel cp = cable.getCablePanel();
    		if (cp.clicked(x, y)) {
    			if (cable == selectedCable) return;
    			
    			unselectActiveCable();   
    			
    			cp.setSelected(true);
    			selectedCable = cable;
    			return;
    		}
    	}
    	
    	unselectActiveCable();   
    }
    
    public GUICableItem getActiveCable() {
        return selectedCable;
    }
    
    public GUINodeItem getActiveItem() {
        return selectedItem;
    }
    
    // Keep track of which element is active
    // when leaving the design mode
    public void freezeActiveElements() {
    	
    	if (! selectedIsFrozen) {
    		frozenSelectedItem = selectedItem;
        	frozenSelectedCable = selectedCable; 
        	
        	selectedIsFrozen = true;
    	}    	
    }
    
    // Restore the active element when
    // returning to the design mode
    public void unFreezeActiveElements() {
    	
    	if (selectedIsFrozen) {
    		if (frozenSelectedItem != null) {
    			selectedItem = frozenSelectedItem;
    			selectedItem.getNodeLabel().setSelected(true);    			
    		} else if (frozenSelectedCable != null) {
    			selectedCable = frozenSelectedCable;
    			selectedCable.getCablePanel().setSelected(true);
    		}        	
        	selectedIsFrozen = false;
    	}  
    }

    /*
     * method called in case of new item creation in GUIContainer, such that this creation process will be registered
     * and the according item is marked active
     */
    public void setNewItemActive(GUINodeItem item) {
        selectedItem = item;
    }

    private void desktopAnzeigen(GUINodeItem aktivesItem) {
    	container.showDesktop(aktivesItem);
    }

    private void connectCableToSecondComponent(GUINodeItem tempitem) {
       
        GUINetworkPanel draftpanel = container.getDesignpanel();
        NetworkInterface nic1, nic2;
        Port anschluss1 = null;
        Port anschluss2 = null;

        neuesKabel.getCablePanel().setZiel2(tempitem);
        draftpanel.remove(kabelPanelVorschau);
        ziel2 = null;

        draftpanel.add(neuesKabel.getCablePanel());
        neuesKabel.getCablePanel().updateBounds();
        draftpanel.updateUI();
        container.getCableItems().add(neuesKabel);
        if (neuesKabel.getCablePanel().getZiel1().getNode() instanceof Modem) {
            Modem vrOut = (Modem) neuesKabel.getCablePanel().getZiel1().getNode();
            anschluss1 = vrOut.getPort();
        } else if (neuesKabel.getCablePanel().getZiel1().getNode() instanceof Vermittlungsrechner) {
            Vermittlungsrechner r = (Vermittlungsrechner) neuesKabel.getCablePanel().getZiel1().getNode();
            anschluss1 = r.getFreePort();
        } else if (neuesKabel.getCablePanel().getZiel1().getNode() instanceof Switch) {
            Switch sw = (Switch) neuesKabel.getCablePanel().getZiel1().getNode();
            anschluss1 = ((SwitchFirmware) sw.getSystemSoftware()).getKnoten().getFreePort();
        } else if (neuesKabel.getCablePanel().getZiel1().getNode() instanceof InternetNode) {
            nic1 = (NetworkInterface) ((InternetNode) neuesKabel.getCablePanel().getZiel1().getNode())
                    .getNIlist().get(0);
            anschluss1 = nic1.getPort();
        }

        if (neuesKabel.getCablePanel().getZiel2().getNode() instanceof Modem) {
            Modem vrOut = (Modem) neuesKabel.getCablePanel().getZiel2().getNode();
            anschluss2 = vrOut.getPort();
        } else if (neuesKabel.getCablePanel().getZiel2().getNode() instanceof Vermittlungsrechner) {
            Vermittlungsrechner r = (Vermittlungsrechner) neuesKabel.getCablePanel().getZiel2().getNode();
            anschluss2 = r.getFreePort();
        } else if (neuesKabel.getCablePanel().getZiel2().getNode() instanceof Switch) {
            Switch sw = (Switch) neuesKabel.getCablePanel().getZiel2().getNode();
            anschluss2 = ((SwitchFirmware) sw.getSystemSoftware()).getKnoten().getFreePort();
        } else if (neuesKabel.getCablePanel().getZiel2().getNode() instanceof InternetNode) {
            nic2 = (NetworkInterface) ((InternetNode) neuesKabel.getCablePanel().getZiel2().getNode())
                    .getNIlist().get(0);
            anschluss2 = nic2.getPort();
        }

        neuesKabel.setCable(new Cable());
        neuesKabel.getCable().setPorts(new Port[] { anschluss1, anschluss2 });

        resetAndHideCablePreview();
    }

    public void resetAndHideCablePreview() {
        resetCableTool();
        hideCableToolPanel();
    }

    private void hideCableToolPanel() {
    	container.getKabelvorschau().setVisible(false);
    }

    private void resetCableTool() {
        neuesKabel = new GUICableItem();
        container.getKabelvorschau()
                .setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/ziel1.png")));
        ziel2 = null;

        if (kabelPanelVorschau != null) {
            kabelPanelVorschau.setVisible(false);
        }
    }

    public void resetAndShowCablePreview(int currentPosX, int currentPosY) {
        resetCableTool();
        showCableToolPanel(currentPosX, currentPosY);
        cancelMultipleSelection();
    }

    private void showCableToolPanel(int currentPosX, int currentPosY) {
        JNodeLabel cablePreview = container.getKabelvorschau();
        cablePreview.setBounds(currentPosX, currentPosY, cablePreview.getWidth(), cablePreview.getHeight());
        cablePreview.setVisible(true);
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
    public void kontextMenueAktionsmodus(final GUINodeItem knotenItem, int posX, int posY) {
        if (knotenItem != null) {
            if (knotenItem.getNode() instanceof InternetNode) {

                JPopupMenu popmen = new JPopupMenu();

                ActionListener al = new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (e.getActionCommand().equals("desktopanzeigen")) {
                            desktopAnzeigen(knotenItem);
                        }

                        if (e.getActionCommand().startsWith("datenaustausch")) {
                            String macAddress = e.getActionCommand().substring(15);
                            datenAustauschAnzeigen(knotenItem, macAddress);
                        }

                    }
                };

                JMenuItem pmVROUTKonf = new JMenuItem(messages.getString("guievents_msg2"));
                pmVROUTKonf.setActionCommand("vroutkonf");
                pmVROUTKonf.addActionListener(al);

                JMenuItem pmDesktopAnzeigen = new JMenuItem(messages.getString("guievents_msg3"));
                pmDesktopAnzeigen.setActionCommand("desktopanzeigen");
                pmDesktopAnzeigen.addActionListener(al);
                if (knotenItem.getNode() instanceof Rechner || knotenItem.getNode() instanceof Notebook) {
                    popmen.add(pmDesktopAnzeigen);
                }

                InternetNode node = (InternetNode) knotenItem.getNode();
                for (NetworkInterface nic : node.getNIlist()) {
                    JMenuItem pmDatenAustauschAnzeigen = new JMenuItem(
                            messages.getString("guievents_msg4") + " (" + nic.getIp() + ")");
                    pmDatenAustauschAnzeigen.setActionCommand("datenaustausch-" + nic.getMac());
                    pmDatenAustauschAnzeigen.addActionListener(al);

                    popmen.add(pmDatenAustauschAnzeigen);
                }

                knotenItem.getNodeLabel().add(popmen);
                popmen.setVisible(true);
                popmen.show(knotenItem.getNodeLabel(), posX, posY);
            }
        }
    }

    private void datenAustauschAnzeigen(GUINodeItem item, String macAddress) {
        InternetKnotenBetriebssystem bs;
        ExchangeDialog exchangeDialog = container.getExchangeDialog();

        if (item.getNode() instanceof InternetNode) {
            bs = (InternetKnotenBetriebssystem) ((InternetNode) item.getNode()).getSystemSoftware();
            exchangeDialog.addTable(bs, macAddress);
            ((JFrame) exchangeDialog).setVisible(true);
        }
    }

    /**
     * @author Johannes Bade & Thomas Gerding
     * 
     *         Bei rechter Maustaste auf ein Item (bei Laufendem Entwurfsmodus) wird ein Kontextmenü angezeigt, in dem
     *         z.B. das Item gelöscht, kopiert oder ausgeschnitten werden kann.
     * 
     * @param templabel
     *            Item auf dem das Kontextmenü erscheint
     * @param e
     *            MouseEvent (Für Position d. Kontextmenü u.a.)
     */
    private void kontextMenueEntwurfsmodus(JNodeLabel templabel, int posX, int posY) {
        String textKabelEntfernen;

        updateAktivesItem(posX, posY);

        if (selectedItem != null) {
            if (selectedItem.getNode() instanceof Rechner || selectedItem.getNode() instanceof Notebook) {
                textKabelEntfernen = messages.getString("guievents_msg5");
            } else {
                textKabelEntfernen = messages.getString("guievents_msg6");
            }

            final JMenuItem pmShowConfig = new JMenuItem(messages.getString("guievents_msg11"));
            pmShowConfig.setActionCommand("showconfig");
            final JMenuItem pmKabelEntfernen = new JMenuItem(textKabelEntfernen);
            pmKabelEntfernen.setActionCommand("kabelentfernen");
            final JMenuItem pmLoeschen = new JMenuItem(messages.getString("guievents_msg7"));
            pmLoeschen.setActionCommand("del");

            JPopupMenu popmen = new JPopupMenu();
            ActionListener al = new ActionListener() {
                public void actionPerformed(ActionEvent e) {

                    if (e.getActionCommand() == pmLoeschen.getActionCommand()) {
                        itemLoeschen(loeschLabel, loeschItem);
                    } else if (e.getActionCommand() == pmKabelEntfernen.getActionCommand()) {
                        kabelEntfernen();
                    } else if (e.getActionCommand() == pmShowConfig.getActionCommand()) {
                    	container.setProperty(selectedItem);
                    	container.getProperty().maximieren();
                    }
                }
            };

            pmLoeschen.addActionListener(al);
            pmKabelEntfernen.addActionListener(al);
            pmShowConfig.addActionListener(al);

            popmen.add(pmShowConfig);
            popmen.add(pmKabelEntfernen);
            popmen.add(pmLoeschen);

            container.getDesignpanel().add(popmen);
            popmen.setVisible(true);
            popmen.show(container.getDesignpanel(), posX, posY);

            loeschLabel = templabel;
            loeschItem = selectedItem;
        }
    }

    /**
     * context menu in case of clicking on single cable item --> used for deleting a single cable
     */
    private void contextMenuCable(final GUICableItem cable, int posX, int posY) {
        final JMenuItem pmRemoveCable = new JMenuItem(messages.getString("guievents_msg5"));
        pmRemoveCable.setActionCommand("removecable");

        JPopupMenu popmen = new JPopupMenu();
        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand() == pmRemoveCable.getActionCommand()) {
                	removeActiveCable();                    
                }
            }
        };

        pmRemoveCable.addActionListener(al);
        popmen.add(pmRemoveCable);

        container.getDesignpanel().add(popmen);
        popmen.setVisible(true);
        popmen.show(container.getDesignpanel(), posX, posY);
    }

    /**
     * 
     * Löscht das durch loeschlabel angegebene Item NOTE: made public for using del key to delete items without local
     * context menu action (cf. JMainFrame)
     */
    public void itemLoeschen(JNodeLabel loeschlabel, GUINodeItem loeschitem) {
        loeschlabel.setVisible(false);
        container.setProperty(null);
        ListIterator<GUICableItem> iteratorAlleKabel = container.getCableItems().listIterator();
        GUICableItem kabel = new GUICableItem();
        LinkedList<GUICableItem> loeschKabel = new LinkedList<GUICableItem>();

        // Zu löschende Elemente werden in eine temporäre Liste gepackt
        while (iteratorAlleKabel.hasNext()) {
            kabel = (GUICableItem) iteratorAlleKabel.next();
            if (kabel.getCablePanel().getZiel1().equals(loeschitem)
                    || kabel.getCablePanel().getZiel2().equals(loeschitem)) {
                loeschKabel.add(kabel);
            }
        }

        // Temporäre Liste der zu löschenden Kabel wird iteriert und dabei
        // werden die Kabel aus der globalen Kabelliste gelöscht
        // und vom Panel entfernt
        ListIterator<GUICableItem> iteratorLoeschKabel = loeschKabel.listIterator();
        while (iteratorLoeschKabel.hasNext()) {
            kabel = iteratorLoeschKabel.next();

            this.removeSingleCable(kabel);
        }
        
        // Remove the simulation mode's elements related to the item
        if (loeschitem.getNode() instanceof Host) {
        	// Remove the desktop's JFrame
        	container.removeDesktopWindow(loeschitem);
        	
        	// Remove the table in ExchangeDialog        	
        	String mac = ((Host)loeschitem.getNode()).getMac();
        	container.getExchangeDialog().removeTable(mac, null);
        	
        } else if (loeschitem.getNode() instanceof Switch) {
        	// Remove the SATtable's JFrame
        	SatViewerControl.getInstance().removeViewer((Switch)loeschitem.getNode());
        	
        } else if (loeschitem.getNode() instanceof Vermittlungsrechner) {
        	// Remove the tables in ExchangeDialog
        	List<String> macs = ((Vermittlungsrechner)loeschitem.getNode()).getMacs();
        	for (String mac: macs) container.getExchangeDialog().removeTable(mac, null);
        }

        container.removeNodeItem(loeschitem);
        container.getDesignpanel().remove(loeschlabel);
        container.getDesignpanel().updateUI();
        container.updateViewport();
    }

    // remove a single cable without using touching the connected node
    protected void removeSingleCable(GUICableItem cableItem) {
        Main.debug.println("INVOKED filius.gui.GUIEvents, removeSingleCable(" + cableItem + ")");
        if (cableItem == null)
            return; // no cable to be removed (this variable should be set in
                    // contextMenuCable)

        filius.gui.netzwerksicht.JVermittlungsrechnerKonfiguration ziel1konf = null;
        filius.gui.netzwerksicht.JVermittlungsrechnerKonfiguration ziel2konf = null;

        if (JKonfiguration.getInstance(cableItem.getCablePanel().getZiel1()
                .getNode()) instanceof filius.gui.netzwerksicht.JVermittlungsrechnerKonfiguration) {
            // Main.debug.println("DEBUG filius.gui.GUIEvents, removeSingleCable: getZiel1 -->
            // JVermittlungsrechnerKonfiguration");
            ziel1konf = ((filius.gui.netzwerksicht.JVermittlungsrechnerKonfiguration) JKonfiguration
                    .getInstance(cableItem.getCablePanel().getZiel1().getNode()));
        }
        if (JKonfiguration.getInstance(cableItem.getCablePanel().getZiel2()
                .getNode()) instanceof filius.gui.netzwerksicht.JVermittlungsrechnerKonfiguration) {
            // Main.debug.println("DEBUG filius.gui.GUIEvents, removeSingleCable: getZiel1 -->
            // JVermittlungsrechnerKonfiguration");
            ziel2konf = ((filius.gui.netzwerksicht.JVermittlungsrechnerKonfiguration) JKonfiguration
                    .getInstance(cableItem.getCablePanel().getZiel2().getNode()));
        }
        cableItem.getCable().disconnect();
        container.getCableItems().remove(cableItem);
        container.getDesignpanel().remove(cableItem.getCablePanel());
        container.updateViewport();

        if (ziel1konf != null)
            ziel1konf.updateAttribute();
        if (ziel2konf != null)
            ziel2konf.updateAttribute();
    }

    /**
     * 
     * Entfernt das Kabel, welches am aktuellen Item angeschlossen ist
     * 
     * Ersetzt spaeter kabelEntfernen!
     * 
     */
    private void kabelEntfernen() {
        ListIterator<GUICableItem> iteratorAlleKabel = container.getCableItems().listIterator();
        GUICableItem tempKabel = null;
        LinkedList<GUICableItem> loeschListe = new LinkedList<GUICableItem>();

        // Zu löschende Elemente werden in eine temporäre Liste gepackt
        while (iteratorAlleKabel.hasNext()) {
            tempKabel = (GUICableItem) iteratorAlleKabel.next();
            if (tempKabel.getCablePanel().getZiel1().equals(loeschItem)) {
                loeschListe.add(tempKabel);
            }

            if (tempKabel.getCablePanel().getZiel2().equals(loeschItem)) {
                loeschListe.add(tempKabel);
                ziel2 = loeschItem;
            }
        }

        // Temporäre Liste der zu löschenden Kabel wird iteriert und dabei
        // werden die Kabel aus der globalen Kabelliste gelöscht
        // und vom Panel entfernt
        ListIterator<GUICableItem> iteratorLoeschKabel = loeschListe.listIterator();
        while (iteratorLoeschKabel.hasNext()) {
            tempKabel = iteratorLoeschKabel.next();
            this.removeSingleCable(tempKabel);
        }

        container.updateViewport();

    }
}
