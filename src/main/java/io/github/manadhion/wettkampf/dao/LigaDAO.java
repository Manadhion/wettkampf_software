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
                + "name TEXT NOT NULL"     //Name der Liga
                + ")";
        
        try (Connection con = DBController.getConnection();
            Statement stmt = con.createStatement()) {
                stmt.execute(sql);
        } catch (SQLException e) {
	        throw new RuntimeException("Tabelle 'liga' konnte nicht angelegt werden", e);
	    }
    }

    /**
     * Neue Liga in die Datenbank einfügen.
     * @param liga einzufügende Liga
     */
    public void insert(Liga liga) {
        
        //erstellen oder ignorieren wenn es die Entität bereits gibt
        String sql = "INSERT OR IGNORE INTO liga(id, name) "
                + "VALUES(?,?)";

        //Verbindung zu DB und arbeit ausführen
        try (Connection con = DBController.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){
			
			//set Values
            ps.setString(1, liga.getId());
            ps.setString(2, liga.getLigaName());
			
			ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
    }

    /**
     * Bestehende Liga ändern.
     * @param liga Liga mit den geänderten Werten
     */
    public void update(Liga liga) {

        String sql = "UPDATE liga SET name=? WHERE id=?";

        //Verbindung zu DB und arbeit ausführen
        try (Connection con = DBController.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){

			//set Values
            ps.setString(1, liga.getLigaName());
            ps.setString(2, liga.getId());

			ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
    }

    /**
     * Alle Ligen holen.
     * @return Liste aller Ligen
     */
    public List<Liga> alleLigen() {
        List<Liga> ligen = new ArrayList<>();

        String sql = "Select * FROM liga";

        //Abrufen der Werte
		try (Connection con = DBController.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
                //für jede Zeile ein neues Objekt von Liga erzeugen und der Liste hinzufügen
				Liga l = new Liga(rs.getString(1), rs.getString(2));

                ligen.add(l);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
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
        String sql = "Select * FROM liga WHERE id=?";

        //Abrufen der Werte
		try (Connection con = DBController.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){
			
            ps.setString(1, id);
			ResultSet rs = ps.executeQuery();
			l = new Liga(rs.getString(1), rs.getString(2));

			
		} catch (SQLException e) {
			e.printStackTrace();
		}

        return l;
    }

    /**
     * Liga löschen, falls falsch eingetragen oder ausgefallen.
     * @param id id der zu löschenden Liga
     * @return Anzahl der gelöschten Zeilen, größer 0 wenn das Löschen erfolgreich war
     */
    public int delete(String id) {
        String sql = "DELETE FROM liga WHERE id=?;";

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
