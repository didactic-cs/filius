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
import filius.hardware.Kabel;
import filius.hardware.knoten.Modem;
import filius.hardware.knoten.Notebook;
import filius.hardware.knoten.Rechner;
import filius.hardware.knoten.Switch;
import filius.hardware.knoten.Vermittlungsrechner;

/**
 * Klasse für das linke Panel in der Entwurfsansicht. Darin werden alle
 * nutzbaren Elemente für den Netzwerkentwurf angezeigt und können per Drag&Drop
 * in den Entwurfsbildschirm gezogen werden.
 * 
 * @author Johannes Bade & Thomas Gerding
 */
public class GUIDesignSidebar extends GUISidebar {

	public static final String KABEL = "gfx/hardware/kabel.png";
	public static final String RECHNER = "gfx/hardware/server.png";
	public static final String SWITCH = "gfx/hardware/switch.png";
	public static final String SWITCH_CLOUD = "gfx/hardware/cloud.png";
	public static final String VERMITTLUNGSRECHNER = "gfx/hardware/router.png";
	public static final String NOTEBOOK = "gfx/hardware/laptop.png";
	public static final String MODEM = "gfx/hardware/vermittlungsrechner-out.png";

	private JLabel newCableCursor;

	private static GUIDesignSidebar sidebar;

	public static GUIDesignSidebar getGUIDesignSidebar() {
		if (sidebar == null) {
			sidebar = new GUIDesignSidebar();
		}
		return sidebar;
	}

	private void addCableItemToSidebar() {
		newCableCursor = new JLabel(new ImageIcon(getClass().getResource("/" + KABEL)));
		newCableCursor.setText(Kabel.TYPE);
		newCableCursor.setVerticalTextPosition(SwingConstants.BOTTOM);
		newCableCursor.setHorizontalTextPosition(SwingConstants.CENTER);
		newCableCursor.setAlignmentX(0.5f);

		newCableCursor.setVerticalTextPosition(SwingConstants.BOTTOM);
		newCableCursor.setHorizontalTextPosition(SwingConstants.CENTER);

		newCableCursor.setToolTipText("<Alt>+1");

		leistenpanel.add(newCableCursor);

		newCableCursor.addMouseListener(new MouseInputAdapter() {
			public void mousePressed(MouseEvent e) {
				GUIEvents.getGUIEvents().resetAndShowCablePreview(
				        e.getX() - GUIContainer.getGUIContainer().getSidebarScrollpane().getWidth(), e.getY());
			}
		});
	}

	@Override
	protected void addItemsToSidebar() {
		addCableItemToSidebar();
		addComponentItemsToSidebar();
	}

	private void addComponentItemsToSidebar() {
		String[] bildDateien;
		String[] hardwareTypen;
		JSidebarButton newLabel;
		ImageIcon icon;

		bildDateien = new String[5];
		hardwareTypen = new String[5];
		bildDateien[0] = RECHNER;
		hardwareTypen[0] = Rechner.TYPE;
		bildDateien[1] = NOTEBOOK;
		hardwareTypen[1] = Notebook.TYPE;
		bildDateien[2] = SWITCH;
		hardwareTypen[2] = Switch.TYPE;
		bildDateien[3] = VERMITTLUNGSRECHNER;
		hardwareTypen[3] = Vermittlungsrechner.TYPE;
		bildDateien[4] = MODEM;
		hardwareTypen[4] = Modem.TYPE;

		for (int i = 0; i < bildDateien.length && i < hardwareTypen.length; i++) {
			icon = new ImageIcon(getClass().getResource("/" + bildDateien[i]));
			newLabel = new JSidebarButton(hardwareTypen[i], icon, hardwareTypen[i]);

			/* Label wird liste und Leiste hinzugefuegt */
			buttonList.add(newLabel);
			leistenpanel.add(newLabel);
		}
	}
}
