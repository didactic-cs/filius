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
/*
 * NewJFrame.java
 *
 * Created on 28. April 2006, 18:31
 */

package filius.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.MouseInfo;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import filius.Main;
import filius.gui.netzwerksicht.GUIKnotenItem;
import filius.rahmenprogramm.SzenarioVerwaltung;

public class JMainFrame extends javax.swing.JFrame implements WindowListener, Observer {

    private static final long serialVersionUID = 1L;
    private static JMainFrame frame = null;

    /** Creates new form NewJFrame */
    private JMainFrame() {
        this.addWindowListener(this);
        SzenarioVerwaltung.getInstance().addObserver(this);
        initComponents();

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getID() == KeyEvent.KEY_PRESSED && !(e.getSource() instanceof JTextField)) {
                    Main.debug.print("KEY dispatcher:\n" + "\tkey:'" + e.getKeyCode() + "'\n" + "\tmodifier: '"
                            + e.getModifiers() + "'\n" + "\tmodifierText: '"
                            + KeyEvent.getKeyModifiersText(e.getModifiers()) + "'\n" + "\tkeyChar: '" + e.getKeyChar()
                            + "'\n" + "\tsourceType: '" + e.getSource().getClass().getSimpleName() + "'\n");

                    /* ignore space bar pressing on buttons */
                    if ((e.getKeyChar() == KeyEvent.VK_SPACE)
                            && (e.getSource().getClass().getSimpleName() == "JButton")) {
                        return true; // no further action by 'clicking'
                                     // involuntarily via the space bar;
                                     // other shortcuts will be given for this
                    }
                    /* delete item on deletion key press */
                    if ((e.getKeyChar() == KeyEvent.VK_DELETE) && (frame.isFocused())
                            && GUIContainer.getGUIContainer().getActiveSite() == GUIMainMenu.MODUS_ENTWURF) {
                        if (GUIContainer.getGUIContainer().isMarkerVisible()) { // several
                            // items
                            // are
                            // selected
                            List<GUIKnotenItem> itemlist = GUIContainer.getGUIContainer().getKnotenItems();
                            JMarkerPanel auswahl = GUIContainer.getGUIContainer().getAuswahl();
                            JScrollPane scrollPane = GUIContainer.getGUIContainer().getScrollPane();
                            GUIKnotenItem tempitem;
                            int tx, ty, twidth, theight;
                            LinkedList<GUIKnotenItem> markedlist = new LinkedList<GUIKnotenItem>();
                            ListIterator<GUIKnotenItem> it = itemlist.listIterator();
                            while (it.hasNext()) {
                                tempitem = (GUIKnotenItem) it.next();
                                tx = tempitem.getImageLabel().getX();
                                twidth = tempitem.getImageLabel().getWidth();
                                ty = tempitem.getImageLabel().getY();
                                theight = tempitem.getImageLabel().getHeight();
                                if (tx > auswahl.getX() - GUIContainer.getGUIContainer().getAbstandLinks()
                                        + scrollPane.getHorizontalScrollBar().getValue()
                                        && tx + twidth < auswahl.getX() + auswahl.getWidth()
                                                - GUIContainer.getGUIContainer().getAbstandLinks()
                                                + scrollPane.getHorizontalScrollBar().getValue()
                                        && ty > auswahl.getY() - GUIContainer.getGUIContainer().getAbstandOben()
                                                + scrollPane.getVerticalScrollBar().getValue()
                                        && ty + theight < auswahl.getY() + auswahl.getHeight()
                                                - GUIContainer.getGUIContainer().getAbstandOben()
                                                + scrollPane.getVerticalScrollBar().getValue()) {
                                    markedlist.add(tempitem);
                                }
                            }
                            // Main.debug.println("selected elements for deletion (via key press):");
                            for (int i = 0; i < markedlist.size(); i++) {
                                // Main.debug.println("\t"+((GUIKnotenItem)
                                // markedlist.get(i)).getKnoten().getName());
                                GUIEvents.getGUIEvents().itemLoeschen(
                                        ((GUIKnotenItem) markedlist.get(i)).getImageLabel(),
                                        ((GUIKnotenItem) markedlist.get(i)));
                            }
                            auswahl.setVisible(false);
                            GUIContainer.getGUIContainer().getMarkierung().setVisible(false);
                            return true;
                        } else if (GUIEvents.getGUIEvents().getActiveItem() != null) { // single
                                                                                       // item
                                                                                       // active
                            // Main.debug.println("KeyDispatcher: delete item
                            // '"+(GUIEvents.getGUIEvents().getActiveItem()!=null
                            // ?
                            // GUIEvents.getGUIEvents().getActiveItem().getKnoten().getName()
                            // : "<null>"));
                            GUIEvents.getGUIEvents().itemLoeschen(
                                    GUIEvents.getGUIEvents().getActiveItem().getImageLabel(),
                                    GUIEvents.getGUIEvents().getActiveItem());
                            return true;
                        }
                        // else
                        // Main.debug.println("DEL pressed, but nothing selected");
                    } // del key
                    if (e.getModifiers() == 2) { // CTRL key pressed
                        // Main.debug.println("KeyDispatcher: CTRL-Key pressed, waiting for additional key!");
                        switch (e.getKeyCode()) {
                        case 78: // N (new)
                            // Main.debug.println("KeyDispatcher: CTRL+N recognised");
                            GUIContainer.getGUIContainer().getMenu().doClick("btNeu");
                            return true;
                        case 79: // O (open)
                            // Main.debug.println("KeyDispatcher: CTRL+O recognised");
                            GUIContainer.getGUIContainer().getMenu().doClick("btOeffnen");
                            return true;
                        case 83: // S (save file)
                            // Main.debug.println("KeyDispatcher: CTRL+S recognised");
                            GUIContainer.getGUIContainer().getMenu().doClick("btSpeichern");
                            return true;
                        case 68: // D (development mode)
                            // Main.debug.println("KeyDispatcher: CTRL+D recognised");
                            GUIContainer.getGUIContainer().getMenu().doClick("btEntwurfsmodus");
                            return true;
                        case 82: // R (run-time/simulation mode)
                            // Main.debug.println("KeyDispatcher: CTRL+R recognised");
                            GUIContainer.getGUIContainer().getMenu().doClick("btAktionsmodus");
                            return true;
                        case 37: // left arrow (slower simulation)
                            // Main.debug.println("KeyDispatcher: CTRL+left recognised");
                            GUIContainer.getGUIContainer().getMenu().changeSlider(-1);
                            return true;
                        case 39: // right arrow (faster simulation)
                            // Main.debug.println("KeyDispatcher: CTRL+right recognised");
                            GUIContainer.getGUIContainer().getMenu().changeSlider(1);
                            return true;
                        case 87: // W (wizard for new modules)
                            // Main.debug.println("KeyDispatcher: CTRL+W recognised");
                            GUIContainer.getGUIContainer().getMenu().doClick("btWizard");
                            return true;
                        case 72: // H (help)
                            // Main.debug.println("KeyDispatcher: CTRL+H recognised");
                            GUIContainer.getGUIContainer().getMenu().doClick("btHilfe");
                            return true;
                        case 65: // A (about dialog)
                            // Main.debug.println("KeyDispatcher: CTRL+A recognised");
                            GUIContainer.getGUIContainer().getMenu().doClick("btInfo");
                            return true;
                        }
                    } // CTRL key pressed, i.e., menu command
                    if (e.getModifiers() == 8) { // ALT key pressed; only makes
                                                 // sense for cables!
                        if (e.getKeyCode() == 49) { // key '1' (cable)
                            // Main.debug.println("KeyDispatcher: ALT+1 recognised");
                            switchCablePreview();
                            return true;
                        }
                    } // ALT key pressed, i.e., sidebar item selected
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        GUIEvents.getGUIEvents().resetAndHideCablePreview();
                    }
                } // KEY_PRESSED
                return false;
            }
        });
        aktualisiere();
    }

    public static JMainFrame getJMainFrame() {
        if (frame == null) {
            frame = new JMainFrame();
        }

        return frame;
    }

    public void addToContentPane(Component comp, Object constraints) {
        if (comp != null) {
            getContentPane().add(comp, constraints);
        }
    }

    private void initComponents() {

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1000, 700);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width / 2 - (getWidth() / 2), screenSize.height / 2 - (getHeight() / 2));

    }

    public void windowActivated(WindowEvent e) {}

    public void windowClosed(WindowEvent e) {}

    /**
     * 
     * Fragt ab, ob wirklich beendet werden soll, ausserdem wird der temp-Ordner geleert
     * 
     */
    public void windowClosing(WindowEvent e) {
        Main.beenden();
    }

    public void windowDeactivated(WindowEvent e) {}

    public void windowDeiconified(WindowEvent e) {}

    public void windowIconified(WindowEvent e) {}

    public void windowOpened(WindowEvent e) {}

    private void aktualisiere() {
        String dateipfad;
        int startIndex;

        dateipfad = SzenarioVerwaltung.getInstance().holePfad();
        if (dateipfad != null) {
            startIndex = dateipfad.length() - 80;
            if (startIndex > 0)
                dateipfad = dateipfad.substring(startIndex);
            if (SzenarioVerwaltung.getInstance().istGeaendert())
                dateipfad = dateipfad + "*";
            setTitle("FILIUS - " + dateipfad);
        } else {
            setTitle("FILIUS");
        }
    }

    public void update(Observable arg0, Object arg1) {
        aktualisiere();
    }

    private void switchCablePreview() {
        if (GUIContainer.getGUIContainer().getKabelvorschau().isVisible()) {
            GUIEvents.getGUIEvents().resetAndHideCablePreview();
        } else {
            int currentPosX = (int) (MouseInfo.getPointerInfo().getLocation().getX() - getLocationOnScreen().getX());
            int currentPosY = (int) (MouseInfo.getPointerInfo().getLocation().getY() - getLocationOnScreen().getY());
            GUIEvents.getGUIEvents().resetAndShowCablePreview(currentPosX, currentPosY);
        }
    }

    public void removeFromContentPane(Component comp) {
        if (comp != null) {
            this.getContentPane().remove(comp);
        }
    }
}
