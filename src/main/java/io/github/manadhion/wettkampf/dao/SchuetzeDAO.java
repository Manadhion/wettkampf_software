package io.github.manadhion.wettkampf.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import io.github.manadhion.wettkampf.app.DBController;
import io.github.manadhion.wettkampf.data.Schuetze;


//Data Access-Objekt, koordiniert arbeiten zwischen DB-Tabelle und App
public class SchuetzeDAO {
    
    //neue Tabelle anlegen wenn sie noch nicht existiert
    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS schuetze ("
                + "id TEXT PRIMARY KEY,"
                + "vorname TEXT,"
                + "nachname TEXT,"
                + "mannschaftid TEXT REFERENCES mannschaft(id),"
                + "altersKlasse INTEGER"
                + ")";
        
        try (Connection con = DBController.getConnection();
            Statement stmt = con.createStatement()) {
                stmt.execute(sql);
        } catch (SQLException e) {
	        e.printStackTrace();
	    }
    }

    //neue Entität einfügen
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
            ps.setInt(5, schuetze.getAltersKlasse());
			
			ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}

    }




}
