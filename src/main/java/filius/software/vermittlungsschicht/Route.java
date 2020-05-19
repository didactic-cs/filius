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

import filius.exception.InvalidParameterException;

/**
 * @author stefan
 * 
 */
public class Route {

    private IPAddress destNetwork;
    protected IPAddress gatewayAddress;
    protected IPAddress localInterface;

    public String getNetAddress() {
        return destNetwork.networkAddress();
    }

    public String getNetMask() {
        return destNetwork.netmask();
    }

    public String getGateway() {
        return gatewayAddress.address();
    }

    public String getInterfaceIpAddress() {
        return localInterface.address();
    }

    public Route(String netAddress, String netMask, String gateway, String interfaceIpAddress) {
        try {
            this.destNetwork = new IPAddress(netAddress, netMask);
            this.gatewayAddress = new IPAddress(gateway);
            this.localInterface = new IPAddress(interfaceIpAddress);
        } catch (InvalidParameterException e) {}
    }

    public Route(String[] routingInfo) {
        if (routingInfo.length == 4) {
            try {
                this.destNetwork = new IPAddress(routingInfo[0], routingInfo[1]);
                this.gatewayAddress = new IPAddress(routingInfo[2]);
                this.localInterface = new IPAddress(routingInfo[3]);
            } catch (InvalidParameterException e) {}
        } else if (routingInfo.length == 2) {
            try {
                this.gatewayAddress = new IPAddress(routingInfo[0]);
                this.localInterface = new IPAddress(routingInfo[1]);
            } catch (InvalidParameterException e) {}
        }
    }
}
