package filius.software.firewall;

import static filius.software.firewall.FirewallRule.ACCEPT;
import static filius.software.firewall.FirewallRule.DROP;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.BeforeClass;
import org.junit.Test;

import filius.hardware.knoten.Rechner;
import filius.rahmenprogramm.Information;
import filius.software.system.Betriebssystem;
import filius.software.transportschicht.TcpSegment;
import filius.software.transportschicht.UdpSegment;
import filius.software.vermittlungsschicht.IcmpPaket;
import filius.software.vermittlungsschicht.IpPaket;

public class FirewallTest {

    private static final String DEST_IP_ADDRESS = "192.168.1.2";
    private static final String SENDER_IP_ADDRESS = "192.168.1.1";

    @BeforeClass
    public static void initI18N() {
        Information.getInformation().setLocale(Locale.GERMANY);
    }

    @Test
    public void testAcceptIPPacket_Syn_Accept() throws Exception {
        TcpSegment segment = new TcpSegment();
        segment.setSyn(true);
        IpPaket ipPacket = new IpPaket(IpPaket.TCP);
        ipPacket.setSegment(segment);

        Firewall firewall = createActiveFirewallDefaultAccept(ACCEPT);
        firewall.setFilterSYNSegmentsOnly(false);

        assertTrue(firewall.acceptIPPacket(ipPacket));
    }

    private Firewall createActiveFirewallDefaultAccept(short defaultPolicy) {
        Firewall firewall = new Firewall();
        Betriebssystem os = new Betriebssystem();
        os.setKnoten(new Rechner());
        firewall.setSystemSoftware(os);
        firewall.setDefaultPolicy(defaultPolicy);
        firewall.setActivated(true);
        return firewall;
    }

    @Test
    public void testAcceptIPPacket_ICMP_Drop() throws Exception {
        IpPaket ipPacket = new IcmpPaket();

        Firewall firewall = createActiveFirewallDefaultAccept(ACCEPT);
        firewall.setDropICMP(true);

        assertFalse(firewall.acceptIPPacket(ipPacket));
    }

    @Test
    public void testCheckAcceptIcmp_IsIcmpAndDropIcmp_DoNOTAccept() throws Exception {
        IcmpPaket icmp = new IcmpPaket();

        Firewall firewall = createActiveFirewallDefaultAccept(ACCEPT);
        firewall.setDropICMP(true);

        assertFalse(firewall.checkAcceptIcmp(icmp));
    }

    @Test
    public void testCheckAcceptIcmp_IsIcmpAndNOTDropIcmp_DoAccept() throws Exception {
        IcmpPaket icmp = new IcmpPaket();

        Firewall firewall = createActiveFirewallDefaultAccept(ACCEPT);
        firewall.setDropICMP(false);

        assertTrue(firewall.checkAcceptIcmp(icmp));
    }

    @Test
    public void testCheckAcceptIcmp_IsNOTIcmpAndDropIcmp_DoAccept() throws Exception {
        IpPaket paket = new IpPaket(IpPaket.TCP);

        Firewall firewall = createActiveFirewallDefaultAccept(ACCEPT);
        firewall.setDropICMP(true);

        assertTrue(firewall.checkAcceptIcmp(paket));
    }

    @Test
    public void testCheckAcceptTCP_IsIcmp_Accept() throws Exception {
        IpPaket paket = new IcmpPaket();

        Firewall firewall = createActiveFirewallDefaultAccept(DROP);

        assertTrue(firewall.checkAcceptTCP(paket));

    }

    @Test
    public void testCheckAcceptTCP_IsTCPRuleApplies_Drop() throws Exception {
        IpPaket paket = createIPPacketTcp(SENDER_IP_ADDRESS, DEST_IP_ADDRESS, 80);

        Firewall firewall = createActiveFirewallDefaultAccept(ACCEPT);
        firewall.setFilterSYNSegmentsOnly(false);
        FirewallRule rule = createDropRule(SENDER_IP_ADDRESS, DEST_IP_ADDRESS, 80, FirewallRule.TCP);

        firewall.addRule(rule);

        assertFalse(firewall.checkAcceptTCP(paket));
    }

    @Test
    public void testCheckAcceptTCP_IsTCPRuleAppliesNOSync_Accept() throws Exception {
        IpPaket paket = createIPPacketTcp(SENDER_IP_ADDRESS, DEST_IP_ADDRESS, 80);

        Firewall firewall = createActiveFirewallDefaultAccept(ACCEPT);
        firewall.setFilterSYNSegmentsOnly(true);
        FirewallRule rule = createDropRule(SENDER_IP_ADDRESS, DEST_IP_ADDRESS, 80, FirewallRule.TCP);
        firewall.addRule(rule);

        assertTrue(firewall.checkAcceptTCP(paket));
    }

    @Test
    public void testCheckAcceptTCP_IsTCPAndRuleNOTApplies_NOTDrop() throws Exception {
        IpPaket paket = createIPPacketTcp(SENDER_IP_ADDRESS, DEST_IP_ADDRESS, 80);

        Firewall firewall = createActiveFirewallDefaultAccept(ACCEPT);
        FirewallRule rule = createDropRule(SENDER_IP_ADDRESS, DEST_IP_ADDRESS, 99, FirewallRule.TCP);
        firewall.addRule(rule);

        assertTrue(firewall.checkAcceptTCP(paket));
    }

    private FirewallRule createDropRule(String sender, String dest, int port, short protocol) {
        FirewallRule rule = new FirewallRule(sender, "255.255.255.0", dest, "255.255.255.0", port, protocol, DROP);
        return rule;
    }

    private IpPaket createIPPacketTcp(String sender, String dest, int port) {
        IpPaket paket = new IpPaket(IpPaket.TCP);
        paket.setSender(sender);
        paket.setEmpfaenger(dest);
        TcpSegment segment = new TcpSegment();
        segment.setZielPort(port);
        paket.setSegment(segment);
        return paket;
    }

    @Test
    public void testCheckAcceptUDP_IsUDPAndRuleApplies_Drop() throws Exception {
        IpPaket paket = createIPPacketUDP(SENDER_IP_ADDRESS, DEST_IP_ADDRESS, 80);

        Firewall firewall = createActiveFirewallDefaultAccept(ACCEPT);
        firewall.setFilterUdp(true);
        FirewallRule rule = createDropRule(SENDER_IP_ADDRESS, DEST_IP_ADDRESS, 80, FirewallRule.UDP);
        firewall.addRule(rule);

        assertFalse(firewall.checkAcceptUDP(paket));
    }

    @Test
    public void testCheckAcceptUDP_IsUDPAndRuleApplies_UDPFilterInactive_Accept() throws Exception {
        IpPaket paket = createIPPacketUDP(SENDER_IP_ADDRESS, DEST_IP_ADDRESS, 80);

        Firewall firewall = createActiveFirewallDefaultAccept(ACCEPT);
        firewall.setFilterUdp(false);
        FirewallRule rule = createDropRule(SENDER_IP_ADDRESS, DEST_IP_ADDRESS, 80, FirewallRule.UDP);
        firewall.addRule(rule);

        assertTrue(firewall.checkAcceptUDP(paket));
    }

    private IpPaket createIPPacketUDP(String sender, String dest, int port) {
        IpPaket paket = new IpPaket(IpPaket.UDP);
        paket.setSender(sender);
        paket.setEmpfaenger(dest);
        UdpSegment segment = new UdpSegment();
        segment.setZielPort(port);
        paket.setSegment(segment);
        return paket;
    }
}
