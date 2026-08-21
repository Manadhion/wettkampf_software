package io.github.manadhion.wettkampf.server.fachdaten;

import java.sql.Date;
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
import io.github.manadhion.wettkampf.server.web.NichtGefundenException;

@Service
public class FachdatenService {

    private final JdbcTemplate jdbc;

    public FachdatenService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public List<Liga> ligen(UUID saisonId) {
        if (saisonId == null) {
            return jdbc.query("SELECT id,name,rangfolge FROM liga ORDER BY rangfolge,LOWER(name)",
                    (rs, z) -> new Liga(uuid(rs, "id"), rs.getString("name"),
                            rs.getInt("rangfolge")));
        }
        return jdbc.query("""
                SELECT DISTINCT COALESCE(b.liga,m.klasse) id,
                       COALESCE(b.liga_name,l.name) name,
                       COALESCE(l.rangfolge,999) rangfolge
                FROM begegnung b
                JOIN wettkampftag w ON w.id=b.wettkampftag_id
                JOIN mannschaft m ON m.id=b.heim
                LEFT JOIN liga l ON l.id=COALESCE(b.liga,m.klasse)
                WHERE w.saison_id=?
                ORDER BY rangfolge,name
                """, (rs, z) -> new Liga(uuid(rs, "id"), rs.getString("name"),
                        rs.getInt("rangfolge")), saisonId);
    }

    @Transactional(readOnly = true)
    public Liga liga(UUID id) {
        return jdbc.query("SELECT id,name,rangfolge FROM liga WHERE id=?",
                (rs, z) -> new Liga(uuid(rs, "id"), rs.getString("name"),
                        rs.getInt("rangfolge")), id).stream().findFirst()
                .orElseThrow(() -> nichtGefunden("Liga"));
    }

    @Transactional(readOnly = true)
    public int naechsteLigaRangfolge() {
        Integer wert = jdbc.queryForObject(
                "SELECT COALESCE(MAX(rangfolge),0)+1 FROM liga", Integer.class);
        return wert == null ? 1 : wert;
    }

    @Transactional
    public Liga ligaAnlegen(Liga liga) {
        jdbc.update("UPDATE liga SET rangfolge=rangfolge+1 WHERE rangfolge>=?",
                liga.rangfolge());
        jdbc.update("INSERT INTO liga(id,name,rangfolge) VALUES(?,?,?)",
                liga.id(), liga.name(), liga.rangfolge());
        return liga;
    }

    @Transactional
    public Liga ligaAktualisieren(UUID id, Liga liga) {
        int bisher = jdbc.query("SELECT rangfolge FROM liga WHERE id=?",
                (rs, z) -> rs.getInt(1), id).stream().findFirst()
                .orElseThrow(() -> nichtGefunden("Liga"));
        if (liga.rangfolge() < bisher) {
            jdbc.update("UPDATE liga SET rangfolge=rangfolge+1 WHERE id<>? AND rangfolge>=? AND rangfolge<?",
                    id, liga.rangfolge(), bisher);
        } else if (liga.rangfolge() > bisher) {
            jdbc.update("UPDATE liga SET rangfolge=rangfolge-1 WHERE id<>? AND rangfolge>? AND rangfolge<=?",
                    id, bisher, liga.rangfolge());
        }
        jdbc.update("UPDATE liga SET name=?,rangfolge=? WHERE id=?",
                liga.name(), liga.rangfolge(), id);
        return new Liga(id, liga.name(), liga.rangfolge());
    }

    @Transactional
    public void ligaLoeschen(UUID id) {
        loeschen("DELETE FROM liga WHERE id=?", id, "Liga");
    }

    @Transactional(readOnly = true)
    public List<Altersklasse> altersklassen() {
        return jdbc.query("SELECT id,name FROM altersklasse ORDER BY LOWER(name)",
                (rs, z) -> new Altersklasse(uuid(rs, "id"), rs.getString("name")));
    }

    @Transactional
    public Altersklasse altersklasseAnlegen(Altersklasse wert) {
        jdbc.update("INSERT INTO altersklasse(id,name) VALUES(?,?)", wert.id(), wert.name());
        return wert;
    }

    @Transactional
    public Altersklasse altersklasseAktualisieren(UUID id, Altersklasse wert) {
        aktualisieren("UPDATE altersklasse SET name=? WHERE id=?", "Altersklasse",
                wert.name(), id);
        return new Altersklasse(id, wert.name());
    }

    @Transactional
    public void altersklasseLoeschen(UUID id) {
        loeschen("DELETE FROM altersklasse WHERE id=?", id, "Altersklasse");
    }

    @Transactional(readOnly = true)
    public List<Mannschaft> mannschaften(UUID saisonId) {
        String basis = """
                SELECT m.id,m.name,m.klasse,l.name liga_name
                FROM mannschaft m LEFT JOIN liga l ON l.id=m.klasse
                """;
        if (saisonId == null) {
            return jdbc.query(basis + " ORDER BY LOWER(m.name)", this::mapMannschaft);
        }
        return jdbc.query(basis + """
                WHERE m.id IN (
                  SELECT b.heim FROM begegnung b JOIN wettkampftag w ON w.id=b.wettkampftag_id WHERE w.saison_id=?
                  UNION
                  SELECT b.gegner FROM begegnung b JOIN wettkampftag w ON w.id=b.wettkampftag_id WHERE w.saison_id=?
                ) ORDER BY LOWER(m.name)
                """, this::mapMannschaft, saisonId, saisonId);
    }

    @Transactional(readOnly = true)
    public Mannschaft mannschaft(UUID id) {
        return jdbc.query("""
                SELECT m.id,m.name,m.klasse,l.name liga_name
                FROM mannschaft m LEFT JOIN liga l ON l.id=m.klasse WHERE m.id=?
                """, this::mapMannschaft, id).stream().findFirst()
                .orElseThrow(() -> nichtGefunden("Mannschaft"));
    }

    @Transactional
    public Mannschaft mannschaftAnlegen(Mannschaft wert) {
        jdbc.update("INSERT INTO mannschaft(id,name,klasse) VALUES(?,?,?)",
                wert.id(), wert.name(), wert.klasse());
        return mannschaft(wert.id());
    }

    @Transactional
    public Mannschaft mannschaftAktualisieren(UUID id, Mannschaft wert) {
        aktualisieren("UPDATE mannschaft SET name=?,klasse=? WHERE id=?", "Mannschaft",
                wert.name(), wert.klasse(), id);
        return mannschaft(id);
    }

    @Transactional
    public void mannschaftLoeschen(UUID id) {
        loeschen("DELETE FROM mannschaft WHERE id=?", id, "Mannschaft");
    }

    @Transactional(readOnly = true)
    public List<Schuetze> schuetzen(UUID mannschaftId) {
        return jdbc.query("""
                SELECT id,vorname,nachname,mannschaft_id,altersklasse_id
                FROM schuetze WHERE mannschaft_id=? ORDER BY LOWER(vorname),LOWER(nachname)
                """, (rs, z) -> new Schuetze(uuid(rs, "id"), rs.getString("vorname"),
                        rs.getString("nachname"), uuid(rs, "mannschaft_id"),
                        uuid(rs, "altersklasse_id")), mannschaftId);
    }

    @Transactional
    public Schuetze schuetzeAnlegen(Schuetze wert) {
        jdbc.update("INSERT INTO schuetze(id,vorname,nachname,mannschaft_id,altersklasse_id) VALUES(?,?,?,?,?)",
                wert.id(), wert.vorname(), wert.nachname(), wert.mannschaftId(),
                wert.altersklasseId());
        return wert;
    }

    @Transactional
    public Schuetze schuetzeAktualisieren(UUID id, Schuetze wert) {
        aktualisieren("UPDATE schuetze SET vorname=?,nachname=?,mannschaft_id=?,altersklasse_id=? WHERE id=?",
                "Schütze", wert.vorname(), wert.nachname(), wert.mannschaftId(),
                wert.altersklasseId(), id);
        return new Schuetze(id, wert.vorname(), wert.nachname(), wert.mannschaftId(),
                wert.altersklasseId());
    }

    @Transactional
    public void schuetzeLoeschen(UUID id) {
        loeschen("DELETE FROM schuetze WHERE id=?", id, "Schütze");
    }

    @Transactional(readOnly = true)
    public List<Wettkampftag> wettkampftage(UUID saisonId) {
        String sql = "SELECT id,datum,ausrichterverein,saison_id FROM wettkampftag";
        if (saisonId == null) {
            return jdbc.query(sql + " ORDER BY datum", this::mapWettkampftag);
        }
        return jdbc.query(sql + " WHERE saison_id=? ORDER BY datum", this::mapWettkampftag,
                saisonId);
    }

    @Transactional
    public Wettkampftag wettkampftagAnlegen(Wettkampftag wert) {
        jdbc.update("INSERT INTO wettkampftag(id,datum,ausrichterverein,saison_id) VALUES(?,?,?,?)",
                wert.id(), Date.valueOf(wert.datum()), wert.ausrichterverein(), wert.saisonId());
        return wert;
    }

    @Transactional
    public Wettkampftag wettkampftagAktualisieren(UUID id, Wettkampftag wert) {
        aktualisieren("UPDATE wettkampftag SET datum=?,ausrichterverein=?,saison_id=? WHERE id=?",
                "Wettkampftag", Date.valueOf(wert.datum()), wert.ausrichterverein(),
                wert.saisonId(), id);
        return new Wettkampftag(id, wert.datum(), wert.ausrichterverein(), wert.saisonId());
    }

    @Transactional
    public void wettkampftagLoeschen(UUID id) {
        loeschen("DELETE FROM wettkampftag WHERE id=?", id, "Wettkampftag");
    }

    @Transactional(readOnly = true)
    public List<Begegnung> begegnungen(UUID wettkampftagId) {
        return jdbc.query("""
                SELECT id,heim,gegner,wettkampftag_id,liga,liga_name,heim_name,gegner_name
                FROM begegnung WHERE wettkampftag_id=? ORDER BY heim_name,gegner_name
                """, this::mapBegegnung, wettkampftagId);
    }

    @Transactional(readOnly = true)
    public boolean begegnungExistiert(UUID tag, UUID a, UUID b) {
        Integer anzahl = jdbc.queryForObject("""
                SELECT COUNT(*) FROM begegnung WHERE wettkampftag_id=?
                AND ((heim=? AND gegner=?) OR (heim=? AND gegner=?))
                """, Integer.class, tag, a, b, b, a);
        return anzahl != null && anzahl > 0;
    }

    @Transactional
    public Begegnung begegnungAnlegen(Begegnung wert) {
        int anzahl = jdbc.update("""
                INSERT INTO begegnung(id,heim,gegner,wettkampftag_id,liga,liga_name,heim_name,gegner_name)
                SELECT ?,h.id,g.id,?,h.klasse,l.name,h.name,g.name
                FROM mannschaft h JOIN mannschaft g ON g.id=?
                LEFT JOIN liga l ON l.id=h.klasse WHERE h.id=?
                """, wert.id(), wert.wettkampftagId(), wert.gegner(), wert.heim());
        if (anzahl == 0) {
            throw nichtGefunden("Mannschaft");
        }
        return begegnungen(wert.wettkampftagId()).stream()
                .filter(b -> b.id().equals(wert.id())).findFirst().orElseThrow();
    }

    @Transactional
    public void begegnungLoeschen(UUID id) {
        loeschen("DELETE FROM begegnung WHERE id=?", id, "Begegnung");
    }

    @Transactional(readOnly = true)
    public Ergebnis ergebnis(UUID schuetzeId, UUID tagId) {
        return jdbc.query("""
                SELECT id,schuetze_id,wettkampftag_id,wert FROM ergebnis
                WHERE schuetze_id=? AND wettkampftag_id=?
                """, this::mapErgebnis, schuetzeId, tagId).stream().findFirst().orElse(null);
    }

    @Transactional
    public boolean ergebnisSpeichern(Ergebnis wert) {
        boolean neu = ergebnis(wert.schuetzeId(), wert.wettkampftagId()) == null;
        jdbc.update("""
                INSERT INTO saison_schuetze(saison_id,schuetze_id,vorname,nachname,mannschaft_id,
                  mannschaft_name,altersklasse_id,altersklasse_name)
                SELECT w.saison_id,s.id,s.vorname,s.nachname,m.id,m.name,a.id,a.name
                FROM wettkampftag w JOIN schuetze s ON s.id=?
                JOIN mannschaft m ON m.id=s.mannschaft_id
                JOIN altersklasse a ON a.id=s.altersklasse_id WHERE w.id=?
                ON CONFLICT (saison_id,schuetze_id) DO NOTHING
                """, wert.schuetzeId(), wert.wettkampftagId());
        jdbc.update("""
                INSERT INTO ergebnis(id,schuetze_id,wettkampftag_id,wert) VALUES(?,?,?,?)
                ON CONFLICT (schuetze_id,wettkampftag_id) DO UPDATE SET wert=EXCLUDED.wert
                """, wert.id(), wert.schuetzeId(), wert.wettkampftagId(), wert.wert());
        return neu;
    }

    @Transactional(readOnly = true)
    public int gesamtErgebnis(UUID mannschaftId, UUID tagId) {
        Integer gesamt = jdbc.queryForObject("""
                SELECT COALESCE(SUM(wert),0) FROM (
                  SELECT e.wert FROM ergebnis e
                  JOIN wettkampftag w ON w.id=e.wettkampftag_id
                  JOIN saison_schuetze ss ON ss.saison_id=w.saison_id AND ss.schuetze_id=e.schuetze_id
                  WHERE ss.mannschaft_id=? AND e.wettkampftag_id=?
                  ORDER BY e.wert DESC LIMIT 3
                ) beste
                """, Integer.class, mannschaftId, tagId);
        return gesamt == null ? 0 : gesamt;
    }

    @Transactional(readOnly = true)
    public List<SaisonSchuetze> saisonSchuetzen(UUID saisonId, UUID mannschaftId) {
        return jdbc.query("""
                SELECT saison_id,schuetze_id,vorname,nachname,mannschaft_id,mannschaft_name,
                       altersklasse_id,altersklasse_name
                FROM saison_schuetze WHERE saison_id=? AND mannschaft_id=?
                ORDER BY LOWER(vorname),LOWER(nachname)
                """, this::mapSaisonSchuetze, saisonId, mannschaftId);
    }

    @Transactional(readOnly = true)
    public SaisonSchuetze saisonSchuetze(UUID saisonId, UUID schuetzeId) {
        return jdbc.query("""
                SELECT saison_id,schuetze_id,vorname,nachname,mannschaft_id,mannschaft_name,
                       altersklasse_id,altersklasse_name
                FROM saison_schuetze WHERE saison_id=? AND schuetze_id=?
                """, this::mapSaisonSchuetze, saisonId, schuetzeId).stream().findFirst().orElse(null);
    }

    @Transactional
    public SaisonSchuetze saisonSchuetzeSpeichern(SaisonSchuetze wert) {
        jdbc.update("""
                INSERT INTO saison_schuetze(saison_id,schuetze_id,vorname,nachname,mannschaft_id,
                  mannschaft_name,altersklasse_id,altersklasse_name) VALUES(?,?,?,?,?,?,?,?)
                ON CONFLICT (saison_id,schuetze_id) DO UPDATE SET
                  vorname=EXCLUDED.vorname,nachname=EXCLUDED.nachname,
                  mannschaft_id=EXCLUDED.mannschaft_id,mannschaft_name=EXCLUDED.mannschaft_name,
                  altersklasse_id=EXCLUDED.altersklasse_id,altersklasse_name=EXCLUDED.altersklasse_name
                """, wert.saisonId(), wert.schuetzeId(), wert.vorname(), wert.nachname(),
                wert.mannschaftId(), wert.mannschaftName(), wert.altersklasseId(),
                wert.altersklasseName());
        return wert;
    }

    private Mannschaft mapMannschaft(java.sql.ResultSet rs, int zeile) throws java.sql.SQLException {
        return new Mannschaft(uuid(rs, "id"), rs.getString("name"),
                nullableUuid(rs, "klasse"), rs.getString("liga_name"));
    }

    private Wettkampftag mapWettkampftag(java.sql.ResultSet rs, int zeile) throws java.sql.SQLException {
        return new Wettkampftag(uuid(rs, "id"), rs.getDate("datum").toLocalDate(),
                rs.getString("ausrichterverein"), uuid(rs, "saison_id"));
    }

    private Begegnung mapBegegnung(java.sql.ResultSet rs, int zeile) throws java.sql.SQLException {
        return new Begegnung(uuid(rs, "id"), uuid(rs, "heim"), uuid(rs, "gegner"),
                uuid(rs, "wettkampftag_id"), nullableUuid(rs, "liga"),
                rs.getString("liga_name"), rs.getString("heim_name"),
                rs.getString("gegner_name"));
    }

    private Ergebnis mapErgebnis(java.sql.ResultSet rs, int zeile) throws java.sql.SQLException {
        return new Ergebnis(uuid(rs, "id"), uuid(rs, "schuetze_id"),
                uuid(rs, "wettkampftag_id"), rs.getInt("wert"));
    }

    private SaisonSchuetze mapSaisonSchuetze(java.sql.ResultSet rs, int zeile)
            throws java.sql.SQLException {
        return new SaisonSchuetze(uuid(rs, "saison_id"), uuid(rs, "schuetze_id"),
                rs.getString("vorname"), rs.getString("nachname"),
                uuid(rs, "mannschaft_id"), rs.getString("mannschaft_name"),
                uuid(rs, "altersklasse_id"), rs.getString("altersklasse_name"));
    }

    private static UUID uuid(java.sql.ResultSet rs, String spalte) throws java.sql.SQLException {
        return rs.getObject(spalte, UUID.class);
    }

    private static UUID nullableUuid(java.sql.ResultSet rs, String spalte)
            throws java.sql.SQLException {
        return rs.getObject(spalte, UUID.class);
    }

    private void aktualisieren(String sql, String bezeichnung, Object... parameter) {
        if (jdbc.update(sql, parameter) == 0) {
            throw nichtGefunden(bezeichnung);
        }
    }

    private void loeschen(String sql, UUID id, String bezeichnung) {
        if (jdbc.update(sql, id) == 0) {
            throw nichtGefunden(bezeichnung);
        }
    }

    private NichtGefundenException nichtGefunden(String bezeichnung) {
        return new NichtGefundenException(bezeichnung + " wurde nicht gefunden.");
    }
}
