package io.github.manadhion.wettkampf.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import io.github.manadhion.wettkampf.app.DBController;
import io.github.manadhion.wettkampf.data.SaisonSchuetze;

/**
 * Verwaltet unveränderliche Schützenmeldungen je Saison.
 */
public class SaisonSchuetzeDAO {

    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS saison_schuetze ("
                + "saisonID TEXT NOT NULL REFERENCES saison(id),"
                + "schuetzeID TEXT NOT NULL REFERENCES schuetze(id),"
                + "vorname TEXT NOT NULL, nachname TEXT NOT NULL,"
                + "mannschaftID TEXT NOT NULL REFERENCES mannschaft(id),"
                + "mannschaftName TEXT NOT NULL,"
                + "altersklasseID TEXT NOT NULL REFERENCES altersklasse(id),"
                + "altersklasseName TEXT NOT NULL,"
                + "PRIMARY KEY (saisonID, schuetzeID))";
        try (Connection con = DBController.getConnection(); Statement stmt = con.createStatement()) {
            stmt.execute(sql);
            //Migration alter Datenbanken: vorhandene Ergebnisse mit dem derzeitigen Stamm vorbelegen.
            //Eine fachlich bekannte ältere Zuordnung kann anschließend gezielt korrigiert werden.
            stmt.executeUpdate("INSERT OR IGNORE INTO saison_schuetze "
                    + "SELECT DISTINCT w.saisonID,s.id,s.vorname,s.nachname,m.id,m.name,a.id,a.name "
                    + "FROM ergebnisse e JOIN wettkampftage w ON w.id=e.wettkampftagID "
                    + "JOIN schuetze s ON s.id=e.schuetzeID "
                    + "JOIN mannschaft m ON m.id=s.mannschaftid "
                    + "JOIN altersklasse a ON a.id=s.altersKlasse");
        } catch (SQLException e) {
            throw new RuntimeException("Tabelle 'saison_schuetze' konnte nicht angelegt werden", e);
        }
    }

    public List<SaisonSchuetze> schuetzenVonMannschaft(String saisonID, String mannschaftID) {
        List<SaisonSchuetze> meldungen = new ArrayList<>();
        String sql = "SELECT * FROM saison_schuetze WHERE saisonID=? AND mannschaftID=? "
                + "ORDER BY vorname COLLATE NOCASE,nachname COLLATE NOCASE";
        try (Connection con = DBController.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, saisonID);
            ps.setString(2, mannschaftID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                meldungen.add(new SaisonSchuetze(rs.getString("saisonID"), rs.getString("schuetzeID"),
                        rs.getString("vorname"), rs.getString("nachname"), rs.getString("mannschaftID"),
                        rs.getString("mannschaftName"), rs.getString("altersklasseID"),
                        rs.getString("altersklasseName")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Saisonmeldungen konnten nicht geladen werden", e);
        }
        return meldungen;
    }

    public SaisonSchuetze finde(String saisonID, String schuetzeID) {
        String sql = "SELECT * FROM saison_schuetze WHERE saisonID=? AND schuetzeID=?";
        try (Connection con = DBController.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, saisonID);
            ps.setString(2, schuetzeID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new SaisonSchuetze(rs.getString("saisonID"), rs.getString("schuetzeID"),
                        rs.getString("vorname"), rs.getString("nachname"), rs.getString("mannschaftID"),
                        rs.getString("mannschaftName"), rs.getString("altersklasseID"),
                        rs.getString("altersklasseName"));
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Saisonmeldung konnte nicht geladen werden", e);
        }
    }

    public void speichern(SaisonSchuetze meldung) {
        String sql = "INSERT INTO saison_schuetze(saisonID,schuetzeID,vorname,nachname,mannschaftID,"
                + "mannschaftName,altersklasseID,altersklasseName) VALUES(?,?,?,?,?,?,?,?) "
                + "ON CONFLICT(saisonID,schuetzeID) DO UPDATE SET vorname=excluded.vorname,"
                + "nachname=excluded.nachname,mannschaftID=excluded.mannschaftID,"
                + "mannschaftName=excluded.mannschaftName,altersklasseID=excluded.altersklasseID,"
                + "altersklasseName=excluded.altersklasseName";
        try (Connection con = DBController.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, meldung.getSaisonID());
            ps.setString(2, meldung.getSchuetzeID());
            ps.setString(3, meldung.getVorname());
            ps.setString(4, meldung.getNachname());
            ps.setString(5, meldung.getMannschaftID());
            ps.setString(6, meldung.getMannschaftName());
            ps.setString(7, meldung.getAltersklasseID());
            ps.setString(8, meldung.getAltersklasseName());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Saisonmeldung konnte nicht gespeichert werden", e);
        }
    }
}
