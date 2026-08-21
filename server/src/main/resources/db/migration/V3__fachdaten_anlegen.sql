CREATE TABLE liga (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    rangfolge INTEGER NOT NULL CHECK (rangfolge > 0)
);

CREATE TABLE altersklasse (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL
);

CREATE TABLE mannschaft (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    klasse UUID REFERENCES liga(id) ON DELETE RESTRICT
);

CREATE TABLE schuetze (
    id UUID PRIMARY KEY,
    vorname TEXT NOT NULL,
    nachname TEXT NOT NULL,
    mannschaft_id UUID NOT NULL REFERENCES mannschaft(id) ON DELETE RESTRICT,
    altersklasse_id UUID NOT NULL REFERENCES altersklasse(id) ON DELETE RESTRICT
);

CREATE TABLE wettkampftag (
    id UUID PRIMARY KEY,
    datum DATE NOT NULL UNIQUE,
    ausrichterverein TEXT NOT NULL,
    saison_id UUID NOT NULL REFERENCES saison(id) ON DELETE RESTRICT
);

CREATE TABLE begegnung (
    id UUID PRIMARY KEY,
    heim UUID NOT NULL REFERENCES mannschaft(id) ON DELETE RESTRICT,
    gegner UUID NOT NULL REFERENCES mannschaft(id) ON DELETE RESTRICT,
    wettkampftag_id UUID NOT NULL REFERENCES wettkampftag(id) ON DELETE RESTRICT,
    liga UUID REFERENCES liga(id) ON DELETE RESTRICT,
    liga_name TEXT,
    heim_name TEXT NOT NULL,
    gegner_name TEXT NOT NULL,
    CHECK (heim <> gegner)
);

CREATE UNIQUE INDEX begegnung_tag_paar_idx
    ON begegnung(wettkampftag_id, LEAST(heim, gegner), GREATEST(heim, gegner));

CREATE TABLE saison_schuetze (
    saison_id UUID NOT NULL REFERENCES saison(id) ON DELETE RESTRICT,
    schuetze_id UUID NOT NULL REFERENCES schuetze(id) ON DELETE RESTRICT,
    vorname TEXT NOT NULL,
    nachname TEXT NOT NULL,
    mannschaft_id UUID NOT NULL REFERENCES mannschaft(id) ON DELETE RESTRICT,
    mannschaft_name TEXT NOT NULL,
    altersklasse_id UUID NOT NULL REFERENCES altersklasse(id) ON DELETE RESTRICT,
    altersklasse_name TEXT NOT NULL,
    PRIMARY KEY (saison_id, schuetze_id)
);

CREATE TABLE ergebnis (
    id UUID PRIMARY KEY,
    schuetze_id UUID NOT NULL REFERENCES schuetze(id) ON DELETE RESTRICT,
    wettkampftag_id UUID NOT NULL REFERENCES wettkampftag(id) ON DELETE RESTRICT,
    wert INTEGER NOT NULL CHECK (wert BETWEEN 0 AND 600),
    UNIQUE (schuetze_id, wettkampftag_id)
);

CREATE INDEX wettkampftag_saison_idx ON wettkampftag(saison_id, datum);
CREATE INDEX schuetze_mannschaft_idx ON schuetze(mannschaft_id, vorname, nachname);
CREATE INDEX begegnung_tag_idx ON begegnung(wettkampftag_id);
CREATE INDEX saison_schuetze_mannschaft_idx
    ON saison_schuetze(saison_id, mannschaft_id, vorname, nachname);
