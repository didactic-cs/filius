package filius.software.dhcp;

import org.apache.commons.lang3.StringUtils;

public enum DHCPMessageType {
    DISCOVER("DHCPDISCOVER"), REQUEST("DHCPREQUEST"), ACK("DHCPACK"), NACK("DHCPNAK"), OFFER("DHCPOFFER"), DECLINE(
            "DHCPDECLINE");

    String command;

    private DHCPMessageType(String command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return command;
    }

    public static DHCPMessageType fromString(String command) {
        DHCPMessageType result = null;
        for (DHCPMessageType type : DHCPMessageType.values()) {
            if (StringUtils.equalsIgnoreCase(type.toString(), command)) {
                result = type;
                break;
            }
        }
        return result;
    }
}