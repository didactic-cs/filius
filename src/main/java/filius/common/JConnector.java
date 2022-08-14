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

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.ImageIcon;

public class JConnector {
	
	public static final String CURSOR1 = "/gfx/hardware/cursor1.png";
	public static final String CURSOR1MAC = "/gfx/hardware/cursor1-mac.png";
	public static final String CURSOR2 = "/gfx/hardware/cursor2.png";
	public static final String CURSOR2MAC = "/gfx/hardware/cursor2-mac.png";
	
    public static final int CONNECTED_TOOL_OFF = 0;
    public static final int CONNECTED_TOOL_1 = 1;
    public static final int CONNECTED_TOOL_2 = 2;
	
    private int state = CONNECTED_TOOL_OFF;
	private Cursor cursor1;
    private Cursor cursor2;
	
	
	public JConnector() {

		Toolkit tk = Toolkit.getDefaultToolkit();		
		Point point = new Point(0,0);
		
		ImageIcon icon = new ImageIcon(getClass().getResource((isMacOS() ? CURSOR1MAC : CURSOR1)));
		cursor1 = tk.createCustomCursor(icon.getImage(), point, "cursor1");
		
		icon = new ImageIcon(getClass().getResource((isMacOS() ? CURSOR2MAC : CURSOR2)));
		cursor2 = tk.createCustomCursor(icon.getImage(), point, "cursor2");
	}   

    private static boolean isMacOS() {
  	   return System.getProperty("os.name").toLowerCase().contains("mac");
  	}   
}
