#!/usr/bin/env bash
set -euo pipefail

backup_dir=/opt/wettkampf-server/backups
container=wettkampf-server-postgres-1
aufbewahrung_tage=30

aufgeloest=$(realpath -m "$backup_dir")
if [[ "$aufgeloest" != /opt/wettkampf-server/backups ]]; then
    echo "Unerwartetes Backupverzeichnis: $aufgeloest" >&2
    exit 90
fi

install -d -m 700 "$backup_dir"
umask 077
zeitpunkt=$(date -u +%Y%m%dT%H%M%SZ)
ziel="$backup_dir/wettkampf-$zeitpunkt.dump"
temporaer="$backup_dir/.wettkampf-$zeitpunkt.dump.tmp"
trap 'rm -f -- "$temporaer"' EXIT

docker exec "$container" sh -c \
    'exec pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Fc' > "$temporaer"
test -s "$temporaer"
docker exec -i "$container" sh -c \
    'exec pg_restore -U "$POSTGRES_USER" --list' < "$temporaer" >/dev/null
mv -- "$temporaer" "$ziel"

while IFS= read -r -d '' alt; do
    rm -- "$alt"
done < <(find "$backup_dir" -maxdepth 1 -type f -name 'wettkampf-*.dump' \
    -mtime "+$aufbewahrung_tage" -print0)

printf 'Backup erstellt: %s (%s Bytes)\n' "$ziel" "$(stat -c %s "$ziel")"
