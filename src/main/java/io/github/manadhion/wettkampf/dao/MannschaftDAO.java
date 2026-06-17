package io.github.manadhion.wettkampf.dao;

import io.github.manadhion.wettkampf.app.DBController;
import io.github.manadhion.wettkampf.data.Mannschaft;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access-Objekt für die Tabelle mannschaft, koordiniert Arbeiten zwischen DB und App.
 */
public class MannschaftDAO {

    /**
     * Legt die Tabelle mannschaft an, falls sie noch nicht existiert.
     */
    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS mannschaft ("
                + "id TEXT PRIMARY KEY,"
                + "name TEXT NOT NULL,"     //Name der Mannschaft
                + "klasse TEXT REFERENCES liga(id)"    //Klasse in der sich die Mannschaft befindet, z.B. A-Klasse als Referenz auf die Liga-table
                + ")";
        
        try (Connection con = DBController.getConnection();
            Statement stmt = con.createStatement()) {
                stmt.execute(sql);
        } catch (SQLException e) {
	        throw new RuntimeException("Tabelle 'mannschaft' konnte nicht angelegt werden", e);
	    }
    }


    /**
     * Neue Mannschaft in die Datenbank einfügen.
     * @param mannschaft einzufügende Mannschaft
     */
    public void insert(Mannschaft mannschaft) {
        
        //erstellen oder ignorieren wenn es die Entität bereits gibt
        String sql = "INSERT OR IGNORE INTO mannschaft(id, name, klasse) "
                + "VALUES(?,?,?)";

        //Verbindung zu DB und arbeit ausführen
        try (Connection con = DBController.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){
			
			//set Values
            ps.setString(1, mannschaft.getId());
			ps.setString(2, mannschaft.getName());
            ps.setString(3, mannschaft.getKlasse());
			
			ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}

    }

    /**
     * Bestehende Mannschaft ändern.
     * @param mannschaft Mannschaft mit den geänderten Werten
     */
    public void update(Mannschaft mannschaft) {

        String sql = "UPDATE mannschaft SET name=?, klasse=? WHERE id=?";

        //Verbindung zu DB und arbeit ausführen
        try (Connection con = DBController.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){

			//set Values
            ps.setString(1, mannschaft.getName());
            ps.setString(2, mannschaft.getKlasse());
            ps.setString(3, mannschaft.getId());

			ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}

    }

    /**
     * Alle Mannschaften holen, inklusive Liganame per JOIN für die Anzeige.
     * @return Liste aller Mannschaften
     */
    public List<Mannschaft> alleMannschaften() {

        List<Mannschaft> mannschaften = new ArrayList<>();

        //Liganame gleich per JOIN mitladen, damit die Anzeige keine eigene DB-Abfrage braucht
        String sql = "SELECT m.id, m.name, m.klasse, l.name "
                + "FROM mannschaft m LEFT JOIN liga l ON m.klasse = l.id";

        //Abrufen der Werte
		try (Connection con = DBController.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
                //für jede Zeile ein neues Objekt von Mannschaft erzeugen und der Liste hinzufügen
				Mannschaft m = new Mannschaft(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4));

                mannschaften.add(m);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

        return mannschaften;

    }

    /**
     * Mannschaft durch id finden, inklusive Liganame per JOIN für die Anzeige.
     * @param mID id der gesuchten Mannschaft
     * @return gefundene Mannschaft, oder null wenn es keine gibt
     */
    public Mannschaft mannschaftMitID(String mID) {

        Mannschaft m = null;

        //Liganame gleich per JOIN mitladen, damit die Anzeige keine eigene DB-Abfrage braucht
        String sql = "SELECT m.id, m.name, m.klasse, l.name "
                + "FROM mannschaft m LEFT JOIN liga l ON m.klasse = l.id WHERE m.id=?";

        //Abrufen der Werte
		try (Connection con = DBController.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){

            ps.setString(1, mID);

			ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                //gefundene Zeile in ein Objekt umwandeln
                m = new Mannschaft(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4));
            }
        
			return m;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
        
        return null;
    }

    /**
     * Mannschaft löschen, falls falsch eingetragen oder ausgefallen.
     * @param id id der zu löschenden Mannschaft
     * @return Anzahl der gelöschten Zeilen, größer 0 wenn das Löschen erfolgreich war
     */
    public int delete(String id) {
        String sql = "DELETE FROM mannschaft WHERE id=?;";

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
