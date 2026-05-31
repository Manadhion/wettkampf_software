package io.github.manadhion.wettkampf.data;

import java.util.UUID;

//DB-Entitäten-Objekt
public class Schuetze {

    //Parameter
    private String id;
    private String vorname;
    private String nachname;
    private String mannschaftid;
    private int altersKlasse;

    //Konstruktor für neue Schützen
    public Schuetze(String vorname, String nachname, String mannschaftid, int altersKlasse) {
        this.vorname = vorname;
        this.nachname = nachname;
        this.mannschaftid = mannschaftid;
        this.altersKlasse = altersKlasse;

        //id erzeugen
        id = UUID.randomUUID().toString();
    }

        //Konstruktor um Schützen aus DB zu laden
    public Schuetze(String id, String vorname, String nachname, String mannschaftid, int altersKlasse) {
        this.id = id;
        this.vorname = vorname;
        this.nachname = nachname;
        this.mannschaftid = mannschaftid;
        this.altersKlasse = altersKlasse;
    }

    public int getAltersKlasse() {
        return altersKlasse;
    }

    public void setAltersKlasse(int altersKlasse) {
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
    
}
