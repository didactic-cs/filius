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

public class GUIDocuItem {
    public static final int RECT = 1, TEXT = 2;

    private int type;
    private String text;
    private int x;
    private int y;
    private int width;
    private int height;

    private JDocuElement elem;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getText() {
        if (elem != null) {
            this.text = elem.getText();
        }
        return text;
    }

    public void setText(String text) {
        this.text = text;
        if (elem != null) {
            elem.setText(this.text);
        }
    }

    public JDocuElement asDocuElement() {
        if (elem == null) {
            elem = new JDocuElement(type == TEXT);
            elem.setText(text);
            elem.setBounds(x, y, width, height);
            elem.setEnabled(false);
        }
        return elem;
    }

    public static GUIDocuItem createDocuItem(JDocuElement elem) {
        GUIDocuItem item = new GUIDocuItem();
        item.text = elem.getText();
        item.type = item.text != null ? TEXT : RECT;
        item.x = elem.getX();
        item.y = elem.getY();
        item.width = elem.getWidth();
        item.height = elem.getHeight();
        item.elem = elem;
        return item;
    }

    public int getX() {
        if (elem != null) {
            this.x = elem.getX();
        }
        return x;
    }

    public void setX(int x) {
        this.x = x;
        if (elem != null) {
            elem.setBounds(x, y, width, height);
        }
    }

    public int getY() {
        if (elem != null) {
            this.y = elem.getY();
        }
        return y;
    }

    public void setY(int y) {
        this.y = y;
        if (elem != null) {
            elem.setBounds(x, y, width, height);
        }
    }

    public int getWidth() {
        if (elem != null) {
            this.width = elem.getWidth();
        }
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
        if (elem != null) {
            elem.setBounds(x, y, width, height);
        }
    }

    public int getHeight() {
        if (elem != null) {
            this.height = elem.getHeight();
        }
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
        if (elem != null) {
            elem.setBounds(x, y, width, height);
        }
    }
}
