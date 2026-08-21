package io.github.manadhion.wettkampf.server.auth;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface AuthRepository {

    boolean hatKonto();
    void kontoAnlegen(UUID id, String name, String passwortHash);
    Optional<Konto> kontoMitName(String name);
    void sitzungAnlegen(UUID id, UUID kontoId, String tokenHash, Instant laeuftAb);
    boolean istSitzungGueltig(String tokenHash, Instant jetzt);
    int sitzungWiderrufen(String tokenHash, Instant jetzt);
}
