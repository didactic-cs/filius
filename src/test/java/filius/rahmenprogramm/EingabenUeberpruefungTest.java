package filius.rahmenprogramm;

import static filius.rahmenprogramm.EingabenUeberpruefung.musterDomain;
import static filius.rahmenprogramm.EingabenUeberpruefung.musterEmailAdresse;
import static filius.rahmenprogramm.EingabenUeberpruefung.musterIpAdresse;
import static filius.rahmenprogramm.EingabenUeberpruefung.musterIpAdresseAuchLeer;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EingabenUeberpruefungTest {

    @Test
    public void testEmailAdresse_einfach() throws Exception {
        assertTrue(EingabenUeberpruefung.isGueltig("thomas@mustermann.de", musterEmailAdresse));
    }

    @Test
    public void testEmailAdresse_mitPunktUndBindestrich() throws Exception {
        assertTrue(EingabenUeberpruefung.isGueltig("thomas.peter-mueller@mustermann.de", musterEmailAdresse));
    }

    @Test
    public void testEmailAdresse_mitPunktAmAnfang_Ungueltig() throws Exception {
        assertFalse(EingabenUeberpruefung.isGueltig(".peter-mueller@mustermann.de", musterEmailAdresse));
    }

    @Test
    public void testEmailAdresse_mitPunktVorAt_Ungueltig() throws Exception {
        assertFalse(EingabenUeberpruefung.isGueltig("thomas.@mustermann.de", musterEmailAdresse));
    }

    @Test
    public void testEmailAdresse_mitBindestrichAmAnfang_Ungueltig() throws Exception {
        assertFalse(EingabenUeberpruefung.isGueltig("-mueller@mustermann.de", musterEmailAdresse));
    }

    @Test
    public void testEmailAdresse_mitBindestrichVorAt_Ungueltig() throws Exception {
        assertFalse(EingabenUeberpruefung.isGueltig("thomas-@mustermann.de", musterEmailAdresse));
    }

    @Test
    public void testEmailAdresse_mitEinemZeichen() throws Exception {
        assertTrue(EingabenUeberpruefung.isGueltig("a@mustermann.de", musterEmailAdresse));
    }

    @Test
    public void testEmailAdresse_Erweitert() throws Exception {
        assertTrue(EingabenUeberpruefung.isGueltig("Thomas <thomas@mustermann.de>", musterEmailAdresse));
    }

    @Test
    public void testIpAdresse_AktuellesNetzwerk() throws Exception {
        assertTrue(EingabenUeberpruefung.isGueltig("0.0.0.0", musterIpAdresse));
    }

    @Test
    public void testIpAdresseAuchLeer_AktuellesNetzwerk() throws Exception {
        assertTrue(EingabenUeberpruefung.isGueltig("0.0.0.0", musterIpAdresseAuchLeer));
    }
    
    @Test
    public void testDomain_StartsWithLetter() throws Exception {
        assertFalse(EingabenUeberpruefung.isGueltig("0a2b3c.a4b5c6", musterDomain));
        assertFalse(EingabenUeberpruefung.isGueltig("a2b3c.0a4b5c6", musterDomain));
        assertFalse(EingabenUeberpruefung.isGueltig("-a2b3c.a4b5c6", musterDomain));
        assertFalse(EingabenUeberpruefung.isGueltig("a2b3c.-a4b5c6", musterDomain));
    }

    @Test
    public void testDomain_WithDigitsAtArbitraryPos() throws Exception {
        assertTrue(EingabenUeberpruefung.isGueltig("a2b3c.a4b5c6", musterDomain));
    }

    @Test
    public void testDomain_NoHyphenAtEnd() throws Exception {
        assertFalse(EingabenUeberpruefung.isGueltig("abc-.def", musterDomain));
        assertFalse(EingabenUeberpruefung.isGueltig("abc.def-", musterDomain));
    }

    @Test
    public void testDomain_WithHypenAtArbitraryPos() throws Exception {
        assertTrue(EingabenUeberpruefung.isGueltig("a-b-c.d-e-f", musterDomain));
    }

    @Test
    public void testDomain_DoNotAllowUnderscore() throws Exception {
        assertFalse(EingabenUeberpruefung.isGueltig("a_b.a_b", musterDomain));
    }

    @Test
    public void testDomain_AllowEmptyLabelAkaRootDomain() throws Exception {
        assertTrue(EingabenUeberpruefung.isGueltig("", musterDomain));
        assertTrue(EingabenUeberpruefung.isGueltig(".", musterDomain));
    }

    @Test
    public void testDomain_AllowUpTo63Characters() throws Exception {
        assertTrue(EingabenUeberpruefung.isGueltig("a12345678901234567890123456789012345678901234567890123456789012."
                + "a12345678901234567890123456789012345678901234567890123456789012.", musterDomain));
    }

    @Test
    public void testDomain_DoNOTAllow64Characters() throws Exception {
        assertFalse(EingabenUeberpruefung.isGueltig("a123456789012345678901234567890123456789012345678901234567890123."
                + "a123456789012345678901234567890123456789012345678901234567890123.",
                musterDomain));
    }
}
