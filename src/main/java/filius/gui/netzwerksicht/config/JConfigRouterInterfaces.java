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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ListIterator;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import filius.gui.GUIContainer;
import filius.gui.GUIHelper;
import filius.gui.JExtendedDialog;
import filius.gui.JExtendedTable;
import filius.gui.Palette;
import filius.hardware.Cable;
import filius.hardware.NetworkInterface;
import filius.hardware.Port;
import filius.hardware.knoten.Router;
import filius.rahmenprogramm.EntryValidator;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.ProjectManager;
import filius.rahmenprogramm.nachrichten.PacketAnalyzer;

@SuppressWarnings("serial")
public class JConfigRouterInterfaces extends JExtendedDialog implements I18n {
		
	private GUIHelper       gui;
	private GUIContainer    container;
	private Router          router;
	private Frame           owner;
	private JExtendedTable  table;
	private Cable           highlightedCable = null;
	
	
	public JConfigRouterInterfaces(Frame owner, Router router) {
    	
        super(owner, true);
        container = GUIContainer.getInstance();        
        this.owner = owner;
        this.router = router;
        
        initComponents();   
        
        //addCloseListener();

        updateDisplayedValues();
    }

	private void initComponents() {		
		
		gui = new GUIHelper();

        setTitle(messages.getString("jconfigrouterinterfaces_msg1"));        

        // Size and Location 
        setSize(580, 221);
        setResizable(false);
        // Above the button used to pop up the dialogbox
        setLocation(owner.getX() + 10, owner.getY() + owner.getHeight() - getHeight() - 10); 
        
        // Table
        table = new JExtendedTable(5);      
        table.setBackground(Palette.ROUTER_TABLE_EVEN_ROW_BG);
        table.setRowColors(Palette.ROUTER_TABLE_EVEN_ROW_BG, Palette.ROUTER_TABLE_ODD_ROW_BG);
        
        table.setHeader(0, messages.getString("jconfigrouterinterfaces_msg2"));  // N°
        table.setHeader(1, messages.getString("jconfigrouterinterfaces_msg3"));  // MAC address
        table.setHeader(2, messages.getString("jconfigrouterinterfaces_msg4"));  // IP address
        table.setHeader(3, messages.getString("jconfigrouterinterfaces_msg5"));  // Mask
        table.setHeader(4, messages.getString("jconfigrouterinterfaces_msg6"));   // Connected to
        table.setHeaderResizable(false);   
        
        table.setColumnWidth(0, 20);
        table.setColumnWidth(1, 120);
        table.setColumnWidth(2, 110);
        table.setColumnWidth(3, 110);
        
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
        		
        		int count = table.getRowCount();
        		if (count < 8) addNICEntry();
        	}
        };       	
    	gui.mapKeyToAction(table, "INSERT", onAddRow);
    	
    	// Edit a row
    	AbstractAction onEditRow = new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {
        		
        		int row = table.getSelectedRow();
        		if (row < 0) return;
        		if (showRowEditDialog(row)) ;   		
        	}
        };   
        table.setDoubleClickListener(onEditRow); 
        
        // Swap a row
//    	AbstractAction onSwapRow = new AbstractAction() {
//        	public void actionPerformed(ActionEvent e) {
//        		
//        		int row = table.getSelectedRow();
//        		if (row < 0) return;
//        		   		
//        	}
//        };   
    	
    	// Delete a row
    	AbstractAction onDeleteRow = new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {     
        	
        		if (table.getRowCount() == 2) return; 
        		
        		removeNICEntry(table.getSelectedRow());   
        	}
        };       	
    	gui.mapKeyToAction(table, "DELETE", onDeleteRow);  
    	
    	// Close the dialog
    	AbstractAction closeActionListener = new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {        		
        		close(); 
        	}
        };       	
    	gui.mapKeyToAction(table, "ESCAPE", closeActionListener);    	
       	
    	// POPUP MENU
    	
    	MouseAdapter popupMenuAdapter = new MouseAdapter() {
    		
    		public void mouseReleased(MouseEvent e) {
    			
    			JPopupMenu menu = new JPopupMenu();
    			
    			// Add item
    			JMenuItem miAdd = new JMenuItem(messages.getString("jconfigrouterinterfaces_msg7")); // Add 
		    	miAdd.addActionListener(onAddRow);
		    	miAdd.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0));
		    	menu.add(miAdd);
        		if (table.getRowCount() == 8) { miAdd.setEnabled(false); }

    			int row = table.getSelectedRow();
    			if (row >= 0) {   
    		    	
    				// Edit selected item
    		    	JMenuItem miEdit = new JMenuItem(messages.getString("jconfigrouterinterfaces_msg8")); // Edit
    		    	miEdit.addActionListener(onEditRow);
    		    	menu.add(miEdit);	    
    		    	
    		    	// Delete selected item
    		    	JMenuItem miDelete = new JMenuItem(messages.getString("jconfigrouterinterfaces_msg9")); // Delete
    		    	miDelete.addActionListener(onDeleteRow);
    		    	miDelete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
    		    	menu.add(miDelete);   
    		    	if (table.getRowCount() == 2) { miDelete.setEnabled(false); }    				    	
    	        }     
    			
    			menu.show(table, e.getX(), e.getY());
    		}   
    	};  
    	table.setPopupMenuAdapter(popupMenuAdapter); 
    	
    	// Selection change listener
    	
    	AbstractAction selectionChangeListener = new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {
        		
        		highlightCable(table.getSelectedRow());   		
        	}
        };       	
    	table.setSelectionChangeListener(selectionChangeListener);
    	
        // Close listener to unselect the highlighted cable if any
    	addWindowListener(new WindowAdapter() {
    		public void windowClosing(WindowEvent e) { highlightCable(-1); }
    	});    	 
	}	
    
    // Dialog box used to create or modify an interface
    private boolean showRowEditDialog (int rowIndex) {
     	
    	JExtendedDialog dlg = new JExtendedDialog();
        
        dlg.setTitle(messages.getString("jconfigrouterinterfaces_msg10")); // Interface settings
        dlg.setSize(352, 218);
        dlg.setResizable(false);  
        int y = getY() - dlg.getHeight() + 28;
        dlg.setLocation(getX() + (getWidth() - dlg.getWidth())/2, y);   
        dlg.setLayout(null);    
        
        // Interface number
        
        JPanel pn = gui.addTitledPanel(messages.getString("jconfigrouterinterfaces_msg11")+" "+String.valueOf(rowIndex+1), 
        		                       dlg, 10, 10, dlg.getWidth()-36, 130);    
                        
        // MAC textfield      
        
        JTextField tfMAC = gui.addTextField(messages.getString("jconfigrouterinterfaces_msg3"), (String)table.getValueAt(rowIndex, 1), 
        		                            pn, 20, 20, 276, GUIHelper.TEXTFIELD_HEIGHT, 151,
        		                            null, null, null, null); 
        tfMAC.setEditable(false);
        
        // IP textfield    
                
        ActionListener onKeyReleased = new ActionListener() {			
			public void actionPerformed(ActionEvent e) { checkIPAddress((Component)e.getSource()); }
		};  
        
        JTextField tfIP = gui.addTextField(messages.getString("jconfigrouterinterfaces_msg4"), (String)table.getValueAt(rowIndex, 2), 
        		                           pn, 20, 46, 276, GUIHelper.TEXTFIELD_HEIGHT, 151,
        		                           null, null, null, onKeyReleased); 
        
        // Mask textfield   
        
        ActionListener onKeyReleased2 = new ActionListener() {			
			public void actionPerformed(ActionEvent e) { checkMask((Component)e.getSource()); }
		}; 
        
        JTextField tfMask = gui.addTextField(messages.getString("jconfigrouterinterfaces_msg5"), (String)table.getValueAt(rowIndex, 3), 
        		                             pn, 20, 72, 276, GUIHelper.TEXTFIELD_HEIGHT, 151,
        		                             null, null, null, onKeyReleased2);         
        
        // Connected node              
       
//        gui.addTextField(messages.getString("jconfigrouterinterfaces_msg6"), (String)table.getValueAt(rowIndex, 4), 
//        		         pn, 20, 98, 276, GUIHelper.TEXTFIELD_HEIGHT, 104,
//        		         null, null, null, null);       
        JComboBox<String> cb = gui.addComboBox(messages.getString("jconfigrouterinterfaces_msg6"), (String)table.getValueAt(rowIndex, 4), 
		                pn, 20, 98, 276, GUIHelper.TEXTFIELD_HEIGHT, 104,
		                null, null, null, null); 
        // temporary
        cb.addItem((String)table.getValueAt(rowIndex, 4));
        cb.setSelectedIndex(0);
        
        // OK & Cancel buttons
        
        AbstractAction onOKClick = new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {
        		
        		if (! checkIPAddress(tfIP) || ! checkMask(tfMask)) return;
              		
        		updateNIC(rowIndex, tfIP.getText(), tfMask.getText());
        		
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
    
    private void addNICEntry() {
		
    	NetworkInterface nic = router.addNIC();
        int index = router.getNICCount() - 1; // Index of the newly created NIC
    	 
		nic.setIp("192.168."+String.valueOf(index)+".1");
    	table.addRow(String.valueOf(index+1), nic.getMac(), nic.getIp(), nic.getSubnetMask(), "");

		saveChanges();
	}  
    
    private void updateNIC(int entryIndex, String ip, String mask) {
		
    	NetworkInterface nic = router.getNIC(entryIndex);
    	if (nic == null) return;
    	nic.setIp(ip);
    	nic.setSubnetMask(mask);
    	
    	table.setValueAt(ip, entryIndex, 2);
		table.setValueAt(mask, entryIndex, 3);
		
		saveChanges();
	}        
    
	private void removeNICEntry(int entryIndex) {
		
		NetworkInterface nic = router.getNIC(entryIndex);
		if (nic == null) return;
				
		router.removeNIC(nic);  
		
		PacketAnalyzer.getInstance().removeID(nic.getMac());        		
		
		Cable cable = nic.getPort().getCable();
		if (cable != null) container.removeCableItem(cable.getCableItem());
				      		
		table.clear();
		updateDisplayedValues();
		
		if (entryIndex > table.getRowCount()-1) entryIndex--; 
    	table.setSelectedRow(entryIndex);
    	
		saveChanges();
	}    
    
    public boolean checkIPAddress(Component comp) {    	
    	
    	if (!((JTextField)comp).isEditable()) return true;
    	return gui.checkAndHighlight(EntryValidator.musterIpAdresse, (JTextField)comp);    		
    }
    
    public boolean checkMask(Component comp) {    	
    	
    	if (!((JTextField)comp).isEditable()) return true;
    	return gui.checkAndHighlight(EntryValidator.musterSubNetz, (JTextField)comp);    		
    }  
    
    private void createEmptyRows(int count) {
    	table.clear();
    	for (int i = 0; i<count; i++) table.addRow("","","","","");
    }
        
    private void highlightCable(int rowIndex) {     
        
        if (highlightedCable != null) highlightedCable.setActive(false);
        if (rowIndex < 0) return;
        
        Cable cable = router.getNetworkInterfaces().get(rowIndex).getPort().getCable();        
      
        if (cable != null) {
        	cable.setActive(true);
        	highlightedCable = cable;
        }
    }
    
//    private void addCloseListener() {
//
//    	addWindowListener(new WindowAdapter() {
//    	  public void windowClosing(WindowEvent e) { highlightCable(-1); }
//    	});
//    }
	
    private void updateDisplayedValues() {    	      
      	      
      // Create the necessary rows
      int nicCount = router.getNICCount();
      if (table.getRowCount() == 0) createEmptyRows(nicCount);
      
      // Populate the rows
      ListIterator<NetworkInterface> it = router.getNICList().listIterator();
      for (int i = 0; it.hasNext(); i++) {
    	  NetworkInterface nic = it.next();    		   	  
    	  table.setValueAt(String.valueOf(i+1), i, 0);  
    	  table.setValueAt(nic.getMac(), i, 1);  
    	  table.setValueAt(nic.getIp(), i, 2);
    	  table.setValueAt(nic.getSubnetMask(), i, 3);
    	  Port rp = nic.getPort().getRemotePort();
    	  if (rp != null) {
    		  String name = rp.getOwner().getName();
    		  if (name.contentEquals("")) name = "("+messages.getString("jvermittlungsrechnerkonfiguration_msg35")+")";
    		  table.setValueAt(" "+name, i, 4);
    	  }
      }    
	}

    public void saveChanges() {

    	// There is nothing to save here since the interfaces are modified on the fly

    	GUIContainer.getInstance().updateViewport();

    	ProjectManager.getInstance().setModified(); 
    }
}
