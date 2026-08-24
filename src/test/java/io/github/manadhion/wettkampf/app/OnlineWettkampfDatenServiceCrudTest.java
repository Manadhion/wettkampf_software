package io.github.manadhion.wettkampf.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import io.github.manadhion.wettkampf.app.OnlineApi.OnlineSaison;
import io.github.manadhion.wettkampf.app.OnlineApi.OnlineSnapshot;
import io.github.manadhion.wettkampf.data.Altersklasse;
import io.github.manadhion.wettkampf.data.Begegnung;
import io.github.manadhion.wettkampf.data.Liga;
import io.github.manadhion.wettkampf.data.Mannschaft;
import io.github.manadhion.wettkampf.data.Saison;
import io.github.manadhion.wettkampf.data.SaisonSchuetze;
import io.github.manadhion.wettkampf.data.Schuetze;
import io.github.manadhion.wettkampf.data.Wettkampftage;

class OnlineWettkampfDatenServiceCrudTest {

    @Test
    void verwaltetAlleOnlineFachdatenUndErgebnisAuswertungen() throws Exception {
        SpeicherApi api = new SpeicherApi();
        try (OnlineWettkampfDatenService service = new OnlineWettkampfDatenService(api)) {
            Saison saison = new Saison("saison", 2627);
            service.saisonAnlegen(saison);
            assertTrue(service.saisonExistiert(2627));
            saison.setName(2728);
            service.saisonAktualisieren(saison);
            assertEquals(2728, service.saisonMitId("saison").getName());
            assertEquals(1, service.alleSaisons().size());

            Liga ligaA = new Liga("liga-a", "A-Klasse", 1);
            Liga ligaB = new Liga("liga-b", "B-Klasse", 1);
            service.ligaAnlegen(ligaA);
            service.ligaAnlegen(ligaB);
            assertEquals(List.of("liga-b", "liga-a"),
                    service.alleLigen().stream().map(Liga::getId).toList());
            ligaA.setLigaName("Gauliga");
            ligaA.setRangfolge(1);
            service.ligaAktualisieren(ligaA);
            assertEquals("Gauliga", service.ligaMitId("liga-a").getLigaName());
            assertEquals(3, service.naechsteLigaRangfolge());

            Altersklasse klasse = new Altersklasse("klasse", "Herren");
            service.altersklasseAnlegen(klasse);
            klasse.setKlassenName("Offene Klasse");
            service.altersklasseAktualisieren(klasse);
            assertEquals("Offene Klasse", service.alleAltersklassen().getFirst().getKlassenName());

            Mannschaft heim = new Mannschaft("heim", "Heim", "liga-a", null);
            Mannschaft gast = new Mannschaft("gast", "Gast", "liga-a", null);
            service.mannschaftAnlegen(heim);
            service.mannschaftAnlegen(gast);
            heim.setName("Heimverein");
            service.mannschaftAktualisieren(heim);
            assertEquals("Heimverein", service.mannschaftMitId("heim").getName());
            assertEquals(2, service.alleMannschaften().size());

            Schuetze eins = schuetze("s1", "Anna", heim);
            Schuetze zwei = schuetze("s2", "Berta", heim);
            Schuetze drei = schuetze("s3", "Clara", heim);
            Schuetze vier = schuetze("s4", "Dora", heim);
            for (Schuetze schuetze : List.of(eins, zwei, drei, vier)) service.schuetzeAnlegen(schuetze);
            eins.setNachname("Geändert");
            service.schuetzeAktualisieren(eins);
            assertEquals("Geändert", service.schuetzenVonMannschaft("heim").getFirst().getNachname());

            Wettkampftage tag = new Wettkampftage("tag", LocalDate.of(2026, 9, 1), "Heim", "saison");
            service.wettkampftagAnlegen(tag);
            tag.setAusrichterVerein("Heimverein");
            service.wettkampftagAktualisieren(tag);
            assertEquals("Heimverein", service.alleWettkampfTage().getFirst().getAusrichterVerein());
            assertEquals(1, service.wettkampftageVonSaison("saison").size());

            Begegnung begegnung = new Begegnung("begegnung", "heim", "gast", "tag");
            service.begegnungAnlegen(begegnung);
            assertTrue(service.begegnungExistiert("tag", "gast", "heim"));
            assertEquals(1, service.begegnungenAnDiesemTag("tag").size());
            assertEquals(2, service.mannschaftenVonSaison("saison").size());
            assertEquals("liga-a", service.ligenVonSaison("saison").getFirst().getId());
            assertEquals(1, service.begegnungLoeschen("begegnung"));
            assertFalse(service.begegnungExistiert("tag", "heim", "gast"));

            SaisonSchuetze manuell = new SaisonSchuetze("saison", "s1", "Anna", "Geändert",
                    "heim", "Heimverein", "klasse", "Offene Klasse");
            service.saisonSchuetzeSpeichern(manuell);
            assertNotNull(service.saisonSchuetzeFinden("saison", "s1"));
            assertEquals(1, service.saisonSchuetzenVonMannschaft("saison", "heim").size());

            assertTrue(service.ergebnisSpeichern("s1", "tag", 510));
            assertTrue(service.ergebnisSpeichern("s2", "tag", 500));
            assertTrue(service.ergebnisSpeichern("s3", "tag", 490));
            assertTrue(service.ergebnisSpeichern("s4", "tag", 100));
            assertFalse(service.ergebnisSpeichern("s1", "tag", 520));
            assertEquals(520, service.ergebnisFuer("s1", "tag").getErgebnis());
            assertNull(service.ergebnisFuer("nicht-da", "tag"));
            assertEquals(1510, service.gesamtErgebnisBeste3("heim", "tag"));

            warteAufSynchronisation(service);
            assertTrue(api.snapshot.ergebnisse().stream().anyMatch(e -> e.wert() == 520));
            assertEquals(4, api.snapshot.saisonSchuetzen().size());
        }
    }

    @Test
    void loeschtUnbenutzteDatenUndSchuetztAbhaengigkeiten() {
        SpeicherApi api = new SpeicherApi();
        try (OnlineWettkampfDatenService service = new OnlineWettkampfDatenService(api)) {
            Saison benutzt = new Saison("benutzt", 2627);
            Saison frei = new Saison("frei", 2728);
            Liga liga = new Liga("liga", "Liga", 1);
            Liga freieLiga = new Liga("freie-liga", "Freie Liga", 2);
            Altersklasse klasse = new Altersklasse("klasse", "Herren");
            Altersklasse freieKlasse = new Altersklasse("freie-klasse", "Jugend");
            Mannschaft team = new Mannschaft("team", "Team", "liga", null);
            Mannschaft freiesTeam = new Mannschaft("freies-team", "Frei", null, null);
            Schuetze schuetze = new Schuetze("schuetze", "Max", "Muster", "team", "klasse");
            Schuetze freierSchuetze = new Schuetze("freier-schuetze", "Frei", "Mann", "team", "klasse");
            Wettkampftage tag = new Wettkampftage("tag", LocalDate.now(), "Verein", "benutzt");
            Wettkampftage freierTag = new Wettkampftage("freier-tag", LocalDate.now(), "Verein", "benutzt");

            service.saisonAnlegen(benutzt); service.saisonAnlegen(frei);
            service.ligaAnlegen(liga); service.ligaAnlegen(freieLiga);
            service.altersklasseAnlegen(klasse); service.altersklasseAnlegen(freieKlasse);
            service.mannschaftAnlegen(team); service.mannschaftAnlegen(freiesTeam);
            service.schuetzeAnlegen(schuetze); service.schuetzeAnlegen(freierSchuetze);
            service.wettkampftagAnlegen(tag); service.wettkampftagAnlegen(freierTag);
            service.saisonSchuetzeSpeichern(new SaisonSchuetze("benutzt", "schuetze", "Max", "Muster",
                    "team", "Team", "klasse", "Herren"));

            assertThrows(IllegalStateException.class, () -> service.saisonLoeschen("benutzt"));
            assertThrows(IllegalStateException.class, () -> service.ligaLoeschen("liga"));
            assertThrows(IllegalStateException.class, () -> service.altersklasseLoeschen("klasse"));
            assertThrows(IllegalStateException.class, () -> service.mannschaftLoeschen("team"));
            assertThrows(IllegalStateException.class, () -> service.schuetzeLoeschen("schuetze"));

            assertEquals(1, service.wettkampftagLoeschen("freier-tag"));
            assertEquals(1, service.schuetzeLoeschen("freier-schuetze"));
            assertEquals(1, service.mannschaftLoeschen("freies-team"));
            assertEquals(1, service.altersklasseLoeschen("freie-klasse"));
            assertEquals(1, service.ligaLoeschen("freie-liga"));
            assertEquals(1, service.saisonLoeschen("frei"));
        }
    }

    private Schuetze schuetze(String id, String vorname, Mannschaft mannschaft) {
        return new Schuetze(id, vorname, "Muster", mannschaft.getId(), "klasse");
    }

    private void warteAufSynchronisation(OnlineWettkampfDatenService service) throws InterruptedException {
        long ende = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        while (service.hatNichtSynchronisierteAenderungen() && System.nanoTime() < ende) {
            Thread.sleep(10);
        }
        assertFalse(service.hatNichtSynchronisierteAenderungen());
    }

    private static final class SpeicherApi implements OnlineApi {
        private volatile OnlineSnapshot snapshot = leererSnapshot(1);

        @Override public void anmelden(String name, char[] passwort) { }
        @Override public synchronized OnlineSnapshot snapshotLaden() { return snapshot; }
        @Override public synchronized long snapshotSpeichern(OnlineSnapshot neu) {
            long revision = snapshot.revision() + 1;
            snapshot = new OnlineSnapshot(revision, neu.saisons(), neu.ligen(), neu.altersklassen(),
                    neu.mannschaften(), neu.schuetzen(), neu.wettkampftage(), neu.begegnungen(),
                    neu.saisonSchuetzen(), neu.ergebnisse());
            return revision;
        }
        @Override public void statusPruefen() { }
        @Override public List<OnlineSaison> alleSaisons() { return snapshot.saisons(); }
        @Override public OnlineSaison saisonAnlegen(int name) { throw new UnsupportedOperationException(); }
        @Override public OnlineSaison saisonAktualisieren(String id, int name, long version) { throw new UnsupportedOperationException(); }
        @Override public void saisonLoeschen(String id, long version) { throw new UnsupportedOperationException(); }
        @Override public void close() { }
    }

    private static OnlineSnapshot leererSnapshot(long revision) {
        return new OnlineSnapshot(revision, List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of());
    }
}
