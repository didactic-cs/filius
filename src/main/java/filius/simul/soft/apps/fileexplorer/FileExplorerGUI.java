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
package filius.gui.anwendungssicht;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Enumeration;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.border.Border;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import filius.gui.JDirTree;
import filius.gui.JFileList;
import filius.software.system.FiliusFileNode;
import filius.software.system.FiliusFileSystem;
import filius.software.system.FiliusFileSystem.FileType;
import filius.software.system.FiliusFileSystem.errorCode;

@SuppressWarnings("serial")
public class GUIApplicationFileExplorerWindow extends GUIApplicationWindow implements PropertyChangeListener {
    
    private FiliusFileSystem FFS;
    private String importExportPath = null;
    private FileFilter importExportFilter = null;
    private GUIDesktopPanel desktop;
    
    // Icons
    private ImageIcon refreshIcon     = new ImageIcon(getClass().getResource("/gfx/desktop/explorer_refresh.png"));
    private ImageIcon newDirIcon      = new ImageIcon(getClass().getResource("/gfx/desktop/explorer_newDirectory.png"));
    private ImageIcon importIcon      = new ImageIcon(getClass().getResource("/gfx/desktop/explorer_import.png"));
    
    // Top components
    private JToolBar toolBar;
    private JLabel  lblPath;
    private JButton btnRefresh;
    private JButton btnNewDirectory;
    private JButton btnImport;

    // Center components    
    private FiliusFileNode rootNode;
    private FiliusFileNode currentDirNode;
    private FiliusFileNode selectedNode;
    private FiliusFileNode copySourceNode = null;
    private FiliusFileNode cutSourceNode = null; 
    
    private JDirTree dirTree;    
    private JScrollPane tvsp;
    private JFileList fileList;
    private JScrollPane lvsp;
    private JSplitPane splitter;

    // Bottom components
    //private JToolBar statusBar;
    //private JLabel statusLbl;

    
    public GUIApplicationFileExplorerWindow(final GUIDesktopPanel desktop, String appName) {
        super(desktop, appName);
        
        FFS = getApplication().getSystemSoftware().getFileSystem();
        FFS.sortTree();    // Only required for older versions of the FFS (saved as Dateisystem) ; Move to ProjectManager?
        rootNode = FFS.getRoot();
        currentDirNode = rootNode;
        this.desktop = desktop;

        initComponents();        
        initListeners();
        registerListeners();
    }

    private void initComponents() {

		JPanel contentPane = (JPanel) getContentPane();

		// Upper toolbar
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setRollover(true);

		// Path label
		lblPath = new JLabel();
		lblPath.setPreferredSize(new Dimension(9000,0));
		toolBar.add(lblPath);
		toolBar.addSeparator();

		// Tool buttons
		Dimension d = new Dimension(22,22);
		
		btnNewDirectory = new JButton(newDirIcon);
		btnNewDirectory.setPreferredSize(d);
		btnNewDirectory.setMargin(new Insets(0, 0, 0, 0));
		btnNewDirectory.setToolTipText(messages.getString("fileexplorer_msg3"));  // New subdirectory
		toolBar.add(btnNewDirectory);

		btnImport = new JButton(importIcon);
		btnImport.setPreferredSize(d);
		btnImport.setMargin(new Insets(0, 0, 0, 0));
		btnImport.setToolTipText(messages.getString("fileexplorer_msg12"));       // Import a file
		toolBar.add(btnImport);
		
		btnRefresh = new JButton(refreshIcon);
		btnRefresh.setPreferredSize(d);
		btnRefresh.setMargin(new Insets(0, 0, 0, 0));
		btnRefresh.setToolTipText(messages.getString("fileexplorer_msg29"));      // Refresh the display (F5)
		toolBar.add(btnRefresh);

		contentPane.add(toolBar, BorderLayout.NORTH);

		// Directories' treeview				
		dirTree = new JDirTree(rootNode);	
		
		tvsp = new JScrollPane(dirTree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		tvsp.setMinimumSize(new Dimension(100,0));
		tvsp.setPreferredSize(new Dimension(140,0));

		// Content's listview
		fileList = new JFileList();
    	
		lvsp = new JScrollPane(fileList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		lvsp.setMinimumSize(new Dimension(100,0));
		
		// Drag & drop
		//fileList.setDragEnabled(true);
		//fileList.setDropMode(DropMode.ON_OR_INSERT);
		
		//fileList.setTransferHandler(new lvFilesTransferHandler());
		//dirTree.setTransferHandler(new ImportTransferHandler());

		// Splitter
		splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tvsp, lvsp);
		splitter.setDividerSize(3);
		contentPane.add(splitter, BorderLayout.CENTER);

		// Lower status bar
		//statusBar = new JToolBar();
		//statusBar.setFloatable(false);

		//statusLbl = new JLabel("Status bar");
		//statusBar.add(statusLbl);

		//contentPane.add(statusBar, BorderLayout.SOUTH);

		updateListContent();
		updatePathLabel();

		pack();
		
		centerWindow();
    }
    
    private void initListeners() {

    	btnRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	refreshDisplay();
            }
        });

    	btnNewDirectory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	createSubdirectory();
            }
        });

    	btnImport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                importFile();
            }
        });

    	dirTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
            	FiliusFileNode node = dirTree.getSelectedNode();

                if (node != null) {
                    currentDirNode = node;
                    updateListContent();
                    updatePathLabel();
                }
            }
        });

    	fileList.addMouseListener(new MouseAdapter() {    		
            @Override
            public void mouseClicked(MouseEvent e) {
            	
            	if (e.getButton() == MouseEvent.BUTTON1) {
                    if (currentDirNode != null) {
                    	int index = fileList.locationToIndex(e.getPoint());
                    	if (index > -1) {
                    		selectedNode = fileList.getElementAt(index);
                    	}                    	
                    }
                    if (e.getClickCount() == 2) {
                    	int index = locationToRealIndex(fileList, e.getPoint());
                    	if (index > -1) {
                    		openInApplication(fileList.getElementAt(index));
                    	}                    	
                    }
            	}
            	
                if (e.getButton() == MouseEvent.BUTTON3) {
                    if (currentDirNode != null) {

                        final JMenuItem miNewDirectory = new JMenuItem(messages.getString("fileexplorer_msg3"));
                        final JMenuItem miNewFile = new JMenuItem(messages.getString("fileexplorer_msg23"));      // New text file
                        final JMenuItem miImportFile = new JMenuItem(messages.getString("fileexplorer_msg12"));   // Import a file
                        final JMenuItem miExportFile = new JMenuItem(messages.getString("fileexplorer_msg35"));   // Export a file
                        final JMenuItem miCutFile = new JMenuItem(messages.getString("fileexplorer_msg5"));
                        final JMenuItem miCopyFile = new JMenuItem(messages.getString("fileexplorer_msg6"));
                        final JMenuItem miPasteFile = new JMenuItem(messages.getString("fileexplorer_msg7"));
                        final JMenuItem miDeleteFile = new JMenuItem(messages.getString("fileexplorer_msg4"));
                        final JMenuItem miRenameFile = new JMenuItem(messages.getString("fileexplorer_msg8"));

                        ActionListener al = new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                if (e.getSource() == miNewDirectory) createSubdirectory();
                                if (e.getSource() == miNewFile) createFile();
                                if (e.getSource() == miImportFile) importFile();
                                if (e.getSource() == miExportFile) exportFile();
                                if (e.getSource() == miDeleteFile) deleteFile();
                                if (e.getSource() == miCutFile) cutFile();
                                if (e.getSource() == miCopyFile) copyFile();
                                if (e.getSource() == miPasteFile) pasteFile();
                                if (e.getSource() == miRenameFile) renameFile();
                            }
                        };

                        miNewDirectory.addActionListener(al);
                        miNewFile.addActionListener(al);
                        miImportFile.addActionListener(al);
                        miExportFile.addActionListener(al);
                        miDeleteFile.addActionListener(al);
                        miCutFile.addActionListener(al);
                        miCopyFile.addActionListener(al);
                        miPasteFile.addActionListener(al);
                        miRenameFile.addActionListener(al);

                        JPopupMenu popMenu = new JPopupMenu();

                        int index = locationToRealIndex(fileList, e.getPoint());
                        if (index == -1) {
                            if (copySourceNode != null || (cutSourceNode != null && !selectedNode.isAncestorOf(cutSourceNode))) {
                            	popMenu.add(miPasteFile);
                            	popMenu.addSeparator();
                            }
                            popMenu.add(miNewDirectory);
                            popMenu.add(miNewFile);
                            popMenu.addSeparator();
                            popMenu.add(miImportFile);

                        } else {
                            selectedNode = fileList.getElementAt(index);
                            fileList.setSelectedIndex(index);
                            popMenu.add(miCutFile);
                            popMenu.add(miCopyFile);
                            popMenu.addSeparator();
                            popMenu.add(miRenameFile);
                            popMenu.add(miDeleteFile);
                            if (selectedNode.isFile()) {
                            	popMenu.addSeparator();
                            	popMenu.add(miExportFile);
                            }
                        }

                        fileList.add(popMenu); 
                        popMenu.show(fileList, e.getX(), e.getY());
                    }
                }
            }
        });
    	
    	fileList.addKeyListener(new KeyAdapter() {      		
    		@Override
            public void keyPressed(KeyEvent e) {
    			switch (e.getKeyCode()) {
    				case KeyEvent.VK_C:
    					if (e.isControlDown()) copyFile();
    					break;
    				case KeyEvent.VK_V:
    					if (e.isControlDown()) pasteFile();
    					break;	
    				case KeyEvent.VK_X:
    					if (e.isControlDown()) cutFile();
    					break;
    				case KeyEvent.VK_DELETE:
    					deleteFile();
    					break;
    			}
    		}
    	});

    	addInternalFrameListener(new InternalFrameAdapter() {    		
    		
    		public void internalFrameActivated(InternalFrameEvent e) {
    			
    			// Refresh the display when switching back to the file explorer       			  			
    			Object node = fileList.getSelectedValue();    			
    			
    			refreshDisplay();
    			
    			// Try to restore the memorized selection if it still exists 		
    			if (node != null) {
    				for (int i = 0; i < fileList.getCount(); i++) {
    					if (node ==  fileList.getElementAt(i)) {
    						fileList.setSelectedValue(node, true);
    						break;
    					}
    				}
    			}
            }
    	});
    }


    /** Returns the index of the list cell on which the point is located or -1 if the point is below the last cell */
    public int locationToRealIndex(JFileList list, Point point) {

    	int index = list.locationToIndex(point);
        if ((list.indexToLocation(index) != null) && (point.getY() < list.indexToLocation(index).getY() + list.getFixedCellHeight())) {
            return index;
        } else {
        	return -1;
        }
    }

    /** Update the JList with the content of the current directory node */
    public void updateListContent() {

    	fileList.clear();

        for (Enumeration<TreeNode> e = currentDirNode.children(); e.hasMoreElements();) {        	        	
        	fileList.addElement((FiliusFileNode) e.nextElement());
        }
    }

    /** Update the "breadcrumbs" shown above the JTree and the JList */
    public void updatePathLabel() {

    	TreeNode[] nodes = currentDirNode.getPath();

    	String path = "  ";
    	for (int i=0; i<nodes.length; i++) {
    		if (i>0)  path = path + " \u25B8 ";  // right full triangle
    		path = path + nodes[i].toString();
    	}

    	lblPath.setText(path);
    }

    public void refreshDisplay() {
        dirTree.updateUI();
        updateListContent();
    }

    private void createSubdirectory() {
    	
    	String dirname = "";
    	
    	while(true) {
    		dirname = OKCancelDialog(messages.getString("fileexplorer_msg19"), // Type in the name of a new subdirectory
    				                 messages.getString("fileexplorer_msg3"),  // New directory    
    		                         dirname);
    		if (dirname == null) return;
    		
    		dirname = dirname.trim();
    		
    		if (dirname.isEmpty())  
    			warningDialog(messages.getString("fileexplorer_msg20"), // The name can't be empty
    					      messages.getString("fileexplorer_msg3")); // New directory
    		
    		else if (currentDirNode.hasChildNamed(dirname))
    			warningDialog(messages.getString("fileexplorer_msg21"), // This name is already in use.
    				          messages.getString("fileexplorer_msg3")); // New directory
    		 
    		else if (! FFS.nameIsValid(dirname)) 
    			warningDialog(messages.getString("fileexplorer_msg22"), // The following characters are not allowed...
				              messages.getString("fileexplorer_msg3")); // New directory    			
    		else {
    			currentDirNode.addDirectory(dirname);
    			refreshDisplay();
    			return; 
    		}    		
    	}    	
    }

    private void createFile() {
    	
    	String filename = "";
    			
    	while(true) {
    		filename = OKCancelDialog(messages.getString("fileexplorer_msg24"), // Type in the name of a new text file
    				                  messages.getString("fileexplorer_msg23"), // New text file
    				                  filename);
    		if (filename == null) return;
    		
    		filename = filename.trim();
    		
    		if (filename.isEmpty()) 
    			warningDialog(messages.getString("fileexplorer_msg20"),  // The name can't be empty
					          messages.getString("fileexplorer_msg23")); // New text file 
    			
    		else if (currentDirNode.hasChildNamed(filename)) 
    		    warningDialog(messages.getString("fileexplorer_msg21"),  // This name is already in use.
				              messages.getString("fileexplorer_msg23")); // New text file
    		
    		else if (! FFS.nameIsValid(filename)) 
    			warningDialog(messages.getString("fileexplorer_msg22"),  // The following characters are not allowed...
			                  messages.getString("fileexplorer_msg23")); // New text file    
    		
    		else {
    			currentDirNode.addFile(filename, FileType.TEXT, null);   
    			refreshDisplay();
    			return; 
    		}    		
    	}   
    }

    private void deleteFile() {
    	
    	if (selectedNode == null) return;
    	
    	String msg = messages.getString("fileexplorer_msg25") + selectedNode.getName() + messages.getString("fileexplorer_msg26");
    	             // Are you sure you want to delete ' ... '?
    	if (selectedNode.getChildCount() > 0) msg = msg + "\n" + messages.getString("fileexplorer_msg27");   
    	                                                         // All the files and subdirectories it contains will also be deleted.
        	
        if (yesNoDialog(msg, messages.getString("fileexplorer_msg4"))) { // Delete
            currentDirNode.remove(selectedNode);
            selectedNode = null;
            refreshDisplay();
        }
    }
    
    private void copyFile() {
    	
    	if (selectedNode == null) return;
    	cutSourceNode = null;
    	
    	copySourceNode = selectedNode;
    }

    private void cutFile() {
    	if (selectedNode == null) return;
    	copySourceNode = null;
    	
    	cutSourceNode = selectedNode;
    }

    private void pasteFile() {
    	
    	if (copySourceNode != null) {
    		// Create a copy of the copySourceNode 
    		
    		// Get a unique name for the copy
    		String name = copySourceNode.getName();
    		name = currentDirNode.makeNameUnique(name);
    		
    		FiliusFileNode newNode = copySourceNode.duplicate();
    		
    		if (newNode != null) {
    			newNode.setName(name);
    			currentDirNode.addSubNode(newNode);
    			refreshDisplay();
    		}
    		
    	} else if (cutSourceNode != null) {
    		// Move the cutSourceNode
    		
    		if (cutSourceNode.getParent() != currentDirNode) {
    			currentDirNode.addSubNode(cutSourceNode);
    			refreshDisplay();
    		}    		
            cutSourceNode = null;
    	}
    }

    private void renameFile() {
    	
    	if (selectedNode == null) return;

    	String currentName = selectedNode.getName();
    	
    	while (true) {
    		String newName = OKCancelDialog(messages.getString("fileexplorer_msg28"), // Type in the new name of the file
    				                        messages.getString("fileexplorer_msg8"),  // Rename
    				                        currentName);
    		if (newName == null || newName.equals(currentName)) return;
    		
    		newName = newName.trim();
    		
    		if (newName.isEmpty()) 
    			warningDialog(messages.getString("fileexplorer_msg20"),  // The name can't be empty
				              messages.getString("fileexplorer_msg8"));  // Rename
    			
    		else if (currentDirNode.hasChildNamed(newName) && (!currentName.toLowerCase().equals(newName.toLowerCase()))) 
    			warningDialog(messages.getString("fileexplorer_msg21"),  // This name is already in use.
			                  messages.getString("fileexplorer_msg8"));  // Rename
    			
    		else if (! FFS.nameIsValid(newName)) 
    		    warningDialog(messages.getString("fileexplorer_msg22"),  // The following characters are not allowed...
	                          messages.getString("fileexplorer_msg8"));  // Rename
    			
    		else {
    			selectedNode.setName(newName);
    			refreshDisplay();
    			return; 
    		}
    	}    	
    }

    public void importFile() {
    	
    	String dialogTitle  = messages.getString("fileexplorer_msg12");  // Import a file
    	String dialogButton = messages.getString("fileexplorer_msg15");  // Import
    	
    	while (true) {
    		JFileChooser fc = new JFileChooser(importExportPath);
        	fc.setDialogTitle(dialogTitle);
        	fc.addChoosableFileFilter(new FileNameExtensionFilter(messages.getString("fileexplorer_msg38"), "txt", "ini", "cfg", "conf", "xml"));
        	                                                      // Text files
        	fc.addChoosableFileFilter(new FileNameExtensionFilter(messages.getString("fileexplorer_msg39"), "htm", "html"));
        	                                                      // Html files
        	fc.addChoosableFileFilter(new FileNameExtensionFilter(messages.getString("fileexplorer_msg40"), "bmp", "jpeg", "jpg", "png"));
        	                                                      // Images
        	if (importExportFilter != null) fc.setFileFilter(importExportFilter);
         	
        	// File selection
        	int dlgRes = fc.showDialog(this, dialogButton);
        	
        	// Cancel import operation
        	if (dlgRes == JFileChooser.CANCEL_OPTION) return;
        	
        	// Keep path and filter for future imports
        	importExportPath = fc.getSelectedFile().getParent();
        	importExportFilter = fc.getFileFilter();
        	
        	// Split filePath and fileName    	
        	String filePath = importExportPath;
        	String fileName = fc.getSelectedFile().getName();

        	errorCode erCode = currentDirNode.importRealFile(filePath, fileName);
        	
        	// File imported with success
        	if (erCode == errorCode.NO_ERROR) {
        		refreshDisplay();
        		return;   
        	}
        	
        	String msg = messages.getString("fileexplorer_msg30") + "\n";  // The selected file could not be imported
        	switch (erCode) {
        	case FILE_NOT_FOUND:        	
        		msg += messages.getString("fileexplorer_msg31");   // The file could not be found
        		break; 
        	case FILE_TOO_LARGE:          
        		msg += messages.getString("fileexplorer_msg32");   // The file is too large.\nThe maximum size allowed is 150 KB.
        		break;
        	default:   
        	}    
        	
        	// Error message display
        	warningDialog(msg, dialogTitle); 
    	}
    }
    
    public void exportFile() {
    	
    	if (selectedNode == null) return;

    	String dialogTitle  = messages.getString("fileexplorer_msg35");  // Export the file
    	String dialogButton = messages.getString("fileexplorer_msg36");  // Export
    	
    	while (true) {
    		JFileChooser fc = new JFileChooser(importExportPath);
        	fc.setDialogTitle(dialogTitle);     
        	fc.setSelectedFile(new File(selectedNode.getName()));
         	
        	// File selection
        	int dlgRes = fc.showDialog(this, dialogButton);
        	
        	// Cancel import operation
        	if (dlgRes == JFileChooser.CANCEL_OPTION) return;
        	
        	// Keep path and filter for future imports
        	importExportPath = fc.getSelectedFile().getParent();  
        	
        	if (fc.getSelectedFile().exists()) {
        		if (! yesNoDialog(messages.getString("fileexplorer_msg37"), messages.getString("fileexplorer_msg36"))) continue;  // Replace ?  // Export
        	}
        	
        	// Split filePath and fileName    	
        	String filePath = importExportPath;
        	String fileName = fc.getSelectedFile().getName();

        	errorCode erCode = selectedNode.exportFile(filePath, fileName);
                	
        	// File exported with success
        	if (erCode == errorCode.NO_ERROR) return;   
    	}
    }
    
    /** <b>openInApplication</b> opens the file associated to the node in the corresponding application (if it's installed).
     * If the node corresponds to a directory, this node is opened in the dirTree.
     * 
     * */ 
    public void openInApplication(FiliusFileNode node) {
    	
    	if (node.isDirectory()) {
    		dirTree.setSelectionPath(new TreePath(node.getPath()));
    	} else {
    		FileType type = node.getType();
    		
    		GUIApplicationWindow appWindow = null;
    		
            if (type == FileType.TEXT || type == FileType.CSS || type == FileType.XML) {
    			
    			appWindow = desktop.startApp("filius.software.lokal.TextEditor", node);
    		}
            else if (type == FileType.HTML) {
            	
            	appWindow = desktop.startApp("filius.software.www.WebBrowser", node);
    		}
            else if (type == FileType.IMAGE_JPG || type == FileType.IMAGE_PNG ||
    		         type == FileType.IMAGE_GIF || type == FileType.IMAGE_BMP) {
    			
    			appWindow = desktop.startApp("filius.software.lokal.ImageViewer", node);        		
    		} 
            
            // Display a message if the associated application is not installed?
            
            if (appWindow != null) appWindow.makeActiveWindow();
    	}
    }   
    
    // Event listeners
    //-----------------
    
    private void registerListeners() {    	
    	
    	//node.getSystemSoftware().addPropertyChangeListener("message", this);
    }
    
	/**
     * <b>propertyChange</b> is called whenever a change in the host must be reflected by the user interface. 
     *     
     */
	public void propertyChange(PropertyChangeEvent evt) { 
		
//		String pn = evt.getPropertyName();		

//		if (pn.equals("nodename")) {
//			                           
//			// Update the display	
//			// From: FFS
//			refreshDisplay();			
//		} 
	};    
    
//    @Override
//    public void update(Observable arg0, Object arg1) {
//        //refreshDisplay();
//    }    
}
