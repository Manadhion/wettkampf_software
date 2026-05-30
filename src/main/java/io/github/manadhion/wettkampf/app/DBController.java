package io.github.manadhion.wettkampf.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//Objekt um eine Verbindung zur Datenbank aufzubauen
public class DBController {

    //Verbindung zu DB mit Dateipfad - private,damit nicht von außen darauf zugegriffen werden kann
    private static final String DB_PATH = "jdbc:sqlite:" + System.getProperty("user.home") + "/" + "wettkampf_db.db";
    private DBController() {} //es kann keine Instanz erstellt werden - private

    //Methode die vom private DBController aus anderen Klassen aufgerufen werden kann
    public static Connection getConnection() throws SQLException{
    	return DriverManager.getConnection(DB_PATH);
    }
    
}
