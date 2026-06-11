package io.github.manadhion.wettkampf.app;

import io.github.manadhion.wettkampf.dao.AltersklasseDAO;
import io.github.manadhion.wettkampf.dao.BegegnungDAO;
import io.github.manadhion.wettkampf.dao.ErgebnisseDAO;
import io.github.manadhion.wettkampf.dao.LigaDAO;
import io.github.manadhion.wettkampf.dao.MannschaftDAO;
import io.github.manadhion.wettkampf.dao.SaisonDAO;
import io.github.manadhion.wettkampf.dao.SchuetzeDAO;
import io.github.manadhion.wettkampf.dao.WettkampftageDAO;
import io.github.manadhion.wettkampf.view.AltersklasseView;
import io.github.manadhion.wettkampf.view.BegegnungView;
import io.github.manadhion.wettkampf.view.LigaView;
import io.github.manadhion.wettkampf.view.Main;
import io.github.manadhion.wettkampf.view.MannschaftView;
import io.github.manadhion.wettkampf.view.OwnAlert;
import io.github.manadhion.wettkampf.view.SaisonView;
import io.github.manadhion.wettkampf.view.SchuetzeView;
import io.github.manadhion.wettkampf.view.WTagView;
import io.github.manadhion.wettkampf.data.Altersklasse;
import io.github.manadhion.wettkampf.data.Begegnung;
import io.github.manadhion.wettkampf.data.Ergebnisse;
import io.github.manadhion.wettkampf.data.Liga;
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
    private WTagView wTageView;
    private MannschaftView mannschaftView;
    private LigaView ligaView;
    private AltersklasseView alterView;
    private SchuetzeView schuetzeView;
    public BegegnungView begegnungView;

    //Objekt OwnAlert anlegen
    OwnAlert alert = new OwnAlert();

    //Konstruktor ohne Objekt
    public Controller() {
		super();
	}

    //Konstruktor mit Main-Objekt
    public Controller(Main viewMain) {
        this.viewMain = viewMain;
    }

    //Konstruktor mit SaisonView-Objekt
    public Controller(SaisonView saisonView) {
        this.saisonView = saisonView;
    }

    //Konstruktor mit WTagView-Objekt
    public Controller(WTagView wTageView) {
        this.wTageView = wTageView;
    }

    //Konstruktor mit MannschaftView-Objekt
    public Controller(MannschaftView mannschaftView) {
        this.mannschaftView = mannschaftView;
    }

    //Konstruktor mit Liga-Objekt
    public Controller(LigaView ligaView) {
        this.ligaView = ligaView;
    }

    //Konstruktor mit AltersklasseView-Objekt
    public Controller(AltersklasseView alterView) {
        this.alterView = alterView;
    }

    //Konstruktor mit SchuetzeView-Objekt
    public Controller(SchuetzeView schuetzeView) {
        this.schuetzeView = schuetzeView;
    }

    //Konstruktor mit BegegnungView-Objekt
    public Controller(BegegnungView begegnungView) {
        this.begegnungView = begegnungView;
    }


    //Methode Tabelle erstellen aufrufen
    public void createTableIfNotExists() {
        MannschaftDAO maDAO = new MannschaftDAO();
        BegegnungDAO bDAO = new BegegnungDAO();
        ErgebnisseDAO eDAO = new ErgebnisseDAO();
        SchuetzeDAO sDAO = new SchuetzeDAO();
        WettkampftageDAO wDAO = new WettkampftageDAO();
        SaisonDAO saDAO = new SaisonDAO();
        LigaDAO lDAO = new LigaDAO();
        AltersklasseDAO aDAO = new AltersklasseDAO();
        
        maDAO.createTableIfNotExists();
        bDAO.createTableIfNotExists();
        eDAO.createTableIfNotExists();
        sDAO.createTableIfNotExists();
        wDAO.createTableIfNotExists();
        saDAO.createTableIfNotExists();
        lDAO.createTableIfNotExists();
        aDAO.createTableIfNotExists();
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

    //alle Altersklassen abrufen
    public List<Altersklasse> alleAltersklassen() {
        AltersklasseDAO aDAO = new AltersklasseDAO();
        List<Altersklasse> aKlassen = aDAO.alleAltersklassen();
        return aKlassen;
    }

    //Ergebnis eines Schützen an einem Wettkampftag abrufen, oder null wenn es noch keins gibt
    public Ergebnisse ergebnisFuer(String schuetzeID, String wettkampftagID) {

        //Objekt zum kommunizieren mit DB erzeugen
        ErgebnisseDAO eDAO = new ErgebnisseDAO();

        //Methode aus DAO ausführen
        Ergebnisse ergebnis = eDAO.ergebnisFuerSchuetzeUndTag(schuetzeID, wettkampftagID);

        return ergebnis;
    }

    //alle Ligen abrufen
    public List<Liga> alleLigen() {

        //Objekt zum kommunizieren mit DB erzeugen
        LigaDAO lDAO = new LigaDAO();

        //Methode aus DAO ausführen
        List<Liga> ligen = lDAO.alleLigen();

        return ligen;
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
            alert.savedAlert("Ergebnis gespeichert");
        } else {
            //schon vorhanden, Wert ändern und updaten
            vorhanden.setErgebnis(wert);
            eDAO.update(vorhanden);
            alert.savedAlert("Ergebnis geändert");
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
        sView.saisonFormular(null);//Szene aufbauen, null = neu anlegen
        sView.show();
    }

    //Fenster zum Bearbeiten einer bestehenden Saison öffnen
    public void saisonBearbeitenFenster(Saison saison) {
        SaisonView sView = new SaisonView();
        sView.setController(this); //bestehenden Controller reinreichen
        this.saisonView = sView;   //Controller merkt sich die View
        sView.saisonFormular(saison);//Szene aufbauen, bestehende Saison reingeben
        sView.show();
    }

    //bestehende Saison ändern
    public void saisonAktualisieren(Saison saison) {
        SaisonDAO sDAO = new SaisonDAO();
        sDAO.update(saison);
        saisonView.close();
        viewMain.saisonComboAktualisieren();
    }

    //Prüfen ob es die Saison schon gibt
    public boolean saisonExistiert(int name) {
        SaisonDAO sDAO = new SaisonDAO();
        return sDAO.existiert(name);
    }

    //neue Saison speichern
    public void neueSaisonAnlegen (Saison saison) {
        SaisonDAO sDAO = new SaisonDAO();
        sDAO.insert(saison);
        saisonView.close();
        viewMain.saisonComboAktualisieren();
    }

    //Saison löschen-Warnung
    public void saisonLöschenWarnen(String id) {
        if (alert.saisonLoeschenBestaetigen()) { //Wenn mit ok bestätigt wurde
            saisonLoeschen(id);
        }
    }

    //Saison aus DB löschen
    public void saisonLoeschen(String id) {
        SaisonDAO sDAO = new SaisonDAO();
        int geloescht = sDAO.delete(id); //Zum testen ob tatsächlich etwas gelöscht wurde

        if (geloescht > 0) { //Wenn tatsächlich etwas gelöscht wurde
            alert.infoAlert("Saison wurde gelöscht");
            viewMain.saisonComboAktualisieren(); //Saison ComboBox aktuallisieren
        }
        else {
            alert.errorAlert("Saison konnte nicht gelöscht werden!");
        }
    }

    //Fenster zur eingabe eines neuen Wettkampftages öffnen
    public void neuerWettkampftagFenster(Saison saison) {
        WTagView wView = new WTagView();
        wView.setController(this); //bestehenden Controller reinreichen
        this.wTageView = wView;   //Controller merkt sich die View
        wView.wTagFormular(saison, null);      //Szene aufbauen, null = neu anlegen
        wView.show();
    }

    //Fenster zum Bearbeiten eines bestehenden Wettkampftages öffnen
    public void wTagBearbeitenFenster(Wettkampftage wettkampftag) {
        WTagView wView = new WTagView();
        wView.setController(this); //bestehenden Controller reinreichen
        this.wTageView = wView;   //Controller merkt sich die View
        wView.wTagFormular(null, wettkampftag);//Szene aufbauen, bestehenden Wettkampftag reingeben
        wView.show();
    }

    //neuen Wettkampftag speichern
    public void neuenWettkampftagSpeichern (Wettkampftage w, String id) {
        WettkampftageDAO wDAO = new WettkampftageDAO();
        wDAO.insert(w);
        wTageView.close();
        viewMain.wTagComboAktualisieren(id);
    }

    //bestehenden Wettkampftag ändern
    public void wTagAktualisieren (Wettkampftage w, String id) {
        WettkampftageDAO wDAO = new WettkampftageDAO();
        wDAO.update(w);
        wTageView.close();
        viewMain.wTagComboAktualisieren(id);
    }

    //Wettkampftag löschen-Warnung
    public void wTagLöschenWarnen(String id, String saisonID) {
        if (alert.wTagLoeschenBestaetigen()) { //Wenn mit ok bestätigt wurde
            wTagLoeschen(id, saisonID);
        }
    }

    //Wettkampftag aus DB löschen
    public void wTagLoeschen(String id, String saisonID) {
        WettkampftageDAO wDAO = new WettkampftageDAO();
        int geloescht = wDAO.delete(id); //Zum testen ob tatsächlich etwas gelöscht wurde

        if (geloescht > 0) { //Wenn tatsächlich etwas gelöscht wurde
            alert.infoAlert("Wettkampftag wurde gelöscht");
            viewMain.wTagComboAktualisieren(saisonID);; //Wettkampftage ComboBox aktuallisieren
        }
        else {
            alert.errorAlert("Wettkampftag konnte nicht gelöscht werden!");
        }
    }

    //Fenster zur eingabe eines neuen Wettkampftages öffnen
    public void neueeMannschaftFenster() {
        MannschaftView mView = new MannschaftView();
        mView.setController(this); //bestehenden Controller reinreichen
        this.mannschaftView = mView;   //Controller merkt sich die View
        mView.mannschaftFormular(null);//Szene aufbauen, null = neu anlegen
        mView.show();
    }

    //Fenster zum Bearbeiten einer bestehenden Mannschaft öffnen
    public void mannschaftBearbeitenFenster(Mannschaft m) {
        MannschaftView mView = new MannschaftView();
        mView.setController(this); //bestehenden Controller reinreichen
        this.mannschaftView = mView;   //Controller merkt sich die View
        mView.mannschaftFormular(m);   //Szene aufbauen, bestehende Mannschaft reingeben
        mView.show();
    }

    //neue Mannschaft speichern
    public void neueMannschaftSpeichern (Mannschaft m) {
        MannschaftDAO mDAO = new MannschaftDAO();
        mDAO.insert(m);
        mannschaftView.close();
        viewMain.mannschaftComboAktualisieren();
    }

    //bestehende Mannschaft ändern
    public void mannschaftAktualisieren (Mannschaft m) {
        MannschaftDAO mDAO = new MannschaftDAO();
        mDAO.update(m);
        mannschaftView.close();
        viewMain.mannschaftComboAktualisieren();
    }

    //Fenster zur eingabe einer neuen Liga öffnen
    public void neueLigaFenster() {
        LigaView lView = new LigaView();
        lView.setController(this); //bestehenden Controller reinreichen
        this.ligaView = lView;   //Controller merkt sich die View
        lView.ligaFormular(null);//Szene aufbauen, null = neu anlegen
        lView.show();
    }

    //Fenster zum Bearbeiten einer bestehenden Liga öffnen
    public void ligaBearbeitenFenster(Liga l) {
        LigaView lView = new LigaView();
        lView.setController(this); //bestehenden Controller reinreichen
        this.ligaView = lView;   //Controller merkt sich die View
        lView.ligaFormular(l);   //Szene aufbauen, bestehende Liga reingeben
        lView.show();
    }

    //neue Liga speichern
    public void neueLigaSpeichern (Liga l) {
        LigaDAO lDAO = new LigaDAO();
        lDAO.insert(l);
        ligaView.close();
        mannschaftView.ligaComboAktualisieren();
    }

    //bestehende Liga ändern
    public void ligaAktualisieren (Liga l) {
        LigaDAO lDAO = new LigaDAO();
        lDAO.update(l);
        ligaView.close();
        mannschaftView.ligaComboAktualisieren();
    }

    //Liga löschen-Warnung
    public void ligaLöschenWarnen(String id) {
        if (alert.ligaBestaetigen()) { //Wenn mit ok bestätigt wurde
           ligaLoeschen(id);
        }
    }

    //Liga aus DB löschen
    public void ligaLoeschen(String id) {
        LigaDAO lDAO = new LigaDAO();
        int geloescht = lDAO.delete(id); //Zum testen ob tatsächlich etwas gelöscht wurde

        if (geloescht > 0) { //Wenn tatsächlich etwas gelöscht wurde
            alert.infoAlert("Liga wurde gelöscht");
            mannschaftView.ligaComboAktualisieren();
        }
        else {
            alert.errorAlert("Liga konnte nicht gelöscht werden!");
        }
    }

    //Liga mit ID finden
    public Liga ligaMitIdFinden(String id) {
        LigaDAO lDAO = new LigaDAO();
        Liga l = lDAO.ligaMitIdFinden(id);
        return l;
    }

    //Mannschaft löschen-Warnung
    public void mannschaftLöschenWarnen(String id) {
        if (alert.mannschaftBestaetigen()) { //Wenn mit ok bestätigt wurde
           mannschaftLoeschen(id);
        }
    }

    //Mannschaft aus DB löschen
    public void mannschaftLoeschen(String id) {
        MannschaftDAO mDAO = new MannschaftDAO();
        int geloescht = mDAO.delete(id); //Zum testen ob tatsächlich etwas gelöscht wurde

        if (geloescht > 0) { //Wenn tatsächlich etwas gelöscht wurde
            alert.infoAlert("Mannschaft wurde gelöscht");
            viewMain.mannschaftComboAktualisieren();
        }
        else {
            alert.errorAlert("Mannschaft konnte nicht gelöscht werden!");
        }
    }

    //Fenster zur eingabe einer neuen Altersklasse öffnen
    public void neueAlterFenster() {
        AltersklasseView aView = new AltersklasseView();
        aView.setController(this); //bestehenden Controller reinreichen
        this.alterView = aView;   //Controller merkt sich die View
        aView.alterFormular(null);//Szene aufbauen, null = neu anlegen
        aView.show();
    }

    //Fenster zum Bearbeiten einer bestehenden Altersklasse öffnen
    public void alterBearbeitenFenster(Altersklasse a) {
        AltersklasseView aView = new AltersklasseView();
        aView.setController(this); //bestehenden Controller reinreichen
        this.alterView = aView;   //Controller merkt sich die View
        aView.alterFormular(a);   //Szene aufbauen, bestehende Altersklasse reingeben
        aView.show();
    }

    //neue Altersklasse speichern
    public void neueAlterSpeichern (Altersklasse a) {
        AltersklasseDAO aDAO = new AltersklasseDAO();
        aDAO.insert(a);
        alterView.close();
        schuetzeView.alterComboAktualisieren();;
    }

    //bestehende Altersklasse ändern
    public void alterAktualisieren (Altersklasse a) {
        AltersklasseDAO aDAO = new AltersklasseDAO();
        aDAO.update(a);
        alterView.close();
        schuetzeView.alterComboAktualisieren();
    }

    //Altersklasse löschen-Warnung
    public void alterLöschenWarnen(String id) {
        if (alert.alterBestaetigen()) { //Wenn mit ok bestätigt wurde
           alterLoeschen(id);
        }
    }

    //Altersklasse aus DB löschen
    public void alterLoeschen(String id) {
        AltersklasseDAO aDAO = new AltersklasseDAO();
        int geloescht = aDAO.delete(id); //Zum testen ob tatsächlich etwas gelöscht wurde

        if (geloescht > 0) { //Wenn tatsächlich etwas gelöscht wurde
            alert.infoAlert("Altersklasse wurde gelöscht");
            schuetzeView.alterComboAktualisieren();
        }
        else {
            alert.errorAlert("Altersklasse konnte nicht gelöscht werden!");
        }
    }

    //Fenster zur eingabe eines neuen Schützen öffnen
    public void neueSchuetzeFenster(Mannschaft m) {
        SchuetzeView sView = new SchuetzeView();
        sView.setController(this); //bestehenden Controller reinreichen
        this.schuetzeView = sView;   //Controller merkt sich die View
        sView.schuetzeFormular(m, null);//Szene aufbauen, null = neu anlegen
        sView.show();
    }

    //Fenster zum Bearbeiten eines bestehenden Schützen öffnen
    public void schuetzeBearbeitenFenster(Schuetze s) {
        SchuetzeView sView = new SchuetzeView();
        sView.setController(this); //bestehenden Controller reinreichen
        this.schuetzeView = sView;   //Controller merkt sich die View
        sView.schuetzeFormular(null, s);//Szene aufbauen, bestehenden Schützen reingeben
        sView.show();
    }

    //neuen Schützen speichern
    public void neuenSchuetzenpeichern (Schuetze s, String id) {
        SchuetzeDAO sDAO = new SchuetzeDAO();
        sDAO.insert(s);
        schuetzeView.close();
        viewMain.schuetzeComboAktualisieren(id);
    }

    //bestehenden Schützen ändern
    public void schuetzeAktualisieren (Schuetze s, String id) {
        SchuetzeDAO sDAO = new SchuetzeDAO();
        sDAO.update(s);
        schuetzeView.close();
        viewMain.schuetzeComboAktualisieren(id);
    }

    //Schütze löschen-Warnung
    public void schuetzeLöschenWarnen(String id) {
        if (alert.alterBestaetigen()) { //Wenn mit ok bestätigt wurde
           schuetzeLoeschen(id);
        }
    }

    //Schütze aus DB löschen
    public void schuetzeLoeschen(String id) {
        SchuetzeDAO sDAO = new SchuetzeDAO();
        int geloescht = sDAO.delete(id); //Zum testen ob tatsächlich etwas gelöscht wurde

        if (geloescht > 0) { //Wenn tatsächlich etwas gelöscht wurde
            alert.infoAlert("Schütze wurde gelöscht");
            viewMain.mannschaftComboAktualisieren();
        }
        else {
            alert.errorAlert("Schütze konnte nicht gelöscht werden!");
        }
    }

    //neue Begegnung speichern
    public void neueBegegnungSpeichern (Begegnung b) {
        BegegnungDAO bDAO = new BegegnungDAO();
        bDAO.insert(b);
        begegnungView.close();
        viewMain.begegnungenAnzeigen();
    }

    //Fenster zur eingabe einer neuen Begegnung öffnen
    public void neueBegegnungFenster(String id) {
        BegegnungView bView = new BegegnungView();
        bView.setController(this); //bestehenden Controller reinreichen
        this.begegnungView = bView;   //Controller merkt sich die View
        bView.newBegegnung(id);        //Szene aufbauen
        bView.show();
    }

      //Begegnung löschen-Warnung
    public void begegnungLöschenWarnen(String id) {
        if (alert.begegnungBestaetigen()) { //Wenn mit ok bestätigt wurde
           begegnungLoeschen(id);
        }
    }

    //Schütze aus DB löschen
    public void begegnungLoeschen(String id) {
        BegegnungDAO bDAO = new BegegnungDAO();
        int geloescht = bDAO.delete(id); //Zum testen ob tatsächlich etwas gelöscht wurde

        if (geloescht > 0) { //Wenn tatsächlich etwas gelöscht wurde
            alert.infoAlert("Begegnung wurde gelöscht");
            viewMain.begegnungenAnzeigen();;
        }
        else {
            alert.errorAlert("Begegnung konnte nicht gelöscht werden!");
        }
    }
}
