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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.event.MouseInputAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("deprecation")
public class ControlPanel extends JBackgroundPanel implements Observer {
    private static Logger LOG = LoggerFactory.getLogger(ControlPanel.class);
    /**
     * Panel mit den spezifischen Attributen der Komponenten zur Anzeige und Konfiguration
     */
    private JBackgroundPanel contentPanel;

    private JLabel minimierenButton;

    /** unveraenderbare Hoehe des Konfigurations-Panels (konfigPanel) */
    private static final int HOEHE = 250;

    protected ControlPanel() {
        init();
        minimieren();
    }

    /**
     * Zur Initialisierung des Konfigurations-Panels (konfigPanel), das ausgeblendet werden kann
     */
    private void init() {
        Container c = JMainFrame.getJMainFrame().getContentPane();

        this.setLayout(null);
        this.setBounds(0, 0, c.getWidth(), 100); // WAR 300
        this.setEnabled(false);
        this.setBackgroundImage("gfx/allgemein/konfigPanel_hg.png");
        this.setPreferredSize(new Dimension(100, HOEHE));
        this.setLayout(new BorderLayout());

        contentPanel = new JBackgroundPanel();
        contentPanel.setBackgroundImage("gfx/allgemein/konfigPanel_hg.png");
        contentPanel.setOpaque(false);
        contentPanel.setVisible(true);
        contentPanel.setBounds(0, 0, c.getWidth(), 300);
        this.add(new JScrollPane(contentPanel), BorderLayout.CENTER);

        minimierenButton = new JLabel(new ImageIcon(getClass().getResource("/gfx/allgemein/minimieren.png")));
        minimierenButton.setBounds(0, 0, minimierenButton.getIcon().getIconWidth(),
                minimierenButton.getIcon().getIconHeight());
        minimierenButton.addMouseListener(new MouseInputAdapter() {
            public void mousePressed(MouseEvent e) {
                if (ControlPanel.this.getHeight() > 20) {
                    ControlPanel.this.minimieren();
                } else {
                    ControlPanel.this.maximieren();
                }
            }
        });
        this.add(minimierenButton, BorderLayout.NORTH);
    }

    /**
     * Zur Initialisierung des Attribut-Panels. Hierin wird die in den Unterklassen implementierte Methode
     * initContents() aufgerufen.
     */
    public void reInit() {
        Box hauptBox;

        contentPanel.removeAll();
        contentPanel.updateUI();
        contentPanel.setLayout(new BorderLayout());

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

        initContents(hauptBox, auxBox);

        contentPanel.add(hauptBox, BorderLayout.CENTER);
        contentPanel.add(auxBox, BorderLayout.LINE_END);
        contentPanel.updateUI();
        contentPanel.invalidate();
        contentPanel.validate();
    }

    public void minimieren() {
        this.setPreferredSize(new Dimension(this.getWidth(), 20));
        minimierenButton.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/maximieren.png")));
        contentPanel.setVisible(false);
        this.updateUI();
    }

    /** method for conducting specific updates (also in sub-classes) */
    public void updateSettings() {}

    /** method for doing postprocessing prior to being unselected (also in sub-classes) */
    public void doUnselectAction() {}

    public void maximieren() {
        this.setPreferredSize(new Dimension(this.getWidth(), HOEHE));
        minimierenButton.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/minimieren.png")));
        updateSettings();
        contentPanel.setVisible(true);
        this.updateUI();
    }

    public boolean isMaximiert() {
        return contentPanel.isVisible();
    }

    /**
     * Mit dieser Methode werden die hardwarespezifischen Eingabe- und Anzeigekomponenten initialisiert.
     */
    protected void initContents(Box box, Box rightBox) {}

    /**
     * Mit dieser Methode wird die Anzeige entsprechend der Attributwerte der Hardwarekomponente aktualisiert.
     */
    public void updateAttribute() {}

    @Override
    public void update(Observable o, Object arg) {
        updateAttribute();
    }
}
