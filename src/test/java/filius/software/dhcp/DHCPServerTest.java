package filius.software.dhcp;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import filius.exception.AddressRequestNotAcceptedException;
import filius.exception.NoAvailableAddressException;

public class DHCPServerTest {

    @Test
    public void testInRange() throws Exception {
        DHCPServer server = new DHCPServer();
        server.setUntergrenze("1.1.1.1");
        server.setObergrenze("1.1.1.1");
        assertTrue(server.inRange("1.1.1.1"));
    }

    @Test
    public void testInRange_ComplexRange() throws Exception {
        DHCPServer server = new DHCPServer();
        server.setUntergrenze("1.10.100.200");
        server.setObergrenze("200.100.10.1");
        assertTrue(server.inRange("50.5.20.7"));
    }

    @Test
    public void testInRange_OverUpperLimit() throws Exception {
        DHCPServer server = new DHCPServer();
        server.setUntergrenze("1.1.1.1");
        server.setObergrenze("1.1.1.2");
        assertFalse(server.inRange("1.1.1.3"));
    }

    @Test
    public void testInRange_BelowLowerLimit() throws Exception {
        DHCPServer server = new DHCPServer();
        server.setUntergrenze("1.1.1.10");
        server.setObergrenze("1.1.1.12");
        assertFalse(server.inRange("1.1.1.9"));
    }

    @Test
    public void testInRange_BelowLowerLimitInFirstPart() throws Exception {
        DHCPServer server = new DHCPServer();
        server.setUntergrenze("2.1.1.10");
        server.setObergrenze("3.1.1.12");
        assertFalse(server.inRange("1.1.1.11"));
    }

    @Test
    public void testIpToIntArray() throws Exception {
        int[] ipAsIntArray = DHCPServer.ipToIntArray("1.2.3.4");

        assertThat(ipAsIntArray.length, is(4));
        assertThat(ipAsIntArray[0], is(1));
        assertThat(ipAsIntArray[1], is(2));
        assertThat(ipAsIntArray[2], is(3));
        assertThat(ipAsIntArray[3], is(4));
    }

    @Test(expected = NumberFormatException.class)
    public void testIpToIntArray_InvalidNumberInAddress() throws Exception {
        DHCPServer.ipToIntArray("1.2.3.x");
    }

    @Test(expected = NumberFormatException.class)
    public void testIpToIntArray_NotAnIpAddress() throws Exception {
        DHCPServer.ipToIntArray("hallo.welt");
    }

    @Test(expected = NumberFormatException.class)
    public void testIpToIntArray_TooManyParts() throws Exception {
        DHCPServer.ipToIntArray("1.2.3.4.5");
    }

    @Test
    public void testIpToLong() throws Exception {
        assertThat(DHCPServer.ipToLong("1.2.3.4"), is(0x1020304l));
    }

    @Test
    public void testLongToIp() throws Exception {
        assertThat(DHCPServer.longToIp(0x01_02_03_04l), is("1.2.3.4"));
    }

    @Test
    public void testCleanUpAssignments_doNotRemoveNeverExpiringEntries() throws Exception {
        DHCPServer server = new DHCPServer();
        server.dynamicAssignedAddresses.add(new DHCPAddressAssignment("dyn-mac", "dyn-ip", 0));
        server.staticAssignedAddresses.add(new DHCPAddressAssignment("stat-mac", "stat-ip", 0));
        server.offeredAddresses.add(new DHCPAddressAssignment("offered-mac", "offered-ip", 0));

        server.cleanUpAssignments();

        assertThat(server.dynamicAssignedAddresses.size(), is(1));
        assertThat(server.staticAssignedAddresses.size(), is(1));
        assertThat(server.offeredAddresses.size(), is(1));
    }

    @Test
    public void testCleanUpAssignments_RemoveExpiredEntries() throws Exception {
        DHCPServer server = new DHCPServer();
        long timestamp = System.currentTimeMillis() - 100;
        server.dynamicAssignedAddresses.add(new DHCPAddressAssignment("dyn-mac", "dyn-ip", timestamp));
        server.staticAssignedAddresses.add(new DHCPAddressAssignment("stat-mac", "stat-ip", timestamp));
        server.offeredAddresses.add(new DHCPAddressAssignment("offered-mac", "offered-ip", timestamp));

        server.cleanUpAssignments();

        assertThat(server.dynamicAssignedAddresses.size(), is(0));
        assertThat(server.staticAssignedAddresses.size(), is(0));
        assertThat(server.offeredAddresses.size(), is(0));
    }

    @Test
    public void testCleanUpAssignments_DoNotRemoveStillValidEntries() throws Exception {
        DHCPServer server = new DHCPServer();
        long timestamp = System.currentTimeMillis() + 1000;
        server.dynamicAssignedAddresses.add(new DHCPAddressAssignment("dyn-mac", "dyn-ip", timestamp));
        server.staticAssignedAddresses.add(new DHCPAddressAssignment("stat-mac", "stat-ip", timestamp));
        server.offeredAddresses.add(new DHCPAddressAssignment("offered-mac", "offered-ip", timestamp));

        server.cleanUpAssignments();

        assertThat(server.dynamicAssignedAddresses.size(), is(1));
        assertThat(server.staticAssignedAddresses.size(), is(1));
        assertThat(server.offeredAddresses.size(), is(1));
    }

    @Test
    public void testNextAddress_Sequence() throws Exception {
        DHCPServer server = prepareDhcpServer("10.0.0.100", "10.0.0.101");

        String nextAddress1 = server.nextAddress();
        String nextAddress2 = server.nextAddress();

        assertThat(nextAddress1, is("10.0.0.100"));
        assertThat(nextAddress2, is("10.0.0.101"));
    }

    @Test
    public void testNextAddress_RepeatInterval() throws Exception {
        DHCPServer server = prepareDhcpServer("10.0.0.100", "10.0.0.101");
        server.nextAddress();
        server.nextAddress();

        String nextAddress = server.nextAddress();

        assertThat(nextAddress, is("10.0.0.100"));
    }

    @Test
    public void testAddressAvailable_NoAssignedAddresses() throws Exception {
        DHCPServer server = prepareDhcpServer("10.0.0.100", "10.0.0.101");

        boolean available = server.checkAddressAvailable("10.0.0.100");

        assertTrue(available);
    }

    @Test
    public void testAddressAvailable_AlreadyDynamicallyAssigned() throws Exception {
        String ipAddress = "10.0.0.100";
        DHCPServer server = prepareDhcpServer(ipAddress, "10.0.0.101");
        server.dynamicAssignedAddresses.add(new DHCPAddressAssignment("mac", ipAddress, 0));

        boolean available = server.checkAddressAvailable(ipAddress);

        assertFalse(available);
    }

    @Test
    public void testAddressAvailable_AlreadyStaticallyAssigned() throws Exception {
        String ipAddress = "10.0.0.100";
        DHCPServer server = prepareDhcpServer(ipAddress, "10.0.0.101");
        server.staticAssignedAddresses.add(new DHCPAddressAssignment("mac", ipAddress, 0));

        boolean available = server.checkAddressAvailable(ipAddress);

        assertFalse(available);
    }

    @Test
    public void testAddressAvailable_AlreadyOffered() throws Exception {
        String ipAddress = "10.0.0.100";
        DHCPServer server = prepareDhcpServer(ipAddress, "10.0.0.101");
        server.offeredAddresses.add(new DHCPAddressAssignment("mac", ipAddress, 0));

        boolean available = server.checkAddressAvailable(ipAddress);

        assertFalse(available);
    }

    @Test
    public void testAddressAvailable_NoApplicableAssignment() throws Exception {
        DHCPServer server = prepareDhcpServer("10.0.0.100", "10.0.0.104");
        server.dynamicAssignedAddresses.add(new DHCPAddressAssignment("mac", "10.0.0.100", 0));
        server.staticAssignedAddresses.add(new DHCPAddressAssignment("mac", "10.0.0.101", 0));
        server.offeredAddresses.add(new DHCPAddressAssignment("mac", "10.0.0.102", 0));

        boolean available = server.checkAddressAvailable("10.0.0.103");

        assertTrue(available);
    }

    @Test
    public void testOfferAddress_SkipAssigned() throws Exception {
        DHCPServer server = prepareDhcpServer("10.0.0.100", "10.0.0.104");
        server.dynamicAssignedAddresses.add(new DHCPAddressAssignment("mac", "10.0.0.100", 0));
        server.staticAssignedAddresses.add(new DHCPAddressAssignment("mac", "10.0.0.101", 0));
        server.offeredAddresses.add(new DHCPAddressAssignment("mac", "10.0.0.102", 0));

        String offeredAddress = server.offerAddress("01:02:03:04:05:06");

        assertThat(offeredAddress, is("10.0.0.103"));
    }

    @Test(expected = NoAvailableAddressException.class)
    public void testOfferAddress_NoAddressAvailable() throws Exception {
        DHCPServer server = prepareDhcpServer("10.0.0.100", "10.0.0.102");
        server.dynamicAssignedAddresses.add(new DHCPAddressAssignment("mac", "10.0.0.100", 0));
        server.staticAssignedAddresses.add(new DHCPAddressAssignment("mac", "10.0.0.101", 0));
        server.offeredAddresses.add(new DHCPAddressAssignment("mac", "10.0.0.102", 0));

        server.offerAddress("01:02:03:04:05:06");
    }

    @Test
    public void testOfferAddress_AddressMayBeOfferedJustOnce() throws Exception {
        DHCPServer server = prepareDhcpServer("10.0.0.100", "10.0.0.104");

        String offeredAddress = server.offerAddress("01:02:03:04:05:06");

        assertThat(offeredAddress, is("10.0.0.100"));
        assertFalse(server.checkAddressAvailable("10.0.0.100"));
    }

    @Test
    public void testRequestAddress_AddressOfferedToClient() throws Exception {
        DHCPServer server = prepareDhcpServer("10.0.0.100", "10.0.0.104");
        String mac = "01:02:03:04:05:06";
        String offeredAddress = server.offerAddress(mac);

        DHCPAddressAssignment assignment = server.requestAddress(mac, offeredAddress);

        verifyAssignment(server, mac, offeredAddress, assignment);
    }

    private void verifyAssignment(DHCPServer server, String mac, String ip, DHCPAddressAssignment result) {
        assertThat(result.getIp(), is(ip));
        assertThat(result.getMAC(), is(mac));
        for (DHCPAddressAssignment entry : server.offeredAddresses) {
            assertFalse(entry.getMAC().equalsIgnoreCase(mac));
        }
        boolean assigned = false;
        for (DHCPAddressAssignment entry : server.dynamicAssignedAddresses) {
            if (entry.getMAC().equalsIgnoreCase(mac) && entry.getIp().equalsIgnoreCase(ip)) {
                assigned = true;
                break;
            }
        }
        assertTrue(assigned);
    }

    @Test(expected = AddressRequestNotAcceptedException.class)
    public void testRequestAddress_AddressOfferedToOTHERClient() throws Exception {
        DHCPServer server = prepareDhcpServer("10.0.0.100", "10.0.0.104");
        String offeredAddress = server.offerAddress("aa:bb:cc:dd:ee:ff");

        server.requestAddress("01:02:03:04:05:06", offeredAddress);
    }

    @Test
    public void testRequestAddress_AddressOfferedToNoOneButStillAvailable() throws Exception {
        DHCPServer server = prepareDhcpServer("10.0.0.100", "10.0.0.104");
        String ip = "10.0.0.101";
        String mac = "01:02:03:04:05:06";

        DHCPAddressAssignment assignment = server.requestAddress(mac, ip);

        verifyAssignment(server, mac, ip, assignment);
    }

    private DHCPServer prepareDhcpServer(String lowerLimit, String upperLimit) {
        DHCPServer server = new DHCPServer();
        server.setUntergrenze(lowerLimit);
        server.setObergrenze(upperLimit);
        return server;
    }
}
