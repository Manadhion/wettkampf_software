package io.github.manadhion.wettkampf.app;

import java.time.LocalDate;
import java.util.List;

/** Für Tests austauschbarer Vertrag des technischen Online-Zugriffs. */
public interface OnlineApi extends AutoCloseable {

    void anmelden(String name, char[] passwort);
    default OnlineSnapshot snapshotLaden() { throw fehlt(); }
    default long snapshotSpeichern(OnlineSnapshot snapshot) { throw fehlt(); }
    default void statusPruefen() { throw fehlt(); }
    List<OnlineSaison> alleSaisons();
    OnlineSaison saisonAnlegen(int name);
    OnlineSaison saisonAktualisieren(String id, int name, long version);
    void saisonLoeschen(String id, long version);

    default List<OnlineLiga> ligen(String saisonId) { throw fehlt(); }
    default OnlineLiga liga(String id) { throw fehlt(); }
    default int naechsteLigaRangfolge() { throw fehlt(); }
    default void ligaAnlegen(OnlineLiga wert) { throw fehlt(); }
    default void ligaAktualisieren(OnlineLiga wert) { throw fehlt(); }
    default void ligaLoeschen(String id) { throw fehlt(); }

    default List<OnlineAltersklasse> altersklassen() { throw fehlt(); }
    default void altersklasseAnlegen(OnlineAltersklasse wert) { throw fehlt(); }
    default void altersklasseAktualisieren(OnlineAltersklasse wert) { throw fehlt(); }
    default void altersklasseLoeschen(String id) { throw fehlt(); }

    default List<OnlineMannschaft> mannschaften(String saisonId) { throw fehlt(); }
    default OnlineMannschaft mannschaft(String id) { throw fehlt(); }
    default void mannschaftAnlegen(OnlineMannschaft wert) { throw fehlt(); }
    default void mannschaftAktualisieren(OnlineMannschaft wert) { throw fehlt(); }
    default void mannschaftLoeschen(String id) { throw fehlt(); }

    default List<OnlineSchuetze> schuetzen(String mannschaftId) { throw fehlt(); }
    default void schuetzeAnlegen(OnlineSchuetze wert) { throw fehlt(); }
    default void schuetzeAktualisieren(OnlineSchuetze wert) { throw fehlt(); }
    default void schuetzeLoeschen(String id) { throw fehlt(); }

    default List<OnlineWettkampftag> wettkampftage(String saisonId) { throw fehlt(); }
    default void wettkampftagAnlegen(OnlineWettkampftag wert) { throw fehlt(); }
    default void wettkampftagAktualisieren(OnlineWettkampftag wert) { throw fehlt(); }
    default void wettkampftagLoeschen(String id) { throw fehlt(); }

    default List<OnlineBegegnung> begegnungen(String wettkampftagId) { throw fehlt(); }
    default boolean begegnungExistiert(String tagId, String mannschaftA, String mannschaftB) { throw fehlt(); }
    default void begegnungAnlegen(OnlineBegegnung wert) { throw fehlt(); }
    default void begegnungLoeschen(String id) { throw fehlt(); }

    default OnlineErgebnis ergebnis(String schuetzeId, String wettkampftagId) { throw fehlt(); }
    default boolean ergebnisSpeichern(OnlineErgebnis wert) { throw fehlt(); }
    default int gesamtErgebnis(String mannschaftId, String wettkampftagId) { throw fehlt(); }

    default List<OnlineSaisonSchuetze> saisonSchuetzen(String saisonId, String mannschaftId) { throw fehlt(); }
    default OnlineSaisonSchuetze saisonSchuetze(String saisonId, String schuetzeId) { throw fehlt(); }
    default void saisonSchuetzeSpeichern(OnlineSaisonSchuetze wert) { throw fehlt(); }

    @Override void close();

    private static UnsupportedOperationException fehlt() {
        return new UnsupportedOperationException("API-Funktion im Test nicht bereitgestellt.");
    }

    record OnlineSaison(String id, int name, long version) {
    }

    record OnlineLiga(String id, String name, int rangfolge) { }
    record OnlineAltersklasse(String id, String name) { }
    record OnlineMannschaft(String id, String name, String klasse, String ligaName) { }
    record OnlineSchuetze(String id, String vorname, String nachname,
            String mannschaftId, String altersklasseId) { }
    record OnlineWettkampftag(String id, LocalDate datum, String ausrichterverein,
            String saisonId) { }
    record OnlineBegegnung(String id, String heim, String gegner, String wettkampftagId,
            String liga, String ligaName, String heimName, String gegnerName) { }
    record OnlineErgebnis(String id, String schuetzeId, String wettkampftagId, int wert) { }
    record OnlineSaisonSchuetze(String saisonId, String schuetzeId, String vorname,
            String nachname, String mannschaftId, String mannschaftName,
            String altersklasseId, String altersklasseName) { }

    record OnlineSnapshot(long revision,
            List<OnlineSaison> saisons, List<OnlineLiga> ligen,
            List<OnlineAltersklasse> altersklassen, List<OnlineMannschaft> mannschaften,
            List<OnlineSchuetze> schuetzen, List<OnlineWettkampftag> wettkampftage,
            List<OnlineBegegnung> begegnungen, List<OnlineSaisonSchuetze> saisonSchuetzen,
            List<OnlineErgebnis> ergebnisse) {
        public OnlineSnapshot {
            saisons = List.copyOf(saisons);
            ligen = List.copyOf(ligen);
            altersklassen = List.copyOf(altersklassen);
            mannschaften = List.copyOf(mannschaften);
            schuetzen = List.copyOf(schuetzen);
            wettkampftage = List.copyOf(wettkampftage);
            begegnungen = List.copyOf(begegnungen);
            saisonSchuetzen = List.copyOf(saisonSchuetzen);
            ergebnisse = List.copyOf(ergebnisse);
        }
    }
}
