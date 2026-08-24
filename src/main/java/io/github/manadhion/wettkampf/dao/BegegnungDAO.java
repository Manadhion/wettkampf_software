package io.github.manadhion.wettkampf.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import io.github.manadhion.wettkampf.app.DBController;
import io.github.manadhion.wettkampf.data.Begegnung;

/**
 * Data Access-Objekt für die Tabelle begegnung, koordiniert Arbeiten zwischen DB und App.
 */
public class BegegnungDAO {

    /**
     * Legt die Tabelle begegnung an, falls sie noch nicht existiert.
     */
    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS begegnung ("
                + "id TEXT PRIMARY KEY,"
                + "heim TEXT REFERENCES mannschaft(id) NOT NULL,"        //Referenz zur id der Mannschaft die als Heim antritt
                + "gegner TEXT REFERENCES mannschaft(id) NOT NULL,"      //Referenz zur id der Mannschaft die als Gast antritt
                + "wettkampftag TEXT REFERENCES wettkampftage(id) NOT NULL," //Referenz zur id des Wettkampftages
                + "liga TEXT REFERENCES liga(id),"
                + "ligaName TEXT,"
                + "heimName TEXT, gegnerName TEXT,"
                + "CHECK(heim <> gegner)"
                + ")";
        
        try (Connection con = DBController.getConnection();
            Statement stmt = con.createStatement()) {
                stmt.execute(sql);
                spalteFallsNoetigErgaenzen(con, "liga", "TEXT REFERENCES liga(id)");
                spalteFallsNoetigErgaenzen(con, "ligaName", "TEXT");
                spalteFallsNoetigErgaenzen(con, "heimName", "TEXT");
                spalteFallsNoetigErgaenzen(con, "gegnerName", "TEXT");
                stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS ux_begegnung_tag_paar "
                        + "ON begegnung(wettkampftag, CASE WHEN heim<gegner THEN heim ELSE gegner END, "
                        + "CASE WHEN heim<gegner THEN gegner ELSE heim END)");
                stmt.execute("CREATE TRIGGER IF NOT EXISTS trg_begegnung_nicht_selbst_insert BEFORE INSERT ON begegnung "
                        + "WHEN NEW.heim=NEW.gegner BEGIN SELECT RAISE(ABORT,'Mannschaft kann nicht gegen sich selbst antreten'); END");
                stmt.execute("CREATE TRIGGER IF NOT EXISTS trg_begegnung_nicht_selbst_update BEFORE UPDATE OF heim,gegner ON begegnung "
                        + "WHEN NEW.heim=NEW.gegner BEGIN SELECT RAISE(ABORT,'Mannschaft kann nicht gegen sich selbst antreten'); END");
        } catch (SQLException e) {
	        throw new RuntimeException("Tabelle 'begegnung' konnte nicht vorbereitet werden", e);
	    }
    }

    private void spalteFallsNoetigErgaenzen(Connection con, String name, String definition) throws SQLException {
        try (Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("PRAGMA table_info(begegnung)")) {
            while (rs.next()) {
                if (name.equalsIgnoreCase(rs.getString("name"))) {
                    return;
                }
            }
        }
        try (Statement stmt = con.createStatement()) {
            stmt.execute("ALTER TABLE begegnung ADD COLUMN " + name + " " + definition);
        }
    }

    /**
     * Neue Begegnung in die Datenbank einfügen.
     * @param begegnung einzufügende Begegnung
     */
    public void insert(Begegnung begegnung) {
        
        //erstellen oder ignorieren wenn es die Entität bereits gibt
        String sql = "INSERT OR IGNORE INTO begegnung(id, heim, gegner, wettkampftag, liga, ligaName,heimName,gegnerName) "
                + "SELECT ?,?,?,?,h.klasse,l.name,h.name,g.name FROM mannschaft h "
                + "JOIN mannschaft g ON g.id=? LEFT JOIN liga l ON l.id=h.klasse WHERE h.id=?";

        //Verbindung zu DB und arbeit ausführen
        try (Connection con = DBController.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){
			
			//set Values
			ps.setString(1, begegnung.getId());
            ps.setString(2, begegnung.getHeim());
            ps.setString(3, begegnung.getGegner());
            ps.setString(4, begegnung.getWettkampftag());
			ps.setString(5, begegnung.getGegner());
			ps.setString(6, begegnung.getHeim());
			
			ps.executeUpdate();

		} catch (SQLException e) {
			throw new RuntimeException("Tabelle 'begegnung' konnte nicht angelegt werden", e);
		}
    }

    /**
     * Begegnung löschen, falls falsch eingetragen oder ausgefallen.
     * @param id id der zu löschenden Begegnung
     * @return Anzahl der gelöschten Zeilen, größer 0 wenn das Löschen erfolgreich war
     */
    public int delete(String id) {
        String sql = "DELETE FROM begegnung WHERE id=?;";

        //return Statement
        int erg = 0;

        //Löschvorgang
        try(Connection con = DBController.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){
			ps.setString(1, id);;
			erg = ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Begegnung konnte nicht gelöscht werden", e);
		}
		
		//wenn erg >0 ist war das Löschen erfolgreich
		return erg;
    }

    /**
     * Alle Begegnungen eines bestimmten Tages auslesen.
     * @param wettkampftag id des Wettkampftages
     * @return Liste der Begegnungen an diesem Tag
     */
    public List<Begegnung> begegnungenAnDiesemTag(String wettkampftag) {
        List<Begegnung> begegnungen = new ArrayList<>();
        
        String sql = "SELECT * FROM begegnung WHERE wettkampftag = ?";

        //Abrufen der Werte
		try (Connection con = DBController.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){

			ps.setString(1, wettkampftag);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
                //für jede Zeile ein neues Objekt von Wettkampftage erzeugen und der Liste hinzufügen
				Begegnung b = new Begegnung(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getString("liga"), rs.getString("ligaName"), rs.getString("heimName"),
                        rs.getString("gegnerName"));

                begegnungen.add(b);
			}

		} catch (SQLException e) {
			throw new RuntimeException("Begegnungen konnten nicht geladen werden", e);
		}

        return begegnungen;
    }

    public boolean existiert(String wettkampftag, String mannschaftA, String mannschaftB) {
        String sql = "SELECT 1 FROM begegnung WHERE wettkampftag=? "
                + "AND ((heim=? AND gegner=?) OR (heim=? AND gegner=?))";
        try (Connection con = DBController.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, wettkampftag);
            ps.setString(2, mannschaftA);
            ps.setString(3, mannschaftB);
            ps.setString(4, mannschaftB);
            ps.setString(5, mannschaftA);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            throw new RuntimeException("Begegnung konnte nicht geprüft werden", e);
        }
    }

    

}
