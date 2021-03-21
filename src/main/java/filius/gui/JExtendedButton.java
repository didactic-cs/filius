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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.EventListenerList;

@SuppressWarnings("serial")
public class JExtendedButton extends JButton implements MouseMotionListener, MouseListener {
	
	Color DISABLE_OVERLAY = new Color(208, 208, 208, 216);
	Color DISABLED_BORDER = new Color(153, 153, 153);
	
	final int arrowWidth = 14;
	JPopupMenu popupMenu = null;	
	boolean mouseIsOverButton = false;
	boolean mouseIsOverArrow = false;
	
	public JExtendedButton() {
		
		setBorderPainted(false);
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	public void setPopupMenu(JPopupMenu popupMenu) {
		this.popupMenu = popupMenu;
		setHorizontalAlignment(SwingConstants.RIGHT);
	}
	
	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width + arrowWidth, height);
    }
	
	@Override   
	public void paintComponent(Graphics g){
							
		int w = getWidth();
		int h = getHeight();
		
		// Background
		Color GRAY = new Color(233, 233, 233);
		
		Graphics2D g2d = (Graphics2D)g;	 
		g2d.setColor(GRAY);
		g2d.fillRect(0, 0, w, h);	
		
		// Icon		
		getIcon().paintIcon(this, g, 0, 0);	
		
		// Right arrow
		g2d.setColor(Color.BLACK);
		int w2 = w - arrowWidth +1;
		int h2 = h / 2 - 2;
		g2d.drawLine(w2+1, h2, w2+9, h2);
		h2++;
		g2d.drawLine(w2+2, h2, w2+8, h2);
		h2++;
		g2d.drawLine(w2+3, h2, w2+7, h2);
		h2++;
		g2d.drawLine(w2+4, h2, w2+6, h2);
		h2++;
		g2d.drawLine(w2+5, h2, w2+5, h2);	
		
		// Border
		g2d.setColor(UIManager.getColor("Button.darkShadow"));
		g2d.drawRect(0, 0, w-1, h-1);
		if (mouseIsOverButton) {
			g2d.setColor(UIManager.getColor("Button.select"));
			g2d.drawRect(0, 0, w2-1, h-1);
			g2d.drawRect(2, 2, w2-5, h-5);
			g2d.setColor(UIManager.getColor("Button.darkShadow"));
			g2d.drawRect(1, 1, w2-3, h-3);
		} else if (mouseIsOverArrow) {
			g2d.setColor(UIManager.getColor("Button.select"));
			g2d.drawRect(w2-3, 0, arrowWidth+1, h-1);
			g2d.drawRect(w2-1, 2,arrowWidth-3, h-5);
			g2d.setColor(UIManager.getColor("Button.darkShadow"));
			g2d.drawRect(w2-2, 1, arrowWidth-1, h-3);
		} 
		
		// Disabled
		if (!isEnabled()) {
			// Translucent gray				
			g2d.setColor(DISABLE_OVERLAY);
			g2d.fillRect(0, 0, w, h);	
			
			// Border				
			g2d.setColor(DISABLED_BORDER);
			g2d.drawRect(0, 0, w-1, h-1);
		}
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		
		if (!isEnabled()) return;

		if (e.getX() < this.getWidth()- arrowWidth) {
			// Mouse over the button
			if (!mouseIsOverButton) {
				mouseIsOverButton = true;
				mouseIsOverArrow = false;
				repaint();
			}
		} else {
			// Mouse over the arrow
			if (!mouseIsOverArrow) {
				mouseIsOverButton = false;
				mouseIsOverArrow = true;
				repaint();
			}
		}
	}
	
	@Override
	public void mouseExited(MouseEvent e) {		
		mouseIsOverButton = false;
		mouseIsOverArrow = false;
		repaint();
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {}	
	@Override
	public void mouseEntered(MouseEvent e) {}
	@Override
	public void mouseClicked(MouseEvent e) {
		
		if (mouseIsOverButton) fireActionPerformedEx();
	}
	@Override
	public void mousePressed(MouseEvent e) {
		
		if (popupMenu != null) {
			
			if (mouseIsOverArrow && !popupMenu.isVisible()) {
				popupMenu.show(this, 0, getHeight());
			} 				
		}
	}	
	@Override
	public void mouseReleased(MouseEvent e) {}
	
	public void doClick() {
		fireActionPerformedEx();
    }
	
	//------------------------------------------------------------------------------------------------
    // Listeners management
    //------------------------------------------------------------------------------------------------ 
	
    protected EventListenerList actionListenerList = new EventListenerList();

    public void addActionListener(ActionListener al) {    	
    	
    	actionListenerList.add(ActionListener.class, al);    	
    }  
    
    protected void fireActionPerformedEx() {

        Object[] listeners = actionListenerList.getListenerList();
        ActionEvent e = null;

        for (int i = listeners.length-1; i>=0; i--) {
            if (listeners[i]==ActionListener.class) {
                // Lazily create the event:
                if (e == null) {
                    e = new ActionEvent(JExtendedButton.this, ActionEvent.ACTION_PERFORMED, getActionCommand(), 0, 0);
                }
                ((ActionListener)listeners[i+1]).actionPerformed(e);
            }
        }
    }
}
