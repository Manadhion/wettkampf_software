package io.github.manadhion.wettkampf.server.saison;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/saisons")
public class SaisonController {

    private final SaisonService service;

    public SaisonController(SaisonService service) {
        this.service = service;
    }

    @GetMapping
    public List<Saison> alle() {
        return service.alle();
    }

    @PostMapping
    public ResponseEntity<Saison> anlegen(@Valid @RequestBody SaisonAnlegenAnfrage anfrage) {
        Saison saison = service.anlegen(anfrage.name());
        return ResponseEntity.created(URI.create("/api/v1/saisons/" + saison.id())).body(saison);
    }

    @PutMapping("/{id}")
    public Saison aktualisieren(@PathVariable UUID id,
            @Valid @RequestBody SaisonAendernAnfrage anfrage) {
        return service.aktualisieren(id, anfrage.name(), anfrage.version());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> loeschen(@PathVariable UUID id, @RequestParam long version) {
        service.loeschen(id, version);
        return ResponseEntity.noContent().build();
    }
}
