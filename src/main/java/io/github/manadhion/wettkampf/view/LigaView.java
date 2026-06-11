package io.github.manadhion.wettkampf.view;

import io.github.manadhion.wettkampf.app.Controller;
import io.github.manadhion.wettkampf.data.Liga;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class LigaView extends Stage {

    //Parameter
    private Controller controller;

    //Objekt der eigenen Alert-Klasse
    OwnAlert alert = new OwnAlert();
    
    //Controller von aussen setzen (kommt aus Main, kennt viewMain)
    public void setController(Controller controller) {
        this.controller = controller;
    }

    //Formular zum Anlegen (bearbeiten == null) oder Bearbeiten (bearbeiten != null) einer Liga
    public void ligaFormular(Liga bearbeiten) {

        setTitle(bearbeiten == null ? "Neue Liga anlegen" : "Liga bearbeiten"); //Titel für das Fenster
		setResizable(false);                    //die Größe des Fensters kann nicht geändert werden

        //oberstes Layout
        VBox layout = new VBox();
        layout.getStyleClass().add("newLiga-container"); //Aufrufname für die .css Datei
        layout.setFillWidth(false); //Kinder schrumpfen auf Inhaltsbreite

        //Hbox für Eingabereihe
        HBox nameBox = new HBox();
        nameBox.getStyleClass().add("Box-newLiga"); //Aufrufname für die .css Datei
        layout.getChildren().add(nameBox);

        //Text als Label
        Text nameText = new Text("Liganame: ");
        nameBox.getChildren().add(nameText);
        nameText.getStyleClass().add("Text-newLiga"); //Aufrufname für die .css Datei

        //Eingabefelde
        TextField nameField = new TextField();
        nameField.getStyleClass().add("TextField-newLiga"); //Aufrufname für die .css Datei
        nameBox.getChildren().add(nameField);

        //beim Bearbeiten den vorhandenen Namen vorbelegen
        if (bearbeiten != null) {
            nameField.setText(bearbeiten.getLigaName());
        }

        //ButtonBox
        HBox buttonBox = new HBox();
        layout.getChildren().add(buttonBox);
        buttonBox.getStyleClass().add("Box-newLiga"); //Aufrufname für die .css Datei

        //Button zum speichern
        Button speichern = new Button("speichern");
        speichern.setOnAction(event -> {

            if (bearbeiten == null) {
                Liga l = new Liga(nameField.getText());
                controller.neueLigaSpeichern(l);
            } else {
                //bestehende Liga ändern, ID bleibt erhalten
                bearbeiten.setLigaName(nameField.getText());
                controller.ligaAktualisieren(bearbeiten);
            }

        });

        //Button zum abbrechen
        Button abbrechen = new Button("abbrechen");
        abbrechen.setOnAction(event -> {
			this.close();
		});
        buttonBox.getChildren().addAll(speichern, abbrechen);

        Scene scene = new Scene(layout,450, 100); //Fenstereinstellungens-Parameter

        //style.css in dieses Fenster/Szene einbinden
        scene.getStylesheets().add(getClass().getResource("/io/github/manadhion/wettkampf/view/style.css").toExternalForm());

        setScene(scene); //übernimmt Szene scene als Argument

    }
    
}
