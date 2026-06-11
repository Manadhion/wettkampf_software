package io.github.manadhion.wettkampf.data;

import java.util.UUID;

public class Liga {

    private String id;
    private String ligaName;

    //Konstruktor zum neu Anlegen
    public Liga (String ligaName) {
        this.ligaName = ligaName;

        //id erzeugen
        id = UUID.randomUUID().toString();
    }

    //Konstruktor zum laden aus DB
    public Liga(String id, String ligaName) {

        this.id = id;
        this.ligaName = ligaName;
    }

    public String getLigaName() {
        return ligaName;
    }

    public void setLigaName(String ligaName) {
        this.ligaName = ligaName;
    }

    public String getId() {
        return id;
    }

    //Anzeigetext für die ComboBox
    @Override
    public String toString() {
        return ligaName;
    }

}
