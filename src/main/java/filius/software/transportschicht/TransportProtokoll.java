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
package filius.software.transportschicht;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import filius.exception.SocketException;
import filius.rahmenprogramm.I18n;
import filius.software.Protokoll;
import filius.software.system.InternetKnotenBetriebssystem;

public abstract class TransportProtokoll extends Protokoll implements I18n, Runnable {
    private static Logger LOG = LoggerFactory.getLogger(TransportProtokoll.class);

    private static final int PORT_UNTERE_GRENZE = 1024;

    private static final int PORT_OBERE_GRENZE = 65535;

    protected static final int TTL = 64;

    private int typ;

    /** IP-Adresse und Segment, das in einem neuen Thread verschickt wird */
    private LinkedList<Object[]> segmentListe = new LinkedList<Object[]>();

    private Map<Integer, SocketSchnittstelle> portTabelle = new HashMap<Integer, SocketSchnittstelle>();

    private TransportProtokollThread thread;

    /**
     * Dieser Thread fuehrt die run()-Methode dieser Klasse aus und wird dazu verwendet, Segmente zu verschicken.
     */
    private Thread sendeThread = null;
    /**
     * In diesem Attribut wird gespeichert, ob der Thread zum Versenden von Segmenten laeuft.
     */
    private boolean running = false;

    /**
     * @author carsten
     * @param betriebssystem
     */
    public TransportProtokoll(InternetKnotenBetriebssystem betriebssystem, int typ) {
        super(betriebssystem);
        LOG.trace("INVOKED-2 (" + this.hashCode() + ") " + getClass()
                + " (TransportProtokoll), constr: TransportProtokoll(" + betriebssystem + "," + typ + ")");
        this.typ = typ;
    }

    public List<SocketSchnittstelle> holeAktiveSockets() {
        List<SocketSchnittstelle> sockets = new ArrayList<>();
        for (SocketSchnittstelle socket : portTabelle.values()) {
            if (null != socket) {
                sockets.add(socket);
            }
        }
        return sockets;
    }

    public int holeTyp() {
        return typ;
    }

    public int reserviereFreienPort() {
        return reserviereFreienPort(null);
    }

    public int reserviereFreienPort(Socket socket) {
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass() + " (TransportProtokoll), reserviereFreienPort("
                + socket + ")");
        // Freien Port suchen
        boolean portGefunden = false;
        int freienPort;

        do {
            freienPort = sucheFreienPort();
            if (!portTabelle.containsKey(freienPort)) {
                portGefunden = true;
            }
        } while (!portGefunden);
        reservierePort(freienPort, socket);

        return freienPort;
    }

    public SocketSchnittstelle holeSocket(int port) throws SocketException {
        LOG.trace(
                "INVOKED (" + this.hashCode() + ") " + getClass() + " (TransportProtokoll), holeSocket(" + port + ")");
        if (port == -1) {
            throw new SocketException(messages.getString("sw_transportprotokoll_msg3"));
        }
        if (!portTabelle.containsKey(port)) {
            throw new SocketException(messages.getString("sw_transportprotokoll_msg1") + " " + port + " "
                    + messages.getString("sw_transportprotokoll_msg2"));
        }
        return (SocketSchnittstelle) portTabelle.get(port);
    }

    public boolean isUsed(int port) {
        return portTabelle.containsKey(port);
    }

    private int sucheFreienPort() {
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass() + " (TransportProtokoll), sucheFreienPort()");
        int spanne = PORT_OBERE_GRENZE - PORT_UNTERE_GRENZE;
        Random random = new Random();
        int zufallsZahl = Math.abs(random.nextInt());
        int zahl = (zufallsZahl) % spanne;

        return (PORT_UNTERE_GRENZE + zahl);
    }

    public boolean reservierePort(int port, SocketSchnittstelle socket) {
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass() + " (TransportProtokoll), reservierePort(" + port
                + "," + socket + ")");
        boolean portIsSubscribed = false;
        synchronized (portTabelle) {
            if (portTabelle.containsKey(port) && !portTabelle.get(port).equals(socket)) {
                LOG.debug("Port " + port + " cannot be subscribed for. It is already in use.");
            } else if (portTabelle.containsKey(port) && portTabelle.get(port).equals(socket)) {
                LOG.debug("Port {} already subscribed for the socket. Nothing to do.", port);
                portIsSubscribed = true;
            } else {
                LOG.debug("Port {} is subscribed for.", port);
                portTabelle.put(port, socket);
                portIsSubscribed = true;
            }
        }
        return portIsSubscribed;
    }

    public boolean gibPortFrei(int port) {
        LOG.debug("remove port {} from list of registered ports.", port);
        synchronized (portTabelle) {
            if (portTabelle.containsKey(port)) {
                portTabelle.remove(port);
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * @param zielIp
     *            - Ip des Empfaengers
     * @param protokoll
     *            - Protokollnummer des Protokolls, auf dass aufgesetzt wird.
     * @param segment
     *            - Segment mit Daten zur IP-Schicht
     */
    protected void senden(String zielIp, Object segment) {
        senden(zielIp, null, segment);
    }

    protected void senden(String zielIp, String quellIp, Object segment) {
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass() + " (TransportProtokoll), senden(" + zielIp + ","
                + segment + ")");

        synchronized (segmentListe) {
            segmentListe.addLast((new Object[] { zielIp, quellIp, segment }));
            segmentListe.notifyAll();
        }
    }

    public void run() {
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass() + " (TransportProtokoll), run()");
        InternetKnotenBetriebssystem bs;

        Object[] temp;

        while (running) {
            synchronized (segmentListe) {
                if (segmentListe.size() < 1) {
                    try {
                        segmentListe.wait();
                    } catch (InterruptedException e1) {}
                }
                if (segmentListe.size() > 0) {
                    temp = (Object[]) segmentListe.removeFirst();
                    bs = (InternetKnotenBetriebssystem) holeSystemSoftware();
                    bs.holeIP().senden((String) temp[0], (String) temp[1], holeTyp(), TTL, (Segment) temp[2]);
                }
            }
        }
    }

    public void starten() {
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass() + " (TransportProtokoll), starten()");

        thread = new TransportProtokollThread(this);
        thread.starten();

        if (!running) {
            running = true;
            if (sendeThread == null || (!sendeThread.getState().equals(State.WAITING)
                    && !sendeThread.getState().equals(State.BLOCKED))) {
                sendeThread = new Thread(this);
                sendeThread.start();
            }
        }
    }

    public void beenden() {
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass() + " (TransportProtokoll), beenden()");
        thread.beenden();

        running = false;
        if (sendeThread != null
                && (sendeThread.getState().equals(State.WAITING) || sendeThread.getState().equals(State.BLOCKED))) {
            sendeThread.interrupt();
        }
        portTabelle.clear();
    }
}
