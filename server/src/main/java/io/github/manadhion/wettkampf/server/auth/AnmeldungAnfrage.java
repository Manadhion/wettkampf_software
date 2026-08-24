package io.github.manadhion.wettkampf.server.auth;

import jakarta.validation.constraints.NotBlank;

public record AnmeldungAnfrage(
        @NotBlank String name,
        @NotBlank String passwort) {

    @Override
    public String toString() {
        return "AnmeldungAnfrage[name=" + name + ", passwort=<geschützt>]";
    }
}
