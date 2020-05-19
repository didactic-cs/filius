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
package filius.hardware;

import java.io.Serializable;

import filius.exception.InvalidParameterException;
import filius.rahmenprogramm.Information;
import filius.rahmenprogramm.SzenarioVerwaltung;
import filius.software.vermittlungsschicht.IPAddress;

@SuppressWarnings("serial")
public class NetzwerkInterface implements Serializable {
    private String mac;
    private String addressIP = IPAddress.defaultAddress(SzenarioVerwaltung.getInstance().ipVersion()).address();
    private String netmaskIP = IPAddress.defaultAddress(SzenarioVerwaltung.getInstance().ipVersion()).netmask();
    private String gatewayIP;
    private String dnsIP;
    private Port anschluss;

    public NetzwerkInterface() {
        setMac(Information.getInformation().holeFreieMACAdresse());
        setGateway("");
        setDns("");
        anschluss = new Port(this);
    }

    public Port getPort() {
        return anschluss;
    }

    public void setPort(Port port) {
        this.anschluss = port;
    }

    /**
     * IPv6-Adresse des DNS-Servers, der zur Aufloesung von Domainnamen verwendet wird.
     */
    public String getDns() {
        return dnsIP == null ? "" : dnsIP;
    }

    /**
     * IPv6-Adresse des DNS-Servers, der zur Aufloesung von Domainnamen verwendet wird.
     */
    public void setDns(String dns) {
        this.dnsIP = dns;
    }

    /** IPv6-Adresse der Netzwerkschnittstelle */
    public String getIp() {
        return addressIP;
    }

    public IPAddress addressIP() throws InvalidParameterException {
        return new IPAddress(addressIP, netmaskIP);
    }

    /** IPv6-Adresse der Netzwerkschnittstelle */
    public void setIp(String ip) {
        this.addressIP = ip;
    }

    public String getMac() {
        return mac.toUpperCase();
    }

    public void setMac(String mac) {
        if (mac != null) {
            Information.getInformation().macHinzufuegen(mac);
            this.mac = mac;
        }
    }

    public void setSubnetzMaske(String subnetzMaske) {
        this.netmaskIP = subnetzMaske;
    }

    public String getSubnetzMaske() {
        return netmaskIP;
    }

    /**
     * IPv6-Adresse des Standard-Gateways. Dorthin werden alle Pakete gesendet, fuer dessen Zieladresse kein anderer
     * Eintrag in der Weiterleitungstabelle vorhanden ist.
     */
    public String getGateway() {
        return gatewayIP == null ? "" : gatewayIP;
    }

    /**
     * IPv6-Adresse des Standard-Gateways. Dorthin werden alle Pakete gesendet, fuer dessen Zieladresse kein anderer
     * Eintrag in der Weiterleitungstabelle vorhanden ist.
     */
    public void setGateway(String gateway) {
        this.gatewayIP = gateway;
    }
}
