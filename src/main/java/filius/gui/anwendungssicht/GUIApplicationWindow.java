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

import static filius.gui.anwendungssicht.GUIDesktopPanel.HEIGHT_APP_TITLEBAR;
import static filius.gui.anwendungssicht.GUIDesktopPanel.HEIGHT_CONTENT;
import static filius.gui.anwendungssicht.GUIDesktopPanel.SIZE_TITLEBAR_ICON;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.util.Map;
import java.util.Observer;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;

import org.apache.commons.lang3.StringUtils;

import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.Information;
import filius.software.Anwendung;

/**
 * Diese Klasse dient als Oberklasse für alle Anwendungsfenster
 * 
 */
@SuppressWarnings({ "serial", "deprecation" })
public abstract class GUIApplicationWindow extends JPanel implements I18n, Observer {
    protected GUIDesktopPanel desktop;
    protected Anwendung anwendung;
    protected JLabel titleLabel;
    protected JLabel icon;

    public GUIApplicationWindow(GUIDesktopPanel desktop) {
        super(new BorderLayout());

        this.desktop = desktop;
        initComponents();
    }

    public GUIApplicationWindow(GUIDesktopPanel desktop, String appKlasse) {
        super(new BorderLayout());

        this.desktop = desktop;
        initComponents();

        this.anwendung = desktop.getBetriebssystem().holeSoftware(appKlasse);
        this.anwendung.hinzuBeobachter(this);

        setIcon(anwendung);
        setTitle(anwendung);
    }

    private void initComponents() {
        setSize(WIDTH, HEIGHT_CONTENT);
        setPreferredSize(new Dimension(WIDTH, HEIGHT_CONTENT));
        BorderLayout appWindowLayout = (BorderLayout) getLayout();
        appWindowLayout.setHgap(0);
        appWindowLayout.setVgap(0);
        setLayout(appWindowLayout);

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setPreferredSize(new Dimension(WIDTH, HEIGHT_APP_TITLEBAR));
        titleBar.setBackground(Color.DARK_GRAY);
        add(titleBar, BorderLayout.NORTH);

        icon = new JLabel();
        icon.setPreferredSize(new Dimension(SIZE_TITLEBAR_ICON, SIZE_TITLEBAR_ICON));
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        titleBar.add(icon, BorderLayout.WEST);

        titleLabel = new JLabel();
        titleLabel.setForeground(new Color(225, 225, 225));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleBar.add(titleLabel, BorderLayout.CENTER);
    }

    public void close() {
        desktop.showApplications();
    }

    private void setIcon(Anwendung app) {
        for (Map<String, String> tmpMap : Information.getInformation().ladeProgrammListe()) {
            if (app.holeAnwendungsName().equals(tmpMap.get("Anwendung"))) {
                String path = "/" + tmpMap.get("gfxFile");
                setIcon(path);
                break;
            }
        }
    }

    protected void setIcon(String path) {
        ImageIcon image = new ImageIcon(getClass().getResource(path));
        image.setImage(image.getImage().getScaledInstance(16, 16, Image.SCALE_AREA_AVERAGING));
        icon.setIcon(image);
    }

    protected void setTitle(Anwendung app) {
        setTitle(app, StringUtils.EMPTY);
    }

    protected void setTitle(Anwendung app, String titleExtension) {
        String title;
        if (StringUtils.isNoneBlank(titleExtension) && null != app) {
            title = app.holeAnwendungsName() + " - " + titleExtension;
        } else if (null != app) {
            title = app.holeAnwendungsName();
        } else {
            title = titleExtension;
        }
        setTitle(title);
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public Anwendung holeAnwendung() {
        return anwendung;
    }

    public void showMessageDialog(String msg) {
        JOptionPane.showMessageDialog(desktop, msg);
    }

    public int showOptionDialog(Object message, String title, int optionType, int messageType, Icon icon,
            Object[] options, Object initialValue) {
        return JOptionPane.showOptionDialog(desktop, message, title, optionType, messageType, icon, options,
                initialValue);
    }

    public int showConfirmDialog(String msg) {
        return JOptionPane.showConfirmDialog(desktop, msg);
    }

    public void addFrame(JInternalFrame frame) {
        desktop.getDesktopPane().add(frame);
    }

    public void starteExterneAnwendung(String softwareName) {
        desktop.starteAnwendung(softwareName);
    }

    public void starteExterneAnwendung(String softwareName, String[] param) {
        desktop.starteAnwendung(softwareName, param);
    }

    public String[] holeParameter() {
        return desktop.getParameter();
    }

    public void zeigePopupMenu(JPopupMenu menu, int x, int y) {
        menu.show(desktop, x, y);
    }

    public void starten(String[] param) {}
}
