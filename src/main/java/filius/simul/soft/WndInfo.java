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
package filius.software;

import java.awt.Dimension;
import java.awt.Point;
import java.io.Serializable;

/**
 * WndInfo is stored along with the application in the projects
 * It is used to restore the application's main window on the desktop the
 * first time the simulation mode is started after opening a project.
 */
@SuppressWarnings("serial")
public class WndInfo implements Serializable {
	
	public static final int WI_MOVED   = 0;
	public static final int WI_RESIZED = 1;
	public static final int WI_ICON    = 2;
	public static final int WI_MAXIMUM = 3;
	
	private int left;
	private int top;
	private int width;
	private int height;
	private boolean icon;
	private boolean maximum;
	
	
	public WndInfo() {		
	}
	
	public int getLeft() {
		
		return left;
	}
	
	public void setLeft(int left) {
		
		this.left = left;
	}
	
	public int getTop() {
		
		return top;
	}
	
	public void setTop(int top) {
		
		this.top = top;
	}
	
//	public Point getLocation() {
//		
//		return new Point();
//	}
	
	public void setLocation(Point p) {
		
		left = p.x;
		top = p.y;
	}
	
	public int getWidth() {
		
		return width;
	}
	
	public void setWidth(int width) {
		
		this.width = width;
	}
	
	public int getHeight() {
		
		return height;
	}
	
	public void setHeight(int height) {
		
		this.height = height;
	}
	
//	public Dimension getSize() {
//		
//		return size;
//	}
	
	public void setSize (Dimension size) {
		
		width = size.width;
		height = size.height;
	}
	
	public boolean getIcon() {
		
		return icon;
	}
	
	public void setIcon(boolean icon) {
		
		this.icon = icon;
	}
	
	public boolean getMaximum() {
		
		return maximum;
	}
	
	public void setMaximum(boolean maximum) {
		
		this.maximum = maximum;
	}
}
