# Betriebssicherheit – Nacharbeiten vom 24.08.2026

## Erledigter Umfang

Die nach dem ersten Online-Umbau priorisierten technischen Nacharbeiten wurden
bis auf den bewusst vom Betreiber durchzuführenden manuellen JavaFX-Test
umgesetzt.

### Automatische Backups

Auf dem Produktionsserver ist ein systemd-Timer für wöchentliche
PostgreSQL-Backups aktiv. Er läuft sonntags um 02:30 UTC mit bis zu 20 Minuten
zufälliger Verzögerung. Die Sicherungen werden im PostgreSQL-Custom-Format mit restriktiven
Dateirechten unter `/opt/wettkampf-server/backups` gespeichert und nach 30 Tagen
gelöscht. Vor der endgültigen Ablage prüft `pg_restore --list`, dass das Archiv
lesbar ist.

Am 24.08.2026 wurden sowohl ein manuelles Ausgangsbackup als auch ein Backup
nach Flyway-Migration V4 in jeweils eine getrennte temporäre Datenbank
zurückgespielt. Der jüngste Restore-Test ergab:

| Bereich | Zeilen |
|---|---:|
| Saisons | 2 |
| Mannschaften | 20 |
| Schützen | 121 |
| Ergebnisse | 654 |

Die temporäre Testdatenbank wurde anschließend automatisch entfernt. Der
nächste Timerlauf wurde vom Server korrekt eingeplant.

### Schutz vor veralteten Offline-Clients

Flyway-Migration V4 führt eine globale Fachdatenrevision ein. `GET
/api/v1/snapshot` liefert sie zusammen mit dem Datenbestand. `PUT
/api/v1/snapshot` erhöht die Revision nur dann, wenn der Client noch von der
aktuellen Ausgangsversion ausgeht. Die Revisionsprüfung und das Ersetzen der
Fachdaten laufen in derselben Transaktion.

Ein veralteter Client erhält `409 VERSION_KONFLIKT`, bevor das transaktionale
Löschen der Fachtabellen beginnt. Seine lokalen Änderungen bleiben im
Arbeitsspeicher und die rote Warnanzeige erklärt, dass der Server inzwischen
einen neueren Stand besitzt. Automatische Synchronisationsversuche pausieren,
bis der Konflikt bewusst gelöst wurde.

Im Hauptfenster erscheint dann die Aktion `Versionskonflikt lösen …`. Sie bietet
zwei ausdrücklich zu bestätigende Möglichkeiten:

- den neuesten Serverstand laden und die lokalen Änderungen verwerfen oder
- den lokalen Gesamtstand auf Basis der inzwischen aktuellen Serverrevision
  hochladen.

Unterschiedliche fachliche Änderungen werden nicht automatisch vermischt. Vor
beiden Entscheidungen erklärt ein zweiter Dialog genau, welcher Stand ersetzt
wird. Eine zwischen Auswahl und Serverabruf neu vorgenommene lokale Änderung
bricht die Übernahme des Serverstands ab, statt unbemerkt verloren zu gehen.

Der Java-Live-Test hat gegen die Produktions-API bestätigt:

1. Snapshot mit Revision laden,
2. unverändert speichern und neue Revision erhalten,
3. denselben alten Snapshot erneut senden,
4. erwarteten Versionskonflikt erhalten,
5. keine fachlichen Daten verlieren.

### Login- und Sitzungsbegrenzung

Die Serveranwendung erlaubt je weitergeleiteter Clientadresse höchstens zehn
Anmeldeversuche pro Minute. Weitere Versuche erhalten HTTP `429` und einen
`Retry-After`-Header. Der Live-Test ergab zehnmal HTTP `400` für absichtlich
leere Testanfragen und anschließend HTTP `429`.

Abgelaufene sowie widerrufene Sitzungen, die älter als sieben Tage sind, werden
bei Anmeldeversuchen aus PostgreSQL entfernt. Damit wächst die Sitzungstabelle
nicht dauerhaft weiter.

### Online-CRUD und feste Serveradresse

Ein zusätzlicher automatisierter Test durchläuft Anlegen, Lesen, Ändern und –
soweit fachlich vorgesehen – Löschen der Online-Fachdaten. Abgedeckt sind
Saisons, Ligen, Altersklassen, Mannschaften, Schützen, Wettkampftage,
Begegnungen, Saisonmeldungen und Ergebnisse einschließlich der Beste-3-Summe
und der Löschsperren bei vorhandenen Abhängigkeiten.

Die Produktionsadresse bleibt fest im Programm hinterlegt. Der ungenutzte
Preferences-Schlüssel sowie die nicht verwendeten Methoden zum freien Speichern
einer abweichenden Serveradresse wurden entfernt.

### Portable Windows-Anwendung

Das portable App-Verzeichnis wurde erfolgreich mit `jlink` und `jpackage`
gebaut. Es enthält:

- den Windows-Launcher,
- eine eingebettete Java-Laufzeit,
- den Desktopclient,
- `jna` und `jna-platform` für Windows-DPAPI.

Der Build liegt als generierte, von Git ausgeschlossene Ausgabe unter
`dist/Blasrohr-Wettkampf-Manager`.

## Erfolgreiche Prüfungen

- Desktop: 27 Tests, davon 26 erfolgreich und der standardmäßig deaktivierte
  Live-Test übersprungen.
- Separater Java-Live-Test: 1 von 1 erfolgreich.
- Server: 11 von 11 Tests erfolgreich.
- API-Container: `healthy`.
- Backup-Restore: erfolgreich.
- Produktive UTF-8-Prüfung: weiterhin 165 Zeichen aus `äöüÄÖÜß`.

## Weiterhin offen

1. Manueller JavaFX-Test des Offline-Notbetriebs durch den Betreiber.
2. Eine zusätzliche verschlüsselte Backupkopie außerhalb desselben
   Hetzner-Servers oder ein aktiviertes Hetzner-Serverbackup.
3. Test des portablen Verzeichnisses auf einem zweiten Windows-Rechner ohne
   Entwicklungsumgebung.
