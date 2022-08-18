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
package filius.gui.anwendungssicht;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;

import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.Information;
import filius.software.Anwendung;
import filius.software.system.InternetKnotenBetriebssystem;

@SuppressWarnings("serial")
public class GUIInstallationsDialog extends GUIApplicationWindow implements I18n {
    private JList<String> softwareInstalliert;
    private JList<String> softwareVerfuegbar;

    private DefaultListModel<String> lmVerfuegbar;
    private DefaultListModel<String> lmInstalliert;

    private List<Map<String, String>> programme = null;

    public GUIInstallationsDialog(GUIDesktopPanel dp) {
        super(dp);

        programme = Information.getInformation().ladeProgrammListe();

        setTitle(messages.getString("installationsdialog_msg1"));
        setIcon("/gfx/desktop/icon_softwareinstallation.png");

        /* Komponenten dem Panel hinzufügen */
        Box gesamtBox = Box.createVerticalBox();

        Box listenBox = Box.createHorizontalBox();
        listenBox.add(Box.createHorizontalStrut(10));
        listenBox.add(createInstalledAppsBox());
        listenBox.add(Box.createHorizontalGlue());
        listenBox.add(createSelectionButtonBox());
        listenBox.add(createAvailableAppsBox());
        listenBox.add(Box.createHorizontalStrut(10));

        gesamtBox.add(Box.createVerticalStrut(10));
        gesamtBox.add(listenBox);
        gesamtBox.add(Box.createVerticalStrut(10));
        gesamtBox.add(createLowerButtonBox());
        gesamtBox.add(Box.createVerticalStrut(10));

        add(gesamtBox, BorderLayout.CENTER);
    }

    private Box createInstalledAppsBox() {
        Box wrapperInstBox = Box.createVerticalBox();

        wrapperInstBox.add(new JLabel(messages.getString("installationsdialog_msg3")));
        wrapperInstBox.add(Box.createVerticalStrut(10));

        lmInstalliert = new DefaultListModel<>();
        Anwendung[] anwendungen = desktop.getBetriebssystem().holeArrayInstallierteSoftware();
        for (int i = 0; i < anwendungen.length; i++) {
            if (anwendungen[i] != null) {
                lmInstalliert.addElement(anwendungen[i].holeAnwendungsName());
            }
        }
        softwareInstalliert = new JList<>();
        softwareInstalliert.setModel(lmInstalliert);
        softwareInstalliert.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    GUIInstallationsDialog.this.entfernen();
                }
            }
        });

        JScrollPane scrollAnwendungInstallieren = new JScrollPane(softwareInstalliert);
        scrollAnwendungInstallieren.setPreferredSize(new Dimension(170, 200));
        wrapperInstBox.add(scrollAnwendungInstallieren);
        return wrapperInstBox;
    }

    private Box createAvailableAppsBox() {
        Box wrapperAvailBox = Box.createVerticalBox();
        wrapperAvailBox.add(new JLabel(messages.getString("installationsdialog_msg4")));
        wrapperAvailBox.add(Box.createVerticalStrut(10));

        lmVerfuegbar = new DefaultListModel<>();
        if (programme != null) {
            for (Map<String, String> programmInfo : programme) {
                String awKlasse = (String) programmInfo.get("Klasse");

                if (desktop.getBetriebssystem().holeSoftware(awKlasse) == null) {
                    lmVerfuegbar.addElement(programmInfo.get("Anwendung"));
                }
            }
        }

        softwareVerfuegbar = new JList<>();
        softwareVerfuegbar.setModel(lmVerfuegbar);
        softwareVerfuegbar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    GUIInstallationsDialog.this.hinzufuegen();
                }
            }
        });
        JScrollPane scrollAnwendungVerfuegbar = new JScrollPane(softwareVerfuegbar);
        scrollAnwendungVerfuegbar.setPreferredSize(new Dimension(170, 200));
        wrapperAvailBox.add(scrollAnwendungVerfuegbar);
        return wrapperAvailBox;
    }

    private Box createSelectionButtonBox() {
        Box topButtonBox = Box.createVerticalBox();
        JButton addButton = new JButton(new ImageIcon(getClass().getResource("/gfx/allgemein/pfeil_links.png")));
        addButton.setMargin(new Insets(2, 2, 2, 2));
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                hinzufuegen();
            }
        });
        topButtonBox.add(addButton);
        topButtonBox.add(Box.createVerticalStrut(10));

        JButton removeButton = new JButton(new ImageIcon(getClass().getResource("/gfx/allgemein/pfeil_rechts.png")));
        removeButton.setMargin(new Insets(2, 2, 2, 2));
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                entfernen();
            }
        });
        topButtonBox.add(removeButton);
        return topButtonBox;
    }

    private Box createLowerButtonBox() {
        Box bottomButtonBox = Box.createVerticalBox();
        JButton confirmButton = new JButton(messages.getString("installationsdialog_msg2"));
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                aenderungenSpeichern();
                setVisible(false);
            }
        });
        confirmButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        bottomButtonBox.add(confirmButton);
        return bottomButtonBox;
    }

    private void hinzufuegen() {
        Vector<String> vLoeschen = new Vector<String>();
        int[] selektiertIndices = softwareVerfuegbar.getSelectedIndices();

        for (int i : selektiertIndices) {
            lmInstalliert.addElement(lmVerfuegbar.get(i));
            vLoeschen.add((String) lmVerfuegbar.get(i));
        }

        for (Enumeration<String> e = vLoeschen.elements(); e.hasMoreElements();) {
            String oZuLoeschen = e.nextElement();
            lmVerfuegbar.removeElement(oZuLoeschen);
        }
    }

    private void entfernen() {
        int[] selektiertIndices = softwareInstalliert.getSelectedIndices();
        Vector<String> hinzu = new Vector<String>();

        for (int i : selektiertIndices) {
            lmVerfuegbar.addElement(lmInstalliert.getElementAt(i));
            hinzu.add((String) lmInstalliert.getElementAt(i));
        }

        for (Enumeration<String> e = hinzu.elements(); e.hasMoreElements();) {
            String hinzuObjekt = e.nextElement();
            lmInstalliert.removeElement(hinzuObjekt);
        }
    }

    private void aenderungenSpeichern() {
        InternetKnotenBetriebssystem bs = desktop.getBetriebssystem();
        Anwendung anwendung;

        for (Map<String, String> appInfo : programme) {
            for (int i = 0; i < lmInstalliert.getSize(); i++) {
                if (lmInstalliert.getElementAt(i).equals(appInfo.get("Anwendung"))
                        && bs.holeSoftware(appInfo.get("Klasse").toString()) == null) {
                    bs.installAppIfAvailable(appInfo.get("Klasse").toString());

                    anwendung = bs.holeSoftware(appInfo.get("Klasse").toString());
                    anwendung.starten();
                }
            }

            for (int i = 0; i < lmVerfuegbar.getSize(); i++) {
                if (lmVerfuegbar.getElementAt(i).equals(appInfo.get("Anwendung"))) {
                    anwendung = bs.holeSoftware(appInfo.get("Klasse").toString());
                    if (anwendung != null) {
                        anwendung.beenden();
                        bs.entferneSoftware(appInfo.get("Klasse").toString());
                    }
                }
            }
        }
        desktop.updateAppPane();
    }

    @Override
    public void update(Observable arg0, Object arg1) {}
}
