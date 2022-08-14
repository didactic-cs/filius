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
package filius.simul.soft.clientserver.dns;

import java.util.LinkedList;

public class DNSCache {
	
	// Only store type A records
	private LinkedList<DNSRecord> records = new LinkedList<DNSRecord>();
	
	
	public void add(String name, String rdata) {
		
		// Add only once
		if (getIpAddress(name) != null)  return;
		
		records.add(new DNSRecord(name.toLowerCase(), DNSRecord.TYPE_A, rdata));
	}

	public void clear() {
	
		records.clear();
	}
	
	public String getIpAddress(String name) {
		
		name = name.toLowerCase();
		for (DNSRecord record : records) {
            if (record.getDomainname().equals(name)) {
                return record.getRdata();
            }
        }
		return null;
	}	
	
	public LinkedList<DNSRecord> getRecords() {
		
		return records;
	}
}
