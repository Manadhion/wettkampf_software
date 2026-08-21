# Arbeitsanweisungen für Codex

## Projekt

- Windows-Desktopanwendung zur Verwaltung von Blasrohr-Rundenwettkämpfen.
- Java 24, JavaFX 21, Maven Wrapper, SQLite und OpenPDF.
- Einstiegspunkt: `io.github.manadhion.wettkampf.view.Main`.
- Schichten: `view` -> `app` -> `dao` -> SQLite; Views greifen nicht direkt auf DAOs oder die Datenbank zu.

## Wichtige Befehle (PowerShell)

- Bauen und prüfen: `.\mvnw.cmd verify`
- Anwendung starten: `.\mvnw.cmd javafx:run`
- Javadoc erzeugen: `.\mvnw.cmd javadoc:javadoc`
- Portables App-Verzeichnis bauen: `powershell -ExecutionPolicy Bypass -File packaging\build-exe.ps1 -Type app-image`

## Arbeitsweise

- Bestehende deutsche Bezeichnungen und die aktuelle Paketstruktur beibehalten.
- Neue Abhängigkeiten nur nach ausdrücklicher Zustimmung hinzufügen.
- Datenbankdateien (`*.db`), erzeugte Dateien in `target/` oder `dist/` und fachliche PDF-Dokumente nicht verändern, sofern die Aufgabe dies nicht ausdrücklich verlangt.
- Änderungen klein und aufgabenbezogen halten; fremde uncommittete Änderungen bewahren.
- Fachlogik möglichst außerhalb der JavaFX-Views halten.
- Bei einer Verhaltensänderung nach Möglichkeit einen automatisierten Test unter `src/test/java` ergänzen. Für reine UI-Änderungen mindestens den Maven-Build ausführen.
- Vor Abschluss mindestens `.\mvnw.cmd verify` ausführen und das Ergebnis nennen. GUI-Verhalten, das nicht automatisiert geprüft wurde, ausdrücklich als manuell zu prüfen kennzeichnen.
