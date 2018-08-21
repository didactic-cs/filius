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
package filius.software.email;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import filius.Main;
import filius.rahmenprogramm.EingabenUeberpruefung;
import filius.software.Anwendung;
import filius.software.system.Datei;

/**
 * 
 * @author Andre Asschoff
 * 
 */
public class EmailAnwendung extends Anwendung {
    // Attribute
    private Vector<Kontakt> adressbuch = new Vector<Kontakt>();

    private List<Email> erstellteNachrichten = new ArrayList<Email>();
    private List<Email> empfangeneNachrichten = new ArrayList<Email>();
    private List<Email> gesendeteNachrichten = new ArrayList<Email>();
    private POP3Client pop3client;
    private SMTPClient smtpclient;
    private EmailKonto konto;

    /**
     * Startet die Email-Anwendung und für Sie jeweils einen Pop3- und Smtp-Client.
     */
    public void starten() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (EmailAnwendung), starten()");
        super.starten();

        pop3client = new POP3Client(this);
        pop3client.starten();

        smtpclient = new SMTPClient(this);
        smtpclient.starten();
    }

    /**
     * Methode beendet die EmailAnwendung inkl. der dazu gehörigen smtp und pop3 clients. Dazu wird die Methode der
     * Superklasse aufgerufen und der Socket geschlossen.
     */
    public void beenden() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (EmailAnwendung), beenden()");
        super.beenden();
        if (pop3client != null)
            pop3client.beenden();
        if (smtpclient != null)
            smtpclient.beenden();
    }

    /**
     * ruft die Methode versendeEmail in SMTPClient auf, um eine Email zu versenden. Diese Methode selbst ist nicht
     * blockierend und übernimmt auch den Verbindungs- auf-,bzw. abbau.
     * 
     * @param email
     * @param remoteServerIP
     */
    public void versendeEmail(String remoteServerIP, Email email, String absender) {
        String rcpts = EmailUtils.addressEntryListToString(email.getEmpfaenger());
        if (email.getCc().size() > 0) {
            rcpts += ",";
            rcpts += EmailUtils.addressEntryListToString(email.getCc());
        }
        if (email.getBcc().size() > 0) {
            rcpts += ",";
            rcpts += EmailUtils.addressEntryListToString(email.getBcc());
        }
        smtpclient.versendeEmail(remoteServerIP, email, absender, rcpts);
    }

    /**
     * 
     * @param emailAdresse
     * @param benutzername
     * @param passwort
     * @param pop3Port
     * @param pop3Server
     */
    public void emailsAbholenEmails(String benutzername, String passwort, String pop3Port, String pop3Server) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass()
                + " (EmailAnwendung), emailsAbholenEmails(" + benutzername + "," + passwort + "," + pop3Port + ","
                + pop3Server + ")");
        pop3client.emailsHolen(pop3Server, pop3Port, benutzername, passwort);
    }

    public boolean kontaktHinzufuegen(String name, String vorname, String strasse, int hausnr, int plz, String wohnort,
            String email, String telefon) {
        Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
                + " (EmailAnwendung), kontaktHinzufuegen(" + name + "," + vorname + "," + strasse + "," + hausnr + ","
                + plz + "," + wohnort + "," + email + "," + telefon + ")");
        if (EingabenUeberpruefung.isGueltig(name, EingabenUeberpruefung.musterMindEinZeichen)
                && EingabenUeberpruefung.isGueltig(vorname, EingabenUeberpruefung.musterMindEinZeichen)
                && EingabenUeberpruefung.isGueltig(email, EingabenUeberpruefung.musterEmailAdresse)) {
            try {
                Kontakt kontaktNeu = new Kontakt();

                kontaktNeu.setName(name);
                kontaktNeu.setVorname(vorname);
                kontaktNeu.setStrasse(strasse);
                kontaktNeu.setHausnr(hausnr);
                kontaktNeu.setPlz(plz);
                kontaktNeu.setWohnort(wohnort);
                kontaktNeu.setEmail(email);
                kontaktNeu.setTelefon(telefon);

                getAdressbuch().add(kontaktNeu);
            } catch (Exception e) {
                e.printStackTrace(Main.debug);
                return false;
            }
        } else {
            return false;
        }

        return true;
    }

    public boolean kontaktLoeschen(String name, String vorname, String email) {
        Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
                + " (EmailAnwendung), kontaktLoeschen(" + name + "," + vorname + "," + email + ")");
        if (EingabenUeberpruefung.isGueltig(name, EingabenUeberpruefung.musterMindEinZeichen)
                && EingabenUeberpruefung.isGueltig(vorname, EingabenUeberpruefung.musterMindEinZeichen)
                && EingabenUeberpruefung.isGueltig(email, EingabenUeberpruefung.musterEmailAdresse)) {
            for (Kontakt kontakt : adressbuch) {
                if (email.equalsIgnoreCase(kontakt.getEmail())) {
                    if (name.equals(kontakt.getName()) && vorname.equals(kontakt.getVorname())) {
                        adressbuch.remove(kontakt);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public void speichern() {
        Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
                + " (EmailAnwendung), speichern()");
        Datei datei = new Datei();
        datei.setDateiInhalt(konto.toString());
        datei.setName("konten.txt");
        datei.setDateiTyp("text/txt");
        getSystemSoftware().getDateisystem().speicherDatei(getSystemSoftware().getDateisystem().getRoot(), datei);
    }

    public void laden() {
        Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
                + " (EmailAnwendung), laden()");
        Datei datei = getSystemSoftware().getDateisystem().holeDatei(getSystemSoftware().getDateisystem().getRoot(),
                "konten.txt");
        if (datei != null) {
            String kontenString = datei.getDateiInhalt();
            String[] konten = kontenString.split("\n");
            for (int i = 0; i < konten.length; i++) {
                this.konto = new EmailKonto(konten[i]);
            }
        }

    }

    public POP3Client holePOP3Client() {
        return pop3client;
    }

    public Vector<Kontakt> getAdressbuch() {
        return adressbuch;
    }

    public void setAdressbuch(Vector<Kontakt> adressbuch) {
        this.adressbuch = adressbuch;
    }

    public List<Email> getEmpfangeneNachrichten() {
        return empfangeneNachrichten;
    }

    public void removeReceivedMail(int index) {
        if (empfangeneNachrichten.size() > index && 0 <= index) {
            empfangeneNachrichten.remove(index);
        }
    }

    public void setEmpfangeneNachrichten(List<Email> empfangeneNachrichten) {
        this.empfangeneNachrichten = empfangeneNachrichten;
    }

    public List<Email> getErstellteNachrichten() {
        return erstellteNachrichten;
    }

    public void setErstellteNachrichten(List<Email> erstellteNachrichten) {
        this.erstellteNachrichten = erstellteNachrichten;
    }

    public List<Email> getGesendeteNachrichten() {
        return gesendeteNachrichten;
    }

    public void addGesendeteNachricht(Email gesendeteNachricht) {
        if (!gesendeteNachrichten.contains(gesendeteNachricht)) {
            this.gesendeteNachrichten.add(gesendeteNachricht);
        }
    }

    public void removeSentMail(int index) {
        if (gesendeteNachrichten.size() > index && 0 <= index) {
            gesendeteNachrichten.remove(index);
        }
    }

    public void setGesendeteNachrichten(List<Email> gesendeteNachrichten) {
        this.gesendeteNachrichten = gesendeteNachrichten;
    }

    public Map<String, EmailKonto> holeKontoListe() {
        if (konto != null) {
            return Collections.singletonMap(this.konto.getBenutzername(), this.konto);
        }
        return Collections.<String, EmailKonto> emptyMap();
    }

    public void setzeKonto(EmailKonto konto) {
        this.konto = konto;
    }

    public void setzeKontoListe(Map<String, EmailKonto> kontoListe) {
        if (!kontoListe.isEmpty()) {
            this.konto = kontoListe.values().toArray(new EmailKonto[kontoListe.size()])[0];
        }
    }
}
