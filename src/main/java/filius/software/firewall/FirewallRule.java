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

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import filius.hardware.NetzwerkInterface;
import filius.rahmenprogramm.EingabenUeberpruefung;
import filius.software.vermittlungsschicht.VermittlungsProtokoll;

public class FirewallRule {

    /**
     * fake "IP" address to represent "any IP from same network" NOTE: netmask is irrelevant for this "special IP"
     */
    public static String SAME_NETWORK = "999.999.999.999";

    // see /etc/protocols
    public static final short ALL_PROTOCOLS = -1; // do not limit to specific protocol
    public static final short ICMP = 1;
    public static final short TCP = 6;
    public static final short UDP = 17;

    // ports; see /etc/services
    public static final int ALL_PORTS = -1;

    // action, if rules matches
    public static final short DROP = 0;
    public static final short ACCEPT = 1;

    // attributes; direct access tolerable... ;-)
    public String srcIP = "";
    public String srcMask = "";
    public String destIP = "";
    public String destMask = "";
    public int port = FirewallRule.ALL_PORTS;
    public short protocol = FirewallRule.TCP;
    public short action = FirewallRule.DROP;

    // constructor
    public FirewallRule(String srcIP, String srcMask, String destIP, String destMask, int port, short protocol,
            short action) {
        this.srcIP = srcIP;
        this.srcMask = srcMask;
        this.destIP = destIP;
        this.destMask = destMask;
        this.port = port;
        this.protocol = protocol;
        this.action = action;
    }

    public FirewallRule() {}

    public boolean validateExistingAttributes() {
        boolean result = true;
        if (StringUtils.isNoneBlank(srcIP)
                && !EingabenUeberpruefung.isGueltig(srcIP, EingabenUeberpruefung.musterIpAdresse)
                && !SAME_NETWORK.equals(srcIP)) {
            result = false;
        }
        if (StringUtils.isNoneBlank(srcMask) && !EingabenUeberpruefung.isValidSubnetmask(srcMask)) {
            result = false;
        }
        if (StringUtils.isNoneBlank(destIP)
                && !EingabenUeberpruefung.isGueltig(destIP, EingabenUeberpruefung.musterIpAdresse)) {
            result = false;
        }
        if (StringUtils.isNoneBlank(destMask) && !EingabenUeberpruefung.isValidSubnetmask(destMask)) {
            result = false;
        }
        if (port != ALL_PORTS
                && !EingabenUeberpruefung.isGueltig(String.valueOf(port), EingabenUeberpruefung.musterPort)) {
            result = false;
        }
        if (protocol != TCP && protocol != ICMP && protocol != UDP && protocol != ALL_PROTOCOLS) {
            result = false;
        }
        if (action != DROP && action != ACCEPT) {
            return false;
        }
        return result;
    }

    public String toString() {
        return toString(null);
    }

    public String toString(List<NetzwerkInterface> nics) {
        String ip = null;
        String mask = null;
        String result = "";
        boolean sameNet = false;

        if (nics != null) {
            ip = nics.get(0).getIp();
            mask = nics.get(0).getSubnetzMaske();
            if (srcIP.equals(FirewallRule.SAME_NETWORK) && ip != null && mask != null)
                sameNet = true;
        }

        if (StringUtils.isEmpty(srcIP))
            result += "*/";
        else if (sameNet)
            result += VermittlungsProtokoll.getSubnetForIp(ip, mask) + "/" + mask + " -> ";
        else
            result += srcIP + "/";
        if (sameNet) {} else if (StringUtils.isEmpty(srcMask))
            result += "* -> ";
        else
            result += srcMask + " -> ";
        if (StringUtils.isEmpty(destIP))
            result += "*/";
        else
            result += destIP + "/";
        if (StringUtils.isEmpty(destMask))
            result += "*; ";
        else
            result += destMask + "; ";
        if (protocol >= 0) {
            if (protocol == FirewallRule.TCP)
                result += "TCP:";
            else if (protocol == FirewallRule.UDP)
                result += "UDP:";
            else
                result += "?:";
        } else {
            result += "*:";
        }
        if (port >= 0)
            result += port + "  => ";
        else
            result += "*  => ";
        if (action == FirewallRule.ACCEPT)
            result += "ACCEPT";
        else if (action == FirewallRule.DROP)
            result += "DROP";
        else
            result += action;
        return result;
    }
}
