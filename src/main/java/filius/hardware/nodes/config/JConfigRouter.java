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
import filius.hardware.knoten.Router;
import filius.hardware.Cable;
import filius.rahmenprogramm.EntryValidator;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.ProjectManager;
import filius.software.system.RouterOS;

@SuppressWarnings("serial")
public class JConfigRouter extends JConfigPanel implements I18n {	

	private GUIHelper   gui;
    private JTextField  tfName;
    private JTextField  tfGateway;
    private JLabel      lbFirewallStatus;
    private JButton     btRoutingTable;
    private JCheckBox   cbUseRIP;       
    private Cable       highlightedCable = null;    
    private boolean     modified = false;  
    
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
    		public void actionPerformed(ActionEvent e) {  checkIPAddress((Component)e.getSource()); }
    	};   


    	// Left column  
    	//-------------        

    	// Name of the host   

    	tfName = gui.addTextField(messages.getString("jconfigrouter_msg1"), 
    			                  messages.getString("jconfigrouter_msg2"), 
    			                  mainPanel, 50, 12, 280, GUIHelper.TEXTFIELD_HEIGHT, 110, 
    			                  onModification, onEnter, onEnter, null);    	    	    	
    	
        // Interfaces config button
        
        ActionListener onInterfacesClick = new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			showInterfacesDialog();
    		}
    	};    	 
    	gui.addButton(messages.getString("jconfigrouter_msg3"),
        		      mainPanel, 100, 51, 180, GUIHelper.BUTTON_HEIGHT,
        		      onInterfacesClick);
    	
    	// Firewall config button
    	
    	lbFirewallStatus = gui.addLabel("", mainPanel, 50, 94, 280, 15);
        
        ActionListener onFirewallClick = new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			showFirewallDialog();
    		}
    	};    	 
    	gui.addButton(messages.getString("jconfigrouter_msg6"),  // Settings
        		      mainPanel, 190, 90, 140, GUIHelper.BUTTON_HEIGHT,
        		      onFirewallClick);
    	
    	// Right column  
    	//---------------      	   	
    	
        // Routing table config button
        
        ActionListener onRoutingTableClick = new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			showRoutingTableDialog();
    		}
    	};    	 
    	btRoutingTable = gui.addButton(messages.getString("jconfigrouter_msg7"),  // Routing table
        		                      mainPanel, 450, 12, 180, GUIHelper.BUTTON_HEIGHT,
        		                      onRoutingTableClick);
    	
        // Gateway 
    	
    	tfGateway = gui.addTextField(messages.getString("jconfigrouter_msg8"), "", // Gateway
	                                 mainPanel, 430, 51, 220, GUIHelper.TEXTFIELD_HEIGHT, 90, 
	                                 onModification, onEnter, onEnter, onKeyReleased);  
        
        // Autorouting checkbox
    	
    	ActionListener onUseRIPChange = new ActionListener() {			
    		public void actionPerformed(ActionEvent e) { 

    	        RouterOS os = (RouterOS) ((Router) getHardware()).getSystemSoftware();  
    	        os.setRipEnabled(cbUseRIP.isSelected());
    	        
    	        btRoutingTable.setEnabled(!cbUseRIP.isSelected());
    	        tfGateway.setEnabled(!cbUseRIP.isSelected());    	        
    		}
    	};  
        cbUseRIP = gui.addCheckBox(messages.getString("jconfigrouter_msg9"), // Automatic routing
                                   mainPanel, 450, 95, 280, 15, 
                                   onUseRIPChange);
    }
       
    private void showInterfacesDialog() {
    	
    	JConfigRouterInterfaces dlg = new JConfigRouterInterfaces(JMainFrame.getInstance(), (Router) getHardware());
    	dlg.setVisible(true);   
    }
    
    private void showRoutingTableDialog() {
    	
    	JConfigRouterRules dlg = new JConfigRouterRules(JMainFrame.getInstance(), (Router) getHardware());
    	dlg.setVisible(true);  
    }    
    
    private void showFirewallDialog() {
    	
    	JConfigRouterFirewall dlg = new JConfigRouterFirewall(JMainFrame.getInstance(), (Router) getHardware());
    	dlg.setVisible(true); 
    	updateFirewallStatus(); 
    }
    
    public boolean checkIPAddress(Component comp) {    	
    	
    	if (!((JTextField)comp).isEditable()) return true;
    	return gui.checkAndHighlight(EntryValidator.musterIpAdresseAuchLeer, (JTextField)comp);    	
    }  

    public void highlightCable() {
    	
        if (highlightedCable != null) highlightedCable.setActive(true);
    }
    
    public void updateFirewallStatus() {
    	
    	Router router = (Router) getHardware();
    	RouterOS routerOS = (RouterOS) router.getSystemSoftware();
        boolean isActive = routerOS.getFirewall().isActivated();
        
        if (isActive) lbFirewallStatus.setText(messages.getString("jconfigrouter_msg4"));  
        else          lbFirewallStatus.setText(messages.getString("jconfigrouter_msg5"));        
    }

    /** 
     * {@inheritDoc}
     */
    public void updateDisplayedValues() {        
        
        Router router = (Router) getHardware();        
        RouterOS os = (RouterOS) router.getSystemSoftware();        

        tfName.setText(router.getDisplayName());    
        updateFirewallStatus();
        
        btRoutingTable.setEnabled(!os.isRIPEnabled());        
        tfGateway.setText(os.getStandardGateway());
        tfGateway.setEnabled(!os.isRIPEnabled());
        checkIPAddress(tfGateway);
        cbUseRIP.setSelected(os.isRIPEnabled());    
        
        modified = false;
    }
    
    /** 
     * {@inheritDoc}
     */
    public void saveChanges() {
    	
    	if (modified == false) return;
        
        Router router = (Router) getHardware();
        RouterOS os = (RouterOS) router.getSystemSoftware();

        router.setName(tfName.getText());
        os.setStandardGateway(tfGateway.getText());             
        os.setRipEnabled(cbUseRIP.isSelected());
        
        GUIContainer.getInstance().updateViewport();
        
        ProjectManager.getInstance().setModified(); 
        modified = false;
    }
}
