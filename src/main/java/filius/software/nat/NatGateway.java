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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import filius.hardware.knoten.Gateway;
import filius.rahmenprogramm.I18n;
import filius.software.firewall.Firewall;
import filius.software.system.GatewayFirmware;
import filius.software.vermittlungsschicht.IpPaket;

public class NatGateway extends Firewall implements I18n {
    static final int PSEUDO_PORT_ICMP = 0;

    private static Logger LOG = LoggerFactory.getLogger(NatGateway.class);

    private NetworkAddressTranslationTable natTable = new NetworkAddressTranslationTable();

    @Override
    protected void initFirewallThreads() {
        Gateway gateway = (Gateway) getSystemSoftware().getKnoten();
        addAndStartThread(new NatGatewayLANThread(this, gateway.holeLANInterface(), gateway.holeWANInterface()));
        addAndStartThread(new NatGatewayWANThread(this, gateway.holeWANInterface(), gateway.holeLANInterface()));
        LOG.debug("Threads for WAN and LAN nic are started on {}", gateway.getName());
    }

    public void insertNewConnection(int protocol, String lanIpAddress, int lanPort, String wanIpAddress, int wanPort) {
        InetAddress lanAddress = new InetAddress(lanIpAddress, lanPort, protocol);
        Gateway gateway = (Gateway) getSystemSoftware().getKnoten(); //neu
        if (!natTable.hasConnection(lanAddress)) {
            int port = PSEUDO_PORT_ICMP;
            if (protocol == IpPaket.TCP) {
                port = getSystemSoftware().holeTcp().reserviereFreienPort();
            } else if (protocol == IpPaket.UDP) {
                port = getSystemSoftware().holeUdp().reserviereFreienPort();
            }
            LOG.debug("New connection in NAT table: protocol={}, WANaddress={}, port={}, LANaddress={}", protocol, wanIpAddress, port, lanAddress);
            natTable.addDynamic(port, wanIpAddress, protocol, lanAddress, NatType.DynamicEntry);
            natTable.print();
            ((GatewayFirmware) gateway.getSystemSoftware()).fireNATPropertyChange();
        }
    }

    public void replaceSource(IpPaket packet) {
        Gateway gateway = (Gateway) getSystemSoftware().getKnoten();
        if (packet.getProtocol() == IpPaket.TCP || packet.getProtocol() == IpPaket.UDP) {
            InetAddress lanAddress = new InetAddress(packet.getSender(), packet.getSegment().getQuellPort(),
                    packet.getProtocol());
            int port = natTable.findPort(lanAddress);
            packet.getSegment().setQuellPort(port);
        }
        packet.setSender(gateway.holeWANInterface().getIp());
    }

    public void replaceDestination(IpPaket packet) {
        int port = (packet.getProtocol() == IpPaket.TCP || packet.getProtocol() == IpPaket.UDP)
                ? packet.getSegment().getZielPort()
                : PSEUDO_PORT_ICMP;
    	InetAddress dest = natTable.find(port, packet.getSender(), packet.getProtocol());
        Gateway gateway = (Gateway) getSystemSoftware().getKnoten();
        ((GatewayFirmware) gateway.getSystemSoftware()).fireNATPropertyChange();
        if (dest != null) {
        	packet.setEmpfaenger(dest.getIpAddress());
            if (packet.getProtocol() == IpPaket.TCP || packet.getProtocol() == IpPaket.UDP) {
                packet.getSegment().setZielPort(dest.getPort());
            }
        }
    }
    
    public NetworkAddressTranslationTable getNATTable() {
    	return natTable;
    }
    
    public boolean eintragExistiert(IpPaket packet) {
    	int port = (packet.getProtocol() == IpPaket.TCP || packet.getProtocol() == IpPaket.UDP)
                ? packet.getSegment().getZielPort()
                : PSEUDO_PORT_ICMP;
    	return (natTable.find(port, packet.getSender(), packet.getProtocol())) == null? false: true;
    }
}
