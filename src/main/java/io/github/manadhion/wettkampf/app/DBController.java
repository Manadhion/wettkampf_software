package io.github.manadhion.wettkampf.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.prefs.Preferences;

/**
 * Baut Verbindungen zur SQLite-Datenbank auf und verwaltet den dauerhaft gespeicherten Pfad zur aktiven Datenbank.
 */
public class DBController {

    //Schlüssel unter dem der Pfad zur aktiven Datenbank dauerhaft gespeichert wird
    private static final String PFAD_SCHLUESSEL = "datenbankPfad";

    //dauerhafter Speicher für Programmeinstellungen, hält den Pfad zur aktiven Datenbank
    private static final Preferences EINSTELLUNGEN = Preferences.userNodeForPackage(DBController.class);

    private DBController() {} //es kann keine Instanz erstellt werden - private

    /**
     * Verbindung zur aktiven Datenbank aufbauen.
     * @return offene Verbindung zur SQLite-Datenbank
     * @throws SQLException wenn die Verbindung nicht aufgebaut werden kann
     */
    public static Connection getConnection() throws SQLException{
    	return DriverManager.getConnection("jdbc:sqlite:" + getDatenbankPfad());
    }

    /**
     * Gibt zurück ob schon eine Datenbank festgelegt wurde.
     * @return true wenn ein Pfad zur Datenbank gespeichert ist
     */
    public static boolean hatDatenbank() {
        return EINSTELLUNGEN.get(PFAD_SCHLUESSEL, null) != null;
    }

    /**
     * Den gespeicherten Pfad zur aktiven Datenbank holen.
     * @return Pfad zur Datenbank, oder null wenn keiner festgelegt ist
     */
    public static String getDatenbankPfad() {
        return EINSTELLUNGEN.get(PFAD_SCHLUESSEL, null);
    }

    /**
     * Den Pfad zur aktiven Datenbank dauerhaft festlegen.
     * @param pfad Pfad zur Datenbank-Datei
     */
    public static void setDatenbankPfad(String pfad) {
        EINSTELLUNGEN.put(PFAD_SCHLUESSEL, pfad);
    }

}
