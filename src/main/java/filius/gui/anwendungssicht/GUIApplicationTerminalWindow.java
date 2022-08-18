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
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Observable;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.InternalFrameEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import filius.software.lokal.Terminal;
import filius.software.system.Dateisystem;

/**
 * Applikationsfenster fuer ein Terminal
 * 
 * @author Johannes Bade & Thomas Gerding
 * 
 */
@SuppressWarnings("serial")
public class GUIApplicationTerminalWindow extends GUIApplicationWindow {
    private static Logger LOG = LoggerFactory.getLogger(GUIApplicationTerminalWindow.class);

    private static final Color BACKGROUND = new Color(0, 0, 0);
    private static final Color FOREGROUND = new Color(222, 222, 222);
    private static final String MENU_LINE = "==========================================================================\n";
    private JTextArea terminalField;
    private JPanel backPanel;
    private JLabel inputLabel;
    private JScrollPane tpPane;

    private boolean jobRunning;
    private String enteredCommand;
    private String[] enteredParameters;

    private boolean multipleObserverEvents;

    private ArrayList<String> commandHistory = new ArrayList<String>();
    private int commandHistoryPointer = -1;

    public GUIApplicationTerminalWindow(GUIDesktopPanel desktop, String appName) {
        super(desktop, appName);
        jobRunning = false;
        multipleObserverEvents = false;

        terminalField = new JTextArea("");
        terminalField.setEditable(false);
        terminalField.setCaretColor(FOREGROUND);
        terminalField.setForeground(FOREGROUND);
        terminalField.setBackground(BACKGROUND);
        terminalField.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        terminalField.setFocusable(false);
        terminalField.setBorder(null);
        terminalField.setLineWrap(true);

        JTextField inputField = initInput();

        inputLabel = new JLabel(">");
        inputLabel.setBackground(BACKGROUND);
        inputLabel.setForeground(FOREGROUND);
        inputLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));

        Box terminalBox = Box.createHorizontalBox();
        terminalBox.setBackground(BACKGROUND);
        terminalBox.add(terminalField);
        terminalBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 1, 5));

        Box inputBox = Box.createHorizontalBox();
        inputBox.setBackground(BACKGROUND);
        inputBox.add(inputLabel);
        inputBox.add(Box.createHorizontalStrut(1));
        inputBox.add(inputField);
        inputBox.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));

        backPanel = new JPanel(new BorderLayout());
        backPanel.setBackground(BACKGROUND);
        backPanel.add(terminalBox, BorderLayout.CENTER);
        backPanel.add(inputBox, BorderLayout.SOUTH);

        tpPane = new JScrollPane(backPanel); // make textfield scrollable
        tpPane.setBorder(null);
        tpPane.setBackground(BACKGROUND);
        tpPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        tpPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(tpPane, BorderLayout.CENTER);

        terminalField.setText("");
        showStartScreen();

        inputField.requestFocusInWindow();
        this.inputLabel.setText(Dateisystem.absoluterPfad(((Terminal) holeAnwendung()).getAktuellerOrdner()) + "> ");

        scrollDown();
    }

    private void showStartScreen() {
        terminalField.append(messages.getString("sw_terminal_msg57") + "\n");
        terminalField.append(MENU_LINE);
        terminalField.append(messages.getString("sw_terminal_msg26") + "\n");
        terminalField.append(MENU_LINE);
    }

    private JTextField initInput() {
        JTextField inputField = new JTextField("");
        inputField.setEditable(true);
        inputField.setBackground(BACKGROUND);
        inputField.setForeground(FOREGROUND);
        inputField.setCaretColor(FOREGROUND);
        inputField.setBorder(null);
        inputField.setFont(new Font("Courier New", Font.PLAIN, 11));
        inputField.setOpaque(false);

        inputField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "doNothing");
        inputField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "doNothing");
        inputField.getActionMap().put("doNothing", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {}
        });

        inputField.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    commandHistoryPointer = -1; // lass uns doch besser wieder
                                                // von unten/vorne beginnen
                    if (!(inputField.getText().isEmpty() || inputField.getText().replaceAll(" ", "").isEmpty())) {
                        terminalField.append("\n" + inputLabel.getText() + inputField.getText() + "\n");
                        StringTokenizer tk = new StringTokenizer(inputField.getText(), " ");

                        /* Erstes Token enthaelt den Befehl */
                        enteredCommand = tk.nextToken();

                        /*
                         * restliche Tokens werden in String Array geschrieben. Array wird sicherheitshalber mit
                         * mindestens 3 leeren Strings gefüllt!
                         */
                        enteredParameters = new String[3 + tk.countTokens()];
                        for (int i = 0; i < 3 + tk.countTokens(); i++) {
                            enteredParameters[i] = new String();
                        }
                        int iti = 0;
                        while (tk.hasMoreTokens()) {
                            enteredParameters[iti] = tk.nextToken();
                            iti++;
                        }

                        commandHistory.add(inputField.getText());
                        if (enteredCommand.equals("exit")) {
                            GUIApplicationTerminalWindow.this.close();
                        } else if (enteredCommand.equals("reset")) {
                            terminalField.setText("");
                            showStartScreen();
                            scrollDown();
                        } else {
                            inputLabel.setVisible(false);
                            jobRunning = true;
                            ((Terminal) holeAnwendung()).terminalEingabeAuswerten(enteredCommand, enteredParameters);
                        }
                    } else {
                        terminalField.append("\n");
                    }
                    inputField.setText("");
                    scrollDown();
                }
                // [strg] + [c]
                else if (e.getKeyCode() == KeyEvent.VK_C
                        && (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK) {
                    ((Terminal) holeAnwendung()).setInterrupt(true);
                    LOG.debug("execution aborted with ctrl+c");
                }
                // 38 arrow-up / 40 arrow-down
                else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (e.getKeyCode() == KeyEvent.VK_UP) {
                        commandHistoryPointer++;
                    }
                    if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        commandHistoryPointer--;
                    }
                    if (commandHistoryPointer < -1) {
                        commandHistoryPointer = -1;
                    }
                    if (commandHistoryPointer >= commandHistory.size()) {
                        commandHistoryPointer = commandHistory.size() - 1;
                    }
                    try {
                        if (commandHistoryPointer != -1) {
                            inputField.setText(commandHistory.get(commandHistory.size() - 1 - commandHistoryPointer));
                        } else if (commandHistoryPointer == -1) {
                            inputField.setText("");
                        }
                    } catch (IndexOutOfBoundsException eis) {}
                }
            }

            public void keyReleased(KeyEvent arg0) {}

            public void keyTyped(KeyEvent arg0) {}

        });
        return inputField;
    }

    public void setMultipleObserverEvents(boolean flag) {}

    public void windowActivated(WindowEvent e) {}

    public void windowClosing(WindowEvent e) {}

    public void windowDeactivated(WindowEvent e) {}

    public void windowDeiconified(WindowEvent e) {}

    public void windowIconified(WindowEvent e) {}

    public void windowOpened(WindowEvent e) {}

    public void internalFrameActivated(InternalFrameEvent e) {}

    public void internalFrameClosed(InternalFrameEvent e) {}

    public void internalFrameClosing(InternalFrameEvent e) {}

    public void internalFrameDeactivated(InternalFrameEvent e) {}

    public void internalFrameDeiconified(InternalFrameEvent e) {}

    public void internalFrameIconified(InternalFrameEvent e) {}

    public void internalFrameOpened(InternalFrameEvent e) {}

    public void update(Observable arg0, Object arg1) {
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass() + " (GUIApplicationTerminalWindow), update(" + arg0
                + "," + arg1 + ")");
        if (arg1 != null && jobRunning) {
            if (arg1 instanceof Boolean) {
                multipleObserverEvents = ((Boolean) arg1).booleanValue();
            } else { // expect String
                this.terminalField.append(arg1.toString());
                try {
                    // mini delay to let the terminalField reliably update its
                    // new height
                    Thread.sleep(200);
                } catch (InterruptedException e) {}
            }
            if (!multipleObserverEvents) {
                this.inputLabel
                        .setText(Dateisystem.absoluterPfad(((Terminal) holeAnwendung()).getAktuellerOrdner()) + "> ");
                this.inputLabel.setVisible(true);
                jobRunning = false;
            }
            scrollDown();
        }
    }

    private void scrollDown() {
        this.tpPane.repaint();
        this.terminalField.repaint();
        this.inputLabel.repaint();
        this.tpPane.getVerticalScrollBar().setValue(this.tpPane.getVerticalScrollBar().getMaximum());
    }

}
