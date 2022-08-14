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

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import filius.common.Base64;
import filius.common.Information;
import filius.software.system.FiliusFile;

@SuppressWarnings("serial")
public class JImageViewer extends JPanel {
	
	private Image image;
	private JLabel imageContainer;
	private int imageWidth;
	private int imageHeight;
	private int currentWidth;
	private int currentHeight;
	
	
	public JImageViewer() {
		
		super(new BorderLayout());		
		initListeners();
	}
	
	private void initListeners() {
		
		addComponentListener(new ComponentAdapter() {
			
            public void componentResized(ComponentEvent e) {
            	super.componentResized(e);
            	updateImageSize();
            }
        });
	}	
	
	/** 
	 * <b>clearImage</b> releases the image when it no longer needs to be displayed 
	 * */
	public void clearImage() {		

		image = null; 
		imageWidth = 0;
		imageHeight = 0;
		if (imageContainer != null) remove(imageContainer);
	}
	
	/** 
	 * <b>loadFiliusFile</b> loads an image from a FiliusFile
	 * @param file
	 */
	public void loadFiliusFile(FiliusFile file) {		

		if (file == null) return;			
		
		String path = Information.getInstance().getTempPath() + file.getName();		
		Base64.decodeToFile(file.getContent(), path);	
		
		ImageIcon icon = new ImageIcon(path);
		
		image = icon.getImage(); 
		imageWidth = icon.getIconWidth();
		imageHeight = icon.getIconHeight();
		
		currentWidth = 0;
		currentHeight = 0;
			
		updateImageSize();	
	}
	
	/** 
	 * <b>loadFile</b> loads an image from a real file
	 * @param file
	 */
	public void loadFile(String path) {		
		
		ImageIcon icon = new ImageIcon(path);
		
		image = icon.getImage(); 
		imageWidth = icon.getIconWidth();
		imageHeight = icon.getIconHeight();
		
		currentWidth = 0;
		currentHeight = 0;
			
		updateImageSize();	
	}
	
	// Resize the icon so that it doesn't overflow from the panel
	// Scale is 1 or less.
	private void updateImageSize() {
		
		int w = imageWidth;
		int h = imageHeight;
		
		if (getWidth() < w) {
			
			h = h * getWidth() / w;
			w = getWidth();
		}
		
		if (getHeight() < h) {
			
			w = w * getHeight() / h;
			h = getHeight();	
		}
		
		if (w == currentWidth && h == currentHeight) return;
		
		currentWidth = w;
		currentHeight = h;
		
		ImageIcon icon = new ImageIcon(image.getScaledInstance(w, h, Image.SCALE_DEFAULT));		
		
		if (imageContainer != null) remove(imageContainer);
		imageContainer = new JLabel(icon);
		add(imageContainer, BorderLayout.CENTER);
		
		updateUI();	
	}
}
