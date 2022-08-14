package filius.gui.simulationmode.highlighters;

import javax.swing.text.Element;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

@SuppressWarnings("serial")
public class JavaEditorKit extends StyledEditorKit {

	private ViewFactory javaViewFactory;
	private JHighlightedTextPane textPane;

	public JavaEditorKit(JHighlightedTextPane textPane) {
		javaViewFactory = new JavaViewFactory();
		this.textPane = textPane;
	}

	@Override
	public ViewFactory getViewFactory() {
		return javaViewFactory;
	}

	@Override
	public String getContentType() {
		return "text/css";
	}
	
	private class JavaViewFactory extends Object implements ViewFactory {

		public View create(Element elem) {
			HighlightView view = new JavaView(elem);
			textPane.setView(view);
			return view;
		}
	}
}