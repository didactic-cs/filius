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
package filius.gui.anwendungssicht;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Observable;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import filius.Main;
import filius.rahmenprogramm.Base64;
import filius.rahmenprogramm.Information;
import filius.software.system.HostOS;
import filius.software.system.FiliusFile;
import filius.software.system.FiliusFileNode;

@SuppressWarnings("serial")
public class GUIApplicationImageViewerWindow extends GUIApplicationWindow {

	private JPanel backPanel;
	private JLabel container;
	private Image image;
	private int imageWidth;
	private int imageHeight;
	private int curWidth;
	private int curHeight;
	

	public GUIApplicationImageViewerWindow(final GUIDesktopPanel desktop, String appName) {
		
		super(desktop, appName);

		initMenu();
			
		backPanel = new JPanel(new BorderLayout());
		getContentPane().add(backPanel);
		pack();		
		
		addComponentListener(new ComponentAdapter() {
			
            public void componentResized(ComponentEvent e) {
            	super.componentResized(e);
            	updateIcon();
            }
        });
	}
	
	private void initMenu() {
		
		JMenu menu = new JMenu(messages.getString("imageviewer_msg1"));

		menu.add(new AbstractAction(messages.getString("imageviewer_msg2")) {

			public void actionPerformed(ActionEvent arg0) {
				open();
			}
		});

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(menu);

		setJMenuBar(menuBar);		
	}

	public void open() {
		
		DMTNFileChooser fc = new DMTNFileChooser((HostOS) getApplication().getSystemSoftware());
		int rueckgabe = fc.openDialog();

		if (rueckgabe == DMTNFileChooser.OK) {
			
			FiliusFile file = fc.getAktuellerOrdner().getFiliusFile(fc.getAktuellerDateiname());			
			displayFile(file);
		} 
		else {
			Main.debug.println("ERROR (" + hashCode() + "): Fehler beim oeffnen einer Datei");
		}
	}
	
	public void start(FiliusFileNode node, String[] param) {
		
		if (node != null) displayFile(node.getFiliusFile());		
	}
	
	private void displayFile(FiliusFile file) {		

		if (file == null) return;
		
		setTitle(file.getName());
		
		String path = Information.getInstance().getTempPath() + file.getName();		
		Base64.decodeToFile(file.getContent(), path);	
		
		ImageIcon icon = new ImageIcon(path);
		
		image = icon.getImage(); 
		imageWidth = icon.getIconWidth();
		imageHeight = icon.getIconHeight();
		
		curWidth = 0;
		curHeight = 0;
			
		updateIcon();	
	}
	
	// Resize the icon so that it doesn't overflows from the backPanel
	// Scale is 1 or less.
	private void updateIcon() {
		
		int w = imageWidth;
		int h = imageHeight;
		
		if (backPanel.getWidth() < w) {
			
			h = h * backPanel.getWidth() / w;
			w = backPanel.getWidth();
		}
		
		if (backPanel.getHeight() < h) {
			
			w = w * backPanel.getHeight() / h;
			h = backPanel.getHeight();	
		}
		
		if (w == curWidth && h == curHeight) return;
		
		curWidth = w;
		curHeight = h;
		
		ImageIcon icon = new ImageIcon(image.getScaledInstance(w, h, Image.SCALE_DEFAULT));		
		
		if (container != null) backPanel.remove(container);		
		container = new JLabel(icon);			
		backPanel.add(container, BorderLayout.CENTER);
		
		backPanel.updateUI();	
	}

	public void update(Observable arg0, Object arg1) {
	}
}
