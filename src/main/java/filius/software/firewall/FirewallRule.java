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
package filius.software.firewall;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import filius.hardware.NetzwerkInterface;
import filius.rahmenprogramm.I18n;
import filius.software.vermittlungsschicht.VermittlungsProtokoll;

public class FirewallRule implements I18n {

	public static String SAME_NETWORK = "999.999.999.999";   	// fake "IP" address to represent "any IP from same network"
																// NOTE: netmask is irrelevant for this "special IP"

	// see /etc/protocols
	public static final short ALL_PROTOCOLS = -1;  // do not limit to specific protocol
	public static final short ICMP = 1;
	public static final short TCP = 6;
	public static final short UDP = 17;
	
	// ports; see /etc/services
	public static final int ALL_PORTS = -1; 
	
	// action, if rules matches
	public static final short DROP = 0;
	public static final short ACCEPT = 1;
	
	// attributes; direct access tolerable... ;-)
	public String srcIP = "";
	public String srcMask = "";
	public String destIP = "";
	public String destMask = "";
	public int port = FirewallRule.ALL_PORTS;
	public short protocol = FirewallRule.TCP;
	public short action = FirewallRule.DROP;

	// constructor
	public FirewallRule(String srcIP, String srcMask, String destIP, String destMask, int port, short protocol,short action) {
		this.srcIP = srcIP;
		this.srcMask = srcMask;
		this.destIP = destIP;
		this.destMask = destMask;
		this.port = port;
		this.protocol = protocol;
		this.action = action;
	}
	
	public FirewallRule() {		
	}
	
	/*
	 * @param id	ID is considered to be started from index 1, 
	 * 				i.e., parameter shown in table not internal representation of rule ID!
	 */
	public Vector<String> getVector(int id) {
		Vector<String> resultVec = new Vector<String>();
		resultVec.addElement(Integer.toString(id));
		resultVec.addElement(srcIP);
		resultVec.addElement(srcMask);
		resultVec.addElement(destIP);
		resultVec.addElement(destMask);
		if(protocol == FirewallRule.TCP)
			resultVec.addElement("TCP");
		else if(protocol == FirewallRule.UDP)
			resultVec.addElement("UDP");
		else if(protocol == FirewallRule.ICMP)
			resultVec.addElement("ICMP");
		else resultVec.addElement("");  // = alle
		if(port >= 0)
			resultVec.addElement(Integer.toString(port));
		else
			resultVec.addElement("");
		if(action == FirewallRule.ACCEPT)
			resultVec.addElement(messages.getString("jfirewalldialog_msg33"));
		else if(action == FirewallRule.DROP)
			resultVec.addElement(messages.getString("jfirewalldialog_msg34"));
		else resultVec.addElement("");
		return resultVec;
	}
	
	/*
	 * Returns vector to be inserted as row in Personal Firewall table
	 */
	public Vector<String> getVectorPFW() {
		Vector<String> resultVec = new Vector<String>();
		if(port >= 0)
			resultVec.addElement(Integer.toString(port));
		else
			resultVec.addElement("");
		if(srcIP.equals(FirewallRule.SAME_NETWORK))
			resultVec.addElement(messages.getString("firewall_msg12"));
		else
			resultVec.addElement(messages.getString("firewall_msg4"));
		return resultVec;
	}
	
	public String toString() {
		return toString(null);
	}
	public String toString(List<NetzwerkInterface> nics) {
		String ip = null;
		String mask = null;
		String result = "";
		boolean sameNet = false;
		
		if(nics != null) {
			ip = nics.get(0).getIp();
			mask = nics.get(0).getSubnetzMaske();
			if(srcIP.equals(FirewallRule.SAME_NETWORK) && ip != null && mask != null)
				sameNet = true;
		}
		
		if(srcIP.isEmpty())
			result += "*/";
		else if(sameNet)
			result += VermittlungsProtokoll.getSubnetForIp(ip, mask)+"/"+mask+" -> ";
		else
			result += srcIP+"/";
		if(sameNet) {}
		else if(srcMask.isEmpty())
			result += "* -> ";
		else
			result += srcMask+" -> ";
		if(destIP.isEmpty())
			result += "*/";
		else
			result += destIP+"/";
		if(destMask.isEmpty())
			result += "*; ";
		else
			result += destMask+"; ";
		if(protocol >= 0) {
			if(protocol == FirewallRule.TCP)
				result += "TCP:";
			else if(protocol == FirewallRule.UDP)
				result += "UDP:";
			else
				result += "?:";
		}
		else {
			result += "*:";
		}
		if(port >= 0)
			result += port+"  => ";
		else
			result += "*  => ";
		if(action == FirewallRule.ACCEPT)
			result += "ACCEPT";
		else if(action == FirewallRule.DROP)
			result += "DROP";
		else
			result += action;
		return result;
	}
}
