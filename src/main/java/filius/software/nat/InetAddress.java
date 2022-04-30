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

public class InetAddress {
    private String ipAddress;
    private int port;
    private int protocol;

    public InetAddress(String ipAddress, int port, int protocol) {
        super();
        this.ipAddress = ipAddress;
        this.port = port;
        this.protocol = protocol;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
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

    @Override
    public boolean equals(Object other) {
        boolean result = true;
        if (null == other) {
            result = false;
        } else if (!(other instanceof InetAddress)) {
            result = false;
        } else if (!StringUtils.equals(ipAddress, ((InetAddress) other).ipAddress)) {
            return false;
        } else if (port != ((InetAddress) other).port) {
            return false;
        } else if (protocol != ((InetAddress) other).protocol) {
            return false;
        }
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ipAddress, port, protocol);
    }
}
