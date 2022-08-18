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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.tree.DefaultMutableTreeNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import filius.software.system.Betriebssystem;
import filius.software.system.Datei;

/**
 * Applikationsfenster fuer TextEditor
 * 
 * @author Johannes Bade & Thomas Gerding
 * 
 */
public class GUIApplicationTextEditorWindow extends GUIApplicationWindow {
    private static Logger LOG = LoggerFactory.getLogger(GUIApplicationTextEditorWindow.class);

    private static final long serialVersionUID = 1L;
    private JTextArea editorField;
    private JPanel backPanel;
    private Datei aktuelleDatei = null;
    private String original = "";
    private DefaultMutableTreeNode arbeitsVerzeichnis;

    public GUIApplicationTextEditorWindow(GUIDesktopPanel desktop, String appName) {
        super(desktop, appName);

        editorField = new JTextArea("");
        editorField.setEditable(true);
        editorField.setFont(new Font("Courier New", Font.PLAIN, 11));

        this.arbeitsVerzeichnis = holeAnwendung().getSystemSoftware().getDateisystem().getRoot();

        String dateiName = holeParameter()[0];
        if (!dateiName.equals("")) {
            if (this.arbeitsVerzeichnis == null) {
                this.arbeitsVerzeichnis = holeAnwendung().getSystemSoftware().getDateisystem().getRoot();
            }
            Datei datei = holeAnwendung().getSystemSoftware().getDateisystem().holeDatei(arbeitsVerzeichnis, dateiName);
            if (datei != null) {
                aktuelleDatei = datei;
            }
            updateFromFile();
        }

        JScrollPane tpPane = new JScrollPane(editorField);
        tpPane.setBorder(null);

        Box editorBox = Box.createHorizontalBox();

        // editorBox.add(editorField);
        editorBox.add(tpPane);
        editorBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        backPanel = new JPanel(new BorderLayout());
        backPanel.add(editorBox, BorderLayout.CENTER);

        add(backPanel, BorderLayout.CENTER);
        JPanel menubar = new JPanel(new FlowLayout());

        JButton newFile = new JButton(messages.getString("texteditor_msg3"));
        newFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                neu();
            }
        });
        menubar.add(newFile);

        JButton openFile = new JButton(messages.getString("texteditor_msg4"));
        openFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                oeffnen();
            }
        });
        menubar.add(openFile);

        JButton saveFile = new JButton(messages.getString("texteditor_msg5"));
        saveFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                speichern();
            }
        });
        menubar.add(saveFile);

        JButton saveAsFile = new JButton(messages.getString("texteditor_msg6"));
        saveAsFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                speichernUnter();
            }
        });
        menubar.add(saveAsFile);

        backPanel.add(menubar, BorderLayout.NORTH);
    }

    public void speichern() {
        if (aktuelleDatei != null) {
            original = editorField.getText();
            aktuelleDatei.setDateiInhalt(original);
        } else {
            speichernUnter();
        }
    }

    public void speichernUnter() {
        DMTNFileChooser fc = new DMTNFileChooser((Betriebssystem) holeAnwendung().getSystemSoftware());
        int rueckgabe = fc.saveDialog();

        if (rueckgabe == DMTNFileChooser.OK) {
            String dateiNameNeu = fc.getAktuellerDateiname();
            Datei tmpFile = new Datei(dateiNameNeu, messages.getString("texteditor_msg8"), editorField.getText());
            this.holeAnwendung().getSystemSoftware().getDateisystem().speicherDatei(fc.getAktuellerOrdner(), tmpFile);
            changeCurrentFile(tmpFile);
        }
    }

    public void changeCurrentFile(Datei tmpFile) {
        if (aktuelleDatei != null) {
            aktuelleDatei.deleteObserver(this);
        }
        aktuelleDatei = tmpFile;
        updateFromFile();
        if (aktuelleDatei != null) {
            aktuelleDatei.addObserver(this);
        }
    }

    public void oeffnen() {
        DMTNFileChooser fc = new DMTNFileChooser((Betriebssystem) holeAnwendung().getSystemSoftware());
        int rueckgabe = fc.openDialog();
        if (rueckgabe == DMTNFileChooser.OK) {
            String aktuellerDateiname = fc.getAktuellerDateiname();
            Datei tmpFile = holeAnwendung().getSystemSoftware().getDateisystem().holeDatei(fc.getAktuellerOrdner(),
                    aktuellerDateiname);
            changeCurrentFile(tmpFile);
        } else {
            LOG.debug("ERROR (" + this.hashCode() + "): Fehler beim oeffnen einer Datei");
        }
    }

    private void updateFromFile() {
        if (aktuelleDatei != null) {
            setTitle(anwendung, aktuelleDatei.getName());
            original = aktuelleDatei.getDateiInhalt();
            editorField.setText(original);
        } else {
            setTitle(anwendung);
            LOG.debug("Fehler beim oeffnen einer Datei: keine Datei ausgewaehlt");
        }
    }

    public void starten(String[] param) {
        String dateiName = holeParameter()[0];
        if (!dateiName.equals("")) {
            Datei datei = this.holeAnwendung().getSystemSoftware().getDateisystem().holeDatei(arbeitsVerzeichnis,
                    dateiName);
            if (datei != null) {
                aktuelleDatei = datei;
            }
            updateFromFile();
        }
    }

    public void neu() {
        editorField.setText("");
        setTitle(anwendung);
        changeCurrentFile(null);
    }

    public void updateUnchangedTextFromFile() {
        if (original != null && editorField != null && aktuelleDatei != null
                && original.equals(editorField.getText())) {
            original = aktuelleDatei.getDateiInhalt();
            editorField.setText(original);
        }
    }

    @Override
    public void update(Observable observable, Object arg1) {
        if (observable == aktuelleDatei) {
            updateUnchangedTextFromFile();
        }
    }
}
