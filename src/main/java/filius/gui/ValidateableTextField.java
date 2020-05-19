package filius.gui;

import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.border.Border;

import filius.rahmenprogramm.EingabenUeberpruefung;

@SuppressWarnings("serial")
public class ValidateableTextField extends JTextField {

    private static final Border DEFAULT_BORDER = new JTextField().getBorder();

    public ValidateableTextField(String text) {
        super(text);
    }

    public ValidateableTextField() {
        super();
    }

    public void setValid(boolean valid) {
        if (valid) {
            setForeground(EingabenUeberpruefung.farbeRichtig);
            setBorder(DEFAULT_BORDER);
        } else {
            setForeground(EingabenUeberpruefung.farbeFalsch);
            setForeground(EingabenUeberpruefung.farbeFalsch);
            setBorder(BorderFactory.createLineBorder(EingabenUeberpruefung.farbeFalsch, 1));
        }

    }
}
