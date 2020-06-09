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

import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;

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

    private void centerViewer(SatViewer viewer) {
    	JMainFrame MF = JMainFrame.getJMainFrame();
    	viewer.setLocation(MF.getX() + (MF.getWidth() - viewer.getWidth())/2, MF.getY() + (MF.getHeight() - viewer.getHeight())/2);
    }
    
    public void showViewer(Switch sw) {
        if (!satViewer.containsKey(sw.getSystemSoftware())) {
            SatViewer viewer = new SatViewer(sw);
            centerViewer(viewer);
            sw.getSystemSoftware().addPropertyChangeListener(viewer);
            satViewer.put(sw.getSystemSoftware(), viewer);
        }
        satViewer.get(sw.getSystemSoftware()).setVisible(true);
    }
    
    public void removeViewer(Switch sw) {
    	SatViewer viewer = satViewer.get(sw.getSystemSoftware());
    	viewer.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    	viewer.dispatchEvent(new WindowEvent(viewer, WindowEvent.WINDOW_CLOSING));
        satViewer.remove(sw.getSystemSoftware());        
    }

    public void reshowViewer(Set<SystemSoftware> activeSystemSoftware) {
        Set<SystemSoftware> satToRemove = new HashSet<>();
        for (SystemSoftware sysSoftware : satViewer.keySet()) {
            if (!activeSystemSoftware.contains(sysSoftware)) {
                satToRemove.add(sysSoftware);
            } else if (visibleViewer.contains(sysSoftware)) {
                satViewer.get(sysSoftware).setVisible(true);
            }
        }
        for (SystemSoftware sysSoftware : satToRemove) {
            satViewer.remove(sysSoftware);
        }
    }
}
