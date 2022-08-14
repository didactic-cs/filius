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
package filius.gui.netzwerksicht.config;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.event.MouseInputAdapter;

import filius.gui.JBackgroundPanel;
import filius.gui.JMainFrame;
import filius.hardware.Cable;
import filius.hardware.Hardware;
import filius.hardware.knoten.Host;
import filius.hardware.knoten.Modem;
import filius.hardware.knoten.Router;
import filius.hardware.knoten.Switch;

@SuppressWarnings("serial")
public class JConfigPanel extends JBackgroundPanel {
	
	// Fixed height of the configuration panel
    private static final int PANEL_HEIGHT = 160;
	
    // Keep one instance of each configuration panel type
    protected static JConfigPanel    configEmpty  = null;
    protected static JConfigCable    configCable  = null; 
    protected static JConfigHost     configHost   = null;
    protected static JConfigSwitch   configSwitch = null;
    protected static JConfigRouter   configRouter = null;
    protected static JConfigModem    configModem  = null;       
    
    // Reference to the node or cable
    private Hardware hardware;          
    private JLabel minMaxButton; 
    private JBackgroundPanel mainPanel;      
 
    
    /**
     * <b>select</b> returns the configuration panel corresponding to the given hardware.
     * There is only one instance of the panel for each type of node or cable.
     * The hardware is attached to the panel.
     * 
     */
    public static JConfigPanel select(Hardware hardware) {
    	
    	if (hardware == null) {
    		if (configEmpty == null) configEmpty = new JConfigPanel();
    		return configEmpty;

    	} else if (hardware instanceof Cable) {
    		if (configCable == null) configCable = new JConfigCable();
    		configCable.setHardware(hardware); 
    		return configCable;

    	} else if (hardware instanceof Host) {
    		if (configHost == null) configHost = new JConfigHost();
    		configHost.setHardware(hardware); 
    		return configHost;
    		
    	} else if (hardware instanceof Switch) {
    		if (configSwitch == null) configSwitch = new JConfigSwitch();
    		configSwitch.setHardware(hardware); 
    		return configSwitch;
    		
    	} else if (hardware instanceof Router) {
    		if (configRouter == null) configRouter = new JConfigRouter();
    		configRouter.setHardware(hardware); 
    		return configRouter;
    		
    	} else if (hardware instanceof Modem) {
    		if (configModem == null) configModem = new JConfigModem();
    		configModem.setHardware(hardware); 
    		return configModem;  
    		
    	} else return null;    	
    }
    
    /**
     * <b>selectEmptyPanel</b> returns a empty configuration panel. 
     * 
     */
    public static JConfigPanel selectEmptyPanel() {
        
        return select(null);        
    }
    
    //------------------------------------------------------------------------------------------
    // Non-static methods
    //------------------------------------------------------------------------------------------
    
    protected JConfigPanel() {       

    	initPanel();    
    }  

    public Hardware getHardware() {
        return hardware;
    }
    
    protected void setHardware(Hardware hardware) {
    	// Required when switching from one hardware to another of the same type, 
    	// to not lose the last parameter change
    	if (this.hardware != null)  this.saveChanges();
    	
        this.hardware = hardware;
    }

    /**
     * <b>initPanel</b> initializes the configuration panel and its minimize/maximize button.
     * The initConfigBox method is called to let each subclass fill the panel with its specific
     * items.
     * 
     */
    private final void initPanel() {
    	
        int containerWidth = JMainFrame.getInstance().getContentPane().getWidth();

        setLayout(new BorderLayout());
        setBounds(0, 0, containerWidth, 100);
        setEnabled(false);
        setBackgroundImage("gfx/allgemein/konfigPanel_hg.png");
        setPreferredSize(new Dimension(100, PANEL_HEIGHT));        

        // The minimize/maximize button (white on black arrow on top of the panel)
        minMaxButton = new JLabel(new ImageIcon(getClass().getResource("/gfx/allgemein/minimieren.png")));
        minMaxButton.setBounds(0, 0, minMaxButton.getIcon().getIconWidth(), minMaxButton.getIcon().getIconHeight());
        minMaxButton.addMouseListener(new MouseInputAdapter() {
            public void mousePressed(MouseEvent e) {
            	if (getHeight() > 20)  minimize();
            	else                   maximize();    
            }
        });
        add(minMaxButton, BorderLayout.NORTH);
        
        // The main background panel
        mainPanel = new JBackgroundPanel();
        mainPanel.setLayout(null);
        mainPanel.setBackgroundImage("gfx/allgemein/konfigPanel_hg.png");
        mainPanel.setOpaque(false);
        mainPanel.setVisible(true);
        mainPanel.setBounds(0, 0, containerWidth, 300);
        add(new JScrollPane(mainPanel), BorderLayout.CENTER);        

        // Let each control panel create its specific content
        initMainPanel(mainPanel);     
        
        mainPanel.updateUI();
        mainPanel.invalidate();
        mainPanel.validate();        
        
    	minimize();
    }    
   
    /**
     * <b>initMainPanel</b> creates the content of the configuration panel, specific to
     * a particular type of node or cable. No value is set in this method.
     * Subclasses should override this method.
     * 
     * @param mainPanel The panel in which the items are to be placed.
     */
    protected void initMainPanel(JBackgroundPanel mainPanel) {}        
    
    // method for doing postprocessing prior to being unselected (also in sub-classes)
    public void doUnselectAction() {}

    public void minimize() {
        setPreferredSize(new Dimension(getWidth(), 20));
        minMaxButton.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/maximieren.png")));
        mainPanel.setVisible(false);
        updateUI();
    }
    
    public void maximize() {
        setPreferredSize(new Dimension(getWidth(), PANEL_HEIGHT));
        minMaxButton.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/minimieren.png")));
        mainPanel.setVisible(true);
        updateUI();
    }

    public boolean isMaximized() {
        return mainPanel.isVisible();
    }
    
    /**
     * <b>updateDisplayedValues</b> updates all the values of the components of the config 
     * panel to reflect the corresponding hardware parameters.
     * 
     */
    public void updateDisplayedValues() {}
    
    /**
     * <b>saveChanges</b> is called automatically when an item of the config panel loses 
     * focus to update the associated hardware according to its value.
     * 
     */
    public void saveChanges() {}  
}
