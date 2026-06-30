package io.github.manadhion.wettkampf.view;

import io.github.manadhion.wettkampf.app.Controller;
import io.github.manadhion.wettkampf.data.Liga;
import io.github.manadhion.wettkampf.data.Mannschaft;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Fenster zum Anlegen und Bearbeiten einer Mannschaft.
 */
public class MannschaftView extends Stage {

    //Parameter
    private Controller controller;
    private ComboBox<Liga> ligaCombo;

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
     * Formular zum Anlegen oder Bearbeiten einer Mannschaft aufbauen.
     * @param bearbeiten zu bearbeitende Mannschaft, oder null zum Anlegen einer neuen
     */
    public void mannschaftFormular(Mannschaft bearbeiten) {

        setTitle(bearbeiten == null ? "Neue Mannschaft anlegen" : "Mannschaft bearbeiten"); //Titel für das Fenster
		setResizable(false);                    //die Größe des Fensters kann nicht geändert werden

        //oberstes Layout
        VBox layout = new VBox();
        layout.getStyleClass().add("newMannschaft-container"); //Aufrufname für die .css Datei
        layout.setFillWidth(false); //Kinder schrumpfen auf Inhaltsbreite

        //Hbox für Eingabereihe
        HBox nameBox = new HBox();
        nameBox.getStyleClass().add("Box-newMannschaft"); //Aufrufname für die .css Datei
        layout.getChildren().add(nameBox);

        //Text als Label
        Text nameText = new Text("Mannschaftsname: ");
        nameBox.getChildren().add(nameText);
        nameText.getStyleClass().add("Text-newMannschaft"); //Aufrufname für die .css Datei

        //Eingabefelde
        TextField nameField = new TextField();
        nameField.getStyleClass().add("TextField-newMannschaft"); //Aufrufname für die .css Datei
        nameBox.getChildren().add(nameField);

        //beim Bearbeiten den Namen vorbelegen, dieser darf aber nicht mehr geändert werden
        if (bearbeiten != null) {
            nameField.setText(bearbeiten.getName());
            nameField.setDisable(true);
        }

        //Hbox für Eingabereihe
        HBox ligaBox = new HBox();
        ligaBox.getStyleClass().add("Box-newMannschaft"); //Aufrufname für die .css Datei
        layout.getChildren().add(ligaBox);

        //Text als Label
        Text ligaText = new Text("Liga: ");
        ligaBox.getChildren().add(ligaText);
        ligaText.getStyleClass().add("Text-newMannschaft"); //Aufrufname für die .css Datei

        //ComboBox für die Auswahl der Liga
        ligaCombo = new ComboBox<>();
        ligaCombo.getStyleClass().add("comboL"); //Aufrufname für die .css Datei
        HBox.setMargin(ligaCombo, new Insets(0, 0, 20, 0)); //Abstand unter der ComboBox zum nächsten Element
        ligaCombo.setItems(FXCollections.observableArrayList(controller.alleLigen()));
        ligaBox.getChildren().add(ligaCombo);

        //beim Bearbeiten die Liga der Mannschaft vorauswählen
        if (bearbeiten != null) {
            for (Liga l : ligaCombo.getItems()) {
                if (l.getId().equals(bearbeiten.getKlasse())) {
                    ligaCombo.getSelectionModel().select(l);
                    break;
                }
            }
        }

        //Buton um neue Liga anzulegen
        Button saPlusButton = new Button("+");
        saPlusButton.getStyleClass().add("add"); //Aufrufname für die .css Datei
        ligaBox.getChildren().add(saPlusButton);
        Tooltip tipsaPlus = new Tooltip("Neue Liga anlegen");
        tipsaPlus.setShowDelay(Duration.millis(300));
        saPlusButton.setTooltip(tipsaPlus);
        saPlusButton.setOnAction(event -> {
			controller.neueLigaFenster();
		});

        //Buton um Liga zu löschen
        Button saMinusButton = new Button("-");
        saMinusButton.setDisable(true);
        saMinusButton.getStyleClass().add("danger"); //Aufrufname für die .css Datei
        ligaBox.getChildren().add(saMinusButton);
        Tooltip tipsaMinus = new Tooltip("Liga löschen");
        tipsaMinus.setShowDelay(Duration.millis(300));
        saMinusButton.setTooltip(tipsaMinus);
        saMinusButton.setOnAction(event -> {
            controller.ligaLöschenWarnen(ligaCombo.getSelectionModel().getSelectedItem().getId());
		});

        //Buton um Liga zu bearbeiten
        Button saEditButton = new Button("✏");
        saEditButton.setDisable(true);
        saEditButton.getStyleClass().add("edit"); //Aufrufname für die .css Datei
        ligaBox.getChildren().add(saEditButton);
        Tooltip tipsaEdit = new Tooltip("Liga bearbeiten");
        tipsaEdit.setShowDelay(Duration.millis(300));
        saEditButton.setTooltip(tipsaEdit);
        saEditButton.setOnAction(event -> {
            controller.ligaBearbeitenFenster(ligaCombo.getSelectionModel().getSelectedItem());
		});

        //löschen_button und bearbeiten-Button sollen reagieren auf ComboBox Liga
        ligaCombo.getSelectionModel().selectedItemProperty().addListener(
            (obs, alteLiga, neueLiga) -> {

                if (neueLiga == null) {
                    saMinusButton.setDisable(true);
                    saEditButton.setDisable(true);
                } else {
                    saMinusButton.setDisable(false);
                    saEditButton.setDisable(false);
                }
            });

        //ButtonBox
        HBox buttonBox = new HBox();
        layout.getChildren().add(buttonBox);
        buttonBox.getStyleClass().add("Box-newMannschaft"); //Aufrufname für die .css Datei

        //Button zum speichern
        Button speichern = new Button("speichern");
        speichern.setOnAction(event -> {

            Liga liga = ligaCombo.getSelectionModel().getSelectedItem();

            if (liga == null) {
                alert.errorAlert("Bitte eine Liga auswählen");
                return;
            }

            if (bearbeiten == null) {
                Mannschaft m = new Mannschaft(nameField.getText(), liga.getId());
                controller.neueMannschaftSpeichern(m);
            } else {
                //bestehende Mannschaft ändern, Name und ID bleiben erhalten
                bearbeiten.setKlasse(liga.getId());
                controller.mannschaftAktualisieren(bearbeiten);
            }

        });

        //Button zum abbrechen
        Button abbrechen = new Button("abbrechen");
        abbrechen.setOnAction(event -> {
			this.close();
		});
        buttonBox.getChildren().addAll(speichern, abbrechen);

        Scene scene = new Scene(layout,450, 210); //Fenstereinstellungens-Parameter

        //style.css in dieses Fenster/Szene einbinden
        scene.getStylesheets().add(getClass().getResource("/io/github/manadhion/wettkampf/view/style.css").toExternalForm());

        setScene(scene); //übernimmt Szene scene als Argument

    }

    /**
     * Liga-ComboBox neu aus der Datenbank laden.
     */
    public void ligaComboAktualisieren() {
        ligaCombo.setItems(FXCollections.observableArrayList(controller.alleLigen()));
    }
    
}
