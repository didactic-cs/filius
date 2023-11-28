package filius.software.nat;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import filius.software.vermittlungsschicht.IpPaket;

public class PortProtocolPairTest { //neu

    @Test
    public void testEquals_True() throws Exception {
        PortProtocolPair pair = new PortProtocolPair(5555, "192.168.0.1", IpPaket.TCP);

        assertTrue(pair.equals(new PortProtocolPair(pair.getPort(), "192.168.0.1", pair.getProtocol())));
    }

    @Test
    public void testEquals_Null_False() throws Exception {
        PortProtocolPair pair = new PortProtocolPair(5555, "192.168.0.1", IpPaket.TCP);

        assertFalse(pair.equals(null));
    }

    @Test
    public void testEquals_NOPortProtocolPair_False() throws Exception {
        PortProtocolPair pair = new PortProtocolPair(5555, "192.168.0.1", IpPaket.TCP);

        assertFalse(pair.equals("hallo"));
    }

    @Test
    public void testEquals_DifferentPort_False() throws Exception {
        PortProtocolPair pair = new PortProtocolPair(5555, "192.168.0.1", IpPaket.TCP);

        assertFalse(pair.equals(new PortProtocolPair(pair.getPort() + 100, "192.168.0.1", pair.getProtocol())));
    }

    @Test
    public void testEquals_DifferentProtocol_False() throws Exception {
        PortProtocolPair pair = new PortProtocolPair(5555, "192.168.0.1", IpPaket.TCP);

        assertFalse(pair.equals(new PortProtocolPair(pair.getPort(), "192.168.0.1", pair.getProtocol() + 100)));
    }
}
