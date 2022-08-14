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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Observable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.FormSubmitEvent;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;

import filius.Main;
import filius.rahmenprogramm.EntryValidator;
import filius.rahmenprogramm.Information;
import filius.rahmenprogramm.ProjectManager;
import filius.software.system.FiliusFileNode;
import filius.software.www.HTTPNachricht;
import filius.software.www.WebBrowser;

@SuppressWarnings("serial")
public class GUIApplicationWebBrowserWindow extends GUIApplicationWindow {

	private JPanel browserPanel;
	private JTextField tfURL;
	private JEditorPane htmlViewer;
	private JButton goButton;
	

	public GUIApplicationWebBrowserWindow(final GUIDesktopPanel desktop, String appName) {
		
		super(desktop, appName);	
		
		initComponents();		
		initListeners();
	}
	
	private void initComponents() {
		
		browserPanel = new JPanel(new BorderLayout());
		getContentPane().add(browserPanel);

		Box topBox = Box.createHorizontalBox();

		tfURL = new JTextField("http://");
		tfURL.setVisible(true);

		topBox.add(tfURL);
		topBox.add(Box.createHorizontalStrut(5)); // Platz zw. urlFeld und senden

		goButton = new JButton(messages.getString("webbrowser_msg2"));
		topBox.add(goButton);
		topBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		browserPanel.add(topBox, BorderLayout.NORTH);

		Box middleBox = Box.createHorizontalBox();
		middleBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		/* ActionListener */
		ActionListener al = new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				loadWebpage(createURL(tfURL.getText()), null);
			}
		};
		goButton.addActionListener(al);

		/* KeyListener */
		tfURL.addKeyListener(new KeyListener() {
			
			public void keyPressed(KeyEvent e) {

				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			
					loadWebpage(createURL(tfURL.getText()), null);
				}
			}

			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {}
		});

		HTMLEditorKit ek = new HTMLEditorKit(); // Braucht er für SubmitEvent!
		ek.setAutoFormSubmission(false); // muss!

		htmlViewer = new JEditorPane();
		htmlViewer.setEditorKit(ek);
		htmlViewer.setContentType("text/html"); // text/html muss bleiben
		// wegen folgendem Quelltext:
		htmlViewer.setText("<html><head><base href=\"file:bilder\"></head><body margin=\"0\">"
		        + "<center><img src=\"browser_waterwolf_logo.png\" align=\"top\"></center>" //+ "</font>"
		        + "</body></html>");

		// filius.rahmenprogramm.SzenarioVerwaltung.kopiereDatei(Information
		// .getInformation().getProgrammPfad()
		// + "gfx/desktop/browser_waterwolf_logo.png", Information
		// .getInformation().getTempPfad()
		// + "browser_waterwolf_logo.png");
		ProjectManager.saveStream(getClass().getResourceAsStream("/gfx/desktop/browser_waterwolf_logo.png"), 
				                  Information.getInstance().getTempPath() + "browser_waterwolf_logo.png");
		try {
			((HTMLDocument) htmlViewer.getDocument()).setBase(new URL("file:" + Information.getInstance().getTempPath()));
		} catch (MalformedURLException e1) {
			e1.printStackTrace(Main.debug);
		}
		htmlViewer.setEditable(false);
		htmlViewer.setBorder(null);		
		htmlViewer.setVisible(true);

		middleBox.add(new JScrollPane(htmlViewer));

		browserPanel.add(middleBox, BorderLayout.CENTER);		

		pack();
	}
	
	private void initListeners() {
		
		htmlViewer.addHyperlinkListener(new HyperlinkListener() {
			
			public void hyperlinkUpdate(HyperlinkEvent e) {
	
				// Hier wird auf einen Klick reagiert
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					
					URL baseURL = ((HTMLDocument) htmlViewer.getDocument()).getBase();

					URL url = null;
					if (e.getURL().getProtocol().equals(baseURL.getProtocol())) {
						try {
							String path = e.getURL().getFile().replace(baseURL.getFile(), "/");
							url = new URL("http", "", path);
						} catch (MalformedURLException e1) {
							e1.printStackTrace(Main.debug);
						}
					} else {
						url = e.getURL();
					}

					// in diesem Fall kam das Event vom Submit-Button:
					if (e instanceof FormSubmitEvent) {
						FormSubmitEvent evt = (FormSubmitEvent) e;
						// Zerlegen erfolgt erst im Server
						String postDatenteil = evt.getData();
						loadWebpage(url, postDatenteil);
					} else {
						loadWebpage(url, null);
					}
				}
			}
		});
	}

	private URL createURL(String ressource) {
		
		URL url = null;
		String[] teilstrings;
		String host = null, pfad = "";

		teilstrings = ressource.split("/");
		// Fuer den Fall, dass URL-Eingabe mit Hostadresse beginnt
		if (teilstrings.length > 0 && !teilstrings[0].equalsIgnoreCase("http:")) {
			if (EntryValidator.isValid(teilstrings[0], EntryValidator.musterDomain)
			        || EntryValidator.isValid(teilstrings[0], EntryValidator.musterIpAdresse)) {
				host = teilstrings[0];

				for (int i = 1; i < teilstrings.length; i++) {
					pfad = pfad + "/" + teilstrings[i];
				}
			}
		}
		// Fuer den Fall, dass URL-Eingabe mit http:// beginnt
		if (teilstrings.length > 2 && teilstrings[0].equalsIgnoreCase("http:")) {
			if (EntryValidator.isValid(teilstrings[2], EntryValidator.musterDomain)
			        || EntryValidator.isValid(teilstrings[2], EntryValidator.musterIpAdresse)) {
				host = teilstrings[2];
			}
			for (int i = 3; i < teilstrings.length; i++) {
				pfad = pfad + "/" + teilstrings[i];
			}
		}

		if (host != null) {
			if (pfad.equals(""))
				pfad = "/";

			try {
				url = new URL("http", host, pfad);
			} catch (MalformedURLException e) {
				e.printStackTrace(Main.debug);
			}
		}

		return url;
	}

	private void loadWebpage(URL url, String postDaten) {		

		if (url != null) {
			if (postDaten == null)
				((WebBrowser) getApplication()).holeWebseite(url);
			else
				((WebBrowser) getApplication()).holeWebseite(url, postDaten);

			String host;
			
			if (url.getHost() == null || url.getHost().equals("")) {
				host = ((WebBrowser) getApplication()).holeHost();
			} else {
				host = url.getHost();
			}
			tfURL.setText(url.getProtocol() + "://" + host + url.getPath());
			setTitle(url.getProtocol() + "://" + host + url.getPath());

		} else {
			tfURL.setText("http://");
		}
	}

	private void initializeWebpage(String quelltext) {
		
		htmlViewer.setContentType("text/html");
		htmlViewer.setText(quelltext);

		Parser parser = Parser.createParser(quelltext, null);

		try {
			NodeList liste = parser.parse(new TagNameFilter("title"));
			if (liste.size() > 0) {
				Tag tag = (Tag) liste.elementAt(0);
				if (tag.getChildren() != null && tag.getChildren().size() > 0)
					setTitle(tag.getChildren().elementAt(0).toHtml());
			}
		} catch (Exception e) {
			e.printStackTrace(Main.debug);
		}
	}
	
	public void start(FiliusFileNode node, String[] param) {
		
		//if (node != null) displayFile(node.getFiliusFile());		
	}

	public void update(Observable arg0, Object arg1) {
		Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass()
		        + " (GUIApplicationWebBrowserWindow), update(" + arg0 + "," + arg1 + ")");
		
		if (arg1 == null) {
			htmlViewer.updateUI();
		} else if (arg1 instanceof HTTPNachricht) {
			if (((HTTPNachricht) arg1).getDaten() == null) {
				htmlViewer.updateUI();
			} else {
				initializeWebpage(((HTTPNachricht) arg1).getDaten());
			}
		} else {
			// Main.debug.println(arg1);
		}
	}
}
