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
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.MouseInputAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import filius.hardware.NetzwerkInterface;
import filius.hardware.knoten.InternetKnoten;
import filius.rahmenprogramm.FiliusClassLoader;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.Information;
import filius.software.system.Betriebssystem;

@SuppressWarnings({ "deprecation", "serial" })
public class GUIDesktopPanel extends JPanel implements I18n, Observer {
    private static Logger LOG = LoggerFactory.getLogger(GUIDesktopPanel.class);

    static final int HEIGHT_TASKBAR = 30;
    static final int HEIGHT_OVERALL = 445;
    static final int WIDTH = 640;
    static final int HEIGHT_CONTENT = HEIGHT_OVERALL - HEIGHT_TASKBAR;
    static final int HEIGHT_APP_TITLEBAR = 30;
    static final int SIZE_TITLEBAR_ICON = 30;

    private static final String SOFTWARE_INSTALLATION = "Software-Installation";
    private static final String DESKTOP_WITH_APPS = "desktop-with-apps";

    private Betriebssystem betriebssystem;
    private HashMap<String, GUIApplicationWindow> laufendeAnwendung = new HashMap<String, GUIApplicationWindow>();

    private CardLayout workingAreaCardLayout = new CardLayout();
    private JBackgroundDesktopPane desktopCard;
    private JPanel workingLayer;
    private JPanel appPanel;
    private JPanel taskbar;
    private JLayeredPane mainPaneWithOverlay;
    private JPanel infoLayer;
    private NetworkInfoPanel networkInfo;
    private JLabel networkIcon;
    private JPanel dialogLayer;

    private String[] parameter = { "", "", "" };

    public GUIDesktopPanel(Betriebssystem betriebssystem) {
        super();
        this.betriebssystem = betriebssystem;
        betriebssystem.addObserver(this);

        initOverallArea();
        initWorkingArea();
        initTaskbar();

        updateAppPane();
    }

    private void initOverallArea() {
        setBounds(0, 0, WIDTH, HEIGHT_OVERALL);
        BorderLayout layout = new BorderLayout();
        layout.setHgap(0);
        layout.setVgap(0);
        setLayout(layout);
    }

    private void initTaskbar() {
        taskbar = new JPanel(new BorderLayout());
        taskbar.setPreferredSize(new Dimension(WIDTH, HEIGHT_TASKBAR));
        taskbar.setBackground(new Color(180, 180, 180));
        add(taskbar, BorderLayout.SOUTH);

        networkIcon = new JLabel(new ImageIcon(getClass().getResource("/gfx/desktop/netzwek_aus.png")));
        networkIcon.setPreferredSize(new Dimension(50, 30));
        networkIcon.setAlignmentX(0.5f);
        networkIcon.addMouseListener(new MouseInputAdapter() {
            public void mousePressed(MouseEvent e) {
                GUIDesktopPanel.this.setInfoVisible(true);
            }
        });
        initNetworkIcon();
        taskbar.add(networkIcon, BorderLayout.EAST);

        JButton homeButton = new JButton(" " + messages.getString("desktoppanel_application") + " ");
        homeButton.setBackground(taskbar.getBackground());
        homeButton.setContentAreaFilled(false);
        homeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                GUIDesktopPanel.this.showApplications();
            }
        });
        taskbar.add(homeButton, BorderLayout.CENTER);

        JLabel leftPlaceholder = new JLabel();
        leftPlaceholder.setPreferredSize(networkIcon.getPreferredSize());
        taskbar.add(leftPlaceholder, BorderLayout.WEST);
    }

    private void setInfoVisible(boolean visible) {
        if (visible) {
            mainPaneWithOverlay.moveToFront(infoLayer);
        } else {
            mainPaneWithOverlay.moveToBack(infoLayer);
        }
        mainPaneWithOverlay.updateUI();
    }

    private void initWorkingArea() {
        mainPaneWithOverlay = new JLayeredPane();
        mainPaneWithOverlay.setPreferredSize(new Dimension(WIDTH, HEIGHT_OVERALL - HEIGHT_TASKBAR));

        add(mainPaneWithOverlay, BorderLayout.CENTER);

        workingLayer = new JPanel(workingAreaCardLayout);
        workingLayer.setSize(new Dimension(WIDTH, HEIGHT_OVERALL - HEIGHT_TASKBAR));
        mainPaneWithOverlay.add(workingLayer, JLayeredPane.DEFAULT_LAYER);

        desktopCard = new JBackgroundDesktopPane();
        desktopCard.setBackgroundImage("gfx/desktop/hintergrundbild.png");

        appPanel = new JPanel();
        appPanel.setBounds(0, 0, WIDTH, HEIGHT_OVERALL);
        appPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 10));
        appPanel.setOpaque(false);
        desktopCard.add(appPanel, BorderLayout.CENTER);
        workingLayer.add(desktopCard, DESKTOP_WITH_APPS);

        infoLayer = new JPanel();
        infoLayer.setLayout(null);
        infoLayer.setOpaque(false);
        infoLayer.setBounds(0, 0, WIDTH, HEIGHT_OVERALL);
        infoLayer.addMouseListener(new MouseInputAdapter() {
            public void mousePressed(MouseEvent e) {
                GUIDesktopPanel.this.setInfoVisible(false);
            }
        });

        networkInfo = new NetworkInfoPanel(this);
        int infoWidth = (int) networkInfo.getPreferredSize().getWidth();
        int infoHeight = (int) networkInfo.getPreferredSize().getHeight();
        networkInfo.setBounds(WIDTH - infoWidth, HEIGHT_OVERALL - HEIGHT_TASKBAR - infoHeight, infoWidth, infoHeight);
        infoLayer.add(networkInfo);
        mainPaneWithOverlay.add(infoLayer, JLayeredPane.DEFAULT_LAYER);

        dialogLayer = new JPanel();
        dialogLayer.setLayout(null);
        dialogLayer.setBounds(0, HEIGHT_APP_TITLEBAR, WIDTH, HEIGHT_CONTENT);
        dialogLayer.addMouseListener(new MouseInputAdapter() {
            public void mousePressed(MouseEvent e) {}
        });
        dialogLayer.setVisible(false);
        mainPaneWithOverlay.add(dialogLayer, JLayeredPane.DRAG_LAYER);
    }

    public void showApplications() {
        workingAreaCardLayout.show(workingLayer, DESKTOP_WITH_APPS);
        dialogLayer.setVisible(false);
    }

    public void showModularWindow(String title, JComponent panel) {
        dialogLayer.removeAll();
        dialogLayer.setVisible(true);

        int windowWidth = (int) panel.getPreferredSize().getWidth();
        int windowHeight = (int) panel.getPreferredSize().getHeight();
        panel.setBounds((WIDTH - windowWidth) / 2, HEIGHT_APP_TITLEBAR, windowWidth, windowHeight);
        panel.setBorder(BorderFactory.createEtchedBorder());
        dialogLayer.add(panel);

        JLabel titlebar = new JLabel(title);
        titlebar.setOpaque(true);
        titlebar.setBackground(new Color(100, 100, 100));
        titlebar.setForeground(new Color(250, 250, 250));
        titlebar.setHorizontalAlignment(SwingConstants.CENTER);
        titlebar.setBounds(0, 0, WIDTH, HEIGHT_APP_TITLEBAR);
        dialogLayer.add(titlebar);
        dialogLayer.updateUI();

        mainPaneWithOverlay.moveToFront(dialogLayer);
        mainPaneWithOverlay.updateUI();
    }

    public void closeModularWindow() {
        dialogLayer.removeAll();
        dialogLayer.setVisible(false);
        mainPaneWithOverlay.moveToBack(dialogLayer);
        mainPaneWithOverlay.updateUI();
    }

    public void closeModularWindow(JPanel progressPanel) {
        for (Component comp : dialogLayer.getComponents()) {
            if (comp.equals(progressPanel)) {
                closeModularWindow();
                break;
            }
        }
    }

    public void updateAppPane() {
        appPanel.removeAll();

        appPanel.add(initInstaller());

        for (Map<String, String> appInfo : Information.getInformation().ladeProgrammListe()) {
            String appClass = appInfo.get("Klasse");
            if ((betriebssystem.holeSoftware(appClass) != null)) {
                appPanel.add(initApp(appInfo));
            }
        }
        appPanel.updateUI();
    }

    private GUIDesktopIcon initInstaller() {
        GUIApplicationWindow tempWindow = new GUIInstallationsDialog(this);
        tempWindow.setBounds(0, 0, WIDTH, HEIGHT_OVERALL);
        addLaufendeAnwendung(SOFTWARE_INSTALLATION, tempWindow);
        return createIcon(messages.getString("desktoppanel_msg1"), SOFTWARE_INSTALLATION,
                "/gfx/desktop/icon_softwareinstallation.png");
    }

    private GUIDesktopIcon initApp(Map<String, String> appInfo) {
        GUIDesktopIcon tmpLabel = null;
        try {
            Class<?> cl = Class.forName(appInfo.get("GUI-Klasse"), true,
                    FiliusClassLoader.getInstance(Thread.currentThread().getContextClassLoader()));
            GUIApplicationWindow tempWindow = (GUIApplicationWindow) cl
                    .getConstructor(GUIDesktopPanel.class, String.class).newInstance(this, appInfo.get("Klasse"));
            tempWindow.setBounds(0, 0, WIDTH, HEIGHT_OVERALL);

            addLaufendeAnwendung(appInfo.get("Klasse"), tempWindow);

            tmpLabel = createIcon((String) appInfo.get("Anwendung"), (String) appInfo.get("Klasse"),
                    "/" + ((String) appInfo.get("gfxFile")));
        } catch (Exception e) {
            LOG.debug("app {} could not be initiated", appInfo, e);
        }
        return tmpLabel;
    }

    private GUIDesktopIcon createIcon(String appName, String invokeName, String imagePath) {
        GUIDesktopIcon tmpLabel = new GUIDesktopIcon(this, new ImageIcon(getClass().getResource(imagePath)));
        tmpLabel.setAnwendungsName(appName);
        tmpLabel.setToolTipText(appName);
        tmpLabel.setText(appName);
        tmpLabel.setInvokeName(invokeName);
        tmpLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
        tmpLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        tmpLabel.setForeground(new Color(255, 255, 255));
        tmpLabel.setPreferredSize(new Dimension(120, 96));
        return tmpLabel;
    }

    private void initNetworkIcon() {
        NetzwerkInterface nic = ((InternetKnoten) betriebssystem.getKnoten()).getNetzwerkInterfaces().get(0);
        if (nic != null && nic.getPort() != null && nic.getPort().getVerbindung() != null) {
            nic.getPort().getVerbindung().addObserver(this);
            networkIcon.setToolTipText(nic.getIp());
        }
    }

    public GUIApplicationWindow starteAnwendung(String softwareKlasse, String[] param) {
        setParameter(param);
        return starteAnwendung(softwareKlasse);
    }

    public GUIApplicationWindow starteAnwendung(String softwareKlasse) {
        GUIApplicationWindow tempWindow = null;
        if (getLaufendeAnwendungByName(softwareKlasse) != null) {
            tempWindow = getLaufendeAnwendungByName(softwareKlasse);
            tempWindow.updateUI();
            tempWindow.starten(parameter);
            workingAreaCardLayout.show(workingLayer, softwareKlasse);
            workingLayer.transferFocus();
        }
        return tempWindow;
    }

    /**
     * Fuegt der Hashmap laufendeAnwendung das Fenster der laufenden Anwendung hinzu, damit Fenster geschlossen und
     * wieder geoeffnet werden koennen, ohne die Anwendung dafuer neu starten zu muessen.
     * 
     * @author Thomas Gerding & Johannes Bade
     * @param fenster
     *            Das GUIApplicationWindow der Anwendung
     * @param appClass
     *            Name der Anwendung
     */
    private void addLaufendeAnwendung(String appClass, GUIApplicationWindow fenster) {
        laufendeAnwendung.put(appClass, fenster);
        workingLayer.add(fenster, appClass);
    }

    /**
     * Gibt das GUIApplicationWindow einer Anwendung aus der HashMap laufendeAnwendung zurueck.
     * 
     * @param anwendungsName
     * @return Das GUIApplicationWindow der angeforderten Anwendung
     */
    private GUIApplicationWindow getLaufendeAnwendungByName(String anwendungsName) {
        GUIApplicationWindow tmpFenster = null;

        tmpFenster = (GUIApplicationWindow) laufendeAnwendung.get(anwendungsName);

        return tmpFenster;
    }

    public filius.software.system.Betriebssystem getBetriebssystem() {
        return betriebssystem;
    }

    public JDesktopPane getDesktopPane() {
        return desktopCard;
    }

    public String[] getParameter() {
        return parameter;
    }

    public void setParameter(String[] parameter) {
        this.parameter = parameter;
    }

    public void update(Observable o, Object arg) {
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass() + " (GUIDesktopPanel), update(" + o + "," + arg
                + ")");
        if (arg == null) {
            updateAppPane();
            initNetworkIcon();
        } else if (arg.equals(Boolean.TRUE)) {
            networkIcon.setIcon(new ImageIcon(getClass().getResource("/gfx/desktop/netzwek_c.png")));
        } else {
            networkIcon.setIcon(new ImageIcon(getClass().getResource("/gfx/desktop/netzwek_aus.png")));
        }
    }
}
