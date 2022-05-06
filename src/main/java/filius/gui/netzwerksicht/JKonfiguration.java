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
package filius.gui.netzwerksicht;

import java.util.HashMap;
import java.util.regex.Pattern;

import javax.swing.JTextField;

import filius.gui.ControlPanel;
import filius.hardware.Hardware;
import filius.hardware.knoten.Gateway;
import filius.hardware.knoten.Host;
import filius.hardware.knoten.Modem;
import filius.hardware.knoten.Switch;
import filius.hardware.knoten.Vermittlungsrechner;
import filius.rahmenprogramm.EingabenUeberpruefung;

@SuppressWarnings({ "deprecation", "serial" })
public class JKonfiguration extends ControlPanel {
    private Hardware hardware;

    protected static HashMap<Hardware, JKonfiguration> instances = new HashMap<Hardware, JKonfiguration>();

    protected JKonfiguration(Hardware hardware) {
        super();
        this.hardware = hardware;
        if (hardware != null) {
            hardware.addObserver(this);
            reInit();
            updateAttribute();
        }
        minimieren();
    }

    public static JKonfiguration getInstance(Hardware hardware) {
        if (hardware == null) {
            return new JKonfiguration(null);
        }

        JKonfiguration newInstance;
        if (!instances.containsKey(hardware)) {
            if (hardware instanceof Host) {
                newInstance = new JHostKonfiguration(hardware);
            } else if (hardware instanceof Modem) {
                newInstance = new JModemKonfiguration(hardware);
            } else if (hardware instanceof Switch) {
                newInstance = new JSwitchKonfiguration(hardware);
            } else if (hardware instanceof Vermittlungsrechner) {
                newInstance = new JVermittlungsrechnerKonfiguration(hardware);
            } else if (hardware instanceof Gateway) {
                newInstance = new JGatewayConfiguration(hardware);
            } else {
                newInstance = new JKonfiguration(null);
            }
            instances.put(hardware, newInstance);
        }
        return instances.get(hardware);
    }

    public Hardware holeHardware() {
        return hardware;
    }

    /**
     * Funktion die waehrend der Eingabe ueberprueft ob die bisherige Eingabe einen korrekten Wert darstellt.
     * 
     * @author Johannes Bade & Thomas Gerding
     * @param pruefRegel
     * @param feld
     */
    public boolean ueberpruefen(Pattern pruefRegel, JTextField feld) {
        if (EingabenUeberpruefung.isGueltig(feld.getText(), pruefRegel)) {
            feld.setForeground(EingabenUeberpruefung.farbeRichtig);
            return true;
        } else {
            feld.setForeground(EingabenUeberpruefung.farbeFalsch);
            return false;
        }
    }
}
