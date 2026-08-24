package io.github.manadhion.wettkampf.app;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class DatenServiceFabrikTest {

    @Test
    void erstelltImSportleiterbetriebDenLokalenDatenservice() {
        assertInstanceOf(LokalerWettkampfDatenService.class,
                DatenServiceFabrik.erstellen(Betriebsart.SPORTLEITER));
    }

    @Test
    void verwendetImOnlinebetriebKeinLokalesFallback() {
        assertThrows(IllegalStateException.class,
                () -> DatenServiceFabrik.erstellen(Betriebsart.ONLINE));
    }
}
