package io.github.manadhion.wettkampf.app;

import java.util.Arrays;
import java.util.Optional;

/** Vergleichbare numerische Anwendungs- oder Releaseversion mit bis zu vier Bestandteilen. */
record Versionsnummer(int haupt, int neben, int korrektur, int build)
        implements Comparable<Versionsnummer> {

    static Optional<Versionsnummer> lesen(String text) {
        if (text == null) return Optional.empty();
        String normalisiert = text.trim();
        if (normalisiert.startsWith("v") || normalisiert.startsWith("V")) {
            normalisiert = normalisiert.substring(1);
        }
        if (!normalisiert.matches("[0-9]+(?:\\.[0-9]+){0,3}")) return Optional.empty();
        try {
            int[] teile = Arrays.stream(normalisiert.split("\\."))
                    .mapToInt(Integer::parseInt).toArray();
            int[] vollstaendig = new int[4];
            System.arraycopy(teile, 0, vollstaendig, 0, teile.length);
            return Optional.of(new Versionsnummer(vollstaendig[0], vollstaendig[1],
                    vollstaendig[2], vollstaendig[3]));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    @Override
    public int compareTo(Versionsnummer andere) {
        int vergleich = Integer.compare(haupt, andere.haupt);
        if (vergleich != 0) return vergleich;
        vergleich = Integer.compare(neben, andere.neben);
        if (vergleich != 0) return vergleich;
        vergleich = Integer.compare(korrektur, andere.korrektur);
        return vergleich != 0 ? vergleich : Integer.compare(build, andere.build);
    }
}
