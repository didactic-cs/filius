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

import filius.software.vermittlungsschicht.IPAddress;

/**
 * 
 * @author pyropeter
 *
 */
public class RIPMessageRoute {
    public String ip;
    public String mask;
    public int hops;

    public RIPMessageRoute(String ip, String mask, int hops) {
        this.ip = ip;
        this.mask = mask;
        this.hops = hops;
    }

    public RIPMessageRoute(String msg) throws IllegalArgumentException {
        String[] fields = msg.split(" ");
        try {
            ip = IPAddress.verifyAddress(fields[0]) ? fields[0] : null;
            mask = IPAddress.verifyAddress(fields[1]) ? fields[1] : null;
            hops = Integer.parseInt(fields[2]);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException();
        }

        if (ip == null || mask == null || hops < 0) {
            throw new IllegalArgumentException();
        }
    }

    public String toString() {
        return ip + " " + mask + " " + hops;
    }
}
