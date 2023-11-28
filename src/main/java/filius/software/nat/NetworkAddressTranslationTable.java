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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkAddressTranslationTable {
    private static Logger LOG = LoggerFactory.getLogger(NetworkAddressTranslationTable.class);

    private ConcurrentHashMap<PortProtocolPair, NatEntry> dynamicNATTable = new ConcurrentHashMap<>();
    private Map<PortProtocolPair, NatEntry> staticNATTable = new HashMap<>();
    private NatMethod natMethod = NatMethod.fullCone;

    public Map<PortProtocolPair, NatEntry> getStaticNATTable() {
        return Collections.unmodifiableMap(staticNATTable);
    }

    public ConcurrentHashMap<PortProtocolPair, NatEntry> getDynamicNATTable() {
        return dynamicNATTable;
    }

    public void setStaticNATTable(Map<PortProtocolPair, NatEntry> staticNATTable) {
        this.staticNATTable = new HashMap<PortProtocolPair, NatEntry>(staticNATTable);
    }
    
    public void resetStaticNATTable() {
    	this.staticNATTable.clear();
    }

    void addDynamic(int port, String wanAddress, int protocol, InetAddress lanAddress, NatType natType) {
    	if (lanAddress != null) {
    		NatEntry entry = new NatEntry();
    		entry.setInetAddress(lanAddress);
    		entry.setNatType(natType);
    		entry.setLastUpdate(new Date());
    		dynamicNATTable.put(new PortProtocolPair(port, (natMethod == NatMethod.restrictedCone?wanAddress:""), protocol), entry);
        }
    }

    public void addStatic(int port, int protocol, InetAddress lanAddress) {
    	if (lanAddress != null) {
    		NatEntry entry = new NatEntry();
    		entry.setInetAddress(lanAddress);
    		entry.setNatType(NatType.StaticEntry);
    		staticNATTable.put(new PortProtocolPair(port, "", protocol), entry);
    	}
    }
    
    public NatMethod getNatMethod() {
    	return natMethod;
    }
    
    public void setNatMethod(NatMethod method) {
    	this.natMethod = method;
    }

    public InetAddress find(int port, String address, int protocol) {
        PortProtocolPair lookup = new PortProtocolPair(port,(natMethod == NatMethod.restrictedCone?address:""), protocol);
        NatEntry natEntry = dynamicNATTable.get(lookup);
        InetAddress IpAddress = null;
        if (natEntry == null) {
        	lookup = new PortProtocolPair(port, "", protocol);
            natEntry = staticNATTable.get(lookup);
        	if (natEntry!= null) {
            	IpAddress = natEntry.getInetAddress();
            	addDynamic(port,address, protocol, IpAddress, NatType.DynamicEnryFromStatic);
        	}
        } else {
        	natEntry.setLastUpdate(new Date());
    		IpAddress = natEntry.getInetAddress();
    	}
        return IpAddress;
    }

    public boolean hasConnection(InetAddress lanAddress) {
        boolean exists = false;
        for (PortProtocolPair key : dynamicNATTable.keySet()) {
            if (natMethod == NatMethod.restrictedCone?dynamicNATTable.get(key).getInetAddress().equals(lanAddress):dynamicNATTable.get(key).getInetAddress().equalsWOIp(lanAddress)) {
                exists = true;
                break;
            }
        }
        if (!exists) {
        	for (PortProtocolPair key : staticNATTable.keySet()) {
            	if (staticNATTable.get(key).getInetAddress().equalsWOIp(lanAddress)) {
                	exists = true;
                	break;
            	}
        	}
        }
        return exists;
    }

    public int findPort(InetAddress lanAddress) {
        int port = 0;
        for (PortProtocolPair key : dynamicNATTable.keySet()) {
            if (natMethod == NatMethod.restrictedCone?dynamicNATTable.get(key).getInetAddress().equals(lanAddress):dynamicNATTable.get(key).getInetAddress().equalsWOIp(lanAddress)) {
                port = key.getPort();
                break;
            }
        }
        if (port == 0) {
        	for (PortProtocolPair key : staticNATTable.keySet()) {
        		if (staticNATTable.get(key).getInetAddress().equalsWOIp(lanAddress)) {
        			port = key.getPort();
        			break;
        		}
        	}
        }
        return port;
    }

    public void print() {
        for (Entry<PortProtocolPair, NatEntry> natEntry : staticNATTable.entrySet()) {
            LOG.debug("{} -> {}", natEntry.getKey(), natEntry.getValue());
        }
        for (Entry<PortProtocolPair, NatEntry> natEntry : dynamicNATTable.entrySet()) {
            LOG.debug("{} -> {}", natEntry.getKey(), natEntry.getValue());
        }
    }
}
