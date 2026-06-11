package io.github.manadhion.wettkampf.data;

import java.util.UUID;

public class Mannschaft {

    //Parameter
    private String id;
    private String name;    //Name der Mannschaft
    private String klasse;  //Klasse in der sich die Mannschaft befindet, z.B. A-Klasse
    private String ligaName; //Name der Liga zur Anzeige, beim Laden aus der DB mitgegeben

    //Konstruktor für neue Mannschaft
    public Mannschaft(String name, String klasse) {
        this.name = name;
        this.klasse = klasse; //Referenz auf die Liga in der sich die Mannschaft befindet

        //id erzeugen
        id = UUID.randomUUID().toString();
    }

    //Konstruktor zum laden von Mannschaften aus DB, inklusive Liganame für die Anzeige
    public Mannschaft(String id, String name, String klasse, String ligaName) {
        this.id = id;
        this.name = name;
        this.klasse = klasse;
        this.ligaName = ligaName;
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

    public String getLigaName() {
        return ligaName;
    }

    //Anzeigetext für die ComboBox, sonst würde dort Mannschaft@123abc stehen
    @Override
    public String toString() {
        //ohne geladenen Liganame nur den Mannschaftsnamen zeigen
        if (ligaName == null) {
            return name;
        }
        return name + " - " + ligaName;
    }

}
