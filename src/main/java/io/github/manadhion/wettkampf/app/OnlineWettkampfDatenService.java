package io.github.manadhion.wettkampf.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.github.manadhion.wettkampf.app.OnlineApi.OnlineAltersklasse;
import io.github.manadhion.wettkampf.app.OnlineApi.OnlineBegegnung;
import io.github.manadhion.wettkampf.app.OnlineApi.OnlineErgebnis;
import io.github.manadhion.wettkampf.app.OnlineApi.OnlineLiga;
import io.github.manadhion.wettkampf.app.OnlineApi.OnlineMannschaft;
import io.github.manadhion.wettkampf.app.OnlineApi.OnlineSaison;
import io.github.manadhion.wettkampf.app.OnlineApi.OnlineSaisonSchuetze;
import io.github.manadhion.wettkampf.app.OnlineApi.OnlineSchuetze;
import io.github.manadhion.wettkampf.app.OnlineApi.OnlineSnapshot;
import io.github.manadhion.wettkampf.app.OnlineApi.OnlineWettkampftag;
import io.github.manadhion.wettkampf.data.Altersklasse;
import io.github.manadhion.wettkampf.data.Begegnung;
import io.github.manadhion.wettkampf.data.Ergebnisse;
import io.github.manadhion.wettkampf.data.Liga;
import io.github.manadhion.wettkampf.data.Mannschaft;
import io.github.manadhion.wettkampf.data.Saison;
import io.github.manadhion.wettkampf.data.SaisonSchuetze;
import io.github.manadhion.wettkampf.data.Schuetze;
import io.github.manadhion.wettkampf.data.Wettkampftage;

/**
 * Online-Datenservice mit vollstaendigem fluechtigem Arbeitsspeicher-Snapshot.
 * Aenderungen werden im Hintergrund atomar zum Server uebertragen.
 */
public final class OnlineWettkampfDatenService implements WettkampfDatenService, AutoCloseable {

    private static final Comparator<String> TEXT = String.CASE_INSENSITIVE_ORDER;

    private final OnlineApi apiClient;
    private final String kontoName;
    private final char[] kontoPasswort;
    private final Object sperre = new Object();
    private final ScheduledExecutorService hintergrund;
    private final List<Consumer<OnlineStatus>> statusListener = new CopyOnWriteArrayList<>();

    private final Map<String, OnlineSaison> saisons = new LinkedHashMap<>();
    private final Map<String, OnlineLiga> ligen = new LinkedHashMap<>();
    private final Map<String, OnlineAltersklasse> altersklassen = new LinkedHashMap<>();
    private final Map<String, OnlineMannschaft> mannschaften = new LinkedHashMap<>();
    private final Map<String, OnlineSchuetze> schuetzen = new LinkedHashMap<>();
    private final Map<String, OnlineWettkampftag> wettkampftage = new LinkedHashMap<>();
    private final Map<String, OnlineBegegnung> begegnungen = new LinkedHashMap<>();
    private final Map<String, OnlineSaisonSchuetze> saisonSchuetzen = new LinkedHashMap<>();
    private final Map<String, OnlineErgebnis> ergebnisse = new LinkedHashMap<>();

    private long aenderungsstand;
    private long bestaetigterStand;
    private long serverRevision;
    private volatile boolean verbunden = true;
    private volatile boolean geschlossen;
    private volatile boolean versionskonflikt;
    private volatile boolean konfliktloesungLaeuft;
    private volatile String synchronisationsfehler;

    public OnlineWettkampfDatenService(OnlineApi apiClient) {
        this(apiClient, null, null);
    }

    public OnlineWettkampfDatenService(OnlineApi apiClient, String kontoName,
            char[] kontoPasswort) {
        this.apiClient = Objects.requireNonNull(apiClient);
        this.kontoName = kontoName;
        this.kontoPasswort = kontoPasswort == null ? new char[0] : kontoPasswort.clone();
        snapshotUebernehmen(apiClient.snapshotLaden());
        hintergrund = Executors.newSingleThreadScheduledExecutor(aufgabe -> {
            Thread thread = new Thread(aufgabe, "wettkampf-online-synchronisation");
            thread.setDaemon(true);
            return thread;
        });
        hintergrund.scheduleWithFixedDelay(this::periodischPruefen, 5, 5, TimeUnit.SECONDS);
    }

    public void statusListenerHinzufuegen(Consumer<OnlineStatus> listener) {
        statusListener.add(Objects.requireNonNull(listener));
        listener.accept(status());
    }

    public OnlineStatus status() {
        synchronized (sperre) {
            return new OnlineStatus(verbunden, aenderungsstand - bestaetigterStand,
                    synchronisationsfehler, versionskonflikt, konfliktloesungLaeuft);
        }
    }

    public boolean hatNichtSynchronisierteAenderungen() {
        return status().ausstehendeAenderungen() > 0;
    }

    /** Stoesst ausserhalb des regelmaessigen Fuenf-Sekunden-Takts eine erneute Pruefung an. */
    public void synchronisationJetztAnfordern() {
        hintergrund.execute(this::periodischPruefen);
    }

    /** Verwirft nach ausdruecklicher Bestaetigung die lokalen Aenderungen und laedt den Serverstand. */
    public void serverstandVerwenden() {
        konfliktloesungStarten(this::serverstandLaden);
    }

    /** Schreibt nach ausdruecklicher Bestaetigung den lokalen Stand ueber den aktuellen Serverstand. */
    public void lokalenStandVerwenden() {
        konfliktloesungStarten(this::lokalenStandHochladen);
    }

    @Override public void initialisieren() { }

    @Override public List<Saison> alleSaisons() {
        synchronized (sperre) {
            return saisons.values().stream().sorted(Comparator.comparingInt(OnlineSaison::name).reversed())
                    .map(this::saison).toList();
        }
    }
    @Override public Saison saisonMitId(String id) {
        synchronized (sperre) { return saisonOderNull(saisons.get(id)); }
    }
    @Override public boolean saisonExistiert(int name) {
        synchronized (sperre) { return saisons.values().stream().anyMatch(w -> w.name() == name); }
    }
    @Override public void saisonAnlegen(Saison wert) {
        aendern(() -> saisons.put(wert.getId(), new OnlineSaison(wert.getId(), wert.getName(), 0)));
    }
    @Override public void saisonAktualisieren(Saison wert) {
        aendern(() -> {
            OnlineSaison alt = vorhanden(saisons, wert.getId(), "Saison");
            saisons.put(wert.getId(), new OnlineSaison(wert.getId(), wert.getName(), alt.version() + 1));
        });
    }
    @Override public int saisonLoeschen(String id) {
        aendern(() -> {
            if (wettkampftage.values().stream().anyMatch(w -> id.equals(w.saisonId()))) verwendet("Saison");
            vorhanden(saisons, id, "Saison");
            saisons.remove(id);
        });
        return 1;
    }

    @Override public List<Wettkampftage> alleWettkampfTage() {
        synchronized (sperre) {
            return wettkampftage.values().stream().sorted(Comparator.comparing(OnlineWettkampftag::datum))
                    .map(this::wettkampftag).toList();
        }
    }
    @Override public List<Wettkampftage> wettkampftageVonSaison(String id) {
        synchronized (sperre) {
            return wettkampftage.values().stream().filter(w -> id.equals(w.saisonId()))
                    .sorted(Comparator.comparing(OnlineWettkampftag::datum)).map(this::wettkampftag).toList();
        }
    }
    @Override public void wettkampftagAnlegen(Wettkampftage w) { aendern(() -> wettkampftage.put(w.getId(), online(w))); }
    @Override public void wettkampftagAktualisieren(Wettkampftage w) {
        aendern(() -> { vorhanden(wettkampftage, w.getId(), "Wettkampftag"); wettkampftage.put(w.getId(), online(w)); });
    }
    @Override public int wettkampftagLoeschen(String id) {
        aendern(() -> {
            if (begegnungen.values().stream().anyMatch(w -> id.equals(w.wettkampftagId()))
                    || ergebnisse.values().stream().anyMatch(w -> id.equals(w.wettkampftagId()))) verwendet("Wettkampftag");
            vorhanden(wettkampftage, id, "Wettkampftag"); wettkampftage.remove(id);
        });
        return 1;
    }

    @Override public List<Mannschaft> alleMannschaften() {
        synchronized (sperre) {
            return mannschaften.values().stream().sorted(Comparator.comparing(OnlineMannschaft::name, TEXT))
                    .map(this::mannschaft).toList();
        }
    }
    @Override public List<Mannschaft> mannschaftenVonSaison(String saisonId) {
        synchronized (sperre) {
            Set<String> tage = wettkampftage.values().stream().filter(w -> saisonId.equals(w.saisonId()))
                    .map(OnlineWettkampftag::id).collect(Collectors.toSet());
            Set<String> ids = begegnungen.values().stream().filter(w -> tage.contains(w.wettkampftagId()))
                    .flatMap(w -> java.util.stream.Stream.of(w.heim(), w.gegner())).collect(Collectors.toSet());
            return mannschaften.values().stream().filter(w -> ids.contains(w.id()))
                    .sorted(Comparator.comparing(OnlineMannschaft::name, TEXT)).map(this::mannschaft).toList();
        }
    }
    @Override public Mannschaft mannschaftMitId(String id) {
        synchronized (sperre) { OnlineMannschaft w = mannschaften.get(id); return w == null ? null : mannschaft(w); }
    }
    @Override public void mannschaftAnlegen(Mannschaft w) { aendern(() -> mannschaften.put(w.getId(), onlineMitLiga(w))); }
    @Override public void mannschaftAktualisieren(Mannschaft w) {
        aendern(() -> { vorhanden(mannschaften, w.getId(), "Mannschaft"); mannschaften.put(w.getId(), onlineMitLiga(w)); });
    }
    @Override public int mannschaftLoeschen(String id) {
        aendern(() -> {
            if (schuetzen.values().stream().anyMatch(w -> id.equals(w.mannschaftId()))
                    || begegnungen.values().stream().anyMatch(w -> id.equals(w.heim()) || id.equals(w.gegner()))
                    || saisonSchuetzen.values().stream().anyMatch(w -> id.equals(w.mannschaftId()))) verwendet("Mannschaft");
            vorhanden(mannschaften, id, "Mannschaft"); mannschaften.remove(id);
        });
        return 1;
    }

    @Override public List<Schuetze> schuetzenVonMannschaft(String id) {
        synchronized (sperre) {
            return schuetzen.values().stream().filter(w -> id.equals(w.mannschaftId()))
                    .sorted(Comparator.comparing(OnlineSchuetze::vorname, TEXT).thenComparing(OnlineSchuetze::nachname, TEXT))
                    .map(this::schuetze).toList();
        }
    }
    @Override public void schuetzeAnlegen(Schuetze w) { aendern(() -> schuetzen.put(w.getId(), online(w))); }
    @Override public void schuetzeAktualisieren(Schuetze w) {
        aendern(() -> { vorhanden(schuetzen, w.getId(), "Schuetze"); schuetzen.put(w.getId(), online(w)); });
    }
    @Override public int schuetzeLoeschen(String id) {
        aendern(() -> {
            if (ergebnisse.values().stream().anyMatch(w -> id.equals(w.schuetzeId()))
                    || saisonSchuetzen.values().stream().anyMatch(w -> id.equals(w.schuetzeId()))) verwendet("Schuetze");
            vorhanden(schuetzen, id, "Schuetze"); schuetzen.remove(id);
        });
        return 1;
    }

    @Override public List<Altersklasse> alleAltersklassen() {
        synchronized (sperre) {
            return altersklassen.values().stream().sorted(Comparator.comparing(OnlineAltersklasse::name, TEXT))
                    .map(this::altersklasse).toList();
        }
    }
    @Override public void altersklasseAnlegen(Altersklasse w) { aendern(() -> altersklassen.put(w.getId(), online(w))); }
    @Override public void altersklasseAktualisieren(Altersklasse w) {
        aendern(() -> { vorhanden(altersklassen, w.getId(), "Altersklasse"); altersklassen.put(w.getId(), online(w)); });
    }
    @Override public int altersklasseLoeschen(String id) {
        aendern(() -> {
            if (schuetzen.values().stream().anyMatch(w -> id.equals(w.altersklasseId()))
                    || saisonSchuetzen.values().stream().anyMatch(w -> id.equals(w.altersklasseId()))) verwendet("Altersklasse");
            vorhanden(altersklassen, id, "Altersklasse"); altersklassen.remove(id);
        });
        return 1;
    }

    @Override public Ergebnisse ergebnisFuer(String schuetzeId, String tagId) {
        synchronized (sperre) {
            OnlineErgebnis w = ergebnisse.get(ergebnisSchluessel(schuetzeId, tagId));
            return w == null ? null : ergebnis(w);
        }
    }
    @Override public int gesamtErgebnisBeste3(String mannschaftId, String tagId) {
        synchronized (sperre) {
            OnlineWettkampftag tag = wettkampftage.get(tagId);
            if (tag == null) return 0;
            Set<String> ids = saisonSchuetzen.values().stream()
                    .filter(w -> tag.saisonId().equals(w.saisonId()) && mannschaftId.equals(w.mannschaftId()))
                    .map(OnlineSaisonSchuetze::schuetzeId).collect(Collectors.toSet());
            return ergebnisse.values().stream().filter(w -> tagId.equals(w.wettkampftagId()) && ids.contains(w.schuetzeId()))
                    .map(OnlineErgebnis::wert).sorted(Comparator.reverseOrder()).limit(3).mapToInt(Integer::intValue).sum();
        }
    }
    @Override public boolean ergebnisSpeichern(String schuetzeId, String tagId, int wert) {
        final boolean[] neu = new boolean[1];
        aendern(() -> {
            saisonMeldungFallsNoetig(schuetzeId, tagId);
            String schluessel = ergebnisSchluessel(schuetzeId, tagId);
            OnlineErgebnis alt = ergebnisse.get(schluessel);
            neu[0] = alt == null;
            String id = alt == null ? java.util.UUID.randomUUID().toString() : alt.id();
            ergebnisse.put(schluessel, new OnlineErgebnis(id, schuetzeId, tagId, wert));
        });
        return neu[0];
    }

    @Override public List<Liga> alleLigen() { synchronized (sperre) { return sortierteLigen(ligen.values()); } }
    @Override public List<Liga> ligenVonSaison(String saisonId) {
        synchronized (sperre) {
            Set<String> tage = wettkampftage.values().stream().filter(w -> saisonId.equals(w.saisonId()))
                    .map(OnlineWettkampftag::id).collect(Collectors.toSet());
            Set<String> ids = begegnungen.values().stream().filter(w -> tage.contains(w.wettkampftagId()))
                    .map(w -> w.liga() != null ? w.liga() : mannschaften.get(w.heim()).klasse())
                    .filter(Objects::nonNull).collect(Collectors.toSet());
            return sortierteLigen(ligen.values().stream().filter(w -> ids.contains(w.id())).toList());
        }
    }
    @Override public int naechsteLigaRangfolge() {
        synchronized (sperre) { return ligen.values().stream().mapToInt(OnlineLiga::rangfolge).max().orElse(0) + 1; }
    }
    @Override public Liga ligaMitId(String id) {
        synchronized (sperre) { OnlineLiga w = ligen.get(id); return w == null ? null : liga(w); }
    }
    @Override public void ligaAnlegen(Liga w) {
        aendern(() -> { verschiebeLigenBeimEinfuegen(w.getRangfolge()); ligen.put(w.getId(), online(w)); });
    }
    @Override public void ligaAktualisieren(Liga w) {
        aendern(() -> {
            OnlineLiga alt = vorhanden(ligen, w.getId(), "Liga");
            verschiebeLigenBeimAendern(w.getId(), alt.rangfolge(), w.getRangfolge());
            ligen.put(w.getId(), online(w));
        });
    }
    @Override public int ligaLoeschen(String id) {
        aendern(() -> {
            if (mannschaften.values().stream().anyMatch(w -> id.equals(w.klasse()))
                    || begegnungen.values().stream().anyMatch(w -> id.equals(w.liga()))) verwendet("Liga");
            vorhanden(ligen, id, "Liga"); ligen.remove(id);
        });
        return 1;
    }

    @Override public List<SaisonSchuetze> saisonSchuetzenVonMannschaft(String saisonId, String mannschaftId) {
        synchronized (sperre) {
            return saisonSchuetzen.values().stream()
                    .filter(w -> saisonId.equals(w.saisonId()) && mannschaftId.equals(w.mannschaftId()))
                    .sorted(Comparator.comparing(OnlineSaisonSchuetze::vorname, TEXT)
                            .thenComparing(OnlineSaisonSchuetze::nachname, TEXT))
                    .map(this::saisonSchuetze).toList();
        }
    }
    @Override public SaisonSchuetze saisonSchuetzeFinden(String saisonId, String schuetzeId) {
        synchronized (sperre) {
            OnlineSaisonSchuetze w = saisonSchuetzen.get(saisonSchuetzeSchluessel(saisonId, schuetzeId));
            return w == null ? null : saisonSchuetze(w);
        }
    }
    @Override public void saisonSchuetzeSpeichern(SaisonSchuetze w) {
        aendern(() -> saisonSchuetzen.put(saisonSchuetzeSchluessel(w.getSaisonID(), w.getSchuetzeID()), online(w)));
    }

    @Override public List<Begegnung> begegnungenAnDiesemTag(String id) {
        synchronized (sperre) {
            return begegnungen.values().stream().filter(w -> id.equals(w.wettkampftagId()))
                    .sorted(Comparator.comparing(OnlineBegegnung::heimName, Comparator.nullsLast(TEXT))
                            .thenComparing(OnlineBegegnung::gegnerName, Comparator.nullsLast(TEXT)))
                    .map(this::begegnung).toList();
        }
    }
    @Override public boolean begegnungExistiert(String tagId, String a, String b) {
        synchronized (sperre) {
            return begegnungen.values().stream().anyMatch(w -> tagId.equals(w.wettkampftagId())
                    && ((a.equals(w.heim()) && b.equals(w.gegner())) || (b.equals(w.heim()) && a.equals(w.gegner()))));
        }
    }
    @Override public void begegnungAnlegen(Begegnung w) {
        aendern(() -> {
            OnlineMannschaft heim = vorhanden(mannschaften, w.getHeim(), "Mannschaft");
            OnlineMannschaft gegner = vorhanden(mannschaften, w.getGegner(), "Mannschaft");
            OnlineLiga liga = heim.klasse() == null ? null : ligen.get(heim.klasse());
            begegnungen.put(w.getId(), new OnlineBegegnung(w.getId(), w.getHeim(), w.getGegner(),
                    w.getWettkampftag(), heim.klasse(), liga == null ? null : liga.name(), heim.name(), gegner.name()));
        });
    }
    @Override public int begegnungLoeschen(String id) {
        aendern(() -> { vorhanden(begegnungen, id, "Begegnung"); begegnungen.remove(id); });
        return 1;
    }

    private void aendern(Runnable aenderung) {
        synchronized (sperre) { aenderung.run(); aenderungsstand++; }
        statusMelden();
        hintergrund.execute(this::synchronisieren);
    }

    private void synchronisieren() {
        if (geschlossen) return;
        OnlineSnapshot snapshot;
        long stand;
        synchronized (sperre) {
            if (versionskonflikt || konfliktloesungLaeuft
                    || aenderungsstand == bestaetigterStand) return;
            stand = aenderungsstand;
            snapshot = snapshotErzeugen();
        }
        try {
            long neueRevision = snapshotSenden(snapshot);
            synchronized (sperre) {
                serverRevision = neueRevision;
                bestaetigterStand = Math.max(bestaetigterStand, stand);
                verbunden = true;
                versionskonflikt = false;
                synchronisationsfehler = null;
            }
        } catch (OnlineApiException e) {
            synchronized (sperre) {
                if (e.istVerbindungsfehler()) verbunden = false;
                else if (e.istVersionskonflikt()) {
                    versionskonflikt = true;
                    synchronisationsfehler = "Der Server enthält inzwischen einen neueren Stand. "
                            + "Die lokalen Änderungen wurden nicht überschrieben. Bitte wählen Sie "
                            + "bewusst aus, welcher vollständige Stand erhalten bleiben soll.";
                } else synchronisationsfehler = e.getMessage();
            }
        }
        statusMelden();
    }

    private long snapshotSenden(OnlineSnapshot snapshot) {
        try {
            return apiClient.snapshotSpeichern(snapshot);
        } catch (OnlineApiException e) {
            if (!e.istAnmeldungAbgelaufen() || kontoName == null || kontoPasswort.length == 0) {
                throw e;
            }
            apiClient.anmelden(kontoName, kontoPasswort);
            return apiClient.snapshotSpeichern(snapshot);
        }
    }

    private OnlineSnapshot snapshotLaden() {
        try {
            return apiClient.snapshotLaden();
        } catch (OnlineApiException e) {
            if (!e.istAnmeldungAbgelaufen() || kontoName == null || kontoPasswort.length == 0) {
                throw e;
            }
            apiClient.anmelden(kontoName, kontoPasswort);
            return apiClient.snapshotLaden();
        }
    }

    private void konfliktloesungStarten(Runnable aufgabe) {
        synchronized (sperre) {
            if (geschlossen || !versionskonflikt || konfliktloesungLaeuft) return;
            konfliktloesungLaeuft = true;
        }
        statusMelden();
        hintergrund.execute(() -> {
            try {
                aufgabe.run();
            } catch (OnlineApiException e) {
                synchronized (sperre) {
                    if (e.istVerbindungsfehler()) verbunden = false;
                    versionskonflikt = true;
                    synchronisationsfehler = "Der Versionskonflikt konnte nicht gelöst werden: "
                            + e.getMessage();
                }
            } finally {
                konfliktloesungLaeuft = false;
                statusMelden();
            }
        });
    }

    private void serverstandLaden() {
        long standVorLaden;
        synchronized (sperre) {
            standVorLaden = aenderungsstand;
        }
        OnlineSnapshot snapshot = snapshotLaden();
        synchronized (sperre) {
            if (aenderungsstand != standVorLaden) {
                synchronisationsfehler = "Während der Konfliktlösung wurden weitere lokale "
                        + "Änderungen vorgenommen. Bitte prüfen Sie den Stand und wählen Sie erneut.";
                return;
            }
            snapshotUebernehmen(snapshot);
            aenderungsstand = 0;
            bestaetigterStand = 0;
            verbunden = true;
            versionskonflikt = false;
            synchronisationsfehler = null;
        }
    }

    private void lokalenStandHochladen() {
        OnlineSnapshot serverstand = snapshotLaden();
        OnlineSnapshot lokalerStand;
        long stand;
        synchronized (sperre) {
            stand = aenderungsstand;
            lokalerStand = snapshotErzeugen(serverstand.revision());
        }
        long neueRevision = snapshotSenden(lokalerStand);
        boolean weitereAenderungen;
        synchronized (sperre) {
            serverRevision = neueRevision;
            bestaetigterStand = Math.max(bestaetigterStand, stand);
            verbunden = true;
            versionskonflikt = false;
            synchronisationsfehler = null;
            weitereAenderungen = aenderungsstand > bestaetigterStand;
        }
        if (weitereAenderungen) hintergrund.execute(this::synchronisieren);
    }

    private void periodischPruefen() {
        if (geschlossen) return;
        if (hatNichtSynchronisierteAenderungen()) { synchronisieren(); return; }
        try {
            apiClient.statusPruefen();
            verbunden = true;
            synchronisationsfehler = null;
        } catch (OnlineApiException e) {
            if (e.istVerbindungsfehler()) verbunden = false;
            else synchronisationsfehler = e.getMessage();
        }
        statusMelden();
    }

    private void statusMelden() {
        OnlineStatus wert = status();
        statusListener.forEach(listener -> listener.accept(wert));
    }

    private void snapshotUebernehmen(OnlineSnapshot snapshot) {
        synchronized (sperre) {
            serverRevision = snapshot.revision();
            fuellen(saisons, snapshot.saisons(), OnlineSaison::id);
            fuellen(ligen, snapshot.ligen(), OnlineLiga::id);
            fuellen(altersklassen, snapshot.altersklassen(), OnlineAltersklasse::id);
            fuellen(mannschaften, snapshot.mannschaften(), OnlineMannschaft::id);
            fuellen(schuetzen, snapshot.schuetzen(), OnlineSchuetze::id);
            fuellen(wettkampftage, snapshot.wettkampftage(), OnlineWettkampftag::id);
            fuellen(begegnungen, snapshot.begegnungen(), OnlineBegegnung::id);
            fuellen(saisonSchuetzen, snapshot.saisonSchuetzen(), w -> saisonSchuetzeSchluessel(w.saisonId(), w.schuetzeId()));
            fuellen(ergebnisse, snapshot.ergebnisse(), w -> ergebnisSchluessel(w.schuetzeId(), w.wettkampftagId()));
        }
    }

    private OnlineSnapshot snapshotErzeugen() {
        return snapshotErzeugen(serverRevision);
    }

    private OnlineSnapshot snapshotErzeugen(long revision) {
        return new OnlineSnapshot(revision,
                new ArrayList<>(saisons.values()), new ArrayList<>(ligen.values()),
                new ArrayList<>(altersklassen.values()), new ArrayList<>(mannschaften.values()),
                new ArrayList<>(schuetzen.values()), new ArrayList<>(wettkampftage.values()),
                new ArrayList<>(begegnungen.values()), new ArrayList<>(saisonSchuetzen.values()),
                new ArrayList<>(ergebnisse.values()));
    }

    private void saisonMeldungFallsNoetig(String schuetzeId, String tagId) {
        OnlineWettkampftag tag = vorhanden(wettkampftage, tagId, "Wettkampftag");
        String schluessel = saisonSchuetzeSchluessel(tag.saisonId(), schuetzeId);
        if (saisonSchuetzen.containsKey(schluessel)) return;
        OnlineSchuetze schuetze = vorhanden(schuetzen, schuetzeId, "Schuetze");
        OnlineMannschaft mannschaft = vorhanden(mannschaften, schuetze.mannschaftId(), "Mannschaft");
        OnlineAltersklasse klasse = vorhanden(altersklassen, schuetze.altersklasseId(), "Altersklasse");
        saisonSchuetzen.put(schluessel, new OnlineSaisonSchuetze(tag.saisonId(), schuetzeId,
                schuetze.vorname(), schuetze.nachname(), mannschaft.id(), mannschaft.name(), klasse.id(), klasse.name()));
    }

    private OnlineMannschaft onlineMitLiga(Mannschaft w) {
        OnlineLiga liga = w.getKlasse() == null ? null : ligen.get(w.getKlasse());
        return new OnlineMannschaft(w.getId(), w.getName(), w.getKlasse(), liga == null ? null : liga.name());
    }

    private void verschiebeLigenBeimEinfuegen(int rangfolge) {
        ligen.replaceAll((id, w) -> w.rangfolge() >= rangfolge ? new OnlineLiga(id, w.name(), w.rangfolge() + 1) : w);
    }

    private void verschiebeLigenBeimAendern(String id, int bisher, int neu) {
        ligen.replaceAll((andereId, w) -> {
            if (andereId.equals(id)) return w;
            if (neu < bisher && w.rangfolge() >= neu && w.rangfolge() < bisher)
                return new OnlineLiga(andereId, w.name(), w.rangfolge() + 1);
            if (neu > bisher && w.rangfolge() > bisher && w.rangfolge() <= neu)
                return new OnlineLiga(andereId, w.name(), w.rangfolge() - 1);
            return w;
        });
    }

    private List<Liga> sortierteLigen(java.util.Collection<OnlineLiga> werte) {
        return werte.stream().sorted(Comparator.comparingInt(OnlineLiga::rangfolge)
                .thenComparing(OnlineLiga::name, TEXT)).map(this::liga).toList();
    }

    private static <T> void fuellen(Map<String, T> ziel, List<T> werte, Function<T, String> id) {
        ziel.clear();
        werte.forEach(w -> ziel.put(id.apply(w), w));
    }

    private static <T> T vorhanden(Map<String, T> werte, String id, String bezeichnung) {
        T wert = werte.get(id);
        if (wert == null) throw new IllegalStateException(bezeichnung + " wurde nicht gefunden.");
        return wert;
    }

    private static void verwendet(String bezeichnung) {
        throw new IllegalStateException(bezeichnung + " wird noch verwendet und kann nicht geloescht werden.");
    }

    private static String ergebnisSchluessel(String schuetzeId, String tagId) { return schuetzeId + "\0" + tagId; }
    private static String saisonSchuetzeSchluessel(String saisonId, String schuetzeId) { return saisonId + "\0" + schuetzeId; }

    private Saison saison(OnlineSaison w) { return new Saison(w.id(), w.name()); }
    private Saison saisonOderNull(OnlineSaison w) { return w == null ? null : saison(w); }
    private Wettkampftage wettkampftag(OnlineWettkampftag w) { return new Wettkampftage(w.id(),w.datum(),w.ausrichterverein(),w.saisonId()); }
    private OnlineWettkampftag online(Wettkampftage w) { return new OnlineWettkampftag(w.getId(),w.getDatum(),w.getAusrichterVerein(),w.getSaisonID()); }
    private Mannschaft mannschaft(OnlineMannschaft w) { return new Mannschaft(w.id(),w.name(),w.klasse(),w.ligaName()); }
    private Schuetze schuetze(OnlineSchuetze w) { return new Schuetze(w.id(),w.vorname(),w.nachname(),w.mannschaftId(),w.altersklasseId()); }
    private OnlineSchuetze online(Schuetze w) { return new OnlineSchuetze(w.getId(),w.getVorname(),w.getNachname(),w.getMannschaftid(),w.getAltersKlasse()); }
    private Altersklasse altersklasse(OnlineAltersklasse w) { return new Altersklasse(w.id(),w.name()); }
    private OnlineAltersklasse online(Altersklasse w) { return new OnlineAltersklasse(w.getId(),w.getKlassenName()); }
    private Liga liga(OnlineLiga w) { return new Liga(w.id(),w.name(),w.rangfolge()); }
    private OnlineLiga online(Liga w) { return new OnlineLiga(w.getId(),w.getLigaName(),w.getRangfolge()); }
    private Begegnung begegnung(OnlineBegegnung w) { return new Begegnung(w.id(),w.heim(),w.gegner(),w.wettkampftagId(),w.liga(),w.ligaName(),w.heimName(),w.gegnerName()); }
    private Ergebnisse ergebnis(OnlineErgebnis w) { return new Ergebnisse(w.id(),w.schuetzeId(),w.wettkampftagId(),w.wert()); }
    private SaisonSchuetze saisonSchuetze(OnlineSaisonSchuetze w) { return new SaisonSchuetze(w.saisonId(),w.schuetzeId(),w.vorname(),w.nachname(),w.mannschaftId(),w.mannschaftName(),w.altersklasseId(),w.altersklasseName()); }
    private OnlineSaisonSchuetze online(SaisonSchuetze w) { return new OnlineSaisonSchuetze(w.getSaisonID(),w.getSchuetzeID(),w.getVorname(),w.getNachname(),w.getMannschaftID(),w.getMannschaftName(),w.getAltersklasseID(),w.getAltersklasseName()); }

    @Override
    public void close() {
        geschlossen = true;
        hintergrund.shutdownNow();
        apiClient.close();
        Arrays.fill(kontoPasswort, '\0');
    }

    public record OnlineStatus(boolean verbunden, long ausstehendeAenderungen,
            String synchronisationsfehler, boolean versionskonflikt,
            boolean konfliktloesungLaeuft) { }
}
