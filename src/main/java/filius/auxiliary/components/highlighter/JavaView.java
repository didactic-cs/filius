package filius.gui.simulationmode.highlighters;

import java.awt.Color;
import java.awt.Font;

import javax.swing.text.Element;

public class JavaView extends HighlightView {

	private static String SEP              = "[ |\\t|\\n]";
	private static String BASIC_NAME       = "[A-Za-z][A-Za-z0-9_]*";
	private static String KEYWORDS         = "abstract|assert|boolean|break|byte|case|catch|char|class|const|" +
	                                         "continue|default|do|double|else|enum|extends|false|final|finally|float|" +
	                                         "for|goto|if|implements|import|instanceof|int|interface|long|native|" +
	                                         "new|null|package|private|protected|public|return|short|static|strictfp|super|" +
	                                         "switch|synchronized|this|throw|throws|transient|true|try|void|volatile|while";

	private static String PAT_COMMENT      = "(?ms)(/\\*.*?\\*/)";
	private static String PAT_LINE_COMMENT = "(//.*)";
	private static String PAT_KEYWORD      = "\\b(" + KEYWORDS + ")\\b";
	private static String PAT_PACKAGE      = "package" + SEP + "*(" + BASIC_NAME + ")" + SEP + "*[;]";
	private static String PAT_IMPORT       = "import" + SEP + "*(" + BASIC_NAME + "(." +BASIC_NAME + ")*)" + SEP + "*[;]";
	private static String PAT_VARIABLE     = "(" + BASIC_NAME + ")" + SEP + "*[=;]";
	private static String PAT_STRING       = "[(\\=+]" + SEP + "*\"([^\"]+?)\"";
	private static String PAT_CHAR         = "[(\\=+]" + SEP + "*'([^']+?)'";

	private static Color COLOR_DEFAULT     = Color.black;
	private static Color COLOR_COMMENT     = new Color(0, 128, 0);     // Green
	private static Color COLOR_KEYWORD     = new Color(128, 0, 128);   // Purple
	private static Color COLOR_VARIABLE    = new Color(42, 0, 255);    // Blue
	private static Color COLOR_STRING      = new Color(42, 0, 255);    // Blue
	
	private static int STYLE_DEFAULT       = Font.PLAIN;
	private static int STYLE_COMMENT       = Font.PLAIN;
	private static int STYLE_KEYWORD       = Font.BOLD;
	private static int STYLE_VARIABLE      = Font.PLAIN;
	private static int STYLE_STRING        = Font.PLAIN;
	

	public JavaView(Element elem) {
		
		super(elem);
		
		setDefaultColorAndStyle(COLOR_DEFAULT, STYLE_DEFAULT);
		
		// Patterns to be found should not overlap!
	    // Comment pattern should come first to prevent its content from being parsed 
		
		initPatternArrays(8);
		addPattern(PAT_COMMENT, COLOR_COMMENT, STYLE_COMMENT);       // First
		addPattern(PAT_LINE_COMMENT, COLOR_COMMENT, STYLE_COMMENT);  // Second
		addPattern(PAT_KEYWORD, COLOR_KEYWORD, STYLE_KEYWORD);
		addPattern(PAT_PACKAGE, COLOR_DEFAULT, STYLE_DEFAULT);
		addPattern(PAT_IMPORT, COLOR_DEFAULT, STYLE_DEFAULT);
		addPattern(PAT_VARIABLE, COLOR_VARIABLE, STYLE_VARIABLE);
		addPattern(PAT_STRING, COLOR_STRING, STYLE_STRING);
		addPattern(PAT_CHAR, COLOR_STRING, STYLE_STRING);
	}
}
