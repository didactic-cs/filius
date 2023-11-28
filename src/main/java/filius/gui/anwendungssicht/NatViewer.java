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

import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;

import filius.gui.JMainFrame;
import filius.hardware.knoten.Gateway;
import filius.rahmenprogramm.I18n;
import filius.software.nat.NatGateway;
import filius.software.nat.NatMethod;
import filius.software.system.GatewayFirmware;

@SuppressWarnings("serial")
public class NatViewer extends JDialog implements I18n, PropertyChangeListener {

    private Gateway gw;
    private DefaultTableModel dtm;

    public NatViewer(Gateway gw) {
        super(JMainFrame.getJMainFrame(), messages.getString("guievents_msg29") + " " + gw.holeAnzeigeName());
        this.gw = gw;
        init();
        updateNat();
    }

    private void init() {
        getContentPane().removeAll();
    	NatMethod natMethod = ((NatGateway) ((GatewayFirmware) gw.getSystemSoftware()).holeFirewall()).getNATTable().getNatMethod();
    	if (getBounds().width==0 && getBounds().height==0 && getBounds().x==0 && getBounds().y==0) {
    		setBounds(100,100, (natMethod == NatMethod.restrictedCone? 840: 720), 240);
    	} else {
    		setBounds(getBounds().x,getBounds().y, (natMethod == NatMethod.restrictedCone? 7*getBounds().width/6: 6*getBounds().width/7), getBounds().height);
    	}

        ImageIcon icon = new ImageIcon(getClass().getResource("/gfx/hardware/router.png"));
        setIconImage(icon.getImage());

        dtm = new DefaultTableModel(0, (natMethod == NatMethod.restrictedCone? 7: 6));
        JTable tableNATNachrichten = new JTable(dtm);
        DefaultTableColumnModel dtcm = (DefaultTableColumnModel) tableNATNachrichten.getColumnModel();
        dtcm.getColumn(0).setHeaderValue(messages.getString("guievents_msg30"));
        if (natMethod == NatMethod.restrictedCone) {
        	dtcm.getColumn(1).setHeaderValue(messages.getString("guievents_msg31"));
        	dtcm.getColumn(2).setHeaderValue(messages.getString("guievents_msg32"));
        	dtcm.getColumn(3).setHeaderValue(messages.getString("guievents_msg33"));
        	dtcm.getColumn(4).setHeaderValue(messages.getString("guievents_msg34"));
        	dtcm.getColumn(5).setHeaderValue(messages.getString("guievents_msg35"));
        	dtcm.getColumn(6).setHeaderValue(messages.getString("guievents_msg27"));
        } else {
        	dtcm.getColumn(1).setHeaderValue(messages.getString("guievents_msg32"));
        	dtcm.getColumn(2).setHeaderValue(messages.getString("guievents_msg33"));
        	dtcm.getColumn(3).setHeaderValue(messages.getString("guievents_msg34"));
        	dtcm.getColumn(4).setHeaderValue(messages.getString("guievents_msg35"));
        	dtcm.getColumn(5).setHeaderValue(messages.getString("guievents_msg27"));
        }
        
        JPopupMenu menu = new JPopupMenu();

        JMenuItem resetMenuItem = new JMenuItem(messages.getString("guievents_msg36"));
        resetMenuItem.addMouseListener(new MouseInputAdapter() {
            public void mousePressed(MouseEvent e) {
                menu.setVisible(false);
                ((GatewayFirmware) gw.getSystemSoftware()).loescheNAT();
            }
        });
        menu.add(resetMenuItem);
        
        tableNATNachrichten.setComponentPopupMenu(menu);
        JScrollPane spNAT = new JScrollPane(tableNATNachrichten);
        getContentPane().add(spNAT);

    }

    public Gateway getGateway() {
        return gw;
    }

    private void updateNat() {
        dtm.setRowCount(0);
        for (Vector<String> zeile : ((GatewayFirmware) gw.getSystemSoftware()).holeNAT()) {
            dtm.addRow(zeile);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
    	if (evt.getPropertyName() == "nat_entry") {
    		updateNat();
    	} else if (evt.getPropertyName() == "nat_method") {
    		init();
    		updateNat();
    	}
    }
}
