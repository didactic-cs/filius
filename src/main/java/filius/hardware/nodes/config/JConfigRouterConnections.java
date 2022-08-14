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
package filius.gui.netzwerksicht.config;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;

import filius.gui.GUIContainer;
import filius.hardware.NetworkInterface;
import filius.hardware.Port;
import filius.hardware.knoten.InternetNode;
import filius.hardware.knoten.Node;
import filius.hardware.knoten.Router;
import filius.hardware.Cable;
import filius.rahmenprogramm.I18n;

/**
 * @author stefan
 * 
 */
@SuppressWarnings("serial")
public class JConfigRouterConnections extends JDialog implements I18n {
	
    private class LinePanel extends JPanel {
        Point lineStart = new Point(0, 0);
        Point lineEnd = new Point(0, 0);
        Color lineColor1 = new Color(130, 130, 130);
        Color lineColor2 = new Color(170, 170, 170);
        Color lineColor3 = new Color(190, 190, 190);

        LinePanel() {
            super();
            this.setOpaque(false); 
        }

        public void setStartPoint(int x, int y) {
            lineStart = new Point(x, y);
        }

        public void setEndPoint(int x, int y) {
            lineEnd = new Point(x, y);
        }

        public String toString() {
            return "[" + "name='" + getName() + "', " + "start=(" + lineStart.x + "/" + lineStart.y + "), " + "end=("
                    + lineEnd.x + "/" + lineEnd.y + "), " + "color=" + lineColor1.toString() + ", " + "bounds="
                    + getBounds() + "]";
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);            
            Graphics2D g2 = (Graphics2D) g;
            
            g2.setColor(lineColor1);
            g2.setStroke(new BasicStroke(10));
            g2.drawLine(lineStart.x, lineStart.y, lineEnd.x, lineEnd.y);
            
            g2.setColor(lineColor2);
            g2.setStroke(new BasicStroke(8));
            g2.drawLine(lineStart.x, lineStart.y, lineEnd.x, lineEnd.y);
            
            g2.setColor(lineColor3);
            g2.setStroke(new BasicStroke(4));
            g2.drawLine(lineStart.x, lineStart.y, lineEnd.x, lineEnd.y);  
        }
    }
    
    //------------------------------------------------------------------------------------------------------
	
    private static final int LINE_HEIGHT = 36;

    private final ImageIcon nicIcon = new ImageIcon(getClass().getResource("/gfx/hardware/rj45.png"));

    private GUIContainer container;
    private Router router;

    private JButton[] btnLocal = new JButton[8];
    private JLabel[] lblLocal = new JLabel[8];
    private JPanel buttonPanel;
    private JPanel cablePanel;
    private JPanel remoteInterfacesPanel;
    private JPanel localInterfacesPanel;
    private JButton[] btnRemote;
    private JButton btnAddInterface;
    private JButton btnRemoveInterface;
    private JLabel[] lblRemote;  
    

    public JConfigRouterConnections(Frame owner, Router router) {
    	
        super(owner, true);
        container = GUIContainer.getInstance();        
        this.router = router;
        
        initComponents();
        
        // When closing the dialog window, do quit correctly
        addWindowListener(new WindowAdapter() {        	
        	public void windowClosing(WindowEvent e) { onCloseAction(); }
        });

        // Close when Escape key is pressed
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
        	public boolean dispatchKeyEvent(KeyEvent e) {        		
        		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) { onCloseAction(); }        	
        		return false;
        	}
        });    

        updateAll();
    }
    
    public void onCloseAction() {
    	
		setVisible(false);                
        container.getConfigPanel().updateDisplayedValues();      
        container.getConfigPanel().maximize();
	}

    private void initComponents() {
    		
	    // - create assignment area
	    JPanel upperCompound = new JPanel();
	    // upperCompound.setBackground(Color.GREEN);
	    upperCompound.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	    upperCompound.setLayout(new BoxLayout(upperCompound, BoxLayout.X_AXIS));
	    upperCompound.setPreferredSize(new Dimension(700, 360));
	
	    initRemoteInterfaces();
	    initLocalInterfaces();
	
	    // -- create visual cable connections (middle area)
	    cablePanel = new JPanel();
	    cablePanel.setPreferredSize(new Dimension(240, 700));
	
	    upperCompound.add(remoteInterfacesPanel);
	    upperCompound.add(cablePanel);
	    upperCompound.add(localInterfacesPanel);
	
	    // - create note area
	    JPanel noteCompound = new JPanel();
	    // noteCompound.setBackground(Color.BLUE);
	    noteCompound.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	    JTextArea usageNote = new JTextArea(messages.getString("jvermittlungsrechnerkonfiguration_msg19"));
	    usageNote.setOpaque(false);
	    usageNote.setEditable(false);
	    usageNote.setLineWrap(true);
	    usageNote.setWrapStyleWord(true);
	    usageNote.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
	    usageNote.setSize(new Dimension(700, 200));
	    // usageNote.setBorder(BorderFactory.createLineBorder(Color.GRAY,2));
	    noteCompound.add(Box.createVerticalGlue());
	    noteCompound.add(usageNote, BorderLayout.CENTER);
	    noteCompound.setMinimumSize(new Dimension(700, 200));
	
	    // - create main button area
	    JPanel buttonCompound = new JPanel();
	    // buttonCompound.setBackground(Color.RED);
	    buttonCompound.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	    
	    JButton btnClose = new JButton(messages.getString("jvermittlungsrechnerkonfiguration_msg20"));        
	    btnClose.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) { onCloseAction(); }
	    });
	
	    buttonCompound.add(btnClose);
	    buttonCompound.setMinimumSize(new Dimension(200, 50));
	
	    this.getContentPane().add(upperCompound, BorderLayout.NORTH);
	    this.getContentPane().add(noteCompound, BorderLayout.CENTER);
	    this.getContentPane().add(buttonCompound, BorderLayout.SOUTH);
	}

	private void initRemoteInterfaces() {
		
	    remoteInterfacesPanel = new JPanel();
	    remoteInterfacesPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
	    remoteInterfacesPanel.setPreferredSize(new Dimension(250, 700));
	    remoteInterfacesPanel.setMaximumSize(remoteInterfacesPanel.getPreferredSize());
	    remoteInterfacesPanel.setSize(400, remoteInterfacesPanel.getHeight());
	
	    JLabel lblRemoteTitle = new JLabel(messages.getString("jvermittlungsrechnerkonfiguration_msg21"));
	    remoteInterfacesPanel.add(lblRemoteTitle);
	
	    btnRemote = new JButton[8];
	    lblRemote = new JLabel[8];
	    for (int i = 0; i < 8; i++) {
	
	        btnRemote[i] = new JButton(nicIcon);
	        lblRemote[i] = new JLabel();
	        remoteInterfacesPanel.add(btnRemote[i]);
	        remoteInterfacesPanel.add(lblRemote[i]);
	    }
	    SpringLayout layoutRemote = new SpringLayout();
	
	    layoutRemote.putConstraint(SpringLayout.NORTH, lblRemoteTitle, 5, SpringLayout.NORTH, remoteInterfacesPanel);
	    layoutRemote.putConstraint(SpringLayout.HORIZONTAL_CENTER, lblRemoteTitle, 0, SpringLayout.HORIZONTAL_CENTER, remoteInterfacesPanel);
	    
	    for (int i = 0; i < 8; i++) {	    	
	    	layoutRemote.putConstraint(SpringLayout.NORTH, btnRemote[i], 25 + i * LINE_HEIGHT, SpringLayout.NORTH, remoteInterfacesPanel);
		    layoutRemote.putConstraint(SpringLayout.EAST, btnRemote[i], 0, SpringLayout.EAST, remoteInterfacesPanel);
		    
		    layoutRemote.putConstraint(SpringLayout.VERTICAL_CENTER, lblRemote[i], 0, SpringLayout.VERTICAL_CENTER, btnRemote[i]);
		    layoutRemote.putConstraint(SpringLayout.EAST, lblRemote[i], -10, SpringLayout.WEST, btnRemote[i]);
	    }
	    
	    remoteInterfacesPanel.setLayout(layoutRemote);
	}

	private void initLocalInterfaces() {
		
	    localInterfacesPanel = new JPanel();
	    localInterfacesPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
	    localInterfacesPanel.setPreferredSize(new Dimension(250, 700));
	    localInterfacesPanel.setMaximumSize(localInterfacesPanel.getPreferredSize());
	    localInterfacesPanel.setSize(400, localInterfacesPanel.getHeight());
	
	    JLabel lblLocalTitle = new JLabel(messages.getString("jvermittlungsrechnerkonfiguration_msg22"));
	    localInterfacesPanel.add(lblLocalTitle);
	
	    for (int i = 0; i < 8; i++) {
	        btnLocal[i] = new JButton(nicIcon);
	        btnLocal[i].addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent evt) {
	                int currentIdx = -1;
	                int markedIdx = -1;
	                for (int i = 0; i < 8; i++) {
	                    if (btnLocal[i].equals(evt.getSource())) {
	                        currentIdx = i;
	                    }
	                    if (btnLocal[i].isOpaque() && btnLocal[i].getBackground().equals(Color.YELLOW)) {
	                        markedIdx = i;
	                    }
	                }
	
	                if (markedIdx == currentIdx) {
	                    updateLocalInterfaces();
	                } else if (markedIdx == -1) {
	                    btnLocal[currentIdx].setBackground(Color.YELLOW);
	                    btnLocal[currentIdx].setOpaque(true);
	                } else if (markedIdx >= 0) {
	                    Port port1 = router.getNICList().get(markedIdx).getPort();
	                    Port port2 = router.getNICList().get(currentIdx).getPort();
	                    boolean swapped = JConfigRouterConnections.this.swapConnections(port1, port2);
	                    
	                    // There is an issue when updateConnections is called after two empty ports are swapped
	                    //updateAll();
	                    updateRemoteInterfaces();    
	                    updateLocalInterfaces(); 
	                	if (swapped)  updateConnections();	                    
	                }
	            }
	        });
	        localInterfacesPanel.add(btnLocal[i]);
	
	        lblLocal[i] = new JLabel();
	        localInterfacesPanel.add(lblLocal[i]);
	    }
	
	    btnAddInterface = new JButton("+");
	    Font f = btnAddInterface.getFont();
	    btnAddInterface.setFont(f.deriveFont(14F));          
	    btnAddInterface.setPreferredSize(new Dimension(42, 20));
	    btnAddInterface.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	            router.addNIC();
	            updateAll();
	        }
	    });
	    
	    btnRemoveInterface = new JButton("–");        
	    f = btnRemoveInterface.getFont();
	    btnRemoveInterface.setFont(f.deriveFont(14F));      
	    btnRemoveInterface.setPreferredSize(new Dimension(42, 20));
	    btnRemoveInterface.addActionListener(new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent e) {
	            List<NetworkInterface> nics = router.getNICList();
	            if (!nics.isEmpty()) {
	                NetworkInterface nic = nics.get(nics.size() - 1);	 
	                container.removeCableItem(nic.getPort().getCable().getCableItem());
	                router.removeNIC(nic);
	                updateAll();
	            }
	        }
	    });
	
	    buttonPanel = new JPanel();
	    buttonPanel.setOpaque(false);
	    buttonPanel.add(btnAddInterface);
	    buttonPanel.add(btnRemoveInterface);
	    localInterfacesPanel.add(buttonPanel);
	
	    // ### Layout (Local interfaces)
	    SpringLayout layoutLocal = new SpringLayout();
	    layoutLocal.putConstraint(SpringLayout.NORTH, lblLocalTitle, 5, SpringLayout.NORTH, localInterfacesPanel);
	    layoutLocal.putConstraint(SpringLayout.HORIZONTAL_CENTER, lblLocalTitle, 0, SpringLayout.HORIZONTAL_CENTER, localInterfacesPanel);
	    
	    for (int i = 0; i < 8; i++) {
	    	layoutLocal.putConstraint(SpringLayout.NORTH, btnLocal[i], 25 + i * LINE_HEIGHT, SpringLayout.NORTH, localInterfacesPanel);
		    layoutLocal.putConstraint(SpringLayout.WEST, btnLocal[i], 0, SpringLayout.WEST, localInterfacesPanel);
		    
		    layoutLocal.putConstraint(SpringLayout.VERTICAL_CENTER, lblLocal[i], 0, SpringLayout.VERTICAL_CENTER, btnLocal[i]);
		    layoutLocal.putConstraint(SpringLayout.WEST, lblLocal[i], 10, SpringLayout.EAST, btnLocal[i]);
	    }
	    
	    layoutLocal.putConstraint(SpringLayout.NORTH, buttonPanel, 0, SpringLayout.SOUTH, btnLocal[7]);
	    layoutLocal.putConstraint(SpringLayout.HORIZONTAL_CENTER, buttonPanel, 0, SpringLayout.HORIZONTAL_CENTER, localInterfacesPanel);
	    
	    localInterfacesPanel.setLayout(layoutLocal);
	}

    private void updateRemoteInterfaces() {
    	
        int index = 0;
        
        for (NetworkInterface nic : router.getNICList()) {            

            Node node = getNodeByNIC(nic);
            if (node != null) {
                String remoteAddress = "";
                Cable cable = nic.getPort().getCable(); 
                Port[] ports = cable.getPorts();
                for (Port port : ports) {
                    if (port.getNIC() != null && port.getNIC() != nic) {
                        remoteAddress = port.getNIC().getIp();
                    }
                }
                btnRemote[index].setVisible(true);
                if (node instanceof InternetNode) {
                    lblRemote[index].setText(node.getDisplayName() + " (" + remoteAddress + ")");
                } else {
                    lblRemote[index].setText(node.getDisplayName());
                }
            } else {
                btnRemote[index].setVisible(false);
                lblRemote[index].setText("");
            }
            
            index++;
        }

        for (int i = index; i < 8; i++) {
            btnRemote[i].setVisible(false);
            lblRemote[i].setText("");
        }

    }

    private void updateLocalInterfaces() {
    	
        int nicNr = 0;
        
        for (NetworkInterface nic : router.getNICList()) {            
            lblLocal[nicNr].setText("NIC " + nicNr + ": " + nic.getIp());

            if (isNICConnected(nic)) {
                btnLocal[nicNr].setOpaque(true);
                btnLocal[nicNr].setBackground(Color.GREEN);
                btnLocal[nicNr].setEnabled(true);
            } else {
                btnLocal[nicNr].setOpaque(true);
                btnLocal[nicNr].setBackground(Color.RED);
                btnLocal[nicNr].setEnabled(true);
            }
            nicNr++;
        }
        
        for (int i = nicNr; i < 8; i++) {
            btnLocal[i].setOpaque(false);
            btnLocal[i].setEnabled(false);
            lblLocal[i].setText("");
        }
        
        btnAddInterface.setEnabled(nicNr < 8);
        btnRemoveInterface.setEnabled(nicNr > 2);
    }

    private void updateConnections() {
    	
        cablePanel.removeAll();
        SpringLayout cableLayout = new SpringLayout();
        cablePanel.setLayout(cableLayout);
        
        int nicNr = 0;
        for (NetworkInterface nic : router.getNICList()) {
            if (isNICConnected(nic)) {
        	   LinePanel line = new LinePanel();
                int yPos = 27 + (int) ((nicNr + 0.5) * LINE_HEIGHT);
                line.setStartPoint(-2, yPos);
                line.setEndPoint(282, yPos);
                cableLayout.putConstraint(SpringLayout.WEST, line, 0, SpringLayout.WEST, cablePanel);
                cableLayout.putConstraint(SpringLayout.NORTH, line, 0, SpringLayout.NORTH, cablePanel);
                line.setPreferredSize(new Dimension(280, 700));
                cablePanel.add(line);
            }
            nicNr++;
        }
        cablePanel.repaint();
    }

    private void updateAll() {
    	
        updateRemoteInterfaces();
        updateLocalInterfaces();
        updateConnections();
    }
    
    /** 
     * <b>isNICConnected</b> checks whether the given network interface
     * is connected to a node.
     * 
     * @param nic The network interface the connection of which is to be checked. 
     * @return true when the network interface is connected to a node.
     */
    private boolean isNICConnected(NetworkInterface nic) {
        
    	return (nic.getPort().getRemotePort() != null);
    }

    /** 
     * <b>getNodeByNIC</b> returns the node to which the given network interface
     * is connected to via a cable.
     * 
     * @param nic The network interface for which the connected node is looked for. 
     * @return A Node or null.
     */
    private Node getNodeByNIC(NetworkInterface nic) {
        
        Port remotePort =  nic.getPort().getRemotePort();
        if (remotePort == null) return null;        
        return remotePort.getOwner();
    }

    private boolean swapConnections(Port port1, Port port2) {

    	Cable c1 = port1.getCable();    	
    	Cable c2 = port2.getCable();   
    	if (c1 == c2) return false;
    	
    	Port  p1 = port1.getRemotePort();
    	Port  p2 = port2.getRemotePort();    	
        
        if (c1 != null && c2 != null) {
            c1.disconnect();
            c2.disconnect();            
            c1.setPorts(port2, p1);
            c2.setPorts(port1, p2);            
            
        } else if (c1 == null && c2 != null) {
            c2.disconnect();
            c2.setPorts(port1, p2);            
            
        } else if (c1 != null && c2 == null) {
            c1.disconnect();            
            c1.setPorts(port2, p1);
        }
        
        return true;
    }
}
