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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedList;
import java.util.Vector;

import filius.Main;
import filius.hardware.NetzwerkInterface;
import filius.hardware.knoten.InternetKnoten;
import filius.rahmenprogramm.EingabenUeberpruefung;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.Information;
import filius.software.www.WebServer;
import filius.software.www.WebServerPlugIn;

public class FirewallWebKonfig extends WebServerPlugIn implements I18n {

	private WebServer webserver;
	private Firewall firewall;

	private String srcIP = "";
	private String srcMask = "";
	private String destIP = "";
	private String destMask = "";
	private short protocol = FirewallRule.ALL_PROTOCOLS;
	private int port = FirewallRule.ALL_PORTS;
	private String portStr = "";
	private short action = FirewallRule.DROP;
	private String errMsg = "";
	
	public void setFirewall(Firewall firewall) {
		this.firewall = firewall;
	}

	public Firewall getFirewall() {
		return firewall;
	}

	public void setWebserver(WebServer server) {
		this.webserver = server;
	}

	public WebServer getWebserver() {
		return webserver;
	}

	/**
	 * Wird ueber das Interface WebServerPlugIn aufgerufen. Muss den gelieferten
	 * DatenString verarbeiten, die Firewall bestuecken, und anschließend eine
	 * HTML-Seite zurueckliefern
	 */
	public String holeHtmlSeite(String postDaten) {
		Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (FirewallWebKonfig), holeHtmlSeite("
		        + postDaten + ")");
		String seite = "";
		if (postDaten != null && !postDaten.isEmpty()) {
//			firewallBestuecken(postDaten); // Dort wird die Methode
			                               // postStringZerlegen() ausgefuehrt
			this.processParameters(postDaten);  // process parameters for new firewall format
		}
		// Main.debug.println("FirewallWebKonfig: Seite liefern= \n"+seite);
		seite = konfigSeiteErstellen();
		return seite;
	}

	/*
	 * @author weyer 
	 * liefert zu einem ausgeführten Submit-Befehl die einzelnen
	 * Stücke zurück
	 */
	private String[][] postStringZerlegen(String post) {
		Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass()
		        + " (FirewallWebKonfig), postStringZerlegen(" + post + ")");

		String[] submitTeile;
		String[] element, tmp;
		String[][] tupel;

		// Main.debug.println("String mit submit in FirewallWebKonfig angekommen: "+post);
		// String zerlegen und überprüfen:
		try {
			submitTeile = URLDecoder.decode(post, "UTF-8").split("&");
		} catch (UnsupportedEncodingException e) {
			submitTeile = post.split("&");
		}

		// Die ersten 5 Einträge des Arrays sind immer gleich

		tupel = new String[submitTeile.length][2];
		for (int i = 0; i < submitTeile.length; i++) {
			tmp = submitTeile[i].split("=");
			element = new String[] { "", "" };
			for (int j = 0; j < tmp.length && j < element.length; j++) {
				element[j] = tmp[j].trim();
			}
			tupel[i] = element;
		}

		return tupel;
	}

	/**
	 * 
	 * @param postString	String of POST parameters submitted to web site
	 */
	private void processParameters(String postString) {
		Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass()
		        + " (FirewallWebKonfig), processParameters(" + postString + ")");

		// globally defined to make them usable in HTML creation method
		srcIP = "";
		srcMask = "";
		destIP = "";
		destMask = "";
		protocol = FirewallRule.ALL_PROTOCOLS;
		port = FirewallRule.ALL_PORTS;
		portStr = "";
		action = FirewallRule.DROP;
		errMsg = "";
		//--------------
		
		int radioRule = -1;
		short defaultPolicy = FirewallRule.DROP;
		
		boolean cbActive = false;
		boolean cbDropIcmp = false;
		boolean cbOnlySyn = false;
		
		boolean doSaveState = false;
		boolean doSaveDefPol = false;
		boolean doMoveUp = false;
		boolean doMoveDown = false;
		boolean doDelRule = false;
		boolean doAddRule = false;

		if (webserver.getSystemSoftware() != null) {
			String[][] postContent = postStringZerlegen(postString);
			// copy parameter values to variables
			for (int i = 0; i < postContent.length; i++) {
				if (postContent[i][0].equals("btnSave")) {  // button: save general settings
					doSaveState = true;
				}
				else if (postContent[i][0].equals("defPolSet")) {  // button: set default policy
					doSaveDefPol = true;
				}
				else if (postContent[i][0].equals("moveup")) {  // button: move rule up
					doMoveUp = true;
				}
				else if (postContent[i][0].equals("movedown")) {  // button: move rule down
					doMoveDown = true;
				}
				else if (postContent[i][0].equals("delrule")) {  // button: delete rule
					doDelRule = true;
				}
				else if (postContent[i][0].equals("addrule")) {  // button: add rule
					doAddRule = true;
				}
				else if (postContent[i][0].equals("defaultPolicy")) {
					if(postContent[i][1].equals("accept"))
						defaultPolicy = FirewallRule.ACCEPT;
					else
						defaultPolicy = FirewallRule.DROP;
				}
				else if (postContent[i][0].equals("srcIP")) {
					srcIP = postContent[i][1];
					if(! srcIP.isEmpty()) {
						if(! EingabenUeberpruefung.isGueltig(srcIP, EingabenUeberpruefung.musterIpAdresse))
							errMsg += "<li>" + messages.getString("firewallwebkonfig_msg2") + "</li>";
						if(srcMask.isEmpty() || ! EingabenUeberpruefung.isValidSubnetmask(srcMask))
							errMsg += "<li>" + messages.getString("firewallwebkonfig_msg3") + "</li>";
					}
				}
				else if (postContent[i][0].equals("srcMask")) {
					srcMask = postContent[i][1];
					if(! srcMask.isEmpty()) {
						if(! EingabenUeberpruefung.isValidSubnetmask(srcMask))
							errMsg += "<li>" + messages.getString("firewallwebkonfig_msg3") + "</li>";
						if(srcIP.isEmpty())
							errMsg += "<li>" + messages.getString("firewallwebkonfig_msg7") + "</li>";
					}
				}
				else if (postContent[i][0].equals("destIP")) {
					destIP = postContent[i][1];
					if(! destIP.isEmpty()) {
						if(! EingabenUeberpruefung.isGueltig(destIP, EingabenUeberpruefung.musterIpAdresse))
							errMsg += "<li>" + messages.getString("firewallwebkonfig_msg4") + "</li>";
						if(destMask.isEmpty() || ! EingabenUeberpruefung.isValidSubnetmask(destMask))
							errMsg += "<li>" + messages.getString("firewallwebkonfig_msg5") + "</li>";
					}
				}
				else if (postContent[i][0].equals("destMask")) {
					destMask = postContent[i][1];
					if(! destMask.isEmpty()) {
						if(! EingabenUeberpruefung.isValidSubnetmask(destMask))
							errMsg += "<li>" + messages.getString("firewallwebkonfig_msg5") + "</li>";
						if(destIP.isEmpty())
							errMsg += "<li>" + messages.getString("firewallwebkonfig_msg7") + "</li>";
					}
				}
				else if (postContent[i][0].equals("protocol")) {
					if(postContent[i][1].equals("TCP"))
						protocol = FirewallRule.TCP;
					else if(postContent[i][1].equals("UDP"))
						protocol = FirewallRule.UDP;
				}
				else if (postContent[i][0].equals("port")) {
					portStr = postContent[i][1];
					try {
						port = Integer.parseInt(portStr);
						if(port != FirewallRule.ALL_PORTS)
							if(! EingabenUeberpruefung.isGueltig(portStr, EingabenUeberpruefung.musterPort))
								errMsg += "<li>" + messages.getString("firewallwebkonfig_msg6") + "</li>";
					}
					catch (Exception e) {
						if(! postContent[i][1].isEmpty())
							errMsg += "<li>" + messages.getString("firewallwebkonfig_msg6") + "</li>";
					}
				}
				else if (postContent[i][0].equals("action")) {
					if(postContent[i][1].equals("accept"))
						action = FirewallRule.ACCEPT;
					else
						action = FirewallRule.DROP;
				}
				else if (postContent[i][0].equals("radioRule")) {
					try {
						radioRule = Integer.parseInt(postContent[i][1]);
					}
					catch (Exception e) {						
					}
				}
				else if (postContent[i][0].equals("firewallActivate")) {
					cbActive = true;
				}
				else if (postContent[i][0].equals("dropICMP")) {
					cbDropIcmp = true;
				}
				else if (postContent[i][0].equals("onlySYN")) {
					cbOnlySyn = true;
				}
			}
			
			// actions
			if(doSaveState) {
				firewall.setActivated(cbActive);
				firewall.setDropICMP(cbDropIcmp);
				firewall.setAllowRelatedPackets(cbOnlySyn);
			}
			else if(doSaveDefPol) {
				firewall.setDefaultPolicy(defaultPolicy);
			}
			else if(doMoveUp) {
				firewall.moveUp(radioRule);
			}
			else if(doMoveDown) {
				firewall.moveDown(radioRule);
			}
			else if(doDelRule) {
				firewall.delRule(radioRule);
			}
			else if(doAddRule) {
				if(errMsg.isEmpty())
					firewall.addRule(new FirewallRule(srcIP,srcMask,destIP,destMask,port,protocol,action));
				else
					errMsg = "<font color=\"red\">" + 
							 messages.getString("firewallwebkonfig_msg1") +
							 "<ul>" +
							 errMsg +
							 "</ul>" +
							 "</font><br>";
			}
			if(! doAddRule)
				errMsg = "";  // only use error message, if rule should have been added!
		}
	}

	/*
	 * diese Seite erstellt den kompletten Quelltext für die konfig.html
	 */
	private String konfigSeiteErstellen() {
		Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass()
		        + " (FirewallWebKonfig), konfigSeiteErstellen()");
		String html;

		// Main.debug.println("FirewallWebKonfig: dynamische Generierung der HTML-konfig-Seite!");

		if (firewall != null) {

			try {

				html = textDateiEinlesen("tmpl/firewall_konfig_webseite_"
				        + Information.getInformation().getLocale().toString() + ".txt");

				html = html.replaceAll(":action_pfad:", getPfad());

				StringBuffer cbFwActivate = new StringBuffer();
				cbFwActivate.append("\t\t<input name=\"firewallActivate\" type=\"checkbox\"");
				cbFwActivate.append(" value=\"1\" size=\"30\" maxlength=\"40\"");
				if (firewall.isActivated()) {
					cbFwActivate.append(" checked=\"checked\"");
				}
				cbFwActivate.append(" />");
				html = html.replaceAll(":firewallActivate:", cbFwActivate.toString());

				StringBuffer cbDropIcmp = new StringBuffer();
				cbDropIcmp.append("\t\t<input name=\"dropICMP\" type=\"checkbox\"");
				cbDropIcmp.append(" value=\"1\" size=\"30\" maxlength=\"40\"");
				if (firewall.getDropICMP()) {
					cbDropIcmp.append(" checked=\"checked\"");
				}
				cbDropIcmp.append(" />");
				html = html.replaceAll(":dropICMP:", cbDropIcmp.toString());

				StringBuffer cbOnlySyn = new StringBuffer();
				cbOnlySyn.append("\t\t<input name=\"onlySYN\" type=\"checkbox\"");
				cbOnlySyn.append(" value=\"1\" size=\"30\" maxlength=\"40\"");
				if (firewall.getDropICMP()) {
					cbOnlySyn.append(" checked=\"checked\"");
				}
				cbOnlySyn.append(" />");
				html = html.replaceAll(":onlySYN:", cbOnlySyn.toString());

				if(firewall.getDefaultPolicy() == FirewallRule.ACCEPT) {
					html = html.replaceAll(":defPolDropSelected:", "");
					html = html.replaceAll(":defPolAcceptSelected:", " selected=\"selected\"");
				}
				else {
					html = html.replaceAll(":defPolAcceptSelected:", "");
					html = html.replaceAll(":defPolDropSelected:", " selected=\"selected\"");
				}

				StringBuffer ruleTable = new StringBuffer();
				Vector<FirewallRule> ruleset = firewall.getRuleset();
				for(int i=0; i<ruleset.size(); i++) {
					ruleTable.append("<tr>");
					ruleTable.append("<td><input name=\"radioRule\" type=\"radio\"");
					ruleTable.append(" value=\"");
					ruleTable.append((i+1));
					ruleTable.append("\" size=\"10\" maxlength=\"40\"");
					ruleTable.append(" />&nbsp;");
					ruleTable.append((i+1)+"</td>");
					ruleTable.append("<td>"+ruleset.get(i).srcIP+"</td>");
					ruleTable.append("<td>"+ruleset.get(i).srcMask+"</td>");
					ruleTable.append("<td>"+ruleset.get(i).destIP+"</td>");
					ruleTable.append("<td>"+ruleset.get(i).destMask+"</td>");
					ruleTable.append("<td>");
					if(ruleset.get(i).protocol == FirewallRule.TCP)
						ruleTable.append("TCP");
					else if(ruleset.get(i).protocol == FirewallRule.UDP)
						ruleTable.append("UCP");
					else if(ruleset.get(i).protocol == FirewallRule.ICMP)
						ruleTable.append("ICMP");
					ruleTable.append("</td>");
					ruleTable.append("<td>");
					if(ruleset.get(i).port >= 0)
						ruleTable.append(ruleset.get(i).port);
					ruleTable.append("</td>");
					ruleTable.append("<td>");
					if(ruleset.get(i).action == FirewallRule.ACCEPT)
						ruleTable.append(messages.getString("jfirewalldialog_msg33"));
					else
						ruleTable.append(messages.getString("jfirewalldialog_msg34"));
					ruleTable.append("</td>");
					ruleTable.append("</tr>");
				}
				html = html.replaceAll(":ruleset:", ruleTable.toString());
				
				if(errMsg.isEmpty()) {
					html = html.replaceAll(":errorMsg:", "");
					html = html.replaceAll(":srcIP:", ""); 
					html = html.replaceAll(":srcMask:", ""); 
					html = html.replaceAll(":destIP:", ""); 
					html = html.replaceAll(":destMask:", ""); 
					html = html.replaceAll(":port:", ""); 
					html = html.replaceAll(":optionProtAll:", "");
					html = html.replaceAll(":optionProtTCP:", "");
					html = html.replaceAll(":optionProtUDP:", "");
					html = html.replaceAll(":optionActionDrop:", "");
					html = html.replaceAll(":optionActionAccept:", "");
				}
				else {
					html = html.replaceAll(":errorMsg:", errMsg);
					html = html.replaceAll(":srcIP:", srcIP); 
					html = html.replaceAll(":srcMask:", srcMask); 
					html = html.replaceAll(":destIP:", destIP); 
					html = html.replaceAll(":destMask:", destMask); 
					if(port > 0)
						html = html.replaceAll(":port:", portStr);
					else
						html = html.replaceAll(":port:", portStr);
					if(protocol == FirewallRule.ALL_PROTOCOLS) {
						html = html.replaceAll(":optionProtAll:", " selected");
						html = html.replaceAll(":optionProtTCP:", "");
						html = html.replaceAll(":optionProtUDP:", "");
					}
					else if(protocol == FirewallRule.TCP) {
						html = html.replaceAll(":optionProtAll:", "");
						html = html.replaceAll(":optionProtTCP:", " selected");
						html = html.replaceAll(":optionProtUDP:", "");
					}
					else if(protocol == FirewallRule.UDP) {
						html = html.replaceAll(":optionProtAll:", "");
						html = html.replaceAll(":optionProtTCP:", "");
						html = html.replaceAll(":optionProtUDP:", " selected");
					}
					else {
						html = html.replaceAll(":optionProtAll:", "");
						html = html.replaceAll(":optionProtTCP:", "");
						html = html.replaceAll(":optionProtUDP:", "");
					}
					if(action == FirewallRule.DROP) {
						html = html.replaceAll(":optionActionDrop:", " selected");
						html = html.replaceAll(":optionActionAccept:", "");
					}
					else if(action == FirewallRule.ACCEPT) {
						html = html.replaceAll(":optionActionDrop:", "");
						html = html.replaceAll(":optionActionAccept:", " selected");
					}
				}

			} catch (Exception f) {
				f.printStackTrace(Main.debug);
				return null;
			}

			return html;
		} else {
			return null;
		}
	}

}
