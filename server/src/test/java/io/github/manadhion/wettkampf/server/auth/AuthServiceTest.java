package io.github.manadhion.wettkampf.server.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {

    private final AuthRepository repository = mock(AuthRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final TokenHash tokenHash = new TokenHash();
    private final Instant jetzt = Instant.parse("2026-08-21T15:00:00Z");
    private final AuthService service = new AuthService(repository, passwordEncoder, tokenHash,
            Duration.ofHours(12), Clock.fixed(jetzt, ZoneOffset.UTC));

    @Test
    void speichertNurDenHashDesZufaelligenSitzungstokens() {
        Konto konto = new Konto(UUID.randomUUID(), "vereine", "{bcrypt}hash", true);
        when(repository.kontoMitName("vereine")).thenReturn(Optional.of(konto));
        when(passwordEncoder.matches("geheim", konto.passwortHash())).thenReturn(true);
        ArgumentCaptor<String> gespeicherterHash = ArgumentCaptor.forClass(String.class);

        AnmeldungAntwort antwort = service.anmelden("vereine", "geheim");

        verify(repository).alteSitzungenLoeschen(jetzt.minus(Duration.ofDays(7)));
        verify(repository).sitzungAnlegen(any(UUID.class), eq(konto.id()),
                gespeicherterHash.capture(), eq(jetzt.plus(Duration.ofHours(12))));
        assertEquals(64, gespeicherterHash.getValue().length());
        assertNotEquals(antwort.token(), gespeicherterHash.getValue());
        assertEquals(jetzt.plus(Duration.ofHours(12)), antwort.laeuftAb());
    }

    @Test
    void verrätBeiFalschenZugangsdatenKeineEinzelheiten() {
        when(repository.kontoMitName("unbekannt")).thenReturn(Optional.empty());

        assertThrows(AnmeldungFehlgeschlagenException.class,
                () -> service.anmelden("unbekannt", "falsch"));
    }

}
