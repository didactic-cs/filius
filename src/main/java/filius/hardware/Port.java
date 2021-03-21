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
package filius.hardware;

import java.io.Serializable;
import java.util.LinkedList;

import filius.Main;
import filius.hardware.knoten.Node;
import filius.software.netzzugangsschicht.EthernetFrame;

@SuppressWarnings("serial")
public class Port implements Serializable {
    
    private LinkedList<EthernetFrame> inputBuffer = new LinkedList<EthernetFrame>();
    private LinkedList<EthernetFrame> outputBuffer = new LinkedList<EthernetFrame>();
    private Cable cable = null;
    private NetworkInterface nic = null;
    private Node owner = null;
    
    // portStatus is used with switches for the spanning tree
    public static enum PortStatus {empty, used, blocked};
    private PortStatus portStatus = PortStatus.empty;       

    
    // constructor without parameters (for switches)
    public Port() {}
    
    // constructor with parameter for all other nodes with explicit NIC for each port
    public Port(NetworkInterface nic) {
    	
        this.nic = nic;
    }
    
    public boolean isConnected() {   
    	
        return (cable != null);
    }
    
    public void setEmpty() {
    	
    	portStatus = PortStatus.empty;
    } 
    
    public void setUsed() {
    	
    	portStatus = PortStatus.used;
    } 
    
    public void setBlocked() {
    	
    	portStatus = PortStatus.blocked;
    }   
    
    public boolean isBlocked() {   
    	
        return (portStatus == PortStatus.blocked);
    }    
    
    public boolean isUsed() {   
    	
        return (portStatus == PortStatus.used);
    }

    public NetworkInterface getNIC() {
    	
        return nic;
    }    
    
    // Node owning the port
    public Node getOwner() {
    	
        return owner;
    } 
    
    public void setOwner(Node owner) {
    	
        this.owner = owner;
    } 
    
    // Index of node in the list of owner node (if any)
    public int getIndex() {
    	
    	if (owner == null) return -1;
        return owner.getPortIndex(this);
    } 
    
    /**
     * <b>getRemotePort</b> returns the port connected to the current port. 
     * Returns null if the current port is not connected. 
     * 
     * @return The port to which the current port is connected, or null if there is none.
     */
    public Port getRemotePort() {
    	
    	if (cable == null) return null;
    	return cable.findPortConnectedTo(this);    	
    } 
    
    public Cable getCable() {
    	
        return cable;
    }

    public boolean setCable(Cable cable) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + 
        		           " (Port), setCable(" + cable + ")");
        
        if (! isConnected()) {
            this.cable = cable;
            return true;
        } else {
            return false;
        }
    }

    public void removeCable() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + 
        		           " (Port), removeCable()");
        
        this.cable = null;
    }

    public LinkedList<EthernetFrame> getOutputBuffer() {
    	
        return outputBuffer;
    }

    public LinkedList<EthernetFrame> getInputBuffer() {
    	
        return inputBuffer;
    }

    public void setInputBuffer(LinkedList<EthernetFrame> buffer) {
    	
        this.inputBuffer = buffer;
    }
}
