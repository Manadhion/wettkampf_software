package io.github.manadhion.wettkampf.app;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** Technischer HTTP-Zugang zur Wettkampf-API. */
public final class OnlineApiClient implements OnlineApi {

    private final URI apiBasis;
    private final HttpClient httpClient;
    private String token;

    public OnlineApiClient(URI serverAdresse) {
        this(serverAdresse, HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8)).build());
    }

    OnlineApiClient(URI serverAdresse, HttpClient httpClient) {
        this.apiBasis = apiBasis(serverAdresse);
        this.httpClient = httpClient;
    }

    @Override
    public void anmelden(String name, char[] passwort) {
        Map<String, Object> antwort = objekt(senden("POST", "anmeldung",
                Json.objekt("name", name, "passwort", new String(passwort)), false));
        token = Json.text(antwort, "token");
        if (token == null || token.isBlank()) {
            throw new OnlineApiException("Die Anmeldeantwort enthält kein Sitzungstoken.");
        }
    }

    @Override
    public OnlineSnapshot snapshotLaden() {
        Map<String, Object> o = objekt(senden("GET", "snapshot", null, true));
        return new OnlineSnapshot(
                Json.langeZahl(o, "revision"),
                liste(o, "saisons", this::saison),
                liste(o, "ligen", this::liga),
                liste(o, "altersklassen", this::altersklasse),
                liste(o, "mannschaften", this::mannschaft),
                liste(o, "schuetzen", this::schuetze),
                liste(o, "wettkampftage", this::wettkampftag),
                liste(o, "begegnungen", this::begegnung),
                liste(o, "saisonSchuetzen", this::saisonSchuetze),
                liste(o, "ergebnisse", this::ergebnis));
    }

    @Override
    public long snapshotSpeichern(OnlineSnapshot snapshot) {
        Map<String, Object> antwort = objekt(senden("PUT", "snapshot", Json.objekt(
                "revision", snapshot.revision(),
                "saisons", jsonListe(snapshot.saisons(), OnlineApiClient::saisonJson),
                "ligen", jsonListe(snapshot.ligen(), OnlineApiClient::ligaJson),
                "altersklassen", jsonListe(snapshot.altersklassen(), OnlineApiClient::altersklasseJson),
                "mannschaften", jsonListe(snapshot.mannschaften(), OnlineApiClient::mannschaftJson),
                "schuetzen", jsonListe(snapshot.schuetzen(), OnlineApiClient::schuetzeJson),
                "wettkampftage", jsonListe(snapshot.wettkampftage(), OnlineApiClient::wettkampftagJson),
                "begegnungen", jsonListe(snapshot.begegnungen(), OnlineApiClient::begegnungJson),
                "saisonSchuetzen", jsonListe(snapshot.saisonSchuetzen(), OnlineApiClient::saisonSchuetzeJson),
                "ergebnisse", jsonListe(snapshot.ergebnisse(), OnlineApiClient::ergebnisJson)), true));
        return Json.langeZahl(antwort, "revision");
    }

    @Override
    public void statusPruefen() {
        senden("GET", "status", null, false);
    }

    @Override
    public List<OnlineSaison> alleSaisons() {
        return liste("saisons", this::saison);
    }

    @Override
    public OnlineSaison saisonAnlegen(int name) {
        return saison(objekt(senden("POST", "saisons", Json.objekt("name", name), true)));
    }

    @Override
    public OnlineSaison saisonAktualisieren(String id, int name, long version) {
        return saison(objekt(senden("PUT", "saisons/" + q(id),
                Json.objekt("name", name, "version", version), true)));
    }

    @Override
    public void saisonLoeschen(String id, long version) {
        senden("DELETE", "saisons/" + q(id) + "?version=" + version, null, true);
    }

    @Override
    public List<OnlineLiga> ligen(String saisonId) {
        return liste("ligen" + optional("saisonId", saisonId), this::liga);
    }

    @Override
    public OnlineLiga liga(String id) {
        return liga(objekt(senden("GET", "ligen/" + q(id), null, true)));
    }

    @Override
    public int naechsteLigaRangfolge() {
        return Json.ganzzahl(objekt(senden("GET", "ligen/naechste-rangfolge", null, true)),
                "wert");
    }

    @Override public void ligaAnlegen(OnlineLiga w) { posten("ligen", ligaJson(w)); }
    @Override public void ligaAktualisieren(OnlineLiga w) { put("ligen/" + q(w.id()), ligaJson(w)); }
    @Override public void ligaLoeschen(String id) { loeschen("ligen/" + q(id)); }

    @Override
    public List<OnlineAltersklasse> altersklassen() {
        return liste("altersklassen", this::altersklasse);
    }

    @Override public void altersklasseAnlegen(OnlineAltersklasse w) { posten("altersklassen", altersklasseJson(w)); }
    @Override public void altersklasseAktualisieren(OnlineAltersklasse w) { put("altersklassen/" + q(w.id()), altersklasseJson(w)); }
    @Override public void altersklasseLoeschen(String id) { loeschen("altersklassen/" + q(id)); }

    @Override
    public List<OnlineMannschaft> mannschaften(String saisonId) {
        return liste("mannschaften" + optional("saisonId", saisonId), this::mannschaft);
    }

    @Override
    public OnlineMannschaft mannschaft(String id) {
        return mannschaft(objekt(senden("GET", "mannschaften/" + q(id), null, true)));
    }

    @Override public void mannschaftAnlegen(OnlineMannschaft w) { posten("mannschaften", mannschaftJson(w)); }
    @Override public void mannschaftAktualisieren(OnlineMannschaft w) { put("mannschaften/" + q(w.id()), mannschaftJson(w)); }
    @Override public void mannschaftLoeschen(String id) { loeschen("mannschaften/" + q(id)); }

    @Override
    public List<OnlineSchuetze> schuetzen(String mannschaftId) {
        return liste("schuetzen?mannschaftId=" + q(mannschaftId), this::schuetze);
    }

    @Override public void schuetzeAnlegen(OnlineSchuetze w) { posten("schuetzen", schuetzeJson(w)); }
    @Override public void schuetzeAktualisieren(OnlineSchuetze w) { put("schuetzen/" + q(w.id()), schuetzeJson(w)); }
    @Override public void schuetzeLoeschen(String id) { loeschen("schuetzen/" + q(id)); }

    @Override
    public List<OnlineWettkampftag> wettkampftage(String saisonId) {
        return liste("wettkampftage" + optional("saisonId", saisonId), this::wettkampftag);
    }

    @Override public void wettkampftagAnlegen(OnlineWettkampftag w) { posten("wettkampftage", wettkampftagJson(w)); }
    @Override public void wettkampftagAktualisieren(OnlineWettkampftag w) { put("wettkampftage/" + q(w.id()), wettkampftagJson(w)); }
    @Override public void wettkampftagLoeschen(String id) { loeschen("wettkampftage/" + q(id)); }

    @Override
    public List<OnlineBegegnung> begegnungen(String wettkampftagId) {
        return liste("begegnungen?wettkampftagId=" + q(wettkampftagId), this::begegnung);
    }

    @Override
    public boolean begegnungExistiert(String tagId, String a, String b) {
        return Json.wahrheitswert(objekt(senden("GET", "begegnungen/existiert?wettkampftagId="
                + q(tagId) + "&mannschaftA=" + q(a) + "&mannschaftB=" + q(b), null, true)),
                "neu");
    }

    @Override public void begegnungAnlegen(OnlineBegegnung w) { posten("begegnungen", begegnungJson(w)); }
    @Override public void begegnungLoeschen(String id) { loeschen("begegnungen/" + q(id)); }

    @Override
    public OnlineErgebnis ergebnis(String schuetzeId, String wettkampftagId) {
        String json = senden("GET", "ergebnisse?schuetzeId=" + q(schuetzeId)
                + "&wettkampftagId=" + q(wettkampftagId), null, true);
        return json.isBlank() ? null : ergebnis(objekt(json));
    }

    @Override
    public boolean ergebnisSpeichern(OnlineErgebnis wert) {
        return Json.wahrheitswert(objekt(senden("PUT", "ergebnisse",
                ergebnisJson(wert), true)), "neu");
    }

    @Override
    public int gesamtErgebnis(String mannschaftId, String wettkampftagId) {
        return Json.ganzzahl(objekt(senden("GET", "ergebnisse/gesamt?mannschaftId="
                + q(mannschaftId) + "&wettkampftagId=" + q(wettkampftagId), null, true)),
                "wert");
    }

    @Override
    public List<OnlineSaisonSchuetze> saisonSchuetzen(String saisonId, String mannschaftId) {
        return liste("saison-schuetzen?saisonId=" + q(saisonId) + "&mannschaftId="
                + q(mannschaftId), this::saisonSchuetze);
    }

    @Override
    public OnlineSaisonSchuetze saisonSchuetze(String saisonId, String schuetzeId) {
        String json = senden("GET", "saison-schuetzen/eintrag?saisonId=" + q(saisonId)
                + "&schuetzeId=" + q(schuetzeId), null, true);
        return json.isBlank() ? null : saisonSchuetze(objekt(json));
    }

    @Override
    public void saisonSchuetzeSpeichern(OnlineSaisonSchuetze wert) {
        put("saison-schuetzen", saisonSchuetzeJson(wert));
    }

    @Override
    public void close() {
        if (token != null) {
            try {
                senden("POST", "abmeldung", null, true);
            } catch (OnlineApiException ignoriert) {
                // Beim Beenden ist eine nicht erreichbare Abmeldung unkritisch;
                // die Serversitzung laeuft selbststaendig ab.
            } finally {
                token = null;
            }
        }
    }

    private void posten(String pfad, Map<String, Object> json) { senden("POST", pfad, json, true); }
    private void put(String pfad, Map<String, Object> json) { senden("PUT", pfad, json, true); }
    private void loeschen(String pfad) { senden("DELETE", pfad, null, true); }

    private <T> List<T> liste(String pfad, Function<Map<String, Object>, T> mapper) {
        return Json.alsListe(Json.lesen(senden("GET", pfad, null, true))).stream()
                .map(Json::alsObjekt).map(mapper).toList();
    }

    private String senden(String methode, String pfad, Map<String, Object> json,
            boolean anmeldungErforderlich) {
        HttpRequest.Builder anfrage = HttpRequest.newBuilder(apiBasis.resolve(pfad))
                .timeout(Duration.ofSeconds(15)).header("Accept", "application/json");
        String inhalt = json == null ? null : Json.schreiben(json);
        if (inhalt != null) anfrage.header("Content-Type", "application/json; charset=UTF-8");
        if (anmeldungErforderlich) {
            if (token == null) throw new OnlineApiException("Es besteht keine Online-Anmeldung.");
            anfrage.header("Authorization", "Bearer " + token);
        }
        anfrage.method(methode, inhalt == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(inhalt));
        try {
            HttpResponse<String> antwort = httpClient.send(anfrage.build(),
                    HttpResponse.BodyHandlers.ofString());
            if (antwort.statusCode() < 200 || antwort.statusCode() >= 300) {
                String nachricht = fehlerNachricht(antwort);
                throw new OnlineApiException(nachricht, null, antwort.statusCode() >= 500,
                        antwort.statusCode());
            }
            return antwort.body() == null ? "" : antwort.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OnlineApiException("Die Serveranfrage wurde unterbrochen.", e, true);
        } catch (IOException e) {
            throw new OnlineApiException("Der Online-Server ist nicht erreichbar.", e, true);
        }
    }

    private String fehlerNachricht(HttpResponse<String> antwort) {
        if (antwort.statusCode() == 401) return "Online-Anmeldung fehlgeschlagen.";
        String inhalt = antwort.body();
        if (inhalt != null && !inhalt.isBlank()) {
            try {
                String nachricht = Json.text(Json.alsObjekt(Json.lesen(inhalt)), "nachricht");
                if (nachricht != null && !nachricht.isBlank()) return nachricht;
            } catch (RuntimeException ignoriert) {
                // Bei einer nicht JSON-formatierten Proxyantwort folgt die HTTP-Meldung.
            }
        }
        return "Der Server antwortete mit HTTP " + antwort.statusCode() + ".";
    }

    private static URI apiBasis(URI adresse) {
        String text = adresse.toString();
        if (!text.endsWith("/")) text += "/";
        if (!adresse.getPath().endsWith("/api/v1") && !adresse.getPath().endsWith("/api/v1/")) {
            text += "api/v1/";
        }
        return URI.create(text);
    }

    private static String q(String wert) {
        return URLEncoder.encode(wert, StandardCharsets.UTF_8);
    }

    private static String optional(String name, String wert) {
        return wert == null ? "" : "?" + name + "=" + q(wert);
    }

    private static Map<String, Object> objekt(String json) {
        return Json.alsObjekt(Json.lesen(json));
    }

    private OnlineSaison saison(Map<String, Object> o) {
        return new OnlineSaison(Json.text(o, "id"), Json.ganzzahl(o, "name"),
                Json.langeZahl(o, "version"));
    }
    private OnlineLiga liga(Map<String, Object> o) { return new OnlineLiga(Json.text(o,"id"),Json.text(o,"name"),Json.ganzzahl(o,"rangfolge")); }
    private OnlineAltersklasse altersklasse(Map<String, Object> o) { return new OnlineAltersklasse(Json.text(o,"id"),Json.text(o,"name")); }
    private OnlineMannschaft mannschaft(Map<String, Object> o) { return new OnlineMannschaft(Json.text(o,"id"),Json.text(o,"name"),Json.text(o,"klasse"),Json.text(o,"ligaName")); }
    private OnlineSchuetze schuetze(Map<String, Object> o) { return new OnlineSchuetze(Json.text(o,"id"),Json.text(o,"vorname"),Json.text(o,"nachname"),Json.text(o,"mannschaftId"),Json.text(o,"altersklasseId")); }
    private OnlineWettkampftag wettkampftag(Map<String, Object> o) { return new OnlineWettkampftag(Json.text(o,"id"),LocalDate.parse(Json.text(o,"datum")),Json.text(o,"ausrichterverein"),Json.text(o,"saisonId")); }
    private OnlineBegegnung begegnung(Map<String, Object> o) { return new OnlineBegegnung(Json.text(o,"id"),Json.text(o,"heim"),Json.text(o,"gegner"),Json.text(o,"wettkampftagId"),Json.text(o,"liga"),Json.text(o,"ligaName"),Json.text(o,"heimName"),Json.text(o,"gegnerName")); }
    private OnlineErgebnis ergebnis(Map<String, Object> o) { return new OnlineErgebnis(Json.text(o,"id"),Json.text(o,"schuetzeId"),Json.text(o,"wettkampftagId"),Json.ganzzahl(o,"wert")); }
    private OnlineSaisonSchuetze saisonSchuetze(Map<String, Object> o) { return new OnlineSaisonSchuetze(Json.text(o,"saisonId"),Json.text(o,"schuetzeId"),Json.text(o,"vorname"),Json.text(o,"nachname"),Json.text(o,"mannschaftId"),Json.text(o,"mannschaftName"),Json.text(o,"altersklasseId"),Json.text(o,"altersklasseName")); }

    private <T> List<T> liste(Map<String, Object> objekt, String name,
            Function<Map<String, Object>, T> mapper) {
        return Json.alsListe(objekt.get(name)).stream().map(Json::alsObjekt).map(mapper).toList();
    }

    private static <T> List<Map<String, Object>> jsonListe(List<T> werte,
            Function<T, Map<String, Object>> mapper) {
        return werte.stream().map(mapper).toList();
    }

    private static Map<String,Object> saisonJson(OnlineSaison w) { return Json.objekt("id",w.id(),"name",w.name(),"version",w.version()); }
    private static Map<String,Object> ligaJson(OnlineLiga w) { return Json.objekt("id",w.id(),"name",w.name(),"rangfolge",w.rangfolge()); }
    private static Map<String,Object> altersklasseJson(OnlineAltersklasse w) { return Json.objekt("id",w.id(),"name",w.name()); }
    private static Map<String,Object> mannschaftJson(OnlineMannschaft w) { return Json.objekt("id",w.id(),"name",w.name(),"klasse",w.klasse(),"ligaName",w.ligaName()); }
    private static Map<String,Object> schuetzeJson(OnlineSchuetze w) { return Json.objekt("id",w.id(),"vorname",w.vorname(),"nachname",w.nachname(),"mannschaftId",w.mannschaftId(),"altersklasseId",w.altersklasseId()); }
    private static Map<String,Object> wettkampftagJson(OnlineWettkampftag w) { return Json.objekt("id",w.id(),"datum",w.datum().toString(),"ausrichterverein",w.ausrichterverein(),"saisonId",w.saisonId()); }
    private static Map<String,Object> begegnungJson(OnlineBegegnung w) { return Json.objekt("id",w.id(),"heim",w.heim(),"gegner",w.gegner(),"wettkampftagId",w.wettkampftagId(),"liga",w.liga(),"ligaName",w.ligaName(),"heimName",w.heimName(),"gegnerName",w.gegnerName()); }
    private static Map<String,Object> ergebnisJson(OnlineErgebnis w) { return Json.objekt("id",w.id(),"schuetzeId",w.schuetzeId(),"wettkampftagId",w.wettkampftagId(),"wert",w.wert()); }
    private static Map<String,Object> saisonSchuetzeJson(OnlineSaisonSchuetze w) { return Json.objekt("saisonId",w.saisonId(),"schuetzeId",w.schuetzeId(),"vorname",w.vorname(),"nachname",w.nachname(),"mannschaftId",w.mannschaftId(),"mannschaftName",w.mannschaftName(),"altersklasseId",w.altersklasseId(),"altersklasseName",w.altersklasseName()); }
}
