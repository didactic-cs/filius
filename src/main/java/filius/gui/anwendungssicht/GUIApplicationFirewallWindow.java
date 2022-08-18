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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import filius.gui.JExtendedTable;
import filius.software.firewall.Firewall;
import filius.software.firewall.FirewallRule;

/**
 * Applikationsfenster für die Firewall
 * 
 * @author Johannes Bade & Thomas Gerding
 * 
 */
@SuppressWarnings("serial")
public class GUIApplicationFirewallWindow extends GUIApplicationWindow {

    private static final String ALL_PROT_STRING = "*";

    private static final String UDP_STRING = "UDP";

    private static final String TCP_STRING = "TCP";

    private JTable tTabellePort;

    private JTextField tfPort;

    private JComboBox cbProtocol;
    private JComboBox cbAlleAbsender;

    private JTextArea log; // Log-Fenster

    private JCheckBox cbEinAus = new JCheckBox();
    private JCheckBox cbIcmp = new JCheckBox();
    private JCheckBox cbUdp = new JCheckBox();

    private void initKomponenten() {
        JEditorPane text;
        JLabel label;
        JButton button;
        TableColumnModel columnModel;
        JPanel panel;
        JScrollPane scrollPane;
        Box boxFirewall;
        JTabbedPane tp;

        boxFirewall = Box.createVerticalBox();
        boxFirewall.add(Box.createVerticalStrut(10));

        JPanel globalBox = new JPanel(new GridBagLayout());

        cbEinAus = new JCheckBox(messages.getString("firewall_msg1"));
        cbEinAus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                ((Firewall) holeAnwendung()).setActivated(cbEinAus.isSelected());
                updateAttribute();
            }
        });
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        globalBox.add(cbEinAus, constraints);

        cbIcmp = new JCheckBox(messages.getString("firewall_msg13"));
        cbIcmp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                ((Firewall) holeAnwendung()).setDropICMP(!cbIcmp.isSelected());
                updateAttribute();
            }
        });
        globalBox.add(cbIcmp, constraints);

        cbUdp = new JCheckBox(messages.getString("firewall_msg14"));
        cbUdp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                ((Firewall) holeAnwendung()).setFilterUdp(cbUdp.isSelected());
                updateAttribute();
            }
        });
        globalBox.add(cbUdp, constraints);

        boxFirewall.add(globalBox);
        boxFirewall.add(Box.createVerticalStrut(10));

        text = new JEditorPane();
        text.setEditable(false);
        text.setText(messages.getString("firewall_msg2"));
        text.setBackground(boxFirewall.getBackground());

        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalStrut(10));

        label = new JLabel(messages.getString("firewall_msg3"));
        box.add(label);
        box.add(Box.createHorizontalStrut(10));

        tfPort = new JTextField();
        tfPort.setPreferredSize(new Dimension(30, 15));
        box.add(tfPort);
        box.add(Box.createHorizontalStrut(10));

        String[] protValues = { TCP_STRING, UDP_STRING, ALL_PROT_STRING };
        cbProtocol = new JComboBox(protValues);
        box.add(cbProtocol);
        box.add(Box.createHorizontalStrut(10));

        cbAlleAbsender = new JComboBox();
        cbAlleAbsender.addItem(messages.getString("firewall_msg4"));
        cbAlleAbsender.addItem(messages.getString("firewall_msg5"));
        cbAlleAbsender.setSelectedIndex(0);
        box.add(cbAlleAbsender);
        box.add(Box.createHorizontalStrut(10));

        boxFirewall.add(box);
        boxFirewall.add(Box.createVerticalStrut(10));

        box = Box.createHorizontalBox();
        box.add(Box.createHorizontalStrut(10));

        button = new JButton(messages.getString("firewall_msg6"));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hinzuRegel();
                updateAttribute();
            }
        });
        box.add(button);
        box.add(Box.createHorizontalStrut(10));

        button = new JButton(messages.getString("firewall_msg7"));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loescheRegel();
                updateAttribute();
            }
        });
        box.add(button);
        box.add(Box.createHorizontalStrut(10));

        boxFirewall.add(box);
        boxFirewall.add(Box.createVerticalStrut(10));

        DefaultTableModel tabellenModell = new DefaultTableModel(0, 3);
        tTabellePort = new JExtendedTable(tabellenModell, false);
        tTabellePort.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tTabellePort.setIntercellSpacing(new Dimension(10, 5));
        tTabellePort.setRowHeight(30);
        tTabellePort.setShowGrid(false);
        tTabellePort.setFillsViewportHeight(true);
        tTabellePort.setBackground(Color.WHITE);
        tTabellePort.setShowHorizontalLines(true);

        columnModel = tTabellePort.getColumnModel();
        columnModel.getColumn(0).setHeaderValue(messages.getString("jfirewalldialog_msg31"));
        columnModel.getColumn(0).setWidth(20);
        columnModel.getColumn(1).setHeaderValue(messages.getString("firewall_msg8"));
        columnModel.getColumn(1).setWidth(20);
        columnModel.getColumn(2).setHeaderValue(messages.getString("firewall_msg9"));
        columnModel.getColumn(2).setWidth(300);

        scrollPane = new JScrollPane(tTabellePort);
        scrollPane.setPreferredSize(new Dimension(90, 250));

        boxFirewall.add(scrollPane);
        boxFirewall.add(Box.createVerticalStrut(10));

        tp = new JTabbedPane();
        tp.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent arg0) {
                updateAttribute();
            }
        });

        panel = new JPanel(new BorderLayout());
        panel.add(boxFirewall, BorderLayout.CENTER);
        tp.add(messages.getString("firewall_msg10"), panel);

        log = new JTextArea();
        scrollPane = new JScrollPane(log);
        tp.add(messages.getString("firewall_msg11"), scrollPane);

        add(tp, BorderLayout.CENTER);
    }

    private void hinzuRegel() {
        try {
            int port = Integer.parseInt(tfPort.getText());
            boolean unterscheideNetzwerk = (cbAlleAbsender.getSelectedIndex() == 1);
            short protocol = cbProtocol.getSelectedIndex() == 0 ? FirewallRule.TCP
                    : cbProtocol.getSelectedIndex() == 1 ? FirewallRule.UDP : FirewallRule.ALL_PROTOCOLS;

            FirewallRule newRule = new FirewallRule("", "", "", "", port, protocol, FirewallRule.ACCEPT);
            if (unterscheideNetzwerk) {
                newRule.srcIP = FirewallRule.SAME_NETWORK;
            }
            ((Firewall) holeAnwendung()).addRule(newRule);
            tfPort.setText("");
            log.append(messages.getString("sw_firewall_msg5") + " " + port + "/" + cbProtocol.getSelectedItem() + "\n");
        } catch (Exception e) {}
        updateAttribute();
    }

    private void loescheRegel() {
        if (tTabellePort.getSelectedRow() != -1) {
            ((Firewall) holeAnwendung()).deleteRule(tTabellePort.getSelectedRow());
            log.append(messages.getString("sw_firewall_msg7") + "\n");
        }
        updateAttribute();
    }

    public GUIApplicationFirewallWindow(final GUIDesktopPanel desktop, String appName) {
        super(desktop, appName);

        initKomponenten();
        updateAttribute();
    }

    public void updateAttribute() {
        DefaultTableModel model;

        model = (DefaultTableModel) tTabellePort.getModel();
        model.setRowCount(0);

        Vector<FirewallRule> ruleset = ((Firewall) holeAnwendung()).getRuleset();
        for (int i = 0; i < ruleset.size(); i++) {
            model.addRow(ruleToVector(ruleset.get(i)));
        }

        cbEinAus.setSelected(((Firewall) holeAnwendung()).isActivated());
        cbIcmp.setSelected(!((Firewall) holeAnwendung()).getDropICMP());
        cbIcmp.setEnabled(cbEinAus.isSelected());
        cbUdp.setSelected(((Firewall) holeAnwendung()).getFilterUdp());
        cbUdp.setEnabled(cbEinAus.isSelected());
    }

    private Vector<String> ruleToVector(FirewallRule rule) {
        Vector<String> resultVec = new Vector<String>();
        if (rule.protocol == FirewallRule.TCP) {
            resultVec.addElement(TCP_STRING);
        } else if (rule.protocol == FirewallRule.UDP) {
            resultVec.addElement(UDP_STRING);
        } else {
            resultVec.addElement(ALL_PROT_STRING);
        }
        if (rule.port >= 0) {
            resultVec.addElement(Integer.toString(rule.port));
        } else {
            resultVec.addElement("");
        }
        if (rule.srcIP.equals(FirewallRule.SAME_NETWORK)) {
            resultVec.addElement(messages.getString("firewall_msg12"));
        } else {
            resultVec.addElement(messages.getString("firewall_msg4"));
        }
        return resultVec;
    }

    public void update(Observable arg0, Object nachricht) {
        log.append(nachricht + "\n");
    }

}
