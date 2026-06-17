package io.github.manadhion.wettkampf.data;

import java.util.UUID;

/**
 * Entität einer Saison, der Name ist das Spieljahr als Zahl, z.B. 2526 für 25/26.
 */
public class Saison {
    
    //Parameter
    private String id;
    private int name;

    /**
     * Legt eine neue Saison mit frisch erzeugter id an.
     * @param name Spieljahr als Zahl, z.B. 2526 für 25/26
     */
    public Saison(int name) {
        this.name = name;

        //id erzeugen
        id = UUID.randomUUID().toString();
    }

    /**
     * Lädt eine bestehende Saison aus der Datenbank.
     * @param id vorhandene id der Saison
     * @param name Spieljahr als Zahl, z.B. 2526 für 25/26
     */
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

    /**
     * Anzeigetext für die ComboBox.
     * @return Spieljahr im Format JJ/JJ, z.B. 25/26
     */
    @Override
    public String toString() {
        return String.format("%02d/%02d", name / 100, name % 100);
    }
    
}
