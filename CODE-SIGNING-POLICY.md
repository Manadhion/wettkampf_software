# Hinweise zur Codesignatur

## Projekt und Geltungsbereich

Diese Hinweise gelten für den Blasrohr-Wettkampf-Manager und das öffentliche
Repository <https://github.com/Manadhion/wettkampf_software>.

Die derzeit auf GitHub veröffentlichten Windows-Installer sind **nicht digital
signiert**. Deshalb kann Windows beim Herunterladen oder beim ersten Start eine
SmartScreen-Warnung anzeigen. Eine selbst signierte Datei würde diese Warnung
auf fremden Rechnern nicht zuverlässig verhindern und wird daher nicht als
vermeintliche Vertrauenslösung eingesetzt.

## Verantwortlichkeiten

- Committer und Reviewer: [Benjamin Schneider / Manadhion](https://github.com/Manadhion)
- Freigabeberechtigter für Veröffentlichungen: [Benjamin Schneider / Manadhion](https://github.com/Manadhion)

Beiträge von Personen ohne direkten Schreibzugriff werden vor der Übernahme
geprüft. Änderungen an Build- und Releaseabläufen werden besonders sorgfältig
kontrolliert. Für GitHub wird Mehrfaktor-Authentisierung verwendet.

## Build- und Freigabeprozess

1. Die vollständigen automatisierten Tests müssen erfolgreich sein.
2. Der Windows-Installer wird auf einem von GitHub gehosteten Windows-Runner mit
   dem eingecheckten Skript `packaging/build-exe.ps1` erzeugt.
3. Projektversion, Git-Tag und Installer-Version müssen übereinstimmen.
4. Der erfolgreiche GitHub-Actions-Build wird kontrolliert. Anschließend wird
   dessen Artefakt bewusst als unsignierter Installer im GitHub-Release
   veröffentlicht.
5. Der SHA-256-Prüfwert des veröffentlichten Installers wird in den
   Release-Hinweisen angegeben.

Falls später eine vertrauenswürdige Codesignatur eingeführt wird, werden diese
Hinweise und der Buildprozess vor der ersten signierten Veröffentlichung
aktualisiert. Bereits veröffentlichte Dateien werden nicht nachträglich ersetzt.

## Datenschutz und Sicherheit

Die Anwendung enthält keine Werbung, Telemetrie oder verdeckte Datenerfassung.
Welche Netzwerkverbindungen im Online-Betrieb und bei der Updateprüfung
entstehen, ist in der [Datenschutzinformation](PRIVACY.md) beschrieben.

Sicherheitsprobleme sollen nicht öffentlich mit Zugangsdaten oder Echtdaten in
einem Issue veröffentlicht werden. Stattdessen kann der Betreiber über die im
GitHub-Profil angegebene Kontaktmöglichkeit benachrichtigt werden.
