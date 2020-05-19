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

import filius.exception.InvalidParameterException;
import filius.software.vermittlungsschicht.IPAddress;
import filius.software.vermittlungsschicht.Route;

/**
 * 
 * @author pyropeter
 * @author stefanf
 * 
 */
public class RIPRoute extends Route {
    public long expires;
    public long created;
    public int hops;

    public String hopPublicIp; // hint for system administrator

    public RIPRoute(int timeout, String netAddr, String netMask, String nextHop, String hopPublicIp, String nic,
            int hops) {
        super(netAddr, netMask, nextHop, nic);
        this.created = RIPUtil.getTime();
        refresh(timeout);

        this.hopPublicIp = hopPublicIp;
        this.hops = hops;
    }

    public void refresh(int timeout) {
        if (timeout > 0) {
            this.expires = RIPUtil.getTime() + timeout;
        } else {
            this.expires = 0;
        }
    }

    public boolean isExpired() {
        return (this.expires > 0) && (this.expires < RIPUtil.getTime());
    }

    public void setGateway(String gateway) {
        try {
            this.gatewayAddress = new IPAddress(gateway);
        } catch (InvalidParameterException e) {}
    }

    public void setInterfaceIpAddress(String interfaceIpAddress) {
        try {
            this.localInterface = new IPAddress(interfaceIpAddress);
        } catch (InvalidParameterException e) {}
    }
}
