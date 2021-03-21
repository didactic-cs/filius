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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.event.MouseInputAdapter;

import filius.gui.GUIContainer;
import filius.gui.GUIEvents;
import filius.gui.GUIMainMenu;
import filius.gui.JMainFrame;
import filius.gui.SatViewerControl;
import filius.hardware.NetworkInterface;
import filius.hardware.knoten.Computer;
import filius.hardware.knoten.Host;
import filius.hardware.knoten.InternetNode;
import filius.hardware.knoten.Modem;
import filius.hardware.knoten.Node;
import filius.hardware.knoten.Notebook;
import filius.hardware.knoten.Switch;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.ProjectManager;
import filius.software.system.ModemFirmware.ModemStatus;


/** 
 * The JLabel used to display the node on the work area
 * (previously named JSidebarImage)
 */
@SuppressWarnings("serial")
public class JNodeLabel extends JLabel implements I18n, PropertyChangeListener {
    
    // spotColor is used to display a small colored disk on the icon (with modems only)   
    static final Color TRANSPARENT     = new Color(0, 0, 0, 0);  // transparent
    static final Color CONNECTED_COLOR = new Color(0, 255, 0);   // green
    static final Color WAITING_COLOR   = new Color(255, 192, 0); // orange
    private Color spotColor = TRANSPARENT;
    
    private GUINodeItem nodeItem = null;
    private boolean selected;
    private String type;    
    
    
    public JNodeLabel() {
    	this("", "", null);
    }
    
    public JNodeLabel(boolean useListener) {
    	this("", "", null, useListener);
    }

    public JNodeLabel(String type, String text, Icon icon) {  
    	this(type, text, icon, true);
    }
    
    public JNodeLabel(String type, String text, Icon icon, boolean useListener) {  
    	
    	super(" ", icon, JLabel.CENTER);  // <- The text must not be empty for the height to be correctly computed        
        setVerticalTextPosition(SwingConstants.BOTTOM);
        setHorizontalTextPosition(SwingConstants.CENTER);
        setAlignmentX(0.5f);
        
        if (!text.equals("")) setText(text);
        
        this.type = type;     
        
        // The listener if for the action mode only
        // It must not be activated for the sidebar buttons, nor for the connecting tool !
        if (useListener) initMouseListener();
    }
    
    public GUINodeItem getNodeItem() {
    	
        return nodeItem;
    }

    public void setNodeItem(GUINodeItem nodeItem) {
    	
    	if (this.nodeItem != null) return;
    	
        this.nodeItem = nodeItem;   
        if (nodeItem != null) registerListeners();    
    }
    
    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public Rectangle getBounds() {  

    	return new Rectangle(getX(), getY(), getWidth(), getHeight());
    }

    public boolean inBounds(int x, int y) {
    	
        return (getX() <= x) && (x <= getX() + getWidth()) && (getY() <= y) && (y <= getY() + getHeight());
    }

    public int getWidth() {
    	
        int width = this.getFontMetrics(this.getFont()).stringWidth(this.getText());
        width += 5;
        if (this.getIcon() != null && this.getIcon().getIconWidth() > width)
            width = this.getIcon().getIconWidth();

        return width;
    }

    public int getHeight() {
    	
        int height = this.getFontMetrics(this.getFont()).getHeight();
        if (this.getIcon() != null) {
            height += this.getIcon().getIconHeight();
        }
        height += 5;

        return height;
    }
    
    public Dimension getPreferredSize() {
        return new Dimension(getWidth(), getHeight());
    }
    
    // There seems to be an issue with the inherited setLocation 
    public void setLocation(int x, int y) {     	
    	
    	setBounds(x, y, getWidth(), getHeight());         
    } 

    /** <b>setText2</b> changes the text and update the location so that the icon does not move 
     *  which is more natural for the user.<br>
     *  Should be called instead of setText (except when loading a project)
     * 
     * @param text
     */
    public void setText2(String text) {
    	
    	int icoW = (getIcon() != null ? getIcon().getIconWidth() : 0);
    	if (icoW % 2 == 1) icoW--;  // Trick to avoid a one pixel shift when the icon's width is odd
    	int dW = (getWidth() - icoW)/2;    	
    	setText(text);
    	int dW2 = (getWidth() - icoW)/2;    	
    	setBounds(getX() - dW2 + dW, getY(), getWidth(), getHeight());  // SetLocation won't be enough here
    }    
     
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        if (selected) {
            g.setColor(new Color(0, 0, 0));
            Graphics2D g2 = (Graphics2D) g;
            Stroke stroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1, new float[] { 2 }, 0);
            g2.setStroke(stroke);
            g2.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
            // Use same color as the one used for multiple selection 
            g2.setColor(new Color(0.80f, 0.92f, 1f, 0.2f)); 
            g2.fillRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
        }

        // Green or orange disk on modem when connected or waiting for a connection
        // No disk is displayed for the other types of JNodeLabel    
        if (spotColor != TRANSPARENT) {       
        	g2d.setColor(spotColor);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.fillOval((this.getWidth() / 2) - 6, (this.getHeight() / 2) - 6, 12, 12);
        }
        
    }  
    
    private void updateColorSpot(ModemStatus modemStatus) {

    	switch(modemStatus) {
    	case connected:
    		spotColor = CONNECTED_COLOR;
    		break;
    	case waiting:
    		spotColor = WAITING_COLOR;
    		break;
    	default:
    		spotColor = TRANSPARENT;
    	}    	
    	updateUI();
    }	
    
    public void updateTooltip() {
    	
    	InternetNode node = (InternetNode) getNodeItem().getNode();
    	
    	String tooltip = "<html><pre>";
    	if (node instanceof Host) {
    		NetworkInterface nic = node.getNIC(0);
    		tooltip += "\n <b>"+messages.getString("jnodelabel_tooltip_mac")+"</b> " + nic.getMac();
    		tooltip += " \n <b>"+messages.getString("jnodelabel_tooltip_ip")+"</b> " + nic.getIp();
    		tooltip += " \n <b>"+messages.getString("jnodelabel_tooltip_mask")+"</b> " + nic.getSubnetMask();
    		if (!nic.getGateway().equals("")) tooltip += "\n <b>"+messages.getString("jnodelabel_tooltip_gateway")+"</b> " + nic.getGateway();
    		if (!nic.getDns().equals("")) tooltip += "\n <b>"+messages.getString("jnodelabel_tooltip_dns")+"</b> " + nic.getDns();
    	} else {
    		// For routers, we only show the connected nic
    		tooltip += "\n    <b>"+messages.getString("jnodelabel_tooltip_mac_ip_mask")+"</b> ";
    		for (NetworkInterface nic : node.getNICList()) {
    			if (nic.getPort().isConnected()) {
    				tooltip += "\n " + nic.getMac() + " | " + nic.getIp() + " | " + nic.getSubnetMask() +" ";
    			}    
    		}
    		// The gateway if any
    		NetworkInterface nic = node.getNIC(0);
    		if (!nic.getGateway().equals("")) tooltip += "\n           <b>"+messages.getString("jnodelabel_tooltip_gateway")+"</b> " + nic.getGateway();
    	}
    	tooltip += " </pre></html>";
    	
    	setToolTipText(tooltip);
    }   

    private void initMouseListener() {
    	
    	addMouseListener(new MouseInputAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
            	
                if (GUIContainer.getInstance().getCurrentMode() == GUIMainMenu.ACTION_MODE) {                    
                    
                    if (e.getButton() == MouseEvent.BUTTON3) {
                    	
                    	GUIEvents.getInstance().actionModeNodeContextMenu(nodeItem, e.getX(), e.getY());
                    	//ProjectManager.getInstance().setModified();

                    } else if (e.getButton() == MouseEvent.BUTTON1) {

                    	if (e.getClickCount() == 2) {
                    		if (type.equals(Computer.TYPE) || type.equals(Notebook.TYPE)) {

                    			GUIContainer.getInstance().showDesktop(nodeItem);
                    			ProjectManager.getInstance().setModified();

                    		} else if (type.equals(Switch.TYPE)) {

                    			if (nodeItem != null) {
                    				SatViewerControl.getInstance().addOrShowViewer((Switch) nodeItem.getNode());
                    				ProjectManager.getInstance().setModified();
                    			}                    			
                    		}
                    	} 
                    }
                }
            }
        });
    }    
    
    private void registerListeners() {
    	
    	Node node = nodeItem.getNode();
    	if (node == null) return;    	
 
    	if (node instanceof InternetNode) {
    		node.getSystemSoftware().addPropertyChangeListener("ipaddress", this);
    		node.getSystemSoftware().addPropertyChangeListener("dnsaddress", this); 	
    	}
    	if (node instanceof Modem) node.getSystemSoftware().addPropertyChangeListener("modemstatus", this);
    	node.getSystemSoftware().addPropertyChangeListener("message", this);
    }
    
	/**
     * <b>propertyChange</b> is called whenever a change in the host must be reflected by the user interface. 
     *     
     */
	public void propertyChange(PropertyChangeEvent evt) { 
		
		String pn = evt.getPropertyName();		

		if (pn.equals("nodename")) {
			                           
			// Update the display name	
			// From: Node
			setText2((String)evt.getNewValue());			
		} 
		else if (pn.equals("message")) {   
			
			// Display an error message	
			// From: SystemSoftware
			JOptionPane.showMessageDialog(JMainFrame.getInstance(), (String)evt.getNewValue());
		}
		else if (pn.equals("ipaddress")) {

			// Update the display name and the tooltip
			// From: Node, InternetNodeOS
			setText2((String)evt.getNewValue());	
			updateTooltip();
		} 
		else if (pn.equals("dnsaddress")) {

			// Update the tooltip
			// From: InternetNodeOS
			updateTooltip();
		} 
		else if (pn.equals("modemstatus")) { 
			
			// Update the modem status spot	
			// From: Modem
			updateColorSpot((ModemStatus)evt.getNewValue());		
		}
	};    
}
