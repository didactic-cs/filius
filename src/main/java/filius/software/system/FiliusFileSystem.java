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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import filius.Main;
import filius.rahmenprogramm.Base64;

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
    
    public static enum errorCode {
    	NO_ERROR,    	
    	FILE_NOT_FOUND,
    	FILE_TOO_LARGE,
    	UNSPECIFIED
   	}

    // Character used as separator in a path
    public static final String FILE_SEPARATOR = "/";

    // Root node, similar to the "/" mount point used in some operating system
    private DefaultMutableTreeNode root;

    // Current working directory
    private DefaultMutableTreeNode workingDirectory;
    
    // Filetype map
    private transient HashMap<String, String> fileTypeMap = null;

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
        
        initFileTypeMap();		
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

    // Never used method (but should it ever be?)
//    public void setRoot(DefaultMutableTreeNode root) {
//    
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
     * <b>Do not call!</b> Use getWorkingDirectory() instead.<br><br>
     *
     * <i>Required for backward compatibility. This method is maintained because it is necessary
     * to correctly load file systems saved as Systemdatei in previous versions. 
     * getArbeitsVerzeichnis() is called during deserialization through invoke().</i> 
     */
    public DefaultMutableTreeNode getArbeitsVerzeichnis() {
    	
        return workingDirectory;
    }

    //******************************************************************************************
    //  File and directory related methods
    //******************************************************************************************

    /**
     * <b>fileExists</b> checks whether a file or directory exists.<br>
     * No recursive search in the subdirectories is done.<br>
     * The name comparison is not case sensitive.
     *
     * @param directoryNode
     *            TreeNode of the directory to be searched in.
     * @param fileName
     *            String containing the file's or directory's name to be searched.
     * @return true when a file or a directory with the given name was found.
     */
    public boolean fileExists(DefaultMutableTreeNode directoryNode, String fileName) {
    	
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + 
        		           " (FiliusFileSystem), existsFile(" + directoryNode + "," + fileName + ")");
        
        return (getChildIndex (directoryNode, fileName) > -1);
    }

    /**
     * <b>fileExists</b> checks whether a file or directory exists. <br>
     * No recursive search in the subdirectories is done!
     *
     * @param directoryPath
     *            String containing the the absolute path of the directory to be searched in.
     * @param fileName
     *            String containing the file's or directory's name to be searched.
     * @return true when a file or a directory with the given name was found.
     */
    public boolean fileExists(String directoryPath, String fileName) {
    	
        return fileExists(absolutePathToNode(directoryPath), fileName);
    }
    
    /**
     * <b>deleteFile</b> removes the given file or directory from the filesystem
     *
     * @param node
     *            TreeNode containing the file's or directory's to be deleted.
     * @return true if the deletion succeeded.
     */
    public boolean deleteFile(DefaultMutableTreeNode node) {
    	
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + 
        		           " (FiliusFileSystem), deleteFile(" + node + ")");
                
        if (node != null) {
            node.removeFromParent();
            return true;
        } 
        
        return false;    
    }

    /**
     * <b>deleteFile</b> removes the given file or directory from the filesystem
     *
     * @param filePath
     *            String containing the absolute path of the file or directory to be deleted.
     * @return true if the deletion succeeded.
     */
    public boolean deleteFile(String filePath) {
    	
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + 
        		           " (FiliusFileSystem), deleteFile(" + filePath + ")");
        
        return deleteFile(absolutePathToNode(filePath));          
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
    	
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + 
        		           " (FiliusFileSystem), createDirectory(" + directoryNode + "," + newDirectory + ")");
          
        if (directoryNode != null) {
            if (! fileExists(directoryNode, newDirectory)) {    
                addSubNode(directoryNode, new DefaultMutableTreeNode(newDirectory));
            }
            return true;
        }
            
        return false;
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
    	
    	Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + 
		           " (FiliusFileSystem), createDirectory(" + directoryPath + "," + newDirectory + ")");
     
        return createDirectory(absolutePathToNode(directoryPath), newDirectory);
    }



    /**
     * <b>getDirectoryObjectList</b> returns a list of all the objects of a given node.
     * There is an object for each child node. <br>
     * If the child node corresponds to a subdirectory, the object is a String containing the name of the subdirectory. <br>
     * If the child node corresponds to a file, the object is a Datei object. 
     *
     * @param directoryNode
     *            TreeNode the subnodes of which are to be listed.
     * @return Returns a list of all the objects of the child nodes of the given node. Returns null if the node does not exist.
     */
    public LinkedList<Object> getDirectoryObjectList(DefaultMutableTreeNode directoryNode) {
    	
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + 
        		           " (FiliusFileSystem), getDirectorySubnodes(" + directoryNode + ")");
        
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
    
    /**
     * <b>createFile</b> creates an empty file in the given directory. If a subdirectory or file with 
     * the given name already exists, nothing happens.
     *
     * @param directoryNode
     *            TreeNode of the directory in which the file is to be created.
     * @param newDirectory
     *            String containing the name of the file to be created.
     * @return true when the file was created or already existed.
    */
    public boolean createFile(DefaultMutableTreeNode directoryNode, String newFileName) {
    	
    	Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + 
		                   " (FiliusFileSystem), createFile(" + directoryNode + "," + newFileName + ")");  

    	if (directoryNode != null) {
    		if (! fileExists(directoryNode, newFileName)) {               
    			addSubNode(directoryNode, new DefaultMutableTreeNode(new Datei(newFileName, "", null)));
    		}
    		return true;
    	} 
    	return false;
    }
    
    /**
     * <b>createFile</b> creates an empty file in the given directory. If a subdirectory or file with 
     * the given name already exists, nothing happens.
     *
     * @param directoryPath
     *            String containing the absolute path of the directory in which the subdirectory is to be created.
     * @param newFileName
     *            String containing the name of the subdirectory to be created.
     * @return true when the file was created or already existed.
     */
    public boolean createFile(String directoryPath, String newFileName) {
    	
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + 
        		           " (FiliusFileSystem), createFile(" + directoryPath + "," + newFileName + ")");  
  
        return createFile(absolutePathToNode(directoryPath), newFileName);
    }
    

    //******************************************************************************************
    //  Datei related methods
    //******************************************************************************************
    
    /**
     * <b>getDatei</b> returns the Datei object associated to the given node.<br>
     * If the node is null or is a directory node, null is returned.
     *
     * @param node  
     *            TreeNode owning the Datei object.
     * @return The Datei object associated to the node, or "null" if none was found.
     */
    public Datei getDatei(DefaultMutableTreeNode node) {
    	
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + 
        		           " (FiliusFileSystem), getDatei(" + node + ")");
        
        if (node != null && (node.getUserObject() instanceof Datei)) {
            return (Datei) node.getUserObject();
        } else {
            return null;
        }
    }
    
    /**
     * <b>getDatei</b> returns the Datei object corresponding to the given path.
     *
     * @param filePath
     *            String containing the file's absolute path.
     * @return The Datei object associated to the path, or "null" if none was found.
     */
    public Datei getDatei(String filePath) {
    	
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + 
        		           " (FiliusFileSystem), getDatei(" + filePath + ")");

        return getDatei(absolutePathToNode(filePath));
    }

    /**
     * <b>getDatei</b> returns the Datei object corresponding to the given node and path.
     *
     * @param directoryNode
     *            TreeNode to which the path is relative.
     * @param filePath
     *            String containing the relative path to the Datei.
     * @return The Datei object associated to the path, or "null" if none was found.
     */
    public Datei getDatei(DefaultMutableTreeNode directoryNode, String filePath) {
    	
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + 
        		           " (FiliusFileSystem), getDatei(" + directoryNode + "," + filePath + ")");
        
        return getDatei(nodeToAbsolutePath(directoryNode) + FILE_SEPARATOR + filePath);
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
    	
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + 
        		           " (FiliusFileSystem), getDatei(" + directoryPath + "," + filePath + ")");
        
        return getDatei(directoryPath + FILE_SEPARATOR + filePath);
    }
        
    /**
     * <b>getDateiList</b> returns a list of all the Datei objects belonging to a node.
	 *
     * @param directoryNode
     *            TreeNode of the directory the Datei objects of which are to be listed.
     * @return A list of all the Datei objects belonging to the node.
     */
    public List<Datei> getDateiList(DefaultMutableTreeNode directoryNode) {
    	
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + 
        		           " (FiliusFileSystem), getDateiList(" + directoryNode + ")");
        
        List<Datei> liste = new LinkedList<Datei>();

        if (directoryNode == null) {
            return null;
        } else {
            for (Enumeration<TreeNode> e = directoryNode.children(); e.hasMoreElements();) {
            	
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
                
                if (isFile(node)) {
                    liste.add(getDatei(node));
                }
            }
            return liste;
        }
    }
    
    /**
     * <b>getDateiList</b> returns a list of all the Datei objects belonging to a node.
	 *
     * @param directoryPath
     *            String containing the absolute path of the directory the Datei objects of which are to be listed.
     * @return A list of all the Datei objects belonging to the node.
     */
    public List<Datei> getDateiList(String directoryPath) {
    	
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + 
        		           " (FiliusFileSystem), getDateiList(" + directoryPath + ")");
        
        return getDateiList(absolutePathToNode(directoryPath));
    }
    
    /**
     * <b>findDateiList</b> returns a list of Datei objects matching the search pattern.
	 *
     * @param directoryNode
     *            TreeNode of the directory in which the search is done.
     * @param searchPattern
     *            String containing the pattern to look for in the names of the Datei objects.
     * @return A list of Datei objects the name of which contain the searchPattern.
     */
    public LinkedList<Datei> findDateiList(DefaultMutableTreeNode directoryNode, String searchPattern) {
    	
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + 
        		           " (FiliusFileSystem), findDateiList(" + directoryNode + "," + searchPattern + ")");
        
        LinkedList<Datei> dateiList = new LinkedList<Datei>();
        
        for (Enumeration<TreeNode> e = directoryNode.children(); e.hasMoreElements();) {
        	
        	DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();

            if (isFile(node)) {
                Datei datei = getDatei(node);
                if (datei.getName().toLowerCase().matches("(.+)?" + searchPattern.toLowerCase() + "(.+)?")) {
                    dateiList.addLast(datei);
                }
            }
        }
        return dateiList;
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
    	
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + 
        		           " (FiliusFileSystem), findDateiList(" + directoryPath + "," + searchPattern + ")");

        return findDateiList(absolutePathToNode(directoryPath), searchPattern);
    }
    
    /**
     * <b>saveDatei</b> adds or updates a node for the given datei object.<br>
     * If a node with a name matching the Datei's object already exists in the given directory,
     * this node is updated.<br>
     * If no node matches the name of the Datei's object, a new node is added to the directory.
     *
     * @param directoryNode
     *            TreeNode of a directory in which the Datei is to be stored.
     * @param datei
     *            Datei object to be stored.
     * @return true if the operation succeeded.
     */
    public boolean saveDatei(DefaultMutableTreeNode directoryNode, Datei datei) {
    	
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + 
        		           " (FiliusFileSystem), saveDatei(" + directoryNode + "," + datei + ")");
        
        if (directoryNode == null) return false;
        
        DefaultMutableTreeNode childNode = getChild(directoryNode, datei.getName());
        
        if (childNode == null) {
        	addSubNode (directoryNode, new DefaultMutableTreeNode(datei));        	
        } else {
        	Datei dt = (Datei) childNode.getUserObject();
        	dt.setContent(datei.getContent());
        	dt.setType(datei.getType());
        	dt.setSize(datei.getSize());
        }        
        return true; 
    }
    
    /**
     * <b>saveDatei</b> adds or updates a node for the given datei object.<br>
     * If a node with a name matching the Datei's object already exists in the given directory,
     * this node is updated.<br>
     * If no node matches the name of the Datei's object, a new node is added to the directory.
     *
     * @param directoryPath
     *            String containing the absolute path of a directory in which the Datei is to be stored.
     * @param datei
     *            Datei object to be stored.
     * @return true if the operation succeeded.
     */
    public boolean saveDatei(String directoryPath, Datei datei) {
    	
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + 
        		           " (FiliusFileSystem), saveDatei(" + directoryPath + "," + datei + ")");
        
        return saveDatei(absolutePathToNode(directoryPath), datei);        
    }
    
    
    //******************************************************************************************
    //  Node related methods
    //******************************************************************************************
    
    /**
     * <b>isFile</b> checks whether the given node corresponds to a file.
	 *
     * @param node TreeNode to be checked.
     * @return true if a Datei object is attached to the node.
     */
    public boolean isFile(DefaultMutableTreeNode node) {
    	
    	if (node == null) return false;
    	return node.getUserObject().getClass().equals(Datei.class);
    }
    
    /**
     * <b>isFile</b> checks whether the given node corresponds to a file.
	 *
     * @param filePath String containing the absolute path of the file or directory to be checked.
     * @return true if a Datei object is attached to the corresponding node.
     */
    public boolean isFile(String filePath) {
    	
    	return isFile(absolutePathToNode(filePath));
    }
    
    /**
     * <b>isDirectory</b> checks whether the given node corresponds to a directory.
	 *
     * @param node TreeNode to be checked.
     * @return true if a String object is attached to the node, which means that 
     * the node represents a directory.
     */
    public boolean isDirectory(DefaultMutableTreeNode node) {
    	
    	if (node == null) return false;
    	return node.getUserObject().getClass().equals(String.class);
    }
    
    /**
     * <b>isDirectory</b> checks whether the given node corresponds to a directory.
	 *
     * @param filePath String containing the absolute path of the file or directory to be checked.
     * @return true if a String object is attached to the corresponding node, which means that 
     * the node represents a directory.
     */
    public boolean isDirectory(String filePath) {    	
    	
    	return isDirectory(absolutePathToNode(filePath));
    }    
    
    /**
     * <b>isAncestorNode</b> checks whether the parentNode is an ancestor of the given node.<br>
     * true is also returned when parentNode = node.
	 *
     * @param parentNode Reference TreeNode.
     * @param node TreeNode the ancestry of which is to be checked.
     * @return true if parentNode is an ancestor of node, or if parentNode equals node.
     */
    public boolean isAncestorNode(DefaultMutableTreeNode parentNode, DefaultMutableTreeNode node) {
    	
    	if (node == null) return false;
    	if (node == parentNode) return true;
    	
    	TreeNode p = node.getParent();
    	if (p != null) {
    		if (p == parentNode) return true;
    		p = p.getParent();
    	}
    	return false;
    }
    
    /**
     * <b>getName</b> returns the file's or directory's name attached to the given node.
	 *
     * @param node TreeNode to which the file or directory is attached.
     * @return A String containing the name of the file or directory object attached to the node.
     */
    public String getName(DefaultMutableTreeNode node) {    
    	
    	if (isFile(node)) {
    		return ((Datei) node.getUserObject()).getName();
    	} else {
    		return (String) node.getUserObject();
    	}
    }
    	   
    /**
     * <b>setName</b> sets the file's or directory's name attached to the given node.
	 *
     * @param node TreeNode to which the file or directory is attached.
     * @param newName String containing the name to be assigned.
     */
    public void setName(DefaultMutableTreeNode node, String newName) {   
    	
    	if (isFile(node)) {
    		((Datei) node.getUserObject()).setName(newName);
    	} else {
    		node.setUserObject(newName);
    	}
    	
    	// If the node has a parent, it is repositioned to maintain the alphabetical order
    	DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent(); 
    	if (parent != null) {
    		parent.remove(node);
    		addSubNode(parent, node);
    	}
    }   
    
    /**
     * <b>getType</b> returns the file's type of the object attached to the given node.
	 *
     * @param node TreeNode to which the object is attached.
     * @return A String containing the type of the file or directory object attached to the node.
     */
    public String getType(DefaultMutableTreeNode node) {    
    	
    	if (isFile(node)) {
    		return ((Datei) node.getUserObject()).getType();
    	} else {
    		return "directory";
    	}
    }
    
    /**
     * <b>getType</b> returns the file's size of the object attached to the given node.
	 *
     * @param node TreeNode to which the object is attached.
     * @return A long integer containing the size of the file object attached to the node. 
     * If the object attached to the node is a directory, -1 is returned.
     */
    public long getSize(DefaultMutableTreeNode node) {    
    	
    	if (isFile(node)) {
    		return ((Datei) node.getUserObject()).getSize();
    	} else {
    		return -1;
    	}
    }
    
    /**
     * <b>getChild</b> returns the child node with the given name.<br>
     * The name comparison is not case sensitive.
	 *
     * @param node TreeNode the children of which will be parsed.
     * @param name String containing the name to be looked for.
     * @return The child node having the given name, or null if there is none.
     */
    public DefaultMutableTreeNode getChild(DefaultMutableTreeNode parentNode, String name) {    

    	int index = getChildIndex(parentNode, name);
    	if (index > -1) return (DefaultMutableTreeNode) parentNode.getChildAt(index);
    	else            return null;
    }
    
    /**
     * <b>getChildIndex</b> returns the index of the child node with the given name.<br>
     * The name comparison is not case sensitive.
	 *
     * @param node TreeNode the children of which will be parsed.
     * @param name String containing the name to be looked for.
     * @return The index of the child node having the given name, or -1 if no child has the given name.
     */
    public int getChildIndex(DefaultMutableTreeNode parentNode, String name) {    

    	for (int i = 0; i < parentNode.getChildCount(); i++) {
    		if (getName((DefaultMutableTreeNode)parentNode.getChildAt(i)).equalsIgnoreCase(name)) return i;
    	}  
    	return -1;
    }
    
    /**
     * <b>countSubdirectoryNodes</b> returns the number of child nodes that correspond to a subdirectory.
	 *
	 * @param node TreeNode to which the file or directory is attached.
     */
    public int countSubdirectoryNodes(DefaultMutableTreeNode node) {   	
    	
    	int count = 0;
    	for (int i = 0; i < node.getChildCount(); i++) {
    		if (isDirectory((DefaultMutableTreeNode)node.getChildAt(i))) count++;
    	}  
    	return count;
    }
    
    /**
     * <b>addSubNode</b> inserts a given TreeNode as a child of another one.<br>
     * Directory nodes are stored before the file nodes.<br>
     * Both directory nodes and file nodes are stored in alphabetical order.<br>
     * If the node already has parent, it is removed from the parent first.  
	 *
     * @param parentNode TreeNode in which a new child will be inserted.
     * @param node TreeNode to be inserted.
     */
    public void addSubNode(DefaultMutableTreeNode parentNode, DefaultMutableTreeNode node) {
    	
    	int count = parentNode.getChildCount();
    	
    	// Get the index of the first file in the list    	
    	int f = 0;
    	while (f < count && isDirectory((DefaultMutableTreeNode)parentNode.getChildAt(f))) f++;
    	
    	// Set the initial and final possible indexes
    	int i;  
    	int l;
    	if (isDirectory(node)) {
    		i = 0;
    		l = f;
    	} else {
    		i = f;
    		l = count;
    	}   	
    	
    	// Get the index such that the insertion respects the alphabetical order 
    	String name = getName(node);
    	while (i < l && getName((DefaultMutableTreeNode)parentNode.getChildAt(i)).compareToIgnoreCase(name) < 0) i++;
    	
    	parentNode.insert(node, i);    
    }
    
    /**
     * <b>cloneNode</b> returns a copy of a node with all of the subnodes.  
	 *
     * @param node TreeNode to be copied.
     * @return A TreeNode which is an exact copy of the given one.
     */
    public DefaultMutableTreeNode cloneNode(DefaultMutableTreeNode node) {
    	    	
		try {
			// Create ObjectOutputStream
			ByteArrayOutputStream bufOutStream = new ByteArrayOutputStream();
			
			// Write object to stream
			ObjectOutputStream outStream = new ObjectOutputStream(bufOutStream);	
	        outStream.writeObject(node);
	        outStream.close();

	        // Copy stream to buffer
	        byte[] buffer = bufOutStream.toByteArray();

	        // ObjectInputStream
	        ByteArrayInputStream bufInStream = new ByteArrayInputStream(buffer);
	        ObjectInputStream inStream = new ObjectInputStream(bufInStream);

	        // Create new object from stream
	        return (DefaultMutableTreeNode) inStream.readObject();
			
		} catch (IOException e) {
			return null;
		} catch (ClassNotFoundException e) {
			return null;
		}        
    }
    
    /**
     * <b>sortNodes</b> reorders the child subnodes of a given TreeNode.<br>
     * The ordering is based on the name of the file or directory associated to each node.<br> 
     * Directory nodes are stored before the file nodes.<br>
     * Both directory nodes and file nodes are stored in alphabetical order. 
	 *
     * @param node
     *            TreeNode to be inserted.
     */
    public void sortChildNodes(DefaultMutableTreeNode node) {
    	
    	int count = node.getChildCount();
    	
    	// alphabetical ordering of the nodes
    	// Insertion sort algorithm (fast for already sorted list)
    	for (int i = 1; i < count; i++) {
    		DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)node.getChildAt(i);
    		String name = getName(childNode);
    		int j = i - 1;
    		while (j >= 0) {
    			String name2 = getName((DefaultMutableTreeNode)node.getChildAt(j));
    			if (name.compareToIgnoreCase(name2) >= 0) break;
    			j--;
    		}
    		j++;
    		if (j < i) node.insert(childNode, j);  
    	}    	
    	
    	// Directories are now moved to the beginning of the list without 
    	// breaking the alphabetical order
    	
    	// Locate the first file in the list
    	int i = 0;
    	int f = count;
    	while (i < f && isDirectory((DefaultMutableTreeNode)node.getChildAt(i))) i++;
    	f = i;
    	i++;
    	
    	// Move following directories before the first file
    	// f contains the index of the first file
    	while (i < count) {
    		DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)node.getChildAt(i);
    		
    		// Move the node
    		if (isDirectory(childNode)) {    			
    			node.insert(childNode, f);    			
    			f++;
    		}    		
    		i++;
    	}
    }
    
    /**
     * <b>sortTree</b> reorders the subnodes of a given TreeNode. The <br>
     * The ordering is based on the name of the file or directory associated to each node.<br> 
     * Directory nodes are stored before the file nodes.<br>
     * Both directory nodes and file nodes are stored in alphabetical order. 
	 *
     */
    public void sortSubnodes(DefaultMutableTreeNode node) {
    	
    	sortChildNodes(node);
    	
    	for (int i = 0; i < node.getChildCount(); i++) {
    		sortSubnodes((DefaultMutableTreeNode)node.getChildAt(i));
    	}  
    }
    
    /**
     * <b>sortTree</b> reorders the subnodes of the whole tree, starting from root.<br>
     * The ordering is based on the name of the file or directory associated to each node.<br> 
     * Directory nodes are stored before the file nodes.<br>
     * Both directory nodes and file nodes are stored in alphabetical order. 
	 *
     */
    public void sortTree() {
    	
    	sortSubnodes(root);
    }    


    //******************************************************************************************
    //  Path/node conversions methods
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
        
        StringBuffer path;
        Object[] object;

        object = node.getUserObjectPath();
        path = new StringBuffer();
        path.append(object[0].toString());
        for (int i = 1; i < object.length; i++) {
            path.append(FILE_SEPARATOR + object[i].toString());
        }
        
        return stripRoot(path.toString());
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
    	
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + 
        		           ", absolutePathToNode(" + path + ")");
        
        path = stripRoot(evaluatePath(path));

        if (path.equals(FILE_SEPARATOR) || path.isEmpty()) {
            return root;
        }
        Enumeration enumeration;
        DefaultMutableTreeNode node;

        enumeration = root.preorderEnumeration();
        while (enumeration.hasMoreElements()) {
            node = (DefaultMutableTreeNode) enumeration.nextElement();
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
    	
        Main.debug.println("INVOKED (static) (FiliusFileSystem), pathToNode(" + directoryNode + "," + path + ")");
        
        Enumeration enumeration;
        DefaultMutableTreeNode node;
        String absolutePath;

        if (path.length() > 0 && path.substring(0, 1).equals(FILE_SEPARATOR)) { 
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
    //  Node and file names related methods
    //******************************************************************************************

    /**
     * <b>getFreeName</b> returns a name based on the given one, and not already used by a child of the given node.<br>
     * In case the name is already in use, a new name is created in the form 'name (n)'.
	 *
     * @param node TreeNode in which the returned name must not exist.
     * @param name String containing the name to be made unique.
     * @return A String containing the name made unique.
     */
    public String getUniqueName(DefaultMutableTreeNode node, String name) {    	    	
    	
    	if (getChildIndex(node, name) == -1) return name;
    	
    	String newName;
    	String pre = removeExtension(name);
    	String ext = getExtension(name);
    	int i = 2;
    	do {
    		newName = pre + " (" + String.valueOf(i) + ")" + ext;
    		i++;
    	} while(getChildIndex(node, newName) > -1);
    	
    	return newName;
    }
    
    /**
     * <b>nameIsValid</b> checks if a name uses only allowed characters.<br>
     * The following characters are not allowed: \ | / " : ? * < > 
	 *
     * @param name String containing the name to be checked.
     * @return true if the name uses only allowed characters.
     */
    public boolean nameIsValid(String name) {    	    	
    	
    	for (int i = 0; i < name.length(); i++) {
    		if ("\\|/\":?*<>".indexOf(name.charAt(i)) != -1) return false;
    	}    	
    	return true;
    }
    
    
    //******************************************************************************************
    //  File types methods
    //******************************************************************************************
    
    /**
	 * <b>initFileTypeMap</b> initializes the files' types map.
	 * 
	 * @author Johannes Bade & Thomas Gerding
	 * 
	 */
	private void initFileTypeMap() {
		
		Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + ", getFileTypeMap()");
		
		fileTypeMap = new HashMap<String, String>();
		
		fileTypeMap.put("txt", "text");
		fileTypeMap.put("ini", "text");
		fileTypeMap.put("cfg", "text");
		fileTypeMap.put("xml", "text");
		
		fileTypeMap.put("htm", "web");
		fileTypeMap.put("html", "web");
		fileTypeMap.put("css", "web");
		fileTypeMap.put("js", "web");
		
		fileTypeMap.put("jpg", "image");
		fileTypeMap.put("jpeg", "image");
		fileTypeMap.put("gif", "image");
		fileTypeMap.put("png", "image");
		fileTypeMap.put("bmp", "image");
		
		fileTypeMap.put("mp2", "sound");
		fileTypeMap.put("mp3", "sound");
		fileTypeMap.put("wav", "sound");		

		
		// There is an issue with the file path within the project
		
//		RandomAccessFile configFile;
//		try {
//			configFile = new RandomAccessFile("config/filetypes.txt", "r");
//			
//			for (String line; (line = configFile.readLine()) != null;) {
//				StringTokenizer stx = new StringTokenizer(line, ";");
//				String type = stx.nextToken();
//				StringTokenizer sty = new StringTokenizer(stx.nextToken(), ",");
//
//				while (sty.hasMoreElements()) {
//					fileTypeMap.put(sty.nextToken(), type);
//				}
//			}
//		} catch (FileNotFoundException e) {
//			// Auto-generated catch block
//			e.printStackTrace(Main.debug);			
//		} catch (IOException e) {
//			// Auto-generated catch block
//			e.printStackTrace(Main.debug);
//		}
	}
    
	/**
	 * getFileType determines the file's type based on its extension.
	 * 
	 * @param fileName
	 *             String containing the name of the file the type of which is to be determined.
	 * @return A String containing the file's type 
	 */
	public String getFileType(String fileName) {
		
		Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + ", getFileType(" + fileName + ")");
				
		String type = "binary"; 
		String fileExt = getExtension(fileName).toLowerCase();
		if (fileExt != null) {
			
			fileExt = fileExt.substring(1);
			type = fileTypeMap.get(fileExt);
		}
	
		return type;
	}
	
	//******************************************************************************************
    //  Import-Export with the "real" world
    //******************************************************************************************
    
    /**
	 * <b>importRealFile</b> imports a "real" file into the Filius File System.<br>
	 * The content of binary files is stored in Base64 encoded String.
	 * 
	 * @param directory TreeNode into which the file is to be imported
	 * @param filePath String containing the path to the real file to be imported (the last separator is optional). 
	 * @param fileName String containing the name of the real file to be imported. 
	 * @return An error code. 
	 */
	public errorCode importRealFile(DefaultMutableTreeNode directory, String filePath, String fileName) {
		
		Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + ", importRealFile(" + 
						   filePath + "," + fileName + "," + directory + ")");
		
		if (isFile(directory)) {
			Main.debug.println("ERROR (" + this.hashCode() + "): Es können keine Dateien in Dateien angeleget werden!");
			return errorCode.UNSPECIFIED;
		}		
		
		// Add the last file separator if missing
		if (!filePath.endsWith(System.getProperty("file.separator"))) filePath += System.getProperty("file.separator");
		String fullFileName = filePath + fileName;
		
		// Open the real file
		java.io.File file = new java.io.File(fullFileName);
		
		if (!file.exists()) return errorCode.FILE_NOT_FOUND;		
		if (file.length() > 150000) return errorCode.FILE_TOO_LARGE;

		String newName = getUniqueName(directory, fileName);
		
		String type = getFileType(fileName);
		Datei datei;
		
		if (type != null && type.equals("text")) {
			String txtInhalt = "";
			try {
				BufferedReader in = new BufferedReader(new FileReader(fullFileName));
				String str;
				while ((str = in.readLine()) != null) {
					txtInhalt += str + "\r\n";
				}
				in.close();				
			} catch (IOException e) {
				e.printStackTrace(Main.debug);
			}
			datei = new Datei(newName, type, txtInhalt);
			
		} else {
			datei = new Datei(newName, type, Base64.encodeFromFile(fullFileName));
			datei.setSize(file.length());			
		}
		
		saveDatei(directory, datei);	

		return errorCode.NO_ERROR;
	}
	
	/**
	 * <b>exportVirtualFile</b> exports a file of the Filius File System to a real file.<br>
	 * The content of binary files is stored in Base64 encoded String.
	 * 
	 * @param directory TreeNode which contains the file is to be exported
	 * @param fileName String containing the name of the real file to be imported. 
	 * @param filePath String containing the path to the real file to be imported (the last separator is optional). 
	 * @param realFullName String containing the path and name of the real file to be created. 
	 * @return An integer containing an error code 
	 */
	public errorCode exportVirtualFile(DefaultMutableTreeNode directory, String fileName, String realFullName) {
		
		return errorCode.NO_ERROR;
	}
	
    
    //******************************************************************************************
    //  Path helper methods
    //******************************************************************************************

    /**
     * <b>evaluatePath</b> evaluates a path containing '.' and '..' special directories.
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

        while (tk.hasMoreTokens()) {
            String currString = tk.nextToken();
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
     * <b>stripRoot</b> removes "root" from the beginning of an absolute path<br>
     * For example, if path equals root/dir1/dir2/file, the result is /dir1/dir2/file.
     * 
     * @param path String containing a path
     * @return A String containing the absolute path starting with the FILE_SEPARATOR character.
     */
    private static String stripRoot(String path) {
    	
        Main.debug.println("INVOKED (static) filius.software.system.Dateisystem, stripRoot(" + path + ")");
        
        if (path.indexOf(FiliusFileSystem.FILE_SEPARATOR) >= 0) {
        	return path.substring(path.indexOf(FiliusFileSystem.FILE_SEPARATOR));
        } else {
        	return "/";
        }
    }

    /**
     * <b>getPathDirectory</b> extracts the directories from a path
     * 
     * @param path String containing a path
     * @return A String containing the substring of path containing all characters before the last separator.
     * The last separator is not included in the returned value.<br>
     * If path contains no separator, an empty string is returned.
     */
    public static String getPathDirectory(String path) {
    	
        Main.debug.println("INVOKED (static) filius.software.system.Dateisystem, getDirectory(" + path + ")");
        
        if (path.lastIndexOf(FiliusFileSystem.FILE_SEPARATOR) >= 0) {
            return path.substring(0, path.lastIndexOf(FiliusFileSystem.FILE_SEPARATOR));
        } else {
            return "";
        }
    }

    /**
     * <b>getPathFilename</b> extracts the filename from a path
     * 
     * @param path String containing a path
     * @return A String containing the substring of path containing all characters after the last separator.
     * If path contains no separator, path is returned as is.
     */
    public static String getPathFilename(String path) {
    	
        Main.debug.println("INVOKED (static) filius.software.system.Dateisystem, getPathFilename(" + path + ")");
        
        if (path.lastIndexOf(FiliusFileSystem.FILE_SEPARATOR) >= 0) {
            return path.substring(path.lastIndexOf(FiliusFileSystem.FILE_SEPARATOR) + 1);
        } else {
            return path;
        }
    }
    
    /**
     * <b>removeExtension</b> returns a filepath without its extension.<br>
     * The extension consists of all the characters after the last dot if any.<br>
     * The filepath may be absolute, relative, or even reduce to a filename.
     * 
     * @param filePath String containing a filepath or filename.
     * @return A String containing the filepath or filename without its extension, if any.
     */
    public String removeExtension(String filePath) {
    	
    	int index = filePath.lastIndexOf(".");
        if (index >= 0) {
        	return filePath.substring(0, index);
        } else
            return filePath;        
    }
    
    /**
     * <b>getExtension</b> extracts the extension of a filepath.<br>
     * The extension consists of all the characters after the last dot if any.<br>
     * The filepath may be absolute, relative, or even reduce to a filename.
     * 
     * @param filepath String containing a filepath or filename.
     * @return A String containing the extension of the filepath or filename including
     * the initial dot, or an empty string if there is no extension.
     */
    public String getExtension(String filepath) {
    	
    	int index = filepath.lastIndexOf(".");
        if (index >= 0) {
        	return filepath.substring(index);
        } else
            return "";        
    }


    //******************************************************************************************
    //  Terminal specific methods (-> should probably be relocated in Terminal.java)
    //******************************************************************************************

    /**
     * <b>printSubtree</b> prints the tree structure starting from a given node 
     * 
     * @param indent String used for indentation
     * @param startNode TreeNode from which the printing starts
     */
    private void printSubtree(String indent, DefaultMutableTreeNode startNode) {
    	
        DefaultMutableTreeNode node;
        Main.debug.print(indent + "--");
        
        if (isDirectory(startNode)) {
            Main.debug.println("[" + getName(startNode) + "]");
        }
        
        indent = indent + " |";
        for (Enumeration e = startNode.children(); e.hasMoreElements();) {
            node = (DefaultMutableTreeNode) e.nextElement();
            printSubtree(indent, node);
        }
    }
    
    /**
     * <b>printTree</b> prints the whole tree structure
     */
    public void printTree() {
    	
        printSubtree("", root);
    }
}
