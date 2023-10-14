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
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import filius.gui.GUIContainer;
import filius.gui.GUIEvents;
import filius.gui.JMainFrame;
import filius.hardware.Hardware;
import filius.hardware.knoten.Host;
import filius.hardware.knoten.InternetKnoten;
import filius.hardware.knoten.Switch;
import filius.rahmenprogramm.EingabenUeberpruefung;
import filius.rahmenprogramm.I18n;
import filius.software.system.Betriebssystem;
import filius.software.system.InternetKnotenBetriebssystem;
import filius.software.system.SwitchFirmware;

@SuppressWarnings("serial")
public class JHostKonfiguration extends JKonfiguration implements I18n {
    private static Logger LOG = LoggerFactory.getLogger(JHostKonfiguration.class);

    private static final int LABEL_WIDTH = 160;
    private JTextField name;
    private JTextField macAdresse;
    private JTextField ipAdresse;
    private JTextField netzmaske;
    private JTextField gateway;
    private JTextField dns;
    private JCheckBox dhcp;
    private JCheckBox ipForwarding;
    private JButton btDhcp;
    private JCheckBox useIpAsName;
    private JCheckBox useMacAsName;
    private JComboBox<String> ssid;
    private JLabel ssidLabel;
    private JRadioButton useLAN;
    private JRadioButton useWiFi;
    private ButtonGroup lanWifi;

    protected JHostKonfiguration(Hardware hardware) {
        super(hardware);
    }

    private void aendereAnzeigeName() {
        if (holeHardware() != null) {
            Host host = (Host) holeHardware();
            host.setUseIPAsName(useIpAsName.isSelected());
            host.setUseMACAsName(useMacAsName.isSelected());
        }

        GUIContainer.getGUIContainer().updateViewport();
        updateAttribute();
    }

    private void aenderungenAnnehmen() {
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
            bs.setIpForwardingEnabled(ipForwarding.isSelected());
            bs.setDHCPKonfiguration(dhcp.isSelected());

            boolean wifiStatusBeforeChange = bs.wireless();
            String ssidBeforeChange = bs.getSsid();

            bs.nicWireless(useWiFi.isSelected());
            if (ssid.getSelectedIndex() >= 1) {
                ((Betriebssystem) host.getSystemSoftware()).setSsid(ssid.getSelectedItem().toString());
            } else {
                ((Betriebssystem) host.getSystemSoftware()).setSsid(null);
            }

            boolean wifiStatusAfterChange = bs.wireless();
            String ssidAfterChange = bs.getSsid();
            if (!StringUtils.equals(ssidBeforeChange, ssidAfterChange)
                    || wifiStatusBeforeChange != wifiStatusAfterChange) {
                updateWifiConnection();
            }

            if (dhcp.isSelected()) {
                bs.getDHCPServer().setAktiv(false);
            }
        } else {
            LOG.debug("GUIRechnerKonfiguration: Aenderungen konnten nicht uebernommen werden.");
        }

        GUIContainer.getGUIContainer().updateViewport();
    }

    protected void initContents() {
        JLabel tempLabel;
        Box tempBox;
        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                aenderungenAnnehmen();
                updateAttribute();
            }
        };
        FocusListener focusListener = new FocusListener() {
            public void focusGained(FocusEvent arg0) {}

            public void focusLost(FocusEvent arg0) {
                aenderungenAnnehmen();
                updateAttribute();
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
        tempBox.setMaximumSize(new Dimension(400, 35));
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
        tempBox.setMaximumSize(new Dimension(400, 35));
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
        tempBox.setMaximumSize(new Dimension(400, 35));
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
        tempBox.setMaximumSize(new Dimension(400, 35));
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
        tempBox.setMaximumSize(new Dimension(400, 35));
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
        tempBox.setMaximumSize(new Dimension(400, 35));
        tempBox.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        tempBox.add(tempLabel);
        tempBox.add(Box.createHorizontalStrut(5)); // Platz zw. tempLabel und
        tempBox.add(dns);
        box.add(tempBox, BorderLayout.NORTH);

        // =======================================================
        // Wireless / Cable
        ActionListener wifiLanActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                aenderungenAnnehmen();
                updateAttribute();
            }
        };

        JLabel connectivity = new JLabel(" / ");
        useLAN = new JRadioButton();
        useLAN.setText(messages.getString("jhostkonfiguration_msg14"));
        useLAN.setOpaque(false);
        useLAN.addActionListener(wifiLanActionListener);
        lanWifi = new ButtonGroup();
        lanWifi.add(useLAN);
        useWiFi = new JRadioButton();
        useWiFi.setText(messages.getString("jhostkonfiguration_msg13"));
        useWiFi.setOpaque(false);
        useWiFi.addActionListener(wifiLanActionListener);
        lanWifi.add(useWiFi);

        tempBox = Box.createHorizontalBox();
        tempBox.setOpaque(false);
        tempBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        tempBox.setPreferredSize(new Dimension(400, 35));
        tempBox.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        tempBox.add(useLAN);
        tempBox.add(connectivity);
        tempBox.add(useWiFi);
        middleBox.add(tempBox);

        ssidLabel = new JLabel(messages.getString("jhostkonfiguration_msg15"));
        ssidLabel.setVisible(true);
        ssidLabel.setOpaque(false);
        ssidLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        tempBox = Box.createHorizontalBox();
        tempBox.add(Box.createHorizontalStrut(30));
        tempBox.setOpaque(false);
        tempBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        tempBox.setPreferredSize(new Dimension(400, 35));
        tempBox.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        tempBox.add(ssidLabel);
        middleBox.add(tempBox);

        ssid = new JComboBox<>();
        ssid.addItem(messages.getString("jhostkonfiguration_msg16"));
        ssid.setPreferredSize(new Dimension(LABEL_WIDTH - 60, 15));
        ssid.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                aenderungenAnnehmen();
            }
        });
        tempBox = Box.createHorizontalBox();
        tempBox.add(Box.createHorizontalStrut(30));
        tempBox.setOpaque(false);
        tempBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        tempBox.setPreferredSize(new Dimension(400, 35));
        tempBox.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        tempBox.add(ssid);
        middleBox.add(tempBox);

        middleBox.add(Box.createVerticalStrut(160));

        // =======================================================
        // IP address as name
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
        // MAC address as name
        tempLabel = new JLabel(messages.getString("jhostkonfiguration_msg12"));
        tempLabel.setPreferredSize(new Dimension(LABEL_WIDTH, 10));
        tempLabel.setVisible(true);
        tempLabel.setOpaque(false);
        tempLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        useMacAsName = new JCheckBox();
        useMacAsName.setOpaque(false);
        useMacAsName.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                aendereAnzeigeName();
            }
        });

        tempBox = Box.createHorizontalBox();
        tempBox.setOpaque(false);
        tempBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        tempBox.setPreferredSize(new Dimension(400, 35));
        tempBox.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        tempBox.add(useMacAsName);
        tempBox.add(Box.createHorizontalStrut(5)); // Platz zw. tempLabel und
        tempBox.add(tempLabel);
        rightBox.add(tempBox, BorderLayout.NORTH);

        // =======================================================
        // Activation Status IP Forwarding
        tempLabel = new JLabel(messages.getString("jhostkonfiguration_msg11"));
        tempLabel.setPreferredSize(new Dimension(LABEL_WIDTH, 10));
        tempLabel.setVisible(true);
        tempLabel.setOpaque(false);
        tempLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        ipForwarding = new JCheckBox();
        ipForwarding.setOpaque(false);
        ipForwarding.addActionListener(actionListener);

        tempBox = Box.createHorizontalBox();
        tempBox.setOpaque(false);
        tempBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        tempBox.setPreferredSize(new Dimension(400, 35));
        tempBox.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        tempBox.add(ipForwarding);
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

        // ===================================================
        // DHCP-Server einrichten
        rightBox.add(Box.createVerticalStrut(10));
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

        rightBox.add(Box.createVerticalStrut(80));

        updateAttribute();
    }

    private void showDhcpConfiguration() {
        JDHCPKonfiguration dhcpKonfig = new JDHCPKonfiguration(JMainFrame.getJMainFrame(),
                messages.getString("jhostkonfiguration_msg8"),
                (InternetKnotenBetriebssystem) ((InternetKnoten) holeHardware()).getSystemSoftware());
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
        if (holeHardware() != null) {
            Host host = (Host) holeHardware();
            name.setText(host.holeAnzeigeName());
            useIpAsName.setSelected(host.isUseIPAsName());
            useMacAsName.setSelected(host.isUseMACAsName());
            name.setEnabled(!host.isUseIPAsName() && !host.isUseMACAsName());

            Betriebssystem bs = (Betriebssystem) host.getSystemSoftware();

            macAdresse.setText(bs.primaryMACAddress());
            ipAdresse.setText(bs.primaryIPAdresse());
            netzmaske.setText(bs.primarySubnetMask());
            gateway.setText(bs.getStandardGateway());
            dns.setText(bs.getDNSServer());

            ipForwarding.setSelected(bs.isIpForwardingEnabled());
            dhcp.setSelected(bs.isDHCPKonfiguration());
            btDhcp.setEnabled(!dhcp.isSelected());

            ipAdresse.setEnabled(!bs.isDHCPKonfiguration());
            netzmaske.setEnabled(!bs.isDHCPKonfiguration());
            gateway.setEnabled(!bs.isDHCPKonfiguration());
            dns.setEnabled(!bs.isDHCPKonfiguration());

            useLAN.setSelected(!bs.wireless());
            useWiFi.setSelected(bs.wireless());
            useWiFi.setEnabled(host.holeFreienPort() != null || bs.wireless());
            ssid.setEnabled(useWiFi.isSelected());
            ssidLabel.setEnabled(ssid.isEnabled());
            updateSsid();

            checkIpAddress();
            checkDnsAddress();
            checkGatewayAddress();
            checkNetmask();
        } else {
            LOG.debug("GUIRechnerKonfiguration: keine Hardware-Komponente vorhanden");
        }
    }

    private void updateWifiConnection() {
        Host host = (Host) holeHardware();

        // Step 1: remove existing wifi connection
        for (GUIKabelItem cable : GUIContainer.getGUIContainer().getCableItems()) {
            if (cable.getDasKabel().getWireless() && (host.equals(cable.getKabelpanel().getZiel1().getKnoten())
                    || host.equals(cable.getKabelpanel().getZiel2().getKnoten()))) {
                GUIEvents.getGUIEvents().removeConnection(cable);
                break;
            }
        }
        // Step 2: (Re-)Connect to access point
        Betriebssystem os = (Betriebssystem) host.getSystemSoftware();
        String configuredSSID = os.getSsid();
        boolean connectWifi = os.wireless() && null != configuredSSID;
        if (connectWifi) {
            GUIKnotenItem wifiAccessPoint = null;
            GUIKnotenItem hostNode = null;
            for (GUIKnotenItem item : GUIContainer.getGUIContainer().getKnotenItems()) {
                if (item.getKnoten() instanceof Switch
                        && configuredSSID.equals(((SwitchFirmware) item.getKnoten().getSystemSoftware()).getSSID())) {
                    wifiAccessPoint = item;
                } else if (item.getKnoten().equals(host)) {
                    hostNode = item;
                } else if (null != wifiAccessPoint && null != hostNode) {
                    break;
                }
            }
            if (null != wifiAccessPoint) {
                GUIEvents.getGUIEvents().createConnection(hostNode, wifiAccessPoint);
            }
        }
        GUIContainer.getGUIContainer().updateCables();
    }

    private void updateSsid() {
        List<String> ssidList = new ArrayList<>();
        for (int i = 1; i < ssid.getItemCount(); i++) {
            ssidList.add(ssid.getItemAt(i));
        }

        List<String> accessPointSsidList = new ArrayList<>();
        for (GUIKnotenItem item : GUIContainer.getGUIContainer().getKnotenItems()) {
            if (item.getKnoten() instanceof Switch) {
                String accessPointSsid = ((SwitchFirmware) item.getKnoten().getSystemSoftware()).getSSID();
                accessPointSsidList.add(accessPointSsid);
                if (!ssidList.contains(accessPointSsid)) {
                    ssid.addItem(accessPointSsid);
                }
            }
        }
        for (int i = ssid.getItemCount() - 1; i >= 1; i--) {
            if (!accessPointSsidList.contains(ssid.getItemAt(i))) {
                ssid.removeItemAt(i);
            }
        }

        Host host = (Host) holeHardware();
        String configuredSSID = ((Betriebssystem) host.getSystemSoftware()).getSsid();
        if (null != configuredSSID) {
            ssid.setSelectedItem(configuredSSID);
        } else {
            ssid.setSelectedIndex(0);
        }
    }

}
