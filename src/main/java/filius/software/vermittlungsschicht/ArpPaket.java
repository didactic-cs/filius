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

import java.io.Serializable;

/** Diese Klasse umfasst die Attribute eines ARP-Pakets */
public class ArpPaket implements Serializable {

    public static int REQUEST = 1, REPLY = 2;

    private static final long serialVersionUID = 1L;

    /** Protokoll-Typ der Vermittlungsschicht */
    private String protokollTyp;

    /** Operation: 1 - request, 2 - reply */
    private int operation = 1;

    /** MAC-Adresse des sendenden Knotens */
    private String senderMAC;

    /** IP-Adresse des sendenden Knotens */
    private String senderIP;

    /** MAC-Adresse des Zielknotens (i.d.R. Broadcast) */
    private String targetMAC;

    /**
     * Ziel-IP-Adresse bzw. die Adresse des Knotens, zu dem die MAC-Adresse gesucht wird
     */
    private String targetIP;

    public String getProtokollTyp() {
        return protokollTyp;
    }

    public void setProtokollTyp(String protokollTyp) {
        this.protokollTyp = protokollTyp;
    }

    public String getSenderIP() {
        return senderIP;
    }

    public void setSenderIP(String quellIp) {
        this.senderIP = quellIp;
    }

    public String getSenderMAC() {
        return senderMAC;
    }

    public void setSenderMAC(String quellMacAdresse) {
        this.senderMAC = quellMacAdresse;
    }

    public String getTargetIP() {
        return targetIP;
    }

    public void setTargetIP(String zielIp) {
        this.targetIP = zielIp;
    }

    public String getTargetMAC() {
        return targetMAC;
    }

    public void setTargetMAC(String zielMacAdresse) {
        this.targetMAC = zielMacAdresse;
    }

    public String toString() {
        return "[op=" + (operation == REQUEST ? "REQUEST" : "REPLY") + ", sender=" + senderMAC + "|" + senderIP
                + ", target=" + targetMAC + "|" + targetIP + "]";
    }

    public int getOperation() {
        return operation;
    }

    public void setOperation(int operation) {
        this.operation = operation;
    }
}
