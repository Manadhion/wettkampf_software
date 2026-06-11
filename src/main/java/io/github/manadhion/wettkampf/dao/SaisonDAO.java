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

//Data Access-Objekt, koordiniert arbeiten zwischen DB-Tabelle und App
public class SaisonDAO {

    //neue Tabelle anlegen wenn sie noch nicht existiert
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

    //neue Entität einfügen
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

    //bestehende Saison ändern
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

    //alle Saisons abfragen
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

    //Saison löschen, falls falsch eingetragen oder ausgefallen
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

    //prüfen ob es eine Saison mit diesem Namen schon gibt
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
