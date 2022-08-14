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

/**
 * Diese Klasse umfasst die Attribute bzw. Felder eines IP-Pakets
 */
public class IpPaket implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;
    public static final int UDP = 17, TCP = 6;

    private int version;
    private int ihl;
    private int tos;
    private int totalLength;
    private int identification;
    private int offset;
    private boolean ff;
    private boolean df;
    private boolean mf;
    private int ttl;
    private int protocol;
    private int checksum;
    private String empfaenger;
    private String sender;
    private Object data;

    public IpPaket() {}

    @Override
    public IpPaket clone() {
        IpPaket clone = new IpPaket();
        clone.version = version;
        clone.ihl = ihl;
        clone.tos = tos;
        clone.totalLength = totalLength;
        clone.identification = identification;
        clone.offset = offset;
        clone.ff = ff;
        clone.df = df;
        clone.mf = mf;
        clone.ttl = ttl;
        clone.protocol = protocol;
        clone.checksum = checksum;
        clone.empfaenger = empfaenger;
        clone.sender = sender;
        clone.data = data;
        return clone;
    }

    public int getChecksum() {
        return checksum;
    }

    public void setChecksum(int checksum) {
        this.checksum = checksum;
    }

    public String getEmpfaenger() {
        return empfaenger;
    }

    public void setEmpfaenger(String empfaenger) {
        this.empfaenger = empfaenger;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public boolean isDf() {
        return df;
    }

    public void setDf(boolean df) {
        this.df = df;
    }

    public boolean isFf() {
        return ff;
    }

    public void setFf(boolean ff) {
        this.ff = ff;
    }

    public int getIdentification() {
        return identification;
    }

    public void setIdentification(int identification) {
        this.identification = identification;
    }

    public int getIhl() {
        return ihl;
    }

    public void setIhl(int ihl) {
        this.ihl = ihl;
    }

    public boolean isMf() {
        return mf;
    }

    public void setMf(boolean mf) {
        this.mf = mf;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public int getTos() {
        return tos;
    }

    public void setTos(int tos) {
        this.tos = tos;
    }

    public int getTotalLength() {
        return totalLength;
    }

    public void setTotalLength(int totalLength) {
        this.totalLength = totalLength;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Object getSegment() {
        return data;
    }

    public void setSegment(Object data) {
        this.data = data;
    }

    public String toString() {
        return "[" + "version=" + version + ", " + "ihl=" + ihl + ", " + "tos=" + tos + ", " + "totalLength="
                + totalLength + ", " + "identification=" + identification + ", " + "offset=" + offset + ", " + "ff="
                + ff + ", " + "df=" + df + ", " + "mf=" + mf + ", " + "ttl=" + ttl + ", " + "protocol=" + protocol
                + ", " + "checksum=" + checksum + ", " + "empfaenger=" + empfaenger + ", " + "sender=" + sender + "]";
    }

    public void decrementTtl() {
        ttl--;
    }
}
