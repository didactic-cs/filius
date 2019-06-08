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
import filius.gui.GUIMainMenu;
import filius.gui.JMainFrame;
import filius.gui.SplashScreen;
import filius.hardware.Verbindung;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.Information;
import filius.rahmenprogramm.SzenarioVerwaltung;
import filius.rahmenprogramm.TeeOutputStream;

/**
 * In dieser Klasse wird die Anwendung gestartet und beendet. Das wird in den entsprechenden statischen Methoden
 * implementiert.
 */
public class Main implements I18n {

    private static final String FRANCAIS = "Française";
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
    public static void starten(String szenarioDatei) {
        Main.debug.println("INVOKED (static) filius.Main, starten(" + szenarioDatei + ")");
        SplashScreen splashScreen;
        XMLDecoder xmldec;
        String konfigPfad;
        Object[] programmKonfig;

        try {
            Information.getInformation().loadIni();
        } catch (IOException e1) {
            Main.debug.println("ini could not be read: " + e1.getMessage());
        }

        konfigPfad = Information.getInformation().getArbeitsbereichPfad() + "konfig.xml";
        if (!(new File(konfigPfad)).exists() && null == Information.getInformation().getLocale()) {
            String[] possibleValues = { DEUTSCH, ENGLISH, FRANCAIS };
            String selectedValue = (String) JOptionPane.showInputDialog(null, "", "Sprache/Language/Langue",
                    JOptionPane.INFORMATION_MESSAGE, null, possibleValues, possibleValues[0]);
            if (selectedValue == null) {
                Information.getInformation().setLocale(Locale.GERMANY);
            } else if (ENGLISH.equals(selectedValue)) {
                Information.getInformation().setLocale(Locale.UK);
            } else if (FRANCAIS.equals(selectedValue)) {
                Information.getInformation().setLocale(Locale.FRANCE);
            } else {
                Information.getInformation().setLocale(Locale.GERMANY);
            }
        } else {
            try {
                xmldec = new XMLDecoder(new BufferedInputStream(new FileInputStream(konfigPfad)));
                programmKonfig = (Object[]) xmldec.readObject();
                if (programmKonfig != null) {
                    if (programmKonfig.length >= 4) {
                        JMainFrame.getJMainFrame().setBounds((Rectangle) programmKonfig[0]);
                        if (szenarioDatei == null) {
                            szenarioDatei = (String) programmKonfig[1];
                        }
                        if (programmKonfig[2] != null && programmKonfig[3] != null
                                && null == Information.getInformation().getLocale()) {
                            Information.getInformation()
                                    .setLocale(new Locale((String) programmKonfig[2], (String) programmKonfig[3]));
                        }
                    }
                    if (programmKonfig.length >= 5) {
                        Information.getInformation().setLastOpenedDirectory((String) programmKonfig[4]);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(Main.debug);
            }
        }

        // adapt dialog buttons to current language, since Java does not do this
        // automatically
        UIManager.put("OptionPane.cancelButtonText", messages.getString("main_dlg_CANCEL"));
        UIManager.put("OptionPane.noButtonText", messages.getString("main_dlg_NO"));
        UIManager.put("OptionPane.okButtonText", messages.getString("main_dlg_OK"));
        UIManager.put("OptionPane.yesButtonText", messages.getString("main_dlg_YES"));

        splashScreen = new SplashScreen("gfx/allgemein/splashscreen.png", null);
        splashScreen.setVisible(true);
        splashScreen.setAlwaysOnTop(true);

        GUIContainer.getGUIContainer().initialisieren();
        long splashTime = System.currentTimeMillis();

        if (szenarioDatei != null) {
            try {
                SzenarioVerwaltung.getInstance().laden(szenarioDatei, GUIContainer.getGUIContainer().getKnotenItems(),
                        GUIContainer.getGUIContainer().getCableItems(), GUIContainer.getGUIContainer().getDocuItems());
            } catch (Exception e) {
                e.printStackTrace(Main.debug);
            }
        }
        GUIContainer.getGUIContainer().setProperty(null);
        GUIContainer.getGUIContainer().updateViewport();
        try {
            Thread.sleep(10);
        } catch (Exception e) {}
        GUIContainer.getGUIContainer().updateCables();

        splashTime = System.currentTimeMillis() - splashTime; // time difference
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
    public static void beenden() {
        Main.debug.println("INVOKED (static) filius.Main, beenden()");
        Object[] programmKonfig;
        int entscheidung;
        boolean abbruch = false;

        GUIContainer.getGUIContainer().getMenu().selectMode(GUIMainMenu.MODUS_ENTWURF);

        if (SzenarioVerwaltung.getInstance().istGeaendert()) {
            entscheidung = JOptionPane.showConfirmDialog(JMainFrame.getJMainFrame(), messages.getString("main_msg1"),
                    messages.getString("main_msg2"), JOptionPane.YES_NO_OPTION);
            if (entscheidung == JOptionPane.YES_OPTION) {
                abbruch = false;
            } else {
                abbruch = true;
            }
        }
        if (!abbruch) {
            programmKonfig = new Object[5];
            programmKonfig[0] = JMainFrame.getJMainFrame().getBounds();
            programmKonfig[1] = SzenarioVerwaltung.getInstance().holePfad();
            programmKonfig[2] = Information.getInformation().getLocale().getLanguage();
            programmKonfig[3] = Information.getInformation().getLocale().getCountry();
            programmKonfig[4] = Information.getInformation().getLastOpenedDirectory();

            String applicationConfigPath = Information.getInformation().getArbeitsbereichPfad() + "konfig.xml";
            try (FileOutputStream fos = new FileOutputStream(applicationConfigPath);
                    XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(fos))) {
                encoder.writeObject(programmKonfig);
            } catch (Exception e) {
                e.printStackTrace(Main.debug);
            }
            SzenarioVerwaltung.loescheVerzeichnisInhalt(Information.getInformation().getTempPfad());
            System.exit(0);
        }
    }

    private static boolean loggen(String logDateiPfad, boolean ausgabeKommandozeile) {
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
                            Verbindung.setRTTfactor(rtt);
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
            if (currWD.isEmpty()
                    || (!currWD.substring(currWD.length() - 1).equals(System.getProperty("file.separator")))) {
                // check, whether working directory is
                // usable... else provide dialog for correct
                // paths
                if (Information.getInformation(currWD + System.getProperty("file.separator")) == null)
                    System.exit(6);
                else if (Information.getInformation(currWD) == null)
                    System.exit(6);
            }
            // if no logging specified on command line or logging to file
            // fails, then set logging to null
            if (log) {
                log = loggen(Information.getInformation().getArbeitsbereichPfad() + "filius.log", verbose);
            } else {
                loggen(null, verbose);
            }
        } else {
            if (Information.getInformation(currWD) == null) {
                System.exit(6);
            }
            loggen(null, false);
        }

        showUsageInformation();

        Main.debug.println("------------------------------------------------------");
        Main.debug.println("\tJava Version: " + System.getProperty("java.version"));
        Main.debug.println("\tJava Directory: " + System.getProperty("java.home"));
        Main.debug.println("\tFILIUS Version: " + Information.getVersion());
        Main.debug.println("\tParameters: '" + argsString.trim() + "'");
        // +"\n\tWD Base: "+newWD
        Main.debug.println("\tFILIUS Installation: " + Information.getInformation().getProgrammPfad());
        Main.debug.println("\tFILIUS Working Directory: " + Information.getInformation().getArbeitsbereichPfad());
        Main.debug.println("\tFILIUS Temp Directory: " + Information.getInformation().getTempPfad());
        Main.debug.println("------------------------------------------------------\n");

        if (nativeLookAndFeel) {
            activateNativeLookAndFeel();
        }
        if (args != null && ((args.length >= 1 && !log) || (args.length >= 2 && log))) {
            // Projekt-Datei als letztes Argument uebergeben?
            try {
                file = new File(args[args.length - 1]);
                if (file.exists()) {
                    starten(file.getAbsolutePath());
                } else
                    starten(null);
            } catch (Exception e) {
                e.printStackTrace();
                starten(null);
            }
        } else {
            starten(null);
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
