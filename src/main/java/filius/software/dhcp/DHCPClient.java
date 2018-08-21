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
import filius.exception.VerbindungsException;
import filius.gui.GUIContainer;
import filius.gui.netzwerksicht.GUIKnotenItem;
import filius.hardware.Verbindung;
import filius.hardware.knoten.Host;
import filius.software.clientserver.ClientAnwendung;
import filius.software.system.Betriebssystem;
import filius.software.system.InternetKnotenBetriebssystem;
import filius.software.system.SystemSoftware;
import filius.software.transportschicht.UDPSocket;
import filius.software.vermittlungsschicht.ARP;

public class DHCPClient extends ClientAnwendung {
    enum State {
        ASSIGN_IP, FINISH, DISCOVER, REQUEST, VALIDATE, DECLINE, INIT
    }

    private static final String IP_ADDRESS_CURRENT_NETWORK = "0.0.0.0";
    private static final int MAX_ERROR_COUNT = 10;

    private State zustand;

    public void starten() {
        Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
                + " (DHCPClient), starten()");
        super.starten();

        ausfuehren("configure", null);
    }

    /**
     * es muss gewaehrleistet werden, dass der DHCP-Server bereits gestartet worden ist! (sofern denn einer als "aktiv"
     * gekennzeichnet ist!)
     */
    void waitUntilDhcpServerStarted() {
        boolean activeDHCPserversStarted = false;
        try {
            while (!activeDHCPserversStarted && running) {
                activeDHCPserversStarted = true;
                for (DHCPServer dhcpServer : getDHCPServers()) {
                    if (dhcpServer.isAktiv() && !dhcpServer.isStarted()) {
                        activeDHCPserversStarted = false;
                        Main.debug.println("WARNING (" + this.hashCode() + "): DHCP server on '"
                                + dhcpServer.getSystemSoftware().getKnoten().holeAnzeigeName()
                                + "' has NOT been started --> waiting");
                        break;
                    } else {
                        Main.debug.println("DHCPClient:\tserver on '"
                                + dhcpServer.getSystemSoftware().getKnoten().getName() + "' has been started");
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
        for (GUIKnotenItem knotenItem : GUIContainer.getGUIContainer().getKnotenItems()) {
            syssoft = knotenItem.getKnoten().getSystemSoftware();
            if (syssoft instanceof Betriebssystem) {
                if (((Betriebssystem) syssoft).getDHCPServer().isAktiv()) {
                    activeDHCPServers.add(((Betriebssystem) syssoft).getDHCPServer());
                }
            }
        }
        return activeDHCPServers;
    }

    public void configure() {
        Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
                + " (DHCPClient), starteDatenaustausch()");
        int fehlerzaehler = 0;
        zustand = INIT;

        InternetKnotenBetriebssystem operatingSystem = (InternetKnotenBetriebssystem) getSystemSoftware();
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
                    config = discover(udpSocket, operatingSystem.holeMACAdresse(), Verbindung.holeRTT());
                    zustand = VALIDATE;
                    break;
                case VALIDATE:
                    boolean validAddress = validateOfferedAddress(getSystemSoftware().holeARP(), config.getIpAddress());
                    zustand = validAddress ? REQUEST : DECLINE;
                    break;
                case DECLINE:
                    decline(udpSocket, operatingSystem.holeMACAdresse(), config, Verbindung.holeRTT());
                    zustand = DISCOVER;
                    break;
                case REQUEST:
                    boolean acknowledged = request(udpSocket, operatingSystem.holeMACAdresse(), config,
                            Verbindung.holeRTT());
                    zustand = acknowledged ? ASSIGN_IP : DISCOVER;
                    break;
                case ASSIGN_IP:
                    operatingSystem.setzeIPAdresse(config.getIpAddress());
                    operatingSystem.setzeNetzmaske(config.getSubnetMask());
                    operatingSystem.setStandardGateway(config.getRouter());
                    operatingSystem.setDNSServer(config.getDnsServer());
                default:
                    zustand = FINISH;
                }
            } catch (NoValidDhcpResponseException | TimeOutException | VerbindungsException e) {
                fehlerzaehler++;
            }
        }
        if (fehlerzaehler == MAX_ERROR_COUNT && running) {
            Main.debug.println("ERROR (" + this.hashCode() + "): kein DHCPACK erhalten");
            operatingSystem.setzeIPAdresse(oldIpAddress);
        }
        udpSocket.schliessen();

        Host host = ((Host) getSystemSoftware().getKnoten());
        host.benachrichtigeBeobachter();
        getSystemSoftware().benachrichtigeBeobacher(host);
    }

    private String resetIpConfig() {
        InternetKnotenBetriebssystem operatingSystem = (InternetKnotenBetriebssystem) getSystemSoftware();
        String oldIpAddress = operatingSystem.holeIPAdresse();
        operatingSystem.setzeIPAdresse("0.0.0.0");
        return oldIpAddress;
    }

    UDPSocket initUdpSocket() throws VerbindungsException {
        socket = new UDPSocket(getSystemSoftware(), "255.255.255.255", 67, 68);
        ((UDPSocket) socket).verbinden();
        return (UDPSocket) socket;
    }

    boolean request(UDPSocket socket, String clientMacAddress, IpConfig config, long socketTimeoutMillis)
            throws NoValidDhcpResponseException, TimeOutException {
        socket.sendeBroadcast(IP_ADDRESS_CURRENT_NETWORK,
                DHCPMessage.createRequestMessage(clientMacAddress, config.getIpAddress(), config.getDhcpServer())
                        .toString());

        DHCPMessage result = receiveResponse(socket, socketTimeoutMillis, clientMacAddress, config.getDhcpServer(),
                DHCPMessageType.ACK, DHCPMessageType.NACK);

        return null != result && result.getType() == DHCPMessageType.ACK;
    }

    void decline(UDPSocket socket, String clientMacAddress, IpConfig config, long socketTimeoutMillis)
            throws NoValidDhcpResponseException {
        socket.sendeBroadcast(IP_ADDRESS_CURRENT_NETWORK,
                DHCPMessage.createDeclineMessage(clientMacAddress, config.getIpAddress(), config.getDhcpServer())
                        .toString());
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
            boolean fromOtherServer = null != serverIdentifier
                    && !serverIdentifier.equals(responseMessage.getServerIdentifier());
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
