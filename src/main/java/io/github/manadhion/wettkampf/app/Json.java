package io.github.manadhion.wettkampf.app;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Kleine JSON-Unterstützung für die dependency-freie Desktop-API. */
final class Json {

    private Json() {
    }

    static Object lesen(String text) {
        Parser parser = new Parser(text);
        Object wert = parser.wert();
        parser.leerraum();
        if (!parser.amEnde()) {
            throw new OnlineApiException("Die Serverantwort enthält unerwartete Zeichen.");
        }
        return wert;
    }

    static String schreiben(Object wert) {
        if (wert == null) return "null";
        if (wert instanceof String text) return '"' + text(text) + '"';
        if (wert instanceof Number || wert instanceof Boolean) return wert.toString();
        if (wert instanceof Map<?, ?> objekt) {
            StringBuilder json = new StringBuilder("{");
            boolean erstes = true;
            for (Map.Entry<?, ?> eintrag : objekt.entrySet()) {
                if (!erstes) json.append(',');
                erstes = false;
                json.append(schreiben(eintrag.getKey().toString())).append(':')
                        .append(schreiben(eintrag.getValue()));
            }
            return json.append('}').toString();
        }
        if (wert instanceof Iterable<?> liste) {
            StringBuilder json = new StringBuilder("[");
            boolean erstes = true;
            for (Object eintrag : liste) {
                if (!erstes) json.append(',');
                erstes = false;
                json.append(schreiben(eintrag));
            }
            return json.append(']').toString();
        }
        throw new IllegalArgumentException("Nicht unterstützter JSON-Wert: " + wert.getClass());
    }

    static Map<String, Object> objekt(Object... paare) {
        Map<String, Object> objekt = new LinkedHashMap<>();
        for (int i = 0; i < paare.length; i += 2) {
            objekt.put((String) paare[i], paare[i + 1]);
        }
        return objekt;
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> alsObjekt(Object wert) {
        return (Map<String, Object>) wert;
    }

    @SuppressWarnings("unchecked")
    static List<Object> alsListe(Object wert) {
        return (List<Object>) wert;
    }

    static String text(Map<String, Object> objekt, String name) {
        Object wert = objekt.get(name);
        return wert == null ? null : (String) wert;
    }

    static int ganzzahl(Map<String, Object> objekt, String name) {
        return ((Number) objekt.get(name)).intValue();
    }

    static long langeZahl(Map<String, Object> objekt, String name) {
        return ((Number) objekt.get(name)).longValue();
    }

    static boolean wahrheitswert(Map<String, Object> objekt, String name) {
        return (Boolean) objekt.get(name);
    }

    private static String text(String text) {
        StringBuilder json = new StringBuilder();
        for (char zeichen : text.toCharArray()) {
            switch (zeichen) {
                case '"' -> json.append("\\\"");
                case '\\' -> json.append("\\\\");
                case '\b' -> json.append("\\b");
                case '\f' -> json.append("\\f");
                case '\n' -> json.append("\\n");
                case '\r' -> json.append("\\r");
                case '\t' -> json.append("\\t");
                default -> {
                    if (zeichen < 0x20) json.append("\\u%04x".formatted((int) zeichen));
                    else json.append(zeichen);
                }
            }
        }
        return json.toString();
    }

    private static final class Parser {
        private final String text;
        private int position;

        private Parser(String text) { this.text = text; }

        private Object wert() {
            leerraum();
            if (amEnde()) fehler();
            return switch (text.charAt(position)) {
                case '{' -> objekt();
                case '[' -> liste();
                case '"' -> zeichenkette();
                case 't' -> literal("true", true);
                case 'f' -> literal("false", false);
                case 'n' -> literal("null", null);
                default -> zahl();
            };
        }

        private Map<String, Object> objekt() {
            position++;
            Map<String, Object> objekt = new LinkedHashMap<>();
            leerraum();
            if (nehmen('}')) return objekt;
            do {
                leerraum();
                String name = zeichenkette();
                leerraum();
                erwarten(':');
                objekt.put(name, wert());
                leerraum();
            } while (nehmen(','));
            erwarten('}');
            return objekt;
        }

        private List<Object> liste() {
            position++;
            List<Object> liste = new ArrayList<>();
            leerraum();
            if (nehmen(']')) return liste;
            do {
                liste.add(wert());
                leerraum();
            } while (nehmen(','));
            erwarten(']');
            return liste;
        }

        private String zeichenkette() {
            erwarten('"');
            StringBuilder wert = new StringBuilder();
            while (!amEnde()) {
                char zeichen = text.charAt(position++);
                if (zeichen == '"') return wert.toString();
                if (zeichen != '\\') {
                    wert.append(zeichen);
                    continue;
                }
                if (amEnde()) fehler();
                char escape = text.charAt(position++);
                switch (escape) {
                    case '"', '\\', '/' -> wert.append(escape);
                    case 'b' -> wert.append('\b');
                    case 'f' -> wert.append('\f');
                    case 'n' -> wert.append('\n');
                    case 'r' -> wert.append('\r');
                    case 't' -> wert.append('\t');
                    case 'u' -> {
                        if (position + 4 > text.length()) fehler();
                        wert.append((char) Integer.parseInt(text.substring(position, position + 4), 16));
                        position += 4;
                    }
                    default -> fehler();
                }
            }
            return fehler();
        }

        private Number zahl() {
            int start = position;
            if (nehmen('-')) { }
            while (!amEnde() && Character.isDigit(text.charAt(position))) position++;
            boolean dezimal = false;
            if (nehmen('.')) {
                dezimal = true;
                while (!amEnde() && Character.isDigit(text.charAt(position))) position++;
            }
            if (!amEnde() && (text.charAt(position) == 'e' || text.charAt(position) == 'E')) {
                dezimal = true;
                position++;
                if (!amEnde() && (text.charAt(position) == '+' || text.charAt(position) == '-')) position++;
                while (!amEnde() && Character.isDigit(text.charAt(position))) position++;
            }
            try {
                String wert = text.substring(start, position);
                return dezimal ? Double.valueOf(wert) : Long.valueOf(wert);
            } catch (NumberFormatException e) {
                return fehler();
            }
        }

        private Object literal(String literal, Object wert) {
            if (!text.startsWith(literal, position)) return fehler();
            position += literal.length();
            return wert;
        }

        private void leerraum() {
            while (!amEnde() && Character.isWhitespace(text.charAt(position))) position++;
        }

        private boolean nehmen(char zeichen) {
            if (!amEnde() && text.charAt(position) == zeichen) {
                position++;
                return true;
            }
            return false;
        }

        private void erwarten(char zeichen) {
            if (!nehmen(zeichen)) fehler();
        }

        private boolean amEnde() { return position >= text.length(); }

        private <T> T fehler() {
            throw new OnlineApiException("Die Serverantwort hat kein gültiges JSON-Format.");
        }
    }
}
