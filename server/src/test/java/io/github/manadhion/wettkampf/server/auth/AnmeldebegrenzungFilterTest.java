package io.github.manadhion.wettkampf.server.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class AnmeldebegrenzungFilterTest {

    @Test
    void blockiertDenElftenAnmeldeversuchDerselbenAdresse() throws Exception {
        AnmeldebegrenzungFilter filter = new AnmeldebegrenzungFilter(Clock.fixed(
                Instant.parse("2026-08-24T09:00:00Z"), ZoneOffset.UTC));

        for (int versuch = 1; versuch <= 10; versuch++) {
            MockHttpServletResponse antwort = ausfuehren(filter, "203.0.113.10");
            assertNotEquals(429, antwort.getStatus());
        }

        MockHttpServletResponse blockiert = ausfuehren(filter, "203.0.113.10");
        assertEquals(429, blockiert.getStatus());
        assertEquals("60", blockiert.getHeader("Retry-After"));
    }

    private MockHttpServletResponse ausfuehren(AnmeldebegrenzungFilter filter, String adresse)
            throws Exception {
        MockHttpServletRequest anfrage = new MockHttpServletRequest("POST", "/api/v1/anmeldung");
        anfrage.addHeader("X-Forwarded-For", adresse);
        MockHttpServletResponse antwort = new MockHttpServletResponse();
        filter.doFilter(anfrage, antwort, new MockFilterChain());
        return antwort;
    }
}
