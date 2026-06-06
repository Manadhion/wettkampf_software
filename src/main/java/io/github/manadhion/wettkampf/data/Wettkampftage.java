package io.github.manadhion.wettkampf.data;

import java.time.LocalDate;
import java.util.UUID;

public class Wettkampftage {

    //Parameter
    private String id;
    private LocalDate datum;    //Datum an dem der Wettkampf stattfindet
    private String ausrichterVerein;    //Der VErein bei dem der Wettkampf an diesem Tag stattfindet
    private String saisonID; // Referenz auf die Saison

    //Konstruktor für neue Tage
    public Wettkampftage(LocalDate datum, String ausrichterverein, String saisonID) {
        this.datum = datum;
        this.ausrichterVerein = ausrichterverein;
        this.saisonID = saisonID;

        //id erzeugen
        id = UUID.randomUUID().toString();
    }

    //Konstruktor zum laden aus DB
    public Wettkampftage(String id, LocalDate datum, String ausrichterverein, String saisonID) {
        this.id = id;
        this.datum = datum;
        this.ausrichterVerein = ausrichterverein;
        this.saisonID = saisonID;
    }

    public String getId() {
        return id;
    }

    public LocalDate getDatum() {
        return datum;
    }

    public void setDatum(LocalDate datum) {
        this.datum = datum;
    }

    public String getAusrichterVerein() {
        return ausrichterVerein;
    }

    public void setAusrichterVerein(String ausrichterVerein) {
        this.ausrichterVerein = ausrichterVerein;
    }

    public String getSaisonID() {
        return saisonID;
    }

    public void setSaisonID(String saisonID) {
        this.saisonID = saisonID;
    }
    
    
}
