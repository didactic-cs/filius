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
package filius.software.firewall;

import java.util.Observable;
import java.util.Observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import filius.rahmenprogramm.I18n;
import filius.software.www.WebServerPlugIn;

/**
 * 
 * @author Michell wird verwendet, zu einem fuer das Logfenster des WebServers, zum anderen fuer die Anzeige ueber
 *         /log.html Daher muss die Klasse eine Referenz auf beide Anwendungen haben
 * 
 */
public class FirewallWebLog extends WebServerPlugIn implements Observer, I18n {
    private static Logger LOG = LoggerFactory.getLogger(FirewallWebLog.class);

    private StringBuffer logDaten = new StringBuffer();

    public void setFirewall(Firewall firewall) {
        firewall.hinzuBeobachter(this);
    }

    /**
     * Diese Methode muss die HTML-Seite mit den Log-Informationen zurück liefern
     */
    public String holeHtmlSeite(String postDaten) {
        LOG.debug("INVOKED (" + this.hashCode() + ") " + getClass() + " (FirewallWebLog), holehtmlSeite(" + postDaten
                + ")");
        StringBuffer logSeite = new StringBuffer();

        logSeite.append(messages.getString("sw_firewallweblog_msg1"));
        logSeite.append(messages.getString("sw_firewallweblog_msg2"));
        logSeite.append("\n\n" + logDaten + "\n\n");
        logSeite.append("\t</body>\n</html>");

        return logSeite.toString();
    }

    public void update(Observable arg0, Object arg1) {
        LOG.debug("INVOKED (" + this.hashCode() + ") " + getClass() + " (FirewallWebLog), update(" + arg0 + "," + arg1
                + ")");
        if (arg1 instanceof Object[]) {
            logDaten.append("<br /> \n").append(((Object[]) arg1)[0]);
        } else {
            logDaten.append("<br /> \n").append(arg1);
        }

    }

}
