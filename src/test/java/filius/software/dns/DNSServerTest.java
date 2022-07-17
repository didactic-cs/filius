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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import filius.software.system.Dateisystem;
import filius.software.system.InternetKnotenBetriebssystem;

public class DNSServerTest {

    private static final String DEFAULT_NAMESERVER_ADDRESS = "1.1.1.1";
    @InjectMocks
    private DNSServer dnsServer;
    @Mock
    private Resolver resolverMock;
    @Mock
    private InternetKnotenBetriebssystem osMock;
    private Dateisystem filesystemDnsServer = new Dateisystem();

    @Before
    public void initMocks() {
        MockitoAnnotations.openMocks(this);

        when(osMock.getDateisystem()).thenReturn(filesystemDnsServer);
        when(osMock.getDNSServer()).thenReturn(DEFAULT_NAMESERVER_ADDRESS);
    }

    @Test
    public void testAnswer_AWithLocalDataOnly() throws Exception {
        Query query = new Query("hello.world.", ResourceRecord.ADDRESS);
        dnsServer.hinzuRecord("hello.world.", ResourceRecord.ADDRESS, "1.2.3.4");

        DNSNachricht response = dnsServer.answer(query);
        List<ResourceRecord> rrList = response.holeAntwortResourceRecords();

        assertThat(rrList.size(), is(1));
        assertThat(rrList.get(0).getDomainname(), is("hello.world."));
        assertThat(rrList.get(0).getType(), is(ResourceRecord.ADDRESS));
        assertThat(rrList.get(0).getRdata(), is("1.2.3.4"));
        verify(resolverMock, never()).resolveA(anyString(), anyString());
    }

    @Test
    public void testAnswer_NSWithLocalDataOnly() throws Exception {
        Query query = new Query("hello.world.", ResourceRecord.NAME_SERVER);
        dnsServer.hinzuRecord("hello.world.", ResourceRecord.NAME_SERVER, "ns.hello.world.");
        dnsServer.hinzuRecord("ns.hello.world.", ResourceRecord.ADDRESS, "1.2.3.4");

        DNSNachricht response = dnsServer.answer(query);
        List<ResourceRecord> rrList = response.holeAntwortResourceRecords();

        assertThat(rrList.size(), is(2));
        assertThat(rrList.get(0).getDomainname(), is("hello.world."));
        assertThat(rrList.get(0).getType(), is(ResourceRecord.NAME_SERVER));
        assertThat(rrList.get(0).getRdata(), is("ns.hello.world."));
        assertThat(rrList.get(1).getDomainname(), is("ns.hello.world."));
        assertThat(rrList.get(1).getType(), is(ResourceRecord.ADDRESS));
        assertThat(rrList.get(1).getRdata(), is("1.2.3.4"));
        verify(resolverMock, never()).resolveA(anyString(), anyString());
    }

    @Test
    public void testAnswer_MXWithLocalDataOnly() throws Exception {
        Query query = new Query("hello.world.", ResourceRecord.MAIL_EXCHANGE);
        dnsServer.hinzuRecord("hello.world.", ResourceRecord.MAIL_EXCHANGE, "mail.hello.world.");
        dnsServer.hinzuRecord("mail.hello.world.", ResourceRecord.ADDRESS, "1.2.3.4");

        DNSNachricht response = dnsServer.answer(query);
        List<ResourceRecord> rrList = response.holeAntwortResourceRecords();

        assertThat(rrList.size(), is(2));
        assertThat(rrList.get(0).getDomainname(), is("hello.world."));
        assertThat(rrList.get(0).getType(), is(ResourceRecord.MAIL_EXCHANGE));
        assertThat(rrList.get(0).getRdata(), is("mail.hello.world."));
        assertThat(rrList.get(1).getDomainname(), is("mail.hello.world."));
        assertThat(rrList.get(1).getType(), is(ResourceRecord.ADDRESS));
        assertThat(rrList.get(1).getRdata(), is("1.2.3.4"));
        verify(resolverMock, never()).resolveA(anyString(), anyString());
    }

    @Test
    public void testAnswer_AWithResolver_UseDefaultNameserver() throws Exception {
        Query query = new Query("hello.world.", ResourceRecord.ADDRESS);
        dnsServer.hinzuRecord("not-matching.hello.world.", ResourceRecord.ADDRESS, "4.3.2.1");
        DNSNachricht resolverResponse = new DNSNachricht(DNSNachricht.RESPONSE);
        resolverResponse
                .hinzuAntwortResourceRecord(new ResourceRecord("hello.world.", ResourceRecord.ADDRESS, "1.2.3.4"));
        when(resolverMock.resolve(anyString(), anyString(), eq(DEFAULT_NAMESERVER_ADDRESS)))
                .thenReturn(resolverResponse);
        dnsServer.setRecursiveResolutionEnabled(true);

        DNSNachricht response = dnsServer.answer(query);
        List<ResourceRecord> rrList = response.holeAntwortResourceRecords();

        assertThat(rrList.size(), is(1));
        assertThat(rrList.get(0).getDomainname(), is("hello.world."));
        assertThat(rrList.get(0).getType(), is(ResourceRecord.ADDRESS));
        assertThat(rrList.get(0).getRdata(), is("1.2.3.4"));
    }

    @Test
    public void testAnswer_AWithResolver_UseDomainNameserver() throws Exception {
        Query query = new Query("hello.world.", ResourceRecord.ADDRESS);
        dnsServer.hinzuRecord("not-matching.hello.world.", ResourceRecord.ADDRESS, "4.3.2.1");
        dnsServer.hinzuRecord("world.", ResourceRecord.NAME_SERVER, "ns.world.");
        dnsServer.hinzuRecord("ns.world.", ResourceRecord.ADDRESS, "2.2.2.2");
        DNSNachricht resolverResponse = new DNSNachricht(DNSNachricht.RESPONSE);
        resolverResponse
                .hinzuAntwortResourceRecord(new ResourceRecord("hello.world.", ResourceRecord.ADDRESS, "1.2.3.4"));
        when(resolverMock.resolve(anyString(), anyString(), eq("2.2.2.2"))).thenReturn(resolverResponse);
        dnsServer.setRecursiveResolutionEnabled(true);

        DNSNachricht response = dnsServer.answer(query);
        List<ResourceRecord> rrList = response.holeAntwortResourceRecords();

        verify(resolverMock, never()).resolve(anyString(), anyString(), eq(DEFAULT_NAMESERVER_ADDRESS));
        assertThat(rrList.size(), is(1));
        assertThat(rrList.get(0).getDomainname(), is("hello.world."));
        assertThat(rrList.get(0).getType(), is(ResourceRecord.ADDRESS));
        assertThat(rrList.get(0).getRdata(), is("1.2.3.4"));
    }

    @Test
    public void testAnswer_NoMatchingData_AddNameserver() throws Exception {
        Query query = new Query("hello.world.", ResourceRecord.ADDRESS);
        dnsServer.hinzuRecord("world.", ResourceRecord.NAME_SERVER, "ns.world.");
        dnsServer.hinzuRecord("ns.world.", ResourceRecord.ADDRESS, "2.2.2.2");
        when(resolverMock.resolve(anyString(), anyString(), eq("2.2.2.2")))
                .thenReturn(new DNSNachricht(DNSNachricht.RESPONSE));
        dnsServer.setRecursiveResolutionEnabled(false);

        DNSNachricht response = dnsServer.answer(query);
        List<ResourceRecord> rrList = response.holeAntwortResourceRecords();

        assertThat(rrList.size(), is(2));
        assertThat(rrList.get(0).getType(), is(ResourceRecord.NAME_SERVER));
        assertThat(rrList.get(1).getType(), is(ResourceRecord.ADDRESS));
    }
}
