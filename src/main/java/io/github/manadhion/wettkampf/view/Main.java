package io.github.manadhion.wettkampf.view;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

import io.github.manadhion.wettkampf.app.Controller;
import io.github.manadhion.wettkampf.data.Begegnung;
import io.github.manadhion.wettkampf.data.Ergebnisse;
import io.github.manadhion.wettkampf.data.Mannschaft;
import io.github.manadhion.wettkampf.data.Saison;
import io.github.manadhion.wettkampf.data.Schuetze;
import io.github.manadhion.wettkampf.data.Wettkampftage;

public class Main extends Application {

    //Controller-Objekt erzeugen
    private Controller controller = new Controller(this);

    //GUI-Elemente als Felder, damit auch die Methoden außerhalb von start() sie sehen
    private ComboBox<Saison> saisonCombo;
    private ComboBox<Wettkampftage> wTageBox;
    private ComboBox<Schuetze> schuetzeCombo;
    private ComboBox<Mannschaft> mannschaftCombo;
    private TextField ergebnisFeld;
    private Text aktTagText;
    private VBox tagAnzeige = new VBox();
    private boolean begegnungAngezeigt = true; //ist der ToogleButton gerade auf Begegnung anzeigen?
    private Button ergebnisButton = new Button("Speichern");

    //Einstiegspunkt, erzeugt eine Instanz und startet die Methode start aus der App-Klasse
    public static void main(String[] args) {
        launch(args);
    }

    //start-Methode aus der App-Klasse(JavaFX)
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Blasrohr-Wettkampf-Manager");
        
        //Tabellen anlegen wenn sie noch nicht existieren
        controller.createTableIfNotExists();

        //Oberstes Layout
		VBox top = new VBox();
		top.getStyleClass().add("root-container"); //Aufrufname für die .css Datei
		
		//Menüleiste oben
		MenuBar menuBar = new MenuBar();
		Menu fileMenu = new Menu("Datei");              //Register Datei
		MenuItem exitItem = new MenuItem("Beenden");    //Programm beenden Auswahl
		fileMenu.getItems().add(exitItem);
		menuBar.getMenus().add(fileMenu);
		top.getChildren().add(menuBar);

        //Überschrift
        Text ueberschrift = new Text("Blasrohr - Wettkampf - Manager");
        ueberschrift.getStyleClass().add("ueberschrift"); //Aufrufname für die .css Datei
        top.getChildren().add(ueberschrift);

        //Container für beide Bildhälften
        HBox haelften = new HBox();
        top.getChildren().add(haelften);
        
        //Container für die linke Bildhälfte
        VBox links = new VBox();
        links.getStyleClass().add("upper-container"); //Aufrufname für die .css Datei
        haelften.getChildren().add(links);

        //Container für die rechte Bildhälfte
        VBox rechts = new VBox();
        rechts.getStyleClass().add("upper-container"); //Aufrufname für die .css Datei
        haelften.getChildren().add(rechts);

        //auf der rechten Seite alles horizontal zentrieren
        rechts.setAlignment(Pos.TOP_CENTER);
        rechts.setFillWidth(false); //Elemente auf ihre Inhaltsbreite schrumpfen, damit sie mittig sitzen statt die volle Breite zu füllen


        // linke Seite bleibt auf ihrer benötigten Breite (Inhalt + Padding), nur die rechte Seite wächst
        HBox.setHgrow(rechts, Priority.ALWAYS);

        // links nicht über die bevorzugte Breite hinaus wachsen lassen
        links.setMaxWidth(Region.USE_PREF_SIZE);

        //Textfeld für die Auswahl der Saison
        Text saisonText = new Text("Saison: ");
        saisonText.getStyleClass().add("ueberschriftLinks"); //Aufrufname für die .css Datei
        links.getChildren().add(saisonText);

        //HBox für die Saison-Elemente
        HBox saisonContainer = new HBox();
        saisonContainer.getStyleClass().add("containerLinks"); //Aufrufname für die .css Datei
        links.getChildren().add(saisonContainer);

        //ComboBox für die Auswahl der Saison
        saisonCombo = new ComboBox<>();
        saisonCombo.getStyleClass().add("comboL"); //Aufrufname für die .css Datei
        HBox.setMargin(saisonCombo, new Insets(0, 0, 20, 0)); //Abstand unter der ComboBox zum nächsten Element
        saisonCombo.setItems(FXCollections.observableArrayList(controller.alleSaisons()));
        saisonContainer.getChildren().add(saisonCombo);

        //Buton um neue Saison anzulegen
        Button saPlusButton = new Button("+");
        saisonContainer.getChildren().add(saPlusButton);
        Tooltip tipsaPlus = new Tooltip("Neue Saison anlegen");
        tipsaPlus.setShowDelay(Duration.millis(300));
        saPlusButton.setTooltip(tipsaPlus);

        //Textfeld für die Auswahl des Wettkamptages
        Text wTagText = new Text("Wettkampftag: ");
        wTagText.getStyleClass().add("ueberschriftLinks"); //Aufrufname für die .css Datei
        links.getChildren().add(wTagText);

        //HBox für die Wettkampftage-Elemente
        HBox wTagContainer = new HBox();
        wTagContainer.getStyleClass().add("containerLinks"); //Aufrufname für die .css Datei
        links.getChildren().add(wTagContainer);

        //ComboBox für die Auswahl des Wettkamptages
        wTageBox = new ComboBox<>();
        wTageBox.getStyleClass().add("comboL"); //Aufrufname für die .css Datei
        HBox.setMargin(wTageBox, new Insets(0, 0, 20, 0)); //Abstand unter der ComboBox zum nächsten Element

        wTageBox.setDisable(true);                       // am Anfang deaktiviert
        wTageBox.setPromptText("Erst Saison wählen");    // Hinweis was zu tun ist

        wTageBox.setConverter(new javafx.util.StringConverter<Wettkampftage>() {
             @Override
            public String toString(Wettkampftage w) {   //Datum in einen String umwandeln der in der ComboBox angeuzeigt werden kann
                if (w == null) {
                    return "";
                }
                return w.getDatum().toString();
            }

             @Override
            public Wettkampftage fromString(String arg0) {
                return null; //wird nie aufgerufen da ComboBox nicht editierbar ist
            }
        });
        wTagContainer.getChildren().add(wTageBox);

        //Buton um neuen Wettkampftag anzulegen
        Button wTagPlusButton = new Button("+");
        wTagPlusButton.setDisable(true);
        wTagContainer.getChildren().add(wTagPlusButton);
        Tooltip tipWTagePlus = new Tooltip("Neuen Wettkampftag anlegen");
        tipWTagePlus.setShowDelay(Duration.millis(300));
        wTagPlusButton.setTooltip(tipWTagePlus);

        //Combobox für Wettkampftage soll reagieren, wenn eine Saison ausgewählt wird
        saisonCombo.getSelectionModel().selectedItemProperty().addListener(
            (obs, alteSaison, neueSaison) -> {

                if (neueSaison == null) {
                    //keine Saison gewählt, Wettkampftage-Box leeren und sperren
                    wTageBox.getItems().clear();
                    wTageBox.setDisable(true);
                    wTagPlusButton.setDisable(true);
                } else {
                    //Wettkampftage dieser Saison laden und Box aktivieren
                    wTageBox.setItems(FXCollections.observableArrayList(
                        controller.wettkampftageVonSaison(neueSaison.getId())));
                    wTageBox.setDisable(false);
                    wTagPlusButton.setDisable(false);
                }
            });

        //Textfeld für die Auswahl der Mannschaft
        Text mannschaftText = new Text("Mannschaft: ");
        mannschaftText.getStyleClass().add("ueberschriftLinks"); //Aufrufname für die .css Datei
        links.getChildren().add(mannschaftText);

        //HBox für die Mannschafts-Elemente
        HBox mannschaftBox = new HBox();
        mannschaftBox.getStyleClass().add("containerLinks"); //Aufrufname für die .css Datei
        links.getChildren().add(mannschaftBox);

        //ComboBox für die Auswahl der Mannschaft
        mannschaftCombo = new ComboBox<>();
        mannschaftCombo.getStyleClass().add("comboL"); //Aufrufname für die .css Datei
        HBox.setMargin(mannschaftCombo, new Insets(0, 0, 20, 0)); //Abstand unter der ComboBox zum nächsten Element
        mannschaftCombo.setItems(FXCollections.observableArrayList(controller.alleMannschaften()));
        mannschaftBox.getChildren().add(mannschaftCombo);

        //Buton um neue Mannschaft anzulegen
        Button mPlusButton = new Button("+");
        mannschaftBox.getChildren().add(mPlusButton);
        Tooltip tipmPlus = new Tooltip("Neue Mannschaft anlegen");
        tipmPlus.setShowDelay(Duration.millis(300));
        mPlusButton.setTooltip(tipmPlus);

        //Textfeld für die Auswahl des Schützen
        Text schuetzeText = new Text("Schütze: ");
        schuetzeText.getStyleClass().add("ueberschriftLinks");  //Aufrufname für die .css Datei
        links.getChildren().add(schuetzeText);

        //HBox für die Schützen-Elemente
        HBox schuetzeBox = new HBox();
        schuetzeBox.getStyleClass().add("containerLinks");  //Aufrufname für die .css Datei
        links.getChildren().add(schuetzeBox);

        //ComboBox für die Auswahl des Schützen
        schuetzeCombo = new ComboBox<>();
        schuetzeCombo.getStyleClass().add("comboL");  //Aufrufname für die .css Datei
        HBox.setMargin(schuetzeCombo, new Insets(0, 0, 20, 0)); //Abstand unter der ComboBox zum nächsten Element

        schuetzeCombo.setDisable(true);                         // am Anfang deaktiviert
        schuetzeCombo.setPromptText("Erst Mannschaft wählen");  // Hinweis was zu tun ist

        schuetzeBox.getChildren().add(schuetzeCombo);

        //Buton um neuen Schützen anzulegen
        Button sPlusButton = new Button("+");
        sPlusButton.setDisable(true);
        schuetzeBox.getChildren().add(sPlusButton);
        Tooltip tipsPlus = new Tooltip("Neuen Schützen anlegen");
        tipsPlus.setShowDelay(Duration.millis(300));
        sPlusButton.setTooltip(tipsPlus);

        //Combobox für Schützen soll reagieren, wenn eine Mannschaft ausgewählt wird
        mannschaftCombo.getSelectionModel().selectedItemProperty().addListener(
            (obs, alteMannschaft, neueMannschaft) -> {

                if (neueMannschaft == null) {
                    //keine Mannschaft gewählt, Schützen-Box leeren und sperren
                    schuetzeCombo.getItems().clear();
                    schuetzeCombo.setDisable(true);
                    sPlusButton.setDisable(true);
                } else {
                    //Schützen dieser Mannschaft laden und Box aktivieren
                    schuetzeCombo.setItems(FXCollections.observableArrayList(
                        controller.schuetzenVonMannschaft(neueMannschaft.getId())));
                    schuetzeCombo.setDisable(false);
                    sPlusButton.setDisable(false);
                }
            });
        
        //Textfeld-Überschrift für das Ergebnis
        Text ergebnisText = new Text("Ergebnis: ");
        ergebnisText.getStyleClass().add("ueberschriftLinks"); //Aufrufname für die .css Datei
        links.getChildren().add(ergebnisText);

        //HBox für die Ergebnis-Elemente
        HBox ergebnisBox = new HBox();
        ergebnisBox.getStyleClass().add("containerLinks"); //Aufrufname für die .css Datei
        links.getChildren().add(ergebnisBox);

        //Eingabefeld für das Ergebnis
        ergebnisFeld = new TextField();
        HBox.setMargin(ergebnisFeld, new Insets(0, 0, 20, 0)); //Abstand unter der ComboBox zum nächsten Element

        ergebnisFeld.setDisable(true);                          // am Anfang gesperrt
        ergebnisFeld.setPromptText("Erst Schütze wählen");      // Hinweis was zu tun ist

        //Filter damit nur Ziffern eingegeben werden können, da das Ergebnis eine Zahl ist
        UnaryOperator<TextFormatter.Change> nurZahlen = aenderung -> {
            String neuerText = aenderung.getControlNewText();
            if (neuerText.matches("\\d*")) {    // \d* = keine oder mehrere Ziffern
                return aenderung;               // Eingabe erlauben
            }
            return null;                        // Eingabe ablehnen
        };
        ergebnisFeld.setTextFormatter(new TextFormatter<>(nurZahlen));

        ergebnisBox.getChildren().add(ergebnisFeld);

        //Buton um das eingegebene Ergebnis zu speichern
        ergebnisButton.setDisable(true);
        ergebnisBox.getChildren().add(ergebnisButton);
        Tooltip tipErgebnis = new Tooltip("Ergebnis für diesen Schützen und Wettkampftag speichern");
        tipErgebnis.setShowDelay(Duration.millis(300));
        ergebnisButton.setTooltip(tipErgebnis);
        ergebnisButton.setOnAction(e -> {
            ergebnisSpeichern(); //speichert das Ergebnis in die DB
            if (begegnungAngezeigt == true) {   //wenn Begegnungen gerade angezeigt werden, diese Aktuallisieren
                begegnungenAnzeigen();
            }
        });

        //Ergebnisfeld soll reagieren, wenn sich der Schütze ändert
        schuetzeCombo.getSelectionModel().selectedItemProperty().addListener(
            (obs, alt, neu) -> {
                ergebnisAktualisieren();
            });

        //Ergebnisfeld soll reagieren, wenn sich der Wettkampftag ändert
        wTageBox.getSelectionModel().selectedItemProperty().addListener(
            (obs, alt, neu) -> {
                ergebnisAktualisieren();
                aktTagAnzeigen();  
            });

        //Textfeld-Überschrift für den aktuell angezeigten Wettkampftag
        aktTagText = new Text();
        aktTagAnzeigen();
        aktTagText.getStyleClass().add("ueberschriftRechts"); //Aufrufname für die .css Datei
        rechts.getChildren().add(aktTagText);

        //HBox um zwischen Begegnungen und aktuelle Tabelle zu springen
        HBox steuerButton = new HBox();
        steuerButton.getStyleClass().add("containerRechts"); //Aufrufname für die .css Datei
        VBox.setMargin(steuerButton, new Insets(20, 0, 0, 0)); //Abstand über der ComboBox zum nächsten Element
        rechts.getChildren().add(steuerButton);

        //Umschalter zwischen Begegnungen und Tabelle, es kann immer nur einer aktiv sein
        ToggleGroup ansichtGruppe = new ToggleGroup();
        ToggleButton begegnungenButton = new ToggleButton("Begegnungen");
        ToggleButton tabelleButton = new ToggleButton("Tabelle");
        begegnungenButton.setToggleGroup(ansichtGruppe);
        tabelleButton.setToggleGroup(ansichtGruppe);
        begegnungenButton.setSelected(true);  //Begegnungen als Standardauswahl beim Start
        steuerButton.getChildren().addAll(begegnungenButton, tabelleButton);
        //verhindern, dass durch erneutes Klicken gar nichts mehr ausgewählt ist
        ansichtGruppe.selectedToggleProperty().addListener((obs, alt, neu) -> {
            if (neu == null) {
                ansichtGruppe.selectToggle(alt);   //den vorherigen wieder aktivieren
            }
            if (neu == begegnungenButton) {
                begegnungenAnzeigen();
                begegnungAngezeigt = true;

            } else if (neu == tabelleButton) {
                begegnungAngezeigt = false;
                return;
            }
        });

        //Platzhalter-Box für Begegnungen oder Tabelle einfügen
        rechts.getChildren().add(tagAnzeige);


        Scene scene = (new Scene(top, 1300, 900)); //Fenstereinstellungens-Parameter

        //style.css in dieses Fenster/Szene einbinden
        scene.getStylesheets().add(getClass().getResource("/io/github/manadhion/wettkampf/view/style.css").toExternalForm());

        primaryStage.setScene(scene);   //übernimmt Szene scene als Argument
		primaryStage.show();            //öffnet das Fenster

    }

    //Ergebnisfeld je nach Auswahl füllen und sperren bzw. freigeben
    private void ergebnisAktualisieren() {

        //aktuell gewählten Schützen und Wettkampftag holen
        Schuetze schuetze = schuetzeCombo.getSelectionModel().getSelectedItem();
        Wettkampftage tag = wTageBox.getSelectionModel().getSelectedItem();

        if (schuetze == null || tag == null) {
            //es ist noch nicht beides gewählt, Feld leeren und sperren
            ergebnisFeld.clear();
            ergebnisFeld.setDisable(true);
            ergebnisButton.setDisable(true);
        } else {
            //vorhandenes Ergebnis laden, kann null sein wenn es noch keins gibt
            Ergebnisse ergebnis = controller.ergebnisFuer(schuetze.getId(), tag.getId());
            ergebnisFeld.setText(ergebnis == null ? "" : String.valueOf(ergebnis.getErgebnis()));
            ergebnisFeld.setDisable(false);
            ergebnisButton.setDisable(false);
        }
    }

    //eingegebenes Ergebnis für den gewählten Schützen und Wettkampftag speichern
    private void ergebnisSpeichern() {

        //aktuell gewählten Schützen und Wettkampftag holen
        Schuetze schuetze = schuetzeCombo.getSelectionModel().getSelectedItem();
        Wettkampftage tag = wTageBox.getSelectionModel().getSelectedItem();

        //ohne Schütze und Wettkampftag gibt es nichts zu speichern
        if (schuetze == null || tag == null) {
            return;
        }

        //leeres Feld nicht speichern
        if (ergebnisFeld.getText().isEmpty()) {
            return;
        }

        //Text in eine Zahl umwandeln (durch den Filter sind nur Ziffern möglich) und speichern
        int wert = Integer.parseInt(ergebnisFeld.getText());
        controller.ergebnisSpeichern(schuetze.getId(), tag.getId(), wert);
    }

    //Überschrift rechts auf den aktuell gewählten Wettkampftag setzen
    private void aktTagAnzeigen() {

        Wettkampftage tag = wTageBox.getSelectionModel().getSelectedItem();

        if (tag == null) {
            aktTagText.setText("Wettkampf am — (kein Tag gewählt)");
        } else {
            aktTagText.setText("Wettkampf am " + tag.getDatum().toString());
        }
    }

    //Begegnungen in tagAnzeige anzeigen
    public void begegnungenAnzeigen() {

        //Begegnungen auslesen von diesem Tag
        Wettkampftage tag = wTageBox.getSelectionModel().getSelectedItem();
        List<Begegnung> begegnungen = controller.begegnungenAnDiesemTag(tag.getId());

        //Für jede Begegnung an diesem Tag lege an:
        for(Begegnung b :begegnungen) {

            //Mannschaften holen
            Mannschaft heim = controller.mannschaftMitID(b.getHeim());
            Mannschaft gegner = controller.mannschaftMitID(b.getGegner());

            //Liste der Schützen der Mannschaften holen
            List<Schuetze> heimSchuetzen = controller.schuetzenVonMannschaft(heim.getId());
            List<Schuetze> gegnerSchuetzen = controller.schuetzenVonMannschaft(gegner.getId());

            //Ergebnissliste aller Ergebnisse der Schützen die bereits geschossen haben
            List<Integer> ergebnisHeim = new ArrayList<>();
            List<Integer> ergebnisGegner = new ArrayList<>();

            //Ergebnisse aller Schützen die Heute bereits geschossen haben in die Liste aufnehmen
            for(Schuetze s :heimSchuetzen) {
                Ergebnisse e = controller.ergebnisFuer(s.getId(), b.getWettkampftag());
                if (e != null){
                    ergebnisHeim.add(e.getErgebnis());
                }
            }
            Collections.sort(heimSchuetzen, Collections.reverseOrder()); //Liste absteigend sortieren

            //Ergebnisse aller Schützen die Heute bereits geschossen haben in die Liste aufnehmen
            for(Schuetze s :gegnerSchuetzen) {
                Ergebnisse e = controller.ergebnisFuer(s.getId(), b.getWettkampftag());
                if (e != null){
                    ergebnisGegner.add(e.getErgebnis());
                }
            }
            Collections.sort(gegnerSchuetzen, Collections.reverseOrder());  //Liste absteigend sortieren

            //Variablen für die Gesamtergebnisse
            int gesamtHeim = 0; //Gesamtergebnis
            int gesamtGegner = 0; //Gesamtergebnis

            //Die besten 3 Ergebnisse zum Gesamtergebnis zählen, oder alle wenn noch keine 3 vorhanden sind
            if (ergebnisHeim.size() >= 3) {
                gesamtHeim = ergebnisHeim.get(0) + ergebnisHeim.get(1) + ergebnisHeim.get(2);
            }
            else {
                for (int i :ergebnisHeim) {
                    gesamtHeim += i;
                }
            }

            //Die besten 3 Ergebnisse zum Gesamtergebnis zählen, oder alle wenn noch keine 3 vorhanden sind
            if (ergebnisGegner.size() >= 3) {
                gesamtGegner = ergebnisGegner.get(0) + ergebnisGegner.get(1) + ergebnisGegner.get(2);
            }
            else {
                for (int i :ergebnisGegner) {
                    gesamtGegner += i;
                }
            }
            
            //Text der als Begegnung angezeigt wird   
            Text begegnung = new Text(heim.getName() + " vs. " + gegner.getName() + " " + gesamtHeim + " : " + gesamtGegner);
            begegnung.getStyleClass().add("begegnung-Text"); //Aufrufname für die .css Datei
            tagAnzeige.getChildren().add(begegnung);

        }

    }
}
