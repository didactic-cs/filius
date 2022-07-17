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
package filius.software.dns;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import filius.exception.TimeOutException;
import filius.exception.VerbindungsException;
import filius.hardware.Verbindung;
import filius.software.system.InternetKnotenBetriebssystem;
import filius.software.transportschicht.UDPSocket;

public class DNSQueryAgent {
    private static final Logger LOG = LoggerFactory.getLogger(DNSQueryAgent.class);

    /**
     * Methode zum Abruf eines Resource Record. Das Rueckgabeformat ist NAME TYPE CLASS TTL RDATA (Bsp. web.de. A IN
     * 3600 217.72.195.42)
     * 
     * @param typ
     * @param domainname
     *            must end with '.'
     * @return DNS response from remote server or null if there is no answer at all.
     */
    public DNSNachricht query(String typ, String domainname, String dnsServer, InternetKnotenBetriebssystem os)
            throws TimeOutException {
        DNSNachricht antwort = null;

        if (dnsServer != null && !dnsServer.equals("")) {
            try {
                DNSNachricht anfrage = new DNSNachricht(DNSNachricht.QUERY);
                anfrage.hinzuQuery(domainname + " " + typ + " IN");

                UDPSocket socket = new UDPSocket(os, dnsServer, 53);
                socket.verbinden();
                socket.senden(anfrage.toString());
                String tmp = socket.empfangen(5 * Verbindung.holeRTT());
                socket.schliessen();

                if (tmp != null) {
                    antwort = new DNSNachricht(tmp);
                } else {
                    LOG.debug("Timeout while waiting for DNS query response");
                    throw new TimeOutException();
                }
            } catch (VerbindungsException e) {
                LOG.debug("Could not connect to DNS server", e);
            }
        }
        return antwort;
    }
}
