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
package filius.software.system;

import java.util.Vector;

import filius.software.system.FiliusFileSystem.FileType;

public class FiliusFileFilter {
	
	private String description = "";
	private Vector<FileType> filetypes = new Vector<FileType>(1); 
	
	public FiliusFileFilter(String description, FileType... filetype) {
		
		this.description = description;
		if (filetype.length > 1) filetypes.setSize(filetype.length);
		for (FileType ft:filetype) filetypes.add(ft);		
	}	
	
	public String getDescription() {
		
		return description;
	}
	
	public FileType getFirstType() {
		
		if (filetypes.size() == 0) return FileType.UNKNOWN;
		
		return filetypes.get(0);
	}
	
	public boolean accept(FiliusFileNode filenode) {
		
		return filetypes.contains(filenode.getType());
    }
}
