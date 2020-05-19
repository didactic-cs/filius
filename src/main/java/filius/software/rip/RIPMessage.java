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

import filius.software.vermittlungsschicht.IPAddress;

/**
 * 
 * @author pyropeter
 *
 */
public class RIPMessage {
    public String ip;
    public String publicIp;
    public int infinity;
    public int timeout;

    public LinkedList<RIPMessageRoute> routes;

    public RIPMessage(String ip, String publicIp, int infinity, int timeout) {
        this.ip = ip;
        this.publicIp = publicIp;
        this.infinity = infinity;
        this.timeout = timeout;

        routes = new LinkedList<RIPMessageRoute>();
    }

    public RIPMessage(String msg) throws IllegalArgumentException {
        String[] lines = msg.split("\n");

        try {
            ip = IPAddress.verifyAddress(lines[0]) ? lines[0] : null;
            publicIp = IPAddress.verifyAddress(lines[1]) ? lines[1] : null;
            infinity = Integer.parseInt(lines[2]);
            timeout = Integer.parseInt(lines[3]);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException();
        }

        if (ip == null || publicIp == null || timeout <= 0 || infinity <= 0) {
            throw new IllegalArgumentException();
        }

        routes = new LinkedList<RIPMessageRoute>();

        for (int i = 4; i < lines.length; i++) {
            routes.add(new RIPMessageRoute(lines[i]));
        }
    }

    public String toString() {
        String res = "";
        res += ip + "\n";
        res += publicIp + "\n";
        res += infinity + "\n";
        res += timeout;

        for (RIPMessageRoute route : routes) {
            res += "\n" + route.toString();
        }

        return res;
    }

    public void addRoute(RIPMessageRoute newRoute) {
        for (RIPMessageRoute route : routes) {
            if (route.ip.equals(newRoute.ip) && route.mask.equals(newRoute.mask)) {
                if (newRoute.hops < route.hops) {
                    route.hops = newRoute.hops;
                }
                return;
            }
        }
        routes.add(newRoute);
    }
}
