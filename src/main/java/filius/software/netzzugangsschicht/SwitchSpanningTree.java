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
package filius.software.netzzugangsschicht;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import filius.gui.netzwerksicht.GUINodeItem;
import filius.hardware.knoten.Node;
import filius.hardware.knoten.Switch;

// This class applies a kind of spanning tree to prevent loops
public class SwitchSpanningTree {
	
//	public SwitchSpanningTree() {
//		
//	}
	
    // Called recursively from applySpanningTree
    private void createConnectedList(Switch sw, LinkedList<Switch> sl) {
    	
    	// Create a list of all switches directly connected to sw
    	LinkedList<Switch> csl = sw.getConnectedSwitchList();
    	
    	// Remove from this list the switches already in sl
    	Iterator<Switch> it = csl.iterator();
    	while (it.hasNext()) {
    		Switch s = it.next(); 
    		if (sl.contains(s)) it.remove();
    	}
    	
    	// Add the remaining switches to sl
    	for (Switch cs : csl) sl.add(cs);
    	
    	// Call createConnectedList for each of the remaining switches 
    	// in order to get the next level of switches
    	for (Switch cs : csl) createConnectedList(cs, sl);
    	
    }       
 
    public void apply(List<GUINodeItem> nodeList) {                               
    	
    	// Create a list of all switches
    	LinkedList<Switch> asl = new LinkedList<Switch>();    	
    	for (GUINodeItem nodeItem : nodeList) {
            
            Node node = nodeItem.getNode();
            if (node instanceof Switch)	asl.add((Switch)node);          
        }
    	
    	// Unblock all ports
    	for (Switch sw : asl) sw.resetSpanningTree();      
    	
    	// Apply the Spanning Tree    	
    	while (!asl.isEmpty()) {

    		// The root switch is just the first in the list for which the ST was not already applied
    		// (there is one root switch for each group of interconnected switches)
    		Switch rootSwitch = null;
    		for (Switch sw : asl) {
                if (!sw.isConnectedToRoot()) {
                	rootSwitch = sw;
                	break; // for
                }                      
            }  
    		
    		if (rootSwitch == null) break; // while    		
    		
    		// Create a list of all switches linked to the root, the closest first
    		LinkedList<Switch> sl = new LinkedList<Switch>(); 
    		sl.add(rootSwitch); 
    		createConnectedList(rootSwitch, sl);
    		
    		// Block the redundant ports
    		for (Switch sw : sl) sw.blockRedundantPorts();    		
    	}     
    } 
}
