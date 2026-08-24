CREATE TABLE fachdaten_revision (
    id BOOLEAN PRIMARY KEY DEFAULT TRUE CHECK (id),
    version BIGINT NOT NULL CHECK (version >= 0)
);

INSERT INTO fachdaten_revision(id, version) VALUES (TRUE, 0);
