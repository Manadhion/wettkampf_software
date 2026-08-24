package io.github.manadhion.wettkampf.view;

import io.github.manadhion.wettkampf.app.Controller;
import io.github.manadhion.wettkampf.data.Altersklasse;
import io.github.manadhion.wettkampf.data.Mannschaft;
import io.github.manadhion.wettkampf.data.Saison;
import io.github.manadhion.wettkampf.data.SaisonSchuetze;
import io.github.manadhion.wettkampf.data.Schuetze;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Dialog zur ausdrücklichen Korrektur einer historischen Schützenmeldung.
 */
public class SaisonSchuetzeView extends Stage {

    private final OwnAlert alert = new OwnAlert();

    public void anzeigen(Controller controller, Schuetze schuetze) {
        setTitle("Saisonzuordnung ändern");
        setResizable(false);
        initModality(Modality.APPLICATION_MODAL);

        VBox layout = new VBox(12);
        layout.setPadding(new Insets(18));
        Text hinweis = new Text("Historische Zuordnung von " + schuetze + " ausdrücklich korrigieren");

        ComboBox<Saison> saisonCombo = new ComboBox<>(FXCollections.observableArrayList(controller.alleSaisons()));
        ComboBox<Mannschaft> mannschaftCombo = new ComboBox<>();
        ComboBox<Altersklasse> altersklasseCombo = new ComboBox<>(FXCollections.observableArrayList(controller.alleAltersklassen()));
        mannschaftCombo.setDisable(true);
        altersklasseCombo.setDisable(true);

        Text status = new Text("Bitte zuerst eine Saison auswählen.");
        saisonCombo.getSelectionModel().selectedItemProperty().addListener((obs, alt, saison) -> {
            if (saison == null) return;
            mannschaftCombo.setItems(FXCollections.observableArrayList(controller.mannschaftenVonSaison(saison.getId())));
            SaisonSchuetze meldung = controller.saisonSchuetzeFinden(saison.getId(), schuetze.getId());
            String mannschaftID = meldung == null ? schuetze.getMannschaftid() : meldung.getMannschaftID();
            String altersklasseID = meldung == null ? schuetze.getAltersKlasse() : meldung.getAltersklasseID();
            waehleMannschaft(mannschaftCombo, mannschaftID);
            waehleAltersklasse(altersklasseCombo, altersklasseID);
            mannschaftCombo.setDisable(false);
            altersklasseCombo.setDisable(false);
            status.setText(meldung == null
                    ? "Noch keine Saisonmeldung – beim Speichern wird sie ausdrücklich angelegt."
                    : "Vorhandene Saisonmeldung geladen.");
        });

        Button speichern = new Button("Saisonzuordnung speichern");
        speichern.setOnAction(event -> {
            Saison saison = saisonCombo.getValue();
            Mannschaft mannschaft = mannschaftCombo.getValue();
            Altersklasse altersklasse = altersklasseCombo.getValue();
            if (saison == null || mannschaft == null || altersklasse == null) {
                alert.errorAlert("Bitte Saison, Mannschaft und Altersklasse auswählen.");
                return;
            }
            if (!alert.saisonZuordnungBestaetigen(saison.toString())) return;

            SaisonSchuetze vorhanden = controller.saisonSchuetzeFinden(saison.getId(), schuetze.getId());
            String vorname = vorhanden == null ? schuetze.getVorname() : vorhanden.getVorname();
            String nachname = vorhanden == null ? schuetze.getNachname() : vorhanden.getNachname();
            controller.saisonSchuetzeSpeichern(new SaisonSchuetze(saison.getId(), schuetze.getId(),
                    vorname, nachname, mannschaft.getId(), mannschaft.getName(), altersklasse.getId(),
                    altersklasse.getKlassenName()));
            alert.savedAlert("Saisonzuordnung wurde gespeichert");
            close();
        });

        Button abbrechen = new Button("abbrechen");
        abbrechen.setOnAction(event -> close());
        HBox buttons = new HBox(10, speichern, abbrechen);

        layout.getChildren().addAll(hinweis, zeile("Saison:", saisonCombo),
                zeile("Mannschaft:", mannschaftCombo), zeile("Altersklasse:", altersklasseCombo), status, buttons);
        Scene scene = new Scene(layout, 560, 300);
        scene.getStylesheets().add(getClass().getResource("/io/github/manadhion/wettkampf/view/style.css").toExternalForm());
        setScene(scene);
        showAndWait();
    }

    private HBox zeile(String beschriftung, ComboBox<?> combo) {
        HBox box = new HBox(10, new Text(beschriftung), combo);
        return box;
    }

    private void waehleMannschaft(ComboBox<Mannschaft> combo, String id) {
        for (Mannschaft m : combo.getItems()) if (m.getId().equals(id)) combo.setValue(m);
    }

    private void waehleAltersklasse(ComboBox<Altersklasse> combo, String id) {
        for (Altersklasse a : combo.getItems()) if (a.getId().equals(id)) combo.setValue(a);
    }
}
