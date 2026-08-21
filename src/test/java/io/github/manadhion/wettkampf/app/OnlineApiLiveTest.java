package io.github.manadhion.wettkampf.app;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/** Manueller Ende-zu-Ende-Test; laeuft nur mit ausdruecklich gesetzter Live-Test-Variable. */
class OnlineApiLiveTest {

    @Test
    @EnabledIfEnvironmentVariable(named = "WETTKAMPF_LIVE_TEST", matches = "true")
    void javaClientLiestUndSchreibtLiveSnapshotUnveraendert() {
        String name = benoetigt("WETTKAMPF_KONTO_NAME");
        char[] passwort = benoetigt("WETTKAMPF_KONTO_PASSWORT").toCharArray();
        try (OnlineApiClient client = new OnlineApiClient(
                Anwendungskonfiguration.getOnlineServerAdresse())) {
            client.anmelden(name, passwort);
            OnlineApi.OnlineSnapshot vorher = client.snapshotLaden();
            client.snapshotSpeichern(vorher);
            OnlineApi.OnlineSnapshot danach = client.snapshotLaden();
            assertEquals(vorher, danach);
        } finally {
            java.util.Arrays.fill(passwort, '\0');
        }
    }

    private String benoetigt(String name) {
        String wert = System.getenv(name);
        if (wert == null || wert.isBlank()) throw new IllegalStateException(name + " fehlt.");
        return wert;
    }
}
