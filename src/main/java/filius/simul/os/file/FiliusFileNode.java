package filius.software.system;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import filius.Main;
import filius.rahmenprogramm.Base64;
import filius.software.system.FiliusFileSystem.FileType;
import filius.software.system.FiliusFileSystem.errorCode;


/**
 * The class <b>FiliusFileNode</b> is an extended version of the DefaultMutableTreeNode.
 * It is an essential component of the FiliusFileSystem.<br>
 * Each FiliusFileNode instance has an associated object which is either: <br>
 *  - a String object containing the name of a directory <br>
 *  - a FiliusFile object containing the information relative to a file. 
 *
 * @see filius.software.system.FiliusFileSystem
 * @see filius.software.system.FiliusFile
 */
@SuppressWarnings("serial")
/**
 * <b>FiliusFileNode</b> models a TreeNode specific to the Filius File System.<br>
 * It allows the whole filesystem to be stored in a Filius project file, using
 * the serialization provided by Java.<br>
 * 
 * @see filius.software.system.FiliusFile
 * @see filius.software.system.FiliusFileSystem
 */
public class FiliusFileNode extends DefaultMutableTreeNode {
	
	private FiliusFileSystem FFS = null;
	private transient FiliusFileNode root = null;
	
	
	/**
     * It is important to keep this constructor without parameter for the serialization.
     */
    public FiliusFileNode() {
    	
        this(null, null);
    }

	public FiliusFileNode(FiliusFileSystem FFS, Object userObject) {		
		
        super(userObject);
        this.FFS = FFS;
    }
	
    public void setFFS(FiliusFileSystem FFS) {		
		
        this.FFS = FFS;
    }
	
    public FiliusFileSystem getFFS() {		
		
        return FFS;
    }
	
	// Overridden to avoid casting
	public FiliusFileNode getParent() {
		
        return (FiliusFileNode) super.getParent();
    }
	
	// Overridden to avoid casting
    public FiliusFileNode getChildAt(int index) {
        
        return (FiliusFileNode) super.getChildAt(index);
    }   
    
    /**
     * <b>getRoot</b> returns the root node of the tree to which the node belongs.
	   *
     * @return the root node
     */
	public FiliusFileNode getRoot() {
		
		if (root == null) {
//			FiliusFileNode n = this;
//			FiliusFileNode p = n.getParent();
//	    	while (p != null) {
//	    		n = p;
//	    		p = n.getParent();	     		
//	    	}
//	    	root = n;
			FiliusFileNode p = getParent();
			if (p == null) root = this;
			else root = p.getRoot();
		}
		
        return root;
    }
    
    /**
     * <b>isDirectory</b> checks whether the node corresponds to a directory.
	 *
     * @return true if a String object is attached to the node, which means that 
     * the node represents a directory.
     */
    public boolean isDirectory() {    	
   
    	return getUserObject().getClass().equals(String.class);
    }
	
    /**
     * <b>isFile</b> checks whether the node corresponds to a file.
	 *
     * @return true if a FiliusFile object is attached to the node.
     */
    public boolean isFile() {
    	
    	return getUserObject().getClass().equals(FiliusFile.class);
    }  
    
    /**
     * <b>getFiliusFile</b> returns the FiliusFile object associated to the given node.<br>
     * If the node is a directory node, null is returned.
     *
     * @param node  
     *            TreeNode owning the FiliusFile object.
     * @return The FiliusFile object associated to the node, or null if none was found.
     */
    public FiliusFile getFiliusFile() {
        
    	if (isFile()) {
    		return ((FiliusFile) getUserObject());
    	} else {
    		return null;
    	}
    }
    
    /**
     * <b>getFiliusFile</b> returns the FiliusFile object corresponding to the given path.
     *
     * @param filePath
     *            String containing the file's path relative to the node.
     * @return The FiliusFile object associated to the path, or "null" if none was found.
     */
    public FiliusFile getFiliusFile(String filePath) {
	
    	FiliusFileNode node = toNode(filePath);
    	if (node == null) return null;
    	return node.getFiliusFile();
    }
    
    /**
     * <b>saveFiliusFile</b> adds or updates a node for the given FiliusFile object.<br>
     * If a node with a name matching the FiliusFile's object already exists in the given directory,
     * this node is updated.<br>
     * If no node matches the name of the FiliusFile's object, a new node is added to the directory.
     *
     * @param fFile
     *            FiliusFile object to be stored.
     * @return true if the operation succeeded.
     */
    public boolean saveFiliusFile(FiliusFile fFile) {
    	                
        FiliusFileNode childNode = getChild(fFile.getName());
        
        if (childNode == null) {
        	addSubNode (new FiliusFileNode(FFS, fFile));        	
        } else {
        	FiliusFile dt = (FiliusFile) childNode.getUserObject();
        	dt.setContent(fFile.getContent());
        	dt.setType(fFile.getType());
        	dt.setSize(fFile.getSize());
        }        
        return true; 
    }
    
    /**
     * <b>getName</b> returns the file's or directory's name attached to the node.
	 *
     * @return A String containing the name of the file or directory object attached to the node.
     */
    public String getName() {    
    	
    	if (isFile()) {
    		return ((FiliusFile) getUserObject()).getName();
    	} else {
    		return (String) getUserObject();
    	}
    }
    	   
    /**
     * <b>setName</b> sets the file's or directory's name attached to the node.
	 *
     * @param newName String containing the name to be assigned.
     */
    public void setName(String newName) {   
    	
    	if (isFile()) {
    		((FiliusFile) getUserObject()).setName(newName);
    	} else {
    		setUserObject(newName);
    	}
    	
    	// If the node has a parent, it is repositioned to maintain the alphabetical order
    	FiliusFileNode parent = getParent(); 
    	if (parent != null) {
//    		parent.remove(this);      // not necessary
    		parent.addSubNode(this);
    	}
    }  
    
    /**
     * <b>getType</b> returns the file's type of the object attached to the given node.
	 *
     * @return A String containing the type of the file or directory object attached to the node.
     */
    public FileType getType() {    
    	
    	if (isFile()) {
    		return ((FiliusFile) getUserObject()).getType();
    	} else {
    		return FileType.DIRECTORY;
    	}
    }
    
    /**
     * <b>getSize</b> returns the file's size of the object attached to the given node.
	 *
     * @return A long integer containing the size of the file object attached to the node. 
     * If the object attached to the node is a directory, -1 is returned.
     */
    public long getSize() {    
    	
    	if (isFile()) {
    		return ((FiliusFile) getUserObject()).getSize();
    	} else {
    		return -1;
    	}
    }
    
    /**
     * <b>isAncestorOf</b> checks whether the given node is a descendant.<br>
     * true is also returned when this = node.
	 *
     * @param node FFSNode the ancestry of which is to be checked.
     * @return true if parentNode is an ancestor of node, or if parentNode equals node.
     */
    public boolean isAncestorOf(FiliusFileNode node) {
    	    	
    	FiliusFileNode n = node;
    	if (n != null) {
    		if (n == this) return true;
    		n = n.getParent();
    	}
    	return false;
    }
    
    /**
     * <b>getChild</b> returns the child node with the given name.<br>
     * The name comparison is not case sensitive.
	 *
     * @param name String containing the name to be looked for.
     * @return The child node having the given name, or null if there is none.
     */
    public FiliusFileNode getChild(String name) {    

    	int index = getChildIndex(name);
    	if (index > -1) return getChildAt(index);
    	else            return null;
    }
    
    /**
     * <b>getChildIndex</b> returns the index of the child node with the given name.<br>
     * The name comparison is not case sensitive.
	 *
     * @param name String containing the name to be looked for.
     * @return The index of the child node having the given name, or -1 if no child has the given name.
     */
    public int getChildIndex(String name) {    

    	for (int i = 0; i < getChildCount(); i++) {
    		if (getChildAt(i).getName().equalsIgnoreCase(name)) return i;
    	}  
    	return -1;
    }
    
   /**
    * <b>hasChildNamed</b> checks whether a child node with the given name exists.<br>
    * No recursive search in the subdirectories is done.<br>
    * The name comparison is not case sensitive.
    *
    * @param name
    *            String containing the file's or directory's name to be searched.
    * @return true when a child node with the given name was found.
    */
   public boolean hasChildNamed(String name) {    	
       
       return (getChildIndex (name) > -1);
   }
   
   /**
    * <b>getChildObjects</b> returns a list of all the objects attached to the child nodes.
    * If the child node corresponds to a subdirectory, the object is a String containing the name of the subdirectory. <br>
    * If the child node corresponds to a file, the object is a FiliusFile object. <br>
    * If the node is a file node, it has no children, and null is always returned in this case.
    *
    * @return Returns a list of all the objects of the child nodes of the given node.
    * 
    * @see getChildFiliusFiles
    */
   public List<Object> getChildObjects() {
	   
	   if (isFile()) return null;
   	        
       List<Object> liste = new ArrayList<Object>();
       Enumeration<TreeNode> enumeration = children();
       while (enumeration.hasMoreElements()) {
    	   FiliusFileNode node = (FiliusFileNode) enumeration.nextElement();
    	   liste.add(node.getUserObject());
       }
       return liste;    
   }    
   
   /**
    * <b>getChildFiliusFiles</b> returns a list of all the FiliusFile objects belonging to the direct children of the node.
	 *
    * @return A list of all the FiliusFile objects belonging to the the direct children of the node.
    * 
    * @see getChildObjects
    */
   public List<FiliusFile> getChildrenFiliusFiles() {
       
       List<FiliusFile> list = new LinkedList<FiliusFile>();
       
       for (Enumeration<TreeNode> e = children(); e.hasMoreElements();) {

       	FiliusFileNode node = (FiliusFileNode) e.nextElement();
       	if (node.isFile()) list.add(((FiliusFileNode)node).getFiliusFile());
       }
       return list;        
   }
   
   /**
    * <b>findFiliusFileList</b> returns a list of FiliusFile objects matching the search pattern.
	 *
    * @param searchPattern
    *            String containing the pattern to look for in the names of the FiliusFile objects.
    * @return A list of FiliusFile objects the name of which contain the searchPattern.
    * 
    * @see getChildObjects 
    */
   public List<FiliusFile> findFiliusFileList(String searchPattern) {
   	        
       List<FiliusFile> list = new LinkedList<FiliusFile>();
       
       for (Enumeration<TreeNode> e = children(); e.hasMoreElements();) {
       	
       	FiliusFileNode node = (FiliusFileNode) e.nextElement();

           if (node.isFile()) {
               FiliusFile fFile = node.getFiliusFile();
               if (fFile.getName().toLowerCase().matches("(.+)?" + searchPattern.toLowerCase() + "(.+)?"))  list.add(fFile);
           }
       }
       return list;
   }
   
   /**
    * <b>getSubdirectoryCount</b> returns the number of child nodes that correspond to a subdirectory.
	 *
	 * return An integer value corresponding to the number of subnodes of type directory.
    */
   public int getSubdirectoryCount() {   	
   	
   	int count = 0;
   	for (int i = 0; i < getChildCount(); i++) {
   		if (getChildAt(i).isDirectory()) count++;
   	}  
   	return count;
   }
    
    /**
     * <b>addSubNode</b> inserts a given node to the children.<br>
     * Directory nodes are stored before the file nodes.<br>
     * Both directory nodes and file nodes are stored in alphabetical order.<br>
     * If the node already has parent, it is removed from the previous parent first.  
	 *
     * @param node FFSNode to be inserted.
     */
    public void addSubNode(FiliusFileNode node) {
    	
    	int count = getChildCount();
    	
    	// Get the index of the first file in the list    	
    	int f = 0;
    	while (f < count && getChildAt(f).isDirectory()) f++;
    	
    	// Set the initial and final possible indexes
    	int i;  
    	int l;
    	if (node.isDirectory()) {
    		i = 0;
    		l = f;
    	} else {
    		i = f;
    		l = count;
    	}   	
    	
    	// Get the index such that the insertion respects the alphabetical order 
    	String name = node.getName();
    	while (i < l && getChildAt(i).getName().compareToIgnoreCase(name) < 0) i++;
    	
    	insert(node, i);    
    }
    
//     // Not essential since the subnodes are serialized in the correct order 
//     // and deserialization restores this order
//    public void add(FiliusFileNode node) {
//    	addSubNode(node);
//    }
    
    /**
     * <b>makeNameUnique</b> returns a name based on the given one, and not already used by a child.<br>
     * In case the name is already in use, a new name is created in the form 'filename (n).ext'.
	 *
     * @param name String containing the name to be made unique.
     * @return A String containing the name made unique.
     */
    public String makeNameUnique(String name) {    	    	
    	
    	if (! hasChildNamed(name)) return name;
    	
    	String newName;
    	String pre = FFS.getPathWithoutExtension(name);
    	String ext = FFS.getPathExtension(name);
    	int i = 2;
    	do {
    		newName = pre + " (" + String.valueOf(i) + ")" + ext;
    		i++;
    	} while(hasChildNamed(newName));
    	
    	return newName;
    }
    
    /**
     * <b>addDirectory</b> adds a subdirectory node.
     *
     * @param name
     *            String containing the name of the subdirectory node to be created.
     * @return true when a directory node was added, false when the name is already used.
    */
    public boolean addDirectory(String name) {    	          
       
    	if (hasChildNamed(name)) return false;

    	addSubNode(new FiliusFileNode(FFS, name));
    	return true;  
    }
    
    /**
     * <b>addFile</b> creates an empty file. 
     * If a subdirectory or file with the given name already exists, nothing happens.
	 *
     * @param name
     *            String containing the name of the file to be created.
     * @param type
     *            String containing the type of the file to be created.
     * @return true when the file was created, false when the name is already used.
    */
    public boolean addFile(String name, FileType type, String content) {    	

    	if (hasChildNamed(name)) return false;
    	
    	addSubNode(new FiliusFileNode(FFS, new FiliusFile(name, type, content)));
    	return true;
    }
    
    /**
	 * <b>importRealFile</b> imports a "real" file into the Filius File System.<br>
	 * The content of binary files is stored in Base64 encoded String.
	 * 
	 * @param filePath String containing the path to the real file to be imported (the last separator is optional). 
	 * @param fileName String containing the name of the real file to be imported. 
	 * @return An error code. 
	 */
	public errorCode importRealFile(String filePath, String fileName) {
				
		// Files can only be imported in directory nodes 
		if (isFile()) return errorCode.NO_DIRECTORY_NODE;	
		
		// Add the last file separator if missing
		if (!filePath.endsWith(System.getProperty("file.separator"))) filePath += System.getProperty("file.separator");
		String fullFileName = filePath + fileName;
		
		// Open the real file
		java.io.File file = new java.io.File(fullFileName);
		
		if (!file.exists()) return errorCode.FILE_NOT_FOUND;		
		if (file.length() > 150*1024) return errorCode.FILE_TOO_LARGE;

		String newName = makeNameUnique(fileName);
		
		FileType type = FFS.getTypeFromExtension(fileName);
		FiliusFile fFile;
		
		if (type != null && (type == FileType.TEXT || type == FileType.CSS || type == FileType.XML)) {
			String text = "";
			try {
				BufferedReader in = new BufferedReader(new FileReader(fullFileName));
				String str;
				while ((str = in.readLine()) != null) {
					text += str + "\r\n";
				}
				in.close();				
			} catch (IOException e) {
				e.printStackTrace(Main.debug);
			}
			fFile = new FiliusFile(newName, type, text);
			
		} else {
			fFile = new FiliusFile(newName, type, Base64.encodeFromFile(fullFileName));
			fFile.setSize(file.length());			
		}
		
		saveFiliusFile(fFile);	

		return errorCode.NO_ERROR;
	}    
	
	/**
	 * <b>exportFile</b> exports the content associated to the node as a real file.
	 * 
	 * @param filePath String containing the path to the real file to be imported (the last separator is optional). 
	 * @param fileName String containing the name of the real file to be imported. 
	 * @return An error code. 
	 */
	public errorCode exportFile(String filePath, String fileName) {
				
		// Only files can be exported 
		if (isDirectory()) return errorCode.NOT_EXPORTABLE;	
		
		// Add the last file separator if missing
		if (!filePath.endsWith(System.getProperty("file.separator"))) filePath += System.getProperty("file.separator");
		String fullFileName = filePath + fileName;
				
		FiliusFile fFile = getFiliusFile();
		
		if (fFile.getType().equals("text")) {
			BufferedWriter writer;
			try {
				writer = new BufferedWriter(new FileWriter(fullFileName));
				writer.write(fFile.getContent());		    
			    writer.close();
			} catch (IOException e) {
				e.printStackTrace(Main.debug);
			}
		    
		} else {
			Base64.decodeToFile(fFile.getContent(), fullFileName);
		}

		return errorCode.NO_ERROR;
	}    

    /**
     * <b>delete</b> removes the node from the filesystem
     *
     */
    public void delete() {
                
        removeFromParent();
    }
    
    /**
     * <b>duplicate</b> returns a copy of the node with all of its subnodes and objects.  
	 *
     * @return A FFSNode which is an exact copy of the one.
     */
    public FiliusFileNode duplicate() {
    	    	
		try {
			// Create ObjectOutputStream
			ByteArrayOutputStream bufOutStream = new ByteArrayOutputStream();
			
			// Write object to stream
			ObjectOutputStream outStream = new ObjectOutputStream(bufOutStream);	
	        outStream.writeObject(this);
	        outStream.close();

	        // Copy stream to buffer
	        byte[] buffer = bufOutStream.toByteArray();

	        // ObjectInputStream
	        ByteArrayInputStream bufInStream = new ByteArrayInputStream(buffer);
	        ObjectInputStream inStream = new ObjectInputStream(bufInStream);

	        // Create new object from stream
	        return (FiliusFileNode) inStream.readObject();
			
		} catch (IOException e) {
			return null;
		} catch (ClassNotFoundException e) {
			return null;
		}        
    }
    
    /**
     * <b>sortChildren</b> reorders the child subnodes.<br>
     * The ordering is based on the name of the file or directory associated to each node.<br> 
     * Directory nodes are stored before the file nodes.<br>
     * Both directory nodes and file nodes are stored in alphabetical order. 
	 *
     */
    public void sortChildren() {
    	
    	int count = getChildCount();
    	
    	// alphabetical ordering of the nodes
    	// Insertion sort algorithm (fast for already sorted list)
    	for (int i = 1; i < count; i++) {
    		FiliusFileNode childNode = getChildAt(i);
    		String name = childNode.getName();
    		int j = i - 1;
    		while (j >= 0) {
    			String name2 = getChildAt(j).getName();
    			if (name.compareToIgnoreCase(name2) >= 0) break;
    			j--;
    		}
    		j++;
    		if (j < i) insert(childNode, j);  
    	}    	
    	
    	// Directories are now moved to the beginning of the list without 
    	// breaking the alphabetical order
    	
    	// Locate the first file in the list
    	int i = 0;
    	int f = count;
    	while (i < f && getChildAt(i).isDirectory()) i++;
    	f = i;
    	i++;
    	
    	// Move following directories before the first file
    	// f contains the index of the first file
    	while (i < count) {
    		FiliusFileNode childNode = getChildAt(i);
    		
    		// Move the node
    		if (childNode.isDirectory()) {    			
    			insert(childNode, f);    			
    			f++;
    		}    		
    		i++;
    	}
    }
    
    /**
     * <b>sortTree</b> recursively reorders the subnodes.<br>
     * The ordering is based on the name of the file or directory associated to each node.<br> 
     * Directory nodes are stored before the file nodes.<br>
     * Both directory nodes and file nodes are stored in alphabetical order. 
	 *
     */
    public void sortSubnodes() {
    	
    	sortChildren();
    	
    	for (int i = 0; i < getChildCount(); i++) {
    		getChildAt(i).sortSubnodes();
    	}  
    }

    /**
     * <b>fixDirectory</b> fixes a set of nodes.
     * 
     */
    public void fixDirectory() {
    	                
        if (getUserObject() == null) {
            setUserObject("restored-" + System.currentTimeMillis());
        } else if (getUserObject() instanceof FiliusFile) {    
        	if (((FiliusFile) getUserObject()).getName().isEmpty()) {
        		((FiliusFile) getUserObject()).setName("restored-" + System.currentTimeMillis());
        	}
//        	if (((FiliusFile) getUserObject()).getType().isEmpty()) {
//        		((FiliusFile) getUserObject()).setType("");
//        	}
        }        
        
        for (int i = 0; i < getChildCount(); i++) {
        	getChildAt(i).fixDirectory();
        }    
    }
    
    /**
     * <b>toPath</b> returns the absolute path corresponding to the node.
	 *
     * @return A String containing the absolute path corresponding to the node.
     */
    public String toPath() {
    	
    	if (getParent() == null) return FFS.FILE_SEPARATOR;
    	
    	String path = "";
    	FiliusFileNode node = this;
    	do {
    		path = FFS.FILE_SEPARATOR + node.getName() + path;    		    	
    		node = node.getParent();    	   
    	} while (node != null && node.getParent() != null);    	// Stop when root is reached

    	return path;
    }
    
    /**
     * <b>toSubnode</b> returns the FiliusFileNode corresponding to the given relative path.
     * The subnode may not be a direct child of the node.
     *
     * @param relativePath
     *            String containing the path to a file or directory relative to the node.
     * @return The node of the file or directory corresponding to the path.
     */
    public FiliusFileNode toNode(String relativePath) {
  
        String absolutePath = FFS.toPath(toPath(), relativePath);

        Enumeration<TreeNode> enumeration = getRoot().preorderEnumeration();
        while (enumeration.hasMoreElements()) {
        	FiliusFileNode node = (FiliusFileNode) enumeration.nextElement();        	
            if (absolutePath.equalsIgnoreCase(node.toPath()))  return node;
        }
        return null;
    }
}
