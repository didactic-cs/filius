package filius.software.vermittlungsschicht;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import filius.exception.InvalidParameterException;

public class IPVersionTest {

    @Test
    public void testFromString_IPv4() throws Exception {
        IPVersion version = IPVersion.fromString("IPv4");

        assertThat(version, is(IPVersion.IPv4));
    }

    @Test
    public void testFromString_ipV4() throws Exception {
        IPVersion version = IPVersion.fromString("ipV4");

        assertThat(version, is(IPVersion.IPv4));
    }

    @Test
    public void testFromString_IPv6() throws Exception {
        IPVersion version = IPVersion.fromString("IPv6");

        assertThat(version, is(IPVersion.IPv6));
    }

    @Test
    public void testFromString_ipV6() throws Exception {
        IPVersion version = IPVersion.fromString("ipV6");

        assertThat(version, is(IPVersion.IPv6));
    }

    @Test(expected = InvalidParameterException.class)
    public void testFromString_InvalidVersionString_ThrowsException() throws Exception {
        IPVersion.fromString("invalid_version");
    }

}
