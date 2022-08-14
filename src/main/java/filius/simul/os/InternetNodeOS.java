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
package filius.software.system;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;

import filius.Main;
import filius.hardware.NetworkInterface;
import filius.hardware.knoten.Computer;
import filius.hardware.knoten.InternetNode;
import filius.hardware.knoten.Notebook;
import filius.hardware.knoten.Router;
import filius.rahmenprogramm.EntryValidator;
import filius.rahmenprogramm.FiliusClassLoader;
import filius.software.Application;
import filius.software.dns.Resolver;
import filius.software.netzzugangsschicht.Ethernet;
import filius.software.netzzugangsschicht.EthernetThread;
import filius.software.rip.RIPTable;
import filius.software.transportschicht.TCP;
import filius.software.transportschicht.UDP;
import filius.software.vermittlungsschicht.ARP;
import filius.software.vermittlungsschicht.ICMP;
import filius.software.vermittlungsschicht.IP;
import filius.software.vermittlungsschicht.Route;
import filius.software.vermittlungsschicht.RouteNotFoundException;
import filius.software.vermittlungsschicht.RoutingTable;

/**
 * Diese Klasse implementiert die Funktionalitaet eines Betriebssystems für Internetknoten. Dass heisst, das
 * Betriebssystem unterstuetzt den gesamten Protokollstapel, der fuer den Betrieb von Internetanwendungen benoetigt
 * wird. <br />
 * Ausserdem stellt diese Klasse eine Schnittstelle fuer den Zugriff auf
 * <ol>
 * <li>die erste Netzwerkkarte,</li>
 * <li>den DNS-Client (Resolver)</li>
 * </ol>
 * zur Verfuegung. (als Entwurfsmuster Fassade)
 */
@SuppressWarnings("serial")
public abstract class InternetNodeOS extends SystemSoftware {

    /** The filesystem of the OS */
    private FiliusFileSystem filesystem;

    /**
     * HashMap of the OS installed applications. Each key is an application class name and 
     * the corresponding value is an Application.
     */
    private HashMap<String, Application> installedApps;

    /**
     * Mit Hilfe des DNS-Client werden Rechneradressen, die als Domainname uebergeben werden aufgeloest. Ausserdem wird
     * er benutzt, um jegliche Anfragen an den DNS-Server zu stellen.
     */
    private Resolver dnsclient;

    /** Die Transportschicht wird durch TCP und UDP implementiert. */
    private TCP tcp;

    /** Die Transportschicht wird durch TCP und UDP implementiert. */
    private UDP udp;

    /**
     * Die Vermittlungsschicht wird durch das Address Resolution Protocol (ARP) und das Internetprotokoll implementiert.
     * Dafür stehen die Klassen ARP und Vermittlung.
     */
    private ARP arpVermittlung;

    /**
     * Die Vermittlungsschicht wird durch das Address Resolution Protocol (ARP) und das Internetprotokoll implementiert.
     * Dafür stehen die Klassen ARP und Vermittlung.
     */
    private IP vermittlung;
    private ICMP icmpVermittlung;

    /**
     * Die Weiterleitungstabelle enthaelt neben Standardeintraegen ggfs. auch durch den Anwender hinzugefuegte
     * Eintraege. Diese zusaetzliche Funktionalitaet wird derzeit nur durch den Vermittlungsrechner genutzt. Generell
     * wird die Entscheidung, ueber welche Netzwerkkarte Daten versendet werden, auf Grundlage der Weiterleitungstabelle
     * getroffen. Sie kann nicht der Vermittlungsschicht zugeordnet werden, weil sie mit einem Projekt persistent
     * gespeichert werden muss.
     */
    private RoutingTable routingTable;

    /**
     * Die Netzzugangsschicht wird durch das Ethernet-Protokoll implementiert. Die zugehoerigen Threads werden vom
     * Betriebssystem gestartet und beendet.
     */
    private Ethernet ethernet;

    
    /**
     * Constructor of the operating system of an Internet node.
     * <ul>
     * <li>the layers are initialized,</li>
     * <li>the installed applications are reset,</li>
     * <li>the filesysem is initialized,</li>
     * <li>the DNS client is created.</li>
     * </ul>
     */
    public InternetNodeOS() {
        super();
        Main.debug.println("INVOKED-2 (" + this.hashCode() + ") " + getClass() + " (InternetNodeOS), constr: InternetNodeOS()");

        installedApps = new HashMap<String, Application>();

        routingTable = new RoutingTable();
        routingTable.setInternetNodeOS(this);

        arpVermittlung = new ARP(this);
        vermittlung = new IP(this);
        icmpVermittlung = new ICMP(this);
        ethernet = new Ethernet(this);

        tcp = new TCP(this);
        udp = new UDP(this);

        filesystem = new FiliusFileSystem();

        dnsclient = new Resolver();
        dnsclient.setSystemSoftware(this);

        // print IDs for all network layers and the according node --> for
        // providing debug support in log file
        Main.debug.println("DEBUG: InternetNodeOS (" + this.hashCode() + ")\n" + "\tEthernet: "
                + ethernet.hashCode() + "\n" + "\tARP: " + arpVermittlung.hashCode() + "\n" + "\tIP: "
                + vermittlung.hashCode() + "\n" + "\tICMP: " + icmpVermittlung.hashCode() + "\n" + "\tTCP: "
                + tcp.hashCode() + "\n" + "\tUDP: " + udp.hashCode());
    }    
    
    public InternetNode getNode() {
        return (InternetNode) super.getNode();
    }  

    /**
     * Methode zum starten der Protokoll-Threads und der Anwendungen.
     * 
     * @see filius.software.system.SystemSoftware.starten()
     */
    @Override
    public synchronized void start() {
        super.start();
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (InternetNodeOS), start()");

        filesystem.getRoot().fixDirectory();

        // Die Protokoll-Threads der einzelnen Schichten werden
        // beginnend mit der untersten Schicht gestartet.
        ethernet.start();
        arpVermittlung.start();
        vermittlung.start();
        icmpVermittlung.start();
        tcp.start();
        udp.start();

        printDebugInfo();

        for (Application app : installedApps.values()) {
            if (app != null)  app.startThread();            
        }
    }

    /**
     * Zum beenden der Protokoll-Threads und der Anwendungs-Threads.
     * 
     * @see filius.software.system.SystemSoftware.beenden()
     */
    public void stop() {
        super.stop();
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (InternetNodeOS), stop()");

        // Die einzelnen Protokoll-Threads werden beginnend
        // mit der untersten Schicht beendet.

        // Netzzugangsschicht
        ethernet.stop();

        // Vermittlungsschicht
        arpVermittlung.stop();
        vermittlung.stop();
        icmpVermittlung.stop();

        // Transportschicht
        tcp.stop();
        udp.stop();

        dnsclient.stopThread();

        for (Application app : installedApps.values()) {
            app.stopThread();
        }
    }

    private void printDebugInfo() {
        Main.debug.println("DEBUG (" + this.hashCode() + "): start InternetNodeOS");
        if (getNode() != null) {
            Main.debug.println("DEBUG (" + this.hashCode() + ") - Hostname = " + getNode().getDisplayName());
            Main.debug.print("DEBUG (" + this.hashCode() + ") - Hardwaretyp = '");
            if (getNode() instanceof filius.hardware.knoten.Notebook) {
                Main.debug.println("Notebook'");
            } else if (getNode() instanceof filius.hardware.knoten.Computer) {
                Main.debug.println("Rechner'");
            } else if (getNode() instanceof filius.hardware.knoten.Router) {
                Main.debug.println("Vermittlungsrechner'");
            } else {
                Main.debug.println("<unknown>'");
            }
        } else {
            Main.debug.println("DEBUG (" + this.hashCode() + ") - Hostname = <unknown>");
            Main.debug.println("DEBUG (" + this.hashCode() + ") - Hardwaretyp = <unknown>");
        }
        Main.debug.println("DEBUG (" + this.hashCode() + ") - ETHER = " + ethernet.hashCode());
        List<EthernetThread> threads = ethernet.getEthernetThreads();
        if (threads != null)
            for (int i = 0; i < threads.size(); i++)
                Main.debug.println(
                        "DEBUG (" + this.hashCode() + ")      - ETHER T-" + i + " = " + threads.get(i).hashCode());
        Main.debug.println("DEBUG (" + this.hashCode() + ") - ARP = " + arpVermittlung.hashCode());
        filius.software.vermittlungsschicht.ARPThread thread = arpVermittlung.getARPThread();
        if (thread != null)
            Main.debug.println("DEBUG (" + this.hashCode() + ")      - ARP T = " + thread.hashCode());
        Main.debug.println("DEBUG (" + this.hashCode() + ") - IP = " + vermittlung.hashCode());
        filius.software.vermittlungsschicht.IPThread IPthread = vermittlung.getIPThread();
        if (IPthread != null)
            Main.debug.println("DEBUG (" + this.hashCode() + ")      - IP T = " + IPthread.hashCode());
        Main.debug.println("DEBUG (" + this.hashCode() + ") - ICMP = " + icmpVermittlung.hashCode());
        filius.software.vermittlungsschicht.ICMPThread ICMPthread = icmpVermittlung.getICMPThread();
        if (IPthread != null)
            Main.debug.println("DEBUG (" + this.hashCode() + ")      - ICMP T = " + ICMPthread.hashCode());
        Main.debug.println("DEBUG (" + this.hashCode() + ") - TCP = " + tcp.hashCode());
        Main.debug.println("DEBUG (" + this.hashCode() + ") - UDP = " + udp.hashCode());
        if (getNode() != null) {
            if (getNode() instanceof Notebook) {
                NetworkInterface nic = ((NetworkInterface) ((Notebook) getNode()).getNICList().get(0));
                Main.debug.println(
                        "DEBUG (" + this.hashCode() + ") - NIC: {IP=" + nic.getIp() + "/" + nic.getSubnetMask()
                                + ", MAC=" + nic.getMac() + ", DNS=" + nic.getDns() + ", GW=" + nic.getGateway() + "}");
            } else if (getNode() instanceof Computer) {
                NetworkInterface nic = ((NetworkInterface) ((Computer) getNode()).getNICList().get(0));
                Main.debug.println(
                        "DEBUG (" + this.hashCode() + ") - NIC: {IP=" + nic.getIp() + "/" + nic.getSubnetMask()
                                + ", MAC=" + nic.getMac() + ", DNS=" + nic.getDns() + ", GW=" + nic.getGateway() + "}");
            } else if (getNode() instanceof Router) {
                int nicNr = 0;
                for (NetworkInterface nic : ((Router) getNode()).getNICList()) {
                    Main.debug.println("DEBUG (" + this.hashCode() + ") - NIC" + nicNr + ": {IP=" + nic.getIp() + "/"
                            + nic.getSubnetMask() + ", MAC=" + nic.getMac() + ", DNS=" + nic.getDns() + ", GW="
                            + nic.getGateway() + "}");
                    nicNr++;
                }
            }
        } else {
            Main.debug.println("DEBUG (" + this.hashCode() + ") - NIC=<unknown>");
        }
        getRoutingTable().printTable(Integer.toString(this.hashCode()));
    }

    /** Methode fuer den Zugriff auf den DNS-Resolver */
    public Resolver getDNSClient() {
        return dnsclient;
    }

    /**
     * Methode fuer den Zugriff auf das Transport Control Protocol (TCP).
     */
    public TCP getTcp() {
        return tcp;
    }

    /**
     * Methode fuer den Zugriff auf das User Datagram Protocol (UDP).
     */
    public UDP getUdp() {
        return udp;
    }

    /**
     * Methode fuer den Zugriff auf das Address Resolution Protocol (ARP).
     */
    public ARP getARP() {
        return arpVermittlung;
    }

    /**
     * Methode fuer den Zugriff auf das Internet Control Message Protocol (ICMP).
     */
    public ICMP getICMP() {
        return icmpVermittlung;
    }

    /**
     * Methode fuer den Zugriff auf das Internet Protocol (IP).
     */
    public IP getIP() {
        return vermittlung;
    }

    /** Methode fuer den Zugriff auf das Ethernet-Protokoll */
    public Ethernet getEthernet() {
        return ethernet;
    }
    
    /**
     * Methode fuer den JavaBean-konformen Zugriff auf die Weiterleitungstabelle. Diese Methode wird aber ausserdem von
     * der Implementierung der Vermittlungsschicht verwendet.
     */
    public RoutingTable getRoutingTable() {
        return routingTable;
    }

    /**
     * Methode fuer den JavaBean-konformen Zugriff auf die Weiterleitungstabelle.
     */
    public void setRoutingTable(RoutingTable tabelle) {
        this.routingTable = tabelle;
    }    

    public Route determineRoute(String ipAddress) throws RouteNotFoundException {
        return routingTable.getBestRoute(ipAddress);
    }  
    
    /**
     * Methode fuer den Zugriff auf die MAC-Adresse der einzigen Netwerkkarte. Das ist eine Methode des Entwurfsmusters
     * Fassade
     */
    public String getMACAddress() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (InternetNodeOS), getMACAddress()");
 
        NetworkInterface nic = getNode().getNIC0();

        if (nic != null) return nic.getMac();
        else             return null;
    }

    /**
     * Methode fuer den Zugriff auf die IP-Adresse der einzigen Netwerkkarte. Das ist eine Methode des Entwurfsmusters
     * Fassade
     */
    public String getIPAddress() {
  
        String ip = null;
        
        ListIterator<NetworkInterface> it = getNode().getNICList().listIterator();
        while (it.hasNext()) {      
        	
            ip = it.next().getIp();

            // search for a public IP
            if (!(ip.startsWith("10.") || ip.startsWith("192.168.") || ip.startsWith("0.") || ip.startsWith("127."))) {
                break;
            }
        }

        return ip;      
    }
    
    /**
     * Methode zum Einstellen der IP-Adresse fuer die einzige Netwerkkarte. Das ist eine Methode des Entwurfsmusters
     * Fassade
     */
    public void setIPAddress(String ip) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (InternetNodeOS), setIPAddress(" + ip + ")");
               
        InternetNode node = getNode();
        
        ip = IP.ipCheck(ip);
        if (ip != null && EntryValidator.isValid(ip, EntryValidator.musterIpAdresse)) {
        	if (!node.getNIC0().getIp().equals(ip)) {
        		
        		node.getNIC0().setIp(ip);
        		
        		// notify the JNodeLabel and GUIDesktopWindow
        		fireIPChange(node.getDisplayName());
        	}            
        }
    }

    /**
     * Methode fuer den Zugriff auf die Netzmaske der einzigen Netzwerkkarte. Das ist eine Methode des Entwurfsmusters
     * Fassade
     */
    public String getSubnetMask() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (InternetNodeOS), getSubnetMask()");

        NetworkInterface nic = getNode().getNIC0();

        if (nic != null) return nic.getSubnetMask();
        else             return null;
    }
    
    /**
     * Methode fuer den Zugriff auf die Netzmaske der einzigen Netzwerkkarte. Das ist eine Methode des Entwurfsmusters
     * Fassade
     */
    public void setSubnetMask(String mask) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (InternetNodeOS), setSubnetMask(" + mask + ")");
        
        InternetNode node = getNode();
        
        mask = IP.ipCheck(mask);
        if (mask != null && EntryValidator.isValid(mask, EntryValidator.musterSubNetz)) {
        	if (!node.getNIC0().getSubnetMask().equals(mask)) {
        		node.getNIC0().setSubnetMask(mask);
                // Main.debug.println("\t" + ((NetzwerkInterface) knoten.getNetzwerkInterfaces().getFirst()).getSubnetzMaske());
        	} 
        }
    }
    
    /**
     * Methode fuer den Zugriff auf die IP-Adresse des Standard-Gateways, aller Netzwerkkarten.
     * 
     * @return IP-Adresse der einzigen Netzwerkkarte als String
     */
    public String getStandardGateway() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (InternetNodeOS), getStandardGateway()");
        
        NetworkInterface nic = getNode().getNIC0();

        if (nic != null) return nic.getGateway();
        else             return null;
    }

    /**
     * Methode zum Einstellen des Standard-Gateways fuer die Netwerkkarten. Das ist eine Methode des Entwurfsmusters
     * Fassade.
     * 
     * @param gateway
     *            IP-Adresse der Netzwerkkarten als String
     */
    @SuppressWarnings("deprecation")
	public void setStandardGateway(String gateway) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (InternetNodeOS), setStandardGateway(" + gateway + ")");
      
        gateway = (gateway != null && gateway.trim().equals("")) ? gateway.trim() : IP.ipCheck(gateway);

        if (gateway != null && EntryValidator.isValid(gateway, EntryValidator.musterIpAdresseAuchLeer)) {
         
        	Iterator<NetworkInterface> it = getNode().getNICList().listIterator();        	
        	while (it.hasNext())  it.next().setGateway(gateway);        	
        }
    }
    
    /**
     * Methode fuer den Zugriff auf die IP-Adresse des DNS-Servers der aller Netzwerkkarten. Das ist eine Methode des
     * Entwurfsmusters Fassade
     */
    public String getDNSServer() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (InternetNodeOS), getDNSServer()");
        
        NetworkInterface nic = getNode().getNIC0();

        if (nic != null) return nic.getDns();
        else             return null;
    }

    /**
     * Methode fuer den Zugriff auf die IP-Adresse des DNS-Servers der aller Netzwerkkarten. Das ist eine Methode des
     * Entwurfsmusters Fassade
     */
    @SuppressWarnings("deprecation")
	public void setDNSServer(String dns) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (InternetNodeOS), setDNSServer(" + dns + ")");
            
        dns = (dns != null && dns.trim().equals("")) ? dns.trim() : IP.ipCheck(dns);

        if (dns != null && EntryValidator.isValid(dns, EntryValidator.musterIpAdresseAuchLeer)) {

        	Iterator<NetworkInterface> it = getNode().getNICList().listIterator();
        	while (it.hasNext())  it.next().setDns(dns);
        	
        	// notify the JNodeLabel
        	fireDNSChange(getNode().getDisplayName());
        }
    }

    /**
     * Methode fuer den Zugriff auf das Dateisystem Diese Methode wird fuer das Speichern des Dateisystems in einer
     * Projektdatei benoetigt. (JavaBean- konformer Zugriff erforderlich)
     */
    public FiliusFileSystem getFileSystem() {
        return filesystem;
    }

    /**
     * Methode, um das Dateisystem zu setzen. Diese Methode wird fuer das Speichern des Dateisystems in einer
     * Projektdatei benoetigt. (JavaBean- konformer Zugriff erforderlich)
     * 
     * @param filesystem
     */
    public void setFileSystem(FiliusFileSystem filesystem) {
        this.filesystem = filesystem;
    }

    public abstract RIPTable getRIPTable();

    public abstract boolean isRIPEnabled();

    /**
     * Methode fuer den Zugriff auf die Hash-Map zur Verwaltung der installierten Anwendungen. Diese Methode wird
     * benoetigt, um den Anforderungen an JavaBeans gerecht zu werden.
     */
    public HashMap<String, Application> getInstalledApps() {
        return installedApps;
    }
    
    /**
     * Methode fuer den Zugriff auf die Hash-Map zur Verwaltung der installierten Anwendungen. Diese Methode wird
     * benoetigt, um den Anforderungen an JavaBeans gerecht zu werden.
     */
    public void setInstalledApps(HashMap<String, Application> installedApps) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (InternetNodeOS), setInstalledApps()");
        
        this.installedApps = installedApps;
        // printInstalledApps();
    }
    
    /**
     * Methode zur Abfrage aller aktuell installierter Anwendungen
     * 
     * @return ein Array der Anwendungsnamen
     */
    public Application[] getInstalledAppsArray() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (InternetNodeOS), getInstalledSoftwares()");
        
        Iterator<Entry<String, Application>>  it = installedApps.entrySet().iterator();

        Application[] applications = new Application[installedApps.size()];
        for (int i = 0; it.hasNext() && i < applications.length; i++) {
            applications[i] = it.next().getValue();
        }

        // printInstalledApps();

        return applications;
    }  

    /**
     * Methode fuer den Zugriff auf eine bereits installierte Anwendung.
     * 
     * @param appClassName
     *            Klasse der Anwendung
     * @return das Programm / die Anwendung
     */
    public Application getApp(String appClassName) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (InternetNodeOS), getSoftware(" + appClassName + ")");

        if (appClassName == null) return null;
        
        return (Application) installedApps.get(appClassName);
    }

    /**
     * Create an instance of the application the class name of which is given 
     * and then add it to the list of the installed applications.
     *  
     * @param appClassName
     * @return
     */
    public boolean installApp(String appClassName) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (InternetNodeOS), installApp(" + appClassName + ")");        
        //printInstalledApps(); // DEBUG        
        
        // Install application only once
        if (getApp(appClassName) != null) return false;
        
        Application app = createApp(appClassName);
		if (app == null) return false;
		
		installedApps.put(appClassName, app);
		return true;   
    }  
    
    /** 
     * <b>createApp</b> creates an instance of Application based on the application class name
     * 
     * @param appClassName
     * @return An instance of Application
     */
    private Application createApp(String appClassName) {
    	
    	Application app = null;
		try {
			Class<?> cl = Class.forName(appClassName, true, FiliusClassLoader.getInstance(Thread.currentThread().getContextClassLoader()));
			try {	
				app = (Application) cl.getConstructor().newInstance();
				app.setSystemSoftware(this);
			} catch (Exception e) {
				e.printStackTrace(Main.debug);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace(Main.debug);
		}
		return app;
    }

    /**
     * Methode zum Entfernen einer installierten Anwendung.
     * 
     * @param appClassName
     *            Klasse der zu entfernenden Anwendung
     * @return ob eine Anwendung entfernt wurde
     */
    public boolean removeApp(String appClassName) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (InternetNodeOS), removeApp(" + appClassName + ")");
        //printInstalledApps(); // DEBUG
        
        Iterator<Entry<String, Application>> it = installedApps.entrySet().iterator();

        while (it.hasNext()) {
            if (it.next().getKey().equals(appClassName)) {
                it.remove();
                return true;
            }
        }
        return false;
    }
    
//    /**
//     * Methode zur Ausgabe auf der aktuell installierten Anwendungen auf der Standardausgabe
//     */
//    private void printInstalledApps() {
//        Iterator<Entry<String, Application>> it = installedApps.entrySet().iterator();
//
//        Main.debug.println("\tInternetNodeOS: installierte Anwendungen:");
//        while (it.hasNext()) {
//            //Main.debug.println("\t  - " + ((Entry) it.next()).getKey().toString());
//            Main.debug.println("\t  - " + it.next().getKey());
//        }
//        Main.debug.println("\t  ges: " + installedApps.toString());
//    }    
    
//    public boolean uninstallApp(String appClassName) {
//        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (InternetNodeOS), uninstallApp(" + appClassName + ")");
//        printInstalledApps(); // DEBUG
// 
//        if (appClassName == null) return false;
//        
//        Application app = installedApps.get(appClassName);
//        if (app == null) return false;
//
//        installedApps.remove(app.getAppName());
//        return true;     
//    }
}
