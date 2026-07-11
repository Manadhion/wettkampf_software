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
import io.github.manadhion.wettkampf.view.BeamerView;
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
import java.io.File;
import java.util.List;

/**
 * Vermittelt zwischen den Fenstern (view) und den Data Access-Objekten (dao) und lenkt so den Datenstrom.
 */
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
    private BeamerView beamerView;

    //Objekt OwnAlert anlegen
    OwnAlert alert = new OwnAlert();

    /**
     * Konstruktor ohne zugehöriges Fenster.
     */
    public Controller() {
		super();
	}

    /**
     * Konstruktor mit Hauptfenster.
     * @param viewMain das Hauptfenster
     */
    public Controller(Main viewMain) {
        this.viewMain = viewMain;
    }

    /**
     * Konstruktor mit Saison-Fenster.
     * @param saisonView das Saison-Fenster
     */
    public Controller(SaisonView saisonView) {
        this.saisonView = saisonView;
    }

    /**
     * Konstruktor mit Wettkampftag-Fenster.
     * @param wTageView das Wettkampftag-Fenster
     */
    public Controller(WTagView wTageView) {
        this.wTageView = wTageView;
    }

    /**
     * Konstruktor mit Mannschaft-Fenster.
     * @param mannschaftView das Mannschaft-Fenster
     */
    public Controller(MannschaftView mannschaftView) {
        this.mannschaftView = mannschaftView;
    }

    /**
     * Konstruktor mit Liga-Fenster.
     * @param ligaView das Liga-Fenster
     */
    public Controller(LigaView ligaView) {
        this.ligaView = ligaView;
    }

    /**
     * Konstruktor mit Altersklasse-Fenster.
     * @param alterView das Altersklasse-Fenster
     */
    public Controller(AltersklasseView alterView) {
        this.alterView = alterView;
    }

    /**
     * Konstruktor mit Schütze-Fenster.
     * @param schuetzeView das Schütze-Fenster
     */
    public Controller(SchuetzeView schuetzeView) {
        this.schuetzeView = schuetzeView;
    }

    /**
     * Konstruktor mit Begegnung-Fenster.
     * @param begegnungView das Begegnung-Fenster
     */
    public Controller(BegegnungView begegnungView) {
        this.begegnungView = begegnungView;
    }


    /**
     * Legt alle Tabellen der Datenbank an, falls sie noch nicht existieren.
     */
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

    /**
     * Alle Wettkampftage abrufen.
     * @return Liste aller Wettkampftage
     */
    public List<Wettkampftage> alleWettkampfTage () {

        //Objekt zum kommunizieren mit DB erzeugen
        WettkampftageDAO wkDAO = new WettkampftageDAO();

        //Methode aus DAO ausführen
        List<Wettkampftage> wettkampftage = wkDAO.alleTage();

        return wettkampftage;
    }

    /**
     * Alle Wettkampftage einer bestimmten Saison abrufen.
     * @param saisonID id der Saison
     * @return Liste der Wettkampftage dieser Saison
     */
    public List<Wettkampftage> wettkampftageVonSaison(String saisonID) {

        //Objekt zum kommunizieren mit DB erzeugen
        WettkampftageDAO wkDAO = new WettkampftageDAO();

        //Methode aus DAO ausführen
        List<Wettkampftage> wettkampftage = wkDAO.tageVonSaison(saisonID);

        return wettkampftage;
    }

    /**
     * Alle Mannschaften abrufen.
     * @return Liste aller Mannschaften
     */
    public List<Mannschaft> alleMannschaften () {

        //Objekt zum kommunizieren mit DB erzeugen
        MannschaftDAO mDAO = new MannschaftDAO();

        //Methode aus DAO ausführen
        List<Mannschaft> mannschaften = mDAO.alleMannschaften();

        return mannschaften;
    }

    /**
     * Alle Schützen einer Mannschaft abrufen.
     * @param id id der Mannschaft
     * @return Liste der Schützen dieser Mannschaft
     */
    public List<Schuetze> schuetzenVonMannschaft(String id) {

        //Objekt zum kommunizieren mit DB erzeugen
        SchuetzeDAO sDAO = new SchuetzeDAO();

        //Methode aus DAO ausführen
        List<Schuetze> schuetze = sDAO.schuetzenVonMannschaft(id);

        return schuetze;
    }

    /**
     * Alle Altersklassen abrufen.
     * @return Liste aller Altersklassen
     */
    public List<Altersklasse> alleAltersklassen() {
        AltersklasseDAO aDAO = new AltersklasseDAO();
        List<Altersklasse> aKlassen = aDAO.alleAltersklassen();
        return aKlassen;
    }

    /**
     * Ergebnis eines Schützen an einem Wettkampftag abrufen.
     * @param schuetzeID id des Schützen
     * @param wettkampftagID id des Wettkampftages
     * @return gefundenes Ergebnis, oder null wenn es noch keins gibt
     */
    public Ergebnisse ergebnisFuer(String schuetzeID, String wettkampftagID) {

        //Objekt zum kommunizieren mit DB erzeugen
        ErgebnisseDAO eDAO = new ErgebnisseDAO();

        //Methode aus DAO ausführen
        Ergebnisse ergebnis = eDAO.ergebnisFuerSchuetzeUndTag(schuetzeID, wettkampftagID);

        return ergebnis;
    }

    /**
     * Gesamtergebnis einer Mannschaft an einem Tag (Summe der besten 3 Schützen).
     * @param mannschaftID id der Mannschaft
     * @param wettkampftagID id des Wettkampftages
     * @return Summe der besten 3 Ergebnisse, 0 wenn noch keine Ergebnisse vorliegen
     */
    public int gesamtErgebnisBeste3(String mannschaftID, String wettkampftagID) {

        //Objekt zum kommunizieren mit DB erzeugen
        ErgebnisseDAO eDAO = new ErgebnisseDAO();

        //Methode aus DAO ausführen
        return eDAO.gesamtErgebnisBeste3(mannschaftID, wettkampftagID);
    }

    /**
     * Alle Ligen abrufen.
     * @return Liste aller Ligen
     */
    public List<Liga> alleLigen() {

        //Objekt zum kommunizieren mit DB erzeugen
        LigaDAO lDAO = new LigaDAO();

        //Methode aus DAO ausführen
        List<Liga> ligen = lDAO.alleLigen();

        return ligen;
    }

    /**
     * Ergebnis speichern: neu anlegen wenn es noch keins gibt, sonst den Wert ändern.
     * @param schuetzeID id des Schützen
     * @param wettkampftagID id des Wettkampftages
     * @param wert geschossene Ringzahl
     */
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

    /**
     * Alle Saisons abfragen.
     * @return Liste aller Saisons
     */
    public List<Saison> alleSaisons() {
        SaisonDAO sDAO = new SaisonDAO();
        List<Saison> saison = sDAO.alleSaisons();
        return saison;
    }

    /**
     * Saison mit id finden.
     * @param id id der gesuchten Saison
     * @return gefundene Saison, oder null wenn es keine gibt
     */
    public Saison saisonMitId(String id) {
        SaisonDAO sDAO = new SaisonDAO();
        return sDAO.saisonMitId(id);
    }

    /**
     * Begegnungen eines Wettkampftages abfragen.
     * @param wettkampftag id des Wettkampftages
     * @return Liste der Begegnungen an diesem Tag
     */
    public List<Begegnung> begegnungenAnDiesemTag(String wettkampftag) {
        BegegnungDAO bDAO = new BegegnungDAO();
        List<Begegnung> begegnungen = bDAO.begegnungenAnDiesemTag(wettkampftag);
        return begegnungen;
    }

    /**
     * Mannschaft mit id finden.
     * @param mID id der gesuchten Mannschaft
     * @return gefundene Mannschaft, oder null wenn es keine gibt
     */
    public Mannschaft mannschaftMitID(String mID) {
        MannschaftDAO mDAO = new MannschaftDAO();
        Mannschaft m = mDAO.mannschaftMitID(mID);
        return m;
    }

    /**
     * Fenster zur Eingabe einer neuen Saison öffnen.
     */
    public void neueSaisonFenster() {
        SaisonView sView = new SaisonView();
        sView.setController(this); //bestehenden Controller reinreichen
        this.saisonView = sView;   //Controller merkt sich die View
        sView.saisonFormular(null);//Szene aufbauen, null = neu anlegen
        sView.show();
    }

    /**
     * Fenster zum Bearbeiten einer bestehenden Saison öffnen.
     * @param saison zu bearbeitende Saison
     */
    public void saisonBearbeitenFenster(Saison saison) {
        SaisonView sView = new SaisonView();
        sView.setController(this); //bestehenden Controller reinreichen
        this.saisonView = sView;   //Controller merkt sich die View
        sView.saisonFormular(saison);//Szene aufbauen, bestehende Saison reingeben
        sView.show();
    }

    /**
     * Bestehende Saison ändern und das Hauptfenster aktualisieren.
     * @param saison Saison mit den geänderten Werten
     */
    public void saisonAktualisieren(Saison saison) {
        SaisonDAO sDAO = new SaisonDAO();
        sDAO.update(saison);
        saisonView.close();
        viewMain.saisonComboAktualisieren();
    }

    /**
     * Prüfen ob es die Saison schon gibt.
     * @param name Spieljahr als Zahl, z.B. 2526 für 25/26
     * @return true wenn bereits eine Saison mit diesem Namen existiert
     */
    public boolean saisonExistiert(int name) {
        SaisonDAO sDAO = new SaisonDAO();
        return sDAO.existiert(name);
    }

    /**
     * Neue Saison speichern und das Hauptfenster aktualisieren.
     * @param saison anzulegende Saison
     */
    public void neueSaisonAnlegen (Saison saison) {
        SaisonDAO sDAO = new SaisonDAO();
        sDAO.insert(saison);
        saisonView.close();
        viewMain.saisonComboAktualisieren();
        viewMain.saisonAuswaehlen(saison.getId()); //neu angelegte Saison sofort auswählen
    }

    /**
     * Vor dem Löschen einer Saison nachfragen und bei Bestätigung löschen.
     * @param id id der zu löschenden Saison
     */
    public void saisonLöschenWarnen(String id) {
        if (alert.saisonLoeschenBestaetigen()) { //Wenn mit ok bestätigt wurde
            saisonLoeschen(id);
        }
    }

    /**
     * Saison aus der Datenbank löschen und das Hauptfenster aktualisieren.
     * @param id id der zu löschenden Saison
     */
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

    /**
     * Fenster zur Eingabe eines neuen Wettkampftages öffnen.
     * @param saison Saison, zu der der neue Wettkampftag gehört
     */
    public void neuerWettkampftagFenster(Saison saison) {
        WTagView wView = new WTagView();
        wView.setController(this); //bestehenden Controller reinreichen
        this.wTageView = wView;   //Controller merkt sich die View
        wView.wTagFormular(saison, null);      //Szene aufbauen, null = neu anlegen
        wView.show();
    }

    /**
     * Fenster zum Bearbeiten eines bestehenden Wettkampftages öffnen.
     * @param wettkampftag zu bearbeitender Wettkampftag
     */
    public void wTagBearbeitenFenster(Wettkampftage wettkampftag) {
        WTagView wView = new WTagView();
        wView.setController(this); //bestehenden Controller reinreichen
        this.wTageView = wView;   //Controller merkt sich die View
        wView.wTagFormular(null, wettkampftag);//Szene aufbauen, bestehenden Wettkampftag reingeben
        wView.show();
    }

    /**
     * Neuen Wettkampftag speichern und das Hauptfenster aktualisieren.
     * @param w anzulegender Wettkampftag
     * @param id id der Saison, deren Wettkampftage-ComboBox aktualisiert wird
     */
    public void neuenWettkampftagSpeichern (Wettkampftage w, String id) {
        WettkampftageDAO wDAO = new WettkampftageDAO();
        wDAO.insert(w);
        wTageView.close();
        viewMain.wTagComboAktualisieren(id);
        viewMain.wTagAuswaehlen(w.getId()); //neu angelegten Wettkampftag sofort auswählen
    }

    /**
     * Bestehenden Wettkampftag ändern und das Hauptfenster aktualisieren.
     * @param w Wettkampftag mit den geänderten Werten
     * @param id id der Saison, deren Wettkampftage-ComboBox aktualisiert wird
     */
    public void wTagAktualisieren (Wettkampftage w, String id) {
        WettkampftageDAO wDAO = new WettkampftageDAO();
        wDAO.update(w);
        wTageView.close();
        viewMain.wTagComboAktualisieren(id);
    }

    /**
     * Vor dem Löschen eines Wettkampftages nachfragen und bei Bestätigung löschen.
     * @param id id des zu löschenden Wettkampftages
     * @param saisonID id der Saison, deren Wettkampftage-ComboBox aktualisiert wird
     */
    public void wTagLöschenWarnen(String id, String saisonID) {
        if (alert.wTagLoeschenBestaetigen()) { //Wenn mit ok bestätigt wurde
            wTagLoeschen(id, saisonID);
        }
    }

    /**
     * Wettkampftag aus der Datenbank löschen und das Hauptfenster aktualisieren.
     * @param id id des zu löschenden Wettkampftages
     * @param saisonID id der Saison, deren Wettkampftage-ComboBox aktualisiert wird
     */
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

    /**
     * Fenster zur Eingabe einer neuen Mannschaft öffnen.
     */
    public void neueeMannschaftFenster() {
        MannschaftView mView = new MannschaftView();
        mView.setController(this); //bestehenden Controller reinreichen
        this.mannschaftView = mView;   //Controller merkt sich die View
        mView.mannschaftFormular(null);//Szene aufbauen, null = neu anlegen
        mView.show();
    }

    /**
     * Fenster zum Bearbeiten einer bestehenden Mannschaft öffnen.
     * @param m zu bearbeitende Mannschaft
     */
    public void mannschaftBearbeitenFenster(Mannschaft m) {
        MannschaftView mView = new MannschaftView();
        mView.setController(this); //bestehenden Controller reinreichen
        this.mannschaftView = mView;   //Controller merkt sich die View
        mView.mannschaftFormular(m);   //Szene aufbauen, bestehende Mannschaft reingeben
        mView.show();
    }

    /**
     * Neue Mannschaft speichern und das Hauptfenster aktualisieren.
     * @param m anzulegende Mannschaft
     */
    public void neueMannschaftSpeichern (Mannschaft m) {
        MannschaftDAO mDAO = new MannschaftDAO();
        mDAO.insert(m);
        mannschaftView.close();
        viewMain.mannschaftComboAktualisieren();
        viewMain.mannschaftAuswaehlen(m.getId()); //neu angelegte Mannschaft sofort auswählen
    }

    /**
     * Bestehende Mannschaft ändern und das Hauptfenster aktualisieren.
     * @param m Mannschaft mit den geänderten Werten
     */
    public void mannschaftAktualisieren (Mannschaft m) {
        MannschaftDAO mDAO = new MannschaftDAO();
        mDAO.update(m);
        mannschaftView.close();
        viewMain.mannschaftComboAktualisieren();
    }

    /**
     * Fenster zur Eingabe einer neuen Liga öffnen.
     */
    public void neueLigaFenster() {
        LigaView lView = new LigaView();
        lView.setController(this); //bestehenden Controller reinreichen
        this.ligaView = lView;   //Controller merkt sich die View
        lView.ligaFormular(null);//Szene aufbauen, null = neu anlegen
        lView.show();
    }

    /**
     * Fenster zum Bearbeiten einer bestehenden Liga öffnen.
     * @param l zu bearbeitende Liga
     */
    public void ligaBearbeitenFenster(Liga l) {
        LigaView lView = new LigaView();
        lView.setController(this); //bestehenden Controller reinreichen
        this.ligaView = lView;   //Controller merkt sich die View
        lView.ligaFormular(l);   //Szene aufbauen, bestehende Liga reingeben
        lView.show();
    }

    /**
     * Neue Liga speichern und die Liga-ComboBox im Mannschaft-Fenster aktualisieren.
     * @param l anzulegende Liga
     */
    public void neueLigaSpeichern (Liga l) {
        LigaDAO lDAO = new LigaDAO();
        lDAO.insert(l);
        ligaView.close();
        mannschaftView.ligaComboAktualisieren();
    }

    /**
     * Bestehende Liga ändern und die Liga-ComboBox im Mannschaft-Fenster aktualisieren.
     * @param l Liga mit den geänderten Werten
     */
    public void ligaAktualisieren (Liga l) {
        LigaDAO lDAO = new LigaDAO();
        lDAO.update(l);
        ligaView.close();
        mannschaftView.ligaComboAktualisieren();
    }

    /**
     * Vor dem Löschen einer Liga nachfragen und bei Bestätigung löschen.
     * @param id id der zu löschenden Liga
     */
    public void ligaLöschenWarnen(String id) {
        if (alert.ligaBestaetigen()) { //Wenn mit ok bestätigt wurde
           ligaLoeschen(id);
        }
    }

    /**
     * Liga aus der Datenbank löschen und die Liga-ComboBox im Mannschaft-Fenster aktualisieren.
     * @param id id der zu löschenden Liga
     */
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

    /**
     * Liga mit id finden.
     * @param id id der gesuchten Liga
     * @return gefundene Liga
     */
    public Liga ligaMitIdFinden(String id) {
        LigaDAO lDAO = new LigaDAO();
        Liga l = lDAO.ligaMitIdFinden(id);
        return l;
    }

    /**
     * Vor dem Löschen einer Mannschaft nachfragen und bei Bestätigung löschen.
     * @param id id der zu löschenden Mannschaft
     */
    public void mannschaftLöschenWarnen(String id) {
        if (alert.mannschaftBestaetigen()) { //Wenn mit ok bestätigt wurde
           mannschaftLoeschen(id);
        }
    }

    /**
     * Mannschaft aus der Datenbank löschen und das Hauptfenster aktualisieren.
     * @param id id der zu löschenden Mannschaft
     */
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

    /**
     * Fenster zur Eingabe einer neuen Altersklasse öffnen.
     */
    public void neueAlterFenster() {
        AltersklasseView aView = new AltersklasseView();
        aView.setController(this); //bestehenden Controller reinreichen
        this.alterView = aView;   //Controller merkt sich die View
        aView.alterFormular(null);//Szene aufbauen, null = neu anlegen
        aView.show();
    }

    /**
     * Fenster zum Bearbeiten einer bestehenden Altersklasse öffnen.
     * @param a zu bearbeitende Altersklasse
     */
    public void alterBearbeitenFenster(Altersklasse a) {
        AltersklasseView aView = new AltersklasseView();
        aView.setController(this); //bestehenden Controller reinreichen
        this.alterView = aView;   //Controller merkt sich die View
        aView.alterFormular(a);   //Szene aufbauen, bestehende Altersklasse reingeben
        aView.show();
    }

    /**
     * Neue Altersklasse speichern und die Altersklasse-ComboBox im Schütze-Fenster aktualisieren.
     * @param a anzulegende Altersklasse
     */
    public void neueAlterSpeichern (Altersklasse a) {
        AltersklasseDAO aDAO = new AltersklasseDAO();
        aDAO.insert(a);
        alterView.close();
        schuetzeView.alterComboAktualisieren();;
    }

    /**
     * Bestehende Altersklasse ändern und die Altersklasse-ComboBox im Schütze-Fenster aktualisieren.
     * @param a Altersklasse mit den geänderten Werten
     */
    public void alterAktualisieren (Altersklasse a) {
        AltersklasseDAO aDAO = new AltersklasseDAO();
        aDAO.update(a);
        alterView.close();
        schuetzeView.alterComboAktualisieren();
    }

    /**
     * Vor dem Löschen einer Altersklasse nachfragen und bei Bestätigung löschen.
     * @param id id der zu löschenden Altersklasse
     */
    public void alterLöschenWarnen(String id) {
        if (alert.alterBestaetigen()) { //Wenn mit ok bestätigt wurde
           alterLoeschen(id);
        }
    }

    /**
     * Altersklasse aus der Datenbank löschen und die Altersklasse-ComboBox im Schütze-Fenster aktualisieren.
     * @param id id der zu löschenden Altersklasse
     */
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

    /**
     * Fenster zur Eingabe eines neuen Schützen öffnen.
     * @param m Mannschaft, der der neue Schütze zugeordnet wird
     */
    public void neueSchuetzeFenster(Mannschaft m) {
        SchuetzeView sView = new SchuetzeView();
        sView.setController(this); //bestehenden Controller reinreichen
        this.schuetzeView = sView;   //Controller merkt sich die View
        sView.schuetzeFormular(m, null);//Szene aufbauen, null = neu anlegen
        sView.show();
    }

    /**
     * Fenster zum Bearbeiten eines bestehenden Schützen öffnen.
     * @param s zu bearbeitender Schütze
     */
    public void schuetzeBearbeitenFenster(Schuetze s) {
        SchuetzeView sView = new SchuetzeView();
        sView.setController(this); //bestehenden Controller reinreichen
        this.schuetzeView = sView;   //Controller merkt sich die View
        sView.schuetzeFormular(null, s);//Szene aufbauen, bestehenden Schützen reingeben
        sView.show();
    }

    /**
     * Neuen Schützen speichern und das Hauptfenster aktualisieren.
     * @param s anzulegender Schütze
     * @param id id der Mannschaft, deren Schützen-ComboBox aktualisiert wird
     */
    public void neuenSchuetzenpeichern (Schuetze s, String id) {
        SchuetzeDAO sDAO = new SchuetzeDAO();
        sDAO.insert(s);
        schuetzeView.close();
        viewMain.schuetzeComboAktualisieren(id);
        viewMain.schuetzeAuswaehlen(s.getId()); //neu angelegten Schützen sofort auswählen
    }

    /**
     * Bestehenden Schützen ändern und das Hauptfenster aktualisieren.
     * @param s Schütze mit den geänderten Werten
     * @param id id der Mannschaft, deren Schützen-ComboBox aktualisiert wird
     */
    public void schuetzeAktualisieren (Schuetze s, String id) {
        SchuetzeDAO sDAO = new SchuetzeDAO();
        sDAO.update(s);
        schuetzeView.close();
        viewMain.schuetzeComboAktualisieren(id);
    }

    /**
     * Vor dem Löschen eines Schützen nachfragen und bei Bestätigung löschen.
     * @param id id des zu löschenden Schützen
     */
    public void schuetzeLöschenWarnen(String id) {
        if (alert.alterBestaetigen()) { //Wenn mit ok bestätigt wurde
           schuetzeLoeschen(id);
        }
    }

    /**
     * Schütze aus der Datenbank löschen und das Hauptfenster aktualisieren.
     * @param id id des zu löschenden Schützen
     */
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

    /**
     * Neue Begegnung speichern und die Begegnungen im Hauptfenster neu anzeigen.
     * @param b anzulegende Begegnung
     */
    public void neueBegegnungSpeichern (Begegnung b) {
        BegegnungDAO bDAO = new BegegnungDAO();
        bDAO.insert(b);
        begegnungView.close();
        viewMain.begegnungenAnzeigen();
    }

    /**
     * Beamer-Anzeige für einen Wettkampftag öffnen, ein eventuell offenes Beamer-Fenster wird vorher geschlossen.
     * @param tag Wettkampftag, der auf dem Beamer angezeigt wird
     */
    public void beamerFenster(Wettkampftage tag) {
        //ist bereits ein Beamer-Fenster offen, dieses erst schließen, damit nur eines läuft
        if (beamerView != null && beamerView.isShowing()) {
            beamerView.close();
        }
        BeamerView bView = new BeamerView();
        bView.setController(this); //bestehenden Controller reinreichen
        this.beamerView = bView;   //Controller merkt sich die View
        bView.beamerStarten(tag);  //Szene aufbauen und starten
        bView.show();
    }

    /**
     * Saisondaten bis zum gewählten Wettkampftag als PDF in die gewählte Datei schreiben.
     * @param tag Wettkampftag, bis zu dem die Daten ausgegeben werden
     * @param datei Zieldatei für das PDF
     */
    public void saisonAlsPdf(Wettkampftage tag, File datei) {
        try {
            SaisonPdf pdf = new SaisonPdf(this);
            pdf.erstelle(tag, datei);
            alert.infoAlert("PDF wurde erstellt");
        } catch (Exception e) {
            e.printStackTrace();
            alert.errorAlert("PDF konnte nicht erstellt werden!");
        }
    }

    /**
     * Fenster zur Eingabe einer neuen Begegnung öffnen.
     * @param id id des Wettkampftages, zu dem die neue Begegnung gehört
     */
    public void neueBegegnungFenster(String id) {
        BegegnungView bView = new BegegnungView();
        bView.setController(this); //bestehenden Controller reinreichen
        this.begegnungView = bView;   //Controller merkt sich die View
        bView.newBegegnung(id);        //Szene aufbauen
        bView.show();
    }

    /**
     * Vor dem Löschen einer Begegnung nachfragen und bei Bestätigung löschen.
     * @param id id der zu löschenden Begegnung
     */
    public void begegnungLöschenWarnen(String id) {
        if (alert.begegnungBestaetigen()) { //Wenn mit ok bestätigt wurde
           begegnungLoeschen(id);
        }
    }

    /**
     * Begegnung aus der Datenbank löschen und die Begegnungen im Hauptfenster neu anzeigen.
     * @param id id der zu löschenden Begegnung
     */
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
