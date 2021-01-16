package filius.gui;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
public class ComboBoxTableCellEditor extends AbstractCellEditor implements TableCellEditor {
    private JComboBox cmbBox;

    /** create a new ComboBox with values provided as parameter (array of Strings) */
    public ComboBoxTableCellEditor(String[] values) {
        cmbBox = new JComboBox(values);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int rowIndex,
            int colIndex) {
        cmbBox.setSelectedItem(value);
        TableModel model = table.getModel();
        model.setValueAt(value, rowIndex, colIndex);
        return cmbBox;
    }

    @Override
    public Object getCellEditorValue() {
        return cmbBox.getSelectedItem();
    }
}