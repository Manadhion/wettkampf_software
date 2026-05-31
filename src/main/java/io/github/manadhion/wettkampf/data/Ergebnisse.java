package io.github.manadhion.wettkampf.data;

import java.util.UUID;

public class Ergebnisse {

    //Parameter
    String id;
    String schuetzeID;
    String wettkampftagID;
    int ergebnis;

    //Konstruktor für neues Ergebnis
    public Ergebnisse (String schuetzeID, String wettkampftagID, int ergebnis) {

        this.schuetzeID = schuetzeID;
        this.wettkampftagID = wettkampftagID;
        this.ergebnis = ergebnis;

        //id erzeugen
        id = UUID.randomUUID().toString();
    }

    //Konstruktor für Korrekturen odeer laden aus DB
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
