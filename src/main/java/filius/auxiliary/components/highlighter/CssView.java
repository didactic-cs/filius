package filius.gui.simulationmode.highlighters;

import java.awt.Color;
import java.awt.Font;

import javax.swing.text.Element;

public class CssView extends HighlightView {

	private static String SEP              = "[ |\\t|\\n]";
	private static String BASIC_NAME       = "[A-Za-z\\.#][A-Za-z0-9\\-_\\.#]*";

	private static String PAT_COMMENT      = "(?ms)(/\\*.*?\\*/)";
	private static String PAT_SELECTOR     = "(" + BASIC_NAME + ")[^\\}]*\\{";
	private static String PAT_PROPERTY     = "(" + BASIC_NAME + ")" + SEP + "*:";
	private static String PAT_VALUE        = ":" + "([^;|\\}]*)";   

	private static Color COLOR_DEFAULT     = Color.black;
	private static Color COLOR_COMMENT     = new Color(0, 128, 0);     // Green
	private static Color COLOR_SELECTOR    = new Color(0, 90, 56);     // Dark green
	private static Color COLOR_PROPERTY    = new Color(128, 0, 0);     // Dark red
	private static Color COLOR_VALUE       = new Color(64, 64, 254);   // Pale blue
	
	private static int STYLE_DEFAULT       = Font.PLAIN;
	private static int STYLE_COMMENT       = Font.PLAIN;
	private static int STYLE_SELECTOR      = Font.BOLD;
	private static int STYLE_PROPERTY      = Font.PLAIN;
	private static int STYLE_VALUE         = Font.ITALIC;
			

	public CssView(Element elem) {
		
		super(elem);
		
		setDefaultColorAndStyle(COLOR_DEFAULT, STYLE_DEFAULT);
		
		// Patterns to be found should not overlap!
	    // Comment pattern should come first to prevent its content from being parsed 
		
		initPatternArrays(4);
		addPattern(PAT_COMMENT, COLOR_COMMENT, STYLE_COMMENT);     // First
		addPattern(PAT_SELECTOR, COLOR_SELECTOR, STYLE_SELECTOR);
		addPattern(PAT_PROPERTY, COLOR_PROPERTY, STYLE_PROPERTY);
		addPattern(PAT_VALUE, COLOR_VALUE, STYLE_VALUE);
	}
}
