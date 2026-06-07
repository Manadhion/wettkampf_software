package io.github.manadhion.wettkampf.view;

import java.time.LocalDate;

import io.github.manadhion.wettkampf.app.Controller;
import io.github.manadhion.wettkampf.data.Saison;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class WTagView extends Stage {

    //Parameter
    private Controller controller;
    
    //Controller von aussen setzen (kommt aus Main, kennt viewMain)
    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void newWettkampftag(Saison saisonAuswahl) {

        setTitle("Neuen Wettkampftag anlegen"); //Titel für das Fenster
		setResizable(false);                    //die Größe des Fensters kann nicht geändert werden

        //oberstes Layout
        VBox layout = new VBox();
        layout.getStyleClass().add("newWettkampftag-container"); //Aufrufname für die .css Datei
        layout.setFillWidth(false); //Kinder schrumpfen auf Inhaltsbreite

        //Hbox für Eingabereihe
        HBox datumBox = new HBox();
        datumBox.getStyleClass().add("Box-newWettkampftag"); //Aufrufname für die .css Datei
        layout.getChildren().add(datumBox);

        //Label zur Datumseingabe
        Text datumText = new Text("Datum: ");
        datumBox.getChildren().add(datumText);
        datumText.getStyleClass().add("Text-newWettkampftag"); //Aufrufname für die .css Datei

        //DatePicker um das gewünschte Datum einzustellen
        DatePicker datumPicker = new DatePicker(LocalDate.now());
        datumPicker.setEditable(false);
        datumBox.getChildren().add(datumPicker);

        //Hbox für Eingabereihe
        HBox vereinBox = new HBox();
        vereinBox.getStyleClass().add("Box-newWettkampftag"); //Aufrufname für die .css Datei
        layout.getChildren().add(vereinBox);

        //Label Ausrichterverein
        Text vereinText = new Text("Ausrichterverein: ");
        vereinBox.getChildren().add(vereinText);
        vereinText.getStyleClass().add("Text-newWettkampftag"); //Aufrufname für die .css Datei

        //Eingabefelde
        TextField vereinField = new TextField();
        vereinField.getStyleClass().add("TextField-newWettkampftag"); //Aufrufname für die .css Datei
        vereinBox.getChildren().add(vereinField);

        //Hbox für Eingabereihe
        HBox saisonBox = new HBox();
        saisonBox.getStyleClass().add("Box-newWettkampftag"); //Aufrufname für die .css Datei
        layout.getChildren().add(saisonBox);

        //Label Saison
        Text saisonText = new Text("Saison: ");
        saisonBox.getChildren().add(saisonText);
        saisonText.getStyleClass().add("Text-newWettkampftag"); //Aufrufname für die .css Datei

        //ComboBox für die Auswahl der Saison
        ComboBox<Saison> saisonCombo = new ComboBox<>();
        saisonCombo.getStyleClass().add("combo-newWettkampftag"); //Aufrufname für die .css Datei
        saisonCombo.setItems(FXCollections.observableArrayList(controller.alleSaisons()));
        saisonBox.getChildren().add(saisonCombo);
        for (Saison s : saisonCombo.getItems()) {
            if (s.getId().equals(saisonAuswahl.getId())) {
                saisonCombo.getSelectionModel().select(s);
                break;
            }
        }

        //ButtonBox
        HBox buttonBox = new HBox();
        layout.getChildren().add(buttonBox);
        buttonBox.getStyleClass().add("Box-newWettkampftag"); //Aufrufname für die .css Datei

        //Button zum speichern
        Button speichern = new Button("speichern");
        speichern.setOnAction(event -> {

        });

        //Button zum abbrechen
        Button abbrechen = new Button("abbrechen");
        abbrechen.setOnAction(event -> {
			this.close();
		});
        buttonBox.getChildren().addAll(speichern, abbrechen);


        Scene scene = new Scene(layout,350, 210); //Fenstereinstellungens-Parameter

        //style.css in dieses Fenster/Szene einbinden
        scene.getStylesheets().add(getClass().getResource("/io/github/manadhion/wettkampf/view/style.css").toExternalForm());

        setScene(scene); //übernimmt Szene scene als Argument

    }
}
