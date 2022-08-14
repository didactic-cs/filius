package filius.gui.simulationmode.highlighters;

import javax.swing.text.Element;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

@SuppressWarnings("serial")
public class CssEditorKit extends StyledEditorKit {

	private ViewFactory cssViewFactory;
	private JHighlightedTextPane textPane;

	public CssEditorKit(JHighlightedTextPane textPane) {
		cssViewFactory = new CssViewFactory();
		this.textPane = textPane;
	}

	@Override
	public ViewFactory getViewFactory() {
		return cssViewFactory;
	}

	@Override
	public String getContentType() {
		return "text/css";
	}
	
	private class CssViewFactory extends Object implements ViewFactory {

		public View create(Element elem) {
			HighlightView view = new CssView(elem);
			textPane.setView(view);
			return view;
		}
	}
}
