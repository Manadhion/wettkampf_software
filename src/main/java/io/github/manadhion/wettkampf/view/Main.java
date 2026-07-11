package io.github.manadhion.wettkampf.view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import io.github.manadhion.wettkampf.app.Controller;
import io.github.manadhion.wettkampf.app.DBController;
import io.github.manadhion.wettkampf.data.Begegnung;
import io.github.manadhion.wettkampf.data.Ergebnisse;
import io.github.manadhion.wettkampf.data.Mannschaft;
import io.github.manadhion.wettkampf.data.Saison;
import io.github.manadhion.wettkampf.data.Schuetze;
import io.github.manadhion.wettkampf.data.Wettkampftage;

/**
 * Hauptfenster und Einstiegspunkt der Anwendung, zeigt Saison, Wettkampftage, Mannschaften, Schützen und Begegnungen.
 */
public class Main extends Application {

    //Formatter für die deutsche Datumsanzeige
    private static final DateTimeFormatter DATUM_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    //Controller-Objekt erzeugen
    private Controller controller = new Controller(this);

    //GUI-Elemente als Felder, damit auch die Methoden außerhalb von start  sie sehen
    private ComboBox<Saison> saisonCombo;
    private ComboBox<Wettkampftage> wTageBox;
    private ComboBox<Schuetze> schuetzeCombo;
    private ComboBox<Mannschaft> mannschaftCombo;
    private TextField ergebnisFeld;
    private Text ergebnisText;
    private Text aktTagText;
    private VBox tagAnzeige = new VBox();
    private Button ergebnisButton = new Button("speichern");
    private Button begegnungButton;
    private Button beamerButton = new Button("Beamer-Anzeige starten");
    private Button pdfButton = new Button("Saison-PDF …");

    /**
     * Einstiegspunkt, erzeugt eine Instanz und startet die Methode start aus der App-Klasse.
     * @param args Kommandozeilenargumente
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Baut das Hauptfenster auf und zeigt es an (start-Methode aus der App-Klasse von JavaFX).
     * @param primaryStage von JavaFX bereitgestelltes Hauptfenster
     */
    @Override
    public void start(Stage primaryStage) {

        //beim ersten Start festlegen welche Datenbank verwendet wird
        datenbankFestlegen(primaryStage);

        //Tabellen anlegen wenn sie noch nicht existieren
        controller.createTableIfNotExists();

        //Fenstertitel zeigt die aktuell geöffnete Datenbank
        titelAktualisieren(primaryStage);

        //Oberstes Layout
		VBox top = new VBox();
		top.getStyleClass().add("root-container"); //Aufrufname für die .css Datei
		
		//Menüleiste oben
		MenuBar menuBar = new MenuBar();
		Menu fileMenu = new Menu("Datei");              //Register Datei
		MenuItem dbOeffnenItem = new MenuItem("Datenbank öffnen…"); //andere Datenbank laden
		dbOeffnenItem.setOnAction(event -> {
			datenbankWechseln(primaryStage);
		});
		MenuItem exitItem = new MenuItem("Beenden");    //Programm beenden Auswahl
		exitItem.setOnAction(event -> {
			Platform.exit();
		});
		fileMenu.getItems().add(dbOeffnenItem);
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

        //Button um die Beamer-Anzeige des gewählten Wettkampftages zu starten
        beamerButton.setDisable(true); //erst aktiv wenn ein Wettkampftag gewählt ist
        Tooltip tipBeamer = new Tooltip("Beamer-Anzeige für den gewählten Wettkampftag starten");
        tipBeamer.setShowDelay(Duration.millis(300));
        beamerButton.setTooltip(tipBeamer);
        beamerButton.setOnAction(event -> {
            Wettkampftage tag = wTageBox.getSelectionModel().getSelectedItem();
            if (tag != null) {
                controller.beamerFenster(tag);
            }
        });
        links.getChildren().add(beamerButton);

        //Button um die Saisondaten bis zum gewählten Wettkampftag als PDF zu speichern
        pdfButton.setDisable(true); //erst aktiv wenn ein Wettkampftag gewählt ist
        Tooltip tipPdf = new Tooltip("Saisondaten bis zum gewählten Wettkampftag als PDF speichern");
        tipPdf.setShowDelay(Duration.millis(300));
        pdfButton.setTooltip(tipPdf);
        pdfButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Wettkampftage tag = wTageBox.getSelectionModel().getSelectedItem();
                if (tag == null) {
                    return;
                }
                //Zielort für das PDF wählen, mit Namensvorschlag aus dem Datum des Wettkampftages
                FileChooser pdfAuswahl = new FileChooser();
                pdfAuswahl.setTitle("Saison-PDF speichern");
                pdfAuswahl.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF-Dateien", "*.pdf"));
                pdfAuswahl.setInitialFileName("Saison_bis_" + tag.getDatum().format(DATUM_FORMAT).replace('.', '-') + ".pdf");
                File ziel = pdfAuswahl.showSaveDialog(primaryStage);
                if (ziel != null) {
                    controller.saisonAlsPdf(tag, ziel);
                }
            }
        });
        links.getChildren().add(pdfButton);

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
        saPlusButton.getStyleClass().add("add"); //Aufrufname für die .css Datei
        saisonContainer.getChildren().add(saPlusButton);
        Tooltip tipsaPlus = new Tooltip("Neue Saison anlegen");
        tipsaPlus.setShowDelay(Duration.millis(300));
        saPlusButton.setTooltip(tipsaPlus);
        saPlusButton.setOnAction(event -> {
			controller.neueSaisonFenster();
		});

        //Buton um Saison zu löschen
        Button saMinusButton = new Button("-");
        saMinusButton.setDisable(true);
        saMinusButton.getStyleClass().add("danger"); //Aufrufname für die .css Datei
        saisonContainer.getChildren().add(saMinusButton);
        Tooltip tipsaMinus = new Tooltip("Saison löschen");
        tipsaMinus.setShowDelay(Duration.millis(300));
        saMinusButton.setTooltip(tipsaMinus);
        saMinusButton.setOnAction(event -> {
            String id = saisonCombo.getSelectionModel().getSelectedItem().getId();
            controller.saisonLöschenWarnen(id);
		});

        //Buton um Saison zu bearbeiten
        Button saEditButton = new Button("✏");
        saEditButton.setDisable(true);
        saEditButton.getStyleClass().add("edit"); //Aufrufname für die .css Datei
        saisonContainer.getChildren().add(saEditButton);
        Tooltip tipsaEdit = new Tooltip("Saison bearbeiten");
        tipsaEdit.setShowDelay(Duration.millis(300));
        saEditButton.setTooltip(tipsaEdit);
        saEditButton.setOnAction(event -> {
            controller.saisonBearbeitenFenster(saisonCombo.getSelectionModel().getSelectedItem());
		});

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
                return w.getDatum().format(DATUM_FORMAT);
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
        wTagPlusButton.getStyleClass().add("add"); //Aufrufname für die .css Datei
        wTagContainer.getChildren().add(wTagPlusButton);
        Tooltip tipWTagePlus = new Tooltip("Neuen Wettkampftag anlegen");
        tipWTagePlus.setShowDelay(Duration.millis(300));
        wTagPlusButton.setTooltip(tipWTagePlus);
        wTagPlusButton.setOnAction(event -> {
            controller.neuerWettkampftagFenster(saisonCombo.getSelectionModel().getSelectedItem());
        });

        //Buton um Wettkampftag zu löschen
        Button wtMinusButton = new Button("-");
        wtMinusButton.setDisable(true);
        wtMinusButton.getStyleClass().add("danger"); //Aufrufname für die .css Datei
        wTagContainer.getChildren().add(wtMinusButton);
        Tooltip tipwtMinus = new Tooltip("Wettkampftag löschen");
        tipwtMinus.setShowDelay(Duration.millis(300));
        wtMinusButton.setTooltip(tipwtMinus);
        wtMinusButton.setOnAction(event -> {
            controller.wTagLöschenWarnen(wTageBox.getSelectionModel().getSelectedItem().getId(), saisonCombo.getSelectionModel().getSelectedItem().getId());
		});

        //Buton um Wettkampftag zu bearbeiten
        Button wtEditButton = new Button("✏");
        wtEditButton.setDisable(true);
        wtEditButton.getStyleClass().add("edit"); //Aufrufname für die .css Datei
        wTagContainer.getChildren().add(wtEditButton);
        Tooltip tipwtEdit = new Tooltip("Wettkampftag bearbeiten");
        tipwtEdit.setShowDelay(Duration.millis(300));
        wtEditButton.setTooltip(tipwtEdit);
        wtEditButton.setOnAction(event -> {
            controller.wTagBearbeitenFenster(wTageBox.getSelectionModel().getSelectedItem());
		});

        //Combobox für Wettkampftage soll reagieren, wenn eine Saison ausgewählt wird
        saisonCombo.getSelectionModel().selectedItemProperty().addListener(
            (obs, alteSaison, neueSaison) -> {

                if (neueSaison == null) {
                    //keine Saison gewählt, Wettkampftage-Box leeren und sperren, sowie Saisonlöschen-Button deaktivieren
                    wTageBox.getItems().clear();
                    wTageBox.setDisable(true);
                    wTagPlusButton.setDisable(true);
                    saMinusButton.setDisable(true);
                    saEditButton.setDisable(true);
                } else {
                    //Wettkampftage dieser Saison laden und Box aktivieren, sowie Saisonlöschen-Button aktivieren
                    wTageBox.setItems(FXCollections.observableArrayList(
                        controller.wettkampftageVonSaison(neueSaison.getId())));
                    wTageBox.setDisable(false);
                    wTagPlusButton.setDisable(false);
                    saMinusButton.setDisable(false);
                    saEditButton.setDisable(false);
                }
            });

        //Wettkmapftage löschen-Button aktivieren wenn ein Tag ausgewählt ist
        wTageBox.getSelectionModel().selectedItemProperty().addListener(
            (obs, alterTag, neuerTag) -> {

                if (neuerTag == null) {
                    //wenn kein Tag gewählt ist, löschen-, bearbeiten-, Beamer- und PDF-Button deaktivieren
                    wtMinusButton.setDisable(true);
                    wtEditButton.setDisable(true);
                    beamerButton.setDisable(true);
                    pdfButton.setDisable(true);
                } else {
                    //wenn ein Tag gewählt ist, löschen-, bearbeiten-, Beamer- und PDF-Button aktivieren und begegnungen aktuallisieren
                    wtMinusButton.setDisable(false);
                    wtEditButton.setDisable(false);
                    beamerButton.setDisable(false);
                    pdfButton.setDisable(false);
                    begegnungenAnzeigen();
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
        mPlusButton.getStyleClass().add("add"); //Aufrufname für die .css Datei
        mannschaftBox.getChildren().add(mPlusButton);
        Tooltip tipmPlus = new Tooltip("Neue Mannschaft anlegen");
        tipmPlus.setShowDelay(Duration.millis(300));
        mPlusButton.setTooltip(tipmPlus);
        mPlusButton.setOnAction(event -> {
            controller.neueeMannschaftFenster();
        });

        //Buton um Mannschaft zu löschen
        Button mMinusButton = new Button("-");
        mMinusButton.setDisable(true);
        mMinusButton.getStyleClass().add("danger"); //Aufrufname für die .css Datei
        mannschaftBox.getChildren().add(mMinusButton);
        Tooltip tipmMinus = new Tooltip("Mannschaft löschen");
        tipmMinus.setShowDelay(Duration.millis(300));
        mMinusButton.setTooltip(tipmMinus);
        mMinusButton.setOnAction(event -> {
            controller.mannschaftLöschenWarnen(mannschaftCombo.getSelectionModel().getSelectedItem().getId());
		});

        //Buton um Mannschaft zu bearbeiten
        Button mEditButton = new Button("✏");
        mEditButton.setDisable(true);
        mEditButton.getStyleClass().add("edit"); //Aufrufname für die .css Datei
        mannschaftBox.getChildren().add(mEditButton);
        Tooltip tipmEdit = new Tooltip("Mannschaft bearbeiten");
        tipmEdit.setShowDelay(Duration.millis(300));
        mEditButton.setTooltip(tipmEdit);
        mEditButton.setOnAction(event -> {
            controller.mannschaftBearbeitenFenster(mannschaftCombo.getSelectionModel().getSelectedItem());
		});

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
        sPlusButton.getStyleClass().add("add"); //Aufrufname für die .css Datei
        schuetzeBox.getChildren().add(sPlusButton);
        Tooltip tipsPlus = new Tooltip("Neuen Schützen anlegen");
        tipsPlus.setShowDelay(Duration.millis(300));
        sPlusButton.setTooltip(tipsPlus);
        sPlusButton.setOnAction(event -> {
            controller.neueSchuetzeFenster(mannschaftCombo.getSelectionModel().getSelectedItem());
        });

        //Buton um Schützen zu löschen
        Button sMinusButton = new Button("-");
        sMinusButton.setDisable(true);
        sMinusButton.getStyleClass().add("danger"); //Aufrufname für die .css Datei
        schuetzeBox.getChildren().add(sMinusButton);
        Tooltip tipsMinus = new Tooltip("Schützen löschen");
        tipsMinus.setShowDelay(Duration.millis(300));
        sMinusButton.setTooltip(tipsMinus);
        sMinusButton.setOnAction(event -> {
            controller.schuetzeLöschenWarnen(schuetzeCombo.getSelectionModel().getSelectedItem().getId());
		});

        //Buton um Schützen zu bearbeiten
        Button sEditButton = new Button("✏");
        sEditButton.setDisable(true);
        sEditButton.getStyleClass().add("edit"); //Aufrufname für die .css Datei
        schuetzeBox.getChildren().add(sEditButton);
        Tooltip tipsEdit = new Tooltip("Schützen bearbeiten");
        tipsEdit.setShowDelay(Duration.millis(300));
        sEditButton.setTooltip(tipsEdit);
        sEditButton.setOnAction(event -> {
            controller.schuetzeBearbeitenFenster(schuetzeCombo.getSelectionModel().getSelectedItem());
		});

        //Schütze löschen-Button aktivieren wenn ein Schütze ausgewählt ist
        schuetzeCombo.getSelectionModel().selectedItemProperty().addListener(
            (obs, alterSchuetze, neuerSchuetze) -> {

                if (neuerSchuetze == null) {
                    //wenn kein Schütze gewählt ist, löschen- und bearbeiten-Button deaktivieren
                    sMinusButton.setDisable(true);
                    sEditButton.setDisable(true);
                } else {
                    //wenn ein Schütze gewählt ist, löschen- und bearbeiten-Button aktivieren
                    sMinusButton.setDisable(false);
                    sEditButton.setDisable(false);
                }
            });

        //Combobox für Schützen soll reagieren, wenn eine Mannschaft ausgewählt wird
        mannschaftCombo.getSelectionModel().selectedItemProperty().addListener(
            (obs, alteMannschaft, neueMannschaft) -> {

                if (neueMannschaft == null) {
                    //keine Mannschaft gewählt, Schützen-Box leeren und sperren
                    schuetzeCombo.getItems().clear();
                    schuetzeCombo.setDisable(true);
                    sPlusButton.setDisable(true);
                    mMinusButton.setDisable(true);
                    mEditButton.setDisable(true);
                } else {
                    //Schützen dieser Mannschaft laden und Box aktivieren
                    schuetzeCombo.setItems(FXCollections.observableArrayList(
                        controller.schuetzenVonMannschaft(neueMannschaft.getId())));
                    schuetzeCombo.setDisable(false);
                    sPlusButton.setDisable(false);
                    mMinusButton.setDisable(false);
                    mEditButton.setDisable(false);
                }
            });
        
        //Textfeld-Überschrift für das Ergebnis, benennt den aktuell gewählten Schützen
        ergebnisText = new Text("Ergebnis des Schützen: ");
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
            begegnungenAnzeigen();
        });

        //Ergebnisfeld sollen reagieren, wenn sich der Schütze ändert
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
        aktTagText.getStyleClass().add("ueberschriftRechts"); //Aufrufname für die .css Datei

        //Button um neue Begegnbung an dem Tag anzulegen
        begegnungButton = new Button("Begegnung anlegen");
        Tooltip tipBegegnung = new Tooltip("Neue Begegnung an diesem Wettkampftag anlegen");
        tipBegegnung.setShowDelay(Duration.millis(300));
        begegnungButton.setTooltip(tipBegegnung);
        begegnungButton.setOnAction(event -> {
            controller.neueBegegnungFenster(wTageBox.getSelectionModel().getSelectedItem().getId());
        });

        //Überschrift und Button-Status anhand der aktuellen Auswahl setzen (braucht aktTagText und begegnungButton)
        aktTagAnzeigen();

        //Elemente in gewünschter Reihenfolge einfügen
        rechts.getChildren().add(aktTagText);
        rechts.getChildren().add(begegnungButton);

        //Platzhalter-Box für Begegnungen einfügen
        rechts.getChildren().add(tagAnzeige);


        Scene scene = (new Scene(top, 1300, 900)); //Fenstereinstellungens-Parameter

        //style.css in dieses Fenster/Szene einbinden
        scene.getStylesheets().add(getClass().getResource("/io/github/manadhion/wettkampf/view/style.css").toExternalForm());

        primaryStage.setScene(scene);   //übernimmt Szene scene als Argument
		primaryStage.show();            //öffnet das Fenster

    }

    //beim ersten Start festlegen welche Datenbank verwendet wird
    private void datenbankFestlegen(Stage primaryStage) {

        //ist schon eine Datenbank festgelegt, kann sie direkt weiterverwendet werden
        if (DBController.hatDatenbank()) {
            return;
        }

        //Auswahl ob eine neue Datenbank angelegt oder eine vorhandene geöffnet wird
        ButtonType neuButton = new ButtonType("Neue Datenbank anlegen");
        ButtonType oeffnenButton = new ButtonType("Vorhandene öffnen");
        Alert auswahl = new Alert(AlertType.CONFIRMATION, "", neuButton, oeffnenButton);
        auswahl.setTitle("Datenbank");
        auswahl.setHeaderText("Womit möchten Sie arbeiten?");
        Optional<ButtonType> wahl = auswahl.showAndWait();

        if (wahl.isPresent() && wahl.get() == oeffnenButton) {
            //vorhandene Datenbank auswählen und als aktive Datenbank merken
            datenbankWaehlen(primaryStage, "Datenbank öffnen", null);
        } else {
            //neue, leere Datenbank an einem gewählten Ort anlegen
            datenbankWaehlen(primaryStage, "Neue Datenbank anlegen", "wettkampf_db.db");
        }

        //wurde nichts ausgewählt (Dialog abgebrochen), die Standard-Datenbank im Benutzerordner verwenden
        if (!DBController.hatDatenbank()) {
            DBController.setDatenbankPfad(System.getProperty("user.home") + "/wettkampf_db.db");
        }
    }

    //eine Datenbank-Datei auswählen und als aktive Datenbank merken, gibt zurück ob eine gewählt wurde
    private boolean datenbankWaehlen(Stage primaryStage, String titel, String vorschlag) {
        FileChooser dateiAuswahl = new FileChooser();
        dateiAuswahl.setTitle(titel);
        dateiAuswahl.getExtensionFilters().add(new FileChooser.ExtensionFilter("Datenbank-Dateien", "*.db"));

        //für eine neue Datenbank einen Speichern-Dialog mit Namensvorschlag, sonst einen Öffnen-Dialog
        File datei;
        if (vorschlag == null) {
            datei = dateiAuswahl.showOpenDialog(primaryStage);
        } else {
            dateiAuswahl.setInitialFileName(vorschlag);
            datei = dateiAuswahl.showSaveDialog(primaryStage);
        }

        //ohne Auswahl bleibt alles unverändert
        if (datei == null) {
            return false;
        }

        DBController.setDatenbankPfad(datei.getAbsolutePath());
        return true;
    }

    //zur Laufzeit eine andere Datenbank öffnen und die Anzeige darauf umstellen
    private void datenbankWechseln(Stage primaryStage) {

        //ohne Auswahl bleibt die aktuelle Datenbank bestehen
        if (!datenbankWaehlen(primaryStage, "Datenbank öffnen", null)) {
            return;
        }

        //fehlende Tabellen in der gewählten Datenbank anlegen
        controller.createTableIfNotExists();

        //Anzeige auf die neue Datenbank umstellen
        saisonComboAktualisieren();
        mannschaftComboAktualisieren();
        titelAktualisieren(primaryStage);
    }

    //Fenstertitel auf die aktuell geöffnete Datenbank setzen
    private void titelAktualisieren(Stage primaryStage) {
        String dateiname = new File(DBController.getDatenbankPfad()).getName();
        primaryStage.setTitle("Blasrohr-Wettkampf-Manager — " + dateiname);
    }

    //Ergebnisfeld je nach Auswahl füllen und sperren bzw. freigeben
    private void ergebnisAktualisieren() {

        //aktuell gewählten Schützen und Wettkampftag holen
        Schuetze schuetze = schuetzeCombo.getSelectionModel().getSelectedItem();
        Wettkampftage tag = wTageBox.getSelectionModel().getSelectedItem();

        //Überschrift auf den gewählten Schützen beziehen, damit klar ist wessen Ergebnis erfasst wird
        if (schuetze == null) {
            ergebnisText.setText("Ergebnis des Schützen: ");
        } else {
            ergebnisText.setText("Ergebnis von " + schuetze.getVorname() + " " + schuetze.getNachname() + ": ");
        }

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
            begegnungButton.setDisable(true);
        } else {
            aktTagText.setText("Wettkampf am " + tag.getDatum().format(DATUM_FORMAT) + " - " + tag.getAusrichterVerein());
            begegnungButton.setDisable(false);
        }
    }

    /**
     * Begegnungen des aktuell gewählten Wettkampftages in tagAnzeige anzeigen.
     */
    public void begegnungenAnzeigen() {

        //Inhalt vor dem neu-Aufbau löschen
        tagAnzeige.getChildren().clear();

        //Begegnungen auslesen von diesem Tag
        Wettkampftage tag = wTageBox.getSelectionModel().getSelectedItem();
        List<Begegnung> begegnungen = controller.begegnungenAnDiesemTag(tag.getId());

        //Raster, damit Namen, Ergebnisse und Löschen-Button aller Begegnungen sauber in Spalten untereinander stehen
        GridPane raster = new GridPane();
        raster.getStyleClass().add("begegnung-raster"); //Aufrufname für die .css Datei
        tagAnzeige.getChildren().add(raster);

        //Ausrichtung je Spalte: Heim rechtsbündig ans "vs.", Gegner linksbündig, die Ergebnisse um den Doppelpunkt zentriert
        HPos[] spaltenAusrichtung = { HPos.RIGHT, HPos.CENTER, HPos.LEFT, HPos.RIGHT, HPos.CENTER, HPos.LEFT, HPos.CENTER };
        for (HPos ausrichtung : spaltenAusrichtung) {
            ColumnConstraints spalte = new ColumnConstraints();
            spalte.setHalignment(ausrichtung);
            raster.getColumnConstraints().add(spalte);
        }

        //Für jede Begegnung an diesem Tag eine Zeile im Raster anlegen:
        int zeile = 0;
        for(Begegnung b :begegnungen) {

            //Mannschaften holen
            Mannschaft heim = controller.mannschaftMitID(b.getHeim());
            Mannschaft gegner = controller.mannschaftMitID(b.getGegner());

            //Gesamtergebnisse (Summe der besten 3 Schützen) aus der DB holen
            int gesamtHeim = controller.gesamtErgebnisBeste3(heim.getId(), b.getWettkampftag());
            int gesamtGegner = controller.gesamtErgebnisBeste3(gegner.getId(), b.getWettkampftag());

            //Text der als Begegnung angezeigt wird
            Text heimBegegnung = new Text(heim.getName());
            heimBegegnung.getStyleClass().add("Text-begegnung"); //Aufrufname für die .css Datei

            //Text der als Begegnung angezeigt wird
            Text vsBegegnung = new Text("vs.");
            vsBegegnung.getStyleClass().add("Text-begegnung"); //Aufrufname für die .css Datei

            //Text der als Begegnung angezeigt wird
            Text gegnerBegegnung = new Text(gegner.getName());
            gegnerBegegnung.getStyleClass().add("Text-begegnung"); //Aufrufname für die .css Datei

            //Text der als Begegnung angezeigt wird
            Text ergHeimBegegnung = new Text(String.valueOf(gesamtHeim));
            ergHeimBegegnung.getStyleClass().add("Text-begegnung"); //Aufrufname für die .css Datei

            //Text der als Begegnung angezeigt wird
            Text sepBegegnung = new Text(":");
            sepBegegnung.getStyleClass().add("Text-begegnung"); //Aufrufname für die .css Datei

            //Text der als Begegnung angezeigt wird
            Text ergGegnerBegegnung = new Text(String.valueOf(gesamtGegner));
            ergGegnerBegegnung.getStyleClass().add("Text-begegnung"); //Aufrufname für die .css Datei

            //Buton um Begegnung zu löschen
            Button beMinusButton = new Button("-");
            beMinusButton.getStyleClass().add("danger"); //Aufrufname für die .css Datei
            Tooltip tipbeMinus = new Tooltip("Diese Begegnung löschen");
            tipbeMinus.setShowDelay(Duration.millis(300));
            beMinusButton.setTooltip(tipbeMinus);
            beMinusButton.setOnAction(event -> {
                controller.begegnungLöschenWarnen(b.getId());
            });

            //Zeile mittig ausrichten, damit Texte und Löschen-Button auf einer Höhe liegen
            RowConstraints zeilenAusrichtung = new RowConstraints();
            zeilenAusrichtung.setValignment(VPos.CENTER);
            raster.getRowConstraints().add(zeilenAusrichtung);

            //etwas Luft vor dem Ergebnisblock und vor dem Löschen-Button, damit Begegnung und Ergebnis optisch getrennt sind
            GridPane.setMargin(ergHeimBegegnung, new Insets(0, 0, 0, 18));
            GridPane.setMargin(beMinusButton, new Insets(0, 0, 0, 18));

            //Elemente spaltenweise in die aktuelle Zeile setzen
            raster.add(heimBegegnung, 0, zeile);
            raster.add(vsBegegnung, 1, zeile);
            raster.add(gegnerBegegnung, 2, zeile);
            raster.add(ergHeimBegegnung, 3, zeile);
            raster.add(sepBegegnung, 4, zeile);
            raster.add(ergGegnerBegegnung, 5, zeile);
            raster.add(beMinusButton, 6, zeile);

            zeile++;
        }

    }

    /**
     * Saison-ComboBox neu aus der Datenbank laden.
     */
    public void saisonComboAktualisieren() {
        saisonCombo.setItems(FXCollections.observableArrayList(controller.alleSaisons()));
    }

    /**
     * Wettkampftag-ComboBox neu aus der Datenbank laden.
     * @param id id der Saison, deren Wettkampftage geladen werden
     */
    public void wTagComboAktualisieren(String id) {
        wTageBox.setItems(FXCollections.observableArrayList(controller.wettkampftageVonSaison(id)));
    }

    /**
     * Mannschaft-ComboBox neu aus der Datenbank laden.
     */
    public void mannschaftComboAktualisieren() {
        mannschaftCombo.setItems(FXCollections.observableArrayList(controller.alleMannschaften()));
    }

    /**
     * Schützen-ComboBox neu aus der Datenbank laden.
     * @param id id der Mannschaft, deren Schützen geladen werden
     */
    public void schuetzeComboAktualisieren(String id) {
        schuetzeCombo.setItems(FXCollections.observableArrayList(controller.schuetzenVonMannschaft(id)));
    }

    /**
     * Neu angelegte Saison im Hauptfenster auswählen, damit sie sofort sichtbar ist.
     * @param id id der auszuwählenden Saison
     */
    public void saisonAuswaehlen(String id) {
        for (Saison s : saisonCombo.getItems()) {
            if (s.getId().equals(id)) {
                saisonCombo.getSelectionModel().select(s);
                return;
            }
        }
    }

    /**
     * Neu angelegten Wettkampftag im Hauptfenster auswählen, damit er sofort sichtbar ist.
     * @param id id des auszuwählenden Wettkampftages
     */
    public void wTagAuswaehlen(String id) {
        for (Wettkampftage w : wTageBox.getItems()) {
            if (w.getId().equals(id)) {
                wTageBox.getSelectionModel().select(w);
                return;
            }
        }
    }

    /**
     * Neu angelegte Mannschaft im Hauptfenster auswählen, damit sie sofort sichtbar ist.
     * @param id id der auszuwählenden Mannschaft
     */
    public void mannschaftAuswaehlen(String id) {
        for (Mannschaft m : mannschaftCombo.getItems()) {
            if (m.getId().equals(id)) {
                mannschaftCombo.getSelectionModel().select(m);
                return;
            }
        }
    }

    /**
     * Neu angelegten Schützen im Hauptfenster auswählen, damit er sofort sichtbar ist.
     * @param id id des auszuwählenden Schützen
     */
    public void schuetzeAuswaehlen(String id) {
        for (Schuetze s : schuetzeCombo.getItems()) {
            if (s.getId().equals(id)) {
                schuetzeCombo.getSelectionModel().select(s);
                return;
            }
        }
    }
}
