package io.github.manadhion.wettkampf.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
                + "schuetzeID TEXT REFERENCES schuetze(id) NOT NULL,"            //Referenz zur id des Schützen um dessen Ergebnis es geht
                + "wettkampftagID TEXT REFERENCES wettkampftage(id) NOT NULL,"   //id zum Wettkampftag an dem das Ergebnis geschossen wurde
                + "ergebnis INTEGER NOT NULL"
                + ")";
        
        try (Connection con = DBController.getConnection();
            Statement stmt = con.createStatement()) {
                stmt.execute(sql);
        } catch (SQLException e) {
	        throw new RuntimeException("Tabelle 'ergebnisse' konnte nicht angelegt werden", e);
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

    //Ergebnis eines Schützen an einem bestimmten Wettkampftag holen, oder null wenn es noch keins gibt
    public Ergebnisse ergebnisFuerSchuetzeUndTag(String schuetzeID, String wettkampftagID) {

        Ergebnisse ergebnis = null;

        String sql = "SELECT * FROM ergebnisse WHERE schuetzeID=? AND wettkampftagID=?";

        //Abrufen der Werte
        try (Connection con = DBController.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)){

            //set Values
            ps.setString(1, schuetzeID);
            ps.setString(2, wettkampftagID);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                //gefundene Zeile in ein Objekt umwandeln
                ergebnis = new Ergebnisse(rs.getString(1), rs.getString(2), rs.getString(3), rs.getInt(4));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ergebnis;
    }

    //Gesamtergebnis einer Mannschaft an einem Tag = Summe der besten 3 Schützen-Ergebnisse
    public int gesamtErgebnisBeste3(String mannschaftID, String wettkampftagID) {

        int gesamt = 0;

        //die besten 3 Ergebnisse direkt von der DB sortiert und begrenzt holen und aufsummieren
        String sql = "SELECT COALESCE(SUM(ergebnis), 0) FROM ("
                + "SELECT e.ergebnis FROM ergebnisse e "
                + "JOIN schuetze s ON e.schuetzeID = s.id "
                + "WHERE s.mannschaftid = ? AND e.wettkampftagID = ? "
                + "ORDER BY e.ergebnis DESC LIMIT 3"
                + ")";

        //Abrufen der Werte
        try (Connection con = DBController.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)){

            //set Values
            ps.setString(1, mannschaftID);
            ps.setString(2, wettkampftagID);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                gesamt = rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return gesamt;
    }

    //bestehendes Ergebnis ändern
    public void update(Ergebnisse ergebnisse) {

        String sql = "UPDATE ergebnisse SET ergebnis=? WHERE id=?";

        //Verbindung zu DB und arbeit ausführen
        try (Connection con = DBController.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)){

            //set Values
            ps.setInt(1, ergebnisse.getErgebnis());
            ps.setString(2, ergebnisse.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
