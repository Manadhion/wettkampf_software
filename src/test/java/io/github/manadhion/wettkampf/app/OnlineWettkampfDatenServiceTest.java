package io.github.manadhion.wettkampf.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import io.github.manadhion.wettkampf.app.OnlineApi.OnlineSnapshot;
import io.github.manadhion.wettkampf.app.OnlineApi.OnlineSaison;
import io.github.manadhion.wettkampf.data.Saison;

class OnlineWettkampfDatenServiceTest {

    @Test
    void arbeitetBeiAusfallWeiterUndSynchronisiertNachWiederverbindung() throws Exception {
        FakeOnlineApi api = new FakeOnlineApi();
        try (OnlineWettkampfDatenService service = new OnlineWettkampfDatenService(api)) {
            assertEquals(2627, service.alleSaisons().getFirst().getName());

            api.verbindungUnterbrochen = true;
            Saison neu = new Saison(2728);
            service.saisonAnlegen(neu);
            assertTrue(api.fehlversuch.await(2, TimeUnit.SECONDS));
            assertTrue(service.alleSaisons().stream().anyMatch(s -> s.getName() == 2728));
            assertFalse(service.status().verbunden());
            assertEquals(1, service.status().ausstehendeAenderungen());

            api.verbindungUnterbrochen = false;
            service.synchronisationJetztAnfordern();
            assertTrue(api.erfolg.await(2, TimeUnit.SECONDS));
            warteAufSynchronisation(service);

            assertTrue(service.status().verbunden());
            assertEquals(0, service.status().ausstehendeAenderungen());
            assertTrue(api.gespeichert.saisons().stream().anyMatch(s -> s.name() == 2728));
        }
    }

    @Test
    void meldetSichBeiAbgelaufenerSitzungVorDerSynchronisationNeuAn() throws Exception {
        FakeOnlineApi api = new FakeOnlineApi();
        api.sitzungAbgelaufen = true;
        char[] passwort = "test-passwort".toCharArray();
        try (OnlineWettkampfDatenService service =
                new OnlineWettkampfDatenService(api, "verein", passwort)) {
            service.saisonAnlegen(new Saison(2728));
            assertTrue(api.erfolg.await(2, TimeUnit.SECONDS));
            warteAufSynchronisation(service);
            assertEquals(1, api.anmeldungen.get());
            assertFalse(service.hatNichtSynchronisierteAenderungen());
        }
    }

    private void warteAufSynchronisation(OnlineWettkampfDatenService service)
            throws InterruptedException {
        long ende = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (service.hatNichtSynchronisierteAenderungen() && System.nanoTime() < ende) {
            Thread.sleep(10);
        }
    }

    private static final class FakeOnlineApi implements OnlineApi {
        private final CountDownLatch fehlversuch = new CountDownLatch(1);
        private final CountDownLatch erfolg = new CountDownLatch(1);
        private volatile boolean verbindungUnterbrochen;
        private volatile boolean sitzungAbgelaufen;
        private volatile OnlineSnapshot gespeichert;
        private final AtomicInteger anmeldungen = new AtomicInteger();

        @Override public void anmelden(String name, char[] passwort) {
            anmeldungen.incrementAndGet();
            sitzungAbgelaufen = false;
        }

        @Override
        public OnlineSnapshot snapshotLaden() {
            return new OnlineSnapshot(List.of(new OnlineSaison("online-alt", 2627, 3)),
                    List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                    List.of(), List.of());
        }

        @Override
        public void snapshotSpeichern(OnlineSnapshot snapshot) {
            if (sitzungAbgelaufen) {
                throw new OnlineApiException("Anmeldung abgelaufen", null, false, 401);
            }
            if (verbindungUnterbrochen) {
                fehlversuch.countDown();
                throw new OnlineApiException("nicht erreichbar", null, true);
            }
            gespeichert = snapshot;
            erfolg.countDown();
        }

        @Override public void statusPruefen() {
            if (verbindungUnterbrochen)
                throw new OnlineApiException("nicht erreichbar", null, true);
        }
        @Override public List<OnlineSaison> alleSaisons() { return List.of(); }
        @Override public OnlineSaison saisonAnlegen(int name) { throw new UnsupportedOperationException(); }
        @Override public OnlineSaison saisonAktualisieren(String id, int name, long version) { throw new UnsupportedOperationException(); }
        @Override public void saisonLoeschen(String id, long version) { throw new UnsupportedOperationException(); }
        @Override public void close() { }
    }
}
