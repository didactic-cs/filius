package filius.software.vermittlungsschicht;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ARPTest {

    @Test
    public void testIsValidArpEntry_Yes() throws Exception {
        assertThat(ARP.isValidArpEntry("10.0.0.1", "255.255.255.0"), is(true));
    }

    @Test
    public void testIsValidArpEntry_UnknownAddress_No() throws Exception {
        assertThat(ARP.isValidArpEntry("0.0.0.0", "255.255.255.0"), is(false));
    }

    @Test
    public void testIsValidArpEntry_NetworkAddress_No() throws Exception {
        assertThat(ARP.isValidArpEntry("10.0.0.0", "255.255.255.0"), is(false));
    }

    @Test
    public void testIsValidArpEntry_DirectBroadcast_No() throws Exception {
        assertThat(ARP.isValidArpEntry("10.0.0.255", "255.255.255.0"), is(false));
    }

}
