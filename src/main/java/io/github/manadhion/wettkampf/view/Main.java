package io.github.manadhion.wettkampf.view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import io.github.manadhion.wettkampf.app.Controller;

public class Main extends Application {

    //Controller-Objekt erzeugen
    private Controller controller = new Controller(this);

    //Einstiegspunkt, erzeugt eine Instanz und startet die Methode start aus der App-Klasse
    public static void main(String[] args) {
        launch(args);
    }

    //start-Methode aus der App-Klasse(JavaFX)
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Blasrohr-Wettkampf-Manager");
        
        //Tabellen anlegen wenn sie noch nicht existieren
        controller.createTableIfNotExists();

        //Oberstes Layout
		VBox top = new VBox();
		top.getStyleClass().add("root-container");
		
		//Menüleiste oben
		MenuBar menuBar = new MenuBar();
		Menu fileMenu = new Menu("Datei");              //Register Datei
		MenuItem exitItem = new MenuItem("Beenden");    //Programm beenden Auswahl
		fileMenu.getItems().add(exitItem);
		menuBar.getMenus().add(fileMenu);
		top.getChildren().add(menuBar);

        Scene scene = (new Scene(top, 1300, 900)); //Fenstereinstellungens-Parameter

        //style.css in dieses Fenster/Szene einbinden
        scene.getStylesheets().add(getClass().getResource("/io/github/manadhion/wettkampf/view/style.css").toExternalForm());

        primaryStage.setScene(scene);   //übernimmt Szene scene als Argument
		primaryStage.show();            //öffnet das Fenster
    }
}
