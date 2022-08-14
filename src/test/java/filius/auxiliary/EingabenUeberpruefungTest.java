package filius.rahmenprogramm;

import static filius.rahmenprogramm.EntryValidator.musterEmailAdresse;
import static filius.rahmenprogramm.EntryValidator.musterIpAdresse;
import static filius.rahmenprogramm.EntryValidator.musterIpAdresseAuchLeer;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EingabenUeberpruefungTest {

    @Test
    public void testEmailAdresse_einfach() throws Exception {
        assertTrue(EntryValidator.isValid("thomas@mustermann.de", musterEmailAdresse));
    }

    @Test
    public void testEmailAdresse_mitPunktUndBindestrich() throws Exception {
        assertTrue(EntryValidator.isValid("thomas.peter-mueller@mustermann.de", musterEmailAdresse));
    }

    @Test
    public void testEmailAdresse_mitPunktAmAnfang_Ungueltig() throws Exception {
        assertFalse(EntryValidator.isValid(".peter-mueller@mustermann.de", musterEmailAdresse));
    }

    @Test
    public void testEmailAdresse_mitPunktVorAt_Ungueltig() throws Exception {
        assertFalse(EntryValidator.isValid("thomas.@mustermann.de", musterEmailAdresse));
    }

    @Test
    public void testEmailAdresse_mitBindestrichAmAnfang_Ungueltig() throws Exception {
        assertFalse(EntryValidator.isValid("-mueller@mustermann.de", musterEmailAdresse));
    }

    @Test
    public void testEmailAdresse_mitBindestrichVorAt_Ungueltig() throws Exception {
        assertFalse(EntryValidator.isValid("thomas-@mustermann.de", musterEmailAdresse));
    }

    @Test
    public void testEmailAdresse_mitEinemZeichen() throws Exception {
        assertTrue(EntryValidator.isValid("a@mustermann.de", musterEmailAdresse));
    }

    @Test
    public void testEmailAdresse_Erweitert() throws Exception {
        assertTrue(EntryValidator.isValid("Thomas <thomas@mustermann.de>", musterEmailAdresse));
    }

    @Test
    public void testIpAdresse_AktuellesNetzwerk() throws Exception {
        assertTrue(EntryValidator.isValid("0.0.0.0", musterIpAdresse));
    }

    @Test
    public void testIpAdresseAuchLeer_AktuellesNetzwerk() throws Exception {
        assertTrue(EntryValidator.isValid("0.0.0.0", musterIpAdresseAuchLeer));
    }
}
