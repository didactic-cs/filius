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
package filius.gui.nachrichtensicht;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.util.Hashtable;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import filius.hardware.knoten.Host;
import filius.hardware.knoten.InternetKnoten;
import filius.rahmenprogramm.I18n;
import filius.software.system.SystemSoftware;

public class LayeredExchangeDialog extends JDialog implements ExchangeDialog, I18n {

	private static final long serialVersionUID = 1L;

	private JTabbedPane tabbedPane;

	private static LayeredExchangeDialog lauscherDialog = null;

	private Hashtable<String, JPanel> openedTabs = new Hashtable<String, JPanel>();

	private Hashtable<String, LayeredMessageTable> tabellen = new Hashtable<String, LayeredMessageTable>();

	public static synchronized LayeredExchangeDialog getInstance(Frame owner) {
		if (lauscherDialog == null) {
			lauscherDialog = new LayeredExchangeDialog(owner);
		}

		return lauscherDialog;
	}

	@Override
	public void reset() {
		if (lauscherDialog != null) {
			lauscherDialog.setVisible(false);
		}

		lauscherDialog = null;
	}

	private LayeredExchangeDialog(Frame owner) {
		super(owner);
		((JFrame) owner).getLayeredPane().setLayer(this, JLayeredPane.PALETTE_LAYER);

		Image image;

		setTitle(messages.getString("lauscherdialog_msg1"));
		setBounds(20, (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 350, ((int) Toolkit
		        .getDefaultToolkit().getScreenSize().getWidth()) - 40, 300);
		image = Toolkit.getDefaultToolkit().getImage(
		        getClass().getResource("/gfx/allgemein/nachrichtenfenster_icon.png"));
		setIconImage(image);

		this.setModal(false);

		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		openedTabs.clear();
		tabbedPane = new JTabbedPane();
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		this.setVisible(false);
	}

	/**
	 * Diese Methode fuegt eine Tabelle hinzu
	 */
	@Override
	public void addTable(SystemSoftware system, String identifier) {
		LayeredMessageTable tabelle;
		JPanel panel;
		JScrollPane scrollPane;

		if (openedTabs.get(identifier) == null) {
			tabelle = new LayeredMessageTable(this, identifier);
			tabelle.update();
			panel = new JPanel(new BorderLayout());
			scrollPane = new JScrollPane(tabelle);
			tabelle.setScrollPane(scrollPane);
			panel.add(scrollPane, BorderLayout.CENTER);

			String ipAddress = ((InternetKnoten) system.getKnoten()).getNetzwerkInterfaceByMac(identifier).getIp();
			String tabTitle;
			if (system.getKnoten() instanceof Host && ((Host) system.getKnoten()).isUseIPAsName()) {
				tabTitle = ipAddress;
			} else {
				tabTitle = system.getKnoten().holeAnzeigeName() + " - " + ipAddress;
			}
			tabbedPane.add(tabTitle, panel);

			tabbedPane.setSelectedComponent(panel);

			openedTabs.put(identifier, panel);
			tabellen.put(identifier, tabelle);
		}
		// if there is already a tab opened for this system set it to selected
		else {
			tabbedPane.setSelectedComponent(openedTabs.get(identifier));
			tabellen.get(identifier).update();
		}
	}

	@Override
	public void removeTable(String mac, JPanel panel) {
		if (mac != null) {
			openedTabs.remove(mac);
			tabellen.remove(mac);
			tabbedPane.remove(panel);
		}
	}
}
