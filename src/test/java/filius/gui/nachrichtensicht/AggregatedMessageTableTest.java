package filius.gui.nachrichtensicht;

import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.Test;

import filius.rahmenprogramm.Information;

public class AggregatedMessageTableTest {

    private static final String EMPTY_TABLE_EXPORT = "+============+======================+======================+======================+======================+======================+==========================================+\r\n"
            + "| Nr.        | Zeit                 | Quelle               | Ziel                 | Protokoll            | Schicht              | Bemerkungen                              | \r\n"
            + "+============+======================+======================+======================+======================+======================+==========================================+\r\n";

    @Test
    public void testWriteToStream() throws Exception {
        PipedOutputStream outputStream = new PipedOutputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new PipedInputStream(outputStream), "UTF8"));
        Information.getInformation().setLocale(Locale.GERMANY);

        AggregatedMessageTable messageTable = new AggregatedMessageTable(new AggregatedExchangeDialog(), null);
        messageTable.writeToStream(outputStream);
        outputStream.close();

        String output = readString(reader);
        reader.close();
        System.out.println(output);
        assertThat(output, is(EMPTY_TABLE_EXPORT));
    }

    @Test
    public void testPrepareDataArrays_TooMuchRows() throws Exception {
        String[] values = new String[10];
        Arrays.fill(values, "anything");
        values[AggregatedMessageTable.BEMERKUNG_SPALTE] = "das ist eine sehr lange bemerkung, die umgebrochen werden muss!";

        List<String[]> result = AggregatedMessageTable.prepareDataArrays(values, 12);

        assertThat(result.get(0)[AggregatedMessageTable.BEMERKUNG_SPALTE]).isEqualTo("das ist eine");
        assertThat(result.get(1)[AggregatedMessageTable.BEMERKUNG_SPALTE]).isEqualTo("sehr lange");
        assertThat(result.get(2)[AggregatedMessageTable.BEMERKUNG_SPALTE]).isEqualTo("bemerkung,");
        assertThat(result.get(3)[AggregatedMessageTable.BEMERKUNG_SPALTE]).isEqualTo("die");
        assertThat(result.get(4)[AggregatedMessageTable.BEMERKUNG_SPALTE]).isEqualTo("umgebrochen");
        assertThat(result.get(5)[AggregatedMessageTable.BEMERKUNG_SPALTE]).isEqualTo("...");

    }

    private String readString(BufferedReader reader) throws IOException {
        int nextChar;
        StringBuilder buffer = new StringBuilder();
        while ((nextChar = reader.read()) != -1) {
            buffer.append((char) nextChar);
        }
        String output = buffer.toString();
        return output;
    }

    @Test
    public void testSplitString() throws Exception {
        List<String> lines = AggregatedMessageTable.splitString("das ist mein text", 10);
        assertThat(lines).containsExactly("das ist", "mein text");
    }

    @Test
    public void testSplitString_VeryLongText() throws Exception {
        List<String> lines = AggregatedMessageTable.splitString("dasistmeinseeeeeeehrlangertext", 10);
        assertThat(lines).containsExactly("dasistmein", "seeeeeeehr", "langertext");
    }

    @Test
    public void testSplitString_VeryLongText_MidNotFull() throws Exception {
        List<String> lines = AggregatedMessageTable.splitString("0123456789012345 0123456789012345", 10);
        assertThat(lines).containsExactly("0123456789", "012345", "0123456789", "012345");
    }

    @Test
    public void testSplitString_NormalizeWhitespace() throws Exception {
        List<String> lines = AggregatedMessageTable.splitString(" hallo    \t    \n welt", 10);
        assertThat(lines).containsExactly("hallo welt");
    }

}
