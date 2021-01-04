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
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public class JExtendedTable extends JTable {

    public class ColorTableCellRenderer implements TableCellRenderer {
        private HashMap<Integer, Color> cellColor = new HashMap<Integer, Color>();

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            JLabel label = new JLabel(value != null ? value.toString() : "");
            int key = ((row + 1) * 1000) + column;
            label.setOpaque(true);
            if (isSelected) {
                label.setBackground(Color.CYAN);
            } else {
                if (cellColor.containsKey(key)) {
                    label.setBackground(cellColor.get(key));
                } else {
                    label.setBackground(Color.WHITE);
                }
            }
            return label;
        }

        public void setColor(int row, int column, Color color) {
            int key = ((row + 1) * 1000) + column;
            cellColor.put(key, color);
        }

        public void resetColor(int row, int column) {
            int key = ((row + 1) * 1000) + column;
            cellColor.remove(key);
        }

        public void resetColors() {
            cellColor.clear();
        }
    }

    private static final long serialVersionUID = 1L;
    private boolean editable;
    protected Object parentGUI;

    public JExtendedTable(TableModel model, boolean editable) {
        super(model);
        setEditable(editable);
        for (int i = 0; i < getColumnCount(); i++) {
            this.getColumnModel().getColumn(i).setCellRenderer(new ColorTableCellRenderer());
        }
    }

    public void setParentGUI(Object parent) {
        this.parentGUI = parent;
    }

    /**
     * @return the editable
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * @param editable
     *            the editable to set
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean isCellEditable(int row, int column) {
        return editable;
    }
}
