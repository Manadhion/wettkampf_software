package io.github.manadhion.wettkampf.data;

import java.util.UUID;

//DB-Entitäten-Objekt
public class Schuetze {

    //Parameter
    private String id;
    private String vorname;
    private String nachname;
    private String mannschaft;
    private int ergebnisHeute;
    private int ergebnisGesamt;
    private int altersKlasse;

    //Konstruktor für neue Schützen
    public Schuetze(String vorname, String nachname, String mannschaft, int ergebnisHeute, int ergebnisGesamt, int altersKlasse) {
        this.vorname = vorname;
        this.nachname = nachname;
        this.mannschaft = mannschaft;
        this.ergebnisHeute = ergebnisHeute;
        this.ergebnisGesamt = ergebnisGesamt;
        this.altersKlasse = altersKlasse;

        //id erzeugen
        id = UUID.randomUUID().toString();
    }

        //Konstruktor um Schützen aus DB zu laden
    public Schuetze(String id, String vorname, String nachname, String mannschaft, int ergebnisHeute, int ergebnisGesamt, int altersKlasse) {
        this.id = id;
        this.vorname = vorname;
        this.nachname = nachname;
        this.mannschaft = mannschaft;
        this.ergebnisHeute = ergebnisHeute;
        this.ergebnisGesamt = ergebnisGesamt;
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

    public String getMannschaft() {
        return mannschaft;
    }

    public void setMannschaft(String mannschaft) {
        this.mannschaft = mannschaft;
    }

    public int getErgebnisHeute() {
        return ergebnisHeute;
    }

    public void setErgebnisHeute(int ergebnisHeute) {
        this.ergebnisHeute = ergebnisHeute;
    }

    public int getErgebnisGesamt() {
        return ergebnisGesamt;
    }

    public void setErgebnisGesamt(int ergebnisGesamt) {
        this.ergebnisGesamt = ergebnisGesamt;
    }

    public String getId() {
        return id;
    }
    
}
