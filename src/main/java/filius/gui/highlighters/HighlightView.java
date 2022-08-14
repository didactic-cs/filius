package filius.gui.simulationmode.highlighters;

import static java.awt.RenderingHints.KEY_TEXT_ANTIALIASING;
import static java.awt.RenderingHints.KEY_TEXT_LCD_CONTRAST;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.text.PlainView;
import javax.swing.text.Segment;

public class HighlightView extends PlainView {

	private int       patternCount = 0;
	private Pattern[] patterns;
	private Color[]   colors;
	private int[]     styles;
	
	// The maps store the sections to be highlighted
	// Both maps use the start of a section as key 
	private TreeMap<Integer, Integer> endMap   = new TreeMap<Integer, Integer>();
	private TreeMap<Integer, Color>   colorMap = new TreeMap<Integer, Color>();
	private TreeMap<Integer, Integer> styleMap = new TreeMap<Integer, Integer>();
	
	private Color defaultColor = Color.black;
	private int defaultStyle = Font.PLAIN;


	public HighlightView(Element elem) {
		
		super(elem);

        // One tab equals 4 spaces
        getDocument().putProperty(PlainDocument.tabSizeAttribute, 4);
	}
	
	//-----------------------------------------------------------------------------------------
    //  Highlighting
    //-----------------------------------------------------------------------------------------
	
	public void initPatternArrays(int count) {
		
		patternCount = count;
		patterns = new Pattern[count];
		colors = new Color[count];
		styles = new int[count];
		
		for (int i = 0; i < count; i++)  patterns[i] = null;
	}
		
	public void setDefaultColorAndStyle(Color color, int fontStyle) {
		
		defaultColor = color;
		defaultStyle = fontStyle;
	}
	
	public void addPattern(String pattern, Color color, int style) {
		
		int i = 0;
		while (patterns[i] != null)  i++;
		if (i == patternCount)  return;
		
		patterns[i] = Pattern.compile(pattern);
		colors[i] = color;
		styles[i] = style;
	}
	
	/** parseText() is called whenever the text is modified to update the maps.<br>
	 *  The whole document is parsed because the highlighting patterns may overlap 
	 *  several lines.
	 */
	public void parseText() {
		
		Document doc = getDocument();
		String text = "";		
		try {
			text = doc.getText(0, doc.getLength());
		} catch (BadLocationException e) {}		

		// Locate all sections to be highlighted
		endMap.clear();
		colorMap.clear();

		for (int i = 0; i < patterns.length; i++) {
			
			//Matcher matcher = entry.getKey().matcher(text);
			Matcher matcher = patterns[i].matcher(text);

			int pos = 0;
			while (matcher.find(pos)) {            	
				int start = matcher.start(1);
				int end = matcher.end(1);

				// Ignore if found section is located within an already registered section
				Map.Entry<Integer, Integer> low = endMap.floorEntry(start);
				if (low != null && low.getValue() >= start)  {
					pos = low.getValue();
					continue;
				}

				// Add the section
				endMap.put(start, end);
				colorMap.put(start, colors[i]);
				styleMap.put(start, styles[i]);
				pos = end;
			}
		}
	}
	
	private int drawColorText(Document doc, Segment s, Graphics2D g2d, Color color, int style, int x, int y,
			                  int p0, int startOffset, int endOffset) {

		g2d.setColor(color);
		g2d.setFont(g2d.getFont().deriveFont(style));
		
		try { doc.getText(p0 + startOffset, endOffset - startOffset, s); } 
		catch (BadLocationException e) {}
		
		return (int) drawText(g2d, s, (float) x, (float) y, startOffset);
	}
	
	@Override
	protected int drawUnselectedText(Graphics graphics, int x, int y, int p0, int p1) {

		Graphics2D g2d = (Graphics2D) graphics;

		Document doc = getDocument();
		Segment segment = getLineBuffer();

		// Highlight the section
		int sectionLength = p1 - p0;
		int pos = 0;

		for (Map.Entry<Integer, Integer> entry : endMap.entrySet()) {

			int start = entry.getKey();
			if (start >= p1)  break;
			
			int end = entry.getValue() - p0;			
			if (end <= 0)  continue;
			
			Color sectionColor = colorMap.get(start);
			int sectionStyle = styleMap.get(start);
			start = start - p0;
			
			// Remain within the current range
			if (start < 0)  start = 0;
			if (end > sectionLength)  end = sectionLength;

			// Text between the end of the last highlighted section and the beginning of the current
			if (pos < start)  x = drawColorText(doc, segment, g2d, defaultColor, defaultStyle, x, y, p0, pos, start);

			// Highlight the current section
			x = drawColorText(doc, segment, g2d, sectionColor, sectionStyle, x, y, p0, start, end);
			pos = end;
		}

		// Text after the end of the last section to be highlighted
		if (pos < sectionLength)  x = drawColorText(doc, segment, g2d, defaultColor, defaultStyle, x, y, p0, pos, sectionLength);

		return x;
	}
	
	//-----------------------------------------------------------------------------------------
    //  Anti-aliasing required to get a nice display of the text
    //-----------------------------------------------------------------------------------------
    
    private Object prevAntiAliasingRH;
    private Object prevLCDContrastRH;
    
    private void beginAntiAliasing(Graphics2D g) {
    	
    	JComponent comp = (JComponent) getContainer();

    	prevAntiAliasingRH = null;
    	prevLCDContrastRH = null;

    	Object reqAntiAliasingRH = null;
    	if (comp != null)  reqAntiAliasingRH = comp.getClientProperty(KEY_TEXT_ANTIALIASING);

    	if (reqAntiAliasingRH != null) {

    		prevAntiAliasingRH = g.getRenderingHint(KEY_TEXT_ANTIALIASING);
    		if (reqAntiAliasingRH != prevAntiAliasingRH) {
    			g.setRenderingHint(KEY_TEXT_ANTIALIASING, reqAntiAliasingRH);
    		} else {
    			prevAntiAliasingRH = null;
    		}

    		Object reqLCDContrastRH = comp.getClientProperty(KEY_TEXT_LCD_CONTRAST);

    		if (reqLCDContrastRH != null) {
    			prevLCDContrastRH = g.getRenderingHint(KEY_TEXT_LCD_CONTRAST);
    			if (reqLCDContrastRH.equals(prevLCDContrastRH)) {
    				prevLCDContrastRH = null;
    			} else {
    				g.setRenderingHint(KEY_TEXT_LCD_CONTRAST, reqLCDContrastRH);
    			}
    		}
    	}
    }
    
    private void endAntiAliasing(Graphics2D g) {

    	if (prevLCDContrastRH != null)  g.setRenderingHint(KEY_TEXT_LCD_CONTRAST, prevLCDContrastRH);
    	if (prevAntiAliasingRH != null) g.setRenderingHint(KEY_TEXT_ANTIALIASING, prevAntiAliasingRH);
    }    
    
    private static float getTextWidth(Graphics2D g, char[] data, int offset, int length) {    	
    	
    	if (length == 0) return 0;   
    	FontMetrics fontMetrics = g.getFontMetrics();
    	return (float) fontMetrics.getFont().getStringBounds(data, offset, offset + length, fontMetrics.getFontRenderContext()).getWidth();
    }
    
    private float drawString(Graphics2D g, char[] data, int offset, int length, float x, float y) {
    	
    	g.drawString(new String(data, offset, length), x, y);  
    	
    	return x + getTextWidth(g, data, offset, length);
    }
    
    public float drawText(Graphics2D g, Segment s, float x, float y, int startOffset) {
    	
    	beginAntiAliasing(g);
    	
    	float nextX = x;
    	char[] txt = s.array;
    	int txtOffset = s.offset;
    	int flushLen = 0;
    	int flushIndex = s.offset;
    	int spaceAddon = 0;
    	int spaceAddonLeftoverEnd = -1;
    	int startJustifiableContent = 0;
    	int endJustifiableContent = 0;
    	int n = s.offset + s.count;
    	
    	for (int i = txtOffset; i < n; i++) {
    		if (txt[i] == '\t' || ((spaceAddon != 0 || i <= spaceAddonLeftoverEnd) && (txt[i] == ' ') &&
    			startJustifiableContent <= i && i <= endJustifiableContent )) {
    			if (flushLen > 0) {
    				nextX = drawString(g, txt, flushIndex, flushLen, x, y);
    				flushLen = 0;
    			}
    			flushIndex = i + 1;
    			if (txt[i] == '\t') {
    				nextX = nextTabStop(nextX, startOffset + i - txtOffset);
    			} else if (txt[i] == ' ') {
    				float spaceWidth = getTextWidth(g, new char[]{' '}, 0, 1);
    				nextX += spaceWidth + spaceAddon;
    				if (i <= spaceAddonLeftoverEnd)  nextX++;
    			}
    			x = nextX;
    		} else if ((txt[i] == '\n') || (txt[i] == '\r')) {
    			if (flushLen > 0) {
    				nextX = drawString(g, txt, flushIndex, flushLen, x, y);
    				flushLen = 0;
    			}
    			flushIndex = i + 1;
    			x = nextX;
    		} else {
    			flushLen += 1;
    		}
    	}
    	if (flushLen > 0)  nextX = drawString(g, txt, flushIndex, flushLen, x, y);
    	
    	endAntiAliasing(g);
    	
    	return nextX;
    }
}
