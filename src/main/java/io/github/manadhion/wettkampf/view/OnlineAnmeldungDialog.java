package io.github.manadhion.wettkampf.view;

import java.util.Arrays;
import java.util.Optional;

import io.github.manadhion.wettkampf.app.Anwendungskonfiguration;
import io.github.manadhion.wettkampf.app.OnlineApiClient;
import io.github.manadhion.wettkampf.app.OnlineApiException;
import io.github.manadhion.wettkampf.app.OnlineWettkampfDatenService;
import io.github.manadhion.wettkampf.app.OnlineZugangKonfiguration;
import io.github.manadhion.wettkampf.app.OnlineZugangKonfiguration.KonfigurationException;
import io.github.manadhion.wettkampf.app.OnlineZugangKonfiguration.Zugang;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/** Fragt den ausschließlich für den Online-Modus benötigten Zugang ab. */
final class OnlineAnmeldungDialog {

    private OnlineAnmeldungDialog() {
    }

    static Optional<OnlineWettkampfDatenService> anzeigen(Stage eigentuemer) {
        String vorausgefuellteKennung = "";
        char[] vorausgefuelltesPasswort = new char[0];
        try {
            Optional<Zugang> gespeichert = OnlineZugangKonfiguration.laden();
            if (gespeichert.isPresent()) {
                try (Zugang zugang = gespeichert.get()) {
                    vorausgefuellteKennung = zugang.name();
                    vorausgefuelltesPasswort = zugang.passwort();
                }
            }
        } catch (KonfigurationException e) {
            warnung(eigentuemer, e.getMessage());
        }

        while (true) {
            TextField nameFeld = new TextField(vorausgefuellteKennung);
            PasswordField passwortFeld = new PasswordField();
            passwortFeld.setText(new String(vorausgefuelltesPasswort));
            Arrays.fill(vorausgefuelltesPasswort, '\0');
            vorausgefuelltesPasswort = new char[0];

            GridPane inhalt = new GridPane();
            inhalt.setHgap(10);
            inhalt.setVgap(10);
            inhalt.setPadding(new Insets(10));
            inhalt.addRow(0, new Label("Online-Kennung:"), nameFeld);
            inhalt.addRow(1, new Label("Passwort:"), passwortFeld);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.initOwner(eigentuemer);
            dialog.setTitle("Online-Datenbank");
            dialog.setHeaderText("Mit der Online-Datenbank anmelden");
            dialog.getDialogPane().setContent(inhalt);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Optional<ButtonType> wahl = dialog.showAndWait();
            if (wahl.isEmpty() || wahl.get() != ButtonType.OK) {
                return Optional.empty();
            }

            char[] passwort = passwortFeld.getText().toCharArray();
            passwortFeld.clear();
            OnlineApiClient client = null;
            try {
                String name = nameFeld.getText().trim();
                client = new OnlineApiClient(Anwendungskonfiguration.getOnlineServerAdresse());
                client.anmelden(name, passwort);
                try {
                    OnlineZugangKonfiguration.speichern(name, passwort);
                } catch (KonfigurationException e) {
                    warnung(eigentuemer, e.getMessage());
                }
                return Optional.of(new OnlineWettkampfDatenService(client, name, passwort));
            } catch (IllegalArgumentException | OnlineApiException e) {
                if (client != null) {
                    client.close();
                }
                vorausgefuellteKennung = nameFeld.getText().trim();
                Alert fehler = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
                fehler.initOwner(eigentuemer);
                fehler.setHeaderText("Online-Anmeldung nicht möglich");
                fehler.showAndWait();
            } finally {
                Arrays.fill(passwort, '\0');
            }
        }
    }

    private static void warnung(Stage eigentuemer, String nachricht) {
        Alert warnung = new Alert(Alert.AlertType.WARNING, nachricht, ButtonType.OK);
        warnung.initOwner(eigentuemer);
        warnung.setHeaderText("Online-Zugang nicht gespeichert");
        warnung.showAndWait();
    }
}
