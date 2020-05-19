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
package filius.software.rip;

import java.util.Random;

import filius.exception.VerbindungsException;
import filius.hardware.NetzwerkInterface;
import filius.hardware.knoten.InternetKnoten;
import filius.software.clientserver.ClientAnwendung;
import filius.software.system.VermittlungsrechnerBetriebssystem;
import filius.software.transportschicht.UDPSocket;

/**
 * 
 * @author pyropeter
 * @author stefanf
 */
public class RIPBeacon extends ClientAnwendung {
    private Random rand;

    public void starten() {
        super.starten();

        rand = new Random();

        ausfuehren("announce", null);
    }

    public void announce() {
        VermittlungsrechnerBetriebssystem bs = (VermittlungsrechnerBetriebssystem) getSystemSoftware();
        RIPTable table = bs.getRIPTable();

        UDPSocket sock;
        try {
            sock = new UDPSocket(bs, "255.255.255.255", 520, 521);
            sock.verbinden();
        } catch (VerbindungsException e) {
            return;
        }

        while (running) {
            synchronized (table) {
                while (table.getNextBeacon() - RIPUtil.getTime() > 0) {
                    try {
                        table.wait(table.getNextBeacon() - RIPUtil.getTime());
                    } catch (InterruptedException e) {}
                }

                table.check();
                broadcast(sock, bs, table);

                table.setNextBeacon(RIPUtil.getTime() + (int) (RIPTable.INTERVAL * (rand.nextFloat() / 3 + 0.84)));
            }
        }

        sock.beenden();
    }

    public void broadcast(UDPSocket sock, VermittlungsrechnerBetriebssystem bs, RIPTable table) {
        InternetKnoten knoten = (InternetKnoten) bs.getKnoten();

        RIPMessage msg;

        for (NetzwerkInterface nic : knoten.getNetzwerkInterfaces()) {
            msg = new RIPMessage(nic.getIp(), bs.holeIPAdresse(), RIPTable.INFINITY, RIPTable.TIMEOUT);
            for (RIPRoute route : table.routes) {
                // split horizon:
                if (nic.getIp().equals(route.getInterfaceIpAddress())) {
                    continue;
                }
                msg.addRoute(new RIPMessageRoute(route.getNetAddress(), route.getNetMask(), route.hops));
            }
            sock.bind(nic.getIp());
            sock.senden(msg.toString());
        }
    }
}
