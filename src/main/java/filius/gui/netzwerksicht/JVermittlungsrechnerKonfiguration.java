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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.ListIterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang3.StringUtils;

import filius.Main;
import filius.gui.GUIContainer;
import filius.gui.JMainFrame;
import filius.gui.ValidateableTextField;
import filius.hardware.Hardware;
import filius.hardware.Kabel;
import filius.hardware.NetzwerkInterface;
import filius.hardware.Port;
import filius.hardware.Verbindung;
import filius.hardware.knoten.InternetKnoten;
import filius.hardware.knoten.Knoten;
import filius.hardware.knoten.LokalerKnoten;
import filius.hardware.knoten.Vermittlungsrechner;
import filius.rahmenprogramm.I18n;
import filius.software.firewall.Firewall;
import filius.software.system.VermittlungsrechnerBetriebssystem;
import filius.software.vermittlungsschicht.IPAddress;

public class JVermittlungsrechnerKonfiguration extends JKonfiguration implements I18n {

    private static final long serialVersionUID = 1L;

    private JDialog changeBasicSettingsDialog;

    private JTextField name;

    private ValidateableTextField[] ipAdressen;
    private ValidateableTextField[] netzmasken;
    private ValidateableTextField[] macAdressen;

    private ValidateableTextField gateway;
    private JCheckBox rip;

    private JLabel[] verbundeneKomponente;

    private JWeiterleitungsTabelle weiterleitungstabelle;

    private JCheckBox alleEintraegeAnzeigen;

    private JTabbedPane tpNetzwerkKarten;

    private Kabel highlightedCable = null;

    protected JVermittlungsrechnerKonfiguration(Hardware hardware) {
        super(hardware);
    }

    public void aenderungenAnnehmen() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass()
                + " (JVermittlungsrechnerKonfiguration), aenderungenAnnehmen()");
        ListIterator it;
        Vermittlungsrechner vRechner;
        NetzwerkInterface nic;
        VermittlungsrechnerBetriebssystem bs;

        vRechner = (Vermittlungsrechner) holeHardware();
        bs = (VermittlungsrechnerBetriebssystem) vRechner.getSystemSoftware();

        vRechner.setName(name.getText());
        bs.setStandardGateway(gateway.getText());
        bs.setRipEnabled(rip.isSelected());

        it = vRechner.getNetzwerkInterfaces().listIterator();
        for (int i = 0; it.hasNext(); i++) {
            nic = (NetzwerkInterface) it.next();

            if (IPAddress.verifyAddress(ipAdressen[i].getText())) {
                ipAdressen[i].setValid(true);
                nic.setIp(ipAdressen[i].getText());
            } else {
                ipAdressen[i].setValid(false);
                Main.debug.println("ERROR (" + this.hashCode() + "): IP-Adresse ungueltig " + ipAdressen[i].getText());
            }

            if (IPAddress.verifyNetmaskDefinition(netzmasken[i].getText())) {
                nic.setSubnetzMaske(netzmasken[i].getText());
            } else {
                Main.debug.println("ERROR (" + this.hashCode() + "): Netzmaske ungueltig " + netzmasken[i].getText());
            }
        }

        GUIContainer.getGUIContainer().updateViewport();
        updateAttribute();
    }

    private void showFirewallDialog() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass()
                + " (JVermittlungsrechnerKonfiguration), firewallDialogAnzeigen()");

        Firewall firewall = ((VermittlungsrechnerBetriebssystem) ((Vermittlungsrechner) holeHardware())
                .getSystemSoftware()).holeFirewall();

        JFirewallDialog firewallDialog = new JFirewallDialog(firewall, JMainFrame.getJMainFrame());
        firewallDialog.setBounds(100, 100, 850, 340);
        firewallDialog.setName(messages.getString("jvermittlungsrechnerkonfiguration_msg1"));

        firewallDialog.updateRuleTable();
        firewallDialog.setVisible(true);
    }

    protected void initAttributEingabeBox(Box box, Box rightBox) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass()
                + " (JVermittlungsrechnerKonfiguration), initAttributEingabeBox(" + box + ")");
        Vermittlungsrechner vRechner;
        NetzwerkInterface tempNic;
        Knoten tempKnoten;
        List<NetzwerkInterface> nicListe;
        ListIterator it;
        Box boxNetzwerkKarten;
        Box vBox;
        Box boxNic;
        Box boxIpAdresse;
        Box boxSubnetz;
        Box boxMacAdresse;
        Box boxKomponente;
        KeyAdapter ipAdresseKeyAdapter;
        KeyAdapter netzmaskeKeyAdapter;
        FocusListener focusListener;
        ActionListener actionListener;
        JButton btFirewall;
        JButton btNeuerEintrag;
        JButton btEintragLoeschen;
        Box boxWeiterleitung;
        JButton btTabellenDialog;
        JButton changeBasicSettingsButton;

        JLabel tempLabel;
        Box tempBox;

        actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                aenderungenAnnehmen();
            }
        };

        focusListener = new FocusListener() {

            public void focusGained(FocusEvent arg0) {}

            public void focusLost(FocusEvent arg0) {
                aenderungenAnnehmen();
            }

        };

        this.addFocusListener(focusListener);

        boxNetzwerkKarten = Box.createVerticalBox();
        boxNetzwerkKarten.setPreferredSize(new Dimension(440, 150));
        boxNetzwerkKarten.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        boxNetzwerkKarten.setOpaque(false);

        tpNetzwerkKarten = new JTabbedPane();
        tpNetzwerkKarten.setOpaque(false);
        boxNetzwerkKarten.add(tpNetzwerkKarten);

        ipAdresseKeyAdapter = new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                ValidateableTextField tfQuelle = (ValidateableTextField) e.getSource();
                tfQuelle.setValid(IPAddress.verifyAddress(tfQuelle.getText()));
            }
        };

        netzmaskeKeyAdapter = new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                ValidateableTextField tfQuelle = (ValidateableTextField) e.getSource();
                tfQuelle.setValid(IPAddress.verifyNetmaskDefinition(tfQuelle.getText()));
            }
        };

        vBox = Box.createVerticalBox();

        // Attribut Name
        tempBox = Box.createHorizontalBox();
        tempBox.setMaximumSize(new Dimension(400, 40));

        tempLabel = new JLabel(messages.getString("jvermittlungsrechnerkonfiguration_msg2"));
        tempLabel.setPreferredSize(new Dimension(140, 20));
        tempLabel.setVisible(true);
        tempLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        tempBox.add(tempLabel);

        name = new JTextField(messages.getString("jvermittlungsrechnerkonfiguration_msg3"));
        name.setPreferredSize(new Dimension(160, 20));
        name.addActionListener(actionListener);
        name.addFocusListener(focusListener);
        tempBox.add(name);

        vBox.add(tempBox);
        vBox.add(Box.createVerticalStrut(5));

        // Attribut Gateway
        tempBox = Box.createHorizontalBox();
        tempBox.setMaximumSize(new Dimension(400, 40));

        tempLabel = new JLabel(messages.getString("jvermittlungsrechnerkonfiguration_msg9"));
        tempLabel.setPreferredSize(new Dimension(140, 20));
        tempLabel.setVisible(true);
        tempLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        tempBox.add(tempLabel);

        gateway = new ValidateableTextField();
        gateway.setPreferredSize(new Dimension(160, 20));
        gateway.addActionListener(actionListener);
        gateway.addFocusListener(focusListener);
        gateway.addKeyListener(ipAdresseKeyAdapter);
        tempBox.add(gateway);

        vBox.add(tempBox);
        vBox.add(Box.createVerticalStrut(5));

        // Attribut rip
        tempBox = Box.createHorizontalBox();
        tempBox.setMaximumSize(new Dimension(400, 40));

        tempLabel = new JLabel(messages.getString("jvermittlungsrechnerkonfiguration_msg26"));
        tempLabel.setPreferredSize(new Dimension(140, 20));
        tempLabel.setVisible(true);
        tempLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        tempBox.add(tempLabel);

        tempBox.add(Box.createHorizontalStrut(20));

        rip = new JCheckBox();
        rip.setPreferredSize(new Dimension(160, 20));
        rip.addActionListener(actionListener);
        rip.addFocusListener(focusListener);
        rip.setOpaque(false);
        tempBox.add(rip);

        vBox.add(tempBox);
        vBox.add(Box.createVerticalStrut(5));

        tempBox = Box.createHorizontalBox();
        tempBox.setMaximumSize(new Dimension(400, 40));
        btFirewall = new JButton(messages.getString("jvermittlungsrechnerkonfiguration_msg4"));

        btFirewall.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showFirewallDialog();
            }
        });
        tempBox.add(btFirewall);

        tempBox.add(Box.createHorizontalStrut(20));

        changeBasicSettingsButton = new JButton(messages.getString("jvermittlungsrechnerkonfiguration_msg23"));
        changeBasicSettingsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showConnectionsDialog();
            }
        });
        tempBox.add(changeBasicSettingsButton);

        vBox.add(tempBox);

        // NIC tabs
        tpNetzwerkKarten.addTab(messages.getString("jvermittlungsrechnerkonfiguration_msg17"), vBox);

        vRechner = (Vermittlungsrechner) holeHardware();
        nicListe = vRechner.getNetzwerkInterfaces();
        ipAdressen = new ValidateableTextField[nicListe.size()];
        netzmasken = new ValidateableTextField[nicListe.size()];
        macAdressen = new ValidateableTextField[nicListe.size()];
        verbundeneKomponente = new JLabel[nicListe.size()];

        it = nicListe.listIterator();

        for (int i = 0; it.hasNext(); i++) {
            tempNic = (NetzwerkInterface) it.next();

            boxNic = Box.createVerticalBox();

            boxKomponente = Box.createHorizontalBox();
            boxKomponente.setMaximumSize(new Dimension(400, 40));

            tempKnoten = holeVerbundeneKomponente(tempNic);
            if (tempKnoten == null)
                verbundeneKomponente[i] = new JLabel(messages.getString("jvermittlungsrechnerkonfiguration_msg5"));
            else
                verbundeneKomponente[i] = new JLabel(messages.getString("jvermittlungsrechnerkonfiguration_msg6") + " "
                        + tempKnoten.holeAnzeigeName());
            verbundeneKomponente[i].setPreferredSize(new Dimension(400, 10));
            boxKomponente.add(verbundeneKomponente[i]);

            // show IP address (editable)
            boxIpAdresse = Box.createHorizontalBox();
            boxIpAdresse.setMaximumSize(new Dimension(400, 40));
            tempLabel = new JLabel(messages.getString("jvermittlungsrechnerkonfiguration_msg7"));
            tempLabel.setPreferredSize(new Dimension(120, 10));
            boxIpAdresse.add(tempLabel);

            ipAdressen[i] = new ValidateableTextField(tempNic.getIp());
            boxIpAdresse.add(ipAdressen[i]);

            // show netmask (editable)
            boxSubnetz = Box.createHorizontalBox();
            boxSubnetz.setMaximumSize(new Dimension(400, 40));
            tempLabel = new JLabel(messages.getString("jvermittlungsrechnerkonfiguration_msg8"));
            tempLabel.setPreferredSize(new Dimension(120, 10));
            boxSubnetz.add(tempLabel);

            netzmasken[i] = new ValidateableTextField(tempNic.getSubnetzMaske());
            boxSubnetz.add(netzmasken[i]);

            // show MAC address (not editable)
            boxMacAdresse = Box.createHorizontalBox();
            boxMacAdresse.setMaximumSize(new Dimension(400, 40));
            tempLabel = new JLabel(messages.getString("jvermittlungsrechnerkonfiguration_msg18"));
            tempLabel.setPreferredSize(new Dimension(120, 10));
            boxMacAdresse.add(tempLabel);

            macAdressen[i] = new ValidateableTextField(tempNic.getMac());
            macAdressen[i].setEnabled(false);
            boxMacAdresse.add(macAdressen[i]);

            boxNic.add(boxKomponente);
            boxNic.add(Box.createVerticalStrut(5));
            boxNic.add(boxIpAdresse);
            boxNic.add(Box.createVerticalStrut(5));
            boxNic.add(boxSubnetz);
            boxNic.add(Box.createVerticalStrut(5));
            boxNic.add(boxMacAdresse);

            if (tempKnoten == null) {
                tpNetzwerkKarten.addTab(messages.getString("jvermittlungsrechnerkonfiguration_msg10") + (i + 1),
                        new ImageIcon(getClass().getResource("/gfx/allgemein/conn_fail.png")), boxNic);
            } else {
                tpNetzwerkKarten.addTab(messages.getString("jvermittlungsrechnerkonfiguration_msg10") + (i + 1),
                        new ImageIcon(getClass().getResource("/gfx/allgemein/conn_ok.png")), boxNic);
            }
        }

        for (int i = 0; i < ipAdressen.length; i++) {
            ipAdressen[i].addKeyListener(ipAdresseKeyAdapter);
            ipAdressen[i].addActionListener(actionListener);
            ipAdressen[i].addFocusListener(focusListener);

            netzmasken[i].addKeyListener(netzmaskeKeyAdapter);
            netzmasken[i].addActionListener(actionListener);
            netzmasken[i].addFocusListener(focusListener);
        }
        tpNetzwerkKarten.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent arg0) {
                // Main.debug.println("JVermittlungsrechnerKonfiguration, ChangeListener, stateChanged("+arg0+")");
                JTabbedPane pane = (JTabbedPane) arg0.getSource();
                // Get current tab
                int sel = pane.getSelectedIndex();
                // Main.debug.println("\tsource: "+pane+", index="+sel+", getComponentCount="+pane.getComponentCount());
                if (highlightedCable != null) {
                    highlightedCable.setAktiv(false);
                }
                if (sel > 0 && sel < pane.getComponentCount() - 1) {
                    Verbindung conn = ((NetzwerkInterface) ((Vermittlungsrechner) holeHardware())
                            .getNetzwerkInterfaces().get(sel - 1)).getPort().getVerbindung();
                    if (conn != null) {
                        conn.setAktiv(true);
                        highlightedCable = (Kabel) conn;
                    }
                }

                weiterleitungstabelle.aenderungenAnnehmen();
            }

        });

        /* Weiterleitungs-Tabelle Router */
        weiterleitungstabelle = new JWeiterleitungsTabelle(this);

        JScrollPane spWeiterleitung = new JScrollPane(weiterleitungstabelle);
        spWeiterleitung.setPreferredSize(new Dimension(300, 120));
        spWeiterleitung.addFocusListener(focusListener);

        tempBox = Box.createHorizontalBox();
        tempBox.setOpaque(false);
        tempBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        tempBox.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        alleEintraegeAnzeigen = new JCheckBox();
        alleEintraegeAnzeigen.setSelected(true);
        alleEintraegeAnzeigen.setText(messages.getString("jvermittlungsrechnerkonfiguration_msg11"));
        alleEintraegeAnzeigen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                weiterleitungstabelle.setzeAlleEintraegeAnzeigen(alleEintraegeAnzeigen.isSelected());
                weiterleitungstabelle.updateAttribute();
            }
        });
        tempBox.add(alleEintraegeAnzeigen, BorderLayout.NORTH);

        btNeuerEintrag = new JButton(messages.getString("jvermittlungsrechnerkonfiguration_msg12"));
        btNeuerEintrag.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                weiterleitungstabelle.neuerEintrag();
            }
        });
        tempBox.add(Box.createHorizontalStrut(50));
        tempBox.add(btNeuerEintrag);

        btEintragLoeschen = new JButton(messages.getString("jvermittlungsrechnerkonfiguration_msg13"));
        btEintragLoeschen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                weiterleitungstabelle.markiertenEintragLoeschen();
            }
        });
        tempBox.add(Box.createHorizontalStrut(5));
        tempBox.add(btEintragLoeschen);

        btTabellenDialog = new JButton(messages.getString("jvermittlungsrechnerkonfiguration_msg14"));
        btTabellenDialog.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JDialog tabellenDialog;
                JScrollPane scrollPane;
                JWeiterleitungsTabelle tabelle;

                tabellenDialog = new JDialog(filius.gui.JMainFrame.getJMainFrame(), true);
                tabellenDialog.setTitle(messages.getString("jvermittlungsrechnerkonfiguration_msg15"));
                tabellenDialog.setSize(600, 400);

                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                tabellenDialog.setLocation(screenSize.width / 2 - 300, screenSize.height / 2 - 200);
                tabelle = new JWeiterleitungsTabelle(getKonfiguration());
                tabelle.updateAttribute();
                scrollPane = new JScrollPane(tabelle);
                tabellenDialog.getContentPane().add(scrollPane);

                tabellenDialog.setVisible(true);
                weiterleitungstabelle.updateAttribute();
            }
        });
        tempBox.add(Box.createHorizontalStrut(50));
        tempBox.add(btTabellenDialog);

        boxWeiterleitung = Box.createVerticalBox();
        boxWeiterleitung.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        boxWeiterleitung.add(tempBox);
        boxWeiterleitung.add(spWeiterleitung);

        tpNetzwerkKarten.addTab(messages.getString("jvermittlungsrechnerkonfiguration_msg15"), boxWeiterleitung);

        box.add(boxNetzwerkKarten);

        updateAttribute();

    }

    private void showConnectionsDialog() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass()
                + " (JVermittlungsrechnerKonfiguration), showBasicSettingsDialog()");
        GUIContainer.getGUIContainer().getProperty().minimieren();
        // basic dialog creation and settings
        changeBasicSettingsDialog = new JConnectionsDialog(filius.gui.JMainFrame.getJMainFrame(),
                (Vermittlungsrechner) holeHardware());
        changeBasicSettingsDialog.setTitle(messages.getString("jvermittlungsrechnerkonfiguration_msg23"));

        // positioning and size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        changeBasicSettingsDialog.setLocation(screenSize.width / 2 - 300, screenSize.height / 2 - 200);

        changeBasicSettingsDialog.setSize(750, 530);
        changeBasicSettingsDialog.setResizable(false);

        changeBasicSettingsDialog.setVisible(true);
    }

    public void doUnselectAction() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass()
                + " (JVermittlungsrechnerKonfiguration), doUnselectAction()");
        if (highlightedCable != null) {
            highlightedCable.setAktiv(false);
            highlightedCable = null;
            this.tpNetzwerkKarten.setSelectedIndex(0);
        }
    }

    // method to highlight marked cable; called from GUIMainMenu in case of
    // switching back to development view
    public void highlightConnCable() {
        if (highlightedCable != null) {
            highlightedCable.setAktiv(true);
        }
    }

    public JVermittlungsrechnerKonfiguration getKonfiguration() {
        return this;
    }

    public void updateAttribute() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass()
                + " (JVermittlungsrechnerKonfiguration), updateAttribute()");
        ListIterator it;
        Vermittlungsrechner vRechner;
        VermittlungsrechnerBetriebssystem bs;
        NetzwerkInterface nic;
        Knoten tempKnoten;

        vRechner = (Vermittlungsrechner) holeHardware();
        bs = (VermittlungsrechnerBetriebssystem) vRechner.getSystemSoftware();

        name.setText(vRechner.holeAnzeigeName());
        gateway.setText(bs.getStandardGateway());
        rip.setSelected(bs.isRipEnabled());

        tpNetzwerkKarten.setEnabledAt(tpNetzwerkKarten.getTabCount() - 1, !bs.isRipEnabled());

        it = vRechner.getNetzwerkInterfaces().listIterator();
        for (int i = 0; it.hasNext() && i < ipAdressen.length; i++) {
            nic = (NetzwerkInterface) it.next();
            ipAdressen[i].setText(nic.getIp());
            netzmasken[i].setText(nic.getSubnetzMaske());

            tempKnoten = holeVerbundeneKomponente(nic);
            if (tempKnoten == null)
                verbundeneKomponente[i].setText(messages.getString("jvermittlungsrechnerkonfiguration_msg16"));
            else
                verbundeneKomponente[i].setText(messages.getString("jvermittlungsrechnerkonfiguration_msg6") + " "
                        + tempKnoten.holeAnzeigeName());
        }

        vRechner = (Vermittlungsrechner) holeHardware();
        List<NetzwerkInterface> nicListe = vRechner.getNetzwerkInterfaces();
        NetzwerkInterface tempNic;
        it = nicListe.listIterator();
        for (int i = 0; it.hasNext(); i++) {
            tempNic = (NetzwerkInterface) it.next();
            if (holeVerbundeneKomponente(tempNic) == null) {
                tpNetzwerkKarten
                        .setIconAt(i + 1, new ImageIcon(getClass().getResource("/gfx/allgemein/conn_fail.png")));
            } else {
                tpNetzwerkKarten.setIconAt(i + 1, new ImageIcon(getClass().getResource("/gfx/allgemein/conn_ok.png")));
            }
            String tabtitle;
            if (StringUtils.isNoneBlank(tempNic.getIp())) {
                tabtitle = tempNic.getIp();
            } else {
                tabtitle = messages.getString("jvermittlungsrechnerkonfiguration_msg10") + (i + 1);
            }
            tpNetzwerkKarten.setTitleAt(i + 1, tabtitle);
        }

        weiterleitungstabelle.updateAttribute();
    }

    private Knoten holeVerbundeneKomponente(NetzwerkInterface nic) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass()
                + " (JVermittlungsrechnerKonfiguration), holeVerbundeneKomponente(" + nic + ")");
        Port lokalerAnschluss, entfernterAnschluss;
        Port[] ports;
        ListIterator it1, it2;
        Knoten knoten;

        if (nic.getPort().getVerbindung() == null)
            return null;

        lokalerAnschluss = nic.getPort();
        ports = lokalerAnschluss.getVerbindung().getAnschluesse();
        if (ports[0] == lokalerAnschluss)
            entfernterAnschluss = ports[1];
        else
            entfernterAnschluss = ports[0];

        it1 = GUIContainer.getGUIContainer().getKnotenItems().listIterator();
        while (it1.hasNext()) {
            knoten = ((GUIKnotenItem) it1.next()).getKnoten();
            if (knoten instanceof LokalerKnoten) {
                it2 = ((LokalerKnoten) knoten).getAnschluesse().listIterator();
                while (it2.hasNext()) {
                    if (it2.next() == entfernterAnschluss)
                        return knoten;
                }
            } else if (knoten instanceof InternetKnoten) {
                it2 = ((InternetKnoten) knoten).getNetzwerkInterfaces().listIterator();
                while (it2.hasNext()) {
                    if (((NetzwerkInterface) it2.next()).getPort() == entfernterAnschluss)
                        return knoten;
                }
            } else {
                Main.debug.println("ERROR (" + this.hashCode() + "): Knotentyp unbekannt.");
            }
        }

        return null;
    }
}
