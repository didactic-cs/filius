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
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
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
import filius.software.system.HostOS;

@SuppressWarnings("serial")
public class GUIDesktopPanel extends JBackgroundPanel implements I18n, PropertyChangeListener {

    private HostOS hostOS;

    private HashMap<String, GUIApplicationWindow> laufendeAnwendung = new HashMap<String, GUIApplicationWindow>();

    private JBackgroundDesktopPane desktopPane = null;

    private JPanel iconPanel = null;

    private JPanel taskLeiste;

    private JLabel lbNetzwerk;

    private GUIInstallationsDialog installationsDialog = null;
    private GUINetworkWindow gnw;

    private String[] parameter = { "", "", "" };
    

    public GUIDesktopPanel(HostOS hostOS) {
        super();
        setLayout(null);
        setPreferredSize(new Dimension(640, 480));
        setBounds(0, 0, 640, 480);
        setBackgroundImage("gfx/desktop/hintergrundbild.png");
        setVisible(true);
        setLayout(new BorderLayout());

        this.hostOS = hostOS;      

        desktopPane = new JBackgroundDesktopPane();
        desktopPane.setBackgroundImage("gfx/desktop/hintergrundbild.png");

        add(desktopPane, BorderLayout.CENTER);

        iconPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 10));
        iconPanel.setBounds(0, 0, 640, 432);
        iconPanel.setOpaque(false);

        taskLeiste = new JPanel();

        taskLeiste.setBorder(BorderFactory.createEmptyBorder());
        Box boxTaskLeiste = Box.createHorizontalBox();
        boxTaskLeiste.setBorder(BorderFactory.createEmptyBorder());
        boxTaskLeiste.add(Box.createHorizontalStrut(600));

        gnw = new GUINetworkWindow(this);

        lbNetzwerk = new JLabel(new ImageIcon(getClass().getResource("/gfx/desktop/netzwek_aus.png")));
        lbNetzwerk.addMouseListener(new MouseInputAdapter() {

            public void mousePressed(MouseEvent e) {
                {
                    gnw.setVisible(true);
                    try {
                        gnw.setSelected(true);
                        gnw.toFront();
                    } catch (PropertyVetoException e1) {
                        e1.printStackTrace(Main.debug);
                    }
                }

            }
        });

        boxTaskLeiste.add(lbNetzwerk);

        taskLeiste.add(boxTaskLeiste);

        add(taskLeiste, BorderLayout.SOUTH);
        desktopPane.add(iconPanel);
        desktopPane.validate();
        updateApplications();
    }

    public void updateApplications() {
    	
        GUIDesktopIcon tmpLabel;
        List<HashMap<String, String>> softwareList = null;
        String softwareKlasse, guiKlassenName;
        HashMap<String, String> tmpMap;
        Class<?> cl = null;
        GUIApplicationWindow tempWindow;

        try {
            softwareList = Information.getInstance().ladeProgrammListe();
        } catch (IOException e) {
            e.printStackTrace(Main.debug);
        }
        this.iconPanel.removeAll();

        tmpLabel = new GUIDesktopIcon(new ImageIcon(getClass().getResource("/gfx/desktop/icon_softwareinstallation.png")));
        tmpLabel.setAnwendungsName(messages.getString("desktoppanel_msg1"));
        tmpLabel.setInvokeName("Software-Installation");
        tmpLabel.setToolTipText(tmpLabel.getAnwendungsName());
        tmpLabel.setText(tmpLabel.getAnwendungsName());
        tmpLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
        tmpLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        tmpLabel.setForeground(new Color(255, 255, 255));
        tmpLabel.setPreferredSize(new Dimension(120, 96));
        this.iconPanel.add(tmpLabel);

        ListIterator<HashMap<String, String>> it = ((softwareList != null) ? softwareList.listIterator() : null);
        while (it != null && it.hasNext()) {

            tmpMap = it.next();
            softwareKlasse = tmpMap.get("Klasse");
            if ((hostOS.getSoftware(softwareKlasse) != null)) {
                if (softwareKlasse.equals(tmpMap.get("Klasse"))) {
                    guiKlassenName = tmpMap.get("GUI-Klasse");

                    try {
                        cl = Class.forName(guiKlassenName, true,
                                FiliusClassLoader.getInstance(Thread.currentThread().getContextClassLoader()));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace(Main.debug);
                    }

                    try {

                        if (cl != null) {
                            tempWindow = (GUIApplicationWindow) cl.getConstructor(GUIDesktopPanel.class, String.class)
                                    .newInstance(this, softwareKlasse);

                            tempWindow.setVisible(false);

                            addLaufendeAnwendung(softwareKlasse, tempWindow);

                            tmpLabel = new GUIDesktopIcon(new ImageIcon(getClass().getResource(
                                    "/" + (tmpMap.get("gfxFile")))));

                            tmpLabel.setAnwendungsName(tmpMap.get("Anwendung"));
                            tmpLabel.setInvokeName(tmpMap.get("Klasse"));
                            tmpLabel.setToolTipText(tmpLabel.getAnwendungsName());
                            tmpLabel.setText(tmpLabel.getAnwendungsName());
                            tmpLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
                            tmpLabel.setHorizontalTextPosition(SwingConstants.CENTER);
                            tmpLabel.setForeground(new Color(255, 255, 255));
                            tmpLabel.setPreferredSize(new Dimension(120, 96));
                            this.iconPanel.add(tmpLabel);
                        }
                    } catch (Exception e) {
                        e.printStackTrace(Main.debug);
                    }
                }
            }
        }
        this.iconPanel.updateUI();
            
        // Initialize the IP/mask value in the tooltip
        updateNetworkIconToolTip();
        
        // Listeners for the blinking of the network icon and the change of the IP/mask values     
        registerListeners();      

      
        if (this.getParent() != null) {
            taskLeiste.setBounds(0, 424, 640, 32 + this.getParent().getInsets().top);
        }
    }

    public GUIApplicationWindow startApplication(String softwareClass, String[] param) {
        setParameter(param);
        return startApplication(softwareClass);
    }

    public GUIApplicationWindow startApplication(String softwareClass) {
        GUIApplicationWindow tempWindow = null;

        if (softwareClass.equals("Software-Installation")) {
            this.installationsDialog = new GUIInstallationsDialog(this);
            getDesktopPane().add(this.installationsDialog, 3);

            try {
                this.installationsDialog.setSelected(true);

            } catch (PropertyVetoException e) {
                e.printStackTrace(Main.debug);
            }
        }

        else if (getLaufendeAnwendungByName(softwareClass) != null) {
            tempWindow = getLaufendeAnwendungByName(softwareClass);

            tempWindow.updateUI();
            tempWindow.start(parameter);
            tempWindow.show();

        }

        return tempWindow;
    }

    /**
     * Fuegt der Hashmap laufendeAnwendung das Fenster der laufenden Anwendung hinzu, damit Fenster geschlossen und
     * wieder geoeffnet werden koennen, ohne die Anwendung dafuer neu starten zu muessen.
     * 
     * @author Thomas Gerding & Johannes Bade
     * @param fenster
     *            Das GUIApplicationWindow der Anwendung
     * @param anwendungsName
     *            Name der Anwendung
     */
    private void addLaufendeAnwendung(String anwendungsName, GUIApplicationWindow fenster) {
    	
        this.laufendeAnwendung.put(anwendungsName, fenster);
    }

    /**
     * Gibt das GUIApplicationWindow einer Anwendung aus der HashMap laufendeAnwendung zurueck.
     * 
     * @param anwendungsName
     * @return Das GUIApplicationWindow der angeforderten Anwendung
     */
    private GUIApplicationWindow getLaufendeAnwendungByName(String anwendungsName) {
    	
        GUIApplicationWindow tmpFenster = null;

        tmpFenster = (GUIApplicationWindow) this.laufendeAnwendung.get(anwendungsName);

        return tmpFenster;
    }

    /*
     * public LinkedList getIconListe() { return iconListe; }
     */

    public filius.software.system.HostOS getOS() {
        return hostOS;
    }

    public JDesktopPane getDesktopPane() {
    	
        return desktopPane;
    }

    public String[] getParameter() {
    	
        return parameter;
    }

    public void setParameter(String[] parameter) {
    	
        this.parameter = parameter;
    }
    
    public void updateNetworkIconToolTip() {
    	
    	NetworkInterface nic = (NetworkInterface) ((InternetNode) hostOS.getNode()).getNICList().get(0);   
        
        if (nic != null) lbNetzwerk.setToolTipText(nic.getIp()+"/"+nic.getSubnetMask());        	
        else             lbNetzwerk.setToolTipText("???");        
    }
    
    /**
     * <b>setNetworkStatusIcon</b> sets the icon to reflect the trafic on the network connection of the host.
     * 
     * @param active Boolean which is true when there is some trafic and false otherwise.
     */
    public void setCableActivity (boolean active) {

        if (active) {
            lbNetzwerk.setIcon(new ImageIcon(getClass().getResource("/gfx/desktop/netzwek_c.png")));
        } else {
            lbNetzwerk.setIcon(new ImageIcon(getClass().getResource("/gfx/desktop/netzwek_aus.png")));
        }
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
			setCableActivity ((Boolean) evt.getNewValue());
		} 
		else if (pn.equals("ipaddress")) {           
			// Update the network icon of the desktop
			// From Node, InternetNodeOS
			updateNetworkIconToolTip();
		}
		
	};
}
