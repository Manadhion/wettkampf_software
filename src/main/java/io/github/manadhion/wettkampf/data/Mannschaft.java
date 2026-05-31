package io.github.manadhion.wettkampf.data;

import java.util.UUID;

public class Mannschaft {

    //Parameter
    private String id;
    public String getId() {
        return id;
    }

    private String name;
    private int punkte;
    private int kaempfeBeendet;
    private String klasse;

    //Konstruktor für neue Mannschaft
    public Mannschaft(String name, int punkte, int kaempfeBeendet, String klasse) {
        this.name = name;
        this.punkte = punkte;
        this.kaempfeBeendet = kaempfeBeendet;
        this.klasse = klasse;

        //id erzeugen
        id = UUID.randomUUID().toString();
    }

    //Konstruktor zum laden von Mannschaften aus DB
    public Mannschaft(String id, String name, int punkte, int kaempfeBeendet, String klasse) {
        this.id = id;
        this.name = name;
        this.punkte = punkte;
        this.kaempfeBeendet = kaempfeBeendet;
        this.klasse = klasse;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPunkte() {
        return punkte;
    }

    public void setPunkte(int punkte) {
        this.punkte = punkte;
    }

    public int getKaempfeBeendet() {
        return kaempfeBeendet;
    }

    public void setKaempfeBeendet(int kaempfeBeendet) {
        this.kaempfeBeendet = kaempfeBeendet;
    }

    public String getKlasse() {
        return klasse;
    }

    public void setKlasse(String klasse) {
        this.klasse = klasse;
    }
    
}
