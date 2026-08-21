package io.github.manadhion.wettkampf.server.saison;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcSaisonRepository implements SaisonRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcSaisonRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Saison> alle() {
        return jdbcTemplate.query(
                "SELECT id, name, version FROM saison ORDER BY name DESC",
                this::abbilden);
    }

    @Override
    public Optional<Saison> finde(UUID id) {
        return jdbcTemplate.query(
                "SELECT id, name, version FROM saison WHERE id = ?",
                this::abbilden, id).stream().findFirst();
    }

    @Override
    public Saison anlegen(int name) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO saison(id, name, version) VALUES (?, ?, 0)", id, name);
        return new Saison(id, name, 0);
    }

    @Override
    public Optional<Saison> aktualisieren(UUID id, int name, long version) {
        int anzahl = jdbcTemplate.update("""
                UPDATE saison
                SET name = ?, version = version + 1, geaendert_am = CURRENT_TIMESTAMP
                WHERE id = ? AND version = ?
                """, name, id, version);
        return anzahl == 0 ? Optional.empty() : finde(id);
    }

    @Override
    public int loeschen(UUID id, long version) {
        return jdbcTemplate.update("DELETE FROM saison WHERE id = ? AND version = ?", id, version);
    }

    @Override
    public boolean existiert(UUID id) {
        Integer anzahl = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM saison WHERE id = ?", Integer.class, id);
        return anzahl != null && anzahl > 0;
    }

    private Saison abbilden(ResultSet resultSet, int zeile) throws SQLException {
        return new Saison(
                resultSet.getObject("id", UUID.class),
                resultSet.getInt("name"),
                resultSet.getLong("version"));
    }
}
