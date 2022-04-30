package filius.software.nat;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import filius.software.vermittlungsschicht.IpPaket;

public class InetAddressTest {

    @Test
    public void testEquals_True() throws Exception {
        InetAddress address = new InetAddress("1.1.1.1", 5555, IpPaket.TCP);

        assertTrue(address.equals(new InetAddress(address.getIpAddress(), address.getPort(), address.getProtocol())));
    }

    @Test
    public void testEquals_Null_False() throws Exception {
        InetAddress address = new InetAddress("1.1.1.1", 5555, IpPaket.TCP);

        assertFalse(address.equals(null));
    }

    @Test
    public void testEquals_NOInetAddress_False() throws Exception {
        InetAddress address = new InetAddress("1.1.1.1", 5555, IpPaket.TCP);

        assertFalse(address.equals("hallo"));
    }

    @Test
    public void testEquals_DifferentIPAddress_False() throws Exception {
        InetAddress address = new InetAddress("1.1.1.1", 5555, IpPaket.TCP);

        assertFalse(address.equals(new InetAddress("2.2.2.2", address.getPort(), address.getProtocol())));
    }

    @Test
    public void testEquals_DifferentPort_False() throws Exception {
        InetAddress address = new InetAddress("1.1.1.1", 5555, IpPaket.TCP);

        assertFalse(address
                .equals(new InetAddress(address.getIpAddress(), address.getPort() + 100, address.getProtocol())));
    }

    @Test
    public void testEquals_DifferentProtocol_False() throws Exception {
        InetAddress address = new InetAddress("1.1.1.1", 5555, IpPaket.TCP);

        assertFalse(address
                .equals(new InetAddress(address.getIpAddress(), address.getPort(), address.getProtocol() + 100)));
    }
}
