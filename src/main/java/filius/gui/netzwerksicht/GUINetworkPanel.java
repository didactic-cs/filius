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

import java.awt.Dimension;
import java.util.List;

import javax.swing.ImageIcon;

import filius.hardware.knoten.InternetKnoten;
import filius.hardware.knoten.Knoten;
import filius.hardware.knoten.Modem;
import filius.hardware.knoten.Notebook;
import filius.hardware.knoten.Rechner;
import filius.hardware.knoten.Switch;
import filius.hardware.knoten.Vermittlungsrechner;

public class GUINetworkPanel extends GUIMainArea {

    private static final long serialVersionUID = 1L;

    public GUINetworkPanel(int width, int height) {
        setLayout(null);
        setPreferredSize(new Dimension(width, height));
        setOpaque(false);
        setBounds(0, 0, width, height);
    }

    public void updateViewport(List<GUIKnotenItem> knoten, List<GUIKabelItem> kabel) {
        removeAll();

        for (GUIKnotenItem tempitem : knoten) {
            Knoten tempKnoten = tempitem.getKnoten();
            JSidebarButton templabel = tempitem.getImageLabel();

            tempKnoten.addObserver(templabel);
            tempKnoten.getSystemSoftware().addObserver(templabel);

            templabel.setSelektiert(false);
            templabel.setText(tempKnoten.holeAnzeigeName());
            templabel.setTyp(tempKnoten.holeHardwareTyp());
            if (tempitem.getKnoten() instanceof InternetKnoten) {
                templabel.updateTooltip((InternetKnoten) tempitem.getKnoten());
            }
            if (tempitem.getKnoten() instanceof Switch) {
                if (((Switch) tempitem.getKnoten()).isCloud())
                    templabel.setIcon(new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.SWITCH_CLOUD)));
                else
                    templabel.setIcon(new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.SWITCH)));
            } else if (tempitem.getKnoten() instanceof Vermittlungsrechner) {
                templabel.setIcon(new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.VERMITTLUNGSRECHNER)));
            } else if (tempitem.getKnoten() instanceof Rechner) {
                templabel.setIcon(new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.RECHNER)));
            } else if (tempitem.getKnoten() instanceof Notebook) {
                templabel.setIcon(new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.NOTEBOOK)));
            } else if (tempitem.getKnoten() instanceof Modem) {
                templabel.setIcon(new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.MODEM)));
            }

            templabel.setBounds(tempitem.getImageLabel().getBounds());
            add(templabel);
        }

        for (GUIKabelItem tempcable : kabel) {
            add(tempcable.getKabelpanel());
        }
    }
}
