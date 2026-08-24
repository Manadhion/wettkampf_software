package io.github.manadhion.wettkampf.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import io.github.manadhion.wettkampf.app.DBController;
import io.github.manadhion.wettkampf.data.Ergebnisse;

/**
 * Data Access-Objekt für die Tabelle ergebnisse, koordiniert Arbeiten zwischen DB und App.
 */
public class ErgebnisseDAO {

    /**
     * Legt die Tabelle ergebnisse an, falls sie noch nicht existiert.
     */
    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS ergebnisse ("
                + "id TEXT PRIMARY KEY,"
                + "schuetzeID TEXT REFERENCES schuetze(id) NOT NULL,"            //Referenz zur id des Schützen um dessen Ergebnis es geht
                + "wettkampftagID TEXT REFERENCES wettkampftage(id) NOT NULL,"   //id zum Wettkampftag an dem das Ergebnis geschossen wurde
                + "ergebnis INTEGER NOT NULL CHECK(ergebnis BETWEEN 0 AND 600),"
                + "UNIQUE(schuetzeID, wettkampftagID)"
                + ")";
        
        try (Connection con = DBController.getConnection();
            Statement stmt = con.createStatement()) {
                stmt.execute(sql);
                stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS ux_ergebnis_schuetze_tag "
                        + "ON ergebnisse(schuetzeID,wettkampftagID)");
                stmt.execute("CREATE TRIGGER IF NOT EXISTS trg_ergebnis_bereich_insert BEFORE INSERT ON ergebnisse "
                        + "WHEN NEW.ergebnis<0 OR NEW.ergebnis>600 BEGIN SELECT RAISE(ABORT,'Ergebnis muss zwischen 0 und 600 liegen'); END");
                stmt.execute("CREATE TRIGGER IF NOT EXISTS trg_ergebnis_bereich_update BEFORE UPDATE OF ergebnis ON ergebnisse "
                        + "WHEN NEW.ergebnis<0 OR NEW.ergebnis>600 BEGIN SELECT RAISE(ABORT,'Ergebnis muss zwischen 0 und 600 liegen'); END");
        } catch (SQLException e) {
	        throw new RuntimeException("Tabelle 'ergebnisse' konnte nicht angelegt werden", e);
	    }
    }

    /**
     * Neues Ergebnis in die Datenbank einfügen.
     * @param ergebnisse einzufügendes Ergebnis
     */
    public void insert(Ergebnisse ergebnisse) {
        
        //erstellen oder ignorieren wenn es die Entität bereits gibt
        String sql = "INSERT OR IGNORE INTO ergebnisse(id, schuetzeID, wettkampftagID, ergebnis) "
                + "VALUES(?,?,?,?)";

        //Verbindung zu DB und arbeit ausführen
		try (Connection con = DBController.getConnection()) {
			con.setAutoCommit(false);
			meldungSichern(con, ergebnisse.getSchuetzeID(), ergebnisse.getWettkampftagID());
			try (PreparedStatement ps = con.prepareStatement(sql)) {
			
			//set Values
			ps.setString(1, ergebnisse.getId());
            ps.setString(2, ergebnisse.getSchuetzeID());
            ps.setString(3, ergebnisse.getWettkampftagID());
            ps.setInt(4, ergebnisse.getErgebnis());
			
				ps.executeUpdate();
			}
			con.commit();
		} catch (SQLException e) {
			throw new RuntimeException("Ergebnis konnte nicht gespeichert werden", e);
		}

    }

    private void meldungSichern(Connection con, String schuetzeID, String wettkampftagID) throws SQLException {
        String sql = "INSERT OR IGNORE INTO saison_schuetze "
                + "SELECT w.saisonID,s.id,s.vorname,s.nachname,m.id,m.name,a.id,a.name "
                + "FROM wettkampftage w JOIN schuetze s ON s.id=? "
                + "JOIN mannschaft m ON m.id=s.mannschaftid "
                + "JOIN altersklasse a ON a.id=s.altersKlasse WHERE w.id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, schuetzeID);
            ps.setString(2, wettkampftagID);
            ps.executeUpdate();
        }
    }

    /**
     * Ergebnis eines Schützen an einem bestimmten Wettkampftag holen.
     * @param schuetzeID id des Schützen
     * @param wettkampftagID id des Wettkampftages
     * @return gefundenes Ergebnis, oder null wenn es noch keins gibt
     */
    public Ergebnisse ergebnisFuerSchuetzeUndTag(String schuetzeID, String wettkampftagID) {

        Ergebnisse ergebnis = null;

        String sql = "SELECT * FROM ergebnisse WHERE schuetzeID=? AND wettkampftagID=?";

        //Abrufen der Werte
        try (Connection con = DBController.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)){

            //set Values
            ps.setString(1, schuetzeID);
            ps.setString(2, wettkampftagID);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                //gefundene Zeile in ein Objekt umwandeln
                ergebnis = new Ergebnisse(rs.getString(1), rs.getString(2), rs.getString(3), rs.getInt(4));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ergebnis konnte nicht geladen werden", e);
        }

        return ergebnis;
    }

    /**
     * Gesamtergebnis einer Mannschaft an einem Tag = Summe der besten 3 Schützen-Ergebnisse.
     * @param mannschaftID id der Mannschaft
     * @param wettkampftagID id des Wettkampftages
     * @return Summe der besten 3 Ergebnisse, 0 wenn noch keine Ergebnisse vorliegen
     */
    public int gesamtErgebnisBeste3(String mannschaftID, String wettkampftagID) {

        int gesamt = 0;

        //die besten 3 Ergebnisse direkt von der DB sortiert und begrenzt holen und aufsummieren
        String sql = "SELECT COALESCE(SUM(ergebnis), 0) FROM ("
                + "SELECT e.ergebnis FROM ergebnisse e "
                + "JOIN wettkampftage w ON w.id=e.wettkampftagID "
                + "JOIN saison_schuetze ss ON ss.saisonID=w.saisonID AND ss.schuetzeID=e.schuetzeID "
                + "WHERE ss.mannschaftID = ? AND e.wettkampftagID = ? "
                + "ORDER BY e.ergebnis DESC LIMIT 3"
                + ")";

        //Abrufen der Werte
        try (Connection con = DBController.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)){

            //set Values
            ps.setString(1, mannschaftID);
            ps.setString(2, wettkampftagID);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                gesamt = rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Mannschaftsergebnis konnte nicht berechnet werden", e);
        }

        return gesamt;
    }

    /**
     * Bestehendes Ergebnis ändern.
     * @param ergebnisse Ergebnis mit dem geänderten Wert
     */
    public void update(Ergebnisse ergebnisse) {

        String sql = "UPDATE ergebnisse SET ergebnis=? WHERE id=?";

        //Verbindung zu DB und arbeit ausführen
        try (Connection con = DBController.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)){

            //set Values
            ps.setInt(1, ergebnisse.getErgebnis());
            ps.setString(2, ergebnisse.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Ergebnis konnte nicht aktualisiert werden", e);
        }
    }

}
