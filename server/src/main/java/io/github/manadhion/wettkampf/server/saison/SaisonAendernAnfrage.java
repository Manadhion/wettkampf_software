package io.github.manadhion.wettkampf.server.saison;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SaisonAendernAnfrage(
        @Min(0) @Max(9999) int name,
        @Min(0) long version) {
}
