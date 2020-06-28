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

import java.io.Serializable;
import java.util.ArrayList;

import filius.rahmenprogramm.Base64;
import filius.software.system.FiliusFileListener.ChangeType;

/**
 * The class <b>FiliusFile</b> is used to model a file in Filius.
 * The content of the file is stored as a string. Consequently, binary
 * contents need to be Base64 encoded to String.
 *
 * @see filius.software.system.FiliusFileSystem
 * @see filius.software.system.FiliusFileNode
 * 
 * @author Nadja & Thomas Gerding
 */
public class FiliusFile implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// File's name (also used by the toString() method)
	private String name;

	// File's type: text, web, image, sound, binary
	private String type;

	// File's content as String (binary data is Base64 encoded)
	private String content;	

	// Real size of the file without having enforced Base64 encoding
	private transient long decodedSize = -1;
	
	// List of event listeners
	private ArrayList<FiliusFileListener> listenerList = new ArrayList<FiliusFileListener>();

	/**
	 * <b>FiliusFile</b> models a file in the Filius file system.
	 * The serializability of the class is used to provide an easy way to store
	 * the associated content along with the TreeNode it is attached to.
	 * It is important to keep this constructor without parameter for the serialization.
	 */
	public FiliusFile() {
		
	}

	/**
	 * <b>FiliusFile</b> creates a new instance of the class FiliusFile
	 *
	 * @param name
	 *            String containing the name of the file
	 * @param type
	 *            String containing the type of the file
	 * @param content
	 *            String containing the content of the file. Binary content is Base64 encoded.
	 */
	public FiliusFile(String name, String type, String content) {		
		this.name = name;
		this.type = type;
		this.content = content;
	}	

	/**
	 * <b>getName</b> returns a String containing the name of the file.
	 *
	 * @return A String containing the file's name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * <b>setName</b> assigns a name to the file.
	 *
	 * @param name A String containing the name.
	 */
	public void setName(String name) {
		this.name = name;
		callFileListeners(ChangeType.NAME);
	}
	
	/**
	 * <b>getType</b> returns a String containing the type of the file.
	 *
	 * @return A String containing the file's type.
	 */
	public String getType() {
		if (type != null) return type;
		else return "";
	}

	/**
	 * <b>setType</b> assigns a type as a String.
	 *
	 * @param type A String containing the type.
	 */
	public void setType(String type) {
		this.type = type;
		callFileListeners(ChangeType.TYPE);
	}

	/**
	 * <b>toString</b> returns a String containing the name of the file.
	 *
	 * @return A String containing the file's name.
	 */
//	public String toString() {
//		return name;
//	}

	/**
	 * <b>getContent</b> returns the file's content as a String. That is, returns the content
	 * as it is internally stored (Base64 encoded in case the original data is binary).
	 *
	 * @return A String containing the file's content.
	 */
	public String getContent() {
		return content;
	}

	/**
	 * <b>setContent</b> assigns the file's content as a String. If the original data is in
	 * binary form, it should first be converted to a Base64 encoded String.
	 *
	 * @param content A String containing the file's content.
	 */
	public void setContent(String content) {
		this.content = content;
		callFileListeners(ChangeType.CONTENT);
	}
	
	/**
	 * <b>getSize</b> returns the size of the file's content. <br>
	 * For binary content, the returned value is the size of the data itself, not
	 * the size of the Base64 encoded string.
	 *
	 * @return A long integer the value of which is the real size of the content.
	 */
	public long getSize() {
		if (this.getContent() == null)
			return 0;
		if (this.type != null && this.type.equals("text"))
			return this.getContent().length();
		else {
			try {
				// Set current size to be sure it's correct
				if (decodedSize < 0) setSize(Base64.decode(this.getContent()).length);
				return decodedSize;
			} catch (Exception e) {
				// current file does not seem to be Base64 encoded (that's why
				// an error occurred... hopefully)
				if (decodedSize < 0)
					return this.getContent().length();
				else
					return decodedSize;
			}
		}
	}

	/**
	 * <b>setSize</b> sets the size of the file.
	 *
	 * @param size The desired size for the file.
	 */
	public void setSize(long size) {
		this.decodedSize = size;
	}
	
	//***************************************************************************************
	// Handling event listeners
	//***************************************************************************************
	
    /**
     * <b>addFileListener<b> adds a file listener to the list.
     * 
     * @param l the listener to be added
     */
    public void addFileListener(FiliusFileListener l) {
    	
        listenerList.add(l);
    }

    /**
     * <b>removeFileListener<b> removes a file listener from the list.
     *
     * @param l the listener to be removed
     */
    public void removeFileListener(FiliusFileListener l) {
    	
        listenerList.remove(l);
    }
    
    /**
     * <b>callFileListeners<b> calls every file listener in the list.
     *
     * @param l the listener to be removed
     */
    private void callFileListeners(ChangeType ct) {
    	
        for (FiliusFileListener l : listenerList) {
        	l.onChange(ct);
        }
    }

	// Never used methods
//	/**
//	 * <b>getDecodedContent</b> Methode fuer den Zugriff auf Base64-kodierte Dateien.
//	 *
//	 * @return Gibt den Base64-dekodierten Dateiinhalt als String zur�ck.
//	 * @see setDecodedContent
//	 */
//	public String getDecodedContent() {
//		return (String) Base64.decodeToObject(this.getContent());
//	}
//
//	/**
//	 * <b>setDecodedContent</b> Methode fuer den Zugriff auf Base64-kodierte Dateien.
//	 *
//	 * @param dateiInhalt
//	 *            einen "binaeren" Dateiinhalt, der Base64-kodiert gespeichert
//	 *            werden soll
//	 */
//	public void setDecodedContent(String content) {
//		this.content = Base64.encodeObject(content);
//	}
}