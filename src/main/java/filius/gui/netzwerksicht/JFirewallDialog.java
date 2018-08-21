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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
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
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import filius.Main;
import filius.hardware.NetzwerkInterface;
import filius.hardware.knoten.InternetKnoten;
import filius.rahmenprogramm.EingabenUeberpruefung;
import filius.rahmenprogramm.I18n;
import filius.software.firewall.Firewall;
import filius.software.firewall.FirewallRule;
import filius.gui.anwendungssicht.JTableEditable;
import filius.gui.anwendungssicht.JTableEditable.ColorTableCellRenderer;

public class JFirewallDialog extends JDialog implements I18n {

	public class ComboBoxTableCellEditor extends AbstractCellEditor implements TableCellEditor 
	{
	    /**
		 * 
		 */
		private static final long serialVersionUID = -4372708685136408285L;
		private JComboBox cmbBox;
//	    private String [] values = {"First", "Second", "Third"};

	    public ComboBoxTableCellEditor(String[] values)
	    {
		    // create a new ComboBox with values provided as parameter (array of Strings)
		    cmbBox = new JComboBox(values);
	    }

	    @Override
	    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int rowIndex, int colIndex) 
	    {
		    if(isSelected)
		    {
			    cmbBox.setSelectedItem(value);
			    TableModel model = table.getModel();
			    model.setValueAt(value, rowIndex, colIndex);
		    }
	
		    return cmbBox;
	    }

	    @Override
	    public Object getCellEditorValue() 
	    {
	    	return cmbBox.getSelectedItem();
	    }
	}
	
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

	private JTableEditable ruleTable;
	private JComboBox defaultPolicyCombo;

	private JCheckBox rejectConnections;
	private JCheckBox activateFirewall;
	private JCheckBox dropICMP;
	private JCheckBox onlyFilterSYN;

	public JFirewallDialog(Firewall firewall, JFrame dummyFrame) {
		super(dummyFrame, messages.getString("jfirewalldialog_msg1"), true);
		Main.debug.println("INVOKED-2 (" + this.hashCode() + ") " + getClass() + ", constr: JFirewallDialog("
		        + firewall + "," + dummyFrame + ")");
		this.firewall = firewall;

		jfd = this;

//		this.setSize(1000, 200); // no effect due to call of setBounds in JFirewallKonfiguration
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
		activateFirewall.setSelected(firewall.getRejectIncomingConnections());
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
		dropICMP.setSelected(firewall.getRejectIncomingConnections());
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
		onlyFilterSYN.setSelected(firewall.getRejectIncomingConnections());
		onlyFilterSYN.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				firewall.setAllowRelatedPackets(((JCheckBox) e.getSource()).isSelected());
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
				firewall.setDefaultPolicyString((String) defaultPolicyCombo.getSelectedItem());
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
		ruleTable = new JTableEditable(model,true);
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
		columnModel.getColumn(0).setCellEditor(null);  // ID column must be read-only
		columnModel.getColumn(1).setHeaderValue(messages.getString("jfirewalldialog_msg27"));
		columnModel.getColumn(2).setHeaderValue(messages.getString("jfirewalldialog_msg28"));
		columnModel.getColumn(3).setHeaderValue(messages.getString("jfirewalldialog_msg29"));
		columnModel.getColumn(4).setHeaderValue(messages.getString("jfirewalldialog_msg30"));
		columnModel.getColumn(5).setHeaderValue(messages.getString("jfirewalldialog_msg31"));
		String[] protValues = {"*","TCP","UDP"};
		columnModel.getColumn(5).setCellEditor(new ComboBoxTableCellEditor(protValues));
		columnModel.getColumn(6).setHeaderValue(messages.getString("jfirewalldialog_msg35"));
		columnModel.getColumn(7).setHeaderValue(messages.getString("jfirewalldialog_msg32"));
		String[] actionValues = {messages.getString("jfirewalldialog_msg33"),messages.getString("jfirewalldialog_msg34")};
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
				int rowSel = -1;
				boolean success = false;
				try {
					if(ruleTable.getSelectedRowCount() == 1) {
						rowSel = ruleTable.getSelectedRow();
						String idStr = (String) ruleTable.getValueAt(
								rowSel, 0);
						success = firewall.moveUp(Integer.parseInt(idStr));
					}
				} catch (Exception ex) {
				}
				updateRuleTable();
				if(rowSel >= 0 && success)
					ruleTable.setRowSelectionInterval(rowSel-1, rowSel-1);
			}
		});
		hBox.add(button);
		hBox.add(Box.createHorizontalStrut(10));

		button = new JButton(messages.getString("jfirewalldialog_msg23"));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int rowSel = -1;
				boolean success = false;
				try {
					if(ruleTable.getSelectedRowCount() == 1) {
						rowSel = ruleTable.getSelectedRow();
						String idStr = (String) ruleTable.getValueAt(
								rowSel, 0);
						success = firewall.moveDown(Integer.parseInt(idStr));
					}
				} catch (Exception ex) {
				}
				updateRuleTable();
				if(rowSel >= 0 && success)
					ruleTable.setRowSelectionInterval(rowSel+1, rowSel+1);
			}
		});
		hBox.add(button);
		hBox.add(Box.createHorizontalStrut(30));

		button = new JButton(messages.getString("jfirewalldialog_msg24"));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				firewall.addRule();
				updateRuleTable();
			}
		});
		hBox.add(button);
		hBox.add(Box.createHorizontalStrut(10));

		button = new JButton(messages.getString("jfirewalldialog_msg25"));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int rowSel = -1;
				try {
					if(ruleTable.getSelectedRowCount() == 1) {
						rowSel = ruleTable.getSelectedRow();
						String idStr = (String) ruleTable.getValueAt(
								rowSel, 0);
						Main.debug.println("DEBUG (" + this.hashCode() + ") " + getClass() + ", del action: rowSel="+rowSel+", rows count="+firewall.getRuleset().size());
						firewall.delRule(Integer.parseInt(idStr));
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				updateRuleTable();
				if(rowSel >= 0)
					if(rowSel < firewall.getRuleset().size())
						ruleTable.setRowSelectionInterval(rowSel, rowSel);
					else if(firewall.getRuleset().size() > 0)
						ruleTable.setRowSelectionInterval(firewall.getRuleset().size()-1, firewall.getRuleset().size()-1);
			}
		});
		hBox.add(button);
		hBox.add(Box.createHorizontalStrut(10));

		vBox.add(hBox);
		vBox.add(Box.createVerticalStrut(10));
		
		this.updateRuleTable();

		return vBox;
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

	private void updateCellColors() {
		TableColumnModel colModel = ruleTable.getColumnModel();
		ColorTableCellRenderer cellSrcIP = (ColorTableCellRenderer) colModel.getColumn(1).getCellRenderer();
		ColorTableCellRenderer cellSrcMask = (ColorTableCellRenderer) colModel.getColumn(2).getCellRenderer();
		ColorTableCellRenderer cellDestIP = (ColorTableCellRenderer) colModel.getColumn(3).getCellRenderer();
		ColorTableCellRenderer cellDestMask = (ColorTableCellRenderer) colModel.getColumn(4).getCellRenderer();
		ColorTableCellRenderer cellPort = (ColorTableCellRenderer) colModel.getColumn(6).getCellRenderer();
		for(int i=0; i<ruleTable.getRowCount(); i++) {
			if(EingabenUeberpruefung.isGueltig((String) ruleTable.getValueAt(i,1), EingabenUeberpruefung.musterIpAdresseAuchLeer))
				cellSrcIP.resetColor(i, 1);
			else
				cellSrcIP.setColor(i, 1, EingabenUeberpruefung.farbeFalsch);
			if(! ((String) ruleTable.getValueAt(i,1)).isEmpty())
				if(((String) ruleTable.getValueAt(i,2)).isEmpty())
					cellSrcMask.setColor(i, 2, EingabenUeberpruefung.farbeFalsch);
				else if(! EingabenUeberpruefung.isValidSubnetmask((String) ruleTable.getValueAt(i, 2)))
					cellSrcMask.setColor(i, 2, EingabenUeberpruefung.farbeFalsch);
				else
					cellSrcMask.resetColor(i, 2);
			else
				cellSrcMask.resetColor(i, 2);
			if(EingabenUeberpruefung.isGueltig((String) ruleTable.getValueAt(i,3), EingabenUeberpruefung.musterIpAdresseAuchLeer))
				cellDestIP.resetColor(i, 3);
			else
				cellDestIP.setColor(i, 3, EingabenUeberpruefung.farbeFalsch);
			if(! ((String) ruleTable.getValueAt(i,3)).isEmpty())
				if(((String) ruleTable.getValueAt(i,4)).isEmpty())
					cellDestMask.setColor(i, 4, EingabenUeberpruefung.farbeFalsch);
				else if(! EingabenUeberpruefung.isValidSubnetmask((String) ruleTable.getValueAt(i, 4)))
					cellDestMask.setColor(i, 4, EingabenUeberpruefung.farbeFalsch);
				else
					cellDestMask.resetColor(i, 4);
			else
				cellDestMask.resetColor(i, 4);
			if(EingabenUeberpruefung.isGueltig(((String) ruleTable.getValueAt(i,6)).trim(), EingabenUeberpruefung.musterPortAuchLeer))
				cellPort.resetColor(i, 6);
			else
				cellPort.setColor(i, 6, EingabenUeberpruefung.farbeFalsch);
		}
	}
	
	public void updateRuleTable() {
		Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + ", updateRuleTable()");
		
		DefaultTableModel model;
		Vector<FirewallRule> ruleset = firewall.getRuleset();
		
		model = (DefaultTableModel) this.ruleTable.getModel();
		model.setRowCount(0);
		for (int i = 0; i < ruleset.size(); i++) {
			Main.debug.println("DEBUG Rule #"+(i+1)+": "+ruleset.get(i).toString());
			model.addRow(ruleset.get(i).getVector(i+1));
		}
		// (re-)set colors in cells
		updateCellColors();
		
		defaultPolicyCombo.setSelectedItem(firewall.getDefaultPolicyString());
		activateFirewall.setSelected(firewall.isActivated());
		dropICMP.setSelected(firewall.getDropICMP());
		onlyFilterSYN.setSelected(firewall.getAllowRelatedPackets());
	}

	public Firewall getFirewall() {
		return firewall;
	}
}
