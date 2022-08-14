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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import filius.gui.JExtendedTable;
import filius.gui.JFrameList;
import filius.gui.JMainFrame;
import filius.gui.Palette;
import filius.hardware.knoten.Switch;
import filius.rahmenprogramm.I18n;
import filius.software.system.SwitchFirmware;

@SuppressWarnings("serial")
public class SatViewer extends JFrame implements I18n, PropertyChangeListener {
	
    private Switch sw;
    private JExtendedTable table;
    

    public SatViewer(Switch sw) {
    	JFrameList.getInstance().add(this);   
    	this.sw = sw;
        updateTitle();             
        init();
        updateSat();
        
        sw.addPropertyChangeListener("nodeName", this);
        ((SwitchFirmware)sw.getSystemSoftware()).addPropertyChangeListener("satEntry", this);     }

    private void init() {
        setBounds(0, 0, 240, 300);
        setResizable(false);
        centerViewer();

        ImageIcon icon = new ImageIcon(getClass().getResource("/gfx/hardware/switch.png"));
        setIconImage(icon.getImage());

        table = new JExtendedTable(2);
        table.setHeaderResizable(false);   
        table.setSorted(true);         
        table.setBackground(Palette.SAT_TABLE_EVEN_ROW_BG);
        table.setRowColors(Palette.SAT_TABLE_EVEN_ROW_BG, Palette.SAT_TABLE_ODD_ROW_BG);
    	
    	// MAC        
        table.setHeader(0, messages.getString("guievents_msg9"));
        table.setColumnWidth(0, 130);
        
        // Port
        table.setHeader(1, messages.getString("guievents_msg10"));
                
        JScrollPane spSAT = new JScrollPane(table);
        getContentPane().add(spSAT);
    }
    
    private void centerViewer() {
    	JMainFrame MF = JMainFrame.getInstance();
    	setLocation(MF.getX() + (MF.getWidth() - getWidth())/2, MF.getY() + (MF.getHeight() - getHeight())/2);
    }

    public Switch getSwitch() {
        return sw;
    }

    private void updateSat() {  	    	

    	Vector<Vector<String>> satEntries = ((SwitchFirmware) sw.getSystemSoftware()).getSAT();    	
    	int satSize = satEntries.size();
    	
    	// When sat is empty, just flush the table
    	if (satSize == 0) {
    		table.clear();
    		return;
    	}
    	
    	// Since no single entry can be removed from the sat, we only add the new values     
    	for (Vector<String> row : satEntries) { 
    		table.addRowIfNotPresent(row);
    	}        
    }
    
    private void updateTitle() {
    	
    	setTitle(messages.getString("guievents_msg8") + " " + sw.getDisplayName());
	}       
    
    /**
     * <b>propertyChange</b> whenever a change in the host must be reflected by the user interface. 
     * 
     */
	public void propertyChange(PropertyChangeEvent evt) {
		
		String pn = evt.getPropertyName();
		
		if (pn == "satentry") {    			
			// Update the SAT table
			// From SwitchFirmware
			updateSat();
			
		} else if (pn == "nodename") {     			
			// Update the title of the desktop window
			// From Node
			updateTitle();
		} 
	};
}
