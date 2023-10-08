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
import javax.swing.ScrollPaneConstants;
import javax.swing.event.MouseInputAdapter;

@SuppressWarnings({ "deprecation", "serial" })
public class ControlPanel extends JBackgroundPanel implements Observer {
    /**
     * Panel mit den spezifischen Attributen der Komponenten zur Anzeige und Konfiguration
     */
    private JBackgroundPanel contentPanel;
    protected JScrollPane scrollPane;
    protected Box box;
    protected Box middleBox;
    protected Box rightBox;

    private JLabel openClose;

    /** unveraenderbare Hoehe des Konfigurations-Panels (konfigPanel) */
    private static final int SIZE = 250;

    public static final int VERTICAL = 0;
    public static final int HORIZONTAL = 1;

    private int mode;
    private int sizeMaximized;

    protected ControlPanel() {
        this(HORIZONTAL, SIZE);
    }

    protected ControlPanel(int mode, int size) {
        this.mode = mode;
        this.sizeMaximized = size;
        init();
        minimieren();
    }

    /**
     * Zur Initialisierung des Konfigurations-Panels (konfigPanel), das ausgeblendet werden kann
     */
    private void init() {
        setLayout(new BorderLayout());
        contentPanel = new JBackgroundPanel();
        contentPanel.setBackgroundImage("gfx/allgemein/konfigPanel_hg.png");
        scrollPane = new JScrollPane(contentPanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        if (mode == HORIZONTAL) {
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        } else {
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        }

        add(scrollPane, BorderLayout.CENTER);

        openClose = new JLabel(getIcon(false));
        openClose.setBounds(0, 0, openClose.getIcon().getIconWidth(), openClose.getIcon().getIconHeight());
        openClose.addMouseListener(new MouseInputAdapter() {
            public void mousePressed(MouseEvent e) {
                if (ControlPanel.this.isMaximiert()) {
                    ControlPanel.this.minimieren();
                } else {
                    ControlPanel.this.maximieren();
                }
            }
        });
        add(openClose, (mode == HORIZONTAL) ? BorderLayout.NORTH : BorderLayout.WEST);
    }

    protected ImageIcon getIcon(boolean open) {
        ImageIcon icon = null;
        if (open && mode == HORIZONTAL) {
            icon = new ImageIcon(getClass().getResource("/gfx/allgemein/minimieren.png"));
        } else if (open && mode == VERTICAL) {
            icon = new ImageIcon(getClass().getResource("/gfx/allgemein/min_to_right.png"));
        } else if (!open && mode == VERTICAL) {
            icon = new ImageIcon(getClass().getResource("/gfx/allgemein/max_to_left.png"));
        } else if (!open && mode == HORIZONTAL) {
            icon = new ImageIcon(getClass().getResource("/gfx/allgemein/maximieren.png"));
        }
        return icon;
    }

    /**
     * Zur Initialisierung des Attribut-Panels. Hierin wird die in den Unterklassen implementierte Methode
     * initContents() aufgerufen.
     */
    public void reInit() {
        contentPanel.removeAll();
        contentPanel.updateUI();
        contentPanel.setLayout(new BorderLayout());

        box = Box.createVerticalBox();
        box.setOpaque(false);
        box.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        box.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        Box auxBox = Box.createHorizontalBox();
        auxBox.add(Box.createVerticalGlue());
        auxBox.setOpaque(false);
        auxBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        middleBox = Box.createVerticalBox();
        middleBox.setOpaque(false);
        middleBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        middleBox.setAlignmentY(JComponent.TOP_ALIGNMENT);
        middleBox.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        auxBox.add(middleBox);

        rightBox = Box.createVerticalBox();
        rightBox.setOpaque(false);
        rightBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        rightBox.setAlignmentY(JComponent.TOP_ALIGNMENT);
        rightBox.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        auxBox.add(rightBox);

        initContents();

        middleBox.add(Box.createVerticalGlue());
        rightBox.add(Box.createVerticalGlue());

        contentPanel.add(box, BorderLayout.CENTER);
        contentPanel.add(auxBox, BorderLayout.EAST);
        contentPanel.updateUI();
        contentPanel.invalidate();
        contentPanel.validate();
    }

    public void minimieren() {
        if (mode == HORIZONTAL) {
            setPreferredSize(new Dimension(getWidth(), openClose.getHeight()));
        } else {
            setPreferredSize(new Dimension(openClose.getWidth(), getHeight()));
        }
        openClose.setIcon(getIcon(false));
        contentPanel.setVisible(false);
        this.updateUI();
    }

    /** method for conducting specific updates (also in sub-classes) */
    public void updateSettings() {}

    /** method for doing postprocessing prior to being unselected (also in sub-classes) */
    public void doUnselectAction() {}

    public void maximieren() {
        if (mode == HORIZONTAL) {
            setPreferredSize(new Dimension(getWidth(), sizeMaximized));
        } else {
            setPreferredSize(new Dimension(sizeMaximized, getHeight()));
            scrollPane.getViewport().setBounds(0, 0, sizeMaximized, Short.MAX_VALUE);
        }
        openClose.setIcon(getIcon(true));
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
    protected void initContents() {}

    /**
     * Mit dieser Methode wird die Anzeige aktualisiert.
     */
    public void updateAttribute() {}

    @Override
    public void update(Observable o, Object arg) {
        updateAttribute();
    }
}
