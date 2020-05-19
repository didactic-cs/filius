package filius.rahmenprogramm;

import static filius.rahmenprogramm.EingabenUeberpruefung.musterEmailAdresse;
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
}
