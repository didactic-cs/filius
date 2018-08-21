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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.BadLocationException;

import filius.gui.GUIContainer;
import filius.gui.JMainFrame;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.SzenarioVerwaltung;

public class JDocuElement extends JPanel implements I18n {

    private static final long serialVersionUID = 1L;

    private boolean selected = false;
    private int updateType;
    private Dimension elemMoveOffset;

    private boolean enabled = true;

    private JTextArea textArea;

    private static final int TEXT_BORDER_WIDTH = 5;

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.setFocusable(enabled);
        if (textArea != null) {
            textArea.setEditable(enabled);
            textArea.setFocusable(enabled);
            if (enabled) {
                textArea.setBackground(new Color(0.9f, 0.9f, 0.9f, 0.5f));
            } else {
                textArea.setBackground(new Color(1f, 1f, 1f, 0f));
            }
        }
    }

    @Override
    public boolean requestFocusInWindow() {
        boolean result;
        if (textArea != null) {
            result = textArea.requestFocusInWindow();
        } else {
            result = super.requestFocusInWindow();
        }
        return result;
    }

    private void initRectangle() {
        setSize(100, 50);
        Color color = new Color(0.8f, 0.8f, 0.8f, 0.8f);
        setBackground(color);
    }

    public JDocuElement(boolean text) {
        if (!text) {
            initRectangle();
        } else {
            initTextfield();
        }
        initListener();
    }

    private void initTextfield() {
        setSize(100, 50);
        this.setLayout(new BorderLayout());
        textArea = new JTextArea();
        textArea.setBorder(BorderFactory.createEmptyBorder(TEXT_BORDER_WIDTH, TEXT_BORDER_WIDTH, TEXT_BORDER_WIDTH,
                TEXT_BORDER_WIDTH));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEnabled(enabled);
        this.add(textArea, BorderLayout.CENTER);
    }

    private void initListener() {
        JComponent comp = textArea != null ? textArea : this;
        comp.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (JDocuElement.this.enabled) {
                    JDocuElement.this.selected = false;
                    updateType = GUIContainer.NONE;
                    JDocuElement.this.updateUI();
                    SzenarioVerwaltung.getInstance().setzeGeaendert();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (JDocuElement.this.enabled) {
                    JDocuElement.this.selected = true;
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        final JMenuItem pmLoeschen = new JMenuItem(messages.getString("guievents_msg7"));
                        pmLoeschen.addActionListener(new ActionListener() {

                            public void actionPerformed(ActionEvent evt) {
                                GUIContainer.getGUIContainer().removeDocuElement(JDocuElement.this);
                                GUIContainer.getGUIContainer().updateViewport();
                            }
                        });
                        JPopupMenu popmen = new JPopupMenu();
                        popmen.add(pmLoeschen);

                        JDocuElement.this.add(popmen);
                        popmen.setVisible(true);
                        popmen.show(JDocuElement.this, e.getX(), e.getY());
                    } else if (updateType == GUIContainer.MOVE) {
                        elemMoveOffset = new Dimension(e.getX(), e.getY());
                    }
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (JDocuElement.this.enabled) {
                    JMainFrame.getJMainFrame().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });
        comp.addMouseMotionListener(new MouseInputAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (JDocuElement.this.enabled) {
                    if (JDocuElement.this.updateType == GUIContainer.MOVE) {
                        JDocuElement.this.setLocation(
                                e.getX() - (int) JDocuElement.this.elemMoveOffset.getWidth() + JDocuElement.this.getX(),
                                e.getY() - (int) JDocuElement.this.elemMoveOffset.getHeight()
                                        + JDocuElement.this.getY());
                    } else if (JDocuElement.this.updateType == GUIContainer.LEFT_SIZING) {
                        JDocuElement.this.setBounds(JDocuElement.this.getX() + e.getX(), JDocuElement.this.getY(),
                                JDocuElement.this.getWidth() - e.getX(), JDocuElement.this.getHeight());
                    } else if (JDocuElement.this.updateType == GUIContainer.RIGHT_SIZING) {
                        JDocuElement.this.setBounds(JDocuElement.this.getX(), JDocuElement.this.getY(), e.getX(),
                                JDocuElement.this.getHeight());
                    } else if (JDocuElement.this.updateType == GUIContainer.LOWER_SIZING) {
                        JDocuElement.this.setBounds(JDocuElement.this.getX(), JDocuElement.this.getY(),
                                JDocuElement.this.getWidth(), e.getY());
                    } else if (JDocuElement.this.updateType == GUIContainer.UPPER_SIZING) {
                        JDocuElement.this.setBounds(JDocuElement.this.getX(), JDocuElement.this.getY() + e.getY(),
                                JDocuElement.this.getWidth(), JDocuElement.this.getHeight() - e.getY());
                    }
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (JDocuElement.this.enabled) {
                    int x = e.getX();
                    int y = e.getY();
                    if (x <= TEXT_BORDER_WIDTH) {
                        updateType = GUIContainer.LEFT_SIZING;
                    } else if (x >= JDocuElement.this.getWidth() - TEXT_BORDER_WIDTH) {
                        updateType = GUIContainer.RIGHT_SIZING;
                    } else if (y <= TEXT_BORDER_WIDTH) {
                        updateType = GUIContainer.UPPER_SIZING;
                    } else if (y >= JDocuElement.this.getHeight() - TEXT_BORDER_WIDTH) {
                        updateType = GUIContainer.LOWER_SIZING;
                    } else if (JDocuElement.this.textArea != null) {
                        try {
                            Rectangle charPos = JDocuElement.this.textArea.modelToView(JDocuElement.this.textArea
                                    .viewToModel(e.getPoint()));
                            if (e.getY() >= charPos.getY() && e.getY() <= charPos.getY() + charPos.getHeight()) {
                                updateType = GUIContainer.NONE;
                            } else {
                                updateType = GUIContainer.MOVE;
                            }
                        } catch (BadLocationException e1) {}
                    } else {
                        updateType = GUIContainer.MOVE;
                    }
                    if (updateType == GUIContainer.MOVE) {
                        JMainFrame.getJMainFrame().setCursor(new Cursor(Cursor.MOVE_CURSOR));
                    } else if (updateType == GUIContainer.RIGHT_SIZING) {
                        JMainFrame.getJMainFrame().setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
                    } else if (updateType == GUIContainer.LEFT_SIZING) {
                        JMainFrame.getJMainFrame().setCursor(new Cursor(Cursor.W_RESIZE_CURSOR));
                    } else if (updateType == GUIContainer.UPPER_SIZING) {
                        JMainFrame.getJMainFrame().setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
                    } else if (updateType == GUIContainer.LOWER_SIZING) {
                        JMainFrame.getJMainFrame().setCursor(new Cursor(Cursor.S_RESIZE_CURSOR));
                    } else {
                        JMainFrame.getJMainFrame().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    }
                }
            }
        });
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        repaint();
    }

    public String getText() {
        if (this.textArea != null) {
            return textArea.getText();
        }
        return null;
    }

    public void setText(String text) {
        if (this.textArea != null) {
            textArea.setText(text);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (selected) {
            g.setColor(new Color(0, 0, 0));
            Graphics2D g2 = (Graphics2D) g;
            Stroke stroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1, new float[] { 2 }, 0);
            g2.setStroke(stroke);
            g2.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
            g.setColor(new Color(128, 200, 255));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
            g2.fillRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
        }
    }

}
