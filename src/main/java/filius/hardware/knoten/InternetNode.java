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
import java.util.List;
import java.util.ListIterator;

import filius.Main;
import filius.hardware.NetworkInterface;
import filius.hardware.Port;

@SuppressWarnings("serial")
public abstract class InternetNode extends Node {
    
    private List<NetworkInterface> networkInterfaces = new LinkedList<NetworkInterface>();
        

    /**
     * <b>createNics</b> creates a list of network interfaces for the node.
     *      
     * @param count An integer with the number of network interfaces to be created.
     */
    public void createNIlist(int count) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() +
                           " (InternetNode), setNicCount(" + count + ")");

        networkInterfaces = new LinkedList<NetworkInterface>();
        for (int i = 0; i < count; i++) {
        	NetworkInterface ni = new NetworkInterface();
            networkInterfaces.add(ni);
            ni.getPort().setOwner(this);
        }
    }    
    
    public void addNI() {
        NetworkInterface ni = new NetworkInterface();
        networkInterfaces.add(ni);
        ni.getPort().setOwner(this);
    }
    
    public void removeNI(NetworkInterface nic) {
        this.networkInterfaces.remove(nic);
    }  
    
    public List<NetworkInterface> getNIlist() {
        return networkInterfaces;
    }  

    public int getNIcount() {
        return networkInterfaces.size();
    }
    
    /**
     * <b>getNicByIp</b> returns the network interface corresponding to the given IP address, 
     * or null if there is none.
     * 
     * @author Thomas
     * @param ip String containing the IP address to look for.
     * @return A NetworkInterface instance or null.
     */
    public NetworkInterface getNIbyIP(String ip) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() +
                           " (InternetNode), getNicByIp(" + ip + ")");
        
        if (ip.equals("127.0.0.1")) return (NetworkInterface) networkInterfaces.get(0);
        
        ListIterator<NetworkInterface> it = this.networkInterfaces.listIterator();
        while (it.hasNext()) {
            NetworkInterface ni = (NetworkInterface) it.next();
            if (ni.getIp().equals(ip)) return ni; 
         }
        return null;
    }
    
    /**
     * <b>getNicByMac</b> returns the network interface corresponding to the given MAC address, 
     * or null if there is none.
     * 
     * @author Johannes Bade
     * @param mac String containing the MAC address to look for.
     * @return A NetworkInterface instance or null.
     */
    public NetworkInterface getNIbyMAC(String mac) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + 
        		           " (InternetNode), getNicByMac(" + mac + ")");
        
        ListIterator<NetworkInterface> it = networkInterfaces.listIterator();
        while (it.hasNext()) {
            NetworkInterface ni = (NetworkInterface) it.next();
            if (ni.getMac().equals(mac)) return ni;         
        }
        return null;
    }
    
    /**
     * <b>getNicByPort</b> returns the network interface corresponding to the given port, 
     * or null if there is none.
     * 
     * @param port Port containing the port to look for.
     * @return A NetworkInterface instance or null.
     */
    public NetworkInterface getNIbyPort(Port port) {        
        
        ListIterator<NetworkInterface> it = this.networkInterfaces.listIterator();
        while (it.hasNext()) {
            NetworkInterface ni = (NetworkInterface) it.next();
            if (ni.getPort().equals(port)) return ni; 
         }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    public LinkedList<Port> getPortList() {

    	LinkedList<Port> ports = new LinkedList<Port>();	   
    	for (NetworkInterface nic : networkInterfaces) {
    		ports.add(nic.getPort());
    	}	   
    	return ports;
    }
    
    /**
     * {@inheritDoc}
     */
    public int getPortCount() {
    	
        return networkInterfaces.size();
    }  
    
    /**
     * {@inheritDoc}
     */
    public int getPortIndex(Port port) {
    	
    	int i = 0;
    	for (NetworkInterface nic : networkInterfaces) {
    		if (nic.getPort().equals(port)) return i;
    		i++;
    	}
    	return -1;
    }

    /**
     * {@inheritDoc}
     */
    public Port getFreePort() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + 
        		           " (InternetNode), getFreePort()");
        
        ListIterator<NetworkInterface> iter = getNIlist().listIterator();
        while (iter.hasNext()) {
            NetworkInterface nic = (NetworkInterface) iter.next();
            Port port = nic.getPort();
            if (!port.isConnected()) return port; 
        }
        return null;
    }       
    
    /**
     * Required for the serialization<br>
     * Use getNIlist instead
     */
    public List<NetworkInterface> getNetworkInterfaces() {
        return networkInterfaces;
    }
    
    /**
     * Required for the serialization<br>
     * Do not call directly.
     */
    public void setNetworkInterfaces(List<NetworkInterface> list) {
        networkInterfaces = list;
    }
}
