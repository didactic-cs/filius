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

import javax.swing.ImageIcon;
import javax.swing.event.MouseInputAdapter;

import filius.gui.GUIContainer;
import filius.rahmenprogramm.I18n;

public class GUIDocumentationSidebar extends GUISidebar implements I18n {

    public static final String TYPE_TEXTFIELD = "textfield";
    public static final String TYPE_RECTANGLE = "rectangle";
    public static final String TYPE_EXPORT = "export";

    public static final String ADD_TEXT = "gfx/dokumentation/add_text_small.png";
    public static final String ADD_RECTANGLE = "gfx/dokumentation/add_small.png";
    public static final String EXPORT = "gfx/dokumentation/download_small.png";

    private static GUIDocumentationSidebar sidebar;

    public static GUIDocumentationSidebar getGUIDocumentationSidebar() {
        if (sidebar == null) {
            sidebar = new GUIDocumentationSidebar();
        }
        return sidebar;
    }

    @Override
    protected void addItemsToSidebar() {
        ImageIcon icon = new ImageIcon(getClass().getResource("/" + ADD_TEXT));
        JSidebarButton newLabel = new JSidebarButton(messages.getString("docusidebar_msg1"), icon, TYPE_TEXTFIELD);
        buttonList.add(newLabel);
        leistenpanel.add(newLabel);

        icon = new ImageIcon(getClass().getResource("/" + ADD_RECTANGLE));
        newLabel = new JSidebarButton(messages.getString("docusidebar_msg3"), icon, TYPE_RECTANGLE);
        buttonList.add(newLabel);
        leistenpanel.add(newLabel);

        icon = new ImageIcon(getClass().getResource("/" + EXPORT));
        newLabel = new JSidebarButton(messages.getString("docusidebar_msg5"), icon, TYPE_EXPORT);
        newLabel.setToolTipText(messages.getString("docusidebar_msg6"));
        newLabel.addMouseListener(new MouseInputAdapter() {
            public void mousePressed(MouseEvent e) {
                GUIContainer.getGUIContainer().exportAsImage();
            }
        });
        buttonList.add(newLabel);
        leistenpanel.add(newLabel);
    }
}
