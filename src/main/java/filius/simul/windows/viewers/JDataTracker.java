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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

import filius.gui.JExtendedTabbedPane;
import filius.gui.JFrameList;
import filius.gui.Palette;
import filius.gui.TabClosingListener;
import filius.hardware.knoten.Host;
import filius.hardware.knoten.InternetNode;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.nachrichten.PacketAnalyzer;
import filius.software.system.InternetNodeOS;
import filius.software.system.SystemSoftware;

/**
 * This class is used to show exchanged messages between components. Its functionality shall be akin to that of
 * wireshark.
 *
 * @author stefan
 */
@SuppressWarnings("serial")
public class PacketsAnalyzerDialog extends JFrame implements AbstractPacketsAnalyzerDialog, I18n {
	
    private JExtendedTabbedPane tabbedPane;
    private static PacketsAnalyzerDialog dialog = null;
    private Hashtable<String, JPanel> openedTabs = new Hashtable<String, JPanel>();
    private Hashtable<String, InternetNodeOS> systems = new Hashtable<String, InternetNodeOS>();
    private Hashtable<String, PacketsAnalyzerTable> tables = new Hashtable<String, PacketsAnalyzerTable>();
    

    public static PacketsAnalyzerDialog getInstance(Frame owner) {
    	
        if (dialog == null) dialog = new PacketsAnalyzerDialog(owner);   
        return dialog;
    }

    @Override
    public void reset() {
    	
        if (dialog != null) dialog.setVisible(false);      
        dialog = null;
    }

    /** Do not use this constructor. It is only used for testing! */
    public PacketsAnalyzerDialog() {}

    private PacketsAnalyzerDialog(Frame owner) {
        super();

        JFrameList.getInstance().add(this);
        ((JFrame) owner).getLayeredPane().setLayer(this, JLayeredPane.PALETTE_LAYER);
        setVisible(false);

        setTitle(messages.getString("lauscherdialog_msg1"));    
        
        Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/gfx/allgemein/nachrichtenfenster_icon.png"));
        setIconImage(image);

        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        tabbedPane = new JExtendedTabbedPane();
        getContentPane().add(tabbedPane, BorderLayout.CENTER);    
        tabbedPane.addClosingListener(new TabClosingListener() {
        	
            public boolean canClose(ChangeEvent e) {   
            	removeCurrentTable();
            	// Close the dialog if this is the last tab
            	if (tabbedPane.getTabCount() == 1) setVisible(false);
                return true;
            }
        });     

        addWindowListener(new WindowAdapter() {
        	@Override
            public void windowOpened(WindowEvent e) {
        		setSize(900, 700);
        		
                int screenWidth = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();                
                int x = owner.getX() + (owner.getWidth() - getWidth())/2;
                int y = owner.getY() + (owner.getHeight() - getHeight())/2;
                if (x+getWidth() > screenWidth) x = screenWidth - getWidth();
                if (x < 0) x = 0;
                setLocation(x,y); 
            }
            @Override
            public void windowActivated(WindowEvent e) {
                updateTabTitles();
                clearUnavailableComponents();
            }            
        });
    }

    /**
     * Diese Methode fuegt eine Tabelle hinzu
     */
    @Override
    public void addTable(SystemSoftware system, String identifier) {
    	
        final PacketsAnalyzerTable table; 
        final MessageDetailsPanel detailsPanel = new MessageDetailsPanel(identifier);

        if (openedTabs.get(identifier) == null) {
            table = new PacketsAnalyzerTable(this, identifier);
            table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    if (table.getSelectedRow() >= 0) {
                        detailsPanel.update(table.getValueAt(table.getSelectedRow(), 0));
                    } else {
                        detailsPanel.clear();
                    }
                }
            });            

            JScrollPane tableScrollPane = new JScrollPane(table);
            table.setScrollPane(tableScrollPane);
            
            JScrollPane detailsScrollPane = new JScrollPane(detailsPanel);

            JSplitPane splitPane = new JSplitPane();
            splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
            splitPane.setTopComponent(tableScrollPane);
            splitPane.setBottomComponent(detailsScrollPane);
            splitPane.setDividerSize(5);
            splitPane.setResizeWeight(1D); 
            splitPane.setDividerLocation(400);     
            
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(splitPane, BorderLayout.CENTER);               

            tabbedPane.add(panel);
            tabbedPane.setSelectedComponent(panel);

            openedTabs.put(identifier, panel);
            systems.put(identifier, (InternetNodeOS) system);
            tables.put(identifier, table);

            updateTabTitles();
        }
        // if there is already a tab opened for this system set it to selected
        else {
            tabbedPane.setSelectedComponent(openedTabs.get(identifier));
            tables.get(identifier).update();
        }
    }
    
    /** <b>getIdentifier</b> returns the identifier (the MAC address) associated to the tabIndex 
     *  
     */
    private String getIdentifier(int tabIndex) {

    	Component comp = tabbedPane.getComponentAt(tabIndex);
    	if (comp == null) return "";
    	
    	for (String identifier : openedTabs.keySet()) {
    		if (comp.equals(openedTabs.get(identifier))) return identifier;	
    	}
    	return "";
    }

    /** <b>updateTabTitles</b> updates the title of all the tabs 
     *  
     */
    private void updateTabTitles() {
    	
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
        	
        	String identifier = getIdentifier(i);
        	
        	if (identifier != "") {     
        		SystemSoftware system = systems.get(identifier);
        		String ipAddress = ((InternetNode) system.getNode()).getNICbyMAC(identifier).getIp();
        		String tabTitle;
        		if (system.getNode() instanceof Host && ((Host) system.getNode()).getUseIPAsName()) {
        			tabTitle = ipAddress;
        		} else {
        			tabTitle = system.getNode().getDisplayName() + " - " + ipAddress;
        		}
        		tabbedPane.setTitleAt(i, tabTitle);           
            }
        }
    }

    public String getTabTitle(String interfaceId) {
    	
        String title = interfaceId.replaceAll(":", "-");
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Component tab = tabbedPane.getComponentAt(i);
            if (tab == openedTabs.get(interfaceId)) {
            	title = tabbedPane.getTitleAt(i);
                break;
            }
        }
        return title;
    }
    
    private void clearUnavailableComponents() {
    	
        for (Entry<String, InternetNodeOS> system : systems.entrySet()) {
            if (!system.getValue().isStarted()) {
                removeTable(system.getKey());
            }
        }
    }

    /** <b>removeCurrentTable</b> removes the table of the current tab 
     *  
     */
    public void removeCurrentTable() {
    	
    	String identifier = getIdentifier(tabbedPane.getSelectedIndex());       
        openedTabs.remove(identifier);
        tables.remove(identifier);  
    }

    private void removeTable(String identifier) {
    	
        removeTable(identifier, null);
    }

    @Override
    public void removeTable(String identifier, JPanel panel) {
    	
        if (identifier != null) {
        	if (panel == null) panel = openedTabs.get(identifier);
            openedTabs.remove(identifier);
            tables.remove(identifier);
            tabbedPane.remove(panel);
        }
    }  

    @Override
    public boolean isEmpty() {
    	
    	return (tables.size() == 0);
    };

    private class MessageDetailsPanel extends JPanel {
    	
        private String macAddress;

        public MessageDetailsPanel(String macAddress) {
            this.macAddress = macAddress;
            this.setLayout(new BorderLayout());
            this.setBackground(Palette.PACKETS_ANALYZER_PANEL_BG);
        }

        public void clear() {
            removeAll();
            updateUI();
        }

        public void update(Object messageNo) {
            if (messageNo != null) {
                Object[][] daten = PacketAnalyzer.getInstance().getDataEntries(macAddress, false);
                int number = Integer.parseInt(messageNo.toString());
                int dataSetNo = 0;
                int currNo = 0;
                for (; dataSetNo < daten.length; dataSetNo++) {
                    currNo = Integer.parseInt(daten[dataSetNo][0].toString());
                    if (currNo == number)
                        break;
                }

                Object[] dataSet = daten[dataSetNo];
                DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(messages.getString("rp_lauscher_msg1")
                        + ": " + dataSet[0] + " / " + messages.getString("rp_lauscher_msg2") + ": " + dataSet[1]);
                for (; dataSetNo < daten.length
                        && Integer.parseInt(daten[dataSetNo][0].toString()) == number; dataSetNo++) {
                    dataSet = daten[dataSetNo];
                    DefaultMutableTreeNode layerNode = new DefaultMutableTreeNode(dataSet[5], true);
                    DefaultMutableTreeNode dateNode;
                    DefaultMutableTreeNode labelNode;
                    if (dataSet[2] != null && !dataSet[2].toString().isEmpty()) {
                        String srcLabel = String.format("%-15s", messages.getString("rp_lauscher_msg3") + ": ");
                        dateNode = new DefaultMutableTreeNode(srcLabel + dataSet[2]);
                        layerNode.add(dateNode);
                    }
                    if (dataSet[3] != null && !dataSet[3].toString().isEmpty()) {
                        String destLabel = String.format("%-15s", messages.getString("rp_lauscher_msg4") + ": ");
                        dateNode = new DefaultMutableTreeNode(destLabel + dataSet[3]);
                        layerNode.add(dateNode);
                    }
                    if (dataSet[4] != null && !dataSet[4].toString().isEmpty()) {
                        String protocolLabel = String.format("%-15s", messages.getString("rp_lauscher_msg5") + ": ");
                        dateNode = new DefaultMutableTreeNode(protocolLabel + dataSet[4]);
                        layerNode.add(dateNode);
                    }
                    if (dataSet[6] != null && !dataSet[6].toString().isEmpty()) {
                        String contentLabel = String.format("%-15s", messages.getString("rp_lauscher_msg7") + ": ");
                        if (dataSet[6].toString().contains("\n")) {
                            labelNode = new DefaultMutableTreeNode(contentLabel);
                            dateNode = new DefaultMutableTreeNode(dataSet[6]);
                            labelNode.add(dateNode);
                            layerNode.add(labelNode);
                        } else {
                            dateNode = new DefaultMutableTreeNode(contentLabel + dataSet[6]);
                            layerNode.add(dateNode);
                        }
                    }
                    rootNode.add(layerNode);
                }
                JTree detailsTree = new JTree(rootNode);
                for (int i = 0; i < detailsTree.getRowCount(); i++) {
                    detailsTree.expandRow(i);
                }
                detailsTree.setCellRenderer(new MultiLineCellRenderer());
                detailsTree.setBackground(Palette.PACKETS_ANALYZER_PANEL_BG);
                removeAll();
                add(detailsTree, BorderLayout.WEST);
                updateUI();
            }
        }
    }

    // This code is based on an example published at
    // http://www.java2s.com/Code/Java/Swing-Components/MultiLineTreeExample.htm
    class MultiLineCellRenderer extends JPanel implements TreeCellRenderer {
    	
        protected JLabel icon;
        protected TreeTextArea text;

        public MultiLineCellRenderer() {
            setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
            setBackground(Palette.PACKETS_ANALYZER_PANEL_BG);
            
            icon = new JLabel(); 
            icon.setBackground(Palette.PACKETS_ANALYZER_PANEL_BG);
            add(icon);            
            add(Box.createHorizontalStrut(4));
            
            text = new TreeTextArea();
            add(text);
        }

        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean expanded,
                boolean leaf, int row, boolean hasFocus) {
            String stringValue = tree.convertValueToText(value, isSelected, expanded, leaf, row, hasFocus);
            setEnabled(tree.isEnabled());
            text.setText(stringValue);
            text.setSelect(isSelected);
            return this;
        }

        public Dimension getPreferredSize() {
            Dimension iconD = icon.getPreferredSize();
            Dimension textD = text.getPreferredSize();
            int height = iconD.height < textD.height ? textD.height : iconD.height;
            return new Dimension(iconD.width + textD.width, height);
        }

        class TreeTextArea extends JTextArea {
            Dimension preferredSize;

            TreeTextArea() {
                setLineWrap(true);
                setWrapStyleWord(true);
                setOpaque(true);
                Font font = getFont();
                setFont(new Font(Font.MONOSPACED, Font.BOLD, font.getSize()));
            }

            public void setPreferredSize(Dimension d) {
                if (d != null) {
                    preferredSize = d;
                }
            }

            public Dimension getPreferredSize() {
                return preferredSize;
            }

            public void setText(String str) {
                Font font = getFont();

                // Alternative to getFontMetrics seems a bit complicated:
                //Graphics2D g2 = (Graphics2D)g;    // <- where do we get g?
                //FontRenderContext frc = g2.getFontRenderContext( );
                //LineMetrics lm = font.getLineMetrics(line, frc);
                
                @SuppressWarnings("deprecation")
				FontMetrics fm = getToolkit().getFontMetrics(font);               
                BufferedReader br = new BufferedReader(new StringReader(str));
                String line;
                int maxWidth = 0, lines = 0;
                try {
                    while ((line = br.readLine()) != null) {
                        int width = SwingUtilities.computeStringWidth(fm, line);   
                        if (maxWidth < width) {
                            maxWidth = width;
                        }
                        lines++;
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                lines = (lines < 1) ? 1 : lines;
                int height = fm.getHeight() * lines;
                setPreferredSize(new Dimension(maxWidth + 12, height));
                super.setText(str);
            }

            void setSelect(boolean isSelected) {
                Color bColor;
                if (isSelected) {
                    bColor = Palette.PACKETS_ANALYZER_SELECTED_PANEL_BG;
                } else {
                    bColor = Palette.PACKETS_ANALYZER_PANEL_BG;
                }
                super.setBackground(bColor);
            }
        }
    }
}
