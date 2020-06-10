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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

import filius.Main;
import filius.gui.JMainFrame;
import filius.hardware.NetzwerkInterface;
import filius.hardware.knoten.Host;
import filius.hardware.knoten.InternetKnoten;
import filius.rahmenprogramm.I18n;

public class JSidebarButton extends JLabel implements Observer, I18n {

    private static final long serialVersionUID = 1L;

    private String typ;
    private boolean selektiert;
    private boolean modemVerbunden;

    public boolean isSelektiert() {
        return selektiert;
    }

    public void setSelektiert(boolean selektiert) {
        this.selektiert = selektiert;
    }

    public String getTyp() {
        return typ;
    }

    public void setTyp(String typ) {
        this.typ = typ;
    }

    public JSidebarButton() {
        initAlignment();
    }

    private void initAlignment() {
        this.setVerticalTextPosition(SwingConstants.BOTTOM);
        this.setHorizontalTextPosition(SwingConstants.CENTER);
        this.setAlignmentX(0.5f);
    }

    public JSidebarButton(Icon icon, String typ) {
        super(icon, JLabel.CENTER);
        this.typ = typ;
        initAlignment();
    }

    public JSidebarButton(String text, Icon icon, String typ) {
        super(text, icon, JLabel.CENTER);
        this.typ = typ;
        initAlignment();
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle((int) super.getBounds().getX(), (int) super.getBounds().getY(), this.getWidth(),
                this.getHeight());
    }

    public boolean inBounds(int x, int y) {
        return x >= this.getX() && x <= this.getX() + this.getWidth() && y >= this.getY()
                && y <= this.getY() + this.getHeight();
    }

    public int getWidth() {
        int width = 0;

        if (null != this.getText()) {
            width = this.getFontMetrics(this.getFont()).stringWidth(this.getText());
        }
        width += 5;
        if (this.getIcon() != null && this.getIcon().getIconWidth() > width)
            width = this.getIcon().getIconWidth();

        return width;
    }

    public int getHeight() {
        int height;

        height = this.getFontMetrics(this.getFont()).getHeight();
        if (this.getIcon() != null) {
            height += this.getIcon().getIconHeight();
        }
        height += 5;

        return height;
    }

    public Dimension getPreferredSize() {
        return new Dimension(getWidth(), getHeight());
    }

    // Update location after a text change so that the icon does not move
    public void setTextAndUpdateLocation(String text) {
        int icoW = (getIcon() != null ? getIcon().getIconWidth() : 0);
        if (icoW % 2 == 1)
            icoW--; // Trick to avoid a one pixel shift when the icon's width is odd
        int dW = (getWidth() - icoW) / 2;
        super.setText(text);
        int dW2 = (getWidth() - icoW) / 2;
        setBounds(getX() - dW2 + dW, getY(), getWidth(), getHeight()); // SetLocation won't be enough here
    }

    // Update location after the initial text assignment so that the icon does not move
    public void initTextAndUpdateLocation(String text) {
        setTextAndUpdateLocation(text);
        setLocation(getX(), getY() + 10); // The reason for this 10px down is not clear but it is necessary to fix the
                                          // icon at the end of the drag-and-drop creation
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        if (selektiert) {
            g.setColor(new Color(0, 0, 0));
            Graphics2D g2 = (Graphics2D) g;
            Stroke stroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1, new float[] { 2 }, 0);
            g2.setStroke(stroke);
            g2.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
            // Use same color as the one used for multiple selection
            g2.setColor(new Color(0.80f, 0.92f, 1f, 0.2f));
            g2.fillRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
        }

        if (modemVerbunden) {
            g2d.setColor(new Color(0, 255, 0));
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.fillOval((this.getWidth() / 2) - 6, (this.getHeight() / 2) - 6, 12, 12);
        }
    }

    public void update(Observable o, Object arg) {
        Main.debug.println(
                "INVOKED (" + this.hashCode() + ") " + getClass() + " (JSidebarButton), update(" + o + "," + arg + ")");

        if (arg != null && arg.equals(Boolean.TRUE)) {
            modemVerbunden = true;
        } else if (arg != null && arg.equals(Boolean.FALSE)) {
            modemVerbunden = false;
        } else if (arg != null && arg instanceof String) {
            JOptionPane.showMessageDialog(JMainFrame.getJMainFrame(), arg);
        } else if (arg != null && arg instanceof Host) {
            this.setText(((Host) arg).holeAnzeigeName());
        }
        if (arg != null && arg instanceof InternetKnoten) {
            updateTooltip((InternetKnoten) arg);
        }
        this.updateUI();
    }

    void updateTooltip(InternetKnoten knoten) {
        StringBuilder tooltip = new StringBuilder();
        tooltip.append("<html><pre>");

        tooltip.append(messages.getString("jsidebar_tooltip_ipAddress")).append(":");
        String gateway = null;
        String dns = null;
        for (NetzwerkInterface nic : knoten.getNetzwerkInterfaces()) {
            tooltip.append("\n ").append(nic.getIp()).append(" / ").append(nic.getSubnetzMaske());
            tooltip.append(" (").append(nic.getMac()).append(")");
            gateway = nic.getGateway();
            dns = nic.getDns();
        }
        tooltip.append("\n").append(messages.getString("jsidebar_tooltip_gateway")).append(": ").append(gateway);
        if (knoten instanceof Host) {
            tooltip.append("\n").append(messages.getString("jsidebar_tooltip_dnsServer")).append(": ").append(dns);
        }

        tooltip.append("</pre></html>");
        setToolTipText(tooltip.toString());
    }
}
