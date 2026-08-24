package io.github.manadhion.wettkampf.server.auth;

import java.util.UUID;

public record Konto(UUID id, String name, String passwortHash, boolean aktiv) {
}
