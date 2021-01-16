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
import java.util.Vector;

import filius.Main;
import filius.hardware.NetzwerkInterface;
import filius.hardware.knoten.InternetKnoten;
import filius.rahmenprogramm.I18n;
import filius.software.Anwendung;
import filius.software.system.InternetKnotenBetriebssystem;
import filius.software.transportschicht.Segment;
import filius.software.transportschicht.TcpSegment;
import filius.software.vermittlungsschicht.IcmpPaket;
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
    // firewall ruleset
    private Vector<FirewallRule> ruleset = new Vector<FirewallRule>();

    private short defaultPolicy = FirewallRule.DROP;
    private boolean activated = true;
    private boolean dropICMP = false;
    private boolean filterSYNSegmentsOnly = true;
    private boolean filterUdp = true;

    private LinkedList<FirewallThread> firewallThreads = new LinkedList<FirewallThread>();

    /**
     * startet die Anwendung Firewall.
     */
    public void starten() {
        Main.debug.println(
                "INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass() + " (Firewall), starten()");
        super.starten();

        for (NetzwerkInterface nic : getAllNetworkInterfaces()) {
            starteFirewallThread(nic);
        }
    }

    private void starteFirewallThread(NetzwerkInterface nic) {
        FirewallThread thread = new FirewallThread(this, nic);
        thread.starten();
        firewallThreads.add(thread);
    }

    /**
     * ruft die Methoden zum ordnungsgemäßen Stoppen aller existierenden Threads auf
     */
    public void beenden() {
        Main.debug.println(
                "INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass() + " (Firewall), beenden()");
        super.beenden();

        this.beendeFirewallThread(null);
    }

    private void beendeFirewallThread(NetzwerkInterface nic) {
        for (FirewallThread thread : this.firewallThreads) {
            if (nic == null) {
                thread.beenden();
            } else if (nic == thread.getNetzwerkInterface()) {
                thread.beenden();
                break;
            }
        }
    }

    /**
     * Method to check whether IP packet is allowed.
     * 
     * @param ipPacket
     * @return whether this IP packet must be forwarded (true: forward IP packet; false: discard the IP packet)
     */
    public boolean acceptIPPacket(IpPaket ipPacket) {
        boolean accept = true;
        if (isActivated()) {
            if (ipPacket.getProtocol() == IcmpPaket.ICMP_PROTOCOL) {
                accept = checkAcceptIcmp(ipPacket);
            } else if (ipPacket.getProtocol() == IpPaket.TCP) {
                accept = checkAcceptTCP(ipPacket);
            } else if (ipPacket.getProtocol() == IpPaket.UDP) {
                accept = checkAcceptUDP(ipPacket);
            } else {
                benachrichtigeBeobachter(messages.getString("sw_firewall_msg9") + " "
                        + ((this.defaultPolicy == FirewallRule.ACCEPT) ? messages.getString("jfirewalldialog_msg33")
                                : messages.getString("jfirewalldialog_msg34")));
                accept = defaultPolicy == FirewallRule.ACCEPT;
            }
        }
        return accept;
    }

    /**
     * @param idx
     *            following function assume to be human readable ID starting from 1; --> for internal processing reduce
     *            by 1
     */
    public boolean moveUp(int idx) {
        if (idx <= ruleset.size() && idx > 1) {
            FirewallRule currRule = ruleset.get(idx - 1);
            ruleset.remove(idx - 1);
            ruleset.insertElementAt(currRule, idx - 2);
            return true;
        }
        return false;
    }

    boolean checkAcceptIcmp(IpPaket packet) {
        boolean accept = !(packet instanceof IcmpPaket && dropICMP);
        if (!accept) {
            benachrichtigeBeobachter(messages.getString("firewallthread_msg1") + " " + packet.getSender() + " -> "
                    + packet.getEmpfaenger() + " (code: " + ((IcmpPaket) packet).getIcmpCode() + ", type: "
                    + ((IcmpPaket) packet).getIcmpType() + ")");
        }
        return accept;
    }

    boolean checkAcceptTCP(IpPaket packet) {
        boolean accept = true;
        if (packet.getProtocol() == IpPaket.TCP && isSegmentApplicable(packet)) {
            boolean foundRule = false;
            for (int i = 0; i < ruleset.size() && !foundRule; i++) {
                FirewallRule firewallRule = ruleset.get(i);
                boolean ruleToBeApplied = isProtocolApplicable(packet, firewallRule);
                ruleToBeApplied = ruleToBeApplied && isSourceAddressApplicable(packet, firewallRule);
                ruleToBeApplied = ruleToBeApplied && isDestAddressApplicable(packet, firewallRule);
                ruleToBeApplied = ruleToBeApplied && isPortApplicable(packet, firewallRule);

                if (ruleToBeApplied) { // if rule matches to current packet, then
                    notifyRuleApplication(i, firewallRule);
                    accept = firewallRule.action == FirewallRule.ACCEPT;
                    foundRule = true;
                }
            }
            if (!foundRule) {
                accept = defaultPolicy == FirewallRule.ACCEPT;
            }
        }
        return accept;
    }

    private void notifyRuleApplication(int i, FirewallRule firewallRule) {
        benachrichtigeBeobachter(messages.getString("sw_firewall_msg8") + " #" + (i + 1) + " ("
                + firewallRule.toString(getAllNetworkInterfaces()) + ")  -> "
                + ((firewallRule.action == FirewallRule.ACCEPT) ? messages.getString("jfirewalldialog_msg33")
                        : messages.getString("jfirewalldialog_msg34")));
    }

    private boolean isSegmentApplicable(IpPaket packet) {
        boolean isApplicable = false;
        if (packet.getProtocol() == IpPaket.TCP) {
            TcpSegment segment = (TcpSegment) packet.getSegment();
            isApplicable = !filterSYNSegmentsOnly || segment.isSyn() && !segment.isAck();
        } else if (packet.getProtocol() == IpPaket.UDP) {
            isApplicable = filterUdp;
        }
        return isApplicable;
    }

    boolean checkAcceptUDP(IpPaket packet) {
        boolean accept = true;
        if (packet.getProtocol() == IpPaket.UDP && isSegmentApplicable(packet)) {
            boolean foundRule = false;
            for (int i = 0; i < ruleset.size(); i++) {
                FirewallRule firewallRule = ruleset.get(i);
                boolean ruleToBeApplied = isProtocolApplicable(packet, firewallRule);
                ruleToBeApplied = ruleToBeApplied && isSourceAddressApplicable(packet, firewallRule);
                ruleToBeApplied = ruleToBeApplied && isDestAddressApplicable(packet, firewallRule);
                ruleToBeApplied = ruleToBeApplied && isPortApplicable(packet, firewallRule);

                if (ruleToBeApplied) { // if rule matches to current packet, then
                    notifyRuleApplication(i, firewallRule);
                    accept = firewallRule.action == FirewallRule.ACCEPT;
                    foundRule = true;
                }
            }
            if (!foundRule) {
                accept = defaultPolicy == FirewallRule.ACCEPT;
            }
        }
        return accept;
    }

    private boolean isPortApplicable(IpPaket packet, FirewallRule firewallRule) {
        return firewallRule.port == FirewallRule.ALL_PORTS
                || (((Segment) packet.getSegment()).getZielPort() == firewallRule.port
                        || ((Segment) packet.getSegment()).getQuellPort() == firewallRule.port);
    }

    private boolean isProtocolApplicable(IpPaket packet, FirewallRule firewallRule) {
        return firewallRule.protocol == FirewallRule.ALL_PROTOCOLS
                || (packet.getProtocol() == (int) firewallRule.protocol);
    }

    private boolean isDestAddressApplicable(IpPaket packet, FirewallRule firewallRule) {
        return firewallRule.destIP.isEmpty() || VermittlungsProtokoll.gleichesRechnernetz(packet.getEmpfaenger(),
                firewallRule.destIP, firewallRule.destMask);
    }

    private boolean isSourceAddressApplicable(IpPaket packet, FirewallRule firewallRule) {
        boolean ruleToBeApplied = false;
        if (firewallRule.srcIP.isEmpty()) {
            ruleToBeApplied = true;
        } else {
            if (firewallRule.srcIP.equals(FirewallRule.SAME_NETWORK)) {
                for (NetzwerkInterface iface : ((InternetKnoten) getSystemSoftware().getKnoten())
                        .getNetzwerkInterfaces()) {
                    if (VermittlungsProtokoll.gleichesRechnernetz(packet.getSender(), iface.getIp(),
                            iface.getSubnetzMaske())) {
                        ruleToBeApplied = true;
                        break;
                    }
                }
            } else {
                ruleToBeApplied = VermittlungsProtokoll.gleichesRechnernetz(packet.getSender(), firewallRule.srcIP,
                        firewallRule.srcMask);
            }
        }
        return ruleToBeApplied;
    }

    /**
     * @param idx
     *            following function assume to be human readable ID starting from 1; --> for internal processing reduce
     *            by 1
     */
    public boolean moveDown(int idx) {
        if (idx >= 0 && idx < ruleset.size()) {
            FirewallRule currRule = ruleset.get(idx - 1);
            ruleset.remove(idx - 1);
            ruleset.insertElementAt(currRule, idx);
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

    public boolean updateRule(int idx, FirewallRule rule) {
        if (idx >= 0 && idx < ruleset.size()) {
            ruleset.set(idx, rule);
        }
        return true;
    }

    /**
     * entfernt eine Regel aus dem Regelkatalog
     */
    public void deleteRule(int idx) {
        Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
                + " (Firewall), entferneRegel(" + idx + ")");

        if (idx >= 0 && idx < ruleset.size()) {
            ruleset.remove(idx);
        }
    }

    /**
     * Methode fuer den Zugriff auf das Betriebssystem, auf dem diese Anwendung laeuft.
     * 
     * @param bs
     */
    public void setSystemSoftware(InternetKnotenBetriebssystem bs) {
        super.setSystemSoftware(bs);
    }

    public Vector<FirewallRule> getRuleset() {
        return this.ruleset;
    }

    public void setRuleset(Vector<FirewallRule> rules) {
        this.ruleset = rules;
    }

    public void setFilterUdp(boolean filterUdp) {
        this.filterUdp = filterUdp;
    }

    public boolean getFilterUdp() {
        return filterUdp;
    }

    private List<NetzwerkInterface> getAllNetworkInterfaces() {
        InternetKnoten host = (InternetKnoten) this.getSystemSoftware().getKnoten();
        return host.getNetzwerkInterfaces();
    }

    public void setDefaultPolicy(short defPol) {
        defaultPolicy = defPol;
    }

    public short getDefaultPolicy() {
        return defaultPolicy;
    }

    public void setDropICMP(boolean selState) {
        dropICMP = selState;
    }

    public boolean getDropICMP() {
        return dropICMP;
    }

    /**
     * @deprecated use {@link #setFilterSYNSegmentsOnly(boolean)}; deprecated since 1.10.5; to ensure downward
     *             compatibility this method is used to set the new attribute when old project files are loaded.
     */
    @Deprecated
    public void setAllowRelatedPackets(boolean selState) {
        filterSYNSegmentsOnly = selState;
    }

    public void setFilterSYNSegmentsOnly(boolean selState) {
        filterSYNSegmentsOnly = selState;
    }

    public boolean getFilterSYNSegmentsOnly() {
        return filterSYNSegmentsOnly;
    }

    public void setActivated(boolean selState) {
        activated = selState;
    }

    public boolean isActivated() {
        return activated;
    }
}
