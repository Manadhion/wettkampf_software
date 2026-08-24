package io.github.manadhion.wettkampf.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.manadhion.wettkampf.app.LokalerWettkampfDatenService;
import io.github.manadhion.wettkampf.app.WettkampfDatenService;
import io.github.manadhion.wettkampf.data.Saison;

class LokalerWettkampfDatenServiceTest extends SQLiteTestbasis {

    private final WettkampfDatenService datenService = new LokalerWettkampfDatenService();

    @Test
    void verwaltetSaisonsUeberDieFachlicheSchnittstelle() {
        Saison saison = new Saison("saison-2026", 2627);

        datenService.saisonAnlegen(saison);

        assertEquals(1, datenService.alleSaisons().size());
        assertEquals(2627, datenService.saisonMitId("saison-2026").getName());
        assertTrue(datenService.saisonExistiert(2627));

        saison.setName(2728);
        datenService.saisonAktualisieren(saison);
        assertEquals(2728, datenService.saisonMitId("saison-2026").getName());

        assertEquals(1, datenService.saisonLoeschen("saison-2026"));
        assertFalse(datenService.saisonExistiert(2728));
    }
}
