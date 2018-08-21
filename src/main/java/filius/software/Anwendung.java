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
package filius.software;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.Map;
import java.util.Observer;

import filius.Main;
import filius.rahmenprogramm.Information;
import filius.software.system.InternetKnotenBetriebssystem;

/**
 * Die Klasse Anwendung ist die Oberklasse aller Anwendungen, die auf einem Rechner installiert werden koennen. Als
 * beobachtetes Objekt implementiert diese Klasse eine Komponente des Beobachtermusters mit Hilfe der Klasse
 * AnwendungObservable. Es werden die Standardkomponenten des JDK verwendet.
 * 
 * @see java.util.Observable
 * @see filius.software.AnwendungObservable
 */
public abstract class Anwendung extends Thread {

    /** Bezeichnung fuer die Anwendung */
    private String anwendungsName;

    /**
     * Ein Puffer fuer eingehende Kommandos. In dem Puffer werden Objekt-Arrays aus zwei Elementen gespeichert. Das
     * erste Element ist ein String, der die aufzurufende Methode bestimmt. Das zweite Element ist ein Objekt-Array mit
     * den Parametern fuer den Methodenaufruf.
     */
    private LinkedList<Object[]> kommandos = new LinkedList<Object[]>();

    /**
     * Das Betriebssystem des Rechners/Vermittlungsrechner, auf dem die Anwendung ausgefuehrt wird.
     */
    private InternetKnotenBetriebssystem betriebssystem;

    /** Beobachter der Anwendung */
    private AnwendungObservable observable = new AnwendungObservable();

    /** Dieses Attribut zeigt an, ob der Thread laeuft. */
    protected boolean running = false;

    /**
     * Der Konstruktur bewirkt eine Meldung auf der Standardausgabe, dass die Anwendung erzeugt wurde.
     */
    public Anwendung() {
        Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
                + " (Anwendung), constr: Anwendung()");

        try {
            for (Map<String, String> appInfo : Information.getInformation().ladeProgrammListe()) {
                if (this.getClass().getCanonicalName().equals((String) appInfo.get("Klasse"))) {
                    this.setzeAnwendungsName(appInfo.get("Anwendung").toString());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace(Main.debug);
        }
    }

    /**
     * Methode fuer das Beobachtermuster: Hinzufuegen eines weiteren Beobachters.
     * 
     * @param beobachter
     */
    public void hinzuBeobachter(Observer beobachter) {
        Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
                + " (Anwendung), hinzuBeobachter(" + beobachter + ")");
        observable.addObserver(beobachter);
    }

    /** Methode zur Benachrichtigung der Beobachter. */
    public void benachrichtigeBeobachter(Object daten) {
        Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
                + " (Anwendung), benachrichtigeBeobachter(" + daten + ")");
        observable.notifyObservers(daten);
    }

    /** Methode zur Benachrichtigung der Beobachter. */
    public void benachrichtigeBeobachter() {
        Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
                + " (Anwendung), benachrichtigeBeobachter()");
        observable.notifyObservers();
    }

    /**
     * Methode zum Starten des Threads beim Wechsel vom Entwurfs- in den Aktionsmodus.
     */
    public void starten() {
        Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
                + " (Anwendung), starten()");
        running = true;

        synchronized (kommandos) {
            kommandos.clear();
        }
        if (getState().equals(State.NEW)) {
            start();
        } else {
            synchronized (this) {
                notifyAll();
            }
        }
    }

    /**
     * Methode zum Anhalten des Threads.
     */
    public void beenden() {
        Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
                + " (Anwendung), beenden()");
        running = false;

        if (kommandos != null) {
            synchronized (kommandos) {
                kommandos.clear();
                kommandos.notifyAll();
            }
        }
    }

    /**
     * Methode zur Uebergabe von auszufuehrenden Kommandos. Die Uebergebenen Methodenaufrufe werden in dem Thread
     * ausgefuehrt, der von dieser Klasse implementiert wird. Damit wird der aufrufende Thread nicht blockiert. Die
     * Verwendung dieser Moeglichkeit fuer Methodenaufrufe ist also zur <b>Ausfuehrung blockierender Methoden</b>
     * wichtig.
     * 
     * @param methode
     *            Der Bezeichner der auszufuehrenden Methode
     * @param args
     *            die Parameter der Methode
     */
    protected void ausfuehren(String methode, Object[] args) {
        Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
                + " (Anwendung), ausfuehren(" + methode + "," + args + ")");
        Object[] aufruf;

        aufruf = new Object[2];
        aufruf[0] = methode;
        aufruf[1] = args;

        synchronized (kommandos) {
            kommandos.addLast(aufruf);
            kommandos.notifyAll();
        }
    }

    /**
     * Hier wird der Puffer kommandos ueberwacht und wenn dort ein Methodenaufruf vorliegt wird diese Methode
     * aufgerufen.
     */
    public void run() {
        Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
                + " (Anwendung), run()");
        Class<?>[] argumentKlassen;
        Class<?> klasse;
        Method method;
        String methodenName;
        Object[] args;
        Object[] aufruf;

        while (true) {
            if (running) {
                synchronized (kommandos) { // first block, then check size!
                                           // (otherwise: prone to race
                                           // conditions)
                    if (kommandos.size() < 1) {
                        try {
                            kommandos.wait();
                        } catch (InterruptedException e) {}
                    }
                }
                if (kommandos.size() > 0) {
                    aufruf = (Object[]) kommandos.removeFirst();

                    methodenName = aufruf[0].toString();
                    args = (Object[]) aufruf[1];

                    if (args != null) {
                        argumentKlassen = new Class[args.length];
                        for (int i = 0; i < args.length; i++) {
                            if (args[i] != null)
                                argumentKlassen[i] = args[i].getClass();
                        }
                    } else {
                        argumentKlassen = null;
                    }
                    klasse = getClass();
                    // go upwards in inheritance hierarchy until the class was
                    // found containing
                    // the desired method, i.e., exceptions are rather harmless
                    // here
                    while (klasse != null) {
                        try {
                            method = klasse.getDeclaredMethod(methodenName, argumentKlassen);

                            method.invoke(this, args);

                            klasse = null;
                        } catch (NoSuchMethodException e) {
                            klasse = klasse.getSuperclass();
                        } catch (Exception e) {
                            e.printStackTrace(Main.debug);
                            klasse = null;
                        }
                    }
                }
            } else {
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException e) {}
                }
            }
        }
    }

    /** Methode fuer den Zugriff auf den Anwendungsnamen */
    public String holeAnwendungsName() {
        return anwendungsName;
    }

    /** Methode fuer den Zugriff auf den Anwendungsnamen */
    public void setzeAnwendungsName(String anwendungsName) {
        this.anwendungsName = anwendungsName;
    }

    /*
     * method for downward compatibility; older versions of filius possibly used this method, such that some saved
     * scenarios depend on it! ... or maybe only JAVA demands properties to be set by a "set" method! (required by
     * XMLDecoder)
     */
    public void setAnwendungsName(String anwendungsName) {
        setzeAnwendungsName(anwendungsName);
    }

    /**
     * Methode fuer den Zugriff auf das Betriebssystem, auf dem diese Anwendung laeuft.
     * 
     * @param bs
     */
    public void setSystemSoftware(InternetKnotenBetriebssystem bs) {
        betriebssystem = bs;
    }

    /**
     * Methode fuer den Zugriff auf das Betriebssystem, auf dem diese Anwendung laeuft.
     * 
     * @param bs
     */
    public InternetKnotenBetriebssystem getSystemSoftware() {
        return betriebssystem;
    }
}
