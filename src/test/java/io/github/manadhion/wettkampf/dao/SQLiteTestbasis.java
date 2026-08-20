package io.github.manadhion.wettkampf.dao;

import io.github.manadhion.wettkampf.app.Controller;
import io.github.manadhion.wettkampf.app.DBController;
import java.nio.file.Path;
import java.util.prefs.Preferences;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

/** Gemeinsamer Aufbau für Tests gegen eine kurzlebige lokale SQLite-Datei. */
abstract class SQLiteTestbasis {

    @TempDir
    Path temporaererOrdner;

    private String vorherigerDatenbankpfad;

    @BeforeEach
    void lokaleTestdatenbankVorbereiten() {
        vorherigerDatenbankpfad = DBController.getDatenbankPfad();
        neueTestdatenbank("test.db");
    }

    @AfterEach
    void datenbankpfadWiederherstellen() {
        if (vorherigerDatenbankpfad == null) {
            Preferences.userNodeForPackage(DBController.class).remove("datenbankPfad");
        } else {
            DBController.setDatenbankPfad(vorherigerDatenbankpfad);
        }
    }

    Path neueTestdatenbank(String dateiname) {
        Path datenbank = temporaererOrdner.resolve(dateiname);
        DBController.setDatenbankPfad(datenbank.toString());
        new Controller().createTableIfNotExists();
        return datenbank;
    }
}
