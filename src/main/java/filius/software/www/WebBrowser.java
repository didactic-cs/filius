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
package filius.software.www;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.LinkedList;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.visitors.TagFindingVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import filius.exception.VerbindungsException;
import filius.rahmenprogramm.Base64;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.Information;
import filius.rahmenprogramm.ResourceUtil;
import filius.software.clientserver.ClientAnwendung;
import filius.software.transportschicht.TCPSocket;

/**
 * Diese Klasse implementiert die Funktionalitaet eines HTTP-Clients fuer einen Webbrowser. Dazu werden folgende
 * Funktionen zur Verfuegung gestellt:
 * <ol>
 * <li>Zum Abruf einer Webseite von einem Webserver gibt es die Methoden <b>holeRessource(url: URL)</b> fuer eine
 * GET-Abfrage und</li>
 * <li><b>holeRessource(url: URL, post: String)</b> fuer eine POST-Abfrage mit Daten im Datenteil einer
 * HTTP-Nachricht.</li>
 * </ol>
 * Diese Methoden haben keinen Rueckgabewert und blockieren auch nicht. Die vom Server gelieferten Daten werden an den
 * Beobachter als HTTPNachricht weitergegeben.
 * 
 */
public class WebBrowser extends ClientAnwendung implements I18n {
    private static Logger LOG = LoggerFactory.getLogger(WebBrowser.class);

    private static final int ABRUF_HTML = 1, ABRUF_IMG = 2;

    private LinkedList<String> bilddateien = new LinkedList<String>();

    private String host;

    public void holeWebseite(URL url) {
        holeWebseite(url, "");
    }

    public void holeWebseite(URL url, String post) {
        LOG.trace("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
                + " (WebBrowser), holeWebseite(" + url + "," + post + ")");

        if (socket != null) {
            ausfuehren("closeConnection", new Object[0]);
        }

        ausfuehren("initConnection", new Object[] { url });
        ausfuehren("retrieveWebpage", new Object[] { url, post });
        ausfuehren("retrieveImages", new Object[0]);
        ausfuehren("closeConnection", new Object[0]);
    }

    void initConnection(URL url) {
        if (url.getHost() != null && !url.getHost().equals("")) {
            host = url.getHost();
        }
        if (socket == null) {
            try {
                socket = new TCPSocket(getSystemSoftware(), host, 80);
            } catch (VerbindungsException e) {
                HTTPNachricht fehler = new HTTPNachricht(HTTPNachricht.CLIENT);
                fehler.setDaten(erzeugeHtmlFehlermeldung(0));
                benachrichtigeBeobachter(fehler);
            }
        }
        if (socket != null && !socket.istVerbunden()) {
            try {
                socket.verbinden();
            } catch (Exception e) {
                HTTPNachricht fehler = new HTTPNachricht(HTTPNachricht.CLIENT);
                fehler.setDaten(erzeugeHtmlFehlermeldung(0));
                benachrichtigeBeobachter(fehler);
            }
        }
    }

    void retrieveWebpage(URL url, String post) {
        HTTPNachricht nachricht = createRequest(url, post);
        if (socket != null && socket.istVerbunden()) {
            try {
                socket.senden(nachricht.toString());

                String responseData = socket.empfangen();
                if (responseData == null) {
                    HTTPNachricht response = new HTTPNachricht(HTTPNachricht.CLIENT);
                    response.setDaten(erzeugeHtmlFehlermeldung(0));
                    benachrichtigeBeobachter(response);
                } else {
                    HTTPNachricht response = new HTTPNachricht(responseData);
                    if (response.getStatusCode() != 200) {
                        response.setDaten(erzeugeHtmlFehlermeldung(response.getStatusCode()));
                    } else {
                        String contentType = response.getContentType();
                        if (HTTPNachricht.TEXT_HTML.equalsIgnoreCase(contentType) && response.getDaten() != null) {
                            extractImagesReferences(response.getDaten(), response.getHost());
                        }
                    }
                    benachrichtigeBeobachter(response);
                }
            } catch (Exception e) {
                LOG.debug("Error while retrieving web page.", e);
            }
        }
    }

    private HTTPNachricht createRequest(URL url, String post) {
        HTTPNachricht nachricht = new HTTPNachricht(HTTPNachricht.CLIENT);
        nachricht.setPfad(url.getFile());
        nachricht.setHost(host);
        if (nachricht.getHost() != null && !nachricht.getHost().equals("")) {
            if (post != null && !post.equals("")) {
                nachricht.setMethod(HTTPNachricht.POST);
                nachricht.setDaten(post);
            } else {
                nachricht.setMethod(HTTPNachricht.GET);
            }
        }
        return nachricht;
    }

    public String holeHost() {
        return host;
    }

    public void starten() {
        super.starten();
        bilddateien = new LinkedList<String>();
    }

    /**
     * liest eine reale Textdatei vom Format .txt ein. Diese befinden sich im Ordner /config
     */
    private String einlesenTextdatei(String datei) throws FileNotFoundException, IOException {
        LOG.trace("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
                + " (WebBrowser), einlesenTextdatei(" + datei + ")");
        StringBuffer fullFile = new StringBuffer();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(ResourceUtil.getResourcePath(datei)), Charset.forName("UTF-8")))) {
            String input;
            while ((input = reader.readLine()) != null) {
                fullFile.append(input).append("\n");
            }
        }
        return fullFile.toString();
    }

    private String erzeugeHtmlFehlermeldung(int statusCode) {
        LOG.trace("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
                + " (WebBrowser), erzeugeHtmlFehlermeldung(" + statusCode + ")");
        String quelltext;
        String dateipfad;
        String meldung;

        if (statusCode == 0) {
            quelltext = messages.getString("sw_webbrowser_msg1");
        } else {
            dateipfad = ResourceUtil.getResourcePath(
                    "tmpl/http_fehler_" + Information.getInformation().getLocale().toString() + ".txt");
            try {
                quelltext = einlesenTextdatei(dateipfad);
            } catch (Exception e) {
                quelltext = messages.getString("sw_webbrowser_msg2");
                LOG.debug("", e);
            }

            quelltext = quelltext.replace(":code:", "" + statusCode);
            meldung = HTTPNachricht.holeStatusNachricht(statusCode);
            quelltext = quelltext.replace(":meldung:", meldung);
        }

        return quelltext;
    }

    /**
     * Methode zur Verarbeitung von IMG-Tags. Mit einem Parser werden IMG-Tags im uebergebenen Quelltext gesucht. Alle
     * Bilddateien werden in eine Liste geschrieben.
     * 
     * @param quelltext
     * @param host
     */
    private void extractImagesReferences(String quelltext, String host) {
        LOG.trace("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
                + " (WebBrowser), verarbeiteIMGTags(" + quelltext + "," + host + ")");

        Parser parser = Parser.createParser(quelltext, null);
        TagFindingVisitor nodeVisitor = new TagFindingVisitor(new String[] { "img" });
        try {
            parser.visitAllNodesWith(nodeVisitor);
            Node[] nodes = nodeVisitor.getTags(0);

            for (int i = 0; i < nodes.length; i++) {
                if (nodes[i] instanceof ImageTag) {
                    ImageTag img = (ImageTag) nodes[i];
                    synchronized (bilddateien) {
                        bilddateien.add(img.getImageURL());
                    }
                }
            }
        } catch (Exception e) {
            LOG.debug("", e);
        }
    }

    /**
     * Liste der gefundenen IMG SRCs wird iteriert, die einzelnen Bilder werden abgerufen
     */
    void retrieveImages() {
        synchronized (bilddateien) {
            for (String dateipfad : bilddateien) {
                try {
                    HTTPNachricht request = createRequest(new URL("http", host, dateipfad), "");
                    if (socket != null && socket.istVerbunden()) {
                        socket.senden(request.toString());

                        String responseData = socket.empfangen();
                        if (responseData != null) {
                            HTTPNachricht response = new HTTPNachricht(responseData);

                            if (response.getStatusCode() != 200) {
                                benachrichtigeBeobachter();
                            } else {
                                String contentType = response.getContentType();
                                if (HTTPNachricht.IMAGE_BMP.equalsIgnoreCase(contentType)
                                        || HTTPNachricht.IMAGE_GIF.equalsIgnoreCase(contentType)
                                        || HTTPNachricht.IMAGE_JPG.equalsIgnoreCase(contentType)
                                        || HTTPNachricht.IMAGE_PNG.equalsIgnoreCase(contentType)) {
                                    synchronized (bilddateien) {
                                        if (bilddateien.size() > 0) {
                                            dateipfad = bilddateien.removeFirst();
                                            Base64.decodeToFile(response.getDaten(),
                                                    Information.getInformation().getTempPfad() + dateipfad);
                                        }
                                    }
                                    benachrichtigeBeobachter();
                                }
                            }
                        }
                    }
                } catch (MalformedURLException e) {
                    LOG.debug("Could not retrieve image. Invalid URL.", e);
                } catch (Exception e) {
                    LOG.debug("Unexpected error while retrieving images via http.", e);
                }
            }
        }
    }

    void closeConnection() {
        if (socket != null) {
            socket.schliessen();
            socket = null;
        }
    }
}
