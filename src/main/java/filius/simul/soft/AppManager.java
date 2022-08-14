package filius.gui.modes.simulation.desktop;

import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.Map;

import filius.Main;
import filius.auxiliary.FiliusClassLoader;
import filius.auxiliary.I18n;
import filius.gui.modes.simulation.windows.GUIAppMainWindow;
import filius.system.file.FiliusFileNode;
import filius.system.software.HostOS;

public class ApplicationManager implements I18n {
	
	private HostOS hostOS;
	private JDesktop desktop;
	private HashMap<String, GUIAppMainWindow> runningApps = new HashMap<String, GUIAppMainWindow>();
	private GUIInstallationsDialog installationsDialog = null;
	
	public ApplicationManager(HostOS hostOS, JDesktop desktop) {		
		super();
		this.hostOS = hostOS;
		this.desktop = desktop;
	}
	
    // Instantiate the GUIApplicationWindow corresponding to the class name of the application 
    public GUIAppMainWindow createAppWindow(String appName, String appClassName, String appGUIClassName) {
    	
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
        	GUIAppMainWindow appWindow = 
        		(GUIAppMainWindow) cl.getConstructor(JDesktop.class, String.class, String.class).newInstance(this, appClassName, appName);
        	return appWindow;
        } catch (Exception e) {
        	e.printStackTrace(Main.debug);
        }
        
        return null;
    }
	
    /** 
     * <b>startApp</b> start the application whose className is given and return it.<br>
     * If the application is already running, just return it.
     * 
     * @param appClassName the name of the application's class to be started
     * @param param An array of strings or null. These can be parameters to pass to the application, like a file to open.
     * @return A GUIAppMainWindow instance
     **/
    public GUIAppMainWindow startApp(String appClassName, FiliusFileNode node, String[] param) {    	

        if (appClassName.equals("Software-Installation")) {
        	
            installationsDialog = new GUIInstallationsDialog(this);
            desktop.getDesktopPane().add(installationsDialog, 3);

            try {
                installationsDialog.setSelected(true);
            } catch (PropertyVetoException e) {
                e.printStackTrace(Main.debug);
            }            
            return null;
        } else {        
        	
        	GUIAppMainWindow appWindow = getRunningApp(appClassName);
        	
        	if (appWindow != null) {        		
        		
        		appWindow.start(node, param);
        		appWindow.updateUI();
        		appWindow.show();
        		
        		desktop.addAppButtonToTaskbar(appWindow.getAppName(), appWindow.getFrameIcon());
        	}
        	return appWindow;
        }
    }
    
    public GUIAppMainWindow startApp(String appClassName) {

        return startApp(appClassName, null, null);
    }
    
    public GUIAppMainWindow startApp(String appClassName, FiliusFileNode node) {  
    	
    	return startApp(appClassName, node, null);
    }
    
    /** <b>openNode</b> opens a file (or directory) associated to a node in the corresponding 
     *  application. The application must be installed.
     * 
     * @param node FiliusFileNode corresponding to the file/directory
     * @return GUIAppMainWindow instance of the application or null
     */
    public GUIAppMainWindow openNode(FiliusFileNode node) {

    	String appClassName = "";

    	switch (node.getType()) {

    	case DIRECTORY:
    		appClassName = "filius.software.apps.fileexplorer.FileExplorer";
    		break;

    	case TEXT:
    	case CSS:
    	case XML:
    		appClassName = "filius.software.apps.texteditor.TextEditor";
    		break;

    	case HTML:
    		appClassName = "filius.software.clientserver.web.WebBrowser";
    		break;

    	case IMAGE_JPG:
    	case IMAGE_PNG:
    	case IMAGE_GIF:
    	case IMAGE_BMP:
    		appClassName = "filius.software.apps.imageviewer.ImageViewer";
    		break;

    	default: appClassName = "";
    	}
    	
    	if (appClassName.isEmpty()) {
    		desktop.warningDialog(messages.getString("gui_desktoppanel_msg4"), messages.getString("gui_desktoppanel_msg3")); // Unknown file type
    		return null;
    	}
    		
    	GUIAppMainWindow appWindow = startApp(appClassName, node);

    	if (appWindow != null)  appWindow.setFocus();
    	else                    desktop.warningDialog(messages.getString("gui_desktoppanel_msg5"), messages.getString("gui_desktoppanel_msg3")); // Application not installed
    	
    	return appWindow;
    }
    
    /** <b>openFile</b> opens a file (or directory) in the corresponding 
     *  application. The application must be installed.
     * 
     * @param filepath String corresponding to the path to the file/directory
     * @return GUIAppMainWindow instance of the application or null
     */
    public GUIAppMainWindow openFile(String filepath) {
    	
    	FiliusFileNode node = hostOS.getFileSystem().getNode(filepath);
    	if (node == null)  return null;
    	
    	return openNode(node);
    }
    
    /**
     * Fuegt der Hashmap laufendeAnwendung das Fenster der laufenden Anwendung hinzu, damit Fenster geschlossen und
     * wieder geoeffnet werden koennen, ohne die Anwendung dafuer neu starten zu muessen.
     * 
     * @author Thomas Gerding & Johannes Bade
     * @param appWindow
     *            Das GUIApplicationWindow der Anwendung
     * @param appClassName
     *            Name der Anwendung
     */
    public void addRunningApp(String appClassName, GUIAppMainWindow appWindow) {
    	
        runningApps.put(appClassName, appWindow);
    }

    /**
     * Gibt das GUIApplicationWindow einer Anwendung aus der HashMap laufendeAnwendung zurueck.
     * 
     * @param appClassName
     * @return Das GUIApplicationWindow der angeforderten Anwendung
     */
    private GUIAppMainWindow getRunningApp(String appClassName) {

        return runningApps.get(appClassName);
    }
    
    public GUIAppMainWindow getRunningAppFromName(String appName) {

    	for (Map.Entry<String,GUIAppMainWindow> me : runningApps.entrySet()) {
    		GUIAppMainWindow appWindow = me.getValue();
            if (appWindow.getAppName().equals(appName)) return appWindow;
    	}    	
    	return null;
    }
    
    

}
