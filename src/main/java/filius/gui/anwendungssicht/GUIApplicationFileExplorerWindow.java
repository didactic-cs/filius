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
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.Observable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListModel;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import filius.gui.JMainFrame;
import filius.software.lokal.FileExplorer;
import filius.software.system.Datei;
import filius.software.system.Dateisystem;

public class GUIApplicationFileExplorerWindow extends GUIApplicationWindow {
    private enum OpType {
        CUT_AND_PASTE, COPY_AND_PASTE
    }

    private static Logger LOG = LoggerFactory.getLogger(GUIApplicationFileExplorerWindow.class);

    private static final long serialVersionUID = 1L;

    private final ImageIcon dateiIcon = new ImageIcon(getClass().getResource("/gfx/desktop/datei.png"));
    private final ImageIcon ordnerIcon = new ImageIcon(getClass().getResource("/gfx/desktop/ordner.png"));
    private JPanel backPanel;
    private JTree tv;
    private DefaultMutableTreeNode aktuellerOrdner;
    private JList<String> dateiListe;
    private DefaultMutableTreeNode selektierteNode;
    private DefaultMutableTreeNode zwischenAblageNode;
    private OpType lastSelectedPasteOperation;
    private JButton btImportieren;
    private String datei;
    private String pfad;

    public GUIApplicationFileExplorerWindow(final GUIDesktopPanel desktop, String appName) {
        super(desktop, appName);
        aktuellerOrdner = holeAnwendung().getSystemSoftware().getDateisystem().getRoot();

        initialisiereKomponenten();
    }

    private void initialisiereKomponenten() {
        backPanel = new JPanel(new BorderLayout());

        tv = new JTree(aktuellerOrdner);

        tv.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tv.getLastSelectedPathComponent();

                if (node != null) {
                    aktuellerOrdner = node;
                    ordnerInhaltAnzeigen(node);
                }
            }
        });

        tv.setBounds(0, 0, 150, 100);
        tv.setCellRenderer(new GUITreeRenderer(dateiIcon, ordnerIcon));
        Box buttonBox = Box.createHorizontalBox();
        buttonBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        final JButton aktualisieren = new JButton(messages.getString("fileexplorer_msg1"));
        aktualisieren.setActionCommand("aktualisieren");
        aktualisieren.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                aktualisieren();
            }
        });
        buttonBox.add(aktualisieren);
        buttonBox.add(Box.createHorizontalStrut(5));

        btImportieren = new JButton(messages.getString("fileexplorer_msg2"));
        btImportieren.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileImport();
            }
        });
        buttonBox.add(btImportieren);

        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        box.add(Box.createVerticalStrut(5));
        box.add(buttonBox);

        JScrollPane scrollpane = new JScrollPane(tv);
        scrollpane.setPreferredSize(new Dimension(10, 350));
        Box horBox = Box.createHorizontalBox();
        horBox.add(scrollpane);
        horBox.setPreferredSize(new Dimension(180, 350));
        ListModel<String> lmDateiListe = new DefaultListModel<>();
        dateiListe = new JList<String>(lmDateiListe);
        dateiListe.setFixedCellHeight(16);
        JScrollPane dateiListenScrollPane = new JScrollPane(dateiListe);
        dateiListenScrollPane.setPreferredSize(new Dimension(10, 240));
        horBox.add(dateiListenScrollPane);

        dateiListe.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == 3) {
                    if (aktuellerOrdner != null) {
                        int index = ((JList<?>) e.getSource()).locationToIndex(e.getPoint());

                        ListModel<String> lm = dateiListe.getModel();
                        int selektiert = selektierteZelle(index, e.getPoint());
                        JPopupMenu popmen = new JPopupMenu();
                        final JMenuItem miNeuerOrdner = new JMenuItem(messages.getString("fileexplorer_msg3"));
                        miNeuerOrdner.setActionCommand("neuerordner");
                        final JMenuItem miLoeschen = new JMenuItem(messages.getString("fileexplorer_msg4"));
                        miLoeschen.setActionCommand("loeschen");
                        final JMenuItem miAusschneiden = new JMenuItem(messages.getString("fileexplorer_msg5"));
                        miAusschneiden.setActionCommand("ausschneiden");
                        final JMenuItem miKopieren = new JMenuItem(messages.getString("fileexplorer_msg6"));
                        miKopieren.setActionCommand("kopieren");
                        final JMenuItem miEinfuegen = new JMenuItem(messages.getString("fileexplorer_msg7"));
                        miEinfuegen.setActionCommand("einfuegen");
                        final JMenuItem miUmbenennen = new JMenuItem(messages.getString("fileexplorer_msg8"));
                        miUmbenennen.setActionCommand("umbenennen");

                        ActionListener al = new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                /* Neuer Ordner */
                                if (e.getActionCommand().equals(miNeuerOrdner.getActionCommand())) {
                                    String ordnerName = JOptionPane
                                            .showInputDialog(GUIApplicationFileExplorerWindow.this, "");
                                    if (!ordnerName.equals("")) {
                                        holeAnwendung().getSystemSoftware().getDateisystem()
                                                .erstelleVerzeichnis(aktuellerOrdner, ordnerName);
                                        aktualisieren();
                                    }
                                }
                                /* Loeschen */
                                if (e.getActionCommand().equals(miLoeschen.getActionCommand())) {
                                    int loeschAbfrage = JOptionPane.showConfirmDialog(
                                            GUIApplicationFileExplorerWindow.this,
                                            messages.getString("fileexplorer_msg18"),
                                            messages.getString("fileexplorer_msg18"), JOptionPane.YES_NO_OPTION);

                                    if (loeschAbfrage == JOptionPane.YES_OPTION) {
                                        aktuellerOrdner.remove(selektierteNode);
                                        aktualisieren();
                                    }
                                }
                                /* Ausschneiden */
                                if (e.getActionCommand().equals(miAusschneiden.getActionCommand())) {
                                    zwischenAblageNode = selektierteNode;
                                    lastSelectedPasteOperation = OpType.CUT_AND_PASTE;
                                    aktualisieren();
                                }
                                /* Kopieren */
                                if (e.getActionCommand().equals(miKopieren.getActionCommand())) {
                                    zwischenAblageNode = selektierteNode;
                                    lastSelectedPasteOperation = OpType.COPY_AND_PASTE;
                                    aktualisieren();
                                }
                                /* Einfuegen */
                                if (e.getActionCommand().equals(miEinfuegen.getActionCommand())) {
                                    if (zwischenAblageNode.isNodeDescendant(aktuellerOrdner)) {
                                        JOptionPane.showMessageDialog(GUIApplicationFileExplorerWindow.this,
                                                messages.getString("fileexplorer_msg19"), "",
                                                JOptionPane.ERROR_MESSAGE);
                                    } else {
                                        try {
                                            aktuellerOrdner.add(tiefesKopieren(zwischenAblageNode));
                                            if (lastSelectedPasteOperation == OpType.CUT_AND_PASTE) {
                                                ((DefaultMutableTreeNode) zwischenAblageNode.getParent())
                                                        .remove(zwischenAblageNode);
                                                zwischenAblageNode = null;
                                            }
                                        } catch (ClassNotFoundException | IOException e1) {
                                            LOG.debug("Error inserting file/dir", e);
                                        }
                                        aktualisieren();
                                    }
                                }
                                /* Umbenennen */
                                if (e.getActionCommand().equals(miUmbenennen.getActionCommand())) {
                                    String neuerName = JOptionPane.showInputDialog(
                                            GUIApplicationFileExplorerWindow.this,
                                            messages.getString("fileexplorer_msg9"));
                                    if (neuerName != null && !neuerName.trim().isEmpty()) {
                                        if (!holeAnwendung().getSystemSoftware().getDateisystem()
                                                .dateiVorhanden(aktuellerOrdner, neuerName)) {
                                            if (selektierteNode.getUserObject().getClass().equals(Datei.class)) {
                                                /* Datei umbenennen */
                                                Datei dat = (Datei) selektierteNode.getUserObject();
                                                dat.setName(neuerName);
                                            } else {
                                                /* Ordner umbenennen */
                                                selektierteNode.setUserObject(neuerName);
                                            }
                                            aktualisieren();
                                        }
                                    }
                                }
                            }
                        };
                        miNeuerOrdner.addActionListener(al);
                        miLoeschen.addActionListener(al);
                        miAusschneiden.addActionListener(al);
                        miKopieren.addActionListener(al);
                        miEinfuegen.addActionListener(al);
                        miUmbenennen.addActionListener(al);

                        if (selektiert == -1) {
                            popmen.add(miNeuerOrdner);
                            if (zwischenAblageNode != null) {
                                popmen.add(miEinfuegen);
                            }
                        } else {
                            String[] teile = lm.getElementAt(index).toString().split(";");
                            if (teile.length > 0) {
                                selektierteNode = Dateisystem.verzeichnisKnoten(aktuellerOrdner, teile[1]);
                                dateiListe.setSelectedIndex(index);
                            }
                            popmen.add(miLoeschen);
                            popmen.add(miAusschneiden);
                            popmen.add(miKopieren);
                            popmen.add(miUmbenennen);
                        }

                        GUIApplicationFileExplorerWindow.this.add(popmen);
                        popmen.show(GUIApplicationFileExplorerWindow.this.dateiListe, e.getX(), e.getY());
                    }

                }
            }
        });

        box.add(horBox);
        backPanel.add(box, BorderLayout.CENTER);
        ordnerInhaltAnzeigen(aktuellerOrdner);
        add(backPanel, BorderLayout.CENTER);
    }

    /**
     * Fuegt den Inhalt einer DefaultMutableTreeNode in ListModel der dateiListe ein. Um im CellRenderer zwischen
     * Dateien und Ordnern unterscheiden zu koennen, wird der Typ (Datei/Ordner) gefolgt von einem Semicolon angegeben.
     * 
     * @param node
     *            Die DefaultMutableTreeNode deren Inhalt angezeigt werden soll.
     */
    public void ordnerInhaltAnzeigen(DefaultMutableTreeNode node) {
        DefaultListModel<String> lm = (DefaultListModel<String>) dateiListe.getModel();
        lm.clear();
        dateiListe.setCellRenderer(new OrdnerInhaltListRenderer());
        for (Enumeration<TreeNode> e = node.children(); e.hasMoreElements();) {
            DefaultMutableTreeNode enode = (DefaultMutableTreeNode) e.nextElement();
            if (enode.getUserObject().getClass().equals(Datei.class)) {
                Datei dat = (Datei) enode.getUserObject();
                lm.addElement(messages.getString("fileexplorer_msg10") + ";" + dat.getName());
            } else {
                lm.addElement(messages.getString("fileexplorer_msg11") + ";" + enode.toString());
            }
        }
    }

    /**
     * Ueberprueft ob eine (per locationToIndex) ermittelte Zelle wirklich geklickt wurde Das ist noetig, weil im
     * leeren, unteren Teil der JList automatisch der unterste Index zurueckgegeben wird.
     */
    public int selektierteZelle(int index, Point punkt) {
        int ergebnis = -1;
        if (dateiListe.indexToLocation(index) != null) {
            if (dateiListe.indexToLocation(index).getY() + dateiListe.getFixedCellHeight() > punkt.getY()) {
                ergebnis = index;
            }
        }
        return ergebnis;
    }

    public void aktualisieren() {
        tv.updateUI();
        ordnerInhaltAnzeigen(aktuellerOrdner);
    }

    /**
     * Da bei clone() nur das Objekt und nicht seine Referenzen kopiert werden, wird fuer DefaultMutableTreeNode Tiefes
     * Kopieren gebraucht, um z.B. bei einem Ordner die komplette eingeschlossene Struktur zu erhalten
     */
    public DefaultMutableTreeNode tiefesKopieren(DefaultMutableTreeNode original)
            throws IOException, ClassNotFoundException {
        DefaultMutableTreeNode ergebnis = null;

        // ObjectOutputStream erzeugen
        ByteArrayOutputStream bufOutStream = new ByteArrayOutputStream();
        ObjectOutputStream outStream = new ObjectOutputStream(bufOutStream);

        // Objekt im byte-Array speichern
        outStream.writeObject(original);
        outStream.close();

        // Pufferinhalt abrufen
        byte[] buffer = bufOutStream.toByteArray();
        // ObjectInputStream erzeugen
        ByteArrayInputStream bufInStream = new ByteArrayInputStream(buffer);
        ObjectInputStream inStream = new ObjectInputStream(bufInStream);
        // Objekt wieder auslesen
        ergebnis = (DefaultMutableTreeNode) inStream.readObject();

        return ergebnis;
    }

    public void fileImport() {
        backPanel = new JPanel(new BorderLayout());

        final JTextArea outputField = new JTextArea("");
        outputField.setEditable(false);
        outputField.setBackground(backPanel.getBackground());
        outputField.setFont(outputField.getFont().deriveFont(Font.BOLD));
        outputField.setBorder(null);
        outputField.setPreferredSize(new Dimension(500, 30));

        JLabel fileLabel = new JLabel(messages.getString("fileexplorer_msg13"));
        fileLabel.setPreferredSize(new Dimension(100, 30));

        final JTextField inputField = new JTextField("");
        inputField.setPreferredSize(new Dimension(200, 30));
        inputField.setEditable(false);

        final JTextField renameField = new JTextField("");
        renameField.setPreferredSize(new Dimension(200, 30));

        JLabel renameLabel = new JLabel(messages.getString("fileexplorer_msg9"));
        renameLabel.setPreferredSize(new Dimension(100, 30));

        JButton fileButton = new JButton(messages.getString("fileexplorer_msg14"));
        fileButton.setPreferredSize(new Dimension(150, 30));
        fileButton.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                FileDialog fileDialog = new FileDialog(JMainFrame.getJMainFrame(), messages.getString("main_msg3"),
                        FileDialog.LOAD);
                fileDialog.setVisible(true);
                if (null != fileDialog.getFile()) {
                    datei = fileDialog.getFile();
                    pfad = fileDialog.getDirectory();
                    if (!pfad.endsWith(System.getProperty("file.separator")))
                        pfad += System.getProperty("file.separator");
                }

                if (datei != null) {
                    inputField.setText(pfad + datei);
                    renameField.setText(datei);
                }
            }
        });

        JButton importButton = new JButton(messages.getString("fileexplorer_msg15"));
        importButton.setPreferredSize(new Dimension(150, 30));
        importButton.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent z) {
                if (inputField.getText().equals("") || renameField.getText().equals("")) {
                    outputField.setText(messages.getString("fileexplorer_msg16"));
                } else {

                    if (aktuellerOrdner == null) {
                        outputField.setText(messages.getString("fileexplorer_msg17"));
                    } else {
                        outputField.setText(((FileExplorer) holeAnwendung()).addFile(pfad, datei, aktuellerOrdner,
                                renameField.getText()));
                        aktualisieren();
                    }
                }
            }
        });

        JButton closeButton = new JButton(messages.getString("sw_fileimporter_msg3"));
        closeButton.setPreferredSize(new Dimension(150, 30));
        closeButton.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent z) {
                GUIApplicationFileExplorerWindow.this.desktop.closeModularWindow();
            }
        });

        Box fileSelectionBox = Box.createHorizontalBox();
        fileSelectionBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        fileSelectionBox.add(fileLabel);
        fileSelectionBox.add(Box.createHorizontalStrut(5));
        fileSelectionBox.add(inputField);
        fileSelectionBox.add(Box.createHorizontalStrut(5));
        fileSelectionBox.add(fileButton);

        Box fileImportBox = Box.createHorizontalBox();
        fileImportBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        fileImportBox.add(renameLabel);
        fileImportBox.add(Box.createHorizontalStrut(5));
        fileImportBox.add(renameField);
        fileImportBox.add(Box.createHorizontalStrut(5));
        fileImportBox.add(importButton);

        Box importResultBox = Box.createHorizontalBox();
        importResultBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        importResultBox.add(outputField);

        Box upperBox = Box.createVerticalBox();
        upperBox.add(fileSelectionBox);
        upperBox.add(fileImportBox);
        upperBox.add(importResultBox);

        backPanel.add(upperBox, BorderLayout.NORTH);

        Box lowerBox = Box.createVerticalBox();
        lowerBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        lowerBox.add(Box.createVerticalStrut(5));
        lowerBox.add(closeButton);
        lowerBox.add(Box.createVerticalStrut(5));

        backPanel.add(lowerBox, BorderLayout.SOUTH);

        desktop.showModularWindow(messages.getString("fileexplorer_msg12"), backPanel);
    }

    @Override
    public void update(Observable arg0, Object arg1) {
        aktualisieren();
    }
}
