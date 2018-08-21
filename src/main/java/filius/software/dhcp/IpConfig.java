package filius.software.dhcp;

public class IpConfig {
    private String ipAddress;
    private String router;
    private String subnetMask;
    private String dnsServer;
    private String dhcpServer;

    IpConfig(String ipAddress, String router, String subnetMask, String dnsServer, String dhcpServer) {
        this.ipAddress = ipAddress;
        this.router = router;
        this.subnetMask = subnetMask;
        this.dnsServer = dnsServer;
        this.dhcpServer = dhcpServer;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getRouter() {
        return router;
    }

    public String getSubnetMask() {
        return subnetMask;
    }

    public String getDnsServer() {
        return dnsServer;
    }

    public String getDhcpServer() {
        return dhcpServer;
    }
}
