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

@SuppressWarnings("serial")
public class GUINetworkPanel extends GUIMainArea {

    public GUINetworkPanel(int width, int height) {
        setLayout(null);
        setPreferredSize(new Dimension(width, height));
        setOpaque(false);
        setBounds(0, 0, width, height);
    }

    public void updateViewport(List<GUINodeItem> guiKnoten, List<GUICableItem> guiKabel) {
    	
        removeAll();

        for (GUINodeItem item : guiKnoten) {
            Node node = item.getNode();
            JNodeLabel label = item.getNodeLabel();

            if (node instanceof Modem) ((Modem)node).addListener(label);
            node.getSystemSoftware().addObserver(label);                           // << to replace

            label.setSelected(false);
            // When the text is changed, the location is recomputed so that the icon does not move 
            // (which otherwise happens when the text is wider than the icon)
            label.setTextAndUpdateLocation(node.getDisplayName());
            label.setType(node.getHardwareType());
            if (item.getNode() instanceof InternetNode) {
                label.updateTooltip((InternetNode) item.getNode());
            }
            if (item.getNode() instanceof Switch) {
                if (((Switch) item.getNode()).isCloud())
                    label.setIcon(new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.SWITCH_CLOUD)));
                else
                    label.setIcon(new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.SWITCH)));
            } else if (item.getNode() instanceof Vermittlungsrechner) {
                label.setIcon(new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.VERMITTLUNGSRECHNER)));
            } else if (item.getNode() instanceof Rechner) {
                label.setIcon(new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.RECHNER)));
            } else if (item.getNode() instanceof Notebook) {
                label.setIcon(new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.NOTEBOOK)));
            } else if (item.getNode() instanceof Modem) {
                label.setIcon(new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.MODEM)));
            }

            label.setBounds(item.getNodeLabel().getBounds());
            add(label);
        }

        for (GUICableItem item : guiKabel) {
            add(item.getCablePanel());
        }
    }
}
