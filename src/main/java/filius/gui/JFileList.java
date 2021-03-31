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

import java.awt.Color;
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.border.Border;
import filius.rahmenprogramm.I18n;
import filius.software.system.FiliusFileNode;

@SuppressWarnings("serial")
public class JFileList extends JList<FiliusFileNode> implements I18n {
	
	private ImageIcon closedDirIcon   = new ImageIcon(getClass().getResource("/gfx/desktop/explorer_closedDirectory.png"));
    private ImageIcon genericFileIcon = new ImageIcon(getClass().getResource("/gfx/desktop/explorer_genericFile.png"));
    private ImageIcon textFileIcon    = new ImageIcon(getClass().getResource("/gfx/desktop/explorer_textFile.png"));
    private ImageIcon htmlFileIcon    = new ImageIcon(getClass().getResource("/gfx/desktop/explorer_htmlFile.png"));
    private ImageIcon cssFileIcon     = new ImageIcon(getClass().getResource("/gfx/desktop/explorer_cssFile.png"));
    private ImageIcon xmlFileIcon     = new ImageIcon(getClass().getResource("/gfx/desktop/explorer_xmlFile.png"));
    private ImageIcon imageFileIcon   = new ImageIcon(getClass().getResource("/gfx/desktop/explorer_imageFile.png"));
    
    private DefaultListModel<FiliusFileNode> listModel;
    
    
    public JFileList() {  
    	
    	listModel = new DefaultListModel<FiliusFileNode>();
		setModel(listModel);
		setCellRenderer(new FileListRenderer());
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);		
    	setFixedCellHeight(16);
    }   
    
    public int getCount() {
    	
    	return listModel.getSize();
    }
    
    public FiliusFileNode getElementAt(int index) {
    	
    	return listModel.getElementAt(index);
    }
    
    public void clear() {
    	
    	listModel.clear();
    }
    
    public void addElement(FiliusFileNode node) {
    	
    	listModel.addElement(node);
    }
	
	private class FileListRenderer extends DefaultListCellRenderer {

    	@Override
    	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean hasFocus) {

    		FiliusFileNode node = (FiliusFileNode) value;
    		
    		if (node.isFile()) {
    			int fileSize = (int) node.getSize();
    			String size;
    			if (fileSize < 1024) size = String.valueOf(fileSize) + " " + messages.getString("fileexplorer_msg33");
    			else {
    				fileSize = fileSize / 1024;
    				size = String.valueOf(fileSize) + " " + messages.getString("fileexplorer_msg34");
    			}
    			setText(node.getName() + "  (" + size + ")");
    		} else {
    			setText(node.getName());
    		}

    		switch (node.getType()) {
    			case DIRECTORY: setIcon(closedDirIcon); break;
    			case TEXT:      setIcon(textFileIcon); break;
    			case CSS:       setIcon(cssFileIcon); break;
    			case XML:       setIcon(xmlFileIcon); break; 
    			case HTML:      setIcon(htmlFileIcon); break; 
    			case IMAGE_JPG: setIcon(imageFileIcon); break;
    			case IMAGE_PNG: setIcon(imageFileIcon); break;
    			case IMAGE_GIF: setIcon(imageFileIcon); break;
    			case IMAGE_BMP: setIcon(imageFileIcon); break;    			
    			default:        setIcon(genericFileIcon); 
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
	
//    private class lvFilesTransferHandler extends TransferHandler {
//    	
//        public int getSourceActions(JComponent c) {
//            return TransferHandler.COPY_OR_MOVE;
//        }
//     
//        public Transferable createTransferable(JComponent c) {
////            return new StringSelection(lvFiles.getSelectedValue());
//            return null;
//        }
//        
//        public boolean canImport(TransferHandler.TransferSupport supp) {
//            if (!supp.isDataFlavorSupported(DataFlavor.stringFlavor)) {
//                return false;
//            }
//            return true;
//        }
//        
//        public boolean importData(TransferHandler.TransferSupport supp) {
//            // Fetch the Transferable and its data
//            Transferable t = supp.getTransferable();
//            String data = "";
//            try {
//                data = (String)t.getTransferData(DataFlavor.stringFlavor);
//            } catch (Exception e){
//                System.out.println(e.getMessage());
//                return false;
//            }
//
//            // Fetch the drop location
//            JList.DropLocation loc = lvFiles.getDropLocation();
//            int row = loc.getIndex();
//            model.add(row, data);
//            target.validate();
//            return true;
//        }
//    }

}
