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
package filius.hardware;

import java.io.Serializable;
import java.util.Observable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "serial", "deprecation" })
public abstract class Hardware extends Observable implements Serializable {
    private static Logger LOG = LoggerFactory.getLogger(Hardware.class);

    public static Boolean ACTIVE = Boolean.TRUE;
    public static Boolean INACTIVE = Boolean.FALSE;
    public static String FAILURE = "Failure";

    private Object state = Boolean.FALSE;

    public String holeHardwareTyp() {
        return "";
    }

    public void setAktiv(boolean aktiv) {
        setState(aktiv);
    }

    public void setFailure() {
        setState(FAILURE);
    }

    private void setState(Object newState) {
        LOG.trace("new hardware state: {}", newState);
        if (!state.equals(newState)) {
            state = newState;
            benachrichtigeBeobachter();
        }
    }

    public void benachrichtigeBeobachter() {
        setChanged();
        notifyObservers(state);
    }
}
