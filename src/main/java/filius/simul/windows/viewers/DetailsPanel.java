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
package filius.gui.modes.simulation.viewers;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

import filius.auxiliary.I18n;
import filius.gui.components.JExtendedTree;
import filius.gui.components.Palette;

@SuppressWarnings("serial")
public class DetailsPanel extends JPanel implements I18n {
	
    private String macAddress;

    public DetailsPanel(String macAddress) {
    	
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
            Object[][] data = PacketAnalyzer.getInstance().getDataEntries(macAddress, false);
            int number = Integer.parseInt(messageNo.toString());
            int dataSetNo = 0;
            int currNo = 0;
            for (; dataSetNo < data.length; dataSetNo++) {
                currNo = Integer.parseInt(data[dataSetNo][0].toString());
                if (currNo == number)  break;
            }

            Object[] dataSet = data[dataSetNo];
            DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(messages.getString("packetanalyzer_msg1")
                    + ": " + dataSet[0] + " / " + messages.getString("packetanalyzer_msg2") + ": " + dataSet[1]);
            
            DefaultMutableTreeNode parentNode = rootNode;
            
            for (; dataSetNo < data.length && Integer.parseInt(data[dataSetNo][0].toString()) == number; dataSetNo++) {
            	
                dataSet = data[dataSetNo];
                DefaultMutableTreeNode layerNode = new DefaultMutableTreeNode(dataSet[5], true);
                DefaultMutableTreeNode dateNode;
                DefaultMutableTreeNode labelNode;
                if (dataSet[2] != null && !dataSet[2].toString().isEmpty()) {
                    String srcLabel = String.format("%-15s", messages.getString("packetanalyzer_msg3") + ": ");
                    dateNode = new DefaultMutableTreeNode(srcLabel + dataSet[2]);
                    layerNode.add(dateNode);
                }
                if (dataSet[3] != null && !dataSet[3].toString().isEmpty()) {
                    String destLabel = String.format("%-15s", messages.getString("packetanalyzer_msg4") + ": ");
                    dateNode = new DefaultMutableTreeNode(destLabel + dataSet[3]);
                    layerNode.add(dateNode);
                }
                if (dataSet[4] != null && !dataSet[4].toString().isEmpty()) {
                    String protocolLabel = String.format("%-15s", messages.getString("packetanalyzer_msg5") + ": ");
                    dateNode = new DefaultMutableTreeNode(protocolLabel + dataSet[4]);
                    layerNode.add(dateNode);
                }
                if (dataSet[6] != null && !dataSet[6].toString().isEmpty()) {
                    String contentLabel = String.format("%-15s", messages.getString("packetanalyzer_msg7") + ": ");
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
                parentNode.add(layerNode);
                parentNode = layerNode;
            }
            JExtendedTree detailsTree = new JExtendedTree(rootNode);
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

    			super.setBackground(isSelected ? Palette.PACKETS_ANALYZER_SELECTED_PANEL_BG : Palette.PACKETS_ANALYZER_PANEL_BG);
    		}
    	}
    }
}