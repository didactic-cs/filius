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
/*
 * NewJFrame.java
 *
 * Created on 28. April 2006, 18:31
 */

package filius.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.MouseInfo;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JFrame;
import javax.swing.JTextField;
import filius.Main;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.ProjectManager;

@SuppressWarnings("serial")
public class JMainFrame extends JFrame implements I18n, WindowListener, KeyEventDispatcher, PropertyChangeListener {

    private static JMainFrame mainFrame = null;
    private static GUIContainer container = null;

    
    private JMainFrame() {
    	
    	// Add the main frame to the frame list
    	JFrameList.getInstance().addMain(this);
    	
        addWindowListener(this);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
        ProjectManager.getInstance().addPropertyChangeListener("statusAndName", this);
        
        setMinSize (867, 650);
        
        initComponents();
        
        updateTitle();
    }

    public static JMainFrame getInstance() {
        if (mainFrame == null) {
            mainFrame = new JMainFrame();
        }

        return mainFrame;
    }

    /**
     * 
     * Fragt ab, ob wirklich beendet werden soll, ausserdem wird der temp-Ordner geleert
     * 
     */
    public void windowClosing(WindowEvent e) {
        Main.confirmAndStop();
    }  
    
    // Required for the WindowListener interface
    public void windowOpened(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}    
    public void windowDeactivated(WindowEvent e) {}    
    public void windowIconified(WindowEvent e) {}    
    public void windowDeiconified(WindowEvent e) {}    
    public void windowClosed(WindowEvent e) {}   

    // Required to define a minimum size to the main Window
    private void setMinSize(Integer minWidth, Integer minHeight) {
    	
    	// setMinimumSize seems not to work on all platforms...
    	setMinimumSize(new Dimension(minWidth, minHeight));

    	// ... whence this listener
    	addComponentListener(new ComponentAdapter() {
    		
    		public void componentResized(ComponentEvent evt) {  	
    			
    			int width  = getSize().width;
    			int height = getSize().height;
    			int minWidth  = getMinimumSize().width;
    			int minHeight = getMinimumSize().height;    			
    			
    			Boolean resizeRequired = false;    			
    			if (width < minWidth) {
    				width = minWidth; 
    				resizeRequired = true;
    			}
    			if (height < minHeight) {
    				height = minHeight; 
    				resizeRequired = true;
    			}
    			if (resizeRequired) setSize(width, height);
    		}
    	});
    }
        
    public boolean dispatchKeyEvent(KeyEvent e) {
    	
    	if (container == null) container = GUIContainer.getInstance();
    	int mode = container.getCurrentMode();
    	
        if (e.getID() == KeyEvent.KEY_PRESSED && !(e.getSource() instanceof JTextField)) {

            // ignore space bar pressing on buttons 
            if ((e.getKeyChar() == KeyEvent.VK_SPACE) && (e.getSource().getClass().getSimpleName() == "JButton")) {
                return true;
            }
            
            // Cancel a pending cable connection
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                GUIEvents.getInstance().resetAndHideCablePreview();
            }
            
            // No modifier
            //-------------
            if (!e.isShiftDown() && !e.isControlDown() && !e.isAltDown()) {   
            
                // delete item on deletion key press 
            	if ((e.getKeyChar() == KeyEvent.VK_DELETE) && (mainFrame.isFocused()) && mode == GUIMainMenu.DESIGN_MODE) {

            		if (container.isMarqueeVisible()) {
            			// Remove all nodes under the marquee
            			GUIEvents.getInstance().removeMarqueeNodes();
            			return true;

            		} else {
            			// Remove the selected node or cable, if any
            			GUIEvents.getInstance().removeSelectedItem();
            			return true;
            		}                   
            	}

            	// C or K : toggle cable preview
            	if (((e.getKeyCode() == KeyEvent.VK_C) || (e.getKeyCode() == KeyEvent.VK_K)) && (mode == GUIMainMenu.DESIGN_MODE)) {
            		// Main.debug.println("KeyDispatcher: ALT+1 recognised");
            		toggleCablePreview();
            		return true;
            	}

            	// F1 : show help            	 
            	if ((e.getKeyCode() == KeyEvent.VK_F1)) {
            		GUIContainer.getInstance().getMainMenu().doClick("btHilfe");
            			return true;                    
            	}
      
            	// F9 : Documentation mode            	 
            	if ((e.getKeyCode() == KeyEvent.VK_F9)) {
            		GUIContainer.getInstance().getMainMenu().doClick("btDokumodus");
            			return true;                    
            	}
            	
            	// F10 : Design mode           	 
            	if ((e.getKeyCode() == KeyEvent.VK_F10)) {
            		GUIContainer.getInstance().getMainMenu().doClick("btEntwurfsmodus");
            			return true;                    
            	}
            	
            	// F11 : Simulation/action mode            	 
            	if ((e.getKeyCode() == KeyEvent.VK_F11)) {
            		GUIContainer.getInstance().getMainMenu().doClick("btAktionsmodus");
            			return true;                    
            	}
            }
            
            // Control only
            //--------------
           	if (!e.isShiftDown() && e.isControlDown() && !e.isAltDown()) {              		
           
                // Main.debug.println("KeyDispatcher: CTRL-Key pressed, waiting for additional key!");
                switch (e.getKeyCode()) {
                case KeyEvent.VK_N: // (new)
                    // Main.debug.println("KeyDispatcher: CTRL+N recognised");
                    GUIContainer.getInstance().getMainMenu().doClick("btNeu");
                    return true;
                case KeyEvent.VK_O: // (open)
                    // Main.debug.println("KeyDispatcher: CTRL+O recognised");
                    GUIContainer.getInstance().getMainMenu().doClick("btOeffnen");
                    return true;
                case KeyEvent.VK_S: // (save file)
                    // Main.debug.println("KeyDispatcher: CTRL+S recognised");
                    GUIContainer.getInstance().getMainMenu().doClick("btSpeichern");
                    return true;
                case KeyEvent.VK_D: // (duplicate)
                    // Main.debug.println("KeyDispatcher: CTRL+D recognised");
                    //GUIContainer.getInstance().getMainMenu().doClick("btEntwurfsmodus");
                	if (mode == GUIMainMenu.DOC_MODE) GUIContainer.getInstance().duplicateDocElement(null);
                    return true;
                case KeyEvent.VK_R: // (run-time/simulation mode)
                    // Main.debug.println("KeyDispatcher: CTRL+R recognised");
                    GUIContainer.getInstance().getMainMenu().doClick("btAktionsmodus");
                    return true;
                case KeyEvent.VK_LEFT: // left arrow (slower simulation)
                    // Main.debug.println("KeyDispatcher: CTRL+left recognised");
                    GUIContainer.getInstance().getMainMenu().changeSlider(-1);
                    return true;
                case KeyEvent.VK_RIGHT: // right arrow (faster simulation)
                    // Main.debug.println("KeyDispatcher: CTRL+right recognised");
                    GUIContainer.getInstance().getMainMenu().changeSlider(1);
                    return true;
                case KeyEvent.VK_W: // (wizard for new modules)
                    // Main.debug.println("KeyDispatcher: CTRL+W recognised");
                    GUIContainer.getInstance().getMainMenu().doClick("btWizard");
                    return true;
                }
            }
        } 
        return false;
    }     

    private void initComponents() {

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1000, 700);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width / 2 - (getWidth() / 2), screenSize.height / 2 - (getHeight() / 2));
    }
    
    // Temporary trick to avoid a crash due to a (too early?) call to I18n
    boolean firstCall = true;

    private void updateTitle() {
    	
        String title = ProjectManager.getInstance().getPath();
        
        if (title != null) {        	
        	// Shorten the path if too long            
            if (title.length() > 80) {
            	title = title.substring(0,30) + "…" + title.substring(title.length() - 48);   ;   
            }            
        } else {
        	if (!firstCall) title = messages.getString("guimainmemu_msg23");   // <- Crashes Filius at start
        	else title= "Projet";
        	firstCall = true;
        }
        
        // Add a star to notify an unsaved change in the project
        if (ProjectManager.getInstance().isModified()) title = title + " *";
        setTitle("FILIUS - " + title);        
    }

    private void toggleCablePreview() {
        if (GUIContainer.getInstance().getConnectingTool().isVisible()) {
            GUIEvents.getInstance().resetAndHideCablePreview();
        } else {
            int currentPosX = (int) (MouseInfo.getPointerInfo().getLocation().getX() - GUIContainer.getInstance().getDesignPanel().getLocationOnScreen().getX());
            int currentPosY = (int) (MouseInfo.getPointerInfo().getLocation().getY() - GUIContainer.getInstance().getDesignPanel().getLocationOnScreen().getY());
            GUIEvents.getInstance().resetAndShowCablePreview(currentPosX, currentPosY);
        }
    }
    
    public void addToContentPane(Component comp, Object constraints) {
        if (comp != null) {
            getContentPane().add(comp, constraints);
        }
    }

    public void removeFromContentPane(Component comp) {
        if (comp != null) {
            getContentPane().remove(comp);
        }
    }
    
	/**
     * <b>propertyChange</b> is called whenever a change in the host must be reflected by the user interface. 
     *     
     */
	public void propertyChange(PropertyChangeEvent evt) { 
		
		String pn = evt.getPropertyName();
		
		if (pn.equals("statusAndName")) {
			
			// Update the title of the main window		
			// From: ProjectManager
			updateTitle();
		}		
	};
}
