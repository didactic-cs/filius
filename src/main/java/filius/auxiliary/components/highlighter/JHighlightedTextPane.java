package filius.gui.simulationmode.highlighters;

import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

@SuppressWarnings("serial")
public class JHighlightedTextPane extends JTextPane {
	
	public static final int VIEW_HTML = 0;
	public static final int VIEW_CSS  = 1;
	public static final int VIEW_JAVA = 2;
	
	private HighlightView view = null;
	
	public JHighlightedTextPane() {
    	
    	// Select a monospaced font more suitable for code editing
    	selectFont();
    	
    	// Add autoindent
        addKeyListener(new IndentKeyListener());
    }

	public void setView(HighlightView view) {
		
		this.view = view;
	}
	
	public void initDocListener() {
		
		// Repaint all on text change
        // This is required for the highlighting to work correctly. 
    	// Modifying a character can have repercussions on the way 
    	// surrounding lines should be displayed (inserting <!-- or --> for instance)
		getDocument().addDocumentListener(new DocumentListener() {

			public void insertUpdate(DocumentEvent e) { 
				view.parseText(); 
				repaint();
			}
			public void removeUpdate(DocumentEvent e) { 
				view.parseText(); 
				repaint();
			}

			public void changedUpdate(DocumentEvent e) {}
		});
	}
	
	public void setType(int contentType) {
		
		switch (contentType) {
		
		case VIEW_HTML:
			setEditorKitForContentType("text/html", new HtmlEditorKit(this));
			setContentType("text/html");
			initDocListener();
			break;
			
		case VIEW_CSS:
			setEditorKitForContentType("text/css", new CssEditorKit(this));
			setContentType("text/css");
			initDocListener();
			break;
			
		case VIEW_JAVA:
			setEditorKitForContentType("text/java", new JavaEditorKit(this));
			setContentType("text/java");
			initDocListener();
			break;
			
		default:
			setEditorKitForContentType("text/plain", null);
			setContentType("text/plain");
			initDocListener();
		}		
	}
     
	private void selectFont() {
    	 
    	 if (System.getProperty("os.name").toLowerCase().contains("windows")) {
    		 setFont(new Font("consolas", Font.PLAIN, 13)); 
    	 } else 
         if (System.getProperty("os.name").toLowerCase().contains("mac")) {
        	 setFont(new Font("monaco", Font.PLAIN, 13));
    	 } else {
    		 setFont(new Font("vera mono", Font.PLAIN, 13)); // Linux
    	 } // setFont(new Font("monospaced", Font.PLAIN, 12));
     }
     
     private class IndentKeyListener implements KeyListener {

 		public void keyPressed(KeyEvent event) {

 			if ((event.getKeyCode() == KeyEvent.VK_ENTER) && (event.getModifiersEx() == 0)) {

 				// We manage the enter key 
 				if (getSelectionStart() == getSelectionEnd()) {	event.consume(); }
 			}
 		}

 		public void keyReleased(KeyEvent event) {
 			
 			if ((event.getKeyCode() == KeyEvent.VK_ENTER) && (event.getModifiersEx() == 0)) {

 				event.consume();

 				int start, end;
 				String text = getText();

 				int caretPos = getCaretPosition();
 				try {
 					if (text.charAt(caretPos) == '\n') { caretPos--; }
 				} catch (IndexOutOfBoundsException e) {
 				}

 				start = text.lastIndexOf('\n', caretPos) + 1;
 				end = start;
 				try {
 					if (text.charAt(start) != '\n') {
 						while ((end < text.length()) && (Character.isWhitespace(text.charAt(end))) && (text.charAt(end) != '\n')) {
 							end++;
 						}
 						if (end > start) {
 							getDocument().insertString(caretPos, "\n" + text.substring(start, end), null);
 						} else {
 							getDocument().insertString(caretPos, "\n", null);
 						}
 					} else {
 						getDocument().insertString(caretPos, "\n", null);
 					}
 				} catch (IndexOutOfBoundsException e) {
 					try {
 						getDocument().insertString(caretPos, "\n", null);
 					} catch (BadLocationException e1) {
 					}
 				} catch (BadLocationException e) {
 				}
 			}
 		}

 		public void keyTyped(KeyEvent e) {}
 	}
}
