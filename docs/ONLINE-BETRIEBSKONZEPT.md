# Konzept: Lokaler Sportleiterbetrieb und gemeinsame Vereinsdatenbank

> **Umsetzungsstand:** Die erste Ausbaustufe dieses Konzepts wurde am
> 21.08.2026 umgesetzt und auf dem Server bereitgestellt. Abweichungen zwischen
> ursprünglicher Planung und tatsächlicher Implementierung sowie Bedienung,
> Sicherheit, Migration und Offline-Notbetrieb beschreibt
> [ONLINE-UMBAU-2026-08-21.md](ONLINE-UMBAU-2026-08-21.md).

## 1. Zweck dieses Dokuments

Dieses Dokument beschreibt die geplante Erweiterung des
Blasrohr-Wettkampf-Managers um zwei umschaltbare Betriebsarten:

1. einen vollständig lokalen Betrieb mit einer SQLite-Datenbank und
2. einen gemeinsamen Onlinebetrieb für alle Vereine.

Es handelt sich um ein technisches und fachliches Umsetzungskonzept. Als
Zielsystem für die spätere Bereitstellung wurde der bereits vorhandene
Hetzner-Cloudserver ausgewählt. Die konkrete Serveranwendung und zusätzliche
Programmbibliotheken werden erst während der lokalen Entwicklungsphase
eingeführt. Die dokumentierte Bestandsaufnahme und der vorgesehene Betrieb des
Servers stehen in [HETZNER-SERVERKONZEPT.md](HETZNER-SERVERKONZEPT.md).

## 2. Verbindliche Grundentscheidungen

Für die weitere Planung gelten folgende Entscheidungen:

- Es bleibt bei einer einzigen JavaFX-Desktopanwendung.
- Die Anwendung kann zwischen den Betriebsarten `LOKAL` und `VEREIN` wechseln.
- Im lokalen Modus arbeitet die Anwendung mit einer vom Benutzer ausgewählten
  SQLite-Datei.
- Die lokale Datenbank des Sportleiters befindet sich ausschließlich auf dessen
  Rechner und gilt als fachlich maßgeblicher Datenbestand.
- Im Vereinsmodus arbeiten alle Vereine auf einer gemeinsamen Online-Datenbank.
- Alle Vereine verwenden dasselbe Konto und dasselbe Passwort.
- Alle Vereine dürfen den gesamten Online-Datenbestand lesen und bearbeiten.
- Es gibt keine Einschränkung von Datensätzen auf einen bestimmten Verein.
- Die lokale Datenbank und die Online-Datenbank werden niemals synchronisiert.
- Es gibt keinen digitalen Versand von Ergebnissen an den Sportleiter.
- Der Sportleiter erhält die Ergebnisse in Papierform und trägt sie selbst in
  seine lokale Datenbank ein.
- Es gibt keinen Import von Online-Ergebnissen in die lokale Datenbank und
  keinen Export der lokalen Datenbank in die Online-Datenbank.
- Bei Unterschieden zwischen beiden Datenbeständen gilt ausschließlich die
  lokale Datenbank des Sportleiters.
- Die Serverkomponenten werden zunächst vollständig lokal entwickelt und
  automatisiert getestet.
- Eine Bereitstellung erfolgt später als eigenständiger Docker-Compose-Stack auf
  dem bereits vorhandenen Hetzner-Cloudserver.

## 3. Ziele

### 3.1 Fachliche Ziele

- Mehrere Vereine sollen denselben Datenbestand verwenden können.
- Ein Verein soll einen Wettkampftag erfassen können, sodass andere Vereine die
  eingetragenen Daten anschließend unmittelbar sehen.
- Die Bedienoberfläche soll in beiden Betriebsarten möglichst gleich bleiben.
- Der Sportleiter soll unabhängig vom Internet weiterarbeiten können.
- Der Onlinebetrieb darf die Integrität der lokalen Sportleiter-Datenbank nicht
  beeinflussen.
- Der Wechsel zwischen den Betriebsarten soll verständlich und eindeutig sein.
- Die Bedienung des Vereinszugangs soll möglichst einfach bleiben.

### 3.2 Technische Ziele

- Die Oberfläche soll nicht wissen müssen, ob Daten aus SQLite oder von einem
  Server kommen.
- Bestehende Fachlogik soll nach Möglichkeit in beiden Betriebsarten verwendet
  werden können.
- Netzwerkfehler sollen nicht zu unklaren oder teilweise gespeicherten Daten
  führen.
- Gleichzeitige Änderungen sollen erkannt werden.
- Die Online-Datenbank soll regelmäßig gesichert werden.
- Die Serveradresse und der aktive Betriebsmodus sollen konfigurierbar sein.
- Zusätzliche Abhängigkeiten werden erst nach einer gesonderten Entscheidung
  eingeführt.

## 4. Nicht-Ziele

Folgende Funktionen sind ausdrücklich nicht Bestandteil des geplanten Systems:

- Synchronisation zwischen lokaler und öffentlicher Datenbank
- automatischer Ergebnisversand an den Sportleiter
- Dateiimport von Vereinsergebnissen beim Sportleiter
- zentrale Verwaltung der lokalen Datenbank des Sportleiters
- Fernzugriff auf den Rechner des Sportleiters
- unterschiedliche Rechte für unterschiedliche Vereine
- Zuordnung eines Benutzerkontos zu einem bestimmten Verein
- Freigabeverfahren für Änderungen innerhalb der Vereinsdatenbank
- anonyme Nutzung der Online-Datenbank ohne Anmeldung
- direkter JDBC-Zugriff der Desktopanwendung auf die Internetdatenbank
- Ablage einer gemeinsam bearbeiteten SQLite-Datei in einem synchronisierten
  Cloudordner oder auf einer Netzwerkfreigabe

## 5. Gesamtarchitektur

```text
                    Blasrohr-Wettkampf-Manager
                               |
                   Auswahl der Betriebsart
                      /                    \
                     /                      \
            Modus LOKAL                 Modus VEREIN
                 |                           |
          SQLite-Zugriff                  HTTPS
                 |                           |
      lokale Datenbankdatei         Wettkampf-Server/API
      auf demselben Rechner                   |
                                        Server-Datenbank
```

Die beiden Pfade sind voneinander getrennt. Im lokalen Modus wird keine
Verbindung zum Wettkampf-Server aufgebaut. Im Vereinsmodus wird nicht auf die
fachliche lokale SQLite-Datenbank zugegriffen.

### 5.1 Lokaler Datenpfad

```text
JavaFX-View -> Controller/Fachservice -> lokale DAOs -> DBController -> SQLite
```

Dieser Pfad entspricht weitgehend der bestehenden Anwendung. Die ausgewählte
SQLite-Datei bleibt die einzige fachliche Datenquelle des lokalen Modus.

### 5.2 Online-Datenpfad

```text
JavaFX-View -> Controller/Fachservice -> Online-Client -> HTTPS -> REST-API
                                                               -> Serverlogik
                                                               -> Server-DB
```

Die Desktopanwendung erhält keinen direkten Datenbankbenutzer für die
Server-Datenbank. Ausschließlich der Server darf mit der Server-Datenbank
kommunizieren.

## 6. Bedeutung der beiden Datenbestände

### 6.1 Lokaler Datenbestand

Jede lokale SQLite-Datei ist ein eigenständiger Datenbestand. Insbesondere die
Datei des Sportleiters:

- liegt auf dessen Rechner oder einem von ihm kontrollierten lokalen
  Speichermedium,
- kann ohne Internetverbindung geöffnet und bearbeitet werden,
- wird nicht automatisch hochgeladen,
- wird nicht mit der Online-Datenbank verglichen,
- wird nicht durch einen Wechsel in den Vereinsmodus verändert und
- enthält den maßgeblichen offiziellen Stand.

Andere Benutzer können den lokalen Modus ebenfalls verwenden und dabei ihre
eigenen SQLite-Dateien auswählen. Der Modus ist nicht technisch ausschließlich
dem Sportleiter vorbehalten. Geschützt ist die Sportleiter-Datenbank dadurch,
dass nur sie sich auf seinem Rechner befindet.

### 6.2 Gemeinsamer Online-Datenbestand

Die Online-Datenbank dient der Zusammenarbeit der Vereine. Alle Benutzer des
gemeinsamen Vereinskontos haben dieselben fachlichen Möglichkeiten. Sie dürfen
insbesondere:

- Saisons verwalten,
- Wettkampftage verwalten,
- Ligen und Altersklassen verwalten,
- Mannschaften und Schützen verwalten,
- Begegnungen verwalten,
- Ergebnisse eintragen und korrigieren,
- Tabellen und Auswertungen anzeigen und
- die vorhandenen PDF-Funktionen verwenden, soweit diese für den Onlinebetrieb
  technisch angebunden wurden.

Der Online-Datenbestand ist ein Arbeits- und Informationsstand der Vereine. Er
ist nicht automatisch der offizielle Endstand des Sportleiters.

## 7. Auswahl und Wechsel der Betriebsart

### 7.1 Erster Programmstart

Beim ersten Start soll die Anwendung eine verständliche Auswahl anzeigen:

```text
Wie möchten Sie den Wettkampf-Manager verwenden?

[Lokale Datenbank verwenden]
[Gemeinsame Vereinsdatenbank verwenden]
```

Bei Auswahl von `Lokale Datenbank verwenden` folgt die bisherige Auswahl zum
Öffnen oder Anlegen einer SQLite-Datei.

Bei Auswahl von `Gemeinsame Vereinsdatenbank verwenden` wird die konfigurierte
Serveradresse verwendet und gegebenenfalls die Anmeldung angezeigt.

### 7.2 Späterer Wechsel

Das Menü soll die Betriebsart eindeutig abbilden, beispielsweise:

```text
Datenbank
|- Lokale Datenbank verwenden ...
|- Gemeinsame Vereinsdatenbank verwenden ...
|- Lokale Datenbank wechseln ...
`- Verbindung zum Vereinsserver trennen
```

Nur zum aktiven Modus passende Einträge sollen auswählbar sein.

### 7.3 Ablauf eines Moduswechsels

Vor einem Wechsel muss die Anwendung:

1. laufende Eingaben kontrolliert abschließen oder verwerfen lassen,
2. geöffnete Bearbeitungsfenster schließen,
3. den bisherigen Datenzugriff freigeben,
4. den neuen Datenzugriff initialisieren,
5. alle Auswahllisten und Ansichten aus dem neuen Datenbestand laden und
6. den Fenstertitel und die optische Moduskennzeichnung aktualisieren.

Objekte aus dem bisherigen Datenbestand dürfen nach dem Wechsel nicht in der
Oberfläche weiterverwendet oder im neuen Datenbestand gespeichert werden.

### 7.4 Dauerhaft gespeicherte Einstellungen

Getrennt gespeichert werden sollen:

- zuletzt aktiver Betriebsmodus,
- zuletzt verwendeter lokaler Datenbankpfad,
- Adresse des Vereinsservers,
- Information, ob eine gültige Online-Sitzung vorhanden ist.

Das gemeinsame Vereinskennwort darf nicht im Klartext in der Windows-Registry,
in einer Properties-Datei oder im Quellcode gespeichert werden. Die genaue
Form einer sicheren Sitzungs- oder Anmeldedatenspeicherung wird in der
Detailplanung festgelegt.

## 8. Eindeutige Anzeige des aktiven Modus

Eine Verwechslung der Betriebsarten muss möglichst unwahrscheinlich sein. Nur
der Dateiname im Fenstertitel reicht dafür nicht aus.

Vorgesehene Kennzeichnungen:

- Lokaler Fenstertitel:
  `Blasrohr-Wettkampf-Manager - LOKAL - wettkampf_db.db`
- Online-Fenstertitel:
  `Blasrohr-Wettkampf-Manager - GEMEINSAME VEREINSDATENBANK`
- dauerhafte Statusanzeige im Hauptfenster,
- unterschiedliche, zurückhaltende Akzentfarbe je Betriebsart,
- Tooltip oder Detailanzeige mit lokalem Pfad beziehungsweise Serveradresse.

Der Status darf nicht nur durch Farbe vermittelt werden, damit die Anzeige
auch bei Farbsehschwächen verständlich bleibt.

## 9. Gemeinsames Vereinskonto

### 9.1 Gewähltes Modell

Für maximale Einfachheit gibt es zunächst genau ein fachliches Vereinskonto:

- einen gemeinsamen Benutzernamen und
- ein gemeinsames Passwort.

Alle Vereine erhalten dieselben Zugangsdaten. Nach erfolgreicher Anmeldung darf
jeder Benutzer alle Online-Daten lesen und ändern.

### 9.2 Auswirkungen des gemeinsamen Kontos

Das Modell ist leicht zu erklären und zu bedienen, hat aber bewusst akzeptierte
Folgen:

- Änderungen können nicht sicher einem bestimmten Verein zugeordnet werden.
- Das Passwort muss bei Ausscheiden einer berechtigten Person gemeinsam
  gewechselt und neu verteilt werden.
- Wird das Passwort weitergegeben, erhält die neue Person vollständigen Zugriff.
- Das Änderungsprotokoll kann nur das gemeinsame Konto, nicht den tatsächlich
  handelnden Verein nennen.
- Ein versehentliches Löschen oder Ändern kann grundsätzlich durch jeden
  angemeldeten Benutzer erfolgen.

Diese Nachteile werden nicht durch komplizierte Rechte, sondern durch Backups,
technische Validierung und ein Änderungsprotokoll begrenzt.

### 9.3 Mindestanforderungen an die Anmeldung

- Die Anmeldung erfolgt ausschließlich über HTTPS.
- Das Passwort wird serverseitig niemals im Klartext gespeichert.
- Der Server speichert nur einen geeigneten gesalzenen Passwort-Hash.
- Fehlgeschlagene Anmeldeversuche werden begrenzt oder zeitlich verzögert.
- Nach erfolgreicher Anmeldung verwendet der Client eine zeitlich begrenzte
  Sitzung beziehungsweise ein widerrufbares Zugriffstoken.
- Das Passwort wird nicht in Serverprotokolle geschrieben.
- Das Passwort wird nicht als URL-Parameter übertragen.
- Eine Funktion zum Wechseln des gemeinsamen Passworts wird vorgesehen.
- Eine verlorene oder kompromittierte Sitzung muss serverseitig ungültig
  gemacht werden können.

### 9.4 Nicht vollständig öffentliche Datenbank

Die Bezeichnung „öffentliche Datenbank“ bedeutet in diesem Konzept „gemeinsam
für alle berechtigten Vereine“ und nicht „anonym für das gesamte Internet“.
Ohne Anmeldung wäre die Datenbank automatisierten Angriffen, Suchmaschinen,
Bots und beliebigen Änderungen durch Dritte ausgesetzt.

## 10. Server und API

### 10.1 Aufgabe des Servers

Der Server ist die einzige Komponente, die auf die Online-Datenbank zugreift.
Er übernimmt:

- Anmeldung und Sitzungsprüfung,
- Validierung aller Eingaben,
- Ausführung fachlicher Operationen,
- Transaktionssteuerung,
- Konflikterkennung,
- Protokollierung,
- einheitliche Fehlermeldungen und
- Schutz der Datenbankzugangsdaten.

### 10.2 Warum kein direkter Datenbankzugriff aus JavaFX erfolgt

Ein direkter JDBC-Zugriff aus der ausgelieferten Desktopanwendung würde
Datenbankzugangsdaten auf jedem Vereinsrechner erfordern. Diese könnten aus der
Konfiguration oder Anwendung ausgelesen werden. Außerdem ließen sich die
fachlichen Prüfungen des Programms umgehen und beliebige SQL-Befehle gegen die
Datenbank ausführen.

Deshalb kommuniziert der Client ausschließlich mit einer fachlichen API über
HTTPS. Die Datenbank selbst ist nicht öffentlich erreichbar.

### 10.3 Beispielhafte API-Bereiche

Die endgültigen Pfade werden erst bei der Implementierung festgelegt. Fachlich
werden mindestens folgende Bereiche benötigt:

```text
POST   /api/anmeldung
POST   /api/abmeldung

GET    /api/saisons
POST   /api/saisons
PUT    /api/saisons/{id}
DELETE /api/saisons/{id}

GET    /api/wettkampftage
POST   /api/wettkampftage
PUT    /api/wettkampftage/{id}
DELETE /api/wettkampftage/{id}

GET/POST/PUT/DELETE /api/ligen/...
GET/POST/PUT/DELETE /api/altersklassen/...
GET/POST/PUT/DELETE /api/mannschaften/...
GET/POST/PUT/DELETE /api/schuetzen/...
GET/POST/PUT/DELETE /api/begegnungen/...
GET/POST/PUT        /api/ergebnisse/...
```

Die Desktopanwendung soll fachliche Operationen aufrufen und keine frei
formulierten SQL-Abfragen an den Server schicken.

### 10.4 Antworten und Fehler

API-Antworten müssen eindeutig zwischen folgenden Fällen unterscheiden:

- Operation erfolgreich,
- Eingabe fachlich ungültig,
- Anmeldung erforderlich oder abgelaufen,
- Datensatz wurde zwischenzeitlich geändert,
- Datensatz existiert nicht mehr,
- Server vorübergehend nicht erreichbar und
- interner Serverfehler.

Technische Details, SQL-Texte, Stacktraces und interne Datenbankpfade dürfen
nicht an den Client übertragen werden.

## 11. Datenmodell der Online-Datenbank

Die fachlichen Entitäten sollen zunächst dem lokalen Modell entsprechen:

- Saison
- Wettkampftag
- Liga
- Altersklasse
- Mannschaft
- Schütze
- Saisonmeldung eines Schützen
- Begegnung
- Ergebnis

Bestehende Primär- und Fremdschlüsselbeziehungen sowie fachliche Prüfungen
müssen serverseitig erhalten bleiben. Dazu gehören beispielsweise:

- ein Ergebnis gehört zu genau einem Schützen und einem Wettkampftag,
- pro Schütze und Wettkampftag existiert höchstens ein Ergebnis,
- Ringzahlen liegen im zulässigen Bereich,
- eine Mannschaft kann nicht gegen sich selbst antreten und
- abhängige Datensätze dürfen nicht verwaist zurückbleiben.

Für den über das Internet erreichbaren Mehrbenutzerbetrieb wird PostgreSQL als
Client-Server-Datenbank vorgesehen. SQLite bleibt auf den lokalen Modus
beschränkt. PostgreSQL und die Wettkampf-API werden zunächst lokal und später
als voneinander getrennte Container auf dem vorhandenen Hetzner-Server
betrieben.

## 12. Gleichzeitige Bearbeitung

Da alle Vereine alles ändern dürfen, können mehrere Benutzer denselben Datensatz
gleichzeitig öffnen. Vertrauen verhindert keine versehentlichen
Überschreibungen. Deshalb soll optimistische Konflikterkennung verwendet werden.

Jeder veränderbare Online-Datensatz erhält beispielsweise:

- eine Versionsnummer oder
- einen ausreichend präzisen Änderungszeitpunkt.

Beispiel:

1. Client A und Client B laden Ergebnisversion 3.
2. Client A speichert eine Änderung; der Server erzeugt Version 4.
3. Client B versucht anschließend, seine noch auf Version 3 basierende Änderung
   zu speichern.
4. Der Server lehnt den veralteten Schreibvorgang ab.
5. Client B erhält eine verständliche Meldung und lädt den aktuellen Stand neu.

Die Anwendung soll nicht stillschweigend nach dem Prinzip „der letzte gewinnt“
überschreiben.

## 13. Löschen und Änderungsprotokoll

### 13.1 Sicherheitsabfragen

Löschvorgänge bleiben grundsätzlich möglich, da alle Vereinsbenutzer volle
Rechte besitzen. Die Anwendung muss jedoch:

- das konkrete Löschziel anzeigen,
- auf abhängige Daten hinweisen,
- eine ausdrückliche Bestätigung verlangen und
- eine verständliche Fehlermeldung anzeigen, wenn die Datenbank das Löschen
  wegen bestehender Abhängigkeiten ablehnt.

### 13.2 Änderungsprotokoll

Der Server soll für wichtige Änderungen mindestens speichern:

- eindeutige Protokoll-ID,
- Zeitpunkt in UTC,
- gemeinsames Vereinskonto als Akteur,
- Art der Operation,
- Typ und ID des betroffenen Datensatzes,
- vorherige Werte, soweit sinnvoll,
- neue Werte, soweit sinnvoll und
- technische Anfrage-ID zur Fehlersuche.

Da alle dasselbe Konto verwenden, liefert das Protokoll keine verlässliche
Zuordnung zu einem konkreten Verein. Es hilft dennoch bei der Rekonstruktion von
versehentlichen Änderungen.

## 14. Verhalten bei Netzwerk- und Serverfehlern

### 14.1 Grundsatz

Der Vereinsmodus ist onlineabhängig. Für die erste Ausbaustufe wird kein
vollständiger Offlinebetrieb mit späterer Synchronisation vorgesehen, da dies
eine zusätzliche Konflikt- und Synchronisationslogik erfordern würde.

### 14.2 Erwartetes Verhalten

- Kann der Server nicht erreicht werden, zeigt die Anwendung eine klare Meldung.
- Eingabefenster bleiben möglichst geöffnet, damit Werte nicht unnötig verloren
  gehen.
- Nach einem unklaren Verbindungsabbruch prüft der Client den aktuellen
  Serverstand, bevor dieselbe Änderung erneut gesendet wird.
- Schreiboperationen erhalten eine eindeutige Anfrage-ID, damit Wiederholungen
  nicht versehentlich doppelte Datensätze erzeugen.
- Lesende Ansichten dürfen einen zuvor geladenen Stand anzeigen, müssen ihn aber
  sichtbar als möglicherweise veraltet kennzeichnen.
- Die Anwendung darf bei einem Serverfehler niemals automatisch auf eine lokale
  SQLite-Datei ausweichen.

## 15. Strikte Trennung der Betriebsarten

Die Trennung ist eine zentrale Anforderung und wird auf mehreren Ebenen
abgesichert.

### 15.1 Keine fachliche Verbindung

- keine Synchronisationsfunktion,
- keine Vergleichsfunktion,
- kein Ergebnisimport,
- kein Ergebnisexport zwischen den Betriebsarten,
- keine gemeinsam verwendeten Datenbankdateien und
- keine Übernahme von IDs oder Datensätzen beim Moduswechsel.

### 15.2 Getrennte technische Komponenten

- Lokaler Datenservice verwendet ausschließlich lokale DAOs.
- Online-Datenservice verwendet ausschließlich den API-Client.
- Der API-Client kennt keinen lokalen Datenbankpfad.
- Lokale DAOs kennen keine Serveradresse und keine Online-Sitzung.
- Nur genau ein Datenservice ist zu einem Zeitpunkt aktiv.

### 15.3 Tests der Trennung

Automatisierte Tests sollen unter anderem belegen:

- Im lokalen Modus wird kein Netzwerkaufruf ausgelöst.
- Im Vereinsmodus wird keine fachliche SQLite-Verbindung geöffnet.
- Beim Moduswechsel werden alte Auswahlen verworfen.
- Eine lokale Änderung erzeugt keine Online-Operation.
- Eine Online-Änderung verändert keine lokale Datei.
- Ein fehlender Server führt nicht zum Fallback auf die lokale Datenbank.

## 16. Geplante Anpassung der Desktoparchitektur

### 16.1 Aktueller Stand

Der bestehende `Controller` erzeugt die einzelnen DAOs direkt. Die DAOs rufen
jeweils `DBController.getConnection()` auf. Damit ist der gesamte Datenzugriff
gegenwärtig fest an SQLite gebunden.

Vereinfacht:

```text
View -> Controller -> konkrete DAO -> DBController -> SQLite
```

### 16.2 Zielzustand

Zwischen Controller und konkretem Datenzugriff wird eine fachliche Schnittstelle
eingeführt:

```text
                           -> LokalerDatenService -> vorhandene DAOs -> SQLite
View -> Controller -> DatenService
                           -> OnlineDatenService  -> API-Client -> Server
```

Die Bezeichnungen sind vorläufig. Entscheidend ist die Verantwortlichkeit, nicht
der endgültige Klassenname.

### 16.3 Betriebsart

Eine zentrale Aufzählung soll den aktiven Modus abbilden:

```java
public enum Betriebsart {
    LOKAL,
    VEREIN
}
```

Eine Anwendungskonfiguration verwaltet den aktiven Modus, den lokalen Pfad und
die Serveradresse. Sie darf keine fachlichen Datensätze zwischen den Modi
transportieren.

### 16.4 Fachliche Datenservice-Schnittstelle

Der Datenservice bietet fachliche Operationen an. Eine grobe Skizze:

```java
public interface WettkampfDatenService {
    List<Saison> alleSaisons();
    List<Wettkampftage> wettkampftageVonSaison(String saisonId);
    List<Mannschaft> alleMannschaften();
    List<Schuetze> alleSchuetzen();
    void ergebnisSpeichern(Ergebnisse ergebnis);
    void ergebnisAktualisieren(Ergebnisse ergebnis);
}
```

Die tatsächliche Schnittstelle muss alle derzeit vom `Controller` benötigten
Operationen abdecken. Sie soll nach fachlichen Anwendungsfällen gegliedert und
nicht als generische SQL-Schnittstelle gestaltet werden.

### 16.5 Lokale Implementierung

Der lokale Datenservice delegiert zunächst an die bestehenden DAOs. Dadurch
kann das derzeitige SQLite-Verhalten schrittweise hinter die neue Schnittstelle
verschoben werden, ohne die Datenbankstruktur sofort zu verändern.

### 16.6 Online-Implementierung

Der Online-Datenservice:

- wandelt lokale Datenobjekte in das API-Datenformat um,
- sendet authentifizierte HTTPS-Anfragen,
- wandelt Antworten wieder in Datenobjekte um,
- übersetzt Serverfehler in verständliche Anwendungsfehler und
- erkennt abgelaufene Sitzungen und Versionskonflikte.

### 16.7 Fachlogik

Fachlogik soll möglichst nicht in JavaFX-Views liegen. Regeln, die für beide
Betriebsarten gelten, sollen gemeinsam genutzt werden. Regeln, die für die
Integrität der Online-Daten wichtig sind, müssen zusätzlich serverseitig geprüft
werden, da eine Prüfung allein im Desktopclient umgangen werden könnte.

## 17. Auswirkungen auf vorhandene Funktionen

### 17.1 Stammdaten und Ergebnisse

Die vorhandenen Masken sollen in beiden Betriebsarten funktionieren. Laden,
Speichern, Aktualisieren und Löschen werden über den aktiven Datenservice
ausgeführt.

### 17.2 Beameransicht

Im lokalen Modus bleibt die bisherige Funktionsweise bestehen. Im Vereinsmodus
muss entschieden werden, wie oft die Anzeige aktuelle Daten vom Server lädt.
Ein angemessenes Aktualisierungsintervall soll unnötige Serverlast vermeiden und
trotzdem zeitnahe Ergebnisse zeigen.

### 17.3 PDF-Erstellung

Die PDF-Erstellung darf nicht direkt auf lokale DAOs angewiesen bleiben, wenn
sie auch online funktionieren soll. Benötigte Daten müssen über den aktiven
Datenservice geladen und anschließend lokal als PDF gerendert werden.

### 17.4 Öffnen einer Datenbankdatei

`Datenbank öffnen ...` ist ausschließlich im lokalen Modus verfügbar. Im
Vereinsmodus darf dieser Menüpunkt nicht den aktiven Datenbestand wechseln.

### 17.5 Tabellenanlage und Migration

Das aktuelle `createTableIfNotExists()` betrifft ausschließlich lokale
SQLite-Dateien. Das Schema der Online-Datenbank wird serverseitig erstellt und
migriert. Ein Vereinsclient darf keine Tabellen auf dem Server anlegen oder
verändern.

## 18. Sicherheit

Trotz des Vertrauens zwischen den Vereinen sind grundlegende technische
Sicherheitsmaßnahmen notwendig, weil der Server aus dem Internet erreichbar
ist.

### 18.1 Transport und Erreichbarkeit

- ausschließlich HTTPS mit gültigem Zertifikat,
- keine öffentliche Freigabe des Datenbankports,
- API und Datenbank möglichst in getrennten Netzwerkrollen,
- aktuelle Server- und Laufzeitversionen,
- restriktive Firewallregeln und
- Begrenzung unangemessen großer Anfragen.

### 18.2 Anwendungssicherheit

- parametrisierte Datenbankabfragen,
- serverseitige Eingabevalidierung,
- sichere Passwort-Hashfunktion,
- begrenzte Anmeldeversuche,
- zeitlich begrenzte Sitzungen,
- keine Geheimnisse im Quellcode,
- keine sensiblen Werte in Logs und
- einheitliche Fehlerantworten ohne interne Details.

### 18.3 Datenschutz

In der Datenbank befinden sich Namen von Schützen und damit personenbezogene
Daten. Vor Inbetriebnahme sind mindestens zu klären:

- wer organisatorisch verantwortlich ist,
- welche Daten tatsächlich online benötigt werden,
- wie lange Daten gespeichert werden,
- wer das gemeinsame Passwort erhalten darf,
- wie Auskunft, Korrektur und Löschung organisatorisch gehandhabt werden,
- wo Server und Backups gespeichert werden und
- welcher Vertrag mit dem Hostinganbieter erforderlich ist.

Dieses Konzept ersetzt keine rechtliche Datenschutzprüfung.

## 19. Backups und Wiederherstellung

Da alle Vereinsbenutzer alle Daten ändern und löschen dürfen, sind zuverlässige
Backups wichtiger als eine feingranulare Rechteverwaltung.

### 19.1 Mindestanforderungen

- automatische tägliche Sicherung der Online-Datenbank,
- zusätzliche häufigere Sicherungen oder Wiederherstellungspunkte während der
  aktiven Wettkampfsaison,
- mehrere Aufbewahrungszeiträume, beispielsweise täglich, wöchentlich und
  monatlich,
- verschlüsselte Übertragung und Speicherung der Sicherungen,
- Speicherung mindestens einer Sicherung getrennt vom Produktivserver,
- regelmäßiger automatisierter Integritätstest und
- praktisch getestete Wiederherstellung.

Ein Backup gilt erst dann als belastbar, wenn eine Wiederherstellung erfolgreich
getestet wurde.

### 19.2 Wiederherstellung

Die Wiederherstellung erfolgt durch den technischen Betreiber, nicht durch die
Vereinsbenutzer in der Desktopanwendung. Vor einer Rücksicherung muss geklärt
werden, welche nach dem Sicherungszeitpunkt erfolgten Änderungen verloren gehen
würden.

Die lokale Sportleiter-Datenbank hat einen eigenen, vollständig unabhängigen
Sicherungsprozess.

## 20. Betrieb und Verantwortlichkeiten

Vor der Inbetriebnahme müssen Verantwortlichkeiten benannt werden:

- Wer bezahlt und verwaltet das Hosting?
- Wer besitzt die Domain?
- Wer erneuert Zertifikate, falls dies nicht automatisch geschieht?
- Wer darf das gemeinsame Passwort ändern?
- Wer verteilt das Passwort an die Vereine?
- Wer reagiert bei Ausfall oder Datenverlust?
- Wer spielt bei Bedarf ein Backup zurück?
- Wer installiert Serverupdates?
- Wer entscheidet über Schema- und Anwendungsmigrationen?

Auch ein technisch kleines System benötigt für diese Aufgaben mindestens eine
verantwortliche Person und eine Vertretung.

## 21. Versionierung und Kompatibilität

Desktopanwendung und Server können unterschiedliche Veröffentlichungszyklen
haben. Die API benötigt deshalb eine erkennbare Version, beispielsweise
`/api/v1/...`.

Beim Start des Vereinsmodus soll geprüft werden:

- Ist der Server erreichbar?
- Wird die API-Version vom Client unterstützt?
- Verlangt der Server eine neuere Clientversion?
- Ist die Anmeldung noch gültig?

Eine verständliche Meldung soll den Benutzer über ein notwendiges Update
informieren. Ein inkompatibler Client darf keine unvorhersehbaren Änderungen
ausführen.

## 22. Protokollierung und Diagnose

### 22.1 Client

Der Desktopclient darf für die Fehlersuche technische Ereignisse protokollieren,
beispielsweise:

- Zeitpunkt,
- aktiver Modus,
- Art der Operation,
- Serveradresse ohne Zugangsdaten,
- Anfrage-ID und
- Fehlerkategorie.

Passwörter, Sitzungstoken und vollständige personenbezogene Datensätze dürfen
nicht in Clientlogs erscheinen.

### 22.2 Server

Der Server protokolliert Betriebs- und Sicherheitsereignisse. Fachliche
Änderungen werden getrennt von technischen Fehlerlogs nachvollziehbar gehalten.
Aufbewahrungsdauer und Zugriff auf Logs müssen festgelegt werden.

## 23. Teststrategie

### 23.1 Lokaler Modus

- vorhandene DAO- und Fachlogiktests weiterverwenden,
- Öffnen vorhandener SQLite-Dateien testen,
- Anlegen neuer SQLite-Dateien testen,
- Migration älterer lokaler Datenbanken testen,
- vollständigen Betrieb ohne Netzwerk testen und
- sicherstellen, dass bestehende fachliche Ergebnisse unverändert bleiben.

### 23.2 Vereinsmodus

- Anmeldung mit richtigem und falschem Passwort,
- Ablauf und Erneuerung einer Sitzung,
- Lesen und Schreiben aller Entitäten,
- Validierung ungültiger Eingaben,
- Verhalten bei Serverausfall,
- Verhalten bei Verbindungsabbruch während des Speicherns,
- gleichzeitige Änderungen desselben Datensatzes,
- doppelte Wiederholung derselben Anfrage,
- Löschkonflikte durch abhängige Datensätze und
- Kompatibilitätsprüfung der API-Version.

### 23.3 Moduswechsel

- Wechsel von lokal zu online,
- Wechsel von online zu lokal,
- Abbruch eines Wechsels,
- Wechsel bei geöffnetem Bearbeitungsfenster,
- Wechsel bei ungespeicherten Eingaben,
- korrektes Neuladen sämtlicher Listen,
- korrekte Titel- und Statusanzeige und
- Nachweis, dass keine Daten zwischen den Modi übertragen werden.

### 23.4 Serverbetrieb

- automatisierte API-Tests,
- Datenbankmigrationen gegen eine Testdatenbank,
- Sicherungs- und Wiederherstellungstest,
- Belastungstest für die erwartete gleichzeitige Nutzung,
- Prüfung abgelaufener Zertifikate beziehungsweise Zertifikatsfehler und
- Sicherheitsprüfung der öffentlichen Endpunkte.

## 24. Schrittweiser Umsetzungsplan

Die Umsetzung soll in kleinen, überprüfbaren Schritten erfolgen.

### Phase 1: Betriebsart modellieren

- `Betriebsart` einführen.
- Einstellungen für Modus, lokalen Pfad und Serveradresse trennen.
- Auswahl beim ersten Start konzipieren.
- aktive Betriebsart im Fenstertitel und Hauptfenster anzeigen.
- bestehendes lokales Verhalten funktional unverändert lassen.

Ergebnis: Die Anwendung kennt beide Betriebsarten, aber der Vereinsmodus muss
noch nicht vollständig funktionsfähig sein.

### Phase 2: Lokalen Datenzugriff abstrahieren

- fachliche Datenservice-Schnittstellen aus den vorhandenen Controller-Aufrufen
  ableiten,
- lokale Implementierung über bestehende DAOs erstellen,
- `Controller` schrittweise von konkreten DAOs entkoppeln,
- vorhandene Funktionen und Berechnungen absichern und
- Tests für die strikte lokale Funktionsweise ergänzen.

Ergebnis: Die Oberfläche und der Controller sind nicht mehr unmittelbar an
SQLite gebunden; der lokale Betrieb arbeitet weiterhin wie bisher.

### Phase 3: Serverfachlichkeit lokal festlegen

- endgültiges Server-Datenmodell festlegen,
- API-Vertrag definieren,
- Fehlerformat definieren,
- Versions- und Konfliktmodell definieren,
- Authentifizierungsablauf des gemeinsamen Kontos definieren und
- PostgreSQL als Serverdatenbank lokal einrichten,
- lokale Docker-Compose-Entwicklungsumgebung vorbereiten und
- die technischen Rahmenbedingungen des ausgewählten Hetzner-Servers
  berücksichtigen.

Ergebnis: Client und Server können gegen einen dokumentierten Vertrag entwickelt
werden.

### Phase 4: Server und Online-Datenbank lokal entwickeln

- Serverprojekt aufsetzen,
- Datenbankschema und Migrationen erstellen,
- Anmeldung und Sitzungen umsetzen,
- fachliche API-Endpunkte umsetzen,
- Validierung und Transaktionen umsetzen,
- Änderungsprotokoll ergänzen und
- automatisierte Server- und API-Tests erstellen.

Ergebnis: Die Onlinefunktionen können unabhängig vom Desktopclient getestet
werden.

### Phase 5: Vereinsmodus im Desktopclient anbinden

- Online-Datenservice und API-Client implementieren,
- Anmeldedialog ergänzen,
- Serverfehler verständlich anzeigen,
- Konfliktdialoge ergänzen,
- Ansichten nach erfolgreichen Änderungen aktualisieren und
- Moduswechsel vollständig implementieren.

Ergebnis: Dieselbe Desktopanwendung kann lokal oder mit der gemeinsamen
Vereinsdatenbank arbeiten.

### Phase 6: Hetzner-Betrieb vorbereiten

- vorhandenen Hetzner-Server warten und für den zusätzlichen Stack vorbereiten,
- Wettkampf-Stack unter `/opt/wettkampf-server` bereitstellen,
- eigene Subdomain und HTTPS über den vorhandenen Nginx und Certbot einrichten,
- Monitoring und Backups einrichten,
- Wiederherstellung testen,
- gemeinsames Passwort sicher verteilen,
- kurze Vereinsanleitung erstellen und
- Pilotbetrieb mit wenigen Testdaten durchführen.

Ergebnis: Das System ist kontrolliert für den Vereinsbetrieb freigegeben.

### Phase 7: Einführung

- Online-Datenbestand initial anlegen,
- Vereine über Verantwortlichkeiten und Passwortschutz informieren,
- aktuelle Desktopversion verteilen,
- Supportweg bekanntgeben und
- nach der ersten Wettkampfphase Erfahrungen auswerten.

## 25. Abnahmekriterien

Das Vorhaben gilt fachlich erst dann als umgesetzt, wenn mindestens folgende
Punkte erfüllt sind:

- Die Anwendung bietet eine verständliche Auswahl zwischen lokalem und
  gemeinsamem Vereinsmodus.
- Der aktive Modus ist jederzeit eindeutig sichtbar.
- Vorhandene lokale SQLite-Datenbanken funktionieren weiterhin.
- Der lokale Modus funktioniert vollständig ohne Internetzugang.
- Der lokale Modus baut keine Verbindung zum Vereinsserver auf.
- Der Vereinsmodus arbeitet ausschließlich über die Server-API.
- Der Vereinsmodus öffnet keine lokale fachliche SQLite-Datei.
- Alle Benutzer des gemeinsamen Kontos dürfen alle Online-Daten bearbeiten.
- Ohne gültige Anmeldung sind keine Online-Daten zugänglich.
- Gleichzeitige Änderungen werden erkannt und nicht still überschrieben.
- Netzwerkfehler führen nicht zu unbemerkten Doppel- oder Teiländerungen.
- Online-Backups werden automatisch erzeugt.
- Eine Wiederherstellung aus einem Backup wurde erfolgreich getestet.
- Es existiert keine Synchronisations- oder Übertragungsfunktion zwischen den
  Datenbeständen.
- Ein Wechsel der Betriebsart verändert die lokale Datenbankdatei nicht.
- Die lokale Datenbank des Sportleiters bleibt der allein maßgebliche Bestand.

## 26. Noch zu treffende Entscheidungen

Vor Beginn der Serverimplementierung müssen folgende Punkte separat entschieden
werden:

1. Welche konkrete Java-Servertechnik soll verwendet werden?
2. Welche Subdomain wird für die Wettkampf-API verwendet?
3. Wo wird die externe Kopie der PostgreSQL-Sicherungen gespeichert?
4. Wer übernimmt Betrieb, Updates und Wiederherstellungen?
5. Wie wird das gemeinsame Passwort initial verteilt und später gewechselt?
6. Wie lange bleiben Sitzungen gültig?
7. Wie lange werden Online-Daten, Änderungsprotokolle und Backups aufbewahrt?
8. Welche Daten werden aus Datenschutzgründen tatsächlich online benötigt?
9. Soll die Beameransicht im Vereinsmodus automatisch aktualisiert werden?
10. Soll die PDF-Erstellung bereits in der ersten Onlineversion unterstützt
    werden?
11. Wie soll sich die Anwendung verhalten, wenn beim Moduswechsel ungespeicherte
    Eingaben vorhanden sind?

Diese offenen Punkte ändern nicht die Grundentscheidung zur vollständigen
Trennung zwischen lokaler Sportleiter-Datenbank und gemeinsamer
Vereinsdatenbank.

## 27. Zusammenfassung

Der Blasrohr-Wettkampf-Manager wird als eine umschaltbare Desktopanwendung
weitergeführt. Der lokale Modus verwendet weiterhin eigenständige
SQLite-Dateien. Die lokale Datenbank des Sportleiters bleibt offline,
unabhängig und fachlich maßgeblich.

Im Vereinsmodus greifen alle Vereine mit einem gemeinsamen Konto und einem
gemeinsamen Passwort über eine abgesicherte Server-API auf denselben
Online-Datenbestand zu. Innerhalb dieses Datenbestands gibt es keine
vereinsspezifischen Einschränkungen. Jeder angemeldete Benutzer darf alle Daten
lesen und bearbeiten.

Zwischen dem lokalen und dem gemeinsamen Datenbestand existiert bewusst keine
Synchronisation und kein digitaler Datenaustausch. Ergebnisse gelangen weiterhin
in Papierform zum Sportleiter und werden von ihm selbst in seine lokale
Datenbank eingetragen.
