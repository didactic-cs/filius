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

import filius.Main;
import filius.software.netzzugangsschicht.EthernetFrame;

public class SimplexConnection implements Runnable {

    private boolean threadRunning = true;
    private Connection connection = null;
    private Port port1 = null;
    private Port port2 = null;

    /**
     * @author carsten
     * @param port1
     *            - Sender der einseitigen Verbindung
     * @param port2
     *            - Empfaenger der einseitigen Verbindung
     * @param connection
     *            - Verbindung, auf der die einseitige Kommunikation gestartet wird
     * 
     *            Dieser Konstruktor wird innerhalb der Verbindung aufgerufen und in einem Thread gestartet. Davon gibt
     *            es zwei Verbindungen, die die bidirektionale Verbindung zwischen den beiden Hardwares herstellt.
     */
    public SimplexConnection(Port port1, Port port2, Connection connection) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() +
                           " (SimplexConnection), constr: SimplexConnection(" + port1 + "," + port2 + "," + connection + ")");
        
        this.port1 = port1;
        this.port2 = port2;
        this.connection = connection;
    }

    /**
     * @author carsten Diese run-Methode des Threads (nur Runnable!) sorgt fuer die einzelnen Kommunikationen auf einer
     *         Verbindung in beide Richtungen
     */
    public void run() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (SimplexConnection), run()");
        
        EthernetFrame frame;

        while (threadRunning) {
            synchronized (port1.getOutputBuffer()) {
                if (port1.getOutputBuffer().size() < 1) {
                    try {
                        // Main.debug.println("DEBUG ("+this.hashCode()+", K"+verbindung.hashCode()+"): SimplexConnection, run:   set wait()");
                        connection.setActive(false);
                        port1.getOutputBuffer().wait();
                    } catch (InterruptedException e) {}
                }
                if (port1.getOutputBuffer().size() > 0) {
                    // Main.debug.println("DEBUG ("+this.hashCode()+", K"+verbindung.hashCode()+"): SimplexConnection, run:   set wait()");
                    connection.setActive(true);
                    frame = (EthernetFrame) port1.getOutputBuffer().getFirst();
                    port1.getOutputBuffer().remove(frame);

                    synchronized (this) {
                        try {
                            Thread.sleep(Connection.getDelay());
                        } catch (InterruptedException e) {}
                    }

                    synchronized (port2.getInputBuffer()) {
                        port2.getInputBuffer().add(frame);
                        port2.getInputBuffer().notify();
                    }
                }
            }
        }
    }

    public Port getPort1() {
        return port1;
    }

    public void setPort1(Port port1) {
        this.port1 = port1;
    }

    public Port getPort2() {
        return port2;
    }

    public void setPort2(Port port2) {
        this.port2 = port2;
    }

    public void disconnectPorts() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() +
                           " (SimplexConnection), disconnectPorts()");
        port1.removeConnection();
        port2.removeConnection();
        this.setThreadRunning(false);
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public boolean isThreadRunning() {
        return threadRunning;
    }

    public void setThreadRunning(boolean threadRunning) {
        this.threadRunning = threadRunning;
    }

}
