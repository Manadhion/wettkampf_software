# Blasrohr-Wettkampf-Manager

## Voraussetzungen

- [Java 24](https://www.oracle.com/java/technologies/downloads/) muss installiert sein

## Projekt starten

```
.\mvnw.cmd javafx:run
```

> `mvn javafx:run` funktioniert nur wenn Maven manuell im PATH eingetragen ist.
> Der Wrapper `.\mvnw.cmd` funktioniert immer.

## Projektstruktur

```
src/main/java/io/github/manadhion/wettkampf/
    data/    → Entitäten (Saison, Wettkampftage, Mannschaft, Schuetze,
               Begegnung, Ergebnisse, Liga, Altersklasse, TabellenZeile)
    dao/     → Data Access-Objekte, Zugriff auf die SQLite-Tabellen
    app/     → Controller (verbindet view und dao), DBController,
               MannschaftstabelleRechner
    view/    → JavaFX-Fenster (Main = Hauptfenster, Formulare,
               BeamerView, OwnAlert)

src/main/resources/io/github/manadhion/wettkampf/view/
    style.css    → Aussehen der Hauptanwendung
    beamer.css   → Aussehen der Beamer-Anzeige

src/main/java/module-info.java → Java-Moduldeklaration
pom.xml                        → Abhängigkeiten und Build-Konfiguration
```

Die Oberfläche wird vollständig im Java-Code aufgebaut (kein FXML), das
Aussehen kommt aus den `.css`-Dateien.

## Dokumentation erzeugen (Javadoc)

Aus den Javadoc-Kommentaren im Code lässt sich eine durchsuchbare HTML-Dokumentation
erzeugen:

```
.\mvnw.cmd javadoc:javadoc
```

Ergebnis: `target/reports/apidocs/index.html` im Browser öffnen.

> `clean` nicht im selben Aufruf mitgeben (`clean javadoc:javadoc`) – das löscht die
> frisch erzeugte Ausgabe gleich wieder.

## Datenbank

Die Daten werden in einer SQLite-Datei (`.db`) gespeichert.

- **Erster Start:** Es erscheint die Auswahl, ob eine neue Datenbank angelegt oder eine
  vorhandene `.db`-Datei geöffnet werden soll.
- **Datenbank wechseln:** Über `Datei → Datenbank öffnen…` kann jederzeit eine andere
  Datenbank geladen werden. Das Programm arbeitet direkt auf der gewählten Datei.
- **Aktive Datenbank:** Der Dateiname der aktuell geöffneten Datenbank steht im Fenstertitel.

Welche Datenbank zuletzt verwendet wurde, merkt sich das Programm dauerhaft pro
Windows-Benutzer in der Registry unter
`HKEY_CURRENT_USER\Software\JavaSoft\Prefs\io\github\manadhion\wettkampf\app`.

## Neue Bibliothek einbinden

1. Dependency in `pom.xml` unter `<dependencies>` eintragen
2. `requires <modulname>;` in `module-info.java` ergänzen

## Entwicklungsnotizen

<!-- Hier eigene Notizen eintragen -->
