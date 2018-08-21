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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import filius.Main;
import filius.gui.GUIContainer;
import filius.gui.JMainFrame;
import filius.hardware.Hardware;
import filius.hardware.knoten.Host;
import filius.rahmenprogramm.EingabenUeberpruefung;
import filius.rahmenprogramm.I18n;
import filius.software.system.Betriebssystem;

@SuppressWarnings("serial")
public class JHostKonfiguration extends JKonfiguration implements I18n {

    private static final int LABEL_WIDTH = 160;
    private JTextField name;
    private JTextField macAdresse;
    private JTextField ipAdresse;
    private JTextField netzmaske;
    private JTextField gateway;
    private JTextField dns;
    private JCheckBox dhcp;
    private JButton btDhcp;
    private JCheckBox useIpAsName;

    protected JHostKonfiguration(Hardware hardware) {
        super(hardware);
    }

    private void aendereAnzeigeName() {
        if (holeHardware() != null) {
            Host host = (Host) holeHardware();
            host.setUseIPAsName(useIpAsName.isSelected());
        }

        GUIContainer.getGUIContainer().updateViewport();
        updateAttribute();
    }

    /** Diese Methode wird vom JAendernButton aufgerufen */
    @Override
    public void aenderungenAnnehmen() {
        Host host;
        Betriebssystem bs;

        if (holeHardware() != null) {
            host = (Host) holeHardware();
            if (!useIpAsName.isSelected()) {
                host.setName(name.getText());
            }

            bs = (Betriebssystem) host.getSystemSoftware();
            bs.setzeIPAdresse(ipAdresse.getText());
            bs.setzeNetzmaske(netzmaske.getText());
            bs.setStandardGateway(gateway.getText());
            bs.setDNSServer(dns.getText());
            bs.setDHCPKonfiguration(dhcp.isSelected());

            if (dhcp.isSelected()) {
                bs.getDHCPServer().setAktiv(false);
            }

        } else {
            Main.debug.println("GUIRechnerKonfiguration: Aenderungen konnten nicht uebernommen werden.");
        }

        GUIContainer.getGUIContainer().updateViewport();
        updateAttribute();
    }

    protected void initAttributEingabeBox(Box box, Box rightBox) {
        JLabel tempLabel;
        Box tempBox;
        FocusListener focusListener;
        ActionListener actionListener;

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

        // =======================================================
        // Attribut Name
        tempLabel = new JLabel(messages.getString("jhostkonfiguration_msg1"));
        tempLabel.setPreferredSize(new Dimension(LABEL_WIDTH, 10));
        tempLabel.setVisible(true);
        tempLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        name = new JTextField(messages.getString("jhostkonfiguration_msg2"));
        name.addActionListener(actionListener);
        name.addFocusListener(focusListener);

        tempBox = Box.createHorizontalBox();
        tempBox = Box.createHorizontalBox();
        tempBox.setOpaque(false);
        tempBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        tempBox.setMaximumSize(new Dimension(400, 40));
        tempBox.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        tempBox.add(tempLabel);
        tempBox.add(Box.createHorizontalStrut(5)); // Platz zw. tempLabel und
        tempBox.add(name);
        box.add(tempBox, BorderLayout.NORTH);

        // =======================================================
        // Attribut MAC-Adresse
        tempLabel = new JLabel(messages.getString("jhostkonfiguration_msg9"));
        tempLabel.setPreferredSize(new Dimension(LABEL_WIDTH, 10));
        tempLabel.setVisible(true);
        tempLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        macAdresse = new JTextField("");
        macAdresse.setEditable(false);

        tempBox = Box.createHorizontalBox();
        tempBox.setOpaque(false);
        tempBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        tempBox.setMaximumSize(new Dimension(400, 40));
        tempBox.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        tempBox.add(tempLabel);
        tempBox.add(Box.createHorizontalStrut(5)); // Platz zw. tempLabel und
        tempBox.add(macAdresse);
        box.add(tempBox, BorderLayout.NORTH);

        // =======================================================
        // Attribut IP-Adresse
        tempLabel = new JLabel(messages.getString("jhostkonfiguration_msg3"));
        tempLabel.setPreferredSize(new Dimension(LABEL_WIDTH, 10));
        tempLabel.setVisible(true);
        tempLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        ipAdresse = new JTextField("192.168.0.1");
        ipAdresse.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                checkIpAddress();
            }
        });
        ipAdresse.addActionListener(actionListener);
        ipAdresse.addFocusListener(focusListener);

        tempBox = Box.createHorizontalBox();
        tempBox.setOpaque(false);
        tempBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        tempBox.setMaximumSize(new Dimension(400, 40));
        tempBox.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        tempBox.add(tempLabel);
        tempBox.add(Box.createHorizontalStrut(5)); // Platz zw. tempLabel und
        tempBox.add(ipAdresse);
        box.add(tempBox, BorderLayout.NORTH);

        // =======================================================
        // Attribut Netzmaske
        tempLabel = new JLabel(messages.getString("jhostkonfiguration_msg4"));
        tempLabel.setPreferredSize(new Dimension(LABEL_WIDTH, 10));
        tempLabel.setVisible(true);
        tempLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        netzmaske = new JTextField("255.255.255.0");
        netzmaske.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                checkNetmask();
            }
        });
        netzmaske.addActionListener(actionListener);
        netzmaske.addFocusListener(focusListener);

        tempBox = Box.createHorizontalBox();
        tempBox.setOpaque(false);
        tempBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        tempBox.setMaximumSize(new Dimension(400, 40));
        tempBox.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        tempBox.add(tempLabel);
        tempBox.add(Box.createHorizontalStrut(5)); // Platz zw. tempLabel und
        tempBox.add(netzmaske);
        box.add(tempBox, BorderLayout.NORTH);

        // =======================================================
        // Attribut Gateway-Adresse
        tempLabel = new JLabel(messages.getString("jhostkonfiguration_msg5"));
        tempLabel.setPreferredSize(new Dimension(LABEL_WIDTH, 10));
        tempLabel.setVisible(true);
        tempLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        gateway = new JTextField("192.168.0.1");
        gateway.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                checkGatewayAddress();
            }
        });
        gateway.addActionListener(actionListener);
        gateway.addFocusListener(focusListener);

        tempBox = Box.createHorizontalBox();
        tempBox.setOpaque(false);
        tempBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        tempBox.setMaximumSize(new Dimension(400, 40));
        tempBox.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        tempBox.add(tempLabel);
        tempBox.add(Box.createHorizontalStrut(5)); // Platz zw. tempLabel und
        tempBox.add(gateway);
        box.add(tempBox, BorderLayout.NORTH);

        // =======================================================
        // Attribut Adresse des Domain Name Server
        tempLabel = new JLabel(messages.getString("jhostkonfiguration_msg6"));
        tempLabel.setPreferredSize(new Dimension(LABEL_WIDTH, 10));
        tempLabel.setVisible(true);
        tempLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        dns = new JTextField("192.168.0.1");
        dns.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                checkDnsAddress();
            }
        });
        dns.addActionListener(actionListener);
        dns.addFocusListener(focusListener);

        tempBox = Box.createHorizontalBox();
        tempBox.setOpaque(false);
        tempBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        tempBox.setMaximumSize(new Dimension(400, 40));
        tempBox.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        tempBox.add(tempLabel);
        tempBox.add(Box.createHorizontalStrut(5)); // Platz zw. tempLabel und
        tempBox.add(dns);
        box.add(tempBox, BorderLayout.NORTH);

        tempLabel = new JLabel(messages.getString("jhostkonfiguration_msg10"));
        tempLabel.setPreferredSize(new Dimension(LABEL_WIDTH, 10));
        tempLabel.setVisible(true);
        tempLabel.setOpaque(false);
        tempLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        useIpAsName = new JCheckBox();
        useIpAsName.setOpaque(false);
        useIpAsName.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                aendereAnzeigeName();
            }
        });

        tempBox = Box.createHorizontalBox();
        tempBox.setOpaque(false);
        tempBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        tempBox.setPreferredSize(new Dimension(400, 35));
        tempBox.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        tempBox.add(useIpAsName);
        tempBox.add(Box.createHorizontalStrut(5)); // Platz zw. tempLabel und
        tempBox.add(tempLabel);
        rightBox.add(tempBox, BorderLayout.NORTH);

        // =======================================================
        // Attribut Verwendung von DHCP
        tempLabel = new JLabel(messages.getString("jhostkonfiguration_msg7"));
        tempLabel.setPreferredSize(new Dimension(LABEL_WIDTH, 10));
        tempLabel.setVisible(true);
        tempLabel.setOpaque(false);
        tempLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        dhcp = new JCheckBox();
        dhcp.setSelected(false);
        dhcp.setOpaque(false);
        dhcp.addActionListener(actionListener);

        tempBox = Box.createHorizontalBox();
        tempBox.setOpaque(false);
        tempBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        tempBox.setPreferredSize(new Dimension(400, 35));
        tempBox.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        tempBox.add(dhcp);
        tempBox.add(Box.createHorizontalStrut(5)); // Platz zw. tempLabel und
        tempBox.add(tempLabel);
        rightBox.add(tempBox, BorderLayout.NORTH);

        rightBox.add(Box.createVerticalStrut(10));

        // ===================================================
        // DHCP-Server einrichten
        tempBox = Box.createHorizontalBox();
        tempBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        tempBox.setOpaque(false);

        btDhcp = new JButton(messages.getString("jhostkonfiguration_msg8"));
        btDhcp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showDhcpConfiguration();
            }
        });
        tempBox.add(btDhcp);

        rightBox.add(tempBox);

        tempBox = Box.createVerticalBox();
        tempBox.setOpaque(false);
        tempBox.setPreferredSize(new Dimension(400, 120));
        rightBox.add(tempBox);

        updateAttribute();
    }

    private void showDhcpConfiguration() {
        JDHCPKonfiguration dhcpKonfig = new JDHCPKonfiguration(JMainFrame.getJMainFrame(),
                messages.getString("jhostkonfiguration_msg8"),
                (Betriebssystem) ((Host) holeHardware()).getSystemSoftware());
        dhcpKonfig.setVisible(true);
    }

    private void checkIpAddress() {
        ueberpruefen(EingabenUeberpruefung.musterIpAdresse, ipAdresse);
    }

    private void checkDnsAddress() {
        ueberpruefen(EingabenUeberpruefung.musterIpAdresseAuchLeer, dns);
    }

    private void checkGatewayAddress() {
        ueberpruefen(EingabenUeberpruefung.musterIpAdresseAuchLeer, gateway);
    }

    private void checkNetmask() {
        ueberpruefen(EingabenUeberpruefung.musterSubNetz, netzmaske);
    }

    public void updateAttribute() {
        Betriebssystem bs;
        Host host;

        if (holeHardware() != null) {
            host = (Host) holeHardware();
            name.setText(host.holeAnzeigeName());
            useIpAsName.setSelected(host.isUseIPAsName());
            name.setEnabled(!host.isUseIPAsName());

            bs = (Betriebssystem) host.getSystemSoftware();

            macAdresse.setText(bs.holeMACAdresse());
            ipAdresse.setText(bs.holeIPAdresse());
            netzmaske.setText(bs.holeNetzmaske());
            gateway.setText(bs.getStandardGateway());
            dns.setText(bs.getDNSServer());

            dhcp.setSelected(bs.isDHCPKonfiguration());
            btDhcp.setEnabled(!dhcp.isSelected());

            ipAdresse.setEnabled(!bs.isDHCPKonfiguration());
            netzmaske.setEnabled(!bs.isDHCPKonfiguration());
            gateway.setEnabled(!bs.isDHCPKonfiguration());
            dns.setEnabled(!bs.isDHCPKonfiguration());

            checkIpAddress();
            checkDnsAddress();
            checkGatewayAddress();
            checkNetmask();
        } else {
            Main.debug.println("GUIRechnerKonfiguration: keine Hardware-Komponente vorhanden");
        }
    }

}
