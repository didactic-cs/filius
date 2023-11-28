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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import filius.gui.GUIContainer;
import filius.gui.JMainFrame;
import filius.hardware.Hardware;
import filius.hardware.Kabel;
import filius.hardware.NetzwerkInterface;
import filius.hardware.Port;
import filius.hardware.Verbindung;
import filius.hardware.knoten.Gateway;
import filius.hardware.knoten.InternetKnoten;
import filius.hardware.knoten.Knoten;
import filius.hardware.knoten.LokalerKnoten;
import filius.rahmenprogramm.EingabenUeberpruefung;
import filius.rahmenprogramm.I18n;
import filius.software.firewall.Firewall;
import filius.software.nat.NatMethod;
import filius.software.system.GatewayFirmware;
import filius.software.system.InternetKnotenBetriebssystem;

@SuppressWarnings("serial")
public class JGatewayConfiguration extends JKonfiguration implements I18n {
    private static Logger LOG = LoggerFactory.getLogger(JGatewayConfiguration.class);

    private static final int WAN_TAB_IDX = 2;
    private static final int LAN_TAB_IDX = 1;

    private JTextField name;

    private JTextField ipAddressWANPort;
    private JTextField netmaskWANPort;
    private JTextField macAddressWANPort;
    private JLabel connectedComponentWAN;

    private JTextField ipAddressLANPort;
    private JTextField netmaskLANPort;
    private JTextField macAddressLANPort;
    private JLabel connectedComponentLAN;

    private JTextField gatewayName;
    private JCheckBox ipForwarding;
    private JCheckBox dhcp;

    private JTabbedPane tpNetzwerkKarten;

    private JPortForwarding portForwarding;

    private Kabel highlightedCable = null;
    
    private JComboBox<String> coneTypeSelect;

    private JTextField retentionTime;
    
    protected JGatewayConfiguration(Hardware hardware) {
        super(hardware);
    }

    public void aenderungenAnnehmen() {
        Gateway gateway = (Gateway) holeHardware();
        GatewayFirmware firmware = (GatewayFirmware) gateway.getSystemSoftware();

        gateway.setName(name.getText());
        firmware.setStandardGateway(gatewayName.getText());
        
        switch (coneTypeSelect.getSelectedIndex()) { //neu
        case 0:
        	if (firmware.getNatMethod() != NatMethod.fullCone) {
        		firmware.setNatMethodChanged(true);
        	}
        	firmware.setNatMethod(NatMethod.fullCone);
        	break;
        case 1:
        	if (firmware.getNatMethod() != NatMethod.restrictedCone) {
        		firmware.setNatMethodChanged(true);
        	}
        	firmware.setNatMethod(NatMethod.restrictedCone);
        	break;
        }
        	
        try {
        	firmware.setRetentionTime(Long.parseLong(retentionTime.getText())*1000);
        } catch (NumberFormatException e) {
    		System.out.println(e);
    	}
        	
        applyNICConfig(gateway.holeWANInterface(), ipAddressWANPort, netmaskWANPort);
        firmware.setDHCPKonfiguration(dhcp.isSelected());
        applyNICConfig(gateway.holeLANInterface(), ipAddressLANPort, netmaskLANPort);

        GUIContainer.getGUIContainer().updateViewport();
        updateAttribute();
    }

    private void applyNICConfig(NetzwerkInterface nic, JTextField ipAddressTextfield, JTextField netmaskTextfield) {
        if (ueberpruefen(EingabenUeberpruefung.musterIpAdresse, ipAddressTextfield)) {
            nic.setIp(ipAddressTextfield.getText());
        } else {
            LOG.debug("ERROR (" + this.hashCode() + "): IP-Adresse ungueltig " + ipAddressTextfield.getText());
        }
        if (ueberpruefen(EingabenUeberpruefung.musterSubNetz, netmaskTextfield)) {
            nic.setSubnetzMaske(netmaskTextfield.getText());
        } else {
            LOG.debug("ERROR (" + this.hashCode() + "): Netzmaske ungueltig " + netmaskTextfield.getText());
        }
    }

    private void showFirewallDialog() {
        Firewall firewall = ((GatewayFirmware) ((Gateway) holeHardware()).getSystemSoftware()).holeFirewall();

        JFirewallDialog firewallDialog = new JFirewallDialog(firewall, JMainFrame.getJMainFrame());
        firewallDialog.setBounds(100, 100, 850, 340);
        firewallDialog.setName(messages.getString("jgatewayconfiguration_msg1"));

        firewallDialog.updateRuleTable();
        firewallDialog.setVisible(true);
    }

    private void showDhcpConfiguration() {
        JDHCPKonfiguration dhcpKonfig = new JDHCPKonfiguration(JMainFrame.getJMainFrame(),
                messages.getString("jhostkonfiguration_msg8"),
                (InternetKnotenBetriebssystem) ((InternetKnoten) holeHardware()).getSystemSoftware());
        dhcpKonfig.setVisible(true);
    }

    protected void initContents() {
        Box boxNetzwerkKarten;
        Box vBox;
        Box boxPortForwarding;
        KeyAdapter ipAdresseKeyAdapter;
        KeyAdapter netzmaskeKeyAdapter;
        JButton btFirewall;
        JButton btNeuerEintrag;
        JButton btEintragLoeschen;

        JLabel tempLabel;
        Box tempBox;

        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                aenderungenAnnehmen();
            }
        };

        FocusListener focusListener = new FocusListener() {

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
                JTextField tfQuelle = (JTextField) e.getSource();
                ueberpruefen(EingabenUeberpruefung.musterIpAdresse, tfQuelle);
            }
        };

        netzmaskeKeyAdapter = new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                JTextField tfQuelle = (JTextField) e.getSource();
                ueberpruefen(EingabenUeberpruefung.musterSubNetz, tfQuelle);
            }
        };

        vBox = Box.createVerticalBox();

        // Attribut Name
        tempBox = Box.createHorizontalBox();
        tempBox.setMaximumSize(new Dimension(400, 40));

        tempLabel = new JLabel(messages.getString("jgatewayconfiguration_msg2"));
        tempLabel.setPreferredSize(new Dimension(140, 20));
        tempLabel.setVisible(true);
        tempLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        tempBox.add(tempLabel);

        name = new JTextField(messages.getString("jgatewayconfiguration_msg3"));
        name.setPreferredSize(new Dimension(160, 20));
        name.addActionListener(actionListener);
        name.addFocusListener(focusListener);
        tempBox.add(name);

        vBox.add(tempBox);
        vBox.add(Box.createVerticalStrut(5));

        // Attribut Gateway
        tempBox = Box.createHorizontalBox();
        tempBox.setMaximumSize(new Dimension(400, 40));

        tempLabel = new JLabel(messages.getString("jgatewayconfiguration_msg9"));
        tempLabel.setPreferredSize(new Dimension(140, 20));
        tempLabel.setVisible(true);
        tempLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        tempBox.add(tempLabel);

        gatewayName = new JTextField();
        gatewayName.setPreferredSize(new Dimension(160, 20));
        gatewayName.addActionListener(actionListener);
        gatewayName.addFocusListener(focusListener);
        gatewayName.addKeyListener(ipAdresseKeyAdapter);
        tempBox.add(gatewayName);

        vBox.add(tempBox);
        vBox.add(Box.createVerticalStrut(5));

        // retention time //neu
        tempBox = Box.createHorizontalBox();
        tempBox.setMaximumSize(new Dimension(400, 40));
        
        tempLabel = new JLabel(messages.getString("jgatewayconfiguration_msg23"));
        tempLabel.setPreferredSize(new Dimension(140, 20));
        tempLabel.setVisible(true);
        tempLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        tempBox.add(tempLabel);

        retentionTime = new JTextField("30");
        retentionTime.addActionListener(actionListener);
        retentionTime.addFocusListener(focusListener);
        retentionTime.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                checkRetentionTime();
            }
        });
        tempBox.add(retentionTime);
        
        tempLabel = new JLabel("s");
        tempLabel.setPreferredSize(new Dimension(140, 10));
        tempLabel.setVisible(true);	
        tempLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        tempBox.add(tempLabel);

        vBox.add(tempBox);
        vBox.add(Box.createVerticalStrut(10));

        // full-cone or (address)-restricted-cone NAT //neu
        ActionListener coneTypeActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	aenderungenAnnehmen();
                updateAttribute();
            }
        };
        tempBox = Box.createHorizontalBox();
        tempBox.setMaximumSize(new Dimension(400, 40));
        tempLabel = new JLabel(messages.getString("jgatewayconfiguration_msg24"));
        tempLabel.setPreferredSize(new Dimension(140, 20));
        tempLabel.setVisible(true);
        tempLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        tempBox.add(tempLabel);
        String conetypeList[] = {messages.getString("jgatewayconfiguration_msg25"),messages.getString("jgatewayconfiguration_msg26")};
        coneTypeSelect = new JComboBox<>(conetypeList);
        coneTypeSelect.addActionListener(coneTypeActionListener);
        
        tempBox.setPreferredSize(new Dimension(140, 20));
        tempBox.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        tempBox.add(coneTypeSelect);
        vBox.add(tempBox);

        // IP forwarding status
        tempBox = Box.createHorizontalBox();
        tempBox.setMaximumSize(new Dimension(400, 40));

        ipForwarding = new JCheckBox();
        ipForwarding.setPreferredSize(new Dimension(160, 20));
        ipForwarding.addActionListener(actionListener);
        ipForwarding.addFocusListener(focusListener);
        ipForwarding.setOpaque(false);
        ipForwarding.setEnabled(false);
        tempBox.add(ipForwarding);

        tempBox.add(Box.createHorizontalStrut(10));

        tempLabel = new JLabel(messages.getString("jhostkonfiguration_msg11"));
        tempLabel.setPreferredSize(new Dimension(140, 20));
        tempLabel.setVisible(true);
        tempLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        tempBox.add(tempLabel);

        vBox.add(tempBox);
        vBox.add(Box.createVerticalStrut(5));

        tempBox = Box.createHorizontalBox();
        tempBox.setMaximumSize(new Dimension(400, 40));
        btFirewall = new JButton(messages.getString("jgatewayconfiguration_msg4"));

        btFirewall.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showFirewallDialog();
            }
        });
        tempBox.add(btFirewall);

        vBox.add(tempBox);

        // NIC tabs
        tpNetzwerkKarten.addTab(messages.getString("jgatewayconfiguration_msg17"), vBox);

        ipAddressLANPort = new JTextField();
        netmaskLANPort = new JTextField();
        macAddressLANPort = new JTextField();
        connectedComponentLAN = new JLabel();
        initNICConfigPane(ipAdresseKeyAdapter, netzmaskeKeyAdapter, focusListener, actionListener, ipAddressLANPort,
                macAddressLANPort, netmaskLANPort, connectedComponentLAN, true, false);

        ipAddressWANPort = new JTextField();
        netmaskWANPort = new JTextField();
        macAddressWANPort = new JTextField();
        connectedComponentWAN = new JLabel();
        initNICConfigPane(ipAdresseKeyAdapter, netzmaskeKeyAdapter, focusListener, actionListener, ipAddressWANPort,
                macAddressWANPort, netmaskWANPort, connectedComponentWAN, false, true);

        tpNetzwerkKarten.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent arg0) {
                JTabbedPane pane = (JTabbedPane) arg0.getSource();
                // Get current tab
                if (highlightedCable != null) {
                    highlightedCable.setAktiv(false);
                }
                Verbindung conn = null;
                int sel = pane.getSelectedIndex();
                if (sel == WAN_TAB_IDX) {
                    conn = ((Gateway) holeHardware()).holeWANInterface().getPort().getVerbindung();
                } else if (sel == LAN_TAB_IDX) {
                    conn = ((Gateway) holeHardware()).holeLANInterface().getPort().getVerbindung();
                }
                if (conn != null) {
                    conn.setAktiv(true);
                    highlightedCable = (Kabel) conn;
                }

                portForwarding.aenderungenAnnehmen();
            }

        });
        
        // Port forwarding tabs
        
        portForwarding = new JPortForwarding(this);

        JScrollPane spWeiterleitung = new JScrollPane(portForwarding);
        spWeiterleitung.setPreferredSize(new Dimension(300, 120));
        spWeiterleitung.addFocusListener(focusListener);

        tempBox = Box.createHorizontalBox();
        tempBox.setOpaque(false);
        tempBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        tempBox.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        btNeuerEintrag = new JButton(messages.getString("jgatewayconfiguration_msg21"));
        btNeuerEintrag.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                portForwarding.neuerEintrag();
            }
        });
        tempBox.add(Box.createHorizontalStrut(50));
        tempBox.add(btNeuerEintrag);

        btEintragLoeschen = new JButton(messages.getString("jgatewayconfiguration_msg22"));
        btEintragLoeschen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                portForwarding.markiertenEintragLoeschen();
            }
        });
        tempBox.add(Box.createHorizontalStrut(5));
        tempBox.add(btEintragLoeschen);

        boxPortForwarding = Box.createVerticalBox();
        boxPortForwarding.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        boxPortForwarding.add(tempBox);
        boxPortForwarding.add(spWeiterleitung);

        tpNetzwerkKarten.addTab(messages.getString("jgatewayconfiguration_msg19"), boxPortForwarding);
        

        box.add(boxNetzwerkKarten);

        updateAttribute();
    }

    private void initNICConfigPane(KeyAdapter ipAdresseKeyAdapter, KeyAdapter netzmaskeKeyAdapter,
            FocusListener focusListener, ActionListener actionListener, JTextField ipAddressTextfield,
            JTextField macAddressTextfield, JTextField netmaskTextfield, JLabel connectedComponentLabel,
            boolean dhcpServer, boolean dhcpConfig) {
        JLabel tempLabel;
        Box boxNic = Box.createVerticalBox();
        // boxNic.setPreferredSize(new Dimension(800, 200));

        Box boxKomponente = Box.createHorizontalBox();
        boxKomponente.setMaximumSize(new Dimension(400, 30));
        connectedComponentLabel.setPreferredSize(new Dimension(400, 10));
        boxKomponente.add(connectedComponentLabel);

        // show IP address (editable)
        Box boxIpAdresse = Box.createHorizontalBox();
        boxIpAdresse.setMaximumSize(new Dimension(400, 30));
        tempLabel = new JLabel(messages.getString("jgatewayconfiguration_msg7"));
        tempLabel.setPreferredSize(new Dimension(120, 10));
        boxIpAdresse.add(tempLabel);

        boxIpAdresse.add(ipAddressTextfield);

        // show netmask (editable)
        Box boxSubnetz = Box.createHorizontalBox();
        boxSubnetz.setMaximumSize(new Dimension(400, 30));
        tempLabel = new JLabel(messages.getString("jgatewayconfiguration_msg8"));
        tempLabel.setPreferredSize(new Dimension(120, 10));
        boxSubnetz.add(tempLabel);

        boxSubnetz.add(netmaskTextfield);

        // show MAC address (not editable)
        Box boxMacAdresse = Box.createHorizontalBox();
        boxMacAdresse.setMaximumSize(new Dimension(400, 30));
        tempLabel = new JLabel(messages.getString("jgatewayconfiguration_msg18"));
        tempLabel.setPreferredSize(new Dimension(120, 10));
        boxMacAdresse.add(tempLabel);

        macAddressTextfield.setEnabled(false);
        boxMacAdresse.add(macAddressTextfield);

        boxNic.add(boxKomponente);
        boxNic.add(Box.createVerticalStrut(5));
        boxNic.add(boxIpAdresse);
        boxNic.add(Box.createVerticalStrut(5));
        boxNic.add(boxSubnetz);
        boxNic.add(Box.createVerticalStrut(5));
        boxNic.add(boxMacAdresse);

        if (dhcpServer) {
            boxNic.add(Box.createVerticalStrut(10));
            Box tempBox = Box.createHorizontalBox();
            tempBox.setMaximumSize(new Dimension(400, 40));
            JButton btDhcp = new JButton(messages.getString("jhostkonfiguration_msg8"));
            btDhcp.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showDhcpConfiguration();
                }
            });
            tempBox.add(btDhcp);
            boxNic.add(tempBox);
        } else if (dhcpConfig) {
            boxNic.add(Box.createVerticalStrut(10));
            Box tempBox = Box.createHorizontalBox();
            tempBox.setMaximumSize(new Dimension(400, 40));
            dhcp = new JCheckBox();
            dhcp.setSelected(false);
            dhcp.setOpaque(false);
            dhcp.addActionListener(actionListener);
            dhcp.setText(messages.getString("jhostkonfiguration_msg7"));
            tempBox.add(dhcp);
            boxNic.add(tempBox);
        }

        tpNetzwerkKarten.addTab(messages.getString("jgatewayconfiguration_msg10"),
                new ImageIcon(getClass().getResource("/gfx/allgemein/conn_fail.png")), boxNic);

        ipAddressTextfield.addKeyListener(ipAdresseKeyAdapter);
        ipAddressTextfield.addActionListener(actionListener);
        ipAddressTextfield.addFocusListener(focusListener);

        netmaskTextfield.addKeyListener(netzmaskeKeyAdapter);
        netmaskTextfield.addActionListener(actionListener);
        netmaskTextfield.addFocusListener(focusListener);
    }

    public void doUnselectAction() {
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass() + " (jgatewayconfiguration), doUnselectAction()");
        if (highlightedCable != null) {
            highlightedCable.setAktiv(false);
            highlightedCable = null;
            tpNetzwerkKarten.setSelectedIndex(0);
        }
    }

    public void highlightConnCable() {
        if (highlightedCable != null) {
            highlightedCable.setAktiv(true);
        }
    }

    public JGatewayConfiguration getKonfiguration() {
        return this;
    }

    public void updateAttribute() {
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass() + " (jgatewayconfiguration), updateAttribute()");

        Gateway vRechner = (Gateway) holeHardware();
        GatewayFirmware bs = (GatewayFirmware) vRechner.getSystemSoftware();

        name.setText(vRechner.holeAnzeigeName());
        gatewayName.setText(bs.getStandardGateway());
        ipForwarding.setSelected(bs.isIpForwardingEnabled());

        boolean dhcpEnabled = bs.isDHCPKonfiguration();
        dhcp.setSelected(dhcpEnabled);
        ipAddressWANPort.setEnabled(!dhcpEnabled);
        netmaskWANPort.setEnabled(!dhcpEnabled);

        switch (bs.getNatMethod()) { //neu
        case fullCone:
        	coneTypeSelect.setSelectedIndex(0);
        	break;
        case restrictedCone:
        	coneTypeSelect.setSelectedIndex(1);
        	break;
        }

        updateNICConfigPane("LAN", vRechner.holeLANInterface(), ipAddressLANPort, macAddressLANPort, netmaskLANPort,
                connectedComponentLAN, LAN_TAB_IDX);
        updateNICConfigPane("WAN", vRechner.holeWANInterface(), ipAddressWANPort, macAddressWANPort, netmaskWANPort,
                connectedComponentWAN, WAN_TAB_IDX);

        portForwarding.updateAttribute();
    }

    private void updateNICConfigPane(String titlePrefix, NetzwerkInterface nic, JTextField ipAddressTextfield,
            JTextField macAddressTextfield, JTextField netmaskTextfield, JLabel connectedComponentTextfield,
            int tabIdx) {
        ipAddressTextfield.setText(nic.getIp());
        netmaskTextfield.setText(nic.getSubnetzMaske());
        macAddressTextfield.setText(nic.getMac());
        Knoten tempKnoten = holeVerbundeneKomponente(nic);
        if (tempKnoten == null) {
            connectedComponentTextfield.setText(messages.getString("jgatewayconfiguration_msg16"));
            tpNetzwerkKarten.setIconAt(tabIdx, new ImageIcon(getClass().getResource("/gfx/allgemein/conn_fail.png")));
        } else {
            connectedComponentTextfield
                    .setText(messages.getString("jgatewayconfiguration_msg6") + " " + tempKnoten.holeAnzeigeName());
            tpNetzwerkKarten.setIconAt(tabIdx, new ImageIcon(getClass().getResource("/gfx/allgemein/conn_ok.png")));
        }
        tpNetzwerkKarten.setTitleAt(tabIdx, titlePrefix + ": " + nic.getIp());
    }

    private Knoten holeVerbundeneKomponente(NetzwerkInterface nic) {
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass()
                + " (jgatewayconfiguration), holeVerbundeneKomponente(" + nic + ")");

        if (nic.getPort().getVerbindung() == null) {
            return null;
        }

        Port lokalerAnschluss = nic.getPort();
        Port[] ports = lokalerAnschluss.getVerbindung().getAnschluesse();
        Port entfernterAnschluss;
        if (ports[0] == lokalerAnschluss) {
            entfernterAnschluss = ports[1];
        } else {
            entfernterAnschluss = ports[0];
        }

        for (GUIKnotenItem node : GUIContainer.getGUIContainer().getKnotenItems()) {
            if (node.getKnoten() instanceof LokalerKnoten) {
                for (Port port : ((LokalerKnoten) node.getKnoten()).getAnschluesse()) {
                    if (port == entfernterAnschluss)
                        return node.getKnoten();
                }
            } else if (node.getKnoten() instanceof InternetKnoten) {
                for (NetzwerkInterface tmpNic : ((InternetKnoten) node.getKnoten()).getNetzwerkInterfaces()) {
                    if (tmpNic.getPort() == entfernterAnschluss)
                        return node.getKnoten();
                }
            } else {
                LOG.debug("Knotentyp unbekannt.");
            }
        }
        return null;
    }

    private boolean checkRetentionTime() {
        return ueberpruefen(EingabenUeberpruefung.musterNurZahlen, retentionTime);
    }
}
