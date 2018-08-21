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
package filius.software.rip;

import java.util.LinkedList;

import filius.hardware.NetzwerkInterface;
import filius.hardware.knoten.InternetKnoten;
import filius.software.system.InternetKnotenBetriebssystem;
import filius.software.vermittlungsschicht.IP;

/**
 * 
 * @author pyropeter
 * @author stefanf
 * 
 */
public class RIPTable {
	public static final int INFINITY = 16;
	/**
	 * refresh interval in millis. the refresh is used to update the routing
	 * table in case of changes of the network configuration. the only change
	 * that can occur during simulation mode is because of firewall
	 * re-configuration.
	 */
	public static final int INTERVAL = 1000 * 30;
	public static final int TIMEOUT = INTERVAL * 5 / 2;

	public LinkedList<RIPRoute> routes;

	private long nextBeacon;

	private InternetKnotenBetriebssystem bs;

	public RIPTable(InternetKnotenBetriebssystem bs) {
		this.bs = bs;
		reset();
	}

	public void reset() {
		this.routes = new LinkedList<RIPRoute>();
		this.setNextBeacon(RIPUtil.getTime() + 1000);
	}

	public void addRoute(RIPRoute route) {
		routes.add(route);
	}

	public void addLocalRoutes(InternetKnoten knoten) {
		long netMask, netAddr;

		for (NetzwerkInterface nic : knoten.getNetzwerkInterfaces()) {
			netMask = IP.inetAton(nic.getSubnetzMaske());
			netAddr = IP.inetAton(nic.getIp()) & netMask;

			addRoute(new RIPRoute(0, IP.inetNtoa(netAddr), IP.inetNtoa(netMask), nic.getIp(), bs.holeIPAdresse(),
			        nic.getIp(), 0));
		}
	}

	public RIPRoute search(String net, String mask) {
		for (RIPRoute route : routes) {
			if (route.getNetAddress().equals(net) && route.getNetMask().equals(mask)) {
				return route;
			}
		}
		return null;
	}

	public void check() {
		for (RIPRoute route : routes) {
			if (route.isExpired()) {
				route.hops = INFINITY;
			}
		}
	}

	public long getNextBeacon() {
		return nextBeacon;
	}

	public void setNextBeacon(long nextBeacon) {
		this.nextBeacon = nextBeacon;
	}
}
