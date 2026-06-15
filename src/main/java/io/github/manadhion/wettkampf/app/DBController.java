package io.github.manadhion.wettkampf.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.prefs.Preferences;

//Objekt um eine Verbindung zur Datenbank aufzubauen
public class DBController {

    //Schlüssel unter dem der Pfad zur aktiven Datenbank dauerhaft gespeichert wird
    private static final String PFAD_SCHLUESSEL = "datenbankPfad";

    //dauerhafter Speicher für Programmeinstellungen, hält den Pfad zur aktiven Datenbank
    private static final Preferences EINSTELLUNGEN = Preferences.userNodeForPackage(DBController.class);

    private DBController() {} //es kann keine Instanz erstellt werden - private

    //Methode die vom private DBController aus anderen Klassen aufgerufen werden kann
    public static Connection getConnection() throws SQLException{
    	return DriverManager.getConnection("jdbc:sqlite:" + getDatenbankPfad());
    }

    //gibt zurück ob schon eine Datenbank festgelegt wurde
    public static boolean hatDatenbank() {
        return EINSTELLUNGEN.get(PFAD_SCHLUESSEL, null) != null;
    }

    //den gespeicherten Pfad zur aktiven Datenbank holen
    public static String getDatenbankPfad() {
        return EINSTELLUNGEN.get(PFAD_SCHLUESSEL, null);
    }

    //den Pfad zur aktiven Datenbank dauerhaft festlegen
    public static void setDatenbankPfad(String pfad) {
        EINSTELLUNGEN.put(PFAD_SCHLUESSEL, pfad);
    }

}
