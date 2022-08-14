package filius.project.export;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfDestination;
import com.itextpdf.text.pdf.PdfOutline;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPHeaderCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.VerticalPositionMark;

public class PdfDocument {
	
	public static final Color TRANSPARENT  = new Color( 0, 0, 0, 0);
	
	private String filename;
	private Document document;
	private PdfWriter writer;
	private PdfContentByte canvas;
	private PdfPTable table = null;
	private float indentValue = 0;
	private BaseColor evenColor = BaseColor.WHITE;
	private BaseColor oddColor = BaseColor.LIGHT_GRAY;
	private boolean tablePadLeft = false;
	private boolean tablePadRight = false;
	private PdfOutline lastCreatedBookmark = null;
	
	
	public PdfDocument(String filename) {
		
		this.filename = filename;
		
		init();
	};
	
	private void init() {

		document = new Document();
		document.setMargins(30, 30, 20, 20);
		document.setPageSize(PageSize.A4.rotate());
		try {
			writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
		} catch (FileNotFoundException | DocumentException e) {
			document = null;
			return;
		}
		document.open();
		canvas = writer.getDirectContent();
	}
	
	public boolean isInitialized() {
		
		return (document != null);
	}
	
	public void close() {
		
		if (document == null)  return;		
		document.close();
		document = null;
	};
    
    /**
     * Define the standard metadata
     * @param title
     * @param creator
     * @param author
     * @param subject
     */
    public void addDescription(String title, String author, String subject, String creator) {
    	
    	if (document == null)  return;
    	
		document.addTitle(title);
		document.addAuthor(author);
		document.addSubject(subject);
		document.addCreator(creator);
    }
    
    /**
     * Define the keyworks as metadata
     * @param keywords
     */
    public void addKeywords(String keywords) {
    	
    	if (document == null)  return;
    	
		document.addKeywords(keywords);
    }
    
    /**
    * Adds an Element: Chunk, Phrase, Paragraph, Table, Image...
    * @param element
    */
    public void add(Element element) {
    	
    	if (document == null)  return;
    	
		try {
			document.add(element);
		} catch (DocumentException e) {}
    }
    
    public void setIndentValue(float indentValue) {
    	
    	this.indentValue = indentValue;
    }
    
    /**
     * addTextLine adds a short text
     * 
     * @param text
     * @param font
     * @param alignment
     * @param spacingAboveFactor space left above expressed as a factor of the font size
     * @param spacingBelowFactor space left below expressed as a factor of the font size
     */
    public void addTextLine(int indentLevel, String text, Font font, boolean center, float spacingAboveFactor, float spacingBelowFactor) {
    	
    	float fontSize = font.getSize();
    	
    	if (spacingAboveFactor != 0)  addVPad(fontSize * spacingAboveFactor);
        	
    	Paragraph para = new Paragraph(text, font);
    	para.setLeading(0); // Removes the empty line automatically added after the paragraph
    	para.setAlignment(center ? Paragraph.ALIGN_CENTER : Paragraph.ALIGN_LEFT);
    	//para.setSpacingBefore(fontSize * spacingAbove);
    	para.setSpacingAfter(fontSize * spacingBelowFactor);
    	para.setIndentationLeft(indentValue * indentLevel);
    	add(para);
    	add(Chunk.NEWLINE);
    }
    
    public void addTextLine(String text, Font font, boolean center, float spacingAbove, float spacingBelow) {
    	
    	addTextLine(0, text, font, center, spacingAbove, spacingBelow);
    }

    /** Start a new page if  */
    public void newPageIfHeightLessThan(float height) {
    	
    	// Class used to retrieve the current position
    	class VerticalPosition extends VerticalPositionMark {
        	
        	public float value = 0;
    		
    		public void draw(PdfContentByte canvas, float llx, float lly, float urx, float ury, float y) {
    			value = y;
    		}
    	}
    	
    	if (document == null)  return;
    	
    	VerticalPosition vpos = new VerticalPosition();
    	add(vpos);
    	float remainingHeight = vpos.value - document.bottom();
    	
    	if (remainingHeight < height)  document.newPage();
    }
    
    // Image
    
    public void addImageFullWidth(Image image, float bottom, float top) {

    	if (document == null)  return;
    	
    	float areaWidth = document.right() - document.left();
    	float areaHeight = top - bottom;
    	
    	// Scale to fit the area
    	image.scaleToFit(areaWidth, areaHeight);
    	
    	// But do not scale up too much
    	float factor = image.getScaledWidth() / image.getWidth(); 
    	float limit = 0.7f;
    	if (factor > limit)  image.scalePercent(limit * 100);
      	
    	float sw = image.getScaledWidth();
    	float sh = image.getScaledHeight();
    	
    	float x = document.left() + (areaWidth - sw) / 2;
        float y = bottom + (areaHeight - sh) / 2;
    	    	
    	image.setAbsolutePosition(x, y);
    	
    	add(image);
    }
    
    // Table
    
    public void beginTable(float[] columnWidth, int hAlignment, float hPadding) {

    	if (table != null)  return;
    	
    	// Simulate table padding with an invisible column
    	tablePadLeft = (hAlignment == Element.ALIGN_LEFT);
    	tablePadRight = (hAlignment == Element.ALIGN_RIGHT);
    	    	
    	if (tablePadLeft || tablePadRight) {
    		
    		int colCount = columnWidth.length;
    		float [] tmpArray = columnWidth.clone();
    		columnWidth = new float[colCount + 1];
    		
    		if (tablePadLeft) {
    			System.arraycopy(tmpArray, 0, columnWidth, 1, colCount);
    			columnWidth[0] = hPadding;
    		}
    		else {
    			System.arraycopy(tmpArray, 0, columnWidth, 0, colCount);
    			columnWidth[colCount] = hPadding;
    		}
    	}
    	
    	table = new PdfPTable(columnWidth.length);
        try {
        	
			table.setTotalWidth(columnWidth);
		} catch (DocumentException e) {
			table = null;
			return;
		}
        table.setLockedWidth(true);
        table.setHorizontalAlignment(hAlignment);
    }
    
    public void addTableHeader(String[] columnHeader, Font font, BaseColor backgroundColor) {
    	
    	if (table == null)  return;

    	if (tablePadLeft)  padTable();
        for (int i = 0; i < columnHeader.length; i++) {
            PdfPCell header = new PdfPHeaderCell();
            if (backgroundColor != null)  header.setBackgroundColor(backgroundColor);
            header.setBorder(Rectangle.NO_BORDER);
            header.setPhrase(new Phrase(columnHeader[i], font));
            table.addCell(header);
        }
        if (tablePadRight)  padTable();
    }
    
    private void padTable() {

    	if (table == null)  return;

    	PdfPCell header = new PdfPHeaderCell();
    	header.setBorder(Rectangle.NO_BORDER);
    	header.setPhrase(new Phrase(""));
    	table.addCell(header);
    }
    
    public void setRowColor(BaseColor evenColor) {
    	    	
    	this.evenColor = evenColor;
    	this.oddColor = null;
    }
    
    public void setRowColors(BaseColor evenColor, BaseColor oddColor) {
    	    	
    	this.evenColor = evenColor;
    	this.oddColor = oddColor;
    }

    public void addTableRow(String[] text, Font font, int index) {

    	if (table == null)  return;

    	if (tablePadLeft)  padTable();
    	for (int i = 0; i < text.length; i++) {

    		PdfPCell cell = new PdfPCell(new Phrase((String) text[i], font));
    		cell.setBorder(Rectangle.NO_BORDER);
    		cell.setBackgroundColor(oddColor == null || index % 2 == 0 ? evenColor : oddColor);
    		table.addCell(cell);
    	}
    	if (tablePadRight)  padTable();
    }

    public void endTable() {

    	if (table == null)  return;

        add(table);
        table = null;
    }
    
    public void newPage() {
    	
    	if (document == null)  return;
    	
    	document.newPage();
    }
    
    public void addVPad(float height) {
    	
    	Phrase phrase = new Phrase("\n");
		phrase.setLeading(height);
		add(phrase);
    }
    
    /** Horizontal line in the flow */ 
    public void addHLine(float widthPercent, float lineWidth, float spacingAfter, BaseColor color) {
    	
    	class HLine extends VerticalPositionMark {
    		
    		private float widthPercent;
    		private float lineWidth;
    		private BaseColor color;
    		
    		public HLine(float widthPercent, float lineWidth, BaseColor color) {
    			
    			this.widthPercent = widthPercent;
    			this.lineWidth = lineWidth;
    			this.color = color;
    		}
    		
    		public void draw(PdfContentByte canvas, float llx, float lly, float urx, float ury, float y) {
    			
    			canvas.saveState();
            	canvas.setRGBColorStroke(color.getRed(), color.getGreen(), color.getBlue());
            	canvas.setLineWidth(lineWidth);
            	if (widthPercent > 100)  widthPercent = 100;
            	float docWidth = (document.right() - document.left());
            	float shift = docWidth * (100 - widthPercent) * 0.005f;
            	float xmin = document.left() + shift;
            	float xmax = document.right() - shift; 
            	canvas.moveTo(xmin, y);
            	canvas.lineTo(xmax, y);
            	canvas.stroke();
    	        
    	        canvas.restoreState();
    		}
    	}
    	
    	if (document == null)  return;

    	add(new HLine(widthPercent, lineWidth, color));
    	addVPad(lineWidth + spacingAfter); // Required to move down the page
    }
    
    /** Horizontal line with absolute postion */ 
    public void drawHLine(float y, float widthPercent, float lineWidth, BaseColor color) {
    	
    	if (document == null)  return;
    	
    	canvas.saveState();
    	canvas.setRGBColorStroke(color.getRed(), color.getGreen(), color.getBlue());
    	canvas.setLineWidth(lineWidth);
    	if (widthPercent > 100)  widthPercent = 100;
    	float docWidth = (document.right() - document.left());
    	float shift = docWidth * (100 - widthPercent) * 0.005f;
    	float xmin = document.left() + shift;
    	float xmax = document.right() - shift; 
    	canvas.moveTo(xmin, y);
    	canvas.lineTo(xmax, y);
    	canvas.stroke();
    	canvas.restoreState();
    }
    
    // Bookmarks
    
    public PdfOutline bookmarkPage(String text) {
    	
    	return bookmarkPage(writer.getRootOutline(), text, Font.NORMAL);
    }
    
    public PdfOutline bookmarkPage(String text, int fontStyle) {
    	
    	return bookmarkPage(writer.getRootOutline(), text, fontStyle);
    }
    
    public PdfOutline bookmarkPage(PdfOutline parentNode, String text) {
    	
    	return bookmarkPage(parentNode, text, Font.NORMAL);
    }
    
    public PdfOutline bookmarkPage(PdfOutline parentNode, String text, int fontStyle) {
    	    	
    	PdfOutline bookmark = new PdfOutline(parentNode, new PdfDestination(PdfDestination.FIT), text, true);
    	bookmark.setStyle(fontStyle);
    	
    	lastCreatedBookmark = bookmark;
    	
    	return bookmark;
    }
    
    public PdfOutline subBookmarkPage(String text) {
    	
    	return subBookmarkPage(text, Font.NORMAL);
    }
    
    public PdfOutline subBookmarkPage(String text, int fontStyle) {
    	
    	PdfOutline parentNode = (lastCreatedBookmark != null ? lastCreatedBookmark : writer.getRootOutline());
    	
    	PdfOutline bookmark = new PdfOutline(parentNode, new PdfDestination(PdfDestination.FIT), text, true);
    	bookmark.setStyle(fontStyle);
    	
    	return bookmark;
    }
    
    // For debugging purpose

//    public void drawPageMargins() {
//
//    	class Margins extends VerticalPositionMark {
//
//    		public void draw(PdfContentByte canvas, float llx, float lly, float urx, float ury, float y) {
//
//    			canvas.saveState();
//    			canvas.setLineWidth(0.2f);
//
//    			canvas.setColorStroke(BaseColor.RED);
//    			canvas.rectangle(llx, lly, urx-llx, ury-lly);
//    			canvas.stroke();
//
//    			canvas.restoreState();
//    		}
//    	}
//
//    	add(new Margins());
//    }
//
//    public void drawTopline() {
//
//    	drawTopline(null);
//    }
//
//    public void drawTopline(BaseColor color) {
//
//    	class TopLine extends VerticalPositionMark {
//
//    		BaseColor color = BaseColor.LIGHT_GRAY;
//
//    		public TopLine(BaseColor color) {
//
//    			if (color != null)  this.color = color;
//    		}
//
//    		public void draw(PdfContentByte canvas, float llx, float lly, float urx, float ury, float y) {
//
//    			canvas.saveState();
//    			canvas.setLineWidth(0.2f);
//
//    			canvas.setColorStroke(color);
//    			canvas.moveTo(llx, y);
//    			canvas.lineTo(urx, y);
//    			canvas.stroke();
//
//    			canvas.restoreState();
//    		}
//    	}
//
//    	add(new TopLine(color));
//    }
}
