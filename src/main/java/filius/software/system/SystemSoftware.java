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
import java.io.Serializable;
import filius.hardware.knoten.Node;
import filius.software.system.ModemFirmware.ModemStatus;

/**
 * Die Klasse SystemSoftware umfasst die grundlegenden Funktionen einer Systemsoftware, die auf allen Stationen
 * (Endsysteme und Uebertragungseinheiten) zur Verfuegung stehen muss. <br />
 * Die Klasse ist abstrakt und wird fuer die verschiedenen Stationen unterschiedlich implementiert.
 */
@SuppressWarnings("serial")
public abstract class SystemSoftware implements Serializable {

    /** Die Hardware, auf der diese Systemsoftware laeuft. */
    private Node node;

    private boolean started;

    /**
     * Diese Methode wird beim Wechsel vom Konfigurationsmodus (zum Aufbau des Rechnernetzes und Konfiguration der
     * Komponenten) zum Aktionsmodus (mit der Moeglichkeit den Datenaustausch zu simulieren) ausgefuehrt! <br />
     * In den implementierenden Unterklassen sollen an dieser Stelle alle Threads zur Simulation des virtuellen
     * Netzwerks gestartet werden.
     */
    public void start() {
        started = true;
    }

    /**
     * Diese Methode wird beim Wechsel vom Aktionsmodus (mit der Moeglichkeit den Datenaustausch zu simulieren) zum
     * Konfigurationsmodus (zum Aufbau des Rechnernetzes und Konfiguration der Komponenten) ausgefuehrt! <br />
     * In den implementierenden Unterklassen sollen an dieser Stelle alle Threads zur Simulation des virtuellen
     * Netzwerks angehalten werden.
     */
    public void stop() {
        started = false;
    }

    public boolean isStarted() {
        return started;
    }
    
    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
        //Main.debug.println("DEBUG: SystemSoftware ("+this.hashCode()+") is now connected to Node ("+node.hashCode()+")");
    }    
     
    //------------------------------------------------------------------------------------------------
    // Listeners management
    //------------------------------------------------------------------------------------------------ 

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);     
    
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }
    
    // Notify the listeners that the IP address changed
    // Fired by: InternetNodeOS
    // Listened to by: JNodeLabel, GUIDesktopPanel
    protected void fireIPChange(String name) {
        pcs.firePropertyChange("ipaddress", null, name);
    }
    
    // Notify the listeners that the DNS address changed
    // Fired by: InternetNodeOS
    // Listened to by: JNodeLabel
    protected void fireDNSChange(String name) {
        pcs.firePropertyChange("dnsaddress", null, name);
    }
    
    // Notify the listeners that the color dot needs to be updated
    // Fired by: ModemFirmware
    // Listened to by: JNodeLabel
    protected void fireModemStatusChange(ModemStatus modemStatus) {
        pcs.firePropertyChange("modemstatus", null, modemStatus);
    }
    
    // Notify the listeners that a message must be displayed
    // Fired by: ModemFirmware, IP
    // Listened to by: JNodeLabel
    public void fireDisplayMessage(String message) {
    	pcs.firePropertyChange("message", null, message);    	
    }  
}
