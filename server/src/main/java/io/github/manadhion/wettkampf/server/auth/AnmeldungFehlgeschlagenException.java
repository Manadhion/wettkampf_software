package io.github.manadhion.wettkampf.server.auth;

public class AnmeldungFehlgeschlagenException extends RuntimeException {

    public AnmeldungFehlgeschlagenException() {
        super("Name oder Passwort ist falsch.");
    }
}
