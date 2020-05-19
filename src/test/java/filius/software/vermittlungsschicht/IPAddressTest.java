package filius.software.vermittlungsschicht;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import filius.exception.InvalidParameterException;

public class IPAddressTest {

    @Test
    public void testConstructor_IPv4() throws Exception {
        String ipAddressAsString = "192.168.1.1/24";
        IPAddress address = new IPAddress(ipAddressAsString);

        assertThat(address.toString(), is(ipAddressAsString));
    }

    @Test
    public void testConstructor_IPv4_WithNetmask() throws Exception {
        String ipAddressAsString = "192.168.1.1/24";
        IPAddress address = new IPAddress(ipAddressAsString);

        assertThat(address.toString(), is(ipAddressAsString));
    }

    @Test(expected = InvalidParameterException.class)
    public void testConstructor_IPv4_InvalidSyntax() throws Exception {
        new IPAddress("1.1.1");
    }

    @Test
    public void testConstructor_IPv6() throws Exception {
        String ipAddressAsString = "1:2:3:4:5:6:7:8/16";
        IPAddress address = new IPAddress(ipAddressAsString);

        assertThat(address.toString(), is(ipAddressAsString));
    }

    @Test
    public void testConstructor_IPv6_WithPENDINGZeroSequencePlaceholder() throws Exception {
        String ipAddressAsString = "1::/64";
        IPAddress address = new IPAddress(ipAddressAsString);

        assertThat(address.toString(), is("1:0:0:0:0:0:0:0/64"));
    }

    @Test
    public void testConstructor_IPv6_WithLEADINGZeroSequencePlaceholder() throws Exception {
        String ipAddressAsString = "::1/64";
        IPAddress address = new IPAddress(ipAddressAsString);

        assertThat(address.toString(), is("0:0:0:0:0:0:0:1/64"));
    }

    @Test
    public void testConstructor_IPv6_WithMIDDLEZeroSequencePlaceholder() throws Exception {
        String ipAddressAsString = "1::8/64";
        IPAddress address = new IPAddress(ipAddressAsString);

        assertThat(address.toString(), is("1:0:0:0:0:0:0:8/64"));
    }

    @Test
    public void testConstructor_IPv6_WithIPv4Notation() throws Exception {
        String ipAddressAsString = "1:2:3:4:5:6:192.168.1.1/64";
        IPAddress address = new IPAddress(ipAddressAsString);

        assertThat(address.toString(), is(ipAddressAsString));
    }

    @Test
    public void testConstructor_IPv6_WithHexadecimalDigits() throws Exception {
        String ipAddressAsString = "AAaa:BBbb:CCcc:DDdd:a111:b222:c333:d444/64";
        IPAddress address = new IPAddress(ipAddressAsString);

        assertThat(address.toString(), is(ipAddressAsString.toLowerCase()));
    }

    @Test
    public void testConstructor_IPv6_WithNetmask() throws Exception {
        String ipAddressAsString = "1:2:3:4:5:6:7:8/24";
        IPAddress address = new IPAddress(ipAddressAsString);

        assertThat(address.toString(), is(ipAddressAsString));
    }

    @Test(expected = InvalidParameterException.class)
    public void testConstructor_IPv6_NetmaskToHigh() throws Exception {
        new IPAddress("1:2:3:4:5:6:7:8/129");
    }

    @Test(expected = InvalidParameterException.class)
    public void testConstructor_IPv4_NetmaskToHigh() throws Exception {
        new IPAddress("1.2.3.4/33");
    }

    @Test(expected = InvalidParameterException.class)
    public void testConstructor_IPv6_InvalidSyntax() throws Exception {
        new IPAddress("1:2:3:4:5:6:7");
    }

    @Test
    public void testConstructor2_ValidIPv4() throws Exception {
        IPAddress address = new IPAddress("192.168.1.1", "255.255.0.0");
        assertThat(address.normalizedString(), is("192.168.1.1/16"));
    }

    @Test
    public void testConstructor2_IPv4_MaxNetmask() throws Exception {
        IPAddress address = new IPAddress("10.0.1.1", "255.255.255.255");
        assertThat(address.normalizedString(), is("10.0.1.1/32"));
    }

    @Test(expected = InvalidParameterException.class)
    public void testConstructor2_IPv4_InvalidNetmask() throws Exception {
        new IPAddress("10.0.1.1", "255.253.0.0");
    }

    @Test(expected = InvalidParameterException.class)
    public void testConstructor2_IPv4_InvalidNetmaskAddress() throws Exception {
        new IPAddress("10.0.1.1", "255..0.0");
    }

    @Test
    public void testConstructor2_ValidIPv6() throws Exception {
        IPAddress address = new IPAddress("10::192.168.1.1", "64");
        assertThat(address.normalizedString(), is("10::192.168.1.1/64"));
    }

    @Test
    public void testEquals_DifferentNotations() throws Exception {
        IPAddress address1 = new IPAddress("1:2:3:4:5:6:1.1.1.1/8");
        IPAddress address2 = new IPAddress("1:2:3:4:5:6:101:101/8");

        assertThat(address1, is(address2));
    }

    @Test
    public void testEquals_DifferentNetworkMask() throws Exception {
        IPAddress address1 = new IPAddress("1:2:3:4:5:6:1.1.1.1/16");
        IPAddress address2 = new IPAddress("1:2:3:4:5:6:1.1.1.1/10");

        assertThat(address1, not(is(address2)));
    }

    @Test
    public void testIsLoopbackAddress_IPv4_Yes() throws Exception {
        assertTrue(new IPAddress("127.0.0.1/16").isLoopbackAddress());
        assertTrue(new IPAddress("127.0.0.1/8").isLoopbackAddress());
        assertTrue(new IPAddress("127.0.0.1/0").isLoopbackAddress());
        assertTrue(new IPAddress("127.255.0.42/8").isLoopbackAddress());
    }

    @Test
    public void testIsLoopbackAddress_IPv4_No() throws Exception {
        assertFalse(new IPAddress("128.255.0.42/8").isLoopbackAddress());
        assertFalse(new IPAddress("128.0.0.1/16").isLoopbackAddress());
    }

    @Test
    public void testIsLoopbackAddress_IPv6_Yes() throws Exception {
        assertTrue(new IPAddress("0:0:0:0:0:0:0:1/16").isLoopbackAddress());
        assertTrue(new IPAddress("::1/16").isLoopbackAddress());
        assertTrue(new IPAddress("::1/8").isLoopbackAddress());
    }

    @Test
    public void testIsLoopbackAddress_IPv6_No() throws Exception {
        assertFalse(new IPAddress("0:0:0:0:0:0:0:2/16").isLoopbackAddress());
        assertFalse(new IPAddress("1:0:0:0:0:0:0:1/16").isLoopbackAddress());
    }

    @Test
    public void testDefineVersion_WithoutNetmask_ValidIPv4Address() throws Exception {
        IPVersion version = IPAddress.defineVersion("192.168.1.1");
        assertThat(version, is(IPVersion.IPv4));
    }

    @Test
    public void testDefineVersion_WithNetmask_ValidIPv6Address() throws Exception {
        IPVersion version = IPAddress.defineVersion("192.168.1.1/16");
        assertThat(version, is(IPVersion.IPv4));
    }

    @Test
    public void testDefineVersion_WithoutNetmask_ValidIPv6Address() throws Exception {
        IPVersion version = IPAddress.defineVersion("::1");
        assertThat(version, is(IPVersion.IPv6));
    }

    @Test
    public void testDefineVersion_InvalidAddress() throws Exception {
        IPVersion version = IPAddress.defineVersion("something invalid");
        assertThat(version, nullValue());
    }

    @Test
    public void testIsUnspecifiedAddress_IPv4() throws Exception {
        assertTrue(new IPAddress("0.0.0.0/8").isUnspecifiedAddress());
    }

    @Test
    public void testIsUnspecifiedAddress_IPv4_False() throws Exception {
        assertFalse(new IPAddress("0.0.0.1/8").isUnspecifiedAddress());
        assertFalse(new IPAddress("0.0.1.0/8").isUnspecifiedAddress());
        assertFalse(new IPAddress("0.1.0.0/8").isUnspecifiedAddress());
        assertFalse(new IPAddress("1.0.0.0/8").isUnspecifiedAddress());
        assertFalse(new IPAddress("0.0.0.0/7").isUnspecifiedAddress());
    }

    @Test
    public void testIsUnspecifiedAddress_IPv6() throws Exception {
        assertTrue(new IPAddress("::/128").isUnspecifiedAddress());
    }

    @Test
    public void testIsUnspecifiedAddress_IPv6_False() throws Exception {
        assertFalse(new IPAddress("1::/128").isUnspecifiedAddress());
        assertFalse(new IPAddress("::1/128").isUnspecifiedAddress());
        assertFalse(new IPAddress("::/127").isUnspecifiedAddress());
    }

    @Test
    public void testNetworkAddress_IPv4() throws Exception {
        IPAddress ipAddress = new IPAddress("1.2.3.4/16");

        assertThat(ipAddress.networkAddress(), is("1.2.0.0"));
    }

    @Test
    public void testNetworkAddress_IPv4_13Bit() throws Exception {
        IPAddress ipAddress = new IPAddress("255.255.255.255/13");

        assertThat(ipAddress.networkAddress(), is("255." + 0b1111_1000 + ".0.0"));
    }

    @Test
    public void testNetworkAddress_IPv6() throws Exception {
        IPAddress ipAddress = new IPAddress("1:2:3:4:5:6:7:8/16");

        assertThat(ipAddress.networkAddress(), is("1:0:0:0:0:0:0:0"));
    }

    @Test
    public void testNetworkAddress_IPv6_23Bit() throws Exception {
        IPAddress ipAddress = new IPAddress("abcd:ffff:ffff:ffff:ffff:ffff:ffff:ffff/23");

        assertThat(ipAddress.networkAddress(),
                is("abcd:" + Integer.toHexString(0b1111_1110_0000_0000) + ":0:0:0:0:0:0"));
    }

    @Test
    public void testNetmask_IPv6_23Bit() throws Exception {
        IPAddress ipAddress = new IPAddress("1:2:3:4:5:6:7:8/23");

        assertThat(ipAddress.netmask(), is("23"));
    }

    @Test
    public void testNetmask_IPv4_13Bit() throws Exception {
        IPAddress ipAddress = new IPAddress("1.2.3.4/13");

        assertThat(ipAddress.netmask(), is("255." + 0b1111_1000 + ".0.0"));
    }

    @Test
    public void testNormalizedString_IPv6_WithZeroSequence() throws Exception {
        IPAddress ipAddress = new IPAddress("1:0:0:0:5:6:7:8/23");

        assertThat(ipAddress.normalizedString(), is("1::5:6:7:8/23"));
    }

    @Test
    public void testNormalizedString_IPv6_WithZeroSequenceAndIPv4Notation() throws Exception {
        IPAddress ipAddress = new IPAddress("1:0:0:0:0:0:1.2.3.4/23");

        assertThat(ipAddress.normalizedString(), is("1::1.2.3.4/23"));
    }

    @Test
    public void testNormalizedString_IPv6_WithZeroSequenceAtBeginning() throws Exception {
        IPAddress ipAddress = new IPAddress("0:0:0:0:5:6:7:8/23");

        assertThat(ipAddress.normalizedString(), is("::5:6:7:8/23"));
    }

    @Test
    public void testNormalizedString_IPv6_WithZeroSequenceAtEnd() throws Exception {
        IPAddress ipAddress = new IPAddress("5:6:7:8:0:0:0:0/23");

        assertThat(ipAddress.normalizedString(), is("5:6:7:8::/23"));
    }

    @Test
    public void testNormalizedString_IPv6_WithTWOZeroSequences_ReplaceSecondAsLongest() throws Exception {
        IPAddress ipAddress = new IPAddress("5:0:0:6:0:0:0:8/23");

        assertThat(ipAddress.normalizedString(), is("5:0:0:6::8/23"));
    }

    @Test
    public void testNormalizedString_IPv6_WithTWOZeroSequences_ReplaceFirstAsLongest() throws Exception {
        IPAddress ipAddress = new IPAddress("5:0:0:0:7:0:0:8/23");

        assertThat(ipAddress.normalizedString(), is("5::7:0:0:8/23"));
    }

    @Test
    public void testVerify_IPv4WithNetmask_Failure() throws Exception {
        assertFalse(IPAddress.verifyAddress("192.168.2.2/24", IPVersion.IPv4));
    }

    @Test
    public void testVerify_IPv4WithoutNetmask_Success() throws Exception {
        assertTrue(IPAddress.verifyAddress("192.168.2.2", IPVersion.IPv4));
    }

    @Test
    public void testVerify_IPv4AsIPv6_Failure() throws Exception {
        assertFalse(IPAddress.verifyAddress("192.168.2.2", IPVersion.IPv6));
    }

    @Test
    public void testVerify_IPv6WithNetmask_Failure() throws Exception {
        assertFalse(IPAddress.verifyAddress("1:2:3::8/24", IPVersion.IPv6));
    }

    @Test
    public void testVerify_IPv6Incomplete_Failure() throws Exception {
        assertFalse(IPAddress.verifyAddress("1:", IPVersion.IPv6));
    }

    @Test
    public void testVerify_IPv6WithoutNetmask_Success() throws Exception {
        assertTrue(IPAddress.verifyAddress("1:2:3::8", IPVersion.IPv6));
    }

    @Test
    public void testVerify_IPv6AsIPv4_Failure() throws Exception {
        assertFalse(IPAddress.verifyAddress("1:2:3::8", IPVersion.IPv4));
    }

    @Test
    public void testVerifyNetmaskDefinition_IPv6_True() throws Exception {
        assertTrue(IPAddress.verifyNetmaskDefinition("5", IPVersion.IPv6));
        assertTrue(IPAddress.verifyNetmaskDefinition("1", IPVersion.IPv6));
        assertTrue(IPAddress.verifyNetmaskDefinition("128", IPVersion.IPv6));
    }

    @Test
    public void testVerifyNetmaskDefinition_IPv6_False() throws Exception {
        assertFalse(IPAddress.verifyNetmaskDefinition("0", IPVersion.IPv6));
        assertFalse(IPAddress.verifyNetmaskDefinition("-1", IPVersion.IPv6));
        assertFalse(IPAddress.verifyNetmaskDefinition("129", IPVersion.IPv6));
        assertFalse(IPAddress.verifyNetmaskDefinition("255.255.255.0", IPVersion.IPv6));
    }

    @Test
    public void testVerifyNetmaskDefinition_IPv6_Hexadecimal() throws Exception {
        assertFalse(IPAddress.verifyNetmaskDefinition("f", IPVersion.IPv6));
    }

    @Test
    public void testVerifyNetmaskDefinition_IPv4_True() throws Exception {
        assertTrue(IPAddress.verifyNetmaskDefinition("255.255.0.0", IPVersion.IPv4));
    }

    @Test
    public void testVerifyNetmaskDefinition_IPv4WithPartlyByte_True() throws Exception {
        assertTrue(IPAddress.verifyNetmaskDefinition("255.128.0.0", IPVersion.IPv4));
    }

    @Test
    public void testVerifyNetmaskDefinition_IPv4_InvalidNetmaskString() throws Exception {
        assertFalse(IPAddress.verifyNetmaskDefinition("255.253.0.0", IPVersion.IPv4));
        assertFalse(IPAddress.verifyNetmaskDefinition("16", IPVersion.IPv4));
    }

    @Test
    public void testEqualNetwork_Success() throws InvalidParameterException {
        IPAddress ipAddress = new IPAddress("192.168.0.10", "255.255.255.0");

        assertTrue(ipAddress.equalNetwork("192.168.0.11"));
    }

    @Test
    public void testEqualNetwork_Failure() throws InvalidParameterException {
        IPAddress ipAddress = new IPAddress("192.168.0.10", "255.255.255.0");

        assertFalse(ipAddress.equalNetwork("192.168.1.10"));
    }

    @Test
    public void testIsGlobalBroadcast_IPv4() throws InvalidParameterException {
        IPAddress ipAddress = new IPAddress("255.255.255.255", "255.255.255.0");

        assertTrue(ipAddress.isGlobalBroadcast());
    }

    @Test
    public void testIsGlobalBroadcast_IPv6() throws InvalidParameterException {
        IPAddress ipAddress = new IPAddress("FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF", "17");

        assertTrue(ipAddress.isGlobalBroadcast());
    }

    @Test
    public void testIsLocalBroadcast_IPv4With8BitNetmask() throws InvalidParameterException {
        IPAddress ipAddress = new IPAddress("192.255.255.255", "255.0.0.0");

        assertTrue(ipAddress.isLocalBroadcast());
    }

    @Test
    public void testIsLocalBroadcast_IPv4With12BitNetmask() throws InvalidParameterException {
        IPAddress ipAddress = new IPAddress("192.15.255.255", "255.240.0.0");

        assertTrue(ipAddress.isLocalBroadcast());
    }

    @Test
    public void testIsLocalBroadcast_IPv4With12BitNetmask_Failure() throws InvalidParameterException {
        IPAddress ipAddress = new IPAddress("192.0.255.255", "255.240.0.0");

        assertFalse(ipAddress.isLocalBroadcast());
    }
}
