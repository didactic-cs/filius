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
package filius.rahmenprogramm.nachrichten;

import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Vector;

import filius.Main;
import filius.rahmenprogramm.I18n;
import filius.software.netzzugangsschicht.EthernetFrame;
import filius.software.transportschicht.TcpSegment;
import filius.software.transportschicht.UdpSegment;
import filius.software.vermittlungsschicht.ArpPaket;
import filius.software.vermittlungsschicht.IcmpPaket;
import filius.software.vermittlungsschicht.IpPaket;

public class PacketAnalyzer implements I18n {

    public static final String ETHERNET = "", ARP = "ARP", IP = "IP", ICMP = "ICMP", TCP = "TCP", UDP = "UDP";

    public static final String HTTP = "HTTP", SMTP = "SMTP", POP = "POP3", DNS = "DNS", DHCP = "DHCP";

    public static final String[] SPALTEN = { messages.getString("rp_lauscher_msg1"),
            messages.getString("rp_lauscher_msg2"), messages.getString("rp_lauscher_msg3"),
            messages.getString("rp_lauscher_msg4"), messages.getString("rp_lauscher_msg5"),
            messages.getString("rp_lauscher_msg6"), messages.getString("rp_lauscher_msg7") };

    public static final String[] PROTOKOLL_SCHICHTEN = { messages.getString("rp_lauscher_msg8"),
            messages.getString("rp_lauscher_msg9"), messages.getString("rp_lauscher_msg10"),
            messages.getString("rp_lauscher_msg11") };

    /** Singleton */
    private static PacketAnalyzer packetAnalyzer = null;

    private HashMap<String, LinkedList<PacketAnalyzerObserver>> observer;

    private HashMap<String, LinkedList<Object[]>> datenEinheiten;

    private PacketAnalyzer() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + ", constr: PacketAnalyzer()");
        
        observer = new HashMap<String, LinkedList<PacketAnalyzerObserver>>();
        reset();
    }
    
    public static PacketAnalyzer getInstance() {
        if (packetAnalyzer == null) {
            packetAnalyzer = new PacketAnalyzer();
        }
        return packetAnalyzer;
    }

    public void reset() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + ", reset()");
        
        // packetAnalyzer = null;
        datenEinheiten = new HashMap<String, LinkedList<Object[]>>();
        notifyObserver(null);
    }

    public Collection<String> getInterfaceIDs() {
        return datenEinheiten.keySet();
    }

    public void removeID(String identifier) {
        datenEinheiten.remove(identifier);
        observer.remove(identifier);
    }

    public void addObserver(String computerID, PacketAnalyzerObserver newObserver) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + ", addObserver(" + computerID + ","  + newObserver + ")");
        
        LinkedList<PacketAnalyzerObserver> liste;

        liste = this.observer.get(computerID);
        if (liste == null) {
            liste = new LinkedList<PacketAnalyzerObserver>();
            this.observer.put(computerID, liste);
        }
        liste.add(newObserver);
    }

    private void notifyObserver(String computerID) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + ", notifyObserver(" + computerID + ")");
        
        LinkedList<PacketAnalyzerObserver> liste;
        Collection<LinkedList<PacketAnalyzerObserver>> collection;
        ListIterator<PacketAnalyzerObserver> it;
        Iterator<LinkedList<PacketAnalyzerObserver>> valueIt;

        if (computerID == null) {
            collection = this.observer.values();
            liste = new LinkedList<PacketAnalyzerObserver>();
            valueIt = collection.iterator();
            while (valueIt.hasNext()) {
                liste.addAll((LinkedList<PacketAnalyzerObserver>) valueIt.next());
            }
        } else {
            liste = this.observer.get(computerID);
        }
        if (liste != null) {
            it = liste.listIterator();
            while (it.hasNext()) {
                ((PacketAnalyzerObserver) it.next()).update();
            }
        }
    }

    /**
     * Hinzufuegen von einem EthernetFrame zu den Daten
     * 
     * @param interfaceId
     *            Uebergeben wird der String des NetzwerkInterface nach Aufruf von toString()
     * @param frame
     */
    public void addDataEntry(String interfaceId, EthernetFrame frame) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + ", addDatenEinheit(" + interfaceId + "," + frame + ")");
        
        LinkedList<Object[]> liste;
        Object[] frameMitZeitstempel;

        frameMitZeitstempel = new Object[2];
        frameMitZeitstempel[0] = Long.valueOf(System.currentTimeMillis());
        frameMitZeitstempel[1] = frame;

        liste = (LinkedList<Object[]>) datenEinheiten.get(interfaceId);
        if (liste == null) {
            liste = new LinkedList<Object[]>();
        }
        synchronized (liste) {
            liste.addLast(frameMitZeitstempel);
        }

        datenEinheiten.put(interfaceId, liste);
        notifyObserver(interfaceId);
    }

    public Object[][] getDataEntries(String interfaceId, boolean inheritAddress) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + ", getDaten(" + interfaceId + ")");
        
        Vector<Object[]> vector;
        Object[][] daten;

        vector = prepareData(interfaceId, inheritAddress);
        if (vector == null) {
            daten = new Object[0][SPALTEN.length];
            return daten;
        } else {
            daten = new Object[vector.size()][SPALTEN.length];
            for (int i = 0; i < vector.size(); i++) {
                daten[i] = (Object[]) vector.elementAt(i);
            }
            return daten;
        }
    }

    public void print(String interfaceId) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + ", print(" + interfaceId + ")");
        Object[][] daten;

        daten = getDataEntries(interfaceId, false);
        for (int i = 0; i < daten.length; i++) {
            for (int j = 0; j < daten[i].length; j++) {
                Main.debug.print("\t" + daten[i][j]);
            }
            Main.debug.println();
        }
    }

    private Vector<Object[]> prepareData(String interfaceId, boolean inheritAddress) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + ", datenVorbereiten(" + interfaceId + ")");
        
        Vector<Object[]> daten;
        LinkedList<Object[]> liste;
        Object[] frameMitZeitstempel, neuerEintrag;
        ListIterator<Object[]> it;
        Calendar zeit;
        EthernetFrame frame;
        IpPaket ipPaket;
        IcmpPaket icmpPaket;
        ArpPaket arpPaket;
        TcpSegment tcpSeg = null;
        UdpSegment udpSeg = null;
        String timestampStr = "";

        liste = datenEinheiten.get(interfaceId);
        if (liste == null) {
            return null;
        } else {
            daten = new Vector<Object[]>();

            synchronized (liste) {
                it = liste.listIterator();
                for (int i = 1; it.hasNext(); i++) {
                    frameMitZeitstempel = (Object[]) it.next();
                    neuerEintrag = new Object[SPALTEN.length];
                    neuerEintrag[0] = "" + i;

                    zeit = new GregorianCalendar();
                    zeit.setTimeInMillis(((Long) frameMitZeitstempel[0]).longValue());
                    timestampStr = (zeit.get(Calendar.HOUR_OF_DAY) < 10 ? "0" + zeit.get(Calendar.HOUR_OF_DAY)
                            : zeit.get(Calendar.HOUR_OF_DAY))
                            + ":"
                            + (zeit.get(Calendar.MINUTE) < 10 ? "0" + zeit.get(Calendar.MINUTE)
                                    : zeit.get(Calendar.MINUTE))
                            + ":"
                            + (zeit.get(Calendar.SECOND) < 10 ? "0" + zeit.get(Calendar.SECOND)
                                    : zeit.get(Calendar.SECOND))
                            + "."
                            + (zeit.get(Calendar.MILLISECOND) < 10 ? "00" + zeit.get(Calendar.MILLISECOND)
                                    : (zeit.get(Calendar.MILLISECOND) < 100 ? "0" + zeit.get(Calendar.MILLISECOND)
                                            : zeit.get(Calendar.MILLISECOND)));

                    neuerEintrag[1] = timestampStr;
                    frame = (EthernetFrame) frameMitZeitstempel[1];
                    neuerEintrag[2] = frame.getSourceMacAdresse();
                    neuerEintrag[3] = frame.getTargetMacAdresse();
                    neuerEintrag[4] = ETHERNET;
                    neuerEintrag[5] = PROTOKOLL_SCHICHTEN[0];
                    neuerEintrag[6] = frame.getType();

                    daten.addElement(neuerEintrag);

                    neuerEintrag = new Object[SPALTEN.length];
                    neuerEintrag[0] = "" + i;

                    neuerEintrag[1] = timestampStr;

                    if (frame.getType().equals(EthernetFrame.IP) && !frame.isICMP()) {
                        ipPaket = (IpPaket) frame.getData();
                        neuerEintrag[2] = ipPaket.getSender();
                        neuerEintrag[3] = ipPaket.getEmpfaenger();
                        neuerEintrag[4] = IP;
                        neuerEintrag[5] = PROTOKOLL_SCHICHTEN[1];
                        neuerEintrag[6] = messages.getString("rp_lauscher_msg12") + ipPaket.getProtocol() + ", TTL: "
                                + ipPaket.getTtl();
                        daten.addElement(neuerEintrag);

                        neuerEintrag = new Object[SPALTEN.length];
                        neuerEintrag[0] = "" + i;

                        neuerEintrag[1] = timestampStr;

                        String source = null;
                        String dest = null;

                        if (ipPaket.getProtocol() == IpPaket.TCP) {
                            tcpSeg = (TcpSegment) ipPaket.getSegment();

                            if (inheritAddress) {
                                source = ipPaket.getSender() + ":" + tcpSeg.getQuellPort();
                                neuerEintrag[2] = source;
                                dest = ipPaket.getEmpfaenger() + ":" + tcpSeg.getZielPort();
                                neuerEintrag[3] = dest;
                            } else {
                                neuerEintrag[2] = tcpSeg.getQuellPort();
                                neuerEintrag[3] = tcpSeg.getZielPort();
                            }
                            neuerEintrag[4] = TCP;
                            neuerEintrag[5] = PROTOKOLL_SCHICHTEN[2];
                            if (tcpSeg.isSyn()) {
                                neuerEintrag[6] = "SYN";
                            } else if (tcpSeg.isFin()) {
                                neuerEintrag[6] = "FIN";
                            }
                            neuerEintrag[6] = ((neuerEintrag[6] == null) ? "" : neuerEintrag[6] + ", ") + "SEQ: "
                                    + tcpSeg.getSeqNummer();
                            if (tcpSeg.isAck()) {
                                neuerEintrag[6] = neuerEintrag[6] + ", ACK:" + tcpSeg.getAckNummer();
                            }
                        } else if (ipPaket.getProtocol() == IpPaket.UDP) {
                            udpSeg = (UdpSegment) ipPaket.getSegment();
                            if (inheritAddress) {
                                source = ipPaket.getSender() + ":" + udpSeg.getQuellPort();
                                neuerEintrag[2] = source;
                                dest = ipPaket.getEmpfaenger() + ":" + udpSeg.getZielPort();
                                neuerEintrag[3] = dest;
                            } else {
                                neuerEintrag[2] = udpSeg.getQuellPort();
                                neuerEintrag[3] = udpSeg.getZielPort();
                            }
                            neuerEintrag[4] = UDP;
                            neuerEintrag[5] = PROTOKOLL_SCHICHTEN[2];
                            neuerEintrag[6] = "";
                        } else {
                            Main.debug.println("ERROR (" + this.hashCode() + "): Protokoll der Transportschicht ("
                                    + ipPaket.getProtocol() + ") nicht bekannt.");
                        }
                        daten.addElement(neuerEintrag);

                        neuerEintrag = new Object[SPALTEN.length];
                        neuerEintrag[0] = "" + i;

                        neuerEintrag[1] = timestampStr;
                        neuerEintrag[2] = source;
                        neuerEintrag[3] = dest;
                        neuerEintrag[4] = "";
                        neuerEintrag[5] = PROTOKOLL_SCHICHTEN[3];
                        if (ipPaket.getProtocol() == IpPaket.TCP) {
                            neuerEintrag[6] = tcpSeg.getDaten();
                        } else if (ipPaket.getProtocol() == IpPaket.UDP) {
                            neuerEintrag[6] = udpSeg.getDaten();
                        }

                        if (neuerEintrag[6] != null && !((String) neuerEintrag[6]).trim().equals(""))
                            daten.addElement(neuerEintrag);
                    } else if (frame.getType().equals(EthernetFrame.ARP)) {
                        arpPaket = (ArpPaket) frame.getData();
                        neuerEintrag[2] = arpPaket.getQuellIp();
                        neuerEintrag[3] = arpPaket.getZielIp();
                        neuerEintrag[4] = ARP;
                        neuerEintrag[5] = PROTOKOLL_SCHICHTEN[1];
                        if (arpPaket.getZielMacAdresse().equalsIgnoreCase("ff:ff:ff:ff:ff:ff")) {
                            neuerEintrag[6] = messages.getString("rp_lauscher_msg13") + " " + arpPaket.getZielIp()
                                    + ", ";
                        } else {
                            neuerEintrag[6] = "";
                        }
                        neuerEintrag[6] = neuerEintrag[6] + arpPaket.getQuellIp() + ": "
                                + arpPaket.getQuellMacAdresse();

                        daten.addElement(neuerEintrag);
                    } else if (frame.getType().equals(EthernetFrame.IP) && frame.isICMP()) {
                        icmpPaket = (IcmpPaket) frame.getData();
                        neuerEintrag[2] = icmpPaket.getQuellIp();
                        neuerEintrag[3] = icmpPaket.getZielIp();
                        neuerEintrag[4] = ICMP;
                        neuerEintrag[5] = PROTOKOLL_SCHICHTEN[1];
                        switch (icmpPaket.getIcmpType()) {
                        case 0:
                            neuerEintrag[6] = "ICMP Echo Reply (pong)";
                            break;
                        case 3:
                            switch (icmpPaket.getIcmpCode()) {
                            case 0:
                                neuerEintrag[6] = "ICMP Network Unreachable";
                                break;
                            case 1:
                                neuerEintrag[6] = "ICMP Host Unreachable";
                                break;
                            default:
                                neuerEintrag[6] = "ICMP Destination Unreachable (code " + icmpPaket.getIcmpCode() + ")";
                                break;
                            }
                            break;
                        case 8:
                            neuerEintrag[6] = "ICMP Echo Request (ping)";
                            break;
                        case 11:
                            neuerEintrag[6] = "ICMP Time Exeeded (poof)";
                            break;
                        default:
                            neuerEintrag[6] = "ICMP unknown: " + icmpPaket.getIcmpType() + " / "
                                    + icmpPaket.getIcmpCode();
                            break;
                        }
                        neuerEintrag[6] = neuerEintrag[6] + ", TTL: " + icmpPaket.getTtl() + ", Seq.-Nr.: "
                                + icmpPaket.getSeqNr();

                        daten.addElement(neuerEintrag);
                    }
                }
            }
        }
        return daten;
    }

    public String[] getHeader() {
        return PacketAnalyzer.SPALTEN;
    }
}
