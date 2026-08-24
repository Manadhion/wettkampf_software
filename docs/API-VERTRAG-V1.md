# Wettkampf-API v1

## 1. Grundregeln

- Basis-Pfad: `/api/v1`
- Transport im Produktivbetrieb ausschließlich über HTTPS.
- Anfragen und Antworten verwenden `application/json; charset=UTF-8`.
- IDs werden als UUID-Zeichenketten übertragen.
- Der Desktopclient sendet keine SQL-Anweisungen und erhält keinen
  Datenbankzugang.
- Online- und Sportleiter-Datenbestand werden nicht synchronisiert.

## 2. Status

```http
GET /api/v1/status
```

Erfolgreiche Antwort:

```json
{
  "status": "bereit",
  "apiVersion": "v1"
}
```

## 3. Anmeldung und Sitzung

Die Anmeldung gehört ausschließlich zum Online-Betreiberzugang. Der lokale
Sportleiterbetrieb verwendet diese API nicht und benötigt kein Passwort.

### 3.1 Anmeldung

```http
POST /api/v1/anmeldung
```

```json
{
  "name": "vereine",
  "passwort": "..."
}
```

Antwort `200 OK`:

```json
{
  "token": "zufaelliges-kurzlebiges-sitzungstoken",
  "laeuftAb": "2026-08-22T03:00:00Z"
}
```

Das Passwort wird serverseitig als gesalzener BCrypt-Hash gespeichert. Von den
Sitzungstoken wird nur ein SHA-256-Hash gespeichert. Fachliche Endpunkte
erwarten das Token anschließend im Header:

```http
Authorization: Bearer <token>
```

### 3.2 Abmeldung

```http
POST /api/v1/abmeldung
Authorization: Bearer <token>
```

Ergebnis: `204 No Content`. Die Sitzung wird serverseitig widerrufen und kann
danach nicht erneut verwendet werden.

## 4. Saisons

### 4.1 Alle Saisons

```http
GET /api/v1/saisons
```

Antwort `200 OK`:

```json
[
  {
    "id": "5a5ef80f-c474-4593-a063-eb955ab1ce3b",
    "name": 2627,
    "version": 0
  }
]
```

### 4.2 Saison anlegen

```http
POST /api/v1/saisons
```

```json
{
  "name": 2627
}
```

Ergebnis: `201 Created`, `Location` verweist auf die neue Ressource. Die ID und
Version werden serverseitig erzeugt.

### 4.3 Saison ändern

```http
PUT /api/v1/saisons/{id}
```

```json
{
  "name": 2728,
  "version": 0
}
```

Die Version muss dem zuletzt gelesenen Serverstand entsprechen. Bei Erfolg
liefert der Server die geänderte Saison mit erhöhter Version. Ein veralteter
Stand ergibt `409 Conflict`.

### 4.4 Saison löschen

```http
DELETE /api/v1/saisons/{id}?version=1
```

Ergebnis: `204 No Content`. Eine unbekannte Saison ergibt `404 Not Found`, ein
Versionskonflikt `409 Conflict`.

## 5. Einheitliches Fehlerformat

```json
{
  "code": "VERSION_KONFLIKT",
  "nachricht": "Die Saison wurde zwischenzeitlich geändert.",
  "zeitpunkt": "2026-08-21T15:00:00Z"
}
```

Vorgesehene Codes der ersten Ausbaustufe:

| HTTP | Code | Bedeutung |
|---:|---|---|
| 400 | `UNGUELTIGE_ANFRAGE` | JSON oder Eingabewerte sind ungültig |
| 401 | `ANMELDUNG_FEHLGESCHLAGEN` | Name oder Passwort falsch |
| 401 | `NICHT_ANGEMELDET` | Sitzung fehlt, ist abgelaufen oder wurde widerrufen |
| 404 | `NICHT_GEFUNDEN` | Ressource existiert nicht |
| 409 | `VERSION_KONFLIKT` | gelesene Version ist veraltet |
| 409 | `DATENKONFLIKT` | Eindeutigkeits- oder Abhängigkeitsregel verletzt |
| 500 | `INTERNER_FEHLER` | unerwarteter Serverfehler |

## 6. Weitere Fachdaten-Endpunkte

Alle folgenden Endpunkte setzen `Authorization: Bearer <token>` voraus:

| Bereich | Lesen | Anlegen | Ändern | Löschen |
|---|---|---|---|---|
| Ligen | `GET /ligen` | `POST /ligen` | `PUT /ligen/{id}` | `DELETE /ligen/{id}` |
| Altersklassen | `GET /altersklassen` | `POST /altersklassen` | `PUT /altersklassen/{id}` | `DELETE /altersklassen/{id}` |
| Mannschaften | `GET /mannschaften` | `POST /mannschaften` | `PUT /mannschaften/{id}` | `DELETE /mannschaften/{id}` |
| Schützen | `GET /schuetzen?mannschaftId=…` | `POST /schuetzen` | `PUT /schuetzen/{id}` | `DELETE /schuetzen/{id}` |
| Wettkampftage | `GET /wettkampftage` | `POST /wettkampftage` | `PUT /wettkampftage/{id}` | `DELETE /wettkampftage/{id}` |
| Begegnungen | `GET /begegnungen?wettkampftagId=…` | `POST /begegnungen` | – | `DELETE /begegnungen/{id}` |

Ergebnisse werden mit `GET /ergebnisse?schuetzeId=…&wettkampftagId=…`
gelesen und mit `PUT /ergebnisse` angelegt oder korrigiert. Das Mannschaftsergebnis
der besten drei Schützen liefert `GET /ergebnisse/gesamt`.

Historische Saisonmeldungen stehen unter `/saison-schuetzen`. Listen können
zusätzlich nach Saison gefiltert werden, beispielsweise mit
`GET /mannschaften?saisonId=…` und `GET /ligen?saisonId=…`.

## 7. Flüchtiger Offline-Notbetrieb

```http
GET /api/v1/snapshot
PUT /api/v1/snapshot
Authorization: Bearer <token>
```

`GET` liefert den vollständigen Online-Fachdatenbestand. Der Desktop hält ihn nur
für die laufende Sitzung im Arbeitsspeicher. `PUT` ersetzt alle Fachdaten in einer
einzigen Datenbanktransaktion; Konto und Sitzungen werden nicht verändert. Der
Endpunkt setzt fachlich voraus, dass während eines Wettkampfs nur ein Verein
schreibend arbeitet. Die lokale Sportleiter-Datenbank ist daran nicht beteiligt.

Jeder Snapshot enthält zusätzlich die globale Zahl `revision`. `PUT` wird nur
ausgeführt, wenn diese Revision noch dem Serverstand entspricht. Bei Erfolg
liefert der Server die erhöhte Revision zurück:

```json
{
  "revision": 42
}
```

Hat eine andere Sitzung den Datenbestand seit dem Laden verändert, antwortet
der Server mit `409 VERSION_KONFLIKT`. Dabei werden keine Fachdaten gelöscht
oder überschrieben. Der Client behält seine noch nicht synchronisierten Daten
im Arbeitsspeicher und weist auf den Konflikt hin.

## 8. Datenregeln

- Wettkampfdaten sind eindeutig.
- Eine Mannschaft kann nicht gegen sich selbst antreten.
- Eine Paarung darf an einem Wettkampftag auch mit vertauschter Heim-/Gastfolge
  nur einmal vorkommen.
- Ergebnisse liegen zwischen 0 und 600.
- Bereits verwendete Stammdaten können nicht gelöscht werden.
- Beim ersten Saisonergebnis wird die damalige Mannschafts- und
  Altersklassenzuordnung des Schützen historisch festgehalten.

Ein serverseitiges Änderungsprotokoll ist noch nicht Bestandteil von API v1.
