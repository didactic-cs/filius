package filius.software.vermittlungsschicht;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Test;

import filius.hardware.NetzwerkInterface;
import filius.hardware.knoten.InternetKnoten;
import filius.software.system.InternetKnotenBetriebssystem;
import filius.software.system.SystemSoftware;

public class VermittlungsProtokollTest {

    @Test
    public void testGleichesRechnernetz_Success() throws Exception {
        boolean isBroadcast = VermittlungsProtokoll.gleichesRechnernetz("10.0.0.1", "10.0.0.2", "255.255.255.0");

        assertThat(isBroadcast, is(true));
    }

    @Test
    public void testGleichesRechnernetz_Failure() throws Exception {
        boolean isBroadcast = VermittlungsProtokoll.gleichesRechnernetz("10.0.0.1", "10.0.1.2", "255.255.255.0");

        assertThat(isBroadcast, is(false));
    }

    @Test
    public void testIsNetworkAddress_Yes() throws Exception {
        boolean isNetworkAddress = VermittlungsProtokoll.isNetworkAddress("10.0.0.0", "10.0.0.1", "255.255.255.0");

        assertThat(isNetworkAddress, is(true));
    }

    @Test
    public void testIsNetworkAddress_No() throws Exception {
        boolean isNetworkAddress = VermittlungsProtokoll.isNetworkAddress("10.0.0.2", "10.0.0.1", "255.255.255.0");

        assertThat(isNetworkAddress, is(false));
    }

    @Test
    public void testIsNetworkAddress_ToOtherNetwork_No() throws Exception {
        boolean isNetworkAddress = VermittlungsProtokoll.isNetworkAddress("10.0.1.0", "10.0.0.1", "255.255.255.0");

        assertThat(isNetworkAddress, is(false));
    }

    @Test
    public void testIstBroadcast_No_InCaseOfLocalhost() throws Exception {
        boolean isBroadcast = VermittlungsProtokoll.isBroadcast("10.0.0.1", "10.0.0.1", "255.255.255.0");

        assertThat(isBroadcast, is(false));
    }

    @Test
    public void testIstBroadcast_Yes_InCaseOf255() throws Exception {
        boolean isBroadcast = VermittlungsProtokoll.isBroadcast("10.0.0.255", "10.0.0.1", "255.255.255.0");

        assertThat(isBroadcast, is(true));
    }

    @Test
    public void testIstBroadcast_Yes_DifficultNetmask() throws Exception {
        boolean isBroadcast = VermittlungsProtokoll.isBroadcast("10.0.63.255", "10.0.63.1", "255.255.192.0");

        assertThat(isBroadcast, is(true));
    }

    @Test
    public void testIstBroadcast_No_DifficultNetmask() throws Exception {
        boolean isBroadcast = VermittlungsProtokoll.isBroadcast("10.0.31.255", "10.0.31.1", "255.255.192.0");

        assertThat(isBroadcast, is(false));
    }

    @Test
    public void testIstBroadcast_Yes_GleichesRechnernetz() throws Exception {
        boolean isBroadcast = VermittlungsProtokoll.isBroadcast("10.0.0.255", "10.0.0.11", "255.255.255.0");

        assertThat(isBroadcast, is(true));
    }

    @Test
    public void testIstBroadcast_No_AnderesRechnernetz() throws Exception {
        boolean isBroadcast = VermittlungsProtokoll.isBroadcast("10.0.0.255", "10.0.1.11", "255.255.255.0");

        assertThat(isBroadcast, is(false));
    }

    @Test
    public void testIstBroadcast_No_ToNetworkAddress() throws Exception {
        boolean isBroadcast = VermittlungsProtokoll.isBroadcast("10.0.0.0", "10.0.0.1", "255.255.192.0");

        assertThat(isBroadcast, is(false));
    }

    @Test
    public void testIstBroadcast_Yes_GenericBroadcastAddress() throws Exception {
        boolean isBroadcast = VermittlungsProtokoll.isBroadcast("255.255.255.255", "10.0.0.1", "255.255.192.0");

        assertThat(isBroadcast, is(true));
    }

    private class VermittlungsProtokollTestable extends VermittlungsProtokoll {
        public VermittlungsProtokollTestable(SystemSoftware systemSoftware) {
            super(systemSoftware);
        }

        @Override
        public void starten() {
            throw new RuntimeException();
        }

        @Override
        public void beenden() {
            throw new RuntimeException();
        }
    }

    @Test
    public void testIsApplicableBroadcast_Success() throws Exception {
        String localIpAddress = "10.10.10.5";
        String subnetzMaske = "255.255.255.0";
        InternetKnotenBetriebssystem betriebssystem = initSystemSoftwareAndNodeMock(localIpAddress, subnetzMaske);
        VermittlungsProtokoll protokoll = new VermittlungsProtokollTestable(betriebssystem);

        boolean isApplicableBroadcast = protokoll.isApplicableBroadcast("10.10.10.255");

        assertThat(isApplicableBroadcast, is(true));
    }

    private InternetKnotenBetriebssystem initSystemSoftwareAndNodeMock(String localIpAddress, String subnetzMaske) {
        InternetKnoten internetKnoten = mock(InternetKnoten.class);
        NetzwerkInterface nic = new NetzwerkInterface();
        nic.setIp(localIpAddress);
        nic.setSubnetzMaske(subnetzMaske);
        when(internetKnoten.getNetzwerkInterfaces()).thenReturn(Arrays.asList(nic));
        InternetKnotenBetriebssystem betriebssystem = mock(InternetKnotenBetriebssystem.class);
        when(betriebssystem.getKnoten()).thenReturn(internetKnoten);
        return betriebssystem;
    }

    @Test
    public void testIsApplicableBroadcast_NotBroadcast_Failure() throws Exception {
        String localIpAddress = "10.10.10.5";
        String subnetzMaske = "255.255.255.0";
        InternetKnotenBetriebssystem betriebssystem = initSystemSoftwareAndNodeMock(localIpAddress, subnetzMaske);
        VermittlungsProtokoll protokoll = new VermittlungsProtokollTestable(betriebssystem);

        boolean isApplicableBroadcast = protokoll.isApplicableBroadcast("10.10.10.254");

        assertThat(isApplicableBroadcast, is(false));
    }

    @Test
    public void testIsApplicableBroadcast_WrongNetwork_Failure() throws Exception {
        String localIpAddress = "10.10.10.5";
        String subnetzMaske = "255.255.255.0";
        InternetKnotenBetriebssystem betriebssystem = initSystemSoftwareAndNodeMock(localIpAddress, subnetzMaske);
        VermittlungsProtokoll protokoll = new VermittlungsProtokollTestable(betriebssystem);

        boolean isApplicableBroadcast = protokoll.isApplicableBroadcast("10.11.10.255");

        assertThat(isApplicableBroadcast, is(false));
    }

}
