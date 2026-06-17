package io.github.manadhion.wettkampf.data;

import java.util.UUID;

/**
 * Entität einer Mannschaft, ist über die Klasse einer Liga zugeordnet.
 */
public class Mannschaft {

    //Parameter
    private String id;
    private String name;    //Name der Mannschaft
    private String klasse;  //Klasse in der sich die Mannschaft befindet, z.B. A-Klasse
    private String ligaName; //Name der Liga zur Anzeige, beim Laden aus der DB mitgegeben

    /**
     * Legt eine neue Mannschaft mit frisch erzeugter id an.
     * @param name Name der Mannschaft
     * @param klasse id der Liga, in der die Mannschaft spielt
     */
    public Mannschaft(String name, String klasse) {
        this.name = name;
        this.klasse = klasse; //Referenz auf die Liga in der sich die Mannschaft befindet

        //id erzeugen
        id = UUID.randomUUID().toString();
    }

    /**
     * Lädt eine bestehende Mannschaft aus der Datenbank, inklusive Liganame für die Anzeige.
     * @param id vorhandene id der Mannschaft
     * @param name Name der Mannschaft
     * @param klasse id der Liga, in der die Mannschaft spielt
     * @param ligaName Name der Liga zur Anzeige
     */
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

    /**
     * Anzeigetext für die ComboBox, sonst würde dort Mannschaft@123abc stehen.
     * @return Mannschaftsname, mit Liganame falls dieser geladen wurde
     */
    @Override
    public String toString() {
        //ohne geladenen Liganame nur den Mannschaftsnamen zeigen
        if (ligaName == null) {
            return name;
        }
        return name + " - " + ligaName;
    }

}
