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
package filius.gui.netzwerksicht.config;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import filius.gui.GUIHelper;
import filius.gui.JExtendedDialog;
import filius.gui.JExtendedTable;
import filius.gui.Palette;
import filius.hardware.knoten.Router;
import filius.rahmenprogramm.EntryValidator;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.ProjectManager;
import filius.software.firewall.Firewall;
import filius.software.firewall.FirewallRule;
import filius.software.system.RouterOS;

@SuppressWarnings("serial")
public class JConfigRouterFirewall extends JExtendedDialog implements I18n {
	
	private GUIHelper       gui;
	private Firewall        firewall;
	private Frame           owner;  
    private JCheckBox       cbFilterSynOnly;
    private JCheckBox       cbDropICMP;    
    private JCheckBox       cbActive;
    private JButton         btOK;
    private JButton         btCancel;
	private JExtendedTable  rulesTable;
	private boolean         modified = false;
	private JDialog         rulesDialog;
	
	
	public JConfigRouterFirewall(Frame owner, Router router) {
    	
        super(owner, true);        
        this.owner = owner;         
        firewall = ((RouterOS) router.getSystemSoftware()).getFirewall();
        
        initPanel();   

        updateDisplayedValues();
    }

	private void initPanel() {		
		
		gui = new GUIHelper();

        setTitle(messages.getString("jconfigrouterfirewall_msg1"));  // Firewall settings      

        // Size and Location
        setSize(350, 390);
        setResizable(false);
        // Above the button used to pop de dialogbox up
        setLocation(owner.getX() + 10, owner.getY() + owner.getHeight() - getHeight() - 10);      
    	
    	Container contentPane = getContentPane();
    	contentPane.setLayout(null);   
    	
        
        // TCP and UPD filtering
        
        JPanel pnTCPUDP = gui.addTitledPanel(messages.getString("jconfigrouterfirewall_msg2"), // TCP and UDP
        		                             contentPane, 10, 10, getWidth()-36, 120);        
        
        createRulesDialog();        
        ActionListener onConfigReservedAdressesClick = new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		showRulesDialog();
        	}
        };           
        gui.addButton(messages.getString("jconfigrouterfirewall_msg3"),          // Routing rules
        		      pnTCPUDP, 75, 30, 160, 24, onConfigReservedAdressesClick);     
        
        ActionListener onCheckBoxChange = new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		modified = true;
        	}
        };        
        cbFilterSynOnly = gui.addCheckBox("<html>"+messages.getString("jconfigrouterfirewall_msg4")+"</html>", // Only drop TCP packets with SYN flag set
        		                          pnTCPUDP, 20, 74, 260, 30, onCheckBoxChange);  
                
        // ICMP filtering
        
        JPanel pnICMP = gui.addTitledPanel(messages.getString("jconfigrouterfirewall_msg5"), // ICMP
        		                           contentPane, 10, 134, getWidth()-36, 56);        

        cbDropICMP = gui.addCheckBox(messages.getString("jconfigrouterfirewall_msg6"), // Drop ICMP packets
        		                       pnICMP, 20, 26, 300, 15, onCheckBoxChange); 
        
        // Firewall active checkbox
        
        JPanel pnServerStatus = gui.addTitledPanel(messages.getString("jconfigrouterfirewall_msg7"), // Firewall status
        		                                   contentPane, 10, 194, getWidth()-36, 56);    
        
        cbActive = gui.addCheckBox(messages.getString("jconfigrouterfirewall_msg8"),  // Activate firewall
        		                   pnServerStatus, 20, 26, 200, 15, onCheckBoxChange);
        
        
        // OK & Cancel buttons
        
        AbstractAction onOKClick = new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {
        		saveChanges();
        		close();
        	}
        };        
        btOK = gui.addButton(messages.getString("main_dlg_OK"), contentPane, 115, 320, 100, 24, onOKClick);     
        gui.mapKeyToAction(btOK, "ENTER", onOKClick);  
                  
        AbstractAction onCancelClick = new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {
        		close();
        	}
        };        
        btCancel = gui.addButton(messages.getString("main_dlg_CANCEL"), contentPane, 222, 320, 100, 24, onCancelClick);     
        gui.mapKeyToAction(btCancel, "ESCAPE", onCancelClick);   
	}		
	
    // Dialogbox with a table of the filtering rules
    private void createRulesDialog() {

        rulesDialog = new JDialog();
        JDialog dlg = rulesDialog; 
        
        dlg.setTitle(messages.getString("jconfigrouterfirewall_msg3"));
        dlg.setSize(662, 390);
        dlg.setResizable(false);    
        dlg.setModal(true);                 
        
        // The addresses' table
        rulesTable = new JExtendedTable(8);
    	JExtendedTable table = rulesTable;    	
   	
    	table.setHeader(0, messages.getString("jconfigrouterfirewall_msg9"));  // ID
    	table.setHeader(1, messages.getString("jconfigrouterfirewall_msg11")); // Source IP 
    	table.setHeader(2, messages.getString("jconfigrouterfirewall_msg12")); // Source Mask
    	table.setHeader(3, messages.getString("jconfigrouterfirewall_msg13")); // Destination IP 
    	table.setHeader(4, messages.getString("jconfigrouterfirewall_msg14")); // Destination Mask
    	table.setHeader(5, messages.getString("jconfigrouterfirewall_msg15")); // Protocol
    	table.setHeader(6, messages.getString("jconfigrouterfirewall_msg16")); // Port
    	table.setHeader(7, messages.getString("jconfigrouterfirewall_msg17")); // Action
    	
    	table.setColumnWidth(0, 30);
        table.setColumnWidth(1, 110);
        table.setColumnWidth(2, 110);
        table.setColumnWidth(3, 110);
        table.setColumnWidth(4, 110);
        table.setColumnWidth(5, 40);
        table.setColumnWidth(6, 48);
        table.setColumnWidth(7, 70);     
        
        table.setColumnAlignment(0, JLabel.RIGHT);
        table.setColumnAlignment(1, JLabel.CENTER);
        table.setColumnAlignment(2, JLabel.CENTER);
        table.setColumnAlignment(3, JLabel.CENTER);
        table.setColumnAlignment(4, JLabel.CENTER);
        table.setColumnAlignment(5, JLabel.CENTER);
        table.setColumnAlignment(6, JLabel.CENTER);
        table.setColumnAlignment(7, JLabel.CENTER);
        
    	table.setHeaderResizable(false);      	
    	table.setBackground(Palette.DHCP_TABLE_EVEN_ROW_BG);
    	table.setRowColors(Palette.DHCP_TABLE_EVEN_ROW_BG, Palette.DHCP_TABLE_ODD_ROW_BG);
    	
    	dlg.getContentPane().add(new JScrollPane(table));   
    	
    	// ACTIONS
    	
    	// Shift a row up
    	AbstractAction shiftUpActionListener = new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {
        		
        		int rowIndex = table.getSelectedRow();    
        		if (rowIndex == table.getRowCount() - 1) return;
        		table.shiftRowUp(rowIndex);
        		renumberRules();
        		rowIndex--;
        		if (rowIndex >= 0) table.setRowSelectionInterval(rowIndex, rowIndex);
        		modified = true;
        	}
        };   
        gui.mapKeyToAction(table, "alt UP", shiftUpActionListener);  
        
    	// Shift a row down
    	AbstractAction shiftDownActionListener = new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {
        		
        		int rowIndex = table.getSelectedRow();
        		if (rowIndex == table.getRowCount() - 2) return;
        		table.shiftRowDown(rowIndex);    
        		renumberRules();
        		rowIndex++;
        		if (rowIndex < table.getRowCount()) table.setRowSelectionInterval(rowIndex, rowIndex);
        		modified = true;
        	}
        };  
        gui.mapKeyToAction(table, "alt DOWN", shiftDownActionListener);  
    	
    	// Add a row
    	AbstractAction addRowActionListener = new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {
        		        		
        		FirewallRule rule = new FirewallRule();
        		if (showRuleEditDialog(messages.getString("jconfigrouterfirewall_msg22"), rule, false)) {
        			
        			int rowIndex = rulesTable.getSelectedRow();
        			if (rowIndex < 0) rowIndex = rulesTable.getRowCount()-1;        			
        			rulesTable.addRow(String.valueOf(rulesTable.getRowCount()), rule.srcIP, rule.srcMask, rule.destIP, rule.destMask, 
					                  rule.getProtocolAsString(), rule.getPortAsString(), rule.getActionAsString());
        			rulesTable.moveRowTo(rulesTable.getRowCount()-1, rowIndex);
        			renumberRules();
        		}
        	}
        };       	
        gui.mapKeyToAction(table, "INSERT", addRowActionListener);
    	
    	// Edit a row
    	AbstractAction editRowActionListener = new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {
        		
        		int rowIndex = table.getSelectedRow();
        		if (rowIndex < 0) return;
        		FirewallRule rule = new FirewallRule(rulesTable.getStringAt(rowIndex, 1), rulesTable.getStringAt(rowIndex, 2), rulesTable.getStringAt(rowIndex, 3), 
        				                             rulesTable.getStringAt(rowIndex, 4), rulesTable.getStringAt(rowIndex, 5),
        				                             rulesTable.getStringAt(rowIndex, 6), rulesTable.getStringAt(rowIndex, 7));
        		if (showRuleEditDialog(messages.getString("jconfigrouterfirewall_msg23"), rule, rowIndex == table.getRowCount()-1)) {
        			rulesTable.setValueAt(rule.srcIP, rowIndex, 1);
        			rulesTable.setValueAt(rule.srcMask, rowIndex, 2);
        			rulesTable.setValueAt(rule.destIP, rowIndex, 3);
        			rulesTable.setValueAt(rule.destMask, rowIndex, 4);
        			rulesTable.setValueAt(rule.getProtocolAsString(), rowIndex, 5);
        			rulesTable.setValueAt(rule.getPortAsString(), rowIndex, 6);
        			rulesTable.setValueAt(rule.getActionAsString(), rowIndex, 7);
        		}   		
        	}
        };   
        table.setDoubleClickListener(editRowActionListener); 
    	
    	// Delete a row
    	AbstractAction deleteRowActionListener = new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {
        		int rowIndex = table.getSelectedRow();
        		if (rowIndex > -1 && rowIndex < table.getRowCount()-1) {
        			table.removeSelectedRow();
        			if (rowIndex > table.getRowCount()-1) rowIndex--;        
        			table.setSelectedRow(rowIndex);
        			renumberRules();
        			modified = true;
        		}
        	}
        };       	
        gui.mapKeyToAction(table, "DELETE", deleteRowActionListener);  
    	
    	// Close the dialog
    	AbstractAction closeActionListener = new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {
        		dlg.setVisible(false);
        	}
        };       	
        gui.mapKeyToAction(table, "ESCAPE", closeActionListener);  
       	
    	// POPUP MENU
    	
    	MouseAdapter popupMenuAdapter = new MouseAdapter() {
    		
    		public void mouseReleased(MouseEvent e) {
    			
    			JPopupMenu menu = new JPopupMenu();
    			
    			int rowIndex = table.getSelectedRow();
    			if (rowIndex >= 0) {   
    				
    				JMenuItem miShiftUp = new JMenuItem(messages.getString("jconfigrouterfirewall_msg20"));
    				miShiftUp.addActionListener(shiftUpActionListener);
    				miShiftUp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.ALT_DOWN_MASK));
    		    	menu.add(miShiftUp);
    		    	if (rowIndex == 0 || rowIndex == table.getRowCount()-1) miShiftUp.setEnabled(false);
    		    	
    		    	JMenuItem miShiftDown = new JMenuItem(messages.getString("jconfigrouterfirewall_msg21"));
    		    	miShiftDown.addActionListener(shiftDownActionListener);
    		    	miShiftDown.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.ALT_DOWN_MASK));
    		    	menu.add(miShiftDown);
    		    	if (rowIndex >= table.getRowCount()-2) miShiftDown.setEnabled(false);
    		    	
    				menu.addSeparator();
    			}
    			
    			// Add item
    			JMenuItem miAdd = new JMenuItem(messages.getString("jconfigrouterfirewall_msg22"));
		    	miAdd.addActionListener(addRowActionListener);
		    	miAdd.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0));
		    	menu.add(miAdd);

    			if (rowIndex >= 0) {   
    		    	
    				// Edit selected item
    		    	JMenuItem miEdit = new JMenuItem(messages.getString("jconfigrouterfirewall_msg23"));
    		    	miEdit.addActionListener(editRowActionListener);
    		    	menu.add(miEdit);    		    	
    		    	
    		    	// Delete selected item
    		    	JMenuItem miDelete = new JMenuItem(messages.getString("jconfigrouterfirewall_msg24"));
    		    	miDelete.addActionListener(deleteRowActionListener);
    		    	miDelete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
    		    	menu.add(miDelete);   
    		    	if (rowIndex == table.getRowCount()-1) miDelete.setEnabled(false); // Do not delete the default rule
    	        }     
    			
    			menu.show(table, e.getX(), e.getY());
    		}   
    	};  
    	table.setPopupMenuAdapter(popupMenuAdapter); 
    } 

    private void showRulesDialog() {
    	
    	// Display this secondary dialog box over the main one
    	rulesDialog.setLocation(getX(), getY());
    	rulesDialog.setVisible(true);
    };
    
    // Dialog box used to create or modify a firewall rule
    private boolean showRuleEditDialog (String caption, FirewallRule rule, boolean isDefault) {
    	
    	JExtendedDialog dlg = new JExtendedDialog();
        
        dlg.setTitle(caption);
        dlg.setSize(510, 220);
        dlg.setResizable(false);  
        dlg.setLocation(getX() + (rulesDialog.getWidth() - dlg.getWidth())/2, rulesDialog.getY() - 170);   
        dlg.setLayout(null);        
        
        
        ActionListener onIPChange = new ActionListener() {			
			public void actionPerformed(ActionEvent e) { checkIPAddress((Component) e.getSource()); }
		};	
		
		ActionListener onMaskChange = new ActionListener() {			
			public void actionPerformed(ActionEvent e) { checkMask((Component) e.getSource()); }
		};		
		
		ActionListener onPortChange = new ActionListener() {			
			public void actionPerformed(ActionEvent e) { checkPort((Component) e.getSource()); }
		};	
		
		// Rule panel
		
		String title;
		if (isDefault) title = messages.getString("jconfigrouterfirewall_msg26");
		else           title = messages.getString("jconfigrouterfirewall_msg10");
		JPanel pn = gui.addTitledPanel(title, dlg, 10, 10, dlg.getWidth()-36, 130);    
		
		
		// Source IP and mask  
		
        JTextField tfSourceIP = gui.addTextField(messages.getString("jconfigrouterfirewall_msg11"), rule.srcIP, 
                                                 pn, 20, 20, 200, 22, 90, 
        		                                 null, null, null, onIPChange);
                   
        JTextField tfSourceMask  = gui.addTextField(messages.getString("jconfigrouterfirewall_msg12"), rule.srcMask,
        	                                        pn, 245, 20, 205, 22, 95,
        		                                    null, null, null, onMaskChange); 
        
        // Destination IP and mask  
		
        JTextField tfDestIP = gui.addTextField(messages.getString("jconfigrouterfirewall_msg13"), rule.destIP, 
        		                               pn, 20, 46, 200, 22, 90, 
        		                               null, null, null, onIPChange);
                   
        JTextField tfDestMask  = gui.addTextField(messages.getString("jconfigrouterfirewall_msg14"), rule.destMask,
        		                                  pn, 245, 46, 205, 22, 95,
        		                                  null, null, null, onMaskChange); 
        
        // Protocol and port  
		
        JComboBox<String> cbProtocol = gui.addComboBox(messages.getString("jconfigrouterfirewall_msg25"), rule.getProtocolAsString(), 
        		                                       pn, 20, 72, 200, 22, 90, 
        		                                       null, null, null, null);
        cbProtocol.addItem("*");
        cbProtocol.addItem("TCP");
        cbProtocol.addItem("UDP");
        cbProtocol.setSelectedItem(rule.getProtocolAsString());
                   
        JTextField tfPort  = gui.addTextField(messages.getString("jconfigrouterfirewall_msg16"), rule.getPortAsString(),
        		                              pn, 245, 72, 205, 22, 95,
        		                              null, null, null, onPortChange); 
        
        // Action
        
//        JTextField tfAction = gui.addTextField(messages.getString("jconfigrouterfirewall_msg17"), rule.getActionAsString(), 
//                                               pn, 20, 98, 200, 22, 90, 
//                                               null, null, null, null);
        JComboBox<String> cbAction = gui.addComboBox(messages.getString("jconfigrouterfirewall_msg17"), rule.getActionAsString(), 
                                                     pn, 20, 98, 200, 22, 90, 
                                                     null, null, null, null);
        cbAction.addItem(messages.getString("jconfigrouterfirewall_msg18")); // Accept
        cbAction.addItem(messages.getString("jconfigrouterfirewall_msg19")); // Drop
        cbAction.setSelectedItem(rule.getActionAsString());
        
        if (isDefault) {
        	tfSourceIP.setEnabled(false);
    		tfSourceMask.setEnabled(false);
    		tfDestIP.setEnabled(false);
    		tfDestMask.setEnabled(false);
    		cbProtocol.setEnabled(false);
    		tfPort.setEnabled(false);
        }        
        
        // OK & Cancel buttons
        
        AbstractAction onOKClick = new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {
        		
        		if (! checkIPAddress(tfSourceIP) || ! checkMask(tfSourceMask) || 
        		    ! checkIPAddress(tfDestIP)   || ! checkMask(tfDestMask)   || ! checkPort(tfPort) ) return;
        		
        		if (tfSourceIP.getText().isEmpty() && tfSourceMask.getText().isEmpty() && cbProtocol.getSelectedIndex() == 0 &&
        			tfDestIP.getText().isEmpty() && tfDestMask.getText().isEmpty() && tfPort.getText().isEmpty()) return;

        		rule.srcIP = tfSourceIP.getText();
        		rule.srcMask = tfSourceMask.getText();
        		rule.destIP = tfDestIP.getText();
        		rule.destMask = tfDestMask.getText();
        		switch (cbProtocol.getSelectedIndex()) {
        		case 1:  rule.protocol = FirewallRule.TCP; break;
        		case 2:  rule.protocol = FirewallRule.UDP; break;
        		default: rule.protocol = FirewallRule.ALL_PROTOCOLS; 
        		}        
        		if (tfPort.getText().isEmpty()) rule.port = FirewallRule.ALL_PORTS;
        		else  rule.port = Integer.valueOf(tfPort.getText());
        		switch (cbAction.getSelectedIndex()) {
        		case 0:  rule.action = FirewallRule.ACCEPT; break;
        		default: rule.action = FirewallRule.DROP; 
        		}           	       		

        		dlg.setBooleanValue(true);
        		dlg.setVisible(false);
        	}
        };        
        JButton btOK = gui.addButton(messages.getString("main_dlg_OK"), dlg, dlg.getWidth() - 235, 146, 100, 24, onOKClick);    
        gui.mapKeyToAction(btOK, "ENTER", onOKClick);  
                  
        AbstractAction onCancelClick = new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {
        		dlg.setVisible(false);
        	}
        };        
        JButton btCancel = gui.addButton(messages.getString("main_dlg_CANCEL"), dlg, dlg.getWidth() - 128, 146, 100, 24, onCancelClick);     
        gui.mapKeyToAction(btCancel, "ESCAPE", onCancelClick);   
        
        
        // Show the modal dialog 
        dlg.setModal(true);  
        dlg.setVisible(true);     
       
        return dlg.getBooleanValue();
    }   

    public boolean checkIPAddress(Component comp) {    	
    	
    	return gui.checkAndHighlight(EntryValidator.musterIpAdresseAuchLeer, (JTextField)comp);    	
    }
    
    public boolean checkMask(Component comp) {    	
    	
    	return gui.checkAndHighlight(EntryValidator.musterSubNetzAuchLeer, (JTextField)comp);    	
    } 
    
    public boolean checkPort(Component comp) {    	
    	
    	return gui.checkAndHighlight(EntryValidator.musterPortAuchLeer, (JTextField)comp);    	
    } 
    
    private void renumberRules() {    	

    	for (int i=0; i < rulesTable.getRowCount(); i++) {
    		rulesTable.setValueAt(i+1, i, 0);
    	}
    }

	private void updateDisplayedValues() {    	      

		cbFilterSynOnly.setSelected(firewall.getAllowRelatedPackets());
	    cbDropICMP.setSelected(firewall.getDropICMP());  
		cbActive.setSelected(firewall.isActivated());
		
		// Populate the rulesTable
		rulesTable.clear();		
		int i = 1;
		for (FirewallRule rule: firewall.getRuleset()) {
			rulesTable.addRow(String.valueOf(i), rule.srcIP, rule.srcMask, rule.destIP, rule.destMask, 
					          rule.getProtocolAsString(), rule.getPortAsString(), rule.getActionAsString());
			i++;
		}
        // temporary -------------
//		rulesTable.addRow("98", "192.168.240.123", "255.255.255.255", "192.168.240.123", "255.255.255.0", "TCP", "65535", "Accepter");
//		rulesTable.addRow("99", "", "", "192.168.240.123", "255.255.255.255", "UDP", "", "Rejeter");
//		rulesTable.addRow("100", "192.168.240.123", "255.255.255.0", "", "", "*", "", "Rejeter");
		//-----------------
		
		rulesTable.addRow(String.valueOf(rulesTable.getRowCount()+1), "", "", "", "", "*", "", firewall.getDefaultActionAsString());        
	}

	public void saveChanges() {

		if (!modified) return;
		modified = false;

		firewall.setAllowRelatedPackets(cbFilterSynOnly.isSelected());
		firewall.setDropICMP(cbDropICMP.isSelected());
		firewall.setActivated(cbActive.isSelected());
		
		// Save the rulesTable
		firewall.clearRules();
		for (int i=0; i < rulesTable.getRowCount()-1; i++) {
			FirewallRule rule = new FirewallRule(rulesTable.getStringAt(i, 1), rulesTable.getStringAt(i, 2), rulesTable.getStringAt(i, 3), rulesTable.getStringAt(i, 4), 
					rulesTable.getStringAt(i, 5), rulesTable.getStringAt(i, 6), rulesTable.getStringAt(i, 7));			
			firewall.addRule(rule);	
		}
		
		firewall.setDefaultActionAsString(rulesTable.getStringAt(rulesTable.getRowCount()-1, 7));

		ProjectManager.getInstance().setModified();    
	}
}
