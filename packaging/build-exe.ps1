<#
    Baut aus dem Maven-Projekt eine eigenstaendige Windows-Anwendung mit
    eingebettetem Java. Auf dem Zielrechner (Verein) muss dann KEIN Java
    installiert sein.

    Aufruf (im Projektordner):
        powershell -ExecutionPolicy Bypass -File packaging\build-exe.ps1

    Ergebnis:
        dist\Blasrohr-Wettkampf-Manager-<version>.exe   (Installer, Standard)

    Portablen Ordner statt Installer bauen (ohne WiX, ohne Installation):
        powershell -ExecutionPolicy Bypass -File packaging\build-exe.ps1 -Type app-image
    Ergebnis dann:
        dist\Blasrohr-Wettkampf-Manager\Blasrohr-Wettkampf-Manager.exe

    Voraussetzungen:
        - JDK 24 (JAVA_HOME gesetzt oder java im PATH) -- liefert jpackage/jlink.
        - Nur fuer den Installer: WiX-Toolset 3.14 (candle.exe/light.exe).
          Wird automatisch unter %LOCALAPPDATA%\WiX314 oder im PATH gesucht.
          Portable Binaries: https://github.com/wixtoolset/wix3/releases
          (Datei wix314-binaries.zip nach %LOCALAPPDATA%\WiX314 entpacken.)
#>
param(
    # Installer ("exe") oder portabler Ordner ("app-image")
    [ValidateSet("exe", "msi", "app-image")]
    [string]$Type = "exe",
    # Versionsnummer der Anwendung (muss mit einer Ziffer beginnen)
    [string]$Version = "1.0"
)

$ErrorActionPreference = "Stop"

# feste Eckdaten der Anwendung
$AppName    = "Blasrohr-Wettkampf-Manager"
$Vendor     = "Benjamin Schneider"
$MainModule = "io.github.manadhion/io.github.manadhion.wettkampf.view.Main"
# feste Kennung, damit neuere Installer die alte Version ersetzen statt daneben zu installieren
$UpgradeUuid = "63b98ea8-95aa-4de9-ba2a-894b7b3a14ce"

# Projektwurzel = ein Ordner ueber diesem Skript
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root

# --- JDK-Werkzeuge finden (jpackage, jlink) ---------------------------------
$javaBin = $null
if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME "bin\jpackage.exe"))) {
    $javaBin = Join-Path $env:JAVA_HOME "bin"
} else {
    $javaCmd = Get-Command java -ErrorAction SilentlyContinue
    if ($javaCmd) {
        $cand = Join-Path (Split-Path -Parent $javaCmd.Source) "jpackage.exe"
        if (Test-Path $cand) { $javaBin = Split-Path -Parent $javaCmd.Source }
    }
}
if (-not $javaBin) {
    throw "jpackage nicht gefunden. Bitte JDK 24 installieren und JAVA_HOME setzen."
}
$jpackage = Join-Path $javaBin "jpackage.exe"
$jlink    = Join-Path $javaBin "jlink.exe"
Write-Host "JDK-Werkzeuge: $javaBin" -ForegroundColor Cyan

# --- WiX fuer Installer-Typen bereitstellen ---------------------------------
if ($Type -ne "app-image") {
    $wixDir = Join-Path $env:LOCALAPPDATA "WiX314"
    if (Test-Path (Join-Path $wixDir "candle.exe")) {
        $env:PATH = "$wixDir;$env:PATH"
    } elseif (-not (Get-Command candle.exe -ErrorAction SilentlyContinue)) {
        throw @"
WiX-Toolset (candle.exe/light.exe) nicht gefunden -- fuer den Installer noetig.
Portable Binaries holen und entpacken:
  1. https://github.com/wixtoolset/wix3/releases/download/wix3141rtm/wix314-binaries.zip
  2. Inhalt nach $wixDir entpacken
Oder ohne Installer bauen:  build-exe.ps1 -Type app-image
"@
    }
    Write-Host "WiX gefunden." -ForegroundColor Cyan
}

# --- 1) App-Jar bauen -------------------------------------------------------
Write-Host "`n[1/4] Maven-Build ..." -ForegroundColor Green
& "$Root\mvnw.cmd" -q clean package -DskipTests
if ($LASTEXITCODE -ne 0) { throw "Maven-Build fehlgeschlagen." }

# --- 2) Modulpfad zusammenstellen (target\lib) ------------------------------
Write-Host "[2/4] Abhaengigkeiten einsammeln ..." -ForegroundColor Green
$lib = Join-Path $Root "target\lib"
if (Test-Path $lib) { Remove-Item $lib -Recurse -Force }
& "$Root\mvnw.cmd" -q dependency:copy-dependencies "-DoutputDirectory=$lib" -DincludeScope=runtime
if ($LASTEXITCODE -ne 0) { throw "Abhaengigkeiten konnten nicht kopiert werden." }

# leere JavaFX-Aggregator-Jars entfernen -- die echten Klassen und die
# Windows-Natives stecken in den "-win"-Jars; beide zugleich auf dem Modulpfad
# wuerden als doppeltes Modul kollidieren.
Get-ChildItem (Join-Path $lib "javafx-*.jar") |
    Where-Object { $_.Name -notmatch '-win\.jar$' } |
    Remove-Item -Force

# die gebaute Anwendung selbst dazulegen
$appJar = Get-ChildItem (Join-Path $Root "target\*.jar") |
    Where-Object { $_.Name -notlike "*-sources.jar" -and $_.Name -notlike "*-javadoc.jar" } |
    Select-Object -First 1
if (-not $appJar) { throw "Anwendungs-Jar in target\ nicht gefunden." }
Copy-Item $appJar.FullName $lib

# --- 3) Schlanke Java-Laufzeit erzeugen (jlink) -----------------------------
# JavaFX kommt als Module zur Laufzeit dazu (siehe target\lib); die Laufzeit
# enthaelt nur die benoetigten JDK-Module.
Write-Host "[3/4] Java-Laufzeit mit jlink erzeugen ..." -ForegroundColor Green
$runtime = Join-Path $Root "target\runtime"
if (Test-Path $runtime) { Remove-Item $runtime -Recurse -Force }
& $jlink `
    --add-modules java.base,java.desktop,java.sql,java.sql.rowset,java.prefs,java.xml,java.scripting,java.logging,java.naming,jdk.unsupported `
    --no-header-files --no-man-pages --strip-debug --compress=zip-6 `
    --output $runtime
if ($LASTEXITCODE -ne 0) { throw "jlink fehlgeschlagen." }

# --- 4) Verpacken (jpackage) ------------------------------------------------
Write-Host "[4/4] Mit jpackage verpacken ($Type) ..." -ForegroundColor Green
$dist = Join-Path $Root "dist"
if ($Type -eq "app-image") {
    # vorhandenen portablen Ordner vorher raeumen
    $portable = Join-Path $dist $AppName
    if (Test-Path $portable) { Remove-Item $portable -Recurse -Force }
}
New-Item -ItemType Directory -Force -Path $dist | Out-Null

$icon = Join-Path $PSScriptRoot "icon.ico"
$jpArgs = @(
    "--type", $Type,
    "--name", $AppName,
    "--app-version", $Version,
    "--vendor", $Vendor,
    "--copyright", "(c) $Vendor",
    "--description", "Verwaltung von Blasrohr-Rundenwettkaempfen",
    "--module-path", $lib,
    "--module", $MainModule,
    "--runtime-image", $runtime,
    "--dest", $dist
)
if (Test-Path $icon) { $jpArgs += @("--icon", $icon) }

# Zusatzoptionen fuer die Installer-Varianten
if ($Type -ne "app-image") {
    $jpArgs += @(
        "--win-upgrade-uuid", $UpgradeUuid,
        "--win-menu",                 # Startmenue-Eintrag
        "--win-menu-group", $AppName,
        "--win-shortcut",             # Desktop-Verknuepfung
        "--win-shortcut-prompt",      # Nutzer darf Desktop-Verknuepfung abwaehlen
        "--win-dir-chooser",          # Zielordner waehlbar
        "--win-per-user-install"      # Installation ohne Administratorrechte
    )
    if (Test-Path (Join-Path $Root "LICENSE")) {
        $jpArgs += @("--license-file", (Join-Path $Root "LICENSE"))
    }
}

& $jpackage @jpArgs
if ($LASTEXITCODE -ne 0) { throw "jpackage fehlgeschlagen." }

Write-Host "`nFertig. Ergebnis im Ordner: $dist" -ForegroundColor Green
Get-ChildItem $dist | Select-Object Name, Length, LastWriteTime | Format-Table -AutoSize
