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
package filius.software.lokal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import filius.exception.TimeOutException;
import filius.rahmenprogramm.EingabenUeberpruefung;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.Information;
import filius.rahmenprogramm.nachrichten.Lauscher;
import filius.software.clientserver.ClientAnwendung;
import filius.software.dns.DNSNachricht;
import filius.software.dns.Resolver;
import filius.software.dns.ResourceRecord;
import filius.software.netzzugangsschicht.Ethernet;
import filius.software.system.Betriebssystem;
import filius.software.system.Datei;
import filius.software.system.Dateisystem;
import filius.software.system.InternetKnotenBetriebssystem;
import filius.software.transportschicht.ServerSocket;
import filius.software.transportschicht.Socket;
import filius.software.transportschicht.SocketSchnittstelle;
import filius.software.transportschicht.TransportProtokoll;
import filius.software.vermittlungsschicht.ARP;
import filius.software.vermittlungsschicht.ArpPaket;
import filius.software.vermittlungsschicht.IP;
import filius.software.vermittlungsschicht.IcmpPaket;
import filius.software.vermittlungsschicht.Route;
import filius.software.vermittlungsschicht.RouteNotFoundException;
import filius.software.vermittlungsschicht.VermittlungsProtokoll;

/**
 * Diese Klasse soll eine Art Eingabeaufforderung oder Unix-Shell darstellen, in der zumindest rudimentaere Befehle wie
 * dir/ls/rename etc. moeglich sein sollen. Auerdem soll hierin auch der Start von bestimmten Serveranwendungen und
 * netcat moeglich sein.
 * 
 * @author Thomas Gerding & Johannes Bade
 * 
 */
public class Terminal extends ClientAnwendung implements I18n {
    private static Logger LOG = LoggerFactory.getLogger(Terminal.class);
    private Map<String, String> usageInfo;

    // Betriebssystem betriebssystem;
    boolean abfrageVar;

    private DefaultMutableTreeNode aktuellerOrdner;
    private boolean interrupted = false;

    public void initUsageInfo() {
        if (usageInfo == null) {
            usageInfo = new HashMap<>();
            usageInfo.put("arp",
                    messages.getString("sw_terminal_arp") + "\n" + messages.getString("sw_terminal_usage_arp"));
            usageInfo.put("arpsend",
                    messages.getString("sw_terminal_arpsend") + "\n" + messages.getString("sw_terminal_usage_arpsend"));
            usageInfo.put("cat",
                    messages.getString("sw_terminal_cat") + "\n" + messages.getString("sw_terminal_usage_cat"));
            usageInfo.put("cd",
                    messages.getString("sw_terminal_cd") + "\n" + messages.getString("sw_terminal_usage_cd"));
            usageInfo.put("copy",
                    messages.getString("sw_terminal_copy") + "\n" + messages.getString("sw_terminal_usage_copy"));
            usageInfo.put("cp",
                    messages.getString("sw_terminal_copy") + "\n" + messages.getString("sw_terminal_usage_copy"));
            usageInfo.put("del",
                    messages.getString("sw_terminal_del") + "\n" + messages.getString("sw_terminal_usage_del"));
            usageInfo.put("dir",
                    messages.getString("sw_terminal_dir") + "\n" + messages.getString("sw_terminal_usage_dir"));
            usageInfo.put("help",
                    messages.getString("sw_terminal_help") + "\n" + messages.getString("sw_terminal_usage_help"));
            usageInfo.put("host",
                    messages.getString("sw_terminal_host") + "\n" + messages.getString("sw_terminal_usage_host"));
            usageInfo.put("ipconfig", messages.getString("sw_terminal_ipconfig") + "\n"
                    + messages.getString("sw_terminal_usage_ipconfig"));
            usageInfo.put("ls",
                    messages.getString("sw_terminal_dir") + "\n" + messages.getString("sw_terminal_usage_dir"));
            usageInfo.put("mkdir",
                    messages.getString("sw_terminal_mkdir") + "\n" + messages.getString("sw_terminal_usage_mkdir"));
            usageInfo.put("move",
                    messages.getString("sw_terminal_move") + "\n" + messages.getString("sw_terminal_usage_move"));
            usageInfo.put("mv",
                    messages.getString("sw_terminal_move") + "\n" + messages.getString("sw_terminal_usage_move"));
            usageInfo.put("netstat",
                    messages.getString("sw_terminal_netstat") + "\n" + messages.getString("sw_terminal_usage_netstat"));
            usageInfo.put("nslookup", messages.getString("sw_terminal_nslookup") + "\n"
                    + messages.getString("sw_terminal_usage_nslookup"));
            usageInfo.put("ping",
                    messages.getString("sw_terminal_ping") + "\n" + messages.getString("sw_terminal_usage_ping"));
            usageInfo.put("pwd",
                    messages.getString("sw_terminal_pwd") + "\n" + messages.getString("sw_terminal_usage_pwd"));
            usageInfo.put("rm",
                    messages.getString("sw_terminal_del") + "\n" + messages.getString("sw_terminal_usage_del"));
            usageInfo.put("route",
                    messages.getString("sw_terminal_route") + "\n" + messages.getString("sw_terminal_usage_route"));
            usageInfo.put("tcpdump",
                    messages.getString("sw_terminal_tcpdump") + "\n" + messages.getString("sw_terminal_usage_tcpdump"));
            usageInfo.put("touch",
                    messages.getString("sw_terminal_touch") + "\n" + messages.getString("sw_terminal_usage_touch"));
            usageInfo.put("traceroute", messages.getString("sw_terminal_traceroute") + "\n"
                    + messages.getString("sw_terminal_usage_traceroute"));
            usageInfo.put("type",
                    messages.getString("sw_terminal_cat") + "\n" + messages.getString("sw_terminal_usage_cat"));
        }
    }

    public void setSystemSoftware(InternetKnotenBetriebssystem bs) {
        super.setSystemSoftware(bs);
        this.aktuellerOrdner = getSystemSoftware().getDateisystem().getRoot();
    }

    /**
     * Diese Funktion bildet "move" bzw. "rename" ab und erlaubt es eine bestimmte Datei umzubenennen.
     * 
     * @param alterName
     *            Der bisherige Dateiname
     * @param neuerName
     *            Der gewnschte neue Dateiname
     * @return Gibt eine Meldung ueber den Erfolg oder Misserfolg des Umbenennens zurck.
     * @author Thomas Gerding & Johannes Bade
     */
    public String move(String[] args) {
        return mv(args);
    }

    public String mv(String[] args) {
        LOG.trace("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass() + " (Terminal), mv(");
        for (int i = 0; i < args.length; i++) {
            LOG.debug(i + "='" + args[i] + "' ");
        }
        LOG.debug(")");

        if (!numParams(args, 2)) {
            return usage("mv");
        }
        if (pureCopy(args)) { // positive case, everything worked fine
            this.getSystemSoftware().getDateisystem()
                    .deleteFile(filius.software.system.Dateisystem.absoluterPfad(getAktuellerOrdner())
                            + Dateisystem.FILE_SEPARATOR + args[0]);
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg35"));
            return messages.getString("sw_terminal_msg35");
        } else {
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg36"));
            return messages.getString("sw_terminal_msg36");
        } // negative case, something wrong
    }

    /**
     * delete file
     */
    public String rm(String[] args) {
        return del(args);
    }

    public String del(String[] args) {
        LOG.trace("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass() + " (Terminal), del(");
        for (int i = 0; i < args.length; i++) {
            LOG.debug(i + "='" + args[i] + "' ");
        }
        LOG.debug(")");
        if (!numParams(args, 1)) {
            return usage("del");
        }
        if (this.getSystemSoftware().getDateisystem()
                .deleteFile(filius.software.system.Dateisystem.absoluterPfad(getAktuellerOrdner())
                        + Dateisystem.FILE_SEPARATOR + args[0])) {
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg37"));
            return messages.getString("sw_terminal_msg37");
        } else {
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg38"));
            return messages.getString("sw_terminal_msg38");
        }
    }

    /**
     * Kopiert eine Datei
     * 
     * @param Parameter
     *            Array (String)
     * @return
     */
    // // common functionality for move and copy...
    private boolean pureCopy(String[] args) {
        LOG.trace("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass() + " (Terminal), pureCopy(");
        for (int i = 0; i < args.length; i++) {
            LOG.debug(i + "='" + args[i] + "' ");
        }
        LOG.debug(")");
        this.getSystemSoftware().getDateisystem().printTree();
        String srcString = args[0];
        if (srcString.length() > 0 && srcString.substring(0, 1).equals(Dateisystem.FILE_SEPARATOR)) { // 'pfad'
                                                                                                      // is
                                                                                                      // absolute
                                                                                                      // path!
            srcString = Dateisystem.evaluatePathString(srcString);
        } else {
            srcString = Dateisystem
                    .evaluatePathString(filius.software.system.Dateisystem.absoluterPfad(getAktuellerOrdner())
                            + Dateisystem.FILE_SEPARATOR + srcString);
        }
        String destString = args[1];
        if (destString.length() > 0 && destString.substring(0, 1).equals(Dateisystem.FILE_SEPARATOR)) { // 'pfad'
                                                                                                        // is
                                                                                                        // absolute
                                                                                                        // path!
            destString = Dateisystem.evaluatePathString(destString);
        } else {
            destString = Dateisystem
                    .evaluatePathString(filius.software.system.Dateisystem.absoluterPfad(getAktuellerOrdner())
                            + Dateisystem.FILE_SEPARATOR + destString);
        }
        String destDir = Dateisystem.getDirectory(destString);
        String destFile = Dateisystem.getBasename(destString);

        // LOG.debug("DEBUG: pureCopy: source '"+srcDir+"'-'"+srcFile+"', destination
        // '"+destDir+"'-'"+destFile+"'");
        Datei sfile = this.getSystemSoftware().getDateisystem().holeDatei(srcString);
        if (sfile == null)
            return false;
        Datei dfile = new Datei(destFile, sfile.getDateiTyp(), sfile.getDateiInhalt());
        return this.getSystemSoftware().getDateisystem().speicherDatei(destDir, dfile);
    }

    // individual functionality for copy only
    public String copy(String[] args) {
        return cp(args);
    }

    public String cp(String[] args) {
        LOG.trace("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass() + " (Terminal), cp(");
        for (int i = 0; i < args.length; i++) {
            LOG.debug(i + "='" + args[i] + "' ");
        }
        LOG.debug(")");
        if (!numParams(args, 2)) {
            return usage("cp");
        }
        if (pureCopy(args)) {
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg33"));
            return messages.getString("sw_terminal_msg33");
        } // positive case, everything worked fine
        else {
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg34"));
            return messages.getString("sw_terminal_msg34");
        } // negative case, something wrong
    }

    /* */
    public String ipconfig(String[] args) {
        LOG.trace("INVOKED (" + this.hashCode() + ", " + this.getId() + ") " + getClass() + " (Terminal), ipconfig(");
        for (int i = 0; i < args.length; i++) {
            LOG.debug(i + "='" + args[i] + "' ");
        }
        LOG.debug(")");
        if (!numParams(args, 0)) {
            return usage("ipconfig");
        }
        Betriebssystem bs = (Betriebssystem) getSystemSoftware();
        String ausgabe = "";

        ausgabe += messages.getString("sw_terminal_msg4") + " " + bs.primaryIPAdresse() + "\n";
        ausgabe += messages.getString("sw_terminal_msg5") + " " + bs.primarySubnetMask() + "\n";
        ausgabe += messages.getString("sw_terminal_msg6666") + " " + bs.primaryMACAddress() + "\n";
        ausgabe += messages.getString("sw_terminal_msg6") + " " + bs.getStandardGateway() + "\n";
        ausgabe += messages.getString("sw_terminal_msg27") + " " + bs.getDNSServer() + "\n";

        benachrichtigeBeobachter(ausgabe);
        return ausgabe;
    }

    /* Entspricht route print unter windows */
    public String route(String[] args) {
        LOG.trace("INVOKED (" + this.hashCode() + ", " + this.getId() + ") " + getClass() + " (Terminal), route(");
        for (int i = 0; i < args.length; i++) {
            LOG.debug(i + "='" + args[i] + "' ");
        }
        LOG.debug(")");
        if (!numParams(args, 0)) {
            return usage("route");
        }
        String ausgabe = messages.getString("sw_terminal_msg7");

        LinkedList<String[]> routingTabelle = getSystemSoftware().getWeiterleitungstabelle().holeTabelle();
        ListIterator<String[]> it = routingTabelle.listIterator();

        while (it.hasNext()) {
            String[] eintrag = (String[]) it.next();
            ausgabe += "| ";
            for (int i = 0; i < eintrag.length; i++) {
                ausgabe += eintrag[i] + stringFuellen(15 - eintrag[i].length(), " ") + " | ";
            }
            ausgabe += "\n";
        }

        benachrichtigeBeobachter(ausgabe);
        return ausgabe;
    }

    /**
     * Diese Funktion bietet Aehnliches wie "ls" oder "dir" auf der normalen Eingabeaufforderung. Es gibt eine Liste
     * aller Dateien des Rechners und deren Groesse zurueck.
     * 
     * @return Gibt die Liste der vorhandenen Dateien (und Verzeichnisse) in einem formatierten String zurueck, der
     *         direkt ausgegeben werden kann.
     * 
     * @author Thomas Gerding & Johannes Bade
     * @param Parameter
     *            Array (String)
     */
    public String ls(String[] args) {
        return dir(args);
    }

    public String dir(String[] args) {
        LOG.trace("INVOKED (" + this.hashCode() + ", " + this.getId() + ") " + getClass() + " (Terminal), dir(");
        for (int i = 0; i < args.length; i++) {
            LOG.debug(i + "='" + args[i] + "' ");
        }
        LOG.debug(")");
        if (!numParams(args, 0, 1)) {
            return usage("dir");
        }

        List<Object> liste;
        String currPath;
        if (args[0].isEmpty()) {
            liste = getSystemSoftware().getDateisystem().listeVerzeichnis(aktuellerOrdner);
            currPath = Dateisystem.absoluterPfad(aktuellerOrdner);
        } else {
            if (args[0].length() > 0 && args[0].substring(0, 1).equals(Dateisystem.FILE_SEPARATOR)) {
                liste = getSystemSoftware().getDateisystem()
                        .listeVerzeichnis(getSystemSoftware().getDateisystem().verzeichnisKnoten(args[0]));
                currPath = Dateisystem.evaluatePathString(args[0]);
            } else {
                liste = getSystemSoftware().getDateisystem()
                        .listeVerzeichnis(Dateisystem.verzeichnisKnoten(aktuellerOrdner, args[0]));
                currPath = Dateisystem.evaluatePathString(
                        Dateisystem.absoluterPfad(aktuellerOrdner) + Dateisystem.FILE_SEPARATOR + args[0]);
            }
        }

        if (liste == null || liste.size() == 0) {
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg8"));
            return messages.getString("sw_terminal_msg8");
        } else {
            StringBuffer inhalt = new StringBuffer();
            inhalt.append(messages.getString("sw_terminal_msg9") + " " + currPath + ":");

            int anzahlVerzeichnisse = 0;
            int anzahlDateien = 0;
            for (Object tmp : liste) {
                inhalt.append("\n");
                // Fall Datei:
                if (tmp instanceof Datei) {
                    anzahlDateien++;
                    Datei tmpDatei = (Datei) tmp;
                    int leerzeichen = 40 - tmpDatei.getName().length();
                    inhalt.append(tmpDatei.getName() + stringFuellen(leerzeichen, ".") + tmpDatei.holeGroesse());
                }
                // Fall Ordner:
                else {
                    anzahlVerzeichnisse++;
                    inhalt.append("[" + tmp + "]");
                }
            }
            inhalt.append("\n");
            inhalt.append(messages.getString("sw_terminal_msg10") + anzahlVerzeichnisse);
            inhalt.append(messages.getString("sw_terminal_msg11") + anzahlDateien);

            benachrichtigeBeobachter(inhalt.toString());
            return inhalt.toString();
        }
    }

    /**
     * 
     * touch
     * 
     */
    public String touch(String[] args) {
        LOG.trace("INVOKED (" + this.hashCode() + ", " + this.getId() + ") " + getClass() + " (Terminal), touch(");
        for (int i = 0; i < args.length; i++) {
            LOG.debug(i + "='" + args[i] + "' ");
        }
        LOG.debug(")");
        if (!numParams(args, 1)) {
            return usage("touch");
        }
        String ergebnis = messages.getString("sw_terminal_msg12");
        String absPath;
        if (args[0].length() > 0 && args[0].substring(0, 1).equals(Dateisystem.FILE_SEPARATOR)) {
            absPath = Dateisystem.evaluatePathString(args[0]);
        } else {
            absPath = Dateisystem.evaluatePathString(
                    Dateisystem.absoluterPfad(aktuellerOrdner) + Dateisystem.FILE_SEPARATOR + args[0]);
        }
        String filePath = Dateisystem.getDirectory(absPath);
        String dateiName = Dateisystem.getBasename(absPath);
        if (!dateiName.equals("")) {
            if (!getSystemSoftware().getDateisystem().dateiVorhanden(filePath, dateiName)) {
                getSystemSoftware().getDateisystem().speicherDatei(filePath, new Datei(dateiName, "text/txt", ""));
                ergebnis = messages.getString("sw_terminal_msg13");
            } else {
                ergebnis = messages.getString("sw_terminal_msg14");
            }
        } else {
            ergebnis = messages.getString("sw_terminal_msg15");
        }
        benachrichtigeBeobachter(ergebnis);
        return ergebnis;
    }

    /**
     * 
     * mkdir
     * 
     */
    public String mkdir(String[] args) {
        LOG.trace("INVOKED (" + this.hashCode() + ", " + this.getId() + ") " + getClass() + " (Terminal), mkdir(");
        for (int i = 0; i < args.length; i++) {
            LOG.debug(i + "='" + args[i] + "' ");
        }
        LOG.debug(")");
        if (!numParams(args, 1)) {
            return usage("mkdir");
        }
        String ergebnis = messages.getString("sw_terminal_msg16");
        String absPath;
        if (args[0].length() > 0 && args[0].substring(0, 1).equals(Dateisystem.FILE_SEPARATOR)) {
            absPath = Dateisystem.evaluatePathString(args[0]);
        } else {
            absPath = Dateisystem.evaluatePathString(
                    Dateisystem.absoluterPfad(aktuellerOrdner) + Dateisystem.FILE_SEPARATOR + args[0]);
        }
        String filePath = Dateisystem.getDirectory(absPath);
        String dateiName = Dateisystem.getBasename(absPath);
        if (!dateiName.equals("")) {
            if (!getSystemSoftware().getDateisystem().dateiVorhanden(filePath, dateiName)
                    && getSystemSoftware().getDateisystem().erstelleVerzeichnis(filePath, dateiName)) {
                ergebnis = messages.getString("sw_terminal_msg17");
            } else {
                ergebnis = messages.getString("sw_terminal_msg18");
            }
        } else {
            ergebnis = messages.getString("sw_terminal_msg19");
        }
        benachrichtigeBeobachter(ergebnis);
        return ergebnis;
    }

    /**
     * 
     * cd
     * 
     */
    public String cd(String[] args) {
        LOG.trace("INVOKED (" + this.hashCode() + ", " + this.getId() + ") " + getClass() + " (Terminal), cd(");
        for (int i = 0; i < args.length; i++) {
            LOG.debug(i + "='" + args[i] + "' ");
        }
        LOG.debug(")");
        String ergebnis = "";
        if (!numParams(args, 0, 1)) {
            return usage("cd");
        }
        if (numParams(args, 1)) {
            DefaultMutableTreeNode newDir;
            if (args[0].charAt(0) == '/') {
                newDir = getSystemSoftware().getDateisystem().changeDirectory(args[0]);
            } else {
                newDir = getSystemSoftware().getDateisystem()
                        .changeDirectory(Dateisystem.absoluterPfad(aktuellerOrdner), args[0]);
            }
            if (newDir != null) {
                aktuellerOrdner = newDir;
            } else {
                ergebnis = messages.getString("sw_terminal_msg20");
            }
        } else {
            ergebnis = Dateisystem.absoluterPfad(aktuellerOrdner);
        }

        benachrichtigeBeobachter(ergebnis);
        return ergebnis;
    }

    // Unix Tool 'pwd': print working directory
    public String pwd(String[] args) {
        LOG.trace("INVOKED (" + this.hashCode() + ", " + this.getId() + ") " + getClass() + " (Terminal), pwd(");
        for (int i = 0; i < args.length; i++) {
            LOG.debug(i + "='" + args[i] + "' ");
        }
        LOG.debug(")");
        if (!numParams(args, 0)) {
            return usage("pwd");
        }
        String ergebnis = Dateisystem.absoluterPfad(aktuellerOrdner);
        benachrichtigeBeobachter(ergebnis);
        return ergebnis;
    }

    public String netstat(String[] args) {
        TransportProtokoll transport;
        StringBuffer ergebnis = new StringBuffer();
        String protocol;

        ergebnis.append(messages.getString("sw_terminal_msg49"));
        ergebnis.append("--------------------------------------------------------------------------\n");

        transport = this.getSystemSoftware().holeTcp();
        protocol = "TCP";
        processSockets(ergebnis, transport, protocol);

        transport = this.getSystemSoftware().holeUdp();
        protocol = "UDP";
        processSockets(ergebnis, transport, protocol);

        benachrichtigeBeobachter(ergebnis);
        return ergebnis.toString();
    }

    private void processSockets(StringBuffer ergebnis, TransportProtokoll transport, String protocol) {
        for (SocketSchnittstelle socket : transport.holeAktiveSockets()) {
            for (SocketInformation info : getSocketInfo(socket)) {
                ergebnis.append(String.format("| %-7s ", protocol));
                ergebnis.append(String.format("| %15s:%-5s ", info.localHost, info.localPort));
                ergebnis.append(String.format("| %15s:%-5s ", info.remoteHost, info.remotePort));
                ergebnis.append(String.format("| %-12s |\n", info.state));
            }
        }
    }

    private class SocketInformation {
        String localHost;
        String localPort;
        String remoteHost;
        String remotePort;
        String state;
    }

    private List<SocketInformation> getSocketInfo(SocketSchnittstelle socket) {
        List<SocketInformation> infoList = new ArrayList<>();
        if (socket instanceof Socket) {
            infoList.add(getSocketInfo((Socket) socket));
        } else if (socket instanceof ServerSocket) {
            ServerSocket temp = (ServerSocket) socket;
            infoList.add(getSocketInfo(temp));
            for (String port : temp.getSockets().keySet()) {
                infoList.add(getSocketInfo(temp.getSockets().get(port)));
            }
        }
        return infoList;
    }

    private SocketInformation getSocketInfo(ServerSocket serverSocket) {
        SocketInformation info = new SocketInformation();
        info.remoteHost = "-";
        info.remotePort = "-";
        info.localHost = "0.0.0.0";
        info.localPort = String.valueOf(serverSocket.getLocalPort());
        info.state = "LISTEN";
        return info;
    }

    private SocketInformation getSocketInfo(Socket socket) {
        SocketInformation info = new SocketInformation();
        info.remoteHost = socket.holeZielIPAdresse();
        info.remotePort = String.valueOf(socket.holeZielPort());
        try {
            Route routingEntry = ((InternetKnotenBetriebssystem) this.getSystemSoftware())
                    .determineRoute(info.remoteHost);
            info.localHost = routingEntry.getInterfaceIpAddress();
        } catch (RouteNotFoundException e) {
            info.localHost = "<unknown>";
        }
        info.localPort = String.valueOf(socket.holeLokalenPort());
        info.state = socket.getStateAsString();
        return info;
    }

    /**
     * 
     * test
     * 
     */
    public String test(String[] args) {
        String ergebnis = messages.getString("sw_terminal_msg23");
        if (this.getSystemSoftware().getDateisystem().speicherDatei(aktuellerOrdner,
                new Datei("test.txt", "txt", "blaaa"))) {
            ergebnis = messages.getString("sw_terminal_msg24");
        }
        benachrichtigeBeobachter(ergebnis);
        return ergebnis;
    }

    /**
     * 
     * help command to list all available commands implemented in this terminal application
     * 
     */
    public String help(String[] args) {
        return args.length >= 1 ? usage(args[0]) : usage(null);
    }

    private String usage(String cmd) {
        initUsageInfo();
        if (usageInfo.containsKey(cmd)) {
            benachrichtigeBeobachter(usageInfo.get(cmd) + "\n");
        } else {
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg25"));
        }
        return messages.getString("sw_terminal_msg25");
    }

    public String type(String[] args) {
        return cat(args);
    }

    public String cat(String[] args) {
        StringBuilder result = new StringBuilder();
        if (args == null || args.length < 1 || args[0] == null || "".equals(args[0])) {
            usage("cat");
        } else {
            Datei file = getSystemSoftware().getDateisystem().holeDatei(this.aktuellerOrdner, args[0]);
            if (null != file) {
                result.append(file.getDateiInhalt());
            } else {
                result.append(messages.getString("sw_terminal_msg54"));
            }
        }
        benachrichtigeBeobachter(result.toString());
        return result.toString();
    }

    public String arp(String[] args) {
        StringBuilder ergebnis = new StringBuilder();

        ARP arp = getSystemSoftware().holeARP();
        String filterAddress = null;
        if (numParams(args, 2) && EingabenUeberpruefung.isValidIpAddress(args[1])) {
            if ("-d".contentEquals(args[0])) {
                arp.removeARPTableEntry(args[1]);
            } else if ("-a".contentEquals(args[0])) {
                filterAddress = args[1];
            }
        } else if (numParams(args, 1) && "-d".contentEquals(args[0])) {
            arp.resetArpTable();
        }

        ergebnis.append(messages.getString("sw_terminal_msg50"));
        ergebnis.append("----------------------------------------\n");

        Map<String, String> arpTable = null == filterAddress ? arp.holeARPTabelle() : arp.holeARPTabelle(filterAddress);
        for (String ipAddress : arpTable.keySet()) {
            ergebnis.append(String.format("| %-15s  ", ipAddress));
            ergebnis.append(String.format("| %-17s |\n", arpTable.get(ipAddress)));

        }
        ergebnis.append("----------------------------------------\n");
        benachrichtigeBeobachter(ergebnis.toString());
        return ergebnis.toString();
    }

    /**
     * 
     * 'host' command to resolve URL to an IP address using the client's DNS server entry
     * 
     */
    public String host(String[] args) {
        benachrichtigeBeobachter(Boolean.TRUE);
        if (!numParams(args, 1)) {
            usage("host");
        } else {
            Resolver res = getSystemSoftware().holeDNSClient();
            try {
                DNSNachricht result = res.resolveA(args[0]);
                int resultCount = 0;
                for (ResourceRecord rr : result.holeResourceRecords()) {
                    if (ResourceRecord.ADDRESS.equals(rr.getType())) {
                        benachrichtigeBeobachter(rr.getDomainname() + " " + messages.getString("sw_terminal_msg28")
                                + " " + rr.getRdata() + "\n");
                        resultCount++;
                    }
                }
                result = res.resolveMX(args[0]);
                for (ResourceRecord rr : result.holeResourceRecords()) {
                    if (ResourceRecord.MAIL_EXCHANGE.equals(rr.getType())) {
                        benachrichtigeBeobachter(rr.getDomainname() + " " + messages.getString("sw_terminal_msg58")
                                + " " + rr.getRdata() + "\n");
                        resultCount++;
                    }
                }
                if (resultCount == 0) {
                    LOG.debug("Terminal 'host': result is empty!");
                    benachrichtigeBeobachter(messages.getString("sw_terminal_msg30"));
                }
            } catch (TimeOutException e) {
                benachrichtigeBeobachter(messages.getString("sw_terminal_msg31"));
            } catch (Exception e) {
                LOG.debug("", e);
                benachrichtigeBeobachter(messages.getString("sw_terminal_msg29"));
            }
        }
        benachrichtigeBeobachter(Boolean.FALSE);
        return null;
    }

    public String nslookup(String[] args) {
        if (!numParams(args, 1)) {
            return usage("nslookup");
        }
        Betriebssystem bs = (Betriebssystem) getSystemSoftware();
        Resolver res = bs.holeDNSClient();
        if (res == null) {
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg30"));
            return messages.getString("sw_terminal_msg30");
        }

        benachrichtigeBeobachter(Boolean.TRUE);
        try {
            DNSNachricht response = res.resolveA(args[0], bs.getDNSServer());

            String serverAddress = response.isLocal() ? IP.LOCALHOST : bs.getDNSServer();
            benachrichtigeBeobachter("Server:  " + serverAddress + "\n");
            benachrichtigeBeobachter("Address: " + serverAddress + "\n");

            benachrichtigeBeobachter("\nNon-authoritative Answer:\n");
            for (ResourceRecord rr : response.holeAntwortResourceRecords()) {
                benachrichtigeBeobachter("Name:    " + rr.getDomainname() + "\n");
                benachrichtigeBeobachter("Address: " + rr.getRdata() + "\n");
            }
        } catch (TimeOutException e) {
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg31") + "\n");
        } catch (Exception e) {
            LOG.debug("", e);
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg29") + "\n");
        }
        benachrichtigeBeobachter(Boolean.FALSE);
        return null;
    }

    /**
     * 'ping' command to check connectivity via ICMP echo request/reply
     */
    public String ping(String[] args) {
        LOG.debug("ping with params: {}", args.toString());

        String target = null;
        boolean enableBroadcast = false;
        if (numParams(args, 1)) {
            target = args[0];
        } else if (numParams(args, 2)) {
            target = args[1];
            enableBroadcast = true;
        } else {
            return usage("ping");
        }
        Resolver res = getSystemSoftware().holeDNSClient();
        if (res == null) {
            LOG.debug("ERROR (" + this.hashCode() + "): Terminal 'host': Resolver is null!");
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg30"));
            return messages.getString("sw_terminal_msg30");
        }

        // first: resolve host name
        String targetIp;
        try {
            targetIp = IP.ipCheck(target);
            if (targetIp == null) { // args[0] is not an IP address
                targetIp = res.holeIPAdresse(target);
            }
            if (targetIp == null) { // args[0] could also not be resolved
                LOG.debug("ERROR (" + this.hashCode() + "): Terminal 'host': result is null!");
                benachrichtigeBeobachter(messages.getString("sw_terminal_msg30"));
                return messages.getString("sw_terminal_msg30");
            }
        } catch (TimeOutException e) {
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg31") + " (DNS)");
            return messages.getString("sw_terminal_msg31" + " (DNS)");
        } catch (Exception e) {
            LOG.debug("", e);
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg29"));
            return messages.getString("sw_terminal_msg29");
        }
        try {
            Route route = ((InternetKnotenBetriebssystem) getSystemSoftware()).determineRoute(targetIp);
            if (!enableBroadcast
                    && VermittlungsProtokoll.isBroadcast(targetIp, route.getInterfaceIpAddress(), route.getNetMask())) {
                benachrichtigeBeobachter(messages.getString("sw_terminal_msg53"));
                return messages.getString("sw_terminal_msg53");
            }
        } catch (RouteNotFoundException e1) {
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg52"));
            return messages.getString("sw_terminal_msg52");
        }

        // second: send several ICMP echo requests
        long timeStart, timeDiff;
        // inform about a multiple data transmission to the observer
        benachrichtigeBeobachter(Boolean.TRUE);
        benachrichtigeBeobachter("PING " + target + " (" + targetIp + ")");

        int receivedReplies = 0;
        int num;
        int loopNumber = Information.isPosixCommandLineToolBehaviour() ? 10 : 4;
        for (num = 0; !interrupted && num < loopNumber; num++) {
            try {
                timeStart = System.currentTimeMillis();
                IcmpPaket pingResponse = getSystemSoftware().holeICMP().startSinglePing(targetIp, num + 1);
                timeDiff = 1000 - (System.currentTimeMillis() - timeStart);
                if (pingResponse.getTtl() >= 0) {
                    benachrichtigeBeobachter("\nFrom " + pingResponse.getSender() + ": icmp_seq=" + (num + 1) + " ttl="
                            + pingResponse.getTtl() + " time=" + (System.currentTimeMillis() - timeStart) + "ms");
                    receivedReplies++;
                }
                if (timeDiff > 0) {
                    try {
                        LOG.debug("DEBUG: Terminal waits for " + timeDiff + " ms");
                        Thread.sleep(timeDiff);
                    } catch (InterruptedException e) {}
                }
            } catch (java.util.concurrent.TimeoutException e) {
                benachrichtigeBeobachter(
                        "\nFrom " + args[0] + " (" + targetIp + "): icmp_seq=" + (num + 1) + "   -- Timeout!");
            } catch (Exception e) {
                LOG.debug("", e);
            }
        }
        // print statistics
        benachrichtigeBeobachter("\n--- " + args[0] + " " + messages.getString("sw_terminal_msg45") + " ---\n" + num
                + " " + messages.getString("sw_terminal_msg46") + ", " + receivedReplies + " "
                + messages.getString("sw_terminal_msg47") + ", "
                + ((int) Math.round((1 - (((double) receivedReplies) / ((double) num))) * 100)) + "% "
                + messages.getString("sw_terminal_msg48") + "\n");
        benachrichtigeBeobachter(Boolean.FALSE);
        return "";
    }

    /**
     * 'tcpdump' simple tool to show data exchange
     */
    public String tcpdump(String[] args) {
        LOG.trace("INVOKED (" + this.hashCode() + ", " + this.getId() + ") " + getClass() + " (Terminal), tcpdump(");
        for (int i = 0; i < args.length; i++) {
            LOG.debug(i + "='" + args[i] + "' ");
        }
        LOG.debug(")");

        benachrichtigeBeobachter(Boolean.TRUE);
        benachrichtigeBeobachter(messages.getString("sw_terminal_msg55"));

        Lauscher lauscher = Lauscher.getLauscher();
        String localMacAddress = ((Betriebssystem) getSystemSoftware()).primaryMACAddress();
        int offset = lauscher.getOffsetByTimestamp(localMacAddress, System.currentTimeMillis());
        while (!interrupted) {
            Object[][] data = lauscher.getDaten(localMacAddress, true, offset);
            for (int i = 0; i < data.length; i++) {
                Object[] packetData = data[i];
                int currentFrameSerialNumber = Integer.parseInt(packetData[0].toString());
                int nextFrameSerialNumber = data.length > i + 1 ? Integer.parseInt(data[i + 1][0].toString())
                        : Integer.MAX_VALUE;
                if (currentFrameSerialNumber < nextFrameSerialNumber) {
                    String message = packetData[1] + " " + packetData[4] + " " + packetData[2] + " > " + packetData[3]
                            + " " + packetData[6];
                    benachrichtigeBeobachter("\n  " + message.trim().replace("\n", "  "));
                    offset++;
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {}
        }
        benachrichtigeBeobachter(Boolean.FALSE);
        return "";
    }

    public String arpsend(String[] args) {
        String targetIP = IP.CURRENT_NETWORK;
        String targetMAC = Ethernet.ETHERNET_BROADCAST;
        String senderIP = null;
        Betriebssystem os = (Betriebssystem) getSystemSoftware();
        if (numParams(args, 2)) {
            if (EingabenUeberpruefung.isGueltig(args[1], EingabenUeberpruefung.musterIpAdresse)) {
                targetIP = args[1];
                targetMAC = os.holeARP().holeARPTabellenEintrag(targetIP, 2);
            }
            if (EingabenUeberpruefung.isGueltig(args[0], EingabenUeberpruefung.musterIpAdresse)) {
                senderIP = args[0];
            }
        }
        if (null != targetMAC && null != senderIP) {
            ArpPaket arpPacket = os.holeARP().sendArpReply(os.primaryMACAddress(), senderIP, targetMAC, targetIP);
            benachrichtigeBeobachter("  >>> " + arpPacket);
        } else {
            usage("arpsend");
        }
        return null;
    }

    /**
     * 'traceroute' prints the route packets take to the network host (using ICMP Echo Request and ICMP Time Exceeded)
     */
    public String traceroute(String[] args) {
        if (!numParams(args, 1)) {
            usage("traceroute");
            return null;
        }

        int maxHops = 20;

        // 1.: Hostnamen auflösen
        String destIP = IP.ipCheck(args[0]);
        if (destIP == null) {
            filius.software.dns.Resolver res = getSystemSoftware().holeDNSClient();

            try {
                destIP = res.holeIPAdresse(args[0]);
            } catch (TimeOutException e) {
                benachrichtigeBeobachter(messages.getString("sw_terminal_trace_msg2"));
                return null;
            }

        }
        if (destIP == null) {
            benachrichtigeBeobachter(messages.getString("sw_terminal_trace_msg3"));
            return null;
        }

        benachrichtigeBeobachter(Boolean.TRUE);
        if (destIP.equals(args[0])) {
            benachrichtigeBeobachter(
                    String.format(messages.getString("sw_terminal_trace_msg4") + "\n", args[0], maxHops));
        } else {
            benachrichtigeBeobachter(
                    String.format(messages.getString("sw_terminal_trace_msg5") + "\n", args[0], destIP, maxHops));
        }

        // 2.: Pings senden und gucken, was alles zurueckkommt
        IcmpPaket recv = null;
        int seqNr = 42 * 23;
        int fehler = 0;
        int ttl;

        for (ttl = 0; ttl < maxHops && !interrupted; ttl++) {
            benachrichtigeBeobachter(" " + ttl + "    ");

            for (int i = 0; i < 3 && !interrupted; i++) {
                seqNr++;
                recv = getSystemSoftware().holeICMP().sendProbe(destIP, ttl, seqNr);
                if (recv != null && recv.getSeqNr() == seqNr) {
                    fehler = 0;
                    break;
                }
                fehler++;
                benachrichtigeBeobachter("* ");
            }

            if (fehler == 0) {
                benachrichtigeBeobachter(recv.getSender());
                if (recv.getIcmpType() != 11) {
                    break;
                }
            } else if (fehler > 5) {
                break;
            }

            benachrichtigeBeobachter("\n");
        }

        benachrichtigeBeobachter(Boolean.FALSE);
        if (ttl >= maxHops) {
            benachrichtigeBeobachter(
                    "\n\n" + String.format(messages.getString("sw_terminal_trace_msg6"), args[0], maxHops));
        } else if (interrupted) {
            benachrichtigeBeobachter("\n\n" + messages.getString("sw_terminal_trace_msg7"));
        } else if (recv != null && recv.getIcmpType() == 3) {
            switch (recv.getIcmpCode()) {
            case 0:
                benachrichtigeBeobachter(
                        "\n\n" + String.format(messages.getString("sw_terminal_trace_msg8"), recv.getSender()));
                break;
            case 1:
                benachrichtigeBeobachter(
                        "\n\n" + String.format(messages.getString("sw_terminal_trace_msg9"), recv.getSender()));
                break;
            default:
                benachrichtigeBeobachter(
                        "\n\n" + String.format(messages.getString("sw_terminal_trace_msg10"), recv.getSender()));
                break;
            }
        } else if (fehler == 0) {
            if (ttl == 1) {
                benachrichtigeBeobachter(
                        "\n\n" + String.format(messages.getString("sw_terminal_trace_msg11"), args[0]));
            } else {
                benachrichtigeBeobachter(
                        "\n\n" + String.format(messages.getString("sw_terminal_trace_msg12"), args[0], ttl));
            }
        } else {
            benachrichtigeBeobachter("\n\n" + messages.getString("sw_terminal_trace_msg13"));
        }

        return null;
    }

    public void setInterrupt(boolean val) {
        this.interrupted = val;
    }

    public void beenden() {
        setInterrupt(true);
        super.beenden();
    }

    public void terminalEingabeAuswerten(String enteredCommand, String[] enteredParameters) {
        LOG.trace("INVOKED (" + this.hashCode() + ", " + this.getId() + ") " + getClass()
                + " (Terminal), terminalEingabeAuswerten(" + enteredCommand + "," + enteredParameters + ")");
        Object[] args = new Object[1];
        args[0] = enteredParameters;
        try {
            // test, whether method exists; if not, exception will be evaluated
            this.getClass().getDeclaredMethod(enteredCommand, enteredParameters.getClass());

            setInterrupt(false);
            ausfuehren(enteredCommand, args);
        } catch (NoSuchMethodException e) {
            benachrichtigeBeobachter(messages.getString("terminal_msg2") + "\n" + messages.getString("terminal_msg3"));
        } catch (Exception e) {
            LOG.debug("", e);
        }
    }

    public DefaultMutableTreeNode getAktuellerOrdner() {
        return aktuellerOrdner;
    }

    public void setAktuellerOrdner(DefaultMutableTreeNode aktuellerOrdner) {
        this.aktuellerOrdner = aktuellerOrdner;
    }

    /**
     * 
     * @author Hannes Johannes Bade & Thomas Gerding
     * 
     *         fuellt einen String mit Leerzeichen auf (bis zur länge a)
     * 
     * @param a
     * @param fueller
     * @return
     */
    private String stringFuellen(int a, String fueller) {
        LOG.trace("INVOKED (" + this.hashCode() + ", " + this.getId() + ") " + getClass()
                + " (Terminal), stringFuellen(" + a + "," + fueller + ")");
        String tmp = "";
        for (int i = 0; i < a; i++) {
            tmp = tmp + fueller;
        }
        return tmp;
    }

    /**
     * method to check for correct number of parameters
     */
    private int countParams(String[] args) {
        LOG.trace("INVOKED (" + this.hashCode() + ", " + this.getId() + ") " + getClass() + " (Terminal), countParams("
                + args + ")");
        int count = 0;
        for (int i = 0; i < args.length; i++) {
            if (!args[i].isEmpty()) {
                count++;
            } else
                return count; // return on first empty entry
        }
        return count;
    }

    private boolean numParams(String[] args, int exactNum) {
        return (exactNum == countParams(args));
    }

    private boolean numParams(String[] args, int minNum, int maxNum) {
        int count = countParams(args);
        return ((count >= minNum) && (count <= maxNum));
    }
}
