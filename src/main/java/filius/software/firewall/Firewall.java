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

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import filius.Main;
import filius.hardware.NetzwerkInterface;
import filius.hardware.knoten.InternetKnoten;
import filius.rahmenprogramm.I18n;
import filius.software.Anwendung;
import filius.software.system.InternetKnotenBetriebssystem;
import filius.software.transportschicht.Segment;
import filius.software.transportschicht.TcpSegment;
import filius.software.transportschicht.UdpSegment;
import filius.software.vermittlungsschicht.IPAddress;
import filius.software.vermittlungsschicht.IpPaket;
import filius.software.vermittlungsschicht.VermittlungsProtokoll;

/**
 * Die Firewall kann in zwei verschiedenen Modi betrieben werden.
 * <p>
 * Als <b>Personal Firewall</b> werden lediglich Port-Regeln ausgewertet. Eine Port-Regel spezifiziert zugelassene
 * TCP-/UDP-Ports und ob diese nur von IP-Adressen im lokalen Rechnernetz oder global kontaktiert werden koennen.
 * </p>
 * <p>
 * Wenn die Firewall in einem <b>Gateway</b> betrieben wird, gibt es vier verschiedene Regeltypen. Alle Regeln
 * spezifizieren - im Gegensatz zum Betrieb als Personal Firewall - Dateneinheiten, die nicht zugelassen werden.
 * Geprueft werden:
 * <ol>
 * <li>Sender-IP-Adresse</li>
 * <li>Absender-IP-Adresse</li>
 * <li>TCP-/UDP-Port</li>
 * <li>ACK(=0)+SYN(=1)-Bit der TCP-Segmente (indiziert Initialisierung des Verbindungsaufbaus)</li>
 * </ol>
 */
public class Firewall extends Anwendung implements I18n {

    public static int PERSONAL = 1, GATEWAY = 2;

    // only for internal use necessary, so language is irrelevant!
    public static String ABSENDER_FILTER = "Quelle", EMPFAENGER_FILTER = "Ziel";

    // firewall ruleset
    private Vector<FirewallRule> ruleset;

    private short defaultPolicy = FirewallRule.DROP;
    private boolean verbindungsaufbauAblehnen = false;
    private boolean activated = true;
    private boolean doDropICMP = false;
    private boolean allowRelatedPackets = true;

    /**
     * Das Verhalten der Firewall ist abhaengig davon, ob sie als Personal Firewall oder als Gateway benutzt wird.
     */
    private int modus = PERSONAL;
    private LinkedList<FirewallThread> threads = new LinkedList<FirewallThread>();

    private Vector<Integer> inactiveNics = new Vector<Integer>();

    /**
     * Konstruktor initialisiert Listen mit Regeln für die Firewall. Außerdem setzt ein Firewall-Beobachter Nachrichten
     * ins Logfenster
     */
    public Firewall() {
        super();
        Main.debug.println("INVOKED-2 (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
                + " (Firewall), constr: Firewall()");

        defaultPolicy = FirewallRule.DROP;
        ruleset = new Vector<FirewallRule>();
    }

    /**
     * startet die Anwendung Firewall.
     */
    public void starten() {
        if (this.holeNetzwerkInterfaces() != null) {
            Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
                    + " (Firewall), starten()");
            super.starten();

            for (NetzwerkInterface nic : this.holeNetzwerkInterfaces()) {
                this.starteFirewallThread(nic);
            }
        }
    }

    private void starteFirewallThread(NetzwerkInterface nic) {
        FirewallThread thread = new FirewallThread(this, nic);
        thread.starten();
        this.threads.add(thread);
    }

    /**
     * ruft die Methoden zum ordnungsgemäßen Stoppen aller existierenden Threads auf
     */
    public void beenden() {
        Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
                + " (Firewall), beenden()");
        super.beenden();

        this.beendeFirewallThread(null);
    }

    private void beendeFirewallThread(NetzwerkInterface nic) {
        for (FirewallThread thread : this.threads) {
            if (nic == null) {
                thread.beenden();
            } else if (nic == thread.getNetzwerkInterface()) {
                thread.beenden();
                break;
            }
        }
    }

    /**
     * Method to check whether IP packet is allowed; other packets (like ICMP) have to be evluated in another place
     * 
     * @param ipPacket
     */
    public boolean allowedIPpacket(IpPaket ipPacket) {
        Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
                + " (Firewall), allowedIPpacket(" + ipPacket + ")");
        if (this.isActivated()) {

            if (this.getAllowRelatedPackets() && ipPacket.getProtocol() == IpPaket.TCP) {
                // SYN-ACK or ACK only -> accept
                boolean isSyn = ((TcpSegment) ipPacket.getSegment()).isSyn();
                boolean isAck = ((TcpSegment) ipPacket.getSegment()).isAck();
                if (!isSyn || isAck) {
                    return true;
                }
            }
            if (modus == PERSONAL && ipPacket.getSegment() instanceof UdpSegment) {
                return true;
            }
            boolean ruleMatch = true;
            for (int i = 0; i < ruleset.size(); i++) {
                ruleMatch = true;
                if (!ruleset.get(i).srcIP.isEmpty()) {
                    if (ruleset.get(i).srcIP.equals(FirewallRule.SAME_NETWORK)) {
                        ListIterator<NetzwerkInterface> it = ((InternetKnoten) getSystemSoftware().getKnoten())
                                .getNetzwerkInterfaces().listIterator();
                        boolean foundNIC = false;
                        while (it.hasNext() && !foundNIC) {
                            NetzwerkInterface iface = it.next();
                            if (VermittlungsProtokoll.gleichesRechnernetz(ipPacket.getSender(), iface.getIp(),
                                    iface.getSubnetzMaske())) {
                                foundNIC = true; // found NIC with IP address of
                                                 // same network
                            }
                        }
                        ruleMatch = ruleMatch && foundNIC;
                    } else
                        ruleMatch = ruleMatch
                                && VermittlungsProtokoll.gleichesRechnernetz(ipPacket.getSender(),
                                        ruleset.get(i).srcIP, ruleset.get(i).srcMask);
                }
                if (!ruleset.get(i).destIP.isEmpty()) {
                    ruleMatch = ruleMatch
                            && VermittlungsProtokoll.gleichesRechnernetz(ipPacket.getEmpfaenger(),
                                    ruleset.get(i).destIP, ruleset.get(i).destMask);
                }
                if (ruleset.get(i).protocol != FirewallRule.ALL_PROTOCOLS) {
                    ruleMatch = ruleMatch && (ipPacket.getProtocol() == (int) ruleset.get(i).protocol);
                }
                if (ruleset.get(i).port != FirewallRule.ALL_PORTS) {
                    ruleMatch = ruleMatch
                            && (((Segment) ipPacket.getSegment()).getZielPort() == ruleset.get(i).port || ((Segment) ipPacket
                                    .getSegment()).getQuellPort() == ruleset.get(i).port);
                }

                if (ruleMatch) { // if rule matches to current packet, then
                                 // return true for ACCEPT target, else false
                    benachrichtigeBeobachter(messages.getString("sw_firewall_msg8")
                            + " #"
                            + (i + 1)
                            + " ("
                            + ruleset.get(i).toString(holeNetzwerkInterfaces())
                            + ")  -> "
                            + ((ruleset.get(i).action == FirewallRule.ACCEPT) ? messages
                                    .getString("jfirewalldialog_msg33") : messages.getString("jfirewalldialog_msg34")));
                    return (ruleset.get(i).action == FirewallRule.ACCEPT);
                }
            }
            // return true for defaultPolicy ACCEPT, false otherwise (i.e. in
            // case of DROP policy)
            benachrichtigeBeobachter(messages.getString("sw_firewall_msg9")
                    + " "
                    + ((this.defaultPolicy == FirewallRule.ACCEPT) ? messages.getString("jfirewalldialog_msg33")
                            : messages.getString("jfirewalldialog_msg34")));
            return (this.defaultPolicy == FirewallRule.ACCEPT);
        } else {
            return true;
        }
    }

    // following function assume ID to be human readable ID starting from 1;
    // --> for internal processing reduce by 1
    public boolean moveUp(int id) {
        if (id <= ruleset.size() && id > 1) {
            FirewallRule currRule = ruleset.get(id - 1);
            ruleset.remove(id - 1);
            ruleset.insertElementAt(currRule, id - 2);
            return true;
        }
        return false;
    }

    public boolean moveDown(int id) {
        if (id >= 0 && id < ruleset.size()) {
            FirewallRule currRule = ruleset.get(id - 1);
            ruleset.remove(id - 1);
            ruleset.insertElementAt(currRule, id);
            return true;
        }
        return false;
    }

    public void addRule() {
        ruleset.add(new FirewallRule());
    }

    public void addRule(FirewallRule rule) {
        ruleset.add(rule);
    }

    public void delRule(int id) {
        if (id >= 0 && id <= ruleset.size())
            ruleset.remove(id - 1);
    }

    /*
     * Store changed value. Invoked by cell editor from table in JFirewallDialog.
     * 
     * @param row index of row, i.e., id of ruleset vector (starting with 0)
     * 
     * @param col column of table that was changed
     * 
     * @param value new value (correctness is assumed / evaluated before!)
     */
    public String changeSingleEntry(int row, int col, String value) {
        if (row > ruleset.size() - 1)
            return "";
        if (col == 0) {// ID
            // shouldn't be possible, thus, an error obviously occurred...
        } else if (col == 1) {// srcIP
            ruleset.get(row).srcIP = value;
            if (StringUtils.isNoneBlank(value) && StringUtils.isBlank(ruleset.get(row).srcMask)) {
                ruleset.get(row).srcMask = "255.255.255.255";
            }
        } else if (col == 2) {// srcMask
            ruleset.get(row).srcMask = value;
        } else if (col == 3) {// destIP
            ruleset.get(row).destIP = value;
            if (StringUtils.isNoneBlank(value) && StringUtils.isBlank(ruleset.get(row).destMask)) {
                ruleset.get(row).destMask = "255.255.255.255";
            }
        } else if (col == 4) {// destMask
            ruleset.get(row).destMask = value;
        } else if (col == 5) {// protocol
            if (value.equals("TCP"))
                ruleset.get(row).protocol = FirewallRule.TCP;
            else if (value.equals("UDP"))
                ruleset.get(row).protocol = FirewallRule.UDP;
            else if (value.equals("ICMP"))
                ruleset.get(row).protocol = FirewallRule.ICMP;
            else if (value.equals("*")) {
                ruleset.get(row).protocol = FirewallRule.ALL_PROTOCOLS;
                return "";
            }
        } else if (col == 6) {// port
            try {
                int portInt = Integer.parseInt(value);
                ruleset.get(row).port = portInt;
            } catch (Exception e) {}
        } else if (col == 7) {// action
            if (value.equals(messages.getString("jfirewalldialog_msg33")))
                ruleset.get(row).action = FirewallRule.ACCEPT;
            else
                ruleset.get(row).action = FirewallRule.DROP;
        }
        benachrichtigeBeobachter();
        return value;
    }

    /**
     * fuegt eine IP-Adressenregel in die Liste mit IP-Adressen ein
     */
    public void eintragHinzufuegen(String von, String bis, String typ) {
        Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
                + " (Firewall), eintragHinzufuegen(" + von + "," + bis + "," + typ + ")");
        if (!IPAddress.verifyAddress(von) || !(StringUtils.isBlank(bis) || IPAddress.verifyAddress(bis))) {
            return;
        }

        FirewallRule newRule;
        if (bis.equals("")) {
            bis = " ";
        }

        if (typ.equals(ABSENDER_FILTER)) {
            // ---------
            // write rule equivalent/similar to former effects
            newRule = new FirewallRule(von, "", "", "", FirewallRule.ALL_PORTS, FirewallRule.ALL_PROTOCOLS,
                    FirewallRule.ACCEPT);
            if (bis.equals(" ")) // not set --> assume single host
                newRule.srcMask = "255.255.255.255";
            else
                newRule.srcMask = "255.255.255.0"; // assume class C net
                                                   // (default IP range)
            ruleset.add(newRule);
            // ---------
            // Main.debug.println("Erfolgreich hinzugefügt: "+tmp );
            benachrichtigeBeobachter(messages.getString("sw_firewall_msg4") + von + " - " + bis);
        } else if (typ.equals(EMPFAENGER_FILTER)) {
            // ---------
            // write rule equivalent/similar to former effects
            newRule = new FirewallRule("", "", von, "", FirewallRule.ALL_PORTS, FirewallRule.ALL_PROTOCOLS,
                    FirewallRule.ACCEPT);
            if (bis.equals(" ")) // not set --> assume single host
                newRule.destMask = "255.255.255.255";
            else
                newRule.destMask = "255.255.255.0"; // assume class C net
                                                    // (default IP range)
            ruleset.add(newRule);
            // ---------
            // Main.debug.println("Erfolgreich hinzugefügt: "+tmp );
            benachrichtigeBeobachter(messages.getString("sw_firewall_msg4") + von + " - " + bis);
        }
    }

    /**
     * fuegt eine Portregel in die Liste mit Portregeln ein
     */
    public void eintragHinzufuegenPort(String port) {
        Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
                + " (Firewall), eintragHinzufuegenPort(" + port + ")");
        eintragHinzufuegenPort(port, Boolean.FALSE);
    }

    public void eintragHinzufuegenPort(String port, boolean unterscheideNetzwerk) {
        Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
                + " (Firewall), eintragHinzufuegenPort(" + port + "," + unterscheideNetzwerk + ")");

        if (port != null) {
            // ---------
            // insert comparable rule for new scheme
            int portNum = -1;
            try {
                portNum = Integer.parseInt(port);
                FirewallRule newRule = new FirewallRule("", "", "", "", portNum, FirewallRule.ALL_PROTOCOLS,
                        FirewallRule.ACCEPT);
                if (unterscheideNetzwerk) { // unterscheideNetzwerk = TRUE, wenn
                                            // "Alle Absender" erlaubt => FALSE,
                                            // wenn "nur Intranet"
                    newRule.srcIP = FirewallRule.SAME_NETWORK;
                }
                ruleset.add(newRule);
            } catch (Exception e) {}
            // ---------

            benachrichtigeBeobachter(messages.getString("sw_firewall_msg5") + port);
        }
    }

    /**
     * entfernt eine Portregel aus dem Regelkatalog
     */
    public void entferneRegelPort(int nummer) {
        Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
                + " (Firewall), entferneRegel(" + nummer + ")");

        if (nummer < ruleset.size())
            ruleset.remove(nummer); // CAVE: only works for personal firewall!
                                    // Otherwise numbering may contain offsets
                                    // due to one-for-all set (port, source,
                                    // dest) in new format.
    }

    /**
     * Methode fuer den Zugriff auf das Betriebssystem, auf dem diese Anwendung laeuft.
     * 
     * @param bs
     */
    public void setSystemSoftware(InternetKnotenBetriebssystem bs) {
        super.setSystemSoftware(bs);
    }

    // getter & setter:
    public boolean getRejectIncomingConnections() {
        return verbindungsaufbauAblehnen;
    }

    public void setRejectIncomingConnections(boolean active) {
        this.verbindungsaufbauAblehnen = active;
    }

    public Vector<FirewallRule> getRuleset() {
        return this.ruleset;
    }

    public void setRuleset(Vector<FirewallRule> rules) {
        this.ruleset = rules;
    }

    public void setModus(int modus) {
        this.modus = modus;
    }

    public int getModus() {
        return modus;
    }

    public void setzeNetzwerkInterfaces(LinkedList<NetzwerkInterface> netzwerkInterfaces) {
        List<NetzwerkInterface> allNics = this.getAllNetworkInterfaces();

        this.inactiveNics.removeAllElements();
        for (NetzwerkInterface nic : allNics) {
            if (netzwerkInterfaces.indexOf(nic) == -1) {
                this.inactiveNics.add(new Integer(allNics.indexOf(nic)));
            }
        }
    }

    public List<NetzwerkInterface> holeNetzwerkInterfaces() {
        List<NetzwerkInterface> allNics = this.getAllNetworkInterfaces();
        List<NetzwerkInterface> result = new LinkedList<NetzwerkInterface>();

        for (NetzwerkInterface nic : allNics) {
            try {
                if (!this.inactiveNics.contains(new Integer(allNics.indexOf(nic)))) {
                    result.add(nic);
                }
            } catch (IndexOutOfBoundsException e) {}
        }
        return result;
    }

    private List<NetzwerkInterface> getAllNetworkInterfaces() {
        InternetKnoten host = (InternetKnoten) this.getSystemSoftware().getKnoten();

        return host.getNetzwerkInterfaces();
    }

    public boolean hinzuNetzwerkInterface(NetzwerkInterface nic) {
        int idx = this.getAllNetworkInterfaces().indexOf(nic);
        if (this.inactiveNics.contains(new Integer(idx))) {
            this.inactiveNics.remove(new Integer(idx));

            if (this.running) {
                this.starteFirewallThread(nic);
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean entferneNetzwerkInterface(NetzwerkInterface nic) {
        int idx = this.getAllNetworkInterfaces().indexOf(nic);
        if (!this.inactiveNics.contains(new Integer(idx))) {
            this.inactiveNics.add(new Integer(idx));

            if (this.running) {
                this.beendeFirewallThread(nic);
            }
            return true;
        } else {
            return false;
        }
    }

    public Vector<Integer> getInactiveNics() {
        return inactiveNics;
    }

    public void setInactiveNics(Vector<Integer> inactiveNics) {
        this.inactiveNics = inactiveNics;
    }

    /*
     * change default policy
     * 
     * @param langPolicy new default policy; provided as String in the language currently chosen
     */
    public void setDefaultPolicyString(String langPolicy) {
        if (langPolicy.equals(messages.getString("jfirewalldialog_msg33")))
            defaultPolicy = FirewallRule.ACCEPT;
        else
            defaultPolicy = FirewallRule.DROP;
    }

    /*
     * change default policy
     * 
     * @param defPol new default policy; provided as 'short' value as defined in FirewallRule
     */
    public void setDefaultPolicy(short defPol) {
        defaultPolicy = defPol;
    }

    public short getDefaultPolicy() {
        return defaultPolicy;
    }

    public String getDefaultPolicyString() {
        if (defaultPolicy == FirewallRule.ACCEPT)
            return messages.getString("jfirewalldialog_msg33");
        else if (defaultPolicy == FirewallRule.DROP)
            return messages.getString("jfirewalldialog_msg34");
        else
            return "";
    }

    public void setDropICMP(boolean selState) {
        doDropICMP = selState;
    }

    public boolean getDropICMP() {
        return doDropICMP;
    }

    public void setAllowRelatedPackets(boolean selState) {
        allowRelatedPackets = selState;
    }

    public boolean getAllowRelatedPackets() {
        return allowRelatedPackets;
    }

    public void setActivated(boolean selState) {
        activated = selState;
    }

    public boolean isActivated() {
        return activated;
    }
}
