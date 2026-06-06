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
import io.github.manadhion.wettkampf.data.Schuetze;
import io.github.manadhion.wettkampf.data.Wettkampftage;


//Data Access-Objekt, koordiniert arbeiten zwischen DB-Tabelle und App
public class SchuetzeDAO {
    
    //neue Tabelle anlegen wenn sie noch nicht existiert
    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS schuetze ("
                + "id TEXT PRIMARY KEY,"
                + "vorname TEXT NOT NULL,"
                + "nachname TEXT NOT NULL,"
                + "mannschaftid TEXT REFERENCES mannschaft(id) NOT NULL,"   //Referenz zur id der Mannschaft des Schützen
                + "altersKlasse INTEGER NOT NULL"   //Welcher Altersklasse der Schütze angehört, z.B. Jugend, Herren usw.
                + ")";
        
        try (Connection con = DBController.getConnection();
            Statement stmt = con.createStatement()) {
                stmt.execute(sql);
        } catch (SQLException e) {
	        throw new RuntimeException("Tabelle 'schuetze' konnte nicht angelegt werden", e);
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

    //alle Schützen einer Mannschaft
    public List<Schuetze> schuetzenVonMannschaft(String id) {

        List<Schuetze> schuetze = new ArrayList<>();

        String sql = "SELECT * FROM schuetze WHERE mannschaftid=?";

        //Abrufen der Werte
		try (Connection con = DBController.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){

            ps.setString(1, id);
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
                //für jede Zeile ein neues Objekt von Schuetze erzeugen und der Liste hinzufügen
				Schuetze s = new Schuetze(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getInt(5));

                schuetze.add(s);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

        return schuetze;
    }



}
