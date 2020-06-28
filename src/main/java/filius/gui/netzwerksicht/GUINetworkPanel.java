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

import filius.hardware.knoten.InternetNode;
import filius.hardware.knoten.Node;
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
            Node tempKnoten = tempitem.getNode();
            JNodeLabel templabel = tempitem.getNodeLabel();

            tempKnoten.addObserver(templabel);
            tempKnoten.getSystemSoftware().addObserver(templabel);

            templabel.setSelektiert(false);
            // When the text is changed, the location is recomputed so that the icon does not move (which might happen when the text is wider than the icon)
            templabel.setTextAndUpdateLocation(tempKnoten.getDisplayName());
            templabel.setTyp(tempKnoten.getHardwareType());
            if (tempitem.getNode() instanceof InternetNode) {
                templabel.updateTooltip((InternetNode) tempitem.getNode());
            }
            if (tempitem.getNode() instanceof Switch) {
                if (((Switch) tempitem.getNode()).isCloud())
                    templabel.setIcon(new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.SWITCH_CLOUD)));
                else
                    templabel.setIcon(new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.SWITCH)));
            } else if (tempitem.getNode() instanceof Vermittlungsrechner) {
                templabel.setIcon(new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.VERMITTLUNGSRECHNER)));
            } else if (tempitem.getNode() instanceof Rechner) {
                templabel.setIcon(new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.RECHNER)));
            } else if (tempitem.getNode() instanceof Notebook) {
                templabel.setIcon(new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.NOTEBOOK)));
            } else if (tempitem.getNode() instanceof Modem) {
                templabel.setIcon(new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.MODEM)));
            }

            templabel.setBounds(tempitem.getNodeLabel().getBounds());
            add(templabel);
        }

        for (GUIKabelItem tempcable : kabel) {
            add(tempcable.getCablePanel());
        }
    }
}
