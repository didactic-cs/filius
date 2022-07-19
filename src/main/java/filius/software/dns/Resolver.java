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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import filius.exception.TimeOutException;
import filius.software.clientserver.ClientAnwendung;

/**
 * Ausschnitt aus dem RFC 1035: <br />
 * <b>Question section format </b> <br />
 * The question section is used to carry the "question" in most queries, i.e., the parameters that define what is being
 * asked. The section contains QDCOUNT (usually 1) entries, each of the following format: ...
 * <ol>
 * <li>QNAME a domain name represented as a sequence of labels, where each label consists of a length octet followed by
 * that number of octets. The domain name terminates with the zero length octet for the null label of the root. Note
 * that this field may be an odd number of octets; no padding is used.</li>
 * <li>QTYPE a two octet code which specifies the type of the query. The values for this field include all codes valid
 * for a TYPE field, together with some more general codes which can match more than one type of RR.</li>
 * <li>QCLASS a two octet code that specifies the class of the query. For example, the QCLASS field is IN for the
 * Internet.</li>
 * </ol>
 * 
 * <b> Resource record format </b> <br />
 * The answer, authority, and additional sections all share the same format: a variable number of resource records,
 * where the number of records is specified in the corresponding count field in the header. Each resource record has the
 * following format: ...
 * <ol>
 * <li>NAME a domain name to which this resource record pertains.</li>
 * <li>TYPE two octets containing one of the RR type codes. This field specifies the meaning of the data in the RDATA
 * field.</li>
 * <li>CLASS two octets which specify the class of the data in the RDATA field.</li>
 * <li>TTL a 32 bit unsigned integer that specifies the time interval (in seconds) that the resource record may be
 * cached before it should be discarded. Zero values are interpreted to mean that the RR can only be used for the
 * transaction in progress, and should not be cached.</li>
 * <li>RDLENGTH an unsigned 16 bit integer that specifies the length in octets of the RDATA field.</li>
 * <li>RDATA a variable length string of octets that describes the resource. The format of this information varies
 * according to the TYPE and CLASS of the resource record. For example, the if the TYPE is A and the CLASS is IN, the
 * RDATA field is a 4 octet ARPA Internet address.</li>
 * </ol>
 */

public class Resolver extends ClientAnwendung {
    private static Logger LOG = LoggerFactory.getLogger(Resolver.class);

    private static Pattern LOCALHOST_PATTERN = Pattern.compile("localhost\\.?", Pattern.CASE_INSENSITIVE);

    private DNSQueryAgent queryAgent = new DNSQueryAgent();

    public String holeIPAdresse(String domainname) throws TimeOutException {
        return holeIPAdresse(domainname, getSystemSoftware().getDNSServer());
    }

    /**
     * Methode zur Aufloesung eines Domainnamens zu einer IP-Adresse.
     * 
     * @param domainname
     * @return
     * @throws TimeOutException
     */
    public String holeIPAdresse(String domainname, String dnsServer) throws TimeOutException {
        if (!domainname.matches(".*\\.$")) {
            domainname += ".";
        }

        DNSNachricht data = resolveA(domainname, dnsServer);
        ResourceRecord rr = ResourceRecord.findRecord(domainname, ResourceRecord.ADDRESS, data.holeResourceRecords());
        return null != rr ? rr.getRdata() : null;
    }

    /**
     * Resolve domain name (but not an IP address!)
     * 
     * @throws TimeOutException
     */
    public DNSNachricht resolveA(String domainname, String dnsServer) throws TimeOutException {
        return resolve(domainname, ResourceRecord.ADDRESS, dnsServer);
    }

    public String holeIPAdresseMailServer(String domainname) throws TimeOutException {
        if (!domainname.matches(".*\\.$")) {
            domainname += ".";
        }

        List<ResourceRecord> dataRecords = resolveMX(domainname, getSystemSoftware().getDNSServer())
                .holeResourceRecords();
        ResourceRecord mxRecord = ResourceRecord.findRecord(domainname, ResourceRecord.MAIL_EXCHANGE, dataRecords);
        ResourceRecord aRecord = null == mxRecord ? null
                : ResourceRecord.findRecord(mxRecord.getRdata(), ResourceRecord.ADDRESS, dataRecords);
        return aRecord == null ? null : aRecord.getRdata();
    }

    protected List<ResourceRecord> extractRankedResourceRecordList(DNSNachricht antwort, String type,
            String domainname) {
        List<ResourceRecord> relevantResourceRecords = new ArrayList<>();
        if (antwort != null) {
            relevantResourceRecords
                    .addAll(extractResourceRecords(antwort.holeAntwortResourceRecords(), type, domainname));
            relevantResourceRecords
                    .addAll(extractResourceRecords(antwort.holeAuthoritativeResourceRecords(), type, domainname));
            relevantResourceRecords
                    .addAll(extractResourceRecords(antwort.holeZusatzResourceRecords(), type, domainname));
        }
        return relevantResourceRecords;
    }

    public DNSNachricht resolveMX(String domainname, String dnsServerAddress) throws TimeOutException {
        return resolve(domainname, ResourceRecord.MAIL_EXCHANGE, dnsServerAddress);
    }

    public DNSNachricht resolve(String domainname, String type, String dnsServerAddress) throws TimeOutException {
        if (!domainname.matches(".*\\.$")) {
            domainname += ".";
        }

        DNSNachricht response = new DNSNachricht(DNSNachricht.RESPONSE);
        String serverToQuery = null;
        if (LOCALHOST_PATTERN.matcher(domainname).matches()) {
            response.setLocal();
            response.hinzuAntwortResourceRecord(ResourceRecord.LOCALHOST_ADDRESS);
        } else {
            serverToQuery = dnsServerAddress;
        }
        while (serverToQuery != null) {
            DNSNachricht remoteResponse = queryAgent.query(type, domainname, serverToQuery, getSystemSoftware());
            if (remoteResponse == null) {
                break;
            }

            copyResourceRecords(remoteResponse, response, domainname, type);
            List<ResourceRecord> data = extractRankedResourceRecordList(remoteResponse, type, domainname);
            if (!data.isEmpty()
                    && (ResourceRecord.MAIL_EXCHANGE.equals(type) || ResourceRecord.NAME_SERVER.equals(type))) {
                copyResourceRecords(remoteResponse, response, data.get(0).getRdata(), ResourceRecord.ADDRESS);
            }
            String nextServerToQuery = extractAddressForNameServer(remoteResponse.holeResourceRecords(), domainname);
            serverToQuery = !serverToQuery.equals(nextServerToQuery) ? nextServerToQuery : null;
        }
        LOG.debug("resolved dns query for {} {}: {}", domainname, type, response.holeResourceRecords());
        return response;
    }

    private void copyResourceRecords(DNSNachricht source, DNSNachricht target, String domainname, String type) {
        target.hinzuAntwortResourceRecords(
                extractResourceRecords(source.holeAntwortResourceRecords(), type, domainname));
        target.hinzuAuthoritativeResourceRecords(
                extractResourceRecords(source.holeAuthoritativeResourceRecords(), type, domainname));
    }

    public DNSNachricht resolveNS(String domainname) throws TimeOutException {
        return resolve(domainname, ResourceRecord.NAME_SERVER, getSystemSoftware().getDNSServer());
    }

    protected String extractAddressForNameServer(List<ResourceRecord> rrList, String domain) {
        String dnsServerAddress = null;
        ResourceRecord nsRecord = ResourceRecord.findApplicableNSRecord(domain, rrList);
        if (null != nsRecord) {
            ResourceRecord aRecord = ResourceRecord.findRecord(nsRecord.getRdata(), ResourceRecord.ADDRESS, rrList);
            if (null != aRecord) {
                dnsServerAddress = aRecord.getRdata();
            }
        }
        return dnsServerAddress;
    }

    protected List<ResourceRecord> extractResourceRecords(List<ResourceRecord> liste, String typ, String domainname) {
        List<ResourceRecord> relevantRR = new ArrayList<>();
        for (ResourceRecord rr : liste) {
            if (rr.getDomainname().equalsIgnoreCase(domainname) && rr.getType().equals(typ)) {
                relevantRR.add(rr);
            }
        }
        return relevantRR;
    }
}
