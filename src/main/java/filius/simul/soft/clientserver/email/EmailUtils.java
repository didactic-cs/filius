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
package filius.software.email;

import java.util.LinkedList;
import java.util.List;

/**
 * @author stefan
 * 
 */
public class EmailUtils {

    /**
     * Wandelt einen String in eine Liste von Adressen, jeder der durch Kommata getrennten Werte des Strings wird ein
     * eigenes Listenelement.
     */
    public static List<AddressEntry> stringToAddressEntryList(String addresses) {
        List<AddressEntry> ergebnis = new LinkedList<AddressEntry>();
        String[] argument = addresses.split(",");
        for (String address : argument) {
            if (!address.trim().isEmpty()) {
                ergebnis.add(new AddressEntry(address));
            }
        }
        return ergebnis;
    }

    /**
     * Diese Mehtode wandelt eine Liste von Adressen in einen String, die einzelnen Listenelemente durch Kommata
     * getrennt.
     */
    public static String addressEntryListToString(List<AddressEntry> addresses) {
        StringBuffer str = new StringBuffer();
        for (AddressEntry address : addresses) {
            str.append(address.toString());
            str.append(",");
        }

        if (str.length() > 1) {
            return str.substring(0, str.length() - 1);
        } else {
            return "";
        }
    }
}
