package io.github.manadhion.wettkampf.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.manadhion.wettkampf.app.DBController;
import io.github.manadhion.wettkampf.data.Saison;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DatenbankIsolationTest extends SQLiteTestbasis {

    @Test
    void haeltZweiLokaleDatenbankdateienVollstaendigGetrennt() {
        Path ersteDatenbank = Path.of(DBController.getDatenbankPfad());
        SaisonDAO saisonDAO = new SaisonDAO();
        saisonDAO.insert(new Saison("saison-erste", 2026));
        assertEquals(1, saisonDAO.alleSaisons().size());

        Path zweiteDatenbank = neueTestdatenbank("zweite.db");
        assertEquals(0, saisonDAO.alleSaisons().size());
        saisonDAO.insert(new Saison("saison-zweite", 2027));

        DBController.setDatenbankPfad(ersteDatenbank.toString());
        assertEquals(2026, saisonDAO.alleSaisons().getFirst().getName());

        DBController.setDatenbankPfad(zweiteDatenbank.toString());
        assertEquals(2027, saisonDAO.alleSaisons().getFirst().getName());
    }
}
