package io.github.manadhion.wettkampf.server.auth;

import java.time.Instant;

public record AnmeldungAntwort(String token, Instant laeuftAb) {
}
