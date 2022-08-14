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
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.tools.ToolProvider;

import filius.Main;
import filius.gui.netzwerksicht.GUICableItem;
import filius.gui.netzwerksicht.GUINodeItem;
import filius.gui.netzwerksicht.config.JConfigRouter;
import filius.gui.quelltextsicht.FrameSoftwareWizard;
import filius.hardware.Cable;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.Information;
import filius.rahmenprogramm.ProjectExport;
import filius.rahmenprogramm.ProjectManager;
import filius.rahmenprogramm.ProjectReport;
import filius.software.netzzugangsschicht.SwitchSpanningTree;
import filius.software.system.SystemSoftware;

@SuppressWarnings("serial")
public class GUIMainMenu implements Serializable, I18n {

    public static final int DESIGN_MODE = 1;
    public static final int ACTION_MODE = 2;
    public static final int DOC_MODE = 3;

    private JBackgroundPanel menuPanel;    
    private FileFilter filiusFileFilter;
    private JLabel lbSpeed;
    private JSlider slSpeed;
    private int currentMode = DESIGN_MODE;
    private JButton btNew, btOpen, btDocMode, btActionMode, btDesignMode, btWizard, btInfo, btHelp;   
    private JExtendedButton btSave;
    private GUIContainer container;    
    private SwitchSpanningTree switchSpanningTree = new SwitchSpanningTree();

    public GUIMainMenu() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (GUIMainMenu), constr: GUIMainMenu()");
        container = GUIContainer.getInstance();
        
        Container c = JMainFrame.getInstance().getContentPane();

        menuPanel = new JBackgroundPanel();
        menuPanel.setPreferredSize(new Dimension(100, 63));
        menuPanel.setBounds(0, 0, c.getWidth(), 65);
        menuPanel.setEnabled(false);
        menuPanel.setBackgroundImage("gfx/allgemein/menue_hg.png");
        
        btNew = new JButton();
        btNew.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/neu.png")));
        btNew.setBounds(10, 5, btNew.getIcon().getIconWidth(), btNew.getIcon().getIconHeight());
        btNew.setActionCommand("neu");
        btNew.setToolTipText(messages.getString("guimainmemu_msg5"));

        btOpen = new JButton();
        btOpen.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/oeffnen.png")));
        btOpen.setBounds(70, 5, btOpen.getIcon().getIconWidth(), btOpen.getIcon().getIconHeight());
        btOpen.setActionCommand("oeffnen");
        btOpen.setToolTipText(messages.getString("guimainmemu_msg1"));
        
        JPopupMenu popupMenu = new JPopupMenu();
        
        JMenuItem miSaveAs = new JMenuItem(messages.getString("guimainmemu_msg18"));
        miSaveAs.setActionCommand("speichernunter");
        popupMenu.add(miSaveAs);        
         
        JMenuItem miExportAsImage = new JMenuItem(messages.getString("guimainmemu_msg19"));
        miExportAsImage.setActionCommand("exportImage");
        popupMenu.add(miExportAsImage);
         
        JMenuItem miCreateReport = new JMenuItem(messages.getString("guimainmemu_msg20"));
        miCreateReport.setActionCommand("createReport");
        popupMenu.add(miCreateReport);        
                
        btSave = new JExtendedButton();
        btSave.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/speichern.png")));
        btSave.setPopupMenu(popupMenu);
        btSave.setBounds(130, 5, btSave.getIcon().getIconWidth(), btSave.getIcon().getIconHeight());
        btSave.setActionCommand("speichern");
        btSave.setToolTipText(messages.getString("guimainmemu_msg2"));        
        
        btDocMode = new JButton();
        btDocMode.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/dokumodus.png")));
        btDocMode.setBounds(320, 5, btDocMode.getIcon().getIconWidth(), btDocMode.getIcon().getIconHeight());
        btDocMode.setActionCommand("dokumodus");
        btDocMode.setToolTipText(messages.getString("guimainmemu_msg14"));

        btDesignMode = new JButton();
        btDesignMode.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/entwurfsmodus_aktiv.png")));
        btDesignMode.setBounds(380, 5, btDesignMode.getIcon().getIconWidth(), btDesignMode.getIcon().getIconHeight());
        btDesignMode.setActionCommand("entwurfsmodus");
        btDesignMode.setToolTipText(messages.getString("guimainmemu_msg3"));

        btActionMode = new JButton();
        btActionMode.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/aktionsmodus.png")));
        btActionMode.setBounds(440, 5, btActionMode.getIcon().getIconWidth(), btActionMode.getIcon().getIconHeight());
        btActionMode.setActionCommand("aktionsmodus");
        btActionMode.setToolTipText(messages.getString("guimainmemu_msg4"));          
        
        lbSpeed = new JLabel("100%");
        lbSpeed.setVisible(true);
        lbSpeed.setToolTipText(messages.getString("guimainmemu_msg15"));
        lbSpeed.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
        lbSpeed.setBounds(510, 0, 120, 44);

        slSpeed = new JSlider(0, 100);
        slSpeed.setToolTipText(messages.getString("guimainmemu_msg16"));
        slSpeed.setMaximum(10);
        slSpeed.setMinimum(1);
        slSpeed.setValue(slSpeed.getMaximum());
        Cable.setDelayFactor(slSpeed.getMaximum() - slSpeed.getValue() + 1);
        slSpeed.setBounds(495, 20, 50, 44);
        slSpeed.setOpaque(false);
        slSpeed.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent arg0) {
                Cable.setDelayFactor(slSpeed.getMaximum() - slSpeed.getValue() + 1);
                lbSpeed.setText("" + slSpeed.getValue() * 10 + "%");
            }

        });        

        if (isSoftwareWizardEnabled()) {
            btWizard = new JButton();
            btWizard.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/button_wizard.png")));
            btWizard.setBounds(670, 5, btWizard.getIcon().getIconWidth(), btWizard.getIcon().getIconHeight());
            btWizard.setActionCommand("wizard");
            btWizard.setToolTipText(messages.getString("guimainmemu_msg6"));
        }
        
        btInfo = new JButton();
        btInfo.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/info.png")));
        btInfo.setBounds(730, 5, btInfo.getIcon().getIconWidth(), btInfo.getIcon().getIconHeight());
        btInfo.setActionCommand("info");
        btInfo.setToolTipText(messages.getString("guimainmemu_msg8"));

        btHelp = new JButton();
        btHelp.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/hilfe.png")));
        btHelp.setBounds(790, 5, btHelp.getIcon().getIconWidth(), btHelp.getIcon().getIconHeight());
        btHelp.setActionCommand("hilfe");
        btHelp.setToolTipText(messages.getString("guimainmemu_msg7"));        

        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if (isSoftwareWizardEnabled() && e.getActionCommand().equals(btWizard.getActionCommand())) {
                    FrameSoftwareWizard gsw = new FrameSoftwareWizard();
                    gsw.setVisible(true);
                } 

                // New
                if (e.getActionCommand().equals(btNew.getActionCommand())) {
                	
                	// If the current project is modified, ask the user whether it should be saved
                    int choice = JOptionPane.YES_OPTION;
                    try {
                        if (ProjectManager.getInstance().isModified()) {
                        	
                            choice = JOptionPane.showConfirmDialog(JMainFrame.getInstance(), messages.getString("guimainmemu_msg9"), 
                                                                   messages.getString("guimainmemu_msg10"), JOptionPane.YES_NO_CANCEL_OPTION);
                        } 
                    } catch (Exception exc) {
                        exc.printStackTrace(Main.debug);
                    }
                    if (choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION) return;
                    
                    if (ProjectManager.getInstance().isModified() && choice == JOptionPane.YES_OPTION) {         
                    	// Save the project
                    	GUIContainer.getInstance().getMainMenu().doClick("btSpeichern");
                    	// The user may have canceled the dialog box
                    	if (ProjectManager.getInstance().isModified()) return;
                    }
                    
                    // Create a new project
                    container.clearAllItems();
                	container.setConfigPanel(null);
                    Information.getInstance().reset();
                    ProjectManager.getInstance().reset();
                }
                
                // Open
                if (e.getActionCommand().equals(btOpen.getActionCommand())) {
                	
                	// If the current project is modified, ask the user whether it should be saved
                    int choice = JOptionPane.YES_OPTION;
                    try {
                        if (ProjectManager.getInstance().isModified()) {
                        	
                        	choice = JOptionPane.showConfirmDialog(JMainFrame.getInstance(), messages.getString("guimainmemu_msg21"), 
                                                                   messages.getString("guimainmemu_msg22"), JOptionPane.YES_NO_CANCEL_OPTION);
                        } 
                    } catch (Exception exc) {
                        exc.printStackTrace(Main.debug);
                    }
                    if (choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION) return;
                    
                    if (ProjectManager.getInstance().isModified() && choice == JOptionPane.YES_OPTION) {         
                    	// Save the project
                    	GUIContainer.getInstance().getMainMenu().doClick("btSpeichern");
                    	// The user may have canceled the dialog box
                    	if (ProjectManager.getInstance().isModified()) return;
                    }
                    
                    // Load a project
                    JFileChooser fcOpen = new JFileChooser();
                    fcOpen.setDialogTitle(messages.getString("main_dlg_OPENTITLE"));
                    fcOpen.setFileFilter(filiusFileFilter);
                    initCurrentFileOrDirSelection(fcOpen);

                    if (fcOpen.showOpenDialog(JMainFrame.getInstance()) == JFileChooser.APPROVE_OPTION) {
                    	if (fcOpen.getSelectedFile() != null) {
                    		Information.getInstance().setLastOpenedDirectory(fcOpen.getSelectedFile().getParent());
                    		try {
                    			Information.getInstance().reset();
                    			ProjectManager.getInstance().load(fcOpen.getSelectedFile().getPath(),
                    					container.getNodeItems(),
                    					container.getCableList(),
                    					container.getDocItems());
                    			container.setConfigPanel(null);
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
                
                // Save
                Boolean saveAs = false;
                
                if (e.getActionCommand().equals(btSave.getActionCommand())) {
                	
                    if (container.getCurrentMode() != ACTION_MODE) {                    	
                    	
                    	String projectPath = ProjectManager.getInstance().getPath();
                    	
                    	if (projectPath != null) {
                    		
                    		boolean success = ProjectManager.getInstance().save(projectPath, container.getNodeItems(),
                    				                                            container.getCableList(), container.getDocItems());                                    
                    		if (!success) {
                    			JOptionPane.showMessageDialog(JMainFrame.getInstance(), messages.getString("guimainmemu_msg11"));
                    		}                    		
                    		
                    	}  
                    	else saveAs = true;
                    }
                }                

                // Save as
                if (saveAs || e.getActionCommand().equals(miSaveAs.getActionCommand())) {
                	
                    if (container.getCurrentMode() != ACTION_MODE) {
                        JFileChooser fsSaveAs = new JFileChooser();
                        fsSaveAs.setDialogTitle(messages.getString("main_dlg_SAVETITLE"));
                        fsSaveAs.setFileFilter(filiusFileFilter);
                        initCurrentFileOrDirSelection(fsSaveAs);

                        // Dialogbox
                        if (fsSaveAs.showSaveDialog(JMainFrame.getInstance()) == JFileChooser.APPROVE_OPTION) {
                        	
                            if (fsSaveAs.getSelectedFile() != null) {
                                Information.getInstance().setLastOpenedDirectory(fsSaveAs.getSelectedFile().getParent());
                                
                                String newFilePath = fsSaveAs.getSelectedFile().getPath();
                                if (!newFilePath.endsWith(".fls")) newFilePath += ".fls";
                                File newFile = new File(newFilePath);
                                
                                String currentFilePath = ProjectManager.getInstance().getPath();
                                File currentFile = null;
                                if (currentFilePath != null) currentFile = new File(currentFilePath);
                                
                                int choice = JOptionPane.YES_OPTION;
                                if (newFile.exists() && !newFile.equals(currentFile)) {
                                	
                                	// Confirmation
                                    choice = JOptionPane.showConfirmDialog(JMainFrame.getInstance(), messages.getString("guimainmemu_msg17"),
                                                                           messages.getString("guimainmemu_msg10"), JOptionPane.YES_NO_OPTION);
                                }
                          
                                if (choice == JOptionPane.YES_OPTION) {                                	
                                	boolean success = ProjectManager.getInstance().save(newFilePath, container.getNodeItems(),
                                    		                                            container.getCableList(), container.getDocItems());                                    
                                    if (!success) {
                                        JOptionPane.showMessageDialog(JMainFrame.getInstance(),
                                                messages.getString("guimainmemu_msg11"));
                                    }
                                }
                            }
                        }
                    }
                }   
                
                if (e.getActionCommand().equals(miExportAsImage.getActionCommand())) {
                	
                	ProjectExport.getInstance().exportAsImage();
                	
                } else if (e.getActionCommand().equals(miCreateReport.getActionCommand())) {
                	
                	ProjectReport.getInstance().createReport();
                	
                } else if (e.getActionCommand().equals(btDesignMode.getActionCommand())) {
                	
                    selectMode(DESIGN_MODE);
                    
                } else if (e.getActionCommand().equals(btActionMode.getActionCommand())) {
                	
                    selectMode(ACTION_MODE);
                    
                } else if (e.getActionCommand().equals(btDocMode.getActionCommand())) {
                	
                    selectMode(DOC_MODE);
                    
                } else if (e.getActionCommand().equals(btInfo.getActionCommand())) {
                	
                    (new AboutDialog(JMainFrame.getInstance())).setVisible(true);
                    
                } else if (e.getActionCommand().equals(btHelp.getActionCommand())) {
                	
                    GUIHelp.getGUIHelp().show();
                }
            }
        };

        btNew.addActionListener(al);
        btOpen.addActionListener(al);
        btSave.addActionListener(al);
        miSaveAs.addActionListener(al);
        miExportAsImage.addActionListener(al);
        miCreateReport.addActionListener(al);
        btDocMode.addActionListener(al);
        btDesignMode.addActionListener(al);
        btActionMode.addActionListener(al);        
        if (isSoftwareWizardEnabled()) {
            btWizard.addActionListener(al);
        }
        btInfo.addActionListener(al);
        btHelp.addActionListener(al);        

        menuPanel.setLayout(null);
        
        menuPanel.add(btNew);
        menuPanel.add(btOpen);
        menuPanel.add(btSave);
        menuPanel.add(btDocMode);
        menuPanel.add(btDesignMode);
        menuPanel.add(btActionMode);        
        menuPanel.add(slSpeed);
        menuPanel.add(lbSpeed);        
        if (isSoftwareWizardEnabled()) {
            menuPanel.add(btWizard);
        }
        menuPanel.add(btHelp);
        menuPanel.add(btInfo);

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
    	
        String projectPath = ProjectManager.getInstance().getPath();
        String lastOpenedDir = Information.getInstance().getLastOpenedDirectory();
        File file = null;
        if (projectPath != null) {
            file = new File(projectPath);
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
        if (diff < 0 && slSpeed.getValue() + diff < 1) {
            slSpeed.setValue(1);
        } else if (diff > 0 && slSpeed.getValue() + diff > 10) {
            slSpeed.setValue(10);
        } else
            slSpeed.setValue(slSpeed.getValue() + diff);
    }

    public boolean doClick(String button) { // manually perform click event on a registered button
    	
        if (button.equals("btAktionsmodus"))
            btActionMode.doClick();
        else if (button.equals("btEntwurfsmodus"))
            btDesignMode.doClick();
        else if (button.equals("btDokumodus"))
            btDocMode.doClick();
        else if (button.equals("btOeffnen"))
            btOpen.doClick();
        else if (button.equals("btSpeichern"))
            btSave.doClick();
        else if (button.equals("btNeu"))
            btNew.doClick();
        else if (button.equals("btWizard"))
            btWizard.doClick();
        else if (button.equals("btHilfe"))
            btHelp.doClick();
        else if (button.equals("btInfo"))
            btInfo.doClick();
        else
            return false;
        return true;
    }

    // set/reset cable highlight, i.e., make all cables normal coloured for
    // simulation and possibly highlight in development view
    private void resetCableHighlighting(int mode) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + 
        		           " (GUIMainMenu), resetCableHL(" + mode + ")");
        
        if (mode == ACTION_MODE) { // change to simulation view: unhighlight all cables
            for (GUICableItem cableItem : container.getCableList()) {
                if (cableItem.getCable() != null) cableItem.getCable().setActive(false);
            }
            GUIEvents.getInstance().unselectCable();
        } else { // change to development view: possibly highlight a cable 
                 // (only for 'Router' configuration)
        	for (GUICableItem cableItem : container.getCableList()) {
                if (cableItem.getCable() != null) cableItem.getCable().setBlocked(false);
            }
            if (container.getConfigPanel() instanceof JConfigRouter) {
                ((JConfigRouter) container.getConfigPanel()).highlightCable();
            }
            if (mode == DOC_MODE) GUIEvents.getInstance().unselectCable();
        }
    }

    public synchronized void selectMode(int mode) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + 
        		           " (GUIMainMenu), selectMode(" + mode + ")");

        if (mode == DESIGN_MODE) {
            resetCableHighlighting(mode); // unhighlight cables   

            btDesignMode.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/entwurfsmodus_aktiv.png")));
            btActionMode.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/aktionsmodus.png")));
            btDocMode.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/dokumodus.png")));
            container.setCurrentMode(DESIGN_MODE);

            GUIHelp.getGUIHelp().loadModeMainPage(DESIGN_MODE);

            stopSimulation();

            btOpen.setEnabled(true);
            btNew.setEnabled(true);
            btSave.setEnabled(true);
            if (isSoftwareWizardEnabled()) {
                btWizard.setEnabled(true);
            }
            
            GUIEvents.getInstance().unfreezeSelectedElement();   
            
            ToolTipManager.sharedInstance().setDismissDelay(3000);
            
        } else if (mode == DOC_MODE) {
        	GUIEvents.getInstance().freezeSelectedElement(); 
            resetCableHighlighting(mode); // unhighlight cables

            btDesignMode.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/entwurfsmodus.png")));
            btActionMode.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/aktionsmodus.png")));
            btDocMode.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/dokumodus_aktiv.png")));
            container.setCurrentMode(DOC_MODE);

            GUIHelp.getGUIHelp().loadModeMainPage(DOC_MODE);

            stopSimulation();

            btOpen.setEnabled(true);
            btNew.setEnabled(true);
            btSave.setEnabled(true);
            if (isSoftwareWizardEnabled()) {
                btWizard.setEnabled(false);
            }
            
            ToolTipManager.sharedInstance().setDismissDelay(3000);
            
        } else if (mode == ACTION_MODE && currentMode != ACTION_MODE) {
            // Main.debug.println("\tMode: MODUS_AKTION");
        	GUIEvents.getInstance().freezeSelectedElement(); 
            resetCableHighlighting(mode); // de-highlight cables

            btDesignMode.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/entwurfsmodus.png")));
            btActionMode.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/aktionsmodus_aktiv.png")));
            btDocMode.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/dokumodus.png")));
            container.setCurrentMode(ACTION_MODE);
            GUIHelp.getGUIHelp().loadModeMainPage(ACTION_MODE);
                        
            switchSpanningTree.apply(container.getNodeItems());

            for (GUINodeItem nodeItem : container.getNodeItems()) {
                SystemSoftware system;
                system = nodeItem.getNode().getSystemSoftware();
                system.start();
            }

            btOpen.setEnabled(false);
            btNew.setEnabled(false);
            btSave.setEnabled(false);
            if (isSoftwareWizardEnabled()) {
                btWizard.setEnabled(false);
            }

            lbSpeed.setEnabled(true);
            slSpeed.setEnabled(true);
            
            // Tooltips remain visible longer
            ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
        }
        currentMode = mode;
    }  

    private void stopSimulation() {
        for (GUINodeItem nodeItem : container.getNodeItems()) {
            SystemSoftware system;
            system = nodeItem.getNode().getSystemSoftware();
            try {
                system.stop();
            } catch (Exception e) {}
        }
        ((JFrame) container.getPacketsAnalyzerDialog()).setVisible(false);
    }

    public JBackgroundPanel getMenupanel() {
        return menuPanel;
    }

    public void setMenupanel(JBackgroundPanel menupanel) {
        this.menuPanel = menupanel;
    }
}
