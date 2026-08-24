package io.github.manadhion.wettkampf.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.prefs.Preferences;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfReader;

import io.github.manadhion.wettkampf.data.Begegnung;
import io.github.manadhion.wettkampf.data.Liga;
import io.github.manadhion.wettkampf.data.Mannschaft;
import io.github.manadhion.wettkampf.data.Saison;
import io.github.manadhion.wettkampf.data.Wettkampftage;

class SaisonPdfLogoTest {

    @TempDir
    Path temporaererOrdner;

    private String vorherigerDatenbankpfad;

    @BeforeEach
    void testdatenbankVorbereiten() {
        vorherigerDatenbankpfad = DBController.getDatenbankPfad();
        DBController.setDatenbankPfad(temporaererOrdner.resolve("saison-pdf.db").toString());
    }

    @AfterEach
    void datenbankpfadWiederherstellen() {
        if (vorherigerDatenbankpfad == null) {
            Preferences.userNodeForPackage(DBController.class).remove("datenbankPfad");
        } else {
            DBController.setDatenbankPfad(vorherigerDatenbankpfad);
        }
    }

    @Test
    void erzeugteErgebnisseitenEnthaltenDasGaulogo() throws Exception {
        LokalerWettkampfDatenService service = new LokalerWettkampfDatenService();
        Controller controller = new Controller(service);
        controller.createTableIfNotExists();

        Saison saison = new Saison(2627);
        Liga liga = new Liga("Gauliga", 1);
        Wettkampftage tag = new Wettkampftage(LocalDate.of(2026, 10, 10), "SG Maßbach", saison.getId());
        Mannschaft heim = new Mannschaft("SG Maßbach", liga.getId());
        Mannschaft gegner = new Mannschaft("SV Beispiel", liga.getId());

        service.saisonAnlegen(saison);
        service.ligaAnlegen(liga);
        service.wettkampftagAnlegen(tag);
        service.mannschaftAnlegen(heim);
        service.mannschaftAnlegen(gegner);
        service.begegnungAnlegen(new Begegnung(heim.getId(), gegner.getId(), tag.getId()));

        Path vorschau = Path.of("target", "saisonpdf-logo-vorschau.pdf").toAbsolutePath();
        Files.createDirectories(vorschau.getParent());
        new SaisonPdf(controller).erstelle(tag, vorschau.toFile());

        assertTrue(Files.size(vorschau) > 1_000);
        PdfReader pdf = new PdfReader(vorschau.toString());
        try {
            assertEquals(2, pdf.getNumberOfPages());
            assertTrue(enthaeltBild(pdf, 1), "Einzelergebnisse müssen das Gaulogo enthalten");
            assertTrue(enthaeltBild(pdf, 2), "Mannschaftsergebnisse müssen das Gaulogo enthalten");
        } finally {
            pdf.close();
        }
    }

    private boolean enthaeltBild(PdfReader pdf, int seite) {
        PdfDictionary ressourcen = pdf.getPageN(seite).getAsDict(PdfName.RESOURCES);
        PdfDictionary xObjekte = ressourcen == null ? null : ressourcen.getAsDict(PdfName.XOBJECT);
        if (xObjekte == null) return false;
        for (PdfName name : xObjekte.getKeys()) {
            PdfObject objekt = PdfReader.getPdfObject(xObjekte.get(name));
            if (objekt instanceof PdfDictionary dictionary
                    && PdfName.IMAGE.equals(dictionary.getAsName(PdfName.SUBTYPE))) {
                return true;
            }
        }
        return false;
    }
}
