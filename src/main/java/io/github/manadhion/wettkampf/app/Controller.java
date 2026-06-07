package io.github.manadhion.wettkampf.app;

import io.github.manadhion.wettkampf.dao.BegegnungDAO;
import io.github.manadhion.wettkampf.dao.ErgebnisseDAO;
import io.github.manadhion.wettkampf.dao.MannschaftDAO;
import io.github.manadhion.wettkampf.dao.SaisonDAO;
import io.github.manadhion.wettkampf.dao.SchuetzeDAO;
import io.github.manadhion.wettkampf.dao.WettkampftageDAO;
import io.github.manadhion.wettkampf.view.Main;
import io.github.manadhion.wettkampf.view.SaisonView;
import io.github.manadhion.wettkampf.data.Begegnung;
import io.github.manadhion.wettkampf.data.Ergebnisse;
import io.github.manadhion.wettkampf.data.Mannschaft;
import io.github.manadhion.wettkampf.data.Saison;
import io.github.manadhion.wettkampf.data.Schuetze;
import io.github.manadhion.wettkampf.data.Wettkampftage;
import java.util.List;

//Controller um Datenstrom gezielt zu lenken
public class Controller {
    
    //Instanzen der App-Fenster
    private Main viewMain;
    private SaisonView saisonView;

    //Constructor mit Main-Objekt
    public Controller(Main viewMain) {
        this.viewMain = viewMain;
    }

    //Konstruktor mit SaisonView-Objekt
    public Controller(SaisonView saisonView) {
        this.saisonView = saisonView;
    }


    //Methode Tabelle erstellen aufrufen
    public void createTableIfNotExists() {
        MannschaftDAO maDAO = new MannschaftDAO();
        BegegnungDAO bDAO = new BegegnungDAO();
        ErgebnisseDAO eDAO = new ErgebnisseDAO();
        SchuetzeDAO sDAO = new SchuetzeDAO();
        WettkampftageDAO wDAO = new WettkampftageDAO();
        SaisonDAO saDAO = new SaisonDAO();
        
        maDAO.createTableIfNotExists();
        bDAO.createTableIfNotExists();
        eDAO.createTableIfNotExists();
        sDAO.createTableIfNotExists();
        wDAO.createTableIfNotExists();
        saDAO.createTableIfNotExists();
    }

    //alle Wettkampftage abrufen
    public List<Wettkampftage> alleWettkampfTage () {

        //Objekt zum kommunizieren mit DB erzeugen
        WettkampftageDAO wkDAO = new WettkampftageDAO();

        //Methode aus DAO ausführen
        List<Wettkampftage> wettkampftage = wkDAO.alleTage();

        return wettkampftage;
    }

    //alle Wettkampftage einer bestimmten Saison abrufen
    public List<Wettkampftage> wettkampftageVonSaison(String saisonID) {

        //Objekt zum kommunizieren mit DB erzeugen
        WettkampftageDAO wkDAO = new WettkampftageDAO();

        //Methode aus DAO ausführen
        List<Wettkampftage> wettkampftage = wkDAO.tageVonSaison(saisonID);

        return wettkampftage;
    }

    //alle Mannschaften abrufen
    public List<Mannschaft> alleMannschaften () {

        //Objekt zum kommunizieren mit DB erzeugen
        MannschaftDAO mDAO = new MannschaftDAO();

        //Methode aus DAO ausführen
        List<Mannschaft> mannschaften = mDAO.alleMannschaften();

        return mannschaften;
    }

    //alle Schützen einer Mannschaft abrufen
    public List<Schuetze> schuetzenVonMannschaft(String id) {

        //Objekt zum kommunizieren mit DB erzeugen
        SchuetzeDAO sDAO = new SchuetzeDAO();

        //Methode aus DAO ausführen
        List<Schuetze> schuetze = sDAO.schuetzenVonMannschaft(id);

        return schuetze;
    }

    //Ergebnis eines Schützen an einem Wettkampftag abrufen, oder null wenn es noch keins gibt
    public Ergebnisse ergebnisFuer(String schuetzeID, String wettkampftagID) {

        //Objekt zum kommunizieren mit DB erzeugen
        ErgebnisseDAO eDAO = new ErgebnisseDAO();

        //Methode aus DAO ausführen
        Ergebnisse ergebnis = eDAO.ergebnisFuerSchuetzeUndTag(schuetzeID, wettkampftagID);

        return ergebnis;
    }

    //Ergebnis speichern: neu anlegen wenn es noch keins gibt, sonst den Wert ändern
    public void ergebnisSpeichern(String schuetzeID, String wettkampftagID, int wert) {

        //Objekt zum kommunizieren mit DB erzeugen
        ErgebnisseDAO eDAO = new ErgebnisseDAO();

        //prüfen ob für diesen Schützen an diesem Tag schon ein Ergebnis existiert
        Ergebnisse vorhanden = eDAO.ergebnisFuerSchuetzeUndTag(schuetzeID, wettkampftagID);

        if (vorhanden == null) {
            //noch keins, neues Ergebnis anlegen
            eDAO.insert(new Ergebnisse(schuetzeID, wettkampftagID, wert));
        } else {
            //schon vorhanden, Wert ändern und updaten
            vorhanden.setErgebnis(wert);
            eDAO.update(vorhanden);
        }
    }

    //alle Saisons abfragen
    public List<Saison> alleSaisons() {
        SaisonDAO sDAO = new SaisonDAO();
        List<Saison> saison = sDAO.alleSaisons();
        return saison;
    }

    //Begegnungen eines Wettkampftages abfragen
    public List<Begegnung> begegnungenAnDiesemTag(String wettkampftag) {
        BegegnungDAO bDAO = new BegegnungDAO();
        List<Begegnung> begegnungen = bDAO.begegnungenAnDiesemTag(wettkampftag);
        return begegnungen;
    }

    //Mannschaft mit ID finden
    public Mannschaft mannschaftMitID(String mID) {
        MannschaftDAO mDAO = new MannschaftDAO();
        Mannschaft m = mDAO.mannschaftMitID(mID);
        return m;
    }

    //Fenster zur eingabe einer neuen Saison öffnen
    public void neueSaisonFenster() {
        SaisonView sView = new SaisonView();
        sView.setController(this); //bestehenden Controller reinreichen
        this.saisonView = sView;   //Controller merkt sich die View
        sView.newSaison();         //Szene aufbauen
        sView.show();
    }

    //neue Saison speichern
    public void neueSaisonAnlegen (Saison saison) {
        SaisonDAO sDAO = new SaisonDAO();
        sDAO.insert(saison);
        saisonView.close();
        viewMain.saisonComboAktualisieren();
    }
}
