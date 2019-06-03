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
package filius.software.www;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import filius.Main;
import filius.rahmenprogramm.ResourceUtil;

/**
 * 
 * @author Michell wird vom den Klassen FirewallWebKonfig und FirewallWebLog benutzt
 */
public abstract class WebServerPlugIn {

    private String pfad;

    public String getPfad() {
        return pfad;
    }

    public void setPfad(String pfad) {
        this.pfad = pfad;
    }

    public abstract String holeHtmlSeite(String postDaten);

    /**
     * liest eine reale Textdatei vom Format .txt ein. Diese befinden sich im Ordner /config
     */
    protected String textDateiEinlesen(String datei) throws FileNotFoundException, IOException {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass()
                + " (FirewallWebKonfig), textDateiEinlesen(" + datei + ")");
        BufferedReader test = new BufferedReader(new FileReader(ResourceUtil.getResourcePath(datei)));
        String fullFile = "";
        String input = "";
        while ((input = test.readLine()) != null) {
            fullFile += input + "\n";
        }
        return fullFile;
    }
}
