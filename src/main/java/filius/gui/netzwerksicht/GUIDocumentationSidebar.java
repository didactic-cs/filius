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

import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.event.MouseInputAdapter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.itextpdf.text.DocumentException;

import filius.gui.GUIContainer;
import filius.gui.JMainFrame;
import filius.gui.documentation.ReportGenerator;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.SzenarioVerwaltung;

public class GUIDocumentationSidebar extends GUISidebar implements I18n {

    public static final String TYPE_TEXTFIELD = "textfield";
    public static final String TYPE_RECTANGLE = "rectangle";
    public static final String TYPE_EXPORT = "export";
    public static final String TYPE_REPORT = "report";

    public static final String ADD_TEXT = "gfx/dokumentation/add_text_small.png";
    public static final String ADD_RECTANGLE = "gfx/dokumentation/add_small.png";
    public static final String EXPORT = "gfx/dokumentation/download_small.png";
    public static final String REPORT = "gfx/dokumentation/pdf_small.png";

    private static GUIDocumentationSidebar sidebar;

    public static GUIDocumentationSidebar getGUIDocumentationSidebar() {
        if (sidebar == null) {
            sidebar = new GUIDocumentationSidebar();
        }
        return sidebar;
    }

    @Override
    protected void addItemsToSidebar() {
        ImageIcon icon = new ImageIcon(getClass().getResource("/" + ADD_TEXT));
        JSidebarButton newLabel = new JSidebarButton(messages.getString("docusidebar_msg1"), icon, TYPE_TEXTFIELD);
        buttonList.add(newLabel);
        leistenpanel.add(newLabel);

        icon = new ImageIcon(getClass().getResource("/" + ADD_RECTANGLE));
        newLabel = new JSidebarButton(messages.getString("docusidebar_msg3"), icon, TYPE_RECTANGLE);
        buttonList.add(newLabel);
        leistenpanel.add(newLabel);

        icon = new ImageIcon(getClass().getResource("/" + EXPORT));
        newLabel = new JSidebarButton(messages.getString("docusidebar_msg5"), icon, TYPE_EXPORT);
        newLabel.setToolTipText(messages.getString("docusidebar_msg6"));
        newLabel.addMouseListener(new MouseInputAdapter() {
            public void mousePressed(MouseEvent e) {
                GUIContainer.getGUIContainer().exportAsImage();
            }
        });
        buttonList.add(newLabel);
        leistenpanel.add(newLabel);

        icon = new ImageIcon(getClass().getResource("/" + REPORT));
        newLabel = new JSidebarButton(messages.getString("docusidebar_msg7"), icon, TYPE_REPORT);
        newLabel.setToolTipText(messages.getString("docusidebar_msg8"));
        newLabel.addMouseListener(new MouseInputAdapter() {
            public void mousePressed(MouseEvent e) {
                GUIDocumentationSidebar.this.generateReport();
            }
        });
        buttonList.add(newLabel);
        leistenpanel.add(newLabel);
    }

    private void generateReport() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter pdfFileFilter = new FileNameExtensionFilter("PDF", "pdf");
        fileChooser.addChoosableFileFilter(pdfFileFilter);
        fileChooser.setAcceptAllFileFilterUsed(false);
        String path = SzenarioVerwaltung.getInstance().holePfad();
        if (path != null) {
            String szenarioFile = new File(path).getAbsolutePath();
            File preselectedFile = new File(szenarioFile.substring(0, szenarioFile.lastIndexOf(".")));
            fileChooser.setSelectedFile(preselectedFile);
        }

        if (fileChooser.showSaveDialog(JMainFrame.getJMainFrame()) == JFileChooser.APPROVE_OPTION) {
            String reportPath = fileChooser.getSelectedFile().getAbsolutePath();
            reportPath = reportPath.endsWith(".pdf") ? reportPath : reportPath + ".pdf";

            try {
                ReportGenerator.getInstance().generateReport(reportPath);
            } catch (DocumentException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
