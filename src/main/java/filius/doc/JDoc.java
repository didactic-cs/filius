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
package filius.gui.dokusicht;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MouseInputAdapter;

import filius.gui.GUIContainer;
import filius.gui.JMainFrame;
import filius.gui.Palette;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.ProjectManager;

@SuppressWarnings("serial")
public class JDocElement extends JPanel implements I18n {
	
	// Update types
	private static final int UT_NONE = 0;
	private static final int UT_MOVE = 1;
	private static final int UT_LEFT_SIZING = 2;
	private static final int UT_RIGHT_SIZING = 4;
	private static final int UT_UPPER_SIZING = 8;
	private static final int UT_LOWER_SIZING = 16;
	
	private static final int RECT_CORNER_SIZE = 10;
    private static final int RECT_MIN_WIDTH = 20;
    private static final int RECT_MIN_HEIGHT = 20;
    private static final int TEXT_CORNER_SIZE = 10;
    private static final int TEXT_MIN_WIDTH = 100;
    private static final int TEXT_MIN_HEIGHT = 48;
    private static final int TEXT_BORDER_WIDTH = 7;
    private static final int TEXT_BORDER_BOTTOM = 30;

    private static final String fontSizeText[] = {messages.getString("docelement_msg4"), messages.getString("docelement_msg5"),  // Normal, Large, 
    		                                      messages.getString("docelement_msg6"),                                         // Very large
    	                                          messages.getString("docelement_msg7"), messages.getString("docelement_msg8")}; // Title, Large title
    private static final float fontSize[]      = {12f, 18f, 24f, 36f, 48f};

    private static final String fontStyleText[] = {messages.getString("docelement_msg9"), messages.getString("docelement_msg10"), // Normal, italic,
    		                                       messages.getString("docelement_msg11")};                                       // bold

    private static final Color fontColor[]      = {Palette.DOC_1_1_FG, Palette.DOC_1_2_FG, Palette.DOC_1_3_FG, Palette.DOC_1_4_FG, Palette.DOC_1_5_FG,
                                                   Palette.DOC_2_1_FG, Palette.DOC_2_2_FG, Palette.DOC_2_3_FG, Palette.DOC_2_4_FG, Palette.DOC_2_5_FG,
                                                   Palette.DOC_3_1_FG, Palette.DOC_3_2_FG, Palette.DOC_3_3_FG, Palette.DOC_3_4_FG, Palette.DOC_3_5_FG};    											   

    private static final Color panelColor[]     = {Palette.DOC_1_1_BG, Palette.DOC_1_2_BG, Palette.DOC_1_3_BG, Palette.DOC_1_4_BG, Palette.DOC_1_5_BG,
                                                   Palette.DOC_2_1_BG, Palette.DOC_2_2_BG, Palette.DOC_2_3_BG, Palette.DOC_2_4_BG, Palette.DOC_2_5_BG,
                                                   Palette.DOC_3_1_BG, Palette.DOC_3_2_BG, Palette.DOC_3_3_BG, Palette.DOC_3_4_BG, Palette.DOC_3_5_BG};

    private static final Color panelFocusColor               = new Color(0.22f, 0.6f, 0.98f, 0.3f); // Translucent light blue
    private static final Color borderFocusColor              = new Color(0.48f, 0.73f, 0.98f);      // Opaque light blue
    private static final Color textPanelBorderNormalColor    = new Color(0.7f, 0.7f, 0.7f);         // Light gray
    private static final Color transparent                   = new Color(0, 0, 0, 0);

    private int updateType;
    private boolean movedOrResized = false;
    private Dimension elemMoveOffset;

    private boolean enabled = true;
    // The real focus is not handy here
    // localFocus is true for the last JDocuElement that was clicked on
    private boolean localFocus = false;
    // enlargedPanel is true when isText, enabled, and localFocus are all true
    // In this case the panel is larger to manipulate the text area more easily
    private boolean enlargedPanel = false;
    // designMode is true when the panel is created by dragging the icon at design mode
    private boolean dragDropCreation = false;

    private Color referenceColor;
    private Color opaqueColor;
    private Color transparentColor;
    private Color borderColor;

    private boolean isText;
    private JDocTextArea textArea;    
    

    // JDocuTextArea
    //--------------------------------------------------------

    private class JDocTextArea extends JTextArea {

		private JDocTextArea() {

    		setBackground(transparent);      
    		setEnabled(enabled);
    		setWrapping();

    		initListeners();
    	}

    	private void initListeners() {

    		addKeyListener(new KeyAdapter() {
    			@Override
        		public void keyPressed(KeyEvent e) {
        			switch (e.getKeyCode()) {
        				case KeyEvent.VK_DELETE:
        					if (e.getModifiersEx() == InputEvent.CTRL_DOWN_MASK) delete();
        					break;
        				case KeyEvent.VK_ESCAPE:
        					setLocalFocus(false);
        					// Also release the real focus   
        					getRootPane().requestFocusInWindow();
        			}
        			ProjectManager.getInstance().setModified();
        		}
        	});
    		
    		addMouseListener(new MouseInputAdapter() {   
    			@Override
                public void mouseEntered(MouseEvent e) {
                    if (enabled) setPanelCursor(Cursor.TEXT_CURSOR);
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    if (enabled) setPanelCursor(Cursor.DEFAULT_CURSOR);
                }
            });

            addMouseListener(new MouseInputAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    requestFocusInWindow();
                    if (enabled && e.getButton() == MouseEvent.BUTTON3) {
                 		showPopupMenu(e.getX(), e.getY());
                 	}  
                }
            });

            addFocusListener(new FocusListener() {
            	@Override
            	public void focusGained(FocusEvent e) {
            		setLocalFocus(true);
            	}
            	@Override
            	public void focusLost(FocusEvent e) {
            	}
            });
    	}

    	private void setWrapping() {

    		// There is something wrong in the behaviour of JTextArea when lineWrap is set from the constructor
    		// with the following two lines:
    		//   textArea.setLineWrap(true);
    		//   setWrapStyleWord(true);
    		//
    		// The trick to bypass it is to set the line wrap when the text is modified.
    		// Since JTextArea.text is initially empty, that works OK.
    		getDocument().addDocumentListener(new DocumentListener() {
    			@Override
    			public void insertUpdate(DocumentEvent e) {
    				setLineWrap(true);
    				setWrapStyleWord(true);
    				
    				// Remove it since it is usefull only once
    				getDocument().removeDocumentListener(this); 
    			}
    			@Override
    			public void removeUpdate(DocumentEvent e) {}
    			@Override
    			public void changedUpdate(DocumentEvent e) {}
    		});
    	}
    }


    // JDocuElement
    //--------------------------------------------------------

    public JDocElement(boolean isText) {

    	if (isText) {
    		// A text area
            textArea = new JDocTextArea();
            add(textArea);
            this.isText = true;    // isText must be set to true only when the text area has been added
            
    		// The panel is used to move and resize the text area that it contains
    		// It is only visible in documentation mode when the text area has the local focus            
            setColor(Color.BLACK);
            setOpaque(false);
            setLayout(null);           
            setSize(100, 50);
            // Direct call since the listener is not yet set
            updateTextAreaBounds();              

        } else {
        	// A simple color panel
        	setSize(100, 50);
        	setColor(panelColor[1]);
        	setOpaque(false);
        	setFocusable(true);
        }

        initListeners();
    }
    
    public JDocElement(boolean isText, boolean dragDropCreation) {

    	this(isText);
    	this.dragDropCreation = dragDropCreation;
    }


    // Called just after the component has been assigned a parent
    // Handy to do things that can't be done at creation time when the text area lacks a parent
    public void addNotify() {

        super.addNotify();
        
        if (! (getParent() instanceof GUIDocPanel)) return;  
        
        // There is a special treatment when the panel is created by drag-drop
        if (dragDropCreation) {
        	setLocalFocus(true);   
        	// Every newly added Text component is located on top
            // (which is more natural than the default behaviour which sets them at the bottom, behind all the others)
        	if (isText) setOnTop();
        } 
    }

    // Initialization of the listeners
    private void initListeners() {

        addComponentListener(new ComponentAdapter() {
        	@Override
        	public void componentResized(ComponentEvent e) {
        		if (isText) {
        			updateTextAreaBounds();
        			repaint();
        		}
        	}
        });

    	addKeyListener(new KeyAdapter() {

    		@Override
    		public void keyPressed(KeyEvent e) {
    			switch (e.getKeyCode()) {
    				case KeyEvent.VK_DELETE:
    					delete();
    					break;
    				case KeyEvent.VK_ESCAPE:
    					setLocalFocus(false);
    			}
    		}
    	});

        addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {

                if (enabled) {                	
                	if (! isText) {
                		requestFocusInWindow();
                		if (! localFocus) setLocalFocus(true);
                	}                	
                	switch (e.getButton()) {
                		case MouseEvent.BUTTON1:
                			if (updateType == UT_MOVE) elemMoveOffset = new Dimension(e.getX(), e.getY());
                			break;
                		case MouseEvent.BUTTON3:	
                			showPopupMenu(e.getX(), e.getY());
                	}
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (enabled) {       
                	if (movedOrResized) {
                		updateUI();
                        ProjectManager.getInstance().setModified();
                        movedOrResized = false;
                	}
                    updateType = UT_NONE;                    
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (enabled) {
                	setPanelCursor(Cursor.DEFAULT_CURSOR);
                }
            }
        });

        addMouseMotionListener(new MouseInputAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (enabled && !(isText && ! enlargedPanel)) {
                    boolean left = (e.getX() <= TEXT_BORDER_WIDTH);
                    boolean right = (e.getX() >= getWidth() - TEXT_BORDER_WIDTH);
                    boolean upper = (e.getY() <= TEXT_BORDER_WIDTH);
                    boolean lower = (e.getY() >= getHeight() - TEXT_BORDER_WIDTH);

                    if (left) {
                        if (upper) {
                        	updateType = UT_LEFT_SIZING | UT_UPPER_SIZING;
                        } else if (lower) {
                        	updateType = UT_LEFT_SIZING | UT_LOWER_SIZING;
                        } else {
                        	updateType = UT_LEFT_SIZING;
                        }
                    } else if (right) {
                        if (upper) {
                        	updateType = UT_RIGHT_SIZING | UT_UPPER_SIZING;
                        } else if (lower) {
                        	updateType = UT_RIGHT_SIZING | UT_LOWER_SIZING;
                        } else {
                        	updateType = UT_RIGHT_SIZING;
                        }
                    } else if (upper) {
                    	updateType = UT_UPPER_SIZING;
                    } else if (lower) {
                    	updateType = UT_LOWER_SIZING;
                    } else {
                        updateType = UT_MOVE;
                    }

                    switch (updateType) {
                        case UT_MOVE:
                        	setPanelCursor(Cursor.MOVE_CURSOR);
                            break;
                        case UT_LEFT_SIZING:
                        	setPanelCursor(Cursor.W_RESIZE_CURSOR);
                            break;
                        case UT_RIGHT_SIZING:
                        	setPanelCursor(Cursor.E_RESIZE_CURSOR);
                            break;
                        case UT_UPPER_SIZING:
                        	setPanelCursor(Cursor.N_RESIZE_CURSOR);
                            break;
                        case UT_LOWER_SIZING:
                        	setPanelCursor(Cursor.S_RESIZE_CURSOR);
                            break;
                        case UT_LEFT_SIZING | UT_UPPER_SIZING:
                        	setPanelCursor(Cursor.NW_RESIZE_CURSOR);
                            break;
                        case UT_RIGHT_SIZING | UT_UPPER_SIZING:
                        	setPanelCursor(Cursor.NE_RESIZE_CURSOR);
                            break;
                        case UT_LEFT_SIZING | UT_LOWER_SIZING:
                        	setPanelCursor(Cursor.SW_RESIZE_CURSOR);
                            break;
                        case UT_RIGHT_SIZING | UT_LOWER_SIZING:
                        	setPanelCursor(Cursor.SE_RESIZE_CURSOR);
                            break;
                        default:
                        	setPanelCursor(Cursor.DEFAULT_CURSOR);
                    }                    
                }
            }
            @Override
            public void mouseDragged(MouseEvent e) {
            	if (SwingUtilities.isLeftMouseButton(e)) {
            		if (isText && ! enlargedPanel) return;
            		
            		boolean left  = ((updateType & UT_LEFT_SIZING) != 0);
            		boolean right = ((updateType & UT_RIGHT_SIZING) != 0);
            		boolean upper = ((updateType & UT_UPPER_SIZING) != 0);
            		boolean lower = ((updateType & UT_LOWER_SIZING) != 0);

            		int elemX = getX();
            		int elemY = getY();
            		int elemW = getWidth();
            		int elemH = getHeight();

            		int newX, newY, newW, newH;

            		if (enabled) {
            			if (updateType == UT_MOVE) {
            				
            				setLocation(e.getX() - (int) elemMoveOffset.getWidth()  + elemX,
            						    e.getY() - (int) elemMoveOffset.getHeight() + elemY);            				
            				movedOrResized = true;

            			} else if (updateType != UT_NONE) {
            				
            				int minW = (isText ? TEXT_MIN_WIDTH : RECT_MIN_WIDTH);
            				int minH = (isText ? TEXT_MIN_HEIGHT : RECT_MIN_HEIGHT);

            				if (left) {
            					newX = elemX + e.getX();
            					newW = elemW - e.getX();
            					if (newW < minW) {
            						newX = newX - (minW - newW);
            						newW = minW;
            					}
            				} else if (right) {
            					newX = elemX;
            					newW = e.getX();
            					if (newW < minW) {
            						newW = minW;
            					}
            				} else {
            					newX = elemX;
            					newW = elemW;
            				}

            				if (upper) {
            					newY = elemY + e.getY();
            					newH = elemH - e.getY();
            					if (newH < minH) {
            						newY = newY - (minH - newH);
            						newH = minH;
            					}
            				} else if (lower) {
            					newY = elemY;
            					newH = e.getY();
            					if (newH < minH) {
            						newH = minH;
            					}
            				} else {
            					newY = elemY;
            					newH = elemH;
            				}

            				setBounds(newX, newY, newW, newH);            				
            				movedOrResized = true;
            			}
            		}
            	}
            }
        });
    }   
    
    // popupmenu
    private void showPopupMenu(int x, int y) {

    	JPopupMenu menu = new JPopupMenu();

    	if (isText) {
    		// Font size
    		JMenu mFontSize = new JMenu(messages.getString("docelement_msg3"));  // Font size
    		float tafs = textArea.getFont().getSize();
    		for (int i = 0; i<fontSize.length; i++) {
    			JRadioButtonMenuItem mi = new JRadioButtonMenuItem(fontSizeText[i]);
    			mi.putClientProperty("fontSize", fontSize[i]);
    			mi.setSelected(tafs == fontSize[i]);
        		mi.addActionListener(new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
        				JMenuItem smi = (JMenuItem) e.getSource();
        				float fs = (float) smi.getClientProperty("fontSize");
        				textArea.setFont(textArea.getFont().deriveFont(fs));
        				
        				repaint();        				
        				ProjectManager.getInstance().setModified(); 
        			}
        		});
        		mFontSize.add(mi);
    		}
    		menu.add(mFontSize);

    		// Font style
    		int[] fontStyles = {Font.PLAIN, Font.ITALIC, Font.BOLD};
    		JMenu mFontStyle = new JMenu(messages.getString("docelement_msg2"));  // Font style
    		int tafst = textArea.getFont().getStyle();
    		for (int i = 0; i<fontStyles.length; i++) {
    			JRadioButtonMenuItem mi = new JRadioButtonMenuItem(fontStyleText[i]);
    			mi.putClientProperty("fontStyle", i);
    			mi.setSelected(tafst == fontStyles[i]);
        		mi.addActionListener(new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
        				JMenuItem smi = (JMenuItem) e.getSource();
        				int fs = (int) smi.getClientProperty("fontStyle");
        				textArea.setFont(textArea.getFont().deriveFont(fontStyles[fs]));
        				
        				repaint();
        				ProjectManager.getInstance().setModified(); 
        			}
        		});
        		mFontStyle.add(mi);
    		}
    		menu.add(mFontStyle);

    		// Font color
    		JDocColorSelector mFontColor = new JDocColorSelector(messages.getString("docelement_msg1"), fontColor, 3, getColor());  // Color
    		mFontColor.addActionListener(new ActionListener() {
    			public void actionPerformed(ActionEvent e) {
    				setColor(mFontColor.getColor());
    				
    				repaint();
    				ProjectManager.getInstance().setModified(); 
    			}
    		});
    		menu.add(mFontColor);

    		// Set panel in the foreground
            JMenuItem miMoveToTop = new JMenuItem(messages.getString("docelement_msg12"));  // Send to Front
            miMoveToTop.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                	setOnTop();
                	repaint();
                }
            });
            menu.add(miMoveToTop);

    	} else {
    		// Color of the Panel
    		JDocColorSelector mPanelColor = new JDocColorSelector(messages.getString("docelement_msg1"), panelColor, 3, getColor());  // Color
    		mPanelColor.addActionListener(new ActionListener() {
    			public void actionPerformed(ActionEvent e) {
    				setColor(mPanelColor.getColor());
    				repaint();
    				ProjectManager.getInstance().setModified(); 
    			}
    		});
    		menu.add(mPanelColor);

    		// Set panel in the background
            JMenuItem miMoveToBottom = new JMenuItem(messages.getString("docelement_msg13"));   // Send to Back
            miMoveToBottom.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                	setAtBottom();
                	repaint();
                }
            });
            menu.add(miMoveToBottom);
    	};       
        
        // Duplicate Panel
        JMenuItem miDuplicate = new JMenuItem(messages.getString("docelement_msg14"));  // Duplicate
        miDuplicate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	duplicate();
            }
        });
        menu.add(miDuplicate);
        
        menu.addSeparator();

        // Delete Panel
        JMenuItem miDelete = new JMenuItem(messages.getString("docelement_msg15"));  // Delete
        miDelete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                delete();
            }
        });
        menu.add(miDelete);
        
        menu.show(this, x, y);
    }
        
    // Set the component on top (textAreas are always above the rectangles)
    private void setOnTop() {
    	if (getParent().getComponentZOrder(this) == 0) return;
    	
    	getParent().setComponentZOrder(this, 0);
    	// Update the GUIContainer.docuItems list (necessary for the order to be saved in the project file)
    	GUIContainer.getInstance().setDocItemOnTop(this);
    }
    
    // Set the component at the bottom (rectangles are always behind the textAreas)
    private void setAtBottom() {
    	if (getParent().getComponentZOrder(this) == getParent().getComponentCount() - 1) return;
    	
    	getParent().setComponentZOrder(this, getParent().getComponentCount() - 1);
    	// Update the GUIContainer.docuItems list (necessary for the order to be saved in the project file)
    	GUIContainer.getInstance().setDocItemAtBottom(this);
    }
    
    private void setPanelCursor(int cursor) {
    	JMainFrame.getInstance().setCursor(Cursor.getPredefinedCursor(cursor));
    }
    
    private void duplicate() {

    	GUIContainer.getInstance().duplicateDocElement(this);    	
    }
    
    private void delete() {

    	GUIContainer.getInstance().removeDocElement(this);
        GUIContainer.getInstance().updateViewport();
        setPanelCursor(Cursor.DEFAULT_CURSOR);
    }
    
    public boolean getIsText() {

        return isText;
    }

    public String getText() {

        if (isText) return textArea.getText();
        return null;
    }

    public void setText(String text) {

        if (isText) textArea.setText(text);
    }

	public Color getColor() {
		
		return referenceColor;
	}

	public void setColor(Color color) {

		referenceColor = color;

		if (isText) {
            textArea.setForeground(color);
        } else {
        	int R = color.getRed(); R = 255 + (R-255)*8/10;
        	int G = color.getGreen(); G = 255 + (G-255)*8/10;
        	int B = color.getBlue(); B = 255 + (B-255)*8/10;
        	opaqueColor = new Color(R,G,B);
        	transparentColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 200);
        	borderColor = new Color(color.getRed()*4/5, color.getGreen()*4/5, color.getBlue()*4/5);
        }
	}
	
    public Font getFont() {

		if (isText) {
			return textArea.getFont();
        } else {
        	return null;
        }
	}

	public void setFont(Font font) {

		if (isText) {
            textArea.setFont(font);
        }
	}	

    @Override
    public void setEnabled(boolean enabled) {
    	
    	if (enabled == this.enabled) return;

        this.enabled = enabled;
        setFocusable(enabled);

        if (isText) {
            textArea.setEditable(enabled);
            textArea.setFocusable(enabled);
        }

        if (! enabled) setLocalFocus(false);
    }

    public void setLocalFocus(boolean focused) {

        if (focused == localFocus) return;

        // Remove the localFocus from any sibling
        if (focused) {
        	Component[] c = getParent().getComponents();
        	for (int i = 0; i < c.length; i++) {       
        		if ( c[i] instanceof JDocElement &&  c[i] != this) {
        			((JDocElement) c[i]).setLocalFocus(false);
        		}
        	}
        }
        localFocus = focused;
        
        if (isText) updatePanelSize();
        
        repaint();
        
        if (dragDropCreation) {
        	if (isText) delayFocusOnTextArea();
        	dragDropCreation = false;
        }        
    }
    
    public boolean getLocalFocus() {

        return localFocus;     
    }
    
    // When created with drag-drop, textArea.requestFocusInWindow() does not work when called within setLocalFocus
    // A delayed call fixes it 
    public void delayFocusOnTextArea() {

    	new java.util.Timer().schedule( 
    		new java.util.TimerTask() {
    			@Override
    			public void run() {
    				textArea.requestFocusInWindow(); 
    			}
    		}, 
    		100
    	);
    }
        
    public Rectangle getRealBounds() {
    	
    	if (!isText) {
    		return getBounds();
    	}
    	else {
    		Rectangle b = getBounds();
    		if (!localFocus) {
        		b.x = b.x + 1 - TEXT_BORDER_WIDTH;
    			b.y = b.y + 1 - TEXT_BORDER_WIDTH;
    			b.width = b.width - 2 + 2*TEXT_BORDER_WIDTH;
    			b.height = b.height - 2 + TEXT_BORDER_BOTTOM;
        	}
    		return b;
    	}    		
    }

    private void updateTextAreaBounds() {
    	
    	if (localFocus) {
    		textArea.setBounds(TEXT_BORDER_WIDTH, TEXT_BORDER_WIDTH, getBounds().width-2*TEXT_BORDER_WIDTH, getBounds().height-TEXT_BORDER_BOTTOM);
    	} else {
    		textArea.setBounds(1, 1, getBounds().width-2, getBounds().height-2);
    	}    		
    }
    
    private void updatePanelSize() {
    	
    	if (localFocus) {
    		// enlarge the panel
    		if (enlargedPanel) return;  // Just in case...
    		
    		Rectangle b = getBounds();
    		setBounds(b.x+1-TEXT_BORDER_WIDTH, b.y+1-TEXT_BORDER_WIDTH, b.width-2+2*TEXT_BORDER_WIDTH, b.height-2+TEXT_BORDER_BOTTOM);
    		
    		enlargedPanel = true;
    	} else {
    		// reduce the panel
    		if (! enlargedPanel) return;  // Just in case...		// System.out.println("!!!");
    		
    		Rectangle b = getBounds();
    		setBounds(b.x-1+TEXT_BORDER_WIDTH, b.y-1+TEXT_BORDER_WIDTH, b.width+2-2*TEXT_BORDER_WIDTH, b.height+2-TEXT_BORDER_BOTTOM);
    		
    		enlargedPanel = false;
    	}   
    	
    	updateTextAreaBounds();
    }
    
    // Called when the element is created by duplication
    public void initBounds(Rectangle r) {
    	
    	setBounds(r);
    	if (isText) updateTextAreaBounds();	
    }

    // Returns a polygon wrapping the area outside the given rectangle
    // (used to clip out the rectangle when painting)
    // if corner is not 0, the corners of the rectangle are clipped triangularly to fit the fillRoundRect
    private Polygon invertRectangle(Rectangle R, int corner) {

    	int extW = getBounds().width;
    	int extH = getBounds().height;
    	int inW = R.width;
    	int inH = R.height;
    	Polygon clip;

    	if (corner == 0) {
    		clip = new Polygon(new int[]{ 0, extW, extW,    0,   0, R.x,     R.x, R.x+inW, R.x+inW,   0 }, // x values
    						   new int[]{ 0,    0, extH, extH, R.y, R.y, R.y+inH, R.y+inH,     R.y, R.y }, // y values
    						   10);
    	} else {
    		int ct = corner;
    		int cb = corner + 1; // bottom corners clipping is 1 pixel larger
    		clip = new Polygon(new int[]{ 0, extW, extW,    0,   0, R.x+ct,    R.x,        R.x,  R.x+cb, R.x+inW-cb,    R.x+inW, R.x+inW, R.x+inW-ct,   0 }, // x values
					   		   new int[]{ 0,    0, extH, extH, R.y,    R.y, R.y+ct, R.y+inH-cb, R.y+inH,    R.y+inH, R.y+inH-cb,  R.y+ct,        R.y, R.y }, // y values
							   14);
    	}

    	return clip;
    }

    @Override
    protected void paintComponent(Graphics g) {

    	Graphics2D g2 = (Graphics2D) g;
    	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (isText) {
        	// Paint the background
        	if (enabled) {
        		if (localFocus) {
        			// Paint the part of the Panel not hidden by the textArea
        			// in order that the user can move and resize it
        			g2.setColor(panelFocusColor);
        			Shape prevClip = g2.getClip();
        			g2.setClip(invertRectangle(textArea.getBounds(), 0));
        			g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, TEXT_CORNER_SIZE, TEXT_CORNER_SIZE);
        			g2.setClip(prevClip);

        			// Draw the border
        			g2.setColor(borderFocusColor);
        			Stroke stroke = new BasicStroke(2);
        			g2.setStroke(stroke);
        			g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, TEXT_CORNER_SIZE, TEXT_CORNER_SIZE);
        		} else {        			
        			// Draw the border       
        			g2.setColor(textPanelBorderNormalColor); 
        			Stroke stroke = new BasicStroke(1);
        			g2.setStroke(stroke);
        			g2.drawRect(textArea.getX()-1, textArea.getY()-1, textArea.getWidth()+1, textArea.getHeight()+1);
        		}
        	}

        } else {
            if (enabled && localFocus) {
    			// Paint the border with a distinctive color
    			g2.setColor(panelFocusColor);
    			Shape prevClip = g2.getClip();
    			Rectangle R = getBounds();
    			R.x = 0;
    			R.y = 0;
    			R.grow(-TEXT_BORDER_WIDTH,-TEXT_BORDER_WIDTH);
    			g2.setClip(invertRectangle(R, 3));
    			g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, RECT_CORNER_SIZE, RECT_CORNER_SIZE);
    			g2.setClip(prevClip);

    			// Paint the background with the panel color
    			g2.setColor(transparentColor);
    			//g2.fillRect(R.x, R.y,R.width, R.height);
    			g2.fillRoundRect(R.x, R.y,R.width, R.height, TEXT_CORNER_SIZE, TEXT_CORNER_SIZE);

    			// Draw the border
    			g2.setColor(borderFocusColor);
    			Stroke stroke = new BasicStroke(2);
    			g2.setStroke(stroke);
    			g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, RECT_CORNER_SIZE, RECT_CORNER_SIZE);
    		} else {
    			// Paint the background
    			g2.setColor(enabled ? transparentColor : opaqueColor);
    			g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, RECT_CORNER_SIZE, RECT_CORNER_SIZE);

    			// Draw the border
    			g2.setColor(borderColor);
    			Stroke stroke = new BasicStroke(2);
    			g2.setStroke(stroke);
    			g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, RECT_CORNER_SIZE, RECT_CORNER_SIZE);
    		}
        }
    }
}