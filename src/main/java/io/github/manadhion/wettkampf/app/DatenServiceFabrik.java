package io.github.manadhion.wettkampf.app;

import java.util.Objects;

/** Erzeugt den zur gewählten Betriebsart passenden fachlichen Datenservice. */
public final class DatenServiceFabrik {

    private DatenServiceFabrik() {
    }

    /**
     * Erzeugt den aktiven Datenservice. Der Onlinebetrieb wird erst freigegeben,
     * wenn ein API-basierter Service vorhanden ist; ein SQLite-Fallback gibt es nicht.
     *
     * @param betriebsart gewählte Betriebsart
     * @return passender Datenservice
     * @throws IllegalStateException solange der Online-Datenservice noch nicht implementiert ist
     */
    public static WettkampfDatenService erstellen(Betriebsart betriebsart) {
        return switch (Objects.requireNonNull(betriebsart)) {
            case SPORTLEITER -> new LokalerWettkampfDatenService();
            case ONLINE -> throw new IllegalStateException(
                    "Der Online-Datenservice ist noch nicht eingerichtet.");
        };
    }

    /**
     * Erzeugt den Service für die gespeicherte Betriebsart. Für bestehende
     * Installationen ohne Moduseinstellung bleibt der Sportleiterbetrieb erhalten.
     */
    public static WettkampfDatenService fuerGespeicherteBetriebsart() {
        Betriebsart betriebsart = Anwendungskonfiguration.getBetriebsart()
                .orElse(Betriebsart.SPORTLEITER);
        return erstellen(betriebsart);
    }
}
