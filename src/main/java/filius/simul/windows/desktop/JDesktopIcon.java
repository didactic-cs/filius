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
package filius.gui.anwendungssicht;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.Icon;
import javax.swing.JLabel;

@SuppressWarnings("serial")
public class GUIDesktopIcon extends JLabel implements MouseListener {

	private String appClassName;
	private GUIDesktopPanel desktopPanel;

	
	public GUIDesktopIcon(GUIDesktopPanel desktopPanel, String appClassName, Icon image) {
		super(image);		
		this.desktopPanel = desktopPanel;
		this.appClassName = appClassName;
		addMouseListener(this);
	}
	
	public void mousePressed(MouseEvent e) {

		if (e.getButton() == MouseEvent.BUTTON1  && e.getClickCount() == 2) {
			
			GUIApplicationWindow appWindow = desktopPanel.startApp(appClassName);
	        if (appWindow != null) appWindow.makeActiveWindow();
		}		
	}

	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
}
