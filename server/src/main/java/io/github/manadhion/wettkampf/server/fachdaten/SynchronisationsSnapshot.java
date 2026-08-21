package io.github.manadhion.wettkampf.server.fachdaten;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import io.github.manadhion.wettkampf.server.fachdaten.FachdatenModelle.Altersklasse;
import io.github.manadhion.wettkampf.server.fachdaten.FachdatenModelle.Begegnung;
import io.github.manadhion.wettkampf.server.fachdaten.FachdatenModelle.Ergebnis;
import io.github.manadhion.wettkampf.server.fachdaten.FachdatenModelle.Liga;
import io.github.manadhion.wettkampf.server.fachdaten.FachdatenModelle.Mannschaft;
import io.github.manadhion.wettkampf.server.fachdaten.FachdatenModelle.SaisonSchuetze;
import io.github.manadhion.wettkampf.server.fachdaten.FachdatenModelle.Schuetze;
import io.github.manadhion.wettkampf.server.fachdaten.FachdatenModelle.Wettkampftag;

/** Vollstaendiger Fachdatenstand fuer den fluechtigen Offline-Notbetrieb. */
public record SynchronisationsSnapshot(
        @NotNull List<@Valid SaisonEintrag> saisons,
        @NotNull List<@Valid Liga> ligen,
        @NotNull List<@Valid Altersklasse> altersklassen,
        @NotNull List<@Valid Mannschaft> mannschaften,
        @NotNull List<@Valid Schuetze> schuetzen,
        @NotNull List<@Valid Wettkampftag> wettkampftage,
        @NotNull List<@Valid Begegnung> begegnungen,
        @NotNull List<@Valid SaisonSchuetze> saisonSchuetzen,
        @NotNull List<@Valid Ergebnis> ergebnisse) {

    public record SaisonEintrag(@NotNull UUID id, int name, @Min(0) long version) {
    }
}
