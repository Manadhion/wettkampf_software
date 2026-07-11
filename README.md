# Blasrohr-Wettkampf-Manager

Eine Desktop-Anwendung zur Verwaltung von Blasrohr-Rundenwettkämpfen: Saisons,
Ligen, Mannschaften und Schützen anlegen, Ergebnisse erfassen, Tabellen live
berechnen, die Ergebnisse per Beamer anzeigen und die ganze Saison als PDF
ausgeben.

Entstanden ist das Programm für den Wettkampfbetrieb im Blasrohrsport – es läuft
lokal auf einem Windows-Rechner, braucht keine Internetverbindung und speichert
alle Daten in einer einzigen Datenbankdatei, die man weitergeben oder sichern
kann.

> **Für Vereine:** Alles Wichtige zum Installieren und Bedienen steht weiter
> unten unter [Installation](#installation) und [Erste Schritte](#erste-schritte).
>
> **Für Entwickler/Recruiter:** Der technische Teil beginnt bei
> [Technischer Überblick](#technischer-überblick).

---

## Funktionen

- **Saison- und Wettkampftagverwaltung** – eine Saison enthält beliebig viele
  Wettkampftage, jeder Tag mit Datum und ausrichtendem Verein.
- **Stammdaten** – Ligen, Mannschaften, Schützen (mit Altersklasse) anlegen,
  bearbeiten und löschen.
- **Ergebniserfassung** – Ringzahl je Schütze und Wettkampftag eintragen.
- **Automatische Wertung** – das Mannschaftsergebnis ist die Summe der besten
  drei Schützen; je Begegnung gibt es 2 : 0 für die höhere Ringzahl bzw. 1 : 1
  bei Gleichstand.
- **Live-Tabelle** – die Mannschaftstabelle wird jederzeit aus allen erfassten
  Begegnungen berechnet (sortiert nach Mannschaftspunkten, bei Gleichstand nach
  Ringen).
- **Beamer-Anzeige** – eine Vollbildansicht für den Wettkampfort, die
  automatisch durch alle Ligen rotiert, die Einzelergebnisse durchscrollt und
  eine Uhr zeigt.
- **Saison-PDF** – Einzel- und Mannschaftsergebnisse jeder Liga bis zu einem
  gewählten Wettkampftag als druckfertiges PDF im Querformat.
- **Mehrere Datenbanken** – jede Saison bzw. jeder Wettkampf kann in einer
  eigenen `.db`-Datei liegen; die zuletzt geöffnete wird beim nächsten Start
  automatisch wieder geladen.

## Installation

### Fertige Version (empfohlen für Vereine)

Es gibt eine fertige Windows-Version mit **eingebautem Java – es muss kein Java
installiert werden**. Sie liegt unter
[Releases](https://github.com/manadhion/wettkampf_software/releases) als
Installer bereit:

1. `Blasrohr-Wettkampf-Manager-<version>.exe` herunterladen und ausführen.
2. Den Schritten folgen (Zielordner wählen, fertig).

Danach steht das Programm im **Startmenü** und auf dem **Desktop**. Die
Installation braucht **keine Administratorrechte** (Installation pro Benutzer).
Zum Entfernen: *Windows-Einstellungen → Apps → Blasrohr-Wettkampf-Manager →
Deinstallieren*.

### Aus dem Quellcode starten (für Entwickler)

Voraussetzung: **Windows** und **[Java 24](https://www.oracle.com/java/technologies/downloads/)**
oder neuer. Repository klonen (oder als ZIP über den grünen **Code**-Button
herunterladen und entpacken):

```
git clone https://github.com/manadhion/wettkampf_software.git
```

Im Ordner starten:

```
.\mvnw.cmd javafx:run
```

Der mitgelieferte Wrapper `.\mvnw.cmd` lädt beim ersten Start automatisch alles
Nötige herunter und startet das Programm – eine separate Maven-Installation ist
nicht erforderlich.

> Auf macOS/Linux stattdessen `./mvnw javafx:run` verwenden.
>
> Der Aufruf `mvn javafx:run` funktioniert nur, wenn Maven manuell im PATH
> eingetragen ist. Der Wrapper funktioniert immer.

---

## Erste Schritte

1. **Beim ersten Start** fragt das Programm, ob eine neue Datenbank angelegt oder
   eine vorhandene `.db`-Datei geöffnet werden soll. Für einen neuen Wettkampf:
   *Neue Datenbank anlegen* und einen Speicherort wählen.
2. **Saison anlegen** (oben links, `+` neben „Saison").
3. **Wettkampftage** der Saison hinzufügen – jeweils mit Datum und ausrichtendem
   Verein.
4. **Ligen, Mannschaften und Schützen** anlegen und die Mannschaften ihren Ligen,
   die Schützen ihren Mannschaften zuordnen.
5. **Begegnungen** je Wettkampftag anlegen (Heim gegen Gegner).
6. **Ergebnisse eintragen:** Wettkampftag, Mannschaft und Schütze wählen, die
   Ringzahl eingeben und speichern. Die Begegnungsergebnisse aktualisieren sich
   sofort.
7. **Beamer-Anzeige starten** (Button oben links), sobald ein Wettkampftag
   gewählt ist – ideal zur Live-Anzeige am Wettkampfort.
8. **Saison-PDF …** erzeugt die Ergebnisübersicht bis zum gewählten Wettkampftag.

Eine andere Datenbank lässt sich jederzeit über **Datei → Datenbank öffnen…**
laden. Der Name der aktiven Datenbank steht im Fenstertitel.

---

## Technischer Überblick

### Technologie-Stack

| Bereich        | Verwendet                                              |
|----------------|--------------------------------------------------------|
| Sprache        | Java 24                                                |
| Oberfläche     | JavaFX 21 (vollständig im Code aufgebaut, kein FXML)   |
| Datenhaltung   | SQLite (via `xerial sqlite-jdbc`)                      |
| PDF-Erzeugung  | OpenPDF                                                 |
| Build          | Maven (mit Wrapper), Java Platform Module System       |
| Auslieferung   | eigenständige Windows-App via `jlink` + `jpackage`     |

### Architektur

Die Anwendung ist in klar getrennte Schichten aufgeteilt. Die Oberfläche kennt
nur den `Controller`, der `Controller` vermittelt zwischen Oberfläche und
Datenbankzugriff – die Views greifen nie direkt auf die Datenbank zu.

```
view  ──►  app (Controller)  ──►  dao  ──►  SQLite
             │
             └─►  Fachlogik: MannschaftstabelleRechner, SaisonPdf
```

- **`data/`** – Entitäten (Saison, Wettkampftage, Liga, Mannschaft, Schuetze,
  Begegnung, Ergebnisse, Altersklasse, TabellenZeile).
- **`dao/`** – Data-Access-Objekte, gekapselter Zugriff auf die SQLite-Tabellen.
- **`app/`** – `Controller` (Vermittler zwischen View und DAO), `DBController`
  (aktive Datenbank), `MannschaftstabelleRechner` (Tabellenberechnung),
  `SaisonPdf` (PDF-Export).
- **`view/`** – JavaFX-Fenster (Hauptfenster, Formulare, Beamer-Vollbildansicht).

Das Aussehen der Oberfläche kommt vollständig aus den `.css`-Dateien.

### Projektstruktur

```
src/main/java/io/github/manadhion/wettkampf/
    data/    → Entitäten
    dao/     → Data-Access-Objekte (SQLite-Zugriff)
    app/     → Controller, DBController, MannschaftstabelleRechner, SaisonPdf
    view/    → JavaFX-Fenster (Main, Formulare, BeamerView, OwnAlert)

src/main/resources/io/github/manadhion/wettkampf/view/
    style.css    → Aussehen der Hauptanwendung
    beamer.css   → Aussehen der Beamer-Anzeige

src/main/java/module-info.java → Java-Moduldeklaration
pom.xml                        → Abhängigkeiten und Build-Konfiguration
```

### Datenbank

Die Daten werden in einer SQLite-Datei (`.db`) gespeichert. Fehlende Tabellen
werden beim Öffnen automatisch angelegt, sodass sowohl eine neue als auch eine
bestehende Datei sofort nutzbar ist.

Welche Datenbank zuletzt verwendet wurde, merkt sich das Programm dauerhaft pro
Windows-Benutzer in der Registry unter
`HKEY_CURRENT_USER\Software\JavaSoft\Prefs\io\github\manadhion\wettkampf\app`.

### Dokumentation erzeugen (Javadoc)

Aus den Javadoc-Kommentaren im Code lässt sich eine durchsuchbare
HTML-Dokumentation erzeugen:

```
.\mvnw.cmd javadoc:javadoc
```

Ergebnis: `target/reports/apidocs/index.html` im Browser öffnen.

> `clean` nicht im selben Aufruf mitgeben (`clean javadoc:javadoc`) – das löscht
> die frisch erzeugte Ausgabe gleich wieder.

### Windows-Installer / .exe bauen

Aus dem Projekt lässt sich eine eigenständige Windows-Anwendung mit
eingebettetem Java erzeugen – der Zielrechner braucht dann kein installiertes
Java. Ein Skript erledigt die ganze Kette (`mvn package` → Abhängigkeiten
einsammeln → schlanke Laufzeit mit `jlink` → Verpacken mit `jpackage`):

```
powershell -ExecutionPolicy Bypass -File packaging\build-exe.ps1
```

Ergebnis: `dist\Blasrohr-Wettkampf-Manager-<version>.exe` (Installer mit
Startmenü- und Desktop-Verknüpfung, Installation pro Benutzer ohne
Administratorrechte).

Statt eines Installers einen **portablen Ordner** bauen (kein WiX nötig, einfach
entpacken und die `.exe` starten):

```
powershell -ExecutionPolicy Bypass -File packaging\build-exe.ps1 -Type app-image
```

Voraussetzungen:

- **JDK 24** (`JAVA_HOME` gesetzt oder `java` im PATH) – liefert `jpackage`/`jlink`.
- Nur für den Installer: **WiX-Toolset 3.14** (`candle.exe`/`light.exe`). Die
  [portablen Binaries](https://github.com/wixtoolset/wix3/releases) nach
  `%LOCALAPPDATA%\WiX314` entpacken – das Skript findet sie dort automatisch.
- Das Anwendungs-Icon liegt unter `packaging\icon.ico`.

### Neue Bibliothek einbinden

1. Dependency in `pom.xml` unter `<dependencies>` eintragen.
2. `requires <modulname>;` in `module-info.java` ergänzen.

---

## Lizenz

Veröffentlicht unter der [MIT-Lizenz](LICENSE) – frei nutzbar, veränderbar und
weitergebbar, auch von Vereinen.
