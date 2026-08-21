package io.github.manadhion.wettkampf.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import io.github.manadhion.wettkampf.app.OnlineZugangKonfiguration.PasswortSchutz;
import io.github.manadhion.wettkampf.app.OnlineZugangKonfiguration.Zugang;

class OnlineZugangKonfigurationTest {

    @TempDir
    Path temporaererOrdner;

    @Test
    void speichertPasswortNichtImKlartextUndAktualisiertDenZugang() throws Exception {
        Path datei = temporaererOrdner.resolve("online.properties");
        PasswortSchutz schutz = new UmkehrSchutz();

        char[] erstesPasswort = "erstes-geheimes-passwort".toCharArray();
        OnlineZugangKonfiguration.speichern(datei, "verein", erstesPasswort, schutz);
        String dateiInhalt = Files.readString(datei, StandardCharsets.ISO_8859_1);
        assertTrue(dateiInhalt.contains("verein"));
        assertFalse(dateiInhalt.contains("erstes-geheimes-passwort"));

        char[] neuesPasswort = "neues-geheimes-passwort".toCharArray();
        OnlineZugangKonfiguration.speichern(datei, "neuer-verein", neuesPasswort, schutz);

        try (Zugang zugang = OnlineZugangKonfiguration.laden(datei, schutz).orElseThrow()) {
            assertEquals("neuer-verein", zugang.name());
            char[] geladen = zugang.passwort();
            try {
                assertEquals("neues-geheimes-passwort", new String(geladen));
            } finally {
                Arrays.fill(geladen, '\0');
            }
        }
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void windowsDpapiKannPasswortFuerDenselbenBenutzerEntschluesseln() {
        Path datei = temporaererOrdner.resolve("dpapi.properties");
        PasswortSchutz schutz = new OnlineZugangKonfiguration.WindowsDpapiSchutz();
        char[] passwort = "dpapi-test-passwort-ÄÖÜß".toCharArray();

        OnlineZugangKonfiguration.speichern(datei, "dpapi-test", passwort, schutz);

        try (Zugang zugang = OnlineZugangKonfiguration.laden(datei, schutz).orElseThrow()) {
            char[] geladen = zugang.passwort();
            try {
                assertEquals(new String(passwort), new String(geladen));
            } finally {
                Arrays.fill(geladen, '\0');
            }
        } finally {
            Arrays.fill(passwort, '\0');
        }
    }

    private static final class UmkehrSchutz implements PasswortSchutz {
        @Override
        public byte[] verschluesseln(byte[] klartext) {
            return umkehren(klartext);
        }

        @Override
        public byte[] entschluesseln(byte[] geheimtext) {
            return umkehren(geheimtext);
        }

        private byte[] umkehren(byte[] eingabe) {
            byte[] ausgabe = eingabe.clone();
            for (int links = 0, rechts = ausgabe.length - 1; links < rechts; links++, rechts--) {
                byte wert = ausgabe[links];
                ausgabe[links] = ausgabe[rechts];
                ausgabe[rechts] = wert;
            }
            return ausgabe;
        }
    }
}
