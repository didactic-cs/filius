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
package filius.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.MouseInputAdapter;
import filius.Main;
import filius.gui.anwendungssicht.GUIDesktopWindow;
import filius.gui.dokusicht.GUIDocItem;
import filius.gui.dokusicht.GUIDocPanel;
import filius.gui.dokusicht.GUIDocSidebar;
import filius.gui.dokusicht.JDocElement;
import filius.gui.nachrichtensicht.AbstractPacketsAnalyzerDialog;
import filius.gui.nachrichtensicht.OldPacketsAnalyzerDialog;
import filius.gui.nachrichtensicht.PacketsAnalyzerDialog;
import filius.gui.netzwerksicht.GUIDesignSidebar;
import filius.gui.netzwerksicht.GUICableItem;
import filius.gui.netzwerksicht.GUINodeItem;
import filius.gui.netzwerksicht.GUIDesignPanel;
import filius.gui.netzwerksicht.GUIPrintPanel;
import filius.gui.netzwerksicht.JCablePanel;
import filius.gui.netzwerksicht.JNodeLabel;
import filius.gui.netzwerksicht.config.JConfigPanel;
import filius.hardware.NetworkInterface;
import filius.hardware.Port;
import filius.hardware.knoten.Host;
import filius.hardware.knoten.InternetNode;
import filius.hardware.knoten.Node;
import filius.hardware.knoten.Router;
import filius.hardware.knoten.Switch;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.Information;
import filius.rahmenprogramm.ProjectManager;
import filius.rahmenprogramm.nachrichten.PacketAnalyzer;
import filius.software.system.HostOS;

@SuppressWarnings("serial")
public class GUIContainer implements Serializable, I18n {

    private static final int MIN_DESKTOP_SPACING = 10;
    private static final Integer ACTIVE_LISTENER_LAYER = 1; 
    private static final Integer INACTIVE_LISTENER_LAYER = -2;
    private static final Integer BACKGROUND_LAYER = -10; 
    public  static final int WORKAREA_WIDTH = 2000;
    public  static final int WORKAREA_HEIGHT = 1500;  
    
    private int workareaWidth;
    private int workareaHeight;

    private static GUIContainer guiContainer;
    private JMainFrame mainFrame;
    
    // The current diplay mode
    private int currentMode = GUIMainMenu.DESIGN_MODE;

    private GUIMainMenu mainMenu;    

    // Doc mode elements    
    private GUIDocPanel docPanel;
    private GUIDocSidebar docSidebar;
    private JScrollPane docSidebarScrollpane;
    private JPanel docDragPanel;
    
    /** A JDocElement used to add a doc element by drag and drop */
    private JDocElement dragDocElement;
    
    //Design mode elements
    private JLayeredPane designLayeredPane; 
    private JScrollPane designView; 
    
    private GUIDesignPanel designPanel;
    private JPanel designListenerPanel;    
    
    private GUIDesignSidebar designSidebar;
    private JScrollPane designSidebarScrollpane;
    private JBackgroundPanel designBackgroundPanel;
    private JConfigPanel designConfigPanel;
       
    /** A JNodeLabel used to add a Node by drag and drop */
    private JNodeLabel dragNode;   
    
    /** A JNodeLabel displaying a red connector numbered 1 or 2 */    
    private JNodeLabel connectingTool;    
    
    /** Invisible temporary node used while drawing a cable */
    private GUINodeItem handleEndNodeItem;
    private JNodeLabel handleEndNode;    
    
    /** Cable used to dynamically bind two nodes */
    private JCablePanel designCable;             
    
    /** Marquee while still being constructed */
    private JMarqueePanel designTempMarquee;    
    /** Marquee once constructed */
    private JMarqueePanel designMarquee;        

    // Action mode elements
    private JScrollPane actionPane;
    private JBackgroundPanel actionBackgroundPanel;    
    private List<GUIDesktopWindow> desktopWindowList = new LinkedList<GUIDesktopWindow>();   
    private AbstractPacketsAnalyzerDialog packetsAnalyzerDialog = null;
    
    // Common items
    private List<GUINodeItem> nodeItems = new LinkedList<GUINodeItem>();
    private List<GUICableItem> cableItems = new LinkedList<GUICableItem>();
    private List<GUIDocItem> docItems = new ArrayList<GUIDocItem>();

    
    private GUIContainer(int width, int height) {
    	
        workareaWidth = width;
        workareaHeight = height;

        mainFrame = JMainFrame.getInstance();
        
        Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/gfx/hardware/kabel.png"));        
        mainFrame.setIconImage(image);         
    }

    public static GUIContainer getInstance() {
    	
        return getInstance(WORKAREA_WIDTH, WORKAREA_HEIGHT);
    }

    /* Singleton class */
    public static GUIContainer getInstance(int width, int height) {
    	
        if (guiContainer == null) {
            guiContainer = new GUIContainer(width, height);
            if (guiContainer == null) {
            	Main.debug.println("ERROR (static) getInstance(): Fehler!!! ref==null");
            	return null;
            }
            // guiContainer.init();  // <<< does not work! Why?
        }
        return guiContainer;
    }
    
    // Initialization must be called just after creation (in Main.start())
    // (but can't be called from the instantiator!)
    public void init() { 
    	
    	initPanels();
    	initDocListeners();
    	initDesignListeners();
    	initActionListeners();
    }
    
    
    //-----------------------------------------------------------------------------------------
    //  GUI creation
    //-----------------------------------------------------------------------------------------

    /**
     * Die Prozedur wird erst aufgerufen wenn der Container zugewiesen wurde. Sie Initialisiert die einzelnen Panels.
     * 
     * @author Johannes Bade & Thomas Gerding
     */
    public void initPanels() {    	
    	
    	// Retrieve the main window
        Container contentPane = mainFrame.getContentPane();
        contentPane.setLayout(new BorderLayout(0, 0));
        
        // Add the main menu on top
        mainMenu = new GUIMainMenu();        
        contentPane.add(mainMenu.getMenupanel(), BorderLayout.NORTH);
        
        // Add the configuration panel at the bottom
        designConfigPanel = JConfigPanel.selectEmptyPanel();
        
        // Build the work area
        designLayeredPane = new JLayeredPane();
        designLayeredPane.setSize(workareaWidth, workareaHeight);
        Dimension D = new Dimension(workareaWidth, workareaHeight);
        designLayeredPane.setMinimumSize(D);
        designLayeredPane.setPreferredSize(D);

        designListenerPanel = new JPanel();
        designListenerPanel.setSize(workareaWidth, workareaHeight);
        designListenerPanel.setMinimumSize(D);
        designListenerPanel.setPreferredSize(D);
        designListenerPanel.setOpaque(false);
        designLayeredPane.add(designListenerPanel, ACTIVE_LISTENER_LAYER);

        /* tempDesignMarquee: area covered during mouse pressed, i.e., area with components to be selected */
        designTempMarquee = new JMarqueePanel();
        designTempMarquee.setBounds(0, 0, 0, 0);
        designTempMarquee.setBackgroundImage("gfx/allgemein/auswahl.png");
        designTempMarquee.setOpaque(false);
        designTempMarquee.setVisible(true);
        designLayeredPane.add(designTempMarquee, JLayeredPane.DRAG_LAYER);
        
        /* designMarquee: actual area covering selected objects */
        designMarquee = new JMarqueePanel();
        designMarquee.setBounds(0, 0, 0, 0);
        designMarquee.setBackgroundImage("gfx/allgemein/markierung.png");
        designMarquee.setOpaque(false);
        designMarquee.setVisible(false);
        designMarquee.setCursor(new Cursor(Cursor.MOVE_CURSOR));
        designLayeredPane.add(designMarquee, JLayeredPane.DRAG_LAYER);

        /* Connector labeled 1 or 2 used to draw cables between nodes */
        connectingTool = new JNodeLabel(false);
        connectingTool.setVisible(false);              
        designLayeredPane.add(connectingTool, JLayeredPane.DRAG_LAYER);
        
        /* Invisible node used to handle the 2nd extremity of a cable being drawn */
        handleEndNode = new JNodeLabel(false);
        handleEndNodeItem = new GUINodeItem();
        handleEndNodeItem.setNodeLabel(handleEndNode);        
        handleEndNode.setBounds(0, 0, 8, 8);  

        /* Item dragged from the designSidebar onto the work area */
        dragNode = new JNodeLabel();                                   
        dragNode.setVisible(false);
        designLayeredPane.add(dragNode, JLayeredPane.DRAG_LAYER);            

        /* scrollpane für das Mittlere Panel */
        designPanel = new GUIDesignPanel(workareaWidth, workareaHeight);   
        designLayeredPane.add(designPanel, JLayeredPane.DEFAULT_LAYER);

        designBackgroundPanel = new JBackgroundPanel();
        designBackgroundPanel.setBackgroundImage("gfx/allgemein/entwurfshg.png");
        designBackgroundPanel.setBounds(0, 0, workareaWidth, workareaHeight);

        docDragPanel = new JPanel();
        docDragPanel.setBounds(0, 0, workareaWidth, workareaHeight);
        docDragPanel.setOpaque(false);
        designLayeredPane.add(docDragPanel, JLayeredPane.DRAG_LAYER);

        docPanel = new GUIDocPanel(workareaWidth, workareaHeight);  
        designLayeredPane.add(docPanel, INACTIVE_LISTENER_LAYER);

        designView = new JScrollPane(designLayeredPane);
        designView.getVerticalScrollBar().setUnitIncrement(10);

        // Action related
        actionBackgroundPanel = new JBackgroundPanel();   
        actionBackgroundPanel.setBackgroundImage("gfx/allgemein/simulationshg.png");
        actionBackgroundPanel.setBounds(0, 0, workareaWidth, workareaHeight);
        
        actionPane = new JScrollPane();
        actionPane.getVerticalScrollBar().setUnitIncrement(10);
        
        /* The design sidebar is enclosed in scrollbars */
        designSidebar = GUIDesignSidebar.getGUIDesignSidebar();  
        designSidebarScrollpane = new JScrollPane(designSidebar.getButtonPanel());        
        designSidebarScrollpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        if (Information.isLowResolution()) {
            designSidebarScrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            designSidebarScrollpane.getVerticalScrollBar().setUnitIncrement(10);
        } else {
            designSidebarScrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        }

        /* The documentation sidebar is enclosed in scrollbars */
        docSidebar = GUIDocSidebar.getInstance();
        docSidebarScrollpane = new JScrollPane(docSidebar.getButtonPanel());
        docSidebarScrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        
        /* Create the packets analyzer dialog window */
        initPacketsAnalyzerDialog();
        
        /* Start in design mode */
        mainFrame.setVisible(true);
        setCurrentMode(GUIMainMenu.DESIGN_MODE);
    }
    
    
    //-----------------------------------------------------------------------------------------
    //  Event listeners
    //-----------------------------------------------------------------------------------------    
    
    public void initDocListeners() {       

        docSidebarScrollpane.addMouseListener(new MouseInputAdapter() {
        	
            public void mousePressed(MouseEvent e) {
                JNodeLabel button = docSidebar.findButtonAt(e.getX(), e.getY());
                if (button != null) {
                    if (GUIDocSidebar.TYPE_RECTANGLE.equals(button.getType())) {
                        dragDocElement = new JDocElement(false, true);
                    } else if (GUIDocSidebar.TYPE_TEXTFIELD.equals(button.getType())) {
                        dragDocElement = new JDocElement(true, true);
                    }              
                    dragDocElement.setLocation(e.getX() - designSidebarScrollpane.getWidth() + getXOffset(),
                                                 e.getY() + getYOffset());
                    docDragPanel.add(dragDocElement);
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (e.getX() > docSidebarScrollpane.getWidth() && dragDocElement != null) {
                    docItems.add(getTopDocPanelIndex(), GUIDocItem.createDocItem(dragDocElement));
                    ProjectManager.getInstance().setModified();
                    docDragPanel.remove(dragDocElement);
                    docPanel.add(dragDocElement);                   
                    dragDocElement.requestFocusInWindow();
                    updateViewport();
                } else if (dragDocElement != null) {
                    docDragPanel.remove(dragDocElement);
                    docDragPanel.updateUI();
                }
                dragDocElement = null;
            }
        });

        docSidebarScrollpane.addMouseMotionListener(new MouseInputAdapter() {
        	
            public void mouseDragged(MouseEvent e) {
                if (dragDocElement != null) {
                    dragDocElement.setLocation(
                    		e.getX() + getXOffset() - dragDocElement.getWidth()/2 - designSidebarScrollpane.getWidth(),
                            e.getY() + getYOffset() - dragDocElement.getHeight()/2);
                }
            }
        });     
    }
    
    public void initDesignListeners() {    

        /*
         * Wird auf ein Item der Sidebar geklickt, so wird ein neues Vorschau-Label mit dem entsprechenden Icon
         * erstellt.
         * 
         * Wird die Maus auf dem Entwurfspanel losgelassen, während ein Item gedragged wird, so wird eine neue
         * Komponente erstellt.
         */
        designSidebarScrollpane.addMouseListener(new MouseInputAdapter() {
        	
            public void mousePressed(MouseEvent e) {

                JNodeLabel button = designSidebar.findButtonAt(e.getX(), e.getY() + designSidebarScrollpane.getVerticalScrollBar().getValue());
                if (button != null) {
                    updateDragNode(button.getType(), e.getX() + getXOffset() - designSidebarScrollpane.getWidth(), e.getY() + getYOffset());
                    // The exact location depends on the size of the dragDropNode which is only 
                    // determined in method updateDragDropNode. Hence the call below. 
                    dragNode.setLocation(dragNode.getX() - dragNode.getWidth()/2, dragNode.getY() - dragNode.getHeight()/2); 
                    GUIEvents.getInstance().resetAndHideCablePreview();                    
                }
            }

            public void mouseReleased(MouseEvent e) {
            	
            	int xPosMainArea = e.getX() - dragNode.getWidth()/2 - designSidebarScrollpane.getWidth();
                int yPosMainArea = e.getY() - (dragNode.getHeight())/2;  
                
                if (dragNode.isVisible() && xPosMainArea >= 0 && xPosMainArea <= designView.getWidth() &&
                                            yPosMainArea >= 0 && yPosMainArea <= designView.getHeight()) {
                	// Create a new node
                    addNode(dragNode.getType(), xPosMainArea, yPosMainArea);
                }
                dragNode.setVisible(false);
            }
        });

        /*
         * Sofern die Drag & Drop Vorschau sichtbar ist, wird beim draggen der Maus die entsprechende Vorschau auf die
         * Mausposition verschoben.
         */
        designSidebarScrollpane.addMouseMotionListener(new MouseInputAdapter() {
        	
            public void mouseDragged(MouseEvent e) {
                if (dragNode.isVisible()) {
                    dragNode.setLocation(e.getX() + getXOffset() - dragNode.getWidth()/2 - designSidebarScrollpane.getWidth(),
                                         e.getY() + getYOffset() - dragNode.getHeight()/2); 
                }
            }
        });        
        
        /*
         * Erzeugen und transformieren des Auswahlrahmens, und der sich darin befindenden Objekte.
         */
        
        designListenerPanel.addMouseListener(new MouseInputAdapter() {
        	
            public void mouseReleased(MouseEvent e) {
                if (currentMode == GUIMainMenu.DESIGN_MODE) {
                    GUIEvents.getInstance().mouseReleased();
                }
            }

            public void mousePressed(MouseEvent e) {
                if (currentMode == GUIMainMenu.DESIGN_MODE) {
                    GUIEvents.getInstance().mousePressed(e);
                }
            }
        });
              
        designListenerPanel.addMouseMotionListener(new MouseInputAdapter() {
        	
            public void mouseDragged(MouseEvent e) {
            	
                if (currentMode == GUIMainMenu.DESIGN_MODE) {
                    GUIEvents.getInstance().mouseDragged(e);
                }
            }
            
            public void mouseMoved(MouseEvent e) {
            	
                if (connectingTool.isVisible()) updateConnectingTool(e.getX(), e.getY());               
            }
        });
    }

    public void initActionListeners() { 
    	
    	// Should have been simulationBackgroundPanel.addMouseListener()
    	// but docPanel transparently covers simulationBackgroundPanel
    	docPanel.addMouseListener(new MouseInputAdapter() {

    		public void mouseClicked(MouseEvent e) {
    			if (currentMode != GUIMainMenu.ACTION_MODE) return;
    				
    			if (e.getClickCount() == 2) {
    				JFrameList.getInstance().putMasterInTheBackground();
    			}    		
    		}
    		
    		public void mouseReleased(MouseEvent e) {
    			if (currentMode != GUIMainMenu.ACTION_MODE) return;

    			if (e.getButton() == MouseEvent.BUTTON3) {
    				GUIEvents.getInstance().actionModeContextMenu(e.getX(), e.getY());     
    			}   
            }
    	});      
    }
    
    
    //-----------------------------------------------------------------------------------------
    //  Filius working mode
    //-----------------------------------------------------------------------------------------
        
    public int getCurrentMode() {
        return currentMode;
    }

    public void setCurrentMode(int currentMode) {
        this.currentMode = currentMode;

        for (Component background : designLayeredPane.getComponentsInLayer(BACKGROUND_LAYER)) {
            designLayeredPane.remove(background);
        }
        if (currentMode == GUIMainMenu.DESIGN_MODE) {
        	JFrameList.getInstance().hideAll(); 
            designView.getVerticalScrollBar().setValue(actionPane.getVerticalScrollBar().getValue());
            designView.getHorizontalScrollBar().setValue(actionPane.getHorizontalScrollBar().getValue());
            mainFrame.removeFromContentPane(docSidebarScrollpane);
            mainFrame.addToContentPane(designSidebarScrollpane, BorderLayout.WEST);
            mainFrame.removeFromContentPane(actionPane);
            designLayeredPane.setLayer(docPanel, INACTIVE_LISTENER_LAYER);
            designLayeredPane.setLayer(designListenerPanel, ACTIVE_LISTENER_LAYER);
            designLayeredPane.add(designBackgroundPanel, BACKGROUND_LAYER);
            designView.setViewportView(designLayeredPane);
            mainFrame.addToContentPane(designView, BorderLayout.CENTER);
            mainFrame.addToContentPane(designConfigPanel, BorderLayout.SOUTH);
            docDragPanel.setVisible(false);
            designSidebarScrollpane.updateUI();
            designView.updateUI();
            designConfigPanel.updateUI();
            
        } else if (currentMode == GUIMainMenu.DOC_MODE) {
        	JFrameList.getInstance().hideAll(); 
            designView.getVerticalScrollBar().setValue(actionPane.getVerticalScrollBar().getValue());
            designView.getHorizontalScrollBar().setValue(actionPane.getHorizontalScrollBar().getValue());
            mainFrame.removeFromContentPane(designSidebarScrollpane);
            mainFrame.addToContentPane(docSidebarScrollpane, BorderLayout.WEST);
            mainFrame.removeFromContentPane(actionPane);
            designLayeredPane.add(designBackgroundPanel, BACKGROUND_LAYER);
            designLayeredPane.setLayer(docPanel, ACTIVE_LISTENER_LAYER);
            designLayeredPane.setLayer(designListenerPanel, INACTIVE_LISTENER_LAYER);
            designView.setViewportView(designLayeredPane);
            designTempMarquee.setVisible(false);
            designMarquee.setVisible(false);
            docDragPanel.setVisible(true);
            mainFrame.addToContentPane(designView, BorderLayout.CENTER);
            mainFrame.removeFromContentPane(designConfigPanel);
            docSidebarScrollpane.updateUI();
            designView.updateUI();
            
        } else if (currentMode == GUIMainMenu.ACTION_MODE) {
        	JFrameList.getInstance().restoreAll();  
            actionPane.getVerticalScrollBar().setValue(designView.getVerticalScrollBar().getValue());
            actionPane.getHorizontalScrollBar().setValue(designView.getHorizontalScrollBar().getValue());
            designLayeredPane.setLayer(docPanel, INACTIVE_LISTENER_LAYER);
            designLayeredPane.setLayer(designListenerPanel, INACTIVE_LISTENER_LAYER);
            designLayeredPane.add(actionBackgroundPanel, BACKGROUND_LAYER);
            actionPane.setViewportView(designLayeredPane);
            mainFrame.removeFromContentPane(docSidebarScrollpane);
            mainFrame.removeFromContentPane(designSidebarScrollpane);
            designTempMarquee.setVisible(false);
            designMarquee.setVisible(false);
            docDragPanel.setVisible(false);
            mainFrame.removeFromContentPane(designView);
            mainFrame.addToContentPane(actionPane, BorderLayout.CENTER);
            mainFrame.removeFromContentPane(designConfigPanel);
            actionPane.updateUI();
        }
        GUIEvents.getInstance().resetAndHideCablePreview();
        mainFrame.invalidate();
        mainFrame.validate();
        updateViewport();
    }
    
    
    //-----------------------------------------------------------------------------------------
    //  Packets analyzer dialog 
    //-----------------------------------------------------------------------------------------      

    private void initPacketsAnalyzerDialog() {
    
    	if (Information.getInstance().isOldPacketsAnalyzerDialog()) {
    		packetsAnalyzerDialog = OldPacketsAnalyzerDialog.getInstance(mainFrame);    		
    	} else {
    		packetsAnalyzerDialog = PacketsAnalyzerDialog.getInstance(mainFrame);
    	}
    	packetsAnalyzerDialog.setVisible(false);
    }
    
    public AbstractPacketsAnalyzerDialog getPacketsAnalyzerDialog() {
     
        return packetsAnalyzerDialog;
    }
    
    public void displayInPacketsAnalyzerDialog(GUINodeItem nodeItem, String macAddress) {    	  
        
        if (nodeItem.getNode() instanceof InternetNode) {
            packetsAnalyzerDialog.addTable(((InternetNode) nodeItem.getNode()).getOS(), macAddress);
            packetsAnalyzerDialog.setVisible(true);
        }
    }
    
    
    //-----------------------------------------------------------------------------------------
    //  Getters
    //-----------------------------------------------------------------------------------------    
    
    public int getWidth() {
        return workareaWidth;
    }

    public int getHeight() {
        return workareaHeight;
    }
    
    public int getXOffset() {
        if (currentMode == GUIMainMenu.ACTION_MODE) {
            return actionPane.getHorizontalScrollBar().getValue();
        }
        return designView.getHorizontalScrollBar().getValue();
    }

    public int getYOffset() {
        if (currentMode == GUIMainMenu.ACTION_MODE) {
            return actionPane.getVerticalScrollBar().getValue();
        }
        return designView.getVerticalScrollBar().getValue();
    }
    
    public JNodeLabel getDragNode() {
    	
        return dragNode;
    }
    
    public JScrollPane getScrollPane() {
        return designView;
    }

    public JScrollPane getSidebarScrollpane() {
        if (currentMode == GUIMainMenu.DESIGN_MODE) {
            return designSidebarScrollpane;
        }
        return docSidebarScrollpane;
    }

    public GUIDesignPanel getDesignPanel() {
        return designPanel;
    }

    public JComponent getActionPane() {
        return actionPane;
    }

    public GUIDesignSidebar getDesignSidebar() {
        return designSidebar;
    }

    public GUIMainMenu getMainMenu() {
        return mainMenu;
    }
    
    
    //-----------------------------------------------------------------------------------------
    //  Node, cable, and marquee manipulation
    //-----------------------------------------------------------------------------------------    
    
    public List<GUINodeItem> getNodeItems() {
    	
        return nodeItems;
    }

    public void removeNodeItem(GUINodeItem item) {
    	
        nodeItems.remove(item);
        Node node = item.getNode();
        if (node instanceof InternetNode) {
            for (NetworkInterface nic : ((InternetNode) node).getNICList()) {
                PacketAnalyzer.getInstance().removeID(nic.getMac());
            }
        }
    }
    
    public void unselectNodes() {

    	GUINodeItem nodeItem;
    	
    	ListIterator<GUINodeItem> it = nodeItems.listIterator();
    	while (it.hasNext()) {
    		nodeItem = (GUINodeItem) it.next();
    		nodeItem.getNodeLabel().setSelected(false);
    	}
    }
    
    /**
     * <b>addNode</b> creates a new node and adds it to the designPanel.   
     * 
     * @author Johannes Bade & Thomas Gerding   
     *            
     * @param nodeType
     * @param x
     * @param y
     * @return boolean
     */
    private boolean addNode(String nodeType, int x, int y) {
    	           
        ProjectManager.getInstance().setModified();

        // Unselect the nodes and hide the marquee
        unselectNodes();
        designMarquee.setVisible(false);

        // Create the GUINodeItem, its Node and JNodeLabel based on the type
        
        GUINodeItem nodeItem = new GUINodeItem(nodeType, x + designView.getHorizontalScrollBar().getValue(),
                                                         y + designView.getVerticalScrollBar().getValue());
        if (nodeItem.getNode() == null) return false;
        
        // Latest added component is on top 
        nodeItems.add(0, nodeItem);   
        designPanel.add(nodeItem.getNodeLabel(), 0);        
     
        //javax.swing.JOptionPane.showMessageDialog(null,ord); 
        
        updateViewport();
        
        return true;
    }
    
    /**
     * <b>getRouterPortCount</b> displays a dialog box asking for 
     * the number of interfaces required for the router.<br>
     * 
     * @return An integer between 2 and 8. Default value is 2 when the dialog is escaped.
     */
    public int getRouterPortCount() {
    	
    	Object[] possibleValues = { "2", "3", "4", "5", "6", "7", "8" };
        Object selectedValue = JOptionPane.showInputDialog(JMainFrame.getInstance(),
                messages.getString("guicontainer_msg1"), messages.getString("guicontainer_msg2"),
                JOptionPane.INFORMATION_MESSAGE, null, possibleValues, possibleValues[0]);
        
        if (selectedValue != null) return Integer.parseInt((String) selectedValue);
        else                       return 2;
    }
    
    /**
     * Remove the given nodeItem
     */
    public void removeItem(GUINodeItem nodeItem) {
    	
    	if (nodeItem == null) return;
    	
    	// Hide the configuration panel
        setConfigPanel(null);
        
        // Remove the cables connected to the node
        removeCablesConnectedTo(nodeItem);
        
        // Remove the simulation mode's elements related to the item
        if (nodeItem.getNode() instanceof Host) {
        	// Remove the desktop's JFrame
        	removeDesktopWindow(nodeItem);
        	
        	// Remove the table in ExchangeDialog        	
        	String mac = ((Host)nodeItem.getNode()).getMac();
        	getPacketsAnalyzerDialog().removeTable(mac, null);
        	
        } else if (nodeItem.getNode() instanceof Switch) {
        	// Remove the SAT table's JFrame
        	SatViewerControl.getInstance().removeViewer((Switch)nodeItem.getNode());
        	
        } else if (nodeItem.getNode() instanceof Router) {
        	// Remove the tables in ExchangeDialog
        	List<String> macs = ((Router)nodeItem.getNode()).getMACList();
        	for (String mac: macs) getPacketsAnalyzerDialog().removeTable(mac, null);
        }
        
        getDesignPanel().remove(nodeItem.getNodeLabel());
        removeNodeItem(nodeItem);
        getDesignPanel().updateUI();
        updateViewport();
    }   
    
    /**
     * <b>removeCableItem</b> remove a cableitem and the attached Cable and JCablePanel
     * 
     * @param cableItem GUICableItem which must be removed
     */
    public void removeCableItem(GUICableItem cableItem) {
    	
    	if (cableItem == null) return;
    	
    	cableItem.getCable().disconnect();        
    	designPanel.remove(cableItem.getCablePanel());
        cableItems.remove(cableItem);
        updateViewport();
    }
    
    /**
     * <b>removeCablesConnectedTo</b> remove all the cableItems connected to the given nodeItem
     * 
     * @param nodeItem GUINodeItem from which the cables must be removed
     */
    public void removeCablesConnectedTo(GUINodeItem nodeItem) {          

        // Remove the cables connected to the node
        LinkedList<Port> ports = nodeItem.getNode().getPortList();
        for (Port port: ports) {
        	if (port.isConnected()) {
        		removeCableItem(port.getCable().getCableItem());
        	}
        }          
    }
    
    /**
     * Löscht alle Elemente der Item- und Kabelliste und frischt den Viewport auf. Dies dient dem Reset vor dem Laden
     * oder beim Erstellen eines neuen Projekts.
     * 
     * @author Johannes Bade & Thomas Gerding
     */
    public void clearAllItems() {
    	
        nodeItems.clear();
        cableItems.clear();
        docItems.clear();
        PacketAnalyzer.getInstance().reset();
        updateViewport();
    }

    private void updateDragNode(String hardwareType, int x, int y) { 	
        Main.debug.println("GUIContainer: die Komponenten-Vorschau wird erstellt.");       

        dragNode.setType(hardwareType);
        String iconFile = "/" + GUIDesignSidebar.iconFilesByHardware(hardwareType);
        dragNode.setIcon(new ImageIcon(getClass().getResource(iconFile)));
        dragNode.setBounds(x, y, dragNode.getWidth(), dragNode.getHeight());
        dragNode.setVisible(true);
    }
    
    public void updateViewport() {
        Main.debug.println("INVOKED (" + hashCode() + ") " + getClass() + " (GUIContainer), updateViewport()");
        
        designPanel.updateViewport(nodeItems, cableItems);
        designPanel.updateUI();
        
        docPanel.updateViewport(docItems, currentMode == GUIMainMenu.DOC_MODE);
        docPanel.updateUI();
    }

    /**
     * Geht die Liste der Kabel durch und ruft bei diesen updateBounds() auf. So werden die Kabel neu gezeichnet.
     * 
     * @author Thomas Gerding & Johannes Bade
     */
    public void updateCables() {
        Main.debug.println("INVOKED (" + hashCode() + ") " + getClass() + " (GUIContainer), updateCables()");
        ListIterator<GUICableItem> it = cableItems.listIterator();
        while (it.hasNext()) {
            GUICableItem tempCable = (GUICableItem) it.next();
            tempCable.getCablePanel().updateBounds();
        }
    }   

    public JNodeLabel getConnectingTool() {
        return connectingTool;
    }    
    
    public void setConnectingToolIcon1() {
    	connectingTool.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/ziel1.png")));
    }  
    
    public void setConnectingToolIcon2() {
    	connectingTool.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/ziel2.png")));
    }    
    
    public GUINodeItem getHandleEndNodeItem() {
        return handleEndNodeItem;
    } 
    
    public void setCurrentDesignCable(JCablePanel designCable, int x, int y) {
    	
        this.designCable = designCable;
        
        if (designCable != null) {
    		
    		// Move the invisible handle node 
    		handleEndNode.setLocation(x, y);   
    		
    		// Let the cable follow its handle
    		designCable.updateBounds();
    	}
    }
    
    // Called on mouse move in design mode while a cable is being drawn
    private void updateConnectingTool(int x, int y) {   
    	
    	// Let the connecting tool follow the mouse pointer
    	connectingTool.setLocation(x, y);   
    	
    	if (designCable != null) {
    		
    		// Move the invisible handle node 
    		handleEndNode.setLocation(x, y);   
    		
    		// Let the cable follow its handle
    		designCable.updateBounds();
    	}
    }    

    public List<GUICableItem> getCableList() {
        return cableItems;
    }

    public void setCableList(List<GUICableItem> cablelist) {
        this.cableItems = cablelist;
    }
    
    //-----------------------------------------------------------------------------------------
    //  Marquee methods
    //-----------------------------------------------------------------------------------------

    public JMarqueePanel getTempMarquee() {
    	
        return designTempMarquee;
    }    
                                
    public JMarqueePanel getMarquee() {
    	
        return designMarquee;
    }

    public boolean isMarqueeVisible() {
    	
        return designMarquee.isVisible();
    }

    public void moveMarquee(int incX, int incY, List<GUINodeItem> markedlist) {
    	
    	// Make sure that the marquee remains within the work area
        int newX = designMarquee.getX() + incX;
        if (newX + designMarquee.getWidth() >= workareaWidth || newX < 0) {
            incX = 0;
        }
        int newY = designMarquee.getY() + incY;
        if (newY + designMarquee.getHeight() >= workareaHeight || newY < 0) {
            incY = 0;
        }
        
        // Move the marquee
        designMarquee.setLocation(designMarquee.getX() + incX, designMarquee.getY() + incY);

        // Move the nodes
        for (GUINodeItem nodeItem : markedlist) {
            JNodeLabel nodeLabel = nodeItem.getNodeLabel();
            nodeLabel.setLocation(nodeLabel.getX() + incX, nodeLabel.getY() + incY);
        }
        
        // Redraw the cables
        updateCables();
    }
   
    
    //-----------------------------------------------------------------------------------------
    //  Documentation items
    //-----------------------------------------------------------------------------------------
   
    public List<GUIDocItem> getDocItems() {
    	
    	docPanel.removeElementsFocus();  // Necessary for the size of the selected textArea (if any) to be correctly saved
        return docItems;
    }   
    
    private GUIDocItem getDocItem(JDocElement elem) {
    	
    	for (GUIDocItem item : docItems) {
            if (item.asDocElement().equals(elem)) return item;
        }
        return null;
    }
    
    private int getTopDocPanelIndex() {
    	
    	for (int i=0; i<docPanel.getComponentCount(); i++) {
    		JDocElement elem = (JDocElement) docPanel.getComponent(i);
            if (!elem.getIsText()) return i;
        }
        return 0;
    }
    
    private JDocElement getSelectedDocElement() {
    	
    	for (int i=0; i<docPanel.getComponentCount(); i++) {
    		JDocElement elem = (JDocElement) docPanel.getComponent(i);
            if (elem.getLocalFocus()) return elem;
        }
        return null;
    }
    
    public void duplicateDocElement(JDocElement elem) {
    	
    	if (elem == null) elem = getSelectedDocElement();
    	if (elem == null) return;
    
    	boolean isText = elem.getIsText();
    	
    	JDocElement newElem = new JDocElement(isText, true);  
    	int elemIndex = docItems.indexOf(getDocItem(elem));
    	docItems.add(elemIndex, GUIDocItem.createDocItem(newElem));
    	docPanel.add(newElem);
    	
    	
    	Rectangle r = elem.getRealBounds();
    	r.x += 20;
    	r.y += 20;
    	newElem.initBounds(r);
    	
    	newElem.setColor(elem.getColor());
    	if (isText) {
    		newElem.setText(elem.getText());
    		newElem.setFont(elem.getFont());
    	}       	
    	
    	newElem.setLocalFocus(true);  
    	    
    	updateViewport();
    	
    	ProjectManager.getInstance().setModified(); 
    }   

    public void removeDocElement(JDocElement elem) {
    	
        for (GUIDocItem item : docItems) {
            if (item.asDocElement().equals(elem)) {
                docItems.remove(item);
                
                ProjectManager.getInstance().setModified(); 
                break;
            }
        }
    }    

    public void setDocItemOnTop(JDocElement elem) {
    	for (GUIDocItem item : docItems) {
            if (item.asDocElement().equals(elem)) {
                docItems.remove(item);
                docItems.add(0, item);
                
                ProjectManager.getInstance().setModified(); 
                break;
            }
        }
    }
    
    public void setDocItemAtBottom(JDocElement elem) {
    	for (GUIDocItem item : docItems) {
            if (item.asDocElement().equals(elem)) {
                docItems.remove(item);
                docItems.add(item);
                
                ProjectManager.getInstance().setModified(); 
                break;
            }
        }
    }
    
    
    //-----------------------------------------------------------------------------------------
    //  Desktops methods
    //-----------------------------------------------------------------------------------------

    public void showDesktop(GUINodeItem hardwareItem) {

        if (hardwareItem == null) return;
        if (!(hardwareItem.getNode() instanceof Host)) return;
        	
        GUIDesktopWindow desktop = null;
        boolean fertig = false;

        HostOS nodeOS = ((Host) hardwareItem.getNode()).getOS();

        ListIterator<GUIDesktopWindow> it = desktopWindowList.listIterator();
        while (!fertig && it.hasNext()) {
        	desktop = it.next();
        	if (nodeOS == desktop.getOS()) {
        		desktop.setVisible(true);
        		fertig = true;
        	}
        }

        if (!fertig) {
        	desktop = new GUIDesktopWindow(nodeOS);
        	setDesktopPos(desktop);
        	desktop.setVisible(true);
        	desktop.toFront();

        	fertig = true;
        }

        if (desktop != null)  desktopWindowList.add(desktop);
    }

    private void setDesktopPos(GUIDesktopWindow tmpDesktop) {
    	
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension desktopSize = tmpDesktop.getSize();
        int numberOfDesktopsPerRow = (int) (screenSize.getWidth() / desktopSize.getWidth());
        int numberOfDesktopsPerColumn = (int) (screenSize.getHeight() / desktopSize.getHeight());
        int totalNumberOfDesktops = numberOfDesktopsPerRow * numberOfDesktopsPerColumn;
        int xPos = MIN_DESKTOP_SPACING;
        int yPos = MIN_DESKTOP_SPACING;
        if (desktopWindowList.size() < totalNumberOfDesktops
                && !Information.getDesktopWindowMode().equals(GUIDesktopWindow.Mode.STACK)) {
            if (Information.getDesktopWindowMode().equals(GUIDesktopWindow.Mode.COLUMN)) {
                xPos = MIN_DESKTOP_SPACING
                        + (desktopWindowList.size() / numberOfDesktopsPerColumn) * (int) desktopSize.getWidth();
                yPos = MIN_DESKTOP_SPACING
                        + (desktopWindowList.size() % numberOfDesktopsPerColumn) * (int) desktopSize.getHeight();
            } else {
                xPos = MIN_DESKTOP_SPACING
                        + (desktopWindowList.size() % numberOfDesktopsPerRow) * (int) desktopSize.getWidth();
                yPos = MIN_DESKTOP_SPACING
                        + (desktopWindowList.size() / numberOfDesktopsPerRow) * (int) desktopSize.getHeight();
            }
        } else {
            int overlappingDesktops = Information.getDesktopWindowMode().equals(GUIDesktopWindow.Mode.STACK)
                    ? desktopWindowList.size()
                    : desktopWindowList.size() - totalNumberOfDesktops;
            xPos = (overlappingDesktops + 1) * 20;
            yPos = (overlappingDesktops + 1) * 20;
            if (xPos + desktopSize.getWidth() > screenSize.getWidth()) {
                xPos = MIN_DESKTOP_SPACING;
            }
            if (yPos + desktopSize.getHeight() > screenSize.getHeight()) {
                yPos = MIN_DESKTOP_SPACING;
            }
        }
        tmpDesktop.setBounds(xPos, yPos, tmpDesktop.getWidth(), tmpDesktop.getHeight());
    }

    public void addDesktopWindow(GUINodeItem hardwareItem) {
    	
    	if (hardwareItem == null || !(hardwareItem.getNode() instanceof Host)) return;
        
        HostOS bs = (HostOS) ((Host) hardwareItem.getNode()).getSystemSoftware();
        GUIDesktopWindow tmpDesktop = new GUIDesktopWindow(bs);
        desktopWindowList.add(tmpDesktop);      
    }
    
    public void removeDesktopWindow(GUINodeItem hardwareItem) {
    	
    	if (hardwareItem == null || !(hardwareItem.getNode() instanceof Host)) return;
    	
    	HostOS bs = (HostOS)((Host) hardwareItem.getNode()).getSystemSoftware();
    	
    	for (GUIDesktopWindow desktop: desktopWindowList) {
    		if (desktop.getOS() == bs) {
    			desktop.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    	        desktop.dispatchEvent(new WindowEvent(desktop, WindowEvent.WINDOW_CLOSING));
    	        desktopWindowList.remove(desktop);
    	        return;
    		}
    	}
    }


    //-----------------------------------------------------------------------------------------
    //  Configuration panel
    //-----------------------------------------------------------------------------------------    

    public JConfigPanel getConfigPanel() {

    	return designConfigPanel;
    }

    public void setConfigPanel(Object item) {
    	
        boolean maximized = false;

        if (designConfigPanel != null) {
            // do actions required prior to getting unselected (i.e. postprocessing)
            designConfigPanel.doUnselectAction();
            maximized = designConfigPanel.isMaximized();
            JMainFrame.getInstance().removeFromContentPane(designConfigPanel);
        }

        if (item == null) {
            designConfigPanel = JConfigPanel.selectEmptyPanel();
        } else {
        	if (item instanceof GUINodeItem) {
        		designConfigPanel = JConfigPanel.select(((GUINodeItem)item).getNode());
        	} else if (item instanceof GUICableItem) {
        		designConfigPanel = JConfigPanel.select(((GUICableItem)item).getCable());
        	} 
            
        }
        JMainFrame.getInstance().addToContentPane(designConfigPanel, BorderLayout.SOUTH);
        designConfigPanel.updateDisplayedValues();
        designConfigPanel.updateUI();
        if (item == null || !maximized)  designConfigPanel.minimize();
        else                             designConfigPanel.maximize();      
    }
    
    //------------------------------------------------------------------------------------------------
    // Closing
    //------------------------------------------------------------------------------------------------

    // Called when Filius is about to close
    public void prepareForClosing() {
    	
    	// Last chance to save the last modified parameter in the configuration panel if any   
    	designConfigPanel.saveChanges();
    	
    	// Switch to design mode 
    	mainMenu.selectMode(GUIMainMenu.DESIGN_MODE);   
    }
    
    //------------------------------------------------------------------------------------------------
    // Export related method (The main export methods are located in class ProjectExport)
    //------------------------------------------------------------------------------------------------

    public GUIPrintPanel prepareExportPanel() {
    	
        GUIPrintPanel printPanel = new GUIPrintPanel(workareaWidth, workareaHeight, ProjectManager.getInstance().getPath());
        printPanel.updateViewport(nodeItems, cableItems, docItems);
        return printPanel;
    }
}
