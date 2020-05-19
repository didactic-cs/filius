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

import filius.exception.InvalidParameterException;

/** Diese Klasse umfasst die Attribute eines ARP-Pakets */
public class IcmpPaket implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    /** Protokoll-Typ der Vermittlungsschicht */
    private String protokollTyp;
    /** IP-Adresse des sendenden Knotens */
    private String quellIp;
    private String zielIp;
    private int ttl; // Time-to-Live
    private int seqNr; // sequence number of Echo Request packet
    // ICMP Echo Request: type 8, code 0
    // ICMP Echo Reply: type 0, code 0
    private int icmpType;
    private int icmpCode;

    @Override
    public IcmpPaket clone() {
        IcmpPaket clone = new IcmpPaket();
        clone.protokollTyp = protokollTyp;
        clone.quellIp = quellIp;
        clone.zielIp = zielIp;
        clone.ttl = ttl;
        clone.seqNr = seqNr;
        clone.icmpType = icmpType;
        clone.icmpCode = icmpCode;
        return clone;
    }

    public void setIcmpType(int type) {
        this.icmpType = type;
    }

    public void setIcmpCode(int code) {
        this.icmpCode = code;
    }

    public int getIcmpType() {
        return icmpType;
    }

    public int getIcmpCode() {
        return icmpCode;
    }

    // manage Time-to-Live header field
    public int getTtl() {
        return this.ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public boolean isEchoResponse() {
        return icmpType == ICMP.TYPE_ECHO_REPLY && icmpCode == ICMP.CODE_ECHO;
    }

    public boolean isEchoRequest() {
        return icmpType == ICMP.TYPE_ECHO_REQUEST && icmpCode == ICMP.CODE_ECHO;
    }

    // manage sequence number field
    public int getSeqNr() {
        return this.seqNr;
    }

    public void setSeqNr(int seqNr) {
        this.seqNr = seqNr;
    }

    public String getProtokollTyp() {
        return protokollTyp;
    }

    public void setProtokollTyp(String protokollTyp) {
        this.protokollTyp = protokollTyp;
    }

    public String getQuellIp() {
        return quellIp;
    }

    public void setQuellIp(String quellIp) {
        try {
            this.quellIp = new IPAddress(quellIp).standardAddress();
        } catch (InvalidParameterException e) {}
    }

    public String getZielIp() {
        return zielIp;
    }

    public void setZielIp(String zielIp) {
        try {
            this.zielIp = new IPAddress(zielIp).standardAddress();
        } catch (InvalidParameterException e) {}
    }

    public String toString() {
        return "[" + "protokollTyp=" + protokollTyp + "; " + "quellIp=" + quellIp + "; " + "quellMacAdresse="
                + "zielIp=" + zielIp + "; " + "ttl=" + ttl + "; " + "seqNr=" + seqNr + "; " + "icmpType=" + icmpType
                + "; " + "icmpCode=" + icmpCode + "]";
    }

    public void decrementTtl() {
        ttl--;
    }
}
