package io.github.manadhion.wettkampf.server.fachdaten;

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

import io.github.manadhion.wettkampf.server.fachdaten.FachdatenModelle.Altersklasse;
import io.github.manadhion.wettkampf.server.fachdaten.FachdatenModelle.Begegnung;
import io.github.manadhion.wettkampf.server.fachdaten.FachdatenModelle.Ergebnis;
import io.github.manadhion.wettkampf.server.fachdaten.FachdatenModelle.Gesamt;
import io.github.manadhion.wettkampf.server.fachdaten.FachdatenModelle.Liga;
import io.github.manadhion.wettkampf.server.fachdaten.FachdatenModelle.Mannschaft;
import io.github.manadhion.wettkampf.server.fachdaten.FachdatenModelle.SaisonSchuetze;
import io.github.manadhion.wettkampf.server.fachdaten.FachdatenModelle.Schuetze;
import io.github.manadhion.wettkampf.server.fachdaten.FachdatenModelle.Speicherung;
import io.github.manadhion.wettkampf.server.fachdaten.FachdatenModelle.Wettkampftag;

@RestController
@RequestMapping("/api/v1")
public class FachdatenController {

    private final FachdatenService service;

    public FachdatenController(FachdatenService service) {
        this.service = service;
    }

    @GetMapping("/ligen")
    public List<Liga> ligen(@RequestParam(required = false) UUID saisonId) {
        return service.ligen(saisonId);
    }

    @GetMapping("/ligen/naechste-rangfolge")
    public Gesamt naechsteLigaRangfolge() {
        return new Gesamt(service.naechsteLigaRangfolge());
    }

    @GetMapping("/ligen/{id}")
    public Liga liga(@PathVariable UUID id) {
        return service.liga(id);
    }

    @PostMapping("/ligen")
    public Liga ligaAnlegen(@Valid @RequestBody Liga wert) {
        return service.ligaAnlegen(wert);
    }

    @PutMapping("/ligen/{id}")
    public Liga ligaAktualisieren(@PathVariable UUID id, @Valid @RequestBody Liga wert) {
        return service.ligaAktualisieren(id, wert);
    }

    @DeleteMapping("/ligen/{id}")
    public ResponseEntity<Void> ligaLoeschen(@PathVariable UUID id) {
        service.ligaLoeschen(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/altersklassen")
    public List<Altersklasse> altersklassen() {
        return service.altersklassen();
    }

    @PostMapping("/altersklassen")
    public Altersklasse altersklasseAnlegen(@Valid @RequestBody Altersklasse wert) {
        return service.altersklasseAnlegen(wert);
    }

    @PutMapping("/altersklassen/{id}")
    public Altersklasse altersklasseAktualisieren(@PathVariable UUID id,
            @Valid @RequestBody Altersklasse wert) {
        return service.altersklasseAktualisieren(id, wert);
    }

    @DeleteMapping("/altersklassen/{id}")
    public ResponseEntity<Void> altersklasseLoeschen(@PathVariable UUID id) {
        service.altersklasseLoeschen(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mannschaften")
    public List<Mannschaft> mannschaften(@RequestParam(required = false) UUID saisonId) {
        return service.mannschaften(saisonId);
    }

    @GetMapping("/mannschaften/{id}")
    public Mannschaft mannschaft(@PathVariable UUID id) {
        return service.mannschaft(id);
    }

    @PostMapping("/mannschaften")
    public Mannschaft mannschaftAnlegen(@Valid @RequestBody Mannschaft wert) {
        return service.mannschaftAnlegen(wert);
    }

    @PutMapping("/mannschaften/{id}")
    public Mannschaft mannschaftAktualisieren(@PathVariable UUID id,
            @Valid @RequestBody Mannschaft wert) {
        return service.mannschaftAktualisieren(id, wert);
    }

    @DeleteMapping("/mannschaften/{id}")
    public ResponseEntity<Void> mannschaftLoeschen(@PathVariable UUID id) {
        service.mannschaftLoeschen(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/schuetzen")
    public List<Schuetze> schuetzen(@RequestParam UUID mannschaftId) {
        return service.schuetzen(mannschaftId);
    }

    @PostMapping("/schuetzen")
    public Schuetze schuetzeAnlegen(@Valid @RequestBody Schuetze wert) {
        return service.schuetzeAnlegen(wert);
    }

    @PutMapping("/schuetzen/{id}")
    public Schuetze schuetzeAktualisieren(@PathVariable UUID id,
            @Valid @RequestBody Schuetze wert) {
        return service.schuetzeAktualisieren(id, wert);
    }

    @DeleteMapping("/schuetzen/{id}")
    public ResponseEntity<Void> schuetzeLoeschen(@PathVariable UUID id) {
        service.schuetzeLoeschen(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/wettkampftage")
    public List<Wettkampftag> wettkampftage(@RequestParam(required = false) UUID saisonId) {
        return service.wettkampftage(saisonId);
    }

    @PostMapping("/wettkampftage")
    public Wettkampftag wettkampftagAnlegen(@Valid @RequestBody Wettkampftag wert) {
        return service.wettkampftagAnlegen(wert);
    }

    @PutMapping("/wettkampftage/{id}")
    public Wettkampftag wettkampftagAktualisieren(@PathVariable UUID id,
            @Valid @RequestBody Wettkampftag wert) {
        return service.wettkampftagAktualisieren(id, wert);
    }

    @DeleteMapping("/wettkampftage/{id}")
    public ResponseEntity<Void> wettkampftagLoeschen(@PathVariable UUID id) {
        service.wettkampftagLoeschen(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/begegnungen")
    public List<Begegnung> begegnungen(@RequestParam UUID wettkampftagId) {
        return service.begegnungen(wettkampftagId);
    }

    @GetMapping("/begegnungen/existiert")
    public Speicherung begegnungExistiert(@RequestParam UUID wettkampftagId,
            @RequestParam UUID mannschaftA, @RequestParam UUID mannschaftB) {
        return new Speicherung(service.begegnungExistiert(
                wettkampftagId, mannschaftA, mannschaftB));
    }

    @PostMapping("/begegnungen")
    public Begegnung begegnungAnlegen(@Valid @RequestBody Begegnung wert) {
        return service.begegnungAnlegen(wert);
    }

    @DeleteMapping("/begegnungen/{id}")
    public ResponseEntity<Void> begegnungLoeschen(@PathVariable UUID id) {
        service.begegnungLoeschen(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ergebnisse")
    public ResponseEntity<Ergebnis> ergebnis(@RequestParam UUID schuetzeId,
            @RequestParam UUID wettkampftagId) {
        Ergebnis wert = service.ergebnis(schuetzeId, wettkampftagId);
        return wert == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(wert);
    }

    @PutMapping("/ergebnisse")
    public Speicherung ergebnisSpeichern(@Valid @RequestBody Ergebnis wert) {
        return new Speicherung(service.ergebnisSpeichern(wert));
    }

    @GetMapping("/ergebnisse/gesamt")
    public Gesamt gesamtErgebnis(@RequestParam UUID mannschaftId,
            @RequestParam UUID wettkampftagId) {
        return new Gesamt(service.gesamtErgebnis(mannschaftId, wettkampftagId));
    }

    @GetMapping("/saison-schuetzen")
    public List<SaisonSchuetze> saisonSchuetzen(@RequestParam UUID saisonId,
            @RequestParam UUID mannschaftId) {
        return service.saisonSchuetzen(saisonId, mannschaftId);
    }

    @GetMapping("/saison-schuetzen/eintrag")
    public ResponseEntity<SaisonSchuetze> saisonSchuetze(@RequestParam UUID saisonId,
            @RequestParam UUID schuetzeId) {
        SaisonSchuetze wert = service.saisonSchuetze(saisonId, schuetzeId);
        return wert == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(wert);
    }

    @PutMapping("/saison-schuetzen")
    public SaisonSchuetze saisonSchuetzeSpeichern(@Valid @RequestBody SaisonSchuetze wert) {
        return service.saisonSchuetzeSpeichern(wert);
    }
}
