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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.MouseInputAdapter;

import filius.Main;
import filius.gui.JBackgroundPanel;
import filius.gui.JMainFrame;
import filius.gui.ValidateableTextField;
import filius.hardware.Hardware;
import filius.hardware.knoten.Host;
import filius.hardware.knoten.Modem;
import filius.hardware.knoten.Switch;
import filius.hardware.knoten.Vermittlungsrechner;
import filius.rahmenprogramm.EingabenUeberpruefung;

public class JKonfiguration extends JBackgroundPanel implements Observer {

    private static final long serialVersionUID = 1L;

    /**
     * Panel mit den spezifischen Attributen der Komponenten zur Anzeige und Konfiguration
     */
    private JBackgroundPanel attributPanel;

    private JLabel minimierenButton;

    private Hardware hardware;

    protected static HashMap<Hardware, JKonfiguration> instances = new HashMap<Hardware, JKonfiguration>();

    /** unveraenderbare Hoehe des Konfigurations-Panels (konfigPanel) */
    private static final int HOEHE = 250;

    protected JKonfiguration(Hardware hardware) {
        this.hardware = hardware;

        initKonfigPanel();
        if (hardware != null) {
            hardware.addObserver(this);
            initAttributPanel();
            updateAttribute();
        }

        minimieren();
    }

    public static JKonfiguration getInstance(Hardware hardware) {
        if (hardware == null) {
            return new JKonfiguration(null);
        }

        JKonfiguration newInstance;
        if (!instances.containsKey(hardware)) {
            if (hardware instanceof Host) {
                newInstance = new JHostKonfiguration(hardware);
            } else if (hardware instanceof Modem) {
                newInstance = new JModemKonfiguration(hardware);
            } else if (hardware instanceof Switch) {
                newInstance = new JSwitchKonfiguration(hardware);
            } else if (hardware instanceof Vermittlungsrechner) {
                newInstance = new JVermittlungsrechnerKonfiguration(hardware);
            } else {
                newInstance = new JKonfiguration(null);
            }
            instances.put(hardware, newInstance);
        }
        return instances.get(hardware);
    }

    public Hardware holeHardware() {
        return hardware;
    }

    /**
     * Zur Initialisierung des Konfigurations-Panels (konfigPanel), das ausgeblendet werden kann
     */
    private void initKonfigPanel() {
        Container c = JMainFrame.getJMainFrame().getContentPane();
        final JKonfiguration konfigPanel = this;

        this.setLayout(null);
        this.setBounds(0, 0, c.getWidth(), 100); // WAR 300
        this.setEnabled(false);
        this.setBackgroundImage("gfx/allgemein/konfigPanel_hg.png");
        this.setPreferredSize(new Dimension(100, HOEHE));
        this.setLayout(new BorderLayout());

        attributPanel = new JBackgroundPanel();
        attributPanel.setBackgroundImage("gfx/allgemein/konfigPanel_hg.png");
        attributPanel.setOpaque(false);
        attributPanel.setVisible(true);
        attributPanel.setBounds(0, 0, c.getWidth(), 300);
        this.add(new JScrollPane(attributPanel), BorderLayout.CENTER);

        minimierenButton = new JLabel(new ImageIcon(getClass().getResource("/gfx/allgemein/minimieren.png")));
        minimierenButton.setBounds(0, 0, minimierenButton.getIcon().getIconWidth(), minimierenButton.getIcon()
                .getIconHeight());
        minimierenButton.addMouseListener(new MouseInputAdapter() {
            public void mousePressed(MouseEvent e) {
                {
                    if (konfigPanel.getHeight() > 20) {
                        konfigPanel.minimieren();
                    } else {
                        konfigPanel.maximieren();
                    }

                }
            }
        });
        this.add(minimierenButton, BorderLayout.NORTH);
    }

    /**
     * Zur Initialisierung des Attribut-Panels. Hierin wird die in den Unterklassen implementierte Methode
     * initAttributEingabeBox() aufgerufen.
     * 
     */
    private void initAttributPanel() {
        Box hauptBox;

        attributPanel.removeAll();
        attributPanel.updateUI();
        attributPanel.setLayout(new BorderLayout());

        hauptBox = Box.createVerticalBox();
        hauptBox.add(Box.createHorizontalGlue());
        hauptBox.setOpaque(false);
        hauptBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        hauptBox.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        Box auxBox = Box.createVerticalBox();
        auxBox.add(Box.createHorizontalGlue());
        auxBox.setOpaque(false);
        auxBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        auxBox.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        initAttributEingabeBox(hauptBox, auxBox);

        attributPanel.add(hauptBox, BorderLayout.CENTER);
        attributPanel.add(auxBox, BorderLayout.LINE_END);
        attributPanel.updateUI();
        attributPanel.invalidate();
        attributPanel.validate();
    }

    // manually re-start initiation process (in case of significant changes)
    public void reInit() {
        initAttributPanel();
    }

    public void minimieren() {
        this.setPreferredSize(new Dimension(this.getWidth(), 20));
        minimierenButton.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/maximieren.png")));
        attributPanel.setVisible(false);
        this.updateUI();
    }

    // method for conducting specific updates (also in sub-classes)
    public void updateSettings() {}

    // method for doing postprocessing prior to being unselected (also in
    // sub-classes)
    public void doUnselectAction() {}

    public void maximieren() {
        this.setPreferredSize(new Dimension(this.getWidth(), HOEHE));
        minimierenButton.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/minimieren.png")));
        updateSettings();
        attributPanel.setVisible(true);
        this.updateUI();
    }

    public boolean isMaximiert() {
        return attributPanel.isVisible();
    }

    /**
     * Diese Methode wird vom JAendernButton aufgerufen und muss durch die Unterklassen fuer die jeweilige
     * Hardware-Komponente spezifisch implementiert werden.
     * 
     */
    public void aenderungenAnnehmen() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass()
                + " (JKonfiguration), aenderungenAnnehmen()");
    }

    /**
     * Mit dieser Methode werden die hardwarespezifischen Eingabe- und Anzeigekomponenten initialisiert.
     * 
     * @param rightBox
     *            TODO
     */
    protected void initAttributEingabeBox(Box box, Box rightBox) {}

    /**
     * Mit dieser Methode wird die Anzeige entsprechend der Attributwerte der Hardwarekomponente aktualisiert.
     * 
     */
    public void updateAttribute() {}

    /**
     * Funktion die waehrend der Eingabe ueberprueft ob die bisherige Eingabe einen korrekten Wert darstellt.
     * 
     * @author Johannes Bade & Thomas Gerding
     * @param pruefRegel
     * @param feld
     */
    public boolean ueberpruefen(Pattern pruefRegel, ValidateableTextField feld) {
        if (EingabenUeberpruefung.isGueltig(feld.getText(), pruefRegel)) {
            feld.setValid(true);
            return true;
        } else {
            feld.setValid(false);
            return false;
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        updateAttribute();
    }
}
