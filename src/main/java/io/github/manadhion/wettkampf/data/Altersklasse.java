package io.github.manadhion.wettkampf.data;

import java.util.UUID;

public class Altersklasse {

    private String id;
    private String klassenName; //z.B. Herren, Jugend usw.

    //Konstruktor zum neu Anlegen
    public Altersklasse(String klassenName) {

        this.klassenName = klassenName;

        //id erzeugen
        id = UUID.randomUUID().toString();
    }

    //Konstruktor zum laden aus DB
    public Altersklasse(String id, String klassenName) {

        this.id = id;
        this.klassenName = klassenName;
    }

    public String getKlassenName() {
        return klassenName;
    }

    public void setKlassenName(String klassenName) {
        this.klassenName = klassenName;
    }

    public String getId() {
        return id;
    }
    
    //Anzeigetext für die ComboBox
    @Override
    public String toString() {
        return klassenName;
    }
}
