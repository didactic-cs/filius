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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang3.StringUtils;
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
@SuppressWarnings({ "serial", "deprecation" })
public class GUIApplicationTerminalWindow extends GUIApplicationWindow {
    private static Logger LOG = LoggerFactory.getLogger(GUIApplicationTerminalWindow.class);

    private static final Color BACKGROUND = new Color(0, 0, 0);
    private static final Color FOREGROUND = new Color(222, 222, 222);
    private static final String MENU_LINE = "==========================================================================\n";
    private JTextArea terminalField;
    private JPanel backPanel;
    private JLabel inputLabel;
    private JTextField inputField;
    private JScrollPane tpPane;

    private boolean jobRunning;
    private String enteredCommand;
    private String[] enteredParameters;
    private int lastScrollPaneSize;

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

        initInput();

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
        backPanel.setFocusCycleRoot(true);
        backPanel.add(terminalBox, BorderLayout.CENTER);
        backPanel.add(inputBox, BorderLayout.SOUTH);

        tpPane = new JScrollPane(backPanel); // make textfield scrollable
        tpPane.setBorder(null);
        tpPane.setBackground(BACKGROUND);
        tpPane.setFocusable(false);
        tpPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        tpPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        tpPane.getViewport().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                GUIApplicationTerminalWindow.this.scrollToBottomAndFocusInput();
            }
        });
        add(tpPane, BorderLayout.CENTER);

        terminalField.setText("");
        showStartScreen();

        inputLabel.setText(Dateisystem.absoluterPfad(((Terminal) holeAnwendung()).getAktuellerOrdner()) + "> ");

        scrollToBottomAndFocusInput();
    }

    private void showStartScreen() {
        appendText(messages.getString("sw_terminal_msg57") + "\n");
        appendText(MENU_LINE);
        appendText(messages.getString("sw_terminal_msg26") + "\n");
        appendText(MENU_LINE);
    }

    private void initInput() {
        inputField = new JTextField("");
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
                    commandHistoryPointer = -1;
                    if (!StringUtils.isBlank(inputField.getText())) {
                        appendText("\n" + inputLabel.getText() + inputField.getText() + "\n");
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
                            scrollToBottomAndFocusInput();
                        } else {
                            GUIApplicationTerminalWindow.this.prepareJobStart();
                            ((Terminal) holeAnwendung()).terminalEingabeAuswerten(enteredCommand, enteredParameters);
                        }
                    } else {
                        appendText("  \n");
                    }
                    inputField.setText("");
                } else if (e.getKeyCode() == KeyEvent.VK_C
                        // [strg] + [c]
                        && (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK) {
                    ((Terminal) holeAnwendung()).setInterrupt(true);
                    LOG.debug("execution aborted with ctrl+c");
                } else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
                    // 38 arrow-up / 40 arrow-down
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
                scrollToBottom(true);
            }

            public void keyReleased(KeyEvent arg0) {}

            public void keyTyped(KeyEvent arg0) {}

        });
    }

    public void update(Observable observable, Object notificationObject) {
        LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass() + " (GUIApplicationTerminalWindow), update("
                + observable + "," + notificationObject + ")");
        if (notificationObject != null && jobRunning) {
            if (notificationObject instanceof Boolean) {
                multipleObserverEvents = ((Boolean) notificationObject).booleanValue();
            } else { // expect String
                appendText(notificationObject.toString());
            }
            if (!multipleObserverEvents) {
                inputLabel.setText(Dateisystem.absoluterPfad(((Terminal) holeAnwendung()).getAktuellerOrdner()) + "> ");
                prepareJobFinished();
            }
        }
        scrollToBottomAndFocusInput();
    }

    private void prepareJobFinished() {
        inputLabel.setVisible(true);
        jobRunning = false;
    }

    private void prepareJobStart() {
        inputLabel.setVisible(false);
        jobRunning = true;
    }

    private void appendText(String text) {
        terminalField.append(text);
    }

    private void scrollToBottomAndFocusInput() {
        scrollToBottom(false);
        focusInput();
    }

    private void focusInput() {
        if (null != inputField) {
            inputField.requestFocusInWindow();
            inputField.grabFocus();
        }
    }

    private void scrollToBottom(boolean force) {
        if (force || lastScrollPaneSize < tpPane.getVerticalScrollBar().getMaximum()) {
            lastScrollPaneSize = tpPane.getVerticalScrollBar().getMaximum();
            tpPane.getVerticalScrollBar().setValue(tpPane.getVerticalScrollBar().getMaximum());
        }
    }
}
