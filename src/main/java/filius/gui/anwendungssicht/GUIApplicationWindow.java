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

import java.awt.Dimension;
import java.awt.Image;
import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;

import filius.Main;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.Information;
import filius.software.Application;
import filius.software.system.FiliusFileNode;

/**
 * Diese Klasse dient als Oberklasse für alle Anwendungsfenster
 * 
 */
@SuppressWarnings("serial")
public abstract class GUIApplicationWindow extends JInternalFrame implements I18n, Observer {

    private GUIDesktopPanel desktop;
    private Application application;    
    private ImageIcon icon;

    
    public GUIApplicationWindow(GUIDesktopPanel desktop, String appClassName) {
        super();
        setPreferredSize(new Dimension(450, 350)); 
        setMinimumSize(new Dimension(320, 240)); 
        setClosable(true);
        setMaximizable(true);
        setIconifiable(false);
        setResizable(true);
        
        setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);

        // Add to the desktop
        this.desktop = desktop;
        desktop.addFrame(this); 

        // Bind this window and its application 
        application = desktop.getOS().getApp(appClassName);        
        
        // Notification from the application
        application.addObserver(this);           // To be replaced with Notification

        // Set title and icon
        setTitle(application.getAppName());
        initIcon(appClassName); 
    }

    private void initIcon(String appClassName) {  	
    	    	
    	HashMap<String, String> appInfo = Information.getInstance().getAppInfo(appClassName);  
    	if (appInfo == null) return;
    	
    	icon = new ImageIcon(getClass().getResource("/" + appInfo.get("gfxFile")));
    	icon.setImage(icon.getImage().getScaledInstance(16, 16, Image.SCALE_AREA_AVERAGING));       	
    	setFrameIcon(icon);
    }
    
    /** 
     * Changing this global parameter seems to be the only way to customize the title icon
     * of the JOptionPane's internal dialogs. Must be called before each call. 
     */
    private void setInternalDialogIcon() {  	
    	UIManager.put("InternalFrame.icon", icon);
    }
    
    private void resetInternalDialogIcon() {  	
    	UIManager.put("InternalFrame.icon", null);
    }

    public Application getApplication() {
        return application;
    }
    
    public void centerWindow() {    	

    	// 640x460: dimension of the desktop (without the taskbar)
    	setLocation((640 - getWidth())/2, (480 - 22 - getHeight())/2);
    }
    
    public void makeActiveWindow() {    	

    	try {
    		setSelected(true);
    		requestFocusInWindow();
    	} catch (PropertyVetoException e) {
    		e.printStackTrace(Main.debug);
    	}	
    }
    
    // Dialog boxes
    //--------------
    
    public void infoDialog(String message, String title) {
    	
    	setInternalDialogIcon();
    	JOptionPane.showInternalMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE); 
    	resetInternalDialogIcon();
    }

    public void warningDialog(String message, String title) {

    	setInternalDialogIcon();
    	JOptionPane.showInternalMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE); 
    	resetInternalDialogIcon();
    }
    
    public String OKCancelDialog(String message, String title) {

    	setInternalDialogIcon();
    	String s = (String) JOptionPane.showInternalInputDialog(this, message, title, JOptionPane.QUESTION_MESSAGE);  
    	resetInternalDialogIcon();
    	return s;
    }
    
    public String OKCancelDialog(String message, String title, String defaultvalue) {    	
   	
    	setInternalDialogIcon();
    	String s = (String) JOptionPane.showInternalInputDialog(this, message, title, JOptionPane.QUESTION_MESSAGE, null, null, defaultvalue);  
    	resetInternalDialogIcon();
    	return s;
    }

    public int showConfirmDialog(String msg) {
    	
    	setInternalDialogIcon();
        int i = JOptionPane.showInternalConfirmDialog(desktop, msg);
        resetInternalDialogIcon();
        return i;
    }
    
    /**
     * yesNoDialog returns true when the user clicked on yes
     */
    public boolean yesNoDialog(String message, String title) {
    	
    	setInternalDialogIcon();
    	boolean b = (JOptionPane.showInternalConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION); 
    	resetInternalDialogIcon();
    	return b;
    }    

    public int showOptionDialog(Object message, String title, int optionType, int messageType, Icon icon,
    		                    Object[] options, Object initialValue) {

    	setInternalDialogIcon();
        int i = JOptionPane.showInternalOptionDialog(desktop, message, title, optionType, messageType, icon, options, initialValue);
        resetInternalDialogIcon();
        return i;
    }       

    public void addFrame(JInternalFrame frame) {
    	
    	desktop.addFrame(frame);
    }

    public void removeFrame(JInternalFrame frame) {

    	desktop.removeFrame(frame);
    }  

    /**
     * <b>start</b> starts the application.
     * 
     * @param node A FiliusFileNode the content of which is to be opened by the application. Can be null.
     * @param param An array of strings or null. These can be parameters to be passed to the application. Can be null.
     */
    public void start(FiliusFileNode node, String[] param) {}
    
    public void showPopupMenu(JPopupMenu menu, int x, int y) {
    	
        menu.show(desktop, x, y);
    }    
    
    @Override
    public void update(Observable arg0, Object arg1) {
    }   
}
