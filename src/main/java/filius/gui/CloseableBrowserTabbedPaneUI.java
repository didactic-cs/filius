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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.ListIterator;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

public class CloseableBrowserTabbedPaneUI extends BasicTabbedPaneUI {

	private final int buttonWidth = 24;
	private LinkedList<Rectangle> buttonPositions = new LinkedList<Rectangle>();
	

	public CloseableBrowserTabbedPaneUI() {
		super();
	}

	protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {

		return super.calculateTabWidth(tabPlacement, tabIndex, metrics) + buttonWidth;
	}

	protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
		return super.calculateTabHeight(tabPlacement, tabIndex, fontHeight) + 2;
	}

	protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect) {
		
		super.paintTab(g, tabPlacement, rects, tabIndex, iconRect, textRect);

		buttonPositions = new LinkedList<Rectangle>();

		int posX = rects[tabIndex].x + rects[tabIndex].width - buttonWidth + 5;
		int posY = rects[tabIndex].y + 5;
		int width = 12;
		int hight = 12;
		buttonPositions.add(new Rectangle(posX, posY, width, hight));

		Graphics2D g2 = (Graphics2D) g;

		g2.setColor(new Color(255, 0, 0));
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
		g2.fillRoundRect(posX, posY, width, hight, 3, 3);
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
		g2.setColor(new Color(255, 255, 255));

		g2.setStroke(new BasicStroke(2));
		g2.drawLine(posX + 3, posY + 3, posX + width - 3, posY + hight - 3);
		g2.drawLine(posX + width - 3, posY + 3, posX + 3, posY + hight - 3);
	}

	public LinkedList<Rectangle> getButtonPositions() {
		return buttonPositions;
	}
	
    public int getClickedCrossIndex(int X, int Y) {
		
		int index = 0;
		ListIterator<Rectangle> it = buttonPositions.listIterator();
		while (it.hasNext()) {
			Rectangle rect = (Rectangle) it.next();
			if (rect.intersects(new Rectangle(X, Y, 1, 1))) {
				return index;
			}
			index++;
		}
	    return -1;
	}
    
    public void removeTab(int index) {
		
		if (index < 0 || index >= buttonPositions.size()) return;
		
		buttonPositions.remove(index);
		tabPane.remove(index);
	}	
}
