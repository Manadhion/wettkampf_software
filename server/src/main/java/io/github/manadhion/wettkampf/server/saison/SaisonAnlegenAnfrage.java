package io.github.manadhion.wettkampf.server.saison;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SaisonAnlegenAnfrage(
        @Min(0) @Max(9999) int name) {
}
