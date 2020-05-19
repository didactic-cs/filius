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

import org.apache.commons.lang3.StringUtils;

import filius.Main;
import filius.gui.GUIContainer;
import filius.gui.JMainFrame;
import filius.gui.ValidateableTextField;
import filius.hardware.Hardware;
import filius.hardware.NetzwerkInterface;
import filius.hardware.knoten.Host;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.SzenarioVerwaltung;
import filius.software.system.Betriebssystem;
import filius.software.vermittlungsschicht.IPAddress;
import filius.software.vermittlungsschicht.IPVersion;

@SuppressWarnings("serial")
public class JHostKonfiguration extends JKonfiguration implements I18n {

    private static final int LABEL_WIDTH = 160;
    private ValidateableTextField name;
    private ValidateableTextField macAdresse;
    private ValidateableTextField addressIP;
    private ValidateableTextField netmaskIP;
    private ValidateableTextField gatewayIP;
    private ValidateableTextField dnsIP;
    private JCheckBox dhcp;
    private JButton dhcpButton;
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
        if (holeHardware() != null) {
            Host host = (Host) holeHardware();
            if (!useIpAsName.isSelected()) {
                host.setName(name.getText());
            }
            NetzwerkInterface nic = host.getNetzwerkInterfaces().get(0);
            nic.setIp(addressIP.getText());
            nic.setSubnetzMaske(netmaskIP.getText());
            nic.setGateway(gatewayIP.getText());
            nic.setDns(dnsIP.getText());
            nic.setIp(addressIP.getText());
            nic.setSubnetzMaske(netmaskIP.getText());
            nic.setGateway(gatewayIP.getText());
            nic.setDns(dnsIP.getText());

            Betriebssystem bs = (Betriebssystem) host.getSystemSoftware();
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

        name = addConfigParameter(box, messages.getString("jhostkonfiguration_msg1"),
                messages.getString("jhostkonfiguration_msg2"), true);
        macAdresse = addConfigParameter(box, messages.getString("jhostkonfiguration_msg9"), "", false);

        addressIP = addConfigParameter(box, messages.getString("jhostkonfiguration_msg3"),
                IPAddress.defaultAddress(SzenarioVerwaltung.getInstance().ipVersion()).normalizedAddress(), true);
        addressIP.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                checkIpAddress();
            }
        });
        netmaskIP = addConfigParameter(box, messages.getString("jhostkonfiguration_msg4"),
                IPAddress.defaultAddress(SzenarioVerwaltung.getInstance().ipVersion()).netmask(), true);
        netmaskIP.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                checkNetmask();
            }
        });
        gatewayIP = addConfigParameter(box, messages.getString("jhostkonfiguration_msg5"),
                IPAddress.defaultAddress(SzenarioVerwaltung.getInstance().ipVersion()).normalizedAddress(), true);
        gatewayIP.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                checkGatewayAddress();
            }
        });
        dnsIP = addConfigParameter(box, messages.getString("jhostkonfiguration_msg6"),
                IPAddress.defaultAddress(SzenarioVerwaltung.getInstance().ipVersion()).normalizedAddress(), true);
        dnsIP.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                checkDnsAddress();
            }
        });

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
        dhcp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                aenderungenAnnehmen();
            }
        });

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

        dhcpButton = new JButton(messages.getString("jhostkonfiguration_msg8"));
        dhcpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showDhcpConfiguration(SzenarioVerwaltung.getInstance().ipVersion());
            }
        });
        tempBox.add(dhcpButton);

        rightBox.add(tempBox);

        tempBox = Box.createVerticalBox();
        tempBox.setOpaque(false);
        tempBox.setPreferredSize(new Dimension(400, 120));
        rightBox.add(tempBox);

        updateAttribute();
    }

    private ValidateableTextField addConfigParameter(Box box, String label, String defaultValue, boolean editable) {
        JLabel tempLabel = new JLabel(label);
        tempLabel.setPreferredSize(new Dimension(LABEL_WIDTH, 10));
        tempLabel.setVisible(true);
        tempLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        ValidateableTextField textField = new ValidateableTextField(defaultValue);

        textField.setEditable(editable);
        if (editable) {
            textField.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    aenderungenAnnehmen();
                }
            });
            textField.addFocusListener(new FocusListener() {
                public void focusGained(FocusEvent arg0) {}

                public void focusLost(FocusEvent arg0) {
                    aenderungenAnnehmen();
                }
            });
        }

        Box tempBox = Box.createHorizontalBox();
        tempBox.setOpaque(false);
        tempBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        tempBox.setMaximumSize(new Dimension(400, 40));
        tempBox.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        tempBox.add(tempLabel);
        tempBox.add(Box.createHorizontalStrut(5)); // Platz zw. tempLabel und
        tempBox.add(textField);
        box.add(tempBox, BorderLayout.NORTH);
        return textField;
    }

    private void showDhcpConfiguration(IPVersion ipVersion) {
        JDHCPKonfiguration dhcpKonfig = new JDHCPKonfiguration(JMainFrame.getJMainFrame(),
                messages.getString("jhostkonfiguration_msg8"),
                (Betriebssystem) ((Host) holeHardware()).getSystemSoftware(), ipVersion);
        dhcpKonfig.setVisible(true);
    }

    private void checkIpAddress() {
        addressIP.setValid(IPAddress.verifyAddress(addressIP.getText(), SzenarioVerwaltung.getInstance().ipVersion()));
    }

    private void checkDnsAddress() {
        dnsIP.setValid(StringUtils.isBlank(dnsIP.getText())
                || IPAddress.verifyAddress(dnsIP.getText(), SzenarioVerwaltung.getInstance().ipVersion()));
    }

    private void checkGatewayAddress() {
        gatewayIP.setValid(StringUtils.isBlank(gatewayIP.getText())
                || IPAddress.verifyAddress(gatewayIP.getText(), SzenarioVerwaltung.getInstance().ipVersion()));
    }

    private void checkNetmask() {
        netmaskIP.setValid(
                IPAddress.verifyNetmaskDefinition(netmaskIP.getText(), SzenarioVerwaltung.getInstance().ipVersion()));
    }

    public void updateAttribute() {
        if (holeHardware() != null) {
            Host host = (Host) holeHardware();
            name.setText(host.holeAnzeigeName());
            useIpAsName.setSelected(host.isUseIPAsName());
            name.setEnabled(!host.isUseIPAsName());

            Betriebssystem bs = (Betriebssystem) host.getSystemSoftware();
            NetzwerkInterface nic = host.getNetzwerkInterfaces().get(0);
            macAdresse.setText(nic.getMac());
            addressIP.setText(nic.getIp());
            netmaskIP.setText(nic.getSubnetzMaske());
            gatewayIP.setText(nic.getGateway());
            dnsIP.setText(nic.getDns());

            dhcp.setSelected(bs.isDHCPKonfiguration());
            dhcpButton.setEnabled(!dhcp.isSelected());

            addressIP.setEnabled(!bs.isDHCPKonfiguration());
            netmaskIP.setEnabled(!bs.isDHCPKonfiguration());
            gatewayIP.setEnabled(!bs.isDHCPKonfiguration());
            dnsIP.setEnabled(!bs.isDHCPKonfiguration());

            checkIpAddress();
            checkDnsAddress();
            checkGatewayAddress();
            checkNetmask();
        } else {
            Main.debug.println("GUIRechnerKonfiguration: keine Hardware-Komponente vorhanden");
        }
    }
}
