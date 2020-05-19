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
package filius.hardware.knoten;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import filius.Main;
import filius.exception.InvalidParameterException;
import filius.hardware.NetzwerkInterface;
import filius.hardware.Port;
import filius.software.vermittlungsschicht.IPAddress;

public abstract class InternetKnoten extends Knoten {

    private static final long serialVersionUID = 1L;
    private List<NetzwerkInterface> netzwerkInterfaces = new LinkedList<NetzwerkInterface>();

    public Port holeFreienPort() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (InternetKnoten), holeFreienPort()");
        ListIterator<NetzwerkInterface> iter = getNetzwerkInterfaces().listIterator();
        while (iter.hasNext()) {
            NetzwerkInterface nic = (NetzwerkInterface) iter.next();
            Port anschluss = nic.getPort();
            if (anschluss.isPortFrei()) {
                // Main.debug.println("\tfound free port: "+anschluss);
                return anschluss;
            }
        }
        // Main.debug.println("\tno free port available");
        return null;
    }

    /**
     * 
     * Gibt das NetzwerkInterface zurueck, dass die angegebene mac Adresse hat. Falls kein Interface diese Mac-Adresse
     * besitzt, wird null zurueckgegeben.
     * 
     * @author Johannes Bade
     * @param mac
     * @return
     */
    public NetzwerkInterface getNetzwerkInterfaceByMac(String mac) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass()
                + " (InternetKnoten), getNetzwerkInterfaceByMac(" + mac + ")");
        NetzwerkInterface rueckgabe = null;
        ListIterator<NetzwerkInterface> it = this.netzwerkInterfaces.listIterator();
        while (it.hasNext()) {
            NetzwerkInterface ni = (NetzwerkInterface) it.next();
            if (ni.getMac().equals(mac)) {
                rueckgabe = ni;
            }
        }
        return rueckgabe;
    }

    public void removeNic(NetzwerkInterface nic) {
        this.netzwerkInterfaces.remove(nic);
    }

    /**
     * 
     * Gibt die Netzwerkkarte mit der entsprechenden IP zurueck
     * 
     * @author Thomas
     * @param mac
     * @return
     */
    public NetzwerkInterface getNetzwerkInterfaceByIp(String ip) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass()
                + " (InternetKnoten), getNetzwerkInterfaceByIp(" + ip + ")");
        NetzwerkInterface rueckgabe = null;
        try {
            IPAddress ipAddress = new IPAddress(ip);
            if (ipAddress.isLoopbackAddress()) {
                return (NetzwerkInterface) netzwerkInterfaces.get(0);
            }
            for (NetzwerkInterface ni : netzwerkInterfaces) {
                if (ipAddress.equalAddress(ni.getIp())) {
                    rueckgabe = ni;
                }
            }
        } catch (InvalidParameterException e) {}
        return rueckgabe;
    }

    public List<NetzwerkInterface> getNetzwerkInterfaces() {
        return netzwerkInterfaces;
    }

    public void setNetzwerkInterfaces(List<NetzwerkInterface> netzwerkInterfaces) {
        this.netzwerkInterfaces = netzwerkInterfaces;
    }

    public int holeAnzahlAnschluesse() {
        return netzwerkInterfaces.size();
    }

    public void hinzuAnschluss() {
        netzwerkInterfaces.add(new NetzwerkInterface());
    }

    public void setzeAnzahlAnschluesse(int anzahlAnschluesse) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass()
                + " (InternetKnoten), setzeAnzahlAnschluesse(" + anzahlAnschluesse + ")");

        netzwerkInterfaces = new LinkedList<NetzwerkInterface>();
        for (int i = 0; i < anzahlAnschluesse; i++) {
            netzwerkInterfaces.add(new NetzwerkInterface());
        }
    }
}
