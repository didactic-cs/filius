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

import java.awt.Component;
import java.awt.Dimension;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BoxLayout;

import filius.gui.JBackgroundPanel;

public abstract class GUISidebar {
	protected JBackgroundPanel leistenpanel;
	protected List<JSidebarButton> buttonList;

	private static int width = 0;

	protected GUISidebar() {
		buttonList = new LinkedList<JSidebarButton>();

		leistenpanel = new JBackgroundPanel() {
			private static final long serialVersionUID = 1L;

			public Dimension getPreferredSize() {
				int height = 0;
				for (Component component : this.getComponents()) {
					if (component.getWidth() > width)
						width = component.getWidth();
					height += component.getHeight();
				}
				height += 30;
				return new Dimension(width, height);
			}
		};
		leistenpanel.setBackgroundImage("gfx/allgemein/leisten_hg.png");
		BoxLayout layout = new BoxLayout( leistenpanel, BoxLayout.Y_AXIS );
        leistenpanel.setLayout( layout);
		leistenpanel.setEnabled(false);

		addItemsToSidebar();
	}

	protected abstract void addItemsToSidebar();

	public JSidebarButton findButtonAt(int x, int y) {
		JSidebarButton klickLabel = null;
		for (JSidebarButton tmpLbl : buttonList) {
			if (x >= tmpLbl.getX() && y >= tmpLbl.getY() && x <= tmpLbl.getX() + tmpLbl.getWidth()
			        && y <= tmpLbl.getY() + tmpLbl.getHeight()) {
				klickLabel = tmpLbl;
			}

		}
		return klickLabel;
	}

	public JBackgroundPanel getLeistenpanel() {
		return leistenpanel;
	}
}
