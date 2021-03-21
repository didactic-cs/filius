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
package filius.gui.netzwerksicht;

import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.event.MouseInputAdapter;

import filius.gui.GUIContainer;
import filius.gui.GUIEvents;
import filius.hardware.Cable;
import filius.hardware.knoten.Computer;
import filius.hardware.knoten.Modem;
import filius.hardware.knoten.Notebook;
import filius.hardware.knoten.Router;
import filius.hardware.knoten.Switch;
import filius.rahmenprogramm.I18n;

/**
 * Klasse für das linke Panel in der Entwurfsansicht. Darin werden alle
 * nutzbaren Elemente für den Netzwerkentwurf angezeigt und können per Drag&Drop
 * in den Entwurfsbildschirm gezogen werden.
 * 
 * @author Johannes Bade & Thomas Gerding
 */
public class GUIDesignSidebar extends GUISidebar implements I18n {

	public static final String KABEL = "gfx/hardware/kabel.png";
	public static final String RECHNER = "gfx/hardware/server.png";
	public static final String SWITCH = "gfx/hardware/switch.png";
	public static final String SWITCH_CLOUD = "gfx/hardware/cloud.png";
	public static final String ROUTER = "gfx/hardware/router.png";
	public static final String NOTEBOOK = "gfx/hardware/laptop.png";
	public static final String MODEM = "gfx/hardware/vermittlungsrechner-out.png";
	
	private static final String[] ICON_FILES = {KABEL, RECHNER, NOTEBOOK, SWITCH, ROUTER, MODEM};
	private static final String[] TYPES = {Cable.TYPE, Computer.TYPE, Notebook.TYPE, Switch.TYPE, Router.TYPE, Modem.TYPE};

	private static GUIDesignSidebar designSidebar;
	

	public static GUIDesignSidebar getGUIDesignSidebar() {
		if (designSidebar == null) {
			designSidebar = new GUIDesignSidebar();
		}
		return designSidebar;
	}
	
	public static String iconFilesByHardware(String hardwareType) {
		
		for (int i = 0; i < ICON_FILES.length; i++) {
			if (hardwareType == TYPES[i]) return ICON_FILES[i];
		}
		return null;
	}
	
	@Override
	protected void addItems() {
		
		// Add the cable tool
		JLabel cableTool = new JLabel(new ImageIcon(getClass().getResource("/" + KABEL)));
		cableTool.setText(Cable.TYPE);
		cableTool.setVerticalTextPosition(SwingConstants.BOTTOM);
		cableTool.setHorizontalTextPosition(SwingConstants.CENTER);
		cableTool.setAlignmentX(0.5f);
		cableTool.setToolTipText(messages.getString("guidesignsidebar_msg1"));		           

		cableTool.addMouseListener(new MouseInputAdapter() {
			
			public void mousePressed(MouseEvent e) {
				GUIEvents.getInstance().resetAndShowCablePreview(e.getX() - GUIContainer.getInstance().getSidebarScrollpane().getWidth(), e.getY());
			}
		});
		
		addItem(cableTool);		
		
		// Add the node buttons
		for (int i = 1; i < ICON_FILES.length; i++) {
			ImageIcon icon = new ImageIcon(getClass().getResource("/" + ICON_FILES[i]));
			JNodeLabel nodeLabel = new JNodeLabel(TYPES[i], TYPES[i], icon, false);

			addItem(nodeLabel);
		}
	}
}
