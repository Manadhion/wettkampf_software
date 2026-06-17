package io.github.manadhion.wettkampf.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import io.github.manadhion.wettkampf.app.DBController;
import io.github.manadhion.wettkampf.data.Saison;

/**
 * Data Access-Objekt für die Tabelle saison, koordiniert Arbeiten zwischen DB und App.
 */
public class SaisonDAO {

    /**
     * Legt die Tabelle saison an, falls sie noch nicht existiert.
     */
    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS saison ("
                + "id TEXT PRIMARY KEY,"
                + "name INT UNIQUE"     //Name der Saison z.B.2526 später als 25/26
                + ")";
        
        try (Connection con = DBController.getConnection();
            Statement stmt = con.createStatement()) {
                stmt.execute(sql);
        } catch (SQLException e) {
	        throw new RuntimeException("Tabelle 'Saison' konnte nicht angelegt werden", e);
	    }
    }

    /**
     * Neue Saison in die Datenbank einfügen.
     * @param saison einzufügende Saison
     */
    public void insert(Saison saison) {
        
        //erstellen oder ignorieren wenn es die Entität bereits gibt
        String sql = "INSERT OR IGNORE INTO saison(id, name) "
                + "VALUES(?,?)";

        //Verbindung zu DB und arbeit ausführen
        try (Connection con = DBController.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){
			
			//set Values
			ps.setString(1, saison.getId());
            ps.setInt(2, saison.getName());
			
			ps.executeUpdate();

		} catch (SQLException e) {
			throw new RuntimeException("Tabelle 'Saison' konnte nicht angelegt werden", e);
		}

    }

    /**
     * Bestehende Saison ändern.
     * @param saison Saison mit den geänderten Werten
     */
    public void update(Saison saison) {

        String sql = "UPDATE saison SET name=? WHERE id=?";

        //Verbindung zu DB und arbeit ausführen
        try (Connection con = DBController.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)){

            //set Values
            ps.setInt(1, saison.getName());
            ps.setString(2, saison.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Saison konnte nicht aktualisiert werden", e);
        }
    }

    /**
     * Alle Saisons absteigend nach Name holen.
     * @return Liste aller Saisons
     */
    public List<Saison> alleSaisons() {
        List<Saison> saison = new ArrayList<>();

        String sql = "Select * FROM saison ORDER BY name DESC";

        //Abrufen der Werte
		try (Connection con = DBController.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
                //für jede Zeile ein neues Objekt von Wettkampftage erzeugen und der Liste hinzufügen
				Saison s = new Saison(rs.getString(1), rs.getInt(2));

                saison.add(s);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

        return saison;
    }

    /**
     * Saison mit einer bestimmten id finden.
     * @param id id der gesuchten Saison
     * @return gefundene Saison, oder null wenn es keine gibt
     */
    public Saison saisonMitId(String id) {
        Saison saison = null;

        String sql = "SELECT * FROM saison WHERE id=?";

        try (Connection con = DBController.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)){

            ps.setString(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                saison = new Saison(rs.getString(1), rs.getInt(2));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return saison;
    }

    /**
     * Saison löschen, falls falsch eingetragen oder ausgefallen.
     * @param id id der zu löschenden Saison
     * @return Anzahl der gelöschten Zeilen, größer 0 wenn das Löschen erfolgreich war
     */
    public int delete(String id) {
        String sql = "DELETE FROM saison WHERE id=?;";

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

    /**
     * Prüfen ob es eine Saison mit diesem Namen schon gibt.
     * @param name Spieljahr als Zahl, z.B. 2526 für 25/26
     * @return true wenn bereits eine Saison mit diesem Namen existiert
     */
    public boolean existiert(int name) {
        String sql = "SELECT 1 FROM saison WHERE name = ?";
        try (Connection con = DBController.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, name);
            ResultSet rs = ps.executeQuery();
            return rs.next();   // true, wenn eine Zeile gefunden wurde
        } catch (SQLException e) {
            throw new RuntimeException("Prüfung auf vorhandene Saison fehlgeschlagen", e);
        }
    }
    
}
