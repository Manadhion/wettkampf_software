package io.github.manadhion.wettkampf.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import io.github.manadhion.wettkampf.app.DBController;
import io.github.manadhion.wettkampf.data.Schuetze;


/**
 * Data Access-Objekt für die Tabelle schuetze, koordiniert Arbeiten zwischen DB und App.
 */
public class SchuetzeDAO {

    /**
     * Legt die Tabelle schuetze an, falls sie noch nicht existiert.
     */
    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS schuetze ("
                + "id TEXT PRIMARY KEY,"
                + "vorname TEXT NOT NULL,"
                + "nachname TEXT NOT NULL,"
                + "mannschaftid TEXT REFERENCES mannschaft(id) NOT NULL,"   //Referenz zur id der Mannschaft des Schützen
                + "altersKlasse TEXT REFERENCES altersklasse(id) NOT NULL"   //Welcher Altersklasse der Schütze angehört, z.B. Jugend, Herren usw.
                + ")";
        
        try (Connection con = DBController.getConnection();
            Statement stmt = con.createStatement()) {
                stmt.execute(sql);
        } catch (SQLException e) {
	        throw new RuntimeException("Tabelle 'schuetze' konnte nicht angelegt werden", e);
	    }
    }

    /**
     * Neuen Schützen in die Datenbank einfügen.
     * @param schuetze einzufügender Schütze
     */
    public void insert(Schuetze schuetze) {
        
        //erstellen oder ignorieren wenn es die Entität bereits gibt
        String sql = "INSERT OR IGNORE INTO schuetze(id, vorname, nachname, mannschaftid, altersKlasse) "
                + "VALUES(?,?,?,?,?)";

        //Verbindung zu DB und arbeit ausführen
        try (Connection con = DBController.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){
			
			//set Values
			ps.setString(1, schuetze.getId());
            ps.setString(2, schuetze.getVorname());
            ps.setString(3, schuetze.getNachname());
            ps.setString(4, schuetze.getMannschaftid());
            ps.setString(5, schuetze.getAltersKlasse());
			
			ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}

    }

    /**
     * Bestehenden Schützen ändern.
     * @param schuetze Schütze mit den geänderten Werten
     */
    public void update(Schuetze schuetze) {

        String sql = "UPDATE schuetze SET vorname=?, nachname=?, mannschaftid=?, altersKlasse=? WHERE id=?";

        //Verbindung zu DB und arbeit ausführen
        try (Connection con = DBController.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){

			//set Values
            ps.setString(1, schuetze.getVorname());
            ps.setString(2, schuetze.getNachname());
            ps.setString(3, schuetze.getMannschaftid());
            ps.setString(4, schuetze.getAltersKlasse());
            ps.setString(5, schuetze.getId());

			ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}

    }

    /**
     * Alle Schützen einer Mannschaft holen.
     * @param id id der Mannschaft
     * @return Liste der Schützen dieser Mannschaft
     */
    public List<Schuetze> schuetzenVonMannschaft(String id) {

        List<Schuetze> schuetze = new ArrayList<>();

        String sql = "SELECT * FROM schuetze WHERE mannschaftid=?";

        //Abrufen der Werte
		try (Connection con = DBController.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){

            ps.setString(1, id);
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
                //für jede Zeile ein neues Objekt von Schuetze erzeugen und der Liste hinzufügen
				Schuetze s = new Schuetze(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5));

                schuetze.add(s);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

        return schuetze;
    }

    /**
     * Schütze löschen, falls falsch eingetragen oder ausgefallen.
     * @param id id des zu löschenden Schützen
     * @return Anzahl der gelöschten Zeilen, größer 0 wenn das Löschen erfolgreich war
     */
    public int delete(String id) {
        String sql = "DELETE FROM schuetze WHERE id=?;";

        //return Statement
        int erg = 0;

        //Löschvorgang
        try(Connection con = DBController.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){
			ps.setString(1, id);;
			erg = ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//wenn erg >0 ist war das Löschen erfolgreich
		return erg;
    }



}
