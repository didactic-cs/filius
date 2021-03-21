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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import filius.gui.GUIContainer;
import filius.gui.GUIHelper;
import filius.gui.JBackgroundPanel;
import filius.hardware.knoten.Modem;
import filius.rahmenprogramm.EntryValidator;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.ProjectManager;
import filius.software.system.ModemFirmware;
import filius.software.system.ModemFirmware.ModemMode;
import filius.software.system.ModemFirmware.ModemStatus;

@SuppressWarnings("serial")
public class JConfigModem extends JConfigPanel implements I18n {

	private GUIHelper gui;
    private static final String CMD_CONNECT = "Connect";
    private static final String CMD_DISCONNECT = "Disconnect";
    private JTextField tfName;
    private JTextField tfLocalPort;
    private JCheckBox cbServerMode;
    private JTextField tfRemoteIpAddress;
    private JTextField tfRemotePort;    
    private JButton btStartStop;
    private boolean modified = false;
  
    
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
		
		ActionListener onIPChange = new ActionListener() {			
    		public void actionPerformed(ActionEvent e) { checkIPAddress((Component)e.getSource()); }
    	};
		
		ActionListener onPortChange = new ActionListener() {			
    		public void actionPerformed(ActionEvent e) { checkPort((Component)e.getSource()); }
    	};   
                
        // Left column  
    	//-------------        
        
        // Name of the modem    

		tfName = gui.addTextField(messages.getString("jmodemkonfiguration_msg1"), "", 
				                  mainPanel, 50, 12, 280, GUIHelper.TEXTFIELD_HEIGHT, 110, 
				                  onModification, onEnter, onEnter, null);
             
        
        // Local port number        
		
		tfLocalPort = gui.addTextField(messages.getString("jmodemkonfiguration_msg4"), "", 
	                                   mainPanel, 50, 36, 280, GUIHelper.TEXTFIELD_HEIGHT, 110, 
	                                   onModification, onEnter, onEnter, onPortChange);
      
        // Wait for incoming connections    

        ActionListener onServerModeChange = new ActionListener() {			
			public void actionPerformed(ActionEvent e) { updateModemStatus(true); }
		};
		cbServerMode = gui.addCheckBox(messages.getString("jmodemkonfiguration_msg2"), 
			                           mainPanel, 50, 80, 280, 15, 
			                           onServerModeChange);	        

        // Right column      
        //--------------        
        
        // Remote Modem   	
        
		gui.addLabel(messages.getString("jmodemkonfiguration_msg9"),
        		     mainPanel, 400, 2, 280, GUIHelper.TEXTFIELD_HEIGHT); 

        // Remote IP Address      
        
        tfRemoteIpAddress = gui.addTextField(messages.getString("jmodemkonfiguration_msg3"), "", 
                                             mainPanel, 400, 26, 280, GUIHelper.TEXTFIELD_HEIGHT, 110, 
                                             onModification, onEnter, onEnter, onIPChange);        
        
        // Remote port number   
        
        tfRemotePort = gui.addTextField(messages.getString("jmodemkonfiguration_msg4"), "", 
                                        mainPanel, 400, 50, 280, GUIHelper.TEXTFIELD_HEIGHT, 110, 
                                        onModification, onEnter, onEnter, onPortChange);        
        
        // Start-stop button
        
        ActionListener onClick = new ActionListener() {
    		public void actionPerformed(ActionEvent e) { updateModemStatus(false); }
    	};     
        btStartStop = gui.addButton(messages.getString("jmodemkonfiguration_msg2"), 
        		                    mainPanel, 480, 80, 120, GUIHelper.BUTTON_HEIGHT, 
                                    onClick);   
    }
    
    // Try to apply the required operation to the modem,
    // then update the checkbox and button display
    private void updateModemStatus(boolean serverModeChanged) {
        
        Modem modem = (Modem) getHardware();
        ModemFirmware firmware = modem.getFirmware();
        
        if (serverModeChanged) {
        	// The checkbox status changed; we toggle the modem between client and server modes
        	
        	if (cbServerMode.isSelected()) {    
        		// Switch from client to server
        		firmware.setMode(ModemMode.SERVER);
        		firmware.startServer();
    			// Check if the server could be started
    			if (!firmware.isServerRunning()) {    				      			
    				cbServerMode.setSelected(false);   
    				firmware.setMode(ModemMode.CLIENT);
    				// Add a message to notify the user that the server could not 
    				// be started (perhaps because the port is already used)
    				JOptionPane.showMessageDialog(null, messages.getString("jmodemkonfiguration_msg10")); 
    			}
        	} else {
        		// Switch from server to client
        		firmware.closeConnection();
        		firmware.setMode(ModemMode.CLIENT);
        	} 
        	
        } else {
        	// The button was clicked; we establish or release the connection
            
            if (cbServerMode.isSelected()) {
            	// The modem acts as a server
            	// In this case, the button can only be used to cancel a connection
            	firmware.closeConnection();
            	firmware.startServer();              
            } else {
            	// The Modem acts as a client    
            	if (cbServerMode.isEnabled()) {
                    firmware.startClient();
            	} else {
            		firmware.closeConnection();
            	}                		
            }
        }
        
        // Change the button to reflect the current status
        updateButtonDisplay();
    }
    
    /**
     * <b>updateComponents</b> updates the enabled status of the button as well
     * as its text according to the current status of the modem.
     */
    public void updateButtonDisplay() {
    	
    	Color grayBG= new Color(230,230,230);
    	Color lightGrayText= new Color(210,210,210);
    	Color grayText= new Color(150,150,150);    	
        
    	Modem modem = (Modem) getHardware();
    	
    	if (cbServerMode.isSelected()) {    
    		// The modem acts as a server
    		tfRemoteIpAddress.setEnabled(false);  
    		tfRemoteIpAddress.setBackground(grayBG);    
    		tfRemoteIpAddress.setDisabledTextColor(lightGrayText);
    		tfRemotePort.setEnabled(false);
    		tfRemotePort.setBackground(grayBG);  
    		tfRemotePort.setDisabledTextColor(lightGrayText);
    		tfLocalPort.setEnabled(false); 	    		
    		tfLocalPort.setBackground(Color.WHITE); 
    		tfLocalPort.setDisabledTextColor(grayText);
    		
    		if (modem.getFirmware().getStatus() == ModemStatus.connected) {   
    			cbServerMode.setEnabled(false); 
    			btStartStop.setEnabled(true);  
    			btStartStop.setActionCommand(CMD_DISCONNECT);    
    			btStartStop.setText(messages.getString("jmodemkonfiguration_msg6")); // Disconnect
    		} else {
    			cbServerMode.setEnabled(true); 
    			btStartStop.setEnabled(false);  
    			btStartStop.setText(messages.getString("jmodemkonfiguration_msg7")); // Waiting
    		}    			
    		  		            
    	} else {   
    		// The Modem acts as a client      
    		if (modem.getFirmware().getStatus() == ModemStatus.connected) {
    			tfRemoteIpAddress.setEnabled(false);
    			tfRemoteIpAddress.setBackground(Color.WHITE); 
    			tfRemoteIpAddress.setDisabledTextColor(grayText);
        		tfRemotePort.setEnabled(false); 
        		tfRemotePort.setBackground(Color.WHITE); 
        		tfRemotePort.setDisabledTextColor(grayText);
        		tfLocalPort.setEnabled(false); 	
        		tfLocalPort.setBackground(grayBG);  
        		tfLocalPort.setDisabledTextColor(lightGrayText);
    			cbServerMode.setEnabled(false); 
    			btStartStop.setEnabled(true);  
    			btStartStop.setActionCommand(CMD_DISCONNECT);    
    			btStartStop.setText(messages.getString("jmodemkonfiguration_msg6")); // Disconnect
    		} else {
    			tfRemoteIpAddress.setEnabled(true);
    			tfRemoteIpAddress.setBackground(Color.WHITE); 
        		tfRemotePort.setEnabled(true);
        		tfRemotePort.setBackground(Color.WHITE); 
        		tfLocalPort.setEnabled(true); 	
        		tfLocalPort.setBackground(Color.WHITE); 
    			cbServerMode.setEnabled(true); 
    			btStartStop.setEnabled(true);  
    			btStartStop.setActionCommand(CMD_CONNECT);    
    			btStartStop.setText(messages.getString("jmodemkonfiguration_msg5")); // Connect
    		}
    	}
    }
    
    public boolean checkIPAddress(Component comp) {        	
    	
    	return gui.checkAndHighlight(EntryValidator.musterIpAdresseOderLocalhost, (JTextField)comp);    	
    }  
    
    public boolean checkPort(Component comp) {    	
    	
    	return gui.checkAndHighlight(EntryValidator.musterPort, (JTextField)comp);    	
    }  

    /** 
     * {@inheritDoc}
     */
    @Override
    public synchronized void updateDisplayedValues() {
        
        Modem modem = (Modem) getHardware();
        ModemFirmware firmware = modem.getFirmware();
        
        tfName.setText(modem.getDisplayName());
        tfLocalPort.setText("" + firmware.getLocalPort());  
        checkPort(tfLocalPort);

        tfRemoteIpAddress.setText(firmware.getRemoteIPAddress());
        checkIPAddress(tfRemoteIpAddress);
        tfRemotePort.setText("" + firmware.getRemotePort());  
        checkPort(tfRemotePort);
        
        cbServerMode.setSelected(firmware.getMode() == ModemMode.SERVER);
        if (cbServerMode.isSelected()) firmware.startServer();
        
        updateButtonDisplay();
        
        modified = false;
    }
    
    /** 
     * {@inheritDoc}
     */
    @Override
    public void saveChanges() {   	
    	
        if (modified == false) return;
        
        Modem modem = (Modem) getHardware();
        ModemFirmware firmware = modem.getFirmware();

        modem.setName(tfName.getText());
        if (checkPort(tfLocalPort))  firmware.setLocalPort(Integer.parseInt(tfLocalPort.getText()));
     
        
        if (checkIPAddress(tfRemoteIpAddress))  firmware.setRemoteIPAddress(tfRemoteIpAddress.getText());
        if (checkPort(tfRemotePort))  firmware.setRemotePort(Integer.parseInt(tfRemotePort.getText()));        
        
        GUIContainer.getInstance().updateViewport();
        
        ProjectManager.getInstance().setModified();  
        modified = false;
    }
}
