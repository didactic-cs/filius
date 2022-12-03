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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import filius.hardware.NetzwerkInterface;
import filius.hardware.knoten.InternetKnoten;
import filius.rahmenprogramm.EingabenUeberpruefung;
import filius.rahmenprogramm.FiliusClassLoader;
import filius.rahmenprogramm.Information;
import filius.software.Anwendung;
import filius.software.dhcp.DHCPClient;
import filius.software.dhcp.DHCPServer;
import filius.software.dns.Resolver;
import filius.software.netzzugangsschicht.Ethernet;
import filius.software.rip.RIPTable;
import filius.software.transportschicht.TCP;
import filius.software.transportschicht.UDP;
import filius.software.vermittlungsschicht.ARP;
import filius.software.vermittlungsschicht.ICMP;
import filius.software.vermittlungsschicht.IP;
import filius.software.vermittlungsschicht.Route;
import filius.software.vermittlungsschicht.RouteNotFoundException;
import filius.software.vermittlungsschicht.Weiterleitungstabelle;

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
public abstract class InternetKnotenBetriebssystem extends SystemSoftware {
    private static Logger LOG = LoggerFactory.getLogger(InternetKnotenBetriebssystem.class);

    /** Das lokale Dateisystem eines Rechners */
    private Dateisystem dateisystem;
    /** der DHCP-Server, der aktiviert und deaktiviert werden kann */
    private DHCPServer dhcpServer;
    /** ob die Konfiguration der Netzwerkkarte mit DHCP erfolgt */
    private boolean dhcpKonfiguration;
    /**
     * der DHCP-Client, der zur Konfiguration der Netzwerkkarte genutzt wird, wenn die Konfiguration mit DHCP erfolgen
     * soll
     * 
     * @see dhcpKonfiguration
     */
    private DHCPClient dhcpClient;

    /**
     * Die installierten Anwendungen. Sie werden mit dem Anwendungsnamen als Schluessel in einer HashMap gespeichert.
     */
    private HashMap<String, Anwendung> installierteAnwendung;

    /**
     * Whether IP forwarding is enabled, i.e. if received ip pakets for another destination are sent again toward its
     * destination.
     */
    private boolean ipForwardingEnabled;

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
    private Weiterleitungstabelle weiterleitungstabelle;

    /**
     * Die Netzzugangsschicht wird durch das Ethernet-Protokoll implementiert. Die zugehoerigen Threads werden vom
     * Betriebssystem gestartet und beendet.
     */
    private Ethernet ethernet;

    /**
     * Konstruktor fuer das Betriebssystem eines Internetknotens. Hier werden
     * <ul>
     * <li>die Schichten initialisiert,</li>
     * <li>die installierten Anwendungen zurueck gesetzt,</li>
     * <li>das Dateisystem initialisiert,</li>
     * <li>der DNS-Client erzeugt.</li>
     * </ul>
     */
    public InternetKnotenBetriebssystem() {
        super();
        LOG.trace("INVOKED-2 (" + this.hashCode() + ") " + getClass()
                + " (InternetKnotenBetriebssystem), constr: InternetKnotenBetriebssystem()");

        installierteAnwendung = new HashMap<String, Anwendung>();

        weiterleitungstabelle = new Weiterleitungstabelle();
        weiterleitungstabelle.setInternetKnotenBetriebssystem(this);

        arpVermittlung = new ARP(this);
        vermittlung = new IP(this);
        icmpVermittlung = new ICMP(this);
        ethernet = new Ethernet(this);

        tcp = new TCP(this);
        udp = new UDP(this);

        dateisystem = new Dateisystem();

        dnsclient = new Resolver();
        dnsclient.setSystemSoftware(this);

        dhcpServer = new DHCPServer();
        dhcpServer.setSystemSoftware(this);

        LOG.debug("DEBUG: InternetKnotenBetriebssystem (" + this.hashCode() + ")\n" + "\tEthernet: "
                + ethernet.hashCode() + "\n" + "\tARP: " + arpVermittlung.hashCode() + "\n" + "\tIP: "
                + vermittlung.hashCode() + "\n" + "\tICMP: " + icmpVermittlung.hashCode() + "\n" + "\tTCP: "
                + tcp.hashCode() + "\n" + "\tUDP: " + udp.hashCode());
    }

    /**
     * Zum beenden der Protokoll-Threads und der Anwendungs-Threads.
     * 
     * @see filius.software.system.SystemSoftware.beenden()
     */
    public void beenden() {
        super.beenden();
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass() + " (InternetKnotenBetriebssystem), beenden()");

        // Die einzelnen Protokoll-Threads werden beginnend
        // mit der untersten Schicht beendet.

        // Netzzugangsschicht
        ethernet.beenden();

        // Vermittlungsschicht
        arpVermittlung.beenden();
        vermittlung.beenden();
        icmpVermittlung.beenden();

        // Transportschicht
        tcp.beenden();
        udp.beenden();

        dnsclient.beenden();
        dhcpServer.beenden();

        if (dhcpClient != null) {
            dhcpClient.beenden();
        }

        for (Anwendung anwendung : installierteAnwendung.values()) {
            anwendung.beenden();
        }
    }

    /** Methode zum Zugriff auf den DHCP-Server. */
    public DHCPServer getDHCPServer() {
        return dhcpServer;
    }

    /** Methode zum Zugriff auf den DHCP-Server. */
    public void setDHCPServer(DHCPServer dhcpServer) {
        this.dhcpServer = dhcpServer;
    }

    /**
     * Methode zum starten der Protokoll-Threads und der Anwendungen.
     * 
     * @see filius.software.system.SystemSoftware.starten()
     */
    @Override
    public synchronized void starten() {
        super.starten();
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass() + " (InternetKnotenBetriebssystem), starten()");

        dateisystem.fixDirectory(dateisystem.getRoot());

        // Die Protokoll-Threads der einzelnen Schichten werden
        // beginnend mit der untersten Schicht gestartet.
        ethernet.starten();
        arpVermittlung.starten();
        vermittlung.starten();
        icmpVermittlung.starten();
        tcp.starten();
        udp.starten();

        if (dhcpServer.isAktiv()) {
            dhcpServer.starten();
        }
        if (isDHCPKonfiguration()) {
            dhcpClient = new DHCPClient();
            dhcpClient.setSystemSoftware(this);
            dhcpClient.starten();
        }
        for (Anwendung anwendung : installierteAnwendung.values()) {
            if (anwendung != null) {
                anwendung.starten();
            }
        }
    }

    /** Ob die Konfiguration der Netzwerkkarte mit DHCP erfolgt */
    public boolean isDHCPKonfiguration() {
        return dhcpKonfiguration;
    }

    /** Ob die Konfiguration der Netzwerkkarte mit DHCP erfolgt */
    public void setDHCPKonfiguration(boolean dhcp) {
        this.dhcpKonfiguration = dhcp;
    }

    /** Methode fuer den Zugriff auf den DNS-Resolver */
    public Resolver holeDNSClient() {
        return dnsclient;
    }

    /**
     * Methode fuer den Zugriff auf das Transport Control Protocol (TCP).
     */
    public TCP holeTcp() {
        return tcp;
    }

    /**
     * Methode fuer den Zugriff auf das User Datagram Protocol (UDP).
     */
    public UDP holeUdp() {
        return udp;
    }

    /**
     * Methode fuer den Zugriff auf das Address Resolution Protocol (ARP).
     */
    public ARP holeARP() {
        return arpVermittlung;
    }

    /**
     * Methode fuer den Zugriff auf das Internet Control Message Protocol (ICMP).
     */
    public ICMP holeICMP() {
        return icmpVermittlung;
    }

    /**
     * Methode fuer den Zugriff auf das Internet Protocol (IP).
     */
    public IP holeIP() {
        return vermittlung;
    }

    /** Methode fuer den Zugriff auf das Ethernet-Protokoll */
    public Ethernet holeEthernet() {
        return ethernet;
    }

    /**
     * Methode fuer den Zugriff auf das Dateisystem Diese Methode wird fuer das Speichern des Dateisystems in einer
     * Projektdatei benoetigt. (JavaBean- konformer Zugriff erforderlich)
     */
    public Dateisystem getDateisystem() {
        return dateisystem;
    }

    /**
     * Methode, um das Dateisystem zu setzen. Diese Methode wird fuer das Speichern des Dateisystems in einer
     * Projektdatei benoetigt. (JavaBean- konformer Zugriff erforderlich)
     * 
     * @param dateisystem
     */
    public void setDateisystem(Dateisystem dateisystem) {
        this.dateisystem = dateisystem;
    }

    public abstract RIPTable getRIPTable();

    public abstract boolean isRipEnabled();

    public boolean isIpForwardingEnabled() {
        return ipForwardingEnabled;
    }

    public void setIpForwardingEnabled(boolean ipForwardingEnabled) {
        this.ipForwardingEnabled = ipForwardingEnabled;
    }

    /**
     * Methode fuer den Zugriff auf die Hash-Map zur Verwaltung der installierten Anwendungen. Diese Methode wird
     * benoetigt, um den Anforderungen an JavaBeans gerecht zu werden.
     */
    public void setInstallierteAnwendungen(HashMap<String, Anwendung> anwendungen) {
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass()
                + " (InternetKnotenBetriebssystem), setInstallierteAnwendungen()");
        this.installierteAnwendung = anwendungen;
    }

    /**
     * Methode zur Ausgabe auf der aktuell installierten Anwendungen auf der Standardausgabe
     */
    private void printInstallierteAnwendungen() {
        LOG.debug("\tInternetKnotenBetriebssystem: installierte Anwendungen:");
        for (String app : installierteAnwendung.keySet()) {
            LOG.debug("\t  - {}", app);
        }
        LOG.debug("\t  ges: {}", installierteAnwendung);
    }

    /**
     * Methode fuer den Zugriff auf die Hash-Map zur Verwaltung der installierten Anwendungen. Diese Methode wird
     * benoetigt, um den Anforderungen an JavaBeans gerecht zu werden.
     */
    public HashMap<String, Anwendung> getInstallierteAnwendungen() {
        return installierteAnwendung;
    }

    /**
     * Methode fuer den Zugriff auf eine bereits installierte Anwendung.
     * 
     * @param anwendungsklasse
     *            Klasse der Anwendung
     * @return das Programm / die Anwendung
     */
    public Anwendung holeSoftware(String anwendungsklasse) {
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass() + " (InternetKnotenBetriebssystem), holeSoftware("
                + anwendungsklasse + ")");
        Anwendung anwendung;

        if (anwendungsklasse == null)
            return null;
        anwendung = (Anwendung) installierteAnwendung.get(anwendungsklasse);
        if (anwendung == null) {
            return null;
        } else {
            return anwendung;
        }

    }

    /**
     * Methode zum Entfernen einer installierten Anwendung.
     * 
     * @param awKlasse
     *            Klasse der zu entfernenden Anwendung
     * @return ob eine Anwendung entfernt wurde
     */
    public boolean entferneSoftware(String awKlasse) {
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass()
                + " (InternetKnotenBetriebssystem), entferneSoftware(" + awKlasse + ")");
        printInstallierteAnwendungen(); // DEBUG
        boolean entfernt = false;
        Iterator it = installierteAnwendung.entrySet().iterator();

        while (it.hasNext() && !entfernt) {
            if (awKlasse.equals((String) ((Entry) it.next()).getKey())) {
                it.remove();
                entfernt = true;
            }
        }
        return entfernt;
    }

    public boolean installAppIfAvailable(String klassenname) {
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass()
                + " (InternetKnotenBetriebssystem), installiereSoftware(" + klassenname + ")");
        printInstallierteAnwendungen(); // DEBUG

        boolean erfolg = false;
        if (checkAlreadyInstalled(klassenname)) {
            LOG.info("App {} could not be installed because app is already installed.", klassenname);
        } else if (!checkAppAvailable(klassenname)) {
            LOG.info("{} could not be installed because app is not in list of available software.", klassenname);
        } else if (installApp(klassenname)) {
            LOG.info("App {} installed.", klassenname);
            erfolg = true;
        } else {
            LOG.info("App {} could not be installed. An error occurred.", klassenname);
        }
        return erfolg;
    }

    private boolean checkAlreadyInstalled(String klassenname) {
        return holeSoftware(klassenname) != null;
    }

    private boolean checkAppAvailable(String klassenname) {
        boolean available = false;
        try {
            for (Map<String, String> app : Information.getInformation().ladeProgrammListe()) {
                if (klassenname.equals(app.get("Klasse"))) {
                    available = true;
                }
            }
        } catch (Exception e) {
            LOG.debug("list of applications could not be read.", e);
        }
        return available;
    }

    public boolean installApp(String klassenname) {
        boolean erfolg = true;
        try {
            Class<?> cl = Class.forName(klassenname, true,
                    FiliusClassLoader.getInstance(Thread.currentThread().getContextClassLoader()));
            Anwendung neueAnwendung = (Anwendung) cl.getConstructor().newInstance();
            neueAnwendung.setSystemSoftware(this);
            installierteAnwendung.put(klassenname, neueAnwendung);
        } catch (Exception e) {
            LOG.debug("App could not be instantiated. Probably because class could not be found.", e);
            erfolg = false;
        }
        return erfolg;
    }

    public boolean deinstalliereAnwendung(String anwendungsName) {
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass()
                + " (InternetKnotenBetriebssystem), deinstalliereAnwendung(" + anwendungsName + ")");
        printInstallierteAnwendungen(); // DEBUG
        Anwendung anwendung;

        if (anwendungsName == null)
            return false;
        anwendung = (Anwendung) installierteAnwendung.get(anwendungsName);
        if (anwendung == null) {
            return false;
        } else {
            installierteAnwendung.remove(anwendung.holeAnwendungsName());
            return true;
        }
    }

    /**
     * Methode zur Abfrage aller aktuell installierter Anwendungen
     * 
     * @return ein Array der Anwendungsnamen
     */
    public Anwendung[] holeArrayInstallierteSoftware() {
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass()
                + " (InternetKnotenBetriebssystem), holeArrayInstallierteSoftware()");
        Anwendung[] anwendungen;
        Iterator it = installierteAnwendung.entrySet().iterator();

        anwendungen = new Anwendung[installierteAnwendung.size()];
        for (int i = 0; it.hasNext() && i < anwendungen.length; i++) {
            anwendungen[i] = (Anwendung) ((Entry) it.next()).getValue();
        }

        // printInstallierteAnwendungen();

        return anwendungen;
    }

    /**
     * Methode fuer den JavaBean-konformen Zugriff auf die Weiterleitungstabelle. Diese Methode wird aber ausserdem von
     * der Implementierung der Vermittlungsschicht verwendet.
     */
    public Weiterleitungstabelle getWeiterleitungstabelle() {
        return weiterleitungstabelle;
    }

    public Route determineRoute(String ipAddress) throws RouteNotFoundException {
        return weiterleitungstabelle.holeWeiterleitungsEintrag(ipAddress);
    }

    /**
     * Methode fuer den JavaBean-konformen Zugriff auf die Weiterleitungstabelle.
     */
    public void setWeiterleitungstabelle(Weiterleitungstabelle tabelle) {
        this.weiterleitungstabelle = tabelle;
    }

    /**
     * Methode fuer den Zugriff auf die IP-Adresse des Standard-Gateways, aller Netzwerkkarten.
     * 
     * @return IP-Adresse der einzigen Netzwerkkarte als String
     */
    public String getStandardGateway() {
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass()
                + " (InternetKnotenBetriebssystem), getStandardGateway()");
        InternetKnoten knoten;
        NetzwerkInterface nic;

        if (getKnoten() instanceof InternetKnoten) {
            knoten = (InternetKnoten) getKnoten();

            if (knoten.getNetzwerkInterfaces().size() > 0) {
                nic = (NetzwerkInterface) knoten.getNetzwerkInterfaces().get(0);
                return nic.getGateway();
            }
        }
        return null;
    }

    /**
     * Methode zum Einstellen des Standard-Gateways fuer die Netwerkkarten. Das ist eine Methode des Entwurfsmusters
     * Fassade.
     * 
     * @param gateway
     *            IP-Adresse der Netzwerkkarten als String
     */
    public void setStandardGateway(String gateway) {
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass()
                + " (InternetKnotenBetriebssystem), setStandardGateway(" + gateway + ")");
        InternetKnoten knoten;
        NetzwerkInterface nic;
        Iterator<?> it;
        gateway = (gateway != null && gateway.trim().equals("")) ? gateway.trim() : IP.ipCheck(gateway);

        if (gateway != null && EingabenUeberpruefung.isGueltig(gateway, EingabenUeberpruefung.musterIpAdresseAuchLeer)
                && getKnoten() instanceof InternetKnoten) {
            knoten = (InternetKnoten) getKnoten();
            it = knoten.getNetzwerkInterfaces().listIterator();
            while (it.hasNext()) {
                nic = (NetzwerkInterface) it.next();
                nic.setGateway(gateway);
            }
        }
    }

    /**
     * Methode zum Einstellen der IP-Adresse fuer die einzige Netwerkkarte. Das ist eine Methode des Entwurfsmusters
     * Fassade
     */
    public void setzeIPAdresse(String ip) {
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass() + " (InternetKnotenBetriebssystem), setzeIPAdresse("
                + ip + ")");
        InternetKnoten knoten;
        ip = IP.ipCheck(ip);
        if (ip != null && EingabenUeberpruefung.isGueltig(ip, EingabenUeberpruefung.musterIpAdresse)
                && getKnoten() instanceof InternetKnoten) {
            knoten = (InternetKnoten) getKnoten();
            ((NetzwerkInterface) knoten.getNetzwerkInterfaces().get(0)).setIp(ip);
        }
    }

    /**
     * Methode fuer den Zugriff auf die IP-Adresse der einzigen Netwerkkarte. Das ist eine Methode des Entwurfsmusters
     * Fassade
     */
    public String primaryIPAdresse() {
        NetzwerkInterface nic = primaryNetworkInterface();
        return null == nic ? null : nic.getIp();
    }

    protected NetzwerkInterface primaryNetworkInterface() {
        NetzwerkInterface nic = null;
        if (getKnoten() instanceof InternetKnoten) {
            InternetKnoten knoten = (InternetKnoten) getKnoten();
            nic = knoten.getNetzwerkInterfaces().get(0);
        }
        return nic;
    }

    /**
     * Methode fuer den Zugriff auf die MAC-Adresse der einzigen Netwerkkarte. Das ist eine Methode des Entwurfsmusters
     * Fassade
     */
    public String primaryMACAddress() {
        NetzwerkInterface nic = primaryNetworkInterface();
        return null == nic ? null : nic.getMac();
    }

    public String dhcpEnabledMACAddress() {
        return primaryMACAddress();
    }

    /**
     * Methode fuer den Zugriff auf die IP-Adresse des DNS-Servers der aller Netzwerkkarten. Das ist eine Methode des
     * Entwurfsmusters Fassade
     */
    public String getDNSServer() {
        LOG.debug(
                "INVOKED (" + this.hashCode() + ") " + getClass() + " (InternetKnotenBetriebssystem), getDNSServer()");
        InternetKnoten knoten;

        if (getKnoten() instanceof InternetKnoten) {
            knoten = (InternetKnoten) getKnoten();

            if (knoten.getNetzwerkInterfaces().size() > 0) {
                NetzwerkInterface nic = (NetzwerkInterface) knoten.getNetzwerkInterfaces().get(0);
                return nic.getDns();
            }
        }
        return null;
    }

    /**
     * Methode fuer den Zugriff auf die IP-Adresse des DNS-Servers der aller Netzwerkkarten. Das ist eine Methode des
     * Entwurfsmusters Fassade
     */
    public void setDNSServer(String dns) {
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass() + " (InternetKnotenBetriebssystem), setDNSServer("
                + dns + ")");
        InternetKnoten knoten;
        NetzwerkInterface nic;
        Iterator<?> it;
        dns = (dns != null && dns.trim().equals("")) ? dns.trim() : IP.ipCheck(dns);

        if (dns != null && EingabenUeberpruefung.isGueltig(dns, EingabenUeberpruefung.musterIpAdresseAuchLeer)
                && getKnoten() instanceof InternetKnoten) {
            knoten = (InternetKnoten) getKnoten();
            it = knoten.getNetzwerkInterfaces().listIterator();
            while (it.hasNext()) {
                nic = (NetzwerkInterface) it.next();
                nic.setDns(dns);
            }
        }
    }

    /**
     * Methode fuer den Zugriff auf die Netzmaske der einzigen Netzwerkkarte. Das ist eine Methode des Entwurfsmusters
     * Fassade
     */
    public void setzeNetzmaske(String mask) {
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass() + " (InternetKnotenBetriebssystem), setzeNetzmaske("
                + mask + ")");
        InternetKnoten knoten;
        mask = IP.ipCheck(mask);

        if (mask != null && EingabenUeberpruefung.isGueltig(mask, EingabenUeberpruefung.musterSubNetz)
                && getKnoten() instanceof InternetKnoten) {
            knoten = (InternetKnoten) getKnoten();
            ((NetzwerkInterface) knoten.getNetzwerkInterfaces().get(0)).setSubnetzMaske(mask);
            // LOG.debug("\t"
            // + ((NetzwerkInterface) knoten.getNetzwerkInterfaces()
            // .getFirst()).getSubnetzMaske());
        }
    }

    /**
     * Methode fuer den Zugriff auf die Netzmaske der einzigen Netzwerkkarte. Das ist eine Methode des Entwurfsmusters
     * Fassade
     */
    public String primarySubnetMask() {
        NetzwerkInterface nic = primaryNetworkInterface();
        return null == nic ? null : nic.getSubnetzMaske();

    }
}
