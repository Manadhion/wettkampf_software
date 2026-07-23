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

/**
 * Data Access-Objekt für die Tabelle wettkampftage, koordiniert Arbeiten zwischen DB und App.
 */
public class WettkampftageDAO {

    /**
     * Legt die Tabelle wettkampftage an, falls sie noch nicht existiert.
     */
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

    /**
     * Neuen Wettkampftag in die Datenbank einfügen.
     * @param wettkampftage einzufügender Wettkampftag
     */
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

    /**
     * Bestehenden Wettkampftag ändern.
     * @param wettkampftage Wettkampftag mit den geänderten Werten
     */
    public void update(Wettkampftage wettkampftage) {

        String sql = "UPDATE wettkampftage SET datum=?, ausrichterverein=?, saisonID=? WHERE id=?";

        //Verbindung zu DB und arbeit ausführen
        try (Connection con = DBController.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){

			//set Values
            ps.setObject(1, wettkampftage.getDatum());
            ps.setString(2, wettkampftage.getAusrichterVerein());
            ps.setString(3, wettkampftage.getSaisonID());
            ps.setString(4, wettkampftage.getId());

			ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}

    }

    /**
     * Alle Wettkampftage chronologisch nach Datum holen.
     * @return Liste aller Wettkampftage
     */
    public List<Wettkampftage> alleTage() {
        List<Wettkampftage> wettkampftage = new ArrayList<>();

        String sql = "Select * FROM wettkampftage ORDER BY datum";

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

    /**
     * Alle Wettkampftage einer bestimmten Saison chronologisch nach Datum holen.
     * @param saisonID id der Saison
     * @return Liste der Wettkampftage dieser Saison
     */
    public List<Wettkampftage> tageVonSaison(String saisonID) {
        List<Wettkampftage> wettkampftage = new ArrayList<>();

        String sql = "SELECT * FROM wettkampftage WHERE saisonID = ? ORDER BY datum";

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

    /**
     * Wettkampftag löschen, falls falsch eingetragen oder ausgefallen.
     * @param id id des zu löschenden Wettkampftages
     * @return Anzahl der gelöschten Zeilen, größer 0 wenn das Löschen erfolgreich war
     */
    public int delete(String id) {
        String sql = "DELETE FROM wettkampftage WHERE id=?;";

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
}
