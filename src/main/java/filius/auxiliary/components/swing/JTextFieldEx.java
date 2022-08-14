package filius.auxiliary.components.swing;

import java.awt.KeyboardFocusManager;

import javax.swing.JTextField;

@SuppressWarnings("serial")
public class JTextFieldEx extends JTextField {
	
	public void setEditable(boolean b) {
		
		super.setEditable(b);

		// JTextField is buggy: the caret disappears when switching setEditable to false and back to true.
		// It's not precisely clear why, but the following does the trick!
		if (b && hasFocus()) {
			KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
			requestFocus();
		}
	}
}
