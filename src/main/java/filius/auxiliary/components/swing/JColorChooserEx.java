/*
 ** This file is part of Filius, a network construction and simulation software.
 ** 
 ** Originally created at the University of Siegen, Institute "Didactics of
 ** Informatics and E-Learning" by a students' project group:
 **     members (2006-2007): 
 **         André Asschoff, Johannes Bade, Carsten Dittich, Thomas Gerding,
 **         Nadja Haßler, Ernst Johannes Klebert, Michell Weyer
 **     supervisors:
 **         Stefan Freischlad (maintainer until 2009), Peer Stechert
 ** Project is maintained since 2010 by Christian Eibl <filius@c.fameibl.de>
 **         and Stefan Freischlad
 ** Filius is free software: you can redistribute it and/or modify
 ** it under the terms of the GNU General Public License as published by
 ** the Free Software Foundation, either version 2 of the License, or
 ** (at your option) version 3.
 ** 
 ** Filius is distributed in the hope that it will be useful,
 ** but WITHOUT ANY WARRANTY; without even the implied
 ** warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 ** PURPOSE. See the GNU General Public License for more details.
 ** 
 ** You should have received a copy of the GNU General Public License
 ** along with Filius.  If not, see <http://www.gnu.org/licenses/>.
 */
package filius.auxiliary.components.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.colorchooser.AbstractColorChooserPanel;

/**
 * A JColorChooser without HSV nor CMYK panels
 */
@SuppressWarnings("serial")
public class JColorChooserEx extends JColorChooser {
	
	
	public static Color showDialog(Component component,
            String title, Color initialColor) throws HeadlessException {
        return showDialog(component, title, initialColor, true);
    }
	
	@SuppressWarnings("deprecation")
    public static Color showDialog(Component component, String title, Color initialColor, boolean colorTransparencySelectionEnabled)
            throws HeadlessException {

        final JColorChooser pane = new JColorChooser(initialColor != null?
                                               initialColor : Color.white);

        for (AbstractColorChooserPanel ccPanel : pane.getChooserPanels()) {
            ccPanel.setColorTransparencySelectionEnabled(
                    colorTransparencySelectionEnabled);
        }
        
        AbstractColorChooserPanel[] panels = pane.getChooserPanels();
        pane.removeChooserPanel(panels[1]);
		pane.removeChooserPanel(panels[4]);

        ColorTracker ok = new ColorTracker(pane);
        JDialog dialog = createDialog(component, title, true, pane, ok, null);

        dialog.show(); // blocks until user brings dialog down...

        return ok.getColor();
    }
}



@SuppressWarnings("serial") // JDK-implementation class
class ColorTracker implements ActionListener, Serializable {
    JColorChooser chooser;
    Color color;

    public ColorTracker(JColorChooser c) {
        chooser = c;
    }

    public void actionPerformed(ActionEvent e) {
        color = chooser.getColor();
    }

    public Color getColor() {
        return color;
    }
}
