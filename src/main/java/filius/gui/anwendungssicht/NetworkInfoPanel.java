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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import filius.hardware.NetzwerkInterface;
import filius.hardware.knoten.Host;
import filius.rahmenprogramm.EingabenUeberpruefung;
import filius.rahmenprogramm.I18n;
import filius.software.system.Betriebssystem;

/**
 * Die Klasse stellt einen Dialog dar, in dem der Nutzer die Netzwerkeinstellungen des aktuellen Rechners betrachten und
 * editieren kann, waehrend er im Anwendungsmodus ist.
 * 
 * @author Thomas Gerding
 * 
 */
@SuppressWarnings("serial")
public class NetworkInfoPanel extends JPanel implements I18n {

    private GUIDesktopPanel dp;
    private JLabel ipLabel, dnsLabel, gatewayLabel, netmaskLabel, macLabel;
    private JTextField ipField, dnsField, gatewayField, netmaskField, macField;
    private JButton changeButton;
    private Betriebssystem bs;
    private boolean istGueltig = true;

    public NetworkInfoPanel(final GUIDesktopPanel dp) {
        super(new BorderLayout());

        NetzwerkInterface nic;

        this.dp = dp;

        ipLabel = new JLabel(messages.getString("network_msg1"));
        ipLabel.setSize(new Dimension(150, 15));
        ipLabel.setPreferredSize(new Dimension(150, 15));
        dnsLabel = new JLabel(messages.getString("network_msg2"));
        dnsLabel.setSize(new Dimension(150, 15));
        dnsLabel.setPreferredSize(new Dimension(150, 15));
        gatewayLabel = new JLabel(messages.getString("network_msg3"));
        gatewayLabel.setSize(new Dimension(150, 15));
        gatewayLabel.setPreferredSize(new Dimension(150, 15));
        netmaskLabel = new JLabel(messages.getString("network_msg4"));
        netmaskLabel.setSize(new Dimension(150, 15));
        netmaskLabel.setPreferredSize(new Dimension(150, 15));
        macLabel = new JLabel(messages.getString("network_msg9"));
        macLabel.setSize(new Dimension(150, 15));
        macLabel.setPreferredSize(new Dimension(150, 15));

        bs = this.dp.getBetriebssystem();
        nic = (NetzwerkInterface) ((Host) bs.getKnoten()).getNetzwerkInterfaces().get(0);

        ipField = new JTextField(nic.getIp());
        ipField.setEditable(false);
        ipField.setSize(new Dimension(150, 15));
        ipField.setPreferredSize(new Dimension(150, 15));
        ipField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                ueberpruefen(EingabenUeberpruefung.musterIpAdresse, ipField);
            }

        });
        dnsField = new JTextField(bs.getDNSServer());
        dnsField.setEditable(false);
        dnsField.setSize(new Dimension(150, 15));
        dnsField.setPreferredSize(new Dimension(150, 15));
        dnsField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                ueberpruefen(EingabenUeberpruefung.musterIpAdresse, dnsField);
            }

        });
        gatewayField = new JTextField(bs.getStandardGateway());
        gatewayField.setEditable(false);
        gatewayField.setSize(new Dimension(150, 15));
        gatewayField.setPreferredSize(new Dimension(150, 15));
        gatewayField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                ueberpruefen(EingabenUeberpruefung.musterIpAdresse, gatewayField);
            }

        });
        netmaskField = new JTextField(nic.getSubnetzMaske());
        netmaskField.setEditable(false);
        netmaskField.setSize(new Dimension(150, 15));
        netmaskField.setPreferredSize(new Dimension(150, 15));
        netmaskField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                ueberpruefen(EingabenUeberpruefung.musterIpAdresse, netmaskField);
            }

        });

        macField = new JTextField(nic.getMac());
        macField.setEditable(false);
        macField.setSize(new Dimension(150, 15));
        macField.setPreferredSize(new Dimension(150, 15));

        changeButton = new JButton(messages.getString("network_msg5"));
        changeButton.setToolTipText(messages.getString("network_msg6"));
        changeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                istGueltig = true; // Um den nächsten Testdurchlauf zu
                                   // ermöglichen

                if (!EingabenUeberpruefung.isGueltig(ipField.getText(), EingabenUeberpruefung.musterIpAdresse)
                        || istGueltig == false)
                    istGueltig = false;
                if (!EingabenUeberpruefung.isGueltig(netmaskField.getText(), EingabenUeberpruefung.musterIpAdresse)
                        || istGueltig == false)
                    istGueltig = false;
                if (!EingabenUeberpruefung.isGueltig(dnsField.getText(), EingabenUeberpruefung.musterIpAdresse)
                        || istGueltig == false)
                    istGueltig = false;
                if (!EingabenUeberpruefung.isGueltig(gatewayField.getText(), EingabenUeberpruefung.musterIpAdresse)
                        || istGueltig == false)
                    istGueltig = false;

                if (istGueltig == true) {
                    bs.setzeIPAdresse(ipField.getText());
                    bs.setzeNetzmaske(netmaskField.getText());
                    bs.setDNSServer(dnsField.getText());
                    bs.setStandardGateway(gatewayField.getText());
                } else {
                    JOptionPane.showMessageDialog(dp, messages.getString("network_msg7"));
                }

            }

        });

        Box backBox = Box.createVerticalBox();

        Box ipBox = Box.createHorizontalBox();
        ipBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        ipBox.add(ipLabel);
        ipBox.add(Box.createHorizontalStrut(5));
        ipBox.add(ipField);

        Box maskBox = Box.createHorizontalBox();
        maskBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        maskBox.add(netmaskLabel);
        maskBox.add(Box.createHorizontalStrut(5));
        maskBox.add(netmaskField);

        Box dnsBox = Box.createHorizontalBox();
        dnsBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        dnsBox.add(dnsLabel);
        dnsBox.add(Box.createHorizontalStrut(5));
        dnsBox.add(dnsField);

        Box gateBox = Box.createHorizontalBox();
        gateBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        gateBox.add(gatewayLabel);
        gateBox.add(Box.createHorizontalStrut(5));
        gateBox.add(gatewayField);

        Box macBox = Box.createHorizontalBox();
        macBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        macBox.add(macLabel);
        macBox.add(Box.createHorizontalStrut(5));
        macBox.add(macField);

        Box buttonBox = Box.createHorizontalBox();
        buttonBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        buttonBox.add(changeButton);

        backBox.setBorder(BorderFactory.createBevelBorder(2));
        backBox.add(ipBox);
        backBox.add(Box.createHorizontalStrut(5));
        backBox.add(maskBox);
        backBox.add(Box.createHorizontalStrut(5));
        backBox.add(gateBox);
        backBox.add(Box.createHorizontalStrut(5));
        backBox.add(dnsBox);
        backBox.add(Box.createHorizontalStrut(10));
        backBox.add(macBox);
        backBox.add(Box.createHorizontalStrut(5));
        // backBox.add(buttonBox);

        add(backBox, BorderLayout.CENTER);
    }

    /**
     * Funktion die waehrend der Eingabe ueberprueft ob die bisherige Eingabe einen korrekten Wert darstellt.
     * 
     * @author Johannes Bade & Thomas Gerding
     * @param pruefRegel
     * @param feld
     */
    public void ueberpruefen(Pattern pruefRegel, JTextField feld) {
        if (EingabenUeberpruefung.isGueltig(feld.getText(), pruefRegel)) {
            feld.setForeground(EingabenUeberpruefung.farbeRichtig);
            JTextField test = new JTextField();
            feld.setBorder(test.getBorder());
        } else {
            feld.setForeground(EingabenUeberpruefung.farbeFalsch);

            feld.setForeground(EingabenUeberpruefung.farbeFalsch);
            feld.setBorder(BorderFactory.createLineBorder(EingabenUeberpruefung.farbeFalsch, 1));
        }

    }

    public void setVisible(boolean b) {
        if (b) {
            updateInfo();
        }
        super.setVisible(b);
    }

    public void updateInfo() {
        // bring data up-to-date:
        bs = this.dp.getBetriebssystem();
        NetzwerkInterface nic = (NetzwerkInterface) ((Host) bs.getKnoten()).getNetzwerkInterfaces().get(0);

        ipField.setText(nic.getIp());
        dnsField.setText(bs.getDNSServer());
        gatewayField.setText(bs.getStandardGateway());
        netmaskField.setText(nic.getSubnetzMaske());
    }

}
