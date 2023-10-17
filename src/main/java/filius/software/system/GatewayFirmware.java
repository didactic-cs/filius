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
import filius.software.nat.NatGateway;
import filius.software.rip.RIPTable;
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
}
