/*
 ** This file is part of Filius, a network construction and simulation software.
 ** 
 ** Originally created at the University of Siegen, Institute "Didactics of
 ** Informatics and E-Learning" by a students' project group:
 **     members (2006-2007): 
 **         AndrÃ© Asschoff, Johannes Bade, Carsten Dittich, Thomas Gerding,
 **         Nadja HaÃŸler, Ernst Johannes Klebert, Michell Weyer
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

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

/**
 * @author stefan
 * 
 */
public class EmailTest {

    @Test
    public void subject_trim() throws Exception {
        String subject = "test subject";
        Email mail = new Email("subject:    " + subject);
        assertThat(mail.getBetreff()).isEqualTo(subject);
        mail = new Email("subject: " + subject + "    ");
        assertThat(mail.getBetreff()).isEqualTo(subject);
    }

    @Test
    public void subject_caseOfHeaderSpecifier() throws Exception {
        String subject = "test subject";
        Email mail = new Email("subject:" + subject);
        assertThat(mail.getBetreff()).isEqualTo(subject);
        mail = new Email("SuBjEct:" + subject);
        assertThat(mail.getBetreff()).isEqualTo(subject);
    }

    @Test
    public void sender_trim() throws Exception {
        String sender = "sender";
        Email mail = new Email("from:    " + sender);
        assertThat(mail.getAbsender().toString()).isEqualTo(sender);
        mail = new Email("from: " + sender + "    ");
        assertThat(mail.getAbsender().toString()).isEqualTo(sender);
    }

    @Test
    public void sender_nameAndAddress() throws Exception {
        String sender = "sender";
        String senderAddress = "< senderAddress >";
        Email mail = new Email("from: " + sender + "  " + senderAddress);
        assertThat(mail.getAbsender().toString()).isEqualTo("sender <senderAddress>");
        mail = new Email("from: " + sender);
        assertThat(mail.getAbsender().toString()).isEqualTo("sender");
        mail = new Email("from: " + senderAddress);
        assertThat(mail.getAbsender().toString()).isEqualTo("<senderAddress>");
    }

    @Test
    public void recipient_one_nameAndAddress() throws Exception {
        String rcpt = "rcpt";
        String rcptAddress = "< rcptAddress >";
        Email mail = new Email("to: " + rcpt + "  " + rcptAddress);
        assertThat(mail.getEmpfaenger().get(0).toString()).isEqualTo("rcpt <rcptAddress>");
        mail = new Email("to: " + rcpt);
        assertThat(mail.getEmpfaenger().get(0).toString()).isEqualTo("rcpt");
        mail = new Email("to: " + rcptAddress);
        assertThat(mail.getEmpfaenger().get(0).toString()).isEqualTo("<rcptAddress>");
    }
}
