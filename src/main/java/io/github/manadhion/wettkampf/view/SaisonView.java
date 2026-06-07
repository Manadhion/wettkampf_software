package io.github.manadhion.wettkampf.view;

import javafx.stage.Stage;
import io.github.manadhion.wettkampf.app.Controller;
import io.github.manadhion.wettkampf.data.Saison;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class SaisonView extends Stage {

    //Parameter
    private Controller controller;
    private int saisonName;

    //Controller von aussen setzen (kommt aus Main, kennt viewMain)
    public void setController(Controller controller) {
        this.controller = controller;
    }

    //Alarmfenster wenn keine Zahlen eingetragen wurden
	public void intAlert () {
		Alert intAlert = new Alert(AlertType.ERROR);
		intAlert.setTitle("Fehler!");
		intAlert.setHeaderText("Bitte nur ganze Zahlen eingeben!");
		intAlert.showAndWait();
	}

    //Alarm wenn die eingegebene Zahlenmenge nicht richtig ist
	public void valueAlert(String text) {
		Alert eqAlert = new Alert(AlertType.ERROR);
		eqAlert.setTitle("Fehler!");
		eqAlert.setHeaderText(text);
		eqAlert.showAndWait();
	}

    public void newSaison(){

        setTitle("Neue Saison anlegen"); //Titel für das Fenster
		setResizable(false);             //die Größe des Fensters kann nicht geändert werden

        //oberstes Layout
        VBox layout = new VBox();
        layout.getStyleClass().add("newSaison-container"); //Aufrufname für die .css Datei
        layout.setFillWidth(false); //Kinder schrumpfen auf Inhaltsbreite

        //HBox für Saisoneingabe
        HBox saisonBox = new HBox();
        layout.getChildren().add(saisonBox);
        saisonBox.getStyleClass().add("Box-newSaison"); //Aufrufname für die .css Datei

        //Label zur Namenseingabe
        Text saisonText = new Text("Saison - Jahre: ");
        saisonBox.getChildren().add(saisonText);
        saisonText.getStyleClass().add("Text-newSaison"); //Aufrufname für die .css Datei

        //eingabefelder
        TextField saisonEins = new TextField();
        saisonEins.getStyleClass().add("TextField-newSaison"); //Aufrufname für die .css Datei
        Text trennung = new Text(" / ");
        trennung.getStyleClass().add("Text-newSaison"); //Aufrufname für die .css Datei
        TextField saisonZwei = new TextField();
        saisonZwei.getStyleClass().add("TextField-newSaison"); //Aufrufname für die .css Datei
        saisonBox.getChildren().addAll(saisonEins, trennung, saisonZwei);

        //ButtonBox
        HBox buttonBox = new HBox();
        layout.getChildren().add(buttonBox);
        buttonBox.getStyleClass().add("Box-newSaison"); //Aufrufname für die .css Datei

        //Button zum speichern
        Button speichern = new Button("speichern");
        speichern.setOnAction(event -> {

            String zwischenWert;
            //Textfelder auslesen
            zwischenWert = saisonEins.getText().trim();
            zwischenWert += saisonZwei.getText().trim();

            //Versuchen einen Integer-Wert zu parsen
			try {
				this.saisonName = Integer.parseInt(zwischenWert);
			}catch(NumberFormatException e) {
				this.intAlert();
				return;
			}

            //es dürfen aktuell nur vier Zahlen in der Variablen stehen
			int zahlenLaenge = String.valueOf(Math.abs(saisonName)).length();
			if(zahlenLaenge != 4) {
				this.valueAlert("Bitte zweistellige Jahreszahl in jedes Feld eingeben eingeben! (Beispiel: 25 / 26)");
				return;
			}

            //prüfen ob die Saison bereits existiert
            if (controller.saisonExistiert(saisonName)) {
                this.valueAlert("Diese Saison gibt es bereits!");
                return;
            }

            //neue Saison speichern
            Saison s = new Saison(saisonName);
            controller.neueSaisonAnlegen(s);
        });

        //Button zum abbrechen
        Button abbrechen = new Button("abbrechen");
        abbrechen.setOnAction(event -> {
			this.close();
		});
        buttonBox.getChildren().addAll(speichern, abbrechen);

        Scene scene = new Scene(layout,250, 100); //Fenstereinstellungens-Parameter

        //style.css in dieses Fenster/Szene einbinden
        scene.getStylesheets().add(getClass().getResource("/io/github/manadhion/wettkampf/view/style.css").toExternalForm());

        setScene(scene); //übernimmt Szene scene als Argument
    }
    
}
