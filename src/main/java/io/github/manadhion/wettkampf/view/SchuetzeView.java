package io.github.manadhion.wettkampf.view;

import io.github.manadhion.wettkampf.app.Controller;
import io.github.manadhion.wettkampf.data.Altersklasse;
import io.github.manadhion.wettkampf.data.Mannschaft;
import io.github.manadhion.wettkampf.data.Schuetze;
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
 * Fenster zum Anlegen und Bearbeiten eines Schützen.
 */
public class SchuetzeView extends Stage {

    //Parameter
    private Controller controller;
    private ComboBox<Altersklasse> alterCombo;

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
     * Formular zum Anlegen oder Bearbeiten eines Schützen aufbauen.
     * @param mAuswahl beim Anlegen vorauszuwählende Mannschaft
     * @param bearbeiten zu bearbeitender Schütze, oder null zum Anlegen eines neuen
     */
    public void schuetzeFormular(Mannschaft mAuswahl, Schuetze bearbeiten) {

        setTitle(bearbeiten == null ? "Neuen Schützen anlegen" : "Schützen bearbeiten"); //Titel für das Fenster
		setResizable(false);                    //die Größe des Fensters kann nicht geändert werden

        //oberstes Layout
        VBox layout = new VBox();
        layout.getStyleClass().add("newSchuetze-container"); //Aufrufname für die .css Datei
        layout.setFillWidth(false); //Kinder schrumpfen auf Inhaltsbreite

        //Hbox für Eingabereihe
        HBox vNameBox = new HBox();
        vNameBox.getStyleClass().add("Box-newSchuetze"); //Aufrufname für die .css Datei
        layout.getChildren().add(vNameBox);

        //Text als Label
        Text vNameText = new Text("Vorname: ");
        vNameBox.getChildren().add(vNameText);
        vNameText.getStyleClass().add("Text-newSchuetze"); //Aufrufname für die .css Datei

        //Eingabefelde
        TextField vNameField = new TextField();
        vNameField.getStyleClass().add("TextField-newSchuetze"); //Aufrufname für die .css Datei
        vNameBox.getChildren().add(vNameField);

        //beim Bearbeiten den Vornamen vorbelegen, dieser darf aber nicht mehr geändert werden
        if (bearbeiten != null) {
            vNameField.setText(bearbeiten.getVorname());
            vNameField.setDisable(true);
        }

        //Hbox für Eingabereihe
        HBox nNameBox = new HBox();
        nNameBox.getStyleClass().add("Box-newSchuetze"); //Aufrufname für die .css Datei
        layout.getChildren().add(nNameBox);

        //Text als Label
        Text nNameText = new Text("Nachname: ");
        nNameBox.getChildren().add(nNameText);
        nNameText.getStyleClass().add("Text-newSchuetze"); //Aufrufname für die .css Datei

        //Eingabefelde
        TextField nNameField = new TextField();
        nNameField.getStyleClass().add("TextField-newSchuetze"); //Aufrufname für die .css Datei
        nNameBox.getChildren().add(nNameField);

        //beim Bearbeiten den Nachnamen vorbelegen, dieser darf aber nicht mehr geändert werden
        if (bearbeiten != null) {
            nNameField.setText(bearbeiten.getNachname());
            nNameField.setDisable(true);
        }

        //Hbox für Eingabereihe
        HBox mannschaftBox = new HBox();
        mannschaftBox.getStyleClass().add("Box-newSchuetze"); //Aufrufname für die .css Datei
        layout.getChildren().add(mannschaftBox);

        //Text als Label
        Text mannschaftText = new Text("Mannschaft: ");
        mannschaftBox.getChildren().add(mannschaftText);
        mannschaftText.getStyleClass().add("Text-newSchuetze"); //Aufrufname für die .css Datei

        //ComboBox für die Auswahl der Mannschaft
        ComboBox<Mannschaft> mannschaftCombo = new ComboBox<>();
        mannschaftCombo.getStyleClass().add("comboL"); //Aufrufname für die .css Datei
        HBox.setMargin(mannschaftCombo, new Insets(0, 0, 20, 0)); //Abstand unter der ComboBox zum nächsten Element
        mannschaftCombo.setItems(FXCollections.observableArrayList(controller.alleMannschaften()));
        mannschaftBox.getChildren().add(mannschaftCombo);

        //beim Anlegen die übergebene Mannschaft vorauswählen, beim Bearbeiten die Mannschaft des Schützen
        String vorauswahlMannschaftID = bearbeiten == null ? mAuswahl.getId() : bearbeiten.getMannschaftid();
        for (Mannschaft m : mannschaftCombo.getItems()) {
            if (m.getId().equals(vorauswahlMannschaftID)) {
                mannschaftCombo.getSelectionModel().select(m);
                break;
            }
        }

         //Hbox für Eingabereihe
        HBox alterBox = new HBox();
        alterBox.getStyleClass().add("Box-newSchuetze"); //Aufrufname für die .css Datei
        layout.getChildren().add(alterBox);

        //Text als Label
        Text alterText = new Text("Altersklasse: ");
        alterBox.getChildren().add(alterText);
        alterText.getStyleClass().add("Text-newSchuetze"); //Aufrufname für die .css Datei

        //ComboBox für die Auswahl der Altersklasse
        alterCombo = new ComboBox<>();
        alterCombo.getStyleClass().add("comboL"); //Aufrufname für die .css Datei
        HBox.setMargin(alterCombo, new Insets(0, 0, 20, 0)); //Abstand unter der ComboBox zum nächsten Element
        alterCombo.setItems(FXCollections.observableArrayList(controller.alleAltersklassen()));
        alterBox.getChildren().add(alterCombo);

        //beim Bearbeiten die Altersklasse des Schützen vorauswählen
        if (bearbeiten != null) {
            for (Altersklasse a : alterCombo.getItems()) {
                if (a.getId().equals(bearbeiten.getAltersKlasse())) {
                    alterCombo.getSelectionModel().select(a);
                    break;
                }
            }
        }

        //Buton um neue Altersklasse anzulegen
        Button saPlusButton = new Button("+");
        saPlusButton.getStyleClass().add("add"); //Aufrufname für die .css Datei
        alterBox.getChildren().add(saPlusButton);
        Tooltip tipsaPlus = new Tooltip("Neue Altersklasse anlegen");
        tipsaPlus.setShowDelay(Duration.millis(300));
        saPlusButton.setTooltip(tipsaPlus);
        saPlusButton.setOnAction(event -> {
			controller.neueAlterFenster();
		});

        //Buton um Altersklasse zu löschen
        Button saMinusButton = new Button("-");
        saMinusButton.setDisable(true);
        saMinusButton.getStyleClass().add("danger"); //Aufrufname für die .css Datei
        alterBox.getChildren().add(saMinusButton);
        Tooltip tipsaMinus = new Tooltip("Altersklasse löschen");
        tipsaMinus.setShowDelay(Duration.millis(300));
        saMinusButton.setTooltip(tipsaMinus);
        saMinusButton.setOnAction(event -> {
            controller.alterLöschenWarnen(alterCombo.getSelectionModel().getSelectedItem().getId());
		});

        //Buton um Altersklasse zu bearbeiten
        Button saEditButton = new Button("✏");
        saEditButton.setDisable(true);
        saEditButton.getStyleClass().add("edit"); //Aufrufname für die .css Datei
        alterBox.getChildren().add(saEditButton);
        Tooltip tipsaEdit = new Tooltip("Altersklasse bearbeiten");
        tipsaEdit.setShowDelay(Duration.millis(300));
        saEditButton.setTooltip(tipsaEdit);
        saEditButton.setOnAction(event -> {
            controller.alterBearbeitenFenster(alterCombo.getSelectionModel().getSelectedItem());
		});

        //löschen_button und bearbeiten-Button sollen reagieren auf ComboBox Liga
        alterCombo.getSelectionModel().selectedItemProperty().addListener(
            (obs, alteKlasse, neueKlasse) -> {

                if (neueKlasse == null) {
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
        buttonBox.getStyleClass().add("Box-newSchuetze"); //Aufrufname für die .css Datei

        //Button zum speichern
        Button speichern = new Button("speichern");
        speichern.setOnAction(event -> {

            if (bearbeiten == null) {
                controller.neuenSchuetzenpeichern(new Schuetze(vNameField.getText(), nNameField.getText(),
                    mannschaftCombo.getSelectionModel().getSelectedItem().getId(), alterCombo.getSelectionModel().getSelectedItem().getId()),
                    mannschaftCombo.getSelectionModel().getSelectedItem().getId());
            } else {
                //Mannschaft die in der Hauptansicht gerade angezeigt wird, damit deren Schützenliste danach neu geladen wird
                String anzeigeMannschaftID = bearbeiten.getMannschaftid();

                //bestehenden Schützen ändern, Vor- und Nachname sowie ID bleiben erhalten
                bearbeiten.setMannschaftid(mannschaftCombo.getSelectionModel().getSelectedItem().getId());
                bearbeiten.setAltersKlasse(alterCombo.getSelectionModel().getSelectedItem().getId());
                controller.schuetzeAktualisieren(bearbeiten, anzeigeMannschaftID);
            }

        });

        //Button zum abbrechen
        Button abbrechen = new Button("abbrechen");
        abbrechen.setOnAction(event -> {
			this.close();
		});
        buttonBox.getChildren().addAll(speichern, abbrechen);

        Scene scene = new Scene(layout,510, 310); //Fenstereinstellungens-Parameter

        //style.css in dieses Fenster/Szene einbinden
        scene.getStylesheets().add(getClass().getResource("/io/github/manadhion/wettkampf/view/style.css").toExternalForm());

        setScene(scene); //übernimmt Szene scene als Argument
        
    }


    /**
     * Altersklasse-ComboBox neu aus der Datenbank laden.
     */
    public void alterComboAktualisieren() {
        alterCombo.setItems(FXCollections.observableArrayList(controller.alleAltersklassen()));
    }
    
}
