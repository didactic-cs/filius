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
package filius.common;

import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputAdapter;

import filius.design.hardware.Hardware;
import filius.design.nodes.JNode;
import filius.doc.DocItem;
import filius.doc.JDocItem;
import filius.project.ProjectManager;

@SuppressWarnings("serial")
public class JDocsSidebar extends JSidebar {
	
	private GUIContainer container;
	
	
	public JDocsSidebar(GUIContainer container) {
		
		super();		
		this.container = container;
		initListeners();
	}
	
	public void initListeners() {       
//
//        addMouseListener(new MouseInputAdapter() {
//        	
//            public void mousePressed(MouseEvent e) {
//            	
//                JNode button = findButtonAt(e.getX(), e.getY());
//                if (button != null) {
//                	JDocItem doc = container.getNewDoc();
//                	if (button.type == Hardware.RECTANGLE) {
//                		doc = new JDocItem(false, true);
//                    } else {
//                    	doc = new JDocItem(true, true);
//                    }    
//                	doc.setLocation(e.getX() + container.getXOffset() - doc.getWidth()/2 - getWidth(),
//                                    e.getY() + container.getYOffset() - doc.getHeight()/2);
//                    workarea.add(doc, DRAG_LAYER);
//                }
//            }
//
//            public void mouseReleased(MouseEvent e) {
//            	
//            	JDocItem doc = container.getNewDoc();
//            	
//            	if (doc == null) return;
//            	
//            	if (e.getX() > getWidth()) {
//                    docs.add(getTopDocRectangleIndex(), DocItem.createDocItem(doc));
//                    ProjectManager.getInstance().setModified();
//                    workarea.remove(doc);
//                    docsPanel.add(doc);                   
//                    doc.requestFocusInWindow();
//                    updateViewport();
//                } else {
//                	doc.setVisible(false);
//                	workarea.remove(doc);
//                }
//            	doc = null;
//            }
//        });
//
//        addMouseMotionListener(new MouseInputAdapter() {
//        	
//            public void mouseDragged(MouseEvent e) {
//            	
//            	JDocItem doc = container.getNewDoc();
//            	
//                if (doc != null) {
//                	doc.setLocation(e.getX() + container.getXOffset() - doc.getWidth()/2 - getWidth(),
//                                    e.getY() + container.getYOffset() - doc.getHeight()/2);
//                }
//            }
//        });     
    }

}
