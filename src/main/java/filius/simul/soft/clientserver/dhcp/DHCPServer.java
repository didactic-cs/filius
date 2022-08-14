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
package filius.software.dhcp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;

import filius.Main;
import filius.exception.AddressRequestNotAcceptedException;
import filius.exception.NoAvailableAddressException;
import filius.hardware.Cable;
import filius.rahmenprogramm.EntryValidator;
import filius.software.clientserver.UDPServerAnwendung;
import filius.software.transportschicht.Socket;

/**
 * In dieser Klasse und in DHCPServerMitarbeiter wird ein Sever fuer Dynamic Host Configuration Protocol implementiert. <br />
 * In dieser Klasse werden die Einstellungen des Server verwaltet. Das Protokoll zur Vereinbarung einer IP-Adresse wird
 * durch DHCPServerMitarbeiter realisiert. D. h. zu jeder eingehenden Anfrage wird ein neuer Mitarbeiter erzeugt.
 */

public class DHCPServer extends UDPServerAnwendung {

    private static final String DEFAULT_IP_ADDRESS = "0.0.0.0";
    /** wie lange ein DHCP-Eintrag gueltig sein soll bzw. die IP-Adresse einer MAC zugewiesen bleibt (Standardwert) */
    private static final long DEFAULT_LEASE_TIME_MILLIS = 24 * 60 * 60 * 1000;
    private static final int DHCP_SERVER_PORT = 67;

    /** niedrigste IP-Adresse, die durch diesen DHCP-Server vergeben wird */
    private String lowerLimit = "192.168.0.200";
    /** hoechste IP-Adresse, die durch diesen DHCP-Server vergeben wird */
    private String upperLimit = "192.168.0.254";
    /** setting of DHCP server for the router/gateway (not necessarily equal to operating system settings) */
    private String dhcpGateway = DEFAULT_IP_ADDRESS;
    /** setting of DHCP server for the DNS server (not necessarily equal to operating system settings) */
    private String dhcpDNS = DEFAULT_IP_ADDRESS;
    /** whether to use the operating system or the dhcp settings for the attributes router/gateway and DND server */
    private boolean useOwnGWAndDNSSettings = false;

    /** Die zuletzt vergebene IP-Adresse */
    String lastOfferedAddress = null;
    /** Liste mit dynamisch vergebenen IP-Adressen mit zugehoeriger MAC-Adresse */
    List<DHCPAddressItem> dynamicAssignedAddresses = new ArrayList<DHCPAddressItem>();
    /** Liste mit statisch vergebenen IP-Adressen mit zugehoeriger MAC-Adresse */
    List<DHCPAddressItem> reservedAddresses = new ArrayList<DHCPAddressItem>();
    /** Liste mit angebotenen IP-Adressen mit zugehoeriger MAC-Adresse */
    List<DHCPAddressItem> offeredAddresses = new ArrayList<DHCPAddressItem>();
    /** Liste mit IP-Adressen, die von anderen Servern angeboten wurden. */
    List<DHCPAddressItem> blacklist = new ArrayList<DHCPAddressItem>();

    /** Konstruktor, in dem der UDP-Port 67 gesetzt wird. */
    public DHCPServer() {
        super();
        port = DHCP_SERVER_PORT;
    }

    String nextAddress() {
        String nextAddress;
        if (StringUtils.isBlank(lastOfferedAddress)) {
            nextAddress = lowerLimit;
        } else {
            long address = ipToLong(lastOfferedAddress) + 1;
            long maxIP = ipToLong(upperLimit);
            if (address <= maxIP) {
                nextAddress = longToIp(address);
            } else {
                nextAddress = lowerLimit;
            }
        }
        lastOfferedAddress = nextAddress;
        return nextAddress;
    }

    public synchronized String offerAddress(String mac) throws NoAvailableAddressException {
        String addressToOffer = findStaticOffer(mac);
        if (addressToOffer == null) {
            addressToOffer = findDynamicOffer(mac);
        }
        return addressToOffer;
    }

    private String findDynamicOffer(String mac) throws NoAvailableAddressException {
        String addressToOffer;
        addressToOffer = nextAddress();
        String firstOffer = addressToOffer;
        while (!checkAddressAvailable(addressToOffer)) {
            addressToOffer = nextAddress();
            if (StringUtils.equals(firstOffer, addressToOffer)) {
                throw new NoAvailableAddressException();
            }
        }
        long leaseTime = System.currentTimeMillis() + 4 * Cable.getRTT();
        offeredAddresses.add(new DHCPAddressItem(mac, addressToOffer, leaseTime));
        return addressToOffer;
    }

    private String findStaticOffer(String mac) {
        String addressToOffer = null;
        for (DHCPAddressItem entry : reservedAddresses) {
            if (StringUtils.equalsIgnoreCase(mac, entry.getMAC())) {
                addressToOffer = entry.getIP();
                break;
            }
        }
        return addressToOffer;
    }

    public synchronized void blacklistAddress(String ip) {
        long leaseTime = System.currentTimeMillis() + 4 * Cable.getRTT();
        blacklist.add(new DHCPAddressItem("", ip, leaseTime));
    }

    public synchronized DHCPAddressItem requestAddress(String mac, String ip)
            throws AddressRequestNotAcceptedException {
        if (assignmentListContains(blacklist, ip)) {
            throw new AddressRequestNotAcceptedException();
        }
        DHCPAddressItem assignment = requestStaticAssignment(mac, ip);
        if (assignment == null) {
            assignment = requestDynamicAssignment(mac, ip);
        }
        return assignment;
    }

    private DHCPAddressItem requestStaticAssignment(String mac, String ip) {
        DHCPAddressItem assignment = null;
        for (DHCPAddressItem entry : reservedAddresses) {
            if (StringUtils.equalsIgnoreCase(mac, entry.getMAC()) && StringUtils.equalsIgnoreCase(ip, entry.getIP())) {
                assignment = entry;
                break;
            }
        }
        return assignment;
    }

    private DHCPAddressItem requestDynamicAssignment(String mac, String ip)
            throws AddressRequestNotAcceptedException {
        boolean success = false;
        for (DHCPAddressItem assignment : offeredAddresses) {
            if (StringUtils.equalsIgnoreCase(ip, assignment.getIP())) {
                if (StringUtils.equalsIgnoreCase(mac, assignment.getMAC())) {
                    success = true;
                    offeredAddresses.remove(assignment);
                    break;
                } else {
                    success = false;
                    break;
                }
            }
        }
        if (!success && checkAddressAvailable(ip)) {
            success = true;
        }
        DHCPAddressItem assignment;
        if (success) {
            assignment = new DHCPAddressItem(mac, ip, System.currentTimeMillis() + DEFAULT_LEASE_TIME_MILLIS);
            dynamicAssignedAddresses.add(assignment);
        } else {
            throw new AddressRequestNotAcceptedException();
        }
        return assignment;
    }

    public String getServerIpAddress() {
        return getSystemSoftware().getIPAddress();
    }

    /**
     * Zur Pruefung, ob eine bestimmte IP-Adresse bereits vergeben ist. <br />
     * Zunaechst wird dazu die Liste vergebener IP-Adressen aufgeraeumt (d.h. abgelaufene Eintraege entfernt).
     */
    synchronized boolean checkAddressAvailable(String ip) {
        cleanUpAssignments();

        boolean available = true;
        if (assignmentListContains(dynamicAssignedAddresses, ip)) {
            available = false;
        } else if (assignmentListContains(reservedAddresses, ip)) {
            available = false;
        } else if (assignmentListContains(offeredAddresses, ip)) {
            available = false;
        } else if (assignmentListContains(blacklist, ip)) {
            available = false;
        }

        return available;
    }

    private boolean assignmentListContains(List<DHCPAddressItem> assignmentList, String ip) {
        boolean contains = false;
        for (DHCPAddressItem assignment : assignmentList) {
            if (assignment.getIP().equalsIgnoreCase(ip)) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    public void setUseOwnGWAndDNSSettings(boolean val) {
        this.useOwnGWAndDNSSettings = val;
    }
    
    public boolean getUseOwnGWAndDNSSettings() {
        return useOwnGWAndDNSSettings;
    }

    /**
     * Entfernt abgelaufene Eintraege. Wird automatisch vor jeder Suche nach der naechsten freien IP-Adresse
     * ausgefuehrt.
     */
    synchronized void cleanUpAssignments() {
        cleanUpAssignmentList(dynamicAssignedAddresses);
        cleanUpAssignmentList(reservedAddresses);
        cleanUpAssignmentList(offeredAddresses);
        cleanUpAssignmentList(blacklist);
    }

    private void cleanUpAssignmentList(List<DHCPAddressItem> assignments) {
        List<DHCPAddressItem> expiredAssignments = new LinkedList<>();
        for (DHCPAddressItem assignment : assignments) {
            if (assignment.isExpired()) {
                expiredAssignments.add(assignment);
            }
        }
        for (DHCPAddressItem expired : expiredAssignments) {
            assignments.remove(expired);
        }
    }

    public void startThread() {
        Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
                + " (DHCPServer), starten()");
        dynamicAssignedAddresses.clear();
        lastOfferedAddress = null;
        super.startThread();
    }

    /**
     * Diese Methode wird bei der ersten eingehenden DHCP-Anfrage aufgerufen. Der DHCP-Server verfuegt naemlich nur
     * ueber einen Mitarbeiter, weil es nur einen Port mit der Gegenstelle "0.0.0.0:68" gibt.
     */
    protected void neuerMitarbeiter(Socket socket) {
        Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
                + " (DHCPServer), neuerMitarbeiter(" + socket + ")");
        DHCPServerMitarbeiter dhcpMitarbeiter;

        dhcpMitarbeiter = new DHCPServerMitarbeiter(this, socket);
        dhcpMitarbeiter.starten();
        mitarbeiter.add(dhcpMitarbeiter);
    }

    boolean inRange(String ipAddress) {
        long minIP = ipToLong(lowerLimit);
        long maxIP = ipToLong(upperLimit);
        long ip = ipToLong(ipAddress);
        return (ip <= maxIP) && (ip >= minIP);
    }

    static int[] ipToIntArray(String ipAddress) {
        if (!EntryValidator.isValid(ipAddress, EntryValidator.musterIpAdresse)) {
            throw new NumberFormatException("Not a valid IP address");
        }
        int[] ipAsArray = new int[4];
        StringTokenizer tokenizer = new StringTokenizer(ipAddress, ".");
        for (int i = 0; i < ipAsArray.length; i++) {
            ipAsArray[i] = Integer.parseInt(tokenizer.nextToken());
        }
        return ipAsArray;
    }

    static long ipToLong(String ipAddress) {
        int[] ipIntArray = ipToIntArray(ipAddress);
        long number = 0;
        for (int value : ipIntArray) {
            number = 256 * number + value;
        }
        return number;
    }

    static String longToIp(long ipAsLong) {
        long firstByte = (ipAsLong & 255 << 24) >> 24;
        long secondByte = (ipAsLong & 255 << 16) >> 16;
        long thirdByte = (ipAsLong & 255 << 8) >> 8;
        long fourthByte = ipAsLong & 255;
        return String.format("%d.%d.%d.%d", firstByte, secondByte, thirdByte, fourthByte);
    }

    public String getUpperLimit() {
        return upperLimit;
    }

    public void setUpperLimit(String upperLimit) {
        this.upperLimit = upperLimit;
    }

    public String getLowerLimit() {
        return lowerLimit;
    }

    public void setLowerLimit(String lowerLimit) {
        this.lowerLimit = lowerLimit;
    }

    public String getDNSServerIP() {
        String dns;
        if (useOwnGWAndDNSSettings) {
            dns = dhcpDNS;
        } else {
            dns = getSystemSoftware().getDNSServer();
            if (StringUtils.isEmpty(dns)) {
                dns = DEFAULT_IP_ADDRESS;
            }
        }
        return dns;
    }

    public void setDNSServerIP(String ip) {
        this.dhcpDNS = ip;
    }

    public String getGatewayIP() {
        String gateway;
        if (useOwnGWAndDNSSettings) {
            gateway = dhcpGateway;
        } else {
            gateway = getSystemSoftware().getStandardGateway();
            if (StringUtils.isEmpty(gateway)) {
                gateway = DEFAULT_IP_ADDRESS;
            }
        }
        return gateway;
    }

    public void setGatewayIP(String ip) {
        this.dhcpGateway = ip;
    }

    public String getSubnetMask() {
        return getSystemSoftware().getSubnetMask();
    }

    public List<DHCPAddressItem> getReservedAddresses() {
        return Collections.unmodifiableList(reservedAddresses);
    }

    public List<String> getStaticAssignedAddresses() {
        List<String> entries = new ArrayList<>();
        for (DHCPAddressItem entry : reservedAddresses) {
            entries.add(String.format("%s %s", entry.getMAC(), entry.getIP()));
        }
        return entries;
    }

    public void setStaticAssignedAddresses(List<String> assignedAddresses) {
        reservedAddresses.clear();
        for (String entry : assignedAddresses) {
            String[] pair = StringUtils.split(entry);
            reservedAddresses.add(new DHCPAddressItem(pair[0], pair[1], 0));
        }
    }

    public void addReservedAddress(String mac, String ip) {
        boolean alreadyExisting = false;
        for (DHCPAddressItem entry : reservedAddresses) {
            if (StringUtils.equalsIgnoreCase(entry.getMAC(), mac)) {
                alreadyExisting = true;
                break;
            }
        }
        if (!alreadyExisting && EntryValidator.isValid(mac, EntryValidator.musterMacAddress)
                && EntryValidator.isValid(ip, EntryValidator.musterIpAdresse)) {
            reservedAddresses.add(new DHCPAddressItem(mac, ip, 0));
        }
    }

    public void clearReservedAddresses() {
        reservedAddresses.clear();
    }
}
