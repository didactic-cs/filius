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

import java.io.IOException;

import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.Information;
import filius.software.rip.RIPRoute;
import filius.software.rip.RIPTable;
import filius.software.rip.RIPUtil;
import filius.software.system.VermittlungsrechnerBetriebssystem;
import filius.software.www.WebServerPlugIn;

/**
 * 
 * @author pyropeter
 * @author stefanf
 * 
 */
public class VermittlungWeb extends WebServerPlugIn implements I18n {
	private VermittlungsrechnerBetriebssystem bs;

	public VermittlungWeb(VermittlungsrechnerBetriebssystem bs) {
		super();

		this.bs = bs;
	}

	public String holeHtmlSeite(String postDaten) {
		RIPTable table = bs.getRIPTable();

		String html = null;
		if (table != null) {
			html = createRoutingTablePage(table);
		} else {
			html = createForwardingTablePage(bs.getWeiterleitungstabelle());
		}

		return html;
	}

	private String createForwardingTablePage(Weiterleitungstabelle weiterleitungstabelle) {
		StringBuffer forwardingEntries = new StringBuffer();
		for (String[] forwardingEntry : weiterleitungstabelle.holeTabelle()) {
			forwardingEntries.append(forwardingEntryToHtml(forwardingEntry));
		}
		String html = null;
		try {
			html = textDateiEinlesen("tmpl/routing_" + Information.getInformation().getLocale().toString() + ".html");
			html = html.replaceAll(":title:", messages.getString("sw_vermittlungweb_msg5"));
			html = html.replaceAll(":routing_entries:", forwardingEntries.toString());
			html = html.replaceAll(":hint:", messages.getString("sw_vermittlungweb_msg4"));
		} catch (IOException e) {
			System.err.println("routing table template could not be read.");
			e.printStackTrace();
		}
		return html;
	}

	private String forwardingEntryToHtml(String[] forwardingEntry) {
		String html = "";

		html += "<td>" + forwardingEntry[0] + "</td>";
		html += "<td>" + forwardingEntry[1] + "</td>";

		boolean targetIsDirectlyConnected = forwardingEntry[2].equals(forwardingEntry[3]);
		boolean isLocalhost = forwardingEntry[3].equals("127.0.0.1");
		if (targetIsDirectlyConnected || isLocalhost) {
			html += "<td> 0 </td>";
		} else {
			html += "<td> ? </td>";
		}

		html += "<td>" + messages.getString("sw_vermittlungweb_msg3") + "</td>";

		html += "<td><a href=\"http://" + forwardingEntry[2] + "/routes\">" + forwardingEntry[2] + "</a></td>";
		html += "<td> - </td>";

		boolean isDefaultGateway = forwardingEntry[1].equals("0.0.0.0");
		if (targetIsDirectlyConnected || isLocalhost) {
			return "<tr style='background-color:#aaffaa'>" + html + "</tr>";
		} else if (isDefaultGateway) {
			return "<tr style='background-color:#aaaaff'>" + html + "</tr>";
		} else {
			return "<tr>" + html + "</tr>";
		}
	}

	private String createRoutingTablePage(RIPTable table) {
		StringBuffer routingEntries = new StringBuffer();
		synchronized (table) {
			for (RIPRoute route : table.routes) {
				routingEntries.append(routeToHtml(route));
			}
		}

		String html = null;
		try {
			html = textDateiEinlesen("tmpl/routing_" + Information.getInformation().getLocale().toString() + ".html");
			html = html.replaceAll(":title:", messages.getString("sw_vermittlungweb_msg6"));
			html = html.replaceAll(":routing_entries:", routingEntries.toString());
			html = html.replaceAll(":hint:", messages.getString("sw_vermittlungweb_msg1"));
		} catch (IOException e) {
			System.err.println("routing table template could not be read.");
			e.printStackTrace();
		}
		return html;
	}

	private String routeToHtml(RIPRoute route) {
		String html = "";

		html += "<td>" + route.getNetAddress() + "</td>";
		html += "<td>" + route.getNetMask() + "</td>";
		html += "<td>" + route.hops + "</td>";

		if (route.expires == 0) {
			html += "<td>" + messages.getString("sw_vermittlungweb_msg3") + "</td>";
		} else {
			long gueltig = (route.expires - RIPUtil.getTime()) / 1000;
			html += "<td>" + gueltig + "</td>";
		}

		html += "<td>" + route.getGateway() + "</td>";
		html += "<td><a href=\"http://" + route.hopPublicIp + "/routes\">" + route.hopPublicIp + "</a></td>";

		if (route.hops == 0) {
			return "<tr style='background-color:#aaffaa'>" + html + "</tr>";
		} else if (route.hops == 16) {
			return "<tr style='background-color:#ffaaaa'>" + html + "</tr>";
		} else {
			return "<tr>" + html + "</tr>";
		}
	}
}
