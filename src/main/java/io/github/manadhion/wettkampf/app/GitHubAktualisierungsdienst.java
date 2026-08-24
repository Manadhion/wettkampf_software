package io.github.manadhion.wettkampf.app;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/** Prüft ohne Zugangsdaten das neueste öffentliche GitHub-Release der Anwendung. */
public final class GitHubAktualisierungsdienst {

    private static final URI NEUESTES_RELEASE = URI.create(
            "https://api.github.com/repos/Manadhion/wettkampf_software/releases/latest");
    private static final String RELEASE_PFAD =
            "/Manadhion/wettkampf_software/releases/";

    private final HttpClient httpClient;
    private final URI releaseApi;

    public GitHubAktualisierungsdienst() {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build(),
                NEUESTES_RELEASE);
    }

    GitHubAktualisierungsdienst(HttpClient httpClient, URI releaseApi) {
        this.httpClient = httpClient;
        this.releaseApi = releaseApi;
    }

    /**
     * Liefert eine neuere veröffentlichte Version. Netzwerk- oder Formatfehler werden als
     * leeres Ergebnis behandelt, damit die Updateprüfung den Wettkampfbetrieb nie blockiert.
     */
    public CompletableFuture<Optional<Aktualisierung>> neuereVersionPruefen() {
        HttpRequest anfrage = HttpRequest.newBuilder(releaseApi)
                .timeout(Duration.ofSeconds(8))
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .header("User-Agent", "Blasrohr-Wettkampf-Manager/" + Anwendungsversion.aktuell())
                .GET().build();
        return httpClient.sendAsync(anfrage, HttpResponse.BodyHandlers.ofString())
                .thenApply(antwort -> auswerten(antwort.statusCode(), antwort.body(),
                        Anwendungsversion.aktuell()))
                .exceptionally(fehler -> Optional.empty());
    }

    static Optional<Aktualisierung> auswerten(int status, String json,
            String aktuelleVersion) {
        if (status != 200) return Optional.empty();
        try {
            Map<String, Object> release = Json.alsObjekt(Json.lesen(json));
            String tag = Json.text(release, "tag_name");
            Optional<Versionsnummer> neu = Versionsnummer.lesen(tag);
            Optional<Versionsnummer> aktuell = Versionsnummer.lesen(aktuelleVersion);
            if (neu.isEmpty() || aktuell.isEmpty() || neu.get().compareTo(aktuell.get()) <= 0) {
                return Optional.empty();
            }

            URI releaseAdresse = sichereReleaseAdresse(Json.text(release, "html_url"));
            if (releaseAdresse == null) return Optional.empty();
            URI downloadAdresse = installerAdresse(release).orElse(releaseAdresse);
            return Optional.of(new Aktualisierung(tagOhneV(tag), downloadAdresse,
                    releaseAdresse));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    private static Optional<URI> installerAdresse(Map<String, Object> release) {
        Object assetsWert = release.get("assets");
        if (!(assetsWert instanceof List<?> assets)) return Optional.empty();
        return assets.stream().filter(Map.class::isInstance).map(Json::alsObjekt)
                .filter(asset -> {
                    String name = Json.text(asset, "name");
                    return name != null && name.startsWith("Blasrohr-Wettkampf-Manager-")
                            && name.endsWith(".exe");
                })
                .map(asset -> sichereDownloadAdresse(Json.text(asset, "browser_download_url")))
                .filter(java.util.Objects::nonNull).findFirst();
    }

    private static URI sichereReleaseAdresse(String text) {
        return sichereGitHubAdresse(text, RELEASE_PFAD + "tag/");
    }

    private static URI sichereDownloadAdresse(String text) {
        return sichereGitHubAdresse(text, RELEASE_PFAD + "download/");
    }

    private static URI sichereGitHubAdresse(String text, String erwarteterPfad) {
        if (text == null) return null;
        try {
            URI adresse = URI.create(text);
            return "https".equalsIgnoreCase(adresse.getScheme())
                    && "github.com".equalsIgnoreCase(adresse.getHost())
                    && adresse.getPath().startsWith(erwarteterPfad)
                    && adresse.getUserInfo() == null ? adresse : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String tagOhneV(String tag) {
        return tag != null && (tag.startsWith("v") || tag.startsWith("V"))
                ? tag.substring(1) : tag;
    }

    public record Aktualisierung(String version, URI downloadAdresse,
            URI releaseAdresse) { }
}
