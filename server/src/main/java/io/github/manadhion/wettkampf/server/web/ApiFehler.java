package io.github.manadhion.wettkampf.server.web;

import java.time.Instant;

public record ApiFehler(String code, String nachricht, Instant zeitpunkt) {
}
