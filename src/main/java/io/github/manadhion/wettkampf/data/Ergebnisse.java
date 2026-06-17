package io.github.manadhion.wettkampf.data;

import java.util.UUID;

/**
 * Entität eines Ergebnisses: das von einem Schützen an einem Wettkampftag geschossene Resultat.
 */
public class Ergebnisse {

    //Parameter
    private String id;
    private String schuetzeID;      //Referenz zum Schützen
    private String wettkampftagID;  //Referenz zum Wettkampftag
    private int ergebnis;           //Ergebnis des Schützen an diesem Tag

    /**
     * Legt ein neues Ergebnis mit frisch erzeugter id an.
     * @param schuetzeID id des Schützen, der das Ergebnis geschossen hat
     * @param wettkampftagID id des Wettkampftages, an dem geschossen wurde
     * @param ergebnis geschossene Ringzahl
     */
    public Ergebnisse (String schuetzeID, String wettkampftagID, int ergebnis) {

        this.schuetzeID = schuetzeID;
        this.wettkampftagID = wettkampftagID;
        this.ergebnis = ergebnis;

        //id erzeugen
        id = UUID.randomUUID().toString();
    }

    /**
     * Lädt ein bestehendes Ergebnis aus der Datenbank oder für Korrekturen.
     * @param id vorhandene id des Ergebnisses
     * @param schuetzeID id des Schützen, der das Ergebnis geschossen hat
     * @param wettkampftagID id des Wettkampftages, an dem geschossen wurde
     * @param ergebnis geschossene Ringzahl
     */
    public Ergebnisse (String id, String schuetzeID, String wettkampftagID, int ergebnis) {

        this.id = id;
        this.schuetzeID = schuetzeID;
        this.wettkampftagID = wettkampftagID;
        this.ergebnis = ergebnis;

    }

    public String getId() {
        return id;
    }

    public String getSchuetzeID() {
        return schuetzeID;
    }

    public void setSchuetzeID(String schuetzeID) {
        this.schuetzeID = schuetzeID;
    }

    public String getWettkampftagID() {
        return wettkampftagID;
    }

    public void setWettkampftagID(String wettkampftagID) {
        this.wettkampftagID = wettkampftagID;
    }

    public int getErgebnis() {
        return ergebnis;
    }

    public void setErgebnis(int ergebnis) {
        this.ergebnis = ergebnis;
    }
    
}
