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

    @Test
    void kannNachVersionskonfliktBewusstDenServerstandUebernehmen() throws Exception {
        KonfliktApi api = new KonfliktApi();
        try (OnlineWettkampfDatenService service = new OnlineWettkampfDatenService(api)) {
            service.saisonAnlegen(new Saison("lokal", 2728));
            assertTrue(api.konflikt.await(2, TimeUnit.SECONDS));
            warteAufVersionskonflikt(service);

            assertTrue(service.status().versionskonflikt());
            assertEquals(1, service.status().ausstehendeAenderungen());
            service.serverstandVerwenden();
            warteAufKonfliktloesung(service);

            assertFalse(service.status().versionskonflikt());
            assertFalse(service.hatNichtSynchronisierteAenderungen());
            assertTrue(service.saisonExistiert(2829));
            assertFalse(service.saisonExistiert(2728));
        }
    }

    @Test
    void kannNachVersionskonfliktBewusstDenLokalenStandHochladen() throws Exception {
        KonfliktApi api = new KonfliktApi();
        try (OnlineWettkampfDatenService service = new OnlineWettkampfDatenService(api)) {
            service.saisonAnlegen(new Saison("lokal", 2728));
            assertTrue(api.konflikt.await(2, TimeUnit.SECONDS));
            warteAufVersionskonflikt(service);

            service.lokalenStandVerwenden();
            warteAufKonfliktloesung(service);

            assertFalse(service.status().versionskonflikt());
            assertFalse(service.hatNichtSynchronisierteAenderungen());
            assertTrue(api.gespeichert.saisons().stream().anyMatch(s -> s.name() == 2728));
            assertFalse(api.gespeichert.saisons().stream().anyMatch(s -> s.name() == 2829));
            assertEquals(2, api.gespeichert.revision());
        }
    }

    private void warteAufSynchronisation(OnlineWettkampfDatenService service)
            throws InterruptedException {
        long ende = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (service.hatNichtSynchronisierteAenderungen() && System.nanoTime() < ende) {
            Thread.sleep(10);
        }
    }

    private void warteAufVersionskonflikt(OnlineWettkampfDatenService service)
            throws InterruptedException {
        long ende = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (!service.status().versionskonflikt() && System.nanoTime() < ende) {
            Thread.sleep(10);
        }
    }

    private void warteAufKonfliktloesung(OnlineWettkampfDatenService service)
            throws InterruptedException {
        long ende = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while ((service.status().versionskonflikt() || service.status().konfliktloesungLaeuft())
                && System.nanoTime() < ende) {
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
            return new OnlineSnapshot(7, List.of(new OnlineSaison("online-alt", 2627, 3)),
                    List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                    List.of(), List.of());
        }

        @Override
        public long snapshotSpeichern(OnlineSnapshot snapshot) {
            if (sitzungAbgelaufen) {
                throw new OnlineApiException("Anmeldung abgelaufen", null, false, 401);
            }
            if (verbindungUnterbrochen) {
                fehlversuch.countDown();
                throw new OnlineApiException("nicht erreichbar", null, true);
            }
            gespeichert = snapshot;
            erfolg.countDown();
            return snapshot.revision() + 1;
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

    private static final class KonfliktApi implements OnlineApi {
        private final CountDownLatch konflikt = new CountDownLatch(1);
        private volatile boolean ersterSpeicherversuch = true;
        private volatile OnlineSnapshot server = new OnlineSnapshot(1,
                List.of(new OnlineSaison("alt", 2627, 0)), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of());
        private volatile OnlineSnapshot gespeichert;

        @Override public void anmelden(String name, char[] passwort) { }
        @Override public OnlineSnapshot snapshotLaden() { return server; }
        @Override public synchronized long snapshotSpeichern(OnlineSnapshot snapshot) {
            if (ersterSpeicherversuch) {
                ersterSpeicherversuch = false;
                server = new OnlineSnapshot(2, List.of(new OnlineSaison("server", 2829, 0)),
                        List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                        List.of(), List.of());
                konflikt.countDown();
                throw new OnlineApiException("Versionskonflikt", null, false, 409);
            }
            gespeichert = snapshot;
            server = new OnlineSnapshot(snapshot.revision() + 1, snapshot.saisons(), snapshot.ligen(),
                    snapshot.altersklassen(), snapshot.mannschaften(), snapshot.schuetzen(),
                    snapshot.wettkampftage(), snapshot.begegnungen(), snapshot.saisonSchuetzen(),
                    snapshot.ergebnisse());
            return server.revision();
        }
        @Override public void statusPruefen() { }
        @Override public List<OnlineSaison> alleSaisons() { return server.saisons(); }
        @Override public OnlineSaison saisonAnlegen(int name) { throw new UnsupportedOperationException(); }
        @Override public OnlineSaison saisonAktualisieren(String id, int name, long version) { throw new UnsupportedOperationException(); }
        @Override public void saisonLoeschen(String id, long version) { throw new UnsupportedOperationException(); }
        @Override public void close() { }
    }
}
