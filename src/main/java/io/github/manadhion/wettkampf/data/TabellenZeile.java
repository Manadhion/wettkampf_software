package io.github.manadhion.wettkampf.data;

//eine Zeile der Mannschaftstabelle: Name, gewonnene/verlorene Begegnungspunkte und Ringsumme der Saison
public class TabellenZeile {

    //Parameter
    private String mannschaft;  //Name der Mannschaft
    private int punkte;         //eigene Begegnungspunkte
    private int gegenPunkte;    //Begegnungspunkte der Gegner
    private int ringe;          //Summe aller Gesamtergebnisse über die Saison

    //Konstruktor, Werte starten bei 0 und werden über die Saison aufaddiert
    public TabellenZeile(String mannschaft) {
        this.mannschaft = mannschaft;
    }

    //Begegnungspunkte einer Begegnung dazuzählen
    public void punkteHinzufuegen(int eigene, int gegner) {
        this.punkte += eigene;
        this.gegenPunkte += gegner;
    }

    //Ringe einer Begegnung dazuzählen
    public void ringeHinzufuegen(int ringe) {
        this.ringe += ringe;
    }

    public String getMannschaft() {
        return mannschaft;
    }

    public int getPunkte() {
        return punkte;
    }

    public int getGegenPunkte() {
        return gegenPunkte;
    }

    public int getRinge() {
        return ringe;
    }

}
