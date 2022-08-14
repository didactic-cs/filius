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
package filius.gui.anwendungssicht;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Observable;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import filius.Main;
import filius.gui.CloseableBrowserTabbedPaneUI;
import filius.software.system.HostOS;
import filius.software.system.FiliusFileSystem.FileType;
import filius.software.system.FiliusFile;
import filius.software.system.FiliusFileNode;

/**
 * Applikationsfenster fuer TextEditor
 * 
 * @author Johannes Bade & Thomas Gerding
 * 
 */
@SuppressWarnings("serial")
public class GUIApplicationTextEditorWindow extends GUIApplicationWindow implements PropertyChangeListener {

	private JTextArea editorField;
	private JPanel backPanel;
	private GUIApplicationWindow diesesFenster;
	private FiliusFile currentFile = null;
	private String original = "";
	private FiliusFileNode workingDir;
	private JTabbedPane tpTabs;
	

	public GUIApplicationTextEditorWindow(GUIDesktopPanel desktop, String appName) {
		super(desktop, appName);
		this.diesesFenster = this;

		setTitle(messages.getString("texteditor_msg1"));
		editorField = new JTextArea("");
		editorField.setEditable(true);
		editorField.setFont(new Font("Courier New", Font.PLAIN, 11));

		workingDir = getApplication().getSystemSoftware().getFileSystem().getWorkingDirectory();

		String dateiName = ""; // getParameter()[0];
		if (!dateiName.equals("")) {

			if (this.workingDir == null) {

				this.workingDir = getApplication().getSystemSoftware().getFileSystem().getRoot();

			}
			FiliusFile datei = workingDir.getFiliusFile(dateiName);
			if (datei != null) {
				this.setTitle(dateiName);
				editorField.setText(datei.getContent());
				original = datei.getContent();
				currentFile = datei;
			}
		}

		JScrollPane tpPane = new JScrollPane(editorField);
		tpPane.setBorder(null);

		/* Tabs */
		tpTabs = new JTabbedPane();
		tpTabs.setUI(new CloseableBrowserTabbedPaneUI());
		Box editorBox = Box.createHorizontalBox();

		// editorBox.add(editorField);
		editorBox.add(tpPane);
		editorBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		tabVerhalten();

		backPanel = new JPanel(new BorderLayout());
		backPanel.add(editorBox, BorderLayout.CENTER);

		getContentPane().add(backPanel);

		JMenuBar mb = new JMenuBar();

		JMenu menuDatei = new JMenu(messages.getString("texteditor_msg2"));

		menuDatei.add(new AbstractAction(messages.getString("texteditor_msg3")) {
			private static final long serialVersionUID = 4307765243000198382L;

			public void actionPerformed(ActionEvent arg0) {
				newFile();
			}
		});

		menuDatei.add(new AbstractAction(messages.getString("texteditor_msg4")) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent arg0) {
				open();
			}
		});

		menuDatei.add(new AbstractAction(messages.getString("texteditor_msg5")) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent arg0) {
				save();
			}
		});
		menuDatei.add(new AbstractAction(messages.getString("texteditor_msg6")) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent arg0) {
				saveAs();
			}
		});
		menuDatei.addSeparator();
		menuDatei.add(new AbstractAction(messages.getString("texteditor_msg7")) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent arg0) {
				beenden();
			}
		});

		mb.add(menuDatei);

		this.setJMenuBar(mb);
		pack();
	}
	
	public void open() {
		DMTNFileChooser fc = new DMTNFileChooser((HostOS) getApplication().getSystemSoftware());
		int rueckgabe = fc.openDialog();
		if (rueckgabe == DMTNFileChooser.OK) {
			FiliusFile tmpFile = fc.getAktuellerOrdner().getFiliusFile(fc.getAktuellerDateiname());
			changeCurrentFile(tmpFile);
		} else {
			Main.debug.println("ERROR (" + this.hashCode() + "): Fehler beim oeffnen einer Datei");
		}
	}
	
	public void newFile() {
		editorField.setText("");
		setTitle(messages.getString("texteditor_msg1"));
		changeCurrentFile(null);
	}

	public void save() {
		if (currentFile != null) {
			original = editorField.getText();
			currentFile.setContent(original);
		} else {
			saveAs();
		}
	}

	public void saveAs() {
		DMTNFileChooser fc = new DMTNFileChooser((HostOS) getApplication().getSystemSoftware());
		int rueckgabe = fc.saveDialog();

		if (rueckgabe == DMTNFileChooser.OK) {
			String dateiNameNeu = fc.getAktuellerDateiname();
			FiliusFile tmpFile = new FiliusFile(dateiNameNeu, FileType.TEXT, editorField.getText());
			fc.getAktuellerOrdner().saveFiliusFile(tmpFile);
			changeCurrentFile(tmpFile);
		}
	}

	public void changeCurrentFile(FiliusFile tmpFile) {
		if (currentFile != null) {
			currentFile.addPropertyChangeListener("filecontent", this);
		}
		currentFile = tmpFile;
		updateFromFile();
		if (currentFile != null) {
			currentFile.removePropertyChangeListener("filecontent", this);
		}
	}

	private void updateFromFile() {
		if (currentFile != null) {
			this.setTitle(currentFile.getName());
			original = currentFile.getContent();
			editorField.setText(original);
		} else {
			Main.debug.println("ERROR (" + this.hashCode()
			        + "): Fehler beim oeffnen einer Datei: keine Datei ausgewaehlt");
		}
	}

	public void beenden() {
		if (original != editorField.getText()) {
			if (JOptionPane.showConfirmDialog(this, messages.getString("texteditor_msg9"),
			        messages.getString("texteditor_msg10"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				save();
			}

		}
		diesesFenster.doDefaultCloseAction();
	}

//	public void start(String[] param) {
//		
//		// String filename = getParameter()[0]; // !!!
//		String filename = "";
//		if (param != null && param.length > 0)  filename = param[0]; 
//		
//		if (!filename.equals("")) {
//			this.workingDir = this.getApplication().getSystemSoftware().getFileSystem().getWorkingDirectory();
//			if (this.workingDir == null) {
//				this.workingDir = this.getApplication().getSystemSoftware().getFileSystem().getRoot();
//			}
//			FiliusFile datei = workingDir.getFiliusFile(filename);
//			if (datei != null) {
//				editorField = new JTextArea();
//				editorField.setFont(new Font("Courier New", Font.PLAIN, 11));
//				this.setTitle(filename);
//				editorField.setText(datei.getContent());
//				original = datei.getContent();
//				currentFile = datei;
//
//				JScrollPane tpPane = new JScrollPane(editorField);
//				tpPane.setBorder(null);
//
//				/* Tabs */
//				tpTabs.addTab(datei.getName(), tpPane);
//				tpTabs.setSelectedIndex(tpTabs.getTabCount() - 1);
//			}
//		}
//	}
	
	public void start(FiliusFileNode node, String[] param) {
		
		if (node != null) changeCurrentFile(node.getFiliusFile());		
	}

	public void tabVerhalten() {
		/* Tabs schliessbar machen */
		tpTabs.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				
				if (e.getButton() == 3) {

					JPopupMenu popmen = new JPopupMenu();
					final JMenuItem miTabsSchliessen = new JMenuItem(messages.getString("texteditor_msg11"));
					miTabsSchliessen.setActionCommand("tabsschliessen");
					final JMenuItem miAndereTabsSchliessen = new JMenuItem(messages.getString("texteditor_msg12"));
					miAndereTabsSchliessen.setActionCommand("anderetabsschliessen");

					ActionListener al = new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if (e.getActionCommand().equals(miTabsSchliessen.getActionCommand())) {
								while (tpTabs.getTabCount() > 0) {
									tpTabs.remove(tpTabs.getTabCount() - 1);
								}
							}
							if (e.getActionCommand().equals(miAndereTabsSchliessen.getActionCommand())) {
								Component komponente = tpTabs.getSelectedComponent();
								String tmpTitel = tpTabs.getTitleAt(tpTabs.getSelectedIndex());

								while (tpTabs.getTabCount() > 0) {
									tpTabs.remove(tpTabs.getTabCount() - 1);
								}
								if (komponente != null) {
									tpTabs.addTab(tmpTitel, komponente);
									tpTabs.setSelectedComponent(komponente);
								}

							}
						}

					};

					miTabsSchliessen.addActionListener(al);
					miAndereTabsSchliessen.addActionListener(al);

					popmen.add(miTabsSchliessen);
					popmen.add(miAndereTabsSchliessen);
					popmen.setVisible(true);

					showPopupMenu(popmen, e.getX(), e.getY());

				}
				if (e.getButton() == 1) {
					
					CloseableBrowserTabbedPaneUI tpui = (CloseableBrowserTabbedPaneUI) tpTabs.getUI();
					int index = tpui.getClickedCrossIndex(e.getX(), e.getY());					
					if (index >= 0) {
						if (showConfirmDialog(messages.getString("texteditor_msg13")) == JOptionPane.YES_OPTION) {
							tpui.removeTab(index);
						}
					}
					
//					boolean treffer = false;
//					Rectangle aktuellesRect = null;
//					CloseableBrowserTabbedPaneUI tpui = (CloseableBrowserTabbedPaneUI) tpTabs.getUI();
//
//					ListIterator it = tpui.getButtonPositions().listIterator();
//					while (it.hasNext()) {
//						Rectangle rect = (Rectangle) it.next();
//						if (rect.intersects(new Rectangle(me.getX(), me.getY(), 1, 1))) {
//							treffer = true;
//							aktuellesRect = rect;
//						}
//					}
//
//					if (treffer) {
//						int abfrage = showConfirmDialog(messages.getString("texteditor_msg13"));
//
//						if (abfrage == JOptionPane.YES_OPTION) {
//							tpui.getButtonPositions().remove(aktuellesRect);
//							tpTabs.remove(tpTabs.getSelectedIndex());
//						}
//					}

					/* Neuer Tab bei Doppelklick */
					if (e.getClickCount() == 2) {
						newFile();
					}

				}
			}
		});
	}

	public void updateUnchangedTextFromFile() {
		if (original != null && editorField != null && currentFile != null && original.equals(editorField.getText())) {
			original = currentFile.getContent();
			editorField.setText(original);
		}
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
        String pn = evt.getPropertyName();
		
		if (pn.equals("filecontent")) {
			
			// Update the user interface
			// From: FiliusFile
			updateUnchangedTextFromFile();
		}	
	}
	

	// No longer used (Observer replaced by FiliusFileListener)
	// To be removed when Observer will be removed from ancestor 
	@Override
	public void update(Observable observable, Object arg1) {		
	}	
}
