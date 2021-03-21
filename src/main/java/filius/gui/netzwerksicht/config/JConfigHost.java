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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import filius.gui.GUIContainer;
import filius.gui.GUIHelper;
import filius.gui.JBackgroundPanel;
import filius.gui.JMainFrame;
import filius.hardware.knoten.Host;
import filius.rahmenprogramm.EntryValidator;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.ProjectManager;
import filius.software.system.HostOS;

@SuppressWarnings("serial")
public class JConfigHost extends JConfigPanel implements I18n {
    
	private GUIHelper  gui;
    private JTextField tfName;
    private JTextField tfMACAddress;
    private JTextField tfIPAddress;
    private JTextField tfSubnetMask;
    private JTextField tfGateway;
    private JTextField tfDNS;
    private JCheckBox  cbUseIpAsName;
    private JCheckBox  cbUseDHCP;
    private JLabel     lbDHCPServerStatus;
    private JButton    btConfigDHCPServer;    
    private boolean    modified = false;   
 
    
    /** 
     * {@inheritDoc}
     */
    protected void initMainPanel(JBackgroundPanel mainPanel) {
    	
    	gui = new GUIHelper();
    	
    	// Common listeners
        
    	ActionListener onModification = new ActionListener() {			
        	public void actionPerformed(ActionEvent e) { modified = true; }
		};
		
		ActionListener onEnter = new ActionListener() {			
			public void actionPerformed(ActionEvent e) { saveChanges(); }
		};   
		
		ActionListener onKeyReleased = new ActionListener() {			
			public void actionPerformed(ActionEvent e) {  checkAddress((Component)e.getSource()); }
		};   
		        
        
        // Left column  
    	//-------------        
        
        // Name of the host   

		tfName = gui.addTextField(messages.getString("jhostkonfiguration_msg1"), messages.getString("jhostkonfiguration_msg2"), 
				                  mainPanel, 50, 12, 280, GUIHelper.TEXTFIELD_HEIGHT, 110, 
				                  onModification, onEnter, onEnter, null);
		
        // Use IP as Name  
        
        ActionListener onUseIPAsNameChange = new ActionListener() {        	
            public void actionPerformed(ActionEvent e) {            	
                changeDisplayName();
                modified = true;
            }
        };    
        cbUseIpAsName = gui.addCheckBox(messages.getString("jhostkonfiguration_msg10"), 
                                        mainPanel, 50, 40, 280, 15, 
                                        onUseIPAsNameChange);	              
              
        // MAC-Address 
        tfMACAddress = gui.addTextField(messages.getString("jhostkonfiguration_msg9"), "", 
	                                    mainPanel, 50, 70, 280, GUIHelper.TEXTFIELD_HEIGHT, 110, 
	                                    null, null, null, null); 
        tfMACAddress.setEditable(false);
        
        // DHCP service status
        
        lbDHCPServerStatus = gui.addLabel("", mainPanel, 50, 112, 280, 15);
        
        // DHCP server config button
        
        ActionListener onConfigDHCPServerClick = new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			showDhcpConfiguration();
    			updateDisplayedValues();
    		}
    	};    	 
        btConfigDHCPServer = gui.addButton(messages.getString("jhostkonfiguration_msg11"),
        		                           mainPanel, 210, 108, 120, GUIHelper.BUTTON_HEIGHT,
        		                           onConfigDHCPServerClick);
     

        // Right column      
        //--------------      
        
        // DHCP configuration mode (manual/DHCP) checkbox 
        
        ActionListener onUseDHCPChange = new ActionListener() {        	
            public void actionPerformed(ActionEvent e) {	
            	changeUseDHCP();
            	modified = true;
            }
        };
        cbUseDHCP = gui.addCheckBox(messages.getString("jhostkonfiguration_msg7"),
        	                    mainPanel, 460, 12, 280, 15, 
        	                    onUseDHCPChange);    
        
        // IP-Address       
        
        tfIPAddress = gui.addTextField(messages.getString("jhostkonfiguration_msg3"), "", 
	                                   mainPanel, 460, 36, 270, GUIHelper.TEXTFIELD_HEIGHT, 150, 
	                                   onModification, onEnter, onEnter, onKeyReleased);        

        // Subnet mask
        
        tfSubnetMask = gui.addTextField(messages.getString("jhostkonfiguration_msg4"), "", 
                                        mainPanel, 460, 60, 270, GUIHelper.TEXTFIELD_HEIGHT, 150, 
                                        onModification, onEnter, onEnter, onKeyReleased);    

        // Gateway Address
        
        tfGateway = gui.addTextField(messages.getString("jhostkonfiguration_msg5"), "", 
                                     mainPanel, 460, 84, 270, GUIHelper.TEXTFIELD_HEIGHT, 150, 
                                     onModification, onEnter, onEnter, onKeyReleased);  

        // DNS Address

        tfDNS = gui.addTextField(messages.getString("jhostkonfiguration_msg6"), "", 
                                 mainPanel, 460, 108, 270, GUIHelper.TEXTFIELD_HEIGHT, 150, 
                                 onModification, onEnter, onEnter, onKeyReleased);      
    }
    
    private void changeDisplayName() {
        if (getHardware() != null) {
            Host host = (Host) getHardware();
            host.setUseIPAsName(cbUseIpAsName.isSelected());
        }

        GUIContainer.getInstance().updateViewport();
        updateDisplayedValues();
    }
    
    private void changeUseDHCP() {
        if (getHardware() != null) {
            Host host = (Host) getHardware();   
            HostOS hostOS = (HostOS) host.getSystemSoftware();
            hostOS.setUseDHCPConfiguration(cbUseDHCP.isSelected());
        }

        updateIpMaskGatewayDns(!cbUseDHCP.isSelected());
    }

    private void showDhcpConfiguration() {    	

        JConfigHostDHCP dlg = new JConfigHostDHCP(JMainFrame.getInstance(), (Host) getHardware());  
        dlg.setVisible(true);
    }

    private void checkAddress(Component comp) {
    	if (comp == tfIPAddress) gui.checkAndHighlight(EntryValidator.musterIpAdresse, tfIPAddress);
    	else if (comp == tfSubnetMask) gui.checkAndHighlight(EntryValidator.musterSubNetz, tfSubnetMask);
    	else if (comp == tfDNS || comp == tfGateway)     gui.checkAndHighlight(EntryValidator.musterIpAdresseAuchLeer, (JTextField)comp);
    }      
    
    public void updateDHCPServerStatus() {
    	
    	Host host = (Host) getHardware();
        HostOS hostOS = (HostOS) host.getSystemSoftware();
        Boolean isActive = hostOS.getDHCPServer().isActive();
        
        if (isActive) lbDHCPServerStatus.setText(messages.getString("jhostkonfiguration_msg12"));
        else          lbDHCPServerStatus.setText(messages.getString("jhostkonfiguration_msg13"));        
    	cbUseDHCP.setEnabled(!isActive); 
    }

    public void updateIpMaskGatewayDns(boolean enabled) {
    	
        tfIPAddress.setEnabled(enabled);
        tfSubnetMask.setEnabled(enabled);
        tfGateway.setEnabled(enabled);
        tfDNS.setEnabled(enabled);
        btConfigDHCPServer.setEnabled(enabled);
    }
    
    /** 
     * {@inheritDoc}
     */
    public void updateDisplayedValues() {
    	    	
        Host host = (Host) getHardware();
        tfName.setText(host.getName());
        cbUseIpAsName.setSelected(host.getUseIPAsName());

        HostOS hostOS = (HostOS) host.getSystemSoftware();

        tfMACAddress.setText(hostOS.getMACAddress());
        
        tfIPAddress.setText(hostOS.getIPAddress());
        tfSubnetMask.setText(hostOS.getSubnetMask());
        tfGateway.setText(hostOS.getStandardGateway());
        tfDNS.setText(hostOS.getDNSServer());

        updateDHCPServerStatus();
        cbUseDHCP.setSelected(hostOS.getUseDHCPConfiguration());
        updateIpMaskGatewayDns(!hostOS.getUseDHCPConfiguration());    

        checkAddress(tfIPAddress);
        checkAddress(tfSubnetMask);
        checkAddress(tfGateway);
        checkAddress(tfDNS);
        
        modified = false;
    }
    
    /** 
     * {@inheritDoc}
     */
    @Override
    public void saveChanges() {

        if (getHardware() == null || modified == false) return;
        
        Host host = (Host) getHardware();
        if (!cbUseIpAsName.isSelected()) {
        	host.setName(tfName.getText());
        }

        HostOS hostOS = (HostOS) host.getSystemSoftware();
        hostOS.setIPAddress(tfIPAddress.getText());
        hostOS.setSubnetMask(tfSubnetMask.getText());
        hostOS.setStandardGateway(tfGateway.getText());
        hostOS.setDNSServer(tfDNS.getText());
        hostOS.setUseDHCPConfiguration(cbUseDHCP.isSelected());

        if (cbUseDHCP.isSelected()) {
        	hostOS.getDHCPServer().setActive(false);
        }

        GUIContainer.getInstance().updateViewport();
        
        ProjectManager.getInstance().setModified();  
        modified = false;
    }
}
