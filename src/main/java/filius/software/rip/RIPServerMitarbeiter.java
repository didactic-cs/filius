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

import filius.hardware.NetzwerkInterface;
import filius.hardware.knoten.InternetKnoten;
import filius.software.clientserver.ServerMitarbeiter;
import filius.software.system.VermittlungsrechnerBetriebssystem;
import filius.software.transportschicht.Socket;
import filius.software.vermittlungsschicht.IP;

/**
 * 
 * @author pyropeter
 * @author stefanf
 * 
 */
public class RIPServerMitarbeiter extends ServerMitarbeiter {
    private RIPTable table;
    private VermittlungsrechnerBetriebssystem bs;
    private InternetKnoten knoten;

    public RIPServerMitarbeiter(RIPServer server, Socket socket) {
        super(server, socket);
        bs = (VermittlungsrechnerBetriebssystem) server.getSystemSoftware();
        knoten = (InternetKnoten) bs.getKnoten();
        table = bs.getRIPTable();
    }

    protected void verarbeiteNachricht(String nachricht) {
        RIPMessage msg;
        try {
            msg = new RIPMessage(nachricht);
        } catch (IllegalArgumentException e) {
            return;
        }

        // find the interface that (probably) received the message
        String nicIp = findeInterfaceIp(msg.ip);
        if (nicIp == null || nicIp.equals(msg.ip)) {
            return;
        }

        RIPRoute route;
        int hops;
        synchronized (table) {
            for (RIPMessageRoute entry : msg.routes) {
                if (entry.hops >= msg.infinity || entry.hops + 1 >= RIPTable.INFINITY) {
                    hops = RIPTable.INFINITY;
                } else {
                    hops = entry.hops + 1;
                }

                route = table.search(entry.ip, entry.mask);
                if (route != null) {
                    // route exists, just update
                    if (!route.getGateway().equals(msg.ip) && route.hops <= hops) {
                        continue;
                    }
                    if (route.hops > hops) {
                        // found a shorter route
                        route.setGateway(msg.ip);
                        route.hopPublicIp = msg.publicIp;
                        route.setInterfaceIpAddress(nicIp);
                    } else if (route.hops < hops) {
                        // the old route just got worse. this has to be
                        // flushed to other routers immediately
                        table.setNextBeacon(0);
                    }
                    route.hops = hops;
                    route.refresh(msg.timeout);
                } else {
                    // route is unknown, create it
                    if (hops < RIPTable.INFINITY) {
                        route = new RIPRoute(msg.timeout, entry.ip, entry.mask, msg.ip, msg.publicIp, nicIp, hops);
                        table.addRoute(route);
                        table.setNextBeacon(0);
                    }
                }
            }
            table.notifyAll();
        }
    }

    String findeInterfaceIp(String ipStr) {
        long ip = IP.inetAToN(ipStr);

        for (NetzwerkInterface nic : knoten.getNetzwerkInterfaces()) {
            long netMask = IP.inetAToN(nic.getSubnetzMaske());
            long netAddr = IP.inetAToN(nic.getIp()) & netMask;

            if ((ip & netMask) == netAddr) {
                return nic.getIp();
            }
        }
        return null;
    }
}
