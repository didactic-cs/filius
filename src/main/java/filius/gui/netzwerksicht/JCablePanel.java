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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import filius.hardware.Hardware;

/**
 * 
 * @author Johannes Bade
 */
@SuppressWarnings("deprecation")
public class JCablePanel extends JPanel implements Observer {
    private static Logger LOG = LoggerFactory.getLogger(JCablePanel.class);

    private static final long serialVersionUID = 1L;
    private GUIKnotenItem ziel1, ziel2;
    private GUIKabelItem kabelItem;

    private QuadCurve2D currCurve = null;
    private Color kabelFarbe = new Color(64, 64, 64);
    private final Color farbeKabel = new Color(64, 64, 64);
    private final Color farbeWireless = new Color(192, 192, 192);
    private final Color farbeBlinken = new Color(0, 255, 64);
    private final Color farbeFailure = Color.RED;

    public JCablePanel() {
        super();
        this.setOpaque(false);
    }

    public void updateBounds() {
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass() + " (JCablePanel), updateBounds()");

        if (null != ziel1 && null != ziel2) {
            // Theoretisch korrekte Positionen
            int x1 = (int) (ziel1.getImageLabel().getX());
            int x2 = (int) (ziel2.getImageLabel().getX());
            int y1 = (int) (ziel1.getImageLabel().getY());
            int y2 = (int) (ziel2.getImageLabel().getY());

            x1 = (int) (x1 + (0.5 * ziel1.getImageLabel().getWidth()));
            y1 = (int) (y1 + (0.5 * ziel1.getImageLabel().getHeight()));
            x2 = (int) (x2 + (0.5 * ziel2.getImageLabel().getWidth()));
            y2 = (int) (y2 + (0.5 * ziel2.getImageLabel().getHeight()));

            // Absolut korrekte Positionen (also Sidebar und Menu rausgerechnet)
            if (x1 > x2) {
                int t1 = x1;
                x1 = x2;
                x2 = t1;
            }
            if (y1 > y2) {
                int t1 = y1;
                y1 = y2;
                y2 = t1;
            }

            // add 2 for each direction to take care of linewidth
            setBounds(x1 - 2, y1 - 2, x2 - x1 + 4, y2 - y1 + 4);
            LOG.trace("JCablePanel (" + this.hashCode() + "), bounds: " + x1 + "/" + y1 + ", " + x2 + "/" + y2 + "  (W:"
                    + (x2 - x1) + ", H:" + (y2 - y1) + ")");
        }
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int x1, x2, y1, y2;

        // Theoretisch korrekte Positionen
        x1 = (int) (ziel1.getImageLabel().getX() + (0.5 * ziel1.getImageLabel().getWidth()));
        x2 = (int) (ziel2.getImageLabel().getX() + (0.5 * ziel2.getImageLabel().getWidth()));
        y1 = (int) (ziel1.getImageLabel().getY() + (0.5 * ziel1.getImageLabel().getHeight()));
        y2 = (int) (ziel2.getImageLabel().getY() + (0.5 * ziel2.getImageLabel().getHeight()));

        /* Einfaches Zeichnen */
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(2));

        if (isWireless()) {
            if (kabelFarbe.equals(farbeKabel)) {
                kabelFarbe = farbeWireless;
            }
            if (!kabelFarbe.equals(farbeBlinken)) {
                float dash[] = { 10.0f };
                g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
            }
        }
        g.setColor(kabelFarbe);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int kp1 = (x1 - this.getX() + x2 - this.getX()) / 4;
        if ((x1 > x2 && y1 > y2) || (x1 < x2 && y1 < y2))
            kp1 = 3 * kp1; // correct X value of control point for falling
                           // lines (upper left to lower right corner)
        int kp2 = (y1 - this.getY() + y2 - this.getY()) / 4;

        QuadCurve2D myCurve = new QuadCurve2D.Double(x1 - this.getX(), y1 - this.getY(), kp1, kp2, x2 - this.getX(),
                y2 - this.getY());

        // Kurve malen
        g2.draw(myCurve);
        this.currCurve = myCurve;
        this.setOpaque(false);
    }

    private boolean isWireless() {
        return getKabelItem() != null && getKabelItem().getDasKabel() != null
                && getKabelItem().getDasKabel().getWireless();
    }

    /*
     * Method to examine whether the mouse was clicked close to a line representing a cable in filius. ATTENTION: There
     * are several workarounds necessary to cope with Java's strange boundary handling. Thus, the actual bounds are
     * tested prior to actually use them for determining a point to be inside a bound of a curve or line, respectively.
     */
    public boolean clicked(int x, int y) {
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass() + " (JCablePanel), clicked(" + x + "," + y + ")");
        int delta = 10;

        Rectangle2D absolutePointerRect = new Rectangle2D.Double(x - delta, y - delta, 2 * delta, 2 * delta);
        boolean hitPanel = getBounds().intersects(absolutePointerRect);
        Rectangle2D relativePointerRect = new Rectangle2D.Double(x - getX() - delta, y - getY() - delta, 2 * delta,
                2 * delta);
        boolean hitCurve = currCurve.intersects(relativePointerRect);
        return hitCurve && hitPanel;
    }

    public GUIKnotenItem getZiel1() {
        return ziel1;
    }

    public void setZiel1(GUIKnotenItem ziel1) {
        this.ziel1 = ziel1;
    }

    public GUIKnotenItem getZiel2() {
        return ziel2;
    }

    public void setZiel2(GUIKnotenItem ziel2) {
        this.ziel2 = ziel2;
        updateBounds();
    }

    public void setKabelItem(GUIKabelItem value) {
        kabelItem = value;
    }

    public GUIKabelItem getKabelItem() {
        return kabelItem;
    }

    /**
     * Wird genutzt um Kabel blinken zu lassen :)
     */
    public void update(Observable o, Object arg) {
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass() + " (JCablePanel), update(" + o + "," + arg + ")");
        LOG.trace("update cable: {}", arg);
        if (arg.equals(Boolean.TRUE)) {
            kabelFarbe = farbeBlinken;
        } else if (arg.equals(Hardware.FAILURE)) {
            LOG.debug("set cable color to red");
            kabelFarbe = farbeFailure;
        } else {
            resetColor();
        }
        updateUI();
    }

    private void resetColor() {
        if (isWireless()) {
            kabelFarbe = farbeWireless;
        } else {
            kabelFarbe = farbeKabel;
        }
    }
}
