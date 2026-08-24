package io.github.manadhion.wettkampf.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.manadhion.wettkampf.app.DBController;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

class LokaleDatenbankIntegritaetTest extends SQLiteTestbasis {

    @Test
    void legtAlleFachlichenTabellenAn() throws SQLException {
        Set<String> tabellen = new TreeSet<>();

        try (Connection con = DBController.getConnection();
                Statement stmt = con.createStatement();
                var rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table'")) {
            while (rs.next()) {
                tabellen.add(rs.getString("name"));
            }
        }

        assertTrue(tabellen.containsAll(Set.of(
                "altersklasse", "begegnung", "ergebnisse", "liga", "mannschaft",
                "saison", "saison_schuetze", "schuetze", "wettkampftage")));
    }

    @Test
    void aktiviertFremdschluesselBeiJederVerbindung() throws SQLException {
        try (Connection con = DBController.getConnection();
                Statement stmt = con.createStatement();
                var rs = stmt.executeQuery("PRAGMA foreign_keys")) {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    void lehntErgebnisOhneSchuetzeUndWettkampftagAb() throws SQLException {
        try (Connection con = DBController.getConnection(); Statement stmt = con.createStatement()) {
            assertThrows(SQLException.class, () -> stmt.executeUpdate(
                    "INSERT INTO ergebnisse(id,schuetzeID,wettkampftagID,ergebnis) "
                            + "VALUES('e-ungueltig','fehlt','fehlt',500)"));
        }
    }

    @Test
    void lehntRingzahlenAusserhalbNullBisSechshundertAb() throws SQLException {
        grunddatenEinfuegen();

        try (Connection con = DBController.getConnection(); Statement stmt = con.createStatement()) {
            assertThrows(SQLException.class, () -> stmt.executeUpdate(
                    "INSERT INTO ergebnisse VALUES('e-negativ','schuetze-1','tag-1',-1)"));
            assertThrows(SQLException.class, () -> stmt.executeUpdate(
                    "INSERT INTO ergebnisse VALUES('e-zu-hoch','schuetze-1','tag-1',601)"));
        }
    }

    @Test
    void erlaubtProSchuetzeUndWettkampftagNurEinErgebnis() throws SQLException {
        grunddatenEinfuegen();

        try (Connection con = DBController.getConnection(); Statement stmt = con.createStatement()) {
            stmt.executeUpdate("INSERT INTO ergebnisse VALUES('e-1','schuetze-1','tag-1',500)");
            assertThrows(SQLException.class, () -> stmt.executeUpdate(
                    "INSERT INTO ergebnisse VALUES('e-2','schuetze-1','tag-1',510)"));
        }
    }

    private void grunddatenEinfuegen() throws SQLException {
        try (Connection con = DBController.getConnection(); Statement stmt = con.createStatement()) {
            stmt.executeUpdate("INSERT INTO saison VALUES('saison-1',2026)");
            stmt.executeUpdate("INSERT INTO liga(id,name,rangfolge) VALUES('liga-1','Gauliga',1)");
            stmt.executeUpdate("INSERT INTO altersklasse VALUES('alter-1','Erwachsene')");
            stmt.executeUpdate("INSERT INTO mannschaft VALUES('team-1','Testverein','liga-1')");
            stmt.executeUpdate("INSERT INTO schuetze VALUES('schuetze-1','Max','Muster','team-1','alter-1')");
            stmt.executeUpdate("INSERT INTO wettkampftage VALUES('tag-1','2026-09-01','Testverein','saison-1')");
        }
    }
}
