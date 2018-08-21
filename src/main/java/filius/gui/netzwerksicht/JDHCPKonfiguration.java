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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import filius.Main;
import filius.gui.anwendungssicht.JTableEditable;
import filius.rahmenprogramm.EingabenUeberpruefung;
import filius.rahmenprogramm.I18n;
import filius.software.dhcp.DHCPAddressAssignment;
import filius.software.dhcp.DHCPServer;
import filius.software.system.Betriebssystem;

public class JDHCPKonfiguration extends JDialog implements I18n, ItemListener {

    private static final long serialVersionUID = 1L;
    private DHCPServer server;
    private JTextField tfObergrenze;
    private JTextField tfUntergrenze;
    private JTextField tfNetzmaske;
    private JTextField tfGateway;
    private JTextField tfDNSServer;
    private JCheckBox cbAktiv;
    private JCheckBox cbUseInternal;
    private JTabbedPane tabbedPane;
    protected JTable staticAddressTable;

    public JDHCPKonfiguration(JFrame owner, String titel, Betriebssystem bs) {
        super(owner, titel, true);
        this.server = bs.getDHCPServer();

        this.setSize(380, 380);
        this.setResizable(false);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Point location = new Point((screen.width / 2) - 190, (screen.height / 2) - 140);
        this.setLocation(location);

        initComponents();
    }

    private void initComponents() {
        JPanel jpDhcp;
        JLabel lbObergrenze;
        JLabel lbUntergrenze;
        JLabel lbNetzmaske;
        JLabel lbGateway;
        JLabel lbDNSServer;
        JButton btUebernehmen;

        JLabel lbAktiv;
        JLabel lbUseInternal;
        final JDialog config = this;

        SpringLayout layout = new SpringLayout();
        jpDhcp = new JPanel(layout);

        lbUntergrenze = new JLabel(messages.getString("jdhcpkonfiguration_msg1"));
        tfUntergrenze = new JTextField(server.getUntergrenze());
        tfUntergrenze.setPreferredSize(new Dimension(150, 25));
        tfUntergrenze.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                ueberpruefen(EingabenUeberpruefung.musterIpAdresse, tfUntergrenze);
            }
        });

        lbObergrenze = new JLabel(messages.getString("jdhcpkonfiguration_msg2"));
        tfObergrenze = new JTextField(server.getObergrenze());
        tfObergrenze.setPreferredSize(new Dimension(150, 25));
        tfObergrenze.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                ueberpruefen(EingabenUeberpruefung.musterIpAdresse, tfObergrenze);
            }
        });

        lbNetzmaske = new JLabel(messages.getString("jdhcpkonfiguration_msg3"));
        tfNetzmaske = new JTextField(server.getSubnetzmaske());
        tfNetzmaske.setPreferredSize(new Dimension(150, 25));
        tfNetzmaske.setEditable(false);

        lbGateway = new JLabel(messages.getString("jdhcpkonfiguration_msg4"));
        tfGateway = new JTextField(server.getGatewayip());
        tfGateway.setPreferredSize(new Dimension(150, 25));
        tfGateway.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                ueberpruefen(EingabenUeberpruefung.musterIpAdresse, tfGateway);
            }
        });
        tfGateway.setEditable(server.isOwnSettings());

        lbDNSServer = new JLabel(messages.getString("jdhcpkonfiguration_msg5"));
        tfDNSServer = new JTextField(server.getDnsserverip());
        tfDNSServer.setPreferredSize(new Dimension(150, 25));
        tfDNSServer.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                ueberpruefen(EingabenUeberpruefung.musterIpAdresse, tfDNSServer);
            }
        });
        tfDNSServer.setEditable(server.isOwnSettings());

        jpDhcp.add(lbUntergrenze);
        jpDhcp.add(lbObergrenze);
        jpDhcp.add(lbNetzmaske);

        JPanel borderPanel = new JPanel(); // Panel used to paint border around
                                           // gateway/DNS form field
        borderPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        borderPanel.setOpaque(false);
        jpDhcp.add(borderPanel);

        jpDhcp.add(tfUntergrenze);
        jpDhcp.add(tfObergrenze);
        jpDhcp.add(tfNetzmaske);

        jpDhcp.add(lbGateway);
        jpDhcp.add(lbDNSServer);
        jpDhcp.add(tfGateway);
        jpDhcp.add(tfDNSServer);

        btUebernehmen = new JButton(messages.getString("jdhcpkonfiguration_msg7"));
        btUebernehmen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                speichern();

                update();
                config.setVisible(false);
            }
        });
        btUebernehmen.setSize(new Dimension(50, 30));

        lbAktiv = new JLabel(messages.getString("jdhcpkonfiguration_msg6"));
        lbAktiv.setPreferredSize(new Dimension(200, 15));
        cbAktiv = new JCheckBox();
        cbAktiv.setSelected(server.isAktiv());

        lbUseInternal = new JLabel(messages.getString("jdhcpkonfiguration_msg8"));
        lbUseInternal.setToolTipText(messages.getString("jdhcpkonfiguration_msg9"));
        cbUseInternal = new JCheckBox();
        cbUseInternal.addItemListener(this);
        cbUseInternal.setToolTipText(messages.getString("jdhcpkonfiguration_msg9"));
        cbUseInternal.setSelected(server.isOwnSettings());

        jpDhcp.add(lbUseInternal);
        jpDhcp.add(cbUseInternal);

        jpDhcp.add(lbAktiv);
        jpDhcp.add(cbAktiv);

        /* Layout. Set positions with Constraints. */
        // Labels:
        layout.putConstraint(SpringLayout.NORTH, lbUntergrenze, 20, SpringLayout.NORTH, this.getContentPane());
        layout.putConstraint(SpringLayout.WEST, lbUntergrenze, 25, SpringLayout.WEST, this.getContentPane());

        layout.putConstraint(SpringLayout.NORTH, lbObergrenze, 20, SpringLayout.SOUTH, lbUntergrenze);
        layout.putConstraint(SpringLayout.WEST, lbObergrenze, 25, SpringLayout.WEST, this.getContentPane());

        layout.putConstraint(SpringLayout.NORTH, lbNetzmaske, 20, SpringLayout.SOUTH, lbObergrenze);
        layout.putConstraint(SpringLayout.WEST, lbNetzmaske, 25, SpringLayout.WEST, this.getContentPane());

        layout.putConstraint(SpringLayout.NORTH, lbGateway, 30, SpringLayout.SOUTH, lbNetzmaske);
        layout.putConstraint(SpringLayout.WEST, lbGateway, 30, SpringLayout.WEST, this.getContentPane());

        layout.putConstraint(SpringLayout.NORTH, lbDNSServer, 20, SpringLayout.SOUTH, lbGateway);
        layout.putConstraint(SpringLayout.WEST, lbDNSServer, 30, SpringLayout.WEST, this.getContentPane());

        // Textfields:
        layout.putConstraint(SpringLayout.NORTH, tfUntergrenze, 0, SpringLayout.NORTH, lbUntergrenze);
        layout.putConstraint(SpringLayout.WEST, tfUntergrenze, 200, SpringLayout.WEST, this.getContentPane());

        layout.putConstraint(SpringLayout.NORTH, tfObergrenze, 0, SpringLayout.NORTH, lbObergrenze);
        layout.putConstraint(SpringLayout.WEST, tfObergrenze, 200, SpringLayout.WEST, this.getContentPane());

        layout.putConstraint(SpringLayout.NORTH, tfNetzmaske, 0, SpringLayout.NORTH, lbNetzmaske);
        layout.putConstraint(SpringLayout.WEST, tfNetzmaske, 200, SpringLayout.WEST, this.getContentPane());

        layout.putConstraint(SpringLayout.NORTH, tfGateway, 0, SpringLayout.NORTH, lbGateway);
        layout.putConstraint(SpringLayout.WEST, tfGateway, 195, SpringLayout.WEST, this.getContentPane());

        layout.putConstraint(SpringLayout.NORTH, tfDNSServer, 0, SpringLayout.NORTH, lbDNSServer);
        layout.putConstraint(SpringLayout.WEST, tfDNSServer, 195, SpringLayout.WEST, this.getContentPane());

        layout.putConstraint(SpringLayout.EAST, lbUseInternal, 0, SpringLayout.EAST, tfDNSServer);
        layout.putConstraint(SpringLayout.NORTH, lbUseInternal, 10, SpringLayout.SOUTH, tfDNSServer);
        layout.putConstraint(SpringLayout.EAST, cbUseInternal, 0, SpringLayout.WEST, lbUseInternal);
        layout.putConstraint(SpringLayout.SOUTH, cbUseInternal, 4, SpringLayout.SOUTH, lbUseInternal);

        /* Layout */
        layout.putConstraint(SpringLayout.NORTH, cbAktiv, 25, SpringLayout.SOUTH, lbUseInternal);
        layout.putConstraint(SpringLayout.WEST, cbAktiv, 25, SpringLayout.WEST, this.getContentPane());

        layout.putConstraint(SpringLayout.NORTH, lbAktiv, 4, SpringLayout.NORTH, cbAktiv);
        layout.putConstraint(SpringLayout.WEST, lbAktiv, 4, SpringLayout.EAST, cbAktiv);

        layout.putConstraint(SpringLayout.NORTH, borderPanel, 10, SpringLayout.SOUTH, tfNetzmaske);
        layout.putConstraint(SpringLayout.WEST, borderPanel, 25, SpringLayout.WEST, this.getContentPane());

        borderPanel.setPreferredSize(new Dimension(325, 105));

        tabbedPane = new JTabbedPane();
        tabbedPane.add(messages.getString("jdhcpkonfiguration_msg10"), jpDhcp);
        tabbedPane.setPreferredSize(new Dimension(360, 320));
        getContentPane().add(tabbedPane, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setSize(360, 40);
        buttonPanel.add(btUebernehmen, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        tabbedPane.add(messages.getString("jdhcpkonfiguration_msg11"), createStaticConfigPanel());

        update();
    }

    private JPanel createStaticConfigPanel() {
        Box vBox, hBox;
        DefaultTableModel tabellenModell;
        TableColumnModel tcm;
        JScrollPane scrollPane;

        JPanel staticConfigPanel = new JPanel(new BorderLayout());

        vBox = Box.createVerticalBox();
        vBox.add(Box.createVerticalStrut(5));

        hBox = Box.createHorizontalBox();
        hBox.add(Box.createHorizontalStrut(5));

        JLabel macAddressLabel = new JLabel(messages.getString("jdhcpkonfiguration_msg12"));
        macAddressLabel.setPreferredSize(new Dimension(170, 25));
        hBox.add(macAddressLabel);
        hBox.add(Box.createHorizontalStrut(5));

        JTextField macAddressTextField = new JTextField();
        macAddressTextField.setPreferredSize(new Dimension(275, 25));
        macAddressTextField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                ueberpruefen(EingabenUeberpruefung.musterMacAddress, macAddressTextField);
            }
        });
        hBox.add(macAddressTextField);

        vBox.add(hBox);
        vBox.add(Box.createVerticalStrut(5));

        hBox = Box.createHorizontalBox();
        hBox.add(Box.createHorizontalStrut(5));

        JLabel ipAddressLabel = new JLabel(messages.getString("jdhcpkonfiguration_msg13"));
        ipAddressLabel.setPreferredSize(new Dimension(170, 25));
        hBox.add(ipAddressLabel);
        hBox.add(Box.createHorizontalStrut(5));

        JTextField ipAddressTextField = new JTextField();
        ipAddressTextField.setPreferredSize(new Dimension(275, 25));
        ipAddressTextField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                ueberpruefen(EingabenUeberpruefung.musterIpAdresse, ipAddressTextField);
            }
        });
        hBox.add(ipAddressTextField);

        vBox.add(hBox);
        vBox.add(Box.createVerticalStrut(5));

        hBox = Box.createHorizontalBox();
        hBox.add(Box.createHorizontalStrut(5));

        JButton addButton = new JButton(messages.getString("jdhcpkonfiguration_msg14"));
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if (ueberpruefen(EingabenUeberpruefung.musterMacAddress, macAddressTextField)
                        && ueberpruefen(EingabenUeberpruefung.musterIpAdresse, ipAddressTextField)) {
                    ((DefaultTableModel) staticAddressTable.getModel()).addRow(new Object[] {
                            macAddressTextField.getText(), ipAddressTextField.getText() });
                    macAddressTextField.setText("");
                    ipAddressTextField.setText("");
                }
            }
        });
        hBox.add(addButton);
        hBox.add(Box.createHorizontalStrut(5));

        JButton removeButton = new JButton(messages.getString("jdhcpkonfiguration_msg15"));
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int zeilenNummer = staticAddressTable.getSelectedRow();
                if (zeilenNummer != -1) {
                    List<String> macAddresses = new ArrayList<>();
                    List<String> ipAddresses = new ArrayList<>();
                    for (int i = 0; i < staticAddressTable.getModel().getRowCount(); i++) {
                        if (i != zeilenNummer) {
                            macAddresses.add((String) staticAddressTable.getModel().getValueAt(i, 0));
                            ipAddresses.add((String) staticAddressTable.getModel().getValueAt(i, 1));
                        }
                    }
                    ((DefaultTableModel) staticAddressTable.getModel()).setRowCount(0);
                    for (int i = 0; i < macAddresses.size() && i < ipAddresses.size(); i++) {
                        ((DefaultTableModel) staticAddressTable.getModel()).addRow(new Object[] { macAddresses.get(i),
                                ipAddresses.get(i) });
                    }
                }
            }
        });
        hBox.add(removeButton);

        vBox.add(hBox);
        vBox.add(Box.createVerticalStrut(5));

        tabellenModell = new DefaultTableModel(0, 2);
        staticAddressTable = new JTableEditable(tabellenModell, false);
        staticAddressTable.setIntercellSpacing(new Dimension(5, 5));
        staticAddressTable.setRowHeight(30);
        staticAddressTable.setShowGrid(false);
        staticAddressTable.setFillsViewportHeight(true);
        staticAddressTable.setBackground(Color.WHITE);
        staticAddressTable.setShowHorizontalLines(true);

        tcm = staticAddressTable.getColumnModel();
        tcm.getColumn(0).setHeaderValue(messages.getString("jdhcpkonfiguration_msg12"));
        tcm.getColumn(1).setHeaderValue(messages.getString("jdhcpkonfiguration_msg13"));
        scrollPane = new JScrollPane(staticAddressTable);

        vBox.add(scrollPane);
        staticConfigPanel.add(vBox, BorderLayout.CENTER);
        return staticConfigPanel;
    }

    /** Listens to the check boxes. */
    public void itemStateChanged(java.awt.event.ItemEvent e) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass()
                + " (JDHCPKonfiguration) itemStateChanged(" + e + "); source=" + e.getItemSelectable());
        Object source = e.getItemSelectable();

        if (source == cbUseInternal) {
            // Main.debug.println("\titemStateChanged; source==cbUseInternal");
            if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                server.setOwnSettings(true);
                tfGateway.setText(server.getGatewayip());
                tfGateway.setEditable(true);
                tfDNSServer.setText(server.getDnsserverip());
                tfDNSServer.setEditable(true);
            } else {
                server.setOwnSettings(false);
                tfGateway.setText(server.getGatewayip());
                tfGateway.setEditable(false);
                tfDNSServer.setText(server.getDnsserverip());
                tfDNSServer.setEditable(false);
            }
        } else {
            // Main.debug.println("\titemStateChanged; source ("+source+") != cbUseInternal ("+cbUseInternal+")");
        }
    }

    private void update() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (JDHCPKonfiguration), update()");
        tfObergrenze.setText(server.getObergrenze());
        tfUntergrenze.setText(server.getUntergrenze());
        tfNetzmaske.setText(server.getSubnetzmaske());
        tfGateway.setText(server.getGatewayip());
        tfDNSServer.setText(server.getDnsserverip());
        cbUseInternal.setSelected(server.isOwnSettings());
        cbAktiv.setSelected(server.isAktiv());
        ((DefaultTableModel) staticAddressTable.getModel()).setRowCount(0);
        for (DHCPAddressAssignment entry : server.holeStaticAssignedAddresses()) {
            ((DefaultTableModel) staticAddressTable.getModel()).addRow(new Object[] { entry.getMAC(), entry.getIp() });
        }
    }

    public boolean ueberpruefen(Pattern pruefRegel, JTextField feld) {
        if (EingabenUeberpruefung.isGueltig(feld.getText(), pruefRegel)) {
            feld.setForeground(EingabenUeberpruefung.farbeRichtig);
            JTextField test = new JTextField();
            feld.setBorder(test.getBorder());
            return true;
        } else {
            feld.setForeground(EingabenUeberpruefung.farbeFalsch);

            feld.setForeground(EingabenUeberpruefung.farbeFalsch);
            feld.setBorder(BorderFactory.createLineBorder(EingabenUeberpruefung.farbeFalsch, 1));
            return false;
        }

    }

    private void speichern() {
        if (ueberpruefen(EingabenUeberpruefung.musterIpAdresse, tfObergrenze))
            server.setObergrenze(tfObergrenze.getText());

        if (ueberpruefen(EingabenUeberpruefung.musterIpAdresse, tfUntergrenze))
            server.setUntergrenze(tfUntergrenze.getText());

        if (cbUseInternal.isSelected()) {
            server.setOwnSettings(true);
            if (ueberpruefen(EingabenUeberpruefung.musterIpAdresse, tfGateway))
                server.setGatewayip(tfGateway.getText());
            if (ueberpruefen(EingabenUeberpruefung.musterIpAdresse, tfDNSServer))
                server.setDnsserverip(tfDNSServer.getText());
        } else {
            server.setOwnSettings(false);
        }

        server.setAktiv(cbAktiv.isSelected());

        server.clearStaticAssignments();
        for (int i = 0; i < staticAddressTable.getRowCount(); i++) {
            server.addStaticAssignment((String) staticAddressTable.getValueAt(i, 0),
                    (String) staticAddressTable.getValueAt(i, 1));
        }
    }

}
