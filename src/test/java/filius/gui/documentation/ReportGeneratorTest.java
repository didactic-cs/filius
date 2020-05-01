package filius.gui.documentation;

import static org.mockito.Mockito.when;

import java.net.URL;
import java.util.Arrays;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.itextpdf.text.Document;

import filius.Main;
import filius.rahmenprogramm.nachrichten.Lauscher;

public class ReportGeneratorTest {
    private static final String PDF_OUTPUT = "iTextHelloWorld.pdf";
    private static final Object[][] DATA = {
            { "5", "16:49:09.224", "141.99.5.11", "141.99.5.10", "ARP", "Vermittlung",
                    "Suche nach MAC für 141.99.5.10, 141.99.5.11: 73:E5:39:B7:DC:79" },
            { "6", "16:49:09.231", "141.99.5.10", "141.99.5.11", "ARP", "Vermittlung",
                    "141.99.5.10: 0E:FA:11:C2:E0:67" },
            { "7", "16:49:09.447", "141.99.5.11:10959", "141.99.5.10:55555", "TCP", "Transport",
                    "SYN, SEQ: 3440300023" } };
    public static final String[] COLUMNS = { "No.", "Time", "Source", "Destination", "Protocol", "Layer", "Comment" };
    @InjectMocks
    private ReportGenerator generator;
    @Mock
    private Lauscher lauscherMock;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @BeforeClass
    public static void initScenario() {
        URL resource = ReportGeneratorTest.class.getResource("/einfaches_rechnernetz_komplett.fls");
        String file = resource.getPath();
        Main.starten(file);
    }

    @Test
    public void testAddOverviewSection() throws Exception {
        Document document = generator.initDocument(PDF_OUTPUT);

        generator.addOverviewSection(document);

        generator.closeDocument(document);
    }

    @Test
    public void testAddComponentSection() throws Exception {
        Document document = generator.initDocument(PDF_OUTPUT);

        generator.addComponentConfigSection(document);

        generator.closeDocument(document);
    }

    @Test
    public void testAddNetworkTrafficSection() throws Exception {
        Document document = generator.initDocument(PDF_OUTPUT);
        String interfaceId = "My Interface";
        when(lauscherMock.getHeader()).thenReturn(COLUMNS);
        when(lauscherMock.getInterfaceIDs()).thenReturn(Arrays.asList(interfaceId));
        when(lauscherMock.getDaten(interfaceId, true)).thenReturn(DATA);

        generator.addNetworkTrafficSection(document);

        generator.closeDocument(document);
    }

}
