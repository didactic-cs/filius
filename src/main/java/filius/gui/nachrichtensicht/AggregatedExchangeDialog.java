/*
 ** This file is part of Filius, a network construction and simulation software.
 ** 
 ** Originally created at the University of Siegen, Institute "Didactics of
 ** Informatics and E-Learning" by a students' project group:
 **     members (2006-2007): 
 **         AndrÃ© Asschoff, Johannes Bade, Carsten Dittich, Thomas Gerding,
 **         Nadja HaÃŸler, Ernst Johannes Klebert, Michell Weyer
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
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import filius.rahmenprogramm.I18n;
import filius.software.system.SystemSoftware;

/**
 * This class is used to show exchanged messages between components. Its functionality shall be akin to that of
 * wireshark.
 * 
 * @author stefan
 */
@SuppressWarnings("serial")
public class AggregatedExchangeDialog extends JDialog implements ExchangeComponent, I18n {

    private static AggregatedExchangeDialog instance = null;
    private AggregatedExchangePanel exchangePanel;

    public static AggregatedExchangeDialog getInstance(JFrame owner) {
        if (instance == null) {
            instance = new AggregatedExchangeDialog(owner);
        }
        return instance;
    }

    private AggregatedExchangeDialog(JFrame owner) {
        super(owner);
        ((JFrame) owner).getLayeredPane().setLayer(this, JLayeredPane.PALETTE_LAYER);

        Image image;

        setTitle(messages.getString("lauscherdialog_msg1"));
        int screenHeight = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        int screenWidth = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        setBounds(screenWidth / 2, screenHeight / 10, screenWidth / 2, 4 * screenHeight / 5);
        image = Toolkit.getDefaultToolkit()
                .getImage(getClass().getResource("/gfx/allgemein/nachrichtenfenster_icon.png"));
        setIconImage(image);

        this.setModal(false);

        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        exchangePanel = new AggregatedExchangePanel();
        getContentPane().add(exchangePanel, BorderLayout.CENTER);
        this.setVisible(false);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                AggregatedExchangeDialog.this.exchangePanel.updateTabTitle();
                AggregatedExchangeDialog.this.exchangePanel.clearUnavailableComponents();
            }
        });

    }

    @Override
    public void reset() {
        if (instance != null) {
            instance.setVisible(false);
        }
        instance = null;
    }

    /**
     * Diese Methode fuegt eine Tabelle hinzu
     */
    @Override
    public void addTable(SystemSoftware system, String identifier) {
        exchangePanel.addTable(system, identifier);
    }

    @Override
    public void removeTable(String mac, JPanel panel) {
        exchangePanel.removeTable(mac, panel);
    }
}
