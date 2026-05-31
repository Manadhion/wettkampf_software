package io.github.manadhion.wettkampf.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import io.github.manadhion.wettkampf.app.DBController;
import io.github.manadhion.wettkampf.data.Wettkampftage;

//Data Access-Objekt, koordiniert arbeiten zwischen DB-Tabelle und App
public class WettkampftageDAO {
    
    //neue Tabelle anlegen wenn sie noch nicht existiert
    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS wettkampftage ("
                + "id TEXT PRIMARY KEY,"
                + "datum DATE,"
                + "ausrichterverein TEXT"
                + ")";
        
        try (Connection con = DBController.getConnection();
            Statement stmt = con.createStatement()) {
                stmt.execute(sql);
        } catch (SQLException e) {
	        e.printStackTrace();
	    }
    }

    //neue Entität einfügen
    public void insert(Wettkampftage wettkampftage) {
        
        //erstellen oder ignorieren wenn es die Entität bereits gibt
        String sql = "INSERT OR IGNORE INTO wettkampftage(id, datum, ausrichterverein) "
                + "VALUES(?,?,?)";

        //Verbindung zu DB und arbeit ausführen
        try (Connection con = DBController.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){
			
			//set Values
			ps.setString(1, wettkampftage.getId());
            ps.setObject(2, wettkampftage.getDatum());
            ps.setString(3, wettkampftage.getAusrichterVerein());
			
			ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}

    }
}
