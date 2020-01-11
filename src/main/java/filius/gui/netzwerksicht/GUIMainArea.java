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
import java.io.Serializable;

import javax.swing.JPanel;

/**
 * Diese Klasse dient als Oberklasse für die verschiedenen Sichten im Haupt-Bereich der GUI.
 */
public class GUIMainArea extends JPanel implements Serializable {

    private static final long serialVersionUID = 1L;
    protected double minX = Integer.MAX_VALUE, maxX = 0, minY = Integer.MAX_VALUE, maxY = 0;

    private void resetClipBounds() {
        minX = Integer.MAX_VALUE;
        maxX = 0;
        minY = Integer.MAX_VALUE;
        maxY = 0;
    }

    private void updateClipBounds(Component elem) {
        if (elem.getBounds().getMinX() < minX) {
            minX = Math.max(0, elem.getBounds().getMinX());
        }
        if (elem.getBounds().getMaxX() > maxX) {
            maxX = Math.min(elem.getBounds().getMaxX(), this.getWidth());
        }
        if (elem.getBounds().getMinY() < minY) {
            minY = Math.max(0, elem.getBounds().getMinY());
        }
        if (elem.getBounds().getMaxY() > maxY) {
            maxY = Math.min(elem.getBounds().getMaxY(), this.getHeight());
        }
    }

    public void removeAll() {
        super.removeAll();
        resetClipBounds();
    }

    public Component add(Component comp) {
        super.add(comp);
        updateClipBounds(comp);
        return comp;
    }
}
