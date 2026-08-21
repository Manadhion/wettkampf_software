package io.github.manadhion.wettkampf.server.saison;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.manadhion.wettkampf.server.web.NichtGefundenException;
import io.github.manadhion.wettkampf.server.web.VersionskonfliktException;

@Service
public class SaisonService {

    private final SaisonRepository repository;

    public SaisonService(SaisonRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Saison> alle() {
        return repository.alle();
    }

    @Transactional
    public Saison anlegen(int name) {
        return repository.anlegen(name);
    }

    @Transactional
    public Saison aktualisieren(UUID id, int name, long version) {
        return repository.aktualisieren(id, name, version).orElseThrow(() -> konfliktOderNichtGefunden(id));
    }

    @Transactional
    public void loeschen(UUID id, long version) {
        if (repository.loeschen(id, version) == 0) {
            throw konfliktOderNichtGefunden(id);
        }
    }

    private RuntimeException konfliktOderNichtGefunden(UUID id) {
        if (repository.existiert(id)) {
            return new VersionskonfliktException("Die Saison wurde zwischenzeitlich geändert.");
        }
        return new NichtGefundenException("Die Saison wurde nicht gefunden.");
    }
}
