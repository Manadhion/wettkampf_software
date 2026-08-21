"""Erzeugt ein transaktionales PostgreSQL-Importscript aus der lokalen SQLite-Datenbank."""

from __future__ import annotations

import argparse
import sqlite3
import sys
from pathlib import Path


TABELLEN = (
    "saison",
    "liga",
    "altersklasse",
    "mannschaft",
    "schuetze",
    "wettkampftag",
    "begegnung",
    "saison_schuetze",
    "ergebnis",
)


def sql_wert(wert: object) -> str:
    if wert is None:
        return "NULL"
    if isinstance(wert, bool):
        return "TRUE" if wert else "FALSE"
    if isinstance(wert, (int, float)):
        return str(wert)
    text = str(wert)
    if "\0" in text:
        raise ValueError("Textwerte mit Nullzeichen koennen nicht importiert werden.")
    return "'" + text.replace("'", "''") + "'"


def zeilen(db: sqlite3.Connection) -> dict[str, list[tuple[object, ...]]]:
    return {
        "saison": list(db.execute("SELECT id,name,0 FROM saison ORDER BY id")),
        "liga": list(db.execute("SELECT id,name,rangfolge FROM liga ORDER BY id")),
        "altersklasse": list(db.execute("SELECT id,name FROM altersklasse ORDER BY id")),
        "mannschaft": list(db.execute("SELECT id,name,klasse FROM mannschaft ORDER BY id")),
        "schuetze": list(db.execute(
            "SELECT id,vorname,nachname,mannschaftid,altersKlasse FROM schuetze ORDER BY id"
        )),
        "wettkampftag": list(db.execute(
            "SELECT id,datum,ausrichterverein,saisonID FROM wettkampftage ORDER BY id"
        )),
        "begegnung": list(db.execute("""
            SELECT b.id,b.heim,b.gegner,b.wettkampftag,b.liga,
                   COALESCE(b.ligaName,l.name),
                   COALESCE(b.heimName,h.name),COALESCE(b.gegnerName,g.name)
            FROM begegnung b
            JOIN mannschaft h ON h.id=b.heim
            JOIN mannschaft g ON g.id=b.gegner
            LEFT JOIN liga l ON l.id=b.liga
            ORDER BY b.id
        """)),
        "saison_schuetze": list(db.execute("""
            SELECT saisonID,schuetzeID,vorname,nachname,mannschaftID,mannschaftName,
                   altersklasseID,altersklasseName
            FROM saison_schuetze ORDER BY saisonID,schuetzeID
        """)),
        "ergebnis": list(db.execute(
            "SELECT id,schuetzeID,wettkampftagID,ergebnis FROM ergebnisse ORDER BY id"
        )),
    }


SPALTEN = {
    "saison": "id,name,version",
    "liga": "id,name,rangfolge",
    "altersklasse": "id,name",
    "mannschaft": "id,name,klasse",
    "schuetze": "id,vorname,nachname,mannschaft_id,altersklasse_id",
    "wettkampftag": "id,datum,ausrichterverein,saison_id",
    "begegnung": "id,heim,gegner,wettkampftag_id,liga,liga_name,heim_name,gegner_name",
    "saison_schuetze": (
        "saison_id,schuetze_id,vorname,nachname,mannschaft_id,mannschaft_name,"
        "altersklasse_id,altersklasse_name"
    ),
    "ergebnis": "id,schuetze_id,wettkampftag_id,wert",
}


def sql_ausgeben(daten: dict[str, list[tuple[object, ...]]]) -> None:
    print("\\set ON_ERROR_STOP on")
    print("BEGIN;")
    print("TRUNCATE TABLE ergebnis,saison_schuetze,begegnung,wettkampftag,"
          "schuetze,mannschaft,altersklasse,liga,saison;")
    for tabelle in TABELLEN:
        for zeile in daten[tabelle]:
            werte = ",".join(sql_wert(wert) for wert in zeile)
            print(f"INSERT INTO {tabelle} ({SPALTEN[tabelle]}) VALUES ({werte});")
    print("DO $$")
    print("BEGIN")
    for tabelle in TABELLEN:
        anzahl = len(daten[tabelle])
        print(f"  IF (SELECT COUNT(*) FROM {tabelle}) <> {anzahl} THEN")
        print(f"    RAISE EXCEPTION 'Anzahl fuer {tabelle} stimmt nicht';")
        print("  END IF;")
    print("END $$;")
    print("COMMIT;")


def main() -> None:
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8", newline="\n")
    parser = argparse.ArgumentParser()
    parser.add_argument("datenbank", type=Path)
    parser.add_argument("--pruefen", action="store_true",
                        help="Nur Tabellen lesen und Zeilenzahlen ausgeben")
    args = parser.parse_args()
    if not args.datenbank.is_file():
        parser.error(f"SQLite-Datei nicht gefunden: {args.datenbank}")

    try:
        with sqlite3.connect(f"file:{args.datenbank.resolve()}?mode=ro", uri=True) as db:
            daten = zeilen(db)
    except sqlite3.Error as fehler:
        raise SystemExit(f"SQLite-Datenbank konnte nicht gelesen werden: {fehler}") from fehler

    if args.pruefen:
        for tabelle in TABELLEN:
            print(f"{tabelle}={len(daten[tabelle])}")
        texte = (str(wert) for tabelle in TABELLEN for zeile in daten[tabelle]
                 for wert in zeile if isinstance(wert, str))
        sonderzeichen = set("äöüÄÖÜß")
        anzahl = sum(zeichen in sonderzeichen for text in texte for zeichen in text)
        print(f"umlaute_und_sz={anzahl}")
        return
    sql_ausgeben(daten)


if __name__ == "__main__":
    try:
        main()
    except (ValueError, BrokenPipeError) as fehler:
        print(str(fehler), file=sys.stderr)
        raise SystemExit(1) from fehler
