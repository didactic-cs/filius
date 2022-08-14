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
package filius.gui.netzwerksicht.config;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import filius.gui.GUIContainer;
import filius.gui.GUIHelper;
import filius.gui.JBackgroundPanel;
import filius.hardware.knoten.Switch;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.ProjectManager;

@SuppressWarnings("serial")
public class JConfigSwitch extends JConfigPanel implements I18n {

	private GUIHelper gui;
	private JTextField tfName;
	private JCheckBox  cbDisplayAsCloud;
	private boolean    modified = false;	
	

	/** 
     * {@inheritDoc}
     */
	@SuppressWarnings("static-access")
	@Override
	protected void initMainPanel(JBackgroundPanel mainPanel) {    
		
		gui = new GUIHelper();
   
        // Name of the switch
        
        ActionListener onModification = new ActionListener() {			
        	public void actionPerformed(ActionEvent e) { modified = true; }
		};
		
		ActionListener onEnter = new ActionListener() {			
			public void actionPerformed(ActionEvent e) { saveChanges(); }
		};

		tfName = gui.addTextField(messages.getString("jconfigswitch_msg1"), "", 
				                  mainPanel, 50, 12, 280, gui.TEXTFIELD_HEIGHT, 110, 
				                  onModification, onEnter, onEnter, null);

		// Display as cloud checkbox

        ActionListener onChange = new ActionListener() {			
			public void actionPerformed(ActionEvent e) { updateSwitchDisplay(); }
		};
		cbDisplayAsCloud = gui.addCheckBox(messages.getString("jconfigswitch_msg2"), 
			                               mainPanel, 50, 40, 280, 15, 
			                               onChange);	
	}

	public void updateSwitchDisplay() {
				
		((Switch) getHardware()).setCloud(cbDisplayAsCloud.isSelected());
		modified = true;
		saveChanges();
	}
	
	/** 
     * {@inheritDoc}
     */
	@Override
	public void updateDisplayedValues() {
		
		Switch sw = (Switch) getHardware();
		tfName.setText(sw.getDisplayName());
		cbDisplayAsCloud.setSelected(sw.isCloud());
		modified = false;
	}
	
	/** 
     * {@inheritDoc}
     */
	@Override
	public void saveChanges() {
		
		if (!modified) return;
		
		((Switch) getHardware()).setName(tfName.getText());
		GUIContainer.getInstance().updateViewport();	
		
		ProjectManager.getInstance().setModified();  	
		modified = false;
	}
}
