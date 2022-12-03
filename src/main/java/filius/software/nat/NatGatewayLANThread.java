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

import filius.hardware.NetzwerkInterface;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.nachrichten.Lauscher;
import filius.software.firewall.FirewallThread;
import filius.software.netzzugangsschicht.EthernetFrame;
import filius.software.transportschicht.TcpSegment;
import filius.software.transportschicht.UdpSegment;
import filius.software.vermittlungsschicht.IcmpPaket;
import filius.software.vermittlungsschicht.IpPaket;
import filius.software.vermittlungsschicht.VermittlungsProtokoll;

public class NatGatewayLANThread extends FirewallThread implements I18n {
    private NatGateway natGateway;

    private NetzwerkInterface lanNic;
    private NetzwerkInterface wanNic;

    /** Init thread for the LAN network interface */
    public NatGatewayLANThread(NatGateway natGateway, NetzwerkInterface nic, NetzwerkInterface wanNic) {
        super(natGateway, nic);
        this.lanNic = nic;
        this.wanNic = wanNic;
        this.natGateway = natGateway;
    }

    @Override
    protected void verarbeiteDatenEinheit(EthernetFrame frame) {
        Lauscher.getLauscher().addDatenEinheit(lanNic.getMac(), frame);

        if (!checkDiscardByFirewall(frame) && !checkTTLExceeded(frame)) {
            updateNatTable(frame);
            modifyOutgoingFrame(frame);
            forwardFrame(frame);
        }
    }

    boolean checkTTLExceeded(EthernetFrame frame) {
        boolean exceeded = false;
        if (frame.getDaten() instanceof IpPaket) {
            IpPaket packet = (IpPaket) frame.getDaten();
            if (packet.getTtl() <= 1 && isOutgoingPacket(packet)) {
                int seqNo = 0;
                if (packet instanceof IcmpPaket) {
                    seqNo = ((IcmpPaket) packet).getSeqNr();
                }
                natGateway.getSystemSoftware().holeICMP().sendeTimeExceededReply(packet.getSender(), seqNo);
                exceeded = true;
            }
        }
        return exceeded;
    }

    protected void modifyOutgoingFrame(EthernetFrame frame) {
        if (frame.getDaten() instanceof IpPaket
                && !((IpPaket) frame.getDaten()).getEmpfaenger().equals(wanNic.getIp())) {
            IpPaket packet = (IpPaket) frame.getDaten();
            if (isOutgoingPacket(packet)) {
                natGateway.replaceSource(packet);
            }
        }
    }

    protected void updateNatTable(EthernetFrame frame) {
        if (frame.getDaten() instanceof IpPaket) {
            IpPaket packet = (IpPaket) frame.getDaten();
            if (isOutgoingPacket(packet)) {
                if (packet.getSegment() instanceof TcpSegment) {
                    TcpSegment tcpSegment = (TcpSegment) packet.getSegment();
                    if (tcpSegment.isSyn() && !tcpSegment.isAck()) {
                        natGateway.insertNewConnection(packet.getProtocol(), packet.getSender(),
                                tcpSegment.getQuellPort(), packet.getEmpfaenger(), tcpSegment.getZielPort());
                    }
                } else if (packet.getSegment() instanceof UdpSegment) {
                    UdpSegment udpSegment = (UdpSegment) packet.getSegment();
                    natGateway.insertNewConnection(packet.getProtocol(), packet.getSender(), udpSegment.getQuellPort(),
                            packet.getEmpfaenger(), udpSegment.getZielPort());
                } else if (packet.getProtocol() == IcmpPaket.ICMP_PROTOCOL) {
                    natGateway.insertNewConnection(packet.getProtocol(), packet.getSender(),
                            NatGateway.PSEUDO_PORT_ICMP, packet.getEmpfaenger(), NatGateway.PSEUDO_PORT_ICMP);
                }
            }
        }
    }

    protected boolean isOutgoingPacket(IpPaket packet) {
        return !VermittlungsProtokoll.gleichesRechnernetz(packet.getEmpfaenger(), lanNic.getIp(),
                lanNic.getSubnetzMaske())
                && !VermittlungsProtokoll.isBroadcast(packet.getEmpfaenger(), packet.getSender(),
                        lanNic.getSubnetzMaske())
                && !packet.getEmpfaenger().equals(wanNic.getIp());
    }
}
