package filius.software.firewall;

import org.junit.Test;

import filius.software.transportschicht.TcpSegment;
import filius.software.vermittlungsschicht.IpPaket;

public class FirewallTest {

    @Test
    public void testAcceptIPPacket() throws Exception {
        TcpSegment segment = new TcpSegment();
        segment.setSyn(true);
        IpPaket ipPacket = new IpPaket();
        ipPacket.setProtocol(IpPaket.TCP);
        ipPacket.setSegment(segment);

        Firewall firewall = new Firewall();
        firewall.setActivated(true);
        firewall.setDropSYNSegmentsOnly(true);
    }

}
