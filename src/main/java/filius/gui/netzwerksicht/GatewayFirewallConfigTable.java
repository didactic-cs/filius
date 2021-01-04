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

import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

import filius.gui.JExtendedTable;
import filius.rahmenprogramm.I18n;
import filius.software.firewall.Firewall;
import filius.software.firewall.FirewallRule;

@SuppressWarnings("serial")
public class GatewayFirewallConfigTable extends JExtendedTable implements I18n {

    public GatewayFirewallConfigTable(TableModel model, boolean editable) {
        super(model, editable);
    }

    public void editingStopped(ChangeEvent evt) {
        closeEditor();
    }

    void closeEditor() {
        TableCellEditor editor = getCellEditor();
        if (editor != null) {
            Firewall firewall = (Firewall) ((JFirewallDialog) parentGUI).getFirewall();
            FirewallRule rule = firewall.getRuleset().get(editingRow);
            rule.srcIP = getCurrentValueAt(editingRow, 1);
            rule.srcMask = getCurrentValueAt(editingRow, 2);
            rule.destIP = getCurrentValueAt(editingRow, 3);
            rule.destMask = getCurrentValueAt(editingRow, 4);
            String protocol = getCurrentValueAt(editingRow, 5);
            if (protocol.equals("TCP")) {
                rule.protocol = FirewallRule.TCP;
            } else if (protocol.equals("UDP")) {
                rule.protocol = FirewallRule.UDP;
            } else if (protocol.equals("ICMP")) {
                rule.protocol = FirewallRule.ICMP;
            } else {
                rule.protocol = FirewallRule.ALL_PROTOCOLS;
            }
            String port = getCurrentValueAt(editingRow, 6);
            try {
                rule.port = Integer.valueOf(port);
            } catch (NumberFormatException e) {
                rule.port = FirewallRule.ALL_PORTS;
            }
            String action = getCurrentValueAt(editingRow, 7);
            if (messages.getString("jfirewalldialog_msg33").equals(action)) {
                rule.action = FirewallRule.ACCEPT;
            } else {
                rule.action = FirewallRule.DROP;
            }
            removeEditor();
            if (rule.validateExistingAttributes()) {
                firewall.updateRule(editingRow, rule);
                ((JFirewallDialog) parentGUI).updateRuleTable();
            }
        }
    }

    private String getCurrentValueAt(int row, int col) {
        if (row == editingRow && col == editingColumn) {
            return (String) getCellEditor().getCellEditorValue();
        } else {
            return (String) getValueAt(row, col);
        }
    }
}
