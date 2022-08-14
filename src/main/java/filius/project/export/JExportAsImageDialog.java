package filius.project;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import filius.auxiliary.I18n;
import filius.auxiliary.components.GUIHelper;
import filius.auxiliary.components.JExtendedDialog;
import filius.auxiliary.components.JExtendedTable;
import filius.design.nodes.Host;
import filius.simul.os.HostOS;
import filius.simul.soft.clientserver.dhcp.DHCPServer;

@SuppressWarnings("serial")
public class JExportDialog extends JExtendedDialog implements I18n {
	
	private GUIHelper      gui;
	private JButton        button;
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
    private JDialog        reservedAddressesDialog = null;
    private JExtendedTable reservedAddressesTable;
    

	public JExportDialog(JFrame owner) {

		super(owner, true);      

		initComponents();
	}  

	private void initComponents() {

		gui = new GUIHelper();
    	
    	setTitle(messages.getString("guimainmemu_msg19"));   
    	
    	// Size and Location
        setSize(374, 390);
        setResizable(false);
        // Above the button used to pop the dialogbox up
        Point bPos = button.getLocationOnScreen();
        setLocation(bPos.x - (getWidth() - button.getWidth())/2, bPos.y - getHeight() + 34); 
    	
    	Container contentPane = getContentPane();
    	contentPane.setLayout(null);
    	
        
        // Range of distributed IP addresses
        
        JPanel pnIPRange = gui.addTitledPanel(messages.getString("jconfighostdhcp_msg1"), contentPane, 10, 10, getWidth()-36, 140);        
        
        ActionListener onKeyReleased = new ActionListener() {			
			public void actionPerformed(ActionEvent e) {  checkIPAddress((Component)e.getSource()); }
		};   
        tfLowerLimit = gui.addTextField(messages.getString("jconfighostdhcp_msg2"), "", 
        		                        pnIPRange, 20, 20, 300, GUIHelper.TEXTFIELD_HEIGHT, 190,
        		                        null, null, null, onKeyReleased);
        
        tfUpperLimit = gui.addTextField(messages.getString("jconfighostdhcp_msg3"), "", 
        		                        pnIPRange, 20, 46, 300, GUIHelper.TEXTFIELD_HEIGHT, 190, 
        		                        null, null, null, onKeyReleased);
        
        tfSubnetMask = gui.addTextField(messages.getString("jconfighostdhcp_msg4"), "", 
        		                        pnIPRange, 20, 72, 300, GUIHelper.TEXTFIELD_HEIGHT, 190, 
        		                        null, null, null, null);
        
        tfSubnetMask.setEditable(false);
        
        createReservedAdressesDialog();
        ActionListener onConfigReservedAdressesClick = new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		showReservedAddressesDialog();
        	}
        };
        gui.addButton(messages.getString("jconfighostdhcp_msg5"), pnIPRange, 105, 103, 130, 24, onConfigReservedAdressesClick);
        
        // Gateway and DNS addresses 
        
        JPanel pnGWDNS = gui.addTitledPanel(messages.getString("jconfighostdhcp_msg6"), contentPane, 10, 154, getWidth()-36, 100);        
        
        tfGateway = gui.addTextField(messages.getString("jconfighostdhcp_msg7"), "", 
        		                     pnGWDNS, 20, 20, 300, GUIHelper.TEXTFIELD_HEIGHT, 190, 
        		                     null, null, null, onKeyReleased);
        
        tfDNSServer = gui.addTextField(messages.getString("jconfighostdhcp_msg8"), "",
        		                       pnGWDNS, 20, 46, 300, GUIHelper.TEXTFIELD_HEIGHT, 190, 
        		                       null, null, null, onKeyReleased);
        
        ActionListener onUseServerSettingsChange = new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		updateGatewayAndDNS();
        	}
        }; 
        cbUseServerSettings = gui.addCheckBox(messages.getString("jconfighostdhcp_msg9"), 
        		                              pnGWDNS, 20, 74, 300, 15, 
        		                              onUseServerSettingsChange);
        
        // DHCP server active checkbox
        
        JPanel pnServerStatus = gui.addTitledPanel(messages.getString("jconfighostdhcp_msg10"), contentPane, 10, 258, getWidth()-36, 56);    
        
        cbActive = gui.addCheckBox(messages.getString("jconfighostdhcp_msg11"), 
        		                   pnServerStatus, 20, 26, 200, 15, null);
        
        
        // OK & Cancel buttons
        
        AbstractAction onOKClick = new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {
        		if (saveChanges()) close();
        	}
        };        
        btOK = gui.addButton(messages.getString("main_dlg_OK"), contentPane, 139, 320, 100, 24, onOKClick);     
        gui.mapKeyToAction(btOK, "ENTER", onOKClick);  
                  
        AbstractAction onCancelClick = new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {
        		close();
        	}
        };        
        btCancel = gui.addButton(messages.getString("main_dlg_CANCEL"), contentPane, 246, 320, 100, 24, onCancelClick);     
        gui.mapKeyToAction(btCancel, "ESCAPE", onCancelClick);   

        updateDisplayedValues();
	}


}
