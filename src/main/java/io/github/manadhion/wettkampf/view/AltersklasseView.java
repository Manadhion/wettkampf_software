package io.github.manadhion.wettkampf.view;

import io.github.manadhion.wettkampf.app.Controller;
import io.github.manadhion.wettkampf.data.Altersklasse;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Fenster zum Anlegen und Bearbeiten einer Altersklasse.
 */
public class AltersklasseView extends Stage {

    //Parameter
    private Controller controller;

    //Objekt der eigenen Alert-Klasse
    OwnAlert alert = new OwnAlert();

    /**
     * Controller von außen setzen (kommt aus Main, kennt viewMain).
     * @param controller der gemeinsame Controller
     */
    public void setController(Controller controller) {
        this.controller = controller;
    }

    /**
     * Formular zum Anlegen oder Bearbeiten einer Altersklasse aufbauen.
     * @param bearbeiten zu bearbeitende Altersklasse, oder null zum Anlegen einer neuen
     */
    public void alterFormular(Altersklasse bearbeiten) {

        setTitle(bearbeiten == null ? "Neue Altersklasse anlegen" : "Altersklasse bearbeiten"); //Titel für das Fenster
		setResizable(false);                    //die Größe des Fensters kann nicht geändert werden

        //oberstes Layout
        VBox layout = new VBox();
        layout.getStyleClass().add("newAlter-container"); //Aufrufname für die .css Datei
        layout.setFillWidth(false); //Kinder schrumpfen auf Inhaltsbreite

        //Hbox für Eingabereihe
        HBox nameBox = new HBox();
        nameBox.getStyleClass().add("Box-newAlter"); //Aufrufname für die .css Datei
        layout.getChildren().add(nameBox);

        //Text als Label
        Text nameText = new Text("Altersklasse: ");
        nameBox.getChildren().add(nameText);
        nameText.getStyleClass().add("Text-newAlter"); //Aufrufname für die .css Datei

        //Eingabefelde
        TextField nameField = new TextField();
        nameField.getStyleClass().add("TextField-newAlter"); //Aufrufname für die .css Datei
        nameBox.getChildren().add(nameField);

        //beim Bearbeiten den vorhandenen Namen vorbelegen
        if (bearbeiten != null) {
            nameField.setText(bearbeiten.getKlassenName());
        }

        //ButtonBox
        HBox buttonBox = new HBox();
        layout.getChildren().add(buttonBox);
        buttonBox.getStyleClass().add("Box-newMannschaft"); //Aufrufname für die .css Datei

        //Button zum speichern
        Button speichern = new Button("speichern");
        speichern.setOnAction(event -> {

            if (bearbeiten == null) {
                controller.neueAlterSpeichern(new Altersklasse(nameField.getText()));
            } else {
                //bestehende Altersklasse ändern, ID bleibt erhalten
                bearbeiten.setKlassenName(nameField.getText());
                controller.alterAktualisieren(bearbeiten);
            }

        });

        //Button zum abbrechen
        Button abbrechen = new Button("abbrechen");
        abbrechen.setOnAction(event -> {
			this.close();
		});
        buttonBox.getChildren().addAll(speichern, abbrechen);

        Scene scene = new Scene(layout,400, 110); //Fenstereinstellungens-Parameter

        //style.css in dieses Fenster/Szene einbinden
        scene.getStylesheets().add(getClass().getResource("/io/github/manadhion/wettkampf/view/style.css").toExternalForm());

        setScene(scene); //übernimmt Szene scene als Argument
        
    }
    
}
