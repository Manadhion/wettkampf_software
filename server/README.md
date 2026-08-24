# Wettkampf-Server lokal entwickeln

Der Server ist bewusst vom JavaFX-Projekt getrennt. Er greift auf PostgreSQL
zu; der Desktopclient erhält niemals Datenbankzugang.

## Voraussetzungen

- Java 24
- Docker mit Compose für die lokale PostgreSQL-Instanz

## PostgreSQL starten

Im Verzeichnis `server`:

```powershell
docker compose up -d postgres
```

Die Compose-Datei bindet PostgreSQL nur an `127.0.0.1`. Benutzername und
Passwort darin sind ausschließlich lokale Entwicklungswerte.

## Server bauen und testen

Im Repository-Hauptverzeichnis:

```powershell
.\mvnw.cmd -f server\pom.xml verify
```

## Server lokal starten

In derselben PowerShell-Sitzung:

```powershell
$env:WETTKAMPF_DB_URL = "jdbc:postgresql://localhost:5432/wettkampf"
$env:WETTKAMPF_DB_BENUTZER = "wettkampf"
$env:WETTKAMPF_DB_PASSWORT = "wettkampf-lokal"
$env:WETTKAMPF_KONTO_NAME = "vereine"
$env:WETTKAMPF_KONTO_PASSWORT = "nur-lokal-aendern"
.\mvnw.cmd -f server\pom.xml spring-boot:run
```

Der Status ist anschließend unter
`http://localhost:8002/api/v1/status` erreichbar. HTTP ist nur für die lokale
Entwicklung vorgesehen; im späteren Serverbetrieb terminiert Nginx die
HTTPS-Verbindung.

Flyway spielt beim Start automatisch die Migrationen aus
`src/main/resources/db/migration` ein.

Neben Saisons verwaltet die API inzwischen auch Ligen, Altersklassen,
Mannschaften, Schützen, Wettkampftage, Begegnungen, Ergebnisse und historische
Saisonmeldungen. Die Desktopanwendung greift in der Online-Betriebsart für alle
diese Bereiche ausschließlich über HTTPS und die API auf PostgreSQL zu.

Für den flüchtigen Offline-Notbetrieb stellt die API außerdem den vollständigen
Fachdaten-Snapshot unter `GET /api/v1/snapshot` und `PUT /api/v1/snapshot`
bereit. Das Speichern ersetzt alle Fachdaten in einer Transaktion; Konto und
Sitzungen bleiben unverändert. Dieser Mechanismus setzt voraus, dass zu einem
Zeitpunkt nur ein Verein schreibend arbeitet.

Snapshots tragen eine globale Revision. Ein Client darf nur den Stand
zurückschreiben, den er zuvor geladen hat. Ein veralteter Client erhält
`409 VERSION_KONFLIKT`, bevor Tabelleninhalte verändert werden.

Beim ersten Start wird das ausschließlich vom Betreiber verwendete Online-Konto
aus `WETTKAMPF_KONTO_NAME` und
`WETTKAMPF_KONTO_PASSWORT` angelegt. Das Passwort wird nur als BCrypt-Hash
gespeichert. Bei späteren Starts werden diese Variablen nicht zum automatischen
Überschreiben eines vorhandenen Kontos verwendet.

Passwörter werden ausschließlich durch den Betreiber administriert. Für
Sportleiter gibt es weder in der Fach-API noch im Desktopclient eine Funktion
zum Ändern des Passworts.

## Docker-Stack für die Serverbereitstellung

Der spätere Serverbetrieb verwendet `compose.production.yaml`. Vor dem ersten
Start wird `.env.example` nach `.env` kopiert und ausschließlich vom Betreiber
mit echten Werten befüllt. `.env` ist von Git ausgeschlossen.

```powershell
Copy-Item server\.env.example server\.env
docker compose --env-file server\.env -f server\compose.production.yaml up -d --build
```

Die API ist danach nur auf `127.0.0.1:8002` gebunden. PostgreSQL besitzt in
diesem Stack bewusst keine Portfreigabe zum Host. Das Online-Passwort legt das
Konto ausschließlich beim ersten Start einer leeren Datenbank an; spätere
Neustarts überschreiben es nicht.

Für den vorgeschalteten Nginx liegt
`nginx-wettkampf-api.conf.template` bereit. Vor der Installation muss
`__WETTKAMPF_DOMAIN__` durch die tatsächliche Subdomain ersetzt werden. Die
Konfiguration wird erst nach einem erfolgreichen `nginx -t` aktiviert.

## SQLite-Bestand einmalig nach PostgreSQL übernehmen

Das Importskript liest die SQLite-Datei ausschließlich lesend und schreibt SQL
nach stdout:

```powershell
$env:PYTHONUTF8 = "1"
python server\scripts\sqlite_nach_postgres_sql.py `
  RWK_RhoenSaale.db > import.sql
```

Die erzeugte Datei enthält eine Transaktion und Prüfungen der erwarteten
Zeilenzahlen. In produktiven PowerShell-Pipelines muss die gesamte
Ausgabekette auf UTF-8 eingestellt sein; andernfalls können Umlaute bereits in
der Pipeline beschädigt werden. Zugangsdaten und erzeugte Importdateien werden
nicht eingecheckt.

Der vollständige Umsetzungs- und Prüfstand ist in
[`docs/ONLINE-UMBAU-2026-08-21.md`](../docs/ONLINE-UMBAU-2026-08-21.md)
dokumentiert.

## Produktionsbackups

Unter `operations/` liegen die produktiven Betriebsskripte:

- `backup-postgres.sh` erzeugt ein PostgreSQL-Custom-Format-Backup, prüft dessen
  Inhaltsverzeichnis und bewahrt Sicherungen 30 Tage auf.
- `backup-restore-pruefen.sh` stellt ein Backup in einer klar getrennten,
  temporären Testdatenbank wieder her und entfernt diese anschließend.
- `wettkampf-backup.service` und `wettkampf-backup.timer` starten das Backup
  sonntags um 02:30 UTC mit einer zufälligen Verzögerung bis 20 Minuten.

Installation auf dem Server:

```bash
install -m 644 operations/wettkampf-backup.service /etc/systemd/system/
install -m 644 operations/wettkampf-backup.timer /etc/systemd/system/
chmod 750 operations/backup-postgres.sh operations/backup-restore-pruefen.sh
systemctl daemon-reload
systemctl enable --now wettkampf-backup.timer
```

Backups liegen mit restriktiven Dateirechten unter
`/opt/wettkampf-server/backups`. Diese Sicherungen schützen gegen fachliche
Fehler und ein beschädigtes Datenbankvolume. Eine zusätzliche Kopie außerhalb
des Servers bleibt erforderlich, um auch einen vollständigen Serverausfall
abzudecken.

## Schutz der Anmeldung

Die API begrenzt Anmeldungen je Clientadresse auf zehn Versuche pro Minute und
antwortet danach mit HTTP `429`. Alte abgelaufene oder widerrufene Sitzungen
werden bei einer späteren Anmeldung bereinigt, sobald sie älter als sieben Tage
sind. Die Nginx-Vorlage enthält zusätzlich eine passende Begrenzung für
Neuinstallationen.
