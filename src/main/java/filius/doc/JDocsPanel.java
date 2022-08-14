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
package filius.gui.dokusicht;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.event.MouseInputAdapter;

import filius.gui.netzwerksicht.GUIMainArea;

@SuppressWarnings("serial")
public class GUIDocPanel extends GUIMainArea {

    public GUIDocPanel(int width, int height) {
        setLayout(null);
        setPreferredSize(new Dimension(width, height));
        setBounds(0, 0, width, height);
        setOpaque(false);
        
        // A click on the background removes the focus from any JDocElement
        addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
            	removeElementsFocus();
            }
        });
    }
    
    public void removeElementsFocus() {
    	getRootPane().requestFocusInWindow();
    	Component[] c = getComponents();
    	for (int i = 0; i < c.length; i++) {       
    		if ( c[i] instanceof JDocElement &&  c[i] != this) {
    			((JDocElement) c[i]).setLocalFocus(false);
    		}
    	}
    }

    public void updateViewport(List<GUIDocItem> docItems, boolean enableElements) {
        removeAll();

        for (GUIDocItem item : docItems) {
            add(item.asDocElement());
            item.asDocElement().setEnabled(enableElements);
        }
    }
}
