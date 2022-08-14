package filius.gui.modes.design;

import java.awt.Graphics2D;
import java.awt.image.ImageObserver;

import javax.swing.ImageIcon;

public class JIconOverlay {
	
	private ImageIcon icon = null;
    private int dx = 0;
    private int dy = 0;
    private int w = 0;
    private int h = 0;
    public boolean visible = false;
    
    public JIconOverlay() {
    }
    
    public void init(String resource, int dx, int dy) {
    	
    	icon = new ImageIcon(getClass().getResource(resource));
    	this.dx = dx;
    	this.dy = dy;
    	this.w = icon.getIconWidth();
    	this.h = icon.getIconHeight();
    }
    
    void draw(ImageObserver obs, Graphics2D g2d, int x, int y) {
    	if (icon == null)  return;
    	g2d.drawImage(icon.getImage(), x + dx, y + dy, w, h, obs);
    }
}