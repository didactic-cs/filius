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
import filius.software.system.InternetNodeOS;

@SuppressWarnings("serial")
public abstract class InternetNode extends Node {
    
    private List<NetworkInterface> networkInterfaces = new LinkedList<NetworkInterface>();
        

    /**
     * <b>createNICList</b> creates a list of network interfaces for the node.
     *      
     * @param count An integer with the number of network interfaces to be created.
     */
    public void createNICList(int count) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() +
                           " (InternetNode), createNICList(" + count + ")");

        networkInterfaces = new LinkedList<NetworkInterface>();
        if (this instanceof Host) {
        	// Hosts        	        
        	NetworkInterface ni = addNIC();
        	ni.setIp("192.168.0.10"); 
        } else {
        	// Routers
        	for (int i = 0; i < count; i++) {           
        		NetworkInterface ni = addNIC();
        		ni.setIp("192.168."+String.valueOf(i)+".1"); 
        	}
        }   
    }    
    
    public NetworkInterface addNIC() {
        NetworkInterface ni = new NetworkInterface();
        networkInterfaces.add(ni);
        ni.getPort().setOwner(this);
        return ni;
    }
    
    public void removeNIC(NetworkInterface ni) {
        networkInterfaces.remove(ni);
    }  
    
    public List<NetworkInterface> getNICList() {
        return networkInterfaces;
    }  

    public int getNICCount() {
        return networkInterfaces.size();
    }
    
    /**
     * <b>getNic</b> returns the network interface corresponding to the given index, 
     * or null if there is none.
     * 
     * @return A NetworkInterface instance or null.
     */
    public NetworkInterface getNIC(int index) {

    	if (index < 0 || index >= networkInterfaces.size()) return null;
    	
        ListIterator<NetworkInterface> it = this.networkInterfaces.listIterator();
        NetworkInterface ni = null;
        int i = index;
        while (it.hasNext() && i >= 0) {
            ni = (NetworkInterface) it.next();
            i--;
         }
        return ni;
    }
    
    /**
     * <b>getNic0</b> returns the first network interface corresponding or null if there is none.<br>
     * This convenient for Hosts which only have this one.
     * 
     * @return A NetworkInterface instance or null.
     */
    public NetworkInterface getNIC0(){
    	
    	if (networkInterfaces.size() == 0) return null;
    	else                               return networkInterfaces.get(0);
    }
    
    /**
     * <b>getNicByIp</b> returns the network interface corresponding to the given IP address, 
     * or null if there is none.
     * 
     * @author Thomas
     * @param ip String containing the IP address to look for.
     * @return A NetworkInterface instance or null.
     */
    public NetworkInterface getNICbyIP(String ip) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() +
                           " (InternetNode), getNicByIp(" + ip + ")");
        
        if (networkInterfaces.size() == 0) return null;
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
    public NetworkInterface getNICbyMAC(String mac) {
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
    public NetworkInterface getNICbyPort(Port port) {        
        
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
    
    public int getPortIndex(NetworkInterface ni) {
    	
    	int i = 0;
    	for (NetworkInterface nic : networkInterfaces) {
    		if (nic.equals(ni)) return i;
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
        
        ListIterator<NetworkInterface> iter = getNICList().listIterator();
        while (iter.hasNext()) {
            NetworkInterface nic = (NetworkInterface) iter.next();
            Port port = nic.getPort();
            if (!port.isConnected()) return port; 
        }
        return null;
    }   
    
    /**
     * 
     */
    public InternetNodeOS getOS() {
        return (InternetNodeOS) systemSoftware;
    }
    
    /**
     * Required for the serialization<br>
     * Use {@link #getNICList} instead
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
