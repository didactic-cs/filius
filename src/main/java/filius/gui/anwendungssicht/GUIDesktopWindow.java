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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import filius.gui.JFrameList;
import filius.gui.netzwerksicht.GUIDesignSidebar;
import filius.hardware.knoten.Computer;
import filius.hardware.knoten.Host;
import filius.software.system.HostOS;

@SuppressWarnings("serial")
public class GUIDesktopWindow extends JFrame implements PropertyChangeListener {
		
	public enum Mode {
		ROW(0), COLUMN(1), STACK(2);
		private final int value;
		
		Mode(int mode) {
			this.value = mode;
		}
		
		public static Mode getMode(int value) {
			if (value == ROW.value) {
				return ROW;
			}
			else if (value == COLUMN.value) {
				return COLUMN;
			}
			else {
				return STACK;
			}
		}
	}
		
	Host host = null;
	HostOS hostOS = null;
	private GUIDesktopPanel desktopPanel;
	

	public GUIDesktopWindow(HostOS hostOS) {
		super();
		JFrameList.getInstance().add(this);
		
		this.hostOS = hostOS;
		host = (Host) hostOS.getNode();

		String iconFile;
		if (host instanceof Computer) iconFile = GUIDesignSidebar.RECHNER;
		else                          iconFile = GUIDesignSidebar.NOTEBOOK;
		ImageIcon icon = new ImageIcon(getClass().getResource("/" + iconFile));
		setIconImage(icon.getImage());

		setSize(640, 480);
		setResizable(false);

		desktopPanel = new GUIDesktopPanel(hostOS);
		getContentPane().add(desktopPanel);	
		
		registerListeners();
	}
	
	public HostOS getOS() {
		return desktopPanel.getOS();
	}

	public void setVisible(boolean flag) {
		super.setVisible(flag);

		updateTitle();

		if (flag) {
			toFront();
		}
	}

	private void updateTitle() {

		setTitle(host.getName() + " - " + hostOS.getIPAddress());	
	}
	
    private void registerListeners() {   
    	
    	host.getSystemSoftware().addPropertyChangeListener("ipaddress", this); 	
    }

	/**
     * <b>propertyChange</b> whenever a change in the host must be reflected by the user interface. 
     * 
     */
	public void propertyChange(PropertyChangeEvent evt) {
		
		String pn = evt.getPropertyName();
		
		if (pn == "ipaddress") {                       
			// Update the title of the desktop window
			// From: Node
			updateTitle();
		}
	};
}
