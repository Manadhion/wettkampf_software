package io.github.manadhion.wettkampf.data;

import java.util.UUID;

public class Begegnung {

    //Parameter
    private String id;
    private String heim;
    private String gegner;
    private String wettkampftag;


    //Konstruktor zum alegen von neuen Begegnungen
    public Begegnung(String heim, String gegner, String wettkampftag) {

        this.heim = heim;
        this.gegner = gegner;
        this.wettkampftag = wettkampftag;

        //id erzeugen
        id = UUID.randomUUID().toString();
    }

    //Konstruktor zum laden aus DB
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
