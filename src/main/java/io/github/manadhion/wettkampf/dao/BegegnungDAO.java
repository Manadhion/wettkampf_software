package io.github.manadhion.wettkampf.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import io.github.manadhion.wettkampf.app.DBController;
import io.github.manadhion.wettkampf.data.Begegnung;
import io.github.manadhion.wettkampf.data.Wettkampftage;

//Data Access-Objekt, koordiniert arbeiten zwischen DB-Tabelle und App
public class BegegnungDAO {
    
    //neue Tabelle anlegen wenn sie noch nicht existiert
    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS begegnung ("
                + "id TEXT PRIMARY KEY,"
                + "heim TEXT REFERENCES mannschaft(id) NOT NULL,"        //Referenz zur id der Mannschaft die als Heim antritt
                + "gegner TEXT REFERENCES mannschaft(id) NOT NULL,"      //Referenz zur id der Mannschaft die als Gast antritt
                + "wettkampftag TEXT REFERENCES wettkampftage(id) NOT NULL" //Referenz zur id des Wettkampftages
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
			throw new RuntimeException("Tabelle 'begegnung' konnte nicht angelegt werden", e);
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
			e.printStackTrace();
		}
		
		//wenn erg >0 ist war das Löschen erfolgreich
		return erg;
    }

    //alle Begegnungen eines bestimmten Tages auslesen
    public List<Begegnung> begegnungenAnDiesemTag(String wettkampftag) {
        List<Begegnung> begegnungen = new ArrayList<>();
        
        String sql = "SELECT * FROM begegnungen WHERE wettkampftag = ?";

        //Abrufen der Werte
		try (Connection con = DBController.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){

			ps.setString(1, wettkampftag);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
                //für jede Zeile ein neues Objekt von Wettkampftage erzeugen und der Liste hinzufügen
				Begegnung b = new Begegnung(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4));

                begegnungen.add(b);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

        return begegnungen;
    }

}
