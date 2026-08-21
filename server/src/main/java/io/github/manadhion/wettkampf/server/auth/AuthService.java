package io.github.manadhion.wettkampf.server.auth;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final TokenHash tokenHash;
    private final Duration sitzungsdauer;
    private final Clock clock;

    @Autowired
    public AuthService(AuthRepository repository, PasswordEncoder passwordEncoder,
            TokenHash tokenHash, @Value("${wettkampf.sitzungsdauer}") Duration sitzungsdauer) {
        this(repository, passwordEncoder, tokenHash, sitzungsdauer, Clock.systemUTC());
    }

    AuthService(AuthRepository repository, PasswordEncoder passwordEncoder,
            TokenHash tokenHash, Duration sitzungsdauer, Clock clock) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.tokenHash = tokenHash;
        this.sitzungsdauer = sitzungsdauer;
        this.clock = clock;
    }

    @Transactional
    public AnmeldungAntwort anmelden(String name, String passwort) {
        Konto konto = repository.kontoMitName(name)
                .filter(Konto::aktiv)
                .filter(wert -> passwordEncoder.matches(passwort, wert.passwortHash()))
                .orElseThrow(AnmeldungFehlgeschlagenException::new);

        String token = tokenHash.neuesToken();
        Instant laeuftAb = clock.instant().plus(sitzungsdauer);
        repository.sitzungAnlegen(UUID.randomUUID(), konto.id(), tokenHash.hashen(token), laeuftAb);
        return new AnmeldungAntwort(token, laeuftAb);
    }

    @Transactional(readOnly = true)
    public boolean istTokenGueltig(String token) {
        return repository.istSitzungGueltig(tokenHash.hashen(token), clock.instant());
    }

    @Transactional
    public void abmelden(String token) {
        repository.sitzungWiderrufen(tokenHash.hashen(token), clock.instant());
    }

}
