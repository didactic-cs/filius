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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.MouseInputAdapter;
import filius.Main;
import filius.gui.JBackgroundPanel;
import filius.hardware.NetworkInterface;
import filius.hardware.knoten.InternetNode;
import filius.rahmenprogramm.FiliusClassLoader;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.Information;
import filius.software.Application;
import filius.software.system.FiliusFileNode;
import filius.software.system.HostOS;

@SuppressWarnings("serial")
public class GUIDesktopPanel extends JBackgroundPanel implements I18n, PropertyChangeListener {

    private HostOS hostOS;    
    private JDesktopPane desktopPane = null;
    private JPanel iconPanel = null;
    private JPanel taskbar;
    private JLabel networkIcon;
    private GUINetworkInfo networkInfo;
    
    private GUIInstallationsDialog installationsDialog = null;    
    private HashMap<String, GUIApplicationWindow> runningApps = new HashMap<String, GUIApplicationWindow>();
   

    public GUIDesktopPanel(HostOS hostOS) {
    	
        super();
        setLayout(null);
        setPreferredSize(new Dimension(640, 480));     
        setBackgroundImage("gfx/desktop/hintergrundbild.png");                                                                       
        setVisible(true);
        setLayout(new BorderLayout());

        this.hostOS = hostOS;      
        
        // Desktop pane
        desktopPane = new JDesktopPane();
        desktopPane.setOpaque(false);
        add(desktopPane, BorderLayout.CENTER);        

        // Panel used to hold the icons shown on the desktop
        iconPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 10));
        iconPanel.setBounds(0, 0, 640, 432);
        iconPanel.setOpaque(false);        
        desktopPane.add(iconPanel);
        
        desktopPane.validate();

        // Taskbar
        taskbar = new JPanel();
        taskbar.setLayout(null);
        taskbar.setPreferredSize(new Dimension(640, 22)); 
        taskbar.setBorder(BorderFactory.createEmptyBorder());    
        add(taskbar, BorderLayout.SOUTH);

        // Network icon
        networkIcon = new JLabel(new ImageIcon(getClass().getResource("/gfx/desktop/netzwek_aus.png")));
        networkIcon.setBounds(618, 2, 18, 18);
        networkIcon.setVisible(true);
        networkIcon.addMouseListener(new MouseInputAdapter() {
            public void mousePressed(MouseEvent e) { showNetworkInfo(); }
        });
        taskbar.add(networkIcon); 

        // Initialize the IP/mask value of the network icon tooltip
        updateNetworkIconToolTip();

        // Listeners for the blinking of the network icon and the change of the IP/mask values     
        registerListeners();   

        // Initialize the applications icons on the desktop
        updateIconPanel();
    }

    // Called when the desktop is created and also every time softwares are installed or uninstalled
    public void updateIconPanel() {
       
        iconPanel.removeAll();
        
        // Add the icon of the installer application
        iconPanel.add(createIcon(messages.getString("desktoppanel_msg1"), "Software-Installation", "/gfx/desktop/icon_softwareinstallation.png"));
        
        // Add the icon of the installed applications        
        for (Entry<String, Application> entry : hostOS.getInstalledApps().entrySet()) {

        	HashMap<String, String> appInfo = Information.getInstance().getAppInfo(entry.getKey());         
            
            if (appInfo != null) {
            	
            	GUIApplicationWindow appWindow = createAppWindow(appInfo.get("Klasse"), appInfo.get("GUI-Klasse"));

            	if (appWindow != null) {
            		appWindow.setVisible(false);                        
            		addRunningApp(appInfo.get("Klasse"), appWindow);

            		iconPanel.add(createIcon(appInfo.get("Anwendung"), appInfo.get("Klasse"), appInfo.get("gfxFile")));
            	}      
            };   
        } 
        
        iconPanel.updateUI();
    }
    
    // Instantiate the GUIApplicationWindow corresponding to the class name of the application 
    private GUIApplicationWindow createAppWindow(String appClassName, String appGUIClassName) {
    	
    	// Retrieve the class
    	Class<?> cl = null;
        try {
            cl = Class.forName(appGUIClassName, true, FiliusClassLoader.getInstance(Thread.currentThread().getContextClassLoader()));
        } catch (ClassNotFoundException e) {
            e.printStackTrace(Main.debug);
        }
        if (cl == null) return null;

        // Instantiate the class
        try {
        	GUIApplicationWindow appWindow = 
        		(GUIApplicationWindow) cl.getConstructor(GUIDesktopPanel.class, String.class).newInstance(this, appClassName);      
        	return appWindow;
        } catch (Exception e) {
        	e.printStackTrace(Main.debug);
        }
        
        return null;
    }
    
    private GUIDesktopIcon  createIcon(String appName, String appClassName, String iconFilename) {
    	
    	if (!iconFilename.isEmpty() && iconFilename.charAt(0)!='/') iconFilename = "/" + iconFilename;
        GUIDesktopIcon icon = new GUIDesktopIcon(this, appClassName, new ImageIcon(getClass().getResource(iconFilename)));
           
        icon.setText(appName);
        icon.setToolTipText(appName);
        
        icon.setVerticalTextPosition(SwingConstants.BOTTOM);
        icon.setHorizontalTextPosition(SwingConstants.CENTER);
        icon.setForeground(new Color(255, 255, 255));
        icon.setPreferredSize(new Dimension(123, 96));
        
        return icon;
    }

    /** 
     * <b>startApp</b> starts the application the className of which is given.<br>
     * 
     * @param appClassName the name of the application's class to be started
     * @param param An array of strings or null. These can be parameters to pass to the application, like a file to open.
     * */
    public GUIApplicationWindow startApp(String appClassName, FiliusFileNode node, String[] param) {    	

        if (appClassName.equals("Software-Installation")) {
        	
            installationsDialog = new GUIInstallationsDialog(this);
            getDesktopPane().add(installationsDialog, 3);

            try {
                installationsDialog.setSelected(true);
            } catch (PropertyVetoException e) {
                e.printStackTrace(Main.debug);
            }            
            return null;
        } else {        
        	
        	GUIApplicationWindow appWindow = getRunningApp(appClassName);
        	
        	if (appWindow != null) {        		
        		
        		appWindow.start(node, param);
        		appWindow.updateUI();
        		appWindow.show();
        		return appWindow;
        	}
        }
        return null;
    }
    
    public GUIApplicationWindow startApp(String appClassName) {

        return startApp(appClassName, null, null);
    }
    
    public GUIApplicationWindow startApp(String appClassName, FiliusFileNode node) {  
    	
    	return startApp(appClassName, node, null);
    }    

    /**
     * Fuegt der Hashmap laufendeAnwendung das Fenster der laufenden Anwendung hinzu, damit Fenster geschlossen und
     * wieder geoeffnet werden koennen, ohne die Anwendung dafuer neu starten zu muessen.
     * 
     * @author Thomas Gerding & Johannes Bade
     * @param appWindow
     *            Das GUIApplicationWindow der Anwendung
     * @param appName
     *            Name der Anwendung
     */
    private void addRunningApp(String appName, GUIApplicationWindow appWindow) {
    	
        runningApps.put(appName, appWindow);
    }

    /**
     * Gibt das GUIApplicationWindow einer Anwendung aus der HashMap laufendeAnwendung zurueck.
     * 
     * @param appName
     * @return Das GUIApplicationWindow der angeforderten Anwendung
     */
    private GUIApplicationWindow getRunningApp(String appName) {

        return (GUIApplicationWindow) runningApps.get(appName);
    }

    public filius.software.system.HostOS getOS() {
        return hostOS;
    }

    public JDesktopPane getDesktopPane() {
    	
        return desktopPane;
    }
    
    public void addFrame(JInternalFrame frame) {
    	
    	desktopPane.add(frame);
    }

    public void removeFrame(JInternalFrame frame) {
    	
    	desktopPane.remove(frame);
    }  
    
    private void showNetworkInfo() {
    	
    	if (networkInfo == null) networkInfo = new GUINetworkInfo(this);
    	if (networkInfo == null) return;
    	
    	networkInfo.showAndSelect();
    }    
    
    /**
     * <b>updateNetworkIcon</b> sets the icon to reflect the trafic on the network connection of the host.
     * 
     * @param active Boolean which is true when there is some trafic and false otherwise.
     */
    public void updateNetworkIcon (boolean active) {

    	String iconFilename;
        if (active)  iconFilename = "/gfx/desktop/netzwek_c.png";
        else         iconFilename = "/gfx/desktop/netzwek_aus.png";     
        networkIcon.setIcon(new ImageIcon(getClass().getResource(iconFilename)));
    }    
    
    public void updateNetworkIconToolTip() {
    	
    	NetworkInterface nic = (NetworkInterface) ((InternetNode) hostOS.getNode()).getNICList().get(0);   
        
        if (nic != null) networkIcon.setToolTipText(nic.getIp()+"/"+nic.getSubnetMask());        	
        else             networkIcon.setToolTipText("???");        
    }
    
    private void registerListeners() {
    	
    	// What is this used for?
    	//hostOS.addPropertyChangeListener("UI", this);	
    	
    	// Blinking of the network icon
        NetworkInterface nic = (NetworkInterface) ((InternetNode) hostOS.getNode()).getNICList().get(0);   
        
        if (nic != null && nic.getPort() != null && nic.getPort().getCable() != null) {
        	
        	nic.getPort().getCable().addPropertyChangeListener("cableactivity", this);
        }
        
        // Change in the IP address
        hostOS.addPropertyChangeListener("ipaddress", this);
    }
        
	/**
     * <b>propertyChange</b> is called whenever a change in the host must be reflected by the user interface. 
     *     
     */
	public void propertyChange(PropertyChangeEvent evt) {
		
		String pn = evt.getPropertyName();
		
//		if (pn.equals("UI")) {                         
//			// Update the applications of the host
//			// From SystemSoftware (for what reason?)
//			updateApplications();
//		} else
		if (pn.equals("cableactivity")) {           
			// Update the network icon of the desktop
			// From Cable
			updateNetworkIcon ((Boolean) evt.getNewValue());
		} 
		else if (pn.equals("ipaddress")) {           
			// Update the network icon of the desktop
			// From Node, InternetNodeOS
			updateNetworkIconToolTip();
		}
		
	};
}
