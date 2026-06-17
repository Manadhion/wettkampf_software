package io.github.manadhion.wettkampf.data;

import java.util.UUID;

/**
 * Entität einer Begegnung: Heim- gegen Auswärtsmannschaft an einem Wettkampftag.
 */
public class Begegnung {

    //Parameter
    private String id;
    private String heim;    //Heimmannschaft laut Saisonplanung
    private String gegner;  //Auswärtsmannschaft laut Saisonplanung
    private String wettkampftag;    //Referenz zum Wettkampftag


    /**
     * Legt eine neue Begegnung mit frisch erzeugter id an.
     * @param heim id der Heimmannschaft
     * @param gegner id der Auswärtsmannschaft
     * @param wettkampftag id des Wettkampftages, an dem die Begegnung stattfindet
     */
    public Begegnung(String heim, String gegner, String wettkampftag) {

        this.heim = heim;
        this.gegner = gegner;
        this.wettkampftag = wettkampftag;

        //id erzeugen
        id = UUID.randomUUID().toString();
    }

    /**
     * Lädt eine bestehende Begegnung aus der Datenbank.
     * @param id vorhandene id der Begegnung
     * @param heim id der Heimmannschaft
     * @param gegner id der Auswärtsmannschaft
     * @param wettkampftag id des Wettkampftages, an dem die Begegnung stattfindet
     */
    public Begegnung(String id, String heim, String gegner, String wettkampftag) {

        this.id = id;
        this.heim = heim;
        this.gegner = gegner;
        this.wettkampftag = wettkampftag;
    }

    public String getId() {
        return id;
    }

    public String getHeim() {
        return heim;
    }

    public void setHeim(String heim) {
        this.heim = heim;
    }

    public String getGegner() {
        return gegner;
    }

    public void setGegner(String gegner) {
        this.gegner = gegner;
    }

    public String getWettkampftag() {
        return wettkampftag;
    }

    public void setWettkampftag(String wettkampftag) {
        this.wettkampftag = wettkampftag;
    }

}
