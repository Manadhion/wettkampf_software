package io.github.manadhion.wettkampf.app;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import io.github.manadhion.wettkampf.data.Begegnung;
import io.github.manadhion.wettkampf.data.Ergebnisse;
import io.github.manadhion.wettkampf.data.Liga;
import io.github.manadhion.wettkampf.data.Mannschaft;
import io.github.manadhion.wettkampf.data.Saison;
import io.github.manadhion.wettkampf.data.Schuetze;
import io.github.manadhion.wettkampf.data.TabellenZeile;
import io.github.manadhion.wettkampf.data.Wettkampftage;

/**
 * Erzeugt das Saison-PDF: je Liga die Einzel- und Mannschaftsergebnisse bis zu einem Stichtag.
 */
public class SaisonPdf {

    //Datumsformat für Kopfzeilen und Rundenüberschriften
    private static final DateTimeFormatter DATUM_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    //Schriften für Titel, Untertitel, Abschnitte, Rundenüberschriften, Tabellenkopf und Zellen
    private static final Font TITEL = new Font(Font.HELVETICA, 15, Font.BOLD);
    private static final Font UNTERTITEL = new Font(Font.HELVETICA, 10, Font.NORMAL);
    private static final Font ABSCHNITT = new Font(Font.HELVETICA, 12, Font.BOLD);
    private static final Font RUNDE = new Font(Font.HELVETICA, 10, Font.BOLD);
    private static final Font KOPF = new Font(Font.HELVETICA, 9, Font.BOLD);
    private static final Font ZELLE = new Font(Font.HELVETICA, 9, Font.NORMAL);

    //Hintergrundfarbe für die Kopfzeilen der Tabellen
    private static final Color KOPF_HG = new Color(228, 228, 228);

    //holt die Daten über den Controller
    private Controller controller;

    /**
     * Legt den PDF-Ersteller an.
     * @param controller Controller, über den die Daten geholt werden
     */
    public SaisonPdf(Controller controller) {
        this.controller = controller;
    }

    /**
     * Saisondaten bis zum Stichtag als PDF in die Datei schreiben.
     * @param stichtag Wettkampftag, bis zu dem die Daten ausgegeben werden (einschließlich)
     * @param datei Zieldatei für das PDF
     * @throws DocumentException wenn das PDF nicht aufgebaut werden kann
     * @throws IOException wenn die Datei nicht geschrieben werden kann
     */
    public void erstelle(Wettkampftage stichtag, File datei) throws DocumentException, IOException {

        Saison saison = controller.saisonMitId(stichtag.getSaisonID());
        List<Wettkampftage> tage = tageBisStichtag(stichtag);
        List<Liga> ligen = controller.alleLigen();

        //Querformat, damit die vielen Wettkampftag-Spalten der Einzelergebnisse nebeneinander passen
        try (FileOutputStream os = new FileOutputStream(datei)) {

            Document doc = new Document(PageSize.A4.rotate(), 36, 36, 40, 40);
            PdfWriter.getInstance(doc, os);
            doc.open();

            //ohne Ligen gäbe es nichts zu zeigen und das PDF hätte keine Seite
            if (ligen.isEmpty()) {
                doc.add(new Paragraph("Keine Ligen angelegt.", UNTERTITEL));
                doc.close();
                return;
            }

            //je Liga zuerst die Einzelergebnisse, dann die Mannschaftsergebnisse, jeweils auf eigener Seite
            boolean ersteLiga = true;
            for (Liga liga : ligen) {
                if (!ersteLiga) {
                    doc.newPage();
                }
                ersteLiga = false;

                einzelergebnisse(doc, liga, saison, tage);
                doc.newPage();
                mannschaftsergebnisse(doc, liga, saison, stichtag, tage);
            }

            doc.close();
        }
    }

    //Wettkampftage der Saison bis einschließlich Stichtag, nach Datum aufsteigend sortiert
    private List<Wettkampftage> tageBisStichtag(Wettkampftage stichtag) {

        List<Wettkampftage> tage = new ArrayList<>();
        for (Wettkampftage tag : controller.wettkampftageVonSaison(stichtag.getSaisonID())) {
            if (!tag.getDatum().isAfter(stichtag.getDatum())) {
                tage.add(tag);
            }
        }

        Collections.sort(tage, new Comparator<Wettkampftage>() {
            @Override
            public int compare(Wettkampftage a, Wettkampftage b) {
                return a.getDatum().compareTo(b.getDatum());
            }
        });

        return tage;
    }

    //Blatt "Einzelergebnisse" einer Liga: je Schütze eine Zeile mit Ergebnis je Wettkampftag und Durchschnitt
    private void einzelergebnisse(Document doc, Liga liga, Saison saison, List<Wettkampftage> tage) throws DocumentException {

        doc.add(new Paragraph("Einzelergebnisse " + liga.getLigaName(), TITEL));
        doc.add(untertitel(saison, tage));

        //Spalten: Verein, Name, Durchschnitt und je Wettkampftag eine RWK-Spalte
        PdfPTable tabelle = new PdfPTable(3 + tage.size());
        tabelle.setWidthPercentage(100);
        tabelle.setSpacingBefore(8);
        tabelle.setWidths(einzelBreiten(tage.size()));
        tabelle.setHeaderRows(1);

        //Kopfzeile
        tabelle.addCell(kopf("Verein", Element.ALIGN_LEFT));
        tabelle.addCell(kopf("Name", Element.ALIGN_LEFT));
        tabelle.addCell(kopf("Durchschnitt", Element.ALIGN_RIGHT));
        int rwk = 1;
        for (int i = 0; i < tage.size(); i++) {
            tabelle.addCell(kopf("RWK " + rwk, Element.ALIGN_RIGHT));
            rwk++;
        }

        //je Mannschaft der Liga deren Schützen als Zeilen
        for (Mannschaft m : controller.alleMannschaften()) {
            if (!m.getKlasse().equals(liga.getId())) {
                continue;
            }
            for (Schuetze s : controller.schuetzenVonMannschaft(m.getId())) {

                tabelle.addCell(zelle(m.getName(), Element.ALIGN_LEFT));
                tabelle.addCell(zelle(s.getNachname() + " " + s.getVorname(), Element.ALIGN_LEFT));

                //Ergebnisse des Schützen je Wettkampftag einsammeln, dabei Summe und Anzahl für den Durchschnitt bilden
                int summe = 0;
                int anzahl = 0;
                List<String> werte = new ArrayList<>();
                for (Wettkampftage tag : tage) {
                    Ergebnisse e = controller.ergebnisFuer(s.getId(), tag.getId());
                    if (e == null) {
                        //an diesem Tag nicht geschossen, Zelle bleibt leer
                        werte.add("");
                    } else {
                        werte.add(String.valueOf(e.getErgebnis()));
                        summe += e.getErgebnis();
                        anzahl++;
                    }
                }

                //Durchschnitt nur über die tatsächlich geschossenen Wettkampftage
                String durchschnitt = (anzahl == 0) ? "" : String.format(Locale.GERMANY, "%.1f", summe / (double) anzahl);
                tabelle.addCell(zelle(durchschnitt, Element.ALIGN_RIGHT));

                for (String wert : werte) {
                    tabelle.addCell(zelle(wert, Element.ALIGN_RIGHT));
                }
            }
        }

        doc.add(tabelle);
    }

    //relative Spaltenbreiten der Einzelergebnis-Tabelle: Verein und Name breiter, RWK-Spalten schmal
    private float[] einzelBreiten(int anzahlTage) {
        float[] breiten = new float[3 + anzahlTage];
        breiten[0] = 2.4f;  //Verein
        breiten[1] = 3.0f;  //Name
        breiten[2] = 1.5f;  //Durchschnitt
        for (int i = 3; i < breiten.length; i++) {
            breiten[i] = 1.1f;  //RWK-Spalten
        }
        return breiten;
    }

    //Blatt "Mannschaftsergebnisse" einer Liga: Tabelle bis Stichtag und darunter die absolvierten Wettkämpfe
    private void mannschaftsergebnisse(Document doc, Liga liga, Saison saison, Wettkampftage stichtag, List<Wettkampftage> tage) throws DocumentException {

        doc.add(new Paragraph("Mannschaftsergebnisse " + liga.getLigaName(), TITEL));
        doc.add(untertitel(saison, tage));

        //Tabelle: Rang, Mannschaft, Mannschaftspunkte, Ringe – bis zum Stichtag berechnet
        MannschaftstabelleRechner rechner = new MannschaftstabelleRechner(controller);
        List<TabellenZeile> zeilen = rechner.berechne(liga, saison.getId(), stichtag.getDatum());

        PdfPTable tabelle = new PdfPTable(4);
        tabelle.setWidthPercentage(65);
        tabelle.setHorizontalAlignment(Element.ALIGN_LEFT);
        tabelle.setSpacingBefore(8);
        tabelle.setWidths(new float[] { 1f, 4f, 2f, 2f });
        tabelle.setHeaderRows(1);

        tabelle.addCell(kopf("Rang", Element.ALIGN_LEFT));
        tabelle.addCell(kopf("Mannschaft", Element.ALIGN_LEFT));
        tabelle.addCell(kopf("Mannschaftspunkte", Element.ALIGN_CENTER));
        tabelle.addCell(kopf("Ringe", Element.ALIGN_RIGHT));

        int rang = 1;
        for (TabellenZeile z : zeilen) {
            tabelle.addCell(zelle(rang + ".", Element.ALIGN_LEFT));
            tabelle.addCell(zelle(z.getMannschaft(), Element.ALIGN_LEFT));
            tabelle.addCell(zelle(z.getPunkte() + " : " + z.getGegenPunkte(), Element.ALIGN_CENTER));
            tabelle.addCell(zelle(String.valueOf(z.getRinge()), Element.ALIGN_RIGHT));
            rang++;
        }
        doc.add(tabelle);

        //Abschnitt mit den absolvierten Wettkämpfen, eine Runde je Wettkampftag
        Paragraph abschnitt = new Paragraph("Absolvierte Wettkämpfe", ABSCHNITT);
        abschnitt.setSpacingBefore(16);
        abschnitt.setSpacingAfter(4);
        doc.add(abschnitt);

        int runde = 1;
        for (Wettkampftage tag : tage) {
            runde(doc, liga, tag, runde);
            runde++;
        }
    }

    //eine Runde: Überschrift mit Ausrichter und Datum, darunter die Begegnungen der Liga und die freien Mannschaften
    private void runde(Document doc, Liga liga, Wettkampftage tag, int nummer) throws DocumentException {

        Paragraph kopfzeile = new Paragraph(
            "Runde " + nummer + "   ·   " + tag.getAusrichterVerein() + "   ·   " + tag.getDatum().format(DATUM_FORMAT),
            RUNDE);
        kopfzeile.setSpacingBefore(10);
        kopfzeile.setSpacingAfter(2);
        doc.add(kopfzeile);

        //Begegnungen dieser Liga am Wettkampftag sammeln und merken, welche Mannschaften ein Spiel hatten
        List<Begegnung> begegnungen = new ArrayList<>();
        Set<String> beteiligt = new LinkedHashSet<>();
        for (Begegnung b : controller.begegnungenAnDiesemTag(tag.getId())) {
            Mannschaft heim = controller.mannschaftMitID(b.getHeim());
            //eine Begegnung gehört zur Liga, wenn die Heimmannschaft in dieser Liga spielt
            if (heim == null || !heim.getKlasse().equals(liga.getId())) {
                continue;
            }
            begegnungen.add(b);
            beteiligt.add(b.getHeim());
            beteiligt.add(b.getGegner());
        }

        //ohne eingetragene Begegnungen einen Hinweis ausgeben
        if (begegnungen.isEmpty()) {
            doc.add(new Paragraph("noch keine Begegnungen", ZELLE));
            return;
        }

        //Tabelle der Begegnungen: Heimmannschaft, Ringe, Gegnermannschaft, Begegnungspunkte
        PdfPTable tabelle = new PdfPTable(4);
        tabelle.setWidthPercentage(65);
        tabelle.setHorizontalAlignment(Element.ALIGN_LEFT);
        tabelle.setWidths(new float[] { 3.2f, 1.6f, 3.2f, 1.4f });

        for (Begegnung b : begegnungen) {
            Mannschaft heim = controller.mannschaftMitID(b.getHeim());
            Mannschaft gegner = controller.mannschaftMitID(b.getGegner());

            //Gesamtergebnisse als Summe der besten 3 Schützen
            int ringeHeim = controller.gesamtErgebnisBeste3(heim.getId(), tag.getId());
            int ringeGegner = controller.gesamtErgebnisBeste3(gegner.getId(), tag.getId());

            tabelle.addCell(zelle(heim.getName(), Element.ALIGN_RIGHT));
            tabelle.addCell(zelle(ringeHeim + " : " + ringeGegner, Element.ALIGN_CENTER));
            tabelle.addCell(zelle(gegner.getName(), Element.ALIGN_LEFT));
            tabelle.addCell(zelle(begegnungspunkte(ringeHeim, ringeGegner), Element.ALIGN_CENTER));
        }
        doc.add(tabelle);

        //Mannschaften der Liga, die an diesem Tag kein Spiel hatten (Freilos)
        List<String> frei = new ArrayList<>();
        for (Mannschaft m : controller.alleMannschaften()) {
            if (m.getKlasse().equals(liga.getId()) && !beteiligt.contains(m.getId())) {
                frei.add(m.getName());
            }
        }
        if (!frei.isEmpty()) {
            doc.add(new Paragraph("Frei: " + String.join(", ", frei), ZELLE));
        }
    }

    //Begegnungspunkte nach der 2/1/0-Regel; solange nicht beide Mannschaften geschossen haben, gibt es noch keine Punkte
    private String begegnungspunkte(int ringeHeim, int ringeGegner) {
        if (ringeHeim == 0 || ringeGegner == 0) {
            return "–";
        }
        if (ringeHeim > ringeGegner) {
            return "2 : 0";
        }
        if (ringeHeim < ringeGegner) {
            return "0 : 2";
        }
        return "1 : 1";
    }

    //Untertitel mit Saison und dem Stand bis zum letzten berücksichtigten Wettkampftag
    private Paragraph untertitel(Saison saison, List<Wettkampftage> tage) {
        String saisonName = (saison == null) ? "?" : saison.toString();
        String stand = tage.isEmpty() ? "" : tage.get(tage.size() - 1).getDatum().format(DATUM_FORMAT);
        return new Paragraph("Saison " + saisonName + "   ·   Stand: " + stand, UNTERTITEL);
    }

    //eine Kopf-Zelle der Tabelle
    private PdfPCell kopf(String text, int ausrichtung) {
        PdfPCell zelle = new PdfPCell(new Phrase(text, KOPF));
        zelle.setHorizontalAlignment(ausrichtung);
        zelle.setBackgroundColor(KOPF_HG);
        zelle.setPadding(4);
        return zelle;
    }

    //eine normale Zelle der Tabelle
    private PdfPCell zelle(String text, int ausrichtung) {
        PdfPCell zelle = new PdfPCell(new Phrase(text, ZELLE));
        zelle.setHorizontalAlignment(ausrichtung);
        zelle.setPadding(3);
        return zelle;
    }

}
