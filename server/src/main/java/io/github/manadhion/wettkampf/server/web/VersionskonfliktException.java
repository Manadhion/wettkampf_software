package io.github.manadhion.wettkampf.server.web;

public class VersionskonfliktException extends RuntimeException {

    public VersionskonfliktException(String nachricht) {
        super(nachricht);
    }
}
