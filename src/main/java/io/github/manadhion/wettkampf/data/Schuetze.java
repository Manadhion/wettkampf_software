package io.github.manadhion.wettkampf.data;

import java.util.UUID;

/**
 * Entität eines Schützen, gehört einer Mannschaft und einer Altersklasse an.
 */
public class Schuetze {

    //Parameter
    private String id;
    private String vorname;
    private String nachname;
    private String mannschaftid;    //Referenz zur Mannschaft
    private String altersKlasse;       //Altersklasse z.B. Jugend, Herren usw.

    /**
     * Legt einen neuen Schützen mit frisch erzeugter id an.
     * @param vorname Vorname des Schützen
     * @param nachname Nachname des Schützen
     * @param mannschaftid id der Mannschaft, der der Schütze angehört
     * @param altersKlasse Altersklasse des Schützen, z.B. Jugend, Herren
     */
    public Schuetze(String vorname, String nachname, String mannschaftid, String altersKlasse) {
        this.vorname = vorname;
        this.nachname = nachname;
        this.mannschaftid = mannschaftid;
        this.altersKlasse = altersKlasse;

        //id erzeugen
        id = UUID.randomUUID().toString();
    }

    /**
     * Lädt einen bestehenden Schützen aus der Datenbank.
     * @param id vorhandene id des Schützen
     * @param vorname Vorname des Schützen
     * @param nachname Nachname des Schützen
     * @param mannschaftid id der Mannschaft, der der Schütze angehört
     * @param altersKlasse Altersklasse des Schützen, z.B. Jugend, Herren
     */
    public Schuetze(String id, String vorname, String nachname, String mannschaftid, String altersKlasse) {
        this.id = id;
        this.vorname = vorname;
        this.nachname = nachname;
        this.mannschaftid = mannschaftid;
        this.altersKlasse = altersKlasse;
    }

    public String getAltersKlasse() {
        return altersKlasse;
    }

    public void setAltersKlasse(String altersKlasse) {
        this.altersKlasse = altersKlasse;
    }

    public String getVorname() {
        return vorname;
    }

    public void setVorname(String vorname) {
        this.vorname = vorname;
    }

    public String getNachname() {
        return nachname;
    }

    public void setNachname(String nachname) {
        this.nachname = nachname;
    }

    public String getMannschaftid() {
        return mannschaftid;
    }

    public void setMannschaftid(String mannschaft) {
        this.mannschaftid = mannschaft;
    }

    public String getId() {
        return id;
    }

    /**
     * Anzeigetext für die ComboBox, sonst würde dort Schuetze@123abc stehen.
     * @return Vorname und Nachname des Schützen
     */
    @Override
    public String toString() {
        return vorname + " " + nachname;
    }

}
