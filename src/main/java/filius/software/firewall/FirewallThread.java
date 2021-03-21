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

import filius.Main;
import filius.hardware.NetworkInterface;
import filius.rahmenprogramm.I18n;
import filius.software.ProtocolThread;
import filius.software.netzzugangsschicht.EthernetFrame;
import filius.software.vermittlungsschicht.IcmpPaket;
import filius.software.vermittlungsschicht.IpPaket;

/*
 * @author Weyer
 * Die Klasse schiebt sich zwischen die Ethernetschicht und die Vermittlungsschicht. Sie
 * tauscht den Ip-Pakete-Puffer aus, sodass sie nach Regeln selektieren kann, welche Pakete
 * als gueltig weitergeleitet werden
 */
public class FirewallThread extends ProtocolThread implements I18n {

	private LinkedList<EthernetFrame> ausgangsPuffer;
	private Firewall firewall;
	private NetworkInterface netzwerkInterface = null;

	public NetworkInterface getNetzwerkInterface() {
		return netzwerkInterface;
	}

	public FirewallThread(Firewall firewall, NetworkInterface nic) {
		super(new LinkedList<EthernetFrame>());
		Main.debug.println("INVOKED-2 (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
		        + " (FirewallThread), constr: FirewallThread(" + firewall + ")");
		this.firewall = firewall;
		this.netzwerkInterface = nic;
	}

	/*
	 * tauscht den IP-Puffer zwischen Ethernetschicht und Vermittlungsschicht
	 * aus, und startet den Thread zur Überwachung des Datenaustausches
	 */
	public void startThread() {
		Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
		        + " (FirewallThread), starten()");
		LinkedList<EthernetFrame> eingangsPuffer;

		super.startThread();

		this.ausgangsPuffer = netzwerkInterface.getPort().getInputBuffer();
		eingangsPuffer = (LinkedList<EthernetFrame>) getInputBuffer();
		netzwerkInterface.getPort().setInputBuffer(eingangsPuffer);
	}

	public void stopThread() {
		super.stopThread();

		netzwerkInterface.getPort().setInputBuffer(this.ausgangsPuffer);
	}

	// getter und setter:

	@Override
	protected void processFrame(Object datenEinheit) {
		Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
		        + " (FirewallThread), verarbeiteDatenEinheit(" + datenEinheit.toString() + ")");
		//IpPaket ipPaket = null;
		EthernetFrame frame = (EthernetFrame) datenEinheit;

		// Hier erfolgt nun die Abfrage, ob die Pakete laut Firewall in Ordnung
		// sind:
		// Bei false werden die Pakete weitergeleitet
		// Am Ende in den Ausgangspuffer schreiben, und weiterreichen an
		// EthernetThread
		// oder nicht weiterleiten

		// Main.debug.println("DEBUG EVAL: firewall.getDropICMP = "+firewall.getDropICMP());
		// Main.debug.println("DEBUG EVAL: is ICMP = "+(frame.getDaten()
		// instanceof IcmpPaket));
		// Main.debug.println("DEBUG EVAL: ipPacket null = "+(ipPaket==null));
		// if(ipPaket!=null)
		// Main.debug.println("DEBUG EVAL: accept ipPacket = "+firewall.allowedIPpacket(ipPaket));

		// if (ipPaket == null || !firewall.pruefePaketVerwerfen(ipPaket)) {
		boolean isIcmp = frame.getData() instanceof IcmpPaket;
		if (firewall.isActivated() && firewall.getDropICMP() && isIcmp) {
			IcmpPaket icmp = (IcmpPaket) frame.getData();
			firewall.notifyObservers(messages.getString("firewallthread_msg1") + icmp.getQuellIp() + " -> "
			        + icmp.getZielIp() + " (code: " + icmp.getIcmpCode() + ", type: " + icmp.getIcmpType() + ")");
			return;
		}
		if (frame.getData() != null && frame.getData() instanceof IpPaket
		        && !firewall.allowedIPpacket((IpPaket) frame.getData())) {
			return;
		}
		synchronized (ausgangsPuffer) {
			// Main.debug.println("FirewallThread: Paket wurde von FirewallThread weitergeleitet");

			ausgangsPuffer.add(frame);
			ausgangsPuffer.notify();
		}
	}

}
