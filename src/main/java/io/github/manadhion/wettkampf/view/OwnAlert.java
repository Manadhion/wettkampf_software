package io.github.manadhion.wettkampf.view;

import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

//Klasse um verschiedene Alarme aufzurufen
public class OwnAlert {

    //Für weitere Informationen
	public void infoAlert(String message) {
		Alert info = new Alert(AlertType.INFORMATION);
		info.setTitle("Information");
		info.setHeaderText(message);
		info.showAndWait();
	}

    //Error - Information
	public void errorAlert(String text) {
		Alert eqAlert = new Alert(AlertType.ERROR);
		eqAlert.setTitle("Fehler!");
		eqAlert.setHeaderText(text);
		eqAlert.showAndWait();
	}

    //Information über erfolgreiches speichern
	public void savedAlert(String message) {
		Alert saved = new Alert(AlertType.INFORMATION);
		saved.setTitle("gespeichert");
		saved.setHeaderText(message);
		saved.showAndWait();
	}

    //Lösch-Dialog anzeigen und nur zurückgeben, ob bestätigt wurde
    public boolean saisonLoeschenBestaetigen() {
        Alert saisonLoeschen = new Alert(AlertType.CONFIRMATION);
        saisonLoeschen.setTitle("Löschvorgang");
        saisonLoeschen.setHeaderText("Wollen Sie diese Saison wirklich Löschen?");
        Optional<ButtonType> result = saisonLoeschen.showAndWait();
		return result.isPresent() && result.get() == ButtonType.OK; //true zurück wenn mit ok bestätigt wurde
    }
}
