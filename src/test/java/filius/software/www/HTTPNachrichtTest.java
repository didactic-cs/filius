package filius.software.www;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class HTTPNachrichtTest {

    @Test
    public void testHttpEncodePath_MitLeerzeichen() throws Exception {
        String result = HTTPNachricht.encodePath("ein test");
        assertThat(result, is("ein%20test"));
    }

    @Test
    public void testHttpEncodePath_WithUmlaut() throws Exception {
        String result = HTTPNachricht.encodePath("späßchen");
        assertThat(result, is("sp%C3%A4%C3%9Fchen"));
    }

    @Test
    public void testHttpEncodePath_PathSeparator() throws Exception {
        String result = HTTPNachricht.encodePath("/");
        assertThat(result, is("/"));
    }

    @Test
    public void testHttpEncodePath_ComplexPath() throws Exception {
        String result = HTTPNachricht.encodePath("/hallo welt/eine datei.txt");
        assertThat(result, is("/hallo%20welt/eine%20datei.txt"));
    }

    @Test
    public void testHttpDecodePath_ComplexPath() throws Exception {
        String result = HTTPNachricht.decodePath("/hallo%20welt/eine%20datei.txt");
        assertThat(result, is("/hallo welt/eine datei.txt"));
    }

    @Test
    public void testHttpDecodePath_WithUmlaut() throws Exception {
        String result = HTTPNachricht.decodePath("sp%C3%A4%C3%9Fchen");
        assertThat(result, is("späßchen"));
    }

}
