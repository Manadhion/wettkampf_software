package io.github.manadhion.wettkampf.app;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.prefs.Preferences;

/**
 * Verwaltet die technischen Einstellungen der beiden strikt getrennten Betriebsarten.
 * Fachliche Daten werden hier weder gespeichert noch zwischen den Betriebsarten übertragen.
 */
public final class Anwendungskonfiguration {

    private static final URI ONLINE_SERVER_ADRESSE =
            URI.create("https://wettkampf-api.meshalchemy.com");

    private static final String BETRIEBSART_SCHLUESSEL = "betriebsart";
    private static final String DATENBANKPFAD_SCHLUESSEL = "datenbankPfad";

    private static final Preferences EINSTELLUNGEN =
            Preferences.userNodeForPackage(Anwendungskonfiguration.class);

    private Anwendungskonfiguration() {
    }

    /** Gibt die fest hinterlegte Adresse der Online-Datenbank zurueck. */
    public static URI getOnlineServerAdresse() {
        return ONLINE_SERVER_ADRESSE;
    }

    /** Gibt die gewählte Betriebsart zurück, sofern bereits eine festgelegt wurde. */
    public static Optional<Betriebsart> getBetriebsart() {
        String wert = EINSTELLUNGEN.get(BETRIEBSART_SCHLUESSEL, null);
        if (wert == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(Betriebsart.valueOf(wert));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /** Speichert die aktive Betriebsart. */
    public static void setBetriebsart(Betriebsart betriebsart) {
        EINSTELLUNGEN.put(BETRIEBSART_SCHLUESSEL, Objects.requireNonNull(betriebsart).name());
    }

    /** Gibt den Pfad der lokalen Sportleiter-Datenbank zurück. */
    public static Optional<String> getSportleiterDatenbankPfad() {
        return Optional.ofNullable(EINSTELLUNGEN.get(DATENBANKPFAD_SCHLUESSEL, null));
    }

    /** Speichert den Pfad der lokalen Sportleiter-Datenbank. */
    public static void setSportleiterDatenbankPfad(String pfad) {
        EINSTELLUNGEN.put(DATENBANKPFAD_SCHLUESSEL, Objects.requireNonNull(pfad));
    }
}
