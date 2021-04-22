package filius.rahmenprogramm;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.File;
import java.io.PrintStream;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Test;

public class FiliusArgsTest {
    FiliusArgs filiusArgs = new FiliusArgs();

    @Test
    public void testParseCommandLine_showHelp() throws Exception {
        filiusArgs.parseCommandLine(new String[] { "-h" });

        assertTrue(filiusArgs.help);
    }

    @Test
    public void testParseCommandLine_Default() throws Exception {
        filiusArgs.parseCommandLine(new String[] {});

        assertFalse(filiusArgs.help);
        assertFalse(filiusArgs.verbose);
        assertFalse(filiusArgs.log);
        assertFalse(filiusArgs.nativeLookAndFeel);
        assertFalse(filiusArgs.lowResolution);
    }

    @Test
    public void testParseCommandLine_verbose() throws Exception {
        filiusArgs.parseCommandLine(new String[] { "-v" });

        assertTrue(filiusArgs.verbose);
    }

    @Test
    public void testParseCommandLine_WorkingDir() throws Exception {
        filiusArgs.parseCommandLine(new String[] { "-wd", "/any/path" });

        assertThat(filiusArgs.currWD, is("/any/path"));
    }

    @Test
    public void testParseCommandLine_WorkingDirWithSpace() throws Exception {
        filiusArgs.parseCommandLine(new String[] { "-wd", "/any/path with space" });

        assertThat(filiusArgs.currWD, is("/any/path with space"));
    }

    @Test
    public void testParseCommandLine_InvalidRTT() throws Exception {
        filiusArgs.parseCommandLine(new String[] { "-r", "not a number" });

        assertThat(filiusArgs.rtt, is(1));
    }

    @Test
    public void testParseCommandLine_ValidRTT() throws Exception {
        filiusArgs.parseCommandLine(new String[] { "-r", "2" });

        assertThat(filiusArgs.rtt, is(2));
    }

    @Test
    public void testParseCommandLine_Log() throws Exception {
        filiusArgs.parseCommandLine(new String[] { "-l" });

        assertTrue(filiusArgs.log);
    }

    @Test
    public void testParseCommandLine_NativeLookAndFeel() throws Exception {
        filiusArgs.parseCommandLine(new String[] { "-n" });

        assertTrue(filiusArgs.nativeLookAndFeel);
    }

    @Test
    public void testParseCommandLine_LowRes() throws Exception {
        filiusArgs.parseCommandLine(new String[] { "-s" });

        assertTrue(filiusArgs.lowResolution);
    }

    @Test
    public void testParseCommandLine_ProjectFile() throws Exception {
        File testFile = new File("test.fls");
        testFile.createNewFile();

        filiusArgs.parseCommandLine(new String[] { testFile.getPath() });

        assertThat(filiusArgs.projectFile, is(testFile.getAbsolutePath()));
    }

    @Test
    public void testParseCommandLine_NotExistentProjectFile() throws Exception {
        File testFile = new File("this file does not exist.fls");

        filiusArgs.parseCommandLine(new String[] { testFile.getPath() });

        assertThat(filiusArgs.projectFile, nullValue());
    }

    @Test
    public void testShowUsage() throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer));

        filiusArgs.showUsageInformation();

        String output = new String(buffer.toByteArray());
        assertThat(output, containsString("-h"));
        assertThat(output, containsString("-n"));
        assertThat(output, containsString("-s"));
        assertThat(output, containsString("-wd"));
        assertThat(output, containsString("-l"));
        assertThat(output, containsString("-v"));
        assertThat(output, containsString("-r"));
    }

}
