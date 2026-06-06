package io.github.manadhion.wettkampf.dao;

import io.github.manadhion.wettkampf.app.DBController;
import io.github.manadhion.wettkampf.data.Mannschaft;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

//Data Access-Objekt, koordiniert arbeiten zwischen DB-Tabelle und App
public class MannschaftDAO {
    
    //neue Tabelle anlegen wenn sie noch nicht existiert
    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS mannschaft ("
                + "id TEXT PRIMARY KEY,"
                + "name TEXT NOT NULL,"     //Name der Mannschaft
                + "klasse TEXT"                //Klasse in der sich die Mannschaft befindet, z.B. A-Klasse
                + ")";
        
        try (Connection con = DBController.getConnection();
            Statement stmt = con.createStatement()) {
                stmt.execute(sql);
        } catch (SQLException e) {
	        throw new RuntimeException("Tabelle 'mannschaft' konnte nicht angelegt werden", e);
	    }
    }


    //neue Entität einfügen
    public void insert(Mannschaft mannschaft) {
        
        //erstellen oder ignorieren wenn es die Entität bereits gibt
        String sql = "INSERT OR IGNORE INTO mannschaft(id, name, klasse) "
                + "VALUES(?,?,?)";

        //Verbindung zu DB und arbeit ausführen
        try (Connection con = DBController.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){
			
			//set Values
            ps.setString(1, mannschaft.getId());
			ps.setString(2, mannschaft.getName());
            ps.setString(3, mannschaft.getKlasse());
			
			ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}

    }

    //Liste mit allen Mannschaften
    public List<Mannschaft> alleMannschaften() {

        List<Mannschaft> mannschaften = new ArrayList<>();

        String sql = "Select * FROM mannschaft";

        //Abrufen der Werte
		try (Connection con = DBController.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
                //für jede Zeile ein neues Objekt von Wettkampftage erzeugen und der Liste hinzufügen
				Mannschaft m = new Mannschaft(rs.getString(1), rs.getString(2), rs.getString(5));

                mannschaften.add(m);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

        return mannschaften;

    }

    //MAnnschaft durch ID finden
    public Mannschaft mannschaftMitID(String mID) {

        Mannschaft m;

        String sql = "Select * FROM mannschaft WHERE id=?";

        //Abrufen der Werte
		try (Connection con = DBController.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){
			
            ps.setString(1, mID);

			ResultSet rs = ps.executeQuery();

            m = new Mannschaft(rs.getString(1), rs.getString(2), rs.getString(3));
        
			return m;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
        
        return null;
    }



}
