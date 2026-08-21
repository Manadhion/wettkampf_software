package io.github.manadhion.wettkampf.app;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import com.sun.jna.platform.win32.Crypt32Util;

/** Speichert den Online-Zugang lokal; das Passwort wird mit Windows-DPAPI geschuetzt. */
public final class OnlineZugangKonfiguration {

    private static final String NAME = "online.name";
    private static final String PASSWORT = "online.passwort.dpapi";
    private static final byte[] ENTROPIE =
            "io.github.manadhion.wettkampf.online".getBytes(StandardCharsets.UTF_8);
    private static final Path DATEI = konfigurationsDatei();
    private static final PasswortSchutz DPAPI = new WindowsDpapiSchutz();

    private OnlineZugangKonfiguration() {
    }

    /** Laedt den gespeicherten Online-Zugang, sofern bereits einer vorhanden ist. */
    public static Optional<Zugang> laden() {
        return laden(DATEI, DPAPI);
    }

    /** Speichert Kennung und Passwort erst nach einer erfolgreichen Online-Anmeldung. */
    public static void speichern(String name, char[] passwort) {
        speichern(DATEI, name, passwort, DPAPI);
    }

    /** Pfad der benutzerbezogenen, nicht geheimen Konfigurationsdatei. */
    public static Path getDatei() {
        return DATEI;
    }

    static Optional<Zugang> laden(Path datei, PasswortSchutz schutz) {
        Objects.requireNonNull(datei);
        Objects.requireNonNull(schutz);
        if (!Files.isRegularFile(datei)) {
            return Optional.empty();
        }

        Properties werte = new Properties();
        try (InputStream eingabe = Files.newInputStream(datei)) {
            werte.load(eingabe);
            String name = werte.getProperty(NAME, "").trim();
            String verschluesselt = werte.getProperty(PASSWORT, "").trim();
            if (name.isEmpty() || verschluesselt.isEmpty()) {
                return Optional.empty();
            }
            byte[] geheim = Base64.getDecoder().decode(verschluesselt);
            byte[] klartext = null;
            try {
                klartext = schutz.entschluesseln(geheim);
                return Optional.of(new Zugang(name, zeichen(klartext)));
            } finally {
                Arrays.fill(geheim, (byte) 0);
                if (klartext != null) Arrays.fill(klartext, (byte) 0);
            }
        } catch (IOException | RuntimeException e) {
            throw new KonfigurationException("Der gespeicherte Online-Zugang konnte nicht gelesen werden.", e);
        }
    }

    static void speichern(Path datei, String name, char[] passwort, PasswortSchutz schutz) {
        Objects.requireNonNull(datei);
        Objects.requireNonNull(passwort);
        Objects.requireNonNull(schutz);
        String kennung = Objects.requireNonNull(name).trim();
        if (kennung.isEmpty() || passwort.length == 0) {
            throw new IllegalArgumentException("Online-Kennung und Passwort duerfen nicht leer sein.");
        }

        byte[] klartext = utf8(passwort);
        byte[] geheim = null;
        Path temporaer = null;
        try {
            geheim = schutz.verschluesseln(klartext);
            Properties werte = new Properties();
            werte.setProperty(NAME, kennung);
            werte.setProperty(PASSWORT, Base64.getEncoder().encodeToString(geheim));

            Path ordner = datei.toAbsolutePath().getParent();
            Files.createDirectories(ordner);
            temporaer = Files.createTempFile(ordner, "online-", ".tmp");
            try (OutputStream ausgabe = Files.newOutputStream(temporaer)) {
                werte.store(ausgabe, "Wettkampf Online-Zugang; Passwort mit Windows-DPAPI geschuetzt");
            }
            try {
                Files.move(temporaer, datei, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporaer, datei, StandardCopyOption.REPLACE_EXISTING);
            }
            temporaer = null;
        } catch (IOException | RuntimeException e) {
            throw new KonfigurationException("Der Online-Zugang konnte nicht gespeichert werden.", e);
        } finally {
            Arrays.fill(klartext, (byte) 0);
            if (geheim != null) Arrays.fill(geheim, (byte) 0);
            if (temporaer != null) {
                try {
                    Files.deleteIfExists(temporaer);
                } catch (IOException ignoriert) {
                    // Die eigentliche Ursache wird bereits gemeldet.
                }
            }
        }
    }

    private static Path konfigurationsDatei() {
        String appData = System.getenv("APPDATA");
        Path basis = appData == null || appData.isBlank()
                ? Path.of(System.getProperty("user.home"), ".wettkampf")
                : Path.of(appData, "Wettkampf");
        return basis.resolve("online.properties");
    }

    private static byte[] utf8(char[] zeichen) {
        ByteBuffer puffer = StandardCharsets.UTF_8.encode(CharBuffer.wrap(zeichen));
        byte[] ergebnis = new byte[puffer.remaining()];
        puffer.get(ergebnis);
        if (puffer.hasArray()) Arrays.fill(puffer.array(), (byte) 0);
        return ergebnis;
    }

    private static char[] zeichen(byte[] utf8) {
        CharBuffer puffer = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(utf8));
        char[] ergebnis = new char[puffer.remaining()];
        puffer.get(ergebnis);
        if (puffer.hasArray()) Arrays.fill(puffer.array(), '\0');
        return ergebnis;
    }

    interface PasswortSchutz {
        byte[] verschluesseln(byte[] klartext);
        byte[] entschluesseln(byte[] geheimtext);
    }

    static final class WindowsDpapiSchutz implements PasswortSchutz {
        @Override
        public byte[] verschluesseln(byte[] klartext) {
            return Crypt32Util.cryptProtectData(klartext, ENTROPIE, 0,
                    "Wettkampf Online-Zugang", null);
        }

        @Override
        public byte[] entschluesseln(byte[] geheimtext) {
            return Crypt32Util.cryptUnprotectData(geheimtext, ENTROPIE, 0, null);
        }
    }

    /** Im Speicher gehaltene Zugangsdaten, deren Passwort nach Gebrauch geloescht wird. */
    public static final class Zugang implements AutoCloseable {
        private final String name;
        private final char[] passwort;

        private Zugang(String name, char[] passwort) {
            this.name = name;
            this.passwort = passwort;
        }

        public String name() {
            return name;
        }

        public char[] passwort() {
            return passwort.clone();
        }

        @Override
        public void close() {
            Arrays.fill(passwort, '\0');
        }
    }

    /** Meldet eine nicht les- oder schreibbare Zugangskonfiguration. */
    public static final class KonfigurationException extends RuntimeException {
        public KonfigurationException(String nachricht, Throwable ursache) {
            super(nachricht, ursache);
        }
    }
}
