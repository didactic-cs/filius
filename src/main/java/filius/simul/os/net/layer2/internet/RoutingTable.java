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
import java.util.List;

import filius.Main;
import filius.hardware.NetworkInterface;
import filius.hardware.knoten.InternetNode;
import filius.rahmenprogramm.I18n;
import filius.software.rip.RIPRoute;
import filius.software.rip.RIPTable;
import filius.software.system.InternetNodeOS;

/**
 * Mit dieser Klasse wird die Weiterleitungstabelle implementiert. Es werden
 * manuell erstellte Eintraege und aus der IP-Konfiguration der Netzwerkkarten
 * des Knotens automatisch erzeugte Eintraege unterschieden. Gespeichert werden
 * nur manuelle Eintraege. Ausserdem werden bei jeder Abfrage die automatischen
 * Standard-Eintraege erzeugt.
 */
public class RoutingTable implements I18n {

	/**
	 * Die Tabelle mit den manuellen Eintraegen der Weiterleitungstabelle. Sie
	 * werden als String-Arrays in einer Liste verwaltet. Ein Eintrag besteht
	 * aus folgenden Elementen:
	 * <ol>
	 * <li>Netz-ID der Zieladresse als IP-Adresse</li>
	 * <li>Netzmaske zur Berechnung der Netz-ID aus dem ersten Wert</li>
	 * <li>Das Standard-Gateway, ueber die die Ziel-IP-Adresse erreicht wird,
	 * wenn sie sich nicht im gleichen Rechnernetz wie der eigene Rechner
	 * befindet</li>
	 * <li>Die IP-Adresse der Netzwerkkarte, ueber die die Ziel-IP-Adresse
	 * erreicht wird</li>
	 * </ol>
	 */
	private LinkedList<String[]> manualTable;

	/**
	 * Eine Liste, in der angegeben wird, welche Eintraege in der erzeugten
	 * Tabelle automatisch erzeugt bzw. manuelle Eintraege sind
	 */
	private LinkedList<Boolean> manualEntries;

	/** Die Systemsoftware */
	private InternetNodeOS firmware = null;

	/**
	 * Im Standard-Konstruktor wird die Methode reset() aufgerufen. Damit werden
	 * alle manuellen Eintraege geloescht
	 */
	public RoutingTable() {
		reset();
	}

	/** Methode fuer den Zugriff auf die Systemsoftware */
	public void setInternetNodeOS(InternetNodeOS firmware) {
		this.firmware = firmware;
	}

	/** Methode fuer den Zugriff auf die Systemsoftware */
	public InternetNodeOS getInternetNodeOS() {
		return firmware;
	}

	/**
	 * Methode fuer den Zugriff auf die manuellen Eintrage. Diese Methode sollte
	 * nur fuer das speichern genutzt werden!
	 */
	public void setManualTable(LinkedList<String[]> tabelle) {
		this.manualTable = tabelle;
	}

	/**
	 * Methode fuer den Zugriff auf die manuellen Eintrage. Diese Methode sollte
	 * nur fuer das speichern genutzt werden!
	 */
	public LinkedList<String[]> getManualTable() {
		return manualTable;
	}

	/**
	 * Methode zum hinzufuegen eines neuen Eintrags. Ein Eintrag besteht aus
	 * folgenden Elementen:
	 * <ol>
	 * <li>Netz-ID der Zieladresse als IP-Adresse</li>
	 * <li>Netzmaske zur Berechnung der Netz-ID aus dem ersten Wert</li>
	 * <li>Das Standard-Gateway, ueber die die Ziel-IP-Adresse erreicht wird,
	 * wenn sie sich nicht im gleichen Rechnernetz wie der eigene Rechner
	 * befindet</li>
	 * <li>Die IP-Adresse der Netzwerkkarte, ueber die die Ziel-IP-Adresse
	 * erreicht wird</li>
	 * </ol>
	 * 
	 * @param netzwerkziel
	 * @param netzwerkmaske
	 * @param gateway
	 * @param schnittstelle
	 */
	public void addManualEntry(String netzwerkziel, String netzwerkmaske, String gateway, String schnittstelle) {
		Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + 
				           " (Weiterleitungstabelle), addManuellenEintrag(" + netzwerkziel + "," + netzwerkmaske + "," + 
				           gateway + "," + schnittstelle + ")");
		
		manualEntries = null;

		if (netzwerkziel != null && netzwerkmaske != null && gateway != null && schnittstelle != null) {
			String[] tmpString = { netzwerkziel, netzwerkmaske, gateway, schnittstelle };
			manualTable.addLast(tmpString);
		}
	}
	
	/**
	 * <b>setManualEntry</b> updates a manual entry.
	 * 
	 * @param targetIP
	 * @param targetMask
	 * @param gateway
	 * @param outputIP
	 */
	public void setManualEntry(int index, String targetIP, String targetMask, String gateway, String outputIP) {

		if (targetIP != null && targetMask != null && gateway != null && outputIP != null) {
			String[] route = { targetIP, targetMask, gateway, outputIP };	
			manualTable.set(index, route);
		}
	}
	
	/**
	 * <b>removeManualEntry</b> removes a manual entry.
	 * 
	 * @param targetIP
	 * @param targetMask
	 * @param gateway
	 * @param outputIP
	 */
	public void removeManualEntry(int index) {

			manualTable.remove(index) ;
	}

	/**
	 * Hilfsmethode zum Debugging zur Ausgabe der Tabelleneintraege auf der
	 * Standardausgabe
	 * 
	 * @param name
	 *            der Name, der in der Tabellenueberschrift ausgegeben werden
	 *            soll
	 * @param tabelle
	 *            die auszugebende Tabelle
	 */
	public void printTable(String name) {
		Main.debug.println("DEBUG (" + name + ") Weiterleitungstabelle (IP,mask,gw,if):");
		for (String[] eintrag : getRouteList()) {
			Main.debug.printf("DEBUG (%s)  '%15s' | '%15s' | '%15s' | '%15s'\n", name, eintrag[0], eintrag[1], eintrag[2], eintrag[3]);
		}
	}

	/**
	 * Zugriff auf die Liste, in der steht, welche Eintraege automatisch erzeugt
	 * bzw. manuell erstellt wurden
	 */
	public LinkedList<Boolean> getManualEntriesFlags() {
		return manualEntries;
	}

	/** Zuruecksetzen der Tabelle mit den manuellen Eintraegen */
	public void reset() {
		manualTable = new LinkedList<String[]>();
		manualEntries = null;
	}

	/**
	 * Methode fuer den Zugriff auf die Weiterleitungstabelle bestehend aus
	 * automatisch erzeugten und manuellen Eintraegen
	 */
	public LinkedList<String[]> getRouteList() {
		Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (Weiterleitungstabelle), getList()");
		
		// Routes defined by the user
		LinkedList<String[]> list = new LinkedList<String[]>(manualTable);		
		manualEntries = new LinkedList<Boolean>();
		
		for (int i = 0; i < list.size(); i++) manualEntries.add(true);

		if (firmware != null) {
			
			String[] route;
			int index = 0;
			
			List<NetworkInterface> nicList = ((InternetNode) firmware.getNode()).getNICList();

			// Unique entry added first for the default route, when the standard gateway is configured
			String gateway = firmware.getStandardGateway();
			if (gateway != null && !gateway.trim().equals("")) {
				gateway = gateway.trim();

				// Determine the output IP
				String outputIP = "";				
				for (NetworkInterface nic : nicList) {
					if (nic != null && VermittlungsProtokoll.gleichesRechnernetz(gateway, nic.getIp(), nic.getSubnetMask())) {
						outputIP = nic.getIp();
						break;
					}
				}
				if (outputIP.isEmpty()) outputIP = firmware.getIPAddress();

				route = new String[] {"0.0.0.0", "0.0.0.0", gateway, outputIP};
				list.add(index, route);
				manualEntries.add(index, false);
				index++;
			}

			// Entry for 'localhost'
			route = new String[] {"127.0.0.0", "255.0.0.0", "127.0.0.1", "127.0.0.1"};
			list.add(index, route);
			manualEntries.add(index, false);
			index++;
			
			// Entries for own IP address
			for (NetworkInterface nic : nicList) {
				route = new String[] {nic.getIp(), "255.255.255.255", "127.0.0.1", "127.0.0.1"};
				list.add(index, route);
				manualEntries.add(index, false);
				index++;
			}	

			// Entries for own networks
			for (NetworkInterface nic : nicList) {				
				route = new String[] {computeNetworkID(nic.getIp(), nic.getSubnetMask()), nic.getSubnetMask(), nic.getIp(), nic.getIp()};
				list.add(index, route);
				manualEntries.add(index, false);
				index++;
			}
		}

		return list;
	}
	
	/**
	 * <b>getAutoRouteCount</b> returns the number of automatic routes returned by getRouteList()
	 *  
	 */
	public int getAutoRouteCount() {
		
		int count = 0;

		if (firmware != null) {
			
		
			// Unique entry added first for the default route, when the standard gateway is configured
			String gateway = firmware.getStandardGateway();
			if (gateway != null && !gateway.trim().equals("")) count++;

			// Entry for 'localhost'
			count++;

			// Entries for own networks + entries for own IP address
			List<NetworkInterface> nicList = ((InternetNode) firmware.getNode()).getNICList();
			count += 2*nicList.size();	
		}

		return count;
	}

	/**
	 * Methode, um aus einer IP-Adresse und einer Subnetzmaske eine
	 * Netzwerkkennung als String zu erzeugen. Bsp.: 192.168.2.6 und
	 * 255.255.255.0 wird zu 192.168.2.0
	 */
	private String computeNetworkID(String ipStr, String maskStr) {
		long ipAddr = IP.inetAton(ipStr);
		long maskAddr = IP.inetAton(maskStr);
		long netAddr = ipAddr & maskAddr;
		return IP.inetNtoa(netAddr);
	}

//	@Deprecated
//	public String[] holeWeiterleitungsZiele(String targetIPAddress) throws RouteNotFoundException {
//		
//		Route bestRoute = null;
//		if (firmware.isRIPEnabled()) {
//			bestRoute = determineRouteFromDynamicRoutingTable(targetIPAddress);
//		} else {
//			bestRoute = determineRouteFromStaticRoutingTable(targetIPAddress);
//		}
//		return new String[] { bestRoute.getGateway(), bestRoute.getInterfaceIpAddress() };
//	}

	/**
	 * Tabelle zur Abfrage der Weiterleitungstabelle nach einem passenden
	 * Eintrag fuer eine Ziel-IP-Adresse
	 * 
	 * @param targetIPAddress
	 *            die Ziel-IP-Adresse
	 * @return das Ergebnis als Route
	 * @throws RouteNotFoundException
	 */
	public Route getBestRoute(String targetIPAddress) throws RouteNotFoundException {
		
		Route bestRoute = null;
		if (firmware.isRIPEnabled()) {
			bestRoute = determineRouteFromDynamicRoutingTable(targetIPAddress);
		} else {
			bestRoute = determineRouteFromStaticRoutingTable(targetIPAddress);
		}
		return bestRoute;
	}

	public Route determineRouteFromStaticRoutingTable(String targetIPAddress) throws RouteNotFoundException {
		
		long netAddr, maskAddr, zielAddr = IP.inetAton(targetIPAddress);

		long bestMask = -1;
		Route bestRoute = null;

		for (String[] route : getRouteList()) {
			maskAddr = IP.inetAton(route[1]);
			if (maskAddr <= bestMask) {
				continue;
			}
			netAddr = IP.inetAton(route[0]);
			if (netAddr == (maskAddr & zielAddr)) {
				bestMask = maskAddr;
				bestRoute = new Route(route);
			}
		}
		if (bestRoute != null) {
			return bestRoute;
		} else {
			throw new RouteNotFoundException();
		}
	}

	public Route determineRouteFromDynamicRoutingTable(String ip) throws RouteNotFoundException {
		RIPTable ripTable = firmware.getRIPTable();
		Route bestRoute = null;
		synchronized (ripTable) {
			int bestHops = RIPTable.INFINITY - 1;
			long bestMask = -1;

			for (RIPRoute route : ripTable.routes) {
				if (route.getNetAddress().equals(computeNetworkID(ip, route.getNetMask()))) {
					if (bestHops < route.hops) {
						continue;
					}
					if (bestHops > route.hops || bestMask < IP.inetAton(route.getNetMask())) {
						bestRoute = route;
						bestHops = route.hops;
						bestMask = IP.inetAton(route.getNetMask());
					}
				}
			}
		}
		if (bestRoute != null) {
			return bestRoute;
		} else {
			throw new RouteNotFoundException();
		}
	}
}
