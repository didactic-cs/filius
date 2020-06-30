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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.tools.ToolProvider;

import filius.Main;
import filius.gui.netzwerksicht.GUICableItem;
import filius.gui.netzwerksicht.GUINodeItem;
import filius.gui.netzwerksicht.JVermittlungsrechnerKonfiguration;
import filius.gui.quelltextsicht.FrameSoftwareWizard;
import filius.hardware.Cable;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.Information;
import filius.rahmenprogramm.SzenarioVerwaltung;
import filius.software.system.SystemSoftware;

public class GUIMainMenu implements Serializable, I18n {

    private static final long serialVersionUID = 1L;

    public static final int MODUS_ENTWURF = 1;
    public static final int MODUS_AKTION = 2;
    public static final int MODUS_DOKUMENTATION = 3;

    private JBackgroundPanel menupanel;

    private JSlider verzoegerung;

    private FileFilter filiusFileFilter;

    private JLabel geschwindigkeit;

    private int aktuellerModus = MODUS_ENTWURF;

    private JButton btAktionsmodus, btEntwurfsmodus, btDokumodus, btOeffnen, btSpeichern, btNeu, btWizard, btHilfe, btInfo;
    
    private GUIContainer container;    

    public GUIMainMenu() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (GUIMainMenu), constr: GUIMainMenu()");
        container = GUIContainer.getInstance();
        
        Container c = JMainFrame.getJMainFrame().getContentPane();

        menupanel = new JBackgroundPanel();
        menupanel.setPreferredSize(new Dimension(100, 63));
        menupanel.setBounds(0, 0, c.getWidth(), 65);
        menupanel.setEnabled(false);
        menupanel.setBackgroundImage("gfx/allgemein/menue_hg.png");

        btOeffnen = new JButton();
        btOeffnen.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/oeffnen.png")));
        btOeffnen.setBounds(80, 5, btOeffnen.getIcon().getIconWidth(), btOeffnen.getIcon().getIconHeight());
        btOeffnen.setActionCommand("oeffnen");
        btOeffnen.setToolTipText(messages.getString("guimainmemu_msg1"));

        btSpeichern = new JButton();
        btSpeichern.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/speichern.png")));
        btSpeichern.setBounds(150, 5, btSpeichern.getIcon().getIconWidth(), btSpeichern.getIcon().getIconHeight());
        btSpeichern.setActionCommand("speichern");
        btSpeichern.setToolTipText(messages.getString("guimainmemu_msg2"));

        btEntwurfsmodus = new JButton();
        btEntwurfsmodus.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/entwurfsmodus_aktiv.png")));
        btEntwurfsmodus.setBounds(350, 5, btEntwurfsmodus.getIcon().getIconWidth(),
                btEntwurfsmodus.getIcon().getIconHeight());
        btEntwurfsmodus.setActionCommand("entwurfsmodus");
        btEntwurfsmodus.setToolTipText(messages.getString("guimainmemu_msg3"));

        btAktionsmodus = new JButton();
        btAktionsmodus.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/aktionsmodus.png")));
        btAktionsmodus.setBounds(420, 5, btAktionsmodus.getIcon().getIconWidth(),
                btAktionsmodus.getIcon().getIconHeight());
        btAktionsmodus.setActionCommand("aktionsmodus");
        btAktionsmodus.setToolTipText(messages.getString("guimainmemu_msg4"));

        btNeu = new JButton();
        btNeu.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/neu.png")));
        btNeu.setBounds(10, 5, btNeu.getIcon().getIconWidth(), btNeu.getIcon().getIconHeight());
        btNeu.setActionCommand("neu");
        btNeu.setToolTipText(messages.getString("guimainmemu_msg5"));

        btDokumodus = new JButton();
        btDokumodus.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/dokumodus.png")));
        btDokumodus.setBounds(250, 5, btDokumodus.getIcon().getIconWidth(), btDokumodus.getIcon().getIconHeight());
        btDokumodus.setActionCommand("dokumodus");
        btDokumodus.setToolTipText(messages.getString("guimainmemu_msg14"));

        if (isSoftwareWizardEnabled()) {
            btWizard = new JButton();
            btWizard.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/button_wizard.png")));
            btWizard.setBounds(720, 5, btWizard.getIcon().getIconWidth(), btWizard.getIcon().getIconHeight());
            btWizard.setActionCommand("wizard");
            btWizard.setToolTipText(messages.getString("guimainmemu_msg6"));
        }

        btHilfe = new JButton();
        btHilfe.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/hilfe.png")));
        btHilfe.setBounds(840, 5, btHilfe.getIcon().getIconWidth(), btHilfe.getIcon().getIconHeight());
        btHilfe.setActionCommand("hilfe");
        btHilfe.setToolTipText(messages.getString("guimainmemu_msg7"));

        btInfo = new JButton();
        btInfo.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/info.png")));
        btInfo.setBounds(910, 5, btInfo.getIcon().getIconWidth(), btInfo.getIcon().getIconHeight());
        btInfo.setActionCommand("info");
        btInfo.setToolTipText(messages.getString("guimainmemu_msg8"));

        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                boolean erfolg;

                if (isSoftwareWizardEnabled() && e.getActionCommand().equals(btWizard.getActionCommand())) {
                    FrameSoftwareWizard gsw = new FrameSoftwareWizard();
                    gsw.setVisible(true);
                }

                if (e.getActionCommand().equals(btHilfe.getActionCommand())) {
                    GUIHilfe.getGUIHilfe().anzeigen();
                }

                if (e.getActionCommand().equals(btNeu.getActionCommand())) {
                    int entscheidung = JOptionPane.YES_OPTION;
                    try {
                        if (SzenarioVerwaltung.getInstance().istGeaendert()) {
                            entscheidung = JOptionPane.showConfirmDialog(JMainFrame.getJMainFrame(),
                                    messages.getString("guimainmemu_msg9"), messages.getString("guimainmemu_msg10"),
                                    JOptionPane.YES_NO_OPTION);
                        } else {
                            entscheidung = JOptionPane.YES_OPTION;
                        }
                    } catch (Exception exc) {
                        exc.printStackTrace(Main.debug);
                    }
                    if (entscheidung == JOptionPane.YES_OPTION) {
                    	container.clearAllItems();
                    	container.setProperty(null);
                        Information.getInstance().reset();
                        SzenarioVerwaltung.getInstance().reset();
                    }
                }

                if (e.getActionCommand().equals(btSpeichern.getActionCommand())) {
                    if (container.getActiveSite() != MODUS_AKTION) {
                        JFileChooser fcSpeichern = new JFileChooser();

                        fcSpeichern.setFileFilter(filiusFileFilter);
                        initCurrentFileOrDirSelection(fcSpeichern);

                        if (fcSpeichern.showSaveDialog(JMainFrame.getJMainFrame()) == JFileChooser.APPROVE_OPTION) {
                            if (fcSpeichern.getSelectedFile() != null) {
                                Information.getInstance()
                                        .setLastOpenedDirectory(fcSpeichern.getSelectedFile().getParent());
                                String targetFilePath;
                                if (fcSpeichern.getSelectedFile().getName().endsWith(".fls")) {
                                    targetFilePath = fcSpeichern.getSelectedFile().getPath();
                                } else {
                                    targetFilePath = fcSpeichern.getSelectedFile().getPath() + ".fls";
                                }

                                int entscheidung = JOptionPane.YES_OPTION;
                                if (SzenarioVerwaltung.getInstance().holePfad() != null && targetFilePath != null
                                        && new File(targetFilePath).exists()
                                        && !new File(SzenarioVerwaltung.getInstance().holePfad())
                                                .equals(new File(targetFilePath))) {
                                    entscheidung = JOptionPane.showConfirmDialog(JMainFrame.getJMainFrame(),
                                            messages.getString("guimainmemu_msg17"),
                                            messages.getString("guimainmemu_msg10"), JOptionPane.YES_NO_OPTION);
                                }

                                if (entscheidung == JOptionPane.YES_OPTION) {                                	
                                    erfolg = SzenarioVerwaltung.getInstance().speichern(targetFilePath,
                                    		container.getKnotenItems(),
                                    		container.getCableItems(),
                                    		container.getDocuItems());                                    
                                    if (!erfolg) {
                                        JOptionPane.showMessageDialog(JMainFrame.getJMainFrame(),
                                                messages.getString("guimainmemu_msg11"));
                                    }
                                }
                            }
                        }
                    }
                }

                if (e.getActionCommand().equals(btOeffnen.getActionCommand())) {
                    int entscheidung = JOptionPane.YES_OPTION;
                    try {
                        if (SzenarioVerwaltung.getInstance().istGeaendert()) {
                            entscheidung = JOptionPane.showConfirmDialog(JMainFrame.getJMainFrame(),
                                    messages.getString("guimainmemu_msg9"), messages.getString("guimainmemu_msg10"),
                                    JOptionPane.YES_NO_OPTION);
                        } else {
                            entscheidung = JOptionPane.YES_OPTION;
                        }
                    } catch (Exception exc) {
                        exc.printStackTrace(Main.debug);
                    }
                    if (entscheidung == JOptionPane.YES_OPTION && container.getActiveSite() != MODUS_AKTION) {
                        JFileChooser fcLaden = new JFileChooser();
                        fcLaden.setFileFilter(filiusFileFilter);
                        initCurrentFileOrDirSelection(fcLaden);

                        if (fcLaden.showOpenDialog(JMainFrame.getJMainFrame()) == JFileChooser.APPROVE_OPTION) {
                            if (fcLaden.getSelectedFile() != null) {
                                Information.getInstance()
                                        .setLastOpenedDirectory(fcLaden.getSelectedFile().getParent());
                                try {
                                    Information.getInstance().reset();
                                    SzenarioVerwaltung.getInstance().laden(fcLaden.getSelectedFile().getPath(),
                                    		container.getKnotenItems(),
                                    		container.getCableItems(),
                                    		container.getDocuItems());
                                    container.setProperty(null);
                                    container.updateViewport();
                                    Thread.sleep(10);
                                    container.updateCables();
                                } catch (FileNotFoundException e1) {
                                    e1.printStackTrace(Main.debug);
                                } catch (Exception e2) {
                                    e2.printStackTrace(Main.debug);
                                }
                            }
                        }
                    }
                }

                if (e.getActionCommand().equals(btEntwurfsmodus.getActionCommand())) {
                    selectMode(MODUS_ENTWURF);
                } else if (e.getActionCommand().equals(btAktionsmodus.getActionCommand())) {
                    selectMode(MODUS_AKTION);
                } else if (e.getActionCommand().equals(btDokumodus.getActionCommand())) {
                    selectMode(MODUS_DOKUMENTATION);
                } else if (e.getActionCommand().equals(btInfo.getActionCommand())) {
                    (new InfoDialog(JMainFrame.getJMainFrame())).setVisible(true);
                }
            }
        };

        btNeu.addActionListener(al);
        btOeffnen.addActionListener(al);
        btSpeichern.addActionListener(al);
        btEntwurfsmodus.addActionListener(al);
        btAktionsmodus.addActionListener(al);
        btDokumodus.addActionListener(al);
        if (isSoftwareWizardEnabled()) {
            btWizard.addActionListener(al);
        }
        btInfo.addActionListener(al);
        btHilfe.addActionListener(al);

        geschwindigkeit = new JLabel("100%");
        geschwindigkeit.setVisible(true);
        geschwindigkeit.setToolTipText(messages.getString("guimainmemu_msg15"));
        geschwindigkeit.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
        geschwindigkeit.setBounds(612, 10, 120, 44);

        verzoegerung = new JSlider(0, 100);
        verzoegerung.setToolTipText(messages.getString("guimainmemu_msg16"));
        verzoegerung.setMaximum(10);
        verzoegerung.setMinimum(1);
        verzoegerung.setValue(verzoegerung.getMaximum());
        Cable.setDelayFactor(verzoegerung.getMaximum() - verzoegerung.getValue() + 1);
        verzoegerung.setBounds(510, 10, 100, 44);
        verzoegerung.setOpaque(false);
        verzoegerung.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent arg0) {
                Cable.setDelayFactor(verzoegerung.getMaximum() - verzoegerung.getValue() + 1);
                geschwindigkeit.setText("" + verzoegerung.getValue() * 10 + "%");
            }

        });

        menupanel.setLayout(null);

        menupanel.add(btEntwurfsmodus);
        menupanel.add(btAktionsmodus);
        menupanel.add(btDokumodus);
        menupanel.add(btNeu);
        menupanel.add(btOeffnen);
        menupanel.add(btSpeichern);
        menupanel.add(verzoegerung);
        menupanel.add(geschwindigkeit);
        if (isSoftwareWizardEnabled()) {
            menupanel.add(btWizard);
        }
        menupanel.add(btHilfe);
        menupanel.add(btInfo);

        filiusFileFilter = new FileFilter() {
            public boolean accept(File pathname) {
                if (pathname.isDirectory())
                    return true;
                return pathname.getName().toLowerCase().endsWith(".fls");
            }

            public String getDescription() {
                return messages.getString("guimainmemu_msg13");
            }
        };
    }

    private void initCurrentFileOrDirSelection(JFileChooser fcLaden) {
        String scenarioPath = SzenarioVerwaltung.getInstance().holePfad();
        String lastOpenedDir = Information.getInstance().getLastOpenedDirectory();
        File file = null;
        if (scenarioPath != null) {
            file = new File(scenarioPath);
        }
        if (null != file && file.exists()) {
            fcLaden.setSelectedFile(file);
        } else if (null != lastOpenedDir) {
            file = new File(lastOpenedDir);
            if (file.exists()) {
                fcLaden.setCurrentDirectory(file);
            }
        }
    }

    private boolean isSoftwareWizardEnabled() {
        return (null != ToolProvider.getSystemJavaCompiler()
                && Information.getInstance().getSoftwareWizardMode() != Information.FeatureMode.FORCE_DISABLE)
                || Information.getInstance().getSoftwareWizardMode() == Information.FeatureMode.FORCE_ENABLE;
    }

    public void changeSlider(int diff) {
        if (diff < 0 && verzoegerung.getValue() + diff < 1) {
            verzoegerung.setValue(1);
        } else if (diff > 0 && verzoegerung.getValue() + diff > 10) {
            verzoegerung.setValue(10);
        } else
            verzoegerung.setValue(verzoegerung.getValue() + diff);
    }

    public boolean doClick(String button) { // manually perform click event on a
                                            // registered button
        if (button.equals("btAktionsmodus"))
            btAktionsmodus.doClick();
        else if (button.equals("btEntwurfsmodus"))
            btEntwurfsmodus.doClick();
        else if (button.equals("btDokumodus"))
            btDokumodus.doClick();
        else if (button.equals("btOeffnen"))
            btOeffnen.doClick();
        else if (button.equals("btSpeichern"))
            btSpeichern.doClick();
        else if (button.equals("btNeu"))
            btNeu.doClick();
        else if (button.equals("btWizard"))
            btWizard.doClick();
        else if (button.equals("btHilfe"))
            btHilfe.doClick();
        else if (button.equals("btInfo"))
            btInfo.doClick();
        else
            return false;
        return true;
    }

    // set/reset cable highlight, i.e., make all cables normal coloured for
    // simulation
    // and possibly highlight in development view
    private void resetCableHighlighting(int mode) {
        Main.debug.println(
                "INVOKED (" + this.hashCode() + ") " + getClass() + " (GUIMainMenu), resetCableHL(" + mode + ")");
        if (mode == MODUS_AKTION) { // change to simulation view: de-highlight
                                    // all cables
            for (GUICableItem cableItem : container.getCableItems()) {
                if (cableItem.getCable() != null) cableItem.getCable().setActive(false);
            }
            GUIEvents.getGUIEvents().unselectActiveCable();
        } else { // change to development view: possibly highlight a cable (only
                 // for 'Vermittlungsrechner' configuration
            if (container.getProperty() instanceof JVermittlungsrechnerKonfiguration) {
                ((JVermittlungsrechnerKonfiguration) container.getProperty()).highlightConnCable();
            }
            if (mode == MODUS_DOKUMENTATION) GUIEvents.getGUIEvents().unselectActiveCable();
        }
    }

    public synchronized void selectMode(int mode) {
        Main.debug.println(
                "INVOKED (" + this.hashCode() + ") " + getClass() + " (GUIMainMenu), selectMode(" + mode + ")");

        if (mode == MODUS_ENTWURF) {
            resetCableHighlighting(mode); // de-highlight cables   

            btEntwurfsmodus.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/entwurfsmodus_aktiv.png")));
            btAktionsmodus.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/aktionsmodus.png")));
            btDokumodus.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/dokumodus.png")));
            container.setActiveSite(MODUS_ENTWURF);

            GUIHilfe.getGUIHilfe().laden("entwurfsmodus");

            stopSimulation();

            btOeffnen.setEnabled(true);
            btNeu.setEnabled(true);
            btSpeichern.setEnabled(true);
            if (isSoftwareWizardEnabled()) {
                btWizard.setEnabled(true);
            }
            
            GUIEvents.getGUIEvents().unFreezeActiveElements();   
            
            ToolTipManager.sharedInstance().setDismissDelay(3000);
            
        } else if (mode == MODUS_DOKUMENTATION) {
        	GUIEvents.getGUIEvents().freezeActiveElements(); 
            resetCableHighlighting(mode); // de-highlight cables

            btEntwurfsmodus.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/entwurfsmodus.png")));
            btAktionsmodus.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/aktionsmodus.png")));
            btDokumodus.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/dokumodus_aktiv.png")));
            container.setActiveSite(MODUS_DOKUMENTATION);

            GUIHilfe.getGUIHilfe().laden("dokumodus");

            stopSimulation();

            btOeffnen.setEnabled(true);
            btNeu.setEnabled(true);
            btSpeichern.setEnabled(true);
            if (isSoftwareWizardEnabled()) {
                btWizard.setEnabled(false);
            }
            
            ToolTipManager.sharedInstance().setDismissDelay(3000);
            
        } else if (mode == MODUS_AKTION && aktuellerModus != MODUS_AKTION) {
            // Main.debug.println("\tMode: MODUS_AKTION");
        	GUIEvents.getGUIEvents().freezeActiveElements(); 
            resetCableHighlighting(mode); // de-highlight cables

            btEntwurfsmodus.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/entwurfsmodus.png")));
            btAktionsmodus.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/aktionsmodus_aktiv.png")));
            btDokumodus.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/dokumodus.png")));
            container.setActiveSite(MODUS_AKTION);
            GUIHilfe.getGUIHilfe().laden("simulationsmodus");

            for (GUINodeItem knotenItem : container.getKnotenItems()) {
                SystemSoftware system;
                system = knotenItem.getNode().getSystemSoftware();
                system.starten();
            }

            btOeffnen.setEnabled(false);
            btNeu.setEnabled(false);
            btSpeichern.setEnabled(false);
            if (isSoftwareWizardEnabled()) {
                btWizard.setEnabled(false);
            }

            geschwindigkeit.setEnabled(true);
            verzoegerung.setEnabled(true);
            
            // Tooltips remain visible longer
            ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
        }
        aktuellerModus = mode;
    }

    private void stopSimulation() {
        for (GUINodeItem knotenItem : container.getKnotenItems()) {
            SystemSoftware system;
            system = knotenItem.getNode().getSystemSoftware();
            try {
                system.beenden();
            } catch (Exception e) {}
        }
        ((JFrame) container.getExchangeDialog()).setVisible(false);
    }

    public JBackgroundPanel getMenupanel() {
        return menupanel;
    }

    public void setMenupanel(JBackgroundPanel menupanel) {
        this.menupanel = menupanel;
    }
}
