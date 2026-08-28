# Blasrohr-Wettkampf-Manager

Eine Desktop-Anwendung zur Verwaltung von Blasrohr-Rundenwettkämpfen: Saisons,
Ligen, Mannschaften und Schützen anlegen, Ergebnisse erfassen, Tabellen live
berechnen, die Ergebnisse per Beamer anzeigen und die ganze Saison als PDF
ausgeben.

Entstanden ist das Programm für den Wettkampfbetrieb im Blasrohrsport. Es kann
wahlweise mit einer lokalen SQLite-Datei des Sportleiters oder mit einer
gemeinsamen Online-Datenbank für die Vereine arbeiten. Beide Datenbestände sind
fachlich und technisch voneinander getrennt.

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
- **Zwei Betriebsarten** – der Sportleiter arbeitet ohne Anmeldung lokal; die
  Vereine melden sich an der gemeinsamen Online-Datenbank an.
- **Offline-Notbetrieb** – fällt das Internet erst während des Onlinebetriebs
  aus, kann vorübergehend weitergearbeitet werden. Eine rote Warnung bleibt bis
  zur automatischen Synchronisation sichtbar.

## Installation

### Fertige Version (empfohlen für Vereine)

Es gibt eine fertige Windows-Version mit **eingebautem Java – es muss kein Java
installiert werden**. Sie liegt unter
[Releases](https://github.com/manadhion/wettkampf_software/releases) als
Installer bereit:

1. `Blasrohr-Wettkampf-Manager-<version>.exe` herunterladen und ausführen.
2. Den Schritten folgen (Zielordner wählen, fertig).

Da der kostenlose Installer derzeit nicht digital signiert ist, kann Windows
beim ersten Start eine Schutzmeldung anzeigen. Die bebilderungsunabhängigen
Schritte zum sicheren Download und zum Start stehen in der
[Installationsanleitung für Windows](docs/INSTALLATION-WINDOWS.md).

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

1. **Beim Start** die Betriebsart wählen:
   - *Sportleiter – lokal*: anschließend eine vorhandene `.db`-Datei öffnen
     oder eine neue Datenbank anlegen. Dafür wird kein Passwort benötigt.
   - *Online-Datenbank*: mit der gemeinsamen Online-Kennung und dem
     Online-Passwort anmelden. Die Serveradresse ist bereits im Programm
     hinterlegt.
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
laden. Das gilt ausschließlich für den lokalen Sportleiterbetrieb. Die aktive
Betriebsart steht im Fenstertitel.

Für den Start des Onlinebetriebs wird eine Internetverbindung benötigt. Fällt
die Verbindung später aus, speichert das Programm Änderungen nur im
Arbeitsspeicher. Nach der Wiederverbindung werden sie automatisch übertragen.
Das Programm darf nicht beendet werden, solange die rote Warnung auf noch nicht
synchronisierte Änderungen hinweist.

---

## Technischer Überblick

### Technologie-Stack

| Bereich        | Verwendet                                              |
|----------------|--------------------------------------------------------|
| Sprache        | Java 24                                                |
| Oberfläche     | JavaFX 21 (vollständig im Code aufgebaut, kein FXML)   |
| Datenhaltung   | lokal SQLite; online PostgreSQL über HTTPS-REST-API    |
| Server         | Spring Boot, Flyway, PostgreSQL und Docker Compose     |
| PDF-Erzeugung  | OpenPDF                                                 |
| Build          | Maven (mit Wrapper), Java Platform Module System       |
| Auslieferung   | eigenständige Windows-App via `jlink` + `jpackage`     |

### Architektur

Die Anwendung ist in klar getrennte Schichten aufgeteilt. Die Oberfläche kennt
nur den `Controller`; dieser verwendet einen austauschbaren
`WettkampfDatenService`. Die Views greifen nie direkt auf DAOs, Datenbanken oder
die HTTP-API zu.

```
view ──► app (Controller) ──► WettkampfDatenService
                                  ├─► Lokaler Service ──► dao ──► SQLite
                                  └─► Online-Service ──► HTTPS ──► Server/API
                                                                      └─► PostgreSQL
```

- **`data/`** – Entitäten (Saison, Wettkampftage, Liga, Mannschaft, Schuetze,
  Begegnung, Ergebnisse, Altersklasse, TabellenZeile).
- **`dao/`** – Data-Access-Objekte, gekapselter Zugriff auf die SQLite-Tabellen.
- **`app/`** – `Controller`, lokaler und Online-Datenservice, HTTP-Client,
  Konfiguration, `MannschaftstabelleRechner` und `SaisonPdf`.
- **`view/`** – JavaFX-Fenster (Hauptfenster, Formulare, Beamer-Vollbildansicht).
- **`server/`** – eigenständige Spring-Boot-API, Flyway-Migrationen,
  PostgreSQL- und Docker-Compose-Konfiguration.

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

Im lokalen Modus werden die Daten in einer SQLite-Datei (`.db`) gespeichert.
Fehlende Tabellen werden beim Öffnen automatisch angelegt. Im Online-Modus
kommuniziert das Programm ausschließlich per HTTPS mit der Server-API; es
enthält keine PostgreSQL-Zugangsdaten.

Welche Datenbank zuletzt verwendet wurde, merkt sich das Programm dauerhaft pro
Windows-Benutzer in der Registry unter
`HKEY_CURRENT_USER\Software\JavaSoft\Prefs\io\github\manadhion\wettkampf\app`.

Die Online-Kennung und das Online-Passwort werden nach erfolgreicher Anmeldung
in `%APPDATA%\Wettkampf\online.properties` gespeichert und beim nächsten Start
vorausgefüllt. Das Passwort ist mit Windows-DPAPI an den jeweiligen
Windows-Benutzer gebunden und liegt dort nicht im Klartext vor.

Ausführliche Dokumentation:

- [Umsetzungsstand vom 21.08.2026](docs/ONLINE-UMBAU-2026-08-21.md)
- [Online-Betriebskonzept](docs/ONLINE-BETRIEBSKONZEPT.md)
- [API-Vertrag](docs/API-VERTRAG-V1.md)
- [Serverentwicklung und Docker-Betrieb](server/README.md)

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

Der jeweils aktuelle öffentliche Installer ist dauerhaft unter
<https://github.com/Manadhion/wettkampf_software/releases/latest> erreichbar.
Vereine können diesen Link direkt zum Herunterladen verwenden.

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

Das Paketierungsskript führt vor `jpackage` einen `java --dry-run` mit der
reduzierten `jlink`-Laufzeit aus. Dadurch bricht der Build ab, wenn ein in
`module-info.java` benötigtes JDK-Modul (beispielsweise `java.net.http`) nicht
in `--add-modules` aufgenommen wurde; andernfalls würde die Windows-GUI-EXE
beim Start ohne sichtbare Fehlermeldung beendet.

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

## Sicherheit und Datenschutz

- [Hinweise zur derzeit fehlenden Codesignatur](CODE-SIGNING-POLICY.md)
- [Datenschutzinformation](PRIVACY.md)
