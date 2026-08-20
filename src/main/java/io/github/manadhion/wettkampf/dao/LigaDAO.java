package io.github.manadhion.wettkampf.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import io.github.manadhion.wettkampf.app.DBController;
import io.github.manadhion.wettkampf.data.Liga;

/**
 * Data Access-Objekt für die Tabelle liga, koordiniert Arbeiten zwischen DB und App.
 */
public class LigaDAO {

    /**
     * Legt die Tabelle liga an, falls sie noch nicht existiert.
     */
    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS liga ("
                + "id TEXT PRIMARY KEY,"
                + "name TEXT NOT NULL,"     //Name der Liga
                + "rangfolge INTEGER NOT NULL DEFAULT 999"
                + ")";
        
        try (Connection con = DBController.getConnection();
            Statement stmt = con.createStatement()) {
                stmt.execute(sql);
                if (!hatSpalte(con, "rangfolge")) {
                    stmt.execute("ALTER TABLE liga ADD COLUMN rangfolge INTEGER NOT NULL DEFAULT 999");
                }
        } catch (SQLException e) {
	        throw new RuntimeException("Tabelle 'liga' konnte nicht angelegt werden", e);
	    }
    }

    private boolean hatSpalte(Connection con, String name) throws SQLException {
        try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery("PRAGMA table_info(liga)")) {
            while (rs.next()) if (name.equalsIgnoreCase(rs.getString("name"))) return true;
        }
        return false;
    }

    /**
     * Neue Liga in die Datenbank einfügen.
     * @param liga einzufügende Liga
     */
    public void insert(Liga liga) {
        
        //erstellen oder ignorieren wenn es die Entität bereits gibt
        String sql = "INSERT OR IGNORE INTO liga(id, name, rangfolge) VALUES(?,?,?)";

        //Verbindung zu DB und arbeit ausführen
        try (Connection con = DBController.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement verschieben = con.prepareStatement(
                    "UPDATE liga SET rangfolge=rangfolge+1 WHERE rangfolge>=?")) {
                verschieben.setInt(1, liga.getRangfolge());
                verschieben.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, liga.getId());
                ps.setString(2, liga.getLigaName());
                ps.setInt(3, liga.getRangfolge());
                ps.executeUpdate();
            }
            con.commit();
		} catch (SQLException e) {
			throw new RuntimeException("Liga konnte nicht gespeichert werden", e);
		}
    }

    /**
     * Bestehende Liga ändern.
     * @param liga Liga mit den geänderten Werten
     */
    public void update(Liga liga) {

        String sql = "UPDATE liga SET name=?, rangfolge=? WHERE id=?";

        //Verbindung zu DB und arbeit ausführen
        try (Connection con = DBController.getConnection()) {
            con.setAutoCommit(false);
            int bisherigeRangfolge;
            try (PreparedStatement lesen = con.prepareStatement("SELECT rangfolge FROM liga WHERE id=?")) {
                lesen.setString(1, liga.getId());
                ResultSet rs = lesen.executeQuery();
                if (!rs.next()) throw new SQLException("Liga nicht gefunden");
                bisherigeRangfolge = rs.getInt(1);
            }
            if (liga.getRangfolge() < bisherigeRangfolge) {
                try (PreparedStatement verschieben = con.prepareStatement("UPDATE liga SET rangfolge=rangfolge+1 "
                        + "WHERE id<>? AND rangfolge>=? AND rangfolge<?")) {
                    verschieben.setString(1, liga.getId());
                    verschieben.setInt(2, liga.getRangfolge());
                    verschieben.setInt(3, bisherigeRangfolge);
                    verschieben.executeUpdate();
                }
            } else if (liga.getRangfolge() > bisherigeRangfolge) {
                try (PreparedStatement verschieben = con.prepareStatement("UPDATE liga SET rangfolge=rangfolge-1 "
                        + "WHERE id<>? AND rangfolge>? AND rangfolge<=?")) {
                    verschieben.setString(1, liga.getId());
                    verschieben.setInt(2, bisherigeRangfolge);
                    verschieben.setInt(3, liga.getRangfolge());
                    verschieben.executeUpdate();
                }
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, liga.getLigaName());
                ps.setInt(2, liga.getRangfolge());
                ps.setString(3, liga.getId());
                ps.executeUpdate();
            }
            con.commit();
		} catch (SQLException e) {
			throw new RuntimeException("Liga konnte nicht aktualisiert werden", e);
		}
    }

    /**
     * Alle Ligen alphabetisch nach Name holen.
     * @return Liste aller Ligen
     */
    public List<Liga> alleLigen() {
        List<Liga> ligen = new ArrayList<>();

        String sql = "SELECT id,name,rangfolge FROM liga ORDER BY rangfolge,name COLLATE NOCASE";

        //Abrufen der Werte
		try (Connection con = DBController.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
                //für jede Zeile ein neues Objekt von Liga erzeugen und der Liste hinzufügen
				Liga l = new Liga(rs.getString(1), rs.getString(2), rs.getInt(3));

                ligen.add(l);
			}
			
		} catch (SQLException e) {
			throw new RuntimeException("Ligen konnten nicht geladen werden", e);
		}

        return ligen;
    }

    /**
     * Holt nur die Ligen, für die in einer Saison Begegnungen geplant sind.
     * Dabei wird die historische Liga der Begegnung verwendet.
     * @param saisonID id der Saison
     * @return Ligen der Saison, alphabetisch sortiert
     */
    public List<Liga> ligenVonSaison(String saisonID) {
        List<Liga> ligen = new ArrayList<>();
        String sql = "SELECT DISTINCT COALESCE(b.liga, m.klasse), "
                + "COALESCE(b.ligaName, l.name) AS name, COALESCE(l.rangfolge,999) AS rangfolge "
                + "FROM begegnung b "
                + "JOIN wettkampftage w ON w.id=b.wettkampftag "
                + "JOIN mannschaft m ON m.id=b.heim "
                + "LEFT JOIN liga l ON l.id=COALESCE(b.liga, m.klasse) "
                + "WHERE w.saisonID=? ORDER BY rangfolge,name COLLATE NOCASE";

        try (Connection con = DBController.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, saisonID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ligen.add(new Liga(rs.getString(1), rs.getString(2), rs.getInt(3)));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ligen der Saison konnten nicht geladen werden", e);
        }
        return ligen;
    }

    /**
     * Liga durch id finden.
     * @param id id der gesuchten Liga
     * @return gefundene Liga
     */
    public Liga ligaMitIdFinden(String id) {

        Liga l = null;
        String sql = "SELECT id,name,rangfolge FROM liga WHERE id=?";

        //Abrufen der Werte
		try (Connection con = DBController.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){
			
            ps.setString(1, id);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) l = new Liga(rs.getString(1), rs.getString(2), rs.getInt(3));

			
		} catch (SQLException e) {
			throw new RuntimeException("Liga konnte nicht geladen werden", e);
		}

        return l;
    }

    public int naechsteRangfolge() {
        try (Connection con = DBController.getConnection(); Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COALESCE(MAX(rangfolge),0)+1 FROM liga")) {
            return rs.next() ? rs.getInt(1) : 1;
        } catch (SQLException e) {
            throw new RuntimeException("Nächste Liga-Rangfolge konnte nicht bestimmt werden", e);
        }
    }

    /**
     * Liga löschen, falls falsch eingetragen oder ausgefallen.
     * @param id id der zu löschenden Liga
     * @return Anzahl der gelöschten Zeilen, größer 0 wenn das Löschen erfolgreich war
     */
    public int delete(String id) {
        String sql = "DELETE FROM liga WHERE id=? "
                + "AND NOT EXISTS (SELECT 1 FROM mannschaft WHERE klasse=?) "
                + "AND NOT EXISTS (SELECT 1 FROM begegnung WHERE liga=?)";

        //return Statement
        int erg = 0;

        //Löschvorgang
        try(Connection con = DBController.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){
			ps.setString(1, id);
			ps.setString(2, id);
			ps.setString(3, id);
			erg = ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Liga konnte nicht gelöscht werden", e);
		}
		
		//wenn erg >0 ist war das Löschen erfolgreich
		return erg;
    }
}
