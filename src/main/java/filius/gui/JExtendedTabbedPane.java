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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;

@SuppressWarnings("serial")
public class JExtendedTabbedPane extends JTabbedPane {
	
	private final int  closeboxWidth  = 10;
	private final int  closeboxHeight = 10;
	private final String emptyspace   = "    "; // get free space for the close box
	private final Color DARKBORDER = new Color(166, 166, 166); // Gray
	private final Color SELECTED_LIGHTBORDER = Color.WHITE; 
	private final Color UNSELECTED_LIGHTBORDER = new Color(220, 220, 220); // Lightgray
	private final Color SELECTED_CROSS = new Color(50, 50, 50);      // Very dark gray (like the title)
	private final Color UNSELECTED_CROSS = new Color(110, 110, 110); // Darkgray
	protected EventListenerList closingListenerList = new EventListenerList();
	protected transient ChangeEvent closingEvent = null;
	
	
	public JExtendedTabbedPane() {  
		
		createCloseButtonListener();
    }
	
	public void addTab(String title, Component component) {
		super.addTab(title + emptyspace, component);		
	}
	
	public void setTitleAt(int index, String title) {
		super.setTitleAt(index, title + emptyspace);
	}
	
	public String getTabTitleAt(int index) {
		return super.getTitleAt(index).trim();
	}	
	
	public void addClosingListener(TabClosingListener l) {
		closingListenerList.add(TabClosingListener.class, l);
    }
	
	public void removeClosingListener(TabClosingListener l) {
		closingListenerList.remove(TabClosingListener.class, l);
    }	
	
	private boolean fireCanClose() {

		Object[] listeners = closingListenerList.getListenerList();
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i] == TabClosingListener.class) {
				if (closingEvent == null) closingEvent = new ChangeEvent(this);
				if (!((TabClosingListener)listeners[i+1]).canClose(closingEvent)) return false;
			}
		}
		return true;
	}	

	private void createCloseButtonListener() {
		
		class CloseMouseAdapter extends MouseAdapter {
			
			public void mouseReleased(MouseEvent ev) {
				
				int index = getSelectedIndex();
				Rectangle r = getBoundsAt(index);
				r.x = r.x + r.width - closeboxWidth - 6;
				r.width = closeboxWidth + 2;
				r.y = r.y + r.height - closeboxHeight - 6;
				r.height = closeboxHeight + 2;
				if (r.contains(ev.getPoint())) {
					if (fireCanClose()) removeTabAt(getSelectedIndex());
				}	
			}
		}
		
		addMouseListener(new CloseMouseAdapter());
	}	
	
	public void paint(Graphics g){
		super.paint(g);
		paintTabs(g);
	}
	
	private void paintTabs(Graphics g){

		for (int i = 0; i < getTabCount(); i++) {			
	
			int x = getBoundsAt(i).x + getBoundsAt(i).width - closeboxWidth - 5;
			int y = getBoundsAt(i).y + 5;	
			
			int w = closeboxWidth - 2;
			int h = closeboxHeight - 2; 
			
			// Draw the cross		
			if (getComponent(i).isShowing()) g.setColor(SELECTED_CROSS);
			else                             g.setColor(UNSELECTED_CROSS);	
			g.drawLine(x+1, y+1,  x+w,  y+h);
			g.drawLine(x+2, y+1,  x+w,  y+h-1);
			g.drawLine(x+1, y+2, x+w-1, y+h);
			
			g.drawLine(x+w,   y+1, x+1, y+h);
			g.drawLine(x+w-1, y+1, x+1, y+h-1);
			g.drawLine(x+w,   y+2, x+2, y+h);
			
			// Draw the frame
			if (getComponent(i).isShowing()) g.setColor(SELECTED_LIGHTBORDER);
			else                             g.setColor(UNSELECTED_LIGHTBORDER);		
			g.drawRect(x-1, y-1, closeboxWidth+2, closeboxHeight+2);
			g.drawLine(x-2, y+closeboxHeight+1, x-1, y+closeboxHeight+1);
			g.drawLine(x+closeboxWidth+1, y-2, x+closeboxWidth+1, y-1);
			
            g.setColor(DARKBORDER);	
			g.drawRect(x-2, y-2, closeboxWidth+2, closeboxHeight+2);
		}	
	}
}
