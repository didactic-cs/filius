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

import java.util.ArrayList;
import java.util.LinkedList;

import filius.Main;
import filius.hardware.Port;
import filius.software.system.ModemFirmware;

@SuppressWarnings("serial")
public class Modem extends LocalNode {

	public static final String TYPE = "Modem";
	
	// connectionActive becomes true when a connection with another 
	// modem is established. 
	private boolean connectionActive = false;
	
	// List of event listeners that need to be notified when connectionActive changes
	private ArrayList<ModemConnectedListener> listenerList = new ArrayList<ModemConnectedListener>();

	public Modem() {
		super();

		this.createPortList(1);
		this.setSystemSoftware(new ModemFirmware());
		getSystemSoftware().setKnoten(this);
		this.setName(TYPE);
	}
	
    /**
     * <b>getPort</b> returns the port of the modem.
     *      
     * @return A Port instance or null.
     */
    public Port getPort() {
    	
    	LinkedList<Port> ports = getPortList();
    	
        if (ports == null || ports.size() == 0) return null;
        
        return (Port) ports.getFirst();        
    }
	
	public boolean isConnectionActive() {
		return connectionActive;
	}

	public void setConnectionActive(boolean active) {
		
		this.connectionActive = active;
		// Notify the listeners (GUI of the modem node)  
        for (ModemConnectedListener l : listenerList) l.onModemConnectedChange(active);
	}
	
	//***************************************************************************************
	// Handling event listeners
	//***************************************************************************************
	
    /**
     * <b>addListener<b> adds a listener to the list.<br>
     * Each listener is notified every time the connection status changes.
     * 
     * @param l the listener to be added
     */
    public void addListener(ModemConnectedListener l) {
    	
        if (!listenerList.contains(l)) listenerList.add(l);
    }
    
	@Override
	public String getHardwareType() {
		return TYPE;
	}
}
