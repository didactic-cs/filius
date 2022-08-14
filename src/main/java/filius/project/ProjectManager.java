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

import java.beans.ExceptionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import filius.Main;
import filius.gui.GUIContainer;
import filius.gui.GUIErrorHandler;
import filius.gui.dokusicht.GUIDocItem;
import filius.gui.netzwerksicht.GUICableItem;
import filius.gui.netzwerksicht.GUINodeItem;
import filius.gui.netzwerksicht.config.JConfigModem;
import filius.hardware.knoten.Modem;
import filius.software.system.ModemFirmware;
import filius.software.system.ModemFirmware.ModemMode;

public class ProjectManager implements I18n {

    private boolean modified = false;
    private String path = null;
    private static ProjectManager projectManager = null;
    

    private ProjectManager() {}

    public static ProjectManager getInstance() {
        Main.debug.println("INVOKED (static) filius.rahmenprogramm.SzenarioVerwaltung, getInstance()");
        
        if (projectManager == null) {
            projectManager = new ProjectManager();
        }
        return projectManager;
    }

    public void reset() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + ", reset()");
        path = null;
        modified = false;

        fireStatusChange();
    }

    public void setModified() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + ", setzeGeaendert()");
        modified = true;

        fireStatusChange();
    }

    public boolean isModified() {
        return modified;
    }

    public String getPath() {
        return path;
    }

    /**
     * Speichern: - der Netzwerkknoten (inkl. Betriebssystem, Anwendungen - auch eigene/erweiterte - und
     * Konfigurationen) - der Verbindungen - der Quelldateien und des Bytecodes von selbst erstellten Anwendungen
     * 
     * Loesungsstrategie: - generell einen eigenen ClassLoader verwenden - XML-Datei fuer Objekte und Dateien aus dem
     * Ordner Anwendungen in einem leeren temporaeren Ordner speichern und daraus ein neues ZIP-Archiv erstellen, dass
     * an beliebigem Ort gespeichert werden kann
     */
    public boolean save(String filename, List<GUINodeItem> hardwareItems, List<GUICableItem> kabelItems, List<GUIDocItem> docuItems) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + ", speichern(" + filename + "," +
                                     hardwareItems + "," + kabelItems + ")");

        String tmpDir;
        boolean success = true;
        
        closeModemConnections(hardwareItems);
        
        tmpDir = Information.getInstance().getTempPath() + "projekt" + System.getProperty("file.separator");
        (new File(tmpDir)).mkdirs();

        if (!copyDirectory(Information.getInstance().getApplicationsPath(), tmpDir + "anwendungen")) {
            Main.debug.println("ERROR (" + this.hashCode() + "): Speicherung der eigenen Anwendungen fehlgeschlagen!");
            success = false;
        }

        if (!saveNetwork(tmpDir + "konfiguration.xml", hardwareItems, kabelItems, docuItems)) {
            Main.debug.println("ERROR (" + this.hashCode() + "): Speicherung des Netzwerks fehlgeschlagen!");
            success = false;
        }

        if (!createZipArchive(tmpDir, filename)) {
            Main.debug.println("ERROR (" + this.hashCode() + "): Speicherung der Projektdatei fehlgeschlagen!");
            success = false;
        }

        if (success) {
            path = filename;
            modified = false;

            fireStatusChange();
        }

        deleteFiles(tmpDir);

        return success;
    }
    
    /**
    *  The modems must be saved in off status.
    *  (It's not always possible to restore the connections after loading.)
    */
    private void closeModemConnections(List<GUINodeItem> hardwareItems) {
        
    	for (GUINodeItem nodeItem: hardwareItems) {
    		if (nodeItem.getNode() instanceof Modem) {
    			ModemFirmware firmware = ((Modem)nodeItem.getNode()).getFirmware();
    			firmware.closeConnection();
        		firmware.setMode(ModemMode.CLIENT);
    		}    			
    	}
    	
    	// Also update the ConfigPanel if associated with a Modem
    	if (GUIContainer.getInstance().getConfigPanel() instanceof JConfigModem) GUIContainer.getInstance().getConfigPanel().updateDisplayedValues();
    };

    private static boolean saveNetwork(String filename, List<GUINodeItem> hardwareItems, List<GUICableItem> kabelItems, 
    		                           List<GUIDocItem> docuItems) {
        Main.debug.println("INVOKED (static) filius.rahmenprogramm.SzenarioVerwaltung, netzwerkSpeichern(" + filename + "," +
                           hardwareItems + "," + kabelItems + ")");
        
        XMLEncoder mx = null;
        FileOutputStream fos = null;

        if (Thread.currentThread().getContextClassLoader() != FiliusClassLoader.getInstance(Thread.currentThread()
                .getContextClassLoader()))
            Thread.currentThread().setContextClassLoader(
                    FiliusClassLoader.getInstance(Thread.currentThread().getContextClassLoader()));

        try {
            fos = new FileOutputStream(filename);
            mx = new XMLEncoder(new BufferedOutputStream(fos));
            mx.setExceptionListener(new ExceptionListener() {
                public void exceptionThrown(Exception arg0) {
                    arg0.printStackTrace(Main.debug);
                }
            });

            mx.writeObject(new String("Filius version: " + filius.rahmenprogramm.Information.getVersion()));
            mx.writeObject(hardwareItems);
            mx.writeObject(kabelItems);
            mx.writeObject(docuItems);

            return true;
        } catch (java.lang.RuntimeException e) {
            Main.debug
                    .println("EXCEPTION: java.lang.RuntimeException raised; Java internal problem, not Filius related!");
            return false;
        } catch (FileNotFoundException e2) {
            e2.printStackTrace(Main.debug);

            return false;
        } catch (Exception e) {
            return false;
        } finally {

            if (mx != null)
                mx.close();
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {}
            }
        }
    }

    public boolean load(String filename, List<GUINodeItem> hardwareItems, List<GUICableItem> kabelItems,
                        List<GUIDocItem> docuItems) throws FileNotFoundException {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + ", laden(" + filename + "," + 
                           hardwareItems + "," + kabelItems + ")");

        boolean success = true;
        String tmpDir;

        // Main.debug.println("SzenarioVerwaltung: Laden des Projekts aus Datei "+datei);
        tmpDir = Information.getInstance().getTempPath();

        deleteFiles(tmpDir + "projekt");

        if (success && !deleteDirectoryContent(Information.getInstance().getApplicationsPath())) {
            Main.debug.println("ERROR (" + this.hashCode() + "): Loeschen vorhandener Anwendungen fehlgeschlagen");
        }

        if (!extractZipArchive(filename, tmpDir)) {
            Main.debug.println("ERROR (" + this.hashCode() + "): Entpacken des Zip-Archivs fehlgeschlagen");
            success = false;
        }

        if (success
                && !copyDirectory(tmpDir + "projekt/anwendungen", Information.getInstance()
                        .getApplicationsPath())) {
            Main.debug.println("ERROR (" + this.hashCode() + "): Kopieren der Anwendungen fehlgeschlagen");
        }

        if (success && !loadNetwork(tmpDir + "projekt/konfiguration.xml", hardwareItems, kabelItems, docuItems)) {
            Main.debug.println("ERROR (" + this.hashCode() + "): Laden der Netzwerkkonfiguration fehlgeschlagen");
            success = false;
        }

        if (success) {
            path = filename;
            modified = false;

            fireStatusChange();
        }

        return success;
    }
    
    // Check if the XML requires being updated.
    // If so, create a temporary XML file with the appropriate changes.
    private static boolean fixXML(String filename, String tmpFilename) {
    	
    	// Check if the version of the data requires being updated
    	// The version is located in the third line of the XML file :
    	//   <string>Filius version: 1.9.0 (02.05.2020)</string> 
    	//if (version > ???) return false;
    	
    	// Create a copy of the file and apply the changes, line by line
    	try {
            BufferedReader bufReader = new BufferedReader(new FileReader(filename));            
            BufferedWriter bufWriter = new BufferedWriter(new FileWriter(tmpFilename, false));
 
            String line;
 
            while ((line = bufReader.readLine()) != null) {            	
            	
            	// A few classes and properties were renamed during refactoring.
            	// They are changed here before the XML is decoded.
            	// This way, older projects can be opened.
            	
            	// File and Treenode related
            	line = line.replaceAll("javax.swing.tree.DefaultMutableTreeNode", "filius.software.system.FiliusFileNode");
            	line = line.replaceAll("DefaultMutableTreeNode", "FiliusFileNode");
            	line = line.replaceAll("filius.software.system.Datei", "filius.software.system.FiliusFile");            	
            	line = line.replaceAll("\"Dateisystem\"", "\"FiliusFileSystem\"");
            	line = line.replaceAll("arbeitsVerzeichnis", "root");
            	line = line.replaceAll("dateiInhalt", "content");
            	line = line.replaceAll("\"dateiTyp\"", "\"type\"");        
            	
            	// Network related            	
            	line = line.replaceAll("filius.hardware.NetzwerkInterface", "filius.hardware.NetworkInterface");
            	line = line.replaceAll("filius.hardware.knoten.Rechner", "filius.hardware.knoten.Computer");
            	line = line.replaceAll("filius.hardware.knoten.Vermittlungsrechner", "filius.hardware.knoten.Router");
            	line = line.replaceAll("filius.hardware.Kabel", "filius.hardware.Cable");  
            	line = line.replaceAll("dasKabel", "cable");
            	line = line.replaceAll("kabelpanel", "cablePanel");
            	line = line.replaceAll("anschluesse", "ports");
            	line = line.replaceAll("netzwerkInterfaces", "networkInterfaces");
            	line = line.replaceAll("subnetzMaske", "subnetMask");  
            	line = line.replaceAll("\"ziel1\"", "\"nodeItem1\"");
            	line = line.replaceAll("\"ziel2\"", "\"nodeItem2\""); 
            	
            	// Node related
            	line = line.replaceAll("filius.gui.netzwerksicht.GUIKnotenItem", "filius.gui.netzwerksicht.GUINodeItem");
            	line = line.replaceAll("filius.gui.netzwerksicht.GUIKabelItem", "filius.gui.netzwerksicht.GUICableItem");
            	line = line.replaceAll("filius.gui.netzwerksicht.JSidebarButton", "filius.gui.netzwerksicht.JNodeLabel");
            	line = line.replaceAll("\"knoten\"", "\"node\"");  
            	line = line.replaceAll("\"imageLabel\"", "\"nodeLabel\"");
            	
            	// Application related
            	line = line.replaceAll("filius.hardware.knoten.Rechner", "filius.hardware.knoten.Computer");
            	line = line.replaceAll("filius.hardware.knoten.Vermittlungsrechner", "filius.hardware.knoten.Router");
            	line = line.replaceAll("installierteAnwendungen", "installedApps");
            	line = line.replaceAll("\"dateisystem\"", "\"fileSystem\"");
            	
            	// Doc related
            	line = line.replaceAll("filius.gui.netzwerksicht.GUIDocuItem", "filius.gui.dokusicht.GUIDocItem");
            	line = line.replaceAll("filius.gui.netzwerksicht.GUIDocItem", "filius.gui.dokusicht.GUIDocItem");
            	line = line.replaceAll("filius.gui.netzwerksicht.GUIDocPanel", "filius.gui.dokusicht.GUIDocPanel");
          	            	
            	// Save modified line
            	bufWriter.write(line);
            	bufWriter.newLine();  
            }
            
            bufWriter.close();            
            bufReader.close();
 
        } catch (IOException e) {
            e.printStackTrace(Main.debug);
        }
    	
    	return true;
    }

    private static boolean loadNetwork(String filename, List<GUINodeItem> hardwareItems,
                                       List<GUICableItem> kabelItems, List<GUIDocItem> docuItems) {
        Main.debug.println("INVOKED (static) filius.rahmenprogramm.SzenarioVerwaltung, netzwerkLaden(" + 
                           filename + "," + hardwareItems + "," + kabelItems + ")");
        
        Object tmpObject = null;

        if (Thread.currentThread().getContextClassLoader() != FiliusClassLoader.getInstance(Thread.currentThread()
                .getContextClassLoader()))
            Thread.currentThread().setContextClassLoader(
                    FiliusClassLoader.getInstance(Thread.currentThread().getContextClassLoader()));

        // Substitute a modified version of the XML file if necessary
        String tmpFilename = filename+"_tmp";  
        boolean needsFix = fixXML(filename, tmpFilename); 
        if (needsFix) filename = tmpFilename;

        boolean success = false;
        try (XMLDecoder xmldec = new XMLDecoder(new BufferedInputStream(new FileInputStream(filename)))) {
            xmldec.setExceptionListener(new ExceptionListener() {
                public void exceptionThrown(Exception arg0) {
                    arg0.printStackTrace(Main.debug);
                }
            });

            Information.getInstance().reset();
            tmpObject = xmldec.readObject();

            // in newer versions of Filius the version information is put into
            // the saved file as well
            // WARNING: former versions expect LinkedList as first element in
            // the saved file!
            if (tmpObject instanceof String) {
                String versionInfo = (String) tmpObject;
                Main.debug.println("File saved by Filius in version '"
                        + versionInfo.substring(versionInfo.indexOf(":") + 2) + "'");
                if (versionInfo.substring(versionInfo.indexOf(":") + 2).compareTo(
                        filius.rahmenprogramm.Information.getVersion()) < 0) {
                    Main.debug
                            .println("WARNING: current Filius version is newer ("
                                    + filius.rahmenprogramm.Information.getVersion()
                                    + ") than version of scenario file, such that certain elements might not be rendered correctly any more!");
                } else if (versionInfo.substring(versionInfo.indexOf(":") + 2).compareTo(
                        filius.rahmenprogramm.Information.getVersion()) > 0) {
                    Main.debug
                            .println("WARNING: current Filius version is older ("
                                    + filius.rahmenprogramm.Information.getVersion()
                                    + ") than version of scenario file, such that certain elements might not be rendered correctly!");
                } else {
                    Main.debug.println("\t...good, current version of Filius is equal to version of scenario file");
                }
                tmpObject = null;
            } else {
                Main.debug.println("WARNING: Version information of Filius scenario file could not be determined!");
                Main.debug
                        .println("WARNING: This usually means, the scenario file was created with Filius before version 1.3.0.");
                Main.debug.println("WARNING: Certain elements might not be rendered correctly any more!");
            }

            hardwareItems.clear();
            kabelItems.clear();
            docuItems.clear();

            if (tmpObject == null) {
                tmpObject = xmldec.readObject();
            }
            if (tmpObject instanceof List && !((List<?>) tmpObject).isEmpty()
                    && ((List<?>) tmpObject).get(0) instanceof GUINodeItem) {
                @SuppressWarnings("unchecked")
				List<GUINodeItem> tempList = (List<GUINodeItem>) tmpObject;
                for (GUINodeItem nodeItem : tempList) {
                	hardwareItems.add(nodeItem);              
                }
            }

            tmpObject = xmldec.readObject();
            if (tmpObject instanceof List && !((List<?>) tmpObject).isEmpty()
                    && ((List<?>) tmpObject).get(0) instanceof GUICableItem) {
            	@SuppressWarnings("unchecked")
                List<GUICableItem> tempList = (List<GUICableItem>) tmpObject;
                for (GUICableItem cable : tempList) {
                    kabelItems.add(cable);
                }
            }

            tmpObject = xmldec.readObject();
            if (tmpObject instanceof List && !((List<?>) tmpObject).isEmpty()
                    && ((List<?>) tmpObject).get(0) instanceof GUIDocItem) {
            	@SuppressWarnings("unchecked")
                List<GUIDocItem> tempList = (List<GUIDocItem>) tmpObject;
                for (GUIDocItem docuItem : tempList) {
                    docuItems.add(docuItem);
                }
            }
            success = true;
        } catch (FileNotFoundException e) {
            GUIErrorHandler.getGUIErrorHandler().DisplayError(messages.getString("rp_szenarioverwaltung_msg5"));
            e.printStackTrace(Main.debug);
            success = false;
        } catch (ArrayIndexOutOfBoundsException e) {
            Main.debug.println("Incomplete project file " + filename);
            success = true;
        }
        
        // Delete the temporary XML file
        if (needsFix) {
        	try {
        		java.nio.file.Files.deleteIfExists(Paths.get(tmpFilename));
        		
        	} catch (IOException e) {                
                e.printStackTrace(Main.debug);
            }
        }
        
        return success;
    }

    public static boolean createZipArchive(String dataDirectory, String archiveFilename) {
        Main.debug.println("INVOKED (static) filius.rahmenprogramm.SzenarioVerwaltung, erzeugeZipArchiv(" + 
                           dataDirectory + "," + archiveFilename + ")");
        
        FileOutputStream out;
        ZipOutputStream zipOut;
        File zipFilename;
        File directory;

        zipFilename = new File(archiveFilename);
        new File(zipFilename.getParent()).mkdirs();

        directory = new File(dataDirectory);
        if (!directory.exists())
            return false;

        try {
            zipFilename.createNewFile();
        } catch (IOException e) {
            e.printStackTrace(Main.debug);
            return false;
        }

        try {
            out = new FileOutputStream(zipFilename);
            zipOut = new ZipOutputStream(out);
            writeZipFile(zipOut, directory.getName() + "/", directory.getAbsolutePath());
            try {
                zipOut.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace(Main.debug);
                return false;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace(Main.debug);
            return false;
        }

        return true;
    }

    private static boolean writeZipFile(ZipOutputStream out, String relPfad, String filename) {
        Main.debug.println("INVOKED (static) filius.rahmenprogramm.SzenarioVerwaltung, schreibeZipDatei(" + out + "," +
                            relPfad + "," + filename + ")");
        
        File path;
        boolean result = true;

        path = new File(filename);
        if (path.isFile()) {
            return writeZipEntry(out, relPfad, filename);
        }

        for (File file : path.listFiles()) {
            if (file.isDirectory()) {
                result = writeZipFile(out, relPfad + file.getName() + "/", file.getAbsolutePath() + "/");
            } else {
                result = writeZipEntry(out, relPfad + file.getName(), file.getAbsolutePath());
            }
            if (!result)
                return result;
        }

        return result;
    }

    private static boolean writeZipEntry(ZipOutputStream out, String relPfad, String filename) {
        Main.debug.println("INVOKED (static) filius.rahmenprogramm.SzenarioVerwaltung, schreibeZipEntry(" + out + "," +
                           relPfad + "," + filename + ")");
        
        ZipEntry zipEntry;
        byte[] buffer = new byte[0xFFFF];
        File sourceFilename;

        sourceFilename = new File(filename);
        if (!sourceFilename.exists())
            return false;

        zipEntry = new ZipEntry(relPfad);
        try (FileInputStream fis = new FileInputStream(sourceFilename)) {
            out.putNextEntry(zipEntry);
            for (int len; (len = fis.read(buffer)) != -1;) {
                out.write(buffer, 0, len);
            }
            out.closeEntry();
        } catch (Exception e) {
            Main.debug.println("ERROR (static): Datei " + filename + " konnte nicht zu zip-Archiv hinzugefuegt werden.");
            e.printStackTrace(Main.debug);
            return false;
        }

        return true;
    }

    public static boolean extractZipArchive(String archiveFilename, String targetDir) {
        Main.debug.println("INVOKED (static) filius.rahmenprogramm.SzenarioVerwaltung, entpackeZipArchiv(" +
                           archiveFilename + "," + targetDir + ")");
        
        ZipFile zf;
        File file;
        InputStream is;
        BufferedInputStream bis;
        FileOutputStream fos;
        BufferedOutputStream bos;

        file = new File(archiveFilename);
        if (!file.exists()) {
            Main.debug.println("ERROR (static): " + archiveFilename + " existiert nicht. Entpacken ist fehlgeschlagen!");
            return false;
        }

        file = new File(targetDir);
        if (!file.exists()) {
            file.mkdirs();
        }

        try {
            zf = new ZipFile(archiveFilename);

            for (Enumeration<? extends ZipEntry> e = zf.entries(); e.hasMoreElements();) {
                ZipEntry target = e.nextElement();

                file = new File(targetDir + target.getName());

                if (target.isDirectory())
                    file.mkdirs();
                else {
                    is = zf.getInputStream(target);
                    bis = new BufferedInputStream(is);

                    new File(file.getParent()).mkdirs();

                    fos = new FileOutputStream(file);
                    bos = new BufferedOutputStream(fos);

                    final int EOF = -1;

                    for (int c; (c = bis.read()) != EOF;)
                        bos.write((byte) c);
                    bos.close();
                    fos.close();

                    is.close();
                    bis.close();
                }

            }

            zf.close();
        } catch (FileNotFoundException e) {
            Main.debug.println("EXCEPTION (static): zipfile not found");
            return false;
        } catch (ZipException e) {
            Main.debug.println("EXCEPTION (static): zip error...");
            return false;
        } catch (IOException e) {
            Main.debug.println("EXCEPTION (static): IO error...");
            return false;
        }
        return true;
    }

    public static boolean deleteDirectoryContent(String directory) {
        // Main.debug.println("INVOKED (static) filius.rahmenprogramm.SzenarioVerwaltung, loescheVerzeichnisInhalt("+verzeichnis+")");
        File path;
        File file;
        File[] fileListe;

        path = new File(directory);

        if (path.exists()) {
            fileListe = path.listFiles();
            for (int i = 0; i < fileListe.length; i++) {
                file = fileListe[i];
                if (file.isDirectory()) {
                    if (!deleteFiles(file.getAbsolutePath())) {
                        Main.debug.println("ERROR (static): Ordner " + file.getAbsolutePath()
                                + " konnte nicht geloescht werden.");
                        return false;
                    }
                } else if (!file.delete()) {
                    Main.debug.println("ERROR (static): Datei " + file.getAbsolutePath()
                            + " konnte nicht geloescht werden.");
                    return false;
                } else {

                }
            }
        }
        return true;
    }

    public static boolean deleteFiles(String file) {
        Main.debug.println("INVOKED (static) filius.rahmenprogramm.SzenarioVerwaltung, loescheDateien(" + file + ")");
        File path;

        path = new File(file);

        if (!deleteDirectoryContent(file))
            return false;

        if (path.delete()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean copyDirectory(String source, String destination) {
        Main.debug.println("INVOKED (static) filius.rahmenprogramm.SzenarioVerwaltung, kopiereVerzeichnis(" + 
                           source + "," + destination + ")");
        
        File sourceDir, targetDir, tmp;

        sourceDir = new File(source);
        targetDir = new File(destination);

        if (!sourceDir.exists())
            return false;

        if (!targetDir.exists())
            targetDir.mkdirs();
        for (File file : sourceDir.listFiles()) {
            if (file.isDirectory()) {
                tmp = new File(targetDir.getAbsolutePath() + "/" + file.getName());
                copyDirectory(file.getAbsolutePath(), tmp.getAbsolutePath());
            } else
                copyFile(file.getAbsolutePath(), targetDir.getAbsolutePath() + "/" + file.getName());

        }

        return true;

    }

    public static boolean saveStream(InputStream source, String targetFilename) {
        Main.debug.println("INVOKED (static) filius.rahmenprogramm.SzenarioVerwaltung, saveStream(" + 
                           source + "," + targetFilename + ")");
        
        File destfile;
        FileOutputStream fos = null;
        byte[] buffer;
        boolean result = true;

        destfile = new File(targetFilename);

        if (source == null || destfile.exists())
            result = false;
        else {
            try {
                fos = new FileOutputStream(destfile);

                buffer = new byte[0xFFFF];

                for (int len; (len = source.read(buffer)) != -1;)
                    fos.write(buffer, 0, len);

            } catch (IOException e) {
                e.printStackTrace(Main.debug);
                result = false;
            } finally {
                if (fos != null)
                    try {
                        fos.close();
                    } catch (IOException e) {}
            }
        }

        return result;
    }

    public static boolean copyFile(String sourceFilename, String targetFilename) {
        Main.debug.println("INVOKED (static) filius.rahmenprogramm.SzenarioVerwaltung, kopiereDatei(" + 
                           sourceFilename + "," + targetFilename + ")");
        File srcfile, destfile;
        FileInputStream fis = null;
        FileOutputStream fos = null;
        byte[] buffer;
        boolean result = true;

        srcfile = new File(sourceFilename);
        destfile = new File(targetFilename);

        if (!srcfile.exists() || destfile.exists())
            result = false;
        else {
            try {
                fis = new FileInputStream(srcfile);
                fos = new FileOutputStream(destfile);

                buffer = new byte[0xFFFF];

                for (int len; (len = fis.read(buffer)) != -1;)
                    fos.write(buffer, 0, len);

            } catch (IOException e) {
                e.printStackTrace(Main.debug);
                result = false;
            } finally {
                if (fis != null)
                    try {
                        fis.close();
                    } catch (IOException e) {}
                if (fos != null)
                    try {
                        fos.close();
                    } catch (IOException e) {}
            }
        }

        return result;
    }
    
    //------------------------------------------------------------------------------------------------
    // Listeners management
    //------------------------------------------------------------------------------------------------ 

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);     
    
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }
    
    // Notify the listener that the project's status and/or name changed
    // Fired by: ProjectManager
    // Listened to by: JMainFrame
    protected void fireStatusChange() {
        pcs.firePropertyChange("statusAndName", null, null);
    }
}
