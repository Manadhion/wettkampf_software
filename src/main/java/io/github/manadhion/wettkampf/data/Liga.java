package io.github.manadhion.wettkampf.data;

import java.util.UUID;

/**
 * Entität einer Liga, in die Mannschaften eingeordnet werden.
 */
public class Liga {

    private String id;
    private String ligaName;
    private int rangfolge;

    /**
     * Legt eine neue Liga mit frisch erzeugter id an.
     * @param ligaName Name der Liga
     */
    public Liga (String ligaName, int rangfolge) {
        this.ligaName = ligaName;
        this.rangfolge = rangfolge;

        //id erzeugen
        id = UUID.randomUUID().toString();
    }

    /**
     * Lädt eine bestehende Liga aus der Datenbank.
     * @param id vorhandene id der Liga
     * @param ligaName Name der Liga
     */
    public Liga(String id, String ligaName) {

        this(id, ligaName, 999);
    }

    public Liga(String id, String ligaName, int rangfolge) {

        this.id = id;
        this.ligaName = ligaName;
        this.rangfolge = rangfolge;
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

    public int getRangfolge() { return rangfolge; }
    public void setRangfolge(int rangfolge) { this.rangfolge = rangfolge; }

    /**
     * Anzeigetext für die ComboBox.
     * @return Name der Liga
     */
    @Override
    public String toString() {
        return ligaName;
    }

}
