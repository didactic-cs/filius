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
package filius.gui;

import java.awt.Frame;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;

/**
 *  <b>JBooleanDialog</b> A JDialog with a boolean, a integer and a string value (to return value if needed.
 *  A close() method is also available to simulate a click on the closing cross.  
 *
 */
@SuppressWarnings("serial")
public class JExtendedDialog extends JDialog {
	
	private boolean bValue  = false;
	private int     iValue  = 0;
	private String  sValue  = null;	
	
	
	public JExtendedDialog() {    	
        super();
    }
	
	public JExtendedDialog(Frame owner, boolean modal) {    	
        super(owner, modal);
    }

	public void setBooleanValue(boolean value) {
		bValue = value;
	} 
	
	public boolean getBooleanValue() {
		return bValue;
	}
	
	public void setIntValue(int value) {
		iValue = value;
	} 
	
	public int getIntValue() {
		return iValue;
	}
	
	public void setStringValue(String value) {
		sValue = value;
	} 
	
	public String getStringValue() {
		return sValue;
	}	
	
	/**
	 * <b>close</b> closes the dialog box by simulating a click on the closing cross
	 */
    public void close() {

    	dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }
}
