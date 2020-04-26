package filius.software.transportschicht;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class TCPSocketTest {

    @Test
    public void testNextSequenceNumberTcpSegment_SYN() throws Exception {
        TcpSegment segment = new TcpSegment();
        segment.setSyn(true);
        segment.setSeqNummer(42);

        long newSequenceNumber = TCPSocket.nextSequenceNumber(segment);

        assertThat(newSequenceNumber, is(segment.getSeqNummer() + 1));
    }

    @Test
    public void testNextSequenceNumberTcpSegment_NoSYNAndNoData() throws Exception {
        TcpSegment segment = new TcpSegment();
        segment.setSyn(false);
        segment.setSeqNummer(42);

        long newSequenceNumber = TCPSocket.nextSequenceNumber(segment);

        assertThat(newSequenceNumber, is(segment.getSeqNummer()));
    }

    @Test
    public void testNextSequenceNumberTcpSegment_WithData() throws Exception {
        TcpSegment segment = new TcpSegment();
        segment.setSyn(false);
        segment.setSeqNummer(42);
        String data = "11";
        segment.setDaten(data);

        long newSequenceNumber = TCPSocket.nextSequenceNumber(segment);

        assertThat(newSequenceNumber, is(segment.getSeqNummer() + 2));
    }

    @Test
    public void testNextSequenceNumberTcpSegment_NumberOverflow() throws Exception {
        TcpSegment segment = new TcpSegment();
        segment.setSyn(false);
        segment.setSeqNummer((long) (Math.pow(2, 32)) - 1);
        String data = "1";
        segment.setDaten(data);

        long newSequenceNumber = TCPSocket.nextSequenceNumber(segment);

        assertThat(newSequenceNumber, is(0l));
    }

}
