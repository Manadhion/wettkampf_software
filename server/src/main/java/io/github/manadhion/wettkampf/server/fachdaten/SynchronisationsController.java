package io.github.manadhion.wettkampf.server.fachdaten;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/snapshot")
public class SynchronisationsController {

    private final SynchronisationsService service;

    public SynchronisationsController(SynchronisationsService service) {
        this.service = service;
    }

    @GetMapping
    public SynchronisationsSnapshot laden() {
        return service.laden();
    }

    @PutMapping
    public ResponseEntity<SynchronisationsAntwort> speichern(
            @Valid @RequestBody SynchronisationsSnapshot snapshot) {
        return ResponseEntity.ok(new SynchronisationsAntwort(service.speichern(snapshot)));
    }
}
