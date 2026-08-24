package io.github.manadhion.wettkampf.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;

import org.junit.jupiter.api.Test;

class OnlineApiClientTest {

    @Test
    void meldetSichAnUndSendetTokenNurAnGeschuetzteAnfragen() {
        FakeHttpClient http = new FakeHttpClient();
        http.antworten.add(new FakeAntwort(200,
                "{\"token\":\"sitzung-token\",\"laeuftAb\":\"2026-08-22T03:00:00Z\"}"));
        http.antworten.add(new FakeAntwort(200,
                "[{\"id\":\"saison-id\",\"name\":2627,\"version\":4}]"));
        OnlineApiClient client = new OnlineApiClient(
                URI.create("http://127.0.0.1:8002"), http);

        client.anmelden("online", "nur-fuer-den-test".toCharArray());
        OnlineApi.OnlineSaison saison = client.alleSaisons().getFirst();

        assertEquals("/api/v1/anmeldung", http.anfragen.get(0).uri().getPath());
        assertEquals(Optional.empty(),
                http.anfragen.get(0).headers().firstValue("Authorization"));
        assertEquals("Bearer sitzung-token",
                http.anfragen.get(1).headers().firstValue("Authorization").orElseThrow());
        assertEquals("saison-id", saison.id());
        assertEquals(2627, saison.name());
        assertEquals(4, saison.version());
    }

    @Test
    void liestUndSchreibtVollstaendigenSnapshot() {
        FakeHttpClient http = new FakeHttpClient();
        http.antworten.add(new FakeAntwort(200,
                "{\"token\":\"sitzung-token\",\"laeuftAb\":\"2026-08-22T03:00:00Z\"}"));
        http.antworten.add(new FakeAntwort(200, """
                {"revision":7,"saisons":[{"id":"s1","name":2627,"version":2}],
                 "ligen":[],"altersklassen":[],"mannschaften":[],"schuetzen":[],
                 "wettkampftage":[],"begegnungen":[],"saisonSchuetzen":[],"ergebnisse":[]}
                """));
        http.antworten.add(new FakeAntwort(200, "{\"revision\":8}"));
        OnlineApiClient client = new OnlineApiClient(URI.create("http://127.0.0.1:8002"), http);

        client.anmelden("online", "test-passwort".toCharArray());
        OnlineApi.OnlineSnapshot snapshot = client.snapshotLaden();
        long neueRevision = client.snapshotSpeichern(snapshot);

        assertEquals(1, snapshot.saisons().size());
        assertEquals(7, snapshot.revision());
        assertEquals(8, neueRevision);
        assertEquals("/api/v1/snapshot", http.anfragen.get(1).uri().getPath());
        assertEquals("GET", http.anfragen.get(1).method());
        assertEquals("PUT", http.anfragen.get(2).method());
        assertEquals("Bearer sitzung-token",
                http.anfragen.get(2).headers().firstValue("Authorization").orElseThrow());
        org.junit.jupiter.api.Assertions.assertTrue(
                http.anfragen.get(2).bodyPublisher().orElseThrow().contentLength() > 0);
    }

    @Test
    void erkenntUndErklaertEinenSnapshotVersionskonflikt() {
        FakeHttpClient http = new FakeHttpClient();
        http.antworten.add(new FakeAntwort(200,
                "{\"token\":\"sitzung-token\",\"laeuftAb\":\"2026-08-24T12:00:00Z\"}"));
        http.antworten.add(new FakeAntwort(409,
                "{\"code\":\"VERSION_KONFLIKT\",\"nachricht\":\"Serverstand ist neuer.\"}"));
        OnlineApiClient client = new OnlineApiClient(URI.create("http://127.0.0.1:8002"), http);
        client.anmelden("online", "test-passwort".toCharArray());
        OnlineApi.OnlineSnapshot leer = new OnlineApi.OnlineSnapshot(2, List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());

        OnlineApiException fehler = assertThrows(OnlineApiException.class,
                () -> client.snapshotSpeichern(leer));

        assertTrue(fehler.istVersionskonflikt());
        assertEquals("Serverstand ist neuer.", fehler.getMessage());
    }

    private static final class FakeHttpClient extends HttpClient {
        private final Queue<HttpResponse<String>> antworten = new ArrayDeque<>();
        private final java.util.ArrayList<HttpRequest> anfragen = new java.util.ArrayList<>();

        @Override
        @SuppressWarnings("unchecked")
        public <T> HttpResponse<T> send(HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler) throws IOException {
            anfragen.add(request);
            return (HttpResponse<T>) antworten.remove();
        }

        @Override public Optional<CookieHandler> cookieHandler() { return Optional.empty(); }
        @Override public Optional<Duration> connectTimeout() { return Optional.empty(); }
        @Override public Redirect followRedirects() { return Redirect.NEVER; }
        @Override public Optional<ProxySelector> proxy() { return Optional.empty(); }
        @Override public SSLContext sslContext() { return null; }
        @Override public SSLParameters sslParameters() { return null; }
        @Override public Optional<Authenticator> authenticator() { return Optional.empty(); }
        @Override public Version version() { return Version.HTTP_1_1; }
        @Override public Optional<Executor> executor() { return Optional.empty(); }
        @Override public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request,
                HttpResponse.BodyHandler<T> handler) { throw new UnsupportedOperationException(); }
        @Override public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request,
                HttpResponse.BodyHandler<T> handler,
                HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
            throw new UnsupportedOperationException();
        }
        @Override public WebSocket.Builder newWebSocketBuilder() {
            return super.newWebSocketBuilder();
        }
    }

    private record FakeAntwort(int statusCode, String body) implements HttpResponse<String> {
        @Override public HttpRequest request() { return null; }
        @Override public Optional<HttpResponse<String>> previousResponse() { return Optional.empty(); }
        @Override public HttpHeaders headers() { return HttpHeaders.of(java.util.Map.of(), (a, b) -> true); }
        @Override public Optional<SSLSession> sslSession() { return Optional.empty(); }
        @Override public URI uri() { return URI.create("http://127.0.0.1"); }
        @Override public HttpClient.Version version() { return HttpClient.Version.HTTP_1_1; }
    }
}
