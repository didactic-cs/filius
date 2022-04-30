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

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
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
import filius.software.system.GatewayFirmware;

@SuppressWarnings("serial")
public class JGatewayConfiguration extends JKonfiguration implements I18n {
    private static Logger LOG = LoggerFactory.getLogger(JGatewayConfiguration.class);

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

    private JTabbedPane tpNetzwerkKarten;

    private Kabel highlightedCable = null;

    protected JGatewayConfiguration(Hardware hardware) {
        super(hardware);
    }

    public void aenderungenAnnehmen() {
        Gateway gateway = (Gateway) holeHardware();
        GatewayFirmware firmware = (GatewayFirmware) gateway.getSystemSoftware();

        gateway.setName(name.getText());
        firmware.setStandardGateway(gatewayName.getText());

        applyNICConfig(gateway.holeWANInterface(), ipAddressWANPort, netmaskWANPort);
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

    protected void initAttributEingabeBox(Box box, Box rightBox) {
        Box boxNetzwerkKarten;
        Box vBox;
        KeyAdapter ipAdresseKeyAdapter;
        KeyAdapter netzmaskeKeyAdapter;
        FocusListener focusListener;
        ActionListener actionListener;
        JButton btFirewall;

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

        ipAddressWANPort = new JTextField();
        netmaskWANPort = new JTextField();
        macAddressWANPort = new JTextField();
        connectedComponentWAN = new JLabel();
        initNICConfigPane(ipAdresseKeyAdapter, netzmaskeKeyAdapter, focusListener, actionListener, ipAddressWANPort,
                macAddressWANPort, netmaskWANPort, connectedComponentWAN);

        ipAddressLANPort = new JTextField();
        netmaskLANPort = new JTextField();
        macAddressLANPort = new JTextField();
        connectedComponentLAN = new JLabel();
        initNICConfigPane(ipAdresseKeyAdapter, netzmaskeKeyAdapter, focusListener, actionListener, ipAddressLANPort,
                macAddressLANPort, netmaskLANPort, connectedComponentLAN);

        tpNetzwerkKarten.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent arg0) {
                JTabbedPane pane = (JTabbedPane) arg0.getSource();
                // Get current tab
                int sel = pane.getSelectedIndex();
                if (highlightedCable != null) {
                    highlightedCable.setAktiv(false);
                }
                if (sel > 0 && sel <= pane.getComponentCount() - 1) {
                    Verbindung conn = ((NetzwerkInterface) ((Gateway) holeHardware()).getNetzwerkInterfaces()
                            .get(sel - 1)).getPort().getVerbindung();
                    if (conn != null) {
                        conn.setAktiv(true);
                        highlightedCable = (Kabel) conn;
                    }
                }
            }

        });

        box.add(boxNetzwerkKarten);

        updateAttribute();
    }

    private void initNICConfigPane(KeyAdapter ipAdresseKeyAdapter, KeyAdapter netzmaskeKeyAdapter,
            FocusListener focusListener, ActionListener actionListener, JTextField ipAddressTextfield,
            JTextField macAddressTextfield, JTextField netmaskTextfield, JLabel connectedComponentLabel) {
        Box boxNic;
        Box boxIpAdresse;
        Box boxSubnetz;
        Box boxMacAdresse;
        Box boxKomponente;
        JLabel tempLabel;
        boxNic = Box.createVerticalBox();

        boxKomponente = Box.createHorizontalBox();
        boxKomponente.setMaximumSize(new Dimension(400, 40));

        connectedComponentLabel.setPreferredSize(new Dimension(400, 10));
        boxKomponente.add(connectedComponentLabel);

        // show IP address (editable)
        boxIpAdresse = Box.createHorizontalBox();
        boxIpAdresse.setMaximumSize(new Dimension(400, 40));
        tempLabel = new JLabel(messages.getString("jgatewayconfiguration_msg7"));
        tempLabel.setPreferredSize(new Dimension(120, 10));
        boxIpAdresse.add(tempLabel);

        boxIpAdresse.add(ipAddressTextfield);

        // show netmask (editable)
        boxSubnetz = Box.createHorizontalBox();
        boxSubnetz.setMaximumSize(new Dimension(400, 40));
        tempLabel = new JLabel(messages.getString("jgatewayconfiguration_msg8"));
        tempLabel.setPreferredSize(new Dimension(120, 10));
        boxSubnetz.add(tempLabel);

        boxSubnetz.add(netmaskTextfield);

        // show MAC address (not editable)
        boxMacAdresse = Box.createHorizontalBox();
        boxMacAdresse.setMaximumSize(new Dimension(400, 40));
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

        updateNICConfigPane("WAN", vRechner.holeWANInterface(), ipAddressWANPort, macAddressWANPort, netmaskWANPort,
                connectedComponentWAN, 1);
        updateNICConfigPane("LAN", vRechner.holeLANInterface(), ipAddressLANPort, macAddressLANPort, netmaskLANPort,
                connectedComponentLAN, 2);
    }

    private void updateNICConfigPane(String titlePrefix, NetzwerkInterface nic, JTextField ipAddressTextfield,
            JTextField macAddressTextfield, JTextField netmaskTextfield, JLabel connectedComponentTextfield,
            int tabIdx) {
        ipAddressTextfield.setText(nic.getIp());
        netmaskTextfield.setText(nic.getSubnetzMaske());
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
}
