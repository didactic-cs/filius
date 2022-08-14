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
import filius.software.netzzugangsschicht.ModemReceiver;
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
	
	public static enum ModemStatus {off, waiting, connected};	

	private ModemStatus status = ModemStatus.off;	
	
    /**
     * Das Modem kann in zwei verschiedenen Modi betrieben werden. Als Server wartet es auf Verbindungswuensche und als
     * Client baut es die Verbindung zu einem anderen Modem im Server-Modus auf.
     */
    public static enum ModemMode {SERVER, CLIENT};
    
    /**
     * Der Modus, in dem das Modem betrieben wird. Das Modem kann in zwei verschiedenen Modi betrieben werden. Als
     * Server wartet es auf Verbindungswuensche und als Client baut es die Verbindung zu einem anderen Modem im
     * Server-Modus auf.
     */
    private ModemMode mode = ModemMode.CLIENT;
    
    /**
     * Port number used in server mode
     */
    private int localPort = 12345;

    /**
     * Port number of the contacted server when in client mode 
     */
    private int remotePort = 12345;

    /**
     * IP address of the contacted server when in client mode 
     */
    private String remoteIPAddress = "localhost";

    private ServerSocket serverSocket;

    private Socket socket = null;

    /** Hier kommen die Daten vom anderen Modem an */
    private ModemReceiver receiver = null;

    /**
     * Hier kommen die Daten des verbundenen (eigenen) Rechnernetzes an.
     */
    private ModemSender sender = null;
    
    private boolean serverRunning = false;

    
    /**
     * Diese Methode dient dazu, ein Modem zu starten, dass im Server-Modus betrieben wird. Damit wird der TCP-Port
     * geoeffnet und eingehende Verbindungsanfragen koennen entgegen genommen werden. Das Warten auf eingehende
     * Verbindungen und die Ueberwachung des Socket-Status erfolgt in einem neuen Thread!
     */
    public synchronized void startServer() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (ModemFirmware), startServer()");
        
        if (!serverRunning) {
        	try {
        		serverSocket = new ServerSocket(localPort);
        		serverRunning = true;    
        		
        		setStatus(ModemStatus.waiting);
        		
        		(new Thread(this)).start();
        	} catch (Exception e) {}   
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
            socket = serverSocket.accept();
            activateModemConnection();
            serverSocket.close();
            
        } catch (Exception e) {
            Main.debug.println("EXCEPTION (" + this.hashCode() + "): Modemverbindung beendet.");    
        } finally {     
        	//serverRunning = false;
        }
    }
    
    /**
     * Mit dieser Methode wird das Modem im Client-Modus gestartet. Das heisst, dass eine TCP/IP-Verbindung zu einem
     * anderen Modem im Server-Modus hergestellt wird. Ausserdem wird der Thread zur Ueberwachung des Socket-Status
     * gestartet.
     */
    public void startClient() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (ModemFirmware), startClient()");
        
        try {
            socket = new Socket(remoteIPAddress, remotePort);
            activateModemConnection();
            
        } catch (UnknownHostException e) {
            e.printStackTrace(Main.debug);
            fireDisplayMessage(messages.getString("modemfirmware_msg1"));
            setStatus(ModemStatus.off);
           
        } catch (IOException e) {
            e.printStackTrace(Main.debug);
            fireDisplayMessage(messages.getString("modemfirmware_msg2"));
            setStatus(ModemStatus.off);
            
        } catch (InterruptedException e) {
            e.printStackTrace(Main.debug);     
            setStatus(ModemStatus.off);
        }
    }   

    private synchronized void activateModemConnection() throws IOException, InterruptedException {
    	
        OutputStream out = null;
        InputStream in = null;
        while (!socket.isConnected()) {
            Thread.sleep(100);
        }        
        in = socket.getInputStream();
        out = socket.getOutputStream();   
        
        setStatus(ModemStatus.connected);
        
        if (in != null && out != null) {
            clearPortBuffer();
            receiver = new ModemReceiver(this, in);
            sender = new ModemSender(this, out);
            receiver.startThread();
            sender.startThread();
        }
    }

    private synchronized void deactivateModemConnection() {
    	
    	serverRunning = false;
        if (receiver != null) {
            receiver.stopThread();
            receiver = null;
        }
        if (sender != null) {
            sender.stopThread();
            sender = null;
        }
    }
    
    public ModemStatus getStatus() {
        return status;
    }
    
    public void setStatus (ModemStatus status) {
        this.status = status;
        fireModemStatusChange(status);
    }    
    
    public ModemMode getMode() {
        return mode;
    }

    /**
     * Mit dieser Methode wird der Modus bestimmt, in dem das Modem laeuft. Wenn der Modus gewaechselt wird, werden
     * eventuell bestehende Verbindungen abgebrochen.
     * 
     * @param mode
     *            der neue Modus (SERVER oder CLIENT)
     */
    public void setMode(ModemMode mode) {
    	
    	if (this.mode == mode) return;
    	
        closeConnection();
        this.mode = mode;
    }

    public boolean isServerConnected() {
        return serverSocket != null && serverSocket.isBound();
    }
    
    public boolean isServerRunning() {
        return serverRunning;
    }

    /**
     * Diese Methode wird durch ein Ereignis von der GUI aufgerufen (d. h. durch Benutzereingaben) oder durch den Thread
     * ausgeloest, der den Socket ueberwacht oder beim Wechsel des Modus aufgerufen. <br />
     * Wenn noch Verbindungen bestehen werden diese abgebaut.
     */
    public void closeConnection() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (ModemFirmware), closeConnection()");

        deactivateModemConnection();
        if (mode == ModemMode.SERVER && serverSocket != null) {
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
        setStatus(ModemStatus.off);
    }

    public void resetConnection() {
    	
        closeConnection();        
        if (mode == ModemMode.SERVER) startServer();       
    }

    /**
     * Mit dieser Methode werden gegebenenfalls noch nicht leere Puffer der Modemanschluesse vor dem Start der
     * Datenweiterleitung geleert.
     */
    private void clearPortBuffer() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (ModemFirmware), clearPortBuffer()");
        
        synchronized (((Modem) getNode()).getPort().getInputBuffer()) {
            ((Modem) getNode()).getPort().getInputBuffer().clear();
        }
    }
    
    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }
    
    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public String getRemoteIPAddress() {
        return remoteIPAddress;
    }

    public void setRemoteIPAddress(String remoteIPAddress) {
        this.remoteIPAddress = remoteIPAddress;
    }

}
