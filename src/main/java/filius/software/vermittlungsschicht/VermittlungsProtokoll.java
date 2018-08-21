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
import java.util.StringTokenizer;

import filius.Main;
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
        Main.debug
                .println("INVOKED (static) filius.software.vermittlungsschicht.VermittlungsProtokoll, gleichesRechnernetz("
                        + adresseEins + "," + adresseZwei + "," + netzmaske + ")");
        int addressOneAsInt = ipAddressToInt(adresseEins);
        int addressTwoAsInt = ipAddressToInt(adresseZwei);
        int netmaskAsInt = ipAddressToInt(netzmaske);

        return (addressOneAsInt & netmaskAsInt) == (addressTwoAsInt & netmaskAsInt);
    }

    static int ipAddressToInt(String address) {
        int addressAsInt = 0;
        StringTokenizer tokenizer = new StringTokenizer(address, ".");
        while (tokenizer.hasMoreTokens()) {
            addressAsInt = (addressAsInt << 8) + Integer.parseInt(tokenizer.nextToken());
        }
        return addressAsInt;
    }

    public static boolean isBroadcast(String zielIpAdresse, String sendeIpAdresse, String netzmaske) {
        int addressAsInt = ipAddressToInt(zielIpAdresse);
        int netmaskAsInt = ipAddressToInt(netzmaske);
        boolean isGenericBroadcast = addressAsInt == 0xffffffff;
        boolean isNetworkBroadcast = (addressAsInt & ~netmaskAsInt) == (0xffffffff & ~netmaskAsInt)
                && gleichesRechnernetz(zielIpAdresse, sendeIpAdresse, netzmaske);
        return isGenericBroadcast || isNetworkBroadcast;
    }

    /*
     * Determine subnet address for given IP and netmask.
     */
    public static String getSubnetForIp(String ip, String mask) {
        Main.debug
                .println("INVOKED (static) filius.software.vermittlungsschicht.VermittlungsProtokoll, getSubnetForIp("
                        + ip + "," + mask + ")");
        int[] a1, m;
        int[] res = new int[4];
        StringTokenizer tokenizer;

        tokenizer = new StringTokenizer(ip, ".");
        a1 = new int[4];
        for (int i = 0; i < a1.length && tokenizer.hasMoreTokens(); i++) {
            a1[i] = Integer.parseInt(tokenizer.nextToken());
        }
        tokenizer = new StringTokenizer(mask, ".");
        m = new int[4];
        for (int i = 0; i < m.length && tokenizer.hasMoreTokens(); i++) {
            m[i] = Integer.parseInt(tokenizer.nextToken());
        }

        for (int i = 0; i < 4; i++) {
            res[i] = a1[i] & m[i];
        }

        return res[0] + "." + res[1] + "." + res[2] + "." + res[3];
    }

    public boolean isLocalAddress(String ip) {
        if (gleichesRechnernetz(ip, "127.0.0.0", "255.0.0.0")) {
            return true;
        }

        InternetKnoten knoten = (InternetKnoten) holeSystemSoftware().getKnoten();
        for (NetzwerkInterface nic : knoten.getNetzwerkInterfaces()) {
            if (ip.equals(nic.getIp())) {
                return true;
            }
        }
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
