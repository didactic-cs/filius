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
package filius.software.nat;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

public class PortProtocolPair {
    private int port;
    private int protocol;
    private String address;

    public PortProtocolPair(int port, String address, int protocol) {
        super();
        this.port = port;
        this.address = address;
        this.protocol = protocol;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public boolean equals(Object other) {
    	boolean result = true;
        if (null == other) {
            result = false;
        } else if (!(other instanceof PortProtocolPair)) {
            result = false;
        } else if (port != ((PortProtocolPair) other).port) {
            return false;
        } else if (!StringUtils.equals(address, ((PortProtocolPair) other).address)) {
            result = false;
        } else if (protocol != ((PortProtocolPair) other).protocol) {
        	result = false;
        }
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(port,address,protocol);
    }

    @Override
    public String toString() {
        return "prot=" + protocol + " / " + address +":" + port;
    }
}
