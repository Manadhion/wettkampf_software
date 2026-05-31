package io.github.manadhion.wettkampf.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import io.github.manadhion.wettkampf.app.DBController;
import io.github.manadhion.wettkampf.data.Begegnung;

//Data Access-Objekt, koordiniert arbeiten zwischen DB-Tabelle und App
public class BegegnungDAO {
    
    //neue Tabelle anlegen wenn sie noch nicht existiert
    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS begegnung ("
                + "id TEXT PRIMARY KEY,"
                + "heim TEXT REFERENCES mannschaft(id),"
                + "gegner TEXT REFERENCES mannschaft(id),"
                + "wettkampftag TEXT REFERENCES wettkampftage(id),"
                + ")";
        
        try (Connection con = DBController.getConnection();
            Statement stmt = con.createStatement()) {
                stmt.execute(sql);
        } catch (SQLException e) {
	        e.printStackTrace();
	    }
    }

    //neue Entität einfügen
    public void insert(Begegnung begegnung) {
        
        //erstellen oder ignorieren wenn es die Entität bereits gibt
        String sql = "INSERT OR IGNORE INTO begegnung(id, heim, gegner, wettkampftag) "
                + "VALUES(?,?,?,?)";

        //Verbindung zu DB und arbeit ausführen
        try (Connection con = DBController.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){
			
			//set Values
			ps.setString(1, begegnung.getId());
            ps.setString(2, begegnung.getHeim());
            ps.setString(3, begegnung.getGegner());
            ps.setString(4, begegnung.getWettkampftag());
			
			ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
    }

    //Begegnung löschen, falls falsch eingetragen oder ausgefallen
    public int delete(String id) {
        String sql = "DELETE FROM begegnung WHERE id=?;";

        //return Statement
        int erg = 0;

        //Löschvorgang
        try(Connection con = DBController.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){
			ps.setString(1, id);;
			erg = ps.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//wenn erg >0 ist war das Löschen erfolgreich
		return erg;
    }


}
