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
import java.util.List;

import filius.Main;
import filius.hardware.Port;
import filius.hardware.Connection;
import filius.hardware.NetworkInterface;

public abstract class LocalNode extends Node {
	
	private static final long serialVersionUID = 1L;

    private LinkedList<Port> ports = new LinkedList<Port>();
    
    /**
     * <b>createPorts</b> creates a list of ports for the node.
     *      
     * @param count An integer with the number of Ports to be created.
     */
    public void createPortList(int count) {
        
    	ports = new LinkedList<Port>();
        for (int i = 0; i < count; i++) {
        	Port port = new Port();
        	ports.add(port);
        	port.setOwner(this);
        }
    }    
    
    /**
     * {@inheritDoc}
     */
    public LinkedList<Port> getPortList() {
        return ports;
    }
    
    /**
     * {@inheritDoc}
     */
    public int getPortCount() {
    	
        return ports.size();
    }  
    
    /**
     * {@inheritDoc}
     */
    public int getPortIndex(Port port) {
    	
        int i = 0;
        for (Port p : ports) {
            if (p.equals(port)) return i;
            i++;
        }
        return -1;
    }

    /**
    * {@inheritDoc}
    */
    public Port getFreePort() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (LocalNode), getFreePort()");
        
        for (Port port : ports) {
            if (!port.isConnected()) return port;
        }
        return null;
    }    
    
    /**
     * Required for the serialization<br>
     * Use getPortList instead
     */
    public LinkedList<Port> getPorts() {
        return ports;
    }

    /**
     * Required for the serialization<br>
     * Do not call directly.
     */
    public void setPorts(LinkedList<Port> list) {
        ports = list;
    }
}
