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
package filius.software.system;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;

import filius.Main;
import filius.hardware.Port;
import filius.hardware.knoten.Switch;
import filius.rahmenprogramm.I18n;
import filius.software.netzzugangsschicht.SwitchPortObserver;

/**
 * Diese Klasse stellt die Funktionalitaet des Switches zur Verfuegung. Wichtiges Element ist die Source Address Table
 * (SAT). Der Switch operiert nur auf der Netzzugangsschicht, auf der MAC-Adressen verwendet werden.
 */
@SuppressWarnings("serial")
public class SwitchFirmware extends SystemSoftware implements I18n {

    /**
     * Die Source Address Table (SAT), in der die MAC-Adressen den physischen Anschluessen des Switch zugeordnet werden
     */
    private HashMap<String, Port> sat = new HashMap<String, Port>();

    /**
     * Liste der Anschlussbeobachter. Sie implementieren die Netzzugangsschicht.
     */
    private LinkedList<SwitchPortObserver> portObservers;

    
    /**
     * Hier wird die Netzzugangsschicht des Switch initialisiert und gestartet. Ausserdem wird die SAT zurueckgesetzt.
     */
    public void start() {
        super.start();
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (SwitchFirmware), starten()");

        sat = new HashMap<String, Port>();
        portObservers = new LinkedList<SwitchPortObserver>();

        for (Port port : ((Switch) getNode()).getPortList()) {
        	SwitchPortObserver portObserver = new SwitchPortObserver(this, port);
            portObserver.startThread();
            portObservers.add(portObserver);
        }
        fireSatChange(); 
    }

    /** Hier wird die Netzzugangsschicht des Switch gestoppt. */
    public void stop() {
        super.stop();
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (SwitchFirmware), beenden()");
        
        for (SwitchPortObserver portObserver : portObservers) {
            portObserver.stopThread();
        }
    }

    /** Diese Methode wird genutzt, um die SAT abzurufen. */
    public Vector<Vector<String>> getSAT() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (SwitchFirmware), getSAT()");
        
        Vector<Vector<String>> entries = new Vector<Vector<String>>();
        Vector<String> entry;
        String portString;

        for (String elem : sat.keySet()) {
            Port port = (Port) sat.get(elem);
            portString = messages.getString("sw_switchfirmware_msg1") + " " + String.valueOf(port.getIndex()+1);
            entry = new Vector<String>();
            entry.add(elem.toUpperCase());
            entry.add(portString);
            entries.add(entry);
        }

        return entries;
    }

    /**
     * Methode zum erzeugen eines neuen Eintrags in der SAT. Wenn bereits ein Eintrag zu der uebergebenen MAC-Adresse
     * vorliegt, wird der alte Eintrag aktualisiert.
     * 
     * @param macAdresse
     *            die MAC-Adresse des entfernten Anschlusses
     * @param port
     *            der Anschluss des Switch, der mit dem entfernten Anschluss verbunden ist
     */
    public void addSATEntry(String macAdresse, Port port) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + 
        		           " (SwitchFirmware), addSATEntry(" + macAdresse + "," + port + ")");
        
        sat.put(macAdresse, port);
        fireSatChange(); 
    }

    /**
     * Mit dieser Methode wird der Anschluss ausgewaehlt, der die Verbindung zum Anschuss mit der uebergebenen
     * MAC-Adresse herstellt. Dazu wird die SAT verwendet.
     * 
     * @param macAdresse
     *            die Zieladresse eines Frames nach der in der SAT gesucht werden soll
     * @return der Anschluss zur MAC oder null, wenn kein passender Eintrag existiert
     */
    public Port getPortByMAC(String macAdresse) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() +
                           " (SwitchFirmware), getPortByMAC(" + macAdresse + ")");
        
        if (sat.containsKey(macAdresse)) {
            return sat.get(macAdresse);
        } else {
            return null;
        }
    }
    
  //------------------------------------------------------------------------------------------------
    // Listeners management
    //------------------------------------------------------------------------------------------------ 

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);     
    
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }
    
    // Notify the listeners that the SAT content has changed
    // Fired by: SwitchFirmware
    // Listened to by: SatViewer
    protected void fireSatChange() {
        pcs.firePropertyChange("satentry", null, null);
    }
}
