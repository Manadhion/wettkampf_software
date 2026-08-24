# Code signing policy

## Projekt und Geltungsbereich

Diese Richtlinie gilt für den Blasrohr-Wettkampf-Manager und das öffentliche
Repository <https://github.com/Manadhion/wettkampf_software>. Signiert werden
ausschließlich Windows-Installer, die aus dem Quellcode dieses Repositorys
erzeugt wurden.

Free code signing provided by SignPath.io, certificate by SignPath Foundation.

## Verantwortlichkeiten

- Committer und Reviewer: [Benjamin Schneider / Manadhion](https://github.com/Manadhion)
- Freigabeberechtigter für Signaturanfragen: [Benjamin Schneider / Manadhion](https://github.com/Manadhion)

Beiträge von Personen ohne direkten Schreibzugriff werden vor der Übernahme
geprüft. Änderungen an Build-, Release- und Signaturabläufen werden besonders
sorgfältig kontrolliert. Für GitHub und SignPath wird Mehrfaktor-Authentisierung
verwendet.

## Build- und Freigabeprozess

1. Die vollständigen automatisierten Tests müssen erfolgreich sein.
2. Der Windows-Installer wird auf einem von GitHub gehosteten Windows-Runner mit
   dem eingecheckten Skript `packaging/build-exe.ps1` erzeugt.
3. Projektversion, Git-Tag und Installer-Version müssen übereinstimmen.
4. Das von GitHub Actions gespeicherte Artefakt wird nach der Aufnahme des
   Projekts bei SignPath über dessen GitHub-Integration eingereicht.
5. Jede Release-Signatur wird manuell durch den Freigabeberechtigten bestätigt.
6. Nur das von SignPath zurückgelieferte Artefakt wird als GitHub-Release
   veröffentlicht.

Private Schlüssel werden weder lokal gespeichert noch in GitHub hinterlegt.
SignPath verwaltet den Schlüssel in seiner gesicherten Signaturumgebung.

## Datenschutz und Sicherheit

Die Anwendung enthält keine Werbung, Telemetrie oder verdeckte Datenerfassung.
Welche Netzwerkverbindungen im Online-Betrieb und bei der Updateprüfung
entstehen, ist in der [Datenschutzinformation](PRIVACY.md) beschrieben.

Sicherheitsprobleme sollen nicht öffentlich mit Zugangsdaten oder Echtdaten in
einem Issue veröffentlicht werden. Stattdessen kann der Betreiber über die im
GitHub-Profil angegebene Kontaktmöglichkeit benachrichtigt werden.
