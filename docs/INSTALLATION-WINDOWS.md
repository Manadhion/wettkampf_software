# Installation unter Windows

Diese Anleitung gilt für den Blasrohr-Wettkampf-Manager ab Version 1.3.0.
Java muss nicht zusätzlich installiert werden.

## 1. Installer herunterladen

1. Die offizielle Release-Seite öffnen:
   <https://github.com/Manadhion/wettkampf_software/releases/latest>
2. Unter **Assets** auf
   `Blasrohr-Wettkampf-Manager-<version>.exe` klicken.
3. Falls Microsoft Edge vor einem selten heruntergeladenen Programm warnt, im
   Downloadfenster über die drei Punkte **Beibehalten** wählen. Je nach
   Edge-Version muss zusätzlich **Mehr anzeigen** und anschließend
   **Trotzdem beibehalten** gewählt werden.

Nur Installer aus dem oben genannten offiziellen GitHub-Repository verwenden.
Dateien aus E-Mails oder von anderen Downloadseiten nicht starten.

## 2. Windows-Schutzmeldung bestätigen

Der kostenlose Installer ist derzeit nicht digital signiert. Windows kann
deshalb beim ersten Start die Meldung **„Der Computer wurde durch Windows
geschützt“** anzeigen.

1. In der Meldung auf **Weitere Informationen** klicken.
2. Prüfen, dass als App der Blasrohr-Wettkampf-Manager genannt wird.
3. **Trotzdem ausführen** wählen.

Die Meldung bedeutet in diesem Fall nicht, dass Windows einen Virus gefunden
hat. Sie bedeutet, dass Windows den Herausgeber des unsignierten Installers
nicht über ein gekauftes oder anderweitig vertrauenswürdiges Zertifikat
bestätigen kann.

## 3. Programm installieren

1. Den Schritten des Installationsprogramms folgen.
2. Den vorgeschlagenen Zielordner übernehmen oder einen anderen Ordner wählen.
3. Optional die Desktopverknüpfung anlegen lassen.
4. Die Installation abschließen.

Es werden keine Administratorrechte benötigt. Danach lässt sich das Programm
über das Startmenü oder die Desktopverknüpfung öffnen.

## 4. Erster Start

- **Sportleiter – lokal:** benötigt kein Passwort. Eine vorhandene lokale
  Datenbank öffnen oder eine neue Datenbank anlegen.
- **Online-Datenbank:** Vereinsnamen und das vom Betreiber ausgegebene
  Online-Passwort eingeben. Beim ersten Online-Start ist eine
  Internetverbindung erforderlich.

Vereinsname und Passwort werden nach erfolgreicher Anmeldung auf diesem
Windows-Benutzerkonto gespeichert und beim nächsten Start vorausgefüllt. Das
Passwort ist dabei mit dem Windows-Benutzerkonto geschützt.

## Aktualisierung

Im Online-Betrieb prüft die Anwendung nach dem Start, ob auf GitHub eine neuere
Version verfügbar ist. Nach einer Bestätigung wird die offizielle
Installationsdatei im Browser geöffnet.

Vor einer Aktualisierung:

1. Warten, bis keine rote Offline-Warnung mehr angezeigt wird und alle Daten
   synchronisiert sind.
2. Das Programm schließen.
3. Den neuen Installer herunterladen und ausführen.

Die vorhandene Version wird aktualisiert. Gespeicherte Einstellungen und lokale
Datenbanken werden dabei nicht absichtlich gelöscht.

## Deinstallation

**Windows-Einstellungen → Apps → Installierte Apps →
Blasrohr-Wettkampf-Manager → Deinstallieren** öffnen.

Persönlich angelegte Datenbankdateien werden durch eine Deinstallation nicht
automatisch gelöscht.

## Optional: SHA-256-Prüfwert kontrollieren

Der Release-Hinweis nennt den SHA-256-Prüfwert des Installers. Zum Vergleich in
PowerShell in den Downloadordner wechseln und ausführen:

```powershell
Get-FileHash .\Blasrohr-Wettkampf-Manager-1.3.0.exe -Algorithm SHA256
```

Der angezeigte Wert muss exakt mit dem Wert im GitHub-Release übereinstimmen.
