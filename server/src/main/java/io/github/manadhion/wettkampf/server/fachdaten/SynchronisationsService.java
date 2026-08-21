package io.github.manadhion.wettkampf.server.fachdaten;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.manadhion.wettkampf.server.fachdaten.FachdatenModelle.Altersklasse;
import io.github.manadhion.wettkampf.server.fachdaten.FachdatenModelle.Begegnung;
import io.github.manadhion.wettkampf.server.fachdaten.FachdatenModelle.Ergebnis;
import io.github.manadhion.wettkampf.server.fachdaten.FachdatenModelle.Liga;
import io.github.manadhion.wettkampf.server.fachdaten.FachdatenModelle.Mannschaft;
import io.github.manadhion.wettkampf.server.fachdaten.FachdatenModelle.SaisonSchuetze;
import io.github.manadhion.wettkampf.server.fachdaten.FachdatenModelle.Schuetze;
import io.github.manadhion.wettkampf.server.fachdaten.FachdatenModelle.Wettkampftag;
import io.github.manadhion.wettkampf.server.fachdaten.SynchronisationsSnapshot.SaisonEintrag;

@Service
public class SynchronisationsService {

    private final JdbcTemplate jdbc;

    public SynchronisationsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public SynchronisationsSnapshot laden() {
        List<SaisonEintrag> saisons = jdbc.query(
                "SELECT id,name,version FROM saison ORDER BY name DESC",
                (rs, z) -> new SaisonEintrag(uuid(rs, "id"), rs.getInt("name"),
                        rs.getLong("version")));
        List<Liga> ligen = jdbc.query("SELECT id,name,rangfolge FROM liga ORDER BY rangfolge,LOWER(name)",
                (rs, z) -> new Liga(uuid(rs, "id"), rs.getString("name"), rs.getInt("rangfolge")));
        List<Altersklasse> altersklassen = jdbc.query(
                "SELECT id,name FROM altersklasse ORDER BY LOWER(name)",
                (rs, z) -> new Altersklasse(uuid(rs, "id"), rs.getString("name")));
        List<Mannschaft> mannschaften = jdbc.query("""
                SELECT m.id,m.name,m.klasse,l.name liga_name
                FROM mannschaft m LEFT JOIN liga l ON l.id=m.klasse ORDER BY LOWER(m.name)
                """, (rs, z) -> new Mannschaft(uuid(rs, "id"), rs.getString("name"),
                        nullableUuid(rs, "klasse"), rs.getString("liga_name")));
        List<Schuetze> schuetzen = jdbc.query("""
                SELECT id,vorname,nachname,mannschaft_id,altersklasse_id FROM schuetze
                ORDER BY LOWER(vorname),LOWER(nachname)
                """, (rs, z) -> new Schuetze(uuid(rs, "id"), rs.getString("vorname"),
                        rs.getString("nachname"), uuid(rs, "mannschaft_id"),
                        uuid(rs, "altersklasse_id")));
        List<Wettkampftag> wettkampftage = jdbc.query("""
                SELECT id,datum,ausrichterverein,saison_id FROM wettkampftag ORDER BY datum
                """, this::wettkampftag);
        List<Begegnung> begegnungen = jdbc.query("""
                SELECT id,heim,gegner,wettkampftag_id,liga,liga_name,heim_name,gegner_name
                FROM begegnung ORDER BY wettkampftag_id,heim_name,gegner_name
                """, this::begegnung);
        List<SaisonSchuetze> saisonSchuetzen = jdbc.query("""
                SELECT saison_id,schuetze_id,vorname,nachname,mannschaft_id,mannschaft_name,
                       altersklasse_id,altersklasse_name FROM saison_schuetze
                ORDER BY saison_id,mannschaft_id,LOWER(vorname),LOWER(nachname)
                """, this::saisonSchuetze);
        List<Ergebnis> ergebnisse = jdbc.query("""
                SELECT id,schuetze_id,wettkampftag_id,wert FROM ergebnis ORDER BY id
                """, this::ergebnis);
        return new SynchronisationsSnapshot(saisons, ligen, altersklassen, mannschaften,
                schuetzen, wettkampftage, begegnungen, saisonSchuetzen, ergebnisse);
    }

    /** Ersetzt alle Fachdaten in einer Transaktion; Konto und Sitzungen bleiben erhalten. */
    @Transactional
    public void speichern(SynchronisationsSnapshot snapshot) {
        jdbc.execute("TRUNCATE TABLE ergebnis,saison_schuetze,begegnung,wettkampftag,"
                + "schuetze,mannschaft,altersklasse,liga,saison");
        snapshot.saisons().forEach(w -> jdbc.update(
                "INSERT INTO saison(id,name,version) VALUES(?,?,?)", w.id(), w.name(), w.version()));
        snapshot.ligen().forEach(w -> jdbc.update(
                "INSERT INTO liga(id,name,rangfolge) VALUES(?,?,?)", w.id(), w.name(), w.rangfolge()));
        snapshot.altersklassen().forEach(w -> jdbc.update(
                "INSERT INTO altersklasse(id,name) VALUES(?,?)", w.id(), w.name()));
        snapshot.mannschaften().forEach(w -> jdbc.update(
                "INSERT INTO mannschaft(id,name,klasse) VALUES(?,?,?)", w.id(), w.name(), w.klasse()));
        snapshot.schuetzen().forEach(w -> jdbc.update("""
                INSERT INTO schuetze(id,vorname,nachname,mannschaft_id,altersklasse_id)
                VALUES(?,?,?,?,?)
                """, w.id(), w.vorname(), w.nachname(), w.mannschaftId(), w.altersklasseId()));
        snapshot.wettkampftage().forEach(w -> jdbc.update("""
                INSERT INTO wettkampftag(id,datum,ausrichterverein,saison_id) VALUES(?,?,?,?)
                """, w.id(), Date.valueOf(w.datum()), w.ausrichterverein(), w.saisonId()));
        snapshot.begegnungen().forEach(w -> jdbc.update("""
                INSERT INTO begegnung(id,heim,gegner,wettkampftag_id,liga,liga_name,heim_name,gegner_name)
                VALUES(?,?,?,?,?,?,?,?)
                """, w.id(), w.heim(), w.gegner(), w.wettkampftagId(), w.liga(),
                w.ligaName(), w.heimName(), w.gegnerName()));
        snapshot.saisonSchuetzen().forEach(w -> jdbc.update("""
                INSERT INTO saison_schuetze(saison_id,schuetze_id,vorname,nachname,mannschaft_id,
                  mannschaft_name,altersklasse_id,altersklasse_name) VALUES(?,?,?,?,?,?,?,?)
                """, w.saisonId(), w.schuetzeId(), w.vorname(), w.nachname(),
                w.mannschaftId(), w.mannschaftName(), w.altersklasseId(), w.altersklasseName()));
        snapshot.ergebnisse().forEach(w -> jdbc.update("""
                INSERT INTO ergebnis(id,schuetze_id,wettkampftag_id,wert) VALUES(?,?,?,?)
                """, w.id(), w.schuetzeId(), w.wettkampftagId(), w.wert()));
    }

    private Wettkampftag wettkampftag(ResultSet rs, int zeile) throws SQLException {
        return new Wettkampftag(uuid(rs, "id"), rs.getDate("datum").toLocalDate(),
                rs.getString("ausrichterverein"), uuid(rs, "saison_id"));
    }

    private Begegnung begegnung(ResultSet rs, int zeile) throws SQLException {
        return new Begegnung(uuid(rs, "id"), uuid(rs, "heim"), uuid(rs, "gegner"),
                uuid(rs, "wettkampftag_id"), nullableUuid(rs, "liga"),
                rs.getString("liga_name"), rs.getString("heim_name"), rs.getString("gegner_name"));
    }

    private SaisonSchuetze saisonSchuetze(ResultSet rs, int zeile) throws SQLException {
        return new SaisonSchuetze(uuid(rs, "saison_id"), uuid(rs, "schuetze_id"),
                rs.getString("vorname"), rs.getString("nachname"), uuid(rs, "mannschaft_id"),
                rs.getString("mannschaft_name"), uuid(rs, "altersklasse_id"),
                rs.getString("altersklasse_name"));
    }

    private Ergebnis ergebnis(ResultSet rs, int zeile) throws SQLException {
        return new Ergebnis(uuid(rs, "id"), uuid(rs, "schuetze_id"),
                uuid(rs, "wettkampftag_id"), rs.getInt("wert"));
    }

    private static UUID uuid(ResultSet rs, String spalte) throws SQLException {
        return rs.getObject(spalte, UUID.class);
    }

    private static UUID nullableUuid(ResultSet rs, String spalte) throws SQLException {
        return rs.getObject(spalte, UUID.class);
    }
}
