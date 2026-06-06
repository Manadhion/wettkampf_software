package io.github.manadhion.wettkampf.data;

import java.util.UUID;

public class Mannschaft {

    //Parameter
    private String id;
    private String name;    //Name der Mannschaft
    private String klasse;  //Klasse in der sich die Mannschaft befindet, z.B. A-Klasse

    //Konstruktor für neue Mannschaft
    public Mannschaft(String name, String klasse) {
        this.name = name;
        this.klasse = klasse;

        //id erzeugen
        id = UUID.randomUUID().toString();
    }

    //Konstruktor zum laden von Mannschaften aus DB
    public Mannschaft(String id, String name, String klasse) {
        this.id = id;
        this.name = name;
        this.klasse = klasse;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKlasse() {
        return klasse;
    }

    public void setKlasse(String klasse) {
        this.klasse = klasse;
    }

    public String getId() {
        return id;
    }

    //Anzeigetext für die ComboBox, sonst würde dort Mannschaft@123abc stehen
    @Override
    public String toString() {
        return name;
    }

}
