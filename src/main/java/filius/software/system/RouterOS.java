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

//Netzwerkziel, Netzwerkmaske, ZielIp(Gateway), Schnittstelle
import filius.Main;
import filius.hardware.knoten.InternetNode;
import filius.hardware.knoten.Node;
import filius.rahmenprogramm.Information;
import filius.rahmenprogramm.ResourceUtil;
import filius.software.firewall.Firewall;
import filius.software.firewall.FirewallRule;
import filius.software.firewall.FirewallWebKonfig;
import filius.software.firewall.FirewallWebLog;
import filius.software.rip.RIPBeacon;
import filius.software.rip.RIPServer;
import filius.software.rip.RIPTable;
import filius.software.vermittlungsschicht.VermittlungWeb;
import filius.software.www.WebServer;

/**
 * Diese Klasse stellt die Funktionalitaet eines Betriebssystems fuer Vermittlungsrechner zur Verfuegung. Spezifisch ist
 * die automatische Installation einer Firewall und eines Webservers mit einer Erweiterung zur Konfiguration der
 * Firewall. Die weitere Funktionalitaet wird von der Oberklasse (InternetKnotenBetriebssystem) zur Verfuegung gestellt.
 */
@SuppressWarnings("serial")
public class RouterOS extends InternetNodeOS {

    private boolean ripEnabled;

    private RIPTable riptable;
    private RIPBeacon ripbeacon;
    private RIPServer ripserver;

    /** Konstruktor mit Initialisierung von Firewall und Webserver */
    public RouterOS() {
        super();
        Main.debug.println("INVOKED-2 (" + this.hashCode() + ") " + getClass() + " (RouterOS), constr: RouterOS()");

        initApplications();
    }

    public void setNode(Node router) {
        super.setNode(router);
    }

    /**
     * Methode zur initialisierung der Firewall und des Web-Servers mit den Erweiterungen fuer den Zugriff auf die
     * Firewall ueber eine Web-Schnittstelle
     */
    private void initApplications() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (RouterOS), initApplications()");
        
        FirewallWebLog weblog;
        FirewallWebKonfig webkonfig;
        WebServer server = null;
        Firewall firewall = null;

        // Installation von Firewall und Webserver
        installApp("filius.software.firewall.Firewall");
        installApp("filius.software.www.WebServer");
        firewall = this.getFirewall();
        server = this.getWebServer();
        firewall.setModus(Firewall.GATEWAY);
        firewall.setDefaultAction(FirewallRule.DROP);
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

        server.createIndexFile(ResourceUtil.getResourcePath("tmpl/vermittlung_index_"
                + Information.getInstance().getLocale() + ".txt"));

        // ------------- RIP ------------------
        riptable = new RIPTable(this);
        ripserver = new RIPServer();
        ripserver.setSystemSoftware(this);
        ripbeacon = new RIPBeacon();
        ripbeacon.setSystemSoftware(this);

        VermittlungWeb ripweb = new VermittlungWeb(this);
        ripweb.setPfad("routes");
        server.setzePlugIn(ripweb);
    }

    /**
     * Starten des Webservers
     * 
     * @see filius.software.system.InternetKnotenBetriebssystem.starten()
     */
    public void start() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass()
                + " (VermittlungsrechnerBetriebssystem), starten()");

        super.start();

        // Startet den Web-Server
        getWebServer().setActive(true);

        if (ripEnabled) {
            riptable.reset();
            riptable.addLocalRoutes((InternetNode) this.getNode());
            ripserver.startThread();
            ripserver.setActive(true);
            ripbeacon.startThread();
        }
    }

    public void stop() {
        super.stop();
        if (ripEnabled) {
            ripbeacon.stopThread();
            ripserver.stopThread();
        }
    }

    @Override
    public RIPTable getRIPTable() {
        if (ripEnabled) {
            return riptable;
        } else {
            return null;
        }
    }
    
    public void setRipEnabled(boolean enabled) {
        ripEnabled = enabled;
    }
    
    @Override
    public boolean isRIPEnabled() {
        return ripEnabled;
    }


    /**
     * Methode fuer den Zugriff auf die Firewall. Dieser Zugriff ist nicht JavaBean-konform, weil die Speicherung der
     * Firewall als eine Anwendung durch die Oberklasse erfolgt.
     */
    public Firewall getFirewall() {
        return (Firewall) getApp("filius.software.firewall.Firewall");
    }

    /**
     * Methode fuer den Zugriff auf den Webserver Dieser Zugriff ist nicht JavaBean-konform, weil die Speicherung des
     * Webservers als eine Anwendung durch die Oberklasse erfolgt.
     */
    public WebServer getWebServer() {
        return (WebServer) getApp("filius.software.www.WebServer");
    }
}
