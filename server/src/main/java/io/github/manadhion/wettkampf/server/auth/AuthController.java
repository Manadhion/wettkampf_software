package io.github.manadhion.wettkampf.server.auth;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/anmeldung")
    public AnmeldungAntwort anmelden(@Valid @RequestBody AnmeldungAnfrage anfrage) {
        return service.anmelden(anfrage.name(), anfrage.passwort());
    }

    @PostMapping("/abmeldung")
    public ResponseEntity<Void> abmelden(
            @RequestHeader(name = "Authorization") String authorization) {
        service.abmelden(BearerToken.ausAuthorization(authorization));
        return ResponseEntity.noContent().build();
    }

}
