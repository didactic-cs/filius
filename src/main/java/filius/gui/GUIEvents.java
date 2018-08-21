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
import java.util.ListIterator;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;

import filius.Main;
import filius.gui.nachrichtensicht.ExchangeDialog;
import filius.gui.netzwerksicht.GUIKabelItem;
import filius.gui.netzwerksicht.GUIKnotenItem;
import filius.gui.netzwerksicht.GUINetworkPanel;
import filius.gui.netzwerksicht.JCablePanel;
import filius.gui.netzwerksicht.JKonfiguration;
import filius.gui.netzwerksicht.JSidebarButton;
import filius.hardware.Kabel;
import filius.hardware.NetzwerkInterface;
import filius.hardware.Port;
import filius.hardware.knoten.InternetKnoten;
import filius.hardware.knoten.Knoten;
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

    private GUIKabelItem neuesKabel;

    private static GUIEvents ref;

    private JSidebarButton aktiveslabel = null;

    private boolean aufmarkierung = false;

    private List<GUIKnotenItem> markedlist;

    private GUIKnotenItem loeschitem, aktivesItem, ziel2;

    private JSidebarButton loeschlabel;

    private JCablePanel kabelPanelVorschau;

    private GUIEvents() {
        markedlist = new LinkedList<GUIKnotenItem>();
    }

    public static GUIEvents getGUIEvents() {
        if (ref == null) {
            ref = new GUIEvents();
        }

        return ref;
    }

    public void mausReleased() {
        GUIContainer c = GUIContainer.getGUIContainer();

        List<GUIKnotenItem> itemlist = c.getKnotenItems();
        JMarkerPanel auswahl = c.getAuswahl();
        JMarkerPanel markierung = c.getMarkierung();

        SzenarioVerwaltung.getInstance().setzeGeaendert();

        if (auswahl.isVisible()) {
            int tx, ty, twidth, theight;
            int minx = 999999, miny = 999999, maxx = 0, maxy = 0;
            markedlist = new LinkedList<GUIKnotenItem>();
            for (GUIKnotenItem tempitem : itemlist) {
                tx = tempitem.getImageLabel().getX();
                twidth = tempitem.getImageLabel().getWidth();
                ty = tempitem.getImageLabel().getY();
                theight = tempitem.getImageLabel().getHeight();

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
        // do not allow dragging while cable connector is visible, i.e., during
        // cable assignment
        if (GUIContainer.getGUIContainer().getKabelvorschau().isVisible()) {
            return;
        }

        GUIContainer c = GUIContainer.getGUIContainer();

        JMarkerPanel auswahl = c.getAuswahl();
        int neuX, neuY, neuWidth, neuHeight;
        int tmpX, tmpY; // for calculating the actual position (only within
                        // working panel)

        JSidebarButton dragVorschau = c.getDragVorschau();

        SzenarioVerwaltung.getInstance().setzeGeaendert();

        // Einzelnes Item verschieben
        if (!c.isMarkerVisible()) {
            if (aktiveslabel != null && !dragVorschau.isVisible()) {
                tmpX = e.getX() - (aktiveslabel.getWidth() / 2);
                if (tmpX < -(aktiveslabel.getWidth() / 2)) {
                    neuX = -(aktiveslabel.getWidth() / 2);
                } else if (tmpX > (GUIContainer.getGUIContainer().getWidth() - (aktiveslabel.getWidth() / 2))) {
                    neuX = GUIContainer.getGUIContainer().getWidth() - (aktiveslabel.getWidth() / 2);
                } else {
                    neuX = tmpX;
                }
                tmpY = e.getY() - (aktiveslabel.getHeight() / 2);
                if (tmpY < -(aktiveslabel.getHeight() / 2)) {
                    neuY = -(aktiveslabel.getHeight() / 2);
                } else if (tmpY > (GUIContainer.getGUIContainer().getHeight() - (aktiveslabel.getHeight() / 2))) {
                    neuY = (GUIContainer.getGUIContainer().getHeight() - (aktiveslabel.getHeight() / 2));
                } else {
                    neuY = tmpY;
                }
                neuWidth = aktiveslabel.getWidth();
                neuHeight = aktiveslabel.getHeight();
                aktiveslabel.setBounds(neuX, neuY, neuWidth, neuHeight);
                c.updateCables();
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
            if (aufmarkierung && markedlist.size() > 0 && e.getX() >= 0 && e.getX() <= c.getScrollPane().getWidth()
                    && e.getY() >= 0 && e.getY() <= c.getScrollPane().getHeight()) {

                int verschiebungx = startPosX - e.getX();
                startPosX = e.getX();
                int verschiebungy = startPosY - e.getY();
                startPosY = e.getY();

                c.moveMarker(-verschiebungx, -verschiebungy, markedlist);
            }
        }
    }

    public void mausPressedDesignMode(MouseEvent e) {
        GUIContainer c = GUIContainer.getGUIContainer();
        JMarkerPanel auswahl = c.getAuswahl();

        Port anschluss = null;
        Knoten tempKnoten;

        SzenarioVerwaltung.getInstance().setzeGeaendert();

        if (neuesKabel == null) {
            neuesKabel = new GUIKabelItem();
        }
        updateAktivesItem(e.getX(), e.getY());

        if (GUIContainer.getGUIContainer().getMarkierung().inBounds(e.getX(), e.getY())) {
            if (GUIContainer.getGUIContainer().getMarkierung().isVisible()) {
                aufmarkierung = true;
                startPosX = e.getX();
                startPosY = e.getY();
            }
        } else {
            aufmarkierung = false;
            GUIContainer.getGUIContainer().getMarkierung().setVisible(false);
            auswahl.setBounds(0, 0, 0, 0);
        }

        // Wurde die rechte Maustaste betaetigt?
        if (e.getButton() == 3) {
            if (aktivesItem != null && aktiveslabel != null) {
                GUIContainer.getGUIContainer().getProperty().minimieren();
                GUIContainer.getGUIContainer().setProperty(null);

                if (!c.getKabelvorschau().isVisible()) {
                    kontextMenueEntwurfsmodus(aktiveslabel, e.getX(), e.getY());
                } else {
                    resetAndHideCablePreview();
                }
            } else {
                GUIKabelItem cableItem = findClickedCable(e);
                if ((kabelPanelVorschau == null || !kabelPanelVorschau.isVisible())
                        && GUIContainer.getGUIContainer().getActiveSite() == GUIMainMenu.MODUS_ENTWURF
                        && cableItem != null) {
                    contextMenuCable(cableItem, e.getX(), e.getY());
                } else {
                    resetAndHideCablePreview();
                }
            }
        }
        // Wurde die linke Maustaste betaetigt?
        else {
            if (e.getButton() == 1) {
                // eine neue Kabelverbindung erstellen
                if (aktivesItem != null && aktiveslabel != null) {
                    if (GUIContainer.getGUIContainer().getKabelvorschau().isVisible()) {
                        // hide property panel (JKonfiguration)
                        GUIContainer.getGUIContainer().getProperty().minimieren();

                        if (aktivesItem.getKnoten() instanceof Knoten) {
                            tempKnoten = (Knoten) aktivesItem.getKnoten();
                            anschluss = tempKnoten.holeFreienPort();
                        }

                        if (anschluss != null) {
                            processCableConnection(e.getX(), e.getY());
                        } else {
                            GUIErrorHandler.getGUIErrorHandler().DisplayError(messages.getString("guievents_msg1"));
                        }

                    } else {
                        // einen Knoten zur Bearbeitung der Eigenschaften
                        // auswaehlen
                        if (GUIContainer.getGUIContainer().getKabelvorschau().isVisible()) {
                            resetAndHideCablePreview();
                        }

                        c.setProperty(aktivesItem);
                        if (e.getClickCount() == 2) {
                            GUIContainer.getGUIContainer().getProperty().maximieren();
                        }
                        aktiveslabel.setSelektiert(true);
                    }
                } else {
                    // wurde Maus ueber leerem Bereich betaetigt? -> Markierung
                    // sichtbar machen
                    auswahl.setVisible(false);
                    GUIContainer.getGUIContainer().getProperty().minimieren();
                    GUIContainer.getGUIContainer().setProperty(null);
                }
            }
        }
    }

    public void processCableConnection(int currentPosX, int currentPosY) {
        if (neuesKabel.getKabelpanel().getZiel1() == null) {
            connectCableToFirstComponent(currentPosX, currentPosY);

        } else {
            if (neuesKabel.getKabelpanel().getZiel2() == null && neuesKabel.getKabelpanel().getZiel1() != aktivesItem) {
                connectCableToSecondComponent(aktivesItem);
            }
            int posX = currentPosX;
            int posY = currentPosY;
            resetAndShowCablePreview(posX, posY);
        }
    }

    private void connectCableToFirstComponent(int currentPosX, int currentPosY) {
        // Main.debug.println("\tmausPressed: IF-2.2.1.2.1");
        neuesKabel.getKabelpanel().setZiel1(aktivesItem);
        GUIContainer.getGUIContainer().getKabelvorschau()
                .setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/ziel2.png")));
        kabelPanelVorschau = new JCablePanel();
        GUIContainer.getGUIContainer().getDesignpanel().add(kabelPanelVorschau);
        kabelPanelVorschau.setZiel1(aktivesItem);
        GUIContainer.getGUIContainer().setZiel2Label(new JSidebarButton());
        ziel2 = new GUIKnotenItem();
        ziel2.setImageLabel(GUIContainer.getGUIContainer().getZiel2Label());

        GUIContainer.getGUIContainer().getZiel2Label().setBounds(currentPosX, currentPosY, 8, 8);
        kabelPanelVorschau.setZiel2(ziel2);
        kabelPanelVorschau.setVisible(true);
        GUIContainer.getGUIContainer().setKabelPanelVorschau(kabelPanelVorschau);
    }

    private GUIKabelItem findClickedCable(MouseEvent e) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + ", clickedCable(" + e + ")");
        // Falls kein neues Objekt erstellt werden soll
        int xPos = e.getX() + GUIContainer.getGUIContainer().getXOffset();
        int yPos = e.getY() + GUIContainer.getGUIContainer().getYOffset();

        for (GUIKabelItem tempitem : GUIContainer.getGUIContainer().getCableItems()) {
            // item clicked, i.e., mouse pointer within item bounds
            if (tempitem.getKabelpanel().clicked(xPos, yPos)) {
                // mouse pointer really close to the drawn line, too
                return tempitem;
            }
        }
        return null;
    }

    private void updateAktivesItem(int posX, int posY) {
        // Falls kein neues Objekt erstellt werden soll
        aktiveslabel = null;
        aktivesItem = null;

        if (!GUIContainer.getGUIContainer().isMarkerVisible()) {
            for (GUIKnotenItem tempitem : GUIContainer.getGUIContainer().getKnotenItems()) {
                JSidebarButton templabel = tempitem.getImageLabel();
                templabel.setSelektiert(false);
                templabel.revalidate();
                templabel.updateUI();

                if (templabel.inBounds(posX, posY)) {
                    aktivesItem = tempitem;
                    aktiveslabel = tempitem.getImageLabel();
                }
            }
        }
    }

    public GUIKnotenItem getActiveItem() {
        return aktivesItem;
    }

    /*
     * method called in case of new item creation in GUIContainer, such that this creation process will be registered
     * and the according item is marked active
     */
    public void setNewItemActive(GUIKnotenItem item) {
        aktivesItem = item;
    }

    private void desktopAnzeigen(GUIKnotenItem aktivesItem) {
        GUIContainer.getGUIContainer().showDesktop(aktivesItem);
    }

    private void connectCableToSecondComponent(GUIKnotenItem tempitem) {
        GUIContainer c = GUIContainer.getGUIContainer();
        GUINetworkPanel draftpanel = c.getDesignpanel();
        NetzwerkInterface nic1, nic2;
        Port anschluss1 = null;
        Port anschluss2 = null;

        neuesKabel.getKabelpanel().setZiel2(tempitem);
        draftpanel.remove(kabelPanelVorschau);
        ziel2 = null;

        draftpanel.add(neuesKabel.getKabelpanel());
        neuesKabel.getKabelpanel().updateBounds();
        draftpanel.updateUI();
        c.getCableItems().add(neuesKabel);
        if (neuesKabel.getKabelpanel().getZiel1().getKnoten() instanceof Modem) {
            Modem vrOut = (Modem) neuesKabel.getKabelpanel().getZiel1().getKnoten();
            anschluss1 = vrOut.getErstenAnschluss();
        } else if (neuesKabel.getKabelpanel().getZiel1().getKnoten() instanceof Vermittlungsrechner) {
            Vermittlungsrechner r = (Vermittlungsrechner) neuesKabel.getKabelpanel().getZiel1().getKnoten();
            anschluss1 = r.holeFreienPort();
        } else if (neuesKabel.getKabelpanel().getZiel1().getKnoten() instanceof Switch) {
            Switch sw = (Switch) neuesKabel.getKabelpanel().getZiel1().getKnoten();
            anschluss1 = ((SwitchFirmware) sw.getSystemSoftware()).getKnoten().holeFreienPort();
        } else if (neuesKabel.getKabelpanel().getZiel1().getKnoten() instanceof InternetKnoten) {
            nic1 = (NetzwerkInterface) ((InternetKnoten) neuesKabel.getKabelpanel().getZiel1().getKnoten())
                    .getNetzwerkInterfaces().get(0);
            anschluss1 = nic1.getPort();
        }

        if (neuesKabel.getKabelpanel().getZiel2().getKnoten() instanceof Modem) {
            Modem vrOut = (Modem) neuesKabel.getKabelpanel().getZiel2().getKnoten();
            anschluss2 = vrOut.getErstenAnschluss();
        } else if (neuesKabel.getKabelpanel().getZiel2().getKnoten() instanceof Vermittlungsrechner) {
            Vermittlungsrechner r = (Vermittlungsrechner) neuesKabel.getKabelpanel().getZiel2().getKnoten();
            anschluss2 = r.holeFreienPort();
        } else if (neuesKabel.getKabelpanel().getZiel2().getKnoten() instanceof Switch) {
            Switch sw = (Switch) neuesKabel.getKabelpanel().getZiel2().getKnoten();
            anschluss2 = ((SwitchFirmware) sw.getSystemSoftware()).getKnoten().holeFreienPort();
        } else if (neuesKabel.getKabelpanel().getZiel2().getKnoten() instanceof InternetKnoten) {
            nic2 = (NetzwerkInterface) ((InternetKnoten) neuesKabel.getKabelpanel().getZiel2().getKnoten())
                    .getNetzwerkInterfaces().get(0);
            anschluss2 = nic2.getPort();
        }

        neuesKabel.setDasKabel(new Kabel());
        neuesKabel.getDasKabel().setAnschluesse(new Port[] { anschluss1, anschluss2 });

        resetAndHideCablePreview();
    }

    public void resetAndHideCablePreview() {
        resetCable();

        GUIContainer.getGUIContainer().getKabelvorschau().setVisible(false);

        if (kabelPanelVorschau != null)
            kabelPanelVorschau.setVisible(false);
    }

    private void resetCable() {
        neuesKabel = new GUIKabelItem();
        GUIContainer.getGUIContainer().getKabelvorschau()
                .setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/ziel1.png")));
        ziel2 = null;
    }

    public void resetAndShowCablePreview(int currentPosX, int currentPosY) {
        resetCable();

        JSidebarButton cablePreview = GUIContainer.getGUIContainer().getKabelvorschau();
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
    public void kontextMenueAktionsmodus(final GUIKnotenItem knotenItem, int posX, int posY) {
        if (knotenItem != null) {
            if (knotenItem.getKnoten() instanceof InternetKnoten) {

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
                if (knotenItem.getKnoten() instanceof Rechner || knotenItem.getKnoten() instanceof Notebook) {
                    popmen.add(pmDesktopAnzeigen);
                }

                InternetKnoten node = (InternetKnoten) knotenItem.getKnoten();
                for (NetzwerkInterface nic : node.getNetzwerkInterfaces()) {
                    JMenuItem pmDatenAustauschAnzeigen = new JMenuItem(messages.getString("guievents_msg4") + " ("
                            + nic.getIp() + ")");
                    pmDatenAustauschAnzeigen.setActionCommand("datenaustausch-" + nic.getMac());
                    pmDatenAustauschAnzeigen.addActionListener(al);

                    popmen.add(pmDatenAustauschAnzeigen);
                }

                knotenItem.getImageLabel().add(popmen);
                popmen.setVisible(true);
                popmen.show(knotenItem.getImageLabel(), posX, posY);
            }
        }
    }

    private void datenAustauschAnzeigen(GUIKnotenItem item, String macAddress) {
        InternetKnotenBetriebssystem bs;
        ExchangeDialog exchangeDialog = GUIContainer.getGUIContainer().getExchangeDialog();

        if (item.getKnoten() instanceof InternetKnoten) {
            bs = (InternetKnotenBetriebssystem) ((InternetKnoten) item.getKnoten()).getSystemSoftware();
            exchangeDialog.addTable(bs, macAddress);
            ((JDialog) exchangeDialog).setVisible(true);
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
    private void kontextMenueEntwurfsmodus(JSidebarButton templabel, int posX, int posY) {
        String textKabelEntfernen;

        updateAktivesItem(posX, posY);

        if (aktivesItem != null) {
            if (aktivesItem.getKnoten() instanceof Rechner || aktivesItem.getKnoten() instanceof Notebook) {
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
                        itemLoeschen(loeschlabel, loeschitem);
                    } else if (e.getActionCommand() == pmKabelEntfernen.getActionCommand()) {
                        kabelEntfernen();
                    } else if (e.getActionCommand() == pmShowConfig.getActionCommand()) {
                        GUIContainer.getGUIContainer().setProperty(aktivesItem);
                        GUIContainer.getGUIContainer().getProperty().maximieren();
                    }
                }
            };

            pmLoeschen.addActionListener(al);
            pmKabelEntfernen.addActionListener(al);
            pmShowConfig.addActionListener(al);

            popmen.add(pmShowConfig);
            popmen.add(pmKabelEntfernen);
            popmen.add(pmLoeschen);

            GUIContainer.getGUIContainer().getDesignpanel().add(popmen);
            popmen.setVisible(true);
            popmen.show(GUIContainer.getGUIContainer().getDesignpanel(), posX, posY);

            loeschlabel = templabel;
            loeschitem = aktivesItem;
        }
    }

    /**
     * context menu in case of clicking on single cable item --> used for deleting a single cable
     */
    private void contextMenuCable(final GUIKabelItem cable, int posX, int posY) {
        final JMenuItem pmRemoveCable = new JMenuItem(messages.getString("guievents_msg5"));
        pmRemoveCable.setActionCommand("removecable");

        JPopupMenu popmen = new JPopupMenu();
        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand() == pmRemoveCable.getActionCommand()) {
                    removeSingleCable(cable);
                }
            }
        };

        pmRemoveCable.addActionListener(al);
        popmen.add(pmRemoveCable);

        GUIContainer.getGUIContainer().getDesignpanel().add(popmen);
        popmen.setVisible(true);
        popmen.show(GUIContainer.getGUIContainer().getDesignpanel(), posX, posY);
    }

    /**
     * 
     * Löscht das durch loeschlabel angegebene Item NOTE: made public for using del key to delete items without local
     * context menu action (cf. JMainFrame)
     */
    public void itemLoeschen(JSidebarButton loeschlabel, GUIKnotenItem loeschitem) {
        loeschlabel.setVisible(false);
        GUIContainer.getGUIContainer().setProperty(null);
        ListIterator<GUIKabelItem> iteratorAlleKabel = GUIContainer.getGUIContainer().getCableItems().listIterator();
        GUIKabelItem kabel = new GUIKabelItem();
        LinkedList<GUIKabelItem> loeschKabel = new LinkedList<GUIKabelItem>();

        // Zu löschende Elemente werden in eine temporäre Liste gepackt
        while (iteratorAlleKabel.hasNext()) {
            kabel = (GUIKabelItem) iteratorAlleKabel.next();
            if (kabel.getKabelpanel().getZiel1().equals(loeschitem)
                    || kabel.getKabelpanel().getZiel2().equals(loeschitem)) {
                loeschKabel.add(kabel);
            }
        }

        // Temporäre Liste der zu löschenden Kabel wird iteriert und dabei
        // werden die Kabel aus der globalen Kabelliste gelöscht
        // und vom Panel entfernt
        ListIterator<GUIKabelItem> iteratorLoeschKabel = loeschKabel.listIterator();
        while (iteratorLoeschKabel.hasNext()) {
            kabel = iteratorLoeschKabel.next();

            this.removeSingleCable(kabel);
        }

        GUIContainer.getGUIContainer().getKnotenItems().remove(loeschitem);
        GUIContainer.getGUIContainer().getDesignpanel().remove(loeschlabel);
        GUIContainer.getGUIContainer().getDesignpanel().updateUI();
        GUIContainer.getGUIContainer().updateViewport();

    }

    // remove a single cable without using touching the connected node
    private void removeSingleCable(GUIKabelItem cable) {
        Main.debug.println("INVOKED filius.gui.GUIEvents, removeSingleCable(" + cable + ")");
        if (cable == null)
            return; // no cable to be removed (this variable should be set in
                    // contextMenuCable)

        filius.gui.netzwerksicht.JVermittlungsrechnerKonfiguration ziel1konf = null;
        filius.gui.netzwerksicht.JVermittlungsrechnerKonfiguration ziel2konf = null;

        if (JKonfiguration.getInstance(cable.getKabelpanel().getZiel1().getKnoten()) instanceof filius.gui.netzwerksicht.JVermittlungsrechnerKonfiguration) {
            // Main.debug.println("DEBUG filius.gui.GUIEvents, removeSingleCable: getZiel1 --> JVermittlungsrechnerKonfiguration");
            ziel1konf = ((filius.gui.netzwerksicht.JVermittlungsrechnerKonfiguration) JKonfiguration.getInstance(cable
                    .getKabelpanel().getZiel1().getKnoten()));
        }
        if (JKonfiguration.getInstance(cable.getKabelpanel().getZiel2().getKnoten()) instanceof filius.gui.netzwerksicht.JVermittlungsrechnerKonfiguration) {
            // Main.debug.println("DEBUG filius.gui.GUIEvents, removeSingleCable: getZiel1 --> JVermittlungsrechnerKonfiguration");
            ziel2konf = ((filius.gui.netzwerksicht.JVermittlungsrechnerKonfiguration) JKonfiguration.getInstance(cable
                    .getKabelpanel().getZiel2().getKnoten()));
        }
        cable.getDasKabel().anschluesseTrennen();
        GUIContainer.getGUIContainer().getCableItems().remove(cable);
        GUIContainer.getGUIContainer().getDesignpanel().remove(cable.getKabelpanel());
        GUIContainer.getGUIContainer().updateViewport();

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
        ListIterator<GUIKabelItem> iteratorAlleKabel = GUIContainer.getGUIContainer().getCableItems().listIterator();
        GUIKabelItem tempKabel = null;
        LinkedList<GUIKabelItem> loeschListe = new LinkedList<GUIKabelItem>();

        // Zu löschende Elemente werden in eine temporäre Liste gepackt
        while (iteratorAlleKabel.hasNext()) {
            tempKabel = (GUIKabelItem) iteratorAlleKabel.next();
            if (tempKabel.getKabelpanel().getZiel1().equals(loeschitem)) {
                loeschListe.add(tempKabel);
            }

            if (tempKabel.getKabelpanel().getZiel2().equals(loeschitem)) {
                loeschListe.add(tempKabel);
                ziel2 = loeschitem;
            }
        }

        // Temporäre Liste der zu löschenden Kabel wird iteriert und dabei
        // werden die Kabel aus der globalen Kabelliste gelöscht
        // und vom Panel entfernt
        ListIterator<GUIKabelItem> iteratorLoeschKabel = loeschListe.listIterator();
        while (iteratorLoeschKabel.hasNext()) {
            tempKabel = iteratorLoeschKabel.next();
            this.removeSingleCable(tempKabel);
        }

        GUIContainer.getGUIContainer().updateViewport();

    }

    public void satTabelleAnzeigen(final GUIKnotenItem aktivesItem) {
        Switch sw = (Switch) aktivesItem.getKnoten();

        JFrame jfSATTabelle = new JFrame(messages.getString("guievents_msg8") + " " + sw.holeAnzeigeName());
        jfSATTabelle.setBounds(100, 100, 320, 240);

        ImageIcon icon = new ImageIcon(getClass().getResource("/gfx/hardware/switch.png"));
        jfSATTabelle.setIconImage(icon.getImage());

        DefaultTableModel dtm = new DefaultTableModel(0, 2);
        for (Vector<String> zeile : ((SwitchFirmware) sw.getSystemSoftware()).holeSAT()) {
            dtm.addRow(zeile);
        }

        JTable tableSATNachrichten = new JTable(dtm);
        DefaultTableColumnModel dtcm = (DefaultTableColumnModel) tableSATNachrichten.getColumnModel();
        dtcm.getColumn(0).setHeaderValue(messages.getString("guievents_msg9"));
        dtcm.getColumn(1).setHeaderValue(messages.getString("guievents_msg10"));
        JScrollPane spSAT = new JScrollPane(tableSATNachrichten);
        jfSATTabelle.getContentPane().add(spSAT);
        jfSATTabelle.setVisible(true);

    }
}
