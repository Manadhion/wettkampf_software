package io.github.manadhion.wettkampf.server.fachdaten;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class FachdatenModelle {

    private FachdatenModelle() {
    }

    public record Liga(@NotNull UUID id, @NotBlank String name,
            @Min(1) int rangfolge) {
    }

    public record Altersklasse(@NotNull UUID id, @NotBlank String name) {
    }

    public record Mannschaft(@NotNull UUID id, @NotBlank String name,
            UUID klasse, String ligaName) {
    }

    public record Schuetze(@NotNull UUID id, @NotBlank String vorname,
            @NotBlank String nachname, @NotNull UUID mannschaftId,
            @NotNull UUID altersklasseId) {
    }

    public record Wettkampftag(@NotNull UUID id, @NotNull LocalDate datum,
            @NotBlank String ausrichterverein, @NotNull UUID saisonId) {
    }

    public record Begegnung(@NotNull UUID id, @NotNull UUID heim,
            @NotNull UUID gegner, @NotNull UUID wettkampftagId,
            UUID liga, String ligaName, String heimName, String gegnerName) {
    }

    public record Ergebnis(@NotNull UUID id, @NotNull UUID schuetzeId,
            @NotNull UUID wettkampftagId, @Min(0) @Max(600) int wert) {
    }

    public record SaisonSchuetze(@NotNull UUID saisonId, @NotNull UUID schuetzeId,
            @NotBlank String vorname, @NotBlank String nachname,
            @NotNull UUID mannschaftId, @NotBlank String mannschaftName,
            @NotNull UUID altersklasseId, @NotBlank String altersklasseName) {
    }

    public record Speicherung(boolean neu) {
    }

    public record Gesamt(int wert) {
    }
}
