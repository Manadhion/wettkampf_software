package io.github.manadhion.wettkampf.app;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Liefert die beim Maven-Build in die Anwendung geschriebene Versionsnummer. */
public final class Anwendungsversion {

    private static final String RESSOURCE =
            "/io/github/manadhion/wettkampf/app/anwendung.properties";
    private static final String VERSION = laden();

    private Anwendungsversion() {
    }

    public static String aktuell() {
        return VERSION;
    }

    private static String laden() {
        Properties werte = new Properties();
        try (InputStream eingabe = Anwendungsversion.class.getResourceAsStream(RESSOURCE)) {
            if (eingabe == null) return "0.0.0";
            werte.load(eingabe);
            String version = werte.getProperty("version", "0.0.0").trim();
            return version.matches("[0-9]+(?:\\.[0-9]+){0,3}") ? version : "0.0.0";
        } catch (IOException e) {
            return "0.0.0";
        }
    }
}
