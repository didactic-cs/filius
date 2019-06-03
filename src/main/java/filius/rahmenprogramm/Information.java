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
package filius.rahmenprogramm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JFileChooser;

import filius.Main;
import filius.gui.GUIContainer;
import filius.gui.anwendungssicht.GUIDesktopWindow;
import filius.hardware.Verbindung;

/**
 * In dieser Klasse werden die Verwaltungs-Informationen des Rahmenprogramms verwaltet, die unabhaengig von einem
 * bestimmten Projekt sind.
 */
public class Information implements Serializable {
    public enum FeatureMode {
        FORCE_DISABLE, FORCE_ENABLE, AUTO
    }

    private static final long serialVersionUID = 1L;

    /** Zur Implementierung des Singleton */
    private static Information information = null;

    private static WinFolders winFolders = new WinFolders();

    private static boolean lowResolution = false;

    private static boolean posixCommandLineToolBehaviour = false;

    private static GUIDesktopWindow.Mode desktopWindowMode = GUIDesktopWindow.Mode.STACK;

    public static GUIDesktopWindow.Mode getDesktopWindowMode() {
        return desktopWindowMode;
    }

    public static boolean isPosixCommandLineToolBehaviour() {
        return posixCommandLineToolBehaviour;
    }

    private static String version = null;

    public static boolean isLowResolution() {
        return lowResolution;
    }

    public static void setLowResolution(boolean smallResolution) {
        Information.lowResolution = smallResolution;
    }

    public boolean initOk = false;

    /**
     * Die MAC-Adressen werden zentral verwaltet, um zu gewaehrleisten, dass keine Adresse mehrfach vergeben wird.
     */
    private Vector<String> macAdressen = new Vector<String>();

    /**
     * Die maximale Anzahl von Vermittlungsstellen wird zur Berechnung des Time-Outs fuer eine TCP-Verbindung genutzt.
     */
    private int maxVermittlungsStellen;

    /**
     * Pfad zum Verzeichnis, in dem das Programm ausgefuehrt wird (in dem sich die ausfuehrbare Jar-Datei befindet);
     * <br />
     * der Pfad schliesst mit dem Pfad-Trennzeichen (unter UNIX "/")
     */
    private String programmPfad = null;

    /**
     * Pfad zum Verzeichnis, in dem die benutzerspezifischen Daten abgelegt werden; <br />
     * der Pfad schliesst mit dem Pfad-Trennzeichen (unter UNIX "/")
     */
    public static String initArbeitsbereichPfad = getHomeDir() // System.getProperty("user.home")
            + System.getProperty("file.separator");

    // actually used working directory, i.e., path to be used after initial
    // tests
    private String arbeitsbereichPfad = getHomeDir() // System.getProperty("user.home")
            + System.getProperty("file.separator") + ".filius" + System.getProperty("file.separator");

    /** Lokalisierungsobjekt fuer Standard-Spracheinstellung */
    private Locale locale;
    private String lastOpenedDirectory;

    private boolean oldExchangeDialog = true;

    private FeatureMode softwareWizardMode = FeatureMode.AUTO;

    // private Locale locale = new Locale("en", "GB");

    public FeatureMode getSoftwareWizardMode() {
        return softwareWizardMode;
    }

    public boolean isOldExchangeDialog() {
        return oldExchangeDialog;
    }

    // ////////////////////////////
    private static String getHomeDir() {
        if (com.sun.jna.Platform.isWindows()) {
            try {
                String path = winFolders.getFolder(0x001C); // CSIDL_LOCAL_APPDATA
                return path;
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("EXCEPTION: error on using Java native access");
                return System.getProperty("user.home");
            }
        } else {
            return System.getProperty("user.home");
        }
    }

    // ////////////////////////////////////

    /**
     * ensure a correct and well functioning path to write all necessary data
     */
    private boolean checkWD(String currPath) {
        boolean nowrite = true;
        String directoryPath;
        java.util.Random rnd = new java.util.Random();
        String randomString = Long.toString(rnd.nextLong());
        java.io.File testFile = null;

        do {
            directoryPath = currPath + ".filius" + System.getProperty("file.separator");
            try {
                testFile = new java.io.File(directoryPath);
                //
                // write check, i.e., create random directory and delete it
                // again
                testFile.mkdirs();
                testFile = new java.io.File(directoryPath + randomString);
                testFile.createNewFile();
                if (!testFile.delete()) {
                    throw new Exception("EXCEPTION: Error on deleting test file in write-check");
                }

                nowrite = false;
            } catch (Exception e) {
                // open dialog to choose another directory
                javax.swing.JOptionPane.showMessageDialog(null,
                        "Fehler: Verzeichnis ist nicht schreibbar. Filius benötigt aber Schreibrechte.\n"
                                + "Bitte wählen Sie ein Verzeichnis, für das Sie Schreibrechte besitzen.\n\n"
                                + "Error: Directory is not writeable. But Filius needs write permissions.\n"
                                + "Please choose a directory where you have write permissions.",
                        "Fehler / Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (fc.showOpenDialog(new java.awt.Frame()) == JFileChooser.APPROVE_OPTION) {
                    currPath = fc.getSelectedFile().getAbsolutePath() + System.getProperty("file.separator");
                } else
                    return false;
            }
        } while (nowrite);
        arbeitsbereichPfad = currPath + ".filius" + System.getProperty("file.separator"); // set
                                                                                          // correct
                                                                                          // path
        return true;
    }

    /**
     * nicht oeffentlich zugaenglicher Konstruktor, wird aus getInformation() aufgerufen
     */
    private Information() {
        if (checkWD(initArbeitsbereichPfad)) {
            init();
            initOk = true;
        }
    }

    private Information(String path) {
        if (checkWD(path)) {
            init();
            initOk = true;
        }
    }

    /** Methode zum Zugriff auf Singleton */
    public static Information getInformation() {
        return getInformation((String) null);
    }

    public static Information getInformation(String path) {
        if (information == null) {
            if (path != null)
                information = new Information(path);
            else
                information = new Information();
        }
        if (information.initOk)
            return information;
        else
            return null;
    }

    /** aktuelle Programmversion */
    public static String getVersion() {
        if (version == null) {
            Properties properties = new Properties();
            try {
                InputStream in = Information.class.getClassLoader().getResourceAsStream("application.properties");
                properties.load(in);
                in.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            version = properties.getProperty("application.version") + " ("
                    + properties.getProperty("application.buildDate") + ")";
        }
        return version;
    }

    /**
     * Zugriff auf das sprachabhaengige Objekt zur Verwaltung der Texte.
     * 
     * @return
     */
    public ResourceBundle holeResourceBundle() {
        ResourceBundle bundle;

        bundle = ResourceBundle.getBundle("filius.messages.MessagesBundle", locale);

        return bundle;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * Zuruecksetzen aller Einstellungen, die zur Laufzeit von Filius veraendert wurden und alle Projektspezifischen
     * Daten.
     */
    public void reset() {
        macAdressen.clear();

        GUIContainer.getGUIContainer().getExchangeDialog().reset();
        init();
    }

    private void init() {
        maxVermittlungsStellen = 48;

        try {
            initialisiereVerzeichnisse();
        } catch (Exception e) {
            e.printStackTrace(Main.debug);
        }

        SzenarioVerwaltung.loescheVerzeichnisInhalt(getTempPfad());
    }

    /**
     * Hier werden die Verzeichnisse und die Datei mit den Informationen zu den eigenen Anwendungen erstellt, wenn sie
     * noch nicht existieren:
     * <ul>
     * <li>Arbeitsbereich: Verzeichnis, in dem alle benutzerspezifischen Daten gespeichert werden</li>
     * <li>Temp: Verzeichnis, in dem zur Laufzeit temporaere Dateien gespeichert werden</li>
     * <li>Anwendungen: Verzeichnis, in dem die eigenen Anwendungen gespeichert werden mit den Unterordnern
     * software/clientserver/ und gui/anwendungssicht/</li>
     * <li>Datei, in der die Einstellungen zu eigenen Anwendungen gespeichert werden</li>
     * </ul>
     */
    private void initialisiereVerzeichnisse() throws FileNotFoundException, IOException {
        String pfad;

        pfad = getArbeitsbereichPfad();
        if (!(new java.io.File(pfad)).exists())
            if (!(new java.io.File(pfad)).mkdirs())
                Main.debug.println("ERROR (" + this.hashCode() + ") " + getClass() + "\n\t" + pfad
                        + " konnte nicht erzeugt werden");

        pfad = getTempPfad();
        if (!(new java.io.File(pfad)).exists())
            if (!(new java.io.File(pfad)).mkdirs())
                Main.debug.println("ERROR (" + this.hashCode() + ") " + getClass() + "\n\t" + pfad
                        + " konnte nicht erzeugt werden");

        pfad = getAnwendungenPfad();
        if (!(new java.io.File(pfad)).exists())
            if (!(new java.io.File(pfad)).mkdirs())
                Main.debug.println("ERROR (" + this.hashCode() + ") " + getClass() + "\n\t" + pfad
                        + " konnte nicht erzeugt werden");

        pfad = getAnwendungenPfad() + "filius" + System.getProperty("file.separator") + "software"
                + System.getProperty("file.separator") + "clientserver" + System.getProperty("file.separator");
        if (!(new java.io.File(pfad)).exists())
            if (!(new java.io.File(pfad)).mkdirs())
                Main.debug.println("ERROR (" + this.hashCode() + ") " + getClass() + "\n\t" + pfad
                        + " konnte nicht erzeugt werden");

        pfad = getAnwendungenPfad() + "filius" + System.getProperty("file.separator") + "gui"
                + System.getProperty("file.separator") + "anwendungssicht" + System.getProperty("file.separator");
        if (!(new java.io.File(pfad)).exists())
            if (!(new java.io.File(pfad)).mkdirs())
                Main.debug.println("ERROR (" + this.hashCode() + ") " + getClass() + "\n\t" + pfad
                        + " konnte nicht erzeugt werden");

        pfad = getAnwendungenPfad() + "EigeneAnwendungen.txt";
        if (!(new java.io.File(pfad)).exists())
            (new java.io.File(getAnwendungenPfad() + "EigeneAnwendungen.txt")).createNewFile();
    }

    /**
     * Methode zum lesen der Verfuegbaren Programme aus den Konfigurationsdateien.
     * 
     * @return
     * @throws IOException
     */
    public List<Map<String, String>> ladeProgrammListe() throws IOException {
        List<Map<String, String>> tmpList;
        RandomAccessFile desktopFile;

        tmpList = new LinkedList<Map<String, String>>();
        desktopFile = new RandomAccessFile(holeAnwendungenDateipfad(), "r");
        for (String line; (line = desktopFile.readLine()) != null;) {
            if (!line.trim().startsWith("#") && !line.trim().equals("")) {
                HashMap<String, String> tmpMap = new HashMap<String, String>();
                StringTokenizer st = new StringTokenizer(line, ";");

                tmpMap.put("Anwendung", st.nextToken());
                tmpMap.put("Klasse", st.nextToken());
                tmpMap.put("GUI-Klasse", st.nextToken());
                tmpMap.put("gfxFile", st.nextToken());

                tmpList.add(tmpMap);
            }
        }
        desktopFile.close();

        tmpList.addAll(ladePersoenlicheProgrammListe());

        return tmpList;
    }

    public LinkedList<HashMap<String, String>> ladePersoenlicheProgrammListe() throws IOException {
        LinkedList<HashMap<String, String>> tmpList;
        RandomAccessFile desktopFile = null;

        tmpList = new LinkedList<HashMap<String, String>>();
        try {
            desktopFile = new RandomAccessFile(
                    Information.getInformation().getAnwendungenPfad() + "EigeneAnwendungen.txt", "r");
            for (String line; (line = desktopFile.readLine()) != null;) {
                HashMap<String, String> tmpMap = new HashMap<String, String>();
                if (!line.trim().equals("")) {
                    StringTokenizer st = new StringTokenizer(line, ";");

                    tmpMap.put("Anwendung", st.nextToken());
                    tmpMap.put("Klasse", st.nextToken());
                    tmpMap.put("GUI-Klasse", st.nextToken());
                    tmpMap.put("gfxFile", st.nextToken());

                    tmpList.add(tmpMap);
                }
            }

        } catch (FileNotFoundException e) {

        } finally {
            if (desktopFile != null)
                desktopFile.close();
        }

        return tmpList;
    }

    private String holeAnwendungenDateipfad() {
        String pfad = "config/Desktop";
        File desktopResource = ResourceUtil.getResourceFile(pfad + "_" + locale.toString() + ".txt");
        return desktopResource.getAbsolutePath();
    }

    /**
     * Pfad zum Verzeichnis, in dem das Programm ausgefuehrt wird (in dem sich die ausfuehrbare Jar-Datei befindet);
     * <br />
     * der Pfad schliesst mit dem Pfad-Trennzeichen (unter UNIX "/")
     */
    public String getProgrammPfad() {
        if (programmPfad != null) {
            return programmPfad;
        } else {
            String str = System.getProperty("java.class.path");
            programmPfad = System.getProperty("user.dir") + System.getProperty("file.separator");
            if (str.indexOf("filius.jar") >= 0) { // run from jar file
                if ((new File(str)).isAbsolute())
                    programmPfad = ""; // in case of absolute path, delete
                                       // "user.dir" entry
                // da Java beim Aufruf verschiedene Separatoren unterstützt,
                // wird hier getrennt abgefragt...
                if (str.indexOf("/") >= 0) {
                    programmPfad += str.substring(0, str.lastIndexOf("/")) + System.getProperty("file.separator");
                } else if (str.indexOf("\\") >= 0) {
                    programmPfad += str.substring(0, str.lastIndexOf("\\")) + System.getProperty("file.separator");
                }
            }
            return programmPfad;
        }
    }

    public String getRelativePathToProgramDir() {
        String workPath = System.getProperty("user.dir") + File.separator;
        String progPath = getProgrammPfad();

        // Windows system (with drive letters!):
        if (File.separator.equals("\\")) {
            if (progPath.substring(1, 3).equals(workPath.substring(1, 3))) {
                progPath = progPath.substring(2);
                workPath = workPath.substring(2);
            } else { // different drives; --> return absolute path
                return progPath;
            }
        }
        // /////

        // remove first File.separator (first character)
        progPath = progPath.substring(1);
        workPath = workPath.substring(1);

        String relativePath = "";

        int slashIdx = -1;

        boolean finished = false;

        // if workPath is completely part of progPath and at beginning of it:
        if (progPath.indexOf(workPath) == 0) {
            if (progPath.length() <= workPath.length())
                progPath = "";
            else
                progPath = progPath.substring(workPath.length());
            workPath = "";
        } else if (workPath.indexOf(progPath) == 0) { // otherway round
            if (workPath.length() <= progPath.length())
                workPath = "";
            else
                workPath = workPath.substring(progPath.length());
            progPath = "";
        }

        // further processing
        while (!finished) {
            slashIdx = workPath.indexOf(File.separator);
            if (slashIdx >= 0) { // subdirectories left to be stepped up via
                                 // "../" strings
                relativePath += ".." + File.separator;
                if (workPath.length() <= slashIdx + 2)
                    workPath = "";
                else
                    workPath = workPath.substring(slashIdx + 1);
            } else { // only append remaining path to step down (again)
                finished = true;
                relativePath += progPath;
                return relativePath;
            }
        }

        return null;
    }

    /** Arbeitsbereich: Verzeichnis, in dem alle benutzerspezifischen */
    public String getArbeitsbereichPfad() {
        return arbeitsbereichPfad;
    }

    public void setArbeitsbereichPfad(String otherWD) {
        StringTokenizer tokenizer = null;
        String token = null;
        if (otherWD.indexOf("/") >= 0) {
            tokenizer = new StringTokenizer(otherWD, "/");
        } else if (otherWD.indexOf("\\\\") >= 0) {
            tokenizer = new StringTokenizer(otherWD, "\\\\");
        }
        if (otherWD.startsWith("/") || otherWD.startsWith("\\\\")) {
            arbeitsbereichPfad = System.getProperty("file.separator");
        } else
            arbeitsbereichPfad = "";
        if (tokenizer != null) {
            while (tokenizer.hasMoreTokens()) {
                token = tokenizer.nextToken().trim();
                if (!token.isEmpty())
                    arbeitsbereichPfad += token + System.getProperty("file.separator");
            }
        } else {
            arbeitsbereichPfad = otherWD + System.getProperty("file.separator");
        }
        arbeitsbereichPfad += ".filius" + System.getProperty("file.separator");
    }

    /**
     * Temp: Verzeichnis, in dem zur Laufzeit temporaere Dateien gespeichert werden
     */
    public String getTempPfad() {
        return getArbeitsbereichPfad() + "temp" + System.getProperty("file.separator");
    }

    /**
     * Anwendungen: Verzeichnis, in dem die eigenen Anwendungen gespeichert werden mit den Unterordnern
     * software/clientserver/ und gui/anwendungssicht/
     */
    public String getAnwendungenPfad() {
        return getArbeitsbereichPfad() + "anwendungen" + System.getProperty("file.separator");
    }

    /**
     * Automatische Erzeugung einer MAC-Adresse, funktioniert mit Hexadezimal-Zahlen
     */
    public String holeFreieMACAdresse() {
        Random r = new Random();
        String[] mac;
        String neueMac;

        mac = new String[6];
        for (int i = 0; i < mac.length; i++) {
            mac[i] = Integer.toHexString(r.nextInt(255));
            if (mac[i].length() == 1)
                mac[i] = "0" + mac[i];
        }
        neueMac = mac[0] + ":" + mac[1] + ":" + mac[2] + ":" + mac[3] + ":" + mac[4] + ":" + mac[5];

        if (macPruefen(neueMac)) {
            return neueMac;
        } else {
            return holeFreieMACAdresse();
        }
    }

    /** Eintragen einer verwendeten MAC-Adresse */
    public void macHinzufuegen(String mac) {
        macAdressen.addElement(mac);
    }

    /** Pruefen, ob es sich um eine verfuegbare, gueltige MAC-Adresse handelt. */
    private boolean macPruefen(String mac) {
        boolean macOK = true;

        for (int i = 0; i < macAdressen.size(); i++) {
            if (mac.equals((String) macAdressen.elementAt(i)))
                macOK = false;
        }

        if (mac.equalsIgnoreCase("ff:ff:ff:ff:ff:ff")) {
            macOK = false;
        }

        return macOK;
    }

    public int getMaxVermittlungsStellen() {
        return maxVermittlungsStellen;
    }

    public void setMaxVermittlungsStellen(int maxVermittlungsStellen) {
        this.maxVermittlungsStellen = maxVermittlungsStellen;
    }

    public void loadIni() throws IOException {
        File tmpFile = ResourceUtil.getResourceFile("config/filius.ini");
        if (tmpFile.exists()) {
            RandomAccessFile iniFile = null;

            try {
                iniFile = new RandomAccessFile(tmpFile.getAbsolutePath(), "r");
                int width = 0;
                int height = 0;
                for (String line; (line = iniFile.readLine()) != null;) {
                    if (!line.trim().equals("") && !line.trim().startsWith("#")) {
                        StringTokenizer st = new StringTokenizer(line, "=");

                        if (st.hasMoreTokens()) {
                            String configKey = st.nextToken().trim();
                            if (st.hasMoreTokens()) {
                                String configValue = st.nextToken();
                                if (configKey.equalsIgnoreCase("locale")) {
                                    String language = configValue.substring(0, configValue.indexOf("_"));
                                    String country = configValue.substring(configValue.indexOf("_") + 1);
                                    this.setLocale(new Locale(language, country));
                                } else if (configKey.equalsIgnoreCase("rtt")) {
                                    if (Verbindung.getRTTfactor() == 1) {
                                        Verbindung.setRTTfactor(Integer.parseInt(configValue));
                                    }
                                } else if (configKey.equalsIgnoreCase("native-look-n-feel")) {
                                    if (configValue.trim().equals("1")) {
                                        Main.activateNativeLookAndFeel();
                                    }
                                } else if (configKey.equalsIgnoreCase("posix-behaviour")) {
                                    if (configValue.trim().equals("1")) {
                                        posixCommandLineToolBehaviour = true;
                                    }
                                } else if (configKey.equalsIgnoreCase("desktop-mode")) {
                                    desktopWindowMode = GUIDesktopWindow.Mode
                                            .getMode(Integer.parseInt(configValue.trim()));
                                } else if (configKey.equalsIgnoreCase("old-exchange-dialog")) {
                                    if (configValue.trim().equals("1")) {
                                        this.oldExchangeDialog = true;
                                    } else if (configValue.trim().equals("0")) {
                                        this.oldExchangeDialog = false;
                                    }
                                } else if (configKey.equalsIgnoreCase("software-wizard")) {
                                    if (configValue.trim().equals("1")) {
                                        softwareWizardMode = FeatureMode.FORCE_ENABLE;
                                    } else if (configValue.trim().equals("0")) {
                                        softwareWizardMode = FeatureMode.FORCE_DISABLE;
                                    }
                                } else if (configKey.equalsIgnoreCase("pane-width")) {
                                    try {
                                        width = Integer.parseInt(configValue);
                                    } catch (NumberFormatException e) {}
                                } else if (configKey.equalsIgnoreCase("pane-height")) {
                                    try {
                                        height = Integer.parseInt(configValue);
                                    } catch (NumberFormatException e) {}
                                }
                            }

                        }
                    }
                }
                if (width > 0 && height > 0) {
                    GUIContainer.getGUIContainer(width, height);
                }
            } finally {
                if (iniFile != null)
                    iniFile.close();
            }
        }
    }

    public String getLastOpenedDirectory() {
        return lastOpenedDirectory;
    }

    public void setLastOpenedDirectory(String lastOpenedDirectory) {
        this.lastOpenedDirectory = lastOpenedDirectory;
    }
}
