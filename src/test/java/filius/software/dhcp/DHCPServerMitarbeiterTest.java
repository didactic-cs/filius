package filius.software.dhcp;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import filius.exception.AddressRequestNotAcceptedException;
import filius.exception.NoAvailableAddressException;
import filius.software.transportschicht.UDPSocket;

public class DHCPServerMitarbeiterTest {
    @Mock
    private DHCPServer serverMock;
    @Mock
    private UDPSocket socketMock;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testProcessDiscover() throws Exception {
        DHCPServerMitarbeiter mitarbeiter = new DHCPServerMitarbeiter(serverMock, socketMock);
        String mac = "01:02:03:04:05:06";
        String ip = "10.0.0.101";
        String serverIdentifier = "10.0.0.1";
        String dnsServer = "10.0.0.22";
        String router = "10.0.0.33";
        String subnetMask = "255.255.0.0";
        when(serverMock.offerAddress(mac)).thenReturn(ip);
        when(serverMock.holeServerIpAddress()).thenReturn(serverIdentifier);
        when(serverMock.getDnsserverip()).thenReturn(dnsServer);
        when(serverMock.getGatewayip()).thenReturn(router);
        when(serverMock.getSubnetzmaske()).thenReturn(subnetMask);

        mitarbeiter.processDiscover(mac);

        DHCPMessage expectedResponse = DHCPMessage.createOfferMessage(mac, ip, subnetMask, router, dnsServer,
                serverIdentifier);
        verify(socketMock, times(1)).sendeBroadcast(serverIdentifier, expectedResponse.toString());
    }

    @Test
    public void testProcessDiscover_NoOfferPossible() throws Exception {
        DHCPServerMitarbeiter mitarbeiter = new DHCPServerMitarbeiter(serverMock, socketMock);
        String mac = "01:02:03:04:05:06";
        String serverIdentifier = "10.0.0.1";
        String dnsServer = "10.0.0.22";
        String router = "10.0.0.33";
        String subnetMask = "255.255.0.0";
        when(serverMock.offerAddress(mac)).thenThrow(new NoAvailableAddressException());
        when(serverMock.holeServerIpAddress()).thenReturn(serverIdentifier);
        when(serverMock.getDnsserverip()).thenReturn(dnsServer);
        when(serverMock.getGatewayip()).thenReturn(router);
        when(serverMock.getSubnetzmaske()).thenReturn(subnetMask);

        mitarbeiter.processDiscover(mac);

        verify(socketMock, never()).sendeBroadcast(anyString(), anyString());
    }

    @Test
    public void testProcessRequest_Ack() throws Exception {
        DHCPServerMitarbeiter mitarbeiter = new DHCPServerMitarbeiter(serverMock, socketMock);
        String mac = "01:02:03:04:05:06";
        String ip = "10.0.0.101";
        String serverIdentifier = "10.0.0.1";
        String dnsServer = "10.0.0.22";
        String router = "10.0.0.33";
        String subnetMask = "255.255.0.0";
        when(serverMock.holeServerIpAddress()).thenReturn(serverIdentifier);
        when(serverMock.getDnsserverip()).thenReturn(dnsServer);
        when(serverMock.getGatewayip()).thenReturn(router);
        when(serverMock.getSubnetzmaske()).thenReturn(subnetMask);
        when(serverMock.requestAddress(mac, ip)).thenReturn(
                new DHCPAddressAssignment(mac, ip, System.currentTimeMillis() + 1000));

        mitarbeiter.processRequest(mac, ip, serverIdentifier);

        DHCPMessage expectedResponse = DHCPMessage.createAckMessage(mac, ip, serverIdentifier);
        verify(socketMock, times(1)).sendeBroadcast(serverIdentifier, expectedResponse.toString());
    }

    @Test
    public void testProcessRequest_Nack() throws Exception {
        DHCPServerMitarbeiter mitarbeiter = new DHCPServerMitarbeiter(serverMock, socketMock);
        String mac = "01:02:03:04:05:06";
        String ip = "10.0.0.101";
        String serverIdentifier = "10.0.0.1";
        String dnsServer = "10.0.0.22";
        String router = "10.0.0.33";
        String subnetMask = "255.255.0.0";
        when(serverMock.holeServerIpAddress()).thenReturn(serverIdentifier);
        when(serverMock.getDnsserverip()).thenReturn(dnsServer);
        when(serverMock.getGatewayip()).thenReturn(router);
        when(serverMock.getSubnetzmaske()).thenReturn(subnetMask);
        when(serverMock.requestAddress(mac, ip)).thenThrow(new AddressRequestNotAcceptedException());

        mitarbeiter.processRequest(mac, ip, serverIdentifier);

        DHCPMessage expectedResponse = DHCPMessage.createNackMessage(mac, ip, serverIdentifier);
        verify(socketMock, times(1)).sendeBroadcast(serverIdentifier, expectedResponse.toString());
    }

    @Test
    public void testProcessRequest_WrongServerIdentifier_Ignore() throws Exception {
        DHCPServerMitarbeiter mitarbeiter = new DHCPServerMitarbeiter(serverMock, socketMock);
        String mac = "01:02:03:04:05:06";
        String ip = "10.0.0.101";
        String serverIdentifier = "10.0.0.1";
        String dnsServer = "10.0.0.22";
        String router = "10.0.0.33";
        String subnetMask = "255.255.0.0";
        when(serverMock.holeServerIpAddress()).thenReturn(serverIdentifier);
        when(serverMock.getDnsserverip()).thenReturn(dnsServer);
        when(serverMock.getGatewayip()).thenReturn(router);
        when(serverMock.getSubnetzmaske()).thenReturn(subnetMask);
        when(serverMock.requestAddress(mac, ip)).thenThrow(new AddressRequestNotAcceptedException());

        mitarbeiter.processRequest(mac, ip, "10.0.0.42");

        verify(socketMock, never()).sendeBroadcast(anyString(), anyString());
    }
}
