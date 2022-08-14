package filius.gui.components;

import java.util.EventListener;
import javax.swing.event.ChangeEvent;

/**
 * Defines an object which listens for ClosingEvents of JExtendedTabbedPane.
 *
 * @author Patrice Tréton
 */
public interface TabDoubleClickListener extends EventListener {
    /**
     * Invoked when the listened tab is double clicked.
     *
     * @param e  a ChangeEvent object
     */
    void action(ChangeEvent e);
}