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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

@SuppressWarnings("serial")
public class JExtendedTable extends JTable implements MouseListener {
	
	private DefaultTableModel tableModel;
	private TableColumnModel columnModel;
	private JTableHeader header;
	private AbstractAction doubleClickAction = null;
	private MouseAdapter popupMenuAdapter = null;
	private AbstractAction selectionChangeAction = null;
	private Color evenColor;
	private Color oddColor;	
	private boolean selectedCellHasBorder = false;
	private int cellAlignment = JLabel.LEFT;
	private int columnCellAlignment = JLabel.LEFT;
	private int horizontalSeparator = -1;
	
	
	public JExtendedTable(int columnCount) {			
		
		super(new DefaultTableModel(0, columnCount));
		tableModel = (DefaultTableModel) getModel();
		columnModel = getColumnModel();
		header = getTableHeader();
	
		getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//hideSelectedCellBorder();
		updateCellRenderer();
		
		addMouseListener(this);
			
		setRowHeight(20);			
		header.setReorderingAllowed(false);    			
	}
	
	public void setHeader(int columnIndex, String text) {

		columnModel.getColumn(columnIndex).setHeaderValue(text);
	}	
	
	// Sort rows based on the first column
	public void setSorted(boolean sorted) {

		setAutoCreateRowSorter(sorted);
		getRowSorter().toggleSortOrder(0);
	}	
	
	public void sort() {

		getRowSorter().toggleSortOrder(0);
		getRowSorter().toggleSortOrder(0);
	}	
		
	public void clear() {

		tableModel.setRowCount(0);
	}
	
	public void addRowIfNotPresent(Vector<String> row) {
		
		if (tableModel.getDataVector() == null) return;
		if (!tableModel.getDataVector().contains(row)) tableModel.addRow(row);
	}	
	
	public void addRow(String text1, String text2) {

		tableModel.addRow(new Object[] { text1, text2 });
	}
	
	public void addRow(String text1, String text2, String text3, String text4) {

		tableModel.addRow(new Object[] { text1, text2, text3, text4 });
	}
	
	public void addRow(String text1, String text2, String text3, String text4, String text5) {

		tableModel.addRow(new Object[] { text1, text2, text3, text4, text5 });
	}	
	
	public void addRow(String text1, String text2, String text3, String text4, String text5, String text6, String text7, String text8) {

		tableModel.addRow(new Object[] { text1, text2, text3, text4, text5, text6, text7, text8 });
	}	
	
    public void updateRow(int rowIndex, String text1, String text2) {
    	
    	tableModel.setValueAt(text1, convertRowIndexToModel(rowIndex), convertColumnIndexToModel(0));
    	tableModel.setValueAt(text2, convertRowIndexToModel(rowIndex), convertColumnIndexToModel(1));
    }
	
    public void updateRow(int rowIndex, String text1, String text2, String text3, String text4) {
    	
    	tableModel.setValueAt(text1, convertRowIndexToModel(rowIndex), convertColumnIndexToModel(0));
    	tableModel.setValueAt(text2, convertRowIndexToModel(rowIndex), convertColumnIndexToModel(1));
    	tableModel.setValueAt(text3, convertRowIndexToModel(rowIndex), convertColumnIndexToModel(2));
    	tableModel.setValueAt(text4, convertRowIndexToModel(rowIndex), convertColumnIndexToModel(3));
    }
	
	public void removeRow(int rowIndex) {

		rowIndex = convertRowIndexToModel(rowIndex);
		tableModel.removeRow(rowIndex);
	}
	
	public void shiftRowUp (int rowIndex) {
		if (rowIndex <= 0) return;
		tableModel.moveRow(rowIndex, rowIndex, rowIndex-1);
	}
	
	public void shiftRowDown (int rowIndex) {
		if (rowIndex < 0 || rowIndex == getRowCount()-1) return;
		tableModel.moveRow(rowIndex, rowIndex, rowIndex+1);
	}
	
	public void moveRowTo (int oldIndex, int newIndex) {
		if (oldIndex < 0 || newIndex < 0 || oldIndex > getRowCount()-1 || newIndex > getRowCount()-1 || oldIndex == newIndex) return;
		tableModel.moveRow(oldIndex, oldIndex, newIndex);
	}
	
	public void setSelectedRow(int rowIndex) {
		setRowSelectionInterval(rowIndex, rowIndex); 
    }
	
    public String getStringAt(int row, int column) {
    	
        return (String) tableModel.getValueAt(convertRowIndexToModel(row), convertColumnIndexToModel(column));
    }
	
	public void removeSelectedRow() {

		int rowIndex = convertRowIndexToModel(getSelectedRow());
		if (rowIndex >= 0) tableModel.removeRow(rowIndex);
	}
	
	public void setHeaderResizable(boolean value) {

		header.setResizingAllowed(value);
	}	
	
	// Disable direct editon of cells
	public boolean isCellEditable(int row, int column) {
		
		return false;
	}
	
	// Update the cell renderer of the whole table if columnIndex = -1, 
    // or of the specified column if columnIndex >= 0
	private void updateCellRenderer(int columnIndex) {

		class CellRenderer extends DefaultTableCellRenderer {

			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				
				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); 
				
				if (!selectedCellHasBorder) setBorder(noFocusBorder);
				
				if (row == horizontalSeparator) setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
				
				if (columnIndex < 0) setHorizontalAlignment(cellAlignment);
				else setHorizontalAlignment(columnCellAlignment);
		
				if (!getBackground().equals(getSelectionBackground())) setBackground(row % 2 == 0 ? evenColor : oddColor);	
				
				return this;
			}
		}	
		if (columnIndex < 0) setDefaultRenderer(Object.class, new CellRenderer());
		else getColumnModel().getColumn(columnIndex).setCellRenderer(new CellRenderer());
	}
	
	// Update the cell renderer of the whole table
	private void updateCellRenderer() {

		updateCellRenderer(-1);
	}
	
	// Hide selected cell border (when only row selection is required)
//	private void setSelectedCellBorderVisible(boolean visible) {
//
//		selectedCellHasBorder = visible;
//		updateCellRenderer();		
//	}
	
	public void setRowColors(Color evenColor, Color oddColor) {
		
		this.evenColor = evenColor;
		this.oddColor = oddColor;
	}
	
	
	/** Horizontal separator is the index of a row with a thick bottom border
	 * 
	 */
	public int getHorizontalSeparator() {
		
		return horizontalSeparator;
	}
	
	/** Horizontal separator is the index of a row with a thick bottom border
	 * 
	 */
	public void setHorizontalSeparator(int value) {
		
		horizontalSeparator = value;
		updateCellRenderer();
	}
	
	// JLabel.LEFT, JLabel.CENTER, JLabel.RIGHT
	public void setColumnAlignment(int columnIndex, int alignment) {
		
		columnCellAlignment = alignment;
		updateCellRenderer(columnIndex);
	}		
	
	// Make sure that the table fills the whole container
	public boolean getScrollableTracksViewportHeight() {
		
		return getPreferredSize().height < getParent().getHeight();
	}

	public void setColumnWidth(int columnIndex, int width) {

		TableColumn col = getColumnModel().getColumn(columnIndex);		
		col.setMinWidth(width);
		col.setMaxWidth(width);
		col.setPreferredWidth(width);
	}	
	
	public void setSelectionChangeListener(AbstractAction action) {
    	
    	selectionChangeAction = action;
    	
    	getSelectionModel().addListSelectionListener(new ListSelectionListener() {

    		public void valueChanged(ListSelectionEvent evt) {     
    			if (selectionChangeAction != null) selectionChangeAction.actionPerformed(null);
    		}
    	}); 
    }
		
    public void setDoubleClickListener(AbstractAction action) {
		
		doubleClickAction = action;
    }
    
    public void setPopupMenuAdapter(MouseAdapter mouseAdapter) {
		
		popupMenuAdapter = mouseAdapter;
    }    

	@Override
	public void mouseClicked(MouseEvent e) {

		if (doubleClickAction != null && e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) doubleClickAction.actionPerformed(null);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		
		int row = rowAtPoint(e.getPoint());
		if (row >= 0) 	setSelectedRow(row);		
		else 			clearSelection();       
	}

	@Override
	public void mouseReleased(MouseEvent e) {

		if (popupMenuAdapter != null && e.isPopupTrigger()) popupMenuAdapter.mouseReleased(e); 		
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}	
}
