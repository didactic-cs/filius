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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkAddressTranslationTable {
    private static Logger LOG = LoggerFactory.getLogger(NetworkAddressTranslationTable.class);

    private Map<PortProtocolPair, InetAddress> dynamicNATTable = new HashMap<>();
    private Map<PortProtocolPair, InetAddress> staticNATTable = new HashMap<>();

    public Map<PortProtocolPair, InetAddress> getStaticNATTable() {
        return Collections.unmodifiableMap(staticNATTable);
    }

    public void setStaticNATTable(Map<PortProtocolPair, InetAddress> staticNATTable) {
        this.staticNATTable = new HashMap<PortProtocolPair, InetAddress>(staticNATTable);
    }

    void addDynamic(int port, int protocol, InetAddress address) {
        dynamicNATTable.put(new PortProtocolPair(port, protocol), address);
    }

    public InetAddress find(int port, int protocol) {
        PortProtocolPair lookup = new PortProtocolPair(port, protocol);
        return dynamicNATTable.get(lookup);
    }

    public boolean hasConnection(InetAddress lanAddress) {
        boolean exists = false;
        for (PortProtocolPair key : dynamicNATTable.keySet()) {
            if (dynamicNATTable.get(key).equals(lanAddress)) {
                exists = true;
                break;
            }
        }
        return exists;
    }

    public int findPort(InetAddress lanAddress) {
        int port = 0;
        for (PortProtocolPair key : dynamicNATTable.keySet()) {
            if (dynamicNATTable.get(key).equals(lanAddress)) {
                port = key.getPort();
                break;
            }
        }
        return port;
    }

    public void print() {
        for (Entry<PortProtocolPair, InetAddress> entry : dynamicNATTable.entrySet()) {
            LOG.debug("{} -> {}", entry.getKey(), entry.getValue());
        }
    }
}
