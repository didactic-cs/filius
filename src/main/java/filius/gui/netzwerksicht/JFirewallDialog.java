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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import filius.Main;
import filius.gui.ComboBoxTableCellEditor;
import filius.rahmenprogramm.I18n;
import filius.software.firewall.Firewall;
import filius.software.firewall.FirewallRule;

public class JFirewallDialog extends JDialog implements I18n {

    private static final long serialVersionUID = 1L;

    private static final Color TAB_COLOR = new Color(240, 240, 240);

    JFirewallDialog jfd = null;
    private Firewall firewall;
    DefaultTableModel dtmTabelle;
    DefaultTableModel dtmTabellePort;
    JScrollPane spTabelle;
    JScrollPane spTabellePort;
    Box boxFirewall;
    Box boxTabellen;

    private GatewayFirewallConfigTable ruleTable;
    private JComboBox defaultPolicyCombo;

    private JCheckBox activateFirewall;
    private JCheckBox dropICMP;
    private JCheckBox onlyFilterSYN;

    public JFirewallDialog(Firewall firewall, JFrame dummyFrame) {
        super(dummyFrame, messages.getString("jfirewalldialog_msg1"), true);
        Main.debug.println("INVOKED-2 (" + this.hashCode() + ") " + getClass() + ", constr: JFirewallDialog(" + firewall
                + "," + dummyFrame + ")");
        this.firewall = firewall;
        jfd = this;
        erzeugeFenster();
    }

    private Box erzeugeNicBox() {
        Box vBox, hBox;

        vBox = Box.createVerticalBox();
        vBox.add(Box.createVerticalStrut(10));

        hBox = Box.createHorizontalBox();
        hBox.add(Box.createHorizontalStrut(10));

        activateFirewall = new JCheckBox(messages.getString("jfirewalldialog_msg38"));
        activateFirewall.setOpaque(false);
        activateFirewall.setSelected(firewall.isActivated());
        activateFirewall.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                firewall.setActivated(((JCheckBox) e.getSource()).isSelected());
            }
        });

        hBox.add(activateFirewall);
        hBox.add(Box.createHorizontalGlue());

        vBox.add(hBox);

        JTextArea label = new JTextArea();
        label.setEditable(false);
        label.setLineWrap(true);
        label.setWrapStyleWord(true);
        label.setOpaque(false);
        label.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
        label.setText(messages.getString("jfirewalldialog_msg39"));

        hBox = Box.createHorizontalBox();
        hBox.add(label);
        hBox.add(Box.createHorizontalStrut(10));

        vBox.add(hBox);
        vBox.add(Box.createVerticalStrut(10));

        hBox = Box.createHorizontalBox();
        hBox.add(Box.createHorizontalStrut(10));

        dropICMP = new JCheckBox(messages.getString("jfirewalldialog_msg40"));
        dropICMP.setOpaque(false);
        dropICMP.setSelected(firewall.getDropICMP());
        dropICMP.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                firewall.setDropICMP(((JCheckBox) e.getSource()).isSelected());
            }
        });

        hBox.add(dropICMP);
        hBox.add(Box.createHorizontalGlue());

        vBox.add(hBox);

        label = new JTextArea();
        label.setEditable(false);
        label.setLineWrap(true);
        label.setWrapStyleWord(true);
        label.setOpaque(false);
        label.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
        label.setText(messages.getString("jfirewalldialog_msg41"));

        hBox = Box.createHorizontalBox();
        hBox.add(label);
        hBox.add(Box.createHorizontalStrut(10));

        vBox.add(hBox);
        vBox.add(Box.createVerticalStrut(10));

        hBox = Box.createHorizontalBox();
        hBox.add(Box.createHorizontalStrut(10));

        onlyFilterSYN = new JCheckBox(messages.getString("jfirewalldialog_msg42"));
        onlyFilterSYN.setOpaque(false);
        onlyFilterSYN.setSelected(firewall.getFilterSYNSegmentsOnly());
        onlyFilterSYN.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                firewall.setFilterSYNSegmentsOnly(((JCheckBox) e.getSource()).isSelected());
            }
        });

        hBox.add(onlyFilterSYN);
        hBox.add(Box.createHorizontalGlue());

        vBox.add(hBox);

        label = new JTextArea();
        label.setEditable(false);
        label.setLineWrap(true);
        label.setWrapStyleWord(true);
        label.setOpaque(false);
        label.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
        label.setText(messages.getString("jfirewalldialog_msg43"));

        hBox = Box.createHorizontalBox();
        hBox.add(label);
        hBox.add(Box.createHorizontalStrut(10));

        vBox.add(hBox);
        vBox.add(Box.createVerticalStrut(10));

        vBox.add(Box.createVerticalStrut(1000));

        return vBox;
    }

    private Box firewallRuleBox() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + ", firewallRuleBox()");
        JScrollPane scrollPane;
        Box vBox, hBox;
        DefaultTableModel model;
        TableColumnModel columnModel;
        JButton button;
        JLabel label;
        JTextArea textArea;

        vBox = Box.createVerticalBox();
        vBox.add(Box.createVerticalStrut(10));

        hBox = Box.createHorizontalBox();
        hBox.add(Box.createHorizontalStrut(10));

        textArea = new JTextArea();
        textArea.setText(messages.getString("jfirewalldialog_msg37"));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
        textArea.setEditable(false);
        textArea.setOpaque(false);

        hBox.add(textArea);
        hBox.add(Box.createHorizontalStrut(10));
        vBox.add(hBox);
        vBox.add(Box.createVerticalStrut(10));

        hBox = Box.createHorizontalBox();
        hBox.add(Box.createHorizontalStrut(10));

        label = new JLabel(messages.getString("jfirewalldialog_msg36"));

        hBox.add(label);
        hBox.add(Box.createHorizontalStrut(10));

        defaultPolicyCombo = new JComboBox();
        defaultPolicyCombo.addItem(messages.getString("jfirewalldialog_msg33"));
        defaultPolicyCombo.addItem(messages.getString("jfirewalldialog_msg34"));

        defaultPolicyCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                firewall.setDefaultPolicy(stringToPolicy((String) defaultPolicyCombo.getSelectedItem()));
                updateRuleTable();
            }
        });
        hBox.add(defaultPolicyCombo);
        hBox.add(Box.createHorizontalStrut(10));

        vBox.add(hBox);
        vBox.add(Box.createVerticalStrut(10));

        hBox = Box.createHorizontalBox();
        hBox.add(Box.createHorizontalStrut(10));

        vBox.add(hBox);
        vBox.add(Box.createVerticalStrut(10));

        model = new DefaultTableModel(0, 8);
        ruleTable = new GatewayFirewallConfigTable(model, true);
        ruleTable.setParentGUI(this);
        ruleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ruleTable.setIntercellSpacing(new Dimension(10, 5));
        ruleTable.setRowHeight(30);
        ruleTable.setShowGrid(true);
        ruleTable.setFillsViewportHeight(true);
        ruleTable.setBackground(Color.WHITE);
        ruleTable.setShowHorizontalLines(true);

        columnModel = ruleTable.getColumnModel();
        columnModel.getColumn(0).setHeaderValue(messages.getString("jfirewalldialog_msg26"));
        columnModel.getColumn(0).setCellEditor(null); // ID column must be read-only
        columnModel.getColumn(1).setHeaderValue(messages.getString("jfirewalldialog_msg27"));
        columnModel.getColumn(2).setHeaderValue(messages.getString("jfirewalldialog_msg28"));
        columnModel.getColumn(3).setHeaderValue(messages.getString("jfirewalldialog_msg29"));
        columnModel.getColumn(4).setHeaderValue(messages.getString("jfirewalldialog_msg30"));
        columnModel.getColumn(5).setHeaderValue(messages.getString("jfirewalldialog_msg31"));
        String[] protValues = { "*", "TCP", "UDP" };
        columnModel.getColumn(5).setCellEditor(new ComboBoxTableCellEditor(protValues));
        columnModel.getColumn(6).setHeaderValue(messages.getString("jfirewalldialog_msg35"));
        columnModel.getColumn(7).setHeaderValue(messages.getString("jfirewalldialog_msg32"));
        String[] actionValues = { messages.getString("jfirewalldialog_msg33"),
                messages.getString("jfirewalldialog_msg34") };
        columnModel.getColumn(7).setCellEditor(new ComboBoxTableCellEditor(actionValues));
        columnModel.getColumn(0).setPreferredWidth(30);
        columnModel.getColumn(1).setPreferredWidth(130);
        columnModel.getColumn(2).setPreferredWidth(130);
        columnModel.getColumn(3).setPreferredWidth(130);
        columnModel.getColumn(4).setPreferredWidth(130);
        columnModel.getColumn(5).setPreferredWidth(80);
        columnModel.getColumn(6).setPreferredWidth(60);
        columnModel.getColumn(7).setPreferredWidth(80);

        scrollPane = new JScrollPane(ruleTable);
        scrollPane.setPreferredSize(new Dimension(555, 300));

        vBox.add(scrollPane);
        vBox.add(Box.createVerticalStrut(10));

        hBox = Box.createHorizontalBox();
        hBox.add(Box.createHorizontalStrut(10));

        button = new JButton(messages.getString("jfirewalldialog_msg22"));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ruleTable.closeEditor();
                int rowSel = -1;
                boolean success = false;
                try {
                    if (ruleTable.getSelectedRowCount() == 1) {
                        rowSel = ruleTable.getSelectedRow();
                        String idStr = (String) ruleTable.getValueAt(rowSel, 0);
                        success = firewall.moveUp(Integer.parseInt(idStr));
                    }
                } catch (Exception ex) {}
                updateRuleTable();
                if (rowSel >= 0 && success) {
                    ruleTable.setRowSelectionInterval(rowSel - 1, rowSel - 1);
                }
            }
        });
        hBox.add(button);
        hBox.add(Box.createHorizontalStrut(10));

        button = new JButton(messages.getString("jfirewalldialog_msg23"));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ruleTable.closeEditor();
                int rowSel = -1;
                boolean success = false;
                try {
                    if (ruleTable.getSelectedRowCount() == 1) {
                        rowSel = ruleTable.getSelectedRow();
                        String idStr = (String) ruleTable.getValueAt(rowSel, 0);
                        success = firewall.moveDown(Integer.parseInt(idStr));
                    }
                } catch (Exception ex) {}
                updateRuleTable();
                if (rowSel >= 0 && success) {
                    ruleTable.setRowSelectionInterval(rowSel + 1, rowSel + 1);
                }
            }
        });
        hBox.add(button);
        hBox.add(Box.createHorizontalStrut(30));

        button = new JButton(messages.getString("jfirewalldialog_msg24"));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ruleTable.closeEditor();
                firewall.addRule();
                updateRuleTable();
            }
        });
        hBox.add(button);
        hBox.add(Box.createHorizontalStrut(10));

        button = new JButton(messages.getString("jfirewalldialog_msg25"));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ruleTable.closeEditor();
                int rowSel = -1;
                try {
                    if (ruleTable.getSelectedRowCount() == 1) {
                        rowSel = ruleTable.getSelectedRow();
                        String idStr = (String) ruleTable.getValueAt(rowSel, 0);
                        Main.debug.println("DEBUG (" + this.hashCode() + ") " + getClass() + ", del action: rowSel="
                                + rowSel + ", rows count=" + firewall.getRuleset().size());
                        firewall.deleteRule(Integer.parseInt(idStr) - 1);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                updateRuleTable();
                if (rowSel >= 0) {
                    if (rowSel < firewall.getRuleset().size()) {
                        ruleTable.setRowSelectionInterval(rowSel, rowSel);
                    } else if (firewall.getRuleset().size() > 0) {
                        ruleTable.setRowSelectionInterval(firewall.getRuleset().size() - 1,
                                firewall.getRuleset().size() - 1);
                    }
                }
            }
        });
        hBox.add(button);
        hBox.add(Box.createHorizontalStrut(10));

        vBox.add(hBox);
        vBox.add(Box.createVerticalStrut(10));

        this.updateRuleTable();

        return vBox;
    }

    public short stringToPolicy(String langPolicy) {
        if (langPolicy.equals(messages.getString("jfirewalldialog_msg33"))) {
            return FirewallRule.ACCEPT;
        } else {
            return FirewallRule.DROP;
        }
    }

    public String policyToString(short policy) {
        if (policy == FirewallRule.ACCEPT) {
            return messages.getString("jfirewalldialog_msg33");
        } else if (policy == FirewallRule.DROP) {
            return messages.getString("jfirewalldialog_msg34");
        } else {
            return "";
        }
    }

    /*
     * @author Weyer hier wird das ganze Fenster bestückt
     */
    private void erzeugeFenster() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + ", erzeugeFenster()");
        JTabbedPane tp;
        JPanel hauptPanel;

        hauptPanel = new JPanel(new BorderLayout());

        tp = new JTabbedPane();
        tp.add(messages.getString("jfirewalldialog_msg18"), erzeugeNicBox());
        tp.setBackgroundAt(0, TAB_COLOR);

        tp.add(messages.getString("jfirewalldialog_msg21"), firewallRuleBox());
        tp.setBackgroundAt(1, TAB_COLOR);

        hauptPanel.add(tp, BorderLayout.CENTER);
        hauptPanel.setBackground(TAB_COLOR);

        getContentPane().add(hauptPanel);
    }

    public void updateRuleTable() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + ", updateRuleTable()");

        DefaultTableModel model;
        Vector<FirewallRule> ruleset = firewall.getRuleset();

        model = (DefaultTableModel) this.ruleTable.getModel();
        model.setRowCount(0);
        for (int i = 0; i < ruleset.size(); i++) {
            Main.debug.println("DEBUG Rule #" + (i + 1) + ": " + ruleset.get(i).toString());
            Vector<String> row = ruleAsVector(i + 1, ruleset.get(i));
            model.addRow(row);
        }

        defaultPolicyCombo.setSelectedItem(policyToString(firewall.getDefaultPolicy()));
        activateFirewall.setSelected(firewall.isActivated());
        dropICMP.setSelected(firewall.getDropICMP());
        onlyFilterSYN.setSelected(firewall.getFilterSYNSegmentsOnly());
    }

    public Vector<String> ruleAsVector(int idx, FirewallRule rule) {
        Vector<String> resultVec = new Vector<String>();
        resultVec.addElement(Integer.toString(idx));
        resultVec.addElement(rule.srcIP);
        resultVec.addElement(rule.srcMask);
        resultVec.addElement(rule.destIP);
        resultVec.addElement(rule.destMask);
        if (rule.protocol == FirewallRule.TCP)
            resultVec.addElement("TCP");
        else if (rule.protocol == FirewallRule.UDP)
            resultVec.addElement("UDP");
        else
            resultVec.addElement("*"); // = alle
        if (rule.port >= 0)
            resultVec.addElement(Integer.toString(rule.port));
        else
            resultVec.addElement("");
        if (rule.action == FirewallRule.ACCEPT)
            resultVec.addElement(messages.getString("jfirewalldialog_msg33"));
        else
            resultVec.addElement(messages.getString("jfirewalldialog_msg34"));
        return resultVec;
    }

    public Firewall getFirewall() {
        return firewall;
    }
}
