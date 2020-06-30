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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.QuadCurve2D;
import java.beans.Transient;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import filius.Main;
import filius.gui.GUIContainer;
import filius.gui.GUIMainMenu;
import filius.hardware.CableActiveListener;

/**
 * 
 * @author Johannes Bade
 */
@SuppressWarnings("serial")
public class JCablePanel extends JPanel implements CableActiveListener {
	    
    private GUINodeItem ziel1, ziel2;
    private int xZiel1, yZiel1, xZiel2, yZiel2;
    private boolean flip;
    
    // selected is only used in Design mode 
    private boolean selected = false;
    
    // active is used in Simulation mode  
    // and also in Design mode when the configuration of a router is shown and 
    private boolean active = false;  
    
    private final Color standardColor = new Color(64, 64, 64);   // darkgray    
    private final Color selectedColor = new Color(0, 128, 255);  // blue
    private final Color activeColor   = new Color(0, 255, 64);   // green
    
    private Color color = standardColor; 
    

    public JCablePanel() {
        super();
        this.setOpaque(false);
    }

    public void updateBounds() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (JCablePanel), updateBounds()");        

        // Theoretisch korrekte Positionen        
        int x1 = (int) (ziel1.getNodeLabel().getX() + (0.5 * ziel1.getNodeLabel().getWidth()));
        int y1 = (int) (ziel1.getNodeLabel().getY() + (0.5 * ziel1.getNodeLabel().getHeight()));
        int x2 = (int) (ziel2.getNodeLabel().getX() + (0.5 * ziel2.getNodeLabel().getWidth()));
        int y2 = (int) (ziel2.getNodeLabel().getY() + (0.5 * ziel2.getNodeLabel().getHeight()));

        // Absolut korrekte Positionen (also Sidebar und Menu rausgerechnet)
        int t1;
        
        if (x1 > x2) {
            t1 = x1;
            x1 = x2;
            x2 = t1;
        }
        if (y1 > y2) {
            t1 = y1;
            y1 = y2;
            y2 = t1;
        }

        // Add 2 for each direction to take care of linewidth
        setBounds(x1 - 2, y1 - 2, x2 - x1 + 4, y2 - y1 + 4);
        
        // Keep coordinates
        xZiel1 = (int) (ziel1.getNodeLabel().getX() + (0.5 * ziel1.getNodeLabel().getWidth()));
        yZiel1 = (int) (ziel1.getNodeLabel().getY() + (0.5 * ziel1.getNodeLabel().getHeight()));
        xZiel2 = (int) (ziel2.getNodeLabel().getX() + (0.5 * ziel2.getNodeLabel().getWidth()));
        yZiel2 = (int) (ziel2.getNodeLabel().getY() + (0.5 * ziel2.getNodeLabel().getHeight()));   
        
        Main.debug.println("JCablePanel (" + this.hashCode() + "), bounds: " + x1 + "/" + y1 + ", " + x2 + "/" + y2 +
                                         "  (W:" + (x2 - x1) + ", H:" + (y2 - y1) + ")");
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Coordinates of the extremities of the curve relative to the panel         
        int x1 = xZiel1 - this.getX();
        int y1 = yZiel1 - this.getY();
        int x2 = xZiel2 - this.getX();
        int y2 = yZiel2 - this.getY();  
        
        // Coordinates of the control point
        int xCP = (x1 + x2) / 4;
        int yCP = (y1 + y2) / 4;
        
        // Fix X value of control point so that the curvature is oriented upward
        flip = ((x1 < x2 && y1 < y2) || (x2 < x1 && y2 < y1));
        
        if (flip) xCP = 3 * xCP;      

        // QuadCurve2D is a quadratic parametric curve with a single control point
        QuadCurve2D curve = new QuadCurve2D.Double(x1, y1, xCP, yCP, x2, y2);
        
        // Draw the curve
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2));
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);   
        g2.draw(curve);
    }

    /*
     * Method to examine whether the mouse was clicked close to a curve representing a cable
     * 
     */
    public boolean clicked(int x, int y) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (JCablePanel), clicked(" + x + "," + y + ")");
        
        // Maximum vertical and horizontal allowed distance from the curve
        int delta = 6;
        
        // Use coordinates relative to the top left of the panel 
        int relX = x - this.getX();
        int relY = y - this.getY();
        
        // First check that the click was close to the panel containing the curve
        boolean hitPanel = (- delta <= relX) && (relX <= getWidth() + delta) && (- delta <= relY) && (relY <= getHeight() + delta);
        
        // If not, there is no need for further computation
        if (! hitPanel) return false;
                
        // Use symmetry to reduce to the case where the curve is increasing (from bottom left to top right).    
        if (flip) relX = this.getWidth() - relX;       
                
        // When normalized on [0,1]x[0,1], the curve has the folowing parametric equation:
        // x(t) = 0.5(1-t)(2-t)  y(t) = 0.5t(1+t) with t in [0,1]
        
        // Check vertical distance between clicked point and curve
        double t = 0.5*(3 - Math.sqrt(1+8*((double)relX)/getWidth()));
        double yc = 0.5*t*(1+t)*getHeight();
        if (Math.abs(relY - yc) <= delta) return true;

        // Check horizontal distance between clicked point and curve
        t = 0.5*(-1 + Math.sqrt(1+8*((double)relY)/getHeight()));
        double xc = 0.5*(1-t)*(2-t)*getWidth();
        if (Math.abs(relX - xc) <= delta) return true;
        
        // The point is considered to be too far from the curve
        return false;
    }

    public GUINodeItem getZiel1() {
        return ziel1;
    }

    public void setZiel1(GUINodeItem ziel1) {
        this.ziel1 = ziel1;
    }

    public GUINodeItem getZiel2() {
        return ziel2;
    }

    public void setZiel2(GUINodeItem ziel2) {
        this.ziel2 = ziel2;
        updateBounds();
    }
    
    /** 
     * <b>updateColor</b> changes the color of the cable in order
     * to reflect its current status
     * 
     */
    public void updateColor() {
    	
    	switch (GUIContainer.getInstance().getActiveSite()) {
    	
    		case (GUIMainMenu.MODUS_ENTWURF): {
    			
    			if (selected || active) color = selectedColor; 
    			else color = standardColor;  
    			break;
    		} 
    		
    		case (GUIMainMenu.MODUS_AKTION): {
    			
    			if (active) color = activeColor; 
    			else color = standardColor;  
    			break;
    		} 
    		
    		case (GUIMainMenu.MODUS_DOKUMENTATION): {

    			color = standardColor;  
    		} 
    	}  	

    	updateUI();  	
    }
        
    @Transient
    public boolean getSelected() {
    	
        return selected;
    }

    @Transient
    public void setSelected(boolean selected) {
    	
    	if (this.selected == selected) return;
    	
        this.selected = selected;  
        updateColor();
    }
    
    /** 
     * {@inheritDoc}
     */
    public void onActiveChange(boolean active) {
    	
    	this.active = active;  
        updateColor();	
    }
}
