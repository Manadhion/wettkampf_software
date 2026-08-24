package io.github.manadhion.wettkampf.app;

/** Meldet eine fehlgeschlagene Verbindung oder Antwort der Online-API. */
public class OnlineApiException extends RuntimeException {

    private final boolean verbindungsfehler;
    private final int httpStatus;

    public OnlineApiException(String nachricht) {
        this(nachricht, null, false);
    }

    public OnlineApiException(String nachricht, Throwable ursache) {
        this(nachricht, ursache, false);
    }

    public OnlineApiException(String nachricht, Throwable ursache, boolean verbindungsfehler) {
        this(nachricht, ursache, verbindungsfehler, -1);
    }

    public OnlineApiException(String nachricht, Throwable ursache, boolean verbindungsfehler,
            int httpStatus) {
        super(nachricht, ursache);
        this.verbindungsfehler = verbindungsfehler;
        this.httpStatus = httpStatus;
    }

    public boolean istVerbindungsfehler() {
        return verbindungsfehler;
    }

    public boolean istAnmeldungAbgelaufen() {
        return httpStatus == 401;
    }

    public boolean istVersionskonflikt() {
        return httpStatus == 409;
    }
}
