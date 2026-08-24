package io.github.manadhion.wettkampf.data;

/**
 * Historische Meldung eines Schützen für eine Saison.
 */
public class SaisonSchuetze {

    private final String saisonID;
    private final String schuetzeID;
    private final String vorname;
    private final String nachname;
    private final String mannschaftID;
    private final String mannschaftName;
    private final String altersklasseID;
    private final String altersklasseName;

    public SaisonSchuetze(String saisonID, String schuetzeID, String vorname, String nachname,
            String mannschaftID, String mannschaftName, String altersklasseID, String altersklasseName) {
        this.saisonID = saisonID;
        this.schuetzeID = schuetzeID;
        this.vorname = vorname;
        this.nachname = nachname;
        this.mannschaftID = mannschaftID;
        this.mannschaftName = mannschaftName;
        this.altersklasseID = altersklasseID;
        this.altersklasseName = altersklasseName;
    }

    public String getSaisonID() { return saisonID; }
    public String getSchuetzeID() { return schuetzeID; }
    public String getVorname() { return vorname; }
    public String getNachname() { return nachname; }
    public String getMannschaftID() { return mannschaftID; }
    public String getMannschaftName() { return mannschaftName; }
    public String getAltersklasseID() { return altersklasseID; }
    public String getAltersklasseName() { return altersklasseName; }
}
