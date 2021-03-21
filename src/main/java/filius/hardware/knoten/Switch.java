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
package filius.hardware.knoten;

import java.util.LinkedList;

import javax.swing.ImageIcon;

import filius.Main;
import filius.gui.netzwerksicht.GUIDesignSidebar;
import filius.hardware.Cable;
import filius.hardware.Port;
import filius.rahmenprogramm.I18n;
import filius.software.system.SwitchFirmware;

@SuppressWarnings("serial")
public class Switch extends LocalNode implements I18n {

	public static final String TYPE = messages.getString("hw_switch_msg1");
	private boolean showAsCloud = false;
	private boolean connectedToRoot = false; // Used for the spanning tree

	
	public Switch() {	
		Main.debug.println("INVOKED-2 (" + this.hashCode() + ") " + getClass() + " (Switch), constr: Switch()");

		createPortList(24);
		setSystemSoftware(new SwitchFirmware());
		getSystemSoftware().setNode(this);
		setName(TYPE);
	}

	public void setCloud(boolean value) {
		
		showAsCloud = value;
		
		if (showAsCloud) {
			getNodeItem().getNodeLabel().setIcon(new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.SWITCH_CLOUD)));	
		} else {
			getNodeItem().getNodeLabel().setIcon(new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.SWITCH)));	
		}
	}

	public boolean isCloud() {
		return showAsCloud;
	}	
    
    /**
     * <b>resetSpanningTree</b> sets all ports to unblocked.<br>
     * Called when the simulation mode starts, before applying the spanning tree to all the switches.
     * 
     */
    public void resetSpanningTree() {

    	connectedToRoot = false;
    	
    	LinkedList<Port> ports = getPortList();
    	for (Port port : ports) {
    		port.setEmpty();
    		Cable cable = port.getCable();
    	    if (cable != null) cable.setBlocked(false); 
    	}
	}  
    
    /**
     * <b>isConnectedToRoot</b> returns true when one port is set to "used"
     * which means that a link to the root exists
     * 
     */
    public boolean isConnectedToRoot() {

    	return connectedToRoot;
	}   
    
    /**
     * <b>setConnectedToRoot</b> sets connectedToRoot to true
     * 
     */
    public void setConnectedToRoot() {

    	connectedToRoot = true;
	}         
    
    /**
     * <b>blockRedundantPorts</b> set the spanningTreeApplied flag to true.
     * 
     */
    public void blockRedundantPorts() {

    	connectedToRoot = true;  // <- Only useful for the root itself
    	
    	for (Port port : getPortList()) {
    		
    		// Ignore if already blocked
    		if (port.isBlocked() || port.isUsed()) continue;
    		
    		// If the port is connected to a switch on which the spanningTree 
    		// was already applied, block the local port, the remote one connected 
    		// to it, and also the cable inbetween
    		Port remotePort = port.getRemotePort();
    		if (remotePort != null) {
    			Node remoteNode = remotePort.getOwner();
    			if (remoteNode instanceof Switch)	{
    				Switch remoteSwitch = (Switch)remoteNode;
    				if (remoteSwitch.isConnectedToRoot()) {
    					// The switch is connected to another switch which 
    					// is already connected to the root.
    					// There is no need for another connection, so we 
    					// block the local port, the remote one connected 
    		    		// to it, and also the cable inbetween
    					remotePort.setBlocked();
    					port.setBlocked();
    					port.getCable().setBlocked(true);
    				}
    				else {
    					// The switch is connected to another switch which 
    					// is not connected to the root yet.
    					// The connection is kept and the remote switch
    					// is tagged as connected to the root (through this switch)
    					port.setUsed();
    					remotePort.setUsed();
    					((Switch)remotePort.getOwner()).setConnectedToRoot();
    				}
    			} 
    			else {
    				// Port is connected, but not to a switch
    				port.setUsed();
    			}    				
    		}
    		else {
    			// Port is not connected to anything (for optimization)
    			port.setBlocked();
    		}
    	}    	
	}
    
    /**
     * <b>getConnectedSwitchList</b> returns the list of the switches directly
     * connected to this switch
     * 
     */
    public LinkedList<Switch> getConnectedSwitchList() {

    	LinkedList<Switch> sl = new LinkedList<Switch>(); 
    	LinkedList<Port> ports = getPortList();
    	for (Port port : ports) {
    		Port rp = port.getRemotePort();
    		if (rp != null) {
    			Node node = rp.getOwner();
    			if (node instanceof Switch)	sl.add((Switch)node);     
    		}
    	}
    	return sl;
	}        
	
	@Override
	public String getHardwareType() {
		return TYPE;
	}
}
