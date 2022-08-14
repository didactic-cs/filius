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

import java.util.LinkedList;

import filius.Main;
import filius.hardware.cables.HCable;
import filius.simul.os.net.layer1.link.EthernetFrame;

public class DuplexConnection implements Runnable {

    private boolean threadRunning = false;
    private HCable cable = null;
    private Port port1 = null;
    private Port port2 = null;

    /**
     * <b>SimplexConnection</b> this class models a simplex connection. It is driven by a thread.
     * Each cable has two simplex connections, one for each direction.
     * 
     * @param port1
     *            Port where the data is emitted.
     * @param port2
     *            Port where the data is received.
     * @param cable
     *            Cable to which this simplex connection belongs.
     *            
     * @author Carsten
     */
    public DuplexConnection(Port port1, Port port2, HCable cable) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() +
                           " (SimplexConnection), constr: SimplexConnection(" + port1 + "," + port2 + "," + cable + ")");
        
        this.port1 = port1;
        this.port2 = port2;
        this.cable = cable;
    }

    /**
     * This method is only runnable and should not be called directly.
     * It is the method run by the thread attached to the simplex connection.  
     * It is responsible for transporting a frame from port1.outputBuffer to
     * port2.inputBuffer and for letting the cable blink.
     *         
     * @author Carsten         
     */
    public void run() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (SimplexConnection), run()");
        
        LinkedList<EthernetFrame> out1 = port1.getOutputBuffer();
        LinkedList<EthernetFrame> in2  = port2.getInputBuffer();
        
        EthernetFrame frame;
        
        while (threadRunning) {
        	
        	synchronized (out1) {
        		// Wait for content
        		if (out1.size() == 0) {
        			try {
        				cable.setActive(false);
        				out1.wait();
        			} catch (InterruptedException e) {}
        		}
        		// Transport the content to the other end of the cable
        		if (out1.size() > 0) {
        			frame = (EthernetFrame) out1.getFirst();
        			out1.remove(frame);
        		}
        		else frame = null;
        	}

        	if (frame != null) {
        		
        		// Simulate transportation
        		cable.setActive(true);
        		synchronized (this) {
        			try {
        				Thread.sleep(HCable.getDelay());
        			} catch (InterruptedException e) {}
        		}

        		synchronized (in2) {
        			in2.add(frame/*.clone()*/); // clone() still required?
        			in2.notify();
        		}
        	}
        	
        }
    }
    
//    public void run() {
//        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (SimplexConnection), run()");
//        
//        EthernetFrame frame;
//
//        while (threadRunning) {
//            synchronized (port1.getOutputBuffer()) {
//                if (port1.getOutputBuffer().size() == 0) {
//                    try {
//                        cable.setActive(false);
//                        port1.getOutputBuffer().wait();
//                    } catch (InterruptedException e) {}
//                }
//                if (port1.getOutputBuffer().size() > 0) {
//                    cable.setActive(true);
//                    frame = (EthernetFrame) port1.getOutputBuffer().getFirst();
//                    port1.getOutputBuffer().remove(frame);
//
//                    synchronized (this) {
//                        try {
//                            Thread.sleep(HCable.getDelay());
//                        } catch (InterruptedException e) {}
//                    }
//
//                    synchronized (port2.getInputBuffer()) {
//                        port2.getInputBuffer().add(frame.clone());
//                        port2.getInputBuffer().notify();
//                    }
//                }
//            }
//        }
//    }

    public Port getPort1() {
        return port1;
    }

    public void setPort1(Port port1) {
        this.port1 = port1;
    }

    public Port getPort2() {
        return port2;
    }

    public void setPort2(Port port2) {
        this.port2 = port2;
    }

    public void disconnectPorts() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (SimplexConnection), disconnectPorts()");
        
        port1.removeCable();
        port2.removeCable();
        this.setThreadRunning(false);
    }

    public HCable getCable() {
        return cable;
    }

    public void setCable(HCable cable) {
        this.cable = cable;
    }

    public boolean isThreadRunning() {
        return threadRunning;
    }

    public void setThreadRunning(boolean threadRunning) {
        this.threadRunning = threadRunning;
    }
}
