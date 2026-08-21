package io.github.manadhion.wettkampf.server.web;

public class NichtGefundenException extends RuntimeException {

    public NichtGefundenException(String nachricht) {
        super(nachricht);
    }
}
