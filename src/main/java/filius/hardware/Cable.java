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

import filius.Main;
import filius.exception.ConnectionException;
import filius.rahmenprogramm.I18n;

/**
 * @author carsten
 * 
 */
@SuppressWarnings("serial")
public class Cable extends Hardware implements Serializable, I18n {
    
    public static final String TYPE = messages.getString("hw_kabel_msg1");

    /**
     * Verzoegerung der Uebertragung in Millisekunden, wenn der Verzoegerungsfaktor 1 ist.
     */
    private static final int MIN_DELAY = 50;

    /** Faktor der Verzoegerungszeit, der zwischen 1 und 100 */
    private static int delayFactor = 1;

    /**
     * maximale Anzahl von Hops zum Datenaustausch. Diese Zahl wird verwendet, um eine Round-Trip-Time (RTT) zu
     * berechnen. Da es auch möglich ist, Datenaustausch mit einem einem virtuellen Rechnernetz ueber eine
     * 'Modemverbindung' zu erstellen, wird diese Zahl hoch angesetzt. <br />
     * Mit dieser Zahl sind die HOPS fuer einen Round-Trip als fuer die Hin- und Zurueck-Uebertragung beim
     * Datenaustausch mit einem anderen Knoten gemeint.
     */
    private static final int MAX_HOPS = 50;

    // extend RTT in case of slow machines by this factor; 1: no change
    private static int extendRTTfactor = 1;
    private Port[] ports = null;
    private SimplexConnection simplex01, simplex10;
    private Thread thread01, thread10;
    
    private void connect() throws ConnectionException {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + 
        		           " (Connection), bind()" + "\t" + ports[0].hashCode() + " <-> " + ports[1].hashCode());
        
        try {
            simplex01 = new SimplexConnection(ports[0], ports[1], this);
            simplex10 = new SimplexConnection(ports[1], ports[0], this);

            ports[0].setCable(this);
            ports[1].setCable(this);

            thread01 = new Thread(simplex01);
            thread10 = new Thread(simplex10);

            thread01.start();
            thread10.start();
        } catch (NullPointerException e) {
            simplex01 = null;
            simplex10 = null;
            ports[0].setCable(null);
            ports[1].setCable(null);
            throw new ConnectionException("EXCEPTION: " + messages.getString("verbindung_msg1"));
        }
    }
    
    public void disconnect() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (Connection), disconnect()");
        
        simplex01.disconnectPorts();
        simplex10.disconnectPorts();
        thread01.interrupt();
        thread10.interrupt();
    }
    
    public Port[] getPorts() {
        return ports;
    }

    public void setPorts(Port[] ports) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + 
        		           " (Connection), setPorts(" + ports + ")");
        
        this.ports = ports;

        try {
            connect();
        } catch (ConnectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace(Main.debug);
        }
    }

    /**
     * <b>findPortConnectedTo</b> returns the port that is connected to the given port.
     * 
     * @return The connected port if any, or null if there is none.
     */
    public Port findPortConnectedTo(Port port) {

        if (port == ports[0])      return ports[1];
        else if (port == ports[1]) return ports[0];
        else                       return null;
    }

    public static int getDelayFactor() {
        return delayFactor;
    }

    /**
     * Zum setzen den Verzoegerungsfaktors. Das Produkt daraus und der minimalen Verzoegerung ergibt die tatsaechliche
     * Verzoegerung bei der Uebertragung zwischen zwei Knoten im Rechnernetz. Der Wert des Faktors muss zwischen 1 und
     * 100 liegen. Wenn der uebergebene Parameter ausserhalb dieses Bereichs liegt, wird er auf den Minimal- bzw.
     * Maximalwert gesetzt.
     * 
     * @param factor Integer between 1 and 100 representing a factor by which the delay will be multiplied
     */
    public static void setDelayFactor(int factor) {
        Main.debug.println("INVOKED (static) (Connection), setDelayfactor(" + factor + ")");
        
        if (factor < 1) {
            Cable.delayFactor = 1;
        } else if (factor > 100) {
            Cable.delayFactor = 100;
        } else {
            Cable.delayFactor = factor;
        }
    }

    /**
     * Gibt die Verzoegerung einer Verbindung zwischen zwei Knoten im Rechnernetz in Millisekunden zurueck. Dazu wird
     * die minimale Verzoegerungszeit von 50 Millisekunden mit dem Verzoegerungsfaktor multipliziert.
     * 
     * @return Verzoegerung der Uebertragung zwischen zwei Knoten im Rechnernetz in Millisekunden
     */
    public static int getDelay() {
        return delayFactor * MIN_DELAY;
    }

    public static void setRTTfactor(int factor) {
        if (factor >= 1 && factor <= 5) {
            extendRTTfactor = factor;
        }
    }

    public static int getRTTfactor() {
        return extendRTTfactor;
    }

    /**
     * maximale Round-Trip-Time (RTT) in Millisekunden <br />
     * solange wird auf eine Antwort auf ein Segment gewartet
     */
    public static int getRTT() {
        return MAX_HOPS * getDelay() * extendRTTfactor;
    }

	@Override
	public String getHardwareType() {
		return TYPE;
	}
}
