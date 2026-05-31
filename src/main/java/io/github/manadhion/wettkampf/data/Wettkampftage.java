package io.github.manadhion.wettkampf.data;

import java.time.LocalDate;
import java.util.UUID;

public class Wettkampftage {

    //Parameter
    String id;
    LocalDate datum;
    String ausrichterVerein;

    //Konstruktor für neue Tage
    public Wettkampftage(LocalDate datum, String ausrichterverein) {
        this.datum = datum;
        this.ausrichterVerein = ausrichterverein;

        //id erzeugen
        id = UUID.randomUUID().toString();
    }

    //Konstruktor zum laden aus DB
    public Wettkampftage(String id, LocalDate datum, String ausrichterverein) {
        this.id = id;
        this.datum = datum;
        this.ausrichterVerein = ausrichterverein;
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
    
    
}
