package io.github.manadhion.wettkampf.view;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.github.manadhion.wettkampf.app.Controller;
import io.github.manadhion.wettkampf.app.MannschaftstabelleRechner;
import io.github.manadhion.wettkampf.data.Begegnung;
import io.github.manadhion.wettkampf.data.Ergebnisse;
import io.github.manadhion.wettkampf.data.Liga;
import io.github.manadhion.wettkampf.data.Mannschaft;
import io.github.manadhion.wettkampf.data.Saison;
import io.github.manadhion.wettkampf.data.Schuetze;
import io.github.manadhion.wettkampf.data.TabellenZeile;
import io.github.manadhion.wettkampf.data.Wettkampftage;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

//Vollbild-Anzeige für den Beamer, rotiert automatisch durch alle Ligen
public class BeamerView extends Stage {

    //Parameter
    private Controller controller;

    //Uhrzeit- und Datums-Format für die Kopfzeile
    private static final DateTimeFormatter UHR_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATUM_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    //wie lange eine Liga angezeigt wird, bevor zur nächsten gewechselt wird
    private static final Duration LIGA_WECHSEL = Duration.seconds(12);

    //Zeiten für das Auto-Scrollen der Einzelergebnisse: Halten oben/unten und Dauer des Scrollens
    private static final Duration SCROLL_HALT = Duration.seconds(2);
    private static final Duration SCROLL_DAUER = Duration.seconds(8);

    //angezeigter Wettkampftag, dessen Saison und alle Ligen
    private Wettkampftage tag;
    private Saison saison;
    private List<Liga> ligen;
    private int aktuelleLiga = 0;

    //Elemente die sich ständig aktualisieren
    private Text ligaText = new Text();
    private Text uhrText = new Text();
    private Text pauseText = new Text();
    private BorderPane root = new BorderPane();

    //Timelines für Rotation und Uhr
    private Timeline rotation;
    private Timeline uhr;

    //ScrollPane der Einzelergebnisse und die Timeline, die sie automatisch scrollt
    private ScrollPane einzelScrollPane;
    private Timeline einzelScroll;

    //ob die Rotation gerade pausiert ist (Leertaste)
    private boolean pausiert = false;

    //Controller von aussen setzen
    public void setController(Controller controller) {
        this.controller = controller;
    }

    //Beamer-Anzeige für einen Wettkampftag aufbauen und starten
    public void beamerStarten(Wettkampftage tag) {

        this.tag = tag;
        this.saison = controller.saisonMitId(tag.getSaisonID()); //Saison aus dem Tag ableiten
        this.ligen = controller.alleLigen();

        setTitle("Beamer-Anzeige");
        initStyle(StageStyle.DECORATED); //kein Fensterrahmen UNDACORATED - zu Debugzwecken hier nur DECORATED
        setFullScreen(false);               //Vollbild true - zu debug Zwecken hier false
        setFullScreenExitHint("");         //den Standard-Hinweis ausblenden

        root.getStyleClass().add("beamer-root"); //Aufrufname für die .css Datei

        //Kopfzeile aufbauen
        root.setTop(kopfzeileAufbauen());

        //erste Liga anzeigen (oder Hinweis, wenn es keine gibt)
        seiteAktualisieren();

        //Timelines starten
        timelinesStarten();

        Scene scene = new Scene(root);

        //beamer.css einbinden
        scene.getStylesheets().add(getClass().getResource("/io/github/manadhion/wettkampf/view/beamer.css").toExternalForm());

        //Tastatursteuerung: ESC schließt, Pfeile blättern, Leertaste pausiert
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                if (e.getCode() == KeyCode.ESCAPE) {
                    BeamerView.this.close();
                } else if (e.getCode() == KeyCode.RIGHT) {
                    naechsteLiga();
                    rotationNachManuell();
                } else if (e.getCode() == KeyCode.LEFT) {
                    vorherigeLiga();
                    rotationNachManuell();
                } else if (e.getCode() == KeyCode.SPACE) {
                    pauseUmschalten();
                }
            }
        });

        //beim Schließen alle Timelines stoppen, damit nichts im Hintergrund weiterläuft
        setOnHidden(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                timelinesStoppen();
            }
        });

        setScene(scene);
    }

    //Kopfzeile mit Saison, Liga-Name + Position und Uhr
    private Region kopfzeileAufbauen() {

        //obere Zeile: Saison + Wettkampftag links, Uhr rechts
        String saisonName = (saison == null) ? "?" : saison.toString();
        Text saisonText = new Text("Saison " + saisonName + "   ·   Wettkampftag " + tag.getDatum().format(DATUM_FORMAT));
        saisonText.getStyleClass().add("beamer-saison"); //Aufrufname für die .css Datei

        uhrText.getStyleClass().add("beamer-uhr"); //Aufrufname für die .css Datei

        //Pause-Anzeige, nur sichtbar wenn die Rotation pausiert ist
        pauseText.getStyleClass().add("beamer-pause"); //Aufrufname für die .css Datei

        //Abstandhalter schiebt Pause-Anzeige und Uhr nach rechts
        Region abstand = new Region();
        HBox.setHgrow(abstand, Priority.ALWAYS);

        HBox zeileOben = new HBox(saisonText, abstand, pauseText, uhrText);
        zeileOben.setAlignment(Pos.CENTER);

        //Liga-Name groß und zentriert
        ligaText.getStyleClass().add("beamer-liga"); //Aufrufname für die .css Datei
        HBox ligaZeile = new HBox(ligaText);
        ligaZeile.setAlignment(Pos.CENTER);

        VBox kopf = new VBox(zeileOben, ligaZeile);
        kopf.getStyleClass().add("beamer-kopf"); //Aufrufname für die .css Datei
        return kopf;
    }

    //die Anzeige auf die aktuelle Liga setzen
    private void seiteAktualisieren() {

        //keine Ligen vorhanden -> Hinweis anzeigen
        if (ligen.isEmpty()) {
            ligaText.setText("Keine Ligen angelegt");
            Text hinweis = new Text("Bitte zuerst Ligen und Mannschaften anlegen.");
            hinweis.getStyleClass().add("beamer-platzhalter"); //Aufrufname für die .css Datei
            HBox box = new HBox(hinweis);
            box.setAlignment(Pos.CENTER);
            einzelScrollStoppen();
            einzelScrollPane = null;
            root.setCenter(box);
            return;
        }

        Liga liga = ligen.get(aktuelleLiga);

        //Kopfzeile: Liga-Name + Position in der Rotation
        ligaText.setText(liga.getLigaName() + "   (" + (aktuelleLiga + 1) + " / " + ligen.size() + ")");

        //Inhaltsbereich neu aufbauen (setzt dabei einzelScrollPane neu)
        root.setCenter(inhaltFuerLiga(liga));

        //Auto-Scrollen der Einzelergebnisse für die neue Tabelle starten
        einzelScrollStarten();
    }

    //Inhaltsbereich für eine Liga: Einzelergebnisse, Mannschaftstabelle und Begegnungen
    private Region inhaltFuerLiga(Liga liga) {

        //links: Einzelergebnisse aller Schützen an diesem Wettkampftag
        VBox einzel = einzelergebnisseBereich(liga);

        //rechts oben: Mannschaftstabelle der Saison
        VBox tabelle = mannschaftstabelleBereich(liga);

        //rechts unten: Begegnungen an diesem Wettkampftag
        VBox begegnung = begegnungenBereich(liga);

        VBox rechteSpalte = new VBox(tabelle, begegnung);
        rechteSpalte.getStyleClass().add("beamer-spalte"); //Aufrufname für die .css Datei
        VBox.setVgrow(tabelle, Priority.ALWAYS);
        VBox.setVgrow(begegnung, Priority.ALWAYS);
        HBox.setHgrow(rechteSpalte, Priority.ALWAYS);

        HBox.setHgrow(einzel, Priority.ALWAYS);

        HBox inhalt = new HBox(einzel, rechteSpalte);
        inhalt.getStyleClass().add("beamer-inhalt"); //Aufrufname für die .css Datei
        return inhalt;
    }

    //Bereich mit der Mannschaftstabelle einer Liga über die ganze Saison
    private VBox mannschaftstabelleBereich(Liga liga) {

        Label titelLabel = new Label("Mannschaftstabelle Saison");
        titelLabel.getStyleClass().add("beamer-bereich-titel"); //Aufrufname für die .css Datei

        VBox box = new VBox(titelLabel);
        box.getStyleClass().add("beamer-bereich"); //Aufrufname für die .css Datei

        //Tabelle der Liga über alle Wettkampftage der Saison berechnen lassen
        MannschaftstabelleRechner rechner = new MannschaftstabelleRechner(controller);
        List<TabellenZeile> zeilen = rechner.berechne(liga, tag.getSaisonID());

        //keine Mannschaften in dieser Liga -> Hinweis anzeigen
        if (zeilen.isEmpty()) {
            Text hinweis = new Text("Keine Mannschaften in dieser Liga");
            hinweis.getStyleClass().add("beamer-platzhalter"); //Aufrufname für die .css Datei
            box.getChildren().add(hinweis);
            return box;
        }

        //Tabelle mit Spalten: Platz, Mannschaft, Punkte, Ringe
        GridPane tabelle = new GridPane();
        tabelle.getStyleClass().add("beamer-tabelle"); //Aufrufname für die .css Datei

        //Kopfzeile der Tabelle
        tabelle.add(kopfZelle("Pl."), 0, 0);
        tabelle.add(kopfZelle("Mannschaft"), 1, 0);
        tabelle.add(kopfZelle("Punkte"), 2, 0);
        tabelle.add(kopfZelle("Ringe"), 3, 0);

        //je Mannschaft eine Zeile in der sortierten Reihenfolge
        int zeileNr = 1;
        for (TabellenZeile z : zeilen) {
            tabelle.add(zelle(zeileNr + "."), 0, zeileNr);
            tabelle.add(zelle(z.getMannschaft()), 1, zeileNr);
            tabelle.add(ergebnisZelle(z.getPunkte() + ":" + z.getGegenPunkte()), 2, zeileNr);
            tabelle.add(zelle(String.valueOf(z.getRinge())), 3, zeileNr);
            zeileNr++;
        }

        box.getChildren().add(tabelle);
        return box;
    }

    //Bereich mit den Begegnungen einer Liga an diesem Wettkampftag
    private VBox begegnungenBereich(Liga liga) {

        Label titelLabel = new Label("Begegnungen");
        titelLabel.getStyleClass().add("beamer-bereich-titel"); //Aufrufname für die .css Datei

        VBox box = new VBox(titelLabel);
        box.getStyleClass().add("beamer-bereich"); //Aufrufname für die .css Datei

        //alle Begegnungen dieses Wettkampftages holen
        List<Begegnung> begegnungen = controller.begegnungenAnDiesemTag(tag.getId());

        //merkt sich, ob für diese Liga überhaupt eine Begegnung dabei war
        boolean gefunden = false;

        for (Begegnung b : begegnungen) {

            //Mannschaften der Begegnung holen
            Mannschaft heim = controller.mannschaftMitID(b.getHeim());
            Mannschaft gegner = controller.mannschaftMitID(b.getGegner());

            //nur Begegnungen dieser Liga anzeigen (beide Mannschaften sind in derselben Liga)
            if (!heim.getKlasse().equals(liga.getId())) {
                continue;
            }
            gefunden = true;

            //Gesamtergebnisse (Summe der besten 3 Schützen) holen
            int gesamtHeim = controller.gesamtErgebnisBeste3(heim.getId(), tag.getId());
            int gesamtGegner = controller.gesamtErgebnisBeste3(gegner.getId(), tag.getId());

            //eine Zeile je Begegnung: Heimmannschaft - Ergebnis : Ergebnis - Gegnermannschaft
            Text heimText = new Text(heim.getName());
            heimText.getStyleClass().add("beamer-zeile"); //Aufrufname für die .css Datei

            Text ergebnisText = new Text("   " + gesamtHeim + " : " + gesamtGegner + "   ");
            ergebnisText.getStyleClass().add("beamer-zeile-ergebnis"); //Aufrufname für die .css Datei

            Text gegnerText = new Text(gegner.getName());
            gegnerText.getStyleClass().add("beamer-zeile"); //Aufrufname für die .css Datei

            HBox zeile = new HBox(heimText, ergebnisText, gegnerText);
            zeile.setAlignment(Pos.CENTER_LEFT);
            zeile.getStyleClass().add("beamer-begegnung-zeile"); //Aufrufname für die .css Datei
            box.getChildren().add(zeile);
        }

        //keine Begegnung in dieser Liga -> Hinweis anzeigen
        if (!gefunden) {
            Text hinweis = new Text("Keine Begegnungen");
            hinweis.getStyleClass().add("beamer-platzhalter"); //Aufrufname für die .css Datei
            box.getChildren().add(hinweis);
        }

        return box;
    }

    //Bereich mit den Einzelergebnissen aller Schützen einer Liga an diesem Wettkampftag
    private VBox einzelergebnisseBereich(Liga liga) {

        Label titelLabel = new Label("Einzelergebnisse");
        titelLabel.getStyleClass().add("beamer-bereich-titel"); //Aufrufname für die .css Datei

        VBox box = new VBox(titelLabel);
        box.getStyleClass().add("beamer-bereich"); //Aufrufname für die .css Datei

        //solange noch keine scrollbare Tabelle gebaut ist, gibt es nichts zu scrollen
        einzelScrollPane = null;

        //alle Schützen der Liga mit ihrem heutigen Ergebnis sammeln
        List<EinzelZeile> zeilen = new ArrayList<>();
        for (Mannschaft m : controller.alleMannschaften()) {

            //nur Mannschaften dieser Liga berücksichtigen
            if (!m.getKlasse().equals(liga.getId())) {
                continue;
            }

            for (Schuetze s : controller.schuetzenVonMannschaft(m.getId())) {
                Ergebnisse e = controller.ergebnisFuer(s.getId(), tag.getId());
                Integer wert = (e == null) ? null : e.getErgebnis(); //null wenn der Schütze heute noch kein Ergebnis hat
                zeilen.add(new EinzelZeile(s.getVorname() + " " + s.getNachname(), m.getName(), wert));
            }
        }

        //keine Schützen in dieser Liga -> Hinweis anzeigen
        if (zeilen.isEmpty()) {
            Text hinweis = new Text("Keine Schützen in dieser Liga");
            hinweis.getStyleClass().add("beamer-platzhalter"); //Aufrufname für die .css Datei
            box.getChildren().add(hinweis);
            return box;
        }

        //nach Ergebnis absteigend sortieren, Schützen ohne Ergebnis ans Ende
        Collections.sort(zeilen, new Comparator<EinzelZeile>() {
            @Override
            public int compare(EinzelZeile a, EinzelZeile b) {
                if (a.ergebnis == null && b.ergebnis == null) {
                    return 0;
                }
                if (a.ergebnis == null) {
                    return 1;
                }
                if (b.ergebnis == null) {
                    return -1;
                }
                return b.ergebnis - a.ergebnis;
            }
        });

        //Tabelle mit Spalten: Platz, Name, Mannschaft, Ergebnis
        GridPane tabelle = new GridPane();
        tabelle.getStyleClass().add("beamer-tabelle"); //Aufrufname für die .css Datei

        //Kopfzeile der Tabelle
        tabelle.add(kopfZelle("Pl."), 0, 0);
        tabelle.add(kopfZelle("Name"), 1, 0);
        tabelle.add(kopfZelle("Mannschaft"), 2, 0);
        tabelle.add(kopfZelle("Ergebnis"), 3, 0);

        //je Schütze eine Zeile, Platznummer nur für Schützen mit Ergebnis
        int zeileNr = 1;
        int platz = 1;
        for (EinzelZeile z : zeilen) {

            String platzAnzeige = (z.ergebnis == null) ? "" : platz + ".";
            String ergebnisAnzeige = (z.ergebnis == null) ? "—" : String.valueOf(z.ergebnis);

            tabelle.add(zelle(platzAnzeige), 0, zeileNr);
            tabelle.add(zelle(z.name), 1, zeileNr);
            tabelle.add(zelle(z.mannschaft), 2, zeileNr);
            tabelle.add(ergebnisZelle(ergebnisAnzeige), 3, zeileNr);

            if (z.ergebnis != null) {
                platz++;
            }
            zeileNr++;
        }

        //Tabelle scrollbar machen, damit lange Listen automatisch durchlaufen können
        ScrollPane scroll = new ScrollPane(tabelle);
        scroll.getStyleClass().add("beamer-scroll"); //Aufrufname für die .css Datei
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); //keine sichtbaren Scrollbalken
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        einzelScrollPane = scroll;
        box.getChildren().add(scroll);
        return box;
    }

    //eine Kopf-Zelle der Tabelle
    private Text kopfZelle(String text) {
        Text t = new Text(text);
        t.getStyleClass().add("beamer-tabelle-kopf"); //Aufrufname für die .css Datei
        return t;
    }

    //eine normale Zelle der Tabelle
    private Text zelle(String text) {
        Text t = new Text(text);
        t.getStyleClass().add("beamer-zeile"); //Aufrufname für die .css Datei
        return t;
    }

    //eine Ergebnis-Zelle der Tabelle (hervorgehoben)
    private Text ergebnisZelle(String text) {
        Text t = new Text(text);
        t.getStyleClass().add("beamer-zeile-ergebnis"); //Aufrufname für die .css Datei
        return t;
    }

    //zur nächsten Liga wechseln (umlaufend)
    private void naechsteLiga() {
        if (ligen.isEmpty()) {
            return;
        }
        aktuelleLiga = (aktuelleLiga + 1) % ligen.size();
        seiteAktualisieren();
    }

    //zur vorherigen Liga wechseln (umlaufend)
    private void vorherigeLiga() {
        if (ligen.isEmpty()) {
            return;
        }
        aktuelleLiga = (aktuelleLiga - 1 + ligen.size()) % ligen.size();
        seiteAktualisieren();
    }

    //nach manuellem Blättern die volle Anzeigedauer neu beginnen, solange nicht pausiert ist
    private void rotationNachManuell() {
        if (rotation != null && !pausiert) {
            rotation.playFromStart();
        }
    }

    //Rotation pausieren bzw. fortsetzen
    private void pauseUmschalten() {
        pausiert = !pausiert;
        if (pausiert) {
            rotation.pause();
            pauseText.setText("⏸ Pause   ");
        } else {
            rotation.play();
            pauseText.setText("");
        }
    }

    //alle Timelines anlegen und starten
    private void timelinesStarten() {

        //Rotation: alle paar Sekunden zur nächsten Liga
        rotation = new Timeline(new KeyFrame(LIGA_WECHSEL, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                naechsteLiga();
            }
        }));
        rotation.setCycleCount(Animation.INDEFINITE);
        rotation.play();

        //Uhr: jede Sekunde aktualisieren
        uhrText.setText(LocalTime.now().format(UHR_FORMAT));
        uhr = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                uhrText.setText(LocalTime.now().format(UHR_FORMAT));
            }
        }));
        uhr.setCycleCount(Animation.INDEFINITE);
        uhr.play();
    }

    //Auto-Scrollen der Einzelergebnis-Tabelle starten: oben halten, runter scrollen, unten halten, dann von vorne
    private void einzelScrollStarten() {

        einzelScrollStoppen();

        //ohne scrollbare Tabelle gibt es nichts zu scrollen
        if (einzelScrollPane == null) {
            return;
        }

        ScrollPane sp = einzelScrollPane;

        //Zeitpunkte für die Stützstellen des Scrollwerts (0 = oben, 1 = unten)
        Duration scrollEnde = SCROLL_HALT.add(SCROLL_DAUER);
        Duration untenEnde = scrollEnde.add(SCROLL_HALT);

        einzelScroll = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(sp.vvalueProperty(), 0)),
            new KeyFrame(SCROLL_HALT, new KeyValue(sp.vvalueProperty(), 0)),   //oben halten
            new KeyFrame(scrollEnde, new KeyValue(sp.vvalueProperty(), 1)),    //langsam nach unten
            new KeyFrame(untenEnde, new KeyValue(sp.vvalueProperty(), 1))      //unten halten
        );

        //am Ende jedes Durchlaufs springt die Timeline zurück auf den Anfang (oben)
        einzelScroll.setCycleCount(Animation.INDEFINITE);
        einzelScroll.play();
    }

    //Auto-Scrollen stoppen
    private void einzelScrollStoppen() {
        if (einzelScroll != null) {
            einzelScroll.stop();
            einzelScroll = null;
        }
    }

    //alle Timelines stoppen
    private void timelinesStoppen() {
        if (rotation != null) {
            rotation.stop();
        }
        if (uhr != null) {
            uhr.stop();
        }
        einzelScrollStoppen();
    }

    //eine Zeile der Einzelergebnis-Tabelle: Schützenname, Mannschaft und Ergebnis (null wenn noch keins)
    private static class EinzelZeile {

        private String name;
        private String mannschaft;
        private Integer ergebnis;

        private EinzelZeile(String name, String mannschaft, Integer ergebnis) {
            this.name = name;
            this.mannschaft = mannschaft;
            this.ergebnis = ergebnis;
        }
    }

}
