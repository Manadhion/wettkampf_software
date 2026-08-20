package io.github.manadhion.wettkampf.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.manadhion.wettkampf.app.DBController;
import io.github.manadhion.wettkampf.data.Ergebnisse;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ErgebnisseDAOTest extends SQLiteTestbasis {

    private final ErgebnisseDAO ergebnisseDAO = new ErgebnisseDAO();

    @BeforeEach
    void fachlicheGrunddatenEinfuegen() throws SQLException {
        try (Connection con = DBController.getConnection(); Statement stmt = con.createStatement()) {
            stmt.executeUpdate("INSERT INTO saison VALUES('saison-1',2026)");
            stmt.executeUpdate("INSERT INTO liga(id,name,rangfolge) VALUES('liga-1','Gauliga',1)");
            stmt.executeUpdate("INSERT INTO altersklasse VALUES('alter-1','Erwachsene')");
            stmt.executeUpdate("INSERT INTO mannschaft VALUES('team-1','Testverein','liga-1')");
            stmt.executeUpdate("INSERT INTO wettkampftage VALUES('tag-1','2026-09-01','Testverein','saison-1')");
            for (int nummer = 1; nummer <= 4; nummer++) {
                stmt.executeUpdate("INSERT INTO schuetze VALUES('schuetze-" + nummer
                        + "','Vorname" + nummer + "','Nachname" + nummer + "','team-1','alter-1')");
            }
        }
    }

    @Test
    void speichertUndAktualisiertEinErgebnis() {
        Ergebnisse ergebnis = new Ergebnisse("ergebnis-1", "schuetze-1", "tag-1", 480);
        ergebnisseDAO.insert(ergebnis);

        Ergebnisse geladen = ergebnisseDAO.ergebnisFuerSchuetzeUndTag("schuetze-1", "tag-1");
        assertNotNull(geladen);
        assertEquals(480, geladen.getErgebnis());

        ergebnis.setErgebnis(515);
        ergebnisseDAO.update(ergebnis);

        assertEquals(515,
                ergebnisseDAO.ergebnisFuerSchuetzeUndTag("schuetze-1", "tag-1").getErgebnis());
    }

    @Test
    void bildetDasMannschaftsergebnisAusDenBestenDreiSchuetzen() {
        ergebnisseDAO.insert(new Ergebnisse("e-1", "schuetze-1", "tag-1", 510));
        ergebnisseDAO.insert(new Ergebnisse("e-2", "schuetze-2", "tag-1", 480));
        ergebnisseDAO.insert(new Ergebnisse("e-3", "schuetze-3", "tag-1", 530));
        ergebnisseDAO.insert(new Ergebnisse("e-4", "schuetze-4", "tag-1", 400));

        assertEquals(1520, ergebnisseDAO.gesamtErgebnisBeste3("team-1", "tag-1"));
    }

    @Test
    void sichertBeimErstenErgebnisDenSaisonbezogenenSchuetzenstand() throws SQLException {
        ergebnisseDAO.insert(new Ergebnisse("e-1", "schuetze-1", "tag-1", 500));

        try (Connection con = DBController.getConnection();
                var ps = con.prepareStatement(
                        "SELECT vorname,nachname,mannschaftName,altersklasseName "
                                + "FROM saison_schuetze WHERE saisonID=? AND schuetzeID=?")) {
            ps.setString(1, "saison-1");
            ps.setString(2, "schuetze-1");
            try (var rs = ps.executeQuery()) {
                assertEquals(true, rs.next());
                assertEquals("Vorname1", rs.getString("vorname"));
                assertEquals("Nachname1", rs.getString("nachname"));
                assertEquals("Testverein", rs.getString("mannschaftName"));
                assertEquals("Erwachsene", rs.getString("altersklasseName"));
            }
        }
    }
}
