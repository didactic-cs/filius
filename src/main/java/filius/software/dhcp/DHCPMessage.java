package filius.software.dhcp;

import java.util.Objects;
import java.util.StringTokenizer;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Class representing a simplified dhcp message (e.g. without the not necessarily needed ciaddr, siaddr)
 */
public class DHCPMessage {

    private static final String FIELD_SEPARATOR = "\n";
    private static final String PARAM_SERVERIDENT = "serverident";
    private static final String PARAM_REQUESTED = "requested";
    private static final String PARAM_DNSSERVER = "dnsserver";
    private static final String PARAM_SUBNETMASK = "subnetmask";
    private static final String PARAM_ROUTER = "router";
    private static final String PARAM_CHADDR = "chaddr";
    private static final String PARAM_YIADDR = "yiaddr";
    private static final String BLANK_IP_ADDRESS = "0.0.0.0";

    DHCPMessageType type = DHCPMessageType.DISCOVER;
    String yiaddr = BLANK_IP_ADDRESS;
    String chaddr;
    String optionSubnetMask;
    String optionRouter;
    String optionDnsServer;
    String optionServerIdentifier;
    String optionRequestedAddress;

    DHCPMessage() {}

    /** DHCP message type */
    public DHCPMessageType getType() {
        return type;
    }

    /** Your IP address (i.e. in case of DHCP offer the available address for the client to use) */
    public String getYiaddr() {
        return yiaddr;
    }

    /** Client hardware address (i.e. the clients MAC address) */
    public String getChaddr() {
        return chaddr;
    }

    /** IP subnet mask to use */
    public String getSubnetMask() {
        return optionSubnetMask;
    }

    /** IP address of the router to use */
    public String getRouter() {
        return optionRouter;
    }

    /** IP address of the DNS server to use */
    public String getDnsServer() {
        return optionDnsServer;
    }

    /** IP address of the DHCP server */
    public String getServerIdentifier() {
        return optionServerIdentifier;
    }

    /** IP address the client requests from the server */
    public String getRequestedAddress() {
        return optionRequestedAddress;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder(type.toString());
        builder.append(FIELD_SEPARATOR).append(PARAM_YIADDR).append("=").append(yiaddr);
        if (StringUtils.isNoneEmpty(chaddr)) {
            builder.append(FIELD_SEPARATOR).append(PARAM_CHADDR).append("=").append(chaddr);
        }
        if (StringUtils.isNoneEmpty(optionRouter)) {
            builder.append(FIELD_SEPARATOR).append(PARAM_ROUTER).append("=").append(optionRouter);
        }
        if (StringUtils.isNoneEmpty(optionSubnetMask)) {
            builder.append(FIELD_SEPARATOR).append(PARAM_SUBNETMASK).append("=").append(optionSubnetMask);
        }
        if (StringUtils.isNoneEmpty(optionDnsServer)) {
            builder.append(FIELD_SEPARATOR).append(PARAM_DNSSERVER).append("=").append(optionDnsServer);
        }
        if (StringUtils.isNoneEmpty(optionRequestedAddress)) {
            builder.append(FIELD_SEPARATOR).append(PARAM_REQUESTED).append("=").append(optionRequestedAddress);
        }
        if (StringUtils.isNoneEmpty(optionServerIdentifier)) {
            builder.append(FIELD_SEPARATOR).append(PARAM_SERVERIDENT).append("=").append(optionServerIdentifier);
        }
        return builder.toString();
    }

    static DHCPMessage fromString(String dhcpMessageString) {
        DHCPMessage message = new DHCPMessage();
        StringTokenizer tokenizer = new StringTokenizer(dhcpMessageString, "\n=");
        if (tokenizer.hasMoreTokens()) {
            message.type = DHCPMessageType.fromString(tokenizer.nextToken());
        }
        while (tokenizer.hasMoreTokens()) {
            String keyToken = tokenizer.nextToken();
            if (tokenizer.hasMoreTokens()) {
                switch (keyToken) {
                case PARAM_CHADDR:
                    message.chaddr = tokenizer.nextToken();
                    break;
                case PARAM_DNSSERVER:
                    message.optionDnsServer = tokenizer.nextToken();
                    break;
                case PARAM_REQUESTED:
                    message.optionRequestedAddress = tokenizer.nextToken();
                    break;
                case PARAM_ROUTER:
                    message.optionRouter = tokenizer.nextToken();
                    break;
                case PARAM_SERVERIDENT:
                    message.optionServerIdentifier = tokenizer.nextToken();
                    break;
                case PARAM_SUBNETMASK:
                    message.optionSubnetMask = tokenizer.nextToken();
                    break;
                case PARAM_YIADDR:
                    message.yiaddr = tokenizer.nextToken();
                    break;
                }
            }
        }
        return message;
    }

    @Override
    public boolean equals(Object other) {
        boolean equals = true;
        if (!(other instanceof DHCPMessage)) {
            equals = false;
        } else if (ObjectUtils.notEqual(chaddr, ((DHCPMessage) other).chaddr)) {
            equals = false;
        } else if (ObjectUtils.notEqual(optionDnsServer, ((DHCPMessage) other).optionDnsServer)) {
            equals = false;
        } else if (ObjectUtils.notEqual(optionRequestedAddress, ((DHCPMessage) other).optionRequestedAddress)) {
            equals = false;
        } else if (ObjectUtils.notEqual(optionRouter, ((DHCPMessage) other).optionRouter)) {
            equals = false;
        } else if (ObjectUtils.notEqual(optionServerIdentifier, ((DHCPMessage) other).optionServerIdentifier)) {
            equals = false;
        } else if (ObjectUtils.notEqual(optionSubnetMask, ((DHCPMessage) other).optionSubnetMask)) {
            equals = false;
        } else if (ObjectUtils.notEqual(yiaddr, ((DHCPMessage) other).yiaddr)) {
            equals = false;
        }
        return equals;
    }

    @Override
    public int hashCode() {
        return Objects.hash(chaddr, optionDnsServer, optionRequestedAddress, optionRouter, optionServerIdentifier,
                optionSubnetMask, yiaddr);
    }

    /**
     * Der Client sendet ein DHCPDISCOVER-Paket. Im Folgenden sehen Sie einen Ausschnitt aus einer Aufzeichnung des
     * Netzwerkmonitors mit den IP- und DHCP-Abschnitten eines DHCPDISCOVER-Pakets. Im IP-Abschnitt lautet die
     * Zieladresse 255.255.255.255 und die Quelladresse 0.0.0.0. Der DHCP-Abschnitt bezeichnet das Paket als ein
     * "DHCP-Discover"-Paket und bezeichnet den Client an zwei Stellen mithilfe der physischen Adresse der
     * Netzwerkkarte. Die Werte im Feld CHADDR und im Feld DHCP: CLIENT IDENTIFIER sind identisch.
     * (https://support.microsoft.com/de-de/kb/169289)
     */
    static DHCPMessage createDiscoverMessage(String clientMacAddress) {
        DHCPMessage discoverMessage = new DHCPMessage();
        discoverMessage.type = DHCPMessageType.DISCOVER;
        discoverMessage.chaddr = clientMacAddress;
        return discoverMessage;
    }

    /**
     * Der DHCP-Server reagiert durch Senden eines DHCPOFFER-Pakets. Im IP-Abschnitt des Aufzeichnungsausschnitts weiter
     * unten ist die Quelladresse nun die IP-Adresse des DHCP-Servers, und die Zieladresse ist die Broadcastadresse
     * 255.255.255.255. Der DHCP-Abschnitt identifiziert das Paket als ein Angebot (Offer). Das Feld YIADDR enthält die
     * IP-Adresse, die der Server dem Client anbietet. Das Feld CHADDR enthält noch immer die physische Adresse des
     * Clients, der die Anforderung ausgegeben hat. Darüber hinaus werden im Abschnitt "DHCP Option Field" die
     * verschiedenen Optionen aufgeführt, die vom Server zusammen mit der IP-Adresse gesendet werden. In diesem Fall
     * werden vom Server die Subnetzmaske, der Standardgateway (Router), die Leasedauer, die WINS-Serveradresse
     * (NetBIOS-Namensdienst) und der NetBIOS-Knotentyp gesendet. (https://support.microsoft.com/de-de/kb/169289)
     */
    static DHCPMessage createOfferMessage(String clientMacAddress, String offeredIpAddress, String subnetMask,
            String router, String dnsServer, String serverIdentifier) {
        DHCPMessage offerMessage = new DHCPMessage();
        offerMessage.type = DHCPMessageType.OFFER;
        offerMessage.chaddr = clientMacAddress;
        offerMessage.yiaddr = offeredIpAddress;
        offerMessage.optionSubnetMask = subnetMask;
        offerMessage.optionRouter = router;
        offerMessage.optionDnsServer = dnsServer;
        offerMessage.optionServerIdentifier = serverIdentifier;
        return offerMessage;
    }

    /**
     * Der Client reagiert auf DHCPOFFER durch Senden von DHCPREQUEST. Im IP-Abschnitt der nachfolgenden Aufzeichnung
     * ist die Quelladresse des Clients noch immer 0.0.0.0, und das Ziel des Pakets ist noch immer 255.255.255.255. Der
     * Client behält 0.0.0.0 bei, da er keine Bestätigung des Servers erhalten hat, dass die angebotene Adresse
     * verwendet werden kann. Das Ziel lautet noch immer "Broadcast", da möglicherweise mehr als ein DHCP-Server
     * reagiert hat und über eine Reservierung für ein Angebot (Offer) an den Client verfügt. Dadurch werden andere
     * DHCP-Server in Kenntnis gesetzt, dass sie die angebotenen Adressen freigeben und an die verfügbaren Pools
     * zurückgeben können. Der DHCP-Abschnitt identifiziert das Paket als eine Anforderung (Request) und überprüft die
     * angebotene Adresse mithilfe des Feldes DHCP: REQUESTED ADDRESS. Das Feld DHCP: SERVER IDENTIFIER enthält die
     * IP-Adresse des DHCP-Servers, der die Lease anbietet. (https://support.microsoft.com/de-de/kb/169289)
     */
    static DHCPMessage createRequestMessage(String clientMacAddress, String requestedIpAddress, String serverIdentifier) {
        DHCPMessage requestMessage = new DHCPMessage();
        requestMessage.type = DHCPMessageType.REQUEST;
        requestMessage.chaddr = clientMacAddress;
        requestMessage.optionRequestedAddress = requestedIpAddress;
        requestMessage.optionServerIdentifier = serverIdentifier;
        return requestMessage;
    }

    static DHCPMessage createDeclineMessage(String clientMacAddress, String requestedIpAddress, String serverIdentifier) {
        DHCPMessage declineMessage = new DHCPMessage();
        declineMessage.type = DHCPMessageType.DECLINE;
        declineMessage.chaddr = clientMacAddress;
        declineMessage.optionRequestedAddress = requestedIpAddress;
        declineMessage.optionServerIdentifier = serverIdentifier;
        return declineMessage;
    }

    /**
     * Der DHCP-Server reagiert auf DHCPREQUEST mit DHCPACK und beendet so den Initialisierungszyklus. Die Quelladresse
     * ist die IP-Adresse des DHCP-Servers, und die Zieladresse ist noch immer 255.255.255.255. Das Feld YIADDR enthält
     * die Adresse des Clients, und die Felder CHADDR und DHCP: CLIENT IDENTIFIER zeigen die physische Adresse der
     * Netzwerkkarte des Clients, der die Anforderung ausgegeben hat. Der Abschnitt "DHCP Option" bezeichnet das Paket
     * als "ACK". (https://support.microsoft.com/de-de/kb/169289)
     */
    static DHCPMessage createAckMessage(String clientMacAddress, String acknowledgedIpAddress, String serverIdentifier) {
        DHCPMessage ackMessage = new DHCPMessage();
        ackMessage.type = DHCPMessageType.ACK;
        ackMessage.chaddr = clientMacAddress;
        ackMessage.yiaddr = acknowledgedIpAddress;
        ackMessage.optionServerIdentifier = serverIdentifier;
        return ackMessage;
    }

    static DHCPMessage createNackMessage(String clientMacAddress, String notAcknowledgedIpAddress,
            String serverIdentifier) {
        DHCPMessage nackMessage = new DHCPMessage();
        nackMessage.type = DHCPMessageType.NACK;
        nackMessage.chaddr = clientMacAddress;
        nackMessage.yiaddr = notAcknowledgedIpAddress;
        nackMessage.optionServerIdentifier = serverIdentifier;
        return nackMessage;
    }
}
