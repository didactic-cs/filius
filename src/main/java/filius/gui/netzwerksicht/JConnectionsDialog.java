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
package filius.gui.netzwerksicht;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import filius.Main;
import filius.gui.GUIContainer;
import filius.hardware.Kabel;
import filius.hardware.NetzwerkInterface;
import filius.hardware.Port;
import filius.hardware.Verbindung;
import filius.hardware.knoten.InternetKnoten;
import filius.hardware.knoten.Knoten;
import filius.hardware.knoten.LokalerKnoten;
import filius.hardware.knoten.Vermittlungsrechner;
import filius.rahmenprogramm.I18n;

/**
 * @author stefan
 * 
 */
public class JConnectionsDialog extends JDialog implements I18n {
    private static final int LINE_HEIGHT = 36;

    private final ImageIcon nicIcon = new ImageIcon(getClass().getResource("/gfx/hardware/rj45.png"));

    private Vermittlungsrechner internetSwitch;
    private Map<NetzwerkInterface, Knoten> nicToNodeMap = new HashMap<NetzwerkInterface, Knoten>();

    private JButton[] btnLocal = new JButton[8];
    private JLabel[] lblLocal = new JLabel[8];
    private JPanel buttonPanel;
    private JPanel cablePanel;
    private JPanel remoteInterfacesPanel, localInterfacesPanel;
    private JButton[] btnRemote;
    private JButton btnAddInterface;
    private JButton btnRemoveInterface;
    private JLabel[] lblRemote;;

    private class LinePanel extends JPanel {
        Point lineStart = new Point(0, 0);
        Point lineEnd = new Point(0, 0);
        Color lineColor = new Color(64, 64, 64);

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

        public void setColor(Color col) {
            lineColor = col;
        }

        public String toString() {
            return "[" + "name='" + getName() + "', " + "start=(" + lineStart.x + "/" + lineStart.y + "), " + "end=("
                    + lineEnd.x + "/" + lineEnd.y + "), " + "color=" + lineColor.toString() + ", " + "bounds="
                    + getBounds() + "]";
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(lineColor);
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(lineStart.x, lineStart.y, lineEnd.x, lineEnd.y);
        }
    }

    public JConnectionsDialog(Frame owner, Vermittlungsrechner internetSwitch) {
        super(owner, true);
        this.internetSwitch = internetSwitch;
        initComponents();

        updateAll();
    }

    private void updateNicToNodeMap(Vermittlungsrechner internetSwitch) {
        nicToNodeMap.clear();
        for (NetzwerkInterface nic : internetSwitch.getNetzwerkInterfaces()) {
            Knoten node = getConnectedComponent(nic);
            if (node != null) {
                nicToNodeMap.put(nic, node);
            }
        }
    }

    private void updateRemoteInterfaces() {
        int nicNr = 0;
        for (NetzwerkInterface nic : internetSwitch.getNetzwerkInterfaces()) {
            nicNr++;

            Knoten node = nicToNodeMap.get(nic);
            if (node != null) {
                String remoteAddress = "";
                Verbindung connection = this.getConnectedCable(nic);
                Port[] ports = connection.getAnschluesse();
                for (Port port : ports) {
                    if (port.getNIC() != null && port.getNIC() != nic) {
                        remoteAddress = port.getNIC().getIp();
                    }
                }
                btnRemote[nicNr - 1].setEnabled(false);
                if (node instanceof filius.hardware.knoten.InternetKnoten) {
                    lblRemote[nicNr - 1].setText(node.holeAnzeigeName() + " (" + remoteAddress + ")");
                } else {
                    lblRemote[nicNr - 1].setText(node.holeAnzeigeName());
                }
            } else {
                btnRemote[nicNr - 1].setEnabled(false);
                btnRemote[nicNr - 1].setVisible(false);
                lblRemote[nicNr - 1].setText("");
            }
        }

        for (int i = nicNr; i < 8; i++) {
            btnRemote[i].setEnabled(false);
            btnRemote[i].setVisible(false);
            lblRemote[i].setText("");
        }

    }

    private void updateLocalInterfaces() {
        int nicNr = 0;
        Knoten node;
        for (NetzwerkInterface nic : internetSwitch.getNetzwerkInterfaces()) {
            nicNr++;
            lblLocal[nicNr - 1].setText("NIC " + nicNr + ": " + nic.getIp());

            node = getConnectedComponent(nic);
            if (node != null) {
                btnLocal[nicNr - 1].setOpaque(true);
                btnLocal[nicNr - 1].setBackground(Color.GREEN);
                btnLocal[nicNr - 1].setEnabled(true);
            } else {
                btnLocal[nicNr - 1].setOpaque(true);
                btnLocal[nicNr - 1].setBackground(Color.RED);
                btnLocal[nicNr - 1].setEnabled(true);
            }
        }
        btnAddInterface.setEnabled(nicNr != 8);
        btnRemoveInterface.setEnabled(nicNr != 0);
        for (int i = nicNr; i < 8; i++) {
            btnLocal[nicNr].setOpaque(false);
            btnLocal[i].setEnabled(false);
            lblLocal[i].setText("");
        }
    }

    private void updateConnections() {
        cablePanel.removeAll();
        SpringLayout cableLayout = new SpringLayout();
        cablePanel.setLayout(cableLayout);
        int nicNr = 0;
        for (NetzwerkInterface nic : internetSwitch.getNetzwerkInterfaces()) {
            Knoten node = getConnectedComponent(nic);
            if (node != null) {
                LinePanel cable = new LinePanel();
                int yPos = 25 + (int) ((nicNr + 0.5) * LINE_HEIGHT);
                cable.setStartPoint(-2, yPos);
                cable.setEndPoint(282, yPos);
                cableLayout.putConstraint(SpringLayout.WEST, cable, 0, SpringLayout.WEST, cablePanel);
                cableLayout.putConstraint(SpringLayout.NORTH, cable, 0, SpringLayout.NORTH, cablePanel);
                cable.setPreferredSize(new Dimension(280, 700));
                cablePanel.add(cable);
            }
            nicNr++;
        }
        cablePanel.repaint();
    }

    private void initComponents() {
        JPanel upperCompound, noteCompound, buttonCompound;

        // - create assignment area
        upperCompound = new JPanel();
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
        noteCompound = new JPanel();
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
        buttonCompound = new JPanel();
        // buttonCompound.setBackground(Color.RED);
        buttonCompound.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JButton btnClose = new JButton(messages.getString("jvermittlungsrechnerkonfiguration_msg20"));
        btnClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                Container c = (Container) arg0.getSource();
                do {
                    c = c.getParent();
                } while (!(c instanceof JDialog));
                c.setVisible(false);
                GUIContainer.getGUIContainer().getProperty().reInit();
                GUIContainer.getGUIContainer().getProperty().maximieren();
            }
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
        layoutRemote.putConstraint(SpringLayout.HORIZONTAL_CENTER, lblRemoteTitle, 0, SpringLayout.HORIZONTAL_CENTER,
                remoteInterfacesPanel);

        layoutRemote.putConstraint(SpringLayout.NORTH, btnRemote[0], 25, SpringLayout.NORTH, remoteInterfacesPanel);
        layoutRemote.putConstraint(SpringLayout.EAST, btnRemote[0], 0, SpringLayout.EAST, remoteInterfacesPanel);
        layoutRemote.putConstraint(SpringLayout.NORTH, btnRemote[1], 25 + 1 * LINE_HEIGHT, SpringLayout.NORTH,
                remoteInterfacesPanel);
        layoutRemote.putConstraint(SpringLayout.EAST, btnRemote[1], 0, SpringLayout.EAST, remoteInterfacesPanel);
        layoutRemote.putConstraint(SpringLayout.NORTH, btnRemote[2], 25 + 2 * LINE_HEIGHT, SpringLayout.NORTH,
                remoteInterfacesPanel);
        layoutRemote.putConstraint(SpringLayout.EAST, btnRemote[2], 0, SpringLayout.EAST, remoteInterfacesPanel);
        layoutRemote.putConstraint(SpringLayout.NORTH, btnRemote[3], 25 + 3 * LINE_HEIGHT, SpringLayout.NORTH,
                remoteInterfacesPanel);
        layoutRemote.putConstraint(SpringLayout.EAST, btnRemote[3], 0, SpringLayout.EAST, remoteInterfacesPanel);
        layoutRemote.putConstraint(SpringLayout.NORTH, btnRemote[4], 25 + 4 * LINE_HEIGHT, SpringLayout.NORTH,
                remoteInterfacesPanel);
        layoutRemote.putConstraint(SpringLayout.EAST, btnRemote[4], 0, SpringLayout.EAST, remoteInterfacesPanel);
        layoutRemote.putConstraint(SpringLayout.NORTH, btnRemote[5], 25 + 5 * LINE_HEIGHT, SpringLayout.NORTH,
                remoteInterfacesPanel);
        layoutRemote.putConstraint(SpringLayout.EAST, btnRemote[5], 0, SpringLayout.EAST, remoteInterfacesPanel);
        layoutRemote.putConstraint(SpringLayout.NORTH, btnRemote[6], 25 + 6 * LINE_HEIGHT, SpringLayout.NORTH,
                remoteInterfacesPanel);
        layoutRemote.putConstraint(SpringLayout.EAST, btnRemote[6], 0, SpringLayout.EAST, remoteInterfacesPanel);
        layoutRemote.putConstraint(SpringLayout.NORTH, btnRemote[7], 25 + 7 * LINE_HEIGHT, SpringLayout.NORTH,
                remoteInterfacesPanel);
        layoutRemote.putConstraint(SpringLayout.EAST, btnRemote[7], 0, SpringLayout.EAST, remoteInterfacesPanel);

        layoutRemote.putConstraint(SpringLayout.VERTICAL_CENTER, lblRemote[0], 0, SpringLayout.VERTICAL_CENTER,
                btnRemote[0]);
        layoutRemote.putConstraint(SpringLayout.EAST, lblRemote[0], -10, SpringLayout.WEST, btnRemote[0]);
        layoutRemote.putConstraint(SpringLayout.VERTICAL_CENTER, lblRemote[1], 0, SpringLayout.VERTICAL_CENTER,
                btnRemote[1]);
        layoutRemote.putConstraint(SpringLayout.EAST, lblRemote[1], -10, SpringLayout.WEST, btnRemote[1]);
        layoutRemote.putConstraint(SpringLayout.VERTICAL_CENTER, lblRemote[2], 0, SpringLayout.VERTICAL_CENTER,
                btnRemote[2]);
        layoutRemote.putConstraint(SpringLayout.EAST, lblRemote[2], -10, SpringLayout.WEST, btnRemote[2]);
        layoutRemote.putConstraint(SpringLayout.VERTICAL_CENTER, lblRemote[3], 0, SpringLayout.VERTICAL_CENTER,
                btnRemote[3]);
        layoutRemote.putConstraint(SpringLayout.EAST, lblRemote[3], -10, SpringLayout.WEST, btnRemote[3]);
        layoutRemote.putConstraint(SpringLayout.VERTICAL_CENTER, lblRemote[4], 0, SpringLayout.VERTICAL_CENTER,
                btnRemote[4]);
        layoutRemote.putConstraint(SpringLayout.EAST, lblRemote[4], -10, SpringLayout.WEST, btnRemote[4]);
        layoutRemote.putConstraint(SpringLayout.VERTICAL_CENTER, lblRemote[5], 0, SpringLayout.VERTICAL_CENTER,
                btnRemote[5]);
        layoutRemote.putConstraint(SpringLayout.EAST, lblRemote[5], -10, SpringLayout.WEST, btnRemote[5]);
        layoutRemote.putConstraint(SpringLayout.VERTICAL_CENTER, lblRemote[6], 0, SpringLayout.VERTICAL_CENTER,
                btnRemote[6]);
        layoutRemote.putConstraint(SpringLayout.EAST, lblRemote[6], -10, SpringLayout.WEST, btnRemote[6]);
        layoutRemote.putConstraint(SpringLayout.VERTICAL_CENTER, lblRemote[7], 0, SpringLayout.VERTICAL_CENTER,
                btnRemote[7]);
        layoutRemote.putConstraint(SpringLayout.EAST, lblRemote[7], -10, SpringLayout.WEST, btnRemote[7]);
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
                        Port port1 = internetSwitch.getNetzwerkInterfaces().get(markedIdx).getPort();
                        Port port2 = internetSwitch.getNetzwerkInterfaces().get(currentIdx).getPort();
                        JConnectionsDialog.this.swapConnection(port1, port2);
                        updateAll();
                    }
                }
            });
            localInterfacesPanel.add(btnLocal[i]);

            lblLocal[i] = new JLabel();
            localInterfacesPanel.add(lblLocal[i]);
        }

        // btnAddInterface = new JButton(messages.getString("jvermittlungsrechnerkonfiguration_msg24"));
        btnAddInterface = new JButton("+");
        btnRemoveInterface = new JButton("-");
        btnAddInterface.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                internetSwitch.hinzuAnschluss();
                updateAll();
            }
        });
        btnRemoveInterface.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<NetzwerkInterface> nics = internetSwitch.getNetzwerkInterfaces();
                if (!nics.isEmpty()) {
                    NetzwerkInterface nic = nics.get(nics.size() - 1);
                    removeConnection(nic.getPort());
                    internetSwitch.removeNic(nic);
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
        layoutLocal.putConstraint(SpringLayout.HORIZONTAL_CENTER, lblLocalTitle, 0, SpringLayout.HORIZONTAL_CENTER,
                localInterfacesPanel);

        layoutLocal.putConstraint(SpringLayout.NORTH, btnLocal[0], 25, SpringLayout.NORTH, localInterfacesPanel);
        layoutLocal.putConstraint(SpringLayout.WEST, btnLocal[0], 0, SpringLayout.WEST, localInterfacesPanel);
        layoutLocal.putConstraint(SpringLayout.NORTH, btnLocal[1], 25 + 1 * LINE_HEIGHT, SpringLayout.NORTH,
                localInterfacesPanel);
        layoutLocal.putConstraint(SpringLayout.WEST, btnLocal[1], 0, SpringLayout.WEST, localInterfacesPanel);
        layoutLocal.putConstraint(SpringLayout.NORTH, btnLocal[2], 25 + 2 * LINE_HEIGHT, SpringLayout.NORTH,
                localInterfacesPanel);
        layoutLocal.putConstraint(SpringLayout.WEST, btnLocal[2], 0, SpringLayout.WEST, localInterfacesPanel);
        layoutLocal.putConstraint(SpringLayout.NORTH, btnLocal[3], 25 + 3 * LINE_HEIGHT, SpringLayout.NORTH,
                localInterfacesPanel);
        layoutLocal.putConstraint(SpringLayout.WEST, btnLocal[3], 0, SpringLayout.WEST, localInterfacesPanel);
        layoutLocal.putConstraint(SpringLayout.NORTH, btnLocal[4], 25 + 4 * LINE_HEIGHT, SpringLayout.NORTH,
                localInterfacesPanel);
        layoutLocal.putConstraint(SpringLayout.WEST, btnLocal[4], 0, SpringLayout.WEST, localInterfacesPanel);
        layoutLocal.putConstraint(SpringLayout.NORTH, btnLocal[5], 25 + 5 * LINE_HEIGHT, SpringLayout.NORTH,
                localInterfacesPanel);
        layoutLocal.putConstraint(SpringLayout.WEST, btnLocal[5], 0, SpringLayout.WEST, localInterfacesPanel);
        layoutLocal.putConstraint(SpringLayout.NORTH, btnLocal[6], 25 + 6 * LINE_HEIGHT, SpringLayout.NORTH,
                localInterfacesPanel);
        layoutLocal.putConstraint(SpringLayout.WEST, btnLocal[6], 0, SpringLayout.WEST, localInterfacesPanel);
        layoutLocal.putConstraint(SpringLayout.NORTH, btnLocal[7], 25 + 7 * LINE_HEIGHT, SpringLayout.NORTH,
                localInterfacesPanel);
        layoutLocal.putConstraint(SpringLayout.WEST, btnLocal[7], 0, SpringLayout.WEST, localInterfacesPanel);

        layoutLocal.putConstraint(SpringLayout.VERTICAL_CENTER, lblLocal[0], 0, SpringLayout.VERTICAL_CENTER,
                btnLocal[0]);
        layoutLocal.putConstraint(SpringLayout.WEST, lblLocal[0], 10, SpringLayout.EAST, btnLocal[0]);
        layoutLocal.putConstraint(SpringLayout.VERTICAL_CENTER, lblLocal[1], 0, SpringLayout.VERTICAL_CENTER,
                btnLocal[1]);
        layoutLocal.putConstraint(SpringLayout.WEST, lblLocal[1], 10, SpringLayout.EAST, btnLocal[1]);
        layoutLocal.putConstraint(SpringLayout.VERTICAL_CENTER, lblLocal[2], 0, SpringLayout.VERTICAL_CENTER,
                btnLocal[2]);
        layoutLocal.putConstraint(SpringLayout.WEST, lblLocal[2], 10, SpringLayout.EAST, btnLocal[2]);
        layoutLocal.putConstraint(SpringLayout.VERTICAL_CENTER, lblLocal[3], 0, SpringLayout.VERTICAL_CENTER,
                btnLocal[3]);
        layoutLocal.putConstraint(SpringLayout.WEST, lblLocal[3], 10, SpringLayout.EAST, btnLocal[3]);
        layoutLocal.putConstraint(SpringLayout.VERTICAL_CENTER, lblLocal[4], 0, SpringLayout.VERTICAL_CENTER,
                btnLocal[4]);
        layoutLocal.putConstraint(SpringLayout.WEST, lblLocal[4], 10, SpringLayout.EAST, btnLocal[4]);
        layoutLocal.putConstraint(SpringLayout.VERTICAL_CENTER, lblLocal[5], 0, SpringLayout.VERTICAL_CENTER,
                btnLocal[5]);
        layoutLocal.putConstraint(SpringLayout.WEST, lblLocal[5], 10, SpringLayout.EAST, btnLocal[5]);
        layoutLocal.putConstraint(SpringLayout.VERTICAL_CENTER, lblLocal[6], 0, SpringLayout.VERTICAL_CENTER,
                btnLocal[6]);
        layoutLocal.putConstraint(SpringLayout.WEST, lblLocal[6], 10, SpringLayout.EAST, btnLocal[6]);
        layoutLocal.putConstraint(SpringLayout.VERTICAL_CENTER, lblLocal[7], 0, SpringLayout.VERTICAL_CENTER,
                btnLocal[7]);
        layoutLocal.putConstraint(SpringLayout.WEST, lblLocal[7], 10, SpringLayout.EAST, btnLocal[7]);

        layoutLocal.putConstraint(SpringLayout.NORTH, buttonPanel, 0, SpringLayout.SOUTH, btnLocal[7]);
        layoutLocal.putConstraint(SpringLayout.HORIZONTAL_CENTER, buttonPanel, 0, SpringLayout.HORIZONTAL_CENTER,
                localInterfacesPanel);

        localInterfacesPanel.setLayout(layoutLocal);
    }

    private void updateAll() {
        updateNicToNodeMap(internetSwitch);
        updateRemoteInterfaces();
        updateLocalInterfaces();
        updateConnections();
    }

    private Knoten getConnectedComponent(NetzwerkInterface nic) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass()
                + " (JVermittlungsrechnerKonfiguration), holeVerbundeneKomponente(" + nic + ")");

        if (nic.getPort().getVerbindung() == null) {
            return null;
        }

        Port lokalerAnschluss, entfernterAnschluss;
        lokalerAnschluss = nic.getPort();
        Port[] ports = lokalerAnschluss.getVerbindung().getAnschluesse();
        if (ports[0] == lokalerAnschluss) {
            entfernterAnschluss = ports[1];
        } else {
            entfernterAnschluss = ports[0];
        }

        for (GUIKnotenItem knotenItem : GUIContainer.getGUIContainer().getKnotenItems()) {
            Knoten knoten = knotenItem.getKnoten();
            if (knoten instanceof LokalerKnoten) {
                for (Port port : ((LokalerKnoten) knoten).getAnschluesse()) {
                    if (port.equals(entfernterAnschluss)) {
                        return knoten;
                    }
                }
            } else if (knoten instanceof InternetKnoten) {
                for (NetzwerkInterface networkInterface : ((InternetKnoten) knoten).getNetzwerkInterfaces()) {
                    if (networkInterface.getPort().equals(entfernterAnschluss)) {
                        return knoten;
                    }
                }
            }
        }

        return null;
    }

    private Kabel getConnectedCable(NetzwerkInterface nic) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass()
                + " (JVermittlungsrechnerKonfiguration), getConnectedCable(" + nic + ")");
        Verbindung nicConn = nic.getPort().getVerbindung();
        if (nicConn == null) {
            return null;
        }

        for (GUIKabelItem cable : GUIContainer.getGUIContainer().getCableItems()) {
            if (nicConn.equals(cable.getDasKabel())) {
                return (Kabel) cable.getDasKabel();
            }
        }
        return null;
    }

    private void removeConnection(Port port) {
        for (GUIKabelItem cableItem : GUIContainer.getGUIContainer().getCableItems()) {
            if (cableItem.getDasKabel().getAnschluesse()[0].equals(port)
                    || cableItem.getDasKabel().getAnschluesse()[1].equals(port)) {
                cableItem.getDasKabel().anschluesseTrennen();
                GUIContainer.getGUIContainer().removeCableItem(cableItem);
                GUIContainer.getGUIContainer().updateViewport();
                break;
            }
        }
    }

    private void swapConnection(Port port1, Port port2) {
        Kabel kabel1 = null;
        int portIdx1 = 0;
        Kabel kabel2 = null;
        int portIdx2 = 0;
        for (GUIKabelItem cableItem : GUIContainer.getGUIContainer().getCableItems()) {
            Kabel cable = cableItem.getDasKabel();
            if (cable.getAnschluesse()[0].equals(port1)) {
                kabel1 = cable;
                portIdx1 = 0;
            } else if (cable.getAnschluesse()[1].equals(port1)) {
                kabel1 = cable;
                portIdx1 = 1;
            } else if (cable.getAnschluesse()[0].equals(port2)) {
                kabel2 = cable;
                portIdx2 = 0;
            } else if (cable.getAnschluesse()[1].equals(port2)) {
                kabel2 = cable;
                portIdx2 = 1;
            }
        }
        if (kabel1 != null && kabel2 != null && kabel1 != kabel2) {
            kabel1.anschluesseTrennen();
            kabel2.anschluesseTrennen();

            Port[] anschluesse = kabel1.getAnschluesse();
            port2.setVerbindung(kabel1);
            anschluesse[portIdx1] = port2;
            kabel1.setAnschluesse(anschluesse);

            anschluesse = kabel2.getAnschluesse();
            port1.setVerbindung(kabel2);
            anschluesse[portIdx2] = port1;
            kabel2.setAnschluesse(anschluesse);
        } else if (kabel1 == null && kabel2 != null) {
            kabel2.anschluesseTrennen();

            port1.setVerbindung(kabel2);
            Port[] anschluesse = kabel2.getAnschluesse();
            anschluesse[portIdx2] = port1;
            kabel2.setAnschluesse(anschluesse);
        } else if (kabel1 != null && kabel2 == null) {
            kabel1.anschluesseTrennen();

            Port[] anschluesse = kabel1.getAnschluesse();
            port2.setVerbindung(kabel1);
            anschluesse[portIdx1] = port2;
            kabel1.setAnschluesse(anschluesse);
        }
    }
}
