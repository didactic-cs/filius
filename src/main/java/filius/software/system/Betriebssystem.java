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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import filius.software.rip.RIPTable;

/**
 * Diese Klasse stellt die Funktionalitaet eines Betriebssystems fuer Hosts (d. h. Rechner und Notebooks) zur
 * Verfuegung. Spezifisch ist die Moeglichkeit, einen DHCP-Server zu installieren und die Konfiguration der
 * Netzwerkkarten mit DHCP durchzufuehren. Die weitere Funktionalitaet wird von der Oberklasse
 * (InternetKnotenBetriebssystem) zur Verfuegung gestellt.
 * 
 */
@SuppressWarnings("serial")
public class Betriebssystem extends InternetKnotenBetriebssystem {
    private static Logger LOG = LoggerFactory.getLogger(Betriebssystem.class);

    @Override
    public boolean isRipEnabled() {
        return false;
    }

    @Override
    public RIPTable getRIPTable() {
        return null;
    }

    /**
     * Starten der Threads. Der DHCP-Client wird hier gestartet, wenn die Konfiguration mit DHCP aktiviert ist. Der
     * DHCP-Server wird hier auch gestartet.
     */
    @Override
    public synchronized void starten() {
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass() + " (Betriebssystem), starten()");
        super.starten();
    }

    /**
     * Aufruf erfolgt beim Wechsel vom Aktions- zum Entwurfsmodus. Die entsprechende Methode der Oberklasse wird
     * aufgerufen und der DHCP-Server und -Client beendet.
     */
    public void beenden() {
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass() + " (Betriebssystem), beenden()");
        super.beenden();
    }
}
