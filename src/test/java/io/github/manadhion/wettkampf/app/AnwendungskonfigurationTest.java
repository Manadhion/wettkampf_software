package io.github.manadhion.wettkampf.app;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;

import org.junit.jupiter.api.Test;

class AnwendungskonfigurationTest {

    @Test
    void verwendetFestHinterlegteOnlineAdresse() {
        assertEquals(URI.create("https://wettkampf-api.meshalchemy.com"),
                Anwendungskonfiguration.getOnlineServerAdresse());
    }

    @Test
    void akzeptiertNurSichereServeradressenOhneZugangsdaten() {
        assertTrue(Anwendungskonfiguration.istGueltigeServerAdresse(
                URI.create("https://wettkampf.example.org/api/v1")));
        assertTrue(Anwendungskonfiguration.istGueltigeServerAdresse(
                URI.create("http://127.0.0.1:8002/api/v1")));
        assertFalse(Anwendungskonfiguration.istGueltigeServerAdresse(
                URI.create("http://wettkampf.example.org/api/v1")));
        assertFalse(Anwendungskonfiguration.istGueltigeServerAdresse(
                URI.create("https://benutzer:passwort@wettkampf.example.org/api/v1")));
    }
}
