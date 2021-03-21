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
package filius.rahmenprogramm;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.UIManager;

import filius.Main;

/**
 * 
 * Dient dazu Eingaben auf Richtigkeit zu Pruefen. Dazu stehen verschiedene Pattern zur Verfuegung, z.B. fuer
 * IP-Adressen oder Klassen-Namen. Mit diesen Pattern wird die Funktion isGueltig aufgerufen, die den String auf ein
 * Muster prueft.
 * 
 * @author Johannes Bade
 * 
 */
public class EntryValidator implements I18n {

    // NOTE: include *.*.*.0 to be able to still use this pattern for routing
    // table configuration as network identifier
	static String re_0_255 = "(0{0,2}[0-9]|0{0,1}[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])";  // 0 to 255, with at most three digits (i.e. 007 is accepted)
	
    public static final Pattern musterIpAdresse = Pattern.compile("^(" + re_0_255 + "\\.){3}" + re_0_255 + "$");
    public static final Pattern musterIpAdresseAuchLeer = Pattern.compile("^((" + re_0_255 + "\\.){3}" + re_0_255 + "){0,1}$");
    public static final Pattern musterIpAdresseOderLocalhost = Pattern.compile("^((" + re_0_255 + "\\.){3}" + re_0_255 + "|localhost)$");
    
    static String re_1x10x0   = "(255|254|252|248|240|224|192|128|0)";  // bytes starting with 1s and finishing with 0s 
 
    public static final Pattern musterSubNetz = Pattern.compile("^((255\\.){3}" + re_1x10x0 + "|(255\\.){2}" + re_1x10x0 + "\\.0{1,3}|" +
                                                                   "255\\." + re_1x10x0 + "(\\.0{1,3}){2}|" + re_1x10x0 + "(\\.0{1,3}){3})$");    				
    public static final Pattern musterSubNetzAuchLeer = Pattern.compile("^((255\\.){3}" + re_1x10x0 + "|(255\\.){2}" + re_1x10x0 + "\\.0{1,3}|" +
                                                                        "255\\." + re_1x10x0 + "(\\.0{1,3}){2}|" + re_1x10x0 + "(\\.0{1,3}){3}){0,1}$");    		
    
    private static final String rawMailAddress = "[a-zA-Z0-9]([\\-_\\.]{0,1}[a-zA-Z0-9])*@[a-zA-Z0-9]([\\-_\\.]{0,1}[a-zA-Z0-9])*\\.{0,1}";
    public static final Pattern musterEmailAdresse = Pattern.compile("^[a-zA-Z0-9 \\-_\\.]* <" + rawMailAddress + ">"
            + "|" + rawMailAddress + "$");
    public static final Pattern musterKlassenName = Pattern.compile("[A-Z]([a-zA-Z]{2,})?");
    public static final Pattern musterPort = Pattern
            .compile("([1-9]|[1-9][0-9]{1,3}|[1-5][0-9]{1,4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])");
    public static final Pattern musterPortAuchLeer = Pattern
            .compile("(^$|[1-9]|[1-9][0-9]{1,3}|[1-5][0-9]{1,4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])");
    public static final Pattern musterKeineLeerzeichen = Pattern.compile("[^\\s]*");
    public static final Pattern musterEmailBenutzername = Pattern.compile("([a-zA-Z0-9]|\\.|\\_|\\-)*");
    public static final Pattern musterMindEinZeichen = Pattern.compile("(.){1,}");
    public static final Pattern musterNurZahlen = Pattern.compile("\\d");
    public static final Pattern musterDomain = Pattern.compile("^([a-zA-Z][a-zA-Z0-9\\-_]*(\\.[a-zA-Z0-9][a-zA-Z0-9\\-_]*)*){0,1}\\.{0,1}$");
    public static final Pattern musterSubnetBinary = Pattern.compile("^11*0*$");
    public static final Pattern musterMacAddress = Pattern.compile("^[0-9a-fA-f]{2}(:[0-9a-fA-f]{2}){5}$");

    public static final Color wrongTextColor = new Color(128, 0, 0);
    public static final Color rightTextColor = UIManager.getColor("TextField.foreground");
    public static final Color wrongBGColor = new Color(255, 230, 230);
    public static final Color rightBGColor = UIManager.getColor("TextField.background");

    /**
     * Die Funktion isValid bekommt einen String und ein Muster (Vorzugsweise ein Muster, dass in der Klasse
     * EingabenUeberpruefung vorgegeben ist) Moeglicher Aufruf: isGueltig(ipAdresse,
     * EingabeUeberpruefung.musterIpAdresse)
     * 
     * @param zuPruefen
     * @param muster
     * @return
     */
    public static boolean isValid(String zuPruefen, Pattern muster) {
        Main.debug.println("INVOKED (EingabenUeberpruefung), isGueltig(" + zuPruefen + "," + muster + ")");
        Matcher m = muster.matcher(zuPruefen);
        return m.matches();
    }

    public static boolean isValidSubnetmask(String subnet) {
        Main.debug.println("INVOKED (EingabenUeberpruefung), isValidSubnetmask(" + subnet + ")");
        String[] token = subnet.split("\\.");
        String binary = "";
        Main.debug.println("DEBUG (EingabenUeberpruefung), '" + token + "', length=" + token.length);
        if (token.length != 4)
            return false;
        try {
            for (int i = 0; i < token.length; i++) {
                String currBin = Integer.toBinaryString(Integer.parseInt(token[i]));
                while (currBin.length() < 8)
                    currBin = "0" + currBin;
                binary += currBin;
                Main.debug.println("DEBUG (EingabenUeberpruefung), '" + token[i] + "' ~~> binary (" + i + ") = '"
                        + binary + "'");
            }
        } catch (Exception e) {
            return false;
        }
        if (binary.length() == 32 && isValid(binary, EntryValidator.musterSubnetBinary))
            return true;
        return false;
    }

}
