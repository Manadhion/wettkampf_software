# Hetzner-Server: Bestandsaufnahme und Bereitstellungskonzept

## 1. Zweck und Geltungsbereich

Dieses Dokument hält fest:

- welcher vorhandene Hetzner-Server für den späteren Vereinsbetrieb vorgesehen
  ist,
- wie ein berechtigter Entwickler den Server erreicht,
- welche Dienste und Ressourcen am 20. August 2026 festgestellt wurden,
- wie die Wettkampf-API und PostgreSQL später isoliert ergänzt werden sollen,
- welche Sicherheits- und Betriebsmaßnahmen vor der Inbetriebnahme notwendig
  sind und
- welche Arbeiten ausdrücklich zunächst nur lokal erfolgen.

Es enthält keine konkreten Serveradressen, administrativen Benutzernamen,
Passwörter, privaten SSH-Schlüssel, Tokens, Datenbankzugangsdaten oder Inhalte
von `.env`-Dateien. Solche Angaben dürfen auch künftig nicht in dieses
Repository aufgenommen werden. Die berechtigten lokalen Zugangsdaten stehen auf
dem Entwicklungsrechner in `docs/HETZNER-ZUGANG.local.md`. Dateien mit der
Endung `.local.md` werden von Git ignoriert.

Die fachliche Gesamtplanung steht ergänzend in
[ONLINE-BETRIEBSKONZEPT.md](ONLINE-BETRIEBSKONZEPT.md).

## 2. Verbindliche Entscheidung zum Vorgehen

Die Wettkampf-Serveranwendung wird nicht unmittelbar auf dem Produktivserver
entwickelt. Das Vorgehen lautet:

1. Serveranwendung lokal entwickeln.
2. PostgreSQL lokal als Entwicklungsdatenbank betreiben.
3. Server- und Datenbanktests lokal automatisieren.
4. JavaFX-Vereinsmodus lokal gegen den Entwicklungsserver testen.
5. Docker-Images und Compose-Konfiguration lokal prüfen.
6. Backup und Wiederherstellung lokal erproben.
7. Erst danach den vorhandenen Hetzner-Server vorbereiten.
8. Zunächst Testdaten auf dem Server verwenden.
9. Produktivbetrieb erst nach erfolgreicher technischer Abnahme freigeben.

Bestehender Lizenzserver und Orchestrator dürfen durch die Entwicklung oder
Bereitstellung nicht beeinträchtigt werden.

## 3. Serveridentität und SSH-Zugang

### 3.1 Zielsystem

| Merkmal | Wert |
|---|---|
| Hetzner-Typ | CX23 |
| Hostname | siehe lokale Zugangsdatei |
| IPv4-Adresse | siehe lokale Zugangsdatei |
| Standort | Nürnberg, Deutschland |
| SSH-Benutzer | siehe lokale Zugangsdatei |
| SSH-Port | `22` |
| Authentifizierung | vorhandener SSH-Schlüssel auf dem Entwicklungsrechner |

Die IPv6-Adresse wird hier bewusst nicht erneut dokumentiert, da sie für den
geplanten Arbeitsablauf derzeit nicht benötigt wird.

### 3.2 Verbindungsaufbau

Auf dem autorisierten Entwicklungsrechner funktioniert der Zugang über:

```powershell
ssh <SSH-BENUTZER>@<SERVER-ADRESSE>
```

Für nicht-interaktive Prüfungen wird verwendet:

```powershell
ssh -o BatchMode=yes -o ConnectTimeout=15 <SSH-BENUTZER>@<SERVER-ADRESSE> "BEFEHL"
```

`BatchMode=yes` verhindert eine unerwartete Passwortabfrage. Ein fehlender oder
ungültiger SSH-Key führt dadurch zu einem kontrollierten Abbruch.

### 3.3 Regeln für automatisierte Arbeiten

- Vor jeder Serververbindung wird der Zweck der Verbindung mitgeteilt.
- Eine Bestandsaufnahme beginnt ausschließlich mit lesenden Befehlen.
- Änderungen erfolgen nur, wenn sie vom Benutzer ausdrücklich beauftragt sind.
- Neustarts, Updates, Firewalländerungen und Containeränderungen werden nicht
  aus einer allgemeinen Prüfberechtigung abgeleitet.
- Vor Änderungen werden die exakten Ziele und Auswirkungen geprüft.
- Der bestehende Lizenzserver und Orchestrator werden nicht gestoppt oder neu
  gestartet, sofern dies nicht ausdrücklich beauftragt wurde.
- Keine `.env`-Datei wird vollständig ausgegeben.
- Keine Docker-Secrets, Tokens, Passwort-Hashes oder Datenbankkennwörter werden
  in Befehlsausgaben übernommen.
- Private SSH-Schlüssel werden weder gelesen noch kopiert noch dokumentiert.
- Produktivdaten werden bei Diagnosearbeiten nicht verändert.

### 3.4 Bekannter SSH-Sicherheitszustand

Am 20. August 2026 war die administrative Anmeldung per vorhandenem SSH-Key
möglich. Eine direkte Passwortanmeldung für den administrativen Benutzer war
nicht erlaubt. Passwortanmeldungen waren für andere mögliche Benutzer allgemein
noch aktiviert. Vor einer späteren Deaktivierung muss geprüft werden, ob ein
bestehender Benutzer oder automatisierter Ablauf darauf angewiesen ist. Genaue
Zugangswerte stehen ausschließlich in der lokalen Zugangsdatei.

## 4. Bestandsaufnahme vom 20. August 2026

Die Bestandsaufnahme wurde ausschließlich lesend über SSH durchgeführt.
Bestehende Dienste, Dateien und Konfigurationen wurden nicht verändert.

### 4.1 Betriebssystem

| Merkmal | Festgestellter Wert |
|---|---|
| Betriebssystem | Ubuntu 24.04.4 LTS, Noble Numbat |
| Kernel | Linux 6.8.0-111-generic |
| Architektur | x86-64 |
| Virtualisierung | KVM |
| Laufzeit seit Neustart | ungefähr 98 Tage |

### 4.2 Ressourcen

| Ressource | Gesamt | Belegt/frei zum Prüfzeitpunkt |
|---|---:|---:|
| CPU | 2 vCPU | sehr geringe Last |
| RAM | 3,7 GiB | ca. 728 MiB belegt, ca. 3,0 GiB verfügbar |
| Swap | 0 | nicht eingerichtet |
| Dateisystem `/` | 38 GiB | 8,3 GiB belegt, 28 GiB frei |

Die Load Averages lagen bei `0.19`, `0.06` und `0.01`. Der Server hatte zum
Prüfzeitpunkt deutliche Reserven.

### 4.3 Speicherverbrauch ausgewählter Bereiche

| Bereich | Verbrauch |
|---|---:|
| `/opt` | ca. 134 MiB |
| `/var/lib/docker` | ca. 348 MiB |
| `/var/log` | ca. 3,9 GiB |

Der vergleichsweise hohe Verbrauch unter `/var/log` soll vor der späteren
Produktivbereitstellung genauer untersucht werden. Es wurden keine Logs
gelöscht.

### 4.4 Docker

| Komponente | Version |
|---|---|
| Docker Engine | 29.6.2 |
| Docker Compose | 5.3.1 |

Docker meldete:

- drei Images mit zusammen ungefähr 560 MiB,
- einen laufenden Container,
- drei unbenutzte lokale Volumes mit zusammen ungefähr 12 MiB und
- ungefähr 1,8 GiB ungenutzten Build-Cache.

Diese potentiell bereinigbaren Daten wurden nur festgestellt und nicht
entfernt.

### 4.5 Bestehender Docker-Orchestrator

Der vorhandene Compose-Stack lautet:

```text
Name:        siehe lokale Zugangsdatei
Compose:     siehe lokale Zugangsdatei
Container:   siehe lokale Zugangsdatei
Hostbindung: 127.0.0.1:8001 -> Container-Port 8000
```

Zum Prüfzeitpunkt benötigte der Container ungefähr:

- 80 MiB RAM und
- 0,3 Prozent CPU.

Zusätzlich existiert im Verzeichnis des Orchestrators eine weitere
Compose-Datei. Deren Pfad steht in der lokalen Zugangsdatei; ihr Inhalt wurde bei
der Bestandsaufnahme nicht in die Dokumentation übernommen.

### 4.6 Bestehender Lizenzserver

Der Lizenzserver läuft außerhalb des festgestellten Docker-Stacks als
systemd-Dienst:

```text
<LIZENZSERVER-DIENST>
```

Er lauscht lokal auf:

```text
127.0.0.1:8000
```

Nginx veröffentlicht ihn über:

```text
<LIZENZSERVER-DOMAIN>
```

Die interne Bindung an `127.0.0.1` ist beizubehalten. Der Lizenzserver darf
nicht durch die Ports des Wettkampf-Stacks überlagert werden.

### 4.7 Nginx und HTTPS

Nginx läuft als aktiver systemd-Dienst und lauscht öffentlich auf TCP 80 und
443. Certbot verwaltet die vorhandenen Zertifikate.

Festgestellte Zuordnungen:

```text
<LIZENZSERVER-DOMAIN>
    -> http://127.0.0.1:8000

<ORCHESTRATOR-DOMAIN>
    -> http://127.0.0.1:8001
```

Außerdem sind die Hauptdomain und ihre `www`-Variante in der
Nginx-Konfiguration vorhanden. Die konkreten Namen stehen nur in der lokalen
Zugangsdatei.

Der Certbot-Timer ist aktiv. Die Zertifikatserneuerung wird damit regelmäßig
aufgerufen. Eine erfolgreiche automatische Erneuerung ist vor dem
Produktivbetrieb weiterhin praktisch zu prüfen.

### 4.8 Firewall und öffentlich erreichbare Ports

UFW ist aktiv. Die Standardregeln lauten:

```text
eingehend:  verweigern
ausgehend:  erlauben
geroutet:   verweigern
```

Öffentlich erlaubt sind für IPv4 und IPv6 ausschließlich:

- TCP 22 für SSH,
- TCP 80 für HTTP und
- TCP 443 für HTTPS.

Die Anwendungsports 8000 und 8001 sind nur an `127.0.0.1` gebunden. Dieses
Muster wird für die Wettkampf-API beibehalten. PostgreSQL darf keine öffentliche
Firewallfreigabe erhalten.

### 4.9 Updates

`unattended-upgrades` ist aktiviert und lief zum Prüfzeitpunkt. Eine simulierte
Paketaktualisierung meldete trotzdem 27 aktualisierbare Pakete.

Vor der späteren Serverbereitstellung ist deshalb ein separates Wartungsfenster
vorzusehen:

1. ausstehende Pakete prüfen,
2. Backups der bestehenden Anwendungen kontrollieren,
3. Aktualisierungen installieren,
4. erforderlichen Neustart feststellen,
5. gegebenenfalls kontrolliert neu starten und
6. Lizenzserver, Orchestrator, Nginx und Zertifikate anschließend prüfen.

Diese Wartung ist nicht Teil der lokalen Entwicklungsphase und wurde bislang
nicht ausgeführt.

### 4.10 Bestehende Backups

Im Hetzner-Cloud-Dashboard waren Serverbackups zum Prüfzeitpunkt nicht
aktiviert. Auf dem Betriebssystem wurde kein eigener Timer für fachliche
Anwendungs- oder PostgreSQL-Backups festgestellt. Die sichtbaren Timer für
Paketdatenbank, APT und Certbot sind keine Sicherung der Anwendungsdaten.

## 5. Eignungsbewertung

Der Server ist für den geplanten Vereinsbetrieb voraussichtlich ausreichend:

- genügend freie CPU-Kapazität,
- ungefähr 3 GiB verfügbarer RAM,
- 28 GiB freier lokaler Speicher,
- bestehender und sinnvoll konfigurierter Nginx-Reverse-Proxy,
- funktionierendes HTTPS-Konzept,
- aktive Firewall und
- vorhandene Docker-Compose-Infrastruktur.

Die Eignung gilt unter folgenden Bedingungen:

- API und PostgreSQL erhalten Ressourcenlimits,
- PostgreSQL bleibt intern,
- Backups werden vor der Nutzung mit echten Daten eingerichtet,
- Speicher- und Logverbrauch werden überwacht,
- ausstehende Wartung wird kontrolliert durchgeführt und
- ein Last- und Stabilitätstest bestätigt die Annahmen.

Eine Vergrößerung des CX23 ist derzeit nicht erforderlich. Die tatsächliche
Auslastung wird nach der Testbereitstellung erneut bewertet.

## 6. Zielarchitektur auf dem Server

```text
Internet
   |
   | TCP 443 / HTTPS
   v
Nginx auf dem Host
   |
   | http://127.0.0.1:8002
   v
wettkampf-api (Docker-Container)
   |
   | nur internes Docker-Netzwerk
   v
wettkampf-postgres (Docker-Container)
   |
   v
persistentes PostgreSQL-Volume
```

### 6.1 Geplanter Ablageort

Der produktive Stack soll getrennt von vorhandenen Projekten liegen:

```text
/opt/wettkampf-server/
```

Vorgesehene Struktur:

```text
/opt/wettkampf-server/
|- compose.yml
|- Dockerfile oder referenziertes Image
|- Konfiguration ohne eingecheckte Geheimnisse
|- scripts/
|  |- backup
|  `- restore-check
`- backup-status/
```

Der konkrete Aufbau wird zunächst lokal entwickelt. Dateien werden nicht
manuell auf dem Produktivserver entwickelt.

### 6.2 Container

Geplant sind zwei Container:

#### `wettkampf-api`

- Java-Serveranwendung,
- interner Anwendungsport, voraussichtlich 8080,
- Hostbindung ausschließlich `127.0.0.1:8002`,
- keine direkte öffentliche Erreichbarkeit,
- Datenbankzugang nur über das interne Docker-Netzwerk,
- begrenzter JVM-Heap,
- zunächst ungefähr 768 MiB RAM-Limit,
- Healthcheck und kontrollierte Neustartregel.

#### `wettkampf-postgres`

- PostgreSQL in festgelegter Hauptversion,
- keine Host-Portfreigabe,
- Zugriff nur durch `wettkampf-api` und kontrollierte Backupvorgänge,
- eigenes persistentes Volume,
- ungefähr 512 bis 768 MiB RAM-Limit als Ausgangswert,
- Healthcheck,
- sichere Zugangsdaten aus Server-Secrets beziehungsweise geschützter
  Umgebungskonfiguration.

Die endgültigen Limits werden mit lokaler Messung und Testbereitstellung
festgelegt.

### 6.3 Ports

| Port | Bindung | Verwendung |
|---:|---|---|
| 22 | öffentlich | bestehendes SSH |
| 80 | öffentlich | bestehendes HTTP/Certbot |
| 443 | öffentlich | bestehendes HTTPS/Nginx |
| 8000 | `127.0.0.1` | bestehender Lizenzserver |
| 8001 | `127.0.0.1` | bestehender Orchestrator |
| 8002 | `127.0.0.1` | geplante Wettkampf-API |
| 5432 | nur Docker-Netzwerk | geplantes PostgreSQL |

Für PostgreSQL wird weder UFW noch Docker mit einer öffentlichen
Host-Portfreigabe konfiguriert.

### 6.4 Subdomain

Als möglicher Name wurde vorgeschlagen:

```text
wettkampf-api.<DOMAIN>
```

Die endgültige Subdomain ist noch nicht beschlossen. Nach der Entscheidung sind
folgende Schritte nötig:

1. DNS-A- und gegebenenfalls AAAA-Eintrag setzen.
2. Erreichbarkeit des DNS-Namens prüfen.
3. Nginx-Serverblock für die neue Subdomain erstellen.
4. Anfrage an `127.0.0.1:8002` weiterleiten.
5. HTTPS-Zertifikat mit Certbot beziehen.
6. automatische Zertifikatserneuerung testen.
7. HTTP dauerhaft auf HTTPS umleiten.

## 7. Lokale Entwicklungsarchitektur

### 7.1 Ziel

Vor jeder Serveränderung soll der gesamte Vereinsbetrieb auf dem
Entwicklungsrechner funktionieren:

```text
JavaFX-Vereinsmodus
        |
        | HTTP auf localhost
        v
lokale Wettkampf-API
        |
        | internes lokales Docker-Netzwerk
        v
lokales PostgreSQL
```

Die lokale Sportleiter-SQLite-Datenbank bleibt davon unabhängig.

### 7.2 Vorgesehene Entwicklungsschritte

1. Serverprojekt im Repository anlegen.
2. Server-Build unabhängig vom Desktop-Build ausführbar machen.
3. PostgreSQL für Tests und Entwicklung lokal bereitstellen.
4. Datenbankschema mit versionierten Migrationen erstellen.
5. gemeinsamen Vereinszugang implementieren.
6. fachliche API schrittweise implementieren.
7. automatisierte Server- und Datenbanktests ergänzen.
8. Online-Datenservice der Desktopanwendung implementieren.
9. Moduswechsel und strikte Datentrennung testen.
10. Docker-Image lokal bauen.
11. vollständigen Compose-Stack lokal testen.
12. Backup und Restore mit Testdaten durchführen.
13. erst anschließend Testbereitstellung auf Hetzner planen.

### 7.3 Lokale Geheimnisse

Lokale Passwörter und Tokens werden nicht fest in Quellcode oder Compose-Dateien
geschrieben. Geeignete lokale Konfigurationsdateien werden in `.gitignore`
aufgenommen. Eine eingecheckte Beispieldatei enthält ausschließlich Platzhalter,
zum Beispiel:

```text
DB_PASSWORD=<lokales-testpasswort>
VEREIN_PASSWORD=<gemeinsames-passwort-nicht-hier-eintragen>
```

## 8. Backupkonzept

### 8.1 Schutzziele

Backups müssen schützen vor:

- versehentlichem Löschen durch einen Verein,
- fehlerhaftem Serverupdate,
- beschädigtem PostgreSQL-Volume,
- Verlust des gesamten Servers,
- Fehlkonfiguration und
- notwendigem Anbieter- oder Serverwechsel.

### 8.2 Vorgesehene Ebenen

#### Ebene 1: PostgreSQL-Export

- täglich automatisiertes `pg_dump`,
- mehrere Generationen,
- komprimiertes portables Format,
- Rücksicherung in eine leere Testdatenbank regelmäßig prüfen.

#### Ebene 2: Externe Kopie

- Kopie außerhalb dieses Hetzner-Servers,
- verschlüsselte Übertragung und Speicherung,
- eigenes Aufbewahrungskonzept,
- Zugriff nur für den Betreiber.

#### Ebene 3: Hetzner-Backup oder Snapshot

- zusätzliche Sicherung des gesamten Servers,
- besonders vor größeren Wartungsarbeiten,
- kein Ersatz für einen portablen PostgreSQL-Export.

### 8.3 Noch offene Backupentscheidung

Der Zielort der externen PostgreSQL-Sicherungen ist noch festzulegen. Vor dieser
Entscheidung werden keine echten personenbezogenen Wettkampfdaten auf dem Server
gespeichert.

## 9. Betriebssicherheit vor Produktivsetzung

Vor der Nutzung durch Vereine sind mindestens folgende Punkte abzuschließen:

- ausstehende Betriebssystemupdates kontrolliert installieren,
- Neustartverhalten aller vorhandenen Dienste prüfen,
- Notwendigkeit und Größe eines Swap-Bereichs festlegen,
- `/var/log` und Logrotation untersuchen,
- Container-Ressourcenlimits setzen,
- PostgreSQL-Port als nicht öffentlich nachweisen,
- UFW-Regeln nach der Bereitstellung erneut prüfen,
- Nginx-Konfiguration mit `nginx -t` validieren,
- HTTPS und Zertifikatserneuerung testen,
- tägliche PostgreSQL-Sicherung einrichten,
- externe Sicherung einrichten,
- Wiederherstellung erfolgreich durchführen,
- Monitoring oder mindestens Healthchecks einrichten,
- Verhalten bei vollem Datenträger testen beziehungsweise alarmieren,
- gemeinsames Vereinskennwort sicher erzeugen und verteilen,
- Datenschutz und Aufbewahrung klären und
- Pilotbetrieb nur mit Testdaten durchführen.

## 10. Änderungs- und Bereitstellungsregeln

### 10.1 Vor jeder Produktivänderung

1. genaue Änderung beschreiben,
2. betroffene Dienste benennen,
3. vorhandenen Zustand lesend prüfen,
4. Backupstatus kontrollieren,
5. Rückweg festlegen,
6. Änderung ausdrücklich freigeben lassen und
7. nur den vorgesehenen Stack verändern.

### 10.2 Nach jeder Produktivänderung

1. Compose- beziehungsweise Dienststatus prüfen,
2. Healthcheck der Wettkampf-API prüfen,
3. HTTPS-Endpunkt prüfen,
4. Lizenzserver prüfen,
5. Orchestrator prüfen,
6. öffentlich lauschenende Ports prüfen,
7. Fehlerlogs ohne Ausgabe von Geheimnissen prüfen und
8. Änderung und Ergebnis dokumentieren.

### 10.3 Unzulässige Abkürzungen

- keine Entwicklung direkt in `/opt`,
- keine öffentliche Freigabe von PostgreSQL,
- keine Geheimnisse in Git,
- keine Wiederverwendung der Lizenzserver-Datenbank,
- keine gemeinsame Datenbank mit dem Orchestrator,
- kein unkontrolliertes `docker system prune`,
- kein pauschales Stoppen aller Container,
- keine Betriebssystemaktualisierung ohne Wartungsplanung und
- keine Produktivdaten vor erfolgreichem Restore-Test.

## 11. Diagnosebefehle für spätere Bestandsaufnahmen

Die folgenden Befehle sind Beispiele für lesende Prüfungen. Sie werden nur auf
dem autorisierten Server ausgeführt.

### Ressourcen

```bash
uptime
free -h
df -hT /
```

### Docker

```bash
docker ps
docker compose ls
docker stats --no-stream
docker system df
```

### Ports und Firewall

```bash
ss -lntup
ufw status verbose
```

### Dienste

```bash
systemctl is-active docker
systemctl is-active nginx
systemctl is-active <LIZENZSERVER-DIENST>
```

Diese Befehle ändern keinen Zustand. Befehle zur Bereinigung, Aktualisierung,
Neustart oder Änderung von Konfigurationen gehören nicht zu einer lesenden
Bestandsaufnahme.

## 12. Noch offene Entscheidungen

Vor der lokalen Implementierung beziehungsweise späteren Bereitstellung bleiben
folgende Punkte offen:

1. endgültige Java-Servertechnik,
2. Struktur des Serverprojekts im Repository,
3. genaue PostgreSQL-Hauptversion,
4. endgültige API-Subdomain,
5. externer Zielort für Backups,
6. Aufbewahrungsdauer der Sicherungen,
7. genauer Speicherrahmen der Container,
8. Einrichtung eines Swap-Bereichs,
9. Monitoring- und Alarmierungsweg,
10. Verantwortlicher und Vertretung für den Serverbetrieb und
11. Wartungsfenster für bestehende Serverupdates.

## 13. Zusammenfassung

Der vorhandene Hetzner-CX23 besitzt nach der Bestandsaufnahme genügend Reserve
für den geplanten Vereinsbetrieb. Die existierende Kombination aus Nginx,
Certbot, UFW und Docker bietet eine geeignete Grundlage.

Die Wettkampf-API wird später als eigener Stack unter
`/opt/wettkampf-server` ergänzt und lokal über `127.0.0.1:8002` an Nginx
angebunden. PostgreSQL bleibt vollständig im internen Docker-Netzwerk. Die
bestehenden Anwendungen auf den Ports 8000 und 8001 bleiben unberührt.

Vor der Serverbereitstellung wird die gesamte Lösung lokal entwickelt,
automatisiert getestet und einschließlich Backup und Wiederherstellung geprüft.
Erst danach erfolgen kontrollierte, separat freizugebende Änderungen am
Produktivserver.
