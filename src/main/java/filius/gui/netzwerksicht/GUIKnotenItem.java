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

import javax.swing.event.MouseInputAdapter;

import filius.gui.GUIContainer;
import filius.gui.GUIEvents;
import filius.hardware.knoten.Knoten;
import filius.hardware.knoten.Notebook;
import filius.hardware.knoten.Rechner;
import filius.hardware.knoten.Switch;
import filius.rahmenprogramm.SzenarioVerwaltung;

public class GUIKnotenItem {

    private Knoten knoten;
    private JSidebarButton imageLabel;

    public JSidebarButton getImageLabel() {
        return imageLabel;
    }

    public void setImageLabel(JSidebarButton label) {
        this.imageLabel = label;
        label.addMouseListener(new MouseInputAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                SzenarioVerwaltung.getInstance().setzeGeaendert();

                if (e.getButton() == 3) {
                    GUIEvents.getGUIEvents().kontextMenueAktionsmodus(GUIKnotenItem.this, e.getX(), e.getY());
                } else if (e.getButton() == 1) {
                    if (GUIKnotenItem.this.getKnoten() instanceof Rechner
                            || GUIKnotenItem.this.getKnoten() instanceof Notebook) {
                        GUIContainer.getGUIContainer().showDesktop(GUIKnotenItem.this);
                    } else if (GUIKnotenItem.this.getKnoten() instanceof Switch) {
                        GUIEvents.getGUIEvents().satTabelleAnzeigen(GUIKnotenItem.this);
                    }
                }
            }
        });
    }

    public Knoten getKnoten() {
        return knoten;
    }

    public void setKnoten(Knoten knoten) {
        this.knoten = knoten;
    }
}
