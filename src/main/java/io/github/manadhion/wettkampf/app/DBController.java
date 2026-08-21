package io.github.manadhion.wettkampf.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Baut Verbindungen zur SQLite-Datenbank auf und verwaltet den dauerhaft gespeicherten Pfad zur aktiven Datenbank.
 */
public class DBController {

    private DBController() {} //es kann keine Instanz erstellt werden - private

    /**
     * Verbindung zur aktiven Datenbank aufbauen.
     * @return offene Verbindung zur SQLite-Datenbank
     * @throws SQLException wenn die Verbindung nicht aufgebaut werden kann
     */
    public static Connection getConnection() throws SQLException{
		Connection con = DriverManager.getConnection("jdbc:sqlite:" + getDatenbankPfad());
        try (var stmt = con.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException e) {
            con.close();
            throw e;
        }
        return con;
    }

    /**
     * Gibt zurück ob schon eine Datenbank festgelegt wurde.
     * @return true wenn ein Pfad zur Datenbank gespeichert ist
     */
    public static boolean hatDatenbank() {
        return Anwendungskonfiguration.getSportleiterDatenbankPfad().isPresent();
    }

    /**
     * Den gespeicherten Pfad zur aktiven Datenbank holen.
     * @return Pfad zur Datenbank, oder null wenn keiner festgelegt ist
     */
    public static String getDatenbankPfad() {
        return Anwendungskonfiguration.getSportleiterDatenbankPfad().orElse(null);
    }

    /**
     * Den Pfad zur aktiven Datenbank dauerhaft festlegen.
     * @param pfad Pfad zur Datenbank-Datei
     */
    public static void setDatenbankPfad(String pfad) {
        Anwendungskonfiguration.setSportleiterDatenbankPfad(pfad);
    }

}
