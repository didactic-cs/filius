package filius.rahmenprogramm;

import filius.software.vermittlungsschicht.IP;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class IpTest {
	@Test
	public void CidrTest() throws Exception {
		assertArrayEquals(new String[] { "192.168.0.1", "255.255.255.0" }, IP.parseCidr("192.168.0.1/24"));
		assertArrayEquals(new String[] { "192.168.0.1", "255.255.255.0" }, IP.parseCidr("192.168.0.1 / 24"));
		assertArrayEquals(new String[] { "192.168.0.1", "255.255.255.128" }, IP.parseCidr("192.168.0.1 / 25"));
		assertArrayEquals(new String[] { "192.168.0.1", "255.255.255.192" }, IP.parseCidr("192.168.0.1 / 26"));
		assertArrayEquals(new String[] { "192.168.0.1", "255.255.255.224" }, IP.parseCidr("192.168.0.1 / 27"));
		assertArrayEquals(new String[] { "192.168.0.1", "255.255.255.240" }, IP.parseCidr("192.168.0.1 / 28"));
		assertArrayEquals(new String[] { "192.168.0.1", "255.255.255.248" }, IP.parseCidr("192.168.0.1 / 29"));
		assertArrayEquals(new String[] { "192.168.0.1", "255.255.255.252" }, IP.parseCidr("192.168.0.1 / 30"));
		assertArrayEquals(new String[] { "192.168.0.1 / 31" }, IP.parseCidr("192.168.0.1 / 31"));
		assertArrayEquals(new String[] { "192.168.0.1 / 1" }, IP.parseCidr("192.168.0.1 / 1"));
		assertArrayEquals(new String[] { "192.168.0.1" }, IP.parseCidr("192.168.0.1"));
		assertArrayEquals(new String[] { "Hello World" }, IP.parseCidr("Hello World"));
	}
}
