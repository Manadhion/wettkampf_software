package io.github.manadhion.wettkampf.server.auth;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Begrenzt Anmeldeversuche je Clientadresse auf zehn Anfragen pro Minute. */
@Component
public class AnmeldebegrenzungFilter extends OncePerRequestFilter {

    private static final Duration FENSTER = Duration.ofMinutes(1);
    private static final int MAXIMALE_VERSUCHE = 10;

    private final Map<String, Versuchsfenster> versuche = new ConcurrentHashMap<>();
    private final Clock clock;

    public AnmeldebegrenzungFilter() {
        this(Clock.systemUTC());
    }

    AnmeldebegrenzungFilter(Clock clock) {
        this.clock = clock;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equalsIgnoreCase(request.getMethod())
                || !"/api/v1/anmeldung".equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        Instant jetzt = clock.instant();
        Versuchsfenster fenster = versuche.compute(clientAdresse(request), (adresse, alt) ->
                alt == null || !jetzt.isBefore(alt.beginn().plus(FENSTER))
                        ? new Versuchsfenster(jetzt, 1)
                        : new Versuchsfenster(alt.beginn(), alt.anzahl() + 1));

        if (fenster.anzahl() > MAXIMALE_VERSUCHE) {
            response.setStatus(429);
            response.setHeader("Retry-After", "60");
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                    "{\"code\":\"ZU_VIELE_ANMELDEVERSUCHE\","
                    + "\"nachricht\":\"Bitte warten Sie vor dem nächsten Anmeldeversuch.\"}");
            return;
        }
        filterChain.doFilter(request, response);
        if (versuche.size() > 10_000) {
            versuche.entrySet().removeIf(e ->
                    !jetzt.isBefore(e.getValue().beginn().plus(FENSTER)));
        }
    }

    private String clientAdresse(HttpServletRequest request) {
        String weitergeleitet = request.getHeader("X-Forwarded-For");
        if (weitergeleitet != null && !weitergeleitet.isBlank()) {
            return weitergeleitet.split(",", 2)[0].trim();
        }
        return request.getRemoteAddr();
    }

    private record Versuchsfenster(Instant beginn, int anzahl) {
    }
}
