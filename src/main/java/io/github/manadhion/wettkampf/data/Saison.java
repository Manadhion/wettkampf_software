package io.github.manadhion.wettkampf.data;

import java.util.UUID;

public class Saison {
    
    //Parameter
    private String id;
    private int name;

    //Konstruktor zum neu anlegen
    public Saison(int name) {
        this.name = name;

        //id erzeugen
        id = UUID.randomUUID().toString();
    }

    //Konstruktor zum laden
    public Saison(String id, int name) {
        this.name = name;
        this.id = id;
    }

    public int getName() {
        return name;
    }

    public void setName(int name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    //Anzeigetext für die ComboBox
    @Override
    public String toString() {
        return String.format("%02d/%02d", name / 100, name % 100);
    }
    
}
