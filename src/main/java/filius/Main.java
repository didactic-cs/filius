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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import filius.rahmenprogramm.Sprache;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import filius.gui.GUIContainer;
import filius.gui.GUIMainMenu;
import filius.gui.JMainFrame;
import filius.gui.SplashScreen;
import filius.hardware.Verbindung;
import filius.rahmenprogramm.FiliusArgs;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.Information;
import filius.rahmenprogramm.SzenarioVerwaltung;

/**
 * In dieser Klasse wird die Anwendung gestartet und beendet. Das wird in den entsprechenden statischen Methoden
 * implementiert.
 */
public class Main implements I18n {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

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
        LOG.trace("INVOKED (static) filius.Main, starten(" + szenarioDatei + ")");
        SplashScreen splashScreen;
        XMLDecoder xmldec;
        String konfigPfad;
        Object[] programmKonfig;

        try {
            Information.getInformation().loadIni();
        } catch (IOException e1) {
            LOG.debug("ini could not be read: " + e1.getMessage());
        }

        konfigPfad = Information.getInformation().getArbeitsbereichPfad() + "konfig.xml";
        if (!(new File(konfigPfad)).exists() && null == Information.getInformation().getLocale()) {
            String[] possibleValues = new String[Sprache.values().length];
            for (int i = 0; i < Sprache.values().length; i++) {
                possibleValues[i] = Sprache.values()[i].toString();
            }
            String selectedValue = (String) JOptionPane.showInputDialog(null, "", "Sprache/Language/Langue",
                    JOptionPane.INFORMATION_MESSAGE, null, possibleValues, Sprache.DEUTSCH.toString());
            if (selectedValue == null) Information.getInformation().setLocale(Locale.GERMANY);
            else {
                Sprache sprache = Sprache.getSprache(selectedValue);
                if (sprache != null) Information.getInformation().setLocale(sprache.getLocale());
                else throw new IllegalArgumentException("Unknown language: " + selectedValue);
            }
        } else {
            try {
                xmldec = new XMLDecoder(new BufferedInputStream(Files.newInputStream(Paths.get(konfigPfad))));
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
                LOG.debug("There is no user configuration available.");
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

        long splashTime = System.currentTimeMillis();

        if (szenarioDatei != null) {
            try {
                SzenarioVerwaltung.getInstance().laden(szenarioDatei, GUIContainer.getGUIContainer().getKnotenItems(),
                        GUIContainer.getGUIContainer().getCableItems(), GUIContainer.getGUIContainer().getDocuItems());
            } catch (Exception e) {
                LOG.debug("", e);
            }
        }
        GUIContainer.getGUIContainer().setProperty(null);
        GUIContainer.getGUIContainer().updateViewport();
        try {
            Thread.sleep(10);
        } catch (Exception exception) {
            LOG.error("Caught an unexpected exception.", exception);
        }
        GUIContainer.getGUIContainer().updateCables();

        splashTime = System.currentTimeMillis() - splashTime;
        // time difference
        // since
        // Splashscreen
        // made visible
        LOG.debug("Splash Screen shown for " + splashTime + " ms");
        if (splashTime < 1000) {
            try {
                Thread.sleep(1000 - splashTime);
            } catch (Exception exception) {
                LOG.error("Caught an unexpected exception.", exception);
            }
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
        LOG.trace("INVOKED (static) filius.Main, beenden()");
        Object[] programmKonfig;
        int entscheidung;
        boolean abbruch = false;

        GUIContainer.getGUIContainer().getMenu().selectMode(GUIMainMenu.MODUS_ENTWURF);

        if (SzenarioVerwaltung.getInstance().istGeaendert()) {
            entscheidung = JOptionPane.showConfirmDialog(JMainFrame.getJMainFrame(), messages.getString("main_msg1"),
                    messages.getString("main_msg2"), JOptionPane.YES_NO_OPTION);
            abbruch = entscheidung != JOptionPane.YES_OPTION;
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
                LOG.debug("", e);
            }
            SzenarioVerwaltung.loescheVerzeichnisInhalt(Information.getInformation().getTempPfad());
            System.exit(0);
        }
    }

    /**
     * Hier wird das Programm Filius gestartet! Wenn ein Parameter uebergeben wird, wird geprueft, ob es sich um eine
     * existierende Datei handelt. Dann wird der Pfad an die Methode zum Starten uebergeben als eine Szenario-Datei, die
     * zum Start geladen werden soll.
     */
    public static void main(String[] args) {
        FiliusArgs filiusArgs = new FiliusArgs();
        try {
            filiusArgs.parseCommandLine(args);

            if (filiusArgs.currWD.isEmpty() || (!filiusArgs.currWD.substring(filiusArgs.currWD.length() - 1)
                    .equals(System.getProperty("file.separator")))) {
                // check, whether working directory is
                // usable... else provide dialog for correct
                // paths
                if (Information.getInformation(filiusArgs.currWD + System.getProperty("file.separator")) == null)
                    System.exit(6);
                else if (Information.getInformation(filiusArgs.currWD) == null)
                    System.exit(6);
            }
            if (filiusArgs.log) {
                System.setProperty("FILIUS_LOG_LEVEL", "DEBUG");
                LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
                loggerContext.reset();
                JoranConfigurator configurator = new JoranConfigurator();
                configurator.setContext(loggerContext);
                try {
                    configurator.doConfigure(configurator.getClass().getResourceAsStream("/logback.xml"));
                    LOG.info("Log to file enabled.");
                } catch (JoranException exception) {
                    LOG.error("Caught an unexpected error while trying to load the logback configuration file..", exception);
                }
            }
            if (Information.getInformation(filiusArgs.currWD) == null) {
                System.exit(6);
            }
            Verbindung.setRTTfactor(filiusArgs.rtt);
            Information.setLowResolution(filiusArgs.lowResolution);

            if (filiusArgs.nativeLookAndFeel) {
                activateNativeLookAndFeel();
            }
            if (filiusArgs.help) {
                filiusArgs.showUsageInformation();
            } else {
                starten(filiusArgs.projectFile);
            }
        } catch (ParseException e) {
            filiusArgs.showUsageInformation();
        }
        LOG.debug("------------------------------------------------------");
        LOG.debug("\tJava Version: " + System.getProperty("java.version"));
        LOG.debug("\tJava Directory: " + System.getProperty("java.home"));
        LOG.debug("\tFILIUS Version: " + Information.getVersion());
        LOG.debug("\tParameters: '" + filiusArgs.argsString.trim() + "'");
        // +"\n\tWD Base: "+newWD
        LOG.debug("\tFILIUS Installation: " + Information.getInformation().getProgrammPfad());
        LOG.debug("\tFILIUS Working Directory: " + Information.getInformation().getArbeitsbereichPfad());
        LOG.debug("\tFILIUS Temp Directory: " + Information.getInformation().getTempPfad());
        LOG.debug("------------------------------------------------------\n");
    }

    public static void activateNativeLookAndFeel() {
        try {
            // Set System L&F
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException e) {
            LOG.warn("Native look and feel not supported. Using default look and feel.");
        } catch (ClassNotFoundException e) {
            LOG.warn("LookAndFeel class could not be found. Using default look and feel.");
        } catch (InstantiationException | IllegalAccessException e) {
            LOG.warn("Could not instantiate LookAndFeel. Using default look and feel.");
        }
    }

}