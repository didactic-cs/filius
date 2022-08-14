package filius.gui;

import java.util.EventListener;
import javax.swing.event.ChangeEvent;

/**
 * Defines an object which listens for ClosingEvents of JExtendedTabbedPane.
 *
 * @author Patrice Tréton
 */
public interface TabClosingListener extends EventListener {
    /**
     * Invoked when the listened tab is about to close.
     * Return false to prevent the tab from closing. 
     *
     * @param e  a ChangeEvent object
     */
    boolean canClose(ChangeEvent e);
}