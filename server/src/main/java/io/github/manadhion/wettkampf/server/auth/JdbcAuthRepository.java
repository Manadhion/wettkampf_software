package io.github.manadhion.wettkampf.server.auth;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcAuthRepository implements AuthRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAuthRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean hatKonto() {
        Integer anzahl = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM konto", Integer.class);
        return anzahl != null && anzahl > 0;
    }

    @Override
    public void kontoAnlegen(UUID id, String name, String passwortHash) {
        jdbcTemplate.update(
                "INSERT INTO konto(id, name, passwort_hash) VALUES (?, ?, ?)",
                id, name, passwortHash);
    }

    @Override
    public Optional<Konto> kontoMitName(String name) {
        return jdbcTemplate.query("""
                SELECT id, name, passwort_hash, aktiv
                FROM konto
                WHERE name = ?
                """, (rs, zeile) -> new Konto(
                        rs.getObject("id", UUID.class),
                        rs.getString("name"),
                        rs.getString("passwort_hash"),
                        rs.getBoolean("aktiv")), name).stream().findFirst();
    }

    @Override
    public void sitzungAnlegen(UUID id, UUID kontoId, String tokenHash, Instant laeuftAb) {
        jdbcTemplate.update("""
                INSERT INTO sitzung(id, konto_id, token_hash, laeuft_ab)
                VALUES (?, ?, ?, ?)
                """, id, kontoId, tokenHash, java.sql.Timestamp.from(laeuftAb));
    }

    @Override
    public boolean istSitzungGueltig(String tokenHash, Instant jetzt) {
        Integer anzahl = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM sitzung s
                JOIN konto k ON k.id = s.konto_id
                WHERE s.token_hash = ?
                  AND s.widerrufen_am IS NULL
                  AND s.laeuft_ab > ?
                  AND k.aktiv = TRUE
                """, Integer.class, tokenHash, java.sql.Timestamp.from(jetzt));
        return anzahl != null && anzahl > 0;
    }

    @Override
    public int sitzungWiderrufen(String tokenHash, Instant jetzt) {
        return jdbcTemplate.update("""
                UPDATE sitzung
                SET widerrufen_am = ?
                WHERE token_hash = ? AND widerrufen_am IS NULL
                """, java.sql.Timestamp.from(jetzt), tokenHash);
    }

    @Override
    public int alteSitzungenLoeschen(Instant grenze) {
        return jdbcTemplate.update("""
                DELETE FROM sitzung
                WHERE laeuft_ab < ?
                   OR (widerrufen_am IS NOT NULL AND widerrufen_am < ?)
                """, java.sql.Timestamp.from(grenze), java.sql.Timestamp.from(grenze));
    }

}
