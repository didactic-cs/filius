package filius.gui.simulationmode.highlighters;

import javax.swing.text.Element;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

@SuppressWarnings("serial")
public class HtmlEditorKit extends StyledEditorKit {

	private ViewFactory htmlViewFactory;
	private JHighlightedTextPane textPane;

	public HtmlEditorKit(JHighlightedTextPane textPane) {
		htmlViewFactory = new HtmlViewFactory();
		this.textPane = textPane;
	}

	@Override
	public ViewFactory getViewFactory() {
		return htmlViewFactory;
	}

	@Override
	public String getContentType() {
		return "text/html";
	}
	
	private class HtmlViewFactory extends Object implements ViewFactory {

		public View create(Element elem) {
			HighlightView view = new HtmlView(elem);
			textPane.setView(view);
			return view;
		}
	}
}
