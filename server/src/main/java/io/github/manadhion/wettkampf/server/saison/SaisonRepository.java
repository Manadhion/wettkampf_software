package io.github.manadhion.wettkampf.server.saison;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SaisonRepository {

    List<Saison> alle();
    Optional<Saison> finde(UUID id);
    Saison anlegen(int name);
    Optional<Saison> aktualisieren(UUID id, int name, long version);
    int loeschen(UUID id, long version);
    boolean existiert(UUID id);
}
