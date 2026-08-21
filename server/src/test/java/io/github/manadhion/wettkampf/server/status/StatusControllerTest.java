package io.github.manadhion.wettkampf.server.status;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StatusControllerTest {

    @Test
    void meldetApiVersionUndBereitschaft() {
        var status = new StatusController().status();

        assertEquals("bereit", status.get("status"));
        assertEquals("v1", status.get("apiVersion"));
    }
}
