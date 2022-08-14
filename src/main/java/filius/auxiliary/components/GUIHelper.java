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
package filius.gui;

import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.regex.Pattern;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import filius.rahmenprogramm.EntryValidator;

public class GUIHelper {
	
	public static final int HBOX_WIDTH = 280;
	public static final int HBOX_HEIGHT = 24;
	public static final int LABEL_WIDTH = 120;
	public static final int TEXTFIELD_WIDTH = 160;
	public static final int TEXTFIELD_HEIGHT = 22;
	public static final int BUTTON_HEIGHT = 22;	
	public static final int SMALL_VERT_GAP = 2;
	public static final int LARGE_VERT_GAP = 10;
	
	
	// Methods to add components
	
	/**
     * <b>addTitledPanel</b> creates a JPanel with a title, and sets its size and location.
     * 
     * @param caption Text of the label.
     * @param contentPane Container to which the label is to be added.
     * @param x Left location of the label.
     * @param y Top location of the label.
     * @param width Width of the label.
     * @param height Height of the label.
     * @return The created JPanel instance
     */
	public JPanel addTitledPanel(String caption, Container contentPane, int x, int y, int width, int height) {
    	
    	JPanel panel = new JPanel();
    	panel.setLayout(null);
        panel.setBorder(BorderFactory.createTitledBorder(" "+caption+" "));
        panel.setBounds(x, y, width, height); 
    	contentPane.add(panel);    	
    	return panel;
    };
	
	/**
     * <b>addLabel</b> creates a JLabel, and sets its size and location.
     * 
     * @param caption Text of the label.
     * @param contentPane Container to which the label is to be added.
     * @param x Left location of the label.
     * @param y Top location of the label.
     * @param width Width of the label.
     * @param height Height of the label.
     * @return The created JLabel instance
     */
    public JLabel addLabel(String caption, Container contentPane, int x, int y, int width, int height) {
    	
    	JLabel label = new JLabel(caption);
    	label.setBounds(x, y, width, height); 
    	contentPane.add(label);    	
    	
    	return label;
    };
    
    /**
     * <b>addTextField</b> creates a JTextField and an optional JLabel in front of it, 
     * sets their size and location, and assigns listeners to textfield.
     * 
     * @param caption Text of the label.
     * @param value Default value of the textfield.
     * @param contentPane Container to which the textfield and label are to be added.
     * @param x Left location of the label.
     * @param y Top location of the label.
     * @param width Total width (label + textfield).
     * @param height Height of the textfield.
     * @param labelWidth Width of the label. The textfield is located immediately to its right. 
     * @param onModification ActionListener fired whenever the text of the textfield is changed.
     * @param onEnter ActionListener fired when the Enter key is pressed.
     * @param onFocusLost ActionListener fired when the textfield loses focus.
     * @param onKeyReleased ActionListener fired whenever a key is released while the textfield is focused.
     * @return The created JTextField instance
     */
    public JTextField addTextField(String caption, String value,     
    		                       Container contentPane, 
    		                       int x, int y, int width, int height, int labelWidth,
    		                       ActionListener onModification,     		                          
    		                       ActionListener onEnter, 
    		                       ActionListener onFocusLost,
    		                       ActionListener onKeyReleased) {
    	
    	if (caption != "") {
			JLabel label = new JLabel();
			label.setText(caption);
			label.setBounds(x, y, labelWidth, height); 
			contentPane.add(label); 
		}
		
    	JTextField textField = new JTextField();
    	if (value != "") textField.setText(value);
    	
    	labelWidth += 4;
    	textField.setBounds(x + labelWidth, y, width - labelWidth, height); 
		contentPane.add(textField); 
		
		// Listeners    	
    	
    	if (onModification != null) {
    		
    		DocumentListener docListener = new DocumentListener() {			
       			public void changedUpdate(DocumentEvent e) { onModification.actionPerformed(null); }
    			public void insertUpdate(DocumentEvent e)  { onModification.actionPerformed(null); }
    			public void removeUpdate(DocumentEvent e)  { onModification.actionPerformed(null); }
    		};
    		textField.getDocument().addDocumentListener(docListener);
    	}   
    	
    	if (onEnter != null) {    		

    		textField.addActionListener(onEnter);
    	}
    	
        if (onFocusLost != null) {
    		
    		FocusListener focusLostListener =new FocusListener() {
    			public void focusLost(FocusEvent e)   { onFocusLost.actionPerformed(null); }	
    			public void focusGained(FocusEvent e) {}
    		};
    		textField.addFocusListener(focusLostListener);
    	}
    	
    	if (onKeyReleased != null) {
    		
    		KeyListener keyReleasedListener = new KeyListener() {	
    			public void keyReleased(KeyEvent e) { 
    				ActionEvent ev = new ActionEvent(e.getComponent(), ActionEvent.ACTION_PERFORMED, "keyReleased");   
    				onKeyReleased.actionPerformed(ev); }
				public void keyTyped(KeyEvent e) {}
				public void keyPressed(KeyEvent e) {}
    		};
    		textField.addKeyListener(keyReleasedListener);
    	}    
			
        
        return textField;
	}
    
    public JComboBox<String> addComboBox(String caption, String value,     
    		                     Container contentPane, 
    		                     int x, int y, int width, int height, int labelWidth,
    		                     ActionListener onModification,     		                          
    		                     ActionListener onEnter, 
    		                     ActionListener onFocusLost,
    		                     ActionListener onKeyReleased) {
    	
    	if (caption != "") {
			JLabel label = new JLabel();
			label.setText(caption);
			label.setBounds(x, y, labelWidth, height); 
			contentPane.add(label); 
		}
    	
    	JComboBox<String> comboBox = new JComboBox<String>();   
    	Font font = comboBox.getFont();
    	int style = font.getStyle();
        style = style & ~Font.BOLD;
        comboBox.setFont(font.deriveFont(style));	
    	
    	labelWidth += 4;
    	comboBox.setBounds(x + labelWidth, y, width - labelWidth, height); 
		contentPane.add(comboBox); 		
    	
    	return comboBox;
    }
    
    /**
     * <b>addButton</b> creates a JButton and an optional JLabel after it, 
     * sets their size and location, and assigns a listener to checkbox.
     * 
     * @param caption Text of the button.
     * @param contentPane Container to which the button is to be added.
     * @param x Left location of the button.
     * @param y Top location of the button.
     * @param width Width of the button.
     * @param height Height of the button.
     * @param onClick ActionListener fired when the button is clicked on.
     * @return The created JButton instance
     */
    public JButton addButton(String caption, Container contentPane, int x, int y, int width, int height, ActionListener onClick) {
    	
    	JButton button = new JButton(caption);    	
    	button.setBounds(x, y, width, height); 
    	contentPane.add(button);    	
    	
    	if (onClick != null) button.addActionListener(onClick);
    	
    	return button;
    };    
    
    /**
     * <b>addCheckBox</b> creates a JCheckBox and an optional JLabel after it, 
     * sets their size and location, and assigns a listener to checkbox.
     * 
     * @param caption Text of the label.
     * @param contentPane Container to which the checkbox and label are to be added.
     * @param x Left location of the checkbox.
     * @param y Top location of the checkbox.
     * @param width Total width (checkbox + label).
     * @param height Height of the label.
     * @param onChange ActionListener fired when the status of the checkbox changes.
     * @return The created JCheckBox instance
     */
    public JCheckBox addCheckBox(String caption, Container contentPane, int x, int y, int width, int height, ActionListener onChange){
		
    	JCheckBox checkbox = new JCheckBox();		
    	checkbox.setBounds(x, y, width, height);
    	checkbox.setOpaque(false);
    	checkbox.setFocusPainted(false);
		contentPane.add(checkbox); 
		checkbox.setText(caption);		
		//checkbox.setVerticalTextPosition(JCheckBox.TOP);
		
		if (onChange != null) checkbox.addActionListener(onChange);
        
        return checkbox;
	}
    
    public void setBoldFont(JComponent comp, boolean bold) {
    	
    	Font font = comp.getFont();
    	int style = font.getStyle();
    	if (bold) style = style | Font.BOLD;
    	else      style = style & ~Font.BOLD;
    	comp.setFont(font.deriveFont(style));
    }   
    
    /**
     * <b>checkAndHighlight</b> checks on the fly that the input value entered by the user is correct.
     * In case it is not, the input is displayed in red to warn the user.
     * 
     * @author Johannes Bade & Thomas Gerding
     * @param checkPattern
     * @param field
     */
    public boolean checkAndHighlight(Pattern checkPattern, JTextField field) {
    	
        if (EntryValidator.isValid(field.getText(), checkPattern)) {
            field.setForeground(EntryValidator.rightTextColor);
            field.setBackground(EntryValidator.rightBGColor);
            return true;
        } else {
            field.setForeground(EntryValidator.wrongTextColor);
            field.setBackground(EntryValidator.wrongBGColor);
            return false;
        }
    }
    
    public void mapKeyToAction(JComponent comp, String key, Action action) {
    	
    	comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), key+"_Action");
    	comp.getActionMap().put(key+"_Action", action);
    };  

}
