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

import static filius.gui.anwendungssicht.GUIDesktopPanel.HEIGHT_OVERALL;
import static filius.gui.anwendungssicht.GUIDesktopPanel.PANEL_WIDTH;

import java.util.Observable;
import java.util.Observer;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import filius.gui.JMainFrame;
import filius.gui.netzwerksicht.GUIDesignSidebar;
import filius.hardware.Hardware;
import filius.hardware.knoten.Host;
import filius.hardware.knoten.Notebook;
import filius.hardware.knoten.Rechner;
import filius.software.system.Betriebssystem;

@SuppressWarnings({ "serial", "deprecation" })
public class GUIDesktopWindow extends JFrame implements Observer {
    private GUIDesktopPanel desktopPanel;

    public enum Mode {
        ROW(0), COLUMN(1), STACK(2);

        private final int value;

        Mode(int mode) {
            this.value = mode;
        }

        public static Mode getMode(int value) {
            if (value == ROW.value) {
                return ROW;
            } else if (value == COLUMN.value) {
                return COLUMN;
            } else {
                return STACK;
            }
        }
    }

    public GUIDesktopWindow(Betriebssystem bs) {
        bs.addObserver(this);

        Hardware hardware;
        String imageFile = null;

        hardware = bs.getKnoten();
        if (hardware instanceof Rechner) {
            imageFile = GUIDesignSidebar.RECHNER;
        } else if (hardware instanceof Notebook) {
            imageFile = GUIDesignSidebar.NOTEBOOK;
        }

        ImageIcon icon = new ImageIcon(getClass().getResource("/" + imageFile));
        setIconImage(icon.getImage());

        int titleBarHeight = JMainFrame.getJMainFrame().getHeight()
                - JMainFrame.getJMainFrame().getContentPane().getHeight();
        int borderWidth = JMainFrame.getJMainFrame().getWidth()
                - JMainFrame.getJMainFrame().getContentPane().getWidth();
        setSize(PANEL_WIDTH + borderWidth, HEIGHT_OVERALL + titleBarHeight);
        setResizable(false);

        desktopPanel = new GUIDesktopPanel(bs);
        getContentPane().add(desktopPanel);
    }

    public void setVisible(boolean flag) {
        super.setVisible(flag);

        updateTitle();
        desktopPanel.updateInfo();

        if (flag) {
            toFront();
        }
    }

    private void updateTitle() {
        String title;
        Host host = (Host) desktopPanel.getBetriebssystem().getKnoten();
        if (host.isUseIPAsName()) {
            title = desktopPanel.getBetriebssystem().getKnoten().holeAnzeigeName();
        } else {
            title = desktopPanel.getBetriebssystem().getKnoten().holeAnzeigeName() + " - "
                    + desktopPanel.getBetriebssystem().primaryIPAdresse();
        }
        setTitle(title);
    }

    public Betriebssystem getBetriebssystem() {
        return desktopPanel.getBetriebssystem();
    }

    @Override
    public void update(Observable o, Object arg) {
        desktopPanel.updateInfo();
        updateTitle();
    }
}
