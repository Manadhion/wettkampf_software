package io.github.manadhion.wettkampf.app;

/**
 * Legt fest, aus welchem voneinander unabhängigen Datenbestand die Anwendung arbeitet.
 */
public enum Betriebsart {
    /** Lokale SQLite-Datenbank, insbesondere für den Sportleiterbetrieb. */
    SPORTLEITER,

    /** Gemeinsame Vereinsdatenbank, die ausschließlich über die Server-API erreichbar ist. */
    ONLINE
}
