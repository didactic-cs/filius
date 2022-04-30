package filius.software.nat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import filius.software.vermittlungsschicht.IpPaket;

public class NetworkAddressTranslationTableTest {
    private NetworkAddressTranslationTable table = new NetworkAddressTranslationTable();

    @Test
    public void testFind_EmptyTable_Null() throws Exception {
        assertNull(table.find(1, IpPaket.TCP));
    }

    @Test
    public void testFind_EntryExists() throws Exception {
        InetAddress address = new InetAddress("1.1.1.1", 2002, IpPaket.TCP);
        table.addDynamic(1, IpPaket.TCP, address);

        assertEquals(address, table.find(1, IpPaket.TCP));
    }
}
