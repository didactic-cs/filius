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

import static filius.software.dns.ResourceRecord.ADDRESS;
import static filius.software.dns.ResourceRecord.MAIL_EXCHANGE;
import static filius.software.dns.ResourceRecord.NAME_SERVER;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import filius.software.system.InternetKnotenBetriebssystem;
import filius.software.vermittlungsschicht.IP;

public class ResolverTest {

    private static final String ROOT_DNS_SERVER_ADDRESS = "9.9.9.9";
    private static final String HELLO_WORLD_MX = "mail.hello.world.";
    private static final String WORLD_NS_ADDRESS = "3.3.3.3";
    private static final String HELLO_WORLD_ADDRESS = "10.10.10.10";
    private static final String HELLO_WORLD_DOMAIN = "hello.world.";
    private static final String DNS_SERVER_ADDRESS = "1.1.1.1";

    @InjectMocks
    private Resolver resolver;
    @Mock
    private DNSQueryAgent queryAgentMock;

    @Before
    public void initMocks() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testResolveA_Localhost() throws Exception {
        DNSNachricht response = resolver.resolveA("localHOST", DNS_SERVER_ADDRESS);

        assertTrue(response.isLocal());
        assertThat(response.holeAntwortResourceRecords().getFirst().getRdata(), is(IP.LOCALHOST));
        verify(queryAgentMock, never()).query(anyString(), anyString(), anyString(),
                any(InternetKnotenBetriebssystem.class));
    }

    @Test
    public void testResolveA_DirectResponse_Success() throws Exception {
        DNSNachricht dnsResponse = new DNSNachricht(DNSNachricht.RESPONSE);
        addExampleARecord(dnsResponse);
        when(queryAgentMock.query(ADDRESS, HELLO_WORLD_DOMAIN, DNS_SERVER_ADDRESS, null)).thenReturn(dnsResponse);

        DNSNachricht response = resolver.resolveA(HELLO_WORLD_DOMAIN, DNS_SERVER_ADDRESS);

        assertFalse(response.isLocal());
        assertThat(response.holeAntwortResourceRecords().getFirst().getRdata(), is(HELLO_WORLD_ADDRESS));
    }

    private void addExampleARecord(DNSNachricht dnsResponse) {
        dnsResponse.hinzuAntwortResourceRecord(new ResourceRecord(HELLO_WORLD_DOMAIN, ADDRESS, HELLO_WORLD_ADDRESS));
    }

    @Test
    public void testResolve_RequiresOtherNameserver_Success() throws Exception {
        DNSNachricht nameserverResponse = new DNSNachricht(DNSNachricht.RESPONSE);
        addExampleNSRecords(nameserverResponse, ".", "ns.world.", WORLD_NS_ADDRESS);
        when(queryAgentMock.query(MAIL_EXCHANGE, HELLO_WORLD_DOMAIN, DNS_SERVER_ADDRESS, null))
                .thenReturn(nameserverResponse);

        DNSNachricht addressResponse = new DNSNachricht(DNSNachricht.RESPONSE);
        addressResponse
                .hinzuAntwortResourceRecord(new ResourceRecord(HELLO_WORLD_DOMAIN, MAIL_EXCHANGE, HELLO_WORLD_MX));
        when(queryAgentMock.query(MAIL_EXCHANGE, HELLO_WORLD_DOMAIN, DNS_SERVER_ADDRESS, null))
                .thenReturn(addressResponse);

        DNSNachricht response = resolver.resolve(HELLO_WORLD_DOMAIN, MAIL_EXCHANGE, DNS_SERVER_ADDRESS);

        assertFalse(response.isLocal());
        assertThat(response.holeAntwortResourceRecords().getFirst().getRdata(), is(HELLO_WORLD_MX));
    }

    @Test
    public void testResolve_NoAvailableData() throws Exception {
        DNSNachricht nameserverResponse = new DNSNachricht(DNSNachricht.RESPONSE);
        addExampleNSRecords(nameserverResponse, ".", "ns.world.", WORLD_NS_ADDRESS);
        when(queryAgentMock.query(MAIL_EXCHANGE, HELLO_WORLD_DOMAIN, DNS_SERVER_ADDRESS, null))
                .thenReturn(nameserverResponse);

        DNSNachricht addressResponse = new DNSNachricht(DNSNachricht.RESPONSE);
        when(queryAgentMock.query(MAIL_EXCHANGE, HELLO_WORLD_DOMAIN, DNS_SERVER_ADDRESS, null))
                .thenReturn(addressResponse);

        DNSNachricht response = resolver.resolve(HELLO_WORLD_DOMAIN, MAIL_EXCHANGE, DNS_SERVER_ADDRESS);

        assertFalse(response.isLocal());
        assertTrue(response.holeAntwortResourceRecords().isEmpty());
    }

    @Test
    public void testResolve_ResolveRecursively_Success() throws Exception {
        DNSNachricht nameserverResponse1 = new DNSNachricht(DNSNachricht.RESPONSE);
        addExampleNSRecords(nameserverResponse1, ".", "ns-root.", ROOT_DNS_SERVER_ADDRESS);
        when(queryAgentMock.query(ADDRESS, HELLO_WORLD_DOMAIN, DNS_SERVER_ADDRESS, null))
                .thenReturn(nameserverResponse1);

        DNSNachricht nameserverResponse2 = new DNSNachricht(DNSNachricht.RESPONSE);
        addExampleNSRecords(nameserverResponse2, "world.", "ns.world.", WORLD_NS_ADDRESS);
        when(queryAgentMock.query(ADDRESS, HELLO_WORLD_DOMAIN, ROOT_DNS_SERVER_ADDRESS, null))
                .thenReturn(nameserverResponse2);

        DNSNachricht addressResponse = new DNSNachricht(DNSNachricht.RESPONSE);
        addressResponse
                .hinzuAntwortResourceRecord(new ResourceRecord(HELLO_WORLD_DOMAIN, ADDRESS, HELLO_WORLD_ADDRESS));
        when(queryAgentMock.query(ADDRESS, HELLO_WORLD_DOMAIN, WORLD_NS_ADDRESS, null)).thenReturn(addressResponse);

        DNSNachricht response = resolver.resolve(HELLO_WORLD_DOMAIN, ADDRESS, DNS_SERVER_ADDRESS);

        assertFalse(response.isLocal());
        boolean foundRecord = false;
        for (ResourceRecord rr : response.holeAntwortResourceRecords()) {
            if (ADDRESS.equals(rr.getType())) {
                assertThat(rr.getRdata(), is(HELLO_WORLD_ADDRESS));
                foundRecord = true;
            }
        }
        assertTrue(foundRecord);
    }

    private void addExampleNSRecords(DNSNachricht nameserverResponse, String domain, String server, String ipAddress) {
        nameserverResponse.hinzuAntwortResourceRecord(new ResourceRecord(domain, NAME_SERVER, server));
        nameserverResponse.hinzuAntwortResourceRecord(new ResourceRecord(server, ADDRESS, ipAddress));
    }
}
