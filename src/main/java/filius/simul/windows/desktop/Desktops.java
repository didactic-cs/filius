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
package filius.simul.windows.desktop;

import java.util.LinkedList;
import java.util.List;

import filius.simul.os.HostOS;

public class GUIDesktops {
	
	private List<GUIDesktopWindow> desktops;
	
	
	public GUIDesktops() {
		
		desktops = new LinkedList<GUIDesktopWindow>();
	}

	// Returns the number of desktops whose position on screen is initialized 
    public int getInitializedDesktopCount() {
    	
    	int count = 0;
    	for (GUIDesktopWindow desktop: desktops) {
    		if (desktop.isInitialized())  count++;
    	}
    	return count;
    } 
    
    // Returns the desktop window associated to the OS
    public GUIDesktopWindow getDesktop(HostOS os) {
    	
    	if (os == null)  return null;
        
    	for (GUIDesktopWindow desktop: desktops) {
    		if (desktop.getOS() == os)  return desktop;
    	}
        
        // If no desktop is associated to the OS yet, create one.
        GUIDesktopWindow desktop = new GUIDesktopWindow(os);
    	desktops.add(desktop);
        return desktop;
    }

    public void showDesktop(HostOS os) {
    	
    	if (os == null)  return;
        
        GUIDesktopWindow desktopWindow = getDesktop(os);       
        if (desktopWindow == null)  return; // < Should never happen
        
        desktopWindow.setVisible(true);
    }
    
    // Called everytime a project is loaded or created
    public void resetDesktopWindows() {
    	
    	desktops.clear();	
    }
    
    public void removeDesktopWindow(HostOS os) {
    	
    	if (os == null)  return;
    	
    	for (GUIDesktopWindow desktop: desktops) {
    		
    		if (desktop.getOS() == os) {
    	        desktops.remove(desktop);
    	        desktop.destroy();
    	        return;
    		}
    	}
    }
}
