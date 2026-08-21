package io.github.manadhion.wettkampf.server.auth;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class KontoInitialisierung implements ApplicationRunner {

    private final AuthRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final String name;
    private final String initialesPasswort;

    public KontoInitialisierung(AuthRepository repository, PasswordEncoder passwordEncoder,
            @Value("${wettkampf.konto.name}") String name,
            @Value("${wettkampf.konto.initiales-passwort}") String initialesPasswort) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.name = name;
        this.initialesPasswort = initialesPasswort;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (repository.hatKonto()) {
            return;
        }
        if (name.isBlank() || initialesPasswort.isBlank()) {
            throw new IllegalStateException(
                    "Für den ersten Start müssen WETTKAMPF_KONTO_NAME und "
                            + "WETTKAMPF_KONTO_PASSWORT gesetzt sein.");
        }
        repository.kontoAnlegen(UUID.randomUUID(), name, passwordEncoder.encode(initialesPasswort));
    }
}
