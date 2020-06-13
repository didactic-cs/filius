/*
 ** This file is part of Filius, a network construction and simulation software.
 **
 ** Originally created at the University of Siegen, Institute "Didactics of
 ** Informatics and E-Learning" by a students' project group:
 **     members (2006-2007):
 **         Andr� Asschoff, Johannes Bade, Carsten Dittich, Thomas Gerding,
 **         Nadja Ha�ler, Ernst Johannes Klebert, Michell Weyer
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
package filius.software.system;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import filius.Main;

/**
 * The class <b>FiliusFileSystem</b> reproduces in Filius the behavior of a filesystem.
 * It is structured as a tree of TreeNodes. Each file and directory being associated 
 * to a node, the Java serialization allows an easy way to store the whole filesystem
 * within each Filius project file. <br>
 * The userObject of each node is either: <br>
 *  - a String object containing the name of a directory <br>
 *  - a Datei object containing the information relative to a file. <br>  
 *
 * @see javax.swing.tree.DefaultMutableTree
 * @see filius.software.system.Datei
 */
public class FiliusFileSystem implements Serializable {

    private static final long serialVersionUID = 1L;

    // Character used as separator in a path
    public static final String FILE_SEPARATOR = "/";

    // Root node, similar to the "/" mount point used in some operating system
    private DefaultMutableTreeNode root;

    // Current working directory
    private DefaultMutableTreeNode workingDirectory;

    /**
     * <b>FiliusFileSystem</b> models a filesystem to be used by the virtual applications.<br>
     * In order to store the filesystem's content in a Filius project file, each file and directory
     * is associated with a TreeNode. The Java serialization is used. <br>
     * It is important to keep this constructor without parameter!
     */
    public FiliusFileSystem() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (FiliusFileSystem), constr: FiliusFileSystem()");
        root = new DefaultMutableTreeNode("root");
        workingDirectory = root;
    }

    //******************************************************************************************
    //  Getters and setters
    //******************************************************************************************

    /**
     * <b>getRoot</b> returns the root node of the filesystem.
     *
     * @return The TreeNode correponding to the root of the hierarchy.
     */
    public DefaultMutableTreeNode getRoot() {
        return root;
    }

    // Never used method
//    public void setRoot(DefaultMutableTreeNode root) {
//        this.root = root;
//    }

    /**
     * <b>getWorkingDirectory</b> returns the node corresponding to the current working directory.
     *
     * @return The TreeNode corresponding to the current working directory.
     */
    public DefaultMutableTreeNode getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * <b>setWorkingDirectory</b> sets the node to be used as the current working directory.
     *
     * @param directoryNode The node to be used as the current working directory.
     */
    public void setWorkingDirectory(DefaultMutableTreeNode directoryNode) {
        this.workingDirectory = directoryNode;
    }

    /**
     * <b>getArbeitsVerzeichnis</b> is identical to getWorkingDirectory().<br><br>
     * 
     * <b>Do not call directly!</b> Use getWorkingDirectory() instead.<br><br>
     *
     * <i>Required for backward compatibility. This method is maintained because it is necessary
     * to correctly load file systems saved as Systemdatei in previous versions. 
     * getArbeitsVerzeichnis() is called during deserialization through invoke().</i> 
     */
    public DefaultMutableTreeNode getArbeitsVerzeichnis() {
        return workingDirectory;
    }

    //******************************************************************************************
    //  File and directory operations
    //******************************************************************************************

    /**
     * <b>existsFile</b> checks whether a file or directory exists. <br>
     * No recursive search in the subdirectories is done!
     *
     * @param directoryNode
     *            TreeNode of the directory to be searched in.
     * @param fileName
     *            String containing the file's or directory's name to be searched.
     * @return true when a file or a directory with the given name was found.
     */
    public boolean existsFile(DefaultMutableTreeNode directoryNode, String fileName) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (FiliusFileSystem), existsFile("
                + directoryNode + "," + fileName + ")");
        DefaultMutableTreeNode enode;

        if (directoryNode == null) {
            return false;
        } else {
            for (Enumeration e = directoryNode.children(); e.hasMoreElements();) {
                enode = (DefaultMutableTreeNode) e.nextElement();

                if (enode.getUserObject().toString().equalsIgnoreCase(fileName)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * <b>existsFile</b> checks whether a file or directory exists. <br>
     * No recursive search in the subdirectories is done!
     *
     * @param directoryPath
     *            String containing the the absolute path of the directory to be searched in.
     * @param fileName
     *            String containing the file's or directory's name to be searched.
     * @return true when a file or a directory with the given name was found.
     */
    public boolean existsFile(String directoryPath, String fileName) {
        return existsFile(absolutePathToNode(directoryPath), fileName);
    }

    /**
     * <b>deleteFile</b> removes the given file or directory from the filesystem
     *
     * @param filePath
     *            String containing the file's or directory's absolute path.
     * @return true when a file or a directory was removed.
     */
    public boolean deleteFile(String filePath) {
        Main.debug.println(
                "INVOKED (" + this.hashCode() + ") " + getClass() + " (FiliusFileSystem), deleteFile(" + filePath + ")");
        DefaultMutableTreeNode node = absolutePathToNode(filePath);
        if (node != null) {
            node.removeFromParent();
            return true;
        } else {
            return false;
        }
    }

    /**
     * <b>createDirectory</b> creates a subdirectory in the given directory. If a subdirectory or file with 
     * the given name already exists, nothing happens.
     *
     * @param directoryPath
     *            String containing the absolute path of the directory in which the subdirectory is to be created.
     * @param newDirectory
     *            String containing the name of the subdirectory to be created.
     * @return true when the directory was created or already existed.
     */
    public boolean createDirectory(String directoryPath, String newDirectory) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (FiliusFileSystem), createDirectory("
                + directoryPath + "," + newDirectory + ")");
        DefaultMutableTreeNode node;
        DefaultMutableTreeNode neuerNode = null;
        String absPath;
        if (newDirectory.length() > 0 && newDirectory.substring(0, 1).equals(FILE_SEPARATOR)) { // 'pfad'
                                                                                                        // is
                                                                                                        // absolute
                                                                                                        // path!
            absPath = evaluatePath(newDirectory);
        } else {
            absPath = evaluatePath(directoryPath + FILE_SEPARATOR + newDirectory);
        }
        directoryPath = getPathDirectory(absPath);
        newDirectory = getPathBasename(absPath);

        node = absolutePathToNode(directoryPath);
        if (node != null) {
            if (existsFile(node, newDirectory)) {
                Main.debug.println(
                        "WARNING (" + this.hashCode() + "): Verzeichnis " + newDirectory + " wurde nicht erzeugt, "
                                + "weil es im Verzeichnis " + directoryPath + " bereits existiert.");
            } else {
                neuerNode = new DefaultMutableTreeNode(newDirectory);
                node.add(neuerNode);
                // Main.debug.println("DEBUG ("+this.hashCode()+"): Verzeichnis "
                // + neuesVerzeichnis + " wurde erstellt.");
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * <b>createDirectory</b> creates a subdirectory in the given directory. If a subdirectory or file with 
     * the given name already exists, nothing happens.
     *
     * @param directoryNode
     *            TreeNode of the directory in which the subdirectory is to be created.
     * @param newDirectory
     *            String containing the name of the subdirectory to be created.
     * @return true when the directory was created or already existed.
    */
    public boolean createDirectory(DefaultMutableTreeNode directoryNode, String newDirectory) {
        return createDirectory(nodeToAbsolutePath(directoryNode), newDirectory);
    }

    /**
     * <b>getDirectoryObjects</b> returns a list of all the objects of a given node.
     * There is an object for each direct child node. <br>
     * If the child node corresponds to a subdirectory, the object is a String containing the name of the subdirectory. <br>
     * If the child node corresponds to a file, the object is a Datei object. 
     *
     * @param directoryNode
     *            TreeNode the subnodes of which are to be listed.
     * @return Returns a list of all the objects of the child nodes of the given node. Returns null if the node does not exist.
     */
    public LinkedList<Object> getDirectoryObjects(DefaultMutableTreeNode directoryNode) {
        Main.debug
                .println("INVOKED (" + this.hashCode() + ") " + getClass() + " (FiliusFileSystem), getDirectorySubnodes(" + directoryNode + ")");
        LinkedList<Object> liste = new LinkedList<Object>();
        Enumeration enumeration;
        DefaultMutableTreeNode tmpNode;

        if (directoryNode == null) {
            return null;
        } else {
            enumeration = directoryNode.children();
            while (enumeration.hasMoreElements()) {
                tmpNode = (DefaultMutableTreeNode) enumeration.nextElement();
                liste.addLast(tmpNode.getUserObject());
            }
            return liste;
        }
    }
    

    //******************************************************************************************
    //  Datei operations
    //******************************************************************************************
    
    /**
     * <b>getDatei</b> returns the Datei object corresponding to the given path.
     *
     * @param filePath
     *            String containing the file's absolute path.
     * @return The Datei object associated to the path, or "null" if none was found.
     */
    public Datei getDatei(String filePath) {
        Main.debug.println(
                "INVOKED (" + this.hashCode() + ") " + getClass() + " (FiliusFileSystem), getDatei(" + filePath + ")");
        DefaultMutableTreeNode node;

        node = absolutePathToNode(filePath);
        if (node != null && (node.getUserObject() instanceof Datei)) {
            // Main.debug.println("DEBUG ("+this.hashCode()+") "+getClass()+", holeDatei: return='"+(Datei)
            // node.getUserObject()+"'");
            return (Datei) node.getUserObject();
        } else {
            // Main.debug.println("DEBUG ("+this.hashCode()+") "+getClass()+", holeDatei: return=<null>");
            return null;
        }
    }

    /**
     * <b>getDatei</b> returns the Datei object corresponding to the given node and path.
     *
     * @param directoryNode
     *            TreeNode to which the path is relative.
     * @param filePath
     *            String containing the file's relative path.
     * @return The Datei object associated to the path, or "null" if none was found.
     */
    public Datei getDatei(DefaultMutableTreeNode directoryNode, String filePath) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (FiliusFileSystem), getDatei("
                + directoryNode + "," + filePath + ")");
        String absoluterDateiPfad;

        absoluterDateiPfad = nodeToAbsolutePath(directoryNode) + FILE_SEPARATOR + filePath;
        return getDatei(absoluterDateiPfad);
    }

    /**
     * <b>getDatei</b> returns the Datei object corresponding to the given node and path.
     *
     * @param directoryNode
     *            String containing the absolute path of a directory to which the filePath is relative.
     * @param filePath
     *            String containing the file's relative path.
     * @return The Datei object associated to the path, or "null" if none was found.
     */
    public Datei getDatei(String directoryPath, String filePath) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (FiliusFileSystem), getDatei("
                + directoryPath + "," + filePath + ")");
        String absoluterDateiPfad;

        absoluterDateiPfad = directoryPath + FILE_SEPARATOR + filePath;
        return getDatei(absoluterDateiPfad);
    }
    
    /**
     * Methode zum speichern einer Datei. Existiert die Datei in dem angegebenen Verzeichnis bereits, wird sie
     * ueberschrieben! Existiert der Knoten im Verzeichnisbaum noch nicht, wird er angelegt.
     *
     * @param directoryPath
     *            absoluter Pfad des Verzeichnisses, in dem die Datei gespeichert werden soll
     * @param datei
     *            der Dateiname der zu speichernden Datei
     * @return ob das Speichern erfolgreich war
     */
    public boolean saveDatei(String directoryPath, Datei datei) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (FiliusFileSystem), saveDatei(" + directoryPath
                + "," + datei + ")");
        DefaultMutableTreeNode node = null;

        node = absolutePathToNode(directoryPath);

        if (node != null) {
            if (!existsFile(node, datei.getName())) {
                DefaultMutableTreeNode dateiNode = new DefaultMutableTreeNode(datei);
                node.add(dateiNode);
            } else {
                node = absolutePathToNode(directoryPath + FILE_SEPARATOR + datei.getName());
                Datei file = (Datei) node.getUserObject();
                file.setContent(datei.getContent());
                file.setType(datei.getType());
                file.setSize(datei.getSize());
            }
            return true;
        } else {
            Main.debug.println("ERROR (" + this.hashCode() + "): Datei " + datei + " konnte nicht gespeichert werden, "
                    + "weil Verzeichnis " + directoryPath + " nicht existiert.");
            return false;
        }
    }

    /**
     * Methode zum speichern einer Datei. Existiert die Datei in dem angegebenen Verzeichnis bereits, wird sie
     * ueberschrieben! Existiert der Knoten im Verzeichnisbaum noch nicht, wird er angelegt. <br />
     * Diese Methode verwendet speicherDatei(String, String).
     *
     * @param directoryNode
     *            Verzeichnis, in dem die Datei gespeichert werden soll
     * @param datei
     *            der Dateiname der zu speichernden Datei
     * @return ob das Speichern erfolgreich war
     */
    public boolean saveDatei(DefaultMutableTreeNode directoryNode, Datei datei) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (FiliusFileSystem), saveDatei(" + directoryNode + ","
                + datei + ")");
        return saveDatei(nodeToAbsolutePath(directoryNode), datei);
    }
        
    /**
     * <b>getDateiList</b> returns a list of all the Datei objects belonging to a node.
	 *
     * @param directoryNode
     *            TreeNode of the directory the Datei objects of which are to be listed.
     * @return A list of all the Datei objects belonging to the node.
     */
    public List<Datei> getDateiList(DefaultMutableTreeNode directoryNode) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (FiliusFileSystem), getDateiList(" + directoryNode + ")");
        List<Datei> liste = new LinkedList<Datei>();

        if (directoryNode == null) {
            return null;
        } else {
            for (Enumeration<TreeNode> e = directoryNode.children(); e.hasMoreElements();) {
                DefaultMutableTreeNode n = (DefaultMutableTreeNode) e.nextElement();
                if (n.getUserObject() instanceof Datei) {
                    Datei dat = (Datei) n.getUserObject();
                    liste.add(dat);
                }
            }
            return liste;
        }
    }
    
    /**
     * <b>findDateiList</b> returns a list of Datei objects matching the search pattern.
	 *
     * @param directoryPath
     *            String containing the absolute path of the directory in which the search is done.
     * @param searchPattern
     *            String containing the pattern to look for in the names of the Datei objects.
     * @return A list of Datei objects the name of which contain the searchPattern.
     */
    public LinkedList<Datei> findDateiList(String directoryPath, String searchPattern) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (FiliusFileSystem), findDateiList("
                + directoryPath + "," + searchPattern + ")");
        LinkedList<Datei> dateien = new LinkedList<Datei>();
        DefaultMutableTreeNode verzeichnisNode, node;
        Datei tmpDatei;

        verzeichnisNode = absolutePathToNode(directoryPath);

        for (Enumeration e = verzeichnisNode.children(); e.hasMoreElements();) {
            node = (DefaultMutableTreeNode) e.nextElement();

            if (node.getUserObject() instanceof Datei) {
                tmpDatei = (Datei) node.getUserObject();
                if (tmpDatei.getName().toLowerCase().matches("(.+)?" + searchPattern.toLowerCase() + "(.+)?")) {
                    dateien.addLast(tmpDatei);
                }
            }
        }
        return dateien;
    }


    //******************************************************************************************
    //  Path/node conversions
    //******************************************************************************************

    /**
     * <b>nodeToAbsolutePath</b> returns the path corresponding to a TreeNode.
	 *
     * @param node
     *            TreeNode the path of which is requested.
     * @return A String containing the absolute path corresponding to the node.
     * @see FILE_SEPARATOR
     */
    public static String nodeToAbsolutePath(DefaultMutableTreeNode node) {
        Main.debug.println("INVOKED (static) (FiliusFileSystem), nodeToAbsolutePath(" + node + ")");
        StringBuffer pfad;
        Object[] pfadObjekte;

        pfadObjekte = node.getUserObjectPath();
        pfad = new StringBuffer();
        pfad.append(pfadObjekte[0].toString());
        for (int i = 1; i < pfadObjekte.length; i++) {
            pfad.append(FILE_SEPARATOR + pfadObjekte[i].toString());
        }
        // Main.debug.println("\tpfad='"+stripRoot(pfad.toString())+"'");
        return stripRoot(pfad.toString());
    }

    public String rootToAbsolutePath() {
        return nodeToAbsolutePath(root);
    }

    /**
     * <b>absolutePathToNode</b> returns the TreeNode corresponding to the given path.
	 *
     * @param path
     *            String containing the absolute path of a file or directory.
     * @return TreeNode of the directory corresponding to the path
     * @see nodeToAbsolutePath(DefaultMutableTreeNode)
     */
    public DefaultMutableTreeNode absolutePathToNode(String path) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + ", absolutePathToNode(" + path + ")");
        path = stripRoot(evaluatePath(path));

        if (path.equals(FILE_SEPARATOR) || path.isEmpty()) {
            return root;
        }
        Enumeration enumeration;
        DefaultMutableTreeNode node;

        enumeration = root.preorderEnumeration();
        while (enumeration.hasMoreElements()) {
            node = (DefaultMutableTreeNode) enumeration.nextElement();
            // Main.debug.println("DEBUG: verzeichnisKnoten:\n\t'"+pfad+"' =?= '"+absoluterPfad(node)+"'");
            if (path.equalsIgnoreCase(nodeToAbsolutePath(node))) {
                return node;
            }
        }

        return null;
    }

    /**
     * <b>pathToNode</b> returns the TreeNode corresponding to the given path.
     *
     * @param directoryNode
     *            TreeNode to which the path is relative.
     * @param path
     *            String containing the path of a file or directory relative to the directoryNode.
     * @return TreeNode of the directory corresponding to the path
     * @see nodeToAbsolutePath(DefaultMutableTreeNode)
     */
    public static DefaultMutableTreeNode pathToNode(DefaultMutableTreeNode directoryNode, String path) {
        Main.debug.println("INVOKED (static) filius.software.system.Dateisystem, verzeichnisKnoten(" + directoryNode + ","
                + path + ")");
        Enumeration enumeration;
        DefaultMutableTreeNode node;
        String absolutePath;

        if (path.length() > 0 && path.substring(0, 1).equals(FILE_SEPARATOR)) { // 'pfad'
                                                                                // is
                                                                                // absolute
                                                                                // path!
            absolutePath = evaluatePath(path);
        } else {
            absolutePath = evaluatePath(nodeToAbsolutePath(directoryNode) + FILE_SEPARATOR + path);
        }

        enumeration = directoryNode.preorderEnumeration();
        while (enumeration.hasMoreElements()) {
            node = (DefaultMutableTreeNode) enumeration.nextElement();
            if (absolutePath.equalsIgnoreCase(nodeToAbsolutePath(node))) {
                return node;
            }
        }

        return null;
    }

    /**
     * <b>fixDirectory</b> fixes a set of nodes.
     * 
     * @param directoryNode TreeNode from which the fixing starts.
     */
    public void fixDirectory(DefaultMutableTreeNode directoryNode) {
        if (directoryNode.getAllowsChildren()) {
            for (int i = 0; i < directoryNode.getChildCount(); i++) {
                fixDirectory((DefaultMutableTreeNode) directoryNode.getChildAt(i));
            }
        }
        if (directoryNode.getUserObject() == null) {
            directoryNode.setUserObject("restored-" + System.currentTimeMillis());
        } else if (directoryNode.getUserObject() instanceof Datei && ((Datei) directoryNode.getUserObject()).getName().isEmpty()) {
            ((Datei) directoryNode.getUserObject()).setName("restored-" + System.currentTimeMillis());
        }
    }

    //******************************************************************************************
    //  Path helper methods
    //******************************************************************************************

    /**
     * <b>evaluatePath</b> evaluates a path containing '.' and '..' as special directories.
     * 
     * @param path String containing a path
     * @return A String containing the evaluated path.
     */
    public static String evaluatePath(String path) {
        Main.debug.println("INVOKED (static) filius.software.system.Dateisystem, evaluatePathString(" + path + ")");
        String result = "";
        StringTokenizer tk = new StringTokenizer(path, FiliusFileSystem.FILE_SEPARATOR);
        String[] pathElements = new String[tk.countTokens()];
        int currIndex = -1;
        String currString;
        while (tk.hasMoreTokens()) {
            currString = tk.nextToken();
            if (currString.equals("..")) {
                currIndex--;
            } else if (!currString.equals(".") && !currString.equals("")) {
                currIndex++;
                pathElements[currIndex] = currString;
            }
        }
        for (int i = 0; i <= currIndex; i++) { // NOTE: if currIndex<0, e.g.
                                               // because of multiple '..'
                                               // elements, then empty path will
                                               // be returned!
            result += pathElements[i];
            if (i < currIndex)
                result += FiliusFileSystem.FILE_SEPARATOR;
        }
        if (currIndex >= 0 && path.substring(0, 1).equals(FILE_SEPARATOR))
            result = FILE_SEPARATOR + result; // add leading slash if it was
                                              // present before
        // Main.debug.println(" \tevaluatePathString, result="+result);
        return result;
    }

    /**
     * <b>stripRoot</b> removes "root" from the beginning of an absolute path
     * 
     * @param path String containing a path
     * @return A String containing the absolute path starting with the FILE_SEPARATOR character.
     */
    private static String stripRoot(String path) {
        Main.debug.println("INVOKED (static) filius.software.system.Dateisystem, stripRoot(" + path + ")");
        if (path.indexOf(FiliusFileSystem.FILE_SEPARATOR) >= 0)
            return path.substring(path.indexOf(FiliusFileSystem.FILE_SEPARATOR));
        else
            return "/";
    }

    /**
     * <b>getPathDirectory</b> extracts the directories from a path
     * 
     * @param path String containing a path
     * @return A String containing the substring of path containing all characters before the last separator.
     * If path contains no separator, an empty string is returned.
     */
    public static String getPathDirectory(String path) {
        Main.debug.println("INVOKED (static) filius.software.system.Dateisystem, getDirectory(" + path + ")");
        if (path.lastIndexOf(FiliusFileSystem.FILE_SEPARATOR) >= 0)
            return path.substring(0, path.lastIndexOf(FiliusFileSystem.FILE_SEPARATOR));
        else
            return "";
    }

    /**
     * <b>getPathBasename</b> extracts the filename from a path
     * 
     * @param path String containing a path
     * @return A String containing the substring of path containing all characters after the last separator.
     * If path contains no separator, path is returned as is.
     */
    public static String getPathBasename(String path) {
        Main.debug.println("INVOKED (static) filius.software.system.Dateisystem, getBasename(" + path + ")");
        if (path.lastIndexOf(FiliusFileSystem.FILE_SEPARATOR) >= 0)
            return path.substring(path.lastIndexOf(FiliusFileSystem.FILE_SEPARATOR) + 1);
        else
            return path;
    }


    //******************************************************************************************
    //  Debugging methods
    //******************************************************************************************

    /**
     * <b>printTree</b> prints the whole tree structure (used for debugging)
     */
    public void printTree() {
        printSubtree("", root);
    }

    /**
     * <b>printTree</b> prints the tree structure starting from a given node (used for debugging)
     * 
     * @param indent String used for indentation
     * @param startNode TreeNode from which the printing starts
     */
    private void printSubtree(String indent, DefaultMutableTreeNode startNode) {
        DefaultMutableTreeNode node;
        Main.debug.print(indent + "--");
        if (startNode.getUserObject() instanceof Datei) {
            // Main.debug.println(tmpRoot.getUserObject().toString());
        } else {
            Main.debug.println("[" + startNode.getUserObject().toString() + "]");
        }
        indent = indent + " |";
        for (Enumeration e = startNode.children(); e.hasMoreElements();) {
            node = (DefaultMutableTreeNode) e.nextElement();
            printSubtree(indent, node);
        }
    }
}
