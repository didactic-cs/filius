package filius.gui.simulationmode.highlighters;

import java.awt.Color;
import java.awt.Font;

import javax.swing.text.Element;

public class HtmlView extends HighlightView {

	private static String SEP              = "[ |\\t|\\n]";
	private static String BASIC_NAME       = "[A-Za-z][A-Za-z0-9\\-_]*";
	//private static String GENERIC_XML_NAME = BASIC_NAME + "(:" + BASIC_NAME + ")?";

	private static String PAT_COMMENT      = "(?ms)(<\\!--.*?-->)";
	private static String PAT_NAME         = "</? *(" + BASIC_NAME + ")";
	private static String PAT_ATTRIBUTE    = SEP + "+(" + BASIC_NAME + ")" + SEP + "*\\=";
	private static String PAT_VALUE        = "=" + SEP + "*\"([^\"]+)\"";   

	private static Color COLOR_DEFAULT     = Color.black;
	private static Color COLOR_COMMENT     = new Color(0, 128, 0);   // Dark green
	private static Color COLOR_NAME        = new Color(0, 0, 150);   // Dark blue
	private static Color COLOR_ATTRIBUTE   = new Color(127, 0, 0);   // Dark red
	private static Color COLOR_VALUE       = new Color(64, 64, 254); // Pale blue
	
	private static int STYLE_DEFAULT       = Font.PLAIN;
	private static int STYLE_COMMENT       = Font.PLAIN;
	private static int STYLE_NAME          = Font.BOLD;
	private static int STYLE_ATTRIBUTE     = Font.PLAIN;
	private static int STYLE_VALUE         = Font.ITALIC;
			

	public HtmlView(Element elem) {
		
		super(elem);
		
		setDefaultColorAndStyle(COLOR_DEFAULT, STYLE_DEFAULT);
		
		// Patterns to be found should not overlap!
	    // Comment pattern should come first to prevent its content from being parsed 
		
		initPatternArrays(4);
		addPattern(PAT_COMMENT, COLOR_COMMENT, STYLE_COMMENT);       // First
		addPattern(PAT_NAME, COLOR_NAME, STYLE_NAME);
		addPattern(PAT_ATTRIBUTE, COLOR_ATTRIBUTE, STYLE_ATTRIBUTE);
		addPattern(PAT_VALUE, COLOR_VALUE, STYLE_VALUE);
	}
}
