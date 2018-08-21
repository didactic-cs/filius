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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;

import filius.Main;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.Information;
import filius.software.Anwendung;
import filius.software.system.InternetKnotenBetriebssystem;

public class GUIInstallationsDialog extends JInternalFrame implements I18n {

	private static final long serialVersionUID = 1L;

	private Container c;

	private JList softwareInstalliert;
	private JList softwareVerfuegbar;

	private JButton removeButton, addButton, confirmButton;

	private JLabel titleInstalled, titleAvailable;

	private DefaultListModel lmVerfuegbar;
	private DefaultListModel lmInstalliert;

	private GUIDesktopPanel dp;

	private List<Map<String, String>> programme = null;

	public GUIInstallationsDialog(GUIDesktopPanel dp) {
		super();
		c = this.getContentPane();
		this.dp = dp;

		try {
			programme = Information.getInformation().ladeProgrammListe();
		} catch (IOException e) {
			e.printStackTrace(Main.debug);
		}

		initListen();
		initButtons();

		/* Title above lists */
		titleInstalled = new JLabel(messages.getString("installationsdialog_msg3"));
		titleAvailable = new JLabel(messages.getString("installationsdialog_msg4"));

		/* Komponenten dem Panel hinzufügen */
		Box gesamtBox = Box.createVerticalBox();

		Box wrapperInstBox = Box.createVerticalBox();
		Box wrapperAvailBox = Box.createVerticalBox();

		wrapperInstBox.add(titleInstalled);
		wrapperInstBox.add(Box.createVerticalStrut(10));

		Box listenBox = Box.createHorizontalBox();
		listenBox.add(Box.createHorizontalStrut(10));

		JScrollPane scrollAnwendungInstallieren = new JScrollPane(softwareInstalliert);
		scrollAnwendungInstallieren.setPreferredSize(new Dimension(170, 200));
		wrapperInstBox.add(scrollAnwendungInstallieren);

		listenBox.add(wrapperInstBox);

		listenBox.add(Box.createHorizontalGlue());

		Box topButtonBox = Box.createVerticalBox();
		topButtonBox.add(addButton);
		topButtonBox.add(Box.createVerticalStrut(10));
		topButtonBox.add(removeButton);
		listenBox.add(topButtonBox);

		wrapperAvailBox.add(titleAvailable);
		wrapperAvailBox.add(Box.createVerticalStrut(10));

		JScrollPane scrollAnwendungVerfuegbar = new JScrollPane(softwareVerfuegbar);
		scrollAnwendungVerfuegbar.setPreferredSize(new Dimension(170, 200));
		wrapperAvailBox.add(scrollAnwendungVerfuegbar);
		listenBox.add(wrapperAvailBox);

		listenBox.add(Box.createHorizontalStrut(10));

		gesamtBox.add(Box.createVerticalStrut(10));
		gesamtBox.add(listenBox);
		gesamtBox.add(Box.createVerticalStrut(10));

		Box bottomButtonBox = Box.createVerticalBox();

		bottomButtonBox.add(confirmButton);
		confirmButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);

		gesamtBox.add(bottomButtonBox);
		gesamtBox.add(Box.createVerticalStrut(10));

		c.add(gesamtBox, BorderLayout.CENTER);
		this.setClosable(true);
		this.setMaximizable(true);
		this.setResizable(true);
		this.setBounds(0, 40, 480, 360);
		this.setTitle(messages.getString("installationsdialog_msg1"));
		this.setVisible(true);
		this.setAnwendungsIcon("gfx/desktop/icon_softwareinstallation.png");
	}

	private GUIDesktopPanel getDesktopPanel() {
		return dp;
	}

	private void hinzufuegen() {
		Vector<String> vLoeschen = new Vector<String>();
		int[] selektiertIndices = softwareVerfuegbar.getSelectedIndices();

		for (int i : selektiertIndices) {
			lmInstalliert.addElement(lmVerfuegbar.get(i));
			vLoeschen.add((String)lmVerfuegbar.get(i));
		}

		// umständlich, aber wegen der Möglichkeit von Mehrfachselektion lassen
		// sich nicht einzelne Anwendungen sofort entfernen
		for (Enumeration<String> e = vLoeschen.elements(); e.hasMoreElements();) {
			String oZuLoeschen = e.nextElement();
			lmVerfuegbar.removeElement(oZuLoeschen);
		}
	}

	private void entfernen() {
		int[] selektiertIndices = softwareInstalliert.getSelectedIndices();
		Vector<String> hinzu = new Vector<String>();

		for (int i : selektiertIndices) {
			lmVerfuegbar.addElement(lmInstalliert.getElementAt(i));
			hinzu.add((String)lmInstalliert.getElementAt(i));
		}

		// umständlich, aber wegen der Möglichkeit von Mehrfachselektion lassen
		// sich nicht einzelne Anwendungen sofort entfernen
		for (Enumeration<String> e = hinzu.elements(); e.hasMoreElements();) {
			String hinzuObjekt = e.nextElement();
			lmInstalliert.removeElement(hinzuObjekt);
		}
	}

	private void aenderungenSpeichern() {
		InternetKnotenBetriebssystem bs = getDesktopPanel().getBetriebssystem();
		Anwendung anwendung;

		for (Map<String, String> appInfo : programme) {
			for (int i = 0; i < lmInstalliert.getSize(); i++) {
				if (lmInstalliert.getElementAt(i).equals(appInfo.get("Anwendung"))
				        && bs.holeSoftware(appInfo.get("Klasse").toString()) == null) {
					bs.installiereSoftware(appInfo.get("Klasse").toString());

					anwendung = bs.holeSoftware(appInfo.get("Klasse").toString());
					anwendung.starten();
				}
			}

			for (int i = 0; i < lmVerfuegbar.getSize(); i++) {
				if (lmVerfuegbar.getElementAt(i).equals(appInfo.get("Anwendung"))) {
					anwendung = bs.holeSoftware(appInfo.get("Klasse").toString());
					if (anwendung != null) {
						anwendung.beenden();
						bs.entferneSoftware(appInfo.get("Klasse").toString());
					}
				}
			}
		}

		dp.updateAnwendungen();
	}

	private void initButtons() {
		/* ActionListener */
		ActionListener al = new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				if (arg0.getActionCommand().equals(addButton.getActionCommand())) {
					hinzufuegen();
				} else if (arg0.getActionCommand().equals(removeButton.getActionCommand())) {
					entfernen();
				} else if (arg0.getActionCommand() == confirmButton.getText()) {
					aenderungenSpeichern();
					setVisible(false);
				}

			}
		};

		/* Buttons */
		removeButton = new JButton(new ImageIcon(getClass().getResource("/gfx/allgemein/pfeil_rechts.png")));
		removeButton.setMargin(new Insets(2, 2, 2, 2));
		removeButton.setActionCommand("remove");
		removeButton.addActionListener(al);

		addButton = new JButton(new ImageIcon(getClass().getResource("/gfx/allgemein/pfeil_links.png")));
		addButton.setMargin(new Insets(2, 2, 2, 2));
		addButton.setActionCommand("add");
		addButton.addActionListener(al);

		confirmButton = new JButton(messages.getString("installationsdialog_msg2"));
		confirmButton.addActionListener(al);
	}

	private void initListen() {
		Anwendung[] anwendungen;
		String awKlasse;
		InternetKnotenBetriebssystem bs;

		lmInstalliert = new DefaultListModel();
		lmVerfuegbar = new DefaultListModel();

		bs = dp.getBetriebssystem();

		/* Installierte Anwendung auslesen */
		anwendungen = bs.holeArrayInstallierteSoftware();

		for (int i = 0; i < anwendungen.length; i++) {
			if (anwendungen[i] != null) {
				lmInstalliert.addElement(anwendungen[i].holeAnwendungsName());
			}
		}

		if (programme != null) {
			for (Map<String, String> programmInfo : programme) {
				awKlasse = (String) programmInfo.get("Klasse");

				if (dp.getBetriebssystem().holeSoftware(awKlasse) == null) {
					lmVerfuegbar.addElement(programmInfo.get("Anwendung"));
				}
			}
		}

		/* Listen */
		softwareInstalliert = new JList();
		softwareInstalliert.setModel(lmInstalliert);
		softwareInstalliert.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					GUIInstallationsDialog.this.entfernen();
				}
			}
		});
		softwareVerfuegbar = new JList();
		softwareVerfuegbar.setModel(lmVerfuegbar);
		softwareVerfuegbar.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					GUIInstallationsDialog.this.hinzufuegen();
				}
			}
		});
	}

	public void setAnwendungsIcon(String datei) {
		ImageIcon image = new ImageIcon(getClass().getResource("/" + datei));
		image.setImage(image.getImage().getScaledInstance(16, 16, Image.SCALE_AREA_AVERAGING));
		this.setFrameIcon(image);
	}
}
