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

import java.beans.PropertyChangeEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import filius.hardware.NetzwerkInterface;
import filius.hardware.knoten.Gateway;
import filius.hardware.knoten.Knoten;
import filius.rahmenprogramm.Information;
import filius.rahmenprogramm.ResourceUtil;
import filius.software.firewall.Firewall;
import filius.software.firewall.FirewallRule;
import filius.software.firewall.FirewallWebKonfig;
import filius.software.firewall.FirewallWebLog;
import filius.software.nat.InetAddress;
import filius.software.nat.NatEntry;
import filius.software.nat.NatGateway;
import filius.software.nat.NatMethod;
import filius.software.nat.NatType;
import filius.software.nat.NetworkAddressTranslationTable;
import filius.software.nat.PortProtocolPair;
import filius.software.rip.RIPTable;
import filius.software.vermittlungsschicht.IpPaket;
import filius.software.www.WebServer;

/**
 * The Home Router supports the following functions:
 * <li>DHCP server on the LAN port
 * <li>IP configuration with DHCP on the WAN port
 * <li>Firewall
 * <li>Webserver for basic administration
 */
@SuppressWarnings("serial")
public class GatewayFirmware extends InternetKnotenBetriebssystem {
    private static Logger LOG = LoggerFactory.getLogger(GatewayFirmware.class);

    private long retentionTime = 300000;
    private NatMethod natMethod = NatMethod.fullCone;	
    
    private boolean natMethodChanged = false;

    public long getRetentionTime() {
        return retentionTime;
    }

    public void setRetentionTime(long retentionTime) {
        this.retentionTime = retentionTime;
    }
    
    public NatMethod getNatMethod() {
    	return natMethod;
    }
    
    public void setNatMethod(NatMethod natMethod) {
    	this.natMethod = natMethod;
    }

	public void setNatMethodChanged(boolean b) {
		natMethodChanged = b;
		
	}

    /** Konstruktor mit Initialisierung von Firewall und Webserver */
    public GatewayFirmware() {
        super();        
        LOG.trace("INVOKED-2 (" + this.hashCode() + ") " + getClass()
                + " (VermittlungsrechnerBetriebssystem), constr: VermittlungsrechnerBetriebssystem()");
        setIpForwardingEnabled(true);
        initialisiereAnwendungen();
    }

    public void setKnoten(Knoten gateway) {
        super.setKnoten(gateway);
    }

    /** The IP configuration with DHCP is supported on WAN port only. */
    @Override
    public String dhcpEnabledMACAddress() {
        return ((Gateway) getKnoten()).holeWANInterface().getMac();
    }

    @Override
    public NetzwerkInterface primaryNetworkInterface() {
        return ((Gateway) getKnoten()).holeLANInterface();
    }

    /**
     * Methode zur initialisierung der Firewall und des Web-Servers mit den Erweiterungen fuer den Zugriff auf die
     * Firewall ueber eine Web-Schnittstelle
     */
    private void initialisiereAnwendungen() {
        FirewallWebLog weblog;
        FirewallWebKonfig webkonfig;
        WebServer server = null;
        Firewall firewall = null;

        // Installation von Firewall und Webserver
        installApp("filius.software.nat.NatGateway");
        installApp("filius.software.www.WebServer");
        firewall = this.holeFirewall();
        server = this.holeWebServer();
        firewall.setDefaultPolicy(FirewallRule.DROP);
        firewall.setActivated(false);

        // Erweiterung des Webservers fuer die Anzeige der
        // Log-Eintraege der Firewall
        weblog = new FirewallWebLog();
        weblog.setFirewall(firewall);
        weblog.setPfad("log");
        server.setzePlugIn(weblog);

        // Erweiterung des Webservers fuer die Konfiguration
        // der Firewall
        webkonfig = new FirewallWebKonfig();
        webkonfig.setWebserver(server);
        webkonfig.setFirewall(firewall);
        webkonfig.setPfad("konfig");
        server.setzePlugIn(webkonfig);

        server.erzeugeIndexDatei(ResourceUtil
                .getResourcePath("tmpl/gateway_index_" + Information.getInformation().getLocaleOrDefault() + ".txt"));
    }

    /**
     * Starten des Webservers
     * 
     * @see filius.software.system.InternetKnotenBetriebssystem.starten()
     */
    public void starten() {
        super.starten();
        holeWebServer().setAktiv(true);
        
        Timer timer = new Timer();

        NetworkAddressTranslationTable nat = ((NatGateway) this.holeFirewall()).getNATTable();
        nat.setNatMethod(natMethod);
        nat.resetStaticNATTable();
        LinkedList<String[]> tabelle = getWeiterleitungstabelle().getManuelleTabelle();
        
        if (!tabelle.isEmpty()) {
        	for (int i = 0; i < tabelle.size(); i++) {
        		String[] entry = tabelle.get(i);
        		int protocol;
        		switch (entry[0].toUpperCase()) {
        		case "TCP":
        			protocol = IpPaket.TCP;
        			break;
        		case "UDP":
        			protocol = IpPaket.UDP;
        			break;
        		default:
        			protocol = Integer.parseInt(entry[0]);
        		}
        		InetAddress lanAddress = new InetAddress(entry[2],Integer.parseInt(entry[3]),protocol);
        		nat.addStatic(Integer.parseInt(entry[1]),protocol, lanAddress);
        	}
        }
        fireNATPropertyChange();
        
        Map<PortProtocolPair, NatEntry> dynNat = nat.getDynamicNATTable();
    	timer.scheduleAtFixedRate(new TimerTask(){
        	@Override
        	public void run() {
        		if(!isStarted()) {
        			timer.cancel();
        		} else if (!dynNat.isEmpty()) {
        			checkNAT();	
        		}
        	}
        }, 1000, 1000);
    	
    	if (natMethodChanged){
    		firePropertyChanged(new PropertyChangeEvent(this, "nat_method", null, null));
    		natMethodChanged = false;
    	}
    }

    public void beenden() {
        super.beenden();
    }

    /**
     * Methode fuer den Zugriff auf die Firewall. Dieser Zugriff ist nicht JavaBean-konform, weil die Speicherung der
     * Firewall als eine Anwendung durch die Oberklasse erfolgt.
     */
    public Firewall holeFirewall() {
        Firewall firewall = (Firewall) holeSoftware("filius.software.firewall.Firewall");
        if (null == firewall) {
            firewall = (NatGateway) holeSoftware("filius.software.nat.NatGateway");
        }
        return firewall;
    }

    /**
     * Methode fuer den Zugriff auf den Webserver Dieser Zugriff ist nicht JavaBean-konform, weil die Speicherung des
     * Webservers als eine Anwendung durch die Oberklasse erfolgt.
     */
    public WebServer holeWebServer() {
        return (WebServer) holeSoftware("filius.software.www.WebServer");
    }

    @Override
    public RIPTable getRIPTable() {
        return null;
    }

    @Override
    public boolean isRipEnabled() {
        return false;
    }
    
    public Vector<Vector<String>> holeNAT() { //neu
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass() + " (GatewayFirmware), holeNAT()");
        
        NetworkAddressTranslationTable nat = null;
        nat = ((NatGateway) this.holeFirewall()).getNATTable();
        
        Vector<Vector<String>> eintraege = new Vector<Vector<String>>();
        Vector<String> eintrag;
        
        for (PortProtocolPair key : nat.getStaticNATTable().keySet()) {
            eintrag = new Vector<String>();
            eintrag = createEintrag(key.getProtocol(),"--",key.getPort(),nat.getStaticNATTable().get(key).getInetAddress().getIpAddress(),nat.getStaticNATTable().get(key).getInetAddress().getPort(),nat.getStaticNATTable().get(key).getNatType(),null ,nat.getNatMethod());
            eintraege.add(eintrag);
        }
        for (PortProtocolPair key : nat.getDynamicNATTable().keySet()) {
        	eintrag = new Vector<String>();
            eintrag = createEintrag(key.getProtocol(),key.getAddress(),key.getPort(),nat.getDynamicNATTable().get(key).getInetAddress().getIpAddress(),nat.getDynamicNATTable().get(key).getInetAddress().getPort(),nat.getDynamicNATTable().get(key).getNatType(),nat.getDynamicNATTable().get(key).getLastUpdate(),nat.getNatMethod());
            eintraege.add(eintrag);
        }

        return eintraege;
    }
    
    private Vector<String> createEintrag(int prot, String WANaddr, int WANport, String LANaddr, int LANport, NatType natType, Date update, NatMethod natMethod) {
    	
        Vector<String> eintrag = new Vector<String>();
        String ausgabe;
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        
        switch(prot) {
        case IpPaket.TCP:
        	ausgabe = "TCP";
        	break;
        case IpPaket.UDP:
        	ausgabe = "UDP";
        	break;
        default:
        	ausgabe = ""+prot;
        }
        
		eintrag.add(ausgabe);
		if (natMethod == NatMethod.restrictedCone) {
			eintrag.add(WANaddr);
		}
        eintrag.add(""+WANport);
        eintrag.add(LANaddr);
        eintrag.add(""+LANport);
        switch(natType) {
        case StaticEntry:
        	ausgabe = "static";
        	break;
        case DynamicEntry:
        	ausgabe = "dynamic";
        	break;
        case DynamicEnryFromStatic:
        	ausgabe = "dynamic (static)";
        	break;
        default:
        	ausgabe = "";
        }
        eintrag.add(ausgabe);
        if (update == null) {
        	eintrag.add("");
        } else {
        	eintrag.add(formatter.format(update));
        }
    
    	return eintrag;
    }
    
    /**
     * Methode zum Löschen der dynamischen Einträge der NAT-Tabelle
     */
    public void loescheNAT() {
    	Map<PortProtocolPair, NatEntry> nat = ((NatGateway) this.holeFirewall()).getNATTable().getDynamicNATTable();
    	nat.clear();
        firePropertyChanged(new PropertyChangeEvent(this, "nat_entry", null, null));
    }
    
    /**
     * Methode zum Überprüfen der SAT
     */
    public void checkNAT() {
    	Date jetzt = new Date();
    	ConcurrentHashMap<PortProtocolPair, NatEntry> nat = ((NatGateway) this.holeFirewall()).getNATTable().getDynamicNATTable();
    	nat.forEach((portProtocolPair, natEntry) -> {
    		if (jetzt.getTime()-natEntry.getLastUpdate().getTime() >= getRetentionTime()) {
    			nat.remove(portProtocolPair);
    			firePropertyChanged(new PropertyChangeEvent(this, "nat_entry", null, null));
    		};
    	});
    }
    
    public void fireNATPropertyChange() {
    	firePropertyChanged(new PropertyChangeEvent(this, "nat_entry", null, null));
    }
}
