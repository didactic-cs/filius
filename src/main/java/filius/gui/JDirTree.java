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
package filius.gui;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import filius.software.system.FiliusFileNode;

/**
 * A JTree used for the display of directories 
 */
@SuppressWarnings("serial")
public class JDirTree extends JTree {
	
    private ImageIcon rootIcon        = new ImageIcon(getClass().getResource("/gfx/desktop/explorer_drive.png"));
    private ImageIcon closedDirIcon   = new ImageIcon(getClass().getResource("/gfx/desktop/explorer_closedDirectory.png"));
    private ImageIcon openDirIcon     = new ImageIcon(getClass().getResource("/gfx/desktop/explorer_openDirectory.png"));
	
    private DirTreeModel treeModel;
	
	public JDirTree(TreeNode root) {
		super(root);
		
		treeModel = new DirTreeModel(root);
		setModel(treeModel);
		setCellRenderer(new DirTreeRenderer());
		setSelectionPath(new TreePath(((DefaultMutableTreeNode) root).getPath()));
		addTreeWillExpandListener(new DirTreeWillExpandListener());
	}
	
    private class DirTreeModel extends DefaultTreeModel {
		
	    public DirTreeModel(TreeNode root) {
	       super(root, false);
	    }

		@Override
        public boolean isLeaf(Object node) {

			return (((FiliusFileNode)node).getSubdirectoryCount() == 0);
        }

		@Override
		public int getChildCount(Object parent) {
			
			// Only count the subnodes that are subdirectories	 
			// This relies on the fact that the FiliusFileSystem stores the
			// subdirectories prior to the files for a given directory node.
			return ((FiliusFileNode)parent).getSubdirectoryCount();
	    }
    }

    // Tree renderer
    private class DirTreeRenderer extends DefaultTreeCellRenderer {

		@Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            
            if (expanded) setIcon(openDirIcon);
            else	      setIcon(closedDirIcon);
 
            if (((FiliusFileNode) value).isRoot()) setIcon(rootIcon);

            return this;
        }
    }

    // Tree node pre-expansion/collapse event listener
    public class DirTreeWillExpandListener implements TreeWillExpandListener {
    
        public void treeWillCollapse(TreeExpansionEvent e) throws ExpandVetoException {
            
        	// Prevent the root node from collapsing
            if (e.getPath().getPathCount() == 1) throw new ExpandVetoException(e);
        }
        
        public void treeWillExpand(TreeExpansionEvent e) throws ExpandVetoException {}
    }
    
    public FiliusFileNode getSelectedNode() {
    	
    	return (FiliusFileNode) getLastSelectedPathComponent();
    }
}