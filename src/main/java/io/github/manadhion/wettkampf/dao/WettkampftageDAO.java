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
import io.github.manadhion.wettkampf.data.Wettkampftage;

//Data Access-Objekt, koordiniert arbeiten zwischen DB-Tabelle und App
public class WettkampftageDAO {
    
    //neue Tabelle anlegen wenn sie noch nicht existiert
    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS wettkampftage ("
                + "id TEXT PRIMARY KEY,"
                + "datum DATE NOT NULL UNIQUE,"     //Datum an dem der Wettkampftag stattfindet, darf nicht doppel sein
                + "ausrichterverein TEXT NOT NULL,"   //Ausrichtender Verein des Wettkampftages
                + "saisonID TEXT REFERENCES saison(id) NOT NULL"          //Referenz zu Saison
                + ")";
        
        try (Connection con = DBController.getConnection();
            Statement stmt = con.createStatement()) {
                stmt.execute(sql);
        } catch (SQLException e) {
	        throw new RuntimeException("Tabelle 'Wettkampftage' konnte nicht angelegt werden", e);
	    }
    }

    //neue Entität einfügen
    public void insert(Wettkampftage wettkampftage) {
        
        //erstellen oder ignorieren wenn es die Entität bereits gibt
        String sql = "INSERT OR IGNORE INTO wettkampftage(id, datum, ausrichterverein, saisonID) "
                + "VALUES(?,?,?,?)";

        //Verbindung zu DB und arbeit ausführen
        try (Connection con = DBController.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){
			
			//set Values
			ps.setString(1, wettkampftage.getId());
            ps.setObject(2, wettkampftage.getDatum());
            ps.setString(3, wettkampftage.getAusrichterVerein());
            ps.setString(4, wettkampftage.getSaisonID());
			
			ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}

    }

    //Liste mit allen Wettkampftagen
    public List<Wettkampftage> alleTage() {
        List<Wettkampftage> wettkampftage = new ArrayList<>();

        String sql = "Select * FROM wettkampftage";

        //Abrufen der Werte
		try (Connection con = DBController.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
                //für jede Zeile ein neues Objekt von Wettkampftage erzeugen und der Liste hinzufügen
				Wettkampftage wk = new Wettkampftage(rs.getString(1), LocalDate.parse(rs.getString(2)), rs.getString(3), rs.getString(4));

                wettkampftage.add(wk);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

        return wettkampftage;
    }

    //Liste mit allen Wettkampftagen einer bestimmten Saison
    public List<Wettkampftage> tageVonSaison(String saisonID) {
        List<Wettkampftage> wettkampftage = new ArrayList<>();

        String sql = "SELECT * FROM wettkampftage WHERE saisonID = ?";

        //Abrufen der Werte
		try (Connection con = DBController.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){

			ps.setString(1, saisonID);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
                //für jede Zeile ein neues Objekt von Wettkampftage erzeugen und der Liste hinzufügen
				Wettkampftage wk = new Wettkampftage(rs.getString(1), LocalDate.parse(rs.getString(2)), rs.getString(3), rs.getString(4));

                wettkampftage.add(wk);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

        return wettkampftage;
    }
}
