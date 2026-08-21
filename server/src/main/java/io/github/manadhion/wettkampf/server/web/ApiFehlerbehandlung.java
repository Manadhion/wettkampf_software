package io.github.manadhion.wettkampf.server.web;

import java.time.Instant;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.github.manadhion.wettkampf.server.auth.AnmeldungFehlgeschlagenException;

@RestControllerAdvice
public class ApiFehlerbehandlung {

    @ExceptionHandler(AnmeldungFehlgeschlagenException.class)
    public ResponseEntity<ApiFehler> anmeldungFehlgeschlagen(
            AnmeldungFehlgeschlagenException exception) {
        return antwort(HttpStatus.UNAUTHORIZED, "ANMELDUNG_FEHLGESCHLAGEN", exception.getMessage());
    }

    @ExceptionHandler(NichtGefundenException.class)
    public ResponseEntity<ApiFehler> nichtGefunden(NichtGefundenException exception) {
        return antwort(HttpStatus.NOT_FOUND, "NICHT_GEFUNDEN", exception.getMessage());
    }

    @ExceptionHandler(VersionskonfliktException.class)
    public ResponseEntity<ApiFehler> versionskonflikt(VersionskonfliktException exception) {
        return antwort(HttpStatus.CONFLICT, "VERSION_KONFLIKT", exception.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiFehler> datenkonflikt() {
        return antwort(HttpStatus.CONFLICT, "DATENKONFLIKT",
                "Die Änderung verletzt eine fachliche Datenregel.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiFehler> ungueltigeAnfrage() {
        return antwort(HttpStatus.BAD_REQUEST, "UNGUELTIGE_ANFRAGE",
                "Die Anfrage enthält ungültige Werte.");
    }

    private ResponseEntity<ApiFehler> antwort(HttpStatus status, String code, String nachricht) {
        return ResponseEntity.status(status).body(new ApiFehler(code, nachricht, Instant.now()));
    }
}
