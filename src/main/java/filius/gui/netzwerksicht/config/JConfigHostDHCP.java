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
import javax.swing.JDialog;
import javax.swing.JFrame;
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
import filius.hardware.knoten.Host;
import filius.rahmenprogramm.EntryValidator;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.ProjectManager;
import filius.software.dhcp.DHCPAddressItem;
import filius.software.dhcp.DHCPServer;
import filius.software.system.HostOS;

@SuppressWarnings("serial")
public class JConfigHostDHCP extends JExtendedDialog implements I18n {

	private GUIHelper      gui;
	private Frame      	   owner;
    private DHCPServer     dhcpServer;    
    private JTextField     tfLowerLimit;
    private JTextField     tfUpperLimit;
    private JTextField     tfSubnetMask;
    private JTextField     tfGateway;
    private JTextField     tfDNSServer;
    private JCheckBox      cbActive;
    private JCheckBox      cbUseServerSettings;
    private JButton        btOK;
    private JButton        btCancel;    
    private JDialog        reservedAddressesDialog;
    private JExtendedTable reservedAddressesTable;
    

    public JConfigHostDHCP(JFrame owner, Host host) {
    	
        super(owner, true);
        this.owner = owner;
        dhcpServer = ((HostOS) host.getSystemSoftware()).getDHCPServer();          
        
        initComponents();
    }  

    private void initComponents() {
    	
    	gui = new GUIHelper();
    	
    	setTitle(messages.getString("jhostkonfiguration_msg8"));   
    	
    	// Size and Location
        setSize(380, 390);
        setResizable(false);
        // Above the button used to pop de dialogbox up
        setLocation(owner.getX() + 60, owner.getY() + owner.getHeight() - getHeight() - 65);      
    	
    	Container contentPane = getContentPane();
    	contentPane.setLayout(null);   
    	
        
        // Range of distributed IP addresses
        
        JPanel pnIPRange = gui.addTitledPanel(messages.getString("jdhcpkonfiguration_msg1"), contentPane, 10, 10, getWidth()-36, 140);        
        
        ActionListener onKeyReleased = new ActionListener() {			
			public void actionPerformed(ActionEvent e) {  checkIPAddress((Component)e.getSource()); }
		};   
        tfLowerLimit = gui.addTextField(messages.getString("jdhcpkonfiguration_msg2"), "", 
        		                        pnIPRange, 20, 20, 300, GUIHelper.TEXTFIELD_HEIGHT, 190,
        		                        null, null, null, onKeyReleased);
        
        tfUpperLimit = gui.addTextField(messages.getString("jdhcpkonfiguration_msg3"), "", 
        		                        pnIPRange, 20, 46, 300, GUIHelper.TEXTFIELD_HEIGHT, 190, 
        		                        null, null, null, onKeyReleased);
        
        tfSubnetMask = gui.addTextField(messages.getString("jdhcpkonfiguration_msg4"), "", 
        		                        pnIPRange, 20, 72, 300, GUIHelper.TEXTFIELD_HEIGHT, 190, 
        		                        null, null, null, null);
        
        tfSubnetMask.setEditable(false);
        
        createReservedAdressesDialog();        
        ActionListener onConfigReservedAdressesClick = new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		showReservedAddressesDialog();
        	}
        };           
        gui.addButton(messages.getString("jdhcpkonfiguration_msg5"), pnIPRange, 90, 103, 160, 24, onConfigReservedAdressesClick);            
        
        // Gateway and DNS addresses 
        
        JPanel pnGWDNS = gui.addTitledPanel(messages.getString("jdhcpkonfiguration_msg6"), contentPane, 10, 154, getWidth()-36, 100);        
        
        tfGateway = gui.addTextField(messages.getString("jdhcpkonfiguration_msg7"), "", 
        		                     pnGWDNS, 20, 20, 300, GUIHelper.TEXTFIELD_HEIGHT, 190, 
        		                     null, null, null, onKeyReleased);
        
        tfDNSServer = gui.addTextField(messages.getString("jdhcpkonfiguration_msg8"), "",
        		                       pnGWDNS, 20, 46, 300, GUIHelper.TEXTFIELD_HEIGHT, 190, 
        		                       null, null, null, onKeyReleased);
        
        ActionListener onUseServerSettingsChange = new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		updateGatewayAndDNS();
        	}
        }; 
        cbUseServerSettings = gui.addCheckBox(messages.getString("jdhcpkonfiguration_msg9"), 
        		                              pnGWDNS, 20, 74, 300, 15, 
        		                              onUseServerSettingsChange);
        
        // DHCP server active checkbox
        
        JPanel pnServerStatus = gui.addTitledPanel(messages.getString("jdhcpkonfiguration_msg10"), contentPane, 10, 258, getWidth()-36, 56);    
        
        cbActive = gui.addCheckBox(messages.getString("jdhcpkonfiguration_msg11"), 
        		                   pnServerStatus, 20, 26, 200, 15, null);
        
        
        // OK & Cancel buttons
        
        AbstractAction onOKClick = new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {
        		if (saveChanges()) close();
        	}
        };        
        btOK = gui.addButton(messages.getString("main_dlg_OK"), contentPane, 145, 320, 100, 24, onOKClick);     
        gui.mapKeyToAction(btOK, "ENTER", onOKClick);  
                  
        AbstractAction onCancelClick = new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {
        		close();
        	}
        };        
        btCancel = gui.addButton(messages.getString("main_dlg_CANCEL"), contentPane, 252, 320, 100, 24, onCancelClick);     
        gui.mapKeyToAction(btCancel, "ESCAPE", onCancelClick);   

        updateDisplayedValues();
    }

    // Dialogbox with a table of the reserved addresses
    private void createReservedAdressesDialog() {

        reservedAddressesDialog = new JDialog();
        JDialog dlg = reservedAddressesDialog; 
        
        dlg.setTitle(messages.getString("jdhcpkonfiguration_msg5"));
        dlg.setSize(260, 320);
        dlg.setResizable(false);    
        dlg.setModal(true);                 
        
        // The addresses' table
    	reservedAddressesTable = new JExtendedTable(2);
    	JExtendedTable table = reservedAddressesTable;    	
   	
    	table.setHeader(0, messages.getString("jdhcpkonfiguration_msg12"));
    	table.setHeader(1, messages.getString("jdhcpkonfiguration_msg13"));
    	table.setHeaderResizable(false);   
    	table.setSorted(true);
    	table.setBackground(Palette.DHCP_TABLE_EVEN_ROW_BG);
    	table.setRowColors(Palette.DHCP_TABLE_EVEN_ROW_BG, Palette.DHCP_TABLE_ODD_ROW_BG);
    	
    	dlg.getContentPane().add(new JScrollPane(table));      
    	
    	// ACTIONS
    	
    	// Add a row
    	AbstractAction addRowActionListener = new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {
        		
        		DHCPAddressItem item = new DHCPAddressItem("00:00:00:00:00:00", "0.0.0.0", 0);
        		if (showAddressEditDialog(messages.getString("jdhcpkonfiguration_msg17"), item)) {
        			table.addRow(item.getMAC(), item.getIP());
        		}
        	}
        };       	
        gui.mapKeyToAction(table, "INSERT", addRowActionListener);
    	
    	// Edit a row
    	AbstractAction editRowActionListener = new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {
        		
        		int rowIndex = table.getSelectedRow();
        		if (rowIndex < 0) return;
        		DHCPAddressItem item = new DHCPAddressItem((String)table.getValueAt(rowIndex, 0), (String)table.getValueAt(rowIndex, 1), 0);
        		if (showAddressEditDialog(messages.getString("jdhcpkonfiguration_msg18"), item)) {
        			table.updateRow(rowIndex, item.getMAC(), item.getIP());
        			table.sort();
        		}        		
        	}
        };   
        table.setDoubleClickListener(editRowActionListener); 
    	
    	// Delete a row
    	AbstractAction deleteRowActionListener = new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {
        		if (table.getSelectedRow() > -1) table.removeSelectedRow();
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
    			
    			// Add item
    			JMenuItem miAdd = new JMenuItem(messages.getString("jdhcpkonfiguration_msg14"));
		    	miAdd.addActionListener(addRowActionListener);
		    	miAdd.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0));
		    	menu.add(miAdd);

    			int row = table.getSelectedRow();
    			if (row >= 0) {   
    		    	
    				// Edit selected item
    		    	JMenuItem miEdit = new JMenuItem(messages.getString("jdhcpkonfiguration_msg15"));
    		    	miEdit.addActionListener(editRowActionListener);
    		    	menu.add(miEdit);
    		    	
    		    	// Delete selected item
    		    	JMenuItem miDelete = new JMenuItem(messages.getString("jdhcpkonfiguration_msg16"));
    		    	miDelete.addActionListener(deleteRowActionListener);
    		    	miDelete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
    		    	menu.add(miDelete);   
    	        }     
    			
    			menu.show(table, e.getX(), e.getY());
    		}   
    	};  
    	table.setPopupMenuAdapter(popupMenuAdapter); 
    } 

    private void showReservedAddressesDialog() {
    	
    	// Retrieve the width of the screen the dialogbox is diplayed in
    	int dialogScreenWidth = (int) getGraphicsConfiguration().getDevice().getDefaultConfiguration().getBounds().getWidth();
    	
    	// Display the secondary dialog box to the right or to the left of 
    	// the main dialog box, depending on where there is space for it 
    	int xPos;
    	if (getX() + getWidth() + reservedAddressesDialog.getWidth() <= dialogScreenWidth) 
    		xPos = getX() + getWidth();
    	else 
    		xPos = getX() - reservedAddressesDialog.getWidth();
    	
    	reservedAddressesDialog.setLocation(xPos, getY());
    	reservedAddressesDialog.setVisible(true);
    };
    
    // Dialog box used to create or modify a reserved address
    private boolean showAddressEditDialog (String caption, DHCPAddressItem item) {
    	
    	JExtendedDialog dlg = new JExtendedDialog();
        
        dlg.setTitle(caption);
        dlg.setSize(352, 140);
        dlg.setResizable(false);  
        dlg.setLocation(getX() + (getWidth() - dlg.getWidth())/2, getY() + 140);   
        dlg.setLayout(null);        
        
        // MAC and IP textfields   
        
        ActionListener onMACKeyReleased = new ActionListener() {			
			public void actionPerformed(ActionEvent e) {  
				
				JTextField tf = (JTextField) e.getSource();
            	int pos = tf.getCaretPosition();
            	tf.setText(tf.getText().toUpperCase());
            	tf.setCaretPosition(pos);

                checkMACAddress(tf);                
			}
		};		
        JTextField tfMAC = gui.addTextField(messages.getString("jdhcpkonfiguration_msg12"), item.getMAC(), 
        		                            dlg, 30, 10, 276, 22, 160, 
        		                            null, null, null, onMACKeyReleased);
        
        ActionListener onIPKeyReleased = new ActionListener() {			
			public void actionPerformed(ActionEvent e) {  checkIPAddress((Component)e.getSource()); }
		};            
        JTextField tfIP  = gui.addTextField(messages.getString("jdhcpkonfiguration_msg13"), item.getIP(),
        		                            dlg, 30, 36, 276, 22, 160,
        		                            null, null, null, onIPKeyReleased); 
        
        // OK & Cancel buttons
        
        AbstractAction onOKClick = new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {
        		
        		if (! checkMACAddress(tfMAC) || ! checkIPAddress(tfIP)) return;
        		
        		if (! addressesCanBeUsed(dlg, item.getMAC(), item.getIP(), tfMAC.getText(), tfIP.getText())) return;
        		
        		item.setMAC(tfMAC.getText());
        		item.setIP(tfIP.getText());
        		
        		dlg.setBooleanValue(true);
        		dlg.setVisible(false);
        	}
        };        
        JButton btOK = gui.addButton(messages.getString("main_dlg_OK"), dlg, 138, 70, 80, 24, onOKClick);    
        gui.mapKeyToAction(btOK, "ENTER", onOKClick);  
                  
        AbstractAction onCancelClick = new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {
        		dlg.setVisible(false);
        	}
        };        
        JButton btCancel = gui.addButton(messages.getString("main_dlg_CANCEL"), dlg, 225, 70, 80, 24, onCancelClick);     
        gui.mapKeyToAction(btCancel, "ESCAPE", onCancelClick);   
        
        
        // Show the modal dialog 
        dlg.setModal(true);  
        dlg.setVisible(true);     
       
        return dlg.getBooleanValue();
    }
    
    // Check whether the MAC or IP addresses are already used in the list 
    private boolean addressesCanBeUsed(JDialog dlg, String initialMAC, String initialIP, String newMAC, String newIP) {
    	
    	if (! initialMAC.equals(newMAC)) {
    		for (int i = 0; i < reservedAddressesTable.getRowCount(); i++) {
    			if (reservedAddressesTable.getValueAt(i, 0).equals(newMAC)) {

    				javax.swing.JOptionPane.showMessageDialog(dlg, messages.getString("jdhcpkonfiguration_msg19")); 
    				return false;
    			}
    		}
    	}   
    	
    	if (! initialIP.equals(newIP)) {
    		for (int i = 0; i < reservedAddressesTable.getRowCount(); i++) {
    			if (reservedAddressesTable.getValueAt(i, 1).equals(newIP)) {

    				javax.swing.JOptionPane.showMessageDialog(dlg, messages.getString("jdhcpkonfiguration_msg20")); 
    				return false;
    			}
    		}
    	}    	
    	
    	return true;
    }
    
    public boolean checkIPAddress(Component comp) {    	
    	
    	if (!((JTextField)comp).isEditable()) return true;
    	return gui.checkAndHighlight(EntryValidator.musterIpAdresse, (JTextField)comp);    	
    }  
    
    public boolean checkMACAddress(Component comp) {    	
    	
    	return gui.checkAndHighlight(EntryValidator.musterMacAddress, (JTextField)comp);    	
    }  
    
    public boolean checkAllAddresses() {    	
        
        if (!checkIPAddress(tfUpperLimit)) return false;  
        if (!checkIPAddress(tfLowerLimit)) return false;     

        if (!cbUseServerSettings.isSelected()) {
            if (!checkIPAddress(tfGateway)) return false;            
            if (!checkIPAddress(tfDNSServer)) return false;                
        }

        return true;
    }    

    private void updateGatewayAndDNS() {
    	
    	Boolean useOwnSettings = !cbUseServerSettings.isSelected();
    	
    	tfGateway.setText(dhcpServer.getGatewayIP());
    	checkIPAddress(tfGateway);
    	tfGateway.setEditable(useOwnSettings);
    	tfDNSServer.setText(dhcpServer.getDNSServerIP());
    	checkIPAddress(tfDNSServer);
    	tfDNSServer.setEditable(useOwnSettings);
    }
    
    private void updateDisplayedValues() {
        
        tfUpperLimit.setText(dhcpServer.getUpperLimit());
        tfLowerLimit.setText(dhcpServer.getLowerLimit());
        tfSubnetMask.setText(dhcpServer.getSubnetMask());
        
        tfGateway.setText(dhcpServer.getGatewayIP());
        tfDNSServer.setText(dhcpServer.getDNSServerIP());
        cbUseServerSettings.setSelected(!dhcpServer.getUseOwnGWAndDNSSettings());
        updateGatewayAndDNS();
        
        cbActive.setSelected(dhcpServer.isActive());  
        
        reservedAddressesTable.clear();
        for (DHCPAddressItem entry : dhcpServer.getReservedAddresses()) {           
        	reservedAddressesTable.addRow(entry.getMAC(), entry.getIP());
        }     
    }

    private boolean saveChanges() {
    	
    	if (!checkAllAddresses()) return false;
    	
        dhcpServer.setUpperLimit(tfUpperLimit.getText());
        dhcpServer.setLowerLimit(tfLowerLimit.getText());

        if (!cbUseServerSettings.isSelected()) {
            dhcpServer.setUseOwnGWAndDNSSettings(true);
            dhcpServer.setGatewayIP(tfGateway.getText());
            dhcpServer.setDNSServerIP(tfDNSServer.getText());
        } else {
            dhcpServer.setUseOwnGWAndDNSSettings(false);
        }

        dhcpServer.setActive(cbActive.isSelected());

        dhcpServer.clearReservedAddresses();      
        for (int i = 0; i < reservedAddressesTable.getRowCount(); i++) {
            dhcpServer.addReservedAddress(reservedAddressesTable.getStringAt(i, 0),
                                          reservedAddressesTable.getStringAt(i, 1));
        }
        
        ProjectManager.getInstance().setModified();    
        
        return true;
    }
}
