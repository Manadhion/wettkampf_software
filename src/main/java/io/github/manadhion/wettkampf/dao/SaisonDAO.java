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
    
}
