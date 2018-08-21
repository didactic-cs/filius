package filius.software.dhcp;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class DHCPMessageTest {

    @Test
    public void testTypeEnum_valueOf() throws Exception {
        assertThat(DHCPMessageType.fromString("DHCPACK"), is(DHCPMessageType.ACK));
        assertThat(DHCPMessageType.fromString("DHCPDECLINE"), is(DHCPMessageType.DECLINE));
        assertThat(DHCPMessageType.fromString("DHCPDISCOVER"), is(DHCPMessageType.DISCOVER));
        assertThat(DHCPMessageType.fromString("DHCPNAK"), is(DHCPMessageType.NACK));
        assertThat(DHCPMessageType.fromString("DHCPOFFER"), is(DHCPMessageType.OFFER));
        assertThat(DHCPMessageType.fromString("DHCPREQUEST"), is(DHCPMessageType.REQUEST));
    }

    @Test
    public void testToString_WithoutInitialization() throws Exception {
        assertThat(new DHCPMessage().toString(), is("DHCPDISCOVER\nyiaddr=0.0.0.0"));
    }

    @Test
    public void testToString_WithInitialization() throws Exception {
        DHCPMessage dhcpMessage = prepareTestMessage(DHCPMessageType.OFFER, "192.168.1.111", "abcdefgh", "192.168.1.5",
                "192.168.1.1", "255.255.255.0", "1.2.3.4", "5.6.7.8");
        assertThat(dhcpMessage.toString(), is("DHCPOFFER"
                + "\nyiaddr=192.168.1.111\nchaddr=abcdefgh\nrouter=192.168.1.1"
                + "\nsubnetmask=255.255.255.0\ndnsserver=192.168.1.5" + "\nrequested=1.2.3.4\nserverident=5.6.7.8"));
    }

    private DHCPMessage prepareTestMessage(DHCPMessageType type, String yiaddr, String chaddr, String dnsServer,
            String router, String subnetMask, String requestedAddress, String serverIdent) {
        DHCPMessage dhcpMessage = new DHCPMessage();
        dhcpMessage.type = type;
        dhcpMessage.yiaddr = yiaddr;
        dhcpMessage.chaddr = chaddr;
        dhcpMessage.optionDnsServer = dnsServer;
        dhcpMessage.optionRouter = router;
        dhcpMessage.optionSubnetMask = subnetMask;
        dhcpMessage.optionRequestedAddress = requestedAddress;
        dhcpMessage.optionServerIdentifier = serverIdent;
        return dhcpMessage;
    }

    @Test
    public void testFromString() throws Exception {
        String dhcpMessageString = "DHCPOFFER" + "\nyiaddr=192.168.1.111\nchaddr=abcdefgh\nrouter=192.168.1.1\n"
                + "subnetmask=255.255.255.0\ndnsserver=192.168.1.5" + "\nrequested=1.2.3.4\nserverident=5.6.7.8";

        DHCPMessage dhcpMessage = DHCPMessage.fromString(dhcpMessageString);

        assertThat(dhcpMessage.type, is(DHCPMessageType.OFFER));
        assertThat(dhcpMessage.yiaddr, is("192.168.1.111"));
        assertThat(dhcpMessage.chaddr, is("abcdefgh"));
        assertThat(dhcpMessage.optionDnsServer, is("192.168.1.5"));
        assertThat(dhcpMessage.optionRouter, is("192.168.1.1"));
        assertThat(dhcpMessage.optionSubnetMask, is("255.255.255.0"));
        assertThat(dhcpMessage.optionRequestedAddress, is("1.2.3.4"));
        assertThat(dhcpMessage.optionServerIdentifier, is("5.6.7.8"));
    }

    @Test
    public void testToStringFromString() throws Exception {
        DHCPMessage dhcpMessage = prepareTestMessage(DHCPMessageType.OFFER, "192.168.1.111", "abcdefgh", "192.168.1.5",
                "192.168.1.1", "255.255.255.0", "1.2.3.4", "5.6.7.8");

        DHCPMessage result = DHCPMessage.fromString(dhcpMessage.toString());

        assertThat(result, is(dhcpMessage));
    }

    @Test
    public void testCreateDiscoverMessage() throws Exception {
        String clientMacAddress = "01:23:45:AB:CD:EF";

        DHCPMessage message = DHCPMessage.createDiscoverMessage(clientMacAddress);

        assertThat(message.getType(), is(DHCPMessageType.DISCOVER));
        assertThat(message.getChaddr(), is(clientMacAddress));
        assertThat(message.getYiaddr(), is("0.0.0.0"));
    }

    @Test
    public void testCreateOfferMessage() throws Exception {
        String clientMacAddress = "01:23:45:AB:CD:EF";
        String offeredIpAddress = "1.2.3.4";
        String subnetMask = "255.255.0.0";
        String router = "5.6.7.8";
        String dnsServer = "9.10.11.12";
        String serverIdentifier = "14.15.16.17";

        DHCPMessage message = DHCPMessage.createOfferMessage(clientMacAddress, offeredIpAddress, subnetMask, router,
                dnsServer, serverIdentifier);

        assertThat(message.getType(), is(DHCPMessageType.OFFER));
        assertThat(message.getChaddr(), is(clientMacAddress));
        assertThat(message.getYiaddr(), is(offeredIpAddress));
        assertThat(message.getSubnetMask(), is(subnetMask));
        assertThat(message.getRouter(), is(router));
        assertThat(message.getDnsServer(), is(dnsServer));
        assertThat(message.getServerIdentifier(), is(serverIdentifier));
    }

    @Test
    public void testCreateRequestMessage() throws Exception {
        String clientMacAddress = "01:23:45:AB:CD:EF";
        String requestedIpAddress = "1.2.3.4";
        String serverIdentifier = "14.15.16.17";

        DHCPMessage message = DHCPMessage.createRequestMessage(clientMacAddress, requestedIpAddress, serverIdentifier);

        assertThat(message.getType(), is(DHCPMessageType.REQUEST));
        assertThat(message.getChaddr(), is(clientMacAddress));
        assertThat(message.getYiaddr(), is("0.0.0.0"));
        assertThat(message.getServerIdentifier(), is(serverIdentifier));
        assertThat(message.getRequestedAddress(), is(requestedIpAddress));
    }

    @Test
    public void testCreateDeclineMessage() throws Exception {
        String clientMacAddress = "01:23:45:AB:CD:EF";
        String requestedIpAddress = "1.2.3.4";
        String serverIdentifier = "14.15.16.17";

        DHCPMessage message = DHCPMessage.createDeclineMessage(clientMacAddress, requestedIpAddress, serverIdentifier);

        assertThat(message.getType(), is(DHCPMessageType.DECLINE));
        assertThat(message.getChaddr(), is(clientMacAddress));
        assertThat(message.getYiaddr(), is("0.0.0.0"));
        assertThat(message.getServerIdentifier(), is(serverIdentifier));
        assertThat(message.getRequestedAddress(), is(requestedIpAddress));
    }

    @Test
    public void testCreateAckMessage() throws Exception {
        String clientMacAddress = "01:23:45:AB:CD:EF";
        String acknowledgedIpAddress = "1.2.3.4";
        String serverIdentifier = "14.15.16.17";

        DHCPMessage message = DHCPMessage.createAckMessage(clientMacAddress, acknowledgedIpAddress, serverIdentifier);

        assertThat(message.getType(), is(DHCPMessageType.ACK));
        assertThat(message.getChaddr(), is(clientMacAddress));
        assertThat(message.getYiaddr(), is(acknowledgedIpAddress));
        assertThat(message.getServerIdentifier(), is(serverIdentifier));
    }

    @Test
    public void testCreateNackMessage() throws Exception {
        String clientMacAddress = "01:23:45:AB:CD:EF";
        String notAcknowledgedIpAddress = "1.2.3.4";
        String serverIdentifier = "14.15.16.17";

        DHCPMessage message = DHCPMessage.createNackMessage(clientMacAddress, notAcknowledgedIpAddress,
                serverIdentifier);

        assertThat(message.getType(), is(DHCPMessageType.NACK));
        assertThat(message.getChaddr(), is(clientMacAddress));
        assertThat(message.getYiaddr(), is(notAcknowledgedIpAddress));
        assertThat(message.getServerIdentifier(), is(serverIdentifier));
    }
}
