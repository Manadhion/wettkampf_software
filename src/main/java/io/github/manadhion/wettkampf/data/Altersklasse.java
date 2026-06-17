package io.github.manadhion.wettkampf.data;

import java.util.UUID;

/**
 * Entität einer Altersklasse, z.B. Herren oder Jugend, der Schützen zugeordnet werden.
 */
public class Altersklasse {

    private String id;
    private String klassenName; //z.B. Herren, Jugend usw.

    /**
     * Legt eine neue Altersklasse mit frisch erzeugter id an.
     * @param klassenName Name der Altersklasse, z.B. Herren, Jugend
     */
    public Altersklasse(String klassenName) {

        this.klassenName = klassenName;

        //id erzeugen
        id = UUID.randomUUID().toString();
    }

    /**
     * Lädt eine bestehende Altersklasse aus der Datenbank.
     * @param id vorhandene id der Altersklasse
     * @param klassenName Name der Altersklasse, z.B. Herren, Jugend
     */
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
    
    /**
     * Anzeigetext für die ComboBox.
     * @return Name der Altersklasse
     */
    @Override
    public String toString() {
        return klassenName;
    }
}
