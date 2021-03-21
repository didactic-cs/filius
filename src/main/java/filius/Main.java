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
 ** along with Filius. If not, see <http://www.gnu.org/licenses/>.
 */
package filius;

import java.awt.Color;
import java.awt.Rectangle;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import filius.gui.GUIContainer;
import filius.gui.JMainFrame;
import filius.gui.SplashScreen;
import filius.hardware.Cable;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.Information;
import filius.rahmenprogramm.ProjectManager;
import filius.rahmenprogramm.TeeOutputStream;

/**
 * In dieser Klasse wird die Anwendung gestartet und beendet. Das wird in den entsprechenden statischen Methoden
 * implementiert.
 */
public class Main implements I18n {

    private static final String FRANCAIS = "Français";
    private static final String ENGLISH = "English";
    private static final String DEUTSCH = "Deutsch";
    /**
     * ueber diesen Stream werden Nachrichten ausgegeben, die fuer die Fehlersuche nuetzlich sind. NOTE: in loggen(..)
     * gesetzt
     */
    public static PrintStream debug = System.out;

    /**
     * Der Start laeuft folgendermassen ab:
     * <ol>
     * <li>Anzeigen eines Startfensters</li>
     * <li>Initialisierung des Programm-Hauptfensters</li>
     * <li>Laden eines Szenarios, entweder
     * <ul>
     * <li>ein mit dem Programmstart uebergebene Szenariodatei oder</li>
     * <li>das zuletzt geoeffnete bzw. gespeicherte Szenario</li>
     * </ul>
     * </li>
     * <li>Ausblenden des Startfensters</li>
     * </ol>
     */
    public static void start(String projectFile) {
        Main.debug.println("INVOKED (static) filius.Main, starten(" + projectFile + ")");
        SplashScreen splashScreen;
        XMLDecoder xmldec;
        String konfigPfad;
        Object[] programmKonfig;

        try {
            Information.getInstance().loadIni();
        } catch (IOException e1) {
            Main.debug.println("ini could not be read: " + e1.getMessage());
        }

        konfigPfad = Information.getInstance().getArbeitsbereichPfad() + "konfig.xml";
        if (!(new File(konfigPfad)).exists() && null == Information.getInstance().getLocale()) {
            String[] possibleValues = { DEUTSCH, ENGLISH, FRANCAIS };
            String selectedValue = (String) JOptionPane.showInputDialog(null, "", "Sprache/Language/Langue",
                    JOptionPane.INFORMATION_MESSAGE, null, possibleValues, possibleValues[0]);
            if (selectedValue == null) {
                Information.getInstance().setLocale(Locale.GERMANY);                
            } else if (ENGLISH.equals(selectedValue)) {
                Information.getInstance().setLocale(Locale.UK);
            } else if (FRANCAIS.equals(selectedValue)) {
                Information.getInstance().setLocale(Locale.FRANCE);
            } else {
                Information.getInstance().setLocale(Locale.GERMANY);
            }                                                                           
        } else {
            try {
                xmldec = new XMLDecoder(new BufferedInputStream(new FileInputStream(konfigPfad)));
                programmKonfig = (Object[]) xmldec.readObject();
                if (programmKonfig != null) {
                    if (programmKonfig.length >= 4) {
                        JMainFrame.getInstance().setBounds((Rectangle) programmKonfig[0]);
                        if (projectFile == null) {
                            projectFile = (String) programmKonfig[1];
                        }
                        if (programmKonfig[2] != null && programmKonfig[3] != null && null == Information.getInstance().getLocale()) {
                            Information.getInstance().setLocale(new Locale((String) programmKonfig[2], (String) programmKonfig[3]));
                        }
                    }
                    if (programmKonfig.length >= 5) {
                        Information.getInstance().setLastOpenedDirectory((String) programmKonfig[4]);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(Main.debug);
            }
        } 
        
        // Handy to check the various languages
        //Information.getInstance().setLocale(Locale.UK);
        //Information.getInstance().setLocale(Locale.GERMANY);  
        //Information.getInstance().setLocale(Locale.FRANCE);
        
        localizeAccelerators();
        
        localizeJavaDialogBoxes();
        
        customizeUIColors();

        splashScreen = new SplashScreen("gfx/allgemein/splashscreen.png", null);
        splashScreen.setVisible(true);
        splashScreen.setAlwaysOnTop(true);

        GUIContainer container = GUIContainer.getInstance();
        container.init();
        
        long splashTime = System.currentTimeMillis();

        if (projectFile != null) {
            try {
                ProjectManager.getInstance().load(projectFile, container.getNodeItems(), container.getCableList(), container.getDocItems());
            } catch (Exception e) {
                e.printStackTrace(Main.debug);
            }
        }
        container.setConfigPanel(null); 
        container.updateViewport();
        
        // Still useful? The reason for the following lines is not clear.
//        try {
//            Thread.sleep(10);
//        } catch (Exception e) {}
        
        container.updateCables();

        splashTime = System.currentTimeMillis() - splashTime;
        // time difference
        // since
        // Splashscreen
        // made visible
        Main.debug.println("Splash Screen shown for " + splashTime + " ms");
        if (splashTime < 1000) {
            try {
                Thread.sleep(1000 - splashTime);
            } catch (Exception e) {}
        } // sleep until 1s is over
        splashScreen.setAlwaysOnTop(false);
        splashScreen.setVisible(false);
    }
    
    // >> Does not work yet because it should be called before any toolkit code is executed.
    // >> To be fixed!
    private static void localizeAccelerators() {
        
        Locale locale = Information.getInstance().getLocale();        
        if (locale.equals(Locale.UK))           Locale.setDefault(Locale.ENGLISH); 
        else if (locale.equals(Locale.FRANCE))  Locale.setDefault(Locale.FRENCH); 
        else                                    Locale.setDefault(Locale.GERMAN); 
    }
    
    /**
     * <b>localizeJavaDialogBoxes</b> translates the Java dialog boxes
     */
    private static void localizeJavaDialogBoxes() {

    	// Standard dialog box        
        UIManager.put("OptionPane.yesButtonText", messages.getString("main_dlg_YES"));
        UIManager.put("OptionPane.noButtonText", messages.getString("main_dlg_NO"));
        UIManager.put("OptionPane.okButtonText", messages.getString("main_dlg_OK"));
        UIManager.put("OptionPane.cancelButtonText", messages.getString("main_dlg_CANCEL"));
                
        // File open/save dialog boxes        
        UIManager.put("FileChooser.lookInLabelText", messages.getString("main_dlg_LOOKIN"));
        UIManager.put("FileChooser.saveInLabelText", messages.getString("main_dlg_SAVEIN"));
        UIManager.put("FileChooser.fileNameLabelText", messages.getString("main_dlg_FILENAME"));
        UIManager.put("FileChooser.filesOfTypeLabelText", messages.getString("main_dlg_FILETYPE"));
        
        UIManager.put("FileChooser.openButtonText", messages.getString("main_dlg_OPEN"));
        UIManager.put("FileChooser.saveButtonText", messages.getString("main_dlg_SAVE"));
        UIManager.put("FileChooser.cancelButtonText", messages.getString("main_dlg_CANCEL"));  
        
        UIManager.put("FileChooser.upFolderToolTipText", messages.getString("main_dlg_UPFOLDER"));
        UIManager.put("FileChooser.homeFolderToolTipText", messages.getString("main_dlg_HOMEFOLDER")); 
        UIManager.put("FileChooser.newFolderToolTipText", messages.getString("main_dlg_NEWFOLDER")); 
        UIManager.put("FileChooser.listViewButtonToolTipText", messages.getString("main_dlg_LISTVIEW")); 
        UIManager.put("FileChooser.detailsViewButtonToolTipText", messages.getString("main_dlg_DETAILSVIEW"));
        UIManager.put("FileChooser.fileNameHeaderText", messages.getString("main_dlg_NAME"));
        UIManager.put("FileChooser.fileSizeHeaderText", messages.getString("main_dlg_SIZE"));
        UIManager.put("FileChooser.fileTypeHeaderText", messages.getString("main_dlg_TYPE"));
        UIManager.put("FileChooser.fileDateHeaderText", messages.getString("main_dlg_DATE"));
        UIManager.put("FileChooser.fileAttrHeaderText", messages.getString("main_dlg_ATTRIBUTES"));
        UIManager.put("FileChooser.openButtonToolTipText", messages.getString("main_dlg_OPENTIP"));
        UIManager.put("FileChooser.saveButtonToolTipText", messages.getString("main_dlg_SAVETIP")); 
        UIManager.put("FileChooser.cancelButtonToolTipText", messages.getString("main_dlg_CANCELTIP"));      
  
        UIManager.put("FileChooser.viewMenuLabelText", messages.getString("main_dlg_VIEW"));
        UIManager.put("FileChooser.listViewActionLabelText", messages.getString("main_dlg_LISTVIEW")); 
        UIManager.put("FileChooser.detailsViewActionLabelText", messages.getString("main_dlg_DETAILSVIEW"));
        UIManager.put("FileChooser.refreshActionLabelText", messages.getString("main_dlg_REFRESH"));
        UIManager.put("FileChooser.newFolderActionLabelText", messages.getString("main_dlg_NEWFOLDER")); 
        
        UIManager.put("FileChooser.acceptAllFileFilterText", messages.getString("main_dlg_ALLFILES")); 
        
        UIManager.put("FileChooser.win32.newFolder", messages.getString("main_dlg_WINNEWFOLDER"));
        UIManager.put("FileChooser.win32.newFolder.subsequent", messages.getString("main_dlg_WINNEWFOLDER")+" ({0})");   
    }

    
    /**
     * <b>customizeUIColors</b> customize a few colors of the UI
     * 
     */
    private static void customizeUIColors() {        	
    	
        Color UI_PanelBG = UIManager.getColor ("Panel.background");  // same as Color(238, 238, 238)
        Color UI_UnselectedPanelBG = new Color(200, 200, 200);
        
        // Customize the tabbed colors
        UIManager.put("TabbedPane.contentAreaColor", UI_PanelBG);
        UIManager.put("TabbedPane.selected", UI_PanelBG);
        UIManager.put("TabbedPane.unselectedBackground", UI_UnselectedPanelBG);        
        
        // Tables
        //UIManager.put("Table.alternateRowColor", new Color(245, 245, 255));
    }
    
    
    /**
     * Das Beenden des Programms laeuft folgendermassen ab:
     * <ol>
     * <li>Wechsel in den Entwurfsmodus (und damit beenden der virtuellen Software und der damit verbundenen
     * Threads</li>
     * <li>Pruefung, ob eine Aenderung am Szenario vorgenommen wurde
     * <ul>
     * <li>wenn Szenario geaendert wurde, wird gefragt, ob die Datei noch gespeichert werden soll</li>
     * <li>wenn das Szenario nicht gespeichert werden soll, werden die Aenderungen verworfen</li>
     * <li>wenn die Abfrage abgebrochen wird, wird Filius nicht beendet</li>
     * </ul>
     * </li>
     * <li>Programmkonfiguration wird gespeichert</li>
     * <li>das Verzeichnis fuer temporaere Dateien wird geloescht</li>
     * </ol>
     */
    public static void confirmAndStop() {
        Main.debug.println("INVOKED (static) filius.Main, beenden()");
        
        GUIContainer.getInstance().prepareForClosing();          

        if (ProjectManager.getInstance().isModified()) {
            int choice = JOptionPane.showConfirmDialog(JMainFrame.getInstance(), messages.getString("main_msg1"),
                                                       messages.getString("main_msg2"), JOptionPane.YES_NO_CANCEL_OPTION);
            
            if (choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION) return;
            
            if (choice == JOptionPane.YES_OPTION) {
            	// Save the project
            	GUIContainer.getInstance().getMainMenu().doClick("btSpeichern");
            	// The user may have canceled the dialog box
            	if (ProjectManager.getInstance().isModified()) return;
            };
        }
        
        Object[] programmKonfig = new Object[5];
        programmKonfig[0] = JMainFrame.getInstance().getBounds();
        programmKonfig[1] = ProjectManager.getInstance().getPath();
        programmKonfig[2] = Information.getInstance().getLocale().getLanguage();
        programmKonfig[3] = Information.getInstance().getLocale().getCountry();
        programmKonfig[4] = Information.getInstance().getLastOpenedDirectory();

        String applicationConfigPath = Information.getInstance().getArbeitsbereichPfad() + "konfig.xml";
        try (FileOutputStream fos = new FileOutputStream(applicationConfigPath);
        		XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(fos))) {
        	encoder.writeObject(programmKonfig);
        } catch (Exception e) {
        	e.printStackTrace(Main.debug);
        }
        ProjectManager.deleteDirectoryContent(Information.getInstance().getTempPath());
        System.exit(0);       
    }

    private static boolean log(String logDateiPfad, boolean ausgabeKommandozeile) {
        PrintStream kommandozeile = null;
        if (ausgabeKommandozeile) {
            kommandozeile = System.out;
        }
        if (logDateiPfad != null) {
            try {
                Main.debug = new PrintStream(new TeeOutputStream(new FileOutputStream(logDateiPfad), kommandozeile));
                System.out.println("Ausgaben werden in Datei '" + logDateiPfad + "' protokolliert.");
                System.setErr(Main.debug);
            } catch (FileNotFoundException e) {
                System.err.println(
                        "Error: logging could not be realised due to FileNotFoundException:\n\t'" + e.toString() + "'");
                Main.debug = new PrintStream(new TeeOutputStream(null, kommandozeile));
            } catch (Exception e) {
                System.err.println(
                        "Error: logging could not be realised; reason not specified:\n\t'" + e.toString() + "'");
                Main.debug = new PrintStream(new TeeOutputStream(null, kommandozeile));
            }
        } else {
            Main.debug = new PrintStream(new TeeOutputStream(null, kommandozeile));
        }

        return true;
    }

    /**
     * Hier wird das Programm Filius gestartet! Wenn ein Parameter uebergeben wird, wird geprueft, ob es sich um eine
     * existierende Datei handelt. Dann wird der Pfad an die Methode zum Starten uebergeben als eine Szenario-Datei, die
     * zum Start geladen werden soll.
     */
    public static void main(String[] args) {
        String currWD = Information.initArbeitsbereichPfad;
        File file;
        boolean log = false;
        String newWD = null;
        String argsString = "";
        boolean nativeLookAndFeel = false;
        boolean verbose = false;

        if (args != null && args.length >= 1) {
            for (int i = 0; i < args.length; i++) {
                argsString += args[i] + " ";
                // Protokollieren in Datei?
                if (args[i].equals("-l")) {
                    log = true;
                }
                if (args[i].equals("-wd")) {
                    if (args.length > i + 1 && !args[i + 1].startsWith("-")) {
                        newWD = args[++i].trim();
                        currWD = newWD; // set working directory (not yet set in
                        // Information class, otherwise an
                        // Exception would emerge!)
                        // Information.getInformation().setArbeitsbereichPfad(newWD);
                    } else {
                        System.err.println(
                                "Parameter '-wd' ohne Argument verwendet! Korrekte Verwendung (Beispiel):  '-wd /home/user'\n");
                        System.err.println(
                                "Parameter '-wd' without content! Correct usage (example):  '-wd /home/user'\n");
                        showUsageInformation();
                        System.exit(1);
                    }
                }
                if (args[i].equals("-n")) {
                    nativeLookAndFeel = true;
                }
                if (args[i].equals("-h")) {
                    showUsageInformation();
                    System.exit(0);
                }
                if (args[i].equals("-s")) {
                    Information.setLowResolution(true);
                }
                if (args[i].equals("-v")) {
                    verbose = true;
                }
                if (args[i].equals("-r")) {
                    if (args.length > i + 1 && !args[i + 1].startsWith("-")) {
                        try {
                            int rtt = Integer.parseInt(args[++i]);
                            Cable.setRTTfactor(rtt);
                        } catch (NumberFormatException e) {
                            System.err.println("Ungueltige Round-Trip-Time " + args[i] + ". Ganzzahl erwartet.\n");
                            showUsageInformation();

                            System.exit(1);
                        }

                    } else {
                        System.err.println(
                                "Parameter '-r' ohne Argument verwendet! Korrekte Verwendung (Beispiel):  '-r 2'\n");
                        showUsageInformation();
                        System.exit(1);
                    }
                }
            }
            if (currWD.isEmpty() || (!currWD.substring(currWD.length() - 1).equals(System.getProperty("file.separator")))) {
                // check, whether working directory is usable...
                // else provide dialog for correct paths
                if (Information.getInformation(currWD + System.getProperty("file.separator")) == null)
                    System.exit(6);
                else if (Information.getInformation(currWD) == null)
                    System.exit(6);
            }
            // if no logging specified on command line or logging to file
            // fails, then set logging to null
            if (log) {
                log = log(Information.getInstance().getArbeitsbereichPfad() + "filius.log", verbose);
            } else {
                log(null, verbose);
            }
        } else {
            if (Information.getInformation(currWD) == null) {
                System.exit(6);
            }
            log(null, false);
        }

        showUsageInformation();

        Main.debug.println("------------------------------------------------------");
        Main.debug.println("\tJava Version: " + System.getProperty("java.version"));
        Main.debug.println("\tJava Directory: " + System.getProperty("java.home"));
        Main.debug.println("\tFILIUS Version: " + Information.getVersion());
        Main.debug.println("\tParameters: '" + argsString.trim() + "'");
        // +"\n\tWD Base: "+newWD
        Main.debug.println("\tFILIUS Installation: " + Information.getInstance().getProgramPath());
        Main.debug.println("\tFILIUS Working Directory: " + Information.getInstance().getArbeitsbereichPfad());
        Main.debug.println("\tFILIUS Temp Directory: " + Information.getInstance().getTempPath());
        Main.debug.println("------------------------------------------------------\n");

        if (nativeLookAndFeel) {
            activateNativeLookAndFeel();
        }
        if (args != null && ((args.length >= 1 && !log) || (args.length >= 2 && log))) {
            // Projekt-Datei als letztes Argument uebergeben?
            try {
                file = new File(args[args.length - 1]);
                if (file.exists()) {
                    start(file.getAbsolutePath());
                } else
                    start(null);
            } catch (Exception e) {
                e.printStackTrace();
                start(null);
            }
        } else {
            start(null);
        }
    }

    public static void activateNativeLookAndFeel() {
        try {
            // Set System L&F
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException e) {
            // handle exception
        } catch (ClassNotFoundException e) {
            // handle exception
        } catch (InstantiationException e) {
            // handle exception
        } catch (IllegalAccessException e) {
            // handle exception
        }
    }

    private static void showUsageInformation() {
        StringBuffer usage = new StringBuffer();

        usage.append("\nAufruf:\n");
        usage.append("\tfilius [Optionen] [Pfad zu Projektdatei]\n\n");

        usage.append("Verwenden Sie Filius mit folgenden Optionen:\n");
        usage.append("\t-h   Anzeige dieser Hilfe-Information\n");
        usage.append("\t-v   Ausgabe detaillierter Informationen zu Programmausfuehrung\n");
        usage.append("\t-wd  Pfad zu Arbeitsverzeichnis, in dem durch das Programm Daten\n"
                + "\t     zur Laufzeit abgelegt werden koennen\n");
        usage.append("\t-l   Loggen der Programmausgabe in Datei (filius.log)\n");
        usage.append("\t-r   Setzen eines Faktors zur Erhoehung der erlaubten\n"
                + "\t     Round-Trip-Time in Filius (kann erforderlich sein, um Fehler\n"
                + "\t     bei Ausfuehrung auf langsamer Hardware zu vermeiden)\n"
                + "\t     Gueltige Werte sind 1 (Vorgabe) bis 5.\n");
        usage.append("\t-n   Darstellung mit betriebssystemspezifischem Look & Feel\n");
        usage.append(
                "\t-s   Darstellung fuer niedrige Bildschirmaufloesung (kleiner\n" + "\t     1024x768) anpassen\n");

        System.out.println(usage.toString());
    }

}
