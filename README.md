# Blasrohr-Wettkampf-Manager

## Voraussetzungen

- [Java 24](https://www.oracle.com/java/technologies/downloads/) muss installiert sein

## Projekt starten

```
.\mvnw.cmd javafx:run
```

> `mvn javafx:run` funktioniert nur wenn Maven manuell im PATH eingetragen ist.
> Der Wrapper `.\mvnw.cmd` funktioniert immer.

## Projektstruktur

```
src/main/java/io/github/manadhion/wettkampf/
    view/        → Java-Klassen (Controller, Main)
    
src/main/resources/
    view/        → FXML-Dateien (Oberfläche)

pom.xml          → Abhängigkeiten und Build-Konfiguration
module-info.java → Java-Moduldeklaration
```

## Neue Bibliothek einbinden

1. Dependency in `pom.xml` unter `<dependencies>` eintragen
2. `requires <modulname>;` in `module-info.java` ergänzen

## Entwicklungsnotizen

<!-- Hier eigene Notizen eintragen -->
