package io.github.manadhion.wettkampf.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class JsonTest {

    @Test
    void liestUndSchreibtVerschachtelteApiDatenOhneZusatzbibliothek() {
        Map<String, Object> original = Json.objekt(
                "name", "Schützen \"Nord\"",
                "wert", 500,
                "optional", null,
                "liste", List.of(true, 3));

        Map<String, Object> gelesen = Json.alsObjekt(Json.lesen(Json.schreiben(original)));

        assertEquals("Schützen \"Nord\"", Json.text(gelesen, "name"));
        assertEquals(500, Json.ganzzahl(gelesen, "wert"));
        assertNull(gelesen.get("optional"));
        assertEquals(2, Json.alsListe(gelesen.get("liste")).size());
    }
}
