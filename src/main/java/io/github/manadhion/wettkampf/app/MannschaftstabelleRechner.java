package io.github.manadhion.wettkampf.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.manadhion.wettkampf.data.Begegnung;
import io.github.manadhion.wettkampf.data.Liga;
import io.github.manadhion.wettkampf.data.Mannschaft;
import io.github.manadhion.wettkampf.data.TabellenZeile;
import io.github.manadhion.wettkampf.data.Wettkampftage;

/**
 * Berechnet die Mannschaftstabelle einer Liga aus allen Begegnungen einer Saison.
 */
public class MannschaftstabelleRechner {

    //holt die Daten über den Controller (Wettkampftage, Begegnungen, Gesamtergebnisse)
    private Controller controller;

    /**
     * Legt den Rechner an.
     * @param controller Controller, über den die benötigten Daten geholt werden
     */
    public MannschaftstabelleRechner(Controller controller) {
        this.controller = controller;
    }

    /**
     * Tabelle einer Liga über alle Wettkampftage der Saison berechnen, absteigend sortiert.
     * @param liga Liga, deren Tabelle berechnet wird
     * @param saisonID id der Saison, über die gerechnet wird
     * @return Tabellenzeilen, sortiert nach Begegnungspunkten und bei Gleichstand nach Ringen
     */
    public List<TabellenZeile> berechne(Liga liga, String saisonID) {

        //Mannschafts-ID -> Tabellenzeile, mit allen Mannschaften der Liga vorbelegt (so stehen auch Mannschaften ohne Begegnung drin)
        Map<String, TabellenZeile> tabelle = new LinkedHashMap<>();
        for (Mannschaft m : controller.alleMannschaften()) {
            if (m.getKlasse().equals(liga.getId())) {
                tabelle.put(m.getId(), new TabellenZeile(m.getName()));
            }
        }

        //jeden Wettkampftag der Saison durchgehen
        for (Wettkampftage tag : controller.wettkampftageVonSaison(saisonID)) {

            //jede Begegnung dieses Tages durchgehen
            for (Begegnung b : controller.begegnungenAnDiesemTag(tag.getId())) {

                TabellenZeile heimZeile = tabelle.get(b.getHeim());
                TabellenZeile gegnerZeile = tabelle.get(b.getGegner());

                //nur Begegnungen dieser Liga zählen
                if (heimZeile == null || gegnerZeile == null) {
                    continue;
                }

                //Gesamtergebnisse (Summe der besten 3 Schützen) beider Mannschaften
                int gesamtHeim = controller.gesamtErgebnisBeste3(b.getHeim(), tag.getId());
                int gesamtGegner = controller.gesamtErgebnisBeste3(b.getGegner(), tag.getId());

                //nur abgeschlossene Begegnungen werten, bei denen beide Mannschaften geschossen haben
                if (gesamtHeim == 0 || gesamtGegner == 0) {
                    continue;
                }

                //Ringe beider Mannschaften aufaddieren
                heimZeile.ringeHinzufuegen(gesamtHeim);
                gegnerZeile.ringeHinzufuegen(gesamtGegner);

                //Begegnungspunkte: 2 für das höhere Gesamtergebnis, bei Gleichstand 1:1
                if (gesamtHeim > gesamtGegner) {
                    heimZeile.punkteHinzufuegen(2, 0);
                    gegnerZeile.punkteHinzufuegen(0, 2);
                } else if (gesamtHeim < gesamtGegner) {
                    heimZeile.punkteHinzufuegen(0, 2);
                    gegnerZeile.punkteHinzufuegen(2, 0);
                } else {
                    heimZeile.punkteHinzufuegen(1, 1);
                    gegnerZeile.punkteHinzufuegen(1, 1);
                }
            }
        }

        //nach Begegnungspunkten, bei Gleichstand nach Ringen absteigend sortieren
        List<TabellenZeile> zeilen = new ArrayList<>(tabelle.values());
        Collections.sort(zeilen, new Comparator<TabellenZeile>() {
            @Override
            public int compare(TabellenZeile a, TabellenZeile b) {
                if (b.getPunkte() != a.getPunkte()) {
                    return b.getPunkte() - a.getPunkte();
                }
                return b.getRinge() - a.getRinge();
            }
        });

        return zeilen;
    }

}
