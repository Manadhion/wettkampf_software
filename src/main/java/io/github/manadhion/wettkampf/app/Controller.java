package io.github.manadhion.wettkampf.app;

import io.github.manadhion.wettkampf.dao.MannschaftDAO;
import io.github.manadhion.wettkampf.view.Main;

//Controller um Datenstrom gezielt zu lenken
public class Controller {
    
    //Instanzen der App-Fenster
    private Main viewMain;

    //Constructor mit Main-Objekt
    public Controller(Main viewMain) {
        this.viewMain = viewMain;
    }


    //Methode Tabelle erstellen aufrufen
    public void createTableIfNotExists() {
        MannschaftDAO maDAO = new MannschaftDAO();

        maDAO.createTableIfNotExists();
    }

}
