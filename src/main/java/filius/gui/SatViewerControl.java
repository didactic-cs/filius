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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import filius.gui.anwendungssicht.SatViewer;
import filius.hardware.knoten.Switch;
import filius.rahmenprogramm.I18n;
import filius.software.system.SystemSoftware;

/** Controller of viewer components for Source Address Tables used by Switches. */
public class SatViewerControl implements I18n {

    private Map<SystemSoftware, SatViewer> satViewer = new HashMap<>();
    private Set<SystemSoftware> visibleViewer = new HashSet<>();
    private static SatViewerControl singleton;

    private SatViewerControl() {}

    public static SatViewerControl getInstance() {
        if (null == singleton) {
            singleton = new SatViewerControl();
        }
        return singleton;
    }

    public void hideViewer() {
        visibleViewer.clear();
        for (SatViewer viewer : satViewer.values()) {
            if (viewer.isVisible()) {
                visibleViewer.add(viewer.getSwitch().getSystemSoftware());
            }
            viewer.setVisible(false);
        }
    }

    public void showViewer(Switch sw) {
        if (!satViewer.containsKey(sw.getSystemSoftware())) {
            SatViewer viewer = new SatViewer(sw);
            viewer.setLocation(
                    JMainFrame.getJMainFrame().getX() + (JMainFrame.getJMainFrame().getWidth() - viewer.getWidth()) / 2,
                    JMainFrame.getJMainFrame().getY()
                            + (JMainFrame.getJMainFrame().getHeight() - viewer.getHeight()) / 2);
            sw.getSystemSoftware().addPropertyChangeListener(viewer);
            satViewer.put(sw.getSystemSoftware(), viewer);
        }
        satViewer.get(sw.getSystemSoftware()).setVisible(true);
    }

    public void removeViewer(SystemSoftware systemSoftware) {
        satViewer.remove(systemSoftware);
    }

    public void reshowViewer(Set<SystemSoftware> activeSystemSoftware) {
        Set<SystemSoftware> satToRemove = new HashSet<>();
        for (SystemSoftware sysSoftawre : satViewer.keySet()) {
            if (!activeSystemSoftware.contains(sysSoftawre)) {
                satToRemove.add(sysSoftawre);
            } else if (visibleViewer.contains(sysSoftawre)) {
                satViewer.get(sysSoftawre).setVisible(true);
            }
        }
        for (SystemSoftware sysSoftware : satToRemove) {
            satViewer.remove(sysSoftware);
        }
    }
}
