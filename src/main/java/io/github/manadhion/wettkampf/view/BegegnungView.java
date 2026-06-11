package io.github.manadhion.wettkampf.view;

import io.github.manadhion.wettkampf.app.Controller;
import io.github.manadhion.wettkampf.data.Begegnung;
import io.github.manadhion.wettkampf.data.Mannschaft;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class BegegnungView extends Stage {

    //Parameter
    private Controller controller;

    //Objekt der eigenen Alert-Klasse
    OwnAlert alert = new OwnAlert();
    
    //Controller von aussen setzen (kommt aus Main, kennt viewMain)
    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void newBegegnung(String tagID)  {

        setTitle("Neue Begegnung anlegen"); //Titel für das Fenster
		setResizable(false);                    //die Größe des Fensters kann nicht geändert werden

        //oberstes Layout
        VBox layout = new VBox();
        layout.getStyleClass().add("newBegegnung-container"); //Aufrufname für die .css Datei
        layout.setFillWidth(false); //Kinder schrumpfen auf Inhaltsbreite

        //Hbox für Eingabereihe
        HBox heimBox = new HBox();
        heimBox.getStyleClass().add("Box-newBegegnung"); //Aufrufname für die .css Datei
        layout.getChildren().add(heimBox);

        //Text als Label
        Text heimText = new Text("Heimmannschaft: ");
        heimBox.getChildren().add(heimText);
        heimText.getStyleClass().add("Text-newBegegnung"); //Aufrufname für die .css Datei

        //ComboBox für die Auswahl der Mannschaft
        ComboBox<Mannschaft> heimCombo = new ComboBox<>();
        heimCombo.getStyleClass().add("comboL"); //Aufrufname für die .css Datei
        HBox.setMargin(heimCombo, new Insets(0, 0, 20, 0)); //Abstand unter der ComboBox zum nächsten Element
        heimCombo.setItems(FXCollections.observableArrayList(controller.alleMannschaften()));
        heimBox.getChildren().add(heimCombo);

        //Hbox für Eingabereihe
        HBox gegnerBox = new HBox();
        gegnerBox.getStyleClass().add("Box-newBegegnung"); //Aufrufname für die .css Datei
        layout.getChildren().add(gegnerBox);

        //Text als Label
        Text gegnerText = new Text("Auswärtsmannschaft: ");
        gegnerBox.getChildren().add(gegnerText);
        gegnerText.getStyleClass().add("Text-newBegegnung"); //Aufrufname für die .css Datei

        //ComboBox für die Auswahl der Mannschaft
        ComboBox<Mannschaft> gegnerCombo = new ComboBox<>();
        gegnerCombo.getStyleClass().add("comboL"); //Aufrufname für die .css Datei
        HBox.setMargin(gegnerCombo, new Insets(0, 0, 20, 0)); //Abstand unter der ComboBox zum nächsten Element
        gegnerCombo.setItems(FXCollections.observableArrayList(controller.alleMannschaften()));
        gegnerBox.getChildren().add(gegnerCombo);

        //ButtonBox
        HBox buttonBox = new HBox();
        layout.getChildren().add(buttonBox);
        buttonBox.getStyleClass().add("Box-newBegegnung"); //Aufrufname für die .css Datei

        //Button zum speichern
        Button speichern = new Button("speichern");
        speichern.setOnAction(event -> {

            Mannschaft heim = heimCombo.getSelectionModel().getSelectedItem();
            Mannschaft gegner = gegnerCombo.getSelectionModel().getSelectedItem();

            //ohne Heim- und Auswärtsmannschaft gibt es nichts zu speichern
            if (heim == null || gegner == null) {
                alert.errorAlert("Bitte Heim- und Auswärtsmannschaft auswählen!");
                return;
            }

            if (!heim.getKlasse().equals(gegner.getKlasse())) {
                alert.errorAlert("Es können nur Mannschaften einer Liga gegeneinander antreten!");
                return;
            }

            controller.neueBegegnungSpeichern(new Begegnung(heim.getId(), gegner.getId(), tagID));

        });

        //Button zum abbrechen
        Button abbrechen = new Button("abbrechen");
        abbrechen.setOnAction(event -> {
			this.close();
		});
        buttonBox.getChildren().addAll(speichern, abbrechen);

        Scene scene = new Scene(layout,480, 310); //Fenstereinstellungens-Parameter

        //style.css in dieses Fenster/Szene einbinden
        scene.getStylesheets().add(getClass().getResource("/io/github/manadhion/wettkampf/view/style.css").toExternalForm());

        setScene(scene); //übernimmt Szene scene als Argument

    }
    
}
