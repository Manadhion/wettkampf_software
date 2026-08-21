CREATE TABLE konto (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    passwort_hash TEXT NOT NULL,
    aktiv BOOLEAN NOT NULL DEFAULT TRUE,
    erstellt_am TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sitzung (
    id UUID PRIMARY KEY,
    konto_id UUID NOT NULL REFERENCES konto(id) ON DELETE CASCADE,
    token_hash CHAR(64) NOT NULL UNIQUE,
    erstellt_am TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    laeuft_ab TIMESTAMPTZ NOT NULL,
    widerrufen_am TIMESTAMPTZ
);

CREATE INDEX sitzung_gueltigkeit_idx
    ON sitzung(token_hash, laeuft_ab)
    WHERE widerrufen_am IS NULL;
