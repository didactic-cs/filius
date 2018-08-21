package filius.software.dhcp;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import filius.exception.NoValidDhcpResponseException;
import filius.exception.TimeOutException;
import filius.software.transportschicht.UDPSocket;
import filius.software.vermittlungsschicht.ARP;

public class DHCPClientTest {

    @InjectMocks
    private DHCPClient dhcpClient = new DHCPClient();
    @Mock
    private UDPSocket socketMock;
    @Mock
    private ARP arpMock;
    private long defaultSocketTimeoutMillis = 100;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testDiscover_SendAndReceive_Successfully() throws Exception {
        String clientMacAddress = "01:23:45:67:89:AB";
        String offeredIpAddress = "1.2.3.4";
        String subnetMask = "255.255.0.0";
        String router = "5.6.7.8";
        String dnsServer = "9.10.11.12";
        String serverIdentifier = "13.14.15.16";
        when(socketMock.empfangen(anyLong())).thenReturn(
                DHCPMessage.createOfferMessage(clientMacAddress, offeredIpAddress, subnetMask, router, dnsServer,
                        serverIdentifier).toString());

        dhcpClient.discover(socketMock, clientMacAddress, defaultSocketTimeoutMillis);

        verify(socketMock, times(1)).sendeBroadcast("0.0.0.0",
                DHCPMessage.createDiscoverMessage(clientMacAddress).toString());
    }

    @Test
    public void testDiscover_GetOffer() throws Exception {
        String clientMacAddress = "01:23:45:67:89:AB";
        String offeredIpAddress = "1.2.3.4";
        String subnetMask = "255.255.0.0";
        String router = "5.6.7.8";
        String dnsServer = "9.10.11.12";
        String serverIdentifier = "13.14.15.16";
        when(socketMock.empfangen(anyLong())).thenReturn(
                DHCPMessage.createOfferMessage(clientMacAddress, offeredIpAddress, subnetMask, router, dnsServer,
                        serverIdentifier).toString());

        IpConfig config = dhcpClient.discover(socketMock, clientMacAddress, defaultSocketTimeoutMillis);

        verify(socketMock, times(1)).sendeBroadcast("0.0.0.0",
                DHCPMessage.createDiscoverMessage(clientMacAddress).toString());
        assertThat(config.getIpAddress(), is(offeredIpAddress));
        assertThat(config.getDnsServer(), is(dnsServer));
        assertThat(config.getSubnetMask(), is(subnetMask));
        assertThat(config.getRouter(), is(router));
        assertThat(config.getDhcpServer(), is(serverIdentifier));
    }

    @Test(expected = TimeOutException.class)
    public void testDiscover_NoResponse() throws Exception {
        String clientMacAddress = "01:23:45:67:89:AB";
        when(socketMock.empfangen(anyLong())).thenReturn(null);

        dhcpClient.discover(socketMock, clientMacAddress, defaultSocketTimeoutMillis);
    }

    @Test(expected = NoValidDhcpResponseException.class)
    public void testDiscover_ResponseAfterTimeout() throws Exception {
        String clientMacAddress = "01:23:45:67:89:AB";
        when(socketMock.empfangen(anyLong())).thenAnswer(
                invocation -> {
                    Thread.sleep(defaultSocketTimeoutMillis + 100);
                    return DHCPMessage.createOfferMessage("andere_adresse", "1.2.3.4", "255.255.0.0", "5.6.7.8",
                            "9.10.11.12", "13.14.15.16").toString();
                }).thenAnswer(
                invocation -> DHCPMessage.createOfferMessage(clientMacAddress, "1.2.3.4", "255.255.0.0", "5.6.7.8",
                        "9.10.11.12", "13.14.15.16").toString());

        dhcpClient.discover(socketMock, clientMacAddress, defaultSocketTimeoutMillis);
    }

    @Test(expected = NoValidDhcpResponseException.class)
    public void testDiscover_NoResponseToRequest() throws Exception {
        String clientMacAddress = "01:23:45:67:89:AB";
        String[] messages = new String[100];
        for (int i = 0; i < 100; i++) {
            messages[i] = DHCPMessage.createDiscoverMessage(String.valueOf(i)).toString();
        }
        when(socketMock.empfangen(anyLong())).thenReturn(messages[0], Arrays.copyOfRange(messages, 1, messages.length));

        dhcpClient.discover(socketMock, clientMacAddress, defaultSocketTimeoutMillis);
    }

    @Test
    public void testDiscover_GetOffer_AfterIgnoredMessages() throws Exception {
        String clientMacAddress = "01:23:45:67:89:AB";
        String offeredIpAddress = "1.2.3.4";
        String subnetMask = "255.255.0.0";
        String router = "5.6.7.8";
        String dnsServer = "9.10.11.12";
        String serverIdentifier = "13.14.15.16";
        String[] messages = new String[11];
        for (int i = 0; i < 10; i++) {
            messages[i] = DHCPMessage.createDiscoverMessage(String.valueOf(i)).toString();
        }
        messages[10] = DHCPMessage.createOfferMessage(clientMacAddress, offeredIpAddress, subnetMask, router,
                dnsServer, serverIdentifier).toString();
        when(socketMock.empfangen(anyLong())).thenReturn(messages[0], Arrays.copyOfRange(messages, 1, messages.length));

        IpConfig config = dhcpClient.discover(socketMock, clientMacAddress, defaultSocketTimeoutMillis);

        verify(socketMock, times(1)).sendeBroadcast("0.0.0.0",
                DHCPMessage.createDiscoverMessage(clientMacAddress).toString());
        assertThat(config.getIpAddress(), is(offeredIpAddress));
        assertThat(config.getDnsServer(), is(dnsServer));
        assertThat(config.getSubnetMask(), is(subnetMask));
        assertThat(config.getRouter(), is(router));
        assertThat(config.getDhcpServer(), is(serverIdentifier));
    }

    @Test(expected = TimeOutException.class)
    public void testDiscover_IgnoreOffersForOtherClients() throws Exception {
        String clientMacAddress = "01:23:45:67:89:AB";
        String offeredIpAddress = "1.2.3.4";
        String subnetMask = "255.255.0.0";
        String router = "5.6.7.8";
        String dnsServer = "9.10.11.12";
        String serverIdentifier = "13.14.15.16";
        String[] messages = new String[11];
        for (int i = 0; i < 10; i++) {
            messages[i] = DHCPMessage.createOfferMessage("aa:aa:aa:aa:aa:aa", offeredIpAddress, subnetMask, router,
                    dnsServer, serverIdentifier).toString();
        }
        when(socketMock.empfangen(anyLong())).thenReturn(messages[0], Arrays.copyOfRange(messages, 1, messages.length));

        dhcpClient.discover(socketMock, clientMacAddress, defaultSocketTimeoutMillis);
    }

    @Test
    public void testValidateOfferedAddress_valid() throws Exception {
        String offeredAddress = "192.168.100.100";
        when(arpMock.holeARPTabellenEintrag(offeredAddress)).thenReturn(null);

        boolean valid = dhcpClient.validateOfferedAddress(arpMock, offeredAddress);

        verify(arpMock, times(1)).holeARPTabellenEintrag(offeredAddress);
        assertTrue(valid);
    }

    @Test
    public void testValidateOfferedAddress_NotValid() throws Exception {
        String offeredAddress = "192.168.100.100";
        when(arpMock.holeARPTabellenEintrag(offeredAddress)).thenReturn("aa:bb:cc:dd:ee:ff");

        boolean valid = dhcpClient.validateOfferedAddress(arpMock, offeredAddress);

        verify(arpMock, times(1)).holeARPTabellenEintrag(offeredAddress);
        assertFalse(valid);
    }

    @Test
    public void testRequest_Acknowledged() throws Exception {
        String clientMacAddress = "01:23:45:67:89:AB";
        IpConfig config = new IpConfig("1.2.3.4", "5.6.7.8", "255.255.255.128", "9.10.11.12", "13.14.15.16");
        when(socketMock.empfangen(anyLong())).thenReturn(
                DHCPMessage.createAckMessage(clientMacAddress, config.getIpAddress(), config.getDhcpServer())
                        .toString());

        boolean acknowledged = dhcpClient.request(socketMock, clientMacAddress, config, defaultSocketTimeoutMillis);

        verify(socketMock, times(1)).sendeBroadcast(
                "0.0.0.0",
                DHCPMessage.createRequestMessage(clientMacAddress, config.getIpAddress(), config.getDhcpServer())
                        .toString());
        verify(socketMock, times(1)).empfangen(anyLong());
        assertTrue(acknowledged);
    }

    @Test
    public void testRequest_NOTAcknowledged() throws Exception {
        String clientMacAddress = "01:23:45:67:89:AB";
        IpConfig config = new IpConfig("1.2.3.4", "5.6.7.8", "255.255.255.128", "9.10.11.12", "13.14.15.16");
        when(socketMock.empfangen(anyLong())).thenReturn(
                DHCPMessage.createNackMessage(clientMacAddress, config.getIpAddress(), config.getDhcpServer())
                        .toString());

        boolean acknowledged = dhcpClient.request(socketMock, clientMacAddress, config, defaultSocketTimeoutMillis);

        verify(socketMock, times(1)).sendeBroadcast(
                "0.0.0.0",
                DHCPMessage.createRequestMessage(clientMacAddress, config.getIpAddress(), config.getDhcpServer())
                        .toString());
        verify(socketMock, times(1)).empfangen(anyLong());
        assertFalse(acknowledged);
    }

    @Test(expected = TimeOutException.class)
    public void testRequest_IgnoreAcksForOtherClients() throws Exception {
        String clientMacAddress = "01:23:45:67:89:AB";
        IpConfig config = new IpConfig("1.2.3.4", "5.6.7.8", "255.255.255.128", "9.10.11.12", "13.14.15.16");
        String[] messages = new String[11];
        for (int i = 0; i < 10; i++) {
            messages[i] = DHCPMessage.createAckMessage("aa:aa:aa:aa:aa:aa", config.getIpAddress(),
                    config.getDhcpServer()).toString();
        }
        when(socketMock.empfangen(anyLong())).thenReturn(messages[0], Arrays.copyOfRange(messages, 1, messages.length));

        dhcpClient.request(socketMock, clientMacAddress, config, defaultSocketTimeoutMillis);
    }

    @Test(expected = TimeOutException.class)
    public void testRequest_Timeout() throws Exception {
        String clientMacAddress = "01:23:45:67:89:AB";
        IpConfig config = new IpConfig("1.2.3.4", "5.6.7.8", "255.255.255.128", "9.10.11.12", "13.14.15.16");
        when(socketMock.empfangen(anyLong())).thenReturn(null);

        dhcpClient.request(socketMock, clientMacAddress, config, defaultSocketTimeoutMillis);
    }

    @Test(expected = NoValidDhcpResponseException.class)
    public void testRequest_ResponseAfterTimeout() throws Exception {
        String clientMacAddress = "01:23:45:67:89:AB";
        IpConfig config = new IpConfig("1.2.3.4", "5.6.7.8", "255.255.255.128", "9.10.11.12", "13.14.15.16");
        when(socketMock.empfangen(anyLong())).thenAnswer(
                invocation -> {
                    Thread.sleep(defaultSocketTimeoutMillis + 100);
                    return DHCPMessage
                            .createAckMessage("andere_adresse", config.getIpAddress(), config.getDhcpServer())
                            .toString();
                }).thenAnswer(
                invocation -> DHCPMessage.createAckMessage(clientMacAddress, config.getIpAddress(),
                        config.getDhcpServer()).toString());

        dhcpClient.request(socketMock, clientMacAddress, config, defaultSocketTimeoutMillis);
    }

    @Test(expected = NoValidDhcpResponseException.class)
    public void testRequest_ResponseFromWrongServer() throws Exception {
        String clientMacAddress = "01:23:45:67:89:AB";
        IpConfig config = new IpConfig("1.2.3.4", "5.6.7.8", "255.255.255.128", "9.10.11.12", "13.14.15.16");
        when(socketMock.empfangen(anyLong())).thenAnswer(invocation -> {
            Thread.sleep(defaultSocketTimeoutMillis + 100);
            return DHCPMessage.createAckMessage(clientMacAddress, config.getIpAddress(), "7.7.7.7").toString();
        }).thenAnswer(invocation -> null);

        dhcpClient.request(socketMock, clientMacAddress, config, defaultSocketTimeoutMillis);
    }
}
