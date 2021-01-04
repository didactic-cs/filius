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

import static filius.software.dns.ResourceRecord.ADDRESS;
import static filius.software.dns.ResourceRecord.MAIL_EXCHANGE;
import static filius.software.dns.ResourceRecord.NAME_SERVER;

import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

import filius.gui.JExtendedTable;
import filius.rahmenprogramm.EingabenUeberpruefung;
import filius.software.dns.DNSServer;

@SuppressWarnings("serial")
public class DNSConfigTable extends JExtendedTable {

    private String typeID = null;

    public DNSConfigTable(TableModel model, boolean editable, String type) {
        super(model, editable);
        this.typeID = type;
    }

    public void editingStopped(ChangeEvent e) {
        TableCellEditor editor = getCellEditor();
        if (editor != null) {
            String value = (String) editor.getCellEditorValue();
            value = value.trim();
            DNSServer dnsServer = (DNSServer) ((GUIApplicationDNSServerWindow) parentGUI).holeAnwendung();
            boolean validChange;
            if (typeID != null && typeID.equals("A")) {
                if (editingColumn == 0) {
                    validChange = EingabenUeberpruefung.isGueltig(value, EingabenUeberpruefung.musterDomain);
                    if (validChange) {
                        dnsServer.changeSingleEntry(editingRow, 0, ADDRESS, value);
                    }
                } else {
                    validChange = EingabenUeberpruefung.isGueltig(value, EingabenUeberpruefung.musterIpAdresse);
                    if (validChange) {
                        dnsServer.changeSingleEntry(editingRow, 3, ADDRESS, value);
                    }
                }
            } else if (typeID != null && typeID.equals("MX")) {
                if (editingColumn == 0) {
                    validChange = EingabenUeberpruefung.isGueltig(value, EingabenUeberpruefung.musterDomain);
                    if (validChange) {
                        dnsServer.changeSingleEntry(editingRow, 0, MAIL_EXCHANGE, value);
                    }
                } else {
                    validChange = EingabenUeberpruefung.isGueltig(value, EingabenUeberpruefung.musterDomain);
                    if (validChange) {
                        dnsServer.changeSingleEntry(editingRow, 3, MAIL_EXCHANGE, value);
                    }
                }
            } else if (typeID != null && typeID.equals("NS")) {
                if (editingColumn == 0) {
                    validChange = EingabenUeberpruefung.isGueltig(value, EingabenUeberpruefung.musterDomain);
                    if (validChange) {
                        dnsServer.changeSingleEntry(editingRow, 0, NAME_SERVER, value);
                    }
                } else {
                    validChange = EingabenUeberpruefung.isGueltig(value, EingabenUeberpruefung.musterDomain);
                    if (validChange) {
                        dnsServer.changeSingleEntry(editingRow, 3, NAME_SERVER, value);
                    }
                }
            }
            removeEditor();
        }
    }

    public String getType() {
        return this.typeID;
    }
}
