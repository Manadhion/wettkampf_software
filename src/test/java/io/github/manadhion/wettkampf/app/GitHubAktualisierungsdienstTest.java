package io.github.manadhion.wettkampf.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GitHubAktualisierungsdienstTest {

    @Test
    void erkenntNeuereNumerischeVersionen() {
        Versionsnummer aktuell = Versionsnummer.lesen("1.3").orElseThrow();

        assertTrue(Versionsnummer.lesen("v1.3.1").orElseThrow().compareTo(aktuell) > 0);
        assertTrue(Versionsnummer.lesen("2.0").orElseThrow().compareTo(aktuell) > 0);
        assertEquals(0, Versionsnummer.lesen("1.3.0").orElseThrow().compareTo(aktuell));
        assertTrue(Versionsnummer.lesen("1.2.9").orElseThrow().compareTo(aktuell) < 0);
        assertTrue(Versionsnummer.lesen("1.3-beta").isEmpty());
    }

    @Test
    void liefertFuerNeuesReleaseDenDirektenInstallerDownload() {
        String json = """
                {
                  "tag_name": "v1.4.0",
                  "html_url": "https://github.com/Manadhion/wettkampf_software/releases/tag/v1.4.0",
                  "assets": [{
                    "name": "Blasrohr-Wettkampf-Manager-1.4.0.exe",
                    "browser_download_url": "https://github.com/Manadhion/wettkampf_software/releases/download/v1.4.0/Blasrohr-Wettkampf-Manager-1.4.0.exe"
                  }]
                }
                """;

        GitHubAktualisierungsdienst.Aktualisierung aktualisierung =
                GitHubAktualisierungsdienst.auswerten(200, json, "1.3.0").orElseThrow();

        assertEquals("1.4.0", aktualisierung.version());
        assertTrue(aktualisierung.downloadAdresse().toString().endsWith("1.4.0.exe"));
    }

    @Test
    void ignoriertAlteFehlerhafteOderUmgeleiteteReleaseAntworten() {
        String aktuell = """
                {"tag_name":"v1.3.0","html_url":
                "https://github.com/Manadhion/wettkampf_software/releases/tag/v1.3.0","assets":[]}
                """;
        String fremd = """
                {"tag_name":"v9.0.0","html_url":"https://example.org/releases/tag/v9.0.0",
                "assets":[]}
                """;

        assertTrue(GitHubAktualisierungsdienst.auswerten(200, aktuell, "1.3.0").isEmpty());
        assertTrue(GitHubAktualisierungsdienst.auswerten(200, fremd, "1.3.0").isEmpty());
        assertTrue(GitHubAktualisierungsdienst.auswerten(500, "{}", "1.3.0").isEmpty());
        assertTrue(GitHubAktualisierungsdienst.auswerten(200, "kein json", "1.3.0").isEmpty());
    }

    @Test
    void buildSchreibtDiePomVersionInDieAnwendung() {
        assertEquals("1.3.0", Anwendungsversion.aktuell());
    }
}
