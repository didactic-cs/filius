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
package filius.gui.netzwerksicht;

import java.io.Serializable;

import filius.hardware.Cable;

/**
 * 
 * Die Klasse GUIKabelItem ist die Verbindung von Grafikdarstellung und "realem"
 * Kabel. Sie enthält Jeweils ein JCablePanel und ein Cable. Dazu stellt es die
 * getter und setter Methoden bereit.
 * 
 * @author Thomas Gerding & Johannes Bade
 * 
 */
@SuppressWarnings("serial")
public class GUICableItem implements Serializable {
	
	private Cable cable;
	private JCablePanel cablePanel;	

	public GUICableItem() {
		cablePanel = new JCablePanel();
	}

	public Cable getCable() {
		return cable;
	}

	public void setCable(Cable cable) {
		this.cable = cable;
		cable.addListener(cablePanel);
	}

	public JCablePanel getCablePanel() {
		return cablePanel;
	}

	public void setCablePanel(JCablePanel panel) {
		this.cablePanel = panel;
	}	

	public String toString() {
		String result = "[";
		if (cable != null)
			result += "cable (id)=" + cable.hashCode() + ", ";
		else
			result += "cable=<null>, ";
		if (cablePanel != null) {
			result += "kabelpanel (id)=" + cablePanel.hashCode() + ", ";
			if (cablePanel.getZiel1() != null) {
				result += "kabelpanel.ziel1 (id)" + cablePanel.getZiel1().hashCode() + ", ";
				if (cablePanel.getZiel1().getNode() != null)
					result += "kabelpanel.ziel1.knoten (name)" + cablePanel.getZiel1().getNode().getDisplayName() + ", ";
				else
					result += "kabelpanel.ziel1.knoten=<null>, ";
			} else
				result += "kabelpanel.ziel1=<null>, ";
			if (cablePanel.getZiel2() != null) {
				result += "kabelpanel.ziel2 (id)" + cablePanel.getZiel2().hashCode() + ", ";
				if (cablePanel.getZiel2().getNode() != null)
					result += "kabelpanel.ziel2.knoten (name)" + cablePanel.getZiel2().getNode().getDisplayName() + ", ";
				else
					result += "kabelpanel.ziel2.knoten=<null>, ";
			}
		} else
			result += "kabelpanel=<null>";
		result += "]";
		return result;
	}
}
