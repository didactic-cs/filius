package filius.gui.modes.simulation.desktop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import filius.Main;
import filius.auxiliary.I18n;
import filius.auxiliary.Information;
import filius.auxiliary.ResourceUtil;

public class ApplicationStore implements I18n {

	public static String APP_NAME           = "Anwendung";
	public static String APP_CLASS_NAME     = "Klasse";
	public static String APP_GUI_CLASS_NAME = "GUI-Klasse";
	public static String APP_ICON_FILENAME  = "gfxFile";
	public static String APP_CATEGORY       = "Category";	
    
	private static ApplicationStore instance = null;
    private List<HashMap<String, String>> installableSoftwareList = null;
    
    
    public static ApplicationStore getInstance() {
    	    	
        if (instance == null) { instance = new ApplicationStore(); }
        return instance;
    }
    
    private void loadInstallableSoftwares() {
    	
    	installableSoftwareList = new LinkedList<HashMap<String, String>>();
    	
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(getAppFilepath()), Charset.forName("UTF-8")))) {
        	
            for (String line; (line = reader.readLine()) != null;) {
                if (!line.trim().startsWith("#") && !line.trim().equals("")) {
                    HashMap<String, String> tmpMap = new HashMap<String, String>();
                    StringTokenizer st = new StringTokenizer(line, ";");

                    tmpMap.put(APP_NAME, st.nextToken());           // Title of the application (localized)
                    tmpMap.put(APP_CLASS_NAME, st.nextToken());     // Java class of the application
                    tmpMap.put(APP_GUI_CLASS_NAME, st.nextToken()); // Java class of the application's window
                    String path = st.nextToken();
                    if (!path.isEmpty() && path.charAt(0) != '/')  path = "/" + path;
                    tmpMap.put(APP_ICON_FILENAME, path);            // Path to the icon file
                    tmpMap.put(APP_CATEGORY, st.nextToken());       // Category used for the start menu

                    installableSoftwareList.add(tmpMap);
                }
            }
        } catch (FileNotFoundException e) {
			e.printStackTrace(Main.debug);
		} catch (IOException e) {
			e.printStackTrace(Main.debug);
		}

        installableSoftwareList.addAll(loadCustomInstallableSoftwares());
    }
    
    public LinkedList<HashMap<String, String>> loadCustomInstallableSoftwares() {
    	
        LinkedList<HashMap<String, String>> tmpList;
        RandomAccessFile desktopFile = null;

        tmpList = new LinkedList<HashMap<String, String>>();
        try {desktopFile = new RandomAccessFile(Information.getInstance().getApplicationsPath() + "EigeneAnwendungen.txt", "r");
            
            for (String line; (line = desktopFile.readLine()) != null;) {
                HashMap<String, String> tmpMap = new HashMap<String, String>();
                if (!line.trim().equals("")) {
                    StringTokenizer st = new StringTokenizer(line, ";");

                    tmpMap.put(APP_NAME, st.nextToken());
                    tmpMap.put(APP_CLASS_NAME, st.nextToken());
                    tmpMap.put(APP_GUI_CLASS_NAME, st.nextToken());              
                    String path = st.nextToken();
                    if (!path.isEmpty() && path.charAt(0) != '/')  path = "/" + path;
                    tmpMap.put(APP_ICON_FILENAME, path);
                    try {
                        tmpMap.put(APP_CATEGORY, st.nextToken());
                    }
                    catch (NoSuchElementException e) {
                    	// The last token (category) is not present in old projects 
                    	// Category is used for the start menu of the desktop
                    	// "-" means that the shortcut should be located at the root of the start menu
                    	tmpMap.put(APP_CATEGORY, "-");
                    };                    
                    tmpList.add(tmpMap);
                }
            }

        }
        catch (FileNotFoundException e) {}
        catch (IOException e) {}
        finally {
            if (desktopFile != null) {
            	try {
					desktopFile.close();
				}
            	catch (IOException e) {}
            }				
        }

        return tmpList;
    }
    
    public List<HashMap<String, String>> getInstallableSoftwareList() {
    	
    	if (installableSoftwareList == null)  loadInstallableSoftwares();
    	return installableSoftwareList;
    }
    
    /** <b>getAppInfo</b> returns the application's entry corresponding to the class name.
     *  The return value is a HashMap with the following keys:<br>
     *  <b>Anwendung</b>: The displayed name of the application (localized)<br>
     *  <b>Klasse</b>: The Java class of the application<br>
     *  <b>GUI-Klasse</b>: The Java class of the application's window<br>
     *  <b>gfxFile</b>: The path and file name to the application's icon
     *  <b>Category</b>: Category used to group apps in the Startmenu
     * @param appClassName
     * @return
     */
    public HashMap<String, String> getAppInfo(String appClassName) {
    	
    	if (getInstallableSoftwareList() == null)  return null;
    	
    	for (HashMap<String, String> appInfo: installableSoftwareList) {
    		if (appInfo.get(APP_CLASS_NAME).equals(appClassName))  return appInfo;
    	}
    	return null;
    }
    
    public String getAppName(String appClassName) {
    	
    	HashMap<String, String> appInfo = getAppInfo(appClassName);
    	return (appInfo != null ? appInfo.get(APP_NAME) : "");
    }
    
    private String getAppFilepath() {
    	
    	String langDir = messages.getString("gui_language");  // de, en, or fr
		
        File desktopResource = ResourceUtil.getFileResource("config/" + langDir + "/Desktop.txt");
        return desktopResource.getAbsolutePath();
    }
}
