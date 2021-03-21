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

import javax.swing.ImageIcon;
import filius.gui.netzwerksicht.GUISidebar;
import filius.gui.netzwerksicht.JNodeLabel;
import filius.rahmenprogramm.I18n;

public class GUIDocSidebar extends GUISidebar implements I18n {

    public static final String TYPE_TEXTFIELD = "textfield";
    public static final String TYPE_RECTANGLE = "rectangle";
    public static final String ADD_TEXT = "gfx/dokumentation/add_text_small.png";
    public static final String ADD_RECTANGLE = "gfx/dokumentation/add_small.png";

    private static GUIDocSidebar docSidebar;
    

    public static GUIDocSidebar getInstance() {
        if (docSidebar == null) {
            docSidebar = new GUIDocSidebar();
        }
        return docSidebar;
    }

    @Override
    protected void addItems() {
        ImageIcon icon = new ImageIcon(getClass().getResource("/" + ADD_TEXT));
        JNodeLabel newLabel = new JNodeLabel(TYPE_TEXTFIELD, messages.getString("docusidebar_msg1"), icon, false);
        addItem(newLabel);

        icon = new ImageIcon(getClass().getResource("/" + ADD_RECTANGLE));
        newLabel = new JNodeLabel(TYPE_RECTANGLE, messages.getString("docusidebar_msg3"), icon, false);
        addItem(newLabel);
    }
}
