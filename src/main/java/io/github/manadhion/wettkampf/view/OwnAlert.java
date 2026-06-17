package io.github.manadhion.wettkampf.view;

import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

/**
 * Stellt die verschiedenen Hinweis-, Fehler- und Bestätigungsdialoge der App bereit.
 */
public class OwnAlert {

    /**
     * Informationshinweis anzeigen.
     * @param message anzuzeigender Hinweistext
     */
	public void infoAlert(String message) {
		Alert info = new Alert(AlertType.INFORMATION);
		info.setTitle("Information");
		info.setHeaderText(message);
		info.showAndWait();
	}

    /**
     * Fehlermeldung anzeigen.
     * @param text anzuzeigender Fehlertext
     */
	public void errorAlert(String text) {
		Alert eqAlert = new Alert(AlertType.ERROR);
		eqAlert.setTitle("Fehler!");
		eqAlert.setHeaderText(text);
		eqAlert.showAndWait();
	}

    /**
     * Hinweis über erfolgreiches Speichern anzeigen.
     * @param message anzuzeigender Hinweistext
     */
	public void savedAlert(String message) {
		Alert saved = new Alert(AlertType.INFORMATION);
		saved.setTitle("gespeichert");
		saved.setHeaderText(message);
		saved.showAndWait();
	}

    /**
     * Lösch-Dialog für eine Saison anzeigen.
     * @return true wenn mit OK bestätigt wurde
     */
    public boolean saisonLoeschenBestaetigen() {
        Alert saisonLoeschen = new Alert(AlertType.CONFIRMATION);
        saisonLoeschen.setTitle("Löschvorgang");
        saisonLoeschen.setHeaderText("Wollen Sie diese Saison wirklich Löschen?");
        Optional<ButtonType> result = saisonLoeschen.showAndWait();
		return result.isPresent() && result.get() == ButtonType.OK; //true zurück wenn mit ok bestätigt wurde
    }

	/**
	 * Lösch-Dialog für einen Wettkampftag anzeigen.
	 * @return true wenn mit OK bestätigt wurde
	 */
	public boolean wTagLoeschenBestaetigen() {
        Alert wTagLoeschen = new Alert(AlertType.CONFIRMATION);
        wTagLoeschen.setTitle("Löschvorgang");
        wTagLoeschen.setHeaderText("Wollen Sie diesen Wettkampftag wirklich Löschen?");
        Optional<ButtonType> result = wTagLoeschen.showAndWait();
		return result.isPresent() && result.get() == ButtonType.OK; //true zurück wenn mit ok bestätigt wurde
    }

	/**
	 * Lösch-Dialog für eine Liga anzeigen.
	 * @return true wenn mit OK bestätigt wurde
	 */
	public boolean ligaLöschBestaetigen() {
        Alert ligaLoeschen = new Alert(AlertType.CONFIRMATION);
        ligaLoeschen.setTitle("Löschvorgang");
        ligaLoeschen.setHeaderText("Wollen Sie diese Liga wirklich Löschen?");
        Optional<ButtonType> result = ligaLoeschen.showAndWait();
		return result.isPresent() && result.get() == ButtonType.OK; //true zurück wenn mit ok bestätigt wurde
    }

	/**
	 * Lösch-Dialog für eine Mannschaft anzeigen.
	 * @return true wenn mit OK bestätigt wurde
	 */
	public boolean mannschaftBestaetigen() {
        Alert mannschaftLoeschen = new Alert(AlertType.CONFIRMATION);
        mannschaftLoeschen.setTitle("Löschvorgang");
        mannschaftLoeschen.setHeaderText("Wollen Sie diese Mannschaft wirklich Löschen?");
        Optional<ButtonType> result = mannschaftLoeschen.showAndWait();
		return result.isPresent() && result.get() == ButtonType.OK; //true zurück wenn mit ok bestätigt wurde
    }

	/**
	 * Lösch-Dialog für eine Liga anzeigen.
	 * @return true wenn mit OK bestätigt wurde
	 */
	public boolean ligaBestaetigen() {
        Alert ligaLoeschen = new Alert(AlertType.CONFIRMATION);
        ligaLoeschen.setTitle("Löschvorgang");
        ligaLoeschen.setHeaderText("Wollen Sie diese Liga wirklich Löschen?");
        Optional<ButtonType> result = ligaLoeschen.showAndWait();
		return result.isPresent() && result.get() == ButtonType.OK; //true zurück wenn mit ok bestätigt wurde
    }

	/**
	 * Lösch-Dialog für eine Altersklasse anzeigen.
	 * @return true wenn mit OK bestätigt wurde
	 */
	public boolean alterBestaetigen() {
        Alert altertLoeschen = new Alert(AlertType.CONFIRMATION);
        altertLoeschen.setTitle("Löschvorgang");
        altertLoeschen.setHeaderText("Wollen Sie diese Altersklasse wirklich Löschen?");
        Optional<ButtonType> result = altertLoeschen.showAndWait();
		return result.isPresent() && result.get() == ButtonType.OK; //true zurück wenn mit ok bestätigt wurde
    }

	/**
	 * Lösch-Dialog für einen Schützen anzeigen.
	 * @return true wenn mit OK bestätigt wurde
	 */
	public boolean schuetzeBestaetigen() {
        Alert altertLoeschen = new Alert(AlertType.CONFIRMATION);
        altertLoeschen.setTitle("Löschvorgang");
        altertLoeschen.setHeaderText("Wollen Sie diesen Schützen wirklich Löschen?");
        Optional<ButtonType> result = altertLoeschen.showAndWait();
		return result.isPresent() && result.get() == ButtonType.OK; //true zurück wenn mit ok bestätigt wurde
    }

	/**
	 * Lösch-Dialog für eine Begegnung anzeigen.
	 * @return true wenn mit OK bestätigt wurde
	 */
	public boolean begegnungBestaetigen() {
        Alert begegnungLoeschen = new Alert(AlertType.CONFIRMATION);
        begegnungLoeschen.setTitle("Löschvorgang");
        begegnungLoeschen.setHeaderText("Wollen Sie diese Begegnung wirklich Löschen?");
        Optional<ButtonType> result = begegnungLoeschen.showAndWait();
		return result.isPresent() && result.get() == ButtonType.OK; //true zurück wenn mit ok bestätigt wurde
    }
}
