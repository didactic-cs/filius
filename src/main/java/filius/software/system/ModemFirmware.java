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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import filius.Main;
import filius.hardware.knoten.Modem;
import filius.rahmenprogramm.I18n;
import filius.software.netzzugangsschicht.ModemEmpfaenger;
import filius.software.netzzugangsschicht.ModemSender;

/**
 * <p>
 * Diese Klasse implementiert die Systemfunktionalitaet des Modems. Dies dient dazu, Rechnernetze miteinander zu
 * verbinden, die auch auf verschiedenen realen Rechnern laufen koennen.
 * </p>
 * <p>
 * Das Modem unterscheidet dazu zwei Modi: Im Server-Modus wird eine Verbindungsanfrage eines zweiten Modems angenommen.
 * Im Client-Modus wird der Verbindungsaufbau zu einem Modem im Server-Modus initiiert.
 * </p>
 * <p>
 * Zur Verbindung zweier Modems wird eine reale TCP/IP-Verbindung aufgebaut. Das ermöglicht, dass Modems verschiedener
 * Filius-Prozesse Daten austauschen. Die verbundenen Modems tauschen darueber alle Daten aus, die sie aus dem jeweils
 * angeschlossenen virtuellen Rechnernetz empfangen.
 * </p>
 * <p>
 * Damit kann jedoch die Situation auftreten, dass sich ein Modem im Entwurfsmodus und sich das zweite im Aktionsmodus
 * befindet. Daten, die ein Modem empfaengt, waehrend es sich im Entwurfsmodus befindet, werden verworfen.
 * </p>
 */
@SuppressWarnings("serial")
public class ModemFirmware extends SystemSoftware implements Runnable, I18n {

    /**
     * Das Modem kann in zwei verschiedenen Modi betrieben werden. Als Server wartet es auf Verbindungswuensche und als
     * Client baut es die Verbindung zu einem anderen Modem im Server-Modus auf.
     */
    public static final int SERVER = 1, CLIENT = 2;

    /**
     * Der tatsaechliche TCP-Port, der geoeffnet wird oder zu dem die Verbindung aufgebaut wird. D. h. es wird immer nur
     * der Port des Modems im Server-Modus festgelegt.
     */
    private int port = 12345;

    /**
     * Die IP-Adresse des anderen Rechners, auf dem das Server-Modem laeuft. D. h. dieses Attribut wird nur im
     * Client-Modus verwendet.
     */
    private String ipAdresse = "localhost";

    private ServerSocket serverSocket;

    private Socket socket = null;

    /** Hier kommen die Daten vom anderen Modem an */
    private ModemEmpfaenger empfaenger = null;

    /**
     * Hier kommen die Daten des verbundenen (eigenen) Rechnernetzes an.
     */
    private ModemSender sender = null;

    /**
     * Der Modus, in dem das Modem betrieben wird. Das Modem kann in zwei verschiedenen Modi betrieben werden. Als
     * Server wartet es auf Verbindungswuensche und als Client baut es die Verbindung zu einem anderen Modem im
     * Server-Modus auf.
     */
    private int mode = CLIENT;

    /** Ob ein zuvor verschickter Teststring erfolgreich empfangen wurde */

    /**
     * Ob das Modem bereits gestartet wurde und damit eingehende Daten auch verarbeitet werden.
     */
    private boolean gestartet = false;

    /**
     * Diese Methode dient dazu, ein Modem zu starten, dass im Server-Modus betrieben wird. Damit wird der TCP-Port
     * geoeffnet und eingehende Verbindungsanfragen koennen entgegen genommen werden. Das Warten auf eingehende
     * Verbindungen und die Ueberwachung des Socket-Status erfolgt in einem neuen Thread!
     */
    public synchronized void starteServer() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (ModemFirmware), starteServer()");
        (new Thread(this)).start();
    }

    /**
     * Mit diesr Methode wird das Modem im Client-Modus gestartet. Das heisst, dass eine TCP/IP-Verbindung zu einem
     * anderen Modem im Server-Modus hergestellt wird. Ausserdem wird der Thread zur Ueberwachung des Socket-Status
     * gestartet.
     */
    public void starteClient() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (ModemFirmware), starteClient()");
        try {
            socket = new Socket(ipAdresse, port);
            aktiviereModemVerbindung();
        } catch (UnknownHostException e) {
            e.printStackTrace(Main.debug);
            benachrichtigeBeobacher(messages.getString("modemfirmware_msg1"));
            ((Modem) getKnoten()).setzeModemVerbindungAktiv(false);
        } catch (IOException e) {
            e.printStackTrace(Main.debug);
            benachrichtigeBeobacher(messages.getString("modemfirmware_msg2"));
            ((Modem) getKnoten()).setzeModemVerbindungAktiv(false);
        } catch (InterruptedException e) {
            e.printStackTrace(Main.debug);
            benachrichtigeBeobacher(null);
            ((Modem) getKnoten()).setzeModemVerbindungAktiv(false);
        }
    }

    private synchronized void aktiviereModemVerbindung() throws IOException, InterruptedException {
        OutputStream out = null;
        InputStream in = null;
        while (!socket.isConnected()) {
            Thread.sleep(100);
        }
        benachrichtigeBeobacher(null);
        in = socket.getInputStream();
        out = socket.getOutputStream();
        ((Modem) getKnoten()).setzeModemVerbindungAktiv(true);
        if (in != null && out != null) {
            leerePortPuffer();
            empfaenger = new ModemEmpfaenger(this, in);
            sender = new ModemSender(this, out);
            empfaenger.starten();
            sender.starten();
        }
    }

    private synchronized void deaktiviereModemVerbindung() {
        if (empfaenger != null) {
            empfaenger.beenden();
            empfaenger = null;
        }
        if (sender != null) {
            sender.beenden();
            sender = null;
        }
    }

    /**
     * Mit dieser Methode wird der Modus bestimmt, in dem das Modem laeuft. Wenn der Modus gewaechselt wird, werden
     * eventuell bestehende Verbindungen abgebrochen.
     * 
     * @param mode
     *            der neue Modus (SERVER oder CLIENT)
     */
    public void setMode(int mode) {
        trennen();
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }

    public boolean istGestartet() {
        return gestartet;
    }

    /**
     * In dieser Methode werden die Portbeobachter gegebenenfalls gestartet. Der Verbindungsaufbau erfolgt im
     * Entwurfsmodus durch Benutzerinteraktion.
     */
    @Override
    public void starten() {
        gestartet = true;
    }

    /**
     * In dieser Methode wird nichts ausgefuehrt. Der Verbindungsabbau wird in beiden Modi erst durch
     * Benutzerinteraktion initiiert.
     */
    @Override
    public void beenden() {
        gestartet = false;
    }

    public boolean istServerBereit() {
        return serverSocket != null && serverSocket.isBound();
    }

    /**
     * Diese Methode wird durch ein Ereignis von der GUI aufgerufen (d. h. durch Benutzereingaben) oder durch den Thread
     * ausgeloest, der den Socket ueberwacht oder beim Wechsel des Modus aufgerufen. <br />
     * Wenn noch Verbindungen bestehen werden diese abgebaut.
     */
    public void trennen() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (ModemFirmware), trennen()");

        deaktiviereModemVerbindung();
        if (mode == SERVER && serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {}
            serverSocket = null;
        }

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace(Main.debug);
            }
            socket = null;
        }
        benachrichtigeBeobacher(null);
        ((Modem) getKnoten()).setzeModemVerbindungAktiv(false);
    }

    public void verbindungZuruecksetzen() {
        trennen();
        if (mode == SERVER) {
            starteServer();
        }
    }

    /**
     * Mit dieser Methode werden gegebenenfalls noch nicht leere Puffer der Modemanschluesse vor dem Start der
     * Datenweiterleitung geleert.
     */
    private void leerePortPuffer() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (ModemFirmware), leerePortPuffer()");
        synchronized (((Modem) getKnoten()).getErstenAnschluss().holeEingangsPuffer()) {
            ((Modem) getKnoten()).getErstenAnschluss().holeEingangsPuffer().clear();
        }
    }

    /**
     * Dieser Thread wird ausschliesslich fuer den Verbindungsaufbau im Server-Modus genutzt! Das Modem im Server-Modus
     * wartet auf eingehende Verbindungen. Es wird aber nur eine Verbindungsanfrage angenommen. Um nicht den gesamten
     * Programmablauf zu unterbrechen, erfolgt das Warten in einem eigenen Thread.
     */
    public void run() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (ModemFirmware), run()");
        try {
            serverSocket = new ServerSocket(port);
            benachrichtigeBeobacher(null);
            socket = serverSocket.accept();
            aktiviereModemVerbindung();
            serverSocket.close();
        } catch (Exception e) {
            Main.debug.println("EXCEPTION (" + this.hashCode() + "): Modemverbindung beendet.");
            ((Modem) getKnoten()).setzeModemVerbindungAktiv(false);
        } finally {
            benachrichtigeBeobacher(null);
        }
    }

    public String getIpAdresse() {
        return ipAdresse;
    }

    public void setIpAdresse(String ipAdresse) {
        this.ipAdresse = ipAdresse;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
