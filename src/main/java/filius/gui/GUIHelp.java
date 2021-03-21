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
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import filius.Main;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.ResourceUtil;

public class GUIHelp implements I18n {

    private JDialog jf;
    private static GUIHelp ref = null;
    private JEditorPane epHtml;
    
    
    public static GUIHelp getGUIHelp() {
        if (ref == null) ref = new GUIHelp();     
        return ref;
    }

    private GUIHelp() {
        JFrame mainFrame = JMainFrame.getInstance();
        jf = new JDialog(mainFrame, messages.getString("guihelp_msg1"), false);  // Help
        ImageIcon frameIcon = new ImageIcon(getClass().getResource("/gfx/allgemein/hilfe.png"));
        jf.setIconImage(frameIcon.getImage());

        epHtml = new JEditorPane("text/html;charset=UTF-8", null);                         // << essayer JTextPane à la place
        epHtml.setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));
        epHtml.setEditable(false);
        epHtml.setBorder(BorderFactory.createEmptyBorder());
        
        epHtml.setText(messages.getString("guihelp_msg2"));  // Cannot find the help file
        jf.getContentPane().add(new JScrollPane(epHtml), BorderLayout.CENTER);
        
        epHtml.addHyperlinkListener(new HyperlinkListener() {
        	@Override
        	public void hyperlinkUpdate(HyperlinkEvent e) {
        		
        		if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {      			
        			try {
        				//javax.swing.JOptionPane.showMessageDialog(null, e.getURL().getFile()); 
        				loadPage(e.getURL().getFile());
        			} catch (Exception ex) {
        				ex.printStackTrace();
        			}
        		}
        	}
        });
        
        loadModeMainPage(GUIMainMenu.DESIGN_MODE);
    }
    
    public void show() {    	

        int screenWidth = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        int sreenHeight = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();

        JFrame mainframe = JMainFrame.getInstance();
        int x = mainframe.getX() + mainframe.getWidth();
        int y = mainframe.getY();
        
        int width = 390;
        int height = 550;

        if (y + height > sreenHeight) y = sreenHeight - height;               
        if (x + width > screenWidth)  x = screenWidth - width;
        if (screenWidth < 1200) width = width - 50; 

        jf.setBounds(x, y, width, height);

        jf.setVisible(true);

    }

    public void loadModeMainPage(int mode) {
    	
    	String pageName = "";    	
        if (mode == GUIMainMenu.DOC_MODE)         pageName = "doc_mode.html";   
        else if (mode == GUIMainMenu.ACTION_MODE) pageName = "action_mode.html";  
        else                                      pageName = "design_mode.html";        
        
        loadPage(pageName);
    }
    
    /**
     * <b>loadPage</b> loads a help page
     * 
     * @param pageName Name of the web page including its extension
     */
    private void loadPage(String pageName) {      
        
        String langDir = messages.getString("guihelp_msg3");  // de, en or fr
                
        File file = ResourceUtil.getResourceFile("hilfe/" + langDir + "/" + pageName);
        if (file == null) return;
        
        String gfxPath = "file:" + file.getParentFile().getParentFile().getAbsolutePath() + "/gfx/";        
        if (File.separator.equals("\\"))  gfxPath = gfxPath.replace('\\', '/');
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")))) {
        	StringBuffer sb = new StringBuffer();
        	for (String line = reader.readLine(); line != null; line = reader.readLine()) {
        		sb.append(line);
        	}
        	String newText = sb.toString();
        	newText = newText.replaceAll("hilfe/gfx/", gfxPath);
        	//System.out.println(newText);
        	epHtml.read(new java.io.StringReader(newText), null);
        	epHtml.setCaretPosition(0);
        } catch (FileNotFoundException e) {
        	e.printStackTrace(Main.debug);
        } catch (IOException e) {
        	e.printStackTrace(Main.debug);
        }
    }    
}
