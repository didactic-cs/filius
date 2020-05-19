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
package filius.software.vermittlungsschicht;

import static filius.software.netzzugangsschicht.Ethernet.ETHERNET_BROADCAST;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import filius.Main;
import filius.exception.InvalidParameterException;
import filius.exception.VerbindungsException;
import filius.hardware.NetzwerkInterface;
import filius.hardware.knoten.InternetKnoten;
import filius.rahmenprogramm.I18n;
import filius.software.netzzugangsschicht.EthernetFrame;
import filius.software.system.InternetKnotenBetriebssystem;
import filius.software.transportschicht.TcpSegment;
import filius.software.transportschicht.UdpSegment;

/**
 * Diese Klasse implementiert die Funktionalitaet des Internet-Protokolls. Das heisst, dass Pakete richtig
 * weitergeleitet werden und eingehende Segmente an die Transportschicht weitergeleitet werden.
 */
public class IP extends VermittlungsProtokoll implements I18n {

    /** Puffer fuer eingehende IP-Pakete fuer TCP */
    private LinkedList<IpPaket> ipPaketListeTCP = new LinkedList<IpPaket>();

    /** Puffer fuer eingehende IP-Pakete fuer UDP */
    private LinkedList<IpPaket> ipPaketListeUDP = new LinkedList<IpPaket>();

    /**
     * Der Thread zur Ueberwachung des IP-Pakete-Puffers der Ethernet-Schicht
     */
    private IPThread thread;

    /**
     * Konstruktor zur Initialisierung der Systemsoftware
     * 
     * @param systemsoftware
     */
    public IP(InternetKnotenBetriebssystem systemsoftware) {
        super(systemsoftware);
        Main.debug.println(
                "INVOKED-2 (" + this.hashCode() + ") " + getClass() + " (IP), constr: IP(" + systemsoftware + ")");
    }

    public static long inetAToN(String ipStr) {
        long ipAddr = 0;
        int octet;
        StringTokenizer ipToken = new StringTokenizer(ipStr, ".");

        try {
            for (int i = 0; i < 4; i++) {
                try {
                    octet = Integer.parseInt(ipToken.nextToken());
                } catch (java.lang.NumberFormatException e) {
                    return -1;
                }
                if (0 > octet || octet > 255) {
                    return -1;
                }
                ipAddr += octet;
                if (i < 3) {
                    ipAddr <<= 8;
                }
            }
        } catch (NoSuchElementException e) {
            return -1;
        }

        if (ipToken.hasMoreTokens()) {
            return -1;
        }

        return ipAddr;
    }

    public static String inetNtoa(long ipAddr) {
        String ipStr = "";
        ipStr = "." + (ipAddr & 255);
        ipAddr >>= 8;
        ipStr = "." + (ipAddr & 255) + ipStr;
        ipAddr >>= 8;
        ipStr = "." + (ipAddr & 255) + ipStr;
        ipAddr >>= 8;
        ipStr = (ipAddr & 255) + ipStr;
        return ipStr;
    }

    /** Hilfsmethode zum Versenden eines Broadcast-Pakets */
    private void sendeBroadcast(IpPaket ipPaket) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (IP), sendeBroadcast("
                + ipPaket.toString() + ")");

        InternetKnoten knoten = (InternetKnoten) holeSystemSoftware().getKnoten();
        for (NetzwerkInterface nic : knoten.getNetzwerkInterfaces()) {
            // Broadcast-Nachrichten werden nur im lokalen Rechnernetz
            // verschickt
            sendBroadcastOverNic(nic, ipPaket);
        }
    }

    private void sendBroadcastOverNic(NetzwerkInterface nic, IpPaket ipPaket) {
        // Damit Broadcast-Pakete nicht in Zyklen gesendet werden,
        // wird das Feld Time-to-Live (TTL) auf 1 gesetzt. Damit
        // wird es von keinem Knoten weitergeschickt
        ipPaket.setTtl(1);

        InternetKnotenBetriebssystem bs = (InternetKnotenBetriebssystem) holeSystemSoftware();
        try {
            IPAddress sender = new IPAddress(ipPaket.getSender());
            if (sender.isUnspecifiedAddress()
                    || gleichesRechnernetz(ipPaket.getSender(), nic.getIp(), nic.getSubnetzMaske())) {
                bs.holeEthernet().senden(ipPaket, nic.getMac(), ETHERNET_BROADCAST, EthernetFrame.IP);
            }
        } catch (InvalidParameterException e) {}
    }

    /**
     * Hilfsmethode fuer Pakete, die fuer den eigenen Knoten bestimmt sind
     * 
     * @param segment
     *            das zu verarbeitende Segment
     */
    void benachrichtigeTransportschicht(IpPaket paket) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (IP), benachrichtigeTransportschicht("
                + paket.toString() + ")");
        if (paket.getSegment() instanceof TcpSegment) {
            synchronized (ipPaketListeTCP) {
                ipPaketListeTCP.add(paket);
                ipPaketListeTCP.notify();
            }
        } else if (paket.getSegment() instanceof UdpSegment) {
            synchronized (ipPaketListeUDP) {
                ipPaketListeUDP.add(paket);
                ipPaketListeUDP.notify();
            }
        }
    }

    /**
     * Hilfsmethode zum versenden eines Unicast-Pakets. Hier wird unterschieden, ob sich die Ziel-IP-Adresse im lokalen
     * Rechnernetz befindet oder ueber das Gateway verschickt werden muss.
     * 
     * @param paket
     *            das zu versendende IP-Paket
     * @throws RouteNotFoundException
     */
    private void sendeUnicast(IpPaket paket, Route route) throws RouteNotFoundException {
        NetzwerkInterface nic = ((InternetKnoten) holeSystemSoftware().getKnoten())
                .getNetzwerkInterfaceByIp(route.getInterfaceIpAddress());
        String netzmaske = nic.getSubnetzMaske();

        if (gleichesRechnernetz(paket.getEmpfaenger(), route.getInterfaceIpAddress(), netzmaske)) {
            // adressierter Knoten befindet sich im lokalen Rechnernetz
            sendeUnicastLokal(paket, paket.getEmpfaenger(), nic);
        } else {
            // adressierter Knoten ist ueber Gateway zu erreichen
            sendeUnicastLokal(paket, route.getGateway(), nic);
        }
    }

    private void sendeUnicastLokal(IpPaket paket, String ziel, NetzwerkInterface nic) {
        InternetKnotenBetriebssystem bs = (InternetKnotenBetriebssystem) holeSystemSoftware();
        String zielMacAdresse = bs.holeARP().holeARPTabellenEintrag(ziel);

        if (zielMacAdresse != null) {
            // MAC-Adresse konnte bestimmt werden
            bs.holeEthernet().senden(paket, nic.getMac(), zielMacAdresse, EthernetFrame.IP);
        } else {
            // Es konnte keine MAC-Adresse bestimmt werden.
            // Es muss ein ICMP Destination Unreachable: Host Unreachable
            // (3/1) zurueckgesendet werden:
            bs.holeICMP().sendeICMP(ICMP.TYPE_DESTINATION_UNREACHABLE, ICMP.CODE_DEST_HOST_UNREACHABLE,
                    paket.getSender());
        }
    }

    /**
     * Methode zum Versenden eines IP-Pakets. Zunaechst wird die Sender-IP-Adresse an Hand der Weiterleitungstabelle
     * bestimmt (die Schnittstelle, ueber die das Paket versendet wird). Dann wird das Paket erzeugt und schliesslich an
     * die Methode weiterleitenPaket() uebergeben.
     * 
     * @param zielIp
     *            Gibt die Ziel-IP-Adresse an
     * @param quellIp
     * @param protokoll
     *            Der Parameter Protokoll gibt die Protokollnummer an. Dabei steht die Nummer 6 fuer das Protokoll TCP
     * @param ttl
     * @param segment
     *            - Enthaellt das erzeugte Segment mit den Nutzdaten.
     * @throws VerbindungsException
     */
    public void senden(String zielIp, String quellIp, int protokoll, int ttl, Object segment) {
        IpPaket paket = new IpPaket();
        paket.setEmpfaenger(zielIp);
        paket.setProtocol(protokoll);
        paket.setTtl(ttl);
        paket.setSegment(segment);

        if (isLocalAddress(zielIp)) {
            // Paket ist an diesen Rechner gerichtet
            paket.setSender(quellIp);
            benachrichtigeTransportschicht(paket);
        } else if (isGlobalBroadcast(zielIp)) {
            if (quellIp == null) {
                quellIp = ((InternetKnotenBetriebssystem) holeSystemSoftware()).holeIPAdresse();
            }
            paket.setSender(quellIp);
            sendeBroadcast(paket);
        } else {
            try {
                InternetKnotenBetriebssystem bs = (InternetKnotenBetriebssystem) holeSystemSoftware();
                Route route = bs.determineRoute(paket.getEmpfaenger());
                paket.setSender(route.getInterfaceIpAddress());
                sendeUnicast(paket, route);
            } catch (RouteNotFoundException e) {}
        }
    }

    private boolean isGlobalBroadcast(String zielIpAdresse) {
        boolean isBroadcast = false;
        try {
            IPAddress tmp = new IPAddress(zielIpAdresse);
            isBroadcast = tmp.isGlobalBroadcast();
        } catch (InvalidParameterException e) {}
        return isBroadcast;
    }

    /**
     * Methode zum Weiterleiten eines IP-Pakets. Zunaechst wird geprueft, ob das Feld Time-to-Live-Feld (TTL) noch nicht
     * abgelaufen ist (d. h. TTL groesser 0). Wenn diese Bedingung erfuellt ist, wird die Weiterleitungstabelle nach
     * einem passenden Eintrag abgefragt und entsprechend verschickt.
     * 
     * @param ipPaket
     *            das zu versendende IP-Paket
     */
    public void weiterleitenPaket(IpPaket paket) {
        InternetKnotenBetriebssystem bs = (InternetKnotenBetriebssystem) holeSystemSoftware();

        if (paket.getTtl() <= 0) {
            bs.holeICMP().sendeICMP(ICMP.TYPE_TIME_EXCEEDED, ICMP.CODE_TTL_EXPIRED, paket.getSender());
        } else {
            try {
                Route route = bs.determineRoute(paket.getEmpfaenger());
                sendeUnicast(paket, route);
            } catch (RouteNotFoundException e) {
                bs.holeICMP().sendeICMP(ICMP.TYPE_DESTINATION_UNREACHABLE, ICMP.CODE_DEST_NETWORK_UNREACHABLE,
                        paket.getSender());
                bs.benachrichtigeBeobacher(messages.getString("sw_ip_msg4") + " \"" + bs.getKnoten().getName() + "\"!\n"
                        + messages.getString("sw_ip_msg5") + " " + paket.getEmpfaenger() + " "
                        + messages.getString("sw_ip_msg6"));
            }
        }
    }

    /**
     * Methode fuer den Zugriff auf den Puffer fuer eingehende Segmente
     * 
     * @param protokollTyp
     *            der Protokolltyp UDP oder TCP (Konstanten der Klasse TransportProtokoll)
     * @return die Liste mit Segmenten fuer UDP- oder TCP-Segmente
     */
    public LinkedList<IpPaket> holePaketListe(int protokollTyp) {
        Main.debug.println(
                "INVOKED (" + this.hashCode() + ") " + getClass() + " (IP), holePaketListe(" + protokollTyp + ")");
        if (protokollTyp == IpPaket.TCP)
            return ipPaketListeTCP;
        else if (protokollTyp == IpPaket.UDP)
            return ipPaketListeUDP;
        else
            return null;
    }

    /**
     * Hier wird der Thread zur Ueberwachung des Puffers fuer eingehende IP-Pakete der Netzzugangsschicht
     */
    public void starten() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (IP), starten()");

        thread = new IPThread(this);
        thread.starten();
    }

    /** Der Thread zur Ueberwachung des IP-Pakete-Puffers */
    public void beenden() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (IP), beenden()");
        thread.beenden();
    }

    public IPThread getIPThread() {
        return thread;
    }

}
