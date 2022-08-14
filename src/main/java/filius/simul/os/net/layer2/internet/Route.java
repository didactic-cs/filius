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

/**
 * @author stefan
 * 
 */
public class Route {

	private String netAddress;
	private String netMask;
	protected String gateway;
	protected String interfaceIpAddress;

	public String getNetAddress() {
		return netAddress;
	}

	public String getNetMask() {
		return netMask;
	}

	public String getGateway() {
		return gateway;
	}

	public String getInterfaceIpAddress() {
		return interfaceIpAddress;
	}

	public Route(String netAddress, String netMask, String gateway, String interfaceIpAddress) {
		this.netAddress = netAddress;
		this.netMask = netMask;
		this.gateway = gateway;
		this.interfaceIpAddress = interfaceIpAddress;
	}

	public Route(String[] routingInfo) {
		if (routingInfo.length == 4) {
			netAddress = routingInfo[0];
			netMask = routingInfo[1];
			gateway = routingInfo[2];
			interfaceIpAddress = routingInfo[3];
		} else if (routingInfo.length == 2) {
			gateway = routingInfo[0];
			interfaceIpAddress = routingInfo[1];
		}
	}
}
