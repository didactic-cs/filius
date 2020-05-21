package filius.gui.documentation;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.itextpdf.text.Document;

import filius.Main;

@RunWith(JUnit4.class)
public class ReportGenerator2Test {
    private static final String PDF_OUTPUT = "test2.pdf";
    public static final String[] COLUMNS = { "No.", "Time", "Source", "Destination", "Protocol", "Layer", "Comment" };
    @InjectMocks
    private ReportGenerator generator;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    private void initScenario(String filiusProject) {
        URL resource = ReportGenerator2Test.class.getResource(filiusProject);
        String file = resource.getPath();
        Main.starten(file);
    }

    @Test
    public void testGenerateReport_EmptyDocument() throws Exception {
        initScenario("/empty.fls");
        Document document = generator.initDocument(PDF_OUTPUT);

        generator.addOverviewSection(document);
        generator.addComponentConfigSection(document);
        generator.closeDocument(document);
    }

}
