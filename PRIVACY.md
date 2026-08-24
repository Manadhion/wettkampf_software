# Datenschutzinformation

## Überblick

Der Blasrohr-Wettkampf-Manager enthält keine Werbung und keine Telemetrie. Die
Anwendung verkauft oder übermittelt keine Daten an Werbe- oder Analysedienste.

## Lokaler Sportleiterbetrieb

Im lokalen Sportleiterbetrieb werden Wettkampfdaten in der vom Benutzer
gewählten SQLite-Datei gespeichert. Erzeugte PDF-Dateien werden ausschließlich
an dem vom Benutzer gewählten lokalen Speicherort abgelegt.

## Online-Betrieb

Der Online-Betrieb wird vom Benutzer ausdrücklich ausgewählt. Dabei verbindet
sich die Anwendung verschlüsselt über HTTPS mit
`https://wettkampf-api.meshalchemy.com`. Übertragen werden:

- der eingegebene Kontoname und das Passwort zur Anmeldung,
- die für den Wettkampfbetrieb benötigten Vereins-, Mannschafts-, Schützen-,
  Saison-, Begegnungs- und Ergebnisdaten,
- technische Sitzungsinformationen zur Authentisierung und Synchronisation.

Das Passwort wird lokal nur nach ausdrücklicher Zustimmung gespeichert und
unter Windows mit DPAPI verschlüsselt. Auf dem Server wird kein Klartextpasswort
gespeichert.

## Updateprüfung

Nach erfolgreichem Start im Online-Betrieb fragt die Anwendung einmalig die
öffentliche GitHub-Release-API für dieses Projekt ab. Dabei werden keine
Wettkampf- oder Zugangsdaten an GitHub übertragen. Wie bei jedem HTTPS-Aufruf
erhält GitHub technisch unter anderem die öffentliche IP-Adresse, den Zeitpunkt
und eine Programmkennung. Ist eine neuere Version verfügbar, entscheidet der
Benutzer selbst, ob der Download im Standardbrowser geöffnet wird.

## Verantwortlichkeit

Die Vereine und der Betreiber des Online-Servers sind für die rechtmäßige
Erfassung, Verwendung, Sicherung und Löschung der von ihnen verwalteten
Wettkampfdaten verantwortlich.
