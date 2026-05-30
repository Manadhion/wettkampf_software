package io.github.manadhion.wettkampf.dao;

import io.github.manadhion.wettkampf.app.DBController;
import io.github.manadhion.wettkampf.data.Mannschaft;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

//Data Access-Objekt, koordiniert arbeiten zwischen DB-Tabelle und App
public class MannschaftDAO {
    
    //neue Tabelle anlegen wenn sie noch nicht existiert
    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS mannschaft ("
                + "id TEXT,"
                + "name TEXT,"
                + "ergebnisHeute INTEGER,"
                + "ergebnisGesamt INTEGER,"
                + "punkte INTEGER,"
                + "kaempfeBeendet INTEGER,"
                + "klasse TEXT"
                + ")";
        
        try (Connection con = DBController.getConnection();
            Statement stmt = con.createStatement()) {
                stmt.execute(sql);
        } catch (SQLException e) {
	        e.printStackTrace();
	    }
    }


    //neue Entität einfügen
    public void insert(Mannschaft mannschaft) {
        
        //erstellen oder ignorieren wenn es die Entität bereits gibt
        String sql = "INSERT OR IGNORE INTO mannschaft(id, name, ergebnisHeute, ergebnisGesamt, punkte, kaempfeBeendet, klasse) "
                + "VALUES(?,?,?,?,?,?,?)";

        //Verbindung zu DB und arbeit ausführen
        try (Connection con = DBController.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){
			
			//set Values
            ps.setString(1, mannschaft.getId());
			ps.setString(2, mannschaft.getName());
            ps.setInt(3, mannschaft.getErgebnisHeute());
            ps.setInt(4, mannschaft.getErgebnisGesamt());
            ps.setInt(5, mannschaft.getPunkte());
            ps.setInt(6, mannschaft.getKaempfeBeendet());
            ps.setString(7, mannschaft.getKlasse());
			
			ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}

    }





}
