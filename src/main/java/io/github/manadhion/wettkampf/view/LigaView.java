package io.github.manadhion.wettkampf.view;

import io.github.manadhion.wettkampf.app.Controller;
import io.github.manadhion.wettkampf.data.Liga;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Fenster zum Anlegen und Bearbeiten einer Liga.
 */
public class LigaView extends Stage {

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
     * Formular zum Anlegen oder Bearbeiten einer Liga aufbauen.
     * @param bearbeiten zu bearbeitende Liga, oder null zum Anlegen einer neuen
     */
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

        HBox rangBox = new HBox();
        rangBox.getStyleClass().add("Box-newLiga");
        layout.getChildren().add(rangBox);
        Text rangText = new Text("Rangfolge (1 = höchste Liga): ");
        rangText.getStyleClass().add("Text-newLiga");
        int startRang = bearbeiten == null ? controller.naechsteLigaRangfolge() : bearbeiten.getRangfolge();
        Spinner<Integer> rangSpinner = new Spinner<>();
        rangSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, startRang));
        rangSpinner.setEditable(true);
        rangBox.getChildren().addAll(rangText, rangSpinner);

        //ButtonBox
        HBox buttonBox = new HBox();
        layout.getChildren().add(buttonBox);
        buttonBox.getStyleClass().add("Box-newLiga"); //Aufrufname für die .css Datei

        //Button zum speichern
        Button speichern = new Button("speichern");
        speichern.setOnAction(event -> {

            if (nameField.getText().isBlank()) {
                alert.errorAlert("Bitte einen Liganamen eingeben.");
                return;
            }

            int rangfolge;
            try {
                rangfolge = rangSpinner.getValueFactory().getConverter().fromString(
                        rangSpinner.getEditor().getText());
                if (rangfolge < 1 || rangfolge > 999) throw new NumberFormatException();
            } catch (RuntimeException ex) {
                alert.errorAlert("Bitte eine Rangfolge zwischen 1 und 999 eingeben.");
                return;
            }

            if (bearbeiten == null) {
                Liga l = new Liga(nameField.getText(), rangfolge);
                controller.neueLigaSpeichern(l);
            } else {
                //bestehende Liga ändern, ID bleibt erhalten
                bearbeiten.setLigaName(nameField.getText());
                bearbeiten.setRangfolge(rangfolge);
                controller.ligaAktualisieren(bearbeiten);
            }

        });

        //Button zum abbrechen
        Button abbrechen = new Button("abbrechen");
        abbrechen.setOnAction(event -> {
			this.close();
		});
        buttonBox.getChildren().addAll(speichern, abbrechen);

        Scene scene = new Scene(layout,500, 160); //Fenstereinstellungens-Parameter

        //style.css in dieses Fenster/Szene einbinden
        scene.getStylesheets().add(getClass().getResource("/io/github/manadhion/wettkampf/view/style.css").toExternalForm());

        setScene(scene); //übernimmt Szene scene als Argument

    }
    
}
