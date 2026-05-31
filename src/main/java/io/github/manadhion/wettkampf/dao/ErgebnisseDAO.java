package io.github.manadhion.wettkampf.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import io.github.manadhion.wettkampf.app.DBController;
import io.github.manadhion.wettkampf.data.Ergebnisse;

//Data Access-Objekt, koordiniert arbeiten zwischen DB-Tabelle und App
public class ErgebnisseDAO {

    //neue Tabelle anlegen wenn sie noch nicht existiert
    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS ergebnisse ("
                + "id TEXT PRIMARY KEY,"
                + "schuetzeID TEXT REFERENCES schuetze(id),"
                + "wettkampftagID TEXT REFERENCES wettkampftage(id),"
                + "ergebnis INTEGER"
                + ")";
        
        try (Connection con = DBController.getConnection();
            Statement stmt = con.createStatement()) {
                stmt.execute(sql);
        } catch (SQLException e) {
	        e.printStackTrace();
	    }
    }

    //neue Entität einfügen
    public void insert(Ergebnisse ergebnisse) {
        
        //erstellen oder ignorieren wenn es die Entität bereits gibt
        String sql = "INSERT OR IGNORE INTO ergebnisse(id, schuetzeID, wettkampftagID, ergebnis) "
                + "VALUES(?,?,?,?)";

        //Verbindung zu DB und arbeit ausführen
        try (Connection con = DBController.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){
			
			//set Values
			ps.setString(1, ergebnisse.getId());
            ps.setString(2, ergebnisse.getSchuetzeID());
            ps.setString(3, ergebnisse.getWettkampftagID());
            ps.setInt(4, ergebnisse.getErgebnis());
			
			ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}

    }
    
}
