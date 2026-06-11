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

	//Lösch-Dialog anzeigen und nur zurückgeben, ob bestätigt wurde
	public boolean wTagLoeschenBestaetigen() {
        Alert wTagLoeschen = new Alert(AlertType.CONFIRMATION);
        wTagLoeschen.setTitle("Löschvorgang");
        wTagLoeschen.setHeaderText("Wollen Sie diesen Wettkampftag wirklich Löschen?");
        Optional<ButtonType> result = wTagLoeschen.showAndWait();
		return result.isPresent() && result.get() == ButtonType.OK; //true zurück wenn mit ok bestätigt wurde
    }

	//Lösch-Dialog anzeigen und nur zurückgeben, ob bestätigt wurde
	public boolean ligaLöschBestaetigen() {
        Alert ligaLoeschen = new Alert(AlertType.CONFIRMATION);
        ligaLoeschen.setTitle("Löschvorgang");
        ligaLoeschen.setHeaderText("Wollen Sie diese Liga wirklich Löschen?");
        Optional<ButtonType> result = ligaLoeschen.showAndWait();
		return result.isPresent() && result.get() == ButtonType.OK; //true zurück wenn mit ok bestätigt wurde
    }

	//Lösch-Dialog anzeigen und nur zurückgeben, ob bestätigt wurde
	public boolean mannschaftBestaetigen() {
        Alert mannschaftLoeschen = new Alert(AlertType.CONFIRMATION);
        mannschaftLoeschen.setTitle("Löschvorgang");
        mannschaftLoeschen.setHeaderText("Wollen Sie diese Mannschaft wirklich Löschen?");
        Optional<ButtonType> result = mannschaftLoeschen.showAndWait();
		return result.isPresent() && result.get() == ButtonType.OK; //true zurück wenn mit ok bestätigt wurde
    }

	//Lösch-Dialog anzeigen und nur zurückgeben, ob bestätigt wurde
	public boolean ligaBestaetigen() {
        Alert ligaLoeschen = new Alert(AlertType.CONFIRMATION);
        ligaLoeschen.setTitle("Löschvorgang");
        ligaLoeschen.setHeaderText("Wollen Sie diese Liga wirklich Löschen?");
        Optional<ButtonType> result = ligaLoeschen.showAndWait();
		return result.isPresent() && result.get() == ButtonType.OK; //true zurück wenn mit ok bestätigt wurde
    }

	//Lösch-Dialog anzeigen und nur zurückgeben, ob bestätigt wurde
	public boolean alterBestaetigen() {
        Alert altertLoeschen = new Alert(AlertType.CONFIRMATION);
        altertLoeschen.setTitle("Löschvorgang");
        altertLoeschen.setHeaderText("Wollen Sie diese Altersklasse wirklich Löschen?");
        Optional<ButtonType> result = altertLoeschen.showAndWait();
		return result.isPresent() && result.get() == ButtonType.OK; //true zurück wenn mit ok bestätigt wurde
    }

	//Lösch-Dialog anzeigen und nur zurückgeben, ob bestätigt wurde
	public boolean schuetzeBestaetigen() {
        Alert altertLoeschen = new Alert(AlertType.CONFIRMATION);
        altertLoeschen.setTitle("Löschvorgang");
        altertLoeschen.setHeaderText("Wollen Sie diesen Schützen wirklich Löschen?");
        Optional<ButtonType> result = altertLoeschen.showAndWait();
		return result.isPresent() && result.get() == ButtonType.OK; //true zurück wenn mit ok bestätigt wurde
    }

	//Lösch-Dialog anzeigen und nur zurückgeben, ob bestätigt wurde
	public boolean begegnungBestaetigen() {
        Alert begegnungLoeschen = new Alert(AlertType.CONFIRMATION);
        begegnungLoeschen.setTitle("Löschvorgang");
        begegnungLoeschen.setHeaderText("Wollen Sie diese Begegnung wirklich Löschen?");
        Optional<ButtonType> result = begegnungLoeschen.showAndWait();
		return result.isPresent() && result.get() == ButtonType.OK; //true zurück wenn mit ok bestätigt wurde
    }
}
