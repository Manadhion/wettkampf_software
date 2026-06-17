package io.github.manadhion.wettkampf.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import io.github.manadhion.wettkampf.app.DBController;
import io.github.manadhion.wettkampf.data.Altersklasse;

/**
 * Data Access-Objekt für die Tabelle altersklasse, koordiniert Arbeiten zwischen DB und App.
 */
public class AltersklasseDAO {

    /**
     * Legt die Tabelle altersklasse an, falls sie noch nicht existiert.
     */
    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS altersklasse ("
                + "id TEXT PRIMARY KEY,"
                + "name TEXT NOT NULL"
                + ")";
        
        try (Connection con = DBController.getConnection();
            Statement stmt = con.createStatement()) {
                stmt.execute(sql);
        } catch (SQLException e) {
	        throw new RuntimeException("Tabelle 'altersklasse' konnte nicht angelegt werden", e);
	    }
    }

    /**
     * Neue Altersklasse in die Datenbank einfügen.
     * @param alter einzufügende Altersklasse
     */
    public void insert(Altersklasse alter) {
        
        //erstellen oder ignorieren wenn es die Entität bereits gibt
        String sql = "INSERT OR IGNORE INTO altersklasse(id, name) "
                + "VALUES(?,?)";

        //Verbindung zu DB und arbeit ausführen
        try (Connection con = DBController.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){
			
			//set Values
            ps.setString(1, alter.getId());
            ps.setString(2, alter.getKlassenName());
			
			ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
    }

    /**
     * Bestehende Altersklasse ändern.
     * @param alter Altersklasse mit den geänderten Werten
     */
    public void update(Altersklasse alter) {

        String sql = "UPDATE altersklasse SET name=? WHERE id=?";

        //Verbindung zu DB und arbeit ausführen
        try (Connection con = DBController.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){

			//set Values
            ps.setString(1, alter.getKlassenName());
            ps.setString(2, alter.getId());

			ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
    }

    /**
     * Alle Altersklassen holen.
     * @return Liste aller Altersklassen
     */
    public List<Altersklasse> alleAltersklassen() {
        List<Altersklasse> aKlassen = new ArrayList<>();

        String sql = "Select * FROM altersklasse";

        //Abrufen der Werte
		try (Connection con = DBController.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
                //für jede Zeile ein neues Objekt von Liga erzeugen und der Liste hinzufügen
				Altersklasse a = new Altersklasse(rs.getString(1), rs.getString(2));

                aKlassen.add(a) ;
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

        return aKlassen;
    }

    /**
     * Altersklasse löschen, falls falsch eingetragen oder ausgefallen.
     * @param id id der zu löschenden Altersklasse
     * @return Anzahl der gelöschten Zeilen, größer 0 wenn das Löschen erfolgreich war
     */
    public int delete(String id) {
        String sql = "DELETE FROM altersklasse WHERE id=?;";

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
