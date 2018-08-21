/*
 ** This file is part of Filius, a network construction and simulation software.
 ** 
 ** Originally created at the University of Siegen, Institute "Didactics of
 ** Informatics and E-Learning" by a students' project group:
 **     members (2006-2007): 
 **         AndrÃ© Asschoff, Johannes Bade, Carsten Dittich, Thomas Gerding,
 **         Nadja HaÃŸler, Ernst Johannes Klebert, Michell Weyer
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

import java.io.Serializable;

/**
 * @author stefan
 * 
 */
public class AddressEntry implements Serializable {
    private String name;
    private String mailAddress;

    public AddressEntry() {}

    public AddressEntry(String entry) {
        initFromString(entry);
    }

    private void initFromString(String entry) {
        String trimmedEntry = entry.trim();
        int addressStartPos = trimmedEntry.indexOf("<");
        int addressEndPos = trimmedEntry.indexOf(">");
        if (addressStartPos >= 0 && addressEndPos > addressStartPos) {
            mailAddress = trimmedEntry.substring(addressStartPos + 1, addressEndPos).trim();
            if (addressStartPos > 0) {
                name = trimmedEntry.substring(0, addressStartPos).trim();
            }
        } else if (!trimmedEntry.contains("<") && !trimmedEntry.contains(">") && trimmedEntry.contains("@")) {
            mailAddress = trimmedEntry;
        } else {
            name = trimmedEntry;
        }
    }

    public AddressEntry(Object address) {
        if (address instanceof String) {
            this.initFromString((String) address);
        } else if (address instanceof AddressEntry) {
            AddressEntry addressEntry = (AddressEntry) address;
            this.name = addressEntry.name;
            this.mailAddress = addressEntry.mailAddress;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMailAddress() {
        return mailAddress;
    }

    public void setMailAddress(String mailAddress) {
        this.mailAddress = mailAddress;
    }

    @Override
    public String toString() {
        if (name == null && mailAddress == null)
            return null;
        else if (name == null)
            return "<" + mailAddress + ">";
        else if (mailAddress == null)
            return name;
        else
            return name + " <" + mailAddress + ">";
    }
}
