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
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.ListIterator;
import javax.swing.AbstractAction;
import javax.swing.JButton;
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
import filius.software.system.InternetNodeOS;
import filius.software.vermittlungsschicht.RoutingTable;

@SuppressWarnings("serial")
public class JConfigRouterRules extends JDialog implements I18n{
	
	private GUIHelper       gui;
	private Frame           owner;
	private JTextField      tfMask;
	private JExtendedTable  table;
	private RoutingTable    routingTable;
	private int             autoRoutesCount;

    public JConfigRouterRules(Frame owner, Router router) {
    	
        super(owner, true);     
        this.owner = owner;
        
        routingTable = ((InternetNodeOS)router.getSystemSoftware()).getRoutingTable();
        
        initComponents();   

        updateDisplayedValues();
    }

    private void initComponents() {		

    	gui = new GUIHelper();

    	setTitle(messages.getString("jconfigrouterrules_msg1"));  // Routing table      

    	// Size and Location 
    	setSize(515, 400);
    	setResizable(false);
    	// At the bottom of the main window
    	setLocation(owner.getX() + 230, owner.getY() + owner.getHeight() - getHeight() - 20);      
    	
    	// Table
    	table = new JExtendedTable(4);      
    	table.setBackground(Palette.ROUTER_TABLE_EVEN_ROW_BG);
    	table.setRowColors(Palette.ROUTER_TABLE_EVEN_ROW_BG, Palette.ROUTER_TABLE_ODD_ROW_BG);

    	table.setHeader(0, messages.getString("jconfigrouterrules_msg2"));  // Target IP (network or host)
    	table.setHeader(1, messages.getString("jconfigrouterrules_msg3"));  // Target mask (network or host)
    	table.setHeader(2, messages.getString("jconfigrouterrules_msg4"));  // Next gateway
    	table.setHeader(3, messages.getString("jconfigrouterrules_msg5"));  // IP of the output interface used to reach the next gateway
    	table.setHeaderResizable(false);   

    	table.setColumnWidth(0, 120);
    	table.setColumnWidth(1, 120);
    	table.setColumnWidth(2, 120);
    	table.setColumnWidth(3, 120);

    	table.setColumnAlignment(0, JLabel.CENTER);
    	table.setColumnAlignment(1, JLabel.CENTER);
    	table.setColumnAlignment(2, JLabel.CENTER);
    	table.setColumnAlignment(3, JLabel.CENTER);    
    	
    	getContentPane().add(new JScrollPane(table));  
    	
        //-------------------------------------------------------------
        
        // ACTIONS
    	
    	// Add a row
    	AbstractAction onAddRow = new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {
        		
        		showRowEditDialog(-1);
        	}
        };       	
    	gui.mapKeyToAction(table, "INSERT", onAddRow);
    	
    	// Edit a row
    	AbstractAction onEditRow = new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {
        		
        		int rowIndex = table.getSelectedRow();
        		if (!isEditable(rowIndex)) return;
        		if (showRowEditDialog(rowIndex)) ;   		
        	}
        };   
        table.setDoubleClickListener(onEditRow); 
    	
    	// Delete a row
    	AbstractAction onDeleteRow = new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {     
        	
        		int rowIndex = table.getSelectedRow();       
        		removeRule(rowIndex);
        	}
        };       	
    	gui.mapKeyToAction(table, "DELETE", onDeleteRow);  
    	
    	// Close the dialog
    	AbstractAction closeActionListener = new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {
        		closeDialog();
        	}
        };       	
    	gui.mapKeyToAction(table, "ESCAPE", closeActionListener);  
    	
  	    // POPUP MENU
    	
    	MouseAdapter popupMenuAdapter = new MouseAdapter() {
    		
    		public void mouseReleased(MouseEvent e) {
    			
    			JPopupMenu menu = new JPopupMenu();
    			
    			// Add item
    			JMenuItem miAdd = new JMenuItem(messages.getString("jconfigrouterrules_msg6")); // Add a rule
		    	miAdd.addActionListener(onAddRow);
		    	miAdd.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0));
		    	menu.add(miAdd);        		

    			int row = table.getSelectedRow();
    			if (isEditable(row)) {  
    		    	
    				// Edit selected item
    		    	JMenuItem miEdit = new JMenuItem(messages.getString("jconfigrouterrules_msg7")); // Edit a rule
    		    	miEdit.addActionListener(onEditRow);
    		    	menu.add(miEdit);	    
    		    	
    		    	// Delete selected item
    		    	JMenuItem miDelete = new JMenuItem(messages.getString("jconfigrouterrules_msg8")); // Delete a rule
    		    	miDelete.addActionListener(onDeleteRow);
    		    	miDelete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
    		    	menu.add(miDelete);      		    	 				    	
    	        }     
    			
    			menu.show(table, e.getX(), e.getY());
    		}   
    	};  
    	table.setPopupMenuAdapter(popupMenuAdapter); 
    	
    }
    
    private boolean isEditable(int rowIndex) {
    	
    	return (rowIndex >= autoRoutesCount); 
    }    

    // Dialog box used to create or modify a rule
    private boolean showRowEditDialog (int rowIndex) {
     	
    	JExtendedDialog dlg = new JExtendedDialog();
        
        if (rowIndex < 0) dlg.setTitle(messages.getString("jconfigrouterrules_msg6"));  // Add a rule
        else              dlg.setTitle(messages.getString("jconfigrouterrules_msg7"));  // Edit a rule
        dlg.setSize(352, 218);
        dlg.setResizable(false);  
        int y = getY() - dlg.getHeight() + 28;
        dlg.setLocation(getX() + (getWidth() - dlg.getWidth())/2, y);   
        dlg.setLayout(null);    
        
        // Rule panel
        
        JPanel pn = gui.addTitledPanel(messages.getString("jconfigrouterrules_msg9"), 
        		                       dlg, 10, 10, dlg.getWidth()-36, 130);    
                        
        // Destination IP 
        
        ActionListener onKeyReleased = new ActionListener() {			
			public void actionPerformed(ActionEvent e) { checkAddress((Component)e.getSource()); }
		}; 
        
		String value = "";
		
		if (rowIndex >= 0) value = (String) table.getValueAt(rowIndex, 0); 
        JTextField tfIP = gui.addTextField(messages.getString("jconfigrouterrules_msg2"), value, 
        		                           pn, 20, 20, 276, GUIHelper.TEXTFIELD_HEIGHT, 151,
        		                           null, null, null, onKeyReleased); 
        checkAddress(tfIP);
        
        // Subnet mask       
        
        if (rowIndex >= 0) value = (String) table.getValueAt(rowIndex, 1); 
        tfMask = gui.addTextField(messages.getString("jconfigrouterrules_msg3"), value, 
        		                  pn, 20, 46, 276, GUIHelper.TEXTFIELD_HEIGHT, 151,
        		                  null, null, null, onKeyReleased); 
        checkAddress(tfMask);
        
        // Next gateway    
        
        if (rowIndex >= 0) value = (String) table.getValueAt(rowIndex, 2); 
        JTextField tfGateway = gui.addTextField(messages.getString("jconfigrouterrules_msg4"), value, 
        		                               pn, 20, 72, 276, GUIHelper.TEXTFIELD_HEIGHT, 151,
        		                               null, null, null, onKeyReleased);         
        checkAddress(tfGateway);
        
        // Connected node              
       
        if (rowIndex >= 0) value = (String) table.getValueAt(rowIndex, 3); 
        JTextField tfOutputIP = gui.addTextField(messages.getString("jconfigrouterrules_msg5"), value, 
                                                 pn, 20, 98, 276, GUIHelper.TEXTFIELD_HEIGHT, 151,
                                                 null, null, null, onKeyReleased);    
        checkAddress(tfOutputIP);        
        
        // OK & Cancel buttons
        
        AbstractAction onOKClick = new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {
        		
        		if (! checkAddress(tfIP) || ! checkAddress(tfMask) || ! checkAddress(tfGateway) || ! checkAddress(tfOutputIP)) return;
              		
        		if (rowIndex < 0) addRule(tfIP.getText(), tfMask.getText(), tfGateway.getText(), tfOutputIP.getText());
        		else          	  updateRule(rowIndex, tfIP.getText(), tfMask.getText(), tfGateway.getText(), tfOutputIP.getText());
        		
        		dlg.setBooleanValue(true);
        		dlg.setVisible(false);
        	}
        };        
        JButton btOK = gui.addButton(messages.getString("main_dlg_OK"), dlg, 157, 148, 80, 24, onOKClick);    
        gui.mapKeyToAction(btOK, "ENTER", onOKClick);  
                  
        AbstractAction onCancelClick = new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {
        		dlg.setVisible(false);
        	}
        };        
        JButton btCancel = gui.addButton(messages.getString("main_dlg_CANCEL"), dlg, 244, 148, 80, 24, onCancelClick);     
        gui.mapKeyToAction(btCancel, "ESCAPE", onCancelClick);   
        
        
        // Show the modal dialog 
        dlg.setModal(true);  
        dlg.setVisible(true);     
       
        return dlg.getBooleanValue();
    }    
    
    private void addRule(String targetIP, String targetMask, String gateway, String outputIP) {
    	
    	routingTable.addManualEntry(targetIP, targetMask, gateway, outputIP);
    	table.addRow(targetIP, targetMask, gateway, outputIP);	
    	
    	ProjectManager.getInstance().setModified(); 
	}   
    
    private void updateRule(int ruleIndex, String targetIP, String targetMask, String gateway, String outputIP) {
    	
    	routingTable.setManualEntry(ruleIndex - autoRoutesCount, targetIP, targetMask, gateway, outputIP);
    	table.updateRow(ruleIndex, targetIP, targetMask, gateway, outputIP);	
    	
    	ProjectManager.getInstance().setModified(); 
	}  
    
    private void removeRule(int ruleIndex) {
    	
    	if (ruleIndex < autoRoutesCount || ruleIndex >= table.getRowCount()) return;
    	
    	routingTable.removeManualEntry(ruleIndex - autoRoutesCount);
    	table.removeRow(ruleIndex); 	

    	if (ruleIndex > table.getRowCount()-1) ruleIndex--; 
    	table.setSelectedRow(ruleIndex);
    	
    	ProjectManager.getInstance().setModified(); 
	}  
    
    public boolean checkAddress(Component comp) {    	
    	
    	if (!((JTextField)comp).isEditable()) return true;
    	if (comp == tfMask) return gui.checkAndHighlight(EntryValidator.musterSubNetz, (JTextField)comp);    	
    	else                return gui.checkAndHighlight(EntryValidator.musterIpAdresse, (JTextField)comp);    	
    }      
    
    public void closeDialog() {
    	dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)); 
    }
    
    private void updateDisplayedValues() {    	
    	
    	autoRoutesCount = routingTable.getAutoRouteCount();   
    	table.setHorizontalSeparator(routingTable.getAutoRouteCount()-1);
    	
    	ListIterator<String[]> it = routingTable.getRouteList().listIterator();
        while (it.hasNext()) {
        	String[] rule = it.next(); 
    		table.addRow(rule[0], rule[1], rule[2], rule[3]);
        }
    }

//    public void saveChanges() {
//
//    	// There is nothing to save here since the rules are modified on the fly
//
//    	GUIContainer.getInstance().updateViewport();//
//    	ProjectManager.getInstance().setModified(); 
//    }
}
