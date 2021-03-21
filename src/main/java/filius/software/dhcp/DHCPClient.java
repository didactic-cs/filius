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

import static filius.software.dhcp.DHCPClient.State.ASSIGN_IP;
import static filius.software.dhcp.DHCPClient.State.DECLINE;
import static filius.software.dhcp.DHCPClient.State.DISCOVER;
import static filius.software.dhcp.DHCPClient.State.FINISH;
import static filius.software.dhcp.DHCPClient.State.INIT;
import static filius.software.dhcp.DHCPClient.State.REQUEST;
import static filius.software.dhcp.DHCPClient.State.VALIDATE;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import filius.Main;
import filius.exception.NoValidDhcpResponseException;
import filius.exception.TimeOutException;
import filius.exception.ConnectionException;
import filius.gui.GUIContainer;
import filius.gui.netzwerksicht.GUINodeItem;
import filius.hardware.Cable;
import filius.software.clientserver.ClientApplication;
import filius.software.system.HostOS;
import filius.software.system.InternetNodeOS;
import filius.software.system.SystemSoftware;
import filius.software.transportschicht.UDPSocket;
import filius.software.vermittlungsschicht.ARP;

public class DHCPClient extends ClientApplication {
    enum State {
        ASSIGN_IP, FINISH, DISCOVER, REQUEST, VALIDATE, DECLINE, INIT
    }

    private static final String IP_ADDRESS_CURRENT_NETWORK = "0.0.0.0";
    private static final int MAX_ERROR_COUNT = 10;

    private State zustand;

    public void startThread() {
        Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
                + " (DHCPClient), starten()");
        super.startThread();

        execute("configure", null);
    }

    /**
     * es muss gewaehrleistet werden, dass der DHCP-Server bereits gestartet worden ist! 
     * (sofern denn einer als "aktiv" gekennzeichnet ist!)
     */
    void waitUntilDhcpServerStarted() {
        boolean activeDHCPserversStarted = false;
        try {
            while (!activeDHCPserversStarted && running) {
                activeDHCPserversStarted = true;
                for (DHCPServer dhcpServer : getDHCPServers()) {
                    if (dhcpServer.isActive() && !dhcpServer.isStarted()) {
                        activeDHCPserversStarted = false;
                        Main.debug.println("WARNING (" + this.hashCode() + "): DHCP server on '"
                                + dhcpServer.getSystemSoftware().getNode().getDisplayName()
                                + "' has NOT been started --> waiting");
                        break;
                    } else {
                        Main.debug.println("DHCPClient:\tserver on '"
                                + dhcpServer.getSystemSoftware().getNode().getName() + "' has been started");
                    }
                }
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            e.printStackTrace(Main.debug);
        }
    }

    private List<DHCPServer> getDHCPServers() {
        SystemSoftware syssoft;
        List<DHCPServer> activeDHCPServers = new ArrayList<DHCPServer>();
        for (GUINodeItem knotenItem : GUIContainer.getInstance().getNodeItems()) {
            syssoft = knotenItem.getNode().getSystemSoftware();
            if (syssoft instanceof HostOS) {
                if (((HostOS) syssoft).getDHCPServer().isActive()) {
                    activeDHCPServers.add(((HostOS) syssoft).getDHCPServer());
                }
            }
        }
        return activeDHCPServers;
    }

    public void configure() {
        Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass() +
                           " (DHCPClient), starteDatenaustausch()");
        int fehlerzaehler = 0;
        zustand = INIT;

        InternetNodeOS operatingSystem = (InternetNodeOS) getSystemSoftware();
        String oldIpAddress = resetIpConfig();
        IpConfig config = null;
        UDPSocket udpSocket = null;
        while (zustand != FINISH && fehlerzaehler < MAX_ERROR_COUNT && running) {
            try {
                switch (zustand) {
                case INIT:
                    waitUntilDhcpServerStarted();
                    udpSocket = initUdpSocket();
                    zustand = DISCOVER;
                    break;
                case DISCOVER:
                    config = discover(udpSocket, operatingSystem.getMACAddress(), Cable.getRTT());
                    zustand = VALIDATE;
                    break;
                case VALIDATE:
                    boolean validAddress = validateOfferedAddress(getSystemSoftware().getARP(), config.getIpAddress());
                    zustand = validAddress ? REQUEST : DECLINE;
                    break;
                case DECLINE:
                    decline(udpSocket, operatingSystem.getMACAddress(), config, Cable.getRTT());
                    zustand = DISCOVER;
                    break;
                case REQUEST:
                    boolean acknowledged = request(udpSocket, operatingSystem.getMACAddress(), config, Cable.getRTT());
                    zustand = acknowledged ? ASSIGN_IP : DISCOVER;
                    break;
                case ASSIGN_IP:
                    operatingSystem.setIPAddress(config.getIpAddress());
                    operatingSystem.setSubnetMask(config.getSubnetMask());
                    operatingSystem.setStandardGateway(config.getRouter());
                    operatingSystem.setDNSServer(config.getDnsServer());
                default:
                    zustand = FINISH;
                }
            } catch (NoValidDhcpResponseException | TimeOutException | ConnectionException e) {
                fehlerzaehler++;
            }
        }
        if (fehlerzaehler == MAX_ERROR_COUNT && running) {
            Main.debug.println("ERROR (" + this.hashCode() + "): kein DHCPACK erhalten");
            operatingSystem.setIPAddress(oldIpAddress);
        }
        udpSocket.schliessen();                   
    }

    private String resetIpConfig() {
        InternetNodeOS operatingSystem = (InternetNodeOS) getSystemSoftware();
        String oldIpAddress = operatingSystem.getIPAddress();
        operatingSystem.setIPAddress("0.0.0.0");
        return oldIpAddress;
    }

    UDPSocket initUdpSocket() throws ConnectionException {
        socket = new UDPSocket(getSystemSoftware(), "255.255.255.255", 67, 68);
        ((UDPSocket) socket).verbinden();
        return (UDPSocket) socket;
    }

    boolean request(UDPSocket socket, String clientMacAddress, IpConfig config, long socketTimeoutMillis)
            throws NoValidDhcpResponseException, TimeOutException {
        socket.sendeBroadcast(IP_ADDRESS_CURRENT_NETWORK,
                DHCPMessage.createRequestMessage(clientMacAddress, config.getIpAddress(), config.getDhcpServer()).toString());

        DHCPMessage result = receiveResponse(socket, socketTimeoutMillis, clientMacAddress, config.getDhcpServer(),
                DHCPMessageType.ACK, DHCPMessageType.NACK);

        return null != result && result.getType() == DHCPMessageType.ACK;
    }

    void decline(UDPSocket socket, String clientMacAddress, IpConfig config, long socketTimeoutMillis)
            throws NoValidDhcpResponseException {
        socket.sendeBroadcast(IP_ADDRESS_CURRENT_NETWORK,
                DHCPMessage.createDeclineMessage(clientMacAddress, config.getIpAddress(), config.getDhcpServer()).toString());
    }

    IpConfig discover(UDPSocket socket, String clientMacAddress, long socketTimeoutMillis)
            throws NoValidDhcpResponseException, TimeOutException {
        socket.sendeBroadcast(IP_ADDRESS_CURRENT_NETWORK, DHCPMessage.createDiscoverMessage(clientMacAddress)
                .toString());

        DHCPMessage offer = receiveResponse(socket, socketTimeoutMillis, clientMacAddress, null, DHCPMessageType.OFFER);

        return new IpConfig(offer.getYiaddr(), offer.getRouter(), offer.getSubnetMask(), offer.getDnsServer(),
                offer.getServerIdentifier());
    }

    private DHCPMessage receiveResponse(UDPSocket socket, long socketTimeoutMillis, String clientMacAddress,
            String serverIdentifier, DHCPMessageType... messageTypes) throws NoValidDhcpResponseException,
            TimeOutException {
        DHCPMessage responseMessage = null;
        long start = System.currentTimeMillis();
        long duration = 0;
        do {
            String response = socket.empfangen(socketTimeoutMillis - duration);
            if (null == response) {
                throw new TimeOutException("No response from DHCP server received.");
            }
            responseMessage = DHCPMessage.fromString(response);
            boolean invalidMessageType = !ArrayUtils.contains(messageTypes, responseMessage.getType());
            boolean forOtherClient = !clientMacAddress.equalsIgnoreCase(responseMessage.getChaddr());
            boolean fromOtherServer = null != serverIdentifier && !serverIdentifier.equals(responseMessage.getServerIdentifier());
            if (invalidMessageType || forOtherClient || fromOtherServer) {
                responseMessage = null;
            }
            duration = System.currentTimeMillis() - start;
        } while (null == responseMessage && duration < socketTimeoutMillis);
        if (null == responseMessage) {
            throw new NoValidDhcpResponseException("No valid server response received");
        }
        return responseMessage;
    }

    boolean validateOfferedAddress(ARP arp, String offeredAddress) {
        String macAddress = arp.holeARPTabellenEintrag(offeredAddress);
        return null == macAddress;
    }
}
