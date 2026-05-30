package io.github.manadhion.wettkampf.view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    //Einstiegspunkt, erzeugt eine Insatnz und starte die Methode start aus der App-Klasse
    public static void main(String[] args) {
        launch(args);
    }

    //start-Methode aus der App-Klasse(JavaFX)
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Blasrohr-Wettkampf-Manager");

        //Oberstes Layout
		VBox top = new VBox();
		top.setStyle("-fx-background-color: #82ff8c;");
		
		//Menüleiste oben
		MenuBar menuBar = new MenuBar();
		Menu fileMenu = new Menu("Datei");              //Register Datei
		MenuItem exitItem = new MenuItem("Beenden");    //Programm beenden Auswahl
		fileMenu.getItems().add(exitItem);
		menuBar.getMenus().add(fileMenu);
		top.getChildren().add(menuBar);

        Scene scene = (new Scene(top, 1300, 900)); //Fenstereinstellungens-Parameter

        primaryStage.setScene(scene);   //übernimmt die Fensterparameteer aus der Variable scene
		primaryStage.show();            //öffnet das Fenster
    }
}
