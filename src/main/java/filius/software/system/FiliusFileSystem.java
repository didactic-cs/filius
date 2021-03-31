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
package filius.software.system;

import java.io.Serializable;
import java.util.HashMap;
import java.util.StringTokenizer;

import filius.Main;

/**
 * The class <b>FiliusFileSystem</b> reproduces in Filius the behavior of a filesystem.
 * It is structured as a tree of FiliusFileNodes. Each file and directory being associated 
 * to a node, the Java serialization allows an easy way to store the whole filesystem
 * within each Filius project file. <br>
 * The userObject of each node is either: <br>
 *  - a String object containing the name of a directory <br>
 *  - a FiliusFile object containing the information relative to a file. <br>  
 *
 * @see filius.software.system.FiliusFile
 * @see filius.software.system.FiliusFileNode
 */
@SuppressWarnings("serial")
public class FiliusFileSystem implements Serializable {
    
    // Error codes 
    public static enum errorCode {
    	NO_ERROR,    	
    	FILE_NOT_FOUND,
    	FILE_TOO_LARGE,
    	NO_DIRECTORY_NODE,
    	NOT_EXPORTABLE,
    	UNSPECIFIED
   	}
    
    // File types
    public static enum FileType {
    	UNKNOWN,    	
    	DIRECTORY,
    	TEXT,
    	CSS,
    	XML,
    	HTML,
    	IMAGE_JPG,
    	IMAGE_GIF,
    	IMAGE_PNG,
    	IMAGE_BMP
   	}
    
    // Filetypes map
    // Two special types are also used:
    // binary: for files for which the type is unspecified
    // directory: for directories
    private static HashMap<String, FileType> fileTypeMap;    
    static {	
		fileTypeMap = new HashMap<String, FileType>();
		
		fileTypeMap.put("txt",  FileType.TEXT);
		fileTypeMap.put("ini",  FileType.TEXT);
		fileTypeMap.put("cfg",  FileType.TEXT);
		fileTypeMap.put("conf", FileType.TEXT);
		
		fileTypeMap.put("css",  FileType.CSS);	
		fileTypeMap.put("xml",  FileType.XML);		
		fileTypeMap.put("htm",  FileType.HTML);
		fileTypeMap.put("html", FileType.HTML);
		
		fileTypeMap.put("jpg",  FileType.IMAGE_JPG);
		fileTypeMap.put("jpeg", FileType.IMAGE_JPG);
		fileTypeMap.put("gif",  FileType.IMAGE_GIF);
		fileTypeMap.put("png",  FileType.IMAGE_PNG);
		fileTypeMap.put("bmp",  FileType.IMAGE_BMP);			
	}

    // Character used as separator in a path
    public final String FILE_SEPARATOR = "/";
    
    // Root node, similar to the "/" mount point used in some operating system
    private FiliusFileNode root;

    // Current working directory
    private transient FiliusFileNode workingDirectory;    
    

    /**
     * <b>FiliusFileSystem</b> models a filesystem to be used by the virtual applications.<br>
     * In order to store the filesystem's content in a Filius project file, each file and directory
     * is associated with a FiliusFileNode. The Java serialization is used. <br>
     * It is important to keep this constructor without parameter for the serialization.
     */
    public FiliusFileSystem() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + 
        		           " (FiliusFileSystem), constr: FiliusFileSystem()");
        
        root = new FiliusFileNode(this, "root");
        workingDirectory = root;        
    }
    
    //******************************************************************************************
    //  Getters and setters
    //******************************************************************************************

    /**
     * <b>getRoot</b> returns the root node of the filesystem.
     *
     * @return The FiliusFileNode correponding to the root of the hierarchy.
     */
    public FiliusFileNode getRoot() {
    	
        return root;
    }
    
    /**
     * <b>setRoot</b> sets the root node of the filesystem.
     *
     * Used for deserialization only.
     */
    public void setRoot(FiliusFileNode root) {
    	
        this.root = root;
    }

    /**
     * <b>getWorkingDirectory</b> returns the node corresponding to the current working directory.
     *
     * @return The FiliusFileNode corresponding to the current working directory.
     */
    public FiliusFileNode getWorkingDirectory() {
    	
        return workingDirectory;
    }

    /**
     * <b>setWorkingDirectory</b> sets the node to be used as the current working directory.
     *
     * @param directoryNode The node to be used as the current working directory.
     */
    public void setWorkingDirectory(FiliusFileNode directoryNode) {
    	
        workingDirectory = directoryNode;
    }
    

    //******************************************************************************************
    //  File and directory related methods
    //******************************************************************************************
    
    /**
     * <b>isFile</b> checks whether the given node corresponds to a file.
	 *
     * @param filePath String containing the absolute path of the file or directory to be checked.
     * @return true if a FiliusFile object is attached to the corresponding node.
     */
    public boolean isFile(String filePath) {
    	
    	FiliusFileNode node = toNode(filePath);
    	if (node == null) return false;
    	else return node.isFile();    		
    }
    
    /**
     * <b>isDirectory</b> checks whether the given node corresponds to a directory.
	 *
     * @param filePath String containing the absolute path of the file or directory to be checked.
     * @return true if a String object is attached to the corresponding node, which means that 
     * the node represents a directory.
     */
    public boolean isDirectory(String filePath) {    	
    	
    	FiliusFileNode node = toNode(filePath);
    	if (node == null) return false;
    	else return node.isDirectory();
    }   
        
    /**
     * <b>deleteFile</b> removes the given file or directory from the filesystem
     *
     * @param filePath
     *            String containing the absolute path of the file or directory to be deleted.
     * @return true if the deletion succeeded.
     */
    public boolean deleteFile(String filePath) {
        
    	FiliusFileNode node = toNode(filePath);    	
    	if (node == null) return false;
    	node.delete();
    	return true;       
    }  
    

    //******************************************************************************************
    //  Path related methods
    //******************************************************************************************
  
    /**
     * <b>toPath</b> returns an absolute path built from the concatenation of path1 and path2. <br>
     * FILE_SEPARATOR is added before path1 if missing.<br>
     * FILE_SEPARATOR is added between path1 and path2 if missing. If path1 ends with FILE_SEPARATOR and
     * path2 starts with FILE_SEPARATOR, one separator will be removed before concatenation.<br>
     * path1 or path 2 may be empty (but not null!). If both are empty, the result will be an empty string.<br> 
     * The resulting path is evaluated to interprete possible "." and "..". If the number of '..' exceeds 
     * the depth level of the path, the result will be an empty string (not null!).<br>
     * This method does not check whether the resulting path corresponds to a real node in the tree.
     * See {@link #toNode} for a node version of this method.
     *
     * @param path1
     *            String containing the high part of the path, i.e. the one starting from the root.
     * @param path2
     *            String containing the low part of the path. It may end with a file name. 
     *            
     * @return A String corresponding to the concatenated path.
     * 
     * @see #toNode
     */
    public String toPath(String path1, String path2) {
    	
    	String path; 
    	
    	// Smart concatenation
    	if (path1.isEmpty()) {    		
    		if (path2.isEmpty()) return "";    		
    		path = path2;
    		
    	} else {
    		if (! path2.isEmpty()) {
    			if (! path1.endsWith(FILE_SEPARATOR)) path1 = path1 + FILE_SEPARATOR;    				
    		
    			if (path2.startsWith(FILE_SEPARATOR)) {
    				path = path1 + path2.substring(1);
    			} else {
    				path = path1 + path2;
    			}   
    		} else {
    			path = path1;
    		}
    	}
    	
    	// Check for the initial FILE_SEPARATOR
    	if (! path.startsWith(FILE_SEPARATOR)) path = FILE_SEPARATOR + path;    	
    	
    	// Evaluate "." and ".." and remove consecutive separators
    	path = evaluatePath(path);    
    	        
    	return path;
    } 
    
    /**
     * <b>toPath</b> returns the node corresponding to the absolute path built from the concatenation of path1 and path2.<br>
     * See method {@link #toPath} more details.
     *
     * @param path1
     *            String containing the high part of the path, i.e. the one starting from the root.
     * @param path2
     *            String containing the low part of the path. It may end with a file name. 
     *            
     * @return FiliusFileNode of the directory corresponding to the path
     * 
     * @see #toPath
     */
    public FiliusFileNode toNode(String path1, String path2) {
    	        
    	String path = toPath(path1, path2);
    	
    	if (path.isEmpty()) return null;
    	
    	return root.toNode(path);
    } 
    
    /**
     * <b>toPath</b> returns the node corresponding to the absolute path.
     * The initial separator is not required.<br>
     * Method {@link #toPath} is used to evaluate possible "." and ".." and 
     * remove double separators.
     *
     * @param path
     *            String containing the absolute path.            
     * @return FiliusFileNode of the directory corresponding to the path
     */
    public FiliusFileNode toNode(String path) {
        
    	path = toPath(path, "");
    	
    	if (path.isEmpty()) return null;
    	
    	return root.toNode(path);
    }     

	/**
	 * <b>sortTree</b> reorders all the subnodes of the whole tree, starting from root.<br>
	 * The ordering is based on the name of the file or directory associated to each node.<br> 
	 * Directory nodes are stored before the file nodes.<br>
	 * Both directory nodes and file nodes are stored in alphabetical order. 
	 */
	public void sortTree() {

		root.sortSubnodes();
	}   
    
    /**
     * <b>evaluatePath</b> evaluates a path containing '.' and '..' special directories. The path is 
     * not required to be absolute.<br>
     * The presence of successive separators ('//') in the path will be fixed.<br> 
     * The path is expected to be evaluatable. If it starts with '..' or if it contains an excessive 
     * number of '..', an empty path will be returned.<br>
     * The last element of the path is not followed by a separator. The root path '/' is the only one 
     * ending with a separator.<br>
     * This method does not check whether the resulting path corresponds to a real node in the tree.
     * 
     * @param path String containing a path
     * @return A String containing the evaluated path.
     */
    public String evaluatePath(String path) {
        
        // Split the path in subtrings
        StringTokenizer tk = new StringTokenizer(path, FILE_SEPARATOR);
        String[] pathElements = new String[tk.countTokens()];
        int elemIndex = -1;     
        
        // Build an array of path elements
        while (tk.hasMoreTokens()) {
            String element = tk.nextToken();
            if (element.equals("..")) {
                elemIndex--;
            } else if (!element.equals(".") && !element.isEmpty()) {
                elemIndex++;
                // Excessive account of '..' 
                if (elemIndex < 0) return "";
                pathElements[elemIndex] = element;
            }
        }
        
        // Rebuild the path based on the array of string elements      
        String result = "";
        for (int i = 0; i <= elemIndex; i++) {                                                
            result += pathElements[i];
            if (i < elemIndex) result += FILE_SEPARATOR;
        }

        // Add leading separator if the path is absolute
        if (path.startsWith(FILE_SEPARATOR)) result = FILE_SEPARATOR + result; 

        return result;
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
    
    /**
     * <b>isAbsolute</b> checks if a path is absolute base on the
     * presence of  
	 *
     * @param path String containing the name to be checked.
     * @return true if the path is absolute.
     */
    public boolean isAbsolute(String path) {    	    	
    	
    	if (path == null || path.isEmpty()) return false;
    	
    	return (path.substring(0, 1).equals(FILE_SEPARATOR));
    }

    /**
     * <b>getPathDirectory</b> extracts the directories part from a path
     * 
     * @param path String containing a path
     * @return A String containing the substring of path consisting of all characters before the last separator.
     * The last separator is not included in the returned value.<br>
     * If path contains no separator, an empty string is returned.
     */
    public String getPathDirectory(String path) {
    	        
        if (path.lastIndexOf(FILE_SEPARATOR) >= 0) {
            return path.substring(0, path.lastIndexOf(FILE_SEPARATOR));
        } else {
            return "";
        }
    }

    /**
     * <b>getPathFilename</b> extracts the filename from a path
     * 
     * @param path String containing a path
     * @return A String containing the substring of path consisting of all characters after the last separator.
     * If path contains no separator, path is returned as is.
     */
    public String getPathFilename(String path) {
    	        
        if (path.lastIndexOf(FILE_SEPARATOR) >= 0) {
            return path.substring(path.lastIndexOf(FILE_SEPARATOR) + 1);
        } else {
            return path;
        }
    }
    
    /**
	 * <b>getExtension</b> extracts the extension of a path.<br>
	 * The extension consists of all the characters after the last dot if any.<br>
	 * The path may be absolute, relative, or even reduce to a filename.
	 * 
	 * @param path String containing a filepath or just a filename.
	 * @return A String containing the extension of the path or filename including
	 * the initial dot, or an empty string if there is no extension.
	 */
	public String getPathExtension(String path) {
		
		int index = path.lastIndexOf(".");
	    if (index >= 0) {
	    	return path.substring(index);
	    } else
	        return "";        
	}

	/**
     * <b>getPathWithoutExtension</b> returns a filepath without its extension.<br>
     * The extension consists of all the characters after the last dot if any.<br>
     * The filepath may be absolute, relative, or even reduce to a filename.
     * 
     * @param filePath String containing a filepath or filename.
     * @return A String containing the filepath or filename without its extension, if any.
     */
    public String getPathWithoutExtension(String filePath) {
    	
    	int index = filePath.lastIndexOf(".");
        if (index >= 0) {
        	return filePath.substring(0, index);
        } else
            return filePath;        
    }
    
    //******************************************************************************************
    //  File types method
    //******************************************************************************************
    
	/**
	 * <b>getFileType</b> determines the file's type based on its extension.
	 * 
	 * @param fileName
	 *             String containing the name of the file the type of which is to be determined.
	 * @return A String containing the file's type 
	 */
	public FileType getTypeFromExtension(String fileName) { 
						
		FileType type = FileType.UNKNOWN; 
		String fileExt = getPathExtension(fileName).toLowerCase();
		if (fileExt != null) {
			
			fileExt = fileExt.substring(1);
			type = fileTypeMap.get(fileExt);
			if (type == null) type = FileType.UNKNOWN;
		}
	
		return type;
	}
}
