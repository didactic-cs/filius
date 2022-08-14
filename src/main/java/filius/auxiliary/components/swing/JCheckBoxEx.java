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
package filius.auxiliary.components;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;

@SuppressWarnings("serial")
public class JExtendedCheckBox extends JCheckBox {
	
	private final ImageIcon cbMultiple = new ImageIcon(getClass().getResource("/gfx/common/checkbox_multiple.png"));
	private final ImageIcon cbMultipleOver = new ImageIcon(getClass().getResource("/gfx/common/checkbox_multiple_over.png"));
	private ItemListener itemListener = null;
	private boolean multiple = false;
	
	
	private void addListner() {
		
		if (itemListener == null)  {
			itemListener = new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					setMultiple(false);
				}
			};
		}	  
		addItemListener(itemListener);
	}

	// Adds a third state: multiple
	// multiple is true to reflect that, in a selection, not all boolean values are identical
	// If isMultiple() is true, then isSelected() is meaningless.
	// As soon as the user checks or uncheks the box, all the values become identical (and multiple becomes false)
	public void setMultiple(boolean multiple) {
	
		if (multiple) {
			setIcon(cbMultiple);
			setRolloverIcon(cbMultipleOver);
			setRolloverSelectedIcon(cbMultipleOver);
			addListner();
		}
		else {
			setIcon(null);
			setRolloverIcon(null);
			setRolloverSelectedIcon(null);
			removeItemListener(itemListener);
		}
		this.multiple = multiple;
	}
	
	public boolean isMultiple() {
		
		return multiple;
	}
}
