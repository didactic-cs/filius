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

import java.beans.PropertyChangeEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import filius.hardware.Port;
import filius.hardware.knoten.Switch;
import filius.rahmenprogramm.I18n;
import filius.software.netzzugangsschicht.EthernetFrame;
import filius.software.netzzugangsschicht.SwitchPortBeobachter;

/** Diese Klasse erweitert den Wert der HashMap für die SAT um einen Eintrag für das letzte Update.
 */
class satEntry {
	
	private Port port;
	private Date letztes_Update;
	
	public Port getPort(){
		return port;
	}
	
	public Date holeLetztesUpdate() {
		return letztes_Update;
	}
	
	public void setPort(Port port) {
		this.port = port;
	}
	
	public void hinzuLetztesUpdate(Date letztes_Update) {
		this.letztes_Update = letztes_Update;
	}
}


/**
 * Diese Klasse stellt die Funktionalitaet des Switches zur Verfuegung. Wichtiges Element ist die Source Address Table
 * (SAT). Der Switch operiert nur auf der Netzzugangsschicht, auf der MAC-Adressen verwendet werden.
 */
public class SwitchFirmware extends SystemSoftware implements I18n {
    private static Logger LOG = LoggerFactory.getLogger(SwitchFirmware.class);

    private static final long serialVersionUID = 1L;

    /**
     * Die Source Address Tabel (SAT), in der die MAC-Adressen den physischen Anschluessen des Switch zugeordnet werden
     */
    private ConcurrentHashMap<String, satEntry> sat = new ConcurrentHashMap<String, satEntry>();

    /**
     * Liste der Anschlussbeobachter. Sie implementieren die Netzzugangsschicht.
     */
    private LinkedList<SwitchPortBeobachter> switchBeobachter;

    /**
     * Hier werden bereits weitergeleitete Frames gespeichert. Wird ein Frame wiederholt verschickt, beispielsweise
     * wegen einer Verbindung, die zwei Anschluesse kurzschliesst, wird der Frame verworfen.
     * 
     * @see filius.software.netzzugangsschicht.SwitchPortBeobachter
     */
    private LinkedList<EthernetFrame> durchgelaufeneFrames = new LinkedList<EthernetFrame>();

    private String ssid = UUID.randomUUID().toString().substring(0, 6);

    private long retentionTime = 300000;

    public long getRetentionTime() {
        return retentionTime;
    }

    public void setRetentionTime(long retentionTime) {
        this.retentionTime = retentionTime;
    }

    /**
     * Hier wird die Netzzugangsschicht des Switch initialisiert und gestartet. Ausserdem wird die SAT zurueckgesetzt.
     */
    public void starten() {
        super.starten();
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass() + " (SwitchFirmware), starten()");
        SwitchPortBeobachter anschlussBeobachter;

        sat = new ConcurrentHashMap<String, satEntry>();
        switchBeobachter = new LinkedList<SwitchPortBeobachter>();

        for (Port anschluss : ((Switch) getKnoten()).getAnschluesse()) {
            anschlussBeobachter = new SwitchPortBeobachter(this, anschluss);
            anschlussBeobachter.starten();
            switchBeobachter.add(anschlussBeobachter);
        }
        firePropertyChanged(new PropertyChangeEvent(this, "sat_entry", null, null));
        
        Timer timer = new Timer();
       
        timer.scheduleAtFixedRate(new TimerTask(){
        	@Override
        	public void run() {
        		if(!isStarted()) {
        			timer.cancel();
        		} else if (!sat.isEmpty()) {
        			checkSAT();	
        		}
        	}
        }, 1000, 1000);
    }

    /** Hier wird die Netzzugangsschicht des Switch gestoppt. */
    public void beenden() {
        super.beenden();
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass() + " (SwitchFirmware), beenden()");
        for (SwitchPortBeobachter anschlussBeobachter : switchBeobachter) {
            anschlussBeobachter.beenden();
        }
    }

    /** Diese Methode wird genutzt, um die SAT abzurufen. */
    public Vector<Vector<String>> holeSAT() {
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass() + " (SwitchFirmware), holeSAT()");
        Vector<Vector<String>> eintraege = new Vector<Vector<String>>();
        Vector<String> eintrag;
        String ausgabe;
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");

        for (String elem : sat.keySet()) {
            Port anschluss = (Port) sat.get(elem).getPort();
            ausgabe = messages.getString("sw_switchfirmware_msg1") + " "
                    + (((Switch) getKnoten()).getAnschluesse().indexOf(anschluss) + 1);
            eintrag = new Vector<String>();
            eintrag.add(elem.toUpperCase());
            eintrag.add(ausgabe);
            eintrag.add(formatter.format(sat.get(elem).holeLetztesUpdate()));
            eintraege.add(eintrag);
        }

        return eintraege;
    }

    /**
     * Methode zum erzeugen eines neuen Eintrags in der SAT. Wenn bereits ein Eintrag zu der uebergebenen MAC-Adresse
     * vorliegt, wird der alte Eintrag aktualisiert.
     * 
     * @param macAdresse
     *            die MAC-Adresse des entfernten Anschlusses
     * @param anschluss
     *            der Anschluss des Switch, der mit dem entfernten Anschluss verbunden ist
     */
    public void hinzuSatEintrag(String macAdresse, Port anschluss, Date letztes_Update) {
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass() + " (SwitchFirmware), hinzuSatEintrag(" + macAdresse
                + "," + anschluss + "," + letztes_Update + ")");
        satEntry eintrag = new satEntry();	
        eintrag.setPort(anschluss);
        eintrag.hinzuLetztesUpdate(letztes_Update);	
        sat.put(macAdresse, eintrag);
        firePropertyChanged(new PropertyChangeEvent(this, "sat_entry", null, anschluss));
    }

    /**
     * Mit dieser Methode wird der Anschluss ausgewaehlt, der die Verbindung zum Anschuss mit der uebergebenen
     * MAC-Adresse herstellt. Dazu wird die SAT verwendet.
     * 
     * @param macAdresse
     *            die Zieladresse eines Frames nach der in der SAT gesucht werden soll
     * @return der Anschluss zur MAC oder null, wenn kein passender Eintrag existiert
     */
    public Port holeAnschlussFuerMAC(String macAdresse) {
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass() + " (SwitchFirmware), holeAnschlussFuerMAC("
                + macAdresse + ")");
        if (sat.containsKey(macAdresse)) {
            return (Port) sat.get(macAdresse).getPort();
        } else {
            return null;
        }
    }

    /**
     * Methode zum Zugriff auf die bereits durchgelaufenen Frames. Diese wird dazu genutzt um Fehler durch Zyklen zu
     * vermeiden.
     * 
     * @return Liste der bereits weitergeleiteten Frames.
     */
    public LinkedList<EthernetFrame> holeDurchgelaufeneFrames() {
        return durchgelaufeneFrames;
    }

    public void setSSID(String ssid) {
        this.ssid = ssid;
    }

    public String getSSID() {
        return ssid;
    }
    
    /**
     * Methode zum Löschen der gesamten SAT
     */
    public void loescheSAT() {
    	sat.clear();
        firePropertyChanged(new PropertyChangeEvent(this, "sat_entry", null, null));
    }
    
    /**
     * Methode zum Überprüfen der SAT
     */
    public void checkSAT() {
    	Date jetzt = new Date();
    	sat.forEach((mac,eintrag) -> {
    		if (jetzt.getTime()-eintrag.holeLetztesUpdate().getTime() >= getRetentionTime()) {
    			sat.remove(mac);
    			firePropertyChanged(new PropertyChangeEvent(this, "sat_entry", null, null));
    		};
    	});
    }
}
