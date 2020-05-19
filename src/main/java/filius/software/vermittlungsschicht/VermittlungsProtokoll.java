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
package filius.software.vermittlungsschicht;

import java.util.List;

import filius.Main;
import filius.exception.InvalidParameterException;
import filius.hardware.NetzwerkInterface;
import filius.hardware.knoten.InternetKnoten;
import filius.software.Protokoll;
import filius.software.system.SystemSoftware;

/** Oberklasse von ARP und IP mit Hilfsmethoden */
public abstract class VermittlungsProtokoll extends Protokoll {

    /** Standard-Konstruktor der Oberklasse Protokoll */
    public VermittlungsProtokoll(SystemSoftware systemSoftware) {
        super(systemSoftware);
        Main.debug.println("INVOKED-2 (" + this.hashCode() + ") " + getClass()
                + " (VermittlungsProtokoll), constr: VermittlungsProtokoll(" + systemSoftware + ")");
    }

    /**
     * Methode zum pruefen, ob sich zwei IP-Adressen im gleichen Rechnernetz befinden. Dazu wird die Netzmaske
     * benoetigt:
     * <ol>
     * <li>Bitweise ODER-Verknuepfung der ersten IP-Adresse und der Netzmaske</li>
     * <li>Bitweise ODER-Verknuepfung der zweiten IP-Adresse und der Netzmaske</li>
     * <li>Vergleich der zwei Netz-IDs, die in den vorangegangenen Schritten berechnet wurden</li>
     * </ol>
     * 
     * @param adresseEins
     *            erste IP-Adresse als String
     * @param adresseZwei
     *            zweite IP-Adresse als String
     * @param netzmaske
     *            Netzmaske als String
     * @return ob die Netz-IDs der zwei Adressen uebereinstimmen
     */
    public static boolean gleichesRechnernetz(String adresseEins, String adresseZwei, String netzmaske) {
        Main.debug.println(
                "INVOKED (static) filius.software.vermittlungsschicht.VermittlungsProtokoll, gleichesRechnernetz("
                        + adresseEins + "," + adresseZwei + "," + netzmaske + ")");

        boolean isEqual = false;
        try {
            IPAddress ipAddress1 = new IPAddress(adresseEins, netzmaske);
            isEqual = ipAddress1.equalNetwork(adresseZwei);
        } catch (InvalidParameterException e) {}
        return isEqual;
    }

    public static boolean isNetworkAddress(String zielIpAdresse, String sendeIpAdresse, String netzmaske) {
        boolean result = false;
        try {
            IPAddress dest = new IPAddress(zielIpAdresse, netzmaske);
            result = dest.networkAddress().equalsIgnoreCase(dest.address()) && dest.equalNetwork(sendeIpAdresse);
        } catch (InvalidParameterException e) {}
        return result;
    }

    public static boolean isBroadcast(String zielIpAdresse, String sendeIpAdresse, String netzmaske) {
        boolean isBroadcast = false;
        try {
            IPAddress tmp = new IPAddress(zielIpAdresse, netzmaske);
            isBroadcast = tmp.isGlobalBroadcast() || tmp.isLocalBroadcast() && tmp.equalNetwork(sendeIpAdresse);
        } catch (InvalidParameterException e) {}
        return isBroadcast;
    }

    public boolean isLocalAddress(String ip) {
        try {
            IPAddress tmp = new IPAddress(ip);
            if (tmp.isLoopbackAddress()) {
                return true;
            }

            InternetKnoten knoten = (InternetKnoten) holeSystemSoftware().getKnoten();
            for (NetzwerkInterface nic : knoten.getNetzwerkInterfaces()) {
                if (nic.addressIP() != null && nic.addressIP().equals(new IPAddress(ip, nic.addressIP().netmask()))) {
                    return true;
                }
            }
        } catch (InvalidParameterException e) {}
        return false;
    }

    public boolean isApplicableBroadcast(String zielIp) {
        List<NetzwerkInterface> nics = ((InternetKnoten) this.holeSystemSoftware().getKnoten()).getNetzwerkInterfaces();
        for (NetzwerkInterface nic : nics) {
            if (isBroadcast(zielIp, nic.getIp(), nic.getSubnetzMaske())) {
                return true;
            }
        }
        return false;
    }
}
