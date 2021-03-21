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
package filius.gui.nachrichtensicht;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import filius.gui.Palette;
import filius.rahmenprogramm.nachrichten.PacketAnalyzer;

@SuppressWarnings("serial")
public class PacketsAnalyzerTableCellRenderer extends DefaultTableCellRenderer {

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		
		String layerString;
		int layer = 5;
		int tblCols = table.getColumnCount();
		int tblRows = table.getRowCount();

		if (tblRows > row && tblCols > OldPacketsAnalyzerTable.SCHICHT_SPALTE && table.getValueAt(row, OldPacketsAnalyzerTable.SCHICHT_SPALTE) != null)
			layerString = table.getValueAt(row, OldPacketsAnalyzerTable.SCHICHT_SPALTE).toString();
		else
			layerString = "";

		for (int i = 0; i < PacketAnalyzer.PROTOKOLL_SCHICHTEN.length; i++) {
			if (layerString.equals(PacketAnalyzer.PROTOKOLL_SCHICHTEN[i]))
				layer = i;
		}

		// Row colors selection
		if (isSelected) setForeground(Palette.PACKETS_ANALYZER_SELECTED_FG);
		else 			setForeground(Palette.PACKETS_ANALYZER_FG);		

		switch (layer) {
		case 0:
			if (isSelected) setBackground(Palette.PACKETS_ANALYZER_LAYER0_SELECTED_BG);
			else 			setBackground(Palette.PACKETS_ANALYZER_LAYER0_BG);		
			break;
		case 1:
			if (isSelected) setBackground(Palette.PACKETS_ANALYZER_LAYER1_SELECTED_BG);
			else 			setBackground(Palette.PACKETS_ANALYZER_LAYER1_BG);		
			break;
		case 2:
			if (isSelected) setBackground(Palette.PACKETS_ANALYZER_LAYER2_SELECTED_BG);
			else 			setBackground(Palette.PACKETS_ANALYZER_LAYER2_BG);		
			break;
		case 3:
			if (isSelected) setBackground(Palette.PACKETS_ANALYZER_LAYER3_SELECTED_BG);
			else 			setBackground(Palette.PACKETS_ANALYZER_LAYER3_BG);		
			break;
		default:
			if (isSelected) setBackground(Palette.PACKETS_ANALYZER_LAYERD_SELECTED_BG);
			else 			setBackground(Palette.PACKETS_ANALYZER_LAYERD_BG);		
		}

		// Font selection
		switch (column) {
		case 0:
			setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
			break;
		case 1:
			setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
			break;
		case 2:
		case 3:
			setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
			break;
		case 4:
			setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
			break;
		case 5:
			setFont(new Font(Font.DIALOG, Font.BOLD + Font.ITALIC, 12));
			break;
		case 6:
			setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
			break;
		default:
			setFont(new Font(Font.DIALOG, Font.ITALIC, 12));
		}
		
		if (column == 0) setHorizontalAlignment(JLabel.RIGHT);
		else             setHorizontalAlignment(JLabel.LEFT);

		if (value != null)
			setText(value.toString().replace('\n', ' '));
		else
			setText("");

		return this;
	}
}
