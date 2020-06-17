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
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.ConstructorProperties;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.Observable;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import filius.software.system.Datei;
import filius.software.system.FiliusFileSystem;
import filius.software.system.FiliusFileSystem.errorCode;

public class GUIApplicationFileExplorerWindow extends GUIApplicationWindow {

    private static final long serialVersionUID = 1L;
    
    private FiliusFileSystem FFS;
    private String importExportPath = null;

    // Icons
    private ImageIcon driveIcon = new ImageIcon(getClass().getResource("/gfx/desktop/explorer_drive.png"));
    private ImageIcon closedDirIcon = new ImageIcon(getClass().getResource("/gfx/desktop/explorer_closedDirectory.png"));
    private ImageIcon openDirIcon = new ImageIcon(getClass().getResource("/gfx/desktop/explorer_openDirectory.png"));
    private ImageIcon genericFileIcon = new ImageIcon(getClass().getResource("/gfx/desktop/explorer_genericFile.png"));
    private ImageIcon textFileIcon = new ImageIcon(getClass().getResource("/gfx/desktop/explorer_textFile.png"));
    private ImageIcon htmlFileIcon = new ImageIcon(getClass().getResource("/gfx/desktop/explorer_htmlFile.png"));
    private ImageIcon imageFileIcon = new ImageIcon(getClass().getResource("/gfx/desktop/explorer_imageFile.png"));
    private ImageIcon soundFileIcon = new ImageIcon(getClass().getResource("/gfx/desktop/explorer_soundFile.png"));
    private ImageIcon refreshIcon = new ImageIcon(getClass().getResource("/gfx/desktop/explorer_refresh.png"));
    private ImageIcon newDirIcon = new ImageIcon(getClass().getResource("/gfx/desktop/explorer_newDirectory.png"));
    private ImageIcon importIcon = new ImageIcon(getClass().getResource("/gfx/desktop/explorer_import.png"));
    
    // Top components
    private JToolBar toolBar;
    private JLabel  lblPath;
    private JButton btnRefresh;
    private JButton btnNewDirectory;
    private JButton btnImport;

    // Center components
    private JTree tvDirs;
    private DefaultMutableTreeNode rootNode;
    private DefaultMutableTreeNode currentDirNode;
    private DefaultMutableTreeNode selectedNode;
    private DefaultMutableTreeNode copySourceNode = null;
    private DefaultMutableTreeNode cutSourceNode = null; 
    private JScrollPane tvsp;
    private JList lvFiles;
    private DefaultListModel fileList;
    private JScrollPane lvsp;
    private JSplitPane splitter;

    // Bottom components
    private JToolBar statusBar;
    private JLabel statusLbl;

    // Tree model
    private class expTreeModel extends DefaultTreeModel {

		private static final long serialVersionUID = 1L;		
		
	    public expTreeModel(TreeNode root) {
	       super(root, false);
	    }

		@Override
        public boolean isLeaf(Object node) {

			// Nodes are leaf when they have no child directory
			for (Enumeration e = ((TreeNode)node).children(); e.hasMoreElements();) {

	            DefaultMutableTreeNode subnode = (DefaultMutableTreeNode) e.nextElement();	            
	            if (FFS.isDirectory(subnode)) return false;
	        }			
			return true; 
        }
		
		@Override
		public int getChildCount(Object parent) {
			
			// Only count the subnodes that are subdirectories	 
			// This relies on the fact that the FiliusFileSystem stores the
			// subdirectories prior to the files for a given directory node.
			return FFS.countSubdirectoryNodes((DefaultMutableTreeNode)parent);
	    }
    }

    // Tree renderer
    private class expTreeRenderer extends DefaultTreeCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            
            if (expanded) setIcon(openDirIcon);
            else	      setIcon(closedDirIcon);
 
            if (((DefaultMutableTreeNode) value).isRoot()) setIcon(driveIcon);

            return this;
        }
    }

    // List renderer
    private class expListRenderer extends DefaultListCellRenderer {

    	@Override
    	public Component getListCellRendererComponent(JList list, Object value, int index, boolean selected, boolean hasFocus) {

    		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
    		
    		if (FFS.isFile(node)) {
    			setText(FFS.getName(node)+"  ("+String.valueOf(FFS.getSize(node))+" o)");
    		} else {
    			setText(FFS.getName(node));
    		}

    		switch (FFS.getType(node)) {
    			case "directory": setIcon(closedDirIcon); break;
    			case "image":     setIcon(imageFileIcon); break;
    			case "sound":     setIcon(soundFileIcon); break;
    			case "text":      setIcon(textFileIcon); break;
    			case "web":       setIcon(htmlFileIcon); break;    			
    			default:          setIcon(genericFileIcon); break;
    		}    		

            if (selected) {
                setBackground(new Color(184, 207, 229));
                setOpaque(true);
            } else {
                setOpaque(false);
            }

            Border border = null;
            if (hasFocus) border = BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(99, 130, 191));
            setBorder(border);

            return this;
        }
    }

    //*****************************************************************
    // FileExplorerWindow
    //*****************************************************************
    
    public GUIApplicationFileExplorerWindow(final GUIDesktopPanel desktop, String appName) {
        super(desktop, appName);
        FFS = holeAnwendung().getSystemSoftware().getDateisystem();
        FFS.sortTree(); // Only necessary for older versions of the FFS (saved as Dateisystem)
        rootNode = FFS.getRoot();
        currentDirNode = rootNode;

        createComponents();
        initListeners();
    }

    private void createComponents() {

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
		btnRefresh = new JButton(refreshIcon);
		btnRefresh.setToolTipText("Refresh the display (F5)");        // << I18n
		toolBar.add(btnRefresh);

		btnNewDirectory = new JButton(newDirIcon);
		btnNewDirectory.setToolTipText("Create a new subdirectory");  // << I18n
		toolBar.add(btnNewDirectory);

		btnImport = new JButton(importIcon);
		btnImport.setToolTipText("Import a file");                    // << I18n  (same string as in popup menu below)
		toolBar.add(btnImport);

		contentPane.add(toolBar, BorderLayout.NORTH);

		// Directories' treeview
		tvDirs = new JTree(rootNode);		
		tvDirs.setModel(new expTreeModel(rootNode));
		tvDirs.setCellRenderer(new expTreeRenderer());
		tvDirs.setSelectionPath(new TreePath(rootNode.getPath()));
		tvsp = new JScrollPane(tvDirs, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		tvsp.setMinimumSize(new Dimension(100,0));
		tvsp.setPreferredSize(new Dimension(200,0));

		// Content's listview
		fileList = new DefaultListModel();
		lvFiles = new JList(fileList);
		lvFiles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lvFiles.setCellRenderer(new expListRenderer());
    	lvFiles.setFixedCellHeight(16);
		lvsp = new JScrollPane(lvFiles, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		lvsp.setMinimumSize(new Dimension(100,0));

		// Splitter
		splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tvsp, lvsp);
		splitter.setDividerSize(3);
		contentPane.add(splitter, BorderLayout.CENTER);

		// Lower status bar
//		statusBar = new JToolBar();
//		statusBar.setFloatable(false);
//
//		statusLbl = new JLabel("Status bar");
//		statusBar.add(statusLbl);
//
//		contentPane.add(statusBar, BorderLayout.SOUTH);

		updateListContent();
		updatePathLabel();

		pack();
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

    	tvDirs.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tvDirs.getLastSelectedPathComponent();

                if (node != null) {
                    currentDirNode = node;
                    updateListContent();
                    updatePathLabel();
                }
            }
        });

    	lvFiles.addMouseListener(new MouseAdapter() {    		
            @Override
            public void mouseClicked(MouseEvent e) {
            	
            	if (e.getButton() == MouseEvent.BUTTON1) {
                    if (currentDirNode != null) {
                    	int index = lvFiles.locationToIndex(e.getPoint());
                    	if (index > -1) {
                    		selectedNode = (DefaultMutableTreeNode) fileList.getElementAt(index);
                    	}                    	
                    }
                    if (e.getClickCount() == 2) {
                    	int index = locationToRealIndex(lvFiles, e.getPoint());
                    	if (index > -1) {
                    		openInApplication((DefaultMutableTreeNode) fileList.getElementAt(index));
                    	}                    	
                    }
            	}
            	
                if (e.getButton() == MouseEvent.BUTTON3) {
                    if (currentDirNode != null) {

                        final JMenuItem miNewDirectory = new JMenuItem(messages.getString("fileexplorer_msg3"));
                        final JMenuItem miNewFile = new JMenuItem("New empty file");                                // << I18n
                        final JMenuItem miImportFile = new JMenuItem("Import a file");         						// << I18n
                        final JMenuItem miExportFile = new JMenuItem("Export");                                     // << I18n
                        final JMenuItem miCutFile = new JMenuItem(messages.getString("fileexplorer_msg5"));
                        final JMenuItem miCopyFile = new JMenuItem(messages.getString("fileexplorer_msg6"));
                        final JMenuItem miPasteFile = new JMenuItem(messages.getString("fileexplorer_msg7"));
                        final JMenuItem miDeleteFile = new JMenuItem(messages.getString("fileexplorer_msg4"));
                        final JMenuItem miRenameFile = new JMenuItem(messages.getString("fileexplorer_msg8"));

                        ActionListener al = new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                if (e.getSource() == miNewDirectory) createSubdirectory();
                                if (e.getSource() == miNewFile) createEmptyFile();
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

                        int index = locationToRealIndex(lvFiles, e.getPoint());
                        if (index == -1) {
                            if (copySourceNode != null || (cutSourceNode != null && !FFS.isAncestorNode(selectedNode, cutSourceNode))) {
                            	popMenu.add(miPasteFile);
                            	popMenu.addSeparator();
                            }
                            popMenu.add(miNewDirectory);
                            popMenu.add(miNewFile);
                            popMenu.addSeparator();
                            popMenu.add(miImportFile);

                        } else {
                            selectedNode = (DefaultMutableTreeNode) fileList.getElementAt(index);
                            lvFiles.setSelectedIndex(index);
                            popMenu.add(miCutFile);
                            popMenu.add(miCopyFile);
                            popMenu.addSeparator();
                            popMenu.add(miRenameFile);
                            popMenu.add(miDeleteFile);
//                            popMenu.addSeparator();
//                            popMenu.add(miExportFile);
                        }

                        lvFiles.add(popMenu); 
                        popMenu.show(lvFiles, e.getX(), e.getY());
                    }

                }
            }
        });
    	
    	lvFiles.addKeyListener(new KeyAdapter() {      		
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

    	this.addInternalFrameListener(new InternalFrameAdapter() {    		
    		
    		public void internalFrameActivated(InternalFrameEvent e) {
    			
    			// Refresh the display when switching back to the file explorer       			  			
    			Object node = lvFiles.getSelectedValue();
    			
    			
    			refreshDisplay();
    			
    			// Try to restore the memorized selection  		
    			if (node != null) {
    				for (int i = 0; i < fileList.getSize(); i++) {
    					if (node ==  fileList.getElementAt(i)) {
    						lvFiles.setSelectedValue(node, true);
    						break;
    					}
    				}
    			}
            }
    	});
    }


    // Returns the index of the list cell on which the point is located or -1 if the point is below the last cell
    public int locationToRealIndex(JList list, Point point) {

    	int index = list.locationToIndex(point);
        if ((list.indexToLocation(index) != null) && (point.getY() < list.indexToLocation(index).getY() + list.getFixedCellHeight())) {
            return index;
        } else {
        	return -1;
        }
    }

    // Update the JList with the content of the current directory node
    public void updateListContent() {

    	fileList.clear();

        for (Enumeration e = currentDirNode.children(); e.hasMoreElements();) {        	        	
        	fileList.addElement(e.nextElement());
        }
    }

    public void updatePathLabel() {

    	TreeNode[] nodes = currentDirNode.getPath();

    	String path = "  ";
    	for (int i=0; i<nodes.length; i++) {
    		if (i>0)  path = path + " \u25B8 ";
    		path = path + nodes[i].toString();
    	}

    	lblPath.setText(path);
    }

    public void refreshDisplay() {
        tvDirs.updateUI();
        updateListContent();
    }

    private void createSubdirectory() {
    	
    	while(true) {
    		String newDir = JOptionPane.showInputDialog(this, "Type in the name of the new subdirectory", "New subdirectory",     // I18n
    													JOptionPane.QUESTION_MESSAGE);  
    		if (newDir == null) return;
    		
    		if (newDir.isEmpty()) {
    			JOptionPane.showMessageDialog(this, "A name must be typed in!", "New subdirectory", JOptionPane.WARNING_MESSAGE);    			
    		} else if (FFS.fileExists(currentDirNode, newDir)) {
    			JOptionPane.showMessageDialog(this, "A file or directory with this name already exists!", "New subdirectory", JOptionPane.WARNING_MESSAGE); 
    		} else if (! FFS.nameIsValid(newDir)) {
    			JOptionPane.showMessageDialog(this, "The name typed in contains unallowed characters!"+
    		                                  "\nUnallowed characters are \\ | / \" : ? * < >", "New subdirectory", JOptionPane.WARNING_MESSAGE);     			
    		} else {
    			FFS.createDirectory(currentDirNode, newDir);
    			refreshDisplay();
    			return; 
    		}    		
    	}    	
    }

    private void createEmptyFile() {
    	
    	while(true) {
    		String newFile = JOptionPane.showInputDialog(this, "Type in the name of the new file", "New file",     // I18n
    													JOptionPane.QUESTION_MESSAGE);  
    		if (newFile == null) return;
    		
    		if (newFile.isEmpty()) {
    			JOptionPane.showMessageDialog(this, "A name must be typed in!", "New file", JOptionPane.WARNING_MESSAGE);    			
    		} else if (FFS.fileExists(currentDirNode, newFile)) {
    			JOptionPane.showMessageDialog(this, "A file or directory with this name already exists!", "New file", JOptionPane.WARNING_MESSAGE); 
    		} else if (! FFS.nameIsValid(newFile)) {
    			JOptionPane.showMessageDialog(this, "The name typed in contains unallowed characters!"+
    		                                  "\nUnallowed characters are \\ | / \" : ? * < >", "New file", JOptionPane.WARNING_MESSAGE); 
    		} else {
    			FFS.createFile(currentDirNode, newFile);
    			refreshDisplay();
    			return; 
    		}    		
    	}   
    }

    private void deleteFile() {
    	if (selectedNode == null) return;
    	
    	String msg = "Are you sure that you want to delete ' "+ FFS.getName(selectedNode) +" ' ?";
    	if (selectedNode.getChildCount() > 0) {
    		msg = msg + "\n" + "All the files and subdirectories it contains will also be deleted.";    		
    	}
    	
    	int confirm = JOptionPane.showConfirmDialog(this, msg, "Deletion", JOptionPane.YES_NO_OPTION); // I18n fileexplorer_msg18

        if (confirm == JOptionPane.YES_OPTION) {
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
    		String name = FFS.getName(copySourceNode);
    		name = FFS.getUniqueName(currentDirNode, name);
    		
    		DefaultMutableTreeNode newNode = FFS.cloneNode(copySourceNode);
    		
    		if (newNode != null) {
    			FFS.setName(newNode, name);
    			FFS.addSubNode(currentDirNode, newNode);
    			refreshDisplay();
    		}
    		
    	} else if (cutSourceNode != null) {
    		// Move the cutSourceNode
    		
    		if (cutSourceNode.getParent() != currentDirNode) {
    			FFS.addSubNode(currentDirNode, cutSourceNode);
    			refreshDisplay();
    		}    		
            cutSourceNode = null;
    	}
    }

    private void renameFile() {
    	if (selectedNode == null) return;

    	String currentName = FFS.getName(selectedNode);
    	
    	while(true) {
    		String newName = (String)JOptionPane.showInputDialog(this, "Type in the new name", "Renaming",            // I18n fileexplorer_msg9
					                                             JOptionPane.QUESTION_MESSAGE, null, null, currentName);    		
    		if (newName == null) return;
    		
    		newName = newName.trim();
    		
    		if (newName.isEmpty()) {
    			JOptionPane.showMessageDialog(this, "A name must be typed in!", "Renaming", JOptionPane.WARNING_MESSAGE);    			
    		} else if (FFS.fileExists(currentDirNode, newName) && (!currentName.toLowerCase().equals(newName.toLowerCase()))) {
    			JOptionPane.showMessageDialog(this, "A file or directory with this name already exists!", "Renaming", JOptionPane.WARNING_MESSAGE); 	
    		} else if (! FFS.nameIsValid(newName)) {
    			JOptionPane.showMessageDialog(this, "The name typed in contains unallowed characters!"+
    		                                  "\nUnallowed characters are \\ | / \" : ? * < >", "New file", JOptionPane.WARNING_MESSAGE); 
    		} else {
    			FFS.setName(selectedNode, newName);
    			refreshDisplay();
    			return; 
    		}
    	}    	
    }

    public void importFile() {
    	
    	String dialogTitle  = "Import a file";                       // I18n
    	String dialogButton = "Import";                              // I18n
    	
    	while (true) {
    		JFileChooser fc = new JFileChooser(importExportPath);
        	fc.setDialogTitle(dialogTitle);
        	fc.addChoosableFileFilter(new FileNameExtensionFilter("Text files", "txt", "ini", "cfg", "xml"));
        	fc.addChoosableFileFilter(new FileNameExtensionFilter("Web files", "htm", "html", "css", "js"));
        	fc.addChoosableFileFilter(new FileNameExtensionFilter("Image files", "bmp", "jpeg", "jpg", "png"));
        	fc.addChoosableFileFilter(new FileNameExtensionFilter("Sound files", "mp2", "mp3", "wav"));
        
        	// File selection
        	int dlgRes = fc.showDialog(this, dialogButton);
        	
        	// Cancel import operation
        	if (dlgRes == JFileChooser.CANCEL_OPTION) return;
        	
        	// Keep path for future imports
        	importExportPath = fc.getSelectedFile().getParent();
        	
        	// Split filePath and fileName    	
        	String filePath = importExportPath;
        	String fileName = fc.getSelectedFile().getName();

        	errorCode erCode = FFS.importRealFile(currentDirNode, filePath, fileName);
        	
        	// File imported with success
        	if (erCode == errorCode.NO_ERROR) {
        		refreshDisplay();
        		return;   
        	}
        	
        	String msg = "The selected file could not be imported.\n";                         // I18n
        	switch (erCode) {
        	case FILE_NOT_FOUND:        	
        		msg += "The file could not be found.";                                          // I18n
        		break; 
        	case FILE_TOO_LARGE:          
        		msg += "The file is too large. The maximum size allowed is 150 KB.";            // I18n
        		break;
        	default:   
        	}    
        	
        	// Error message display
        	JOptionPane.showMessageDialog(this, msg, dialogTitle, JOptionPane.WARNING_MESSAGE);  
    	}
    }
    
    public void exportFile() {
    	//
    }
    
    public void openInApplication(DefaultMutableTreeNode node) {
    	if (FFS.isDirectory(node)) {
    		tvDirs.setSelectionPath(new TreePath(node.getPath()));
    	} else {
    		String type = FFS.getType(node);
//    		
//    		if (type.equals("image")) {
//        		GUIApplicationFileExplorerImageViewer.gI().preview(this, node);
//    		}
//    		
//    		if (type.equals("text")) {
//        		
//    		}
    	}
    }   
    

    @Override
    public void update(Observable arg0, Object arg1) {
        refreshDisplay();
    }
    
}
