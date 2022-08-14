package filius.gui.highlighters;

import javax.swing.text.Element;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

@SuppressWarnings("serial")
public class HighlighterEditorKit extends StyledEditorKit {

	private ViewFactory viewFactory;
	private JHighlightedTextPane textPane;
	private int contentType;

	public HighlighterEditorKit(JHighlightedTextPane textPane, int contentType) {
		
		this.textPane = textPane;
		this.contentType = contentType;
		
		switch (contentType) {		
		case JHighlightedTextPane.VIEW_HTML:  viewFactory = new HtmlViewFactory(); break;
		case JHighlightedTextPane.VIEW_CSS:   viewFactory = new CssViewFactory();  break;
		case JHighlightedTextPane.VIEW_JAVA:  viewFactory = new JavaViewFactory(); break;
		}
	}

	@Override
	public ViewFactory getViewFactory() {
		return viewFactory;
	}

	@Override
	public String getContentType() {
		
		switch (contentType) {		
		case JHighlightedTextPane.VIEW_HTML:  return "text/html";
		case JHighlightedTextPane.VIEW_CSS:   return "text/css";
		case JHighlightedTextPane.VIEW_JAVA:  return "text/java"; 
		}
		return "text/plain";
	}
	
	private class HtmlViewFactory extends Object implements ViewFactory {

		public View create(Element elem) {
			HighlighterView view = new HtmlView(elem);
			textPane.setView(view);
			return view;
		}
	}
	
	private class CssViewFactory extends Object implements ViewFactory {

		public View create(Element elem) {
			HighlighterView view = new CssView(elem);
			textPane.setView(view);
			return view;
		}
	}
	
	private class JavaViewFactory extends Object implements ViewFactory {

		public View create(Element elem) {
			HighlighterView view = new JavaView(elem);
			textPane.setView(view);
			return view;
		}
	}
}
