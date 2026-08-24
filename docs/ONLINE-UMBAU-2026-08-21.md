# Online-Umbau – Umsetzungsstand 21.08.2026

> **Nachtrag 24.08.2026:** Wöchentliche automatische Serverbackups,
> Snapshot-Revisionsschutz,
> Login-Begrenzung, Sitzungsbereinigung und der portable Build sind inzwischen
> umgesetzt. Ergebnisse und verbleibende Punkte stehen in
> [BETRIEBSSICHERHEIT-2026-08-24.md](BETRIEBSSICHERHEIT-2026-08-24.md).

## 1. Ziel und fachliche Entscheidungen

Der Blasrohr-Wettkampf-Manager besitzt nun zwei strikt getrennte
Betriebsarten:

1. **Sportleiter – lokal** verwendet wie bisher eine SQLite-Datei auf dem
   Rechner. Dieser Modus benötigt weder Internet noch ein Passwort.
2. **Online-Datenbank** verwendet eine gemeinsame PostgreSQL-Datenbank über
   eine HTTPS-API. Dafür gibt es ein vom Betreiber verwaltetes gemeinsames
   Vereinskonto.

Das Passwort des Online-Kontos wird ausschließlich vom Betreiber festgelegt
und verteilt. Die Anwendung enthält keine Funktion, mit der Sportleiter oder
Vereine dieses Passwort ändern können. Das Datenbankpasswort ist davon
vollständig getrennt und bleibt ausschließlich auf dem Server.

Die lokale Sportleiter-Datenbank bleibt der offizielle Datenbestand. Es gibt
keine laufende Synchronisation zwischen ihr und der Online-Datenbank. Für die
Inbetriebnahme wurde der damalige lokale Stand einmalig in PostgreSQL
übernommen.

## 2. Desktoparchitektur

Der bisher direkt auf DAOs zugreifende `Controller` verwendet jetzt den
abstrakten `WettkampfDatenService`:

```text
JavaFX-View
    |
Controller
    |
WettkampfDatenService
    |-- LokalerWettkampfDatenService -> DAOs -> SQLite-Datei
    `-- OnlineWettkampfDatenService  -> OnlineApiClient -> HTTPS-API
```

Dadurch bleibt die JavaFX-Oberfläche unabhängig von der Datenquelle. Der
lokale Service kapselt die vorhandenen DAOs. Der Online-Service bildet dieselben
fachlichen Operationen auf die API beziehungsweise auf seinen laufenden
Arbeitsspeicher-Snapshot ab.

Beim Programmstart wird eine kleine Scene gesetzt, bevor Dialoge an das
Hauptfenster gebunden werden. Das beseitigt den JavaFX-Startfehler beim Öffnen
des Betriebsart-Dialogs über die Play-Taste in VS Code.

## 3. Auswahl der Betriebsart

Beim Start fragt das Programm nach:

- `Sportleiter – lokal`
- `Online-Datenbank`
- `Abbrechen`

Im lokalen Modus folgt bei Bedarf die bekannte Auswahl einer SQLite-Datei. Der
Fenstertitel enthält `Sportleiter` und den Dateinamen. Im Online-Modus ist der
Menüpunkt zum Wechseln der lokalen Datenbank deaktiviert; der Fenstertitel
kennzeichnet die Online-Datenbank.

Die feste Produktionsadresse der API ist in `Anwendungskonfiguration`
hinterlegt. Vereine müssen deshalb keine Serveradresse eingeben.

## 4. Anmeldung und lokale Zugangskonfiguration

Nur der Online-Modus zeigt eine Anmeldung mit Online-Kennung und Passwort. Nach
einer erfolgreichen Anmeldung speichert die Anwendung beide Werte für den
nächsten Start unter:

```text
%APPDATA%\Wettkampf\online.properties
```

Die Kennung ist normaler Konfigurationsinhalt. Das Passwort wird vor dem
Speichern mit der Windows Data Protection API (DPAPI) verschlüsselt und ist an
den angemeldeten Windows-Benutzer gebunden. Dafür wird `jna-platform` verwendet.
Wird später erfolgreich ein anderes Passwort eingegeben, ersetzt es den
gespeicherten Wert. Passwort-Arrays werden nach der Verwendung soweit möglich
überschrieben.

Der Server speichert das Kontopasswort ausschließlich als BCrypt-Hash. Tokens
liegen serverseitig nur als SHA-256-Hash vor. Eine abgelaufene Sitzung wird vom
Desktop mit den im Arbeitsspeicher gehaltenen Zugangsdaten automatisch erneuert.

## 5. Serveranwendung

Unter `server/` befindet sich eine eigenständige Spring-Boot-Anwendung mit:

- REST-API unter `/api/v1`,
- Authentifizierung per Bearer-Token,
- PostgreSQL-Zugriff über JDBC,
- Flyway-Migrationen für Konto, Sitzungen und alle Fachdaten,
- einheitlicher Fehlerbehandlung,
- Dockerfile und getrennten Compose-Dateien für Entwicklung und Produktion.

PostgreSQL ist im Produktions-Stack nicht öffentlich erreichbar. Nur die API
ist auf dem Server an `127.0.0.1:8002` gebunden; Nginx veröffentlicht sie über
HTTPS. Die öffentliche Basisadresse lautet:

```text
https://wettkampf-api.meshalchemy.com
```

Die produktiven Geheimnisse stehen ausschließlich in `server/.env`, das von
Git ausgeschlossen ist. `server/.env.example` dokumentiert nur die benötigten
Variablennamen. Ebenso bleibt die lokale SSH-Zugangsdokumentation durch das
Muster `*.local.md` außerhalb des Repositorys.

## 6. Datenmodell und einmalige Erstbefüllung

Die Online-Datenbank enthält:

- Saisons,
- Ligen,
- Altersklassen,
- Mannschaften,
- Schützen,
- Wettkampftage,
- Begegnungen,
- historische Saisonmeldungen und
- Ergebnisse.

Das Skript `server/scripts/sqlite_nach_postgres_sql.py` liest eine SQLite-Datei
nur lesend und erzeugt daraus transaktionales PostgreSQL-SQL. Die Ausgabe ist
UTF-8 und enthält Prüfungen der erwarteten Zeilenzahlen. Beim Import und bei
späteren API-Übertragungen müssen PowerShell-Ausgabe und -Pipelines ebenfalls
UTF-8 verwenden.

Die produktive Erstbefüllung wurde geprüft. Der Datenbestand umfasst derzeit:

| Tabelle | Zeilen |
|---|---:|
| Saisons | 2 |
| Ligen | 3 |
| Altersklassen | 1 |
| Mannschaften | 20 |
| Schützen | 121 |
| Wettkampftage | 19 |
| Begegnungen | 123 |
| Saisonmeldungen | 121 |
| Ergebnisse | 654 |

Eine abschließende Prüfung fand 165 korrekt gespeicherte Zeichen aus
`äöüÄÖÜß`.

## 7. Flüchtiger Offline-Notbetrieb

Der Start des Online-Modus benötigt zwingend eine funktionierende
Internetverbindung: Nach der Anmeldung lädt der Client den vollständigen
Fachdaten-Snapshot über `GET /api/v1/snapshot`. Schlägt dies fehl, wird das
Online-Hauptfenster nicht geöffnet.

Fällt die Verbindung erst während der Benutzung aus, arbeitet der Verein mit
dem Snapshot im Arbeitsspeicher weiter:

1. Fachliche Änderungen werden sofort im Arbeitsspeicher übernommen.
2. Der Hintergrunddienst versucht regelmäßig, den vollständigen Stand über
   `PUT /api/v1/snapshot` zu speichern.
3. Der Server ersetzt die Fachdaten innerhalb einer einzigen Transaktion.
4. Erst eine erfolgreiche Serverantwort markiert die Änderungen als
   synchronisiert.
5. Nach der Wiederverbindung erfolgt der nächste Versuch spätestens nach etwa
   fünf Sekunden.

Das Hauptfenster zeigt währenddessen eine auffällige rote Warnung mit der Zahl
der ausstehenden Änderungen. Das Beamerfenster bleibt davon unberührt. Beim
Beenden mit ausstehenden Änderungen erscheint eine zusätzliche Bestätigung.
Da es absichtlich keine dauerhafte Offline-Datei gibt, gehen nicht
synchronisierte Änderungen beim Beenden verloren.

Der vollständige Snapshot-Upload ist für das vereinbarte Betriebsmodell
geeignet, weil während eines Wettkampfs immer nur der austragende Verein
schreibend arbeitet. Gleichzeitiges Bearbeiten durch mehrere Vereine ist für
diesen Notbetrieb nicht vorgesehen und würde eine andere Konfliktstrategie
erfordern.

Konto- und Sitzungstabellen sind ausdrücklich nicht Bestandteil eines
Snapshots und werden bei der fachlichen Synchronisation nicht verändert.

## 8. API- und Fehlerverhalten

Die Kommunikation verwendet JSON mit UTF-8. Verbindungsfehler werden von
fachlichen HTTP-Fehlern unterschieden. Insbesondere:

- `401` führt beim Hintergrundabgleich zu einer erneuten Anmeldung,
- Verbindungsfehler aktivieren den Notbetrieb,
- Server- oder Validierungsfehler bleiben als Synchronisationsfehler sichtbar,
- ein wiederholtes vollständiges `PUT` ist nach einer unklar unterbrochenen
  Verbindung sicher wiederholbar.

Der vollständige API-Vertrag steht in [API-VERTRAG-V1.md](API-VERTRAG-V1.md).

## 9. Tests und Prüfungen

Am 21.08.2026 wurden folgende Prüfungen erfolgreich ausgeführt:

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd -f server\pom.xml verify
```

- Desktopprojekt: 23 Tests, davon 22 erfolgreich und 1 absichtlich
  deaktivierter Live-Test.
- Serverprojekt: 9 von 9 Tests erfolgreich.
- Der optionale Java-Live-Test wurde zusätzlich mit ausdrücklich gesetzten
  Umgebungsvariablen gegen die Produktions-API ausgeführt. Laden, unverändertes
  Zurückschreiben und erneutes Laden des vollständigen Snapshots waren
  erfolgreich.
- Der API-Container wurde anschließend als `healthy` geprüft.
- Die UTF-8-Prüfung der produktiven Fachdaten ergab weiterhin 165 Sonderzeichen.

Der Live-Test ist standardmäßig deaktiviert, damit normale Builds weder echte
Zugangsdaten noch Netzwerkzugriff benötigen. JavaFX-Dialoge, Warnbanner,
Trennen und Wiederherstellen der Internetverbindung müssen zusätzlich manuell
geprüft werden.

## 10. Bewusst offene Punkte

- Regelmäßige, automatisierte PostgreSQL-Backups und ein dokumentierter
  Wiederherstellungstest.
- Vollständiger manueller Abnahmetest des JavaFX-Offlineablaufs auf dem
  Vereinsrechner.
- Installer beziehungsweise portable Anwendung mit den neuen Abhängigkeiten
  bauen und auf einem Rechner ohne Entwicklungsumgebung testen.
- Falls zukünftig doch mehrere Vereine gleichzeitig schreiben sollen, muss der
  Snapshot-Notbetrieb durch versionierte Einzeländerungen oder eine echte
  Konfliktauflösung ersetzt werden.
