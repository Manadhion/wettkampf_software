package io.github.manadhion.wettkampf.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
            long neueRevision = client.snapshotSpeichern(vorher);
            OnlineApi.OnlineSnapshot danach = client.snapshotLaden();
            assertEquals(vorher.revision() + 1, neueRevision);
            assertEquals(neueRevision, danach.revision());
            assertEquals(ohneRevision(vorher), ohneRevision(danach));
            OnlineApiException konflikt = assertThrows(OnlineApiException.class,
                    () -> client.snapshotSpeichern(vorher));
            assertTrue(konflikt.istVersionskonflikt());
        } finally {
            java.util.Arrays.fill(passwort, '\0');
        }
    }

    private OnlineApi.OnlineSnapshot ohneRevision(OnlineApi.OnlineSnapshot snapshot) {
        return new OnlineApi.OnlineSnapshot(0, snapshot.saisons(), snapshot.ligen(),
                snapshot.altersklassen(), snapshot.mannschaften(), snapshot.schuetzen(),
                snapshot.wettkampftage(), snapshot.begegnungen(), snapshot.saisonSchuetzen(),
                snapshot.ergebnisse());
    }

    private String benoetigt(String name) {
        String wert = System.getenv(name);
        if (wert == null || wert.isBlank()) throw new IllegalStateException(name + " fehlt.");
        return wert;
    }
}
