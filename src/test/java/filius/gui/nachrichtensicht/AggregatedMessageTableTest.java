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

import filius.gui.nachrichtensicht.PacketsAnalyzerDialog;
import filius.gui.nachrichtensicht.PacketsAnalyzerTable;
import filius.rahmenprogramm.Information;

public class AggregatedMessageTableTest {

    private static final String EMPTY_TABLE_EXPORT = "+============+======================+======================+======================+======================+======================+==========================================+\r\n"
            + "| Nr.        | Zeit                 | Quelle               | Ziel                 | Protokoll            | Schicht              | Bemerkungen                              | \r\n"
            + "+============+======================+======================+======================+======================+======================+==========================================+\r\n";

    @Test
    public void testWriteToStream() throws Exception {
        PipedOutputStream outputStream = new PipedOutputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new PipedInputStream(outputStream), "UTF8"));
        Information.getInstance().setLocale(Locale.GERMANY);

        PacketsAnalyzerTable messageTable = new PacketsAnalyzerTable(new PacketsAnalyzerDialog(), null);
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
        values[PacketsAnalyzerTable.REMARK_COLUMN] = "das ist eine sehr lange bemerkung, die umgebrochen werden muss!";

        List<String[]> result = PacketsAnalyzerTable.prepareDataArrays(values, 12);

        assertThat(result.get(0)[PacketsAnalyzerTable.REMARK_COLUMN]).isEqualTo("das ist eine");
        assertThat(result.get(1)[PacketsAnalyzerTable.REMARK_COLUMN]).isEqualTo("sehr lange");
        assertThat(result.get(2)[PacketsAnalyzerTable.REMARK_COLUMN]).isEqualTo("bemerkung,");
        assertThat(result.get(3)[PacketsAnalyzerTable.REMARK_COLUMN]).isEqualTo("die");
        assertThat(result.get(4)[PacketsAnalyzerTable.REMARK_COLUMN]).isEqualTo("umgebrochen");
        assertThat(result.get(5)[PacketsAnalyzerTable.REMARK_COLUMN]).isEqualTo("...");

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
        List<String> lines = PacketsAnalyzerTable.splitString("das ist mein text", 10);
        assertThat(lines).containsExactly("das ist", "mein text");
    }

    @Test
    public void testSplitString_VeryLongText() throws Exception {
        List<String> lines = PacketsAnalyzerTable.splitString("dasistmeinseeeeeeehrlangertext", 10);
        assertThat(lines).containsExactly("dasistmein", "seeeeeeehr", "langertext");
    }

    @Test
    public void testSplitString_VeryLongText_MidNotFull() throws Exception {
        List<String> lines = PacketsAnalyzerTable.splitString("0123456789012345 0123456789012345", 10);
        assertThat(lines).containsExactly("0123456789", "012345", "0123456789", "012345");
    }

    @Test
    public void testSplitString_NormalizeWhitespace() throws Exception {
        List<String> lines = PacketsAnalyzerTable.splitString(" hallo    \t    \n welt", 10);
        assertThat(lines).containsExactly("hallo welt");
    }

}
