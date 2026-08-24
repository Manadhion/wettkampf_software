#!/usr/bin/env bash
set -euo pipefail

backup_dir=/opt/wettkampf-server/backups
container=wettkampf-server-postgres-1
backup=${1:-$(find "$backup_dir" -maxdepth 1 -type f -name 'wettkampf-*.dump' \
    -printf '%T@ %p\n' | sort -nr | head -n1 | cut -d' ' -f2-)}

if [[ -z "$backup" || ! -f "$backup" ]]; then
    echo "Kein Backup zum Prüfen gefunden." >&2
    exit 2
fi
case "$(realpath "$backup")" in
    /opt/wettkampf-server/backups/wettkampf-*.dump) ;;
    *) echo "Backup liegt außerhalb des vorgesehenen Verzeichnisses." >&2; exit 90 ;;
esac

zeitpunkt=$(date -u +%Y%m%dT%H%M%SZ)
testdb="wettkampf_restore_test_$zeitpunkt"
testdb=${testdb//[^a-zA-Z0-9_]/}
cleanup() {
    docker exec "$container" sh -c \
        "dropdb -U \"\$POSTGRES_USER\" --if-exists '$testdb'" >/dev/null 2>&1 || true
}
trap cleanup EXIT

docker exec "$container" sh -c "createdb -U \"\$POSTGRES_USER\" '$testdb'"
docker exec -i "$container" sh -c \
    "pg_restore -U \"\$POSTGRES_USER\" -d '$testdb' --no-owner --no-privileges" < "$backup"
docker exec "$container" sh -c \
    "psql -U \"\$POSTGRES_USER\" -d '$testdb' -v ON_ERROR_STOP=1 -c \
    \"SELECT (SELECT count(*) FROM saison) saisons,
             (SELECT count(*) FROM mannschaft) mannschaften,
             (SELECT count(*) FROM schuetze) schuetzen,
             (SELECT count(*) FROM ergebnis) ergebnisse;\""
printf 'Restore-Test erfolgreich: %s\n' "$backup"
