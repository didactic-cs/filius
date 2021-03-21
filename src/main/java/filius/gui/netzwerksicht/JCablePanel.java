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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.Transient;

import javax.swing.JPanel;

import filius.Main;
import filius.gui.GUIContainer;
import filius.gui.GUIMainMenu;
import filius.hardware.Cable;
import filius.rahmenprogramm.I18n;

/**
 * @author Johannes Bade
 */
@SuppressWarnings("serial")
public class JCablePanel extends JPanel implements I18n, PropertyChangeListener {
	    
    private GUINodeItem nodeItem1 = null;
    private GUINodeItem nodeItem2 = null;
    private int nodeItem1X, nodeItem1Y;
    private int nodeItem2X, nodeItem2Y;
    private boolean flip;
    
    // selected is only used in Design mode 
    private boolean selected = false;
    
    // active is used in Simulation mode  
    // and also in Design mode when the configuration of a router is shown and 
    private boolean active = false;
    
    // blocked is only used in Simulation mode  
    private boolean blocked = false; 
    
    private final Color standardColor = new Color(64, 64, 64);    // darkgray    
    private final Color selectedColor = new Color(0, 128, 255);   // blue
    private final Color activeColor   = new Color(0, 255, 64);    // green
    private final Color blockedColor  = new Color(180, 0, 0);     // darkred        
    
    private Color color = standardColor; 
    

    public JCablePanel() {
        super();
        this.setOpaque(false);  
    }

    public void updateBounds() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (JCablePanel), updateBounds()");        

        // Theoretisch korrekte Positionen        
        int x1 = (int) (nodeItem1.getNodeLabel().getX() + (0.5 * nodeItem1.getNodeLabel().getWidth()));
        int y1 = (int) (nodeItem1.getNodeLabel().getY() + (0.5 * nodeItem1.getNodeLabel().getHeight()));
        int x2 = (int) (nodeItem2.getNodeLabel().getX() + (0.5 * nodeItem2.getNodeLabel().getWidth()));
        int y2 = (int) (nodeItem2.getNodeLabel().getY() + (0.5 * nodeItem2.getNodeLabel().getHeight()));

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
        nodeItem1X = (int) (nodeItem1.getNodeLabel().getX() + (0.5 * nodeItem1.getNodeLabel().getWidth()));
        nodeItem1Y = (int) (nodeItem1.getNodeLabel().getY() + (0.5 * nodeItem1.getNodeLabel().getHeight()));
        nodeItem2X = (int) (nodeItem2.getNodeLabel().getX() + (0.5 * nodeItem2.getNodeLabel().getWidth()));
        nodeItem2Y = (int) (nodeItem2.getNodeLabel().getY() + (0.5 * nodeItem2.getNodeLabel().getHeight()));   
        
        Main.debug.println("JCablePanel (" + this.hashCode() + "), bounds: " + x1 + "/" + y1 + ", " + x2 + "/" + y2 +
                                         "  (W:" + (x2 - x1) + ", H:" + (y2 - y1) + ")");
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Coordinates of the extremities of the curve relative to the panel         
        int x1 = nodeItem1X - this.getX();
        int y1 = nodeItem1Y - this.getY();
        int x2 = nodeItem2X - this.getX();
        int y2 = nodeItem2Y - this.getY();  
        
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
     * Method to examine whether the mouse was clicked close to the curve representing a cable
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

    public GUINodeItem getNodeItem1() {
        return nodeItem1;
    }

    public void setNodeItem1(GUINodeItem nodeItem) {
        this.nodeItem1 = nodeItem;
    }

    public GUINodeItem getNodeItem2() {
        return nodeItem2;
    }

    public void setNodeItem2(GUINodeItem nodeItem) {
        this.nodeItem2 = nodeItem;
        updateBounds();
    }
    
    /** 
     * <b>updateColor</b> changes the color of the cable in order
     * to reflect its current status
     * 
     */
    public void updateColor() {
    	
    	switch (GUIContainer.getInstance().getCurrentMode()) {
    	
    		case (GUIMainMenu.DESIGN_MODE): {
    			
    			if (selected || active) color = selectedColor; 
    			else color = standardColor;  
    			break;
    		} 
    		
    		case (GUIMainMenu.ACTION_MODE): {
    			
    			if (blocked) color = blockedColor; 
    			else if (active) color = activeColor; 
    			else color = standardColor;  
    			break;
    		} 
    		
    		case (GUIMainMenu.DOC_MODE): {

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
    
    public void setBlocked(boolean blocked) {
    	
    	if (this.blocked == blocked) return;
    	
        this.blocked = blocked;  
        updateColor();
    }
    
    // Work in progress... 
    // (need to trigger the tooltip only when the mouse is over the cable, not over the containing panel) 
//    void updateTooltip(InternetNode node) {
//    	
//    	String tooltip = "<html><pre>";
//    	tooltip += "cable";
//    	tooltip += " </pre></html>";
//    	
//    	setToolTipText(tooltip);
//    }
    
    public void registerListeners(Cable cable) { 
    	
    	cable.addPropertyChangeListener("cableactivity", this);
    }
    
    
	/**
     * <b>propertyChange</b> whenever a change in the host must be reflected by the user interface. 
     *     
     */
	public void propertyChange(PropertyChangeEvent evt) {
		
		String pn = evt.getPropertyName();
		
		if (pn == "cableactivity") {           
			// Update the cable color to reflect the trafic activity
			this.active = (Boolean) evt.getNewValue();
			updateColor();
		} 		
	};
}
