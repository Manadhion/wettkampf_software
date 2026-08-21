package io.github.manadhion.wettkampf.server.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AnmeldungAnfrageTest {

    @Test
    void gibtPasswortNichtInProtokolltextAus() {
        String text = new AnmeldungAnfrage("online", "streng-geheim").toString();

        assertTrue(text.contains("online"));
        assertFalse(text.contains("streng-geheim"));
    }
}
