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

import filius.Main;
import filius.exception.InvalidParameterException;
import filius.hardware.NetzwerkInterface;
import filius.hardware.Verbindung;
import filius.hardware.knoten.InternetKnoten;
import filius.rahmenprogramm.SzenarioVerwaltung;
import filius.software.netzzugangsschicht.EthernetFrame;
import filius.software.system.InternetKnotenBetriebssystem;
import filius.software.system.SystemSoftware;

/**
 * In dieser Klasse ist das Address Resolution Protocol (ARP) implementiert. Insbesondere wird hier die ARP-Tabelle mit
 * Eintraegen, die aus einer IP-Adresse und einem Paar aus MAC-Adresse und Zeitpunkt der Eintragerstellung besteht.
 */
public class ARP extends VermittlungsProtokoll {

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
        Main.debug.println(
                "INVOKED-2 (" + this.hashCode() + ") " + getClass() + " (ARP), constr: ARP(" + systemAnwendung + ")");
    }

    public void starten() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (ARP), starten()");
        arpTabelle = new HashMap<String, String[]>();
        hinzuARPTabellenEintrag(IPAddress.globalBroadcast(SzenarioVerwaltung.getInstance().ipVersion()),
                "FF:FF:FF:FF:FF:FF");
        thread = new ARPThread(this);
        thread.starten();
    }

    public void beenden() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (ARP), beenden()");
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
    public void hinzuARPTabellenEintrag(IPAddress ipAdresse, String macAdresse) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (ARP), hinzuARPTabellenEintrag("
                + ipAdresse + "," + macAdresse + ")");
        String tmpTime = "" + System.currentTimeMillis();
        String[] tmpString = { macAdresse, tmpTime };
        synchronized (arpTabelle) {
            arpTabelle.put(ipAdresse.standardAddress(), tmpString);
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
     * 
     * @param ipAdresse
     * @return MAC Adresse, zu der die IP Adresse gehoert, oder null, wenn keine MAC-Adresse bestimmt werden konnte
     */
    public String holeARPTabellenEintrag(String zielIp) {
        Main.debug.println(
                "INVOKED (" + this.hashCode() + ") " + getClass() + " (ARP), holeARPTabellenEintrag(" + zielIp + ")");

        try {
            IPAddress ipAddress = new IPAddress(zielIp);
            if (ipAddress.isLoopbackAddress()) {
                return ((InternetKnotenBetriebssystem) holeSystemSoftware()).holeMACAdresse();
            }
            if (holeSystemSoftware() instanceof InternetKnotenBetriebssystem) {
                NetzwerkInterface nic = ((InternetKnoten) holeSystemSoftware().getKnoten())
                        .getNetzwerkInterfaceByIp(zielIp);
                if (nic != null) {
                    return nic.getMac();
                }
            }
            // Eintrag in ARP-Tabelle fuer gesuchte IP-Adresse?
            if (getArpEntry(ipAddress) != null) {
                return ((String[]) getArpEntry(ipAddress))[0];
            } else {
                // ARP-Broadcast und warte auf Antwort
                for (int i = 0; getArpEntry(ipAddress) == null && i < 2; i++) {
                    try {
                        sendeARPBroadcast(zielIp);
                        synchronized (arpTabelle) {
                            try {
                                arpTabelle.wait(Verbindung.holeRTT() / 2);
                            } catch (InterruptedException e) {
                                Main.debug.println("EXCEPTION (" + this.hashCode()
                                        + "): keine Anwort auf ARP-Broadcast fuer IP-Adresse " + zielIp
                                        + " eingegangen!");
                                e.printStackTrace(Main.debug);
                            }
                        }
                    } catch (InvalidParameterException e1) {
                        e1.printStackTrace();
                    }
                }

                // Abfrage in ARP-Tabelle nach Broadcast
                if (getArpEntry(ipAddress) != null) {
                    return ((String[]) getArpEntry(ipAddress))[0];
                }
            }
        } catch (InvalidParameterException e2) {
            e2.printStackTrace();
        }

        Main.debug.println("ERROR (" + this.hashCode() + "): kein ARP-Tabellen-Eintrag fuer " + zielIp);
        return null;
    }

    private String[] getArpEntry(IPAddress ipAddress) {
        return arpTabelle.get(ipAddress.standardAddress());
    }

    /**
     * Hilfsmethode zum Versenden einer ARP-Anfrage
     * 
     * @throws InvalidParameterException
     */
    private void sendeARPBroadcast(String suchIp) throws InvalidParameterException {
        NetzwerkInterface nic = getBroadcastNic(suchIp);
        if (nic == null) {
            return;
        }

        ArpPaket arpPaket = new ArpPaket();
        arpPaket.setProtokollTyp(EthernetFrame.ARP);
        arpPaket.setZielIp(suchIp);
        arpPaket.setZielMacAdresse("FF:FF:FF:FF:FF:FF");
        arpPaket.setQuellIp(nic.getIp());
        arpPaket.setQuellMacAdresse(nic.getMac());

        ((InternetKnotenBetriebssystem) holeSystemSoftware()).holeEthernet().senden(arpPaket, nic.getMac(),
                "FF:FF:FF:FF:FF:FF", EthernetFrame.ARP);
    }

    public static boolean isValidArpEntry(String ipAddress, String localNetmask) {
        return !VermittlungsProtokoll.isBroadcast(ipAddress, ipAddress, localNetmask)
                && !VermittlungsProtokoll.isNetworkAddress(ipAddress, ipAddress, localNetmask);
    }

    public NetzwerkInterface getBroadcastNic(String zielStr) throws InvalidParameterException {
        long bestMask = -1;
        NetzwerkInterface bestNic = null;

        for (NetzwerkInterface nic : ((InternetKnoten) holeSystemSoftware().getKnoten()).getNetzwerkInterfaces()) {
            IPAddress senderAddress = nic.addressIP();
            IPAddress rcptAddress = new IPAddress(zielStr, senderAddress.netmask());
            if (senderAddress.netmaskLength() > bestMask
                    && senderAddress.networkAddress().equals(rcptAddress.networkAddress())) {
                bestMask = senderAddress.netmaskLength();
                bestNic = nic;
            }
        }
        if (null == bestNic) {
            bestNic = ((InternetKnoten) holeSystemSoftware().getKnoten()).getNetzwerkInterfaces().get(0);
        }
        return bestNic;
    }

    public ARPThread getARPThread() {
        return thread;
    }
}
