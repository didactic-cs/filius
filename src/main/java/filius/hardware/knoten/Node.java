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
import filius.Main;
import filius.hardware.Hardware;
import filius.hardware.Port;
import filius.software.system.SystemSoftware;

@SuppressWarnings("serial")
public abstract class Node extends Hardware {

    private String name;
    private SystemSoftware systemSoftware;    
    
    /**
     * <b>getPortList</b> returns the list of all the ports of the node, connected or not.
     *      
     * @return The list of all ports of the node.
     */
    public abstract LinkedList<Port> getPortList();

    /**
     * <b>getPortCount</b> returns the number of ports owned by the node.
     *      
     * @return The number of ports in the list.
     */
    public abstract int getPortCount();

    /**
     * <b>getPortIndex</b> returns the index in the list of the given port.
     * 
     * @param port Port the index of which is to be determined.      
     *      
     * @return The index of the port in the list, or -1 if the port does not belong to the list.
     */ 
    public abstract int getPortIndex(Port port);    

    /**
     * <b>getFreePort</b> returns a port belonging to the node that is not yet connected.
     * Returns null if all ports are connected.
     * 
     * @return A Port that is not connected or null.
     */
    public abstract Port getFreePort();    
    
    /**
     * <b>ownsPort</b> checks whether the given port is one of the node's ports.
     * 
     * @param port Port to look up
     * 
     * @return true if the given port belongs to the node.
     */
    public boolean ownsPort(Port port) {
    	if (port == null) return false;
    	return (getPortIndex(port) > -1);
    };
    
//    /**
//     * <b>fixPortsOwner</b> is only used when loading projects saved in older XML formats.<br>
//     * Should not be called otherwise.
//     */
//    public void fixPortsOwner() {
//
//    	LinkedList<Port> ports = getPortList();
//    	for (Port port : ports) port.setOwner(this);
//    }
    
    /**
     * <b>isConnectedTo</b> checks whether the given node is connected to the current node.
     * 
     * @param remoteNode Node for which the connection must be checked.
     * 
     * @return true if the given node is connected to the node.
     */
    public boolean isConnectedTo(Node remoteNode) {

    	LinkedList<Port> ports = getPortList();
    	for (Port port : ports) {
    		if (remoteNode.ownsPort(port.getRemotePort())) return true;
    	}
    	return false;    	
    }

    public String getDisplayName() {
    	return name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        
        if (systemSoftware != null) {
            Main.debug.println(
                    "DEBUG: node with SystemSoftware (" + systemSoftware.hashCode() + ") now has name '" + name + "'");
        }
    }

    public SystemSoftware getSystemSoftware() {
        return systemSoftware;
    }

    public void setSystemSoftware(SystemSoftware systemSoftware) {
        this.systemSoftware = systemSoftware;
    }
}
