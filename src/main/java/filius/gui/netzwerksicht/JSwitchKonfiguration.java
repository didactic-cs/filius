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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import filius.gui.GUIContainer;
import filius.gui.GUIEvents;
import filius.hardware.Hardware;
import filius.hardware.knoten.Switch;
import filius.rahmenprogramm.EingabenUeberpruefung;
import filius.rahmenprogramm.I18n;
import filius.software.system.SwitchFirmware;

public class JSwitchKonfiguration extends JKonfiguration implements I18n {
    private static Logger LOG = LoggerFactory.getLogger(JSwitchKonfiguration.class);

    private static final long serialVersionUID = 1L;
    private JTextField name; // Name,Name,20,String,editable,Neuer
    private JTextField ssid;
    private JTextField retentionTime;
    private JCheckBox checkCloud;

    protected JSwitchKonfiguration(Hardware hardware) {
        super(hardware);
    }

    public void aenderungenAnnehmen() {
        Switch switchWAP = (Switch) holeHardware();
        switchWAP.setName(name.getText());

        SwitchFirmware switchFirmware = (SwitchFirmware) switchWAP.getSystemSoftware();
        String ssidBeforeChange = switchFirmware.getSSID();
        if (checkSSID()) {
            switchFirmware.setSSID(ssid.getText());
        }
        if (!StringUtils.equals(ssidBeforeChange, switchFirmware.getSSID())) {
            for (GUIKabelItem cable : GUIContainer.getGUIContainer().getCableItems()) {
                if (cable.getDasKabel().getWireless() && (switchWAP.equals(cable.getKabelpanel().getZiel1().getKnoten())
                        || switchWAP.equals(cable.getKabelpanel().getZiel2().getKnoten()))) {
                    GUIEvents.getGUIEvents().removeConnection(cable);
                }
            }
        }
        try {
            ((SwitchFirmware) ((Switch) holeHardware()).getSystemSoftware())
                    .setRetentionTime(Long.parseLong(retentionTime.getText()) * 1000);
        } catch (NumberFormatException e) {}

        GUIContainer.getGUIContainer().updateViewport();
        updateAttribute();
    }

    public void changeAppearance() {
        LOG.debug("DEBUG: changeAppearance invoked for Switch");
        int iconTheme = GUIContainer.getGUIContainer().getMenu().iconTheme;
        if (checkCloud.isSelected()) {
            GUIContainer.getGUIContainer().getLabelforKnoten(((Switch) holeHardware()))
                    .setIcon(new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.SWITCH_CLOUD)));
            ((Switch) holeHardware()).setCloud(true);
            GUIContainer.getGUIContainer().updateViewport();
        } else {
            GUIContainer.getGUIContainer().getLabelforKnoten(((Switch) holeHardware()))
                    .setIcon(new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.SWITCH[iconTheme])));
            ((Switch) holeHardware()).setCloud(false);
            GUIContainer.getGUIContainer().updateViewport();
        }
    }

    @Override
    protected void initContents() {
        JLabel tempLabel;
        Box tempBox;
        Box tempBox2;
        FocusListener focusListener;
        ActionListener actionListener;
        ItemListener itemListener;

        actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                aenderungenAnnehmen();
            }
        };
        itemListener = new ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                changeAppearance();
            }
        };
        focusListener = new FocusListener() {

            public void focusGained(FocusEvent arg0) {}

            public void focusLost(FocusEvent arg0) {
                aenderungenAnnehmen();
            }

        };
        tempBox2 = Box.createVerticalBox();

        // switch name
        tempLabel = new JLabel(messages.getString("jswitchkonfiguration_msg1"));
        tempLabel.setPreferredSize(new Dimension(140, 10));
        tempLabel.setVisible(true);
        tempLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        name = new JTextField(messages.getString("jswitchkonfiguration_msg2"));
        name.addActionListener(actionListener);
        name.addFocusListener(focusListener);

        tempBox = Box.createHorizontalBox();
        tempBox.setOpaque(false);
        tempBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        tempBox.setMaximumSize(new Dimension(400, 40));
        tempBox.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        tempBox.add(tempLabel);
        tempBox.add(Box.createHorizontalStrut(5)); // Platz zw. tempLabel und
        tempBox.add(name);

        tempBox2.add(tempBox);
        tempBox2.add(Box.createVerticalStrut(10));

        // ssid
        tempLabel = new JLabel(messages.getString("jswitchkonfiguration_msg4"));
        tempLabel.setPreferredSize(new Dimension(140, 10));
        tempLabel.setVisible(true);
        tempLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        ssid = new JTextField();
        ssid.addActionListener(actionListener);
        ssid.addFocusListener(focusListener);
        ssid.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                checkSSID();
            }
        });

        tempBox = Box.createHorizontalBox();
        tempBox.setOpaque(false);
        tempBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        tempBox.setMaximumSize(new Dimension(400, 40));
        tempBox.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        tempBox.add(tempLabel);
        tempBox.add(Box.createHorizontalStrut(5)); // Platz zw. tempLabel und
        tempBox.add(ssid);

        tempBox2.add(tempBox);
        tempBox2.add(Box.createVerticalStrut(10));

        // switch icon
        checkCloud = new JCheckBox(messages.getString("jswitchkonfiguration_msg3"));
        checkCloud.setPreferredSize(new Dimension(160, 10));
        checkCloud.setVisible(true);
        checkCloud.setOpaque(false);
        // checkCloud.setAlignmentX(Component.RIGHT_ALIGNMENT);
        checkCloud.addItemListener(itemListener);

        // retention time
        tempLabel = new JLabel(messages.getString("jswitchkonfiguration_msg5"));
        tempLabel.setPreferredSize(new Dimension(300, 10));
        tempLabel.setVisible(true);
        tempLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        retentionTime = new JTextField();
        retentionTime.setPreferredSize(new Dimension(100, 10));
        retentionTime.addActionListener(actionListener);
        retentionTime.addFocusListener(focusListener);
        retentionTime.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                checkRetentionTime();
            }
        });

        tempBox = Box.createHorizontalBox();
        tempBox.setOpaque(false);
        tempBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        tempBox.setPreferredSize(new Dimension(400, 40));
        tempBox.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        tempBox.add(tempLabel);
        tempBox.add(Box.createHorizontalStrut(5)); // Platz zw. tempLabel und
        tempBox.add(retentionTime);

        rightBox.add(tempBox);
        rightBox.add(Box.createVerticalStrut(190));

        tempBox2.add(checkCloud);
        box.add(tempBox2, BorderLayout.NORTH);
    }

    @Override
    public void updateAttribute() {
        Switch switchWAP = (Switch) holeHardware();
        name.setText(switchWAP.holeAnzeigeName());
        ssid.setText(((SwitchFirmware) switchWAP.getSystemSoftware()).getSSID());
        checkSSID();
        retentionTime.setText(Long
                .toString(((SwitchFirmware) ((Switch) holeHardware()).getSystemSoftware()).getRetentionTime() / 1000));
        checkRetentionTime();
        // eingefügt
        checkCloud.setSelected(switchWAP.isCloud());
    }

    private boolean checkSSID() {
        return ueberpruefen(EingabenUeberpruefung.musterServiceSetIdentifier, ssid);
    }

    private boolean checkRetentionTime() {
        return ueberpruefen(EingabenUeberpruefung.musterNurZahlen, retentionTime);
    }
}
