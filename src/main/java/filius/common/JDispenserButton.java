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
package filius.common;

import javax.swing.ImageIcon;

import filius.auxiliary.I18n;
import filius.auxiliary.components.JExtendedLabel;
import filius.design.hardware.Hardware;

@SuppressWarnings("serial")
public class JDispenserButton extends JExtendedLabel implements I18n {
	
	private int type;
	
	
	public JDispenserButton(int type) {		
		super(null);
		updateType(type);
	}
	
    private void updateType(int type) {
    	
    	this.type = type;
        setIcon_(new ImageIcon(getClass().getResource("/"+Hardware.ICONS[type])));
    	setText(Hardware.NAMES[type]);
    	if (type == Hardware.CABLE)  setToolTipText(messages.getString("hardware_msg0"));  // Cable (C) / Kabel (K) 
    }
    
    public int getType() {
    	
    	return this.type;
    }
}
