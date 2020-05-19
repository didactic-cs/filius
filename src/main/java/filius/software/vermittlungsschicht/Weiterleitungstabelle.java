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
package filius.software.vermittlungsschicht;

import java.util.LinkedList;

import filius.Main;
import filius.exception.InvalidParameterException;
import filius.hardware.NetzwerkInterface;
import filius.hardware.knoten.InternetKnoten;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.SzenarioVerwaltung;
import filius.software.rip.RIPRoute;
import filius.software.rip.RIPTable;
import filius.software.system.InternetKnotenBetriebssystem;

/**
 * Mit dieser Klasse wird die Weiterleitungstabelle implementiert. Es werden manuell erstellte Eintraege und aus der
 * IP-Konfiguration der Netzwerkkarten des Knotens automatisch erzeugte Eintraege unterschieden. Gespeichert werden nur
 * manuelle Eintraege. Ausserdem werden bei jeder Abfrage die automatischen Standard-Eintraege erzeugt.
 */
public class Weiterleitungstabelle implements I18n {

    /**
     * Die Tabelle mit den manuellen Eintraegen der Weiterleitungstabelle. Sie werden als String-Arrays in einer Liste
     * verwaltet. Ein Eintrag besteht aus folgenden Elementen:
     * <ol>
     * <li>Netz-ID der Zieladresse als IP-Adresse</li>
     * <li>Netzmaske zur Berechnung der Netz-ID aus dem ersten Wert</li>
     * <li>Das Standard-Gateway, ueber die die Ziel-IP-Adresse erreicht wird, wenn sie sich nicht im gleichen
     * Rechnernetz wie der eigene Rechner befindet</li>
     * <li>Die IP-Adresse der Netzwerkkarte, ueber die die Ziel-IP-Adresse erreicht wird</li>
     * </ol>
     */
    private LinkedList<String[]> manuelleTabelle;

    /**
     * Eine Liste, in der angegeben wird, welche Eintraege in der erzeugten Tabelle automatisch erzeugt bzw. manuelle
     * Eintraege sind
     */
    private LinkedList<Boolean> manuelleEintraege;

    /** Die Systemsoftware */
    private InternetKnotenBetriebssystem firmware = null;

    /**
     * Im Standard-Konstruktor wird die Methode reset() aufgerufen. Damit werden alle manuellen Eintraege geloescht
     */
    public Weiterleitungstabelle() {
        reset();
    }

    /** Methode fuer den Zugriff auf die Systemsoftware */
    public void setInternetKnotenBetriebssystem(InternetKnotenBetriebssystem firmware) {
        this.firmware = firmware;
    }

    /** Methode fuer den Zugriff auf die Systemsoftware */
    public InternetKnotenBetriebssystem getInternetKnotenBetriebssystem() {
        return firmware;
    }

    /**
     * Methode fuer den Zugriff auf die manuellen Eintrage. Diese Methode sollte nur fuer das speichern genutzt werden!
     */
    public void setManuelleTabelle(LinkedList<String[]> tabelle) {
        this.manuelleTabelle = tabelle;
    }

    /**
     * Methode fuer den Zugriff auf die manuellen Eintrage. Diese Methode sollte nur fuer das speichern genutzt werden!
     */
    public LinkedList<String[]> getManuelleTabelle() {
        return manuelleTabelle;
    }

    /**
     * Methode zum hinzufuegen eines neuen Eintrags. Ein Eintrag besteht aus folgenden Elementen:
     * <ol>
     * <li>Netz-ID der Zieladresse als IP-Adresse</li>
     * <li>Netzmaske zur Berechnung der Netz-ID aus dem ersten Wert</li>
     * <li>Das Standard-Gateway, ueber die die Ziel-IP-Adresse erreicht wird, wenn sie sich nicht im gleichen
     * Rechnernetz wie der eigene Rechner befindet</li>
     * <li>Die IP-Adresse der Netzwerkkarte, ueber die die Ziel-IP-Adresse erreicht wird</li>
     * </ol>
     * 
     * @param netzwerkziel
     * @param netzwerkmaske
     * @param gateway
     * @param schnittstelle
     */
    public void addManuellenEintrag(String netzwerkziel, String netzwerkmaske, String gateway, String schnittstelle) {
        Main.debug.println(
                "INVOKED (" + this.hashCode() + ") " + getClass() + " (Weiterleitungstabelle), addManuellenEintrag("
                        + netzwerkziel + "," + netzwerkmaske + "," + gateway + "," + schnittstelle + ")");
        manuelleEintraege = null;

        if (netzwerkziel != null && netzwerkmaske != null && gateway != null && schnittstelle != null) {
            String[] tmpString = { netzwerkziel, netzwerkmaske, gateway, schnittstelle };
            manuelleTabelle.addLast(tmpString);
        }
    }

    /**
     * Hilfsmethode zum Debugging zur Ausgabe der Tabelleneintraege auf der Standardausgabe
     * 
     * @param name
     *            der Name, der in der Tabellenueberschrift ausgegeben werden soll
     * @param tabelle
     *            die auszugebende Tabelle
     */
    public void printTabelle(String name) {
        Main.debug.println("DEBUG (" + name + ") Weiterleitungstabelle (IP,mask,gw,if):");
        for (String[] eintrag : holeTabelle()) {
            Main.debug.printf("DEBUG (%s)  '%15s' | '%15s' | '%15s' | '%15s'\n", name, eintrag[0], eintrag[1],
                    eintrag[2], eintrag[3]);
        }
    }

    /**
     * Zugriff auf die Liste, in der steht, welche Eintraege automatisch erzeugt bzw. manuell erstellt wurden
     */
    public LinkedList<Boolean> holeManuelleEintraegeFlags() {
        return manuelleEintraege;
    }

    /** Zuruecksetzen der Tabelle mit den manuellen Eintraegen */
    public void reset() {
        manuelleTabelle = new LinkedList<String[]>();
        manuelleEintraege = null;
    }

    /**
     * Methode fuer den Zugriff auf die Weiterleitungstabelle bestehend aus automatisch erzeugten und manuellen
     * Eintraegen
     * 
     * @throws InvalidParameterException
     */
    public LinkedList<String[]> holeTabelle() {
        Main.debug
                .println("INVOKED (" + this.hashCode() + ") " + getClass() + " (Weiterleitungstabelle), holeTabelle()");
        InternetKnoten knoten;
        String gateway;
        LinkedList<String[]> tabelle;
        String[] tmp = new String[4];

        tabelle = new LinkedList<String[]>(manuelleTabelle);
        manuelleEintraege = new LinkedList<Boolean>();
        for (int i = 0; i < tabelle.size(); i++) {
            manuelleEintraege.add(Boolean.TRUE);
        }

        if (firmware != null) {
            // Eintrag fuer 'localhost'
            tmp = new String[4];
            IPAddress localhostNetwork = IPAddress.localhostNetwork(SzenarioVerwaltung.getInstance().ipVersion());
            IPAddress localhost = IPAddress.localhost(SzenarioVerwaltung.getInstance().ipVersion());
            tmp[0] = localhostNetwork.address();
            tmp[1] = localhostNetwork.netmask();
            tmp[2] = localhost.address();
            tmp[3] = localhost.address();
            tabelle.addFirst(tmp);
            manuelleEintraege.addFirst(Boolean.FALSE);

            knoten = (InternetKnoten) firmware.getKnoten();

            // Eintrag fuer eigenes Rechnernetz
            for (NetzwerkInterface nic : knoten.getNetzwerkInterfaces()) {
                tmp = new String[4];
                // tmp[0] = nic.getIp();
                try {
                    tmp[0] = nic.addressIP().networkAddress();
                } catch (InvalidParameterException e) {}
                tmp[1] = nic.getSubnetzMaske();
                tmp[2] = nic.getIp();
                tmp[3] = nic.getIp();
                tabelle.addFirst(tmp);
                manuelleEintraege.addFirst(Boolean.FALSE);
            }

            IPAddress unspecifiedAddress = IPAddress.unspecifiedAddress(SzenarioVerwaltung.getInstance().ipVersion());
            // Eintrag fuer eigene IP-Adresse
            for (NetzwerkInterface nic : knoten.getNetzwerkInterfaces()) {
                tmp = new String[4];
                tmp[0] = nic.getIp();
                tmp[1] = unspecifiedAddress.netmask();
                tmp[2] = localhost.address();
                tmp[3] = localhost.address();
                tabelle.addFirst(tmp);
                manuelleEintraege.addFirst(Boolean.FALSE);
            }

            IPAddress defaultRoute = IPAddress.defaultRoute(SzenarioVerwaltung.getInstance().ipVersion());
            // Eintrag fuer Standardgateway, wenn es konfiguriert wurde
            gateway = firmware.getStandardGateway();
            if (gateway != null && !gateway.trim().equals("")) {
                gateway = gateway.trim();
                tmp = null;
                for (NetzwerkInterface nic : knoten.getNetzwerkInterfaces()) {
                    if (nic != null
                            && VermittlungsProtokoll.gleichesRechnernetz(gateway, nic.getIp(), nic.getSubnetzMaske())) {
                        tmp = new String[4];
                        tmp[0] = defaultRoute.address();
                        tmp[1] = defaultRoute.netmask();
                        tmp[2] = gateway;
                        tmp[3] = nic.getIp();
                    }
                }
                if (tmp == null) {
                    tmp = new String[4];
                    tmp[0] = defaultRoute.address();
                    tmp[1] = defaultRoute.netmask();
                    tmp[2] = gateway;
                    tmp[3] = firmware.holeIPAdresse();
                }
                tabelle.addLast(tmp);
                manuelleEintraege.addLast(Boolean.FALSE);
            }
        }
        return tabelle;
    }

    public Route holeWeiterleitungsEintrag(String zielIpAdresse) throws RouteNotFoundException {
        Route bestRoute = null;
        if (firmware.isRipEnabled()) {
            bestRoute = determineRouteFromDynamicRoutingTable(zielIpAdresse);
        } else {
            bestRoute = determineRouteFromStaticRoutingTable(zielIpAdresse);
        }
        return bestRoute;
    }

    public Route determineRouteFromStaticRoutingTable(String targetIPAddress) throws RouteNotFoundException {
        long bestMaskLength = -1;
        Route bestRoute = null;
        try {
            for (String[] route : holeTabelle()) {
                IPAddress routeTarget = new IPAddress(route[0], route[1]);
                if (routeTarget.netmaskLength() <= bestMaskLength) {
                    continue;
                }
                if (routeTarget.equalNetwork(targetIPAddress)) {
                    bestMaskLength = routeTarget.netmaskLength();
                    bestRoute = new Route(route);
                }
            }
        } catch (InvalidParameterException e) {}
        if (bestRoute != null) {
            return bestRoute;
        } else {
            throw new RouteNotFoundException();
        }
    }

    public Route determineRouteFromDynamicRoutingTable(String ip) throws RouteNotFoundException {
        RIPTable table = firmware.getRIPTable();
        Route bestRoute = null;
        synchronized (table) {
            int bestHops = RIPTable.INFINITY - 1;
            long bestMaskLength = -1;

            for (RIPRoute route : table.routes) {
                try {
                    IPAddress ipAddress = new IPAddress(ip, route.getNetMask());
                    if (route.getNetAddress().equals(ipAddress.networkAddress())) {
                        if (bestHops < route.hops) {
                            continue;
                        }
                        if (bestHops > route.hops || bestMaskLength < ipAddress.netmaskLength()) {
                            bestRoute = route;
                            bestHops = route.hops;
                            bestMaskLength = ipAddress.netmaskLength();
                        }
                    }
                } catch (InvalidParameterException e) {}
            }
        }
        if (bestRoute != null) {
            return bestRoute;
        } else {
            throw new RouteNotFoundException();
        }
    }
}
