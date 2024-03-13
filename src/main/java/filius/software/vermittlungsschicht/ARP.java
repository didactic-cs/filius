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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import filius.hardware.NetzwerkInterface;
import filius.hardware.Verbindung;
import filius.hardware.knoten.InternetKnoten;
import filius.software.netzzugangsschicht.EthernetFrame;
import filius.software.system.InternetKnotenBetriebssystem;
import filius.software.system.SystemSoftware;

/**
 * In dieser Klasse ist das Address Resolution Protocol (ARP) implementiert. Insbesondere wird hier die ARP-Tabelle mit
 * Eintraegen, die aus einer IP-Adresse und einem Paar aus MAC-Adresse und Zeitpunkt der Eintragerstellung besteht.
 */
public class ARP extends VermittlungsProtokoll {
    private static Logger LOG = LoggerFactory.getLogger(ARP.class);

    /**
     * Die ARP-Tabelle als Hashtabelle. Als Schluessel wird die IP-Adresse verwendet. Der zugehoerige Wert ist ein
     * String-Array mit der gesuchten MAC-Adresse und dem Zeitpunkt, zu dem der Eintrag vorgenommen wurde.
     */
    private HashMap<String, String[]> arpTabelle = new HashMap<String, String[]>();

    /**
     * Der Thread zur Ueberwachung des Puffers mit eingehenden ARP-Paketen
     */
    private ARPThread thread;

    /**
     * Standard-Konstruktor zur Initialisierung der zugehoerigen Systemsoftware
     * 
     * @param systemAnwendung
     */
    public ARP(SystemSoftware systemAnwendung) {
        super(systemAnwendung);
        LOG.trace("INVOKED-2 (" + this.hashCode() + ") " + getClass() + " (ARP), constr: ARP(" + systemAnwendung + ")");
    }

    public void starten() {
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass() + " (ARP), starten()");
        resetArpTable();
        thread = new ARPThread(this);
        thread.starten();
    }

    public void resetArpTable() {
        synchronized (arpTabelle) {
            arpTabelle = new HashMap<String, String[]>();
            hinzuARPTabellenEintrag("255.255.255.255", "FF:FF:FF:FF:FF:FF");
        }
    }

    public void beenden() {
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass() + " (ARP), beenden()");
        if (thread != null)
            thread.beenden();
    }

    /**
     * Fuegt eine Zeile zur ARP Tabelle hinzu. Dabei werden IP Adresse und MAC-Adresse uebergeben
     * 
     * @author Thomas Gerding
     * 
     * @param ipAdresse
     * @param macAdresse
     */
    public void hinzuARPTabellenEintrag(String ipAdresse, String macAdresse) {
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass() + " (ARP), hinzuARPTabellenEintrag(" + ipAdresse
                + "," + macAdresse + ")");
        String tmpTime = "" + System.currentTimeMillis();
        String[] tmpString = { macAdresse, tmpTime };

        synchronized (arpTabelle) {
            arpTabelle.put(ipAdresse, tmpString);
            arpTabelle.notify();
        }
    }

    public void removeARPTableEntry(String ipAddress) {
        synchronized (arpTabelle) {
            arpTabelle.remove(ipAddress);
            arpTabelle.notify();
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Entry<String, String[]> entry : arpTabelle.entrySet()) {
            builder.append("\t").append(entry.getKey()).append(" \t ").append(entry.getValue()[0]).append("\n");
        }
        return builder.toString();
    }

    public Map<String, String> holeARPTabelle(String address) {
        Map<String, String> table = new HashMap<String, String>();
        String[] entry = arpTabelle.get(address);
        if (null != entry) {
            table.put(address, entry[0]);
        }
        return table;
    }

    public Map<String, String> holeARPTabelle() {
        Map<String, String> table = new HashMap<String, String>();

        for (String ipAddress : arpTabelle.keySet()) {
            table.put(ipAddress, arpTabelle.get(ipAddress)[0]);
        }
        return table;
    }

    /**
     * Liefert die MAC Adresse zu einer IP Adresse aus der ARP Tabelle zurueck. Wenn kein passender Eintrag vorhanden
     * ist, wird eine Broadcast-Anfrage verschickt und auf eingehende Antworten gewartet. Wenn nach einem Timeout ein
     * passender Eintrag vorliegt, wird dieser zurueck gegeben. Andernfalls wird null zurueck gegeben.
     * 
     * @author Thomas Gerding
     * @param ipAdresse
     * @param maxRetries
     * 
     * @return MAC Adresse, zu der die IP Adresse gehoert, oder null, wenn keine MAC-Adresse bestimmt werden konnte
     */
    public String holeARPTabellenEintrag(String zielIp, int maxRetries) {
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass() + " (ARP), holeARPTabellenEintrag(" + zielIp + ")");
        if (zielIp.equals("127.0.0.1")) {
            return ((InternetKnotenBetriebssystem) holeSystemSoftware()).primaryMACAddress();
        }
        if (holeSystemSoftware() instanceof InternetKnotenBetriebssystem) {
            if (zielIp.equals(((InternetKnotenBetriebssystem) holeSystemSoftware()).primaryIPAdresse())) {
                return ((InternetKnotenBetriebssystem) holeSystemSoftware()).primaryMACAddress();
            }
        }
        // Eintrag in ARP-Tabelle fuer gesuchte IP-Adresse?
        String[] arpEntry = arpTabelle.get(zielIp);
        if (arpEntry != null) {
            return ((String[]) arpEntry)[0];
        } else {
            // ARP-Broadcast und warte auf Antwort
            for (int i = 0; arpEntry == null && i < maxRetries; i++) {
                LOG.debug("Send ARP query for " + (i + 1) + ". time.");
                sendeARPBroadcast(zielIp);
                synchronized (arpTabelle) {
                    try {
                        arpTabelle.wait(Verbindung.holeRTT());
                    } catch (InterruptedException e) {
                        LOG.debug("EXCEPTION (" + this.hashCode() + "): keine Anwort auf ARP-Broadcast fuer IP-Adresse "
                                + zielIp + " eingegangen!", e);
                    }
                }
                // Abfrage in ARP-Tabelle nach Broadcast
                arpEntry = arpTabelle.get(zielIp);
            }

            if (arpEntry != null) {
                return ((String[]) arpEntry)[0];
            }
        }

        LOG.debug("ERROR (" + this.hashCode() + "): kein ARP-Tabellen-Eintrag fuer " + zielIp);
        return null;
    }

    /** Hilfsmethode zum Versenden einer ARP-Anfrage */
    private void sendeARPBroadcast(String suchIp) {
        NetzwerkInterface nic = getBroadcastNic(suchIp);
        if (nic == null) {
            return;
        }
        sendArpRequest(nic.getMac(), nic.getIp(), "FF:FF:FF:FF:FF:FF", suchIp);
    }

    public ArpPaket sendArpRequest(String senderMAC, String senderIP, String targetMAC, String lookupOrTargetIP) {
        ArpPaket arpPaket = new ArpPaket();
        arpPaket.setOperation(ArpPaket.REQUEST);
        arpPaket.setProtokollTyp(EthernetFrame.IP);
        arpPaket.setTargetIP(lookupOrTargetIP);
        arpPaket.setTargetMAC(targetMAC);
        arpPaket.setSenderIP(senderIP);
        arpPaket.setSenderMAC(senderMAC);

        ((InternetKnotenBetriebssystem) holeSystemSoftware()).holeEthernet().senden(arpPaket, senderMAC,
                arpPaket.getTargetMAC(), EthernetFrame.ARP);
        return arpPaket;
    }

    public ArpPaket sendArpReply(String senderMAC, String senderIP, String targetMAC, String targetIP) {
        ArpPaket antwortArp = new ArpPaket();
        antwortArp.setOperation(ArpPaket.REPLY);
        antwortArp.setProtokollTyp(EthernetFrame.IP);
        antwortArp.setSenderIP(senderIP);
        antwortArp.setSenderMAC(senderMAC);

        if (targetIP.equalsIgnoreCase("0.0.0.0")) {
            antwortArp.setTargetIP("255.255.255.255");
            antwortArp.setTargetMAC("ff:ff:ff:ff:ff:ff");
        } else {
            antwortArp.setTargetIP(targetIP);
            antwortArp.setTargetMAC(targetMAC);
        }

        ((InternetKnotenBetriebssystem) holeSystemSoftware()).holeEthernet().senden(antwortArp, senderMAC,
                antwortArp.getTargetMAC(), EthernetFrame.ARP);
        return antwortArp;
    }

    public NetzwerkInterface getBroadcastNic(String zielStr) {
        long netAddr, maskAddr, zielAddr = IP.inetAton(zielStr);

        long bestMask = -1;
        NetzwerkInterface bestNic = null;

        for (NetzwerkInterface nic : ((InternetKnoten) holeSystemSoftware().getKnoten()).getNetzwerkInterfaces()) {
            maskAddr = IP.inetAton(nic.getSubnetzMaske());
            if (maskAddr <= bestMask) {
                continue;
            }
            netAddr = IP.inetAton(nic.getIp()) & maskAddr;
            if (netAddr == (maskAddr & zielAddr)) {
                bestMask = maskAddr;
                bestNic = nic;
            }
        }
        return bestNic;
    }

    public ARPThread getARPThread() {
        return thread;
    }
}
