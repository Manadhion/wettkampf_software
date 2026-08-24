package io.github.manadhion.wettkampf.app;

import java.util.List;

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
 * Fachlicher Zugang zu einem Wettkampf-Datenbestand. Die Schnittstelle kennt weder
 * SQLite-Verbindungen noch HTTP-Anfragen.
 */
public interface WettkampfDatenService {

    void initialisieren();

    List<Wettkampftage> alleWettkampfTage();
    List<Wettkampftage> wettkampftageVonSaison(String saisonId);
    void wettkampftagAnlegen(Wettkampftage wettkampftag);
    void wettkampftagAktualisieren(Wettkampftage wettkampftag);
    int wettkampftagLoeschen(String id);

    List<Mannschaft> alleMannschaften();
    List<Mannschaft> mannschaftenVonSaison(String saisonId);
    Mannschaft mannschaftMitId(String id);
    void mannschaftAnlegen(Mannschaft mannschaft);
    void mannschaftAktualisieren(Mannschaft mannschaft);
    int mannschaftLoeschen(String id);

    List<Schuetze> schuetzenVonMannschaft(String mannschaftId);
    void schuetzeAnlegen(Schuetze schuetze);
    void schuetzeAktualisieren(Schuetze schuetze);
    int schuetzeLoeschen(String id);

    List<Altersklasse> alleAltersklassen();
    void altersklasseAnlegen(Altersklasse altersklasse);
    void altersklasseAktualisieren(Altersklasse altersklasse);
    int altersklasseLoeschen(String id);

    Ergebnisse ergebnisFuer(String schuetzeId, String wettkampftagId);
    int gesamtErgebnisBeste3(String mannschaftId, String wettkampftagId);

    /** Speichert ein Ergebnis und gibt {@code true} zurück, wenn es neu angelegt wurde. */
    boolean ergebnisSpeichern(String schuetzeId, String wettkampftagId, int wert);

    List<Liga> alleLigen();
    List<Liga> ligenVonSaison(String saisonId);
    int naechsteLigaRangfolge();
    Liga ligaMitId(String id);
    void ligaAnlegen(Liga liga);
    void ligaAktualisieren(Liga liga);
    int ligaLoeschen(String id);

    List<SaisonSchuetze> saisonSchuetzenVonMannschaft(String saisonId, String mannschaftId);
    SaisonSchuetze saisonSchuetzeFinden(String saisonId, String schuetzeId);
    void saisonSchuetzeSpeichern(SaisonSchuetze meldung);

    List<Saison> alleSaisons();
    Saison saisonMitId(String id);
    boolean saisonExistiert(int name);
    void saisonAnlegen(Saison saison);
    void saisonAktualisieren(Saison saison);
    int saisonLoeschen(String id);

    List<Begegnung> begegnungenAnDiesemTag(String wettkampftagId);
    boolean begegnungExistiert(String tagId, String mannschaftA, String mannschaftB);
    void begegnungAnlegen(Begegnung begegnung);
    int begegnungLoeschen(String id);
}
