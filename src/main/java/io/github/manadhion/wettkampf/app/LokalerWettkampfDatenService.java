package io.github.manadhion.wettkampf.app;

import java.util.List;

import io.github.manadhion.wettkampf.dao.AltersklasseDAO;
import io.github.manadhion.wettkampf.dao.BegegnungDAO;
import io.github.manadhion.wettkampf.dao.ErgebnisseDAO;
import io.github.manadhion.wettkampf.dao.LigaDAO;
import io.github.manadhion.wettkampf.dao.MannschaftDAO;
import io.github.manadhion.wettkampf.dao.SaisonDAO;
import io.github.manadhion.wettkampf.dao.SaisonSchuetzeDAO;
import io.github.manadhion.wettkampf.dao.SchuetzeDAO;
import io.github.manadhion.wettkampf.dao.WettkampftageDAO;
import io.github.manadhion.wettkampf.data.Altersklasse;
import io.github.manadhion.wettkampf.data.Begegnung;
import io.github.manadhion.wettkampf.data.Ergebnisse;
import io.github.manadhion.wettkampf.data.Liga;
import io.github.manadhion.wettkampf.data.Mannschaft;
import io.github.manadhion.wettkampf.data.Saison;
import io.github.manadhion.wettkampf.data.SaisonSchuetze;
import io.github.manadhion.wettkampf.data.Schuetze;
import io.github.manadhion.wettkampf.data.Wettkampftage;

/** Lokale Implementierung des Datenservices über die bestehenden SQLite-DAOs. */
public final class LokalerWettkampfDatenService implements WettkampfDatenService {

    @Override
    public void initialisieren() {
        new MannschaftDAO().createTableIfNotExists();
        new BegegnungDAO().createTableIfNotExists();
        new ErgebnisseDAO().createTableIfNotExists();
        new SchuetzeDAO().createTableIfNotExists();
        new WettkampftageDAO().createTableIfNotExists();
        new SaisonDAO().createTableIfNotExists();
        new LigaDAO().createTableIfNotExists();
        new AltersklasseDAO().createTableIfNotExists();
        new SaisonSchuetzeDAO().createTableIfNotExists();
    }

    @Override public List<Wettkampftage> alleWettkampfTage() { return new WettkampftageDAO().alleTage(); }
    @Override public List<Wettkampftage> wettkampftageVonSaison(String id) { return new WettkampftageDAO().tageVonSaison(id); }
    @Override public void wettkampftagAnlegen(Wettkampftage wert) { new WettkampftageDAO().insert(wert); }
    @Override public void wettkampftagAktualisieren(Wettkampftage wert) { new WettkampftageDAO().update(wert); }
    @Override public int wettkampftagLoeschen(String id) { return new WettkampftageDAO().delete(id); }

    @Override public List<Mannschaft> alleMannschaften() { return new MannschaftDAO().alleMannschaften(); }
    @Override public List<Mannschaft> mannschaftenVonSaison(String id) { return new MannschaftDAO().mannschaftenVonSaison(id); }
    @Override public Mannschaft mannschaftMitId(String id) { return new MannschaftDAO().mannschaftMitID(id); }
    @Override public void mannschaftAnlegen(Mannschaft wert) { new MannschaftDAO().insert(wert); }
    @Override public void mannschaftAktualisieren(Mannschaft wert) { new MannschaftDAO().update(wert); }
    @Override public int mannschaftLoeschen(String id) { return new MannschaftDAO().delete(id); }

    @Override public List<Schuetze> schuetzenVonMannschaft(String id) { return new SchuetzeDAO().schuetzenVonMannschaft(id); }
    @Override public void schuetzeAnlegen(Schuetze wert) { new SchuetzeDAO().insert(wert); }
    @Override public void schuetzeAktualisieren(Schuetze wert) { new SchuetzeDAO().update(wert); }
    @Override public int schuetzeLoeschen(String id) { return new SchuetzeDAO().delete(id); }

    @Override public List<Altersklasse> alleAltersklassen() { return new AltersklasseDAO().alleAltersklassen(); }
    @Override public void altersklasseAnlegen(Altersklasse wert) { new AltersklasseDAO().insert(wert); }
    @Override public void altersklasseAktualisieren(Altersklasse wert) { new AltersklasseDAO().update(wert); }
    @Override public int altersklasseLoeschen(String id) { return new AltersklasseDAO().delete(id); }

    @Override public Ergebnisse ergebnisFuer(String schuetzeId, String tagId) { return new ErgebnisseDAO().ergebnisFuerSchuetzeUndTag(schuetzeId, tagId); }
    @Override public int gesamtErgebnisBeste3(String mannschaftId, String tagId) { return new ErgebnisseDAO().gesamtErgebnisBeste3(mannschaftId, tagId); }
    @Override public boolean ergebnisSpeichern(String schuetzeId, String tagId, int wert) {
        ErgebnisseDAO dao = new ErgebnisseDAO();
        Ergebnisse vorhanden = dao.ergebnisFuerSchuetzeUndTag(schuetzeId, tagId);
        if (vorhanden == null) {
            dao.insert(new Ergebnisse(schuetzeId, tagId, wert));
            return true;
        }
        vorhanden.setErgebnis(wert);
        dao.update(vorhanden);
        return false;
    }

    @Override public List<Liga> alleLigen() { return new LigaDAO().alleLigen(); }
    @Override public List<Liga> ligenVonSaison(String id) { return new LigaDAO().ligenVonSaison(id); }
    @Override public int naechsteLigaRangfolge() { return new LigaDAO().naechsteRangfolge(); }
    @Override public Liga ligaMitId(String id) { return new LigaDAO().ligaMitIdFinden(id); }
    @Override public void ligaAnlegen(Liga wert) { new LigaDAO().insert(wert); }
    @Override public void ligaAktualisieren(Liga wert) { new LigaDAO().update(wert); }
    @Override public int ligaLoeschen(String id) { return new LigaDAO().delete(id); }

    @Override public List<SaisonSchuetze> saisonSchuetzenVonMannschaft(String saisonId, String mannschaftId) { return new SaisonSchuetzeDAO().schuetzenVonMannschaft(saisonId, mannschaftId); }
    @Override public SaisonSchuetze saisonSchuetzeFinden(String saisonId, String schuetzeId) { return new SaisonSchuetzeDAO().finde(saisonId, schuetzeId); }
    @Override public void saisonSchuetzeSpeichern(SaisonSchuetze wert) { new SaisonSchuetzeDAO().speichern(wert); }

    @Override public List<Saison> alleSaisons() { return new SaisonDAO().alleSaisons(); }
    @Override public Saison saisonMitId(String id) { return new SaisonDAO().saisonMitId(id); }
    @Override public boolean saisonExistiert(int name) { return new SaisonDAO().existiert(name); }
    @Override public void saisonAnlegen(Saison wert) { new SaisonDAO().insert(wert); }
    @Override public void saisonAktualisieren(Saison wert) { new SaisonDAO().update(wert); }
    @Override public int saisonLoeschen(String id) { return new SaisonDAO().delete(id); }

    @Override public List<Begegnung> begegnungenAnDiesemTag(String id) { return new BegegnungDAO().begegnungenAnDiesemTag(id); }
    @Override public boolean begegnungExistiert(String tagId, String a, String b) { return new BegegnungDAO().existiert(tagId, a, b); }
    @Override public void begegnungAnlegen(Begegnung wert) { new BegegnungDAO().insert(wert); }
    @Override public int begegnungLoeschen(String id) { return new BegegnungDAO().delete(id); }
}
