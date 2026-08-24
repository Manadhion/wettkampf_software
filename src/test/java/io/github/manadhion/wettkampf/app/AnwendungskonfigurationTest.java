package io.github.manadhion.wettkampf.app;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;

import org.junit.jupiter.api.Test;

class AnwendungskonfigurationTest {

    @Test
    void verwendetFestHinterlegteOnlineAdresse() {
        assertEquals(URI.create("https://wettkampf-api.meshalchemy.com"),
                Anwendungskonfiguration.getOnlineServerAdresse());
    }

}
