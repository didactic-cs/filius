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

import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

import filius.gui.anwendungssicht.SatViewer;
import filius.hardware.knoten.Switch;
import filius.rahmenprogramm.I18n;

/** Controller of viewer components for Source Address Tables used by Switches. */
public class SatViewerControl implements I18n {

    private Map<Switch, SatViewer> satViewer = new HashMap<>();
    private static SatViewerControl singleton;

    private SatViewerControl() {}

    public static SatViewerControl getInstance() {
        if (null == singleton) {
            singleton = new SatViewerControl();
        }
        return singleton;
    }

    public void hideViewer() {
        for (JFrame viewer : satViewer.values()) {
            viewer.setVisible(false);
        }
    }

    public void showViewer(Switch sw) {
        if (!satViewer.containsKey(sw)) {
            SatViewer viewer = new SatViewer(sw);
            sw.getSystemSoftware().addPropertyChangeListener(viewer);
            satViewer.put(sw, viewer);
        }
        satViewer.get(sw).setVisible(true);
    }
}
