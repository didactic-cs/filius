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

import java.awt.Dimension;
import java.util.List;

import javax.swing.JPanel;

import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("serial")
public class GUIPrintPanel extends JPanel {
    private static final int EMPTY_BORDER = 10;

    private GUINetworkPanel networkPanel;
    private GUIDocumentationPanel docuPanel;
    private GUIFooterPanel footer;

    public GUIPrintPanel(int width, int height, String footerText) {
        setOpaque(false);

        networkPanel = new GUINetworkPanel(width, height);
        networkPanel.setBounds(EMPTY_BORDER, EMPTY_BORDER, width, height);
        add(networkPanel);

        docuPanel = new GUIDocumentationPanel(width, height);
        docuPanel.setBounds(EMPTY_BORDER, EMPTY_BORDER, width, height);
        add(docuPanel);

        footer = new GUIFooterPanel(width, height + 100, StringUtils.isBlank(footerText) ? "Filius" : footerText);
        add(footer);

        setSize(footer.getWidth() + 2 * EMPTY_BORDER, footer.getHeight() + 2 * EMPTY_BORDER);
    }

    public void updateViewport(List<GUIKnotenItem> knoten, List<GUIKabelItem> kabel, List<GUIDocuItem> docuItems) {
        networkPanel.updateViewport(knoten, kabel);
        docuPanel.updateViewport(docuItems);

        updateFooterPosition();
        footer.updateViewport();
    }

    private void updateFooterPosition() {
        int footerY = (int) Math.max(networkPanel.maxY, docuPanel.maxY) + 30;
        int footerX = (int) Math.min(networkPanel.minX, docuPanel.minX) + EMPTY_BORDER;
        footer.setFooterPos(footerX, footerY);
    }

    @Override
    public Dimension getSize() {
        return new Dimension(getWidth(), getHeight());
    }

    public int getClipY() {
        return (int) min(networkPanel.minY, docuPanel.minY, footer.minY);
    }

    public int getClipHeight() {
        int totalMaxY = (int) footer.maxY;
        return totalMaxY - getClipY() + 2 * EMPTY_BORDER;
    }

    public int getClipWidth() {
        int totalMaxX = (int) max(networkPanel.maxX, docuPanel.maxX, footer.maxX);
        return totalMaxX - getClipX() + 2 * EMPTY_BORDER;
    }

    public int getClipX() {
        return (int) min(networkPanel.minX, docuPanel.minX, footer.minX);
    }

    private double min(double a, double b, double c) {
        double min = Integer.MAX_VALUE;
        if (a < min) {
            min = a;
        }
        if (b < min) {
            min = b;
        }
        if (c < min) {
            min = c;
        }
        return min;
    }

    private double max(double a, double b, double c) {
        double max = 0;
        if (a > max) {
            max = a;
        }
        if (b > max) {
            max = b;
        }
        if (c > max) {
            max = c;
        }
        return max;
    }
}
